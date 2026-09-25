package de.example.app.feed.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA-Entität, die einen einzelnen Eintrag im RSS- oder Atom-Feed repräsentiert.
 * Jeder Feed-Eintrag ist genau einem Blog-Post zugeordnet und wird bei
 * Statusänderungen des Blog-Posts automatisch synchronisiert.
 */
@Entity
@Table(name = "feed_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedItem {

    /**
     * Eindeutiger Primärschlüssel des Feed-Eintrags (UUID, automatisch generiert).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Titel des Feed-Eintrags, entspricht dem Titel des zugehörigen Blog-Posts.
     */
    @Column(name = "title", nullable = false, length = 500)
    private String title;

    /**
     * Kurztext-Beschreibung des Feed-Eintrags, gewonnen aus dem bereinigten HTML-Inhalt des Blog-Posts.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Direkte URL zum zugehörigen Blog-Post, zusammengesetzt aus Basis-URL und Slug.
     */
    @Column(name = "link", nullable = false, length = 1000)
    private String link;

    /**
     * Veröffentlichungsdatum des Feed-Eintrags, entspricht dem letzten Änderungsdatum des Blog-Posts.
     */
    @Column(name = "pub_date", nullable = false)
    private LocalDateTime pubDate;

    /**
     * Global eindeutiger Bezeichner (GUID) des Feed-Eintrags, entspricht der Link-URL.
     */
    @Column(name = "guid", nullable = false, length = 1000, unique = true)
    private String guid;

    /**
     * Fremdschlüssel-Referenz auf den zugehörigen Blog-Post. Jeder Feed-Eintrag
     * ist genau einem Blog-Post zugeordnet.
     */
    @Column(name = "blog_post_id", nullable = false, unique = true)
    private UUID blogPostId;
}
