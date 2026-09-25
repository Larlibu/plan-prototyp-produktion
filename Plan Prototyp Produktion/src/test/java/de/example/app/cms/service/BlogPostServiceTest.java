package de.example.app.cms.service;

import de.example.app.cms.entity.BlogPost;
import de.example.app.cms.repository.BlogPostRepository;
import de.example.app.shared.util.ImageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für den {@link BlogPostService}.
 * Verwendet Mockito, um Abhängigkeiten wie {@link BlogPostRepository} und {@link ImageUtil} zu mocken.
 */
@ExtendWith(MockitoExtension.class) // Aktiviert Mockito-Erweiterung für JUnit 5
class BlogPostServiceTest {

    @Mock // Erstellt ein Mock-Objekt für BlogPostRepository
    private BlogPostRepository blogPostRepository;

    @Mock // Erstellt ein Mock-Objekt für ImageUtil
    private ImageUtil imageUtil;

    @InjectMocks // Injiziert die Mocks in den BlogPostService
    private BlogPostService blogPostService;

    private BlogPost testBlogPost; // Eine Test-Entität für Blogbeiträge
    private UUID testBlogPostId; // Die ID des Test-Blogbeitrags

    /**
     * Setup-Methode, die vor jedem Test ausgeführt wird.
     * Initialisiert eine Test-Blogbeitrags-Entität.
     */
    @BeforeEach
    void setUp() {
        testBlogPostId = UUID.randomUUID();
        testBlogPost = new BlogPost(testBlogPostId, "Test Titel", "test-slug", "Test Inhalt", null, false, LocalDateTime.now(), LocalDateTime.now());
    }

    /**
     * Testet die {@code createBlogPost}-Methode: Sollte einen neuen Blogbeitrag speichern.
     */
    @Test
    void createBlogPost_shouldSaveNewBlogPost() {
        BlogPost newBlogPost = new BlogPost(null, "Neuer Titel", "neuer-slug", "Neuer Inhalt", null, false, null, null);
        MockMultipartFile imageFile = new MockMultipartFile("image", "image.jpg", "image/jpeg", "some image data".getBytes());

        // Definiert das Verhalten des ImageUtil-Mocks
        when(imageUtil.processAndEncodeImage(imageFile)).thenReturn("base64ImageData");
        // Definiert das Verhalten des BlogPostRepository-Mocks beim Speichern
        when(blogPostRepository.save(any(BlogPost.class))).thenAnswer(invocation -> {
            BlogPost savedPost = invocation.getArgument(0);
            if (savedPost.getId() == null) savedPost.setId(UUID.randomUUID()); // Simuliert die ID-Generierung
            return savedPost;
        });

        BlogPost createdPost = blogPostService.createBlogPost(newBlogPost, imageFile); // Ruft die zu testende Methode auf

        assertNotNull(createdPost.getId()); // Überprüft, ob eine ID zugewiesen wurde
        assertEquals("Neuer Titel", createdPost.getTitle()); // Überprüft den Titel
        assertEquals("base64ImageData", createdPost.getImage()); // Überprüft das kodierte Bild
        assertFalse(createdPost.isPublished()); // Überprüft den Veröffentlichungsstatus
        assertNotNull(createdPost.getCreatedAt()); // Überprüft den Erstellungszeitstempel
        assertNotNull(createdPost.getUpdatedAt()); // Überprüft den Aktualisierungszeitstempel
        verify(blogPostRepository, times(1)).save(any(BlogPost.class)); // Überprüft, ob save einmal aufgerufen wurde
    }

    /**
     * Testet die {@code createBlogPost}-Methode: Sollte eine {@link IllegalArgumentException} werfen, wenn eine ID bereitgestellt wird.
     */
    @Test
    void createBlogPost_shouldThrowExceptionIfIdProvided() {
        // Versucht, einen Blogbeitrag mit bereits vorhandener ID zu erstellen
        assertThrows(IllegalArgumentException.class, () -> blogPostService.createBlogPost(testBlogPost, null));
        verify(blogPostRepository, never()).save(any(BlogPost.class)); // Überprüft, dass save nie aufgerufen wurde
    }

    /**
     * Testet die {@code updateBlogPost}-Methode: Sollte einen bestehenden Blogbeitrag aktualisieren.
     */
    @Test
    void updateBlogPost_shouldUpdateExistingBlogPost() {
        BlogPost updatedInfo = new BlogPost(testBlogPostId, "Aktualisierter Titel", "aktualisierter-slug", "Aktualisierter Inhalt", null, true, null, null);
        MockMultipartFile imageFile = new MockMultipartFile("image", "image.jpg", "image/jpeg", "some new image data".getBytes());

        // Definiert das Verhalten der Mocks
        when(blogPostRepository.findById(testBlogPostId)).thenReturn(Optional.of(testBlogPost));
        when(imageUtil.processAndEncodeImage(imageFile)).thenReturn("newBase64ImageData");
        when(blogPostRepository.save(any(BlogPost.class))).thenReturn(testBlogPost);

        Optional<BlogPost> result = blogPostService.updateBlogPost(testBlogPostId, updatedInfo, imageFile); // Ruft die zu testende Methode auf

        assertTrue(result.isPresent()); // Überprüft, ob ein Ergebnis vorhanden ist
        assertEquals("Aktualisierter Titel", result.get().getTitle()); // Überprüft den aktualisierten Titel
        assertEquals("aktualisierter-slug", result.get().getSlug()); // Überprüft den aktualisierten Slug
        assertEquals("newBase64ImageData", result.get().getImage()); // Überprüft das neue Bild
        assertTrue(result.get().isPublished()); // Überprüft den Veröffentlichungsstatus
        verify(blogPostRepository, times(1)).save(testBlogPost); // Überprüft, ob save einmal aufgerufen wurde
    }

    /**
     * Testet die {@code updateBlogPost}-Methode: Sollte ein leeres Optional zurückgeben, wenn der Blogbeitrag nicht gefunden wird.
     */
    @Test
    void updateBlogPost_shouldReturnEmptyIfNotFound() {
        BlogPost updatedInfo = new BlogPost(testBlogPostId, "Aktualisierter Titel", "aktualisierter-slug", "Aktualisierter Inhalt", null, true, null, null);
        when(blogPostRepository.findById(testBlogPostId)).thenReturn(Optional.empty()); // Simuliert, dass der Beitrag nicht gefunden wird

        Optional<BlogPost> result = blogPostService.updateBlogPost(testBlogPostId, updatedInfo, null); // Ruft die zu testende Methode auf

        assertTrue(result.isEmpty()); // Überprüft, ob das Ergebnis leer ist
        verify(blogPostRepository, never()).save(any(BlogPost.class)); // Überprüft, dass save nie aufgerufen wurde
    }

    /**
     * Testet die {@code deleteBlogPost}-Methode: Sollte true zurückgeben, wenn der Blogbeitrag gelöscht wurde.
     */
    @Test
    void deleteBlogPost_shouldReturnTrueIfDeleted() {
        when(blogPostRepository.existsById(testBlogPostId)).thenReturn(true);
        doNothing().when(blogPostRepository).deleteById(testBlogPostId);
        assertTrue(blogPostService.deleteBlogPost(testBlogPostId));
        verify(blogPostRepository, times(1)).deleteById(testBlogPostId);
    }

    /**
     * Testet die {@code deleteBlogPost}-Methode: Sollte false zurückgeben, wenn der Blogbeitrag nicht gefunden wird.
     */
    @Test
    void deleteBlogPost_shouldReturnFalseIfNotFound() {
        when(blogPostRepository.existsById(testBlogPostId)).thenReturn(false);
        assertFalse(blogPostService.deleteBlogPost(testBlogPostId));
        verify(blogPostRepository, never()).deleteById(any());
    }

    /**
     * Testet die {@code publishBlogPost}-Methode: Sollte den Veröffentlichungsstatus auf true setzen.
     */
    @Test
    void publishBlogPost_shouldSetPublishedToTrue() {
        when(blogPostRepository.findById(testBlogPostId)).thenReturn(Optional.of(testBlogPost)); // Simuliert, dass der Beitrag gefunden wird
        when(blogPostRepository.save(any(BlogPost.class))).thenReturn(testBlogPost); // Simuliert das Speichern

        Optional<BlogPost> result = blogPostService.publishBlogPost(testBlogPostId); // Ruft die zu testende Methode auf

        assertTrue(result.isPresent()); // Überprüft, ob ein Ergebnis vorhanden ist
        assertTrue(result.get().isPublished()); // Überprüft, ob der Status auf veröffentlicht gesetzt wurde
        verify(blogPostRepository, times(1)).save(testBlogPost); // Überprüft, ob save einmal aufgerufen wurde
    }

    /**
     * Testet die {@code unpublishBlogPost}-Methode: Sollte den Veröffentlichungsstatus auf false setzen.
     */
    @Test
    void unpublishBlogPost_shouldSetPublishedToFalse() {
        testBlogPost.setPublished(true); // Stellt sicher, dass der Beitrag zuerst veröffentlicht ist
        when(blogPostRepository.findById(testBlogPostId)).thenReturn(Optional.of(testBlogPost)); // Simuliert, dass der Beitrag gefunden wird
        when(blogPostRepository.save(any(BlogPost.class))).thenReturn(testBlogPost); // Simuliert das Speichern

        Optional<BlogPost> result = blogPostService.unpublishBlogPost(testBlogPostId); // Ruft die zu testende Methode auf

        assertTrue(result.isPresent()); // Überprüft, ob ein Ergebnis vorhanden ist
        assertFalse(result.get().isPublished()); // Überprüft, ob der Status auf nicht veröffentlicht gesetzt wurde
        verify(blogPostRepository, times(1)).save(testBlogPost); // Überprüft, ob save einmal aufgerufen wurde
    }

    /**
     * Testet die {@code getBlogPostById}-Methode: Sollte den Blogbeitrag zurückgeben.
     */
    @Test
    void getBlogPostById_shouldReturnBlogPost() {
        when(blogPostRepository.findById(testBlogPostId)).thenReturn(Optional.of(testBlogPost)); // Simuliert, dass der Beitrag gefunden wird
        Optional<BlogPost> result = blogPostService.getBlogPostById(testBlogPostId); // Ruft die zu testende Methode auf
        assertTrue(result.isPresent()); // Überprüft, ob ein Ergebnis vorhanden ist
        assertEquals(testBlogPost, result.get()); // Überprüft, ob der korrekte Beitrag zurückgegeben wurde
    }

    /**
     * Testet die {@code getBlogPostById}-Methode: Sollte ein leeres Optional zurückgeben, wenn der Blogbeitrag nicht gefunden wird.
     */
    @Test
    void getBlogPostById_shouldReturnEmptyIfNotFound() {
        when(blogPostRepository.findById(testBlogPostId)).thenReturn(Optional.empty()); // Simuliert, dass der Beitrag nicht gefunden wird
        Optional<BlogPost> result = blogPostService.getBlogPostById(testBlogPostId); // Ruft die zu testende Methode auf
        assertTrue(result.isEmpty()); // Überprüft, ob das Ergebnis leer ist
    }

    /**
     * Testet die {@code getBlogPostBySlug}-Methode: Sollte den Blogbeitrag zurückgeben.
     */
    @Test
    void getBlogPostBySlug_shouldReturnBlogPost() {
        when(blogPostRepository.findBySlug("test-slug")).thenReturn(Optional.of(testBlogPost)); // Simuliert, dass der Beitrag gefunden wird
        Optional<BlogPost> result = blogPostService.getBlogPostBySlug("test-slug"); // Ruft die zu testende Methode auf
        assertTrue(result.isPresent()); // Überprüft, ob ein Ergebnis vorhanden ist
        assertEquals(testBlogPost, result.get()); // Überprüft, ob der korrekte Beitrag zurückgegeben wurde
    }

    /**
     * Testet die {@code getBlogPostBySlug}-Methode: Sollte ein leeres Optional zurückgeben, wenn der Blogbeitrag nicht gefunden wird.
     */
    @Test
    void getBlogPostBySlug_shouldReturnEmptyIfNotFound() {
        when(blogPostRepository.findBySlug("non-existent-slug")).thenReturn(Optional.empty()); // Simuliert, dass der Beitrag nicht gefunden wird
        Optional<BlogPost> result = blogPostService.getBlogPostBySlug("non-existent-slug"); // Ruft die zu testende Methode auf
        assertTrue(result.isEmpty()); // Überprüft, ob das Ergebnis leer ist
    }

    /**
     * Testet die {@code getAllBlogPosts}-Methode: Sollte alle Blogbeiträge zurückgeben.
     */
    @Test
    void getAllBlogPosts_shouldReturnAllBlogPosts() {
        List<BlogPost> blogPosts = Arrays.asList(testBlogPost, new BlogPost(UUID.randomUUID(), "Ein anderer", "ein-anderer", "Inhalt", null, true, LocalDateTime.now(), LocalDateTime.now()));
        when(blogPostRepository.findAll()).thenReturn(blogPosts); // Simuliert die Rückgabe aller Beiträge
        List<BlogPost> result = blogPostService.getAllBlogPosts(); // Ruft die zu testende Methode auf
        assertEquals(2, result.size()); // Überprüft die Anzahl der Beiträge
        assertTrue(result.contains(testBlogPost)); // Überprüft, ob der Testbeitrag enthalten ist
    }

    /**
     * Testet die {@code getPublishedBlogPosts}-Methode: Sollte nur veröffentlichte Blogbeiträge zurückgeben.
     */
    @Test
    void getPublishedBlogPosts_shouldReturnOnlyPublished() {
        BlogPost publishedPost = new BlogPost(UUID.randomUUID(), "Veröffentlicht", "veroeffentlicht", "Inhalt", null, true, LocalDateTime.now(), LocalDateTime.now());
        List<BlogPost> allPosts = Arrays.asList(testBlogPost, publishedPost); // testBlogPost ist unveröffentlicht
        when(blogPostRepository.findAll()).thenReturn(allPosts); // Simuliert die Rückgabe aller Beiträge

        List<BlogPost> result = blogPostService.getPublishedBlogPosts(); // Ruft die zu testende Methode auf

        assertEquals(1, result.size()); // Überprüft die Anzahl der veröffentlichten Beiträge
        assertTrue(result.contains(publishedPost)); // Überprüft, ob der veröffentlichte Beitrag enthalten ist
        assertFalse(result.contains(testBlogPost)); // Überprüft, ob der unveröffentlichte Beitrag NICHT enthalten ist
    }
}
