package fr.congesequipe.persistance;

import fr.congesequipe.domaine.StatutDemande;
import fr.congesequipe.domaine.TypeConge;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DemandeCongeRepository extends JpaRepository<DemandeConge, Long> {

    List<DemandeConge> findByDemandeurIdOrderByDateDebutDesc(Long demandeurId);

    List<DemandeConge> findByStatutOrderByDateDebutAsc(StatutDemande statut);

    /** Somme des jours d'une personne, pour un type et des statuts donnes. Null si aucune demande. */
    @Query("""
            select sum(d.jours) from DemandeConge d
            where d.demandeur.id = :demandeurId and d.type = :type and d.statut in :statuts
            """)
    BigDecimal sommeJours(@Param("demandeurId") Long demandeurId, @Param("type") TypeConge type,
            @Param("statuts") Collection<StatutDemande> statuts);

    /** Demandes d'une personne qui recouvrent une periode. */
    @Query("""
            select d from DemandeConge d
            where d.demandeur.id = :demandeurId and d.statut in :statuts
              and d.dateDebut <= :fin and d.dateFin >= :debut
            """)
    List<DemandeConge> chevauchantes(@Param("demandeurId") Long demandeurId,
            @Param("statuts") Collection<StatutDemande> statuts,
            @Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    /** Demandes des membres d'une equipe qui recouvrent une periode. */
    @Query("""
            select d from DemandeConge d join fetch d.demandeur u
            where u.equipe.id = :equipeId and d.statut in :statuts
              and d.dateDebut <= :fin and d.dateFin >= :debut
            order by d.dateDebut, u.nomComplet
            """)
    List<DemandeConge> deLEquipeSurPeriode(@Param("equipeId") Long equipeId,
            @Param("statuts") Collection<StatutDemande> statuts,
            @Param("debut") LocalDate debut, @Param("fin") LocalDate fin);
}
