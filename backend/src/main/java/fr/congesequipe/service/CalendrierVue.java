package fr.congesequipe.service;

import java.time.LocalDate;
import java.util.List;

/** Absences d'une equipe sur un mois, avec les jours feries du mois. */
public record CalendrierVue(Long equipeId, String equipeNom, String mois, List<JourFerie> joursFeries,
        List<Membre> membres, List<DemandeVue> absences) {

    public record JourFerie(LocalDate date, String libelle) {
    }

    public record Membre(Long id, String nomComplet) {
    }
}
