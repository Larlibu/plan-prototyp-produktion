package de.example.app.feed.controller;

import com.rometools.rome.io.FeedException;
import de.example.app.feed.service.FeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * HTTP-Controller, der die öffentlich zugänglichen Feed-Endpunkte bereitstellt.
 * Liefert den Blog-Feed im RSS 2.0- und Atom 1.0-Format unter {@code /feed/rss}
 * bzw. {@code /feed/atom} aus.
 */
@Tag(name = "Feed", description = "Öffentlich zugängliche Blog-Feeds der gesunden Lebensmittel-Plattform im RSS 2.0- und Atom 1.0-Format")
@Controller
@RequestMapping("/feed")
public class FeedController {

    private final FeedService feedService;

    /**
     * Erstellt eine neue Instanz von {@link FeedController}.
     *
     * @param feedService der Service zum Aufbau und zur Serialisierung der Feeds
     */
    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    /**
     * Liefert den Blog-Feed im RSS 2.0-Format aus.
     * Bei einem Fehler während der Feed-Generierung wird HTTP 500 zurückgegeben.
     *
     * @return eine {@link ResponseEntity} mit dem RSS 2.0-XML als Body und dem
     *         Content-Type {@code application/rss+xml;charset=UTF-8}, oder HTTP 500 bei Fehler
     */
    @Operation(summary = "Blog-Feed im RSS 2.0-Format abrufen")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "RSS 2.0-Feed erfolgreich generiert (Content-Type: application/rss+xml;charset=UTF-8)"),
        @ApiResponse(responseCode = "500", description = "Fehler bei der Feed-Generierung (FeedException)")
    })
    @GetMapping(value = "/rss", produces = "application/rss+xml;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<String> rss() {
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/rss+xml;charset=UTF-8"))
                    .body(feedService.buildRssFeed());
        } catch (FeedException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Liefert den Blog-Feed im Atom 1.0-Format aus.
     * Bei einem Fehler während der Feed-Generierung wird HTTP 500 zurückgegeben.
     *
     * @return eine {@link ResponseEntity} mit dem Atom 1.0-XML als Body und dem
     *         Content-Type {@code application/atom+xml;charset=UTF-8}, oder HTTP 500 bei Fehler
     */
    @Operation(summary = "Blog-Feed im Atom 1.0-Format abrufen")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Atom 1.0-Feed erfolgreich generiert (Content-Type: application/atom+xml;charset=UTF-8)"),
        @ApiResponse(responseCode = "500", description = "Fehler bei der Feed-Generierung (FeedException)")
    })
    @GetMapping(value = "/atom", produces = "application/atom+xml;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<String> atom() {
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/atom+xml;charset=UTF-8"))
                    .body(feedService.buildAtomFeed());
        } catch (FeedException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
