package de.example.app.security.service;

import de.example.app.security.entity.Role;
import de.example.app.security.entity.User;
import de.example.app.security.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit-Tests für den {@link FileUserDetailsService}.
 * Verwendet Mockito, um Abhängigkeiten wie {@link UserRepository} zu mocken.
 */
@ExtendWith(MockitoExtension.class) // Aktiviert Mockito-Erweiterung für JUnit 5
class FileUserDetailsServiceTest {

    @Mock // Erstellt ein Mock-Objekt für UserRepository
    private UserRepository userRepository;

    @InjectMocks // Injiziert die Mocks in den FileUserDetailsService
    private FileUserDetailsService fileUserDetailsService;

    private User testUser; // Eine Test-Entität für Benutzer
    private Role adminRole; // Eine Test-Entität für die Admin-Rolle

    /**
     * Setup-Methode, die vor jedem Test ausgeführt wird.
     * Initialisiert einen Test-Benutzer und eine Admin-Rolle.
     */
    @BeforeEach
    void setUp() {
        adminRole = new Role(UUID.randomUUID(), "ROLE_ADMIN");
        Set<Role> roles = new HashSet<>(Collections.singletonList(adminRole));
        testUser = new User(UUID.randomUUID(), "testuser", "encodedPassword", "test@example.com", true, LocalDateTime.now(), LocalDateTime.now(), roles);
    }

    /**
     * Testet die {@code loadUserByUsername}-Methode: Sollte {@link UserDetails} zurückgeben, wenn der Benutzer existiert.
     */
    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        when(userRepository.findByUsernameIgnoreCase("testuser")).thenReturn(Optional.of(testUser)); // Simuliert, dass der Benutzer gefunden wird

        UserDetails userDetails = fileUserDetailsService.loadUserByUsername("testuser"); // Ruft die zu testende Methode auf

        assertNotNull(userDetails); // Überprüft, ob UserDetails nicht null ist
        assertEquals("testuser", userDetails.getUsername()); // Überprüft den Benutzernamen
        assertEquals("encodedPassword", userDetails.getPassword()); // Überprüft das Passwort
        assertTrue(userDetails.isEnabled()); // Überprüft, ob der Benutzer aktiviert ist
        // Überprüft, ob die Admin-Rolle als GrantedAuthority enthalten ist
        assertTrue(userDetails.getAuthorities().contains(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    /**
     * Testet die {@code loadUserByUsername}-Methode: Sollte {@link UsernameNotFoundException} werfen, wenn der Benutzer nicht existiert.
     */
    @Test
    void loadUserByUsername_shouldThrowUsernameNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findByUsernameIgnoreCase("nonexistent")).thenReturn(Optional.empty()); // Simuliert, dass der Benutzer nicht gefunden wird

        // Überprüft, ob die erwartete Ausnahme geworfen wird
        assertThrows(UsernameNotFoundException.class, () -> fileUserDetailsService.loadUserByUsername("nonexistent"));
    }

    /**
     * Testet die {@code loadUserByUsername}-Methode: Sollte {@link UserDetails} mit deaktiviertem Status zurückgeben, wenn der Benutzer deaktiviert ist.
     */
    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserIsDisabled() {
        testUser.setEnabled(false); // Deaktiviert den Test-Benutzer
        when(userRepository.findByUsernameIgnoreCase("testuser")).thenReturn(Optional.of(testUser)); // Simuliert, dass der Benutzer gefunden wird

        UserDetails userDetails = fileUserDetailsService.loadUserByUsername("testuser"); // Ruft die zu testende Methode auf

        assertNotNull(userDetails); // Überprüft, ob UserDetails nicht null ist
        assertFalse(userDetails.isEnabled()); // Überprüft, ob der Benutzer deaktiviert ist
    }
}
