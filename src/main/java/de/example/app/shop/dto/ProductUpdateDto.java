package de.example.app.shop.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO (Data Transfer Object) zum Aktualisieren eines bestehenden Produkts.
 * Enthält Validierungsregeln für die Eingabefelder, die von der Benutzeroberfläche kommen.
 */
@Data
public class ProductUpdateDto {
    private UUID id;

    @NotBlank(message = "Der Produktname darf nicht leer sein.")
    @Size(max = 255, message = "Der Produktname darf maximal 255 Zeichen lang sein.")
    private String name;

    @Size(max = 1000, message = "Die Beschreibung darf maximal 1000 Zeichen lang sein.")
    private String description;

    @NotNull(message = "Der Preis darf nicht leer sein.")
    @DecimalMin(value = "0.01", message = "Der Preis muss größer als 0 sein.")
    private BigDecimal price;

    @Min(value = 0, message = "Der Lagerbestand darf nicht negativ sein.")
    private int stock;

    private MultipartFile imageFile;

    /** Aktuell gespeichertes Bild als Base64-String; wird vom Formular auf {@code null} gesetzt, um das Bild explizit zu entfernen. */
    private String existingImage;
}
