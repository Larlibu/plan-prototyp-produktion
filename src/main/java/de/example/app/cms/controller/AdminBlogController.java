package de.example.app.cms.controller;

import de.example.app.cms.dto.BlogPostCreateDto;
import de.example.app.cms.dto.BlogPostUpdateDto;
import de.example.app.cms.dto.BlogPostViewDto;
import de.example.app.cms.entity.BlogPost;
import de.example.app.cms.service.BlogPostService;
import de.example.app.feed.service.FeedService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Admin-Controller zur Verwaltung von Blogbeiträgen im geschützten Backend-Bereich.
 * Stellt Endpunkte zum Auflisten, Erstellen, Bearbeiten, Löschen sowie zum
 * Veröffentlichen und Zurückziehen von Blogbeiträgen bereit.
 * Zugriff ist auf Benutzer mit der Rolle {@code ADMIN} oder {@code EDITOR} beschränkt.
 * Nach jeder statusändernden Aktion wird der RSS-Feed über den {@link FeedService} synchronisiert.
 */
@Tag(name = "Admin – Blog", description = "Verwaltung von Blogbeiträgen im geschützten Admin-Bereich (Rollen: ADMIN, EDITOR)")
@Controller
@RequestMapping("/admin/blog")
@PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
public class AdminBlogController {

    private final BlogPostService blogPostService;
    private final FeedService feedService;

    /**
     * Erstellt eine neue Instanz des {@link AdminBlogController} mittels Constructor Injection.
     *
     * @param blogPostService Service für die Blogbeitrags-Geschäftslogik
     * @param feedService     Service zur Synchronisierung des RSS-Feeds
     */
    public AdminBlogController(BlogPostService blogPostService, FeedService feedService) {
        this.blogPostService = blogPostService;
        this.feedService = feedService;
    }

    /**
     * Zeigt die Admin-Übersichtsseite mit allen vorhandenen Blogbeiträgen an,
     * unabhängig von deren Veröffentlichungsstatus.
     *
     * @param model das {@link Model}-Objekt zur Übergabe der Beitragsliste an die View
     * @return Name des Thymeleaf-Templates für die Admin-Blog-Übersicht
     */
    @Operation(summary = "Alle Blogbeiträge in der Admin-Übersicht anzeigen (inkl. Entwürfe)")
    @GetMapping
    public String adminListBlogPosts(Model model) {
        List<BlogPostViewDto> blogPosts = blogPostService.getAllBlogPosts().stream()
                .map(BlogPostViewDto::fromEntity)
                .collect(Collectors.toList());
        model.addAttribute("blogPosts", blogPosts);
        return "admin/cms/blog-list-admin";
    }

    /**
     * Zeigt das leere Formular zum Erstellen eines neuen Blogbeitrags an.
     *
     * @param model das {@link Model}-Objekt, dem ein leeres {@link BlogPostCreateDto} und das Flag {@code isEdit = false} hinzugefügt werden
     * @return Name des Thymeleaf-Templates für das Erstell-/Bearbeitungsformular
     */
    @Operation(summary = "Formular zum Erstellen eines neuen Blogbeitrags anzeigen")
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("blogPost", new BlogPostCreateDto());
        model.addAttribute("isEdit", false);
        return "admin/cms/blog-create-edit";
    }

    /**
     * Verarbeitet das Erstellungsformular und legt einen neuen Blogbeitrag an.
     * Bei Validierungsfehlern wird das Formular erneut angezeigt.
     * Bei einem doppelten oder ungültigen Slug wird ein Feldfehler gesetzt.
     *
     * @param blogPostCreateDto  validiertes DTO mit den Formulardaten des neuen Beitrags
     * @param imageFile          optional hochgeladene Bilddatei; kann {@code null} sein
     * @param bindingResult      Ergebnis der Bean-Validierung
     * @param model              das {@link Model}-Objekt für die Fehleransicht
     * @param redirectAttributes Flash-Attribute für Erfolgs- oder Fehlermeldungen nach dem Redirect
     * @return Redirect auf die Admin-Blog-Übersicht bei Erfolg, sonst das Erstellungsformular
     */
    @Operation(summary = "Neuen Blogbeitrag erstellen und speichern")
    @ApiResponses({
        @ApiResponse(responseCode = "302", description = "Blogbeitrag erfolgreich erstellt – Weiterleitung zur Übersicht")
    })
    @PostMapping("/create")
    public String createBlogPost(@ModelAttribute("blogPost") @Valid BlogPostCreateDto blogPostCreateDto,
                                 @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "admin/cms/blog-create-edit";
        }
        try {
            BlogPost blogPost = new BlogPost();
            blogPost.setTitle(blogPostCreateDto.getTitle());
            blogPost.setSlug(blogPostCreateDto.getSlug());
            blogPost.setContent(blogPostCreateDto.getContent());

            blogPostService.createBlogPost(blogPost, imageFile);
            redirectAttributes.addFlashAttribute("message", "Blogbeitrag erfolgreich erstellt!");
            return "redirect:/admin/blog";
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("slug", "error.blogPost", "Slug existiert bereits oder ist ungültig.");
            model.addAttribute("isEdit", false);
            return "admin/cms/blog-create-edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Fehler beim Erstellen des Blogbeitrags: " + e.getMessage());
            return "redirect:/admin/blog/create";
        }
    }

    /**
     * Zeigt das vorausgefüllte Bearbeitungsformular für einen bestehenden Blogbeitrag an.
     * Wird der Beitrag nicht gefunden, wird die 404-Fehlerseite zurückgegeben.
     *
     * @param id    UUID des zu bearbeitenden Blogbeitrags
     * @param model das {@link Model}-Objekt, dem das befüllte {@link BlogPostUpdateDto} und das Flag {@code isEdit = true} hinzugefügt werden
     * @return Name des Thymeleaf-Templates für das Bearbeitungsformular, oder {@code "error/404"} wenn nicht gefunden
     */
    @Operation(summary = "Bearbeitungsformular für einen bestehenden Blogbeitrag anzeigen")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Formular mit den vorhandenen Daten angezeigt"),
        @ApiResponse(responseCode = "404", description = "Blogbeitrag mit der angegebenen ID nicht gefunden")
    })
    @GetMapping("/edit/{id}")
    public String showEditForm(
            @Parameter(description = "UUID des zu bearbeitenden Blogbeitrags") @PathVariable UUID id,
            Model model) {
        Optional<BlogPost> blogPost = blogPostService.getBlogPostById(id);
        if (blogPost.isEmpty()) {
            return "error/404";
        }
        BlogPostUpdateDto blogPostUpdateDto = new BlogPostUpdateDto();
        blogPostUpdateDto.setId(blogPost.get().getId());
        blogPostUpdateDto.setTitle(blogPost.get().getTitle());
        blogPostUpdateDto.setSlug(blogPost.get().getSlug());
        blogPostUpdateDto.setContent(blogPost.get().getContent());
        blogPostUpdateDto.setPublished(blogPost.get().isPublished());
        blogPostUpdateDto.setExistingImage(blogPost.get().getImage());

        model.addAttribute("blogPost", blogPostUpdateDto);
        model.addAttribute("isEdit", true);
        return "admin/cms/blog-create-edit";
    }

    /**
     * Verarbeitet das Bearbeitungsformular und aktualisiert den bestehenden Blogbeitrag.
     * Nach erfolgreichem Speichern wird der RSS-Feed synchronisiert: bei veröffentlichten
     * Beiträgen durch {@link FeedService#syncFromBlogPost}, bei zurückgezogenen durch
     * {@link FeedService#removeByBlogPostId}. Bei Validierungsfehlern wird das Formular erneut angezeigt.
     *
     * @param id                  UUID des zu aktualisierenden Blogbeitrags (aus dem Pfad)
     * @param blogPostUpdateDto   validiertes DTO mit den aktualisierten Formulardaten
     * @param imageFile           optional neu hochgeladene Bilddatei; kann {@code null} sein
     * @param bindingResult       Ergebnis der Bean-Validierung
     * @param model               das {@link Model}-Objekt für die Fehleransicht
     * @param redirectAttributes  Flash-Attribute für Erfolgs- oder Fehlermeldungen nach dem Redirect
     * @return Redirect auf die Admin-Blog-Übersicht bei Erfolg, sonst das Bearbeitungsformular
     */
    @Operation(summary = "Bestehenden Blogbeitrag aktualisieren und RSS-Feed synchronisieren")
    @ApiResponses({
        @ApiResponse(responseCode = "302", description = "Blogbeitrag erfolgreich aktualisiert – Weiterleitung zur Übersicht")
    })
    @PostMapping("/edit/{id}")
    public String updateBlogPost(
            @Parameter(description = "UUID des zu aktualisierenden Blogbeitrags") @PathVariable UUID id,
                                 @ModelAttribute("blogPost") @Valid BlogPostUpdateDto blogPostUpdateDto,
                                 @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "admin/cms/blog-create-edit";
        }
        try {
            BlogPost blogPost = new BlogPost();
            blogPost.setId(id);
            blogPost.setTitle(blogPostUpdateDto.getTitle());
            blogPost.setSlug(blogPostUpdateDto.getSlug());
            blogPost.setContent(blogPostUpdateDto.getContent());
            blogPost.setPublished(blogPostUpdateDto.isPublished());
            blogPost.setImage(blogPostUpdateDto.getExistingImage());

            blogPostService.updateBlogPost(id, blogPost, imageFile).ifPresent(updated -> {
                if (updated.isPublished()) {
                    feedService.syncFromBlogPost(updated);
                } else {
                    feedService.removeByBlogPostId(updated.getId());
                }
            });

            redirectAttributes.addFlashAttribute("message", "Blogbeitrag erfolgreich aktualisiert!");
            return "redirect:/admin/blog";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Fehler beim Aktualisieren des Blogbeitrags: " + e.getMessage());
            return "redirect:/admin/blog/edit/" + id;
        }
    }

    /**
     * Löscht einen Blogbeitrag und entfernt ihn zuvor aus dem RSS-Feed.
     * Setzt ein Flash-Attribut mit Erfolgs- oder Fehlermeldung.
     *
     * @param id                 UUID des zu löschenden Blogbeitrags
     * @param redirectAttributes Flash-Attribute für die Rückmeldung nach dem Redirect
     * @return Redirect auf die Admin-Blog-Übersicht
     */
    @Operation(summary = "Blogbeitrag löschen und aus dem RSS-Feed entfernen")
    @ApiResponse(responseCode = "302", description = "Weiterleitung zur Admin-Übersicht nach dem Löschversuch")
    @PostMapping("/delete/{id}")
    public String deleteBlogPost(
            @Parameter(description = "UUID des zu löschenden Blogbeitrags") @PathVariable UUID id,
            RedirectAttributes redirectAttributes) {
        feedService.removeByBlogPostId(id);
        if (blogPostService.deleteBlogPost(id)) {
            redirectAttributes.addFlashAttribute("message", "Blogbeitrag erfolgreich gelöscht!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Blogbeitrag nicht gefunden oder konnte nicht gelöscht werden.");
        }
        return "redirect:/admin/blog";
    }

    /**
     * Veröffentlicht einen Blogbeitrag und synchronisiert ihn in den RSS-Feed.
     * Setzt ein Flash-Attribut mit Erfolgs- oder Fehlermeldung.
     *
     * @param id                 UUID des zu veröffentlichenden Blogbeitrags
     * @param redirectAttributes Flash-Attribute für die Rückmeldung nach dem Redirect
     * @return Redirect auf die Admin-Blog-Übersicht
     */
    @Operation(summary = "Blogbeitrag veröffentlichen und in den RSS-Feed aufnehmen")
    @ApiResponse(responseCode = "302", description = "Weiterleitung zur Admin-Übersicht nach dem Veröffentlichungsversuch")
    @PostMapping("/publish/{id}")
    public String publishBlogPost(
            @Parameter(description = "UUID des zu veröffentlichenden Blogbeitrags") @PathVariable UUID id,
            RedirectAttributes redirectAttributes) {
        Optional<BlogPost> published = blogPostService.publishBlogPost(id);
        if (published.isPresent()) {
            feedService.syncFromBlogPost(published.get());
            redirectAttributes.addFlashAttribute("message", "Blogbeitrag erfolgreich veröffentlicht!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Blogbeitrag nicht gefunden oder konnte nicht veröffentlicht werden.");
        }
        return "redirect:/admin/blog";
    }

    /**
     * Zieht einen veröffentlichten Blogbeitrag zurück (setzt ihn auf Entwurf)
     * und entfernt ihn aus dem RSS-Feed.
     * Setzt ein Flash-Attribut mit Erfolgs- oder Fehlermeldung.
     *
     * @param id                 UUID des zurückzuziehenden Blogbeitrags
     * @param redirectAttributes Flash-Attribute für die Rückmeldung nach dem Redirect
     * @return Redirect auf die Admin-Blog-Übersicht
     */
    @Operation(summary = "Blogbeitrag auf Entwurf zurücksetzen und aus dem RSS-Feed entfernen")
    @ApiResponse(responseCode = "302", description = "Weiterleitung zur Admin-Übersicht nach dem Entveröffentlichungsversuch")
    @PostMapping("/unpublish/{id}")
    public String unpublishBlogPost(
            @Parameter(description = "UUID des zurückzuziehenden Blogbeitrags") @PathVariable UUID id,
            RedirectAttributes redirectAttributes) {
        if (blogPostService.unpublishBlogPost(id).isPresent()) {
            feedService.removeByBlogPostId(id);
            redirectAttributes.addFlashAttribute("message", "Blogbeitrag erfolgreich entveröffentlicht!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Blogbeitrag nicht gefunden oder konnte nicht entveröffentlicht werden.");
        }
        return "redirect:/admin/blog";
    }
}
