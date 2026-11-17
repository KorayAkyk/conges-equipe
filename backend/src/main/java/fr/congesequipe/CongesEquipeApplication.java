package fr.congesequipe;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CongesEquipeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CongesEquipeApplication.class, args);
    }

    /** Horloge unique de l'application, a l'heure de Paris. Les tests la remplacent. */
    @Bean
    Clock horloge() {
        return Clock.system(ZoneId.of("Europe/Paris"));
    }
}
