package fr.congesequipe.api;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.congesequipe.persistance.Equipe;
import fr.congesequipe.persistance.Utilisateur;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ConnexionApiTest extends ApiTestBase {

    @Test
    void connecteUnUtilisateurEtRenvoieUnJeton() throws Exception {
        Equipe equipe = fx.equipe("Equipe connexion");
        fx.employe("alice", equipe);

        mvc.perform(post("/api/auth/connexion").contentType(MediaType.APPLICATION_JSON).content("""
                {"email": "ALICE@exemple.test", "motDePasse": "%s"}
                """.formatted(Fixtures.MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jeton").isString())
                .andExpect(jsonPath("$.utilisateur.role").value("EMPLOYE"))
                .andExpect(jsonPath("$.utilisateur.equipeNom").value("Equipe connexion"));
    }

    @Test
    void refuseUnMauvaisMotDePasseAvecUnProblemDetail() throws Exception {
        fx.rh("bruno");

        mvc.perform(post("/api/auth/connexion").contentType(MediaType.APPLICATION_JSON).content("""
                {"email": "bruno@exemple.test", "motDePasse": "faux"}
                """))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Connexion refusée"))
                .andExpect(jsonPath("$.detail").value("E-mail ou mot de passe incorrect."));
    }

    @Test
    void refuseUneRequeteMalFormee() throws Exception {
        mvc.perform(post("/api/auth/connexion").contentType(MediaType.APPLICATION_JSON).content("""
                {"email": "pas-un-email", "motDePasse": ""}
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.champs.length()").value(2));
    }

    @Test
    void exigeUnJetonPourLApi() throws Exception {
        mvc.perform(get("/api/moi"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", startsWith("Bearer")));
    }

    @Test
    void refuseUnJetonFalsifie() throws Exception {
        mvc.perform(get("/api/moi").header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.e30.signature-fausse"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void renvoieLeProfilEtLesSoldes() throws Exception {
        Equipe equipe = fx.equipe("Equipe profil");
        Utilisateur claire = fx.employe("claire", equipe);

        lire("/api/moi", fx.jeton(claire))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.utilisateur.email").value("claire@exemple.test"))
                .andExpect(jsonPath("$.soldes.length()").value(2))
                .andExpect(jsonPath("$.soldes[0].type").value("CONGES_PAYES"))
                .andExpect(jsonPath("$.soldes[0].disponible").value(25.0))
                .andExpect(jsonPath("$.soldes[1].type").value("RTT"))
                .andExpect(jsonPath("$.soldes[1].disponible").value(10.0));
    }

    @Test
    void publieLaDocumentationOpenApiEtLaSante() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/demandes']").exists())
                .andExpect(jsonPath("$.components.securitySchemes.jwt.scheme").value("bearer"));
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }
}
