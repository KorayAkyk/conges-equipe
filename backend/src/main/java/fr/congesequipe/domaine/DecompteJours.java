package fr.congesequipe.domaine;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Decompte d'une absence en jours ouvres : du lundi au vendredi, hors jours feries.
 *
 * <p>Une demande peut commencer l'apres-midi du premier jour et finir le matin du dernier jour :
 * chacune de ces options retire une demi-journee, si le jour concerne est ouvre.
 */
public final class DecompteJours {

    private static final BigDecimal DEMI = new BigDecimal("0.5");

    private DecompteJours() {
    }

    public static boolean estOuvre(LocalDate date) {
        DayOfWeek jour = date.getDayOfWeek();
        return jour != DayOfWeek.SATURDAY && jour != DayOfWeek.SUNDAY && !JoursFeries.estFerie(date);
    }

    public static List<LocalDate> joursOuvres(Periode periode) {
        List<LocalDate> jours = new ArrayList<>();
        for (LocalDate d = periode.debut(); !d.isAfter(periode.fin()); d = d.plusDays(1)) {
            if (estOuvre(d)) {
                jours.add(d);
            }
        }
        return jours;
    }

    /** Nombre de jours decomptes. Refuse une demande qui ne couvre aucun jour ouvre. */
    public static BigDecimal decompter(Periode periode, boolean debutApresMidi, boolean finMatin) {
        if (periode.debut().equals(periode.fin()) && debutApresMidi && finMatin) {
            throw new RegleMetierException("DEMI_JOURNEES_INCOHERENTES",
                    "Une demande d'un seul jour ne peut pas commencer l'après-midi et finir le matin.");
        }
        BigDecimal total = BigDecimal.valueOf(joursOuvres(periode).size());
        if (debutApresMidi && estOuvre(periode.debut())) {
            total = total.subtract(DEMI);
        }
        if (finMatin && estOuvre(periode.fin())) {
            total = total.subtract(DEMI);
        }
        if (total.signum() <= 0) {
            throw new RegleMetierException("AUCUN_JOUR_OUVRE",
                    "La période ne contient aucun jour ouvré (week-end ou jour férié).");
        }
        return total.setScale(1);
    }
}
