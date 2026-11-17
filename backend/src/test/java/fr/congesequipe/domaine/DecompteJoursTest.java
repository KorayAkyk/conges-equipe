package fr.congesequipe.domaine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DecompteJoursTest {

    @ParameterizedTest(name = "{0} au {1}, apres-midi={2}, matin={3} : {4} jours")
    @CsvSource({
        // Semaine complete sans ferie
        "2026-12-07, 2026-12-11, false, false, 5.0",
        // Le week-end ne compte pas
        "2026-12-10, 2026-12-15, false, false, 4.0",
        // Semaine de Noel : le vendredi 25 est ferie
        "2026-12-21, 2026-12-25, false, false, 4.0",
        // Semaine de l'Ascension (jeudi 14 mai 2026)
        "2026-05-11, 2026-05-15, false, false, 4.0",
        // Demi-journees aux deux bouts
        "2026-12-07, 2026-12-11, true, true, 4.0",
        "2026-12-07, 2026-12-11, true, false, 4.5",
        // Une seule demi-journee
        "2026-12-08, 2026-12-08, true, false, 0.5",
        "2026-12-08, 2026-12-08, false, true, 0.5",
        // Demi-journee posee sur un samedi : rien a retirer
        "2026-12-12, 2026-12-15, true, false, 2.0"
    })
    void decompteLesJoursOuvres(LocalDate debut, LocalDate fin, boolean apresMidi, boolean matin, BigDecimal attendu) {
        assertThat(DecompteJours.decompter(new Periode(debut, fin), apresMidi, matin)).isEqualByComparingTo(attendu);
    }

    @Test
    void refuseUnePeriodeSansJourOuvre() {
        Periode weekEnd = new Periode(LocalDate.of(2026, 12, 12), LocalDate.of(2026, 12, 13));
        assertThatThrownBy(() -> DecompteJours.decompter(weekEnd, false, false))
                .isInstanceOf(RegleMetierException.class)
                .extracting("code").isEqualTo("AUCUN_JOUR_OUVRE");
    }

    @Test
    void refuseUnJourFerieSeul() {
        Periode noel = new Periode(LocalDate.of(2026, 12, 25), LocalDate.of(2026, 12, 25));
        assertThatThrownBy(() -> DecompteJours.decompter(noel, false, false))
                .isInstanceOf(RegleMetierException.class);
    }

    @Test
    void refuseUnJourQuiCommenceLApresMidiEtFinitLeMatin() {
        Periode jour = new Periode(LocalDate.of(2026, 12, 8), LocalDate.of(2026, 12, 8));
        assertThatThrownBy(() -> DecompteJours.decompter(jour, true, true))
                .isInstanceOf(RegleMetierException.class)
                .extracting("code").isEqualTo("DEMI_JOURNEES_INCOHERENTES");
    }

    @Test
    void listeLesJoursOuvresDUnePeriode() {
        assertThat(DecompteJours.joursOuvres(new Periode(LocalDate.of(2026, 12, 24), LocalDate.of(2026, 12, 28))))
                .containsExactly(LocalDate.of(2026, 12, 24), LocalDate.of(2026, 12, 28));
    }

    @Test
    void periodeRefuseUneFinAvantLeDebut() {
        assertThatThrownBy(() -> new Periode(LocalDate.of(2026, 12, 9), LocalDate.of(2026, 12, 8)))
                .isInstanceOf(RegleMetierException.class)
                .extracting("code").isEqualTo("DATES_INVERSEES");
    }

    @Test
    void periodeDetecteLesChevauchements() {
        Periode a = new Periode(LocalDate.of(2026, 12, 7), LocalDate.of(2026, 12, 11));
        assertThat(a.chevauche(new Periode(LocalDate.of(2026, 12, 11), LocalDate.of(2026, 12, 14)))).isTrue();
        assertThat(a.chevauche(new Periode(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 7)))).isTrue();
        assertThat(a.chevauche(new Periode(LocalDate.of(2026, 12, 12), LocalDate.of(2026, 12, 13)))).isFalse();
    }
}
