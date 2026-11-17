package fr.congesequipe.persistance;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoriqueDemandeRepository extends JpaRepository<HistoriqueDemande, Long> {

    List<HistoriqueDemande> findByDemandeIdOrderByHorodatageAscIdAsc(Long demandeId);
}
