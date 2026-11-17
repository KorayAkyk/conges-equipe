package fr.congesequipe.web;

import fr.congesequipe.domaine.Periode;
import fr.congesequipe.domaine.TypeConge;
import fr.congesequipe.service.DemandeService;
import fr.congesequipe.service.DemandeVue;
import fr.congesequipe.service.EvenementVue;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demandes")
@Tag(name = "Demandes")
@SecurityRequirement(name = "jwt")
public class DemandeController {

    private final DemandeService demandes;

    public DemandeController(DemandeService demandes) {
        this.demandes = demandes;
    }

    @GetMapping("/miennes")
    @Operation(summary = "Demandes de l'utilisateur connecte, les plus recentes d'abord")
    public List<DemandeVue> miennes(@AuthenticationPrincipal Jwt jeton) {
        return demandes.mesDemandes(Acteur.id(jeton));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Depose une demande, decomptee en jours ouvres")
    public DemandeVue creer(@AuthenticationPrincipal Jwt jeton, @Valid @RequestBody NouvelleDemande requete) {
        return demandes.creer(Acteur.id(jeton), requete.type(), new Periode(requete.dateDebut(), requete.dateFin()),
                requete.debutApresMidi(), requete.finMatin(), requete.commentaire());
    }

    @PostMapping("/{id}/annuler")
    @Operation(summary = "Annule sa propre demande, avant son premier jour")
    public DemandeVue annuler(@AuthenticationPrincipal Jwt jeton, @PathVariable Long id) {
        return demandes.annuler(Acteur.id(jeton), id);
    }

    @GetMapping("/a-decider")
    @Operation(summary = "Demandes en attente que l'utilisateur peut valider ou refuser")
    public List<DemandeVue> aDecider(@AuthenticationPrincipal Jwt jeton) {
        return demandes.aDecider(Acteur.id(jeton));
    }

    @PostMapping("/{id}/valider")
    @Operation(summary = "Valide une demande ; joursSousEffectif signale les jours a plus de 50 % d'absents")
    public DemandeVue valider(@AuthenticationPrincipal Jwt jeton, @PathVariable Long id) {
        return demandes.valider(Acteur.id(jeton), id);
    }

    @PostMapping("/{id}/refuser")
    @Operation(summary = "Refuse une demande avec un motif obligatoire")
    public DemandeVue refuser(@AuthenticationPrincipal Jwt jeton, @PathVariable Long id,
            @Valid @RequestBody Refus refus) {
        return demandes.refuser(Acteur.id(jeton), id, refus.motif());
    }

    @GetMapping("/{id}/historique")
    @Operation(summary = "Changements d'etat de la demande, du plus ancien au plus recent")
    public List<EvenementVue> historique(@AuthenticationPrincipal Jwt jeton, @PathVariable Long id) {
        return demandes.historique(Acteur.id(jeton), id);
    }

    public record NouvelleDemande(@NotNull TypeConge type, @NotNull LocalDate dateDebut, @NotNull LocalDate dateFin,
            boolean debutApresMidi, boolean finMatin, @Size(max = 500) String commentaire) {
    }

    public record Refus(@Size(max = 500) String motif) {
    }
}
