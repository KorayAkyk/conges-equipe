package fr.congesequipe.domaine;

import java.time.LocalDate;

/** Intervalle de dates, bornes incluses. */
public record Periode(LocalDate debut, LocalDate fin) {

    public Periode {
        if (debut == null || fin == null) {
            throw new RegleMetierException("DATES_OBLIGATOIRES", "Les dates de début et de fin sont obligatoires.");
        }
        if (fin.isBefore(debut)) {
            throw new RegleMetierException("DATES_INVERSEES", "La date de fin précède la date de début.");
        }
    }

    public boolean chevauche(Periode autre) {
        return !debut.isAfter(autre.fin) && !fin.isBefore(autre.debut);
    }

    public boolean contient(LocalDate date) {
        return !date.isBefore(debut) && !date.isAfter(fin);
    }
}
