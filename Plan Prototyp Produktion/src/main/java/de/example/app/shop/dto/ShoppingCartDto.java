package de.example.app.shop.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DTO (Data Transfer Object) zur Repräsentation des gesamten Warenkorbs.
 * Enthält eine Liste von Warenkorbartikeln und den Gesamtpreis des Warenkorbs.
 */
@Data
public class ShoppingCartDto {
    private List<CartItemDto> items = new ArrayList<>();
    private BigDecimal totalPrice = BigDecimal.ZERO;
    private int totalItems = 0;

    /**
     * Fügt einen {@link CartItemDto} zum Warenkorb hinzu.
     * Wenn ein Artikel mit derselben Produkt-ID bereits existiert, wird dessen Menge aktualisiert.
     *
     * @param newItem Der hinzuzufügende {@link CartItemDto}.
     */
    public void addItem(CartItemDto newItem) {
        boolean found = false;
        for (CartItemDto item : items) {
            if (item.getProductId().equals(newItem.getProductId())) {
                item.setQuantity(item.getQuantity() + newItem.getQuantity());
                found = true;
                break;
            }
        }
        if (!found) {
            items.add(newItem);
        }
        calculateTotals();
    }

    /**
     * Aktualisiert die Menge eines spezifischen Artikels im Warenkorb.
     * Wenn die Menge 0 oder weniger ist, wird der Artikel entfernt.
     *
     * @param productId Die ID des zu aktualisierenden Produkts.
     * @param quantity Die neue Menge.
     */
    public void updateItemQuantity(java.util.UUID productId, int quantity) {
        items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .ifPresent(item -> item.setQuantity(quantity));
        items.removeIf(item -> item.getQuantity() <= 0);
        calculateTotals();
    }

    /**
     * Entfernt einen Artikel aus dem Warenkorb.
     *
     * @param productId Die ID des zu entfernenden Produkts.
     */
    public void removeItem(java.util.UUID productId) {
        items.removeIf(item -> item.getProductId().equals(productId));
        calculateTotals();
    }

    /**
     * Löscht alle Artikel aus dem Warenkorb.
     */
    public void clearCart() {
        items.clear();
        totalPrice = BigDecimal.ZERO;
        totalItems = 0;
    }

    private void calculateTotals() {
        totalPrice = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalItems = items.stream()
                .mapToInt(CartItemDto::getQuantity)
                .sum();
    }
}