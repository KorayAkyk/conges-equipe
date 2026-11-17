package fr.congesequipe.service;

import fr.congesequipe.domaine.StatutDemande;
import java.time.Instant;

public record EvenementVue(StatutDemande ancienStatut, StatutDemande nouveauStatut, String auteurNom,
        String commentaire, Instant horodatage) {
}
