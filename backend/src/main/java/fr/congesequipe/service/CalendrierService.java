package fr.congesequipe.service;

import fr.congesequipe.domaine.AccesInterditException;
import fr.congesequipe.domaine.IntrouvableException;
import fr.congesequipe.domaine.JoursFeries;
import fr.congesequipe.domaine.Role;
import fr.congesequipe.domaine.StatutDemande;
import fr.congesequipe.persistance.DemandeCongeRepository;
import fr.congesequipe.persistance.Equipe;
import fr.congesequipe.persistance.EquipeRepository;
import fr.congesequipe.persistance.Utilisateur;
import fr.congesequipe.persistance.UtilisateurRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Calendrier d'equipe : absences validees et en attente sur un mois. */
@Service
public class CalendrierService {

    private final DemandeCongeRepository demandes;
    private final UtilisateurRepository utilisateurs;
    private final EquipeRepository equipes;

    public CalendrierService(DemandeCongeRepository demandes, UtilisateurRepository utilisateurs,
            EquipeRepository equipes) {
        this.demandes = demandes;
        this.utilisateurs = utilisateurs;
        this.equipes = equipes;
    }

    /** Equipes dont l'acteur peut voir le calendrier. */
    @Transactional(readOnly = true)
    public List<EquipeVue> equipesVisibles(Long acteurId) {
        Utilisateur acteur = chargerUtilisateur(acteurId);
        List<Equipe> liste = acteur.getRole() == Role.RH ? equipes.findAll() : equipes.findAll().stream()
                .filter(e -> Droits.peutVoirEquipe(acteur, e)).toList();
        return liste.stream().map(e -> new EquipeVue(e.getId(), e.getNom())).toList();
    }

    @Transactional(readOnly = true)
    public CalendrierVue calendrier(Long acteurId, YearMonth mois, Long equipeIdDemandee) {
        Utilisateur acteur = chargerUtilisateur(acteurId);
        Long equipeId = equipeIdDemandee;
        if (equipeId == null) {
            if (acteur.getEquipe() == null) {
                throw new IntrouvableException("Aucune équipe rattachée à ce compte.");
            }
            equipeId = acteur.getEquipe().getId();
        }
        Equipe equipe = equipes.findById(equipeId)
                .orElseThrow(() -> new IntrouvableException("Équipe introuvable."));
        if (!Droits.peutVoirEquipe(acteur, equipe)) {
            throw new AccesInterditException("Vous ne pouvez consulter que le calendrier de votre équipe.");
        }
        LocalDate debut = mois.atDay(1);
        LocalDate fin = mois.atEndOfMonth();
        List<CalendrierVue.JourFerie> feries = JoursFeries.de(mois.getYear()).entrySet().stream()
                .filter(e -> !e.getKey().isBefore(debut) && !e.getKey().isAfter(fin))
                .map(e -> new CalendrierVue.JourFerie(e.getKey(), e.getValue()))
                .toList();
        List<CalendrierVue.Membre> membres = utilisateurs.findByEquipeIdOrderByNomComplet(equipe.getId()).stream()
                .map(u -> new CalendrierVue.Membre(u.getId(), u.getNomComplet()))
                .toList();
        List<DemandeVue> absences = demandes.deLEquipeSurPeriode(equipe.getId(),
                Set.of(StatutDemande.EN_ATTENTE, StatutDemande.VALIDEE), debut, fin).stream()
                .map(DemandeVue::de)
                .toList();
        return new CalendrierVue(equipe.getId(), equipe.getNom(), mois.toString(), feries, membres, absences);
    }

    private Utilisateur chargerUtilisateur(Long id) {
        return utilisateurs.findById(id).orElseThrow(() -> new IntrouvableException("Utilisateur introuvable."));
    }
}
