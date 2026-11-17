package fr.congesequipe.api;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base des tests d'API : vraie base PostgreSQL dans un conteneur (Testcontainers), schema cree par
 * Flyway, un seul contexte Spring partage. Chaque test tourne dans une transaction annulee a la fin.
 * L'horloge est figee au lundi 16 novembre 2026.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import({ApiTestBase.HorlogeFixe.class, Fixtures.class})
abstract class ApiTestBase {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17.11-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void base(DynamicPropertyRegistry registre) {
        registre.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registre.add("spring.datasource.username", POSTGRES::getUsername);
        registre.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected Fixtures fx;

    protected ResultActions lire(String url, String jeton) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.get(url).header("Authorization", jeton));
    }

    protected ResultActions envoyer(String url, String jeton, String json) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post(url).header("Authorization", jeton)
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    /** Depose une demande et renvoie son identifiant. */
    protected long deposer(String jeton, String type, String debut, String fin) throws Exception {
        String reponse = envoyer("/api/demandes", jeton, """
                {"type": "%s", "dateDebut": "%s", "dateFin": "%s"}
                """.formatted(type, debut, fin))
                .andReturn().getResponse().getContentAsString();
        return com.jayway.jsonpath.JsonPath.<Number>read(reponse, "$.id").longValue();
    }

    @TestConfiguration
    static class HorlogeFixe {

        @Bean
        @Primary
        Clock horlogeFixe() {
            return Clock.fixed(Instant.parse("2026-11-16T08:00:00Z"), ZoneId.of("Europe/Paris"));
        }
    }
}
