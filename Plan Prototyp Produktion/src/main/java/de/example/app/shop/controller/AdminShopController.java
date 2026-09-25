package de.example.app.shop.controller;

import de.example.app.shop.dto.ProductCreateDto;
import de.example.app.shop.dto.ProductUpdateDto;
import de.example.app.shop.dto.ProductViewDto;
import de.example.app.shop.entity.Product;
import de.example.app.shop.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller zur Verwaltung von Shop-Produkten im Admin-Bereich.
 * Nutzt {@link ProductService} für die Produktlogik.
 */
@Tag(name = "Admin – Shop", description = "Verwaltung von Produkten im geschützten Admin-Bereich (Rolle: ADMIN)")
@Controller
@RequestMapping("/admin/shop")
@PreAuthorize("hasRole('ADMIN')")
public class AdminShopController {

    private final ProductService productService;

    public AdminShopController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Zeigt eine Liste aller Produkte für die Administration an.
     *
     * @param model Das {@link Model}-Objekt, um Daten an die View zu übergeben.
     * @return Der Name des Thymeleaf-Templates für die Admin-Produktliste.
     */
    @Operation(summary = "Alle Produkte in der Admin-Übersicht anzeigen")
    @GetMapping
    public String adminListProducts(Model model) {
        List<ProductViewDto> products = productService.getAllProducts().stream()
                .map(ProductViewDto::fromEntity)
                .collect(Collectors.toList());
        model.addAttribute("products", products);
        return "admin/shop/product-list-admin";
    }

    /**
     * Zeigt das Formular zum Erstellen eines neuen Produkts an.
     *
     * @param model Das {@link Model}-Objekt, um Daten an die View zu übergeben.
     * @return Der Name des Thymeleaf-Templates für das Produkt-Erstellungs-/Bearbeitungsformular.
     */
    @Operation(summary = "Formular zum Erstellen eines neuen Produkts anzeigen")
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new ProductCreateDto());
        model.addAttribute("isEdit", false);
        return "admin/shop/product-create-edit";
    }

    /**
     * Verarbeitet die Übermittlung des Formulars zum Erstellen eines neuen Produkts.
     *
     * @param productCreateDto Das DTO mit den Daten des neuen Produkts.
     * @param imageFile Die hochgeladene Bilddatei.
     * @param bindingResult Das Ergebnis der Validierung.
     * @param redirectAttributes Attribute für die Weiterleitung.
     * @return Eine Weiterleitung zur Admin-Produktliste oder zurück zum Formular mit Fehlern.
     */
    @Operation(summary = "Neues Produkt erstellen und speichern")
    @ApiResponse(responseCode = "302", description = "Produkt erfolgreich erstellt – Weiterleitung zur Admin-Übersicht")
    @PostMapping("/create")
    public String createProduct(@ModelAttribute("product") @Valid ProductCreateDto productCreateDto,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "admin/shop/product-create-edit";
        }
        try {
            Product product = new Product();
            product.setName(productCreateDto.getName());
            product.setDescription(productCreateDto.getDescription());
            product.setPrice(productCreateDto.getPrice());
            product.setStock(productCreateDto.getStock());

            productService.createProduct(product, imageFile);
            redirectAttributes.addFlashAttribute("message", "Produkt erfolgreich erstellt!");
            return "redirect:/admin/shop";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Fehler beim Erstellen des Produkts: " + e.getMessage());
            return "redirect:/admin/shop/create";
        }
    }

    /**
     * Zeigt das Formular zum Bearbeiten eines bestehenden Produkts an.
     *
     * @param id Die ID des zu bearbeitenden Produkts.
     * @param model Das {@link Model}-Objekt, um Daten an die View zu übergeben.
     * @return Der Name des Thymeleaf-Templates für das Produkt-Erstellungs-/Bearbeitungsformular oder eine Fehlerseite.
     */
    @Operation(summary = "Bearbeitungsformular für ein bestehendes Produkt anzeigen")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Formular mit den vorhandenen Produktdaten angezeigt"),
        @ApiResponse(responseCode = "404", description = "Produkt mit der angegebenen ID nicht gefunden")
    })
    @GetMapping("/edit/{id}")
    public String showEditForm(
            @Parameter(description = "UUID des zu bearbeitenden Produkts") @PathVariable UUID id,
            Model model) {
        Optional<Product> product = productService.getProductById(id);
        if (product.isEmpty()) {
            return "error/404";
        }
        ProductUpdateDto productUpdateDto = new ProductUpdateDto();
        productUpdateDto.setId(product.get().getId());
        productUpdateDto.setName(product.get().getName());
        productUpdateDto.setDescription(product.get().getDescription());
        productUpdateDto.setPrice(product.get().getPrice());
        productUpdateDto.setStock(product.get().getStock());
        productUpdateDto.setExistingImage(product.get().getImage());

        model.addAttribute("product", productUpdateDto);
        model.addAttribute("isEdit", true);
        return "admin/shop/product-create-edit";
    }

    /**
     * Verarbeitet die Übermittlung des Formulars zum Bearbeiten eines bestehenden Produkts.
     *
     * @param id Die ID des zu aktualisierenden Produkts.
     * @param productUpdateDto Das DTO mit den aktualisierten Produktdaten.
     * @param imageFile Die neue hochgeladene Bilddatei.
     * @param bindingResult Das Ergebnis der Validierung.
     * @param redirectAttributes Attribute für die Weiterleitung.
     * @return Eine Weiterleitung zur Admin-Produktliste oder zurück zum Formular mit Fehlern.
     */
    @Operation(summary = "Bestehendes Produkt aktualisieren")
    @ApiResponse(responseCode = "302", description = "Produkt erfolgreich aktualisiert – Weiterleitung zur Admin-Übersicht")
    @PostMapping("/edit/{id}")
    public String updateProduct(
            @Parameter(description = "UUID des zu aktualisierenden Produkts") @PathVariable UUID id,
                                @ModelAttribute("product") @Valid ProductUpdateDto productUpdateDto,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "admin/shop/product-create-edit";
        }
        try {
            Product product = new Product();
            product.setId(id);
            product.setName(productUpdateDto.getName());
            product.setDescription(productUpdateDto.getDescription());
            product.setPrice(productUpdateDto.getPrice());
            product.setStock(productUpdateDto.getStock());
            // existingImage ist null, wenn der Benutzer das Bild im Formular explizit entfernt hat
            product.setImage(productUpdateDto.getExistingImage());

            productService.updateProduct(id, product, imageFile);
            redirectAttributes.addFlashAttribute("message", "Produkt erfolgreich aktualisiert!");
            return "redirect:/admin/shop";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Fehler beim Aktualisieren des Produkts: " + e.getMessage());
            return "redirect:/admin/shop/edit/" + id;
        }
    }

    /**
     * Verarbeitet das Löschen eines Produkts.
     *
     * @param id Die ID des zu löschenden Produkts.
     * @param redirectAttributes Attribute für die Weiterleitung.
     * @return Eine Weiterleitung zur Admin-Produktliste.
     */
    @Operation(summary = "Produkt aus dem Shop löschen")
    @ApiResponse(responseCode = "302", description = "Weiterleitung zur Admin-Übersicht nach dem Löschversuch")
    @PostMapping("/delete/{id}")
    public String deleteProduct(
            @Parameter(description = "UUID des zu löschenden Produkts") @PathVariable UUID id,
            RedirectAttributes redirectAttributes) {
        if (productService.deleteProduct(id)) {
            redirectAttributes.addFlashAttribute("message", "Produkt erfolgreich gelöscht!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Produkt nicht gefunden oder konnte nicht gelöscht werden.");
        }
        return "redirect:/admin/shop";
    }
}