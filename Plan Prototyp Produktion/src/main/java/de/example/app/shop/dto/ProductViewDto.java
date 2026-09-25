package de.example.app.shop.dto;

import de.example.app.shop.entity.Product;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO (Data Transfer Object) zur Anzeige eines Produkts.
 * Bietet eine vereinfachte Ansicht der {@link Product}-Entität für Präsentationszwecke.
 */
@Data
public class ProductViewDto {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private String image;
    private int stock;

    /**
     * Statische Factory-Methode zur Konvertierung einer {@link Product}-Entität in ein {@link ProductViewDto}.
     *
     * @param product Die zu konvertierende {@link Product}-Entität.
     * @return Ein neues {@link ProductViewDto}-Objekt.
     */
    public static ProductViewDto fromEntity(Product product) {
        ProductViewDto dto = new ProductViewDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setImage(product.getImage());
        dto.setStock(product.getStock());
        return dto;
    }
}
