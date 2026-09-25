package de.example.app.cms.dto;

import de.example.app.cms.entity.BlogPost;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO (Data Transfer Object) zur Anzeige eines Blogbeitrags in Thymeleaf-Templates.
 * Bietet eine vereinfachte, präsentationsorientierte Sicht auf die {@link BlogPost}-Entität
 * und entkoppelt die View-Schicht von der JPA-Entität. Wird sowohl in der öffentlichen
 * Blog-Übersicht als auch im Admin-Bereich verwendet.
 * Die Lombok-Annotation {@code @Data} generiert automatisch Getter, Setter, equals, hashCode und toString.
 */
@Data
public class BlogPostViewDto {

    /** Eindeutige UUID des Blogbeitrags, wie sie in der Datenbank gespeichert ist. */
    private UUID id;

    /** Anzeigetitel des Blogbeitrags. */
    private String title;

    /** URL-freundlicher Bezeichner des Blogbeitrags, der als Pfadsegment in der öffentlichen URL dient. */
    private String slug;

    /** Vollständiger Textinhalt des Blogbeitrags, gegebenenfalls als HTML. */
    private String content;

    /** Bild des Blogbeitrags als Base64-kodierter String, direkt in {@code <img src="...">} verwendbar. */
    private String image;

    /** Veröffentlichungsstatus; {@code true} bedeutet, der Beitrag ist öffentlich sichtbar. */
    private boolean published;

    /** Zeitpunkt, zu dem der Blogbeitrag erstmalig angelegt wurde. */
    private LocalDateTime createdAt;

    /** Zeitpunkt der letzten inhaltlichen oder statusbezogenen Änderung des Blogbeitrags. */
    private LocalDateTime updatedAt;

    /**
     * Statische Factory-Methode zur Konvertierung einer {@link BlogPost}-Entität in ein {@link BlogPostViewDto}.
     *
     * @param blogPost Die zu konvertierende {@link BlogPost}-Entität.
     * @return Ein neues {@link BlogPostViewDto}-Objekt.
     */
    public static BlogPostViewDto fromEntity(BlogPost blogPost) {
        BlogPostViewDto dto = new BlogPostViewDto();
        dto.setId(blogPost.getId());
        dto.setTitle(blogPost.getTitle());
        dto.setSlug(blogPost.getSlug());
        dto.setContent(blogPost.getContent());
        dto.setImage(blogPost.getImage());
        dto.setPublished(blogPost.isPublished());
        dto.setCreatedAt(blogPost.getCreatedAt());
        dto.setUpdatedAt(blogPost.getUpdatedAt());
        return dto;
    }
}
