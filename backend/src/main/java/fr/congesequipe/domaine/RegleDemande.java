package fr.congesequipe.domaine;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Regles du cycle de vie d'une demande, sans dependance technique. */
public final class RegleDemande {

    private RegleDemande() {
    }

    /** Seule une demande en attente peut etre validee ou refusee. */
    public static void verifierDecisionPossible(StatutDemande statut) {
        if (statut != StatutDemande.EN_ATTENTE) {
            throw new RegleMetierException("DEMANDE_DEJA_TRAITEE",
                    "Cette demande n'est plus en attente (statut : " + statut + ").");
        }
    }

    /** Un refus doit toujours etre motive. */
    public static String verifierMotif(String motif) {
        if (motif == null || motif.isBlank()) {
            throw new RegleMetierException("MOTIF_OBLIGATOIRE", "Un refus doit être motivé.");
        }
        return motif.strip();
    }

    /** L'employe annule une demande vivante, et seulement avant son premier jour. */
    public static void verifierAnnulation(StatutDemande statut, LocalDate debut, LocalDate aujourdhui) {
        if (!statut.estVivante()) {
            throw new RegleMetierException("ANNULATION_IMPOSSIBLE",
                    "Seule une demande en attente ou validée peut être annulée.");
        }
        if (!aujourdhui.isBefore(debut)) {
            throw new RegleMetierException("ANNULATION_TROP_TARDIVE",
                    "Une demande ne peut plus être annulée à partir de son premier jour.");
        }
    }

    /** Le solde disponible doit couvrir la demande. */
    public static void verifierSolde(BigDecimal disponible, BigDecimal demandes, TypeConge type) {
        if (demandes.compareTo(disponible) > 0) {
            throw new RegleMetierException("SOLDE_INSUFFISANT",
                    "Solde " + type.libelle() + " insuffisant : " + disponible.stripTrailingZeros().toPlainString()
                            + " jour(s) disponible(s), " + demandes.stripTrailingZeros().toPlainString()
                            + " demandé(s).");
        }
    }

    /**
     * Jours ou plus de la moitie de l'equipe serait absente si la demande etait acceptee.
     * C'est un avertissement : la decision reste au manager.
     *
     * @param joursDemande jours ouvres de la demande
     * @param autresAbsents nombre d'autres membres deja absents, par jour
     * @param tailleEquipe nombre de membres de l'equipe, demandeur compris
     */
    public static List<LocalDate> joursEnSousEffectif(List<LocalDate> joursDemande,
            Map<LocalDate, Integer> autresAbsents, int tailleEquipe) {
        List<LocalDate> alertes = new ArrayList<>();
        for (LocalDate jour : joursDemande) {
            int absents = autresAbsents.getOrDefault(jour, 0) + 1;
            if (absents * 2 > tailleEquipe) {
                alertes.add(jour);
            }
        }
        return alertes;
    }
}
