package fr.congesequipe.domaine;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Jours feries de France metropolitaine (Code du travail, article L3133-1).
 *
 * <p>Les onze fetes legales sont calculees pour n'importe quelle annee : les dates fixes, puis
 * celles qui dependent de Paques (lundi de Paques, Ascension, lundi de Pentecote). Les deux jours
 * propres a l'Alsace-Moselle ne sont pas pris en compte.
 */
public final class JoursFeries {

    private JoursFeries() {
    }

    /** Date du dimanche de Paques (calendrier gregorien, algorithme de Meeus, Jones et Butcher). */
    public static LocalDate paques(int annee) {
        int a = annee % 19;
        int b = annee / 100;
        int c = annee % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int mois = (h + l - 7 * m + 114) / 31;
        int jour = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(annee, mois, jour);
    }

    /** Les onze jours feries de l'annee, tries par date, avec leur libelle. */
    public static Map<LocalDate, String> de(int annee) {
        Map<LocalDate, String> feries = new TreeMap<>();
        LocalDate paques = paques(annee);
        feries.put(LocalDate.of(annee, Month.JANUARY, 1), "Jour de l'an");
        feries.put(paques.plusDays(1), "Lundi de Pâques");
        feries.put(LocalDate.of(annee, Month.MAY, 1), "Fête du travail");
        feries.put(LocalDate.of(annee, Month.MAY, 8), "Victoire 1945");
        feries.put(paques.plusDays(39), "Ascension");
        feries.put(paques.plusDays(50), "Lundi de Pentecôte");
        feries.put(LocalDate.of(annee, Month.JULY, 14), "Fête nationale");
        feries.put(LocalDate.of(annee, Month.AUGUST, 15), "Assomption");
        feries.put(LocalDate.of(annee, Month.NOVEMBER, 1), "Toussaint");
        feries.put(LocalDate.of(annee, Month.NOVEMBER, 11), "Armistice 1918");
        feries.put(LocalDate.of(annee, Month.DECEMBER, 25), "Noël");
        return Collections.unmodifiableMap(feries);
    }

    public static boolean estFerie(LocalDate date) {
        return de(date.getYear()).containsKey(date);
    }
}
