package de.example.app.security.repository;

import de.example.app.security.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository zur Verwaltung von {@link User}-Entitäten unter Verwendung von Spring Data JPA.
 * Erweitert {@link JpaRepository}, um CRUD-Operationen und abgeleitete Abfragen bereitzustellen.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Findet einen Benutzer anhand seines Benutzernamens, wobei die Groß-/Kleinschreibung ignoriert wird.
     *
     * @param username Der Benutzername des Benutzers.
     * @return Ein Optional, das den Benutzer enthält, falls gefunden, ansonsten ein leeres Optional.
     */
    Optional<User> findByUsernameIgnoreCase(String username);

    /**
     * Findet einen Benutzer anhand seiner E-Mail-Adresse, wobei die Groß-/Kleinschreibung ignoriert wird.
     *
     * @param email Die E-Mail-Adresse des Benutzers.
     * @return Ein Optional, das den Benutzer enthält, falls gefunden, ansonsten ein leeres Optional.
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Zählt die Anzahl aller Benutzer.
     * Diese Methode wird von JpaRepository bereitgestellt, aber explizit deklariert,
     * um mögliche Compiler-Probleme zu umgehen.
     *
     * @return Die Anzahl der Benutzer.
     */
    long count();
}
