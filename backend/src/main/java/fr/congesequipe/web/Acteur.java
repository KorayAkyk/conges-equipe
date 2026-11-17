package fr.congesequipe.web;

import org.springframework.security.oauth2.jwt.Jwt;

/** Lit l'identifiant de l'utilisateur connecte dans le sujet du jeton. */
final class Acteur {

    private Acteur() {
    }

    static Long id(Jwt jeton) {
        return Long.valueOf(jeton.getSubject());
    }
}
