package de.example.app.shop.controller;

import de.example.app.shop.dto.ProductViewDto;
import de.example.app.shop.service.ProductService;
import de.example.app.shop.service.ShoppingCartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller zur Verwaltung von Shop-Produkten und Warenkorb-Operationen.
 * Nutzt {@link ProductService} für die Produktlogik und {@link ShoppingCartService} für die Warenkorblogik.
 */
@Tag(name = "Shop", description = "Öffentlicher Shop mit gesunden Lebensmitteln – Produktübersicht, Detailansichten und Warenkorb-Operationen")
@Controller
@RequestMapping("/shop")
public class ShopController {

    private final ProductService productService;
    private final ShoppingCartService shoppingCartService;

    public ShopController(ProductService productService, ShoppingCartService shoppingCartService) {
        this.productService = productService;
        this.shoppingCartService = shoppingCartService;
    }

    /**
     * Zeigt eine Liste aller Produkte an.
     *
     * @param model Das {@link Model}-Objekt, um Daten an die View zu übergeben.
     * @param session Die aktuelle {@link HttpSession}, um den Warenkorb abzurufen.
     * @return Der Name des Thymeleaf-Templates für die Produktliste.
     */
    @Operation(summary = "Alle Produkte im Shop anzeigen")
    @GetMapping
    public String listProducts(Model model, HttpSession session) {
        List<ProductViewDto> products = productService.getAllProducts().stream()
                .map(ProductViewDto::fromEntity)
                .collect(Collectors.toList());
        model.addAttribute("products", products);
        return "shop/product-list";
    }

    /**
     * Zeigt ein einzelnes Produkt anhand seiner ID an.
     *
     * @param id Die ID des Produkts.
     * @param model Das {@link Model}-Objekt, um Daten an die View zu übergeben.
     * @param session Die aktuelle {@link HttpSession}, um den Warenkorb abzurufen.
     * @return Der Name des Thymeleaf-Templates für die Produktdetails oder eine Fehlerseite.
     */
    @Operation(summary = "Produktdetailseite eines einzelnen Produkts anzeigen")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Produkt gefunden und Detailseite angezeigt"),
        @ApiResponse(responseCode = "404", description = "Produkt mit der angegebenen ID nicht gefunden")
    })
    @GetMapping("/{id}")
    public String viewProduct(
            @Parameter(description = "UUID des anzuzeigenden Produkts") @PathVariable UUID id,
            Model model, HttpSession session) {
        Optional<ProductViewDto> product = productService.getProductById(id)
                .map(ProductViewDto::fromEntity);

        if (product.isEmpty()) {
            return "error/404";
        }
        model.addAttribute("product", product.get());
        return "shop/product-detail";
    }

    /**
     * Zeigt den Warenkorb an.
     *
     * @param model Das {@link Model}-Objekt, um Daten an die View zu übergeben.
     * @param session Die aktuelle {@link HttpSession}.
     * @return Der Name des Thymeleaf-Templates für den Warenkorb.
     */
    @Operation(summary = "Aktuellen Warenkorb anzeigen")
    @GetMapping("/cart")
    public String viewCart(Model model, HttpSession session) {
        return "shop/cart";
    }

    /**
     * Fügt einen Artikel zum Warenkorb hinzu.
     *
     * @param productId Die ID des hinzuzufügenden Produkts.
     * @param quantity Die hinzuzufügende Menge (Standard ist 1).
     * @param session Die aktuelle {@link HttpSession}.
     * @param redirectAttributes Attribute für die Weiterleitung.
     * @return Eine Weiterleitung zur Shop-Seite oder Produktdetailseite.
     */
    @Operation(summary = "Produkt zum Warenkorb hinzufügen")
    @ApiResponse(responseCode = "302", description = "Weiterleitung zur Shop-Übersicht nach dem Hinzufügen")
    @PostMapping("/cart/add")
    public String addItemToCart(
            @Parameter(description = "UUID des hinzuzufügenden Produkts") @RequestParam UUID productId,
            @Parameter(description = "Gewünschte Menge (Standard: 1)") @RequestParam(defaultValue = "1") int quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            shoppingCartService.addItemToCart(productId, quantity, session);
            redirectAttributes.addFlashAttribute("message", "Produkt erfolgreich zum Warenkorb hinzugefügt!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/shop";
    }

    /**
     * Aktualisiert die Menge eines Artikels im Warenkorb.
     *
     * @param productId Die ID des zu aktualisierenden Produkts.
     * @param quantity Die neue Menge.
     * @param session Die aktuelle {@link HttpSession}.
     * @param redirectAttributes Attribute für die Weiterleitung.
     * @return Eine Weiterleitung zur Warenkorb-Seite.
     */
    @Operation(summary = "Menge eines Produkts im Warenkorb aktualisieren")
    @ApiResponse(responseCode = "302", description = "Weiterleitung zur Warenkorb-Seite nach der Aktualisierung")
    @PostMapping("/cart/update")
    public String updateCartItemQuantity(
            @Parameter(description = "UUID des zu aktualisierenden Produkts") @RequestParam UUID productId,
            @Parameter(description = "Neue Menge des Produkts im Warenkorb") @RequestParam int quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            shoppingCartService.updateItemQuantity(productId, quantity, session);
            redirectAttributes.addFlashAttribute("message", "Warenkorb erfolgreich aktualisiert!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/shop/cart";
    }

    /**
     * Entfernt einen Artikel aus dem Warenkorb.
     *
     * @param productId Die ID des zu entfernenden Produkts.
     * @param session Die aktuelle {@link HttpSession}.
     * @param redirectAttributes Attribute für die Weiterleitung.
     * @return Eine Weiterleitung zur Warenkorb-Seite.
     */
    @Operation(summary = "Produkt aus dem Warenkorb entfernen")
    @ApiResponse(responseCode = "302", description = "Weiterleitung zur Warenkorb-Seite nach dem Entfernen")
    @PostMapping("/cart/remove")
    public String removeCartItem(
            @Parameter(description = "UUID des zu entfernenden Produkts") @RequestParam UUID productId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        shoppingCartService.removeItemFromCart(productId, session);
        redirectAttributes.addFlashAttribute("message", "Produkt erfolgreich aus dem Warenkorb entfernt!");
        return "redirect:/shop/cart";
    }

    /**
     * Leert den gesamten Warenkorb.
     *
     * @param session Die aktuelle {@link HttpSession}.
     * @param redirectAttributes Attribute für die Weiterleitung.
     * @return Eine Weiterleitung zur Warenkorb-Seite.
     */
    @Operation(summary = "Gesamten Warenkorb leeren")
    @ApiResponse(responseCode = "302", description = "Weiterleitung zur Warenkorb-Seite nach dem Leeren")
    @PostMapping("/cart/clear")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        shoppingCartService.clearCart(session);
        redirectAttributes.addFlashAttribute("message", "Warenkorb erfolgreich geleert!");
        return "redirect:/shop/cart";
    }
}