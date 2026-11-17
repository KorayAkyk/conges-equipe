package fr.congesequipe.persistance;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DroitCongesRepository extends JpaRepository<DroitConges, Long> {

    List<DroitConges> findByUtilisateurId(Long utilisateurId);
}
