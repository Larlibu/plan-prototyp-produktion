package de.example.app.security.controller;

import de.example.app.security.dto.RegistrationDto;
import de.example.app.security.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller für die Benutzerregistrierung.
 * Verarbeitet GET- und POST-Anfragen unter {@code /register}, zeigt das
 * Registrierungsformular an und delegiert die eigentliche Registrierungslogik
 * an den {@link RegistrationService}.
 */
@Tag(name = "Authentifizierung", description = "Authentifizierungsseiten der gesunden Lebensmittel-Plattform")
@Controller
@RequestMapping("/register")
public class RegistrationController {

    private final RegistrationService registrationService;

    /**
     * Erstellt einen neuen {@code RegistrationController}.
     *
     * @param registrationService Der Service, der die Registrierungslogik enthält
     */
    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    /**
     * Zeigt das leere Registrierungsformular an.
     * Fügt dem Model ein leeres {@link RegistrationDto} hinzu, das vom Formular
     * als Binding-Objekt verwendet wird.
     *
     * @param model Das Spring-MVC-Model für die View
     * @return der Name des Thymeleaf-Templates {@code "register"}
     */
    @Operation(summary = "Registrierungsformular anzeigen")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Leeres Registrierungsformular erfolgreich geladen")
    })
    @GetMapping
    public String showRegistrationForm(Model model) {
        model.addAttribute("registrationDto", new RegistrationDto());
        return "register";
    }

    /**
     * Verarbeitet das abgesendete Registrierungsformular.
     * Prüft zuerst auf Bean-Validation-Fehler. Bei Erfolg wird die Registrierung
     * über den {@link RegistrationService} durchgeführt und der Benutzer zur
     * Login-Seite weitergeleitet. Bei fachlichen Fehlern (z.B. Benutzername vergeben)
     * wird das Formular mit einer Fehlermeldung erneut angezeigt.
     *
     * @param dto                Das aus dem Formular gebundene {@link RegistrationDto}
     * @param bindingResult      Enthält Validierungsfehler aus {@code @Valid}
     * @param redirectAttributes Ermöglicht das Übergeben von Flash-Attributen für die Weiterleitung
     * @return Weiterleitung zu {@code /login} bei Erfolg, oder Rückkehr zum Template
     *         {@code "register"} bei Validierungs- oder Geschäftsfehlern
     */
    @Operation(summary = "Registrierungsformular verarbeiten und neuen Benutzer anlegen")
    @ApiResponses({
        @ApiResponse(responseCode = "302", description = "Weiterleitung zur Login-Seite nach erfolgreicher Registrierung"),
        @ApiResponse(responseCode = "200", description = "Formular mit Validierungs- oder Geschäftsfehlern erneut angezeigt")
    })
    @PostMapping
    public String processRegistration(
            @Valid @ModelAttribute("registrationDto") RegistrationDto dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            registrationService.register(dto);
            redirectAttributes.addFlashAttribute("message", "Registrierung erfolgreich! Bitte jetzt anmelden.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            bindingResult.reject("registration.error", e.getMessage());
            return "register";
        }
    }
}
