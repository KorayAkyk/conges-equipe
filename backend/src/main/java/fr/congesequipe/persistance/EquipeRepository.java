package fr.congesequipe.persistance;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipeRepository extends JpaRepository<Equipe, Long> {

    List<Equipe> findByManagerId(Long managerId);
}
