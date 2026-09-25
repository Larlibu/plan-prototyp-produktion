package de.example.app.cms.repository;

import de.example.app.cms.entity.BlogPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository für die {@link BlogPost}-Entität.
 * Bietet CRUD-Operationen sowie eigene Abfragemethoden zum Suchen und Zählen
 * von Blogbeiträgen nach Slug und Veröffentlichungsstatus.
 */
@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, UUID> {

    /**
     * Sucht einen Blogbeitrag anhand seines URL-freundlichen Slugs.
     *
     * @param slug der eindeutige Slug des gesuchten Blogbeitrags
     * @return ein {@link Optional} mit dem gefundenen Blogbeitrag, oder ein leeres {@link Optional} wenn kein Beitrag mit diesem Slug existiert
     */
    Optional<BlogPost> findBySlug(String slug);

    /**
     * Liefert alle Blogbeiträge, die als veröffentlicht markiert sind.
     *
     * @return Liste aller Blogbeiträge mit {@code published == true}
     */
    List<BlogPost> findByPublishedTrue();

    /**
     * Zählt die Anzahl aller veröffentlichten Blogbeiträge.
     *
     * @return Anzahl der Blogbeiträge mit {@code published == true}
     */
    long countByPublishedTrue();

    /**
     * Zählt die Gesamtanzahl aller Blogbeiträge unabhängig vom Veröffentlichungsstatus.
     * Wird von {@link org.springframework.data.jpa.repository.JpaRepository} bereitgestellt
     * und hier explizit deklariert.
     *
     * @return Gesamtanzahl aller gespeicherten Blogbeiträge
     */
    long count();
}
