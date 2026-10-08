package de.example.app.crm.service;

import de.example.app.crm.entity.NewsletterSubscriber;
import de.example.app.crm.repository.NewsletterSubscriberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für den {@link NewsletterService}.
 * Verwendet Mockito, um Abhängigkeiten wie {@link NewsletterSubscriberRepository} zu mocken.
 */
@ExtendWith(MockitoExtension.class) // Aktiviert Mockito-Erweiterung für JUnit 5
class NewsletterServiceTest {

    @Mock // Erstellt ein Mock-Objekt für NewsletterSubscriberRepository
    private NewsletterSubscriberRepository newsletterSubscriberRepository;

    @InjectMocks // Injiziert die Mocks in den NewsletterService
    private NewsletterService newsletterService;

    private NewsletterSubscriber testSubscriber; // Eine Test-Entität für Newsletter-Abonnenten
    private UUID testSubscriberId; // Die ID des Test-Abonnenten
    private String testEmail; // Die E-Mail-Adresse des Test-Abonnenten

    /**
     * Setup-Methode, die vor jedem Test ausgeführt wird.
     * Initialisiert eine Test-Newsletter-Abonnenten-Entität.
     */
    @BeforeEach
    void setUp() {
        testSubscriberId = UUID.randomUUID();
        testEmail = "test@example.com";
        testSubscriber = new NewsletterSubscriber(testSubscriberId, testEmail, true, LocalDateTime.now());
    }

    /**
     * Testet die {@code subscribeNewsletter}-Methode: Sollte einen neuen Abonnenten erstellen, wenn nicht gefunden.
     */
    @Test
    void subscribeNewsletter_shouldCreateNewSubscriberIfNotFound() {
        when(newsletterSubscriberRepository.findByEmailIgnoreCase(testEmail)).thenReturn(Optional.empty()); // Simuliert, dass kein Abonnent gefunden wird
        // Definiert das Verhalten des Repository-Mocks beim Speichern
        when(newsletterSubscriberRepository.save(any(NewsletterSubscriber.class))).thenAnswer(invocation -> {
            NewsletterSubscriber savedSubscriber = invocation.getArgument(0);
            if (savedSubscriber.getId() == null) savedSubscriber.setId(UUID.randomUUID()); // Simuliert die ID-Generierung
            return savedSubscriber;
        });

        NewsletterSubscriber result = newsletterService.subscribeNewsletter(testEmail); // Ruft die zu testende Methode auf

        assertNotNull(result.getId()); // Überprüft, ob eine ID zugewiesen wurde
        assertEquals(testEmail, result.getEmail()); // Überprüft die E-Mail
        assertTrue(result.isActive()); // Überprüft den Aktivitätsstatus
        verify(newsletterSubscriberRepository, times(1)).save(any(NewsletterSubscriber.class)); // Überprüft, ob save einmal aufgerufen wurde
    }

    /**
     * Testet die {@code subscribeNewsletter}-Methode: Sollte einen inaktiven Abonnenten reaktivieren.
     */
    @Test
    void subscribeNewsletter_shouldReactivateInactiveSubscriber() {
        testSubscriber.setActive(false); // Setzt den Test-Abonnenten auf inaktiv
        when(newsletterSubscriberRepository.findByEmailIgnoreCase(testEmail)).thenReturn(Optional.of(testSubscriber)); // Simuliert, dass ein inaktiver Abonnent gefunden wird
        when(newsletterSubscriberRepository.save(any(NewsletterSubscriber.class))).thenReturn(testSubscriber); // Simuliert das Speichern

        NewsletterSubscriber result = newsletterService.subscribeNewsletter(testEmail); // Ruft die zu testende Methode auf

        assertTrue(result.isActive()); // Überprüft, ob der Abonnent reaktiviert wurde
        verify(newsletterSubscriberRepository, times(1)).save(testSubscriber); // Überprüft, ob save einmal aufgerufen wurde
    }

    /**
     * Testet die {@code subscribeNewsletter}-Methode: Sollte eine {@link IllegalArgumentException} werfen, wenn bereits aktiv.
     */
    @Test
    void subscribeNewsletter_shouldThrowExceptionIfAlreadyActive() {
        when(newsletterSubscriberRepository.findByEmailIgnoreCase(testEmail)).thenReturn(Optional.of(testSubscriber)); // Simuliert, dass ein aktiver Abonnent gefunden wird

        // Versucht, einen bereits aktiven Abonnenten erneut zu abonnieren
        assertThrows(IllegalArgumentException.class, () -> newsletterService.subscribeNewsletter(testEmail));
        verify(newsletterSubscriberRepository, never()).save(any(NewsletterSubscriber.class)); // Überprüft, dass save nie aufgerufen wurde
    }

    /**
     * Testet die {@code unsubscribeNewsletter}-Methode: Sollte den Abonnenten deaktivieren.
     */
    @Test
    void unsubscribeNewsletter_shouldDeactivateSubscriber() {
        when(newsletterSubscriberRepository.findByEmailIgnoreCase(testEmail)).thenReturn(Optional.of(testSubscriber)); // Simuliert, dass der Abonnent gefunden wird
        when(newsletterSubscriberRepository.save(any(NewsletterSubscriber.class))).thenReturn(testSubscriber); // Simuliert das Speichern

        boolean result = newsletterService.unsubscribeNewsletter(testEmail); // Ruft die zu testende Methode auf

        assertTrue(result); // Überprüft, ob die Abmeldung erfolgreich war
        assertFalse(testSubscriber.isActive()); // Überprüft, ob der Abonnent inaktiv gesetzt wurde
        verify(newsletterSubscriberRepository, times(1)).save(testSubscriber); // Überprüft, ob save einmal aufgerufen wurde
    }

    /**
     * Testet die {@code unsubscribeNewsletter}-Methode: Sollte false zurückgeben, wenn der Abonnent nicht gefunden wird.
     */
    @Test
    void unsubscribeNewsletter_shouldReturnFalseIfNotFound() {
        when(newsletterSubscriberRepository.findByEmailIgnoreCase(testEmail)).thenReturn(Optional.empty()); // Simuliert, dass der Abonnent nicht gefunden wird

        boolean result = newsletterService.unsubscribeNewsletter(testEmail); // Ruft die zu testende Methode auf

        assertFalse(result); // Überprüft, ob die Abmeldung fehlschlägt
        verify(newsletterSubscriberRepository, never()).save(any(NewsletterSubscriber.class)); // Überprüft, dass save nie aufgerufen wurde
    }

    /**
     * Testet die {@code getSubscriberById}-Methode: Sollte den Abonnenten zurückgeben.
     */
    @Test
    void getSubscriberById_shouldReturnSubscriber() {
        when(newsletterSubscriberRepository.findById(testSubscriberId)).thenReturn(Optional.of(testSubscriber)); // Simuliert, dass der Abonnent gefunden wird
        Optional<NewsletterSubscriber> result = newsletterService.getSubscriberById(testSubscriberId); // Ruft die zu testende Methode auf
        assertTrue(result.isPresent()); // Überprüft, ob ein Ergebnis vorhanden ist
        assertEquals(testSubscriber, result.get()); // Überprüft, ob der korrekte Abonnent zurückgegeben wurde
    }

    /**
     * Testet die {@code getSubscriberById}-Methode: Sollte ein leeres Optional zurückgeben, wenn der Abonnent nicht gefunden wird.
     */
    @Test
    void getSubscriberById_shouldReturnEmptyIfNotFound() {
        when(newsletterSubscriberRepository.findById(testSubscriberId)).thenReturn(Optional.empty()); // Simuliert, dass der Abonnent nicht gefunden wird
        Optional<NewsletterSubscriber> result = newsletterService.getSubscriberById(testSubscriberId); // Ruft die zu testende Methode auf
        assertTrue(result.isEmpty()); // Überprüft, ob das Ergebnis leer ist
    }

    /**
     * Testet die {@code getAllActiveSubscribers}-Methode: Sollte nur aktive Abonnenten zurückgeben.
     */
    @Test
    void getAllActiveSubscribers_shouldReturnOnlyActiveSubscribers() {
        NewsletterSubscriber inactiveSubscriber = new NewsletterSubscriber(UUID.randomUUID(), "inactive@example.com", false, LocalDateTime.now());
        List<NewsletterSubscriber> allSubscribers = Arrays.asList(testSubscriber, inactiveSubscriber);
        when(newsletterSubscriberRepository.findAll()).thenReturn(allSubscribers); // Simuliert die Rückgabe aller Abonnenten

        List<NewsletterSubscriber> result = newsletterService.getAllActiveSubscribers(); // Ruft die zu testende Methode auf

        assertEquals(1, result.size()); // Überprüft die Anzahl der aktiven Abonnenten
        assertTrue(result.contains(testSubscriber)); // Überprüft, ob der aktive Abonnent enthalten ist
        assertFalse(result.contains(inactiveSubscriber)); // Überprüft, ob der inaktive Abonnent NICHT enthalten ist
    }

    /**
     * Testet die {@code getAllSubscribers}-Methode: Sollte alle Abonnenten zurückgeben (aktive und inaktive).
     */
    @Test
    void getAllSubscribers_shouldReturnAllSubscribers() {
        NewsletterSubscriber inactiveSubscriber = new NewsletterSubscriber(UUID.randomUUID(), "inactive@example.com", false, LocalDateTime.now());
        List<NewsletterSubscriber> allSubscribers = Arrays.asList(testSubscriber, inactiveSubscriber);
        when(newsletterSubscriberRepository.findAll()).thenReturn(allSubscribers); // Simuliert die Rückgabe aller Abonnenten

        List<NewsletterSubscriber> result = newsletterService.getAllSubscribers(); // Ruft die zu testende Methode auf

        assertEquals(2, result.size()); // Überprüft die Anzahl aller Abonnenten
        assertTrue(result.contains(testSubscriber)); // Überprüft, ob der aktive Abonnent enthalten ist
        assertTrue(result.contains(inactiveSubscriber)); // Überprüft, ob der inaktive Abonnent enthalten ist
    }

    /**
     * Testet die {@code deleteSubscriber}-Methode: Sollte true zurückgeben, wenn der Abonnent gelöscht wurde.
     */
    @Test
    void deleteSubscriber_shouldReturnTrueIfDeleted() {
        when(newsletterSubscriberRepository.existsById(testSubscriberId)).thenReturn(true);
        doNothing().when(newsletterSubscriberRepository).deleteById(testSubscriberId);
        assertTrue(newsletterService.deleteSubscriber(testSubscriberId));
        verify(newsletterSubscriberRepository, times(1)).deleteById(testSubscriberId);
    }

    /**
     * Testet die {@code deleteSubscriber}-Methode: Sollte false zurückgeben, wenn der Abonnent nicht gefunden wird.
     */
    @Test
    void deleteSubscriber_shouldReturnFalseIfNotFound() {
        when(newsletterSubscriberRepository.existsById(testSubscriberId)).thenReturn(false);
        assertFalse(newsletterService.deleteSubscriber(testSubscriberId));
        verify(newsletterSubscriberRepository, never()).deleteById(any());
    }
}
