package fr.congesequipe.service;

import fr.congesequipe.domaine.AccesInterditException;
import fr.congesequipe.domaine.DecompteJours;
import fr.congesequipe.domaine.IntrouvableException;
import fr.congesequipe.domaine.Periode;
import fr.congesequipe.domaine.RegleDemande;
import fr.congesequipe.domaine.RegleMetierException;
import fr.congesequipe.domaine.StatutDemande;
import fr.congesequipe.domaine.TypeConge;
import fr.congesequipe.persistance.DemandeConge;
import fr.congesequipe.persistance.DemandeCongeRepository;
import fr.congesequipe.persistance.HistoriqueDemande;
import fr.congesequipe.persistance.HistoriqueDemandeRepository;
import fr.congesequipe.persistance.Utilisateur;
import fr.congesequipe.persistance.UtilisateurRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cycle de vie des demandes : creation, annulation, validation, refus, historique. */
@Service
public class DemandeService {

    private static final Set<StatutDemande> VIVANTES = Set.of(StatutDemande.EN_ATTENTE, StatutDemande.VALIDEE);

    private final DemandeCongeRepository demandes;
    private final UtilisateurRepository utilisateurs;
    private final HistoriqueDemandeRepository historique;
    private final SoldeService soldes;
    private final EffectifService effectif;
    private final Clock horloge;

    public DemandeService(DemandeCongeRepository demandes, UtilisateurRepository utilisateurs,
            HistoriqueDemandeRepository historique, SoldeService soldes, EffectifService effectif, Clock horloge) {
        this.demandes = demandes;
        this.utilisateurs = utilisateurs;
        this.historique = historique;
        this.soldes = soldes;
        this.effectif = effectif;
        this.horloge = horloge;
    }

    @Transactional
    public DemandeVue creer(Long acteurId, TypeConge type, Periode periode, boolean debutApresMidi,
            boolean finMatin, String commentaire) {
        if (type == null) {
            throw new RegleMetierException("TYPE_OBLIGATOIRE", "Le type de congés est obligatoire.");
        }
        Utilisateur demandeur = utilisateurs.verrouiller(acteurId)
                .orElseThrow(() -> new IntrouvableException("Utilisateur introuvable."));
        BigDecimal jours = DecompteJours.decompter(periode, debutApresMidi, finMatin);
        if (!demandes.chevauchantes(acteurId, VIVANTES, periode.debut(), periode.fin()).isEmpty()) {
            throw new RegleMetierException("CHEVAUCHEMENT",
                    "Cette période chevauche une de vos demandes en attente ou validées.");
        }
        RegleDemande.verifierSolde(soldes.solde(acteurId, type).disponible(), jours, type);
        DemandeConge demande = demandes.save(new DemandeConge(demandeur, type, periode, debutApresMidi, finMatin,
                jours, nettoyer(commentaire), Instant.now(horloge)));
        tracer(demande, null, acteurId, null);
        return DemandeVue.de(demande);
    }

    @Transactional
    public DemandeVue annuler(Long acteurId, Long demandeId) {
        DemandeConge demande = charger(demandeId);
        if (!demande.getDemandeur().getId().equals(acteurId)) {
            throw new AccesInterditException("Seul l'auteur d'une demande peut l'annuler.");
        }
        StatutDemande ancien = demande.getStatut();
        demande.annuler(LocalDate.now(horloge));
        tracer(demande, ancien, acteurId, null);
        return DemandeVue.de(demande);
    }

    @Transactional
    public DemandeVue valider(Long acteurId, Long demandeId) {
        Utilisateur acteur = chargerUtilisateur(acteurId);
        DemandeConge demande = charger(demandeId);
        Droits.verifierDecideur(acteur, demande);
        List<LocalDate> alertes = effectif.joursSousEffectif(demande);
        StatutDemande ancien = demande.getStatut();
        demande.valider();
        tracer(demande, ancien, acteurId, null);
        return DemandeVue.de(demande, alertes);
    }

    @Transactional
    public DemandeVue refuser(Long acteurId, Long demandeId, String motif) {
        Utilisateur acteur = chargerUtilisateur(acteurId);
        DemandeConge demande = charger(demandeId);
        Droits.verifierDecideur(acteur, demande);
        StatutDemande ancien = demande.getStatut();
        demande.refuser(motif);
        tracer(demande, ancien, acteurId, demande.getMotifRefus());
        return DemandeVue.de(demande);
    }

    @Transactional(readOnly = true)
    public List<DemandeVue> mesDemandes(Long acteurId) {
        return demandes.findByDemandeurIdOrderByDateDebutDesc(acteurId).stream().map(DemandeVue::de).toList();
    }

    /** Demandes en attente que l'acteur peut decider, avec les jours en sous-effectif. */
    @Transactional(readOnly = true)
    public List<DemandeVue> aDecider(Long acteurId) {
        Utilisateur acteur = chargerUtilisateur(acteurId);
        return demandes.findByStatutOrderByDateDebutAsc(StatutDemande.EN_ATTENTE).stream()
                .filter(d -> Droits.peutDecider(acteur, d))
                .map(d -> DemandeVue.de(d, effectif.joursSousEffectif(d)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EvenementVue> historique(Long acteurId, Long demandeId) {
        Utilisateur acteur = chargerUtilisateur(acteurId);
        DemandeConge demande = charger(demandeId);
        Droits.verifierConsultation(acteur, demande);
        return historique.findByDemandeIdOrderByHorodatageAscIdAsc(demandeId).stream()
                .map(h -> new EvenementVue(h.getAncienStatut(), h.getNouveauStatut(),
                        utilisateurs.findById(h.getAuteurId()).map(Utilisateur::getNomComplet).orElse("?"),
                        h.getCommentaire(), h.getHorodatage()))
                .toList();
    }

    private void tracer(DemandeConge demande, StatutDemande ancien, Long auteurId, String commentaire) {
        historique.save(new HistoriqueDemande(demande.getId(), ancien, demande.getStatut(), auteurId, commentaire,
                Instant.now(horloge)));
    }

    private DemandeConge charger(Long demandeId) {
        return demandes.findById(demandeId)
                .orElseThrow(() -> new IntrouvableException("Demande " + demandeId + " introuvable."));
    }

    private Utilisateur chargerUtilisateur(Long id) {
        return utilisateurs.findById(id).orElseThrow(() -> new IntrouvableException("Utilisateur introuvable."));
    }

    private static String nettoyer(String texte) {
        return texte == null || texte.isBlank() ? null : texte.strip();
    }
}
