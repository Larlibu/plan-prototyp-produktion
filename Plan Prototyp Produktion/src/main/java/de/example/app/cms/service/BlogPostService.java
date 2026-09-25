package de.example.app.cms.service;

import de.example.app.cms.entity.BlogPost;
import de.example.app.cms.repository.BlogPostRepository;
import de.example.app.shared.util.ImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service zur Verwaltung von Blogbeiträgen.
 * Enthält die gesamte Geschäftslogik für das Erstellen, Aktualisieren, Löschen,
 * Veröffentlichen und Abrufen von Blogbeiträgen. Delegiert Datenbankoperationen
 * an das {@link BlogPostRepository} und die Bildverarbeitung an {@link ImageUtil}.
 */
@Service
public class BlogPostService {

    private static final Logger log = LoggerFactory.getLogger(BlogPostService.class);

    private final BlogPostRepository blogPostRepository;
    private final ImageUtil imageUtil;

    /**
     * Erstellt eine neue Instanz des {@link BlogPostService} mittels Constructor Injection.
     *
     * @param blogPostRepository Repository für den Datenbankzugriff auf Blogbeiträge
     * @param imageUtil          Hilfsobjekt zur Verarbeitung und Base64-Kodierung von Bildern
     */
    public BlogPostService(BlogPostRepository blogPostRepository, ImageUtil imageUtil) {
        this.blogPostRepository = blogPostRepository;
        this.imageUtil = imageUtil;
    }

    /**
     * Legt einen neuen Blogbeitrag an. Neue Beiträge werden standardmäßig als Entwurf
     * (nicht veröffentlicht) gespeichert. Wird ein Bild mitgeliefert, wird es verarbeitet
     * und als Base64-String in der Entität gespeichert.
     *
     * @param blogPost  das vorbereitete {@link BlogPost}-Objekt ohne ID
     * @param imageFile optionale Bilddatei, die dem Beitrag zugeordnet werden soll; kann {@code null} oder leer sein
     * @return der persistierte {@link BlogPost} mit generierter ID und Zeitstempeln
     * @throws IllegalArgumentException wenn das übergebene {@link BlogPost}-Objekt bereits eine ID besitzt
     */
    public BlogPost createBlogPost(BlogPost blogPost, MultipartFile imageFile) {
        if (blogPost.getId() != null) {
            throw new IllegalArgumentException("New blog post must not have an ID.");
        }
        if (imageFile != null && !imageFile.isEmpty()) {
            String base64Image = imageUtil.processAndEncodeImage(imageFile);
            blogPost.setImage(base64Image);
        }

        blogPost.setCreatedAt(LocalDateTime.now());
        blogPost.setUpdatedAt(LocalDateTime.now());
        blogPost.setPublished(false);
        log.info("Creating new blog post with title: {}", blogPost.getTitle());
        return blogPostRepository.save(blogPost);
    }

    /**
     * Aktualisiert einen bestehenden Blogbeitrag. Titel, Slug, Inhalt und
     * Veröffentlichungsstatus werden überschrieben. Wird eine neue Bilddatei
     * mitgeliefert, ersetzt sie das vorhandene Bild; ist {@code updatedBlogPost.getImage()}
     * explizit {@code null} und kein neues Bild vorhanden, wird das Bild entfernt.
     *
     * @param id              UUID des zu aktualisierenden Blogbeitrags
     * @param updatedBlogPost {@link BlogPost}-Objekt mit den neuen Feldinhalten
     * @param imageFile       optionale neue Bilddatei; kann {@code null} oder leer sein
     * @return ein {@link Optional} mit dem aktualisierten {@link BlogPost}, oder ein leeres {@link Optional} wenn kein Beitrag mit dieser ID gefunden wurde
     */
    public Optional<BlogPost> updateBlogPost(UUID id, BlogPost updatedBlogPost, MultipartFile imageFile) {
        return blogPostRepository.findById(id).map(existingPost -> {
            existingPost.setTitle(updatedBlogPost.getTitle());
            existingPost.setSlug(updatedBlogPost.getSlug());
            existingPost.setContent(updatedBlogPost.getContent());
            existingPost.setPublished(updatedBlogPost.isPublished());
            existingPost.setUpdatedAt(LocalDateTime.now());

            if (imageFile != null && !imageFile.isEmpty()) {
                String base64Image = imageUtil.processAndEncodeImage(imageFile);
                existingPost.setImage(base64Image);
            } else if (updatedBlogPost.getImage() == null) {
                existingPost.setImage(null);
            }

            log.info("Updating blog post with ID: {}", id);
            return blogPostRepository.save(existingPost);
        });
    }

    /**
     * Löscht einen Blogbeitrag anhand seiner UUID aus der Datenbank.
     *
     * @param id UUID des zu löschenden Blogbeitrags
     * @return {@code true} wenn der Beitrag gefunden und gelöscht wurde, {@code false} wenn kein Beitrag mit dieser ID existiert
     */
    public boolean deleteBlogPost(UUID id) {
        if (!blogPostRepository.existsById(id)) {
            return false;
        }
        blogPostRepository.deleteById(id);
        log.info("Deleted blog post with ID: {}", id);
        return true;
    }

    /**
     * Setzt den Veröffentlichungsstatus eines Blogbeitrags auf {@code true}
     * und aktualisiert den Änderungszeitstempel.
     *
     * @param id UUID des zu veröffentlichenden Blogbeitrags
     * @return ein {@link Optional} mit dem aktualisierten {@link BlogPost}, oder ein leeres {@link Optional} wenn kein Beitrag mit dieser ID gefunden wurde
     */
    public Optional<BlogPost> publishBlogPost(UUID id) {
        return blogPostRepository.findById(id).map(post -> {
            post.setPublished(true);
            post.setUpdatedAt(LocalDateTime.now());
            log.info("Publishing blog post with ID: {}", id);
            return blogPostRepository.save(post);
        });
    }

    /**
     * Setzt den Veröffentlichungsstatus eines Blogbeitrags auf {@code false}
     * (zurück in den Entwurfsmodus) und aktualisiert den Änderungszeitstempel.
     *
     * @param id UUID des zurückzuziehenden Blogbeitrags
     * @return ein {@link Optional} mit dem aktualisierten {@link BlogPost}, oder ein leeres {@link Optional} wenn kein Beitrag mit dieser ID gefunden wurde
     */
    public Optional<BlogPost> unpublishBlogPost(UUID id) {
        return blogPostRepository.findById(id).map(post -> {
            post.setPublished(false);
            post.setUpdatedAt(LocalDateTime.now());
            log.info("Unpublishing blog post with ID: {}", id);
            return blogPostRepository.save(post);
        });
    }

    /**
     * Sucht einen Blogbeitrag anhand seiner UUID.
     *
     * @param id UUID des gesuchten Blogbeitrags
     * @return ein {@link Optional} mit dem gefundenen {@link BlogPost}, oder ein leeres {@link Optional} wenn kein Beitrag mit dieser ID existiert
     */
    public Optional<BlogPost> getBlogPostById(UUID id) {
        return blogPostRepository.findById(id);
    }

    /**
     * Sucht einen Blogbeitrag anhand seines URL-freundlichen Slugs.
     *
     * @param slug der eindeutige Slug des gesuchten Blogbeitrags (z.B. {@code "mein-erster-blog"})
     * @return ein {@link Optional} mit dem gefundenen {@link BlogPost}, oder ein leeres {@link Optional} wenn kein Beitrag mit diesem Slug existiert
     */
    public Optional<BlogPost> getBlogPostBySlug(String slug) {
        return blogPostRepository.findBySlug(slug);
    }

    /**
     * Liefert alle in der Datenbank vorhandenen Blogbeiträge, unabhängig vom Veröffentlichungsstatus.
     * Wird primär im Admin-Bereich genutzt.
     *
     * @return unveränderliche Liste aller {@link BlogPost}-Objekte
     */
    public List<BlogPost> getAllBlogPosts() {
        return blogPostRepository.findAll();
    }

    /**
     * Liefert alle Blogbeiträge, die als veröffentlicht markiert sind.
     * Wird für die öffentliche Blog-Übersicht verwendet.
     *
     * @return Liste aller {@link BlogPost}-Objekte mit {@code published == true}
     */
    public List<BlogPost> getPublishedBlogPosts() {
        return blogPostRepository.findByPublishedTrue();
    }
}
