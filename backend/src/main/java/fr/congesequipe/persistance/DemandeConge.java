package fr.congesequipe.persistance;

import fr.congesequipe.domaine.Periode;
import fr.congesequipe.domaine.RegleDemande;
import fr.congesequipe.domaine.StatutDemande;
import fr.congesequipe.domaine.TypeConge;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Demande d'absence. Les changements d'etat passent par les methodes, qui appliquent les regles. */
@Entity
@Table(name = "demande_conge")
public class DemandeConge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demandeur_id")
    private Utilisateur demandeur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeConge type;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Column(name = "debut_apres_midi", nullable = false)
    private boolean debutApresMidi;

    @Column(name = "fin_matin", nullable = false)
    private boolean finMatin;

    @Column(nullable = false, precision = 5, scale = 1)
    private BigDecimal jours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutDemande statut;

    @Column(name = "motif_refus", length = 500)
    private String motifRefus;

    @Column(length = 500)
    private String commentaire;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    @Version
    private long version;

    protected DemandeConge() {
    }

    public DemandeConge(Utilisateur demandeur, TypeConge type, Periode periode, boolean debutApresMidi,
            boolean finMatin, BigDecimal jours, String commentaire, Instant creeLe) {
        this.demandeur = demandeur;
        this.type = type;
        this.dateDebut = periode.debut();
        this.dateFin = periode.fin();
        this.debutApresMidi = debutApresMidi;
        this.finMatin = finMatin;
        this.jours = jours;
        this.commentaire = commentaire;
        this.creeLe = creeLe;
        this.statut = StatutDemande.EN_ATTENTE;
    }

    public void valider() {
        RegleDemande.verifierDecisionPossible(statut);
        statut = StatutDemande.VALIDEE;
    }

    public void refuser(String motif) {
        RegleDemande.verifierDecisionPossible(statut);
        motifRefus = RegleDemande.verifierMotif(motif);
        statut = StatutDemande.REFUSEE;
    }

    public void annuler(LocalDate aujourdhui) {
        RegleDemande.verifierAnnulation(statut, dateDebut, aujourdhui);
        statut = StatutDemande.ANNULEE;
    }

    public Periode periode() {
        return new Periode(dateDebut, dateFin);
    }

    public Long getId() {
        return id;
    }

    public Utilisateur getDemandeur() {
        return demandeur;
    }

    public TypeConge getType() {
        return type;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public boolean isDebutApresMidi() {
        return debutApresMidi;
    }

    public boolean isFinMatin() {
        return finMatin;
    }

    public BigDecimal getJours() {
        return jours;
    }

    public StatutDemande getStatut() {
        return statut;
    }

    public String getMotifRefus() {
        return motifRefus;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public Instant getCreeLe() {
        return creeLe;
    }
}
