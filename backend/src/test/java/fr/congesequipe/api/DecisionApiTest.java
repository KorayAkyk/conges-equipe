package fr.congesequipe.api;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.congesequipe.persistance.Equipe;
import fr.congesequipe.persistance.Utilisateur;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Validation et refus par le manager ou les RH, et avertissement de sous-effectif. */
class DecisionApiTest extends ApiTestBase {

    Utilisateur mona;
    Utilisateur alice;
    Utilisateur bruno;
    Utilisateur chloe;
    Utilisateur paul;
    Utilisateur rose;

    @BeforeEach
    void preparer() {
        Equipe dev = fx.equipe("Equipe dev");
        mona = fx.manager("mona", dev);
        alice = fx.employe("alice", dev);
        bruno = fx.employe("bruno", dev);
        chloe = fx.employe("chloe", dev);
        Equipe support = fx.equipe("Equipe support");
        paul = fx.manager("paul", support);
        rose = fx.rh("rose");
    }

    @Test
    void leManagerValideUneDemandeDeSonEquipe() throws Exception {
        long id = deposer(fx.jeton(alice), "CONGES_PAYES", "2026-12-07", "2026-12-11");

        envoyer("/api/demandes/" + id + "/valider", fx.jeton(mona), "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("VALIDEE"))
                .andExpect(jsonPath("$.joursSousEffectif.length()").value(0));
        lire("/api/moi", fx.jeton(alice))
                .andExpect(jsonPath("$.soldes[0].pris").value(5.0))
                .andExpect(jsonPath("$.soldes[0].enAttente").value(0.0))
                .andExpect(jsonPath("$.soldes[0].disponible").value(20.0));
    }

    @Test
    void unManagerNeDecidePasPourUneAutreEquipe() throws Exception {
        long id = deposer(fx.jeton(alice), "RTT", "2026-12-07", "2026-12-07");

        envoyer("/api/demandes/" + id + "/valider", fx.jeton(paul), "")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Vous ne pouvez décider que des demandes de votre équipe."));
    }

    @Test
    void unManagerNeValidePasSaPropreDemandeMaisLesRhLePeuvent() throws Exception {
        long id = deposer(fx.jeton(mona), "RTT", "2026-12-07", "2026-12-07");

        envoyer("/api/demandes/" + id + "/valider", fx.jeton(mona), "")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Vous ne pouvez pas décider de votre propre demande."));
        envoyer("/api/demandes/" + id + "/valider", fx.jeton(rose), "")
                .andExpect(status().isOk());
    }

    @Test
    void unEmployeNePeutPasDecider() throws Exception {
        long id = deposer(fx.jeton(alice), "RTT", "2026-12-07", "2026-12-07");

        envoyer("/api/demandes/" + id + "/valider", fx.jeton(bruno), "").andExpect(status().isForbidden());
        lire("/api/demandes/a-decider", fx.jeton(bruno)).andExpect(status().isForbidden());
    }

    @Test
    void unRefusExigeUnMotifPuisLeConserve() throws Exception {
        long id = deposer(fx.jeton(alice), "RTT", "2026-12-07", "2026-12-07");

        envoyer("/api/demandes/" + id + "/refuser", fx.jeton(mona), """
                {"motif": "  "}
                """)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("MOTIF_OBLIGATOIRE"));
        envoyer("/api/demandes/" + id + "/refuser", fx.jeton(mona), """
                {"motif": "Mise en production ce jour-là"}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("REFUSEE"))
                .andExpect(jsonPath("$.motifRefus").value("Mise en production ce jour-là"));
        lire("/api/demandes/" + id + "/historique", fx.jeton(alice))
                .andExpect(jsonPath("$[1].nouveauStatut").value("REFUSEE"))
                .andExpect(jsonPath("$[1].commentaire").value("Mise en production ce jour-là"))
                .andExpect(jsonPath("$[1].auteurNom").value("mona"));
    }

    @Test
    void uneDemandeNeSeDecideQuUneFois() throws Exception {
        long id = deposer(fx.jeton(alice), "RTT", "2026-12-07", "2026-12-07");
        envoyer("/api/demandes/" + id + "/valider", fx.jeton(mona), "").andExpect(status().isOk());

        envoyer("/api/demandes/" + id + "/refuser", fx.jeton(mona), """
                {"motif": "Trop tard"}
                """)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("DEMANDE_DEJA_TRAITEE"));
    }

    @Test
    void listeLesDemandesADeciderSelonLePerimetre() throws Exception {
        deposer(fx.jeton(alice), "RTT", "2026-12-07", "2026-12-07");
        deposer(fx.jeton(mona), "RTT", "2026-12-08", "2026-12-08");

        lire("/api/demandes/a-decider", fx.jeton(mona))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].demandeurNom").value("alice"));
        lire("/api/demandes/a-decider", fx.jeton(paul))
                .andExpect(jsonPath("$.length()").value(0));
        lire("/api/demandes/a-decider", fx.jeton(rose))
                .andExpect(jsonPath("$[?(@.demandeurNom == 'alice')]").exists())
                .andExpect(jsonPath("$[?(@.demandeurNom == 'mona')]").exists());
    }

    @Test
    void avertitSansBloquerQuandPlusDeLaMoitieDeLEquipeSeraitAbsente() throws Exception {
        // Equipe de 4 (mona, alice, bruno, chloe). Alice et Bruno sont deja absents le mardi 8.
        long a = deposer(fx.jeton(alice), "CONGES_PAYES", "2026-12-07", "2026-12-09");
        long b = deposer(fx.jeton(bruno), "RTT", "2026-12-08", "2026-12-08");
        envoyer("/api/demandes/" + a + "/valider", fx.jeton(mona), "").andExpect(status().isOk());
        envoyer("/api/demandes/" + b + "/valider", fx.jeton(mona), "").andExpect(status().isOk());
        long c = deposer(fx.jeton(chloe), "RTT", "2026-12-08", "2026-12-10");

        lire("/api/demandes/a-decider", fx.jeton(mona))
                .andExpect(jsonPath("$[0].joursSousEffectif.length()").value(1))
                .andExpect(jsonPath("$[0].joursSousEffectif[0]").value("2026-12-08"));
        envoyer("/api/demandes/" + c + "/valider", fx.jeton(mona), "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("VALIDEE"))
                .andExpect(jsonPath("$.joursSousEffectif[0]").value("2026-12-08"));
    }
}
