package de.example.app.shop.service;

import de.example.app.shared.util.ImageUtil;
import de.example.app.shop.entity.Product;
import de.example.app.shop.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service zur Verwaltung von Produkten im Shop.
 * Enthält die Geschäftslogik für das Erstellen, Aktualisieren, Löschen und Abrufen von Produkten.
 */
@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final ImageUtil imageUtil;

    public ProductService(ProductRepository productRepository, ImageUtil imageUtil) {
        this.productRepository = productRepository;
        this.imageUtil = imageUtil;
    }

    /**
     * Erstellt ein neues Produkt.
     *
     * @param product Das Produkt-Objekt mit den Daten.
     * @param imageFile Die Bilddatei, die dem Produkt zugeordnet werden soll.
     * @return Das erstellte Produkt.
     * @throws IllegalArgumentException wenn das neue Produkt bereits eine ID hat.
     */
    public Product createProduct(Product product, MultipartFile imageFile) {
        if (product.getId() != null) {
            log.error("Versuch, ein neues Produkt mit bereits vorhandener ID zu erstellen: {}", product.getId());
            throw new IllegalArgumentException("Ein neues Produkt darf keine ID haben.");
        }
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String base64Image = imageUtil.processAndEncodeImage(imageFile);
                product.setImage(base64Image);
            } catch (Exception e) {
                log.error("Fehler beim Verarbeiten des Bildes für Produkt {}: {}", product.getName(), e.getMessage(), e);
                throw new RuntimeException("Fehler beim Verarbeiten des Produktbildes.", e);
            }
        }

        log.info("Versuche neues Produkt mit Namen zu speichern: {}", product.getName());
        try {
            Product savedProduct = productRepository.save(product);
            log.info("Produkt '{}' mit ID {} erfolgreich erstellt.", savedProduct.getName(), savedProduct.getId());
            return savedProduct;
        } catch (Exception e) {
            log.error("Fehler beim Speichern des Produkts '{}': {}", product.getName(), e.getMessage(), e);
            throw new RuntimeException("Fehler beim Speichern des Produkts.", e);
        }
    }

    /**
     * Aktualisiert ein bestehendes Produkt.
     *
     * @param id Die ID des zu aktualisierenden Produkts.
     * @param updatedProduct Das Produkt-Objekt mit den aktualisierten Daten.
     * @param imageFile Die neue Bilddatei, falls vorhanden, die dem Produkt zugeordnet werden soll.
     * @return Ein Optional, das das aktualisierte Produkt enthält, oder leer, wenn nicht gefunden.
     */
    public Optional<Product> updateProduct(UUID id, Product updatedProduct, MultipartFile imageFile) {
        return productRepository.findById(id).map(existingProduct -> {
            existingProduct.setName(updatedProduct.getName());
            existingProduct.setDescription(updatedProduct.getDescription());
            existingProduct.setPrice(updatedProduct.getPrice());
            existingProduct.setStock(updatedProduct.getStock());

            if (imageFile != null && !imageFile.isEmpty()) {
                try {
                    String base64Image = imageUtil.processAndEncodeImage(imageFile);
                    existingProduct.setImage(base64Image);
                } catch (Exception e) {
                    log.error("Fehler beim Verarbeiten des Bildes für Produkt {}: {}", existingProduct.getName(), e.getMessage(), e);
                    throw new RuntimeException("Fehler beim Verarbeiten des Produktbildes.", e);
                }
            } else if (updatedProduct.getImage() == null) {
                existingProduct.setImage(null);
            }

            log.info("Versuche Produkt mit ID {} zu aktualisieren.", id);
            try {
                Product savedProduct = productRepository.save(existingProduct);
                log.info("Produkt '{}' mit ID {} erfolgreich aktualisiert.", savedProduct.getName(), savedProduct.getId());
                return savedProduct;
            } catch (Exception e) {
                log.error("Fehler beim Aktualisieren des Produkts mit ID {}: {}", id, e.getMessage(), e);
                throw new RuntimeException("Fehler beim Aktualisieren des Produkts.", e);
            }
        });
    }

    /**
     * Löscht ein Produkt anhand seiner ID.
     *
     * @param id Die ID des zu löschenden Produkts.
     * @return True, wenn das Produkt gelöscht wurde, false, wenn es nicht gefunden wurde.
     */
    public boolean deleteProduct(UUID id) {
        log.info("Versuche Produkt mit ID {} zu löschen.", id);
        if (productRepository.existsById(id)) {
            try {
                productRepository.deleteById(id);
                log.info("Produkt mit ID {} erfolgreich gelöscht.", id);
                return true;
            } catch (Exception e) {
                log.error("Fehler beim Löschen des Produkts mit ID {}: {}", id, e.getMessage(), e);
                throw new RuntimeException("Fehler beim Löschen des Produkts.", e);
            }
        }
        log.warn("Produkt mit ID {} nicht gefunden zum Löschen.", id);
        return false;
    }

    public Optional<Product> getProductById(UUID id) {
        return productRepository.findById(id);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
}
