package de.example.app.crm.controller;

import de.example.app.crm.dto.NewsletterSubscriberDto;
import de.example.app.crm.service.NewsletterService;
import de.example.app.security.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller zur Verwaltung von Newsletter-Abonnements.
 * Behandelt Anfragen für die Newsletter-Anmeldung und die Verwaltung von Abonnenten.
 * Nutzt {@link NewsletterService} für die Geschäftslogik und {@link NewsletterSubscriberDto} für die Datenübertragung.
 */
@Tag(name = "Newsletter", description = "Öffentliche Endpunkte zur Anmeldung beim Newsletter der gesunden Lebensmittel-Plattform")
@Controller
@RequestMapping("/newsletter")
public class NewsletterController {

    private final NewsletterService newsletterService;
    private final UserRepository userRepository;

    public NewsletterController(NewsletterService newsletterService, UserRepository userRepository) {
        this.newsletterService = newsletterService;
        this.userRepository = userRepository;
    }

    /**
     * Zeigt das Formular für die Newsletter-Anmeldung an.
     * Prüft, ob der eingeloggte Benutzer bereits für den Newsletter registriert ist.
     *
     * @param model Das {@link Model}-Objekt, um Daten an die View zu übergeben.
     * @return Der Name des Thymeleaf-Templates für das Anmeldeformular.
     */
    @Operation(summary = "Newsletter-Anmeldeformular anzeigen")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Anmeldeformular erfolgreich geladen")
    })
    @GetMapping
    public String showSubscriptionForm(Model model) {
        model.addAttribute("newsletterSubscriberDto", new NewsletterSubscriberDto());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            String username = authentication.getName();
            userRepository.findByUsernameIgnoreCase(username).ifPresent(user -> {
                model.addAttribute("userEmail", user.getEmail());
                model.addAttribute("isSubscribed", newsletterService.isUserSubscribed(user.getEmail()));
            });
        } else {
            model.addAttribute("isSubscribed", false);
        }

        return "crm/newsletter-subscribe";
    }

    /**
     * Verarbeitet die Newsletter-Anmeldung.
     *
     * @param newsletterSubscriberDto Das DTO mit der E-Mail-Adresse des Abonnenten.
     * @param bindingResult Das Ergebnis der Validierung.
     * @param redirectAttributes Attribute für die Weiterleitung.
     * @return Eine Weiterleitung zur Newsletter-Seite mit einer Erfolgs- oder Fehlermeldung.
     */
    @Operation(summary = "Newsletter-Anmeldung verarbeiten")
    @ApiResponses({
        @ApiResponse(responseCode = "302", description = "Weiterleitung zur Newsletter-Seite nach erfolgreicher Anmeldung"),
        @ApiResponse(responseCode = "200", description = "Formular mit Validierungsfehlern erneut angezeigt")
    })
    @PostMapping("/subscribe")
    public String subscribe(@Valid NewsletterSubscriberDto newsletterSubscriberDto,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "crm/newsletter-subscribe";
        }
        try {
            newsletterService.subscribeNewsletter(newsletterSubscriberDto.getEmail());
            redirectAttributes.addFlashAttribute("message", "Vielen Dank für Ihre Anmeldung zum Newsletter!");
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("email", "error.newsletterSubscriberDto", e.getMessage());
            return "crm/newsletter-subscribe";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Fehler bei der Anmeldung: " + e.getMessage());
        }
        return "redirect:/newsletter";
    }

    /**
     * Meldet einen eingeloggten Benutzer für den Newsletter an.
     *
     * @param redirectAttributes Attribute für die Weiterleitung.
     * @return Eine Weiterleitung zur Startseite mit einer Erfolgs- oder Fehlermeldung.
     */
    @Operation(summary = "Eingeloggten Benutzer für Newsletter anmelden")
    @ApiResponses({
        @ApiResponse(responseCode = "302", description = "Weiterleitung zur Startseite nach erfolgreicher Anmeldung"),
        @ApiResponse(responseCode = "302", description = "Weiterleitung zur Login-Seite, wenn nicht angemeldet")
    })
    @GetMapping("/subscribe-user")
    @PreAuthorize("isAuthenticated()")
    public String subscribeLoggedInUser(RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            String username = authentication.getName();
            return userRepository.findByUsernameIgnoreCase(username)
                    .map(user -> {
                        try {
                            newsletterService.subscribeNewsletter(user.getEmail());
                            redirectAttributes.addFlashAttribute("message", "Vielen Dank für Ihre Anmeldung zum Newsletter!");
                        } catch (IllegalArgumentException e) {
                            redirectAttributes.addFlashAttribute("error", e.getMessage());
                        } catch (Exception e) {
                            redirectAttributes.addFlashAttribute("error", "Fehler bei der Anmeldung: " + e.getMessage());
                        }
                        return "redirect:/";
                    })
                    .orElseGet(() -> {
                        redirectAttributes.addFlashAttribute("error", "Benutzer nicht gefunden.");
                        return "redirect:/";
                    });
        }
        redirectAttributes.addFlashAttribute("error", "Sie müssen angemeldet sein, um sich anzumelden.");
        return "redirect:/login";
    }

}