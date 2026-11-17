package fr.congesequipe.service;

import fr.congesequipe.domaine.StatutDemande;
import fr.congesequipe.domaine.TypeConge;
import fr.congesequipe.persistance.DemandeConge;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Demande telle que l'API la renvoie. joursSousEffectif liste les jours ou plus de la moitie de
 * l'equipe serait absente : il n'est rempli que pour les demandes a decider.
 */
public record DemandeVue(Long id, Long demandeurId, String demandeurNom, TypeConge type, LocalDate dateDebut,
        LocalDate dateFin, boolean debutApresMidi, boolean finMatin, BigDecimal jours, StatutDemande statut,
        String motifRefus, String commentaire, Instant creeLe, List<LocalDate> joursSousEffectif) {

    static DemandeVue de(DemandeConge d) {
        return de(d, List.of());
    }

    static DemandeVue de(DemandeConge d, List<LocalDate> joursSousEffectif) {
        return new DemandeVue(d.getId(), d.getDemandeur().getId(), d.getDemandeur().getNomComplet(), d.getType(),
                d.getDateDebut(), d.getDateFin(), d.isDebutApresMidi(), d.isFinMatin(), d.getJours(),
                d.getStatut(), d.getMotifRefus(), d.getCommentaire(), d.getCreeLe(), joursSousEffectif);
    }
}
