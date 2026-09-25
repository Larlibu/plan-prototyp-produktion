package de.example.app.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Datenübertragungsobjekt (DTO) für das Registrierungsformular.
 * Kapselt alle Eingaben, die ein neuer Benutzer bei der Registrierung angeben muss,
 * und definiert Bean-Validation-Einschränkungen für jeden Wert.
 * Die Lombok-Annotationen {@code @Data} und {@code @NoArgsConstructor} generieren
 * automatisch Getter, Setter sowie einen parameterlosen Konstruktor.
 */
@Data
@NoArgsConstructor
public class RegistrationDto {

    /**
     * Gewünschter Benutzername des neuen Kontos. Muss zwischen 3 und 50 Zeichen lang sein
     * und darf nicht leer sein.
     */
    @NotBlank(message = "Benutzername ist erforderlich")
    @Size(min = 3, max = 50, message = "Benutzername muss zwischen 3 und 50 Zeichen lang sein")
    private String username;

    /**
     * E-Mail-Adresse des neuen Benutzers. Muss eine gültige E-Mail-Adresse sein
     * und darf nicht leer sein.
     */
    @NotBlank(message = "E-Mail-Adresse ist erforderlich")
    @Email(message = "Ungültige E-Mail-Adresse")
    private String email;

    /**
     * Gewünschtes Passwort des neuen Kontos. Muss mindestens 8 Zeichen lang sein
     * und darf nicht leer sein. Wird vor der Speicherung BCrypt-gehasht.
     */
    @NotBlank(message = "Passwort ist erforderlich")
    @Size(min = 8, message = "Passwort muss mindestens 8 Zeichen lang sein")
    private String password;

    /**
     * Bestätigungspasswort zur Prüfung, ob beide Passworteingaben übereinstimmen.
     * Darf nicht leer sein. Die inhaltliche Übereinstimmung mit {@link #password}
     * wird im {@code RegistrationService} geprüft.
     */
    @NotBlank(message = "Passwortbestätigung ist erforderlich")
    private String confirmPassword;
}
