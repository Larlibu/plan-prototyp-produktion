package de.example.app.cms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * DTO (Data Transfer Object) zum Aktualisieren eines bestehenden Blogbeitrags.
 * Enthält Validierungsregeln für die Eingabefelder, die von der Benutzeroberfläche kommen.
 * Die Lombok-Annotation {@code @Data} generiert automatisch Getter, Setter, equals, hashCode und toString.
 */
@Data
public class BlogPostUpdateDto {

    /** Eindeutige UUID des zu aktualisierenden Blogbeitrags; wird vom Controller aus dem Pfad befüllt. */
    private UUID id;

    /** Aktualisierter Titel des Blogbeitrags; darf nicht leer sein und maximal 255 Zeichen lang sein. */
    @NotBlank(message = "Der Titel darf nicht leer sein.")
    @Size(max = 255, message = "Der Titel darf maximal 255 Zeichen lang sein.")
    private String title;

    /** Aktualisierter URL-freundlicher Bezeichner des Blogbeitrags; muss eindeutig in der Datenbank sein. */
    @NotBlank(message = "Der Slug darf nicht leer sein.")
    @Size(max = 255, message = "Der Slug darf maximal 255 Zeichen lang sein.")
    private String slug;

    /** Aktualisierter vollständiger Inhalt des Blogbeitrags; darf nicht leer sein. */
    @NotBlank(message = "Der Inhalt darf nicht leer sein.")
    private String content;

    /** Optional neu hochgeladene Bilddatei, die das vorhandene Bild ersetzen soll. Kann {@code null} sein. */
    private MultipartFile imageFile;

    /** Veröffentlichungsstatus des Blogbeitrags; {@code true} bedeutet öffentlich sichtbar. */
    private boolean published;

    /** Das aktuell in der Datenbank gespeicherte Bild als Base64-String, zur Anzeige im Formular oder zum expliziten Entfernen. */
    private String existingImage;
}
