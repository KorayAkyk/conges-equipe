package fr.congesequipe.securite;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Reglages du jeton. Le secret vient de l'environnement (APP_JWT_SECRET) : sans lui, l'application
 * refuse de demarrer. Seul le profil demo fournit une valeur, explicitement reservee au developpement.
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProprietes(String secret, Duration duree, String emetteur) {

    public JwtProprietes {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "APP_JWT_SECRET doit etre defini et faire au moins 32 octets (HMAC-SHA256).");
        }
        if (duree == null) {
            duree = Duration.ofHours(8);
        }
        if (emetteur == null || emetteur.isBlank()) {
            emetteur = "conges-equipe";
        }
    }
}
