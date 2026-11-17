package fr.congesequipe.service;

import java.util.List;

public record ProfilVue(UtilisateurVue utilisateur, List<SoldeVue> soldes) {
}
