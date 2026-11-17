package fr.congesequipe.web;

import fr.congesequipe.securite.JetonService;
import fr.congesequipe.service.AuthService;
import fr.congesequipe.service.UtilisateurVue;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Connexion")
public class AuthController {

    private final AuthService auth;
    private final JetonService jetons;

    public AuthController(AuthService auth, JetonService jetons) {
        this.auth = auth;
        this.jetons = jetons;
    }

    @PostMapping("/connexion")
    @Operation(summary = "Echange un e-mail et un mot de passe contre un jeton JWT")
    public ReponseConnexion connexion(@Valid @RequestBody RequeteConnexion requete) {
        UtilisateurVue utilisateur = auth.verifier(requete.email(), requete.motDePasse());
        JetonService.Jeton jeton = jetons.emettre(utilisateur);
        return new ReponseConnexion(jeton.valeur(), jeton.expiration(), utilisateur);
    }

    public record RequeteConnexion(@NotBlank @Email String email, @NotBlank String motDePasse) {
    }

    public record ReponseConnexion(String jeton, Instant expiration, UtilisateurVue utilisateur) {
    }
}
