package fr.congesequipe.api;

import fr.congesequipe.domaine.Role;
import fr.congesequipe.domaine.TypeConge;
import fr.congesequipe.persistance.DroitConges;
import fr.congesequipe.persistance.DroitCongesRepository;
import fr.congesequipe.persistance.Equipe;
import fr.congesequipe.persistance.EquipeRepository;
import fr.congesequipe.persistance.Utilisateur;
import fr.congesequipe.persistance.UtilisateurRepository;
import fr.congesequipe.securite.JetonService;
import fr.congesequipe.service.UtilisateurVue;
import java.math.BigDecimal;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Fabrique de donnees de test : equipes, personnes avec leurs droits, jetons. */
class Fixtures {

    static final String MOT_DE_PASSE = "mot-de-passe-de-test";

    private final EquipeRepository equipes;
    private final UtilisateurRepository utilisateurs;
    private final DroitCongesRepository droits;
    private final PasswordEncoder encodeur;
    private final JetonService jetons;
    private String hash;

    Fixtures(EquipeRepository equipes, UtilisateurRepository utilisateurs, DroitCongesRepository droits,
            PasswordEncoder encodeur, JetonService jetons) {
        this.equipes = equipes;
        this.utilisateurs = utilisateurs;
        this.droits = droits;
        this.encodeur = encodeur;
        this.jetons = jetons;
    }

    Equipe equipe(String nom) {
        return equipes.save(new Equipe(nom));
    }

    /** Cree un manager et le rattache a l'equipe qu'il dirige. */
    Utilisateur manager(String prenom, Equipe equipe) {
        Utilisateur manager = personne(prenom, Role.MANAGER, equipe);
        equipe.setManagerId(manager.getId());
        return manager;
    }

    Utilisateur employe(String prenom, Equipe equipe) {
        return personne(prenom, Role.EMPLOYE, equipe);
    }

    Utilisateur rh(String prenom) {
        return personne(prenom, Role.RH, null);
    }

    /** Personne avec 25 jours de conges payes et 10 RTT. */
    Utilisateur personne(String prenom, Role role, Equipe equipe) {
        if (hash == null) {
            hash = encodeur.encode(MOT_DE_PASSE);
        }
        Utilisateur u = utilisateurs.save(new Utilisateur(prenom + "@exemple.test", prenom, hash, role, equipe));
        droits.save(new DroitConges(u.getId(), TypeConge.CONGES_PAYES, new BigDecimal("25.0")));
        droits.save(new DroitConges(u.getId(), TypeConge.RTT, new BigDecimal("10.0")));
        return u;
    }

    String jeton(Utilisateur u) {
        UtilisateurVue vue = new UtilisateurVue(u.getId(), u.getEmail(), u.getNomComplet(), u.getRole(),
                u.getEquipe() == null ? null : u.getEquipe().getId(),
                u.getEquipe() == null ? null : u.getEquipe().getNom());
        return "Bearer " + jetons.emettre(vue).valeur();
    }
}
