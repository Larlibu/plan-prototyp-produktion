package de.example.app.feed.service;

import com.rometools.rome.feed.synd.*;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedOutput;
import de.example.app.cms.entity.BlogPost;
import de.example.app.feed.entity.FeedItem;
import de.example.app.feed.repository.FeedItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service zur Verwaltung und Generierung von RSS 2.0- und Atom 1.0-Feeds.
 * Synchronisiert Feed-Einträge mit veröffentlichten Blog-Posts und serialisiert
 * den Feed mithilfe der ROME-Bibliothek in das gewünschte XML-Format.
 */
@Service
public class FeedService {

    private static final Logger log = LoggerFactory.getLogger(FeedService.class);
    private static final int DESCRIPTION_MAX_LENGTH = 500;

    private final FeedItemRepository feedItemRepository;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Erstellt eine neue Instanz von {@link FeedService}.
     *
     * @param feedItemRepository das Repository zum Persistieren und Abrufen von Feed-Einträgen
     */
    public FeedService(FeedItemRepository feedItemRepository) {
        this.feedItemRepository = feedItemRepository;
    }

    /**
     * Synchronisiert den Feed-Eintrag eines Blog-Posts mit der Datenbank.
     * Existiert bereits ein Eintrag für den Blog-Post, wird er aktualisiert;
     * andernfalls wird ein neuer Eintrag angelegt.
     *
     * @param blogPost der {@link BlogPost}, dessen Daten in den Feed-Eintrag übertragen werden sollen
     */
    public void syncFromBlogPost(BlogPost blogPost) {
        FeedItem feedItem = feedItemRepository.findByBlogPostId(blogPost.getId())
                .orElse(new FeedItem());

        String link = baseUrl + "/blog/" + blogPost.getSlug();

        feedItem.setTitle(blogPost.getTitle());
        feedItem.setDescription(extractPlainText(blogPost.getContent()));
        feedItem.setLink(link);
        feedItem.setPubDate(blogPost.getUpdatedAt());
        feedItem.setGuid(link);
        feedItem.setBlogPostId(blogPost.getId());

        feedItemRepository.save(feedItem);
        log.info("Feed-Eintrag synchronisiert für Blog-Post '{}'", blogPost.getTitle());
    }

    /**
     * Entfernt den Feed-Eintrag, der dem angegebenen Blog-Post zugeordnet ist.
     * Existiert kein passender Eintrag, wird die Methode ohne Aktion beendet.
     *
     * @param blogPostId die UUID des Blog-Posts, dessen Feed-Eintrag gelöscht werden soll
     */
    public void removeByBlogPostId(UUID blogPostId) {
        feedItemRepository.findByBlogPostId(blogPostId).ifPresent(item -> {
            feedItemRepository.delete(item);
            log.info("Feed-Eintrag entfernt für Blog-Post-ID {}", blogPostId);
        });
    }

    /**
     * Erstellt und serialisiert den RSS 2.0-Feed aller veröffentlichten Blog-Posts.
     *
     * @return den vollständigen RSS 2.0-Feed als XML-String
     * @throws FeedException wenn die Serialisierung durch ROME fehlschlägt
     */
    public String buildRssFeed() throws FeedException {
        return serialize(buildFeed("rss_2.0"));
    }

    /**
     * Erstellt und serialisiert den Atom 1.0-Feed aller veröffentlichten Blog-Posts.
     *
     * @return den vollständigen Atom 1.0-Feed als XML-String
     * @throws FeedException wenn die Serialisierung durch ROME fehlschlägt
     */
    public String buildAtomFeed() throws FeedException {
        return serialize(buildFeed("atom_1.0"));
    }

    /**
     * Erstellt ein {@link SyndFeed}-Objekt mit Metadaten und allen Feed-Einträgen
     * aus der Datenbank, sortiert nach Veröffentlichungsdatum absteigend.
     *
     * @param feedType der Feed-Typ gemäß ROME-Konvention (z. B. {@code "rss_2.0"} oder {@code "atom_1.0"})
     * @return das befüllte {@link SyndFeed}-Objekt
     */
    private SyndFeed buildFeed(String feedType) {
        SyndFeed feed = new SyndFeedImpl();
        feed.setFeedType(feedType);
        feed.setTitle("Plan Prototyp Produktion – Blog");
        feed.setLink(baseUrl + "/blog");
        feed.setDescription("Die neuesten Artikel aus unserem Blog");

        List<SyndEntry> entries = feedItemRepository.findAllByOrderByPubDateDesc()
                .stream()
                .map(this::toSyndEntry)
                .collect(Collectors.toList());
        feed.setEntries(entries);
        return feed;
    }

    /**
     * Konvertiert einen {@link FeedItem} in einen ROME {@link SyndEntry}.
     *
     * @param item der {@link FeedItem}, der in einen Feed-Eintrag umgewandelt werden soll
     * @return ein befülltes {@link SyndEntry}-Objekt für den ROME-Feed
     */
    private SyndEntry toSyndEntry(FeedItem item) {
        SyndEntry entry = new SyndEntryImpl();
        entry.setTitle(item.getTitle());
        entry.setLink(item.getLink());
        entry.setUri(item.getGuid());
        entry.setPublishedDate(
                Date.from(item.getPubDate().atZone(ZoneId.systemDefault()).toInstant()));

        SyndContent content = new SyndContentImpl();
        content.setType("text/plain");
        content.setValue(item.getDescription());
        entry.setDescription(content);

        return entry;
    }

    /**
     * Serialisiert ein {@link SyndFeed}-Objekt in einen XML-String.
     *
     * @param feed das zu serialisierende {@link SyndFeed}-Objekt
     * @return den Feed als XML-String
     * @throws FeedException wenn die Serialisierung durch ROME fehlschlägt
     */
    private String serialize(SyndFeed feed) throws FeedException {
        return new SyndFeedOutput().outputString(feed);
    }

    /**
     * Entfernt alle HTML-Tags aus dem übergebenen HTML-String und kürzt den
     * resultierenden Klartext auf maximal {@value #DESCRIPTION_MAX_LENGTH} Zeichen.
     *
     * @param html der HTML-Inhalt, aus dem Klartext extrahiert werden soll; darf {@code null} sein
     * @return den bereinigten und ggf. gekürzten Klartext; leerer String bei {@code null}-Eingabe
     */
    private String extractPlainText(String html) {
        if (html == null) return "";
        String stripped = html.replaceAll("<[^>]+>", "").replaceAll("\\s+", " ").trim();
        return stripped.length() > DESCRIPTION_MAX_LENGTH
                ? stripped.substring(0, DESCRIPTION_MAX_LENGTH) + "…"
                : stripped;
    }
}
