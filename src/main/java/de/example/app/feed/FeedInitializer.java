package de.example.app.feed;

import de.example.app.cms.entity.BlogPost;
import de.example.app.cms.service.BlogPostService;
import de.example.app.feed.service.FeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Initialisierungskomponente, die beim Anwendungsstart alle bereits veröffentlichten
 * Blog-Posts in die Feed-Datenbank synchronisiert.
 * Stellt sicher, dass der RSS- und Atom-Feed nach einem Neustart konsistent ist
 * und keine veröffentlichten Posts fehlen.
 */
@Component
public class FeedInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FeedInitializer.class);

    private final BlogPostService blogPostService;
    private final FeedService feedService;

    /**
     * Erstellt eine neue Instanz von {@link FeedInitializer}.
     *
     * @param blogPostService der Service zum Abrufen aller veröffentlichten Blog-Posts
     * @param feedService     der Service zum Synchronisieren der Feed-Einträge
     */
    public FeedInitializer(BlogPostService blogPostService, FeedService feedService) {
        this.blogPostService = blogPostService;
        this.feedService = feedService;
    }

    /**
     * Wird beim Anwendungsstart automatisch ausgeführt und synchronisiert alle
     * veröffentlichten Blog-Posts mit den Feed-Einträgen in der Datenbank.
     * Sind keine veröffentlichten Posts vorhanden, wird die Methode ohne Aktion beendet.
     *
     * @param args die Startargumente der Anwendung (werden nicht verwendet)
     */
    @Override
    public void run(ApplicationArguments args) {
        List<BlogPost> published = blogPostService.getPublishedBlogPosts();
        if (published.isEmpty()) {
            return;
        }
        log.info("Feed-Sync: {} veröffentlichte Blog-Posts werden synchronisiert.", published.size());
        published.forEach(feedService::syncFromBlogPost);
        log.info("Feed-Sync abgeschlossen.");
    }
}
