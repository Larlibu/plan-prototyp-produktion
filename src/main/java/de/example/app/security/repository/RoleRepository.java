package de.example.app.security.repository;

import de.example.app.security.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository zur Verwaltung von {@link Role}-Entitäten unter Verwendung von Spring Data JPA.
 * Erweitert {@link JpaRepository}, um CRUD-Operationen und abgeleitete Abfragen bereitzustellen.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    /**
     * Findet eine Rolle anhand ihres Namens, wobei die Groß-/Kleinschreibung ignoriert wird.
     *
     * @param name Der Name der Rolle.
     * @return Ein Optional, das die Rolle enthält, falls gefunden, ansonsten ein leeres Optional.
     */
    Optional<Role> findByNameIgnoreCase(String name);
}
