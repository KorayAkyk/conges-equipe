package fr.congesequipe.persistance;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmailIgnoreCase(String email);

    List<Utilisateur> findByEquipeIdOrderByNomComplet(Long equipeId);

    long countByEquipeId(Long equipeId);

    /**
     * Charge l'utilisateur en verrouillant sa ligne jusqu'a la fin de la transaction : deux demandes
     * envoyees en meme temps par la meme personne sont traitees l'une apres l'autre, ce qui garde le
     * controle du solde et des chevauchements exact.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Utilisateur u where u.id = :id")
    Optional<Utilisateur> verrouiller(@Param("id") Long id);
}
