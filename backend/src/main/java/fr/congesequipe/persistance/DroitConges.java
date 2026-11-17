package fr.congesequipe.persistance;

import fr.congesequipe.domaine.TypeConge;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** Jours acquis par une personne pour un type de conges. Le solde se calcule a partir des demandes. */
@Entity
@Table(name = "droit_conges")
public class DroitConges {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "utilisateur_id", nullable = false)
    private Long utilisateurId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeConge type;

    @Column(name = "jours_acquis", nullable = false, precision = 5, scale = 1)
    private BigDecimal joursAcquis;

    protected DroitConges() {
    }

    public DroitConges(Long utilisateurId, TypeConge type, BigDecimal joursAcquis) {
        this.utilisateurId = utilisateurId;
        this.type = type;
        this.joursAcquis = joursAcquis;
    }

    public Long getUtilisateurId() {
        return utilisateurId;
    }

    public TypeConge getType() {
        return type;
    }

    public BigDecimal getJoursAcquis() {
        return joursAcquis;
    }
}
