package fr.congesequipe.service;

import fr.congesequipe.domaine.IntrouvableException;
import fr.congesequipe.persistance.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfilService {

    private final UtilisateurRepository utilisateurs;
    private final SoldeService soldes;

    public ProfilService(UtilisateurRepository utilisateurs, SoldeService soldes) {
        this.utilisateurs = utilisateurs;
        this.soldes = soldes;
    }

    @Transactional(readOnly = true)
    public ProfilVue profil(Long utilisateurId) {
        UtilisateurVue utilisateur = utilisateurs.findById(utilisateurId).map(UtilisateurVue::de)
                .orElseThrow(() -> new IntrouvableException("Utilisateur introuvable."));
        return new ProfilVue(utilisateur, soldes.soldes(utilisateurId));
    }
}
