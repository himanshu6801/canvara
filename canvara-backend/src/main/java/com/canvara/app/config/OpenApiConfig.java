package com.canvara.app.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) documentation setup.
 *
 * Once the app is running, docs are available at:
 *   - Swagger UI : /swagger-ui.html  (or /swagger-ui/index.html)
 *   - Raw JSON   : /v3/api-docs
 *   - Raw YAML   : /v3/api-docs.yaml
 *
 * The "bearerAuth" security scheme is pre-registered so it's ready to attach
 * to endpoints via @SecurityRequirement once JWT auth is wired into the API
 * (the jjwt dependency is already on the classpath but no filter uses it yet).
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Canvara API",
        version = "0.0.1",
        description = "REST API for the Canvara art marketplace — browse, list, and manage artwork listings.",
        contact = @Contact(name = "Canvara", email = "himanshusharma6801@gmail.com")
    ),
    servers = {
        @Server(url = "/", description = "Current environment")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    type = io.swagger.v3.oas.annotations.enums.SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
