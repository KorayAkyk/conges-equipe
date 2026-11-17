package fr.congesequipe.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import fr.congesequipe.demo.DonneesDemo;
import fr.congesequipe.persistance.DroitCongesRepository;
import fr.congesequipe.persistance.EquipeRepository;
import fr.congesequipe.persistance.UtilisateurRepository;
import fr.congesequipe.service.DemandeService;
import java.time.Clock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Les donnees de demonstration passent par les vraies regles et donnent un scenario complet. */
class DonneesDemoTest extends ApiTestBase {

    @Autowired
    EquipeRepository equipes;
    @Autowired
    UtilisateurRepository utilisateurs;
    @Autowired
    DroitCongesRepository droits;
    @Autowired
    DemandeService demandes;
    @Autowired
    PasswordEncoder encodeur;
    @Autowired
    Clock horloge;

    @Test
    void creeUnScenarioUtilisableDesLaConnexion() throws Exception {
        new DonneesDemo(equipes, utilisateurs, droits, demandes, encodeur, horloge).run(null);

        String julie = connexion("julie.moreau@brindille.test");
        lire("/api/demandes/a-decider", julie)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].demandeurNom").value("Léa Roussel"))
                .andExpect(jsonPath("$[0].joursSousEffectif.length()").value(2));
        lire("/api/demandes/a-decider", connexion("nadia.leclerc@brindille.test"))
                .andExpect(jsonPath("$[?(@.demandeurNom == 'Julie Moreau')]").exists());
        lire("/api/demandes/miennes", connexion("manon.perrin@brindille.test"))
                .andExpect(jsonPath("$[?(@.statut == 'ANNULEE')]").exists())
                .andExpect(jsonPath("$[?(@.statut == 'VALIDEE')]").exists());
    }

    private String connexion(String email) throws Exception {
        String reponse = mvc.perform(post("/api/auth/connexion").contentType(MediaType.APPLICATION_JSON).content("""
                {"email": "%s", "motDePasse": "%s"}
                """.formatted(email, DonneesDemo.MOT_DE_PASSE_DEMO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(reponse, "$.jeton");
    }
}
