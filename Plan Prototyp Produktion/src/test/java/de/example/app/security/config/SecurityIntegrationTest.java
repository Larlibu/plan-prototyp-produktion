package de.example.app.security.config;

import de.example.app.Application;
import de.example.app.security.entity.Role;
import de.example.app.security.entity.User;
import de.example.app.security.repository.RoleRepository;
import de.example.app.security.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integrationstests für die Spring Security Konfiguration.
 * Diese Tests überprüfen das Authentifizierungs- und Autorisierungsverhalten der Anwendung
 * unter Verwendung von {@link MockMvc}.
 */
@SpringBootTest(classes = Application.class) // Lädt den vollständigen Spring-Anwendungskontext
@AutoConfigureMockMvc // Konfiguriert MockMvc automatisch
@ActiveProfiles("test") // Aktiviert das "test"-Profil, falls spezifische Testkonfigurationen vorhanden sind
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc; // MockMvc-Instanz zum Simulieren von HTTP-Anfragen

    @Autowired
    private WebApplicationContext context; // Der Spring-Anwendungskontext

    @Autowired
    private UserRepository userRepository; // Repository für den Datenzugriff auf Benutzer

    @Autowired
    private RoleRepository roleRepository; // Repository für den Datenzugriff auf Rollen

    @Autowired
    private PasswordEncoder passwordEncoder; // Für das Hashing von Passwörtern

    // Test-Benutzer und -Rollen
    private User adminUser;
    private User editorUser;
    private User regularUser;
    private Role adminRole;
    private Role editorRole;

    /**
     * Setup-Methode, die vor jedem Test ausgeführt wird.
     * Initialisiert MockMvc mit Spring Security und erstellt Test-Benutzer und -Rollen.
     * Stellt sicher, dass die Repositories vor jedem Test geleert werden, um einen sauberen Zustand zu gewährleisten.
     */
    @BeforeEach
    void setup() {
        // Konfiguriert MockMvc, um Spring Security zu integrieren
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Leert die Repositories vor jedem Test, um einen sauberen Zustand zu gewährleisten
        userRepository.findAll().forEach(user -> userRepository.deleteById(user.getId()));
        roleRepository.findAll().forEach(role -> roleRepository.deleteById(role.getId()));

        // Erstellt Rollen
        adminRole = roleRepository.save(new Role(UUID.randomUUID(), "ROLE_ADMIN"));
        editorRole = roleRepository.save(new Role(UUID.randomUUID(), "ROLE_EDITOR"));

        // Erstellt Benutzer und speichert sie
        adminUser = new User(UUID.randomUUID(), "admin", passwordEncoder.encode("adminpass"), "admin@test.com", true, null, null, new HashSet<>(Collections.singletonList(adminRole)));
        userRepository.save(adminUser);

        editorUser = new User(UUID.randomUUID(), "editor", passwordEncoder.encode("editorpass"), "editor@test.com", true, null, null, new HashSet<>(Collections.singletonList(editorRole)));
        userRepository.save(editorUser);

        regularUser = new User(UUID.randomUUID(), "user", passwordEncoder.encode("userpass"), "user@test.com", true, null, null, Collections.emptySet());
        userRepository.save(regularUser);
    }

    /**
     * Testet den Login mit einem gültigen Benutzer: Sollte erfolgreich authentifizieren.
     *
     * @throws Exception Wenn ein Fehler bei der MockMvc-Anfrage auftritt.
     */
    @Test
    void loginWithValidUser_shouldAuthenticateSuccessfully() throws Exception {
        mockMvc.perform(formLogin("/login").user("username", "admin").password("password", "adminpass"))
                .andExpect(authenticated().withUsername("admin")) // Erwartet erfolgreiche Authentifizierung
                .andExpect(redirectedUrl("/")); // Erwartet Umleitung zur Startseite
    }

    /**
     * Testet den Login mit einem ungültigen Benutzer: Sollte die Authentifizierung fehlschlagen.
     *
     * @throws Exception Wenn ein Fehler bei der MockMvc-Anfrage auftritt.
     */
    @Test
    void loginWithInvalidUser_shouldFailAuthentication() throws Exception {
        mockMvc.perform(formLogin("/login").user("username", "admin").password("password", "wrongpass"))
                .andExpect(unauthenticated()) // Erwartet fehlgeschlagene Authentifizierung
                .andExpect(redirectedUrl("/login?error=true")); // Erwartet Umleitung zur Login-Seite mit Fehlermeldung
    }

    /**
     * Testet den Zugriff auf die Admin-Seite als Admin: Sollte erfolgreich sein (HTTP 200 OK).
     *
     * @throws Exception Wenn ein Fehler bei der MockMvc-Anfrage auftritt.
     */
    @Test
    void accessAdminPage_asAdmin_shouldSucceed() throws Exception {
        mockMvc.perform(get("/admin").with(user("admin").password("adminpass").roles("ADMIN")))
                .andExpect(status().isOk()); // Erwartet HTTP 200 OK
    }

    /**
     * Testet den Zugriff auf die Admin-Seite als Editor: Sollte fehlschlagen (HTTP 403 Forbidden).
     *
     * @throws Exception Wenn ein Fehler bei der MockMvc-Anfrage auftritt.
     */
    @Test
    void accessAdminPage_asEditor_shouldFail() throws Exception {
        mockMvc.perform(get("/admin").with(user("editor").password("editorpass").roles("EDITOR")))
                .andExpect(status().isForbidden()); // Erwartet HTTP 403 Forbidden
    }

    /**
     * Testet den Zugriff auf die Admin-Seite als regulärer Benutzer: Sollte fehlschlagen (HTTP 403 Forbidden).
     *
     * @throws Exception Wenn ein Fehler bei der MockMvc-Anfrage auftritt.
     */
    @Test
    void accessAdminPage_asRegularUser_shouldFail() throws Exception {
        mockMvc.perform(get("/admin").with(user("user").password("userpass").roles("USER")))
                .andExpect(status().isForbidden()); // Erwartet HTTP 403 Forbidden
    }

    /**
     * Testet den Zugriff auf die Admin-Seite als anonymer Benutzer: Sollte zur Login-Seite umleiten.
     *
     * @throws Exception Wenn ein Fehler bei der MockMvc-Anfrage auftritt.
     */
    @Test
    void accessAdminPage_asAnonymous_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection()) // Erwartet eine Umleitung (3xx Statuscode)
                .andExpect(redirectedUrl("http://localhost/login")); // Erwartet Umleitung zur Login-Seite
    }

    /**
     * Testet den Zugriff auf die CMS-Admin-Seite als Admin: Sollte erfolgreich sein (HTTP 200 OK).
     *
     * @throws Exception Wenn ein Fehler bei der MockMvc-Anfrage auftritt.
     */
    @Test
    void accessCmsAdminPage_asAdmin_shouldSucceed() throws Exception {
        mockMvc.perform(get("/blog/admin").with(user("admin").password("adminpass").roles("ADMIN")))
                .andExpect(status().isOk()); // Erwartet HTTP 200 OK
    }

    /**
     * Testet den Zugriff auf die CMS-Admin-Seite als Editor: Sollte erfolgreich sein (HTTP 200 OK).
     *
     * @throws Exception Wenn ein Fehler bei der MockMvc-Anfrage auftritt.
     */
    @Test
    void accessCmsAdminPage_asEditor_shouldSucceed() throws Exception {
        mockMvc.perform(get("/blog/admin").with(user("editor").password("editorpass").roles("EDITOR")))
                .andExpect(status().isOk()); // Erwartet HTTP 200 OK
    }

    /**
     * Testet den Zugriff auf die CMS-Admin-Seite als regulärer Benutzer: Sollte fehlschlagen (HTTP 403 Forbidden).
     *
     * @throws Exception Wenn ein Fehler bei der MockMvc-Anfrage auftritt.
     */
    @Test
    void accessCmsAdminPage_asRegularUser_shouldFail() throws Exception {
        mockMvc.perform(get("/blog/admin").with(user("user").password("userpass").roles("USER")))
                .andExpect(status().isForbidden()); // Erwartet HTTP 403 Forbidden
    }

    /**
     * Testet den Zugriff auf die Shop-Admin-Seite als Admin: Sollte erfolgreich sein (HTTP 200 OK).
     *
     * @throws Exception Wenn ein Fehler bei der MockMvc-Anfrage auftritt.
     */
    @Test
    void accessShopAdminPage_asAdmin_shouldSucceed() throws Exception {
        mockMvc.perform(get("/shop/admin").with(user("admin").password("adminpass").roles("ADMIN")))
                .andExpect(status().isOk()); // Erwartet HTTP 200 OK
    }

    /**
     * Testet den Zugriff auf die Shop-Admin-Seite als Editor: Sollte fehlschlagen (HTTP 403 Forbidden).
     *
     * @throws Exception Wenn ein Fehler bei der MockMvc-Anfrage auftritt.
     */
    @Test
    void accessShopAdminPage_asEditor_shouldFail() throws Exception {
        mockMvc.perform(get("/shop/admin").with(user("editor").password("editorpass").roles("EDITOR")))
                .andExpect(status().isForbidden()); // Erwartet HTTP 403 Forbidden
    }

    /**
     * Testet den Zugriff auf die Newsletter-Admin-Seite als Admin: Sollte erfolgreich sein (HTTP 200 OK).
     *
     * @throws Exception Wenn ein Fehler bei der MockMvc-Anfrage auftritt.
     */
    @Test
    void accessNewsletterAdminPage_asAdmin_shouldSucceed() throws Exception {
        mockMvc.perform(get("/newsletter/admin").with(user("admin").password("adminpass").roles("ADMIN")))
                .andExpect(status().isOk()); // Erwartet HTTP 200 OK
    }

    /**
     * Testet den Zugriff auf die Newsletter-Admin-Seite als Editor: Sollte fehlschlagen (HTTP 403 Forbidden).
     *
     * @throws Exception Wenn ein Fehler bei der MockMvc-Anfrage auftritt.
     */
    @Test
    void accessNewsletterAdminPage_asEditor_shouldFail() throws Exception {
        mockMvc.perform(get("/newsletter/admin").with(user("editor").password("editorpass").roles("EDITOR")))
                .andExpect(status().isForbidden()); // Erwartet HTTP 403 Forbidden
    }

    /**
     * Testet den Logout-Vorgang: Sollte zur Login-Seite umleiten und den Benutzer abmelden.
     *
     * @throws Exception Wenn ein Fehler bei der MockMvc-Anfrage auftritt.
     */
    @Test
    void logout_shouldRedirectToLoginPage() throws Exception {
        mockMvc.perform(post("/logout").with(csrf())) // Simuliert einen POST-Request an /logout mit CSRF-Token
                .andExpect(redirectedUrl("/login?logout=true")) // Erwartet Umleitung zur Login-Seite mit Logout-Parameter
                .andExpect(unauthenticated()); // Erwartet, dass der Benutzer nicht mehr authentifiziert ist
    }
}
