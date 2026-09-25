package de.example.app.crm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Konfigurationseigenschaften für die MailerLite-Integration, gebunden an den Präfix {@code app.mailerlite}.
 * Die Werte werden aus {@code application.yml} bzw. Umgebungsvariablen gelesen und über
 * Spring Boot's {@code @ConfigurationProperties} automatisch injiziert.
 * Pflichtfelder sind {@code apiKey} und {@code fromEmail}; {@code groupId} ist optional
 * und steuert, welcher MailerLite-Gruppe neue Abonnenten zugeordnet werden.
 */
@Component
@ConfigurationProperties(prefix = "app.mailerlite")
@Data
public class MailerLiteProperties {

    /** Der API-Schlüssel zur Authentifizierung gegenüber der MailerLite REST API. */
    private String apiKey;

    /** Der Anzeigename des Absenders, der in gesendeten Kampagnen erscheint. Standardwert: {@code "Plan Prototyp Produktion"}. */
    private String fromName = "Plan Prototyp Produktion";

    /** Die Absender-E-Mail-Adresse, die in gesendeten Kampagnen erscheint. */
    private String fromEmail;

    /**
     * Die optionale MailerLite-Gruppen-ID, der neue Abonnenten beim Eintragen zugeordnet werden.
     * Ist der Wert {@code null} oder leer, werden Abonnenten keiner Gruppe zugewiesen.
     */
    private String groupId;
}
