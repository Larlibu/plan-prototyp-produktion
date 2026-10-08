package de.example.app.crm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO (Data Transfer Object) zum Erfassen der Eingabedaten für den Newsletter-Versand im Admin-Bereich.
 * Wird im Compose-Formular verwendet und enthält Betreff sowie HTML-Inhalt der zu versendenden Kampagne.
 * Beide Felder unterliegen einer {@code @NotBlank}-Validierung, da MailerLite weder einen leeren
 * Betreff noch leeren Inhalt akzeptiert.
 */
@Data
public class NewsletterSendDto {

    /** Betreff der Newsletter-Kampagne; darf nicht leer sein. */
    @NotBlank(message = "Betreff darf nicht leer sein.")
    private String subject;

    /** HTML-Inhalt der Newsletter-Kampagne; darf nicht leer sein. */
    @NotBlank(message = "Inhalt darf nicht leer sein.")
    private String htmlContent;
}
