package de.example.app.crm.dto;

import de.example.app.crm.entity.NewsletterSubscriber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO (Data Transfer Object) für Newsletter-Abonnenten-Operationen (z.B. Anmeldung, Anzeige).
 * Enthält Validierungsregeln für das E-Mail-Feld.
 */
@Data
public class NewsletterSubscriberDto {
    private UUID id;

    @NotBlank(message = "Die E-Mail-Adresse darf nicht leer sein.")
    @Email(message = "Ungültiges E-Mail-Format.")
    private String email;

    private boolean active;
    private LocalDateTime createdAt;

    /**
     * Statische Factory-Methode zur Konvertierung einer {@link NewsletterSubscriber}-Entität in ein {@link NewsletterSubscriberDto}.
     *
     * @param subscriber Die zu konvertierende {@link NewsletterSubscriber}-Entität.
     * @return Ein neues {@link NewsletterSubscriberDto}-Objekt.
     */
    public static NewsletterSubscriberDto fromEntity(NewsletterSubscriber subscriber) {
        NewsletterSubscriberDto dto = new NewsletterSubscriberDto();
        dto.setId(subscriber.getId());
        dto.setEmail(subscriber.getEmail());
        dto.setActive(subscriber.isActive());
        dto.setCreatedAt(subscriber.getCreatedAt());
        return dto;
    }
}
