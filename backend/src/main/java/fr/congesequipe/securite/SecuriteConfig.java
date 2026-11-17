package fr.congesequipe.securite;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * API sans etat : chaque requete porte un jeton JWT dans l'en-tete Authorization. Pas de cookie de
 * session, donc pas de CSRF a gerer. Les roles du jeton deviennent des autorites ROLE_*.
 */
@Configuration
public class SecuriteConfig {

    @Bean
    SecurityFilterChain filtres(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.POST, "/api/auth/connexion").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/health/**", "/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/demandes/a-decider").hasAnyRole("MANAGER", "RH")
                        .requestMatchers(HttpMethod.POST, "/api/demandes/*/valider", "/api/demandes/*/refuser")
                        .hasAnyRole("MANAGER", "RH")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().denyAll())
                .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(convertisseur())));
        return http.build();
    }

    @Bean
    SecretKey cleJwt(JwtProprietes proprietes) {
        return new SecretKeySpec(proprietes.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey cle) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(cle));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey cle, JwtProprietes proprietes) {
        NimbusJwtDecoder decodeur = NimbusJwtDecoder.withSecretKey(cle).macAlgorithm(MacAlgorithm.HS256).build();
        decodeur.setJwtValidator(JwtValidators.createDefaultWithIssuer(proprietes.emetteur()));
        return decodeur;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static JwtAuthenticationConverter convertisseur() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("roles");
        roles.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter convertisseur = new JwtAuthenticationConverter();
        convertisseur.setJwtGrantedAuthoritiesConverter(roles);
        return convertisseur;
    }
}
