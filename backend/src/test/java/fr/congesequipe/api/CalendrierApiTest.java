package fr.congesequipe.api;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.congesequipe.persistance.Equipe;
import fr.congesequipe.persistance.Utilisateur;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CalendrierApiTest extends ApiTestBase {

    Equipe dev;
    Equipe support;
    Utilisateur alice;
    Utilisateur paul;
    Utilisateur rose;

    @BeforeEach
    void preparer() {
        dev = fx.equipe("Equipe dev");
        fx.manager("mona", dev);
        alice = fx.employe("alice", dev);
        support = fx.equipe("Equipe support");
        paul = fx.manager("paul", support);
        rose = fx.rh("rose");
    }

    @Test
    void afficheLesAbsencesEtLesJoursFeriesDuMois() throws Exception {
        deposer(fx.jeton(alice), "CONGES_PAYES", "2026-12-21", "2026-12-24");
        deposer(fx.jeton(alice), "RTT", "2027-01-04", "2027-01-04");

        lire("/api/calendrier?mois=2026-12", fx.jeton(alice))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equipeNom").value("Equipe dev"))
                .andExpect(jsonPath("$.mois").value("2026-12"))
                .andExpect(jsonPath("$.membres.length()").value(2))
                .andExpect(jsonPath("$.joursFeries.length()").value(1))
                .andExpect(jsonPath("$.joursFeries[0].libelle").value("Noël"))
                .andExpect(jsonPath("$.absences.length()").value(1))
                .andExpect(jsonPath("$.absences[0].statut").value("EN_ATTENTE"));
    }

    @Test
    void reserveLeCalendrierDUneEquipeASesMembresSonManagerEtAuxRh() throws Exception {
        lire("/api/calendrier?mois=2026-12&equipeId=" + support.getId(), fx.jeton(alice))
                .andExpect(status().isForbidden());
        lire("/api/calendrier?mois=2026-12&equipeId=" + support.getId(), fx.jeton(paul))
                .andExpect(status().isOk());
        lire("/api/calendrier?mois=2026-12&equipeId=" + dev.getId(), fx.jeton(rose))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equipeNom").value("Equipe dev"));
    }

    @Test
    void refuseUnMoisMalForme() throws Exception {
        lire("/api/calendrier?mois=decembre", fx.jeton(alice)).andExpect(status().isBadRequest());
    }

    @Test
    void listeLesEquipesVisibles() throws Exception {
        lire("/api/equipes", fx.jeton(alice))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nom").value("Equipe dev"));
        lire("/api/equipes", fx.jeton(rose))
                .andExpect(jsonPath("$[?(@.nom == 'Equipe support')]").exists());
    }

    @Test
    void publieLesJoursFeriesDUneAnnee() throws Exception {
        lire("/api/jours-feries?annee=2027", fx.jeton(alice))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(11))
                .andExpect(jsonPath("$[1].date").value("2027-03-29"));
    }
}
