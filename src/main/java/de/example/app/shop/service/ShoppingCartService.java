package de.example.app.shop.service;

import de.example.app.shop.dto.CartItemDto;
import de.example.app.shop.dto.ShoppingCartDto;
import de.example.app.shop.entity.Product;
import de.example.app.shop.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Service zur Verwaltung des session-basierten Warenkorbs.
 * Behandelt das Hinzufügen, Aktualisieren, Entfernen von Artikeln und das Abrufen des Warenkorbs.
 */
@Service
public class ShoppingCartService {

    private static final Logger log = LoggerFactory.getLogger(ShoppingCartService.class);
    private static final String CART_SESSION_KEY = "shoppingCart";

    private final ProductRepository productRepository;

    public ShoppingCartService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Ruft den aktuellen Warenkorb aus der HTTP-Session ab.
     * Wenn kein Warenkorb in der Session existiert, wird ein neuer erstellt und in der Session gespeichert.
     *
     * @param session Die aktuelle {@link HttpSession}.
     * @return Das {@link ShoppingCartDto}-Objekt.
     */
    public ShoppingCartDto getCart(HttpSession session) {
        return (ShoppingCartDto) session.getAttribute(CART_SESSION_KEY);
    }

    private ShoppingCartDto getOrCreateCart(HttpSession session) {
        ShoppingCartDto cart = getCart(session);
        if (cart == null) {
            cart = new ShoppingCartDto();
            session.setAttribute(CART_SESSION_KEY, cart);
            log.debug("Neuer Warenkorb erstellt und in der Session gespeichert.");
        }
        return cart;
    }

    /**
     * Fügt ein Produkt zum Warenkorb hinzu.
     * Überprüft den Lagerbestand, bevor der Artikel hinzugefügt wird.
     *
     * @param productId Die ID des hinzuzufügenden Produkts.
     * @param quantity Die hinzuzufügende Menge.
     * @param session Die aktuelle {@link HttpSession}.
     * @return Der aktualisierte {@link ShoppingCartDto}.
     * @throws IllegalArgumentException wenn das Produkt nicht gefunden wird, die Menge ungültig ist oder der Lagerbestand nicht ausreicht.
     */
    public ShoppingCartDto addItemToCart(UUID productId, int quantity, HttpSession session) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Die Menge muss positiv sein.");
        }

        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            throw new IllegalArgumentException("Produkt mit ID " + productId + " nicht gefunden.");
        }

        Product product = productOptional.get();
        ShoppingCartDto cart = getOrCreateCart(session);

        // Bereits im Warenkorb liegende Menge des Produkts fließt in die Lagerbestandsprüfung mit ein
        Optional<CartItemDto> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();

        int currentCartQuantity = existingItem.map(CartItemDto::getQuantity).orElse(0);
        if (product.getStock() < (currentCartQuantity + quantity)) {
            throw new IllegalArgumentException("Nicht genügend Lagerbestand für Produkt: " + product.getName() + ". Verfügbar: " + product.getStock());
        }

        CartItemDto newItem = new CartItemDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                quantity,
                product.getImage()
        );
        cart.addItem(newItem);
        log.info("Produkt '{}' (ID: {}) mit Menge {} zum Warenkorb hinzugefügt. Gesamtartikel im Warenkorb: {}", product.getName(), productId, quantity, cart.getItems().size());
        return cart;
    }

    /**
     * Aktualisiert die Menge eines Produkts im Warenkorb.
     * Überprüft den Lagerbestand für die neue Menge.
     *
     * @param productId Die ID des zu aktualisierenden Produkts.
     * @param quantity Die neue Menge.
     * @param session Die aktuelle {@link HttpSession}.
     * @return Der aktualisierte {@link ShoppingCartDto}.
     * @throws IllegalArgumentException wenn das Produkt nicht gefunden wird, die Menge ungültig ist oder der Lagerbestand nicht ausreicht.
     */
    public ShoppingCartDto updateItemQuantity(UUID productId, int quantity, HttpSession session) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Die Menge darf nicht negativ sein.");
        }

        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            throw new IllegalArgumentException("Produkt mit ID " + productId + " nicht gefunden.");
        }

        Product product = productOptional.get();
        ShoppingCartDto cart = getOrCreateCart(session);

        if (quantity > product.getStock()) {
            throw new IllegalArgumentException("Nicht genügend Lagerbestand für Produkt: " + product.getName() + ". Verfügbar: " + product.getStock());
        }

        cart.updateItemQuantity(productId, quantity);
        log.info("Menge von Produkt '{}' (ID: {}) auf {} im Warenkorb aktualisiert.", product.getName(), productId, quantity);
        return cart;
    }

    /**
     * Entfernt ein Produkt aus dem Warenkorb.
     *
     * @param productId Die ID des zu entfernenden Produkts.
     * @param session Die aktuelle {@link HttpSession}.
     * @return Der aktualisierte {@link ShoppingCartDto}.
     */
    public ShoppingCartDto removeItemFromCart(UUID productId, HttpSession session) {
        ShoppingCartDto cart = getOrCreateCart(session);
        cart.removeItem(productId);
        log.info("Produkt mit ID {} aus Warenkorb entfernt.", productId);
        return cart;
    }

    /**
     * Löscht alle Artikel aus dem Warenkorb.
     *
     * @param session Die aktuelle {@link HttpSession}.
     * @return Der leere {@link ShoppingCartDto}.
     */
    public ShoppingCartDto clearCart(HttpSession session) {
        ShoppingCartDto cart = getOrCreateCart(session);
        cart.clearCart();
        log.info("Warenkorb geleert.");
        return cart;
    }
}
