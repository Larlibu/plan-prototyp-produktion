package de.example.app.security.service;

import de.example.app.security.dto.RegistrationDto;
import de.example.app.security.entity.Role;
import de.example.app.security.entity.User;
import de.example.app.security.repository.RoleRepository;
import de.example.app.security.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;

/**
 * Service-Klasse für die Registrierung neuer Benutzer.
 * Validiert die Registrierungsdaten, prüft auf bereits vorhandene Benutzernamen
 * und E-Mail-Adressen, hasht das Passwort und legt den neuen Benutzer mit der
 * Standardrolle {@code ROLE_USER} in der Datenbank an.
 */
@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Erstellt einen neuen {@code RegistrationService} mit den erforderlichen Abhängigkeiten.
     *
     * @param userRepository  Repository für den Datenzugriff auf Benutzer
     * @param roleRepository  Repository für den Datenzugriff auf Rollen
     * @param passwordEncoder Encoder zum BCrypt-Hashen von Passwörtern
     */
    public RegistrationService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registriert einen neuen Benutzer anhand der übergebenen Registrierungsdaten.
     * Prüft, ob Benutzername oder E-Mail-Adresse bereits vergeben sind, und ob die
     * Passwörter übereinstimmen. Weist dem neuen Benutzer automatisch die Rolle
     * {@code ROLE_USER} zu und speichert ihn mit BCrypt-gehastem Passwort.
     *
     * @param dto Die Registrierungsdaten des neuen Benutzers
     * @throws IllegalArgumentException wenn Benutzername oder E-Mail bereits existieren
     *                                  oder die Passwörter nicht übereinstimmen
     * @throws IllegalStateException    wenn die Rolle {@code ROLE_USER} nicht in der
     *                                  Datenbank gefunden werden kann
     */
    public void register(RegistrationDto dto) {
        if (userRepository.findByUsernameIgnoreCase(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Benutzername bereits vergeben.");
        }
        if (userRepository.findByEmailIgnoreCase(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("E-Mail-Adresse bereits registriert.");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwörter stimmen nicht überein.");
        }

        Role userRole = roleRepository.findByNameIgnoreCase("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("ROLE_USER nicht gefunden."));

        User newUser = new User(
                null,
                dto.getUsername(),
                passwordEncoder.encode(dto.getPassword()),
                dto.getEmail(),
                true,
                LocalDateTime.now(),
                LocalDateTime.now(),
                new HashSet<>(Collections.singletonList(userRole))
        );
        userRepository.save(newUser);
    }
}
