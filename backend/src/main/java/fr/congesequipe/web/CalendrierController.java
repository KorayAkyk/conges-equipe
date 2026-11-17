package fr.congesequipe.web;

import fr.congesequipe.domaine.JoursFeries;
import fr.congesequipe.service.CalendrierService;
import fr.congesequipe.service.CalendrierVue;
import fr.congesequipe.service.EquipeVue;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.YearMonth;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Calendrier")
@SecurityRequirement(name = "jwt")
public class CalendrierController {

    private final CalendrierService calendriers;

    public CalendrierController(CalendrierService calendriers) {
        this.calendriers = calendriers;
    }

    @GetMapping("/api/calendrier")
    @Operation(summary = "Absences d'une équipe sur un mois (format AAAA-MM)")
    public CalendrierVue calendrier(@AuthenticationPrincipal Jwt jeton, @RequestParam YearMonth mois,
            @RequestParam(required = false) Long equipeId) {
        return calendriers.calendrier(Acteur.id(jeton), mois, equipeId);
    }

    @GetMapping("/api/equipes")
    @Operation(summary = "Équipes dont l'utilisateur peut consulter le calendrier")
    public List<EquipeVue> equipes(@AuthenticationPrincipal Jwt jeton) {
        return calendriers.equipesVisibles(Acteur.id(jeton));
    }

    @GetMapping("/api/jours-feries")
    @Operation(summary = "Jours fériés d'une année (Code du travail, article L3133-1)")
    public List<CalendrierVue.JourFerie> joursFeries(@RequestParam int annee) {
        return JoursFeries.de(annee).entrySet().stream()
                .map(e -> new CalendrierVue.JourFerie(e.getKey(), e.getValue()))
                .toList();
    }
}
