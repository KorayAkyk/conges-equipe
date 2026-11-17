package fr.congesequipe.service;

import fr.congesequipe.domaine.DecompteJours;
import fr.congesequipe.domaine.RegleDemande;
import fr.congesequipe.domaine.StatutDemande;
import fr.congesequipe.persistance.DemandeConge;
import fr.congesequipe.persistance.DemandeCongeRepository;
import fr.congesequipe.persistance.Equipe;
import fr.congesequipe.persistance.UtilisateurRepository;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

/** Avertit quand une demande laisserait moins de la moitie de l'equipe presente. Ne bloque rien. */
@Service
public class EffectifService {

    private final DemandeCongeRepository demandes;
    private final UtilisateurRepository utilisateurs;

    public EffectifService(DemandeCongeRepository demandes, UtilisateurRepository utilisateurs) {
        this.demandes = demandes;
        this.utilisateurs = utilisateurs;
    }

    /** A appeler dans une transaction : lit l'equipe du demandeur. */
    public List<LocalDate> joursSousEffectif(DemandeConge demande) {
        Equipe equipe = demande.getDemandeur().getEquipe();
        if (equipe == null) {
            return List.of();
        }
        int taille = (int) utilisateurs.countByEquipeId(equipe.getId());
        List<LocalDate> jours = DecompteJours.joursOuvres(demande.periode());
        Map<LocalDate, Integer> autresAbsents = new HashMap<>();
        Long demandeurId = demande.getDemandeur().getId();
        for (DemandeConge autre : demandes.deLEquipeSurPeriode(equipe.getId(), Set.of(StatutDemande.VALIDEE),
                demande.getDateDebut(), demande.getDateFin())) {
            if (autre.getDemandeur().getId().equals(demandeurId)) {
                continue;
            }
            for (LocalDate jour : jours) {
                if (autre.periode().contient(jour)) {
                    autresAbsents.merge(jour, 1, Integer::sum);
                }
            }
        }
        return RegleDemande.joursEnSousEffectif(jours, autresAbsents, taille);
    }
}
