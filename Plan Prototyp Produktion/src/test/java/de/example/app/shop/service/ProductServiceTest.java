package de.example.app.shop.service;

import de.example.app.shared.util.ImageUtil;
import de.example.app.shop.entity.Product;
import de.example.app.shop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für den {@link ProductService}.
 * Verwendet Mockito, um Abhängigkeiten wie {@link ProductRepository} und {@link ImageUtil} zu mocken.
 */
@ExtendWith(MockitoExtension.class) // Aktiviert Mockito-Erweiterung für JUnit 5
class ProductServiceTest {

    @Mock // Erstellt ein Mock-Objekt für ProductRepository
    private ProductRepository productRepository;

    @Mock // Erstellt ein Mock-Objekt für ImageUtil
    private ImageUtil imageUtil;

    @InjectMocks // Injiziert die Mocks in den ProductService
    private ProductService productService;

    private Product testProduct; // Eine Test-Entität für Produkte
    private UUID testProductId; // Die ID des Test-Produkts

    /**
     * Setup-Methode, die vor jedem Test ausgeführt wird.
     * Initialisiert eine Test-Produkt-Entität.
     */
    @BeforeEach
    void setUp() {
        testProductId = UUID.randomUUID();
        testProduct = new Product(testProductId, "Test Produkt", "Beschreibung", BigDecimal.valueOf(99.99), null, 10);
    }

    /**
     * Testet die {@code createProduct}-Methode: Sollte ein neues Produkt speichern.
     */
    @Test
    void createProduct_shouldSaveNewProduct() {
        Product newProduct = new Product(null, "Neues Produkt", "Neue Beschreibung", BigDecimal.valueOf(10.00), null, 5);
        MockMultipartFile imageFile = new MockMultipartFile("image", "image.jpg", "image/jpeg", "some image data".getBytes());

        // Definiert das Verhalten des ImageUtil-Mocks
        when(imageUtil.processAndEncodeImage(imageFile)).thenReturn("base64ImageData");
        // Definiert das Verhalten des ProductRepository-Mocks beim Speichern
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product savedProduct = invocation.getArgument(0);
            if (savedProduct.getId() == null) savedProduct.setId(UUID.randomUUID()); // Simuliert die ID-Generierung
            return savedProduct;
        });

        Product createdProduct = productService.createProduct(newProduct, imageFile); // Ruft die zu testende Methode auf

        assertNotNull(createdProduct.getId()); // Überprüft, ob eine ID zugewiesen wurde
        assertEquals("Neues Produkt", createdProduct.getName()); // Überprüft den Namen
        assertEquals("base64ImageData", createdProduct.getImage()); // Überprüft das kodierte Bild
        assertEquals(5, createdProduct.getStock()); // Überprüft den Lagerbestand
        verify(productRepository, times(1)).save(any(Product.class)); // Überprüft, ob save einmal aufgerufen wurde
    }

    /**
     * Testet die {@code createProduct}-Methode: Sollte eine {@link IllegalArgumentException} werfen, wenn eine ID bereitgestellt wird.
     */
    @Test
    void createProduct_shouldThrowExceptionIfIdProvided() {
        // Versucht, ein Produkt mit bereits vorhandener ID zu erstellen
        assertThrows(IllegalArgumentException.class, () -> productService.createProduct(testProduct, null));
        verify(productRepository, never()).save(any(Product.class)); // Überprüft, dass save nie aufgerufen wurde
    }

    /**
     * Testet die {@code updateProduct}-Methode: Sollte ein bestehendes Produkt aktualisieren.
     */
    @Test
    void updateProduct_shouldUpdateExistingProduct() {
        Product updatedInfo = new Product(testProductId, "Aktualisierter Name", "Aktualisierte Beschreibung", BigDecimal.valueOf(120.00), null, 15);
        MockMultipartFile imageFile = new MockMultipartFile("image", "image.jpg", "image/jpeg", "some new image data".getBytes());

        // Definiert das Verhalten der Mocks
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(imageUtil.processAndEncodeImage(imageFile)).thenReturn("newBase64ImageData");
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Optional<Product> result = productService.updateProduct(testProductId, updatedInfo, imageFile); // Ruft die zu testende Methode auf

        assertTrue(result.isPresent()); // Überprüft, ob ein Ergebnis vorhanden ist
        assertEquals("Aktualisierter Name", result.get().getName()); // Überprüft den aktualisierten Namen
        assertEquals("newBase64ImageData", result.get().getImage()); // Überprüft das neue Bild
        assertEquals(15, result.get().getStock()); // Überprüft den Lagerbestand
        verify(productRepository, times(1)).save(testProduct); // Überprüft, ob save einmal aufgerufen wurde
    }

    /**
     * Testet die {@code updateProduct}-Methode: Sollte ein leeres Optional zurückgeben, wenn das Produkt nicht gefunden wird.
     */
    @Test
    void updateProduct_shouldReturnEmptyIfNotFound() {
        Product updatedInfo = new Product(testProductId, "Aktualisierter Name", "Aktualisierte Beschreibung", BigDecimal.valueOf(120.00), null, 15);
        when(productRepository.findById(testProductId)).thenReturn(Optional.empty()); // Simuliert, dass das Produkt nicht gefunden wird

        Optional<Product> result = productService.updateProduct(testProductId, updatedInfo, null); // Ruft die zu testende Methode auf

        assertTrue(result.isEmpty()); // Überprüft, ob das Ergebnis leer ist
        verify(productRepository, never()).save(any(Product.class)); // Überprüft, dass save nie aufgerufen wurde
    }

    /**
     * Testet die {@code deleteProduct}-Methode: Sollte true zurückgeben, wenn das Produkt gelöscht wurde.
     */
    @Test
    void deleteProduct_shouldReturnTrueIfDeleted() {
        when(productRepository.existsById(testProductId)).thenReturn(true);
        doNothing().when(productRepository).deleteById(testProductId);
        assertTrue(productService.deleteProduct(testProductId));
        verify(productRepository, times(1)).deleteById(testProductId);
    }

    /**
     * Testet die {@code deleteProduct}-Methode: Sollte false zurückgeben, wenn das Produkt nicht gefunden wird.
     */
    @Test
    void deleteProduct_shouldReturnFalseIfNotFound() {
        when(productRepository.existsById(testProductId)).thenReturn(false);
        assertFalse(productService.deleteProduct(testProductId));
        verify(productRepository, never()).deleteById(any());
    }

    /**
     * Testet die {@code getProductById}-Methode: Sollte das Produkt zurückgeben.
     */
    @Test
    void getProductById_shouldReturnProduct() {
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct)); // Simuliert, dass das Produkt gefunden wird
        Optional<Product> result = productService.getProductById(testProductId); // Ruft die zu testende Methode auf
        assertTrue(result.isPresent()); // Überprüft, ob ein Ergebnis vorhanden ist
        assertEquals(testProduct, result.get()); // Überprüft, ob das korrekte Produkt zurückgegeben wurde
    }

    /**
     * Testet die {@code getProductById}-Methode: Sollte ein leeres Optional zurückgeben, wenn das Produkt nicht gefunden wird.
     */
    @Test
    void getProductById_shouldReturnEmptyIfNotFound() {
        when(productRepository.findById(testProductId)).thenReturn(Optional.empty()); // Simuliert, dass das Produkt nicht gefunden wird
        Optional<Product> result = productService.getProductById(testProductId); // Ruft die zu testende Methode auf
        assertTrue(result.isEmpty()); // Überprüft, ob das Ergebnis leer ist
    }

    /**
     * Testet die {@code getAllProducts}-Methode: Sollte alle Produkte zurückgeben.
     */
    @Test
    void getAllProducts_shouldReturnAllProducts() {
        List<Product> products = Arrays.asList(testProduct, new Product(UUID.randomUUID(), "Ein anderes Produkt", "Beschreibung", BigDecimal.valueOf(50.00), null, 5));
        when(productRepository.findAll()).thenReturn(products); // Simuliert die Rückgabe aller Produkte
        List<Product> result = productService.getAllProducts(); // Ruft die zu testende Methode auf
        assertEquals(2, result.size()); // Überprüft die Anzahl der Produkte
        assertTrue(result.contains(testProduct)); // Überprüft, ob das Testprodukt enthalten ist
    }
}
