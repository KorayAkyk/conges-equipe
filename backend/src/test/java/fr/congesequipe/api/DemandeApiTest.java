package fr.congesequipe.api;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.congesequipe.persistance.Equipe;
import fr.congesequipe.persistance.Utilisateur;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Depot et annulation d'une demande par l'employe. Horloge : lundi 16 novembre 2026. */
class DemandeApiTest extends ApiTestBase {

    Utilisateur alice;
    Utilisateur bruno;
    String jetonAlice;

    @BeforeEach
    void preparer() {
        Equipe equipe = fx.equipe("Equipe demandes");
        fx.manager("mona", equipe);
        alice = fx.employe("alice", equipe);
        bruno = fx.employe("bruno", equipe);
        jetonAlice = fx.jeton(alice);
    }

    @Test
    void deposeUneDemandeEtLaDeduitDuSoldeDisponible() throws Exception {
        envoyer("/api/demandes", jetonAlice, """
                {"type": "CONGES_PAYES", "dateDebut": "2026-12-21", "dateFin": "2026-12-25",
                 "debutApresMidi": true, "commentaire": "Fêtes de fin d'année"}
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("EN_ATTENTE"))
                .andExpect(jsonPath("$.jours").value(3.5))
                .andExpect(jsonPath("$.demandeurNom").value("alice"));

        lire("/api/moi", jetonAlice)
                .andExpect(jsonPath("$.soldes[0].enAttente").value(3.5))
                .andExpect(jsonPath("$.soldes[0].disponible").value(21.5));
        lire("/api/demandes/miennes", jetonAlice)
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].commentaire").value("Fêtes de fin d'année"));
    }

    @Test
    void refuseUnChevauchementAvecUneDemandeVivante() throws Exception {
        deposer(jetonAlice, "RTT", "2026-12-07", "2026-12-08");

        envoyer("/api/demandes", jetonAlice, """
                {"type": "CONGES_PAYES", "dateDebut": "2026-12-08", "dateFin": "2026-12-10"}
                """)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("CHEVAUCHEMENT"));
    }

    @Test
    void accepteLaMemePeriodeApresUneAnnulation() throws Exception {
        long id = deposer(jetonAlice, "RTT", "2026-12-07", "2026-12-08");
        envoyer("/api/demandes/" + id + "/annuler", jetonAlice, "").andExpect(status().isOk());

        envoyer("/api/demandes", jetonAlice, """
                {"type": "RTT", "dateDebut": "2026-12-07", "dateFin": "2026-12-08"}
                """)
                .andExpect(status().isCreated());
    }

    @Test
    void refuseUnSoldeInsuffisant() throws Exception {
        envoyer("/api/demandes", jetonAlice, """
                {"type": "RTT", "dateDebut": "2026-12-07", "dateFin": "2026-12-21"}
                """)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("SOLDE_INSUFFISANT"))
                .andExpect(jsonPath("$.type").value("https://conges-equipe.example.com/erreurs/solde-insuffisant"));
    }

    @Test
    void refuseUnePeriodeSansJourOuvre() throws Exception {
        envoyer("/api/demandes", jetonAlice, """
                {"type": "RTT", "dateDebut": "2026-12-25", "dateFin": "2026-12-27"}
                """)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("AUCUN_JOUR_OUVRE"));
    }

    @Test
    void refuseDesDatesInverseesOuManquantes() throws Exception {
        envoyer("/api/demandes", jetonAlice, """
                {"type": "RTT", "dateDebut": "2026-12-09", "dateFin": "2026-12-08"}
                """)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("DATES_INVERSEES"));
        envoyer("/api/demandes", jetonAlice, """
                {"dateDebut": "2026-12-09"}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.champs.length()").value(2));
    }

    @Test
    void annuleAvantLeDebutEtRendLeSolde() throws Exception {
        long id = deposer(jetonAlice, "CONGES_PAYES", "2026-12-07", "2026-12-11");

        envoyer("/api/demandes/" + id + "/annuler", jetonAlice, "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("ANNULEE"));
        lire("/api/moi", jetonAlice).andExpect(jsonPath("$.soldes[0].disponible").value(25.0));
    }

    @Test
    void refuseLAnnulationUneFoisLaDemandeCommencee() throws Exception {
        long id = deposer(jetonAlice, "RTT", "2026-11-16", "2026-11-17");

        envoyer("/api/demandes/" + id + "/annuler", jetonAlice, "")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("ANNULATION_TROP_TARDIVE"));
    }

    @Test
    void personneNAnnuleLaDemandeDUnAutre() throws Exception {
        long id = deposer(jetonAlice, "RTT", "2026-12-07", "2026-12-07");

        envoyer("/api/demandes/" + id + "/annuler", fx.jeton(bruno), "")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Accès refusé"));
    }

    @Test
    void historiseChaqueChangementDEtat() throws Exception {
        long id = deposer(jetonAlice, "RTT", "2026-12-07", "2026-12-07");
        envoyer("/api/demandes/" + id + "/annuler", jetonAlice, "");

        lire("/api/demandes/" + id + "/historique", jetonAlice)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].ancienStatut").doesNotExist())
                .andExpect(jsonPath("$[0].nouveauStatut").value("EN_ATTENTE"))
                .andExpect(jsonPath("$[1].ancienStatut").value("EN_ATTENTE"))
                .andExpect(jsonPath("$[1].nouveauStatut").value("ANNULEE"))
                .andExpect(jsonPath("$[1].auteurNom").value("alice"));
        lire("/api/demandes/" + id + "/historique", fx.jeton(bruno)).andExpect(status().isForbidden());
    }

    @Test
    void renvoie404PourUneDemandeInconnue() throws Exception {
        lire("/api/demandes/999999/historique", jetonAlice)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
