package fr.congesequipe.service;

import fr.congesequipe.domaine.Role;
import fr.congesequipe.persistance.Utilisateur;

public record UtilisateurVue(Long id, String email, String nomComplet, Role role, Long equipeId, String equipeNom) {

    static UtilisateurVue de(Utilisateur u) {
        return new UtilisateurVue(u.getId(), u.getEmail(), u.getNomComplet(), u.getRole(),
                u.getEquipe() == null ? null : u.getEquipe().getId(),
                u.getEquipe() == null ? null : u.getEquipe().getNom());
    }
}
