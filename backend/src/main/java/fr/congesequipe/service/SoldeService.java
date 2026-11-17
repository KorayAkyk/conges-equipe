package fr.congesequipe.service;

import fr.congesequipe.domaine.StatutDemande;
import fr.congesequipe.domaine.TypeConge;
import fr.congesequipe.persistance.DemandeCongeRepository;
import fr.congesequipe.persistance.DroitConges;
import fr.congesequipe.persistance.DroitCongesRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Calcule les soldes a partir des droits acquis et des demandes : aucun compteur a maintenir. */
@Service
public class SoldeService {

    private final DroitCongesRepository droits;
    private final DemandeCongeRepository demandes;

    public SoldeService(DroitCongesRepository droits, DemandeCongeRepository demandes) {
        this.droits = droits;
        this.demandes = demandes;
    }

    @Transactional(readOnly = true)
    public List<SoldeVue> soldes(Long utilisateurId) {
        Map<TypeConge, BigDecimal> acquis = droits.findByUtilisateurId(utilisateurId).stream()
                .collect(Collectors.toMap(DroitConges::getType, DroitConges::getJoursAcquis));
        return Arrays.stream(TypeConge.values())
                .map(type -> solde(utilisateurId, type, acquis.getOrDefault(type, BigDecimal.ZERO)))
                .toList();
    }

    @Transactional(readOnly = true)
    public SoldeVue solde(Long utilisateurId, TypeConge type) {
        BigDecimal acquis = droits.findByUtilisateurId(utilisateurId).stream()
                .filter(d -> d.getType() == type)
                .map(DroitConges::getJoursAcquis)
                .findFirst()
                .orElse(BigDecimal.ZERO);
        return solde(utilisateurId, type, acquis);
    }

    private SoldeVue solde(Long utilisateurId, TypeConge type, BigDecimal acquis) {
        BigDecimal pris = somme(utilisateurId, type, StatutDemande.VALIDEE);
        BigDecimal enAttente = somme(utilisateurId, type, StatutDemande.EN_ATTENTE);
        BigDecimal disponible = acquis.subtract(pris).subtract(enAttente);
        return new SoldeVue(type, type.libelle(), acquis.setScale(1), pris.setScale(1), enAttente.setScale(1),
                disponible.setScale(1));
    }

    private BigDecimal somme(Long utilisateurId, TypeConge type, StatutDemande statut) {
        BigDecimal somme = demandes.sommeJours(utilisateurId, type, Set.of(statut));
        return somme == null ? BigDecimal.ZERO : somme;
    }
}
