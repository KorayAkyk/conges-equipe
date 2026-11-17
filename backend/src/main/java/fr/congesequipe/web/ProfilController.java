package fr.congesequipe.web;

import fr.congesequipe.service.ProfilService;
import fr.congesequipe.service.ProfilVue;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Profil")
@SecurityRequirement(name = "jwt")
public class ProfilController {

    private final ProfilService profils;

    public ProfilController(ProfilService profils) {
        this.profils = profils;
    }

    @GetMapping("/api/moi")
    @Operation(summary = "Utilisateur connecté et ses soldes")
    public ProfilVue moi(@AuthenticationPrincipal Jwt jeton) {
        return profils.profil(Acteur.id(jeton));
    }
}
