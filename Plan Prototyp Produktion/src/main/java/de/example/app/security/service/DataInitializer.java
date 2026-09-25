package de.example.app.security.service;

import de.example.app.security.entity.Role;
import de.example.app.security.entity.User;
import de.example.app.security.repository.RoleRepository;
import de.example.app.security.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Initialisiert wesentliche Daten für die Anwendung, wie z.B. Standardrollen und einen Admin-Benutzer.
 * Diese Komponente wird einmal nach dem Laden des Anwendungskontextes ausgeführt.
 */
@Component
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:password}")
    private String adminPassword;

    @Value("${app.admin.email:admin@example.com}")
    private String adminEmail;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Initialisierungsmethode, die nach der Konstruktion des Beans aufgerufen wird.
     * Initialisiert Standardrollen und einen Admin-Benutzer, falls diese noch nicht existieren.
     */
    @PostConstruct
    public void initData() {
        log.info("Initialisiere Standardrollen und Admin-Benutzer...");
        Role adminRole = createRoleIfNotFound("ROLE_ADMIN");
        createRoleIfNotFound("ROLE_EDITOR");
        createRoleIfNotFound("ROLE_USER");

        ensureAdminUser(adminRole);
        log.info("Initialisierung von Standardrollen und Admin-Benutzer abgeschlossen.");
    }

    /**
     * Erstellt eine Rolle mit dem gegebenen Namen, falls sie noch nicht existiert.
     *
     * @param roleName Der Name der zu erstellenden Rolle (z.B. "ROLE_ADMIN").
     * @return Die bestehende oder neu erstellte {@link Role}-Entität.
     */
    private Role createRoleIfNotFound(String roleName) {
        Optional<Role> existingRole = roleRepository.findByNameIgnoreCase(roleName);
        if (existingRole.isPresent()) {
            log.debug("Rolle '{}' existiert bereits.", roleName);
            return existingRole.get();
        } else {
            Role newRole = new Role(null, roleName);
            roleRepository.save(newRole);
            log.info("Neue Rolle erstellt: '{}'", roleName);
            return newRole;
        }
    }

    /**
     * Stellt sicher, dass der Admin-Benutzer mit Standardanmeldeinformationen und der ADMIN-Rolle existiert.
     * Wenn der Admin-Benutzer nicht existiert, wird er erstellt.
     * Wenn er existiert, wird überprüft, ob das Passwort mit dem konfigurierten Admin-Passwort übereinstimmt,
     * und die ADMIN-Rolle wird hinzugefügt, falls sie fehlt.
     *
     * @param adminRole Die {@link Role}-Entität für die ADMIN-Rolle.
     */
    private void ensureAdminUser(Role adminRole) {
        Optional<User> existingAdminOptional = userRepository.findByUsernameIgnoreCase(adminUsername);
        User adminUser;
        boolean changed = false;

        if (existingAdminOptional.isEmpty()) {
            log.warn("Admin-Benutzer '{}' nicht gefunden → wird angelegt.", adminUsername);
            adminUser = new User(
                    null,
                    adminUsername,
                    adminPassword,
                    adminEmail,
                    true,
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    new HashSet<>(Collections.singletonList(adminRole))
            );
            changed = true;
        } else {
            adminUser = existingAdminOptional.get();
            log.info("Admin-Benutzer '{}' gefunden [enabled={}, rollen={}]",
                    adminUser.getUsername(),
                    adminUser.isEnabled(),
                    adminUser.getRoles().stream().map(Role::getName).collect(Collectors.joining(",")));

            // Erlaubt es, das Admin-Passwort über die konfigurierte Umgebungsvariable zurückzusetzen,
            // falls es vom gespeicherten Wert abweicht (z.B. nach einem manuellen DB-Eingriff).
            if (!passwordEncoder.matches(adminPassword, adminUser.getPassword())) {
                log.warn("Admin-Passwort stimmt NICHT mit konfiguriertem Passwort überein → wird zurückgesetzt.");
                adminUser.setPassword(passwordEncoder.encode(adminPassword));
                changed = true;
            } else {
                log.info("Admin-Passwort verifiziert ✓");
            }

            if (!adminUser.getRoles().contains(adminRole)) {
                log.warn("Admin-Benutzer '{}' fehlt die ADMIN-Rolle → wird hinzugefügt.", adminUsername);
                adminUser.getRoles().add(adminRole);
                changed = true;
            }
        }

        if (changed) {
            userRepository.save(adminUser);
            log.info("Admin-Benutzer '{}' erfolgreich aktualisiert/angelegt.", adminUsername);
        }
    }
}
