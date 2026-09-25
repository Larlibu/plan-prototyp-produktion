package de.example.app.crm.service;

import de.example.app.crm.config.MailerLiteProperties;
import de.example.app.crm.entity.NewsletterSubscriber;
import de.example.app.crm.repository.NewsletterSubscriberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service zur Integration mit der MailerLite REST API.
 * Verantwortlich für das Hinzufügen einzelner Abonnenten, die Synchronisierung von
 * Abonnentenlisten sowie das Erstellen und sofortige Versenden von Newsletter-Kampagnen.
 * Bei einem 422-Fehler mit dem Status "unsubscribed" wird der entsprechende lokale
 * Datenbankeintrag automatisch gelöscht, da eine Wiederanmeldung über MailerLite gesperrt ist.
 * Alle API-Fehler werden als {@link MailerLiteException} weitergereicht.
 */
@Service
public class MailerLiteService {

    private static final Logger log = LoggerFactory.getLogger(MailerLiteService.class);
    private static final String BASE_URL = "https://connect.mailerlite.com/api";

    private final MailerLiteProperties props;
    private final RestClient restClient;
    private final NewsletterSubscriberRepository newsletterSubscriberRepository;

    /**
     * Erstellt einen neuen {@code MailerLiteService} und konfiguriert den {@link RestClient}
     * mit Basis-URL, API-Schlüssel sowie Standard-Headern für JSON-Kommunikation.
     *
     * @param props                         die gebundenen MailerLite-Konfigurationswerte (API-Schlüssel, Absender usw.)
     * @param newsletterSubscriberRepository das Repository zum Löschen abgemeldeter Abonnenten aus der lokalen DB
     */
    public MailerLiteService(MailerLiteProperties props, NewsletterSubscriberRepository newsletterSubscriberRepository) {
        this.props = props;
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("Authorization", "Bearer " + props.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
        this.newsletterSubscriberRepository = newsletterSubscriberRepository;
    }

    /**
     * Fügt eine einzelne E-Mail-Adresse als Abonnent in MailerLite hinzu.
     * Bei einem 422-Fehler mit "unsubscribed" wird die Ausnahme weitergereicht.
     *
     * @param email Die E-Mail-Adresse des neuen Abonnenten
     * @throws MailerLiteException bei API-Fehlern
     */
    public void addSubscriber(String email) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        addGroupIdIfPresent(body);
        try {
            restClient.post()
                    .uri("/subscribers")
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Abonnent '{}' erfolgreich in MailerLite eingetragen.", email);
        } catch (RestClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            if (e.getStatusCode().value() == 422 && responseBody.contains("unsubscribed")) {
                log.warn("Abonnent '{}' hat sich über MailerLite abgemeldet und kann nicht erneut importiert werden.", email);
                throw new IllegalArgumentException(
                        "Diese E-Mail-Adresse wurde über einen Newsletter abgemeldet und kann nicht erneut angemeldet werden.");
            }
            log.warn("Abonnent '{}' konnte nicht in MailerLite eingetragen werden: {} {}",
                    email, e.getStatusCode(), responseBody);
            throw new MailerLiteException("MailerLite-Fehler beim Eintragen des Abonnenten: " + responseBody, e);
        }
    }

    /**
     * Synchronisiert die Abonnenten mit MailerLite, erstellt eine Kampagne und versendet sie sofort.
     *
     * @param subject     Betreff der E-Mail
     * @param htmlContent HTML-Inhalt der E-Mail
     * @param emails      Liste aktiver Abonnenten-E-Mails aus der DB
     * @return Anzahl der Empfänger
     * @throws MailerLiteException wenn ein Fehler bei der Kommunikation mit MailerLite auftritt
     */
    public int sendNewsletter(String subject, String htmlContent, List<String> emails) {
        log.info("Starte Newsletter-Versand: Betreff='{}', Empfänger={}", subject, emails.size());

        int actualRecipients = syncSubscribers(emails);
        if (actualRecipients == 0) {
            log.warn("Keine aktiven Abonnenten zum Versenden des Newsletters gefunden.");
            return 0;
        }

        String campaignId = createCampaign(subject, htmlContent);
        sendImmediately(campaignId);

        log.info("Newsletter erfolgreich versendet. Kampagnen-ID={}, Empfänger={}", campaignId, actualRecipients);
        return actualRecipients;
    }

    // /subscribers/import erwartet eine CSV-Datei (Multipart).
    // Für JSON-basiertes Upsert wird POST /subscribers pro E-Mail verwendet.
    private int syncSubscribers(List<String> emails) {
        int synced = 0;
        int failed = 0;
        int unsubscribed = 0;

        for (String email : emails) {
            Map<String, Object> body = new HashMap<>();
            body.put("email", email);
            addGroupIdIfPresent(body);
            try {
                restClient.post()
                        .uri("/subscribers")
                        .body(body)
                        .retrieve()
                        .toBodilessEntity();
                synced++;
            } catch (RestClientResponseException e) {
                String responseBody = e.getResponseBodyAsString();
                if (e.getStatusCode().value() == 422 && responseBody.contains("unsubscribed")) {
                    log.info("Abonnent '{}' in MailerLite abgemeldet – entferne lokalen DB-Eintrag.", email);
                    newsletterSubscriberRepository.findByEmailIgnoreCase(email).ifPresent(subscriber -> {
                        newsletterSubscriberRepository.delete(subscriber);
                        log.info("Lokaler DB-Eintrag für '{}' gelöscht.", email);
                    });
                    unsubscribed++;
                } else {
                    log.warn("Abonnent '{}' konnte nicht synchronisiert werden: {} {}",
                            email, e.getStatusCode(), responseBody);
                    failed++;
                }
            }
        }
        log.info("Abonnenten synchronisiert: {} erfolgreich, {} abgemeldet (in MailerLite), {} fehlgeschlagen", synced, unsubscribed, failed);
        if (synced == 0 && emails.size() > 0) {
            throw new MailerLiteException("Kein Abonnent konnte in MailerLite synchronisiert werden.");
        }
        return synced;
    }

    @SuppressWarnings("unchecked")
    private String createCampaign(String subject, String htmlContent) {
        Map<String, Object> emailObj = new HashMap<>();
        emailObj.put("subject", subject);
        emailObj.put("from_name", props.getFromName());
        emailObj.put("from", props.getFromEmail());
        emailObj.put("content", htmlContent);

        Map<String, Object> body = new HashMap<>();
        body.put("name", subject + " – " + LocalDate.now());
        body.put("type", "regular");
        body.put("emails", List.of(emailObj));
        addGroupIdIfPresent(body);

        try {
            Map<String, Object> response = restClient.post()
                    .uri("/campaigns")
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            String campaignId = ((Map<String, Object>) response.get("data")).get("id").toString();
            log.info("Kampagne erstellt: ID={}", campaignId);
            return campaignId;
        } catch (RestClientResponseException e) {
            log.error("Fehler beim Erstellen der Kampagne: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new MailerLiteException("MailerLite-Fehler beim Erstellen der Kampagne: " + e.getResponseBodyAsString(), e);
        }
    }

    private void sendImmediately(String campaignId) {
        try {
            restClient.post()
                    .uri("/campaigns/{id}/schedule", campaignId)
                    .body(Map.of("delivery", "instant"))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Kampagne {} sofort gesendet.", campaignId);
        } catch (RestClientResponseException e) {
            log.error("Fehler beim Senden der Kampagne {}: {} {}", campaignId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new MailerLiteException("MailerLite-Fehler beim Versenden: " + e.getResponseBodyAsString(), e);
        }
    }

    private void addGroupIdIfPresent(Map<String, Object> body) {
        if (props.getGroupId() != null && !props.getGroupId().isBlank()) {
            body.put("groups", List.of(props.getGroupId()));
        }
    }
}