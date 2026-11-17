package fr.congesequipe.service;

import fr.congesequipe.domaine.TypeConge;
import java.math.BigDecimal;

/** Solde d'un type de conges : disponible = acquis - pris - en attente. */
public record SoldeVue(TypeConge type, String libelle, BigDecimal acquis, BigDecimal pris,
        BigDecimal enAttente, BigDecimal disponible) {
}
