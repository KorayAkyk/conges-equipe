package fr.congesequipe.domaine;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class JoursFeriesTest {

    @ParameterizedTest(name = "Paques {0} = {1}")
    @CsvSource({
        "2000, 2000-04-23",
        "2024, 2024-03-31",
        "2025, 2025-04-20",
        "2026, 2026-04-05",
        "2027, 2027-03-28",
        "2038, 2038-04-25",
        "2285, 2285-03-22"
    })
    void calculeLeDimancheDePaques(int annee, LocalDate attendu) {
        assertThat(JoursFeries.paques(annee)).isEqualTo(attendu);
    }

    /** Liste publiee par service-public.gouv.fr (fiche F2405) pour 2026. */
    @Test
    void donneLesOnzeJoursFeriesDe2026() {
        assertThat(JoursFeries.de(2026)).containsOnlyKeys(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 6), LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 8), LocalDate.of(2026, 5, 14), LocalDate.of(2026, 5, 25),
                LocalDate.of(2026, 7, 14), LocalDate.of(2026, 8, 15), LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 11), LocalDate.of(2026, 12, 25));
    }

    /** Meme source pour 2027 : Paques tombe en mars. */
    @Test
    void donneLesJoursMobilesDe2027() {
        assertThat(JoursFeries.de(2027))
                .hasSize(11)
                .containsEntry(LocalDate.of(2027, 3, 29), "Lundi de Pâques")
                .containsEntry(LocalDate.of(2027, 5, 6), "Ascension")
                .containsEntry(LocalDate.of(2027, 5, 17), "Lundi de Pentecôte");
    }

    @Test
    void renvoieLesJoursDansLOrdreChronologique() {
        assertThat(List.copyOf(JoursFeries.de(2026).keySet())).isSorted();
    }

    @Test
    void reconnaitUnJourFerieEtUnJourOrdinaire() {
        assertThat(JoursFeries.estFerie(LocalDate.of(2026, 7, 14))).isTrue();
        assertThat(JoursFeries.estFerie(LocalDate.of(2026, 7, 15))).isFalse();
        assertThat(JoursFeries.estFerie(LocalDate.of(2026, 4, 5))).as("Paques est un dimanche, pas un ferie de la liste").isFalse();
    }
}
