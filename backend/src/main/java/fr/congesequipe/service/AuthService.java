package fr.congesequipe.service;

import fr.congesequipe.persistance.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UtilisateurRepository utilisateurs;
    private final PasswordEncoder encodeur;

    public AuthService(UtilisateurRepository utilisateurs, PasswordEncoder encodeur) {
        this.utilisateurs = utilisateurs;
        this.encodeur = encodeur;
    }

    @Transactional(readOnly = true)
    public UtilisateurVue verifier(String email, String motDePasse) {
        return utilisateurs.findByEmailIgnoreCase(email == null ? "" : email.strip())
                .filter(u -> motDePasse != null && encodeur.matches(motDePasse, u.getMotDePasse()))
                .map(UtilisateurVue::de)
                .orElseThrow(IdentifiantsInvalidesException::new);
    }
}
