package de.example.app.cms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO (Data Transfer Object) zum Erstellen eines neuen Blogbeitrags.
 * Enthält Validierungsregeln für die Eingabefelder, die von der Benutzeroberfläche kommen.
 * Die Lombok-Annotation {@code @Data} generiert automatisch Getter, Setter, equals, hashCode und toString.
 */
@Data
public class BlogPostCreateDto {

    /** Titel des neuen Blogbeitrags; darf nicht leer sein und maximal 255 Zeichen lang sein. */
    @NotBlank(message = "Der Titel darf nicht leer sein.")
    @Size(max = 255, message = "Der Titel darf maximal 255 Zeichen lang sein.")
    private String title;

    /** URL-freundlicher Bezeichner des Blogbeitrags (z.B. {@code "gesunder-smoothie-rezepte"}); muss eindeutig sein. */
    @NotBlank(message = "Der Slug darf nicht leer sein.")
    @Size(max = 255, message = "Der Slug darf maximal 255 Zeichen lang sein.")
    private String slug;

    /** Vollständiger HTML- oder Fließtext-Inhalt des Blogbeitrags; darf nicht leer sein. */
    @NotBlank(message = "Der Inhalt darf nicht leer sein.")
    private String content;

    /** Optional hochgeladene Bilddatei für den Beitrag. Wird von Spring als {@link org.springframework.web.multipart.MultipartFile} entgegengenommen und in der Service-Schicht verarbeitet. */
    private MultipartFile imageFile;
}
