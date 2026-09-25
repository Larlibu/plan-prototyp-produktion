package de.example.app.feed.repository;

import de.example.app.feed.entity.FeedItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository für {@link FeedItem}-Entitäten.
 * Stellt CRUD-Operationen sowie abgeleitete Abfragemethoden für Feed-Einträge bereit.
 */
public interface FeedItemRepository extends JpaRepository<FeedItem, UUID> {

    /**
     * Sucht einen Feed-Eintrag anhand der UUID des zugehörigen Blog-Posts.
     *
     * @param blogPostId die UUID des Blog-Posts, dem der Feed-Eintrag zugeordnet ist
     * @return ein {@link Optional} mit dem gefundenen {@link FeedItem}, oder leer wenn kein Eintrag existiert
     */
    Optional<FeedItem> findByBlogPostId(UUID blogPostId);

    /**
     * Gibt alle Feed-Einträge absteigend nach Veröffentlichungsdatum sortiert zurück.
     * Neueste Einträge erscheinen zuerst.
     *
     * @return eine Liste aller {@link FeedItem}-Objekte, sortiert nach {@code pubDate} absteigend
     */
    List<FeedItem> findAllByOrderByPubDateDesc();
}
