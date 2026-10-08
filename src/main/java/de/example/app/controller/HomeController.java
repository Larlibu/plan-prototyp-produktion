package de.example.app.controller;

import de.example.app.crm.service.NewsletterService;
import de.example.app.security.entity.User;
import de.example.app.security.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

/**
 * Spring MVC Controller für die öffentliche Startseite der Anwendung.
 * Lädt den Authentifizierungsstatus des aktuellen Benutzers sowie dessen
 * Newsletter-Abonnementstatus und stellt diese Informationen dem Thymeleaf-Template bereit.
 */
@Tag(name = "Startseite", description = "Öffentliche Startseite der gesunden Lebensmittel-Plattform")
@Controller
public class HomeController {

    private final NewsletterService newsletterService;
    private final UserRepository userRepository;

    /**
     * Erstellt eine neue Instanz von {@link HomeController}.
     *
     * @param newsletterService der Service zur Prüfung des Newsletter-Abonnementstatus
     * @param userRepository    das Repository zum Abrufen von Benutzerdaten
     */
    public HomeController(NewsletterService newsletterService, UserRepository userRepository) {
        this.newsletterService = newsletterService;
        this.userRepository = userRepository;
    }

    /**
     * Verarbeitet GET-Anfragen auf die Wurzel-URL ({@code /}) und zeigt die Startseite an.
     * Ist ein Benutzer eingeloggt, werden dessen Benutzername und Newsletter-Abonnementstatus
     * dem Model hinzugefügt, um eine personalisierte Begrüßung zu ermöglichen.
     *
     * @param model das {@link Model}-Objekt, in das View-Daten eingetragen werden
     * @return der Name des Thymeleaf-Templates ({@code "home"})
     */
    @Operation(summary = "Startseite anzeigen")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Startseite erfolgreich geladen")
    })
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("pageTitle", "Willkommen");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            String username = authentication.getName();
            model.addAttribute("username", username);

            Optional<User> currentUser = userRepository.findByUsernameIgnoreCase(username);
            currentUser.ifPresent(user -> {
                boolean isSubscribed = newsletterService.isUserSubscribed(user.getEmail());
                model.addAttribute("isNewsletterSubscribed", isSubscribed);
            });
        }

        return "home";
    }
}
