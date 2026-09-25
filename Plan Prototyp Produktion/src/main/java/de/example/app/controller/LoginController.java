package de.example.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Spring MVC Controller für die Authentifizierungsseiten der Anwendung.
 * Verarbeitet GET-Anfragen auf {@code /login} und stellt das Login-Formular
 * inklusive Fehler- und Abmeldehinweisen bereit.
 * Die eigentliche Authentifizierungslogik wird von Spring Security übernommen.
 */
@Tag(name = "Authentifizierung", description = "Authentifizierungsseiten der gesunden Lebensmittel-Plattform")
@Controller
public class LoginController {

    /**
     * Zeigt die Login-Seite an und wertet optionale URL-Parameter für
     * Fehler- und Logout-Meldungen aus.
     * Ist der Parameter {@code error} vorhanden, wird eine Fehlermeldung bei
     * ungültigen Anmeldedaten angezeigt. Ist der Parameter {@code logout} vorhanden,
     * wird eine Bestätigungsmeldung für die erfolgreiche Abmeldung angezeigt.
     *
     * @param error  optionaler Request-Parameter, der gesetzt ist, wenn die
     *               Anmeldung fehlgeschlagen ist; {@code null} wenn kein Fehler vorliegt
     * @param logout optionaler Request-Parameter, der gesetzt ist, wenn der
     *               Benutzer sich erfolgreich abgemeldet hat; {@code null} andernfalls
     * @param model  das {@link Model}-Objekt, in das Fehler- und Statusmeldungen
     *               sowie der Seitentitel eingetragen werden
     * @return der Name des Thymeleaf-Templates ({@code "login"})
     */
    @Operation(summary = "Login-Seite anzeigen")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login-Formular erfolgreich geladen")
    })
    @GetMapping("/login")
    public String login(
            @Parameter(description = "Gesetzt, wenn ein fehlgeschlagener Anmeldeversuch vorliegt") @RequestParam(value = "error", required = false) String error,
            @Parameter(description = "Gesetzt, wenn der Benutzer sich erfolgreich abgemeldet hat") @RequestParam(value = "logout", required = false) String logout,
            Model model) {
        if (error != null) {
            model.addAttribute("error", "Ungültiger Benutzername oder Passwort!");
        }
        if (logout != null) {
            model.addAttribute("message", "Sie wurden erfolgreich abgemeldet.");
        }
        model.addAttribute("pageTitle", "Login");
        return "login";
    }
}
