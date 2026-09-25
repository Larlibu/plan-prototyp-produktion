package de.example.app.shop.repository;

import de.example.app.shop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository zur Verwaltung von {@link Product}-Entitäten unter Verwendung von Spring Data JPA.
 * Erweitert {@link JpaRepository}, um CRUD-Operationen und abgeleitete Abfragen bereitzustellen.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
}
