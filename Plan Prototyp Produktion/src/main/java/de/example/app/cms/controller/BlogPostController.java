package de.example.app.cms.controller;

import de.example.app.cms.dto.BlogPostViewDto;
import de.example.app.cms.entity.BlogPost;
import de.example.app.cms.service.BlogPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller zur Verwaltung von Blogbeiträgen.
 * Behandelt Anfragen für öffentliche Blogansichten.
 * Nutzt {@link BlogPostService} für die Geschäftslogik und DTOs für die Datenübertragung.
 */
@Tag(name = "Blog", description = "Öffentliche Blogbeiträge der gesunden Lebensmittel Webplattform")
@Controller
@RequestMapping("/blog")
public class BlogPostController {

    private final BlogPostService blogPostService;

    public BlogPostController(BlogPostService blogPostService) {
        this.blogPostService = blogPostService;
    }

    /**
     * Zeigt eine Liste aller veröffentlichten Blogbeiträge an.
     * Für alle Benutzer zugänglich.
     *
     * @param model Das {@link Model}-Objekt, um Daten an die View zu übergeben.
     * @return Der Name des Thymeleaf-Templates für die Blog-Liste.
     */
    @Operation(summary = "Alle veröffentlichten Blogbeiträge anzeigen")
    @GetMapping
    public String listPublishedBlogPosts(Model model) {
        List<BlogPostViewDto> blogPosts = blogPostService.getPublishedBlogPosts().stream()
                .map(BlogPostViewDto::fromEntity)
                .collect(Collectors.toList());
        model.addAttribute("blogPosts", blogPosts);
        return "cms/blog-list";
    }

    /**
     * Zeigt einen einzelnen Blogbeitrag anhand seines Slugs an.
     * Für alle Benutzer zugänglich.
     *
     * @param slug Der Slug des Blogbeitrags.
     * @param model Das {@link Model}-Objekt, um Daten an die View zu übergeben.
     * @return Der Name des Thymeleaf-Templates für die Blogbeitragsdetails oder eine Fehlerseite.
     */
    @Operation(summary = "Einzelnen Blogbeitrag anhand des Slugs anzeigen")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Blogbeitrag gefunden und angezeigt"),
        @ApiResponse(responseCode = "404", description = "Blogbeitrag nicht gefunden oder nicht veröffentlicht")
    })
    @GetMapping("/{slug}")
    public String viewBlogPost(
            @Parameter(description = "URL-freundlicher Bezeichner des Blogbeitrags") @PathVariable String slug,
            Model model) {
        Optional<BlogPostViewDto> blogPost = blogPostService.getBlogPostBySlug(slug)
                .filter(BlogPost::isPublished)
                .map(BlogPostViewDto::fromEntity);

        if (blogPost.isEmpty()) {
            return "error/404";
        }
        model.addAttribute("blogPost", blogPost.get());
        return "cms/blog-detail";
    }
}