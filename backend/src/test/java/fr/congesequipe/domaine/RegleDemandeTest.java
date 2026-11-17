package fr.congesequipe.domaine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class RegleDemandeTest {

    private static final LocalDate LUNDI = LocalDate.of(2026, 12, 7);

    @ParameterizedTest
    @EnumSource(value = StatutDemande.class, names = {"VALIDEE", "REFUSEE", "ANNULEE"})
    void neDecidePlusUneDemandeDejaTraitee(StatutDemande statut) {
        assertThatThrownBy(() -> RegleDemande.verifierDecisionPossible(statut))
                .isInstanceOf(RegleMetierException.class)
                .extracting("code").isEqualTo("DEMANDE_DEJA_TRAITEE");
    }

    @Test
    void exigeUnMotifDeRefus() {
        assertThatThrownBy(() -> RegleDemande.verifierMotif("   "))
                .extracting("code").isEqualTo("MOTIF_OBLIGATOIRE");
        assertThatThrownBy(() -> RegleDemande.verifierMotif(null))
                .extracting("code").isEqualTo("MOTIF_OBLIGATOIRE");
        assertThat(RegleDemande.verifierMotif("  Charge de fin d'annee ")).isEqualTo("Charge de fin d'annee");
    }

    @Test
    void annuleAvantLePremierJourSeulement() {
        assertThatCode(() -> RegleDemande.verifierAnnulation(StatutDemande.VALIDEE, LUNDI, LUNDI.minusDays(1)))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> RegleDemande.verifierAnnulation(StatutDemande.VALIDEE, LUNDI, LUNDI))
                .extracting("code").isEqualTo("ANNULATION_TROP_TARDIVE");
    }

    @Test
    void nAnnulePasUneDemandeRefusee() {
        assertThatThrownBy(() -> RegleDemande.verifierAnnulation(StatutDemande.REFUSEE, LUNDI, LUNDI.minusDays(3)))
                .extracting("code").isEqualTo("ANNULATION_IMPOSSIBLE");
    }

    @Test
    void refuseUnSoldeInsuffisant() {
        assertThatCode(() -> RegleDemande.verifierSolde(new BigDecimal("5.0"), new BigDecimal("5.0"), TypeConge.RTT))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> RegleDemande.verifierSolde(new BigDecimal("4.5"), new BigDecimal("5.0"),
                TypeConge.RTT))
                .hasMessageContaining("4.5 jour(s) disponible(s)")
                .extracting("code").isEqualTo("SOLDE_INSUFFISANT");
    }

    @Test
    void signaleLesJoursOuPlusDeLaMoitieDeLEquipeEstAbsente() {
        List<LocalDate> jours = List.of(LUNDI, LUNDI.plusDays(1), LUNDI.plusDays(2));
        Map<LocalDate, Integer> autres = Map.of(LUNDI, 1, LUNDI.plusDays(1), 2);
        // Equipe de 4 : 2 absents = la moitie (pas d'alerte), 3 absents = plus de la moitie.
        assertThat(RegleDemande.joursEnSousEffectif(jours, autres, 4)).containsExactly(LUNDI.plusDays(1));
        // Equipe de 3 : 2 absents sur 3 suffit.
        assertThat(RegleDemande.joursEnSousEffectif(jours, autres, 3)).containsExactly(LUNDI, LUNDI.plusDays(1));
    }

    @Test
    void uneDemandeVivanteEstEnAttenteOuValidee() {
        assertThat(StatutDemande.EN_ATTENTE.estVivante()).isTrue();
        assertThat(StatutDemande.VALIDEE.estVivante()).isTrue();
        assertThat(StatutDemande.REFUSEE.estVivante()).isFalse();
        assertThat(StatutDemande.ANNULEE.estVivante()).isFalse();
    }
}
