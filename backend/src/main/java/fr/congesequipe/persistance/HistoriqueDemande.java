package fr.congesequipe.persistance;

import fr.congesequipe.domaine.StatutDemande;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Trace d'un changement d'etat. Les lignes ne sont jamais modifiees. */
@Entity
@Table(name = "historique_demande")
public class HistoriqueDemande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "demande_id", nullable = false)
    private Long demandeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ancien_statut", length = 20)
    private StatutDemande ancienStatut;

    @Enumerated(EnumType.STRING)
    @Column(name = "nouveau_statut", nullable = false, length = 20)
    private StatutDemande nouveauStatut;

    @Column(name = "auteur_id", nullable = false)
    private Long auteurId;

    @Column(length = 500)
    private String commentaire;

    @Column(nullable = false)
    private Instant horodatage;

    protected HistoriqueDemande() {
    }

    public HistoriqueDemande(Long demandeId, StatutDemande ancienStatut, StatutDemande nouveauStatut,
            Long auteurId, String commentaire, Instant horodatage) {
        this.demandeId = demandeId;
        this.ancienStatut = ancienStatut;
        this.nouveauStatut = nouveauStatut;
        this.auteurId = auteurId;
        this.commentaire = commentaire;
        this.horodatage = horodatage;
    }

    public Long getId() {
        return id;
    }

    public StatutDemande getAncienStatut() {
        return ancienStatut;
    }

    public StatutDemande getNouveauStatut() {
        return nouveauStatut;
    }

    public Long getAuteurId() {
        return auteurId;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public Instant getHorodatage() {
        return horodatage;
    }
}
