package de.example.app.crm.controller;

import de.example.app.crm.dto.NewsletterSendDto;
import de.example.app.crm.dto.NewsletterSubscriberDto;
import de.example.app.crm.service.MailerLiteService;
import de.example.app.crm.service.NewsletterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller zur Verwaltung von Newsletter-Abonnements im Admin-Bereich.
 * Behandelt die administrative Verwaltung von Abonnenten und den Newsletter-Versand.
 * Nutzt {@link NewsletterService} für die Geschäftslogik und {@link MailerLiteService} für den Versand.
 */
@Tag(name = "Admin – Newsletter", description = "Administrative Verwaltung von Newsletter-Abonnenten und Versand (nur für Administratoren)")
@Controller
@RequestMapping("/admin/newsletter")
@PreAuthorize("hasRole('ADMIN')")
public class AdminNewsletterController {

    private final NewsletterService newsletterService;
    private final MailerLiteService mailerLiteService;

    public AdminNewsletterController(NewsletterService newsletterService, MailerLiteService mailerLiteService) {
        this.newsletterService = newsletterService;
        this.mailerLiteService = mailerLiteService;
    }

    /**
     * Zeigt eine Liste aller Newsletter-Abonnenten für die Administration an.
     *
     * @param model Das {@link Model}-Objekt, um Daten an die View zu übergeben.
     * @return Der Name des Thymeleaf-Templates für die Admin-Abonnentenliste.
     */
    @Operation(summary = "Alle Newsletter-Abonnenten auflisten")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Abonnentenliste erfolgreich geladen")
    })
    @GetMapping
    public String adminListSubscribers(Model model) {
        List<NewsletterSubscriberDto> subscribers = newsletterService.getAllSubscribers().stream()
                .map(NewsletterSubscriberDto::fromEntity)
                .collect(Collectors.toList());
        model.addAttribute("subscribers", subscribers);
        return "admin/crm/subscriber-list-admin";
    }

    /**
     * Verarbeitet das Löschen eines Newsletter-Abonnenten.
     *
     * @param id Die ID des zu löschenden Abonnenten.
     * @param redirectAttributes Attribute für die Weiterleitung.
     * @return Eine Weiterleitung zur Admin-Abonnentenliste.
     */
    @Operation(summary = "Newsletter-Abonnenten löschen")
    @ApiResponses({
        @ApiResponse(responseCode = "302", description = "Weiterleitung zur Abonnentenliste nach dem Löschen")
    })
    @PostMapping("/delete/{id}")
    public String deleteSubscriber(
            @Parameter(description = "UUID des zu löschenden Abonnenten") @PathVariable UUID id,
            RedirectAttributes redirectAttributes) {
        if (newsletterService.deleteSubscriber(id)) {
            redirectAttributes.addFlashAttribute("message", "Abonnent erfolgreich gelöscht!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Abonnent nicht gefunden oder konnte nicht gelöscht werden.");
        }
        return "redirect:/admin/newsletter";
    }

    /**
     * Zeigt das Formular zum Verfassen eines Newsletters an.
     *
     * @param model Das {@link Model}-Objekt, um Daten an die View zu übergeben.
     * @return Der Name des Thymeleaf-Templates für das Newsletter-Verfassungsformular.
     */
    @Operation(summary = "Newsletter-Verfassungsformular anzeigen")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Formular zum Verfassen eines Newsletters erfolgreich geladen")
    })
    @GetMapping("/compose")
    public String showComposeForm(Model model) {
        model.addAttribute("newsletterSendDto", new NewsletterSendDto());
        model.addAttribute("activeCount", newsletterService.getAllActiveSubscribers().size());
        return "admin/crm/newsletter-compose";
    }

    /**
     * Verarbeitet den Versand eines Newsletters.
     *
     * @param dto Das DTO mit Betreff und HTML-Inhalt des Newsletters.
     * @param bindingResult Das Ergebnis der Validierung.
     * @param model Das {@link Model}-Objekt.
     * @param redirectAttributes Attribute für die Weiterleitung.
     * @return Eine Weiterleitung zur Admin-Abonnentenliste oder zurück zum Formular mit Fehlern.
     */
    @Operation(summary = "Newsletter an alle aktiven Abonnenten versenden")
    @ApiResponses({
        @ApiResponse(responseCode = "302", description = "Weiterleitung zur Abonnentenliste nach erfolgreichem Versand"),
        @ApiResponse(responseCode = "200", description = "Formular mit Validierungsfehlern erneut angezeigt")
    })
    @PostMapping("/compose")
    public String sendNewsletter(@Valid @ModelAttribute("newsletterSendDto") NewsletterSendDto dto,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeCount", newsletterService.getAllActiveSubscribers().size());
            return "admin/crm/newsletter-compose";
        }

        List<String> emails = newsletterService.getAllActiveSubscribers().stream()
                .map(s -> s.getEmail())
                .toList();

        if (emails.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Keine aktiven Abonnenten vorhanden.");
            return "redirect:/admin/newsletter/compose";
        }

        try {
            int count = mailerLiteService.sendNewsletter(dto.getSubject(), dto.getHtmlContent(), emails);
            redirectAttributes.addFlashAttribute("message",
                    "Newsletter erfolgreich an " + count + " Abonnenten versendet!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Fehler beim Versenden: " + e.getMessage());
        }

        return "redirect:/admin/newsletter";
    }
}