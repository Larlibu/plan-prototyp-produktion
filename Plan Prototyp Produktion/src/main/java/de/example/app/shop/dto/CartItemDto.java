package de.example.app.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO (Data Transfer Object) zur Repräsentation eines einzelnen Artikels im Warenkorb.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {
    private UUID productId;
    private String productName;
    private BigDecimal price;
    private int quantity;
    private String productImage;
}
