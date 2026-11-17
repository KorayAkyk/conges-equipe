package fr.congesequipe.securite;

import fr.congesequipe.service.UtilisateurVue;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/** Emet le jeton JWT signe (HS256) remis apres une connexion reussie. */
@Service
public class JetonService {

    private final JwtEncoder encodeur;
    private final JwtProprietes proprietes;
    private final Clock horloge;

    public JetonService(JwtEncoder encodeur, JwtProprietes proprietes, Clock horloge) {
        this.encodeur = encodeur;
        this.proprietes = proprietes;
        this.horloge = horloge;
    }

    public Jeton emettre(UtilisateurVue utilisateur) {
        Instant maintenant = Instant.now(horloge);
        Instant expiration = maintenant.plus(proprietes.duree());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(proprietes.emetteur())
                .subject(String.valueOf(utilisateur.id()))
                .issuedAt(maintenant)
                .expiresAt(expiration)
                .claim("roles", List.of(utilisateur.role().name()))
                .claim("nom", utilisateur.nomComplet())
                .build();
        JwsHeader entete = JwsHeader.with(MacAlgorithm.HS256).build();
        String valeur = encodeur.encode(JwtEncoderParameters.from(entete, claims)).getTokenValue();
        return new Jeton(valeur, expiration);
    }

    public record Jeton(String valeur, Instant expiration) {
    }
}
