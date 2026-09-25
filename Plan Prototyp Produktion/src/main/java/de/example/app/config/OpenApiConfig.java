package de.example.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring-Konfigurationsklasse für die Springdoc OpenAPI-Integration.
 * Definiert benutzerdefinierte API-Metadaten wie Titel, Version, Beschreibung
 * und Lizenzinformationen, die in der Swagger UI unter
 * {@code /swagger-ui.html} angezeigt werden.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Erstellt und konfiguriert das {@link OpenAPI}-Bean mit den Metadaten dieser Anwendung.
     * Die hier hinterlegten Informationen erscheinen im Swagger-UI-Header und in
     * exportierten OpenAPI-Spezifikationsdateien.
     *
     * @return eine konfigurierte {@link OpenAPI}-Instanz mit Titel, Version,
     *         Beschreibung, Nutzungsbedingungen und Lizenz
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Webplattform API")
                        .version("1.0")
                        .description("API-Dokumentation für die integrierte Webplattform (CMS, Shop, CRM)")
                        .termsOfService("http://swagger.io/terms/")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }
}
