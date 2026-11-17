package fr.congesequipe.web;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/** Documentation OpenAPI, servie par Swagger UI sur /swagger-ui.html. */
@Configuration
@OpenAPIDefinition(info = @Info(title = "API conges-equipe", version = "1.0.0",
        description = "Gestion des congés d'une équipe. Projet de démonstration, données inventées."))
@SecurityScheme(name = "jwt", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}
