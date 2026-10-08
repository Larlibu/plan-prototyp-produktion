package de.example.app.feed.dto;

import de.example.app.feed.entity.FeedItem;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) für einen Feed-Eintrag.
 * Wird verwendet, um Feed-Daten zwischen den Schichten zu transportieren,
 * ohne die JPA-Entität direkt zu exponieren.
 */
@Data
public class FeedItemDto {

    /** Eindeutiger Bezeichner des Feed-Eintrags. */
    private UUID id;

    /** Titel des Feed-Eintrags. */
    private String title;

    /** Kurzbeschreibung des Feed-Eintrags. */
    private String description;

    /** URL zum zugehörigen Blog-Post. */
    private String link;

    /** Veröffentlichungsdatum des Feed-Eintrags. */
    private LocalDateTime pubDate;

    /** Global eindeutiger Bezeichner (GUID) des Feed-Eintrags. */
    private String guid;

    /** UUID des zugehörigen Blog-Posts. */
    private UUID blogPostId;

    /**
     * Erstellt ein {@link FeedItemDto} aus einer {@link FeedItem}-Entität.
     * Kopiert alle relevanten Felder von der Entität in das DTO.
     *
     * @param entity die {@link FeedItem}-Entität, aus der das DTO erstellt wird
     * @return ein neu befülltes {@link FeedItemDto}-Objekt
     */
    public static FeedItemDto fromEntity(FeedItem entity) {
        FeedItemDto dto = new FeedItemDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setLink(entity.getLink());
        dto.setPubDate(entity.getPubDate());
        dto.setGuid(entity.getGuid());
        dto.setBlogPostId(entity.getBlogPostId());
        return dto;
    }
}
