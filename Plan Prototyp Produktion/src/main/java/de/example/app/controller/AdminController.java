package de.example.app.controller;

import de.example.app.cms.repository.BlogPostRepository;
import de.example.app.crm.repository.NewsletterSubscriberRepository;
import de.example.app.security.repository.UserRepository;
import de.example.app.shop.repository.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Spring MVC Controller für den passwortgeschützten Admin-Bereich ({@code /admin}).
 * Stellt das Admin-Dashboard bereit und aggregiert Kennzahlen aus allen
 * Feature-Bereichen (CMS, Shop, CRM, Benutzerverwaltung) für Benutzer mit der
 * Rolle {@code ROLE_ADMIN}.
 */
@Tag(name = "Admin – Dashboard", description = "Admin-Dashboard mit aggregierten Kennzahlen für alle Bereiche der Plattform (nur für Administratoren)")
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final BlogPostRepository blogPostRepository;
    private final ProductRepository productRepository;
    private final NewsletterSubscriberRepository subscriberRepository;
    private final UserRepository userRepository;

    /**
     * Erstellt eine neue Instanz von {@link AdminController}.
     *
     * @param blogPostRepository    das Repository für Blog-Post-Statistiken
     * @param productRepository     das Repository für Produkt-Statistiken
     * @param subscriberRepository  das Repository für Newsletter-Abonnentenstatistiken
     * @param userRepository        das Repository für Benutzerstatistiken
     */
    public AdminController(BlogPostRepository blogPostRepository,
                           ProductRepository productRepository,
                           NewsletterSubscriberRepository subscriberRepository,
                           UserRepository userRepository) {
        this.blogPostRepository = blogPostRepository;
        this.productRepository = productRepository;
        this.subscriberRepository = subscriberRepository;
        this.userRepository = userRepository;
    }

    /**
     * Zeigt das Admin-Dashboard an und befüllt das Model mit aggregierten Kennzahlen.
     * Statistiken (Anzahl Blog-Posts, Produkte, Abonnenten, Benutzer) werden nur dann
     * geladen, wenn der aktuell angemeldete Benutzer die Rolle {@code ROLE_ADMIN} besitzt.
     *
     * @param model das {@link Model}-Objekt, in das die Dashboard-Kennzahlen eingetragen werden
     * @return der Name des Thymeleaf-Templates ({@code "admin/dashboard"})
     */
    @Operation(summary = "Admin-Dashboard anzeigen")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Dashboard mit Kennzahlen zu Blog-Posts, Produkten, Abonnenten und Benutzern erfolgreich geladen")
    })
    @GetMapping
    public String adminDashboard(Model model) {
        model.addAttribute("pageTitle", "Adminbereich");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String)) {
            model.addAttribute("username", auth.getName());
        }

        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            model.addAttribute("totalBlogPosts", blogPostRepository.count());
            model.addAttribute("publishedBlogPosts", blogPostRepository.countByPublishedTrue());
            model.addAttribute("totalProducts", productRepository.count());
            model.addAttribute("activeSubscribers", subscriberRepository.countByActiveTrue());
            model.addAttribute("totalSubscribers", subscriberRepository.count());
            model.addAttribute("totalUsers", userRepository.count());
        }

        return "admin/dashboard";
    }
}
