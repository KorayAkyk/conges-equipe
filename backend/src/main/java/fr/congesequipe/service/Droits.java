package fr.congesequipe.service;

import fr.congesequipe.domaine.AccesInterditException;
import fr.congesequipe.domaine.Role;
import fr.congesequipe.persistance.DemandeConge;
import fr.congesequipe.persistance.Equipe;
import fr.congesequipe.persistance.Utilisateur;

/** Qui peut faire quoi. Les controles de role des URL ne suffisent pas : il faut aussi le perimetre. */
final class Droits {

    private Droits() {
    }

    /** Un manager decide pour son equipe, les RH pour tous, et personne pour sa propre demande. */
    static boolean peutDecider(Utilisateur acteur, DemandeConge demande) {
        Utilisateur demandeur = demande.getDemandeur();
        if (acteur.getId().equals(demandeur.getId())) {
            return false;
        }
        return acteur.getRole() == Role.RH || acteur.manage(demandeur);
    }

    static void verifierDecideur(Utilisateur acteur, DemandeConge demande) {
        if (acteur.getId().equals(demande.getDemandeur().getId())) {
            throw new AccesInterditException("Vous ne pouvez pas décider de votre propre demande.");
        }
        if (!peutDecider(acteur, demande)) {
            throw new AccesInterditException("Vous ne pouvez décider que des demandes de votre équipe.");
        }
    }

    static void verifierConsultation(Utilisateur acteur, DemandeConge demande) {
        if (!acteur.getId().equals(demande.getDemandeur().getId()) && !peutDecider(acteur, demande)) {
            throw new AccesInterditException("Cette demande ne vous concerne pas.");
        }
    }

    static boolean peutVoirEquipe(Utilisateur acteur, Equipe equipe) {
        return acteur.getRole() == Role.RH
                || acteur.getId().equals(equipe.getManagerId())
                || (acteur.getEquipe() != null && acteur.getEquipe().getId().equals(equipe.getId()));
    }
}
