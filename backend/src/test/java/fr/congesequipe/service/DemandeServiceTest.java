package fr.congesequipe.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.congesequipe.domaine.AccesInterditException;
import fr.congesequipe.domaine.Periode;
import fr.congesequipe.domaine.RegleMetierException;
import fr.congesequipe.domaine.Role;
import fr.congesequipe.domaine.StatutDemande;
import fr.congesequipe.domaine.TypeConge;
import fr.congesequipe.persistance.DemandeConge;
import fr.congesequipe.persistance.DemandeCongeRepository;
import fr.congesequipe.persistance.Equipe;
import fr.congesequipe.persistance.HistoriqueDemandeRepository;
import fr.congesequipe.persistance.Utilisateur;
import fr.congesequipe.persistance.UtilisateurRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/** Regles d'autorisation et de solde du service, sans base de donnees. */
@ExtendWith(MockitoExtension.class)
class DemandeServiceTest {

    @Mock
    DemandeCongeRepository demandes;
    @Mock
    UtilisateurRepository utilisateurs;
    @Mock
    HistoriqueDemandeRepository historique;
    @Mock
    SoldeService soldes;
    @Mock
    EffectifService effectif;

    DemandeService service;
    Utilisateur manager;
    Utilisateur autreManager;
    Utilisateur employe;
    DemandeConge demande;

    @BeforeEach
    void preparer() {
        Clock horloge = Clock.fixed(Instant.parse("2026-11-16T08:00:00Z"), ZoneId.of("Europe/Paris"));
        service = new DemandeService(demandes, utilisateurs, historique, soldes, effectif, horloge);
        Equipe equipe = new Equipe("Equipe A");
        ReflectionTestUtils.setField(equipe, "id", 1L);
        manager = utilisateur(10L, Role.MANAGER, equipe);
        autreManager = utilisateur(20L, Role.MANAGER, null);
        employe = utilisateur(30L, Role.EMPLOYE, equipe);
        equipe.setManagerId(10L);
        demande = new DemandeConge(employe, TypeConge.CONGES_PAYES,
                new Periode(LocalDate.of(2026, 12, 7), LocalDate.of(2026, 12, 8)), false, false,
                new BigDecimal("2.0"), null, Instant.EPOCH);
        ReflectionTestUtils.setField(demande, "id", 100L);
    }

    @Test
    void leManagerValideUneDemandeDeSonEquipe() {
        when(utilisateurs.findById(10L)).thenReturn(Optional.of(manager));
        when(demandes.findById(100L)).thenReturn(Optional.of(demande));
        when(effectif.joursSousEffectif(demande)).thenReturn(List.of());

        DemandeVue vue = service.valider(10L, 100L);

        assertThat(vue.statut()).isEqualTo(StatutDemande.VALIDEE);
        verify(historique).save(any());
    }

    @Test
    void unManagerNeValidePasHorsDeSonEquipe() {
        when(utilisateurs.findById(20L)).thenReturn(Optional.of(autreManager));
        when(demandes.findById(100L)).thenReturn(Optional.of(demande));

        assertThatThrownBy(() -> service.valider(20L, 100L))
                .isInstanceOf(AccesInterditException.class)
                .hasMessageContaining("votre équipe");
        assertThat(demande.getStatut()).isEqualTo(StatutDemande.EN_ATTENTE);
        verify(historique, never()).save(any());
    }

    @Test
    void unManagerNeDecidePasDeSaPropreDemande() {
        DemandeConge sienne = new DemandeConge(manager, TypeConge.RTT,
                new Periode(LocalDate.of(2026, 12, 7), LocalDate.of(2026, 12, 7)), false, false,
                new BigDecimal("1.0"), null, Instant.EPOCH);
        when(utilisateurs.findById(10L)).thenReturn(Optional.of(manager));
        when(demandes.findById(5L)).thenReturn(Optional.of(sienne));

        assertThatThrownBy(() -> service.valider(10L, 5L))
                .isInstanceOf(AccesInterditException.class)
                .hasMessageContaining("propre demande");
    }

    @Test
    void unRefusSansMotifNeLaisseAucuneTrace() {
        when(utilisateurs.findById(10L)).thenReturn(Optional.of(manager));
        when(demandes.findById(100L)).thenReturn(Optional.of(demande));

        assertThatThrownBy(() -> service.refuser(10L, 100L, " "))
                .isInstanceOf(RegleMetierException.class);
        verify(historique, never()).save(any());
    }

    @Test
    void unSoldeInsuffisantEmpecheLEnregistrement() {
        when(utilisateurs.verrouiller(30L)).thenReturn(Optional.of(employe));
        when(demandes.chevauchantes(any(), any(), any(), any())).thenReturn(List.of());
        when(soldes.solde(30L, TypeConge.RTT)).thenReturn(new SoldeVue(TypeConge.RTT, "RTT",
                new BigDecimal("10.0"), new BigDecimal("9.0"), BigDecimal.ZERO, new BigDecimal("1.0")));

        assertThatThrownBy(() -> service.creer(30L, TypeConge.RTT,
                new Periode(LocalDate.of(2026, 12, 7), LocalDate.of(2026, 12, 8)), false, false, null))
                .isInstanceOf(RegleMetierException.class)
                .extracting("code").isEqualTo("SOLDE_INSUFFISANT");
        verify(demandes, never()).save(any());
    }

    @Test
    void lAnnulationEstReserveeALAuteur() {
        when(demandes.findById(100L)).thenReturn(Optional.of(demande));

        assertThatThrownBy(() -> service.annuler(10L, 100L)).isInstanceOf(AccesInterditException.class);
    }

    private static Utilisateur utilisateur(Long id, Role role, Equipe equipe) {
        Utilisateur u = new Utilisateur("u" + id + "@exemple.test", "Personne " + id, "x", role, equipe);
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }
}
