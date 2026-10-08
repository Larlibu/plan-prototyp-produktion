package de.example.app.crm.repository;

import de.example.app.crm.entity.NewsletterSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository zur Verwaltung von {@link NewsletterSubscriber}-Entitäten unter Verwendung von Spring Data JPA.
 * Erweitert {@link JpaRepository}, um CRUD-Operationen und abgeleitete Abfragen bereitzustellen.
 */
@Repository
public interface NewsletterSubscriberRepository extends JpaRepository<NewsletterSubscriber, UUID> {

    /**
     * Findet einen Newsletter-Abonnenten anhand seiner E-Mail-Adresse, wobei die Groß-/Kleinschreibung ignoriert wird.
     *
     * @param email Die E-Mail-Adresse des Newsletter-Abonnenten.
     * @return Ein Optional, das den Abonnenten enthält, falls gefunden, ansonsten ein leeres Optional.
     */
    Optional<NewsletterSubscriber> findByEmailIgnoreCase(String email);

    /**
     * Zählt die Anzahl der aktiven Newsletter-Abonnenten.
     *
     * @return Die Anzahl der aktiven Abonnenten.
     */
    long countByActiveTrue();
}
