### Dokumentation der vorgenommenen Änderungen (2026-06-10)

**Problemstellung:**
Auf Admin-Pfaden waren Blog-, Shop-, Newsletter-Links und das Warenkorb-Symbol sichtbar, obwohl dies nicht dem Admin-Kontext entspricht.

---

#### 1. Ausblenden öffentlicher Navigationslinks auf Admin-Pfaden

**Problemstellung:**
Auf Admin-Pfaden waren Blog-, Shop-, Newsletter-Links und das Warenkorb-Symbol sichtbar, obwohl dies nicht dem Admin-Kontext entspricht und für nicht eingeloggte Benutzer auf Admin-Seiten verwirrend sein kann.

**Ziel:** Die Navigationslinks für Blog, Shop, Newsletter und das Warenkorb-Symbol ausblenden, wenn sich der Benutzer auf einem Admin-Pfad befindet, unabhängig vom Login-Status.

**Maßnahmen:**

*   **Anpassung von `de.example.app.shared.web.GlobalControllerAdvice.java`:**
    *   Eine neue `@ModelAttribute("isAdminPath")`-Methode wurde hinzugefügt, die den aktuellen Request-URI prüft und `true` zurückgibt, wenn der Pfad mit `/admin` beginnt.
*   **Anpassung von `src/main/resources/templates/fragments/header.html`:**
    *   Die `sec:authorize`-Bedingungen für die `<li>`-Elemente der Blog-, Shop-, Newsletter-Links und des Warenkorb-Symbols wurden erweitert zu `sec:authorize="!hasRole('ADMIN') and !${isAdminPath}"`.

**Auswirkung:**
Die öffentlichen Navigationslinks und das Warenkorb-Symbol werden nun ausgeblendet, wenn sich der Benutzer auf einem Admin-Pfad befindet. Dies sorgt für eine klarere Trennung zwischen dem öffentlichen und dem administrativen Bereich der Anwendung und verbessert die Benutzerführung.

---

**Verständnisfragen / Anmerkungen:**
*   Die Implementierung über `GlobalControllerAdvice` ermöglicht eine zentrale und effiziente Prüfung des Admin-Pfades.
*   Die Kombination von `!hasRole('ADMIN')` und `!${isAdminPath}` in der `sec:authorize`-Direktive stellt sicher, dass die Elemente nur für Nicht-Admins außerhalb des Admin-Bereichs sichtbar sind.

---

#### 2. Warenkorb wird nur noch bei Bedarf erstellt

**Problemstellung:**
Bei jedem HTTP-Request – auch beim bloßen Aufrufen der Startseite ohne Anmeldung – wurde automatisch ein leerer Warenkorb in der Session angelegt. Das Debug-Log `"Neuer Warenkorb erstellt und in der Session gespeichert."` erschien bei jedem Seitenaufruf, obwohl der Benutzer noch kein Produkt hinzugefügt hatte.

**Ursache:**
Der `GlobalControllerAdvice` ruft bei jedem Request `shoppingCartService.getCart(session)` auf, um das `cart`-Attribut für alle Views bereitzustellen. Die Methode `getCart()` erstellte bisher immer einen neuen leeren Warenkorb, wenn noch keiner in der Session vorhanden war.

**Ziel:** Den Warenkorb erst dann in der Session anlegen, wenn tatsächlich ein Produkt hinzugefügt wird.

**Maßnahmen:**

*   **Anpassung von `de.example.app.shop.service.ShoppingCartService.java`:**
    *   Die Methode `getCart()` wurde geändert: Sie liest nun nur noch den Warenkorb aus der Session und gibt `null` zurück, wenn keiner vorhanden ist – ohne einen neuen anzulegen.
    *   Eine neue private Hilfsmethode `getOrCreateCart()` wurde eingeführt, die das bisherige Verhalten (Lesen oder Erstellen) kapselt.
    *   Die Methoden `addItemToCart()`, `updateItemQuantity()`, `removeItemFromCart()` und `clearCart()` verwenden intern nun `getOrCreateCart()` statt `getCart()`.

**Auswirkung:**
Ein Warenkorb-Objekt wird nur noch dann in der HTTP-Session erstellt, wenn ein Benutzer erstmals ein Produkt in den Warenkorb legt. Reine Seitenaufrufe (z. B. Startseite, Produktliste) erzeugen keine unnötigen Session-Einträge mehr. Das Thymeleaf-Template im Header prüfte `cart != null` bereits korrekt und war keine Anpassung nötig.

---

#### 3. Layout-Verbesserung: CSS und fehlende Shop-Templates

**Problemstellung:**
Das visuelle Layout war unpoliert. Mehrere CSS-Klassen fehlten (`.badge`, `.button.tertiary`, `.dashboard-grid`, `.dashboard-card`, `.blog-detail-image`, `.blog-card-date`), die Schriftart `Roboto` wurde referenziert aber nie geladen, und die Shop-Templates `product-list.html` sowie `product-detail.html` waren leer (0 Bytes).

**Ziel:** Klareres, konsistenteres Design bei unveränderter HTML-Struktur; fehlende CSS-Klassen ergänzen; leere Shop-Templates implementieren.

**Maßnahmen:**

*   **`src/main/resources/static/css/style.css` vollständig überarbeitet:**
    *   Google Fonts Import (`Inter`) hinzugefügt und als primäre Schriftart gesetzt.
    *   Farbpalette verfeinert: weicheres Violett (`#7c3aed`), klareres Cyan (`#06b6d4`), dunklerer Hintergrund (`#0f0f0f`).
    *   CSS Custom Properties um `--shadow-sm`, `--shadow`, `--shadow-lg`, `--radius-lg`, `--transition`, `--surface-2` erweitert.
    *   Navbar sticky, mit Farbverlauf-Branding und kompakterer Höhe (64px).
    *   Fehlende Klassen ergänzt: `.badge` (Pill-Badge für Warenkorb-Zähler), `.button.tertiary` (Ghost-Button), `.dashboard-grid` + `.dashboard-card` (Admin-Dashboard-Kacheln), `.blog-detail-image` (Alias für Detailseiten-Bild), `.blog-card-date` (gedämpfte Datumsanzeige).
    *   Hover-Effekte auf Karten (`blog-card`, `product-card`, `feature-card`, `dashboard-card`): sanftes Anheben (`translateY(-4px)`) mit tieferem Schatten und Akzent-Randfarbe.
    *   Tabellenköpfe von hartem Lila auf dezentes `rgba`-Lila umgestellt.
    *   Fokus-Zustand für Formularfelder mit `box-shadow`-Ring verbessert.
    *   Zeilenhervorhebung in Tabellen bei Hover.

*   **`src/main/resources/templates/shop/product-list.html` neu erstellt:**
    *   Zeigt alle Produkte in einem responsiven `.product-grid`.
    *   Jede Karte zeigt Bild, Name (verlinkt auf Detailseite), Preis, Lagerbestand und einen „Details ansehen"-Button.
    *   Flash-Meldungen (`message`/`error`) werden oben angezeigt.

*   **`src/main/resources/templates/shop/product-detail.html` neu erstellt:**
    *   Zweispaltiges Layout (Bild links, Infos rechts) über `.product-detail-grid`.
    *   Zeigt Name, Preis, Lagerbestand und Beschreibung.
    *   Formular zum In-den-Warenkorb-Legen (`POST /shop/cart/add`) mit Mengenfeld, nur sichtbar wenn `stock > 0`.

**Auswirkung:**
Das gesamte Frontend erhält ein konsistentes, modernes Erscheinungsbild. Fehlende CSS-Klassen verursachen keine unsichtbaren Elemente mehr. Der Shop ist nun vollständig navigierbar (Liste → Detail → Warenkorb).

---

#### 4. Fehlende Templates: Warenkorb und Newsletter

**Problemstellung:**
Der Aufruf von `/shop/cart` und `/newsletter` führte zu Template-Fehlern, da `shop/cart.html` vollständig fehlte und das Verzeichnis `crm/` mit `newsletter-subscribe.html` noch nicht existierte.

**Maßnahmen:**

*   **`src/main/resources/templates/shop/cart.html` neu erstellt:**
    *   Leerer Zustand (`cart == null or cart.items.isEmpty()`) mit Link zurück zum Shop.
    *   Bei befülltem Warenkorb: Tabelle mit Bild, Produktname, Einzelpreis, Mengeneingabe (aktualisierbar via `POST /shop/cart/update`), Zeilengesamtpreis und Entfernen-Button (`POST /shop/cart/remove`).
    *   Zusammenfassung mit Gesamtbetrag, „Warenkorb leeren"- und „Weiter einkaufen"-Button.

*   **`src/main/resources/templates/crm/newsletter-subscribe.html` neu erstellt:**
    *   Anmeldeformular mit E-Mail-Validierung (`POST /newsletter/subscribe`).
    *   Bei eingeloggtem, bereits abonnierten Benutzer: Statusmeldung statt Formular.
    *   Flash-Meldungen werden oben angezeigt.

**Auswirkung:**
Warenkorb und Newsletter-Seite sind ohne Template-Fehler aufrufbar.

---

#### 5. Newsletter-Synchronisation mit MailerLite

**Problemstellung:**
Neue Abonnenten wurden nur lokal in der DB gespeichert, aber nicht an MailerLite übertragen. Umgekehrt wurde der lokale DB-Eintrag nicht bereinigt, wenn ein Nutzer sich über den Abmelde-Link in einer MailerLite-E-Mail abgemeldet hatte.

**Ursache:**
`NewsletterService.subscribeNewsletter()` rief `MailerLiteService` nicht auf. In `MailerLiteService.syncSubscribers()` wurde bei einem 422-Fehler mit `"unsubscribed"` der lokale Datensatz absichtlich nicht angefasst.

**Maßnahmen:**

*   **`de.example.app.crm.service.MailerLiteService`:**
    *   Neue öffentliche Methode `addSubscriber(String email)` hinzugefügt, die einen einzelnen Abonnenten per `POST /subscribers` in MailerLite einträgt.
    *   In `syncSubscribers()`: Bei 422 `"unsubscribed"` wird der lokale DB-Eintrag nun vollständig gelöscht (`newsletterSubscriberRepository.delete()`).

*   **`de.example.app.crm.service.NewsletterService`:**
    *   `MailerLiteService` per Constructor Injection hinzugefügt.
    *   In `subscribeNewsletter()`: Nach dem lokalen Speichern wird `mailerLiteService.addSubscriber(email)` aufgerufen.

**Auswirkung:**
Jeder neue Abonnent wird sofort in MailerLite eingetragen. Meldet sich ein Nutzer über den MailerLite-Abmelde-Link ab, wird der lokale DB-Eintrag beim nächsten Newsletter-Versand automatisch gelöscht.

---

#### 6. Abmeldung ausschließlich über MailerLite

**Problemstellung:**
Die Anwendung bot eigene Abmeldeformulare und -endpunkte an, obwohl die Abmeldung ausschließlich über den Abmelde-Link in MailerLite-E-Mails erfolgen soll.

**Maßnahmen:**

*   **`de.example.app.crm.controller.NewsletterController`:**
    *   Endpunkt `POST /newsletter/unsubscribe` entfernt.
    *   Endpunkt `GET /newsletter/unsubscribe-user` entfernt.
    *   Nicht mehr benötigte Felder und Imports bereinigt.

*   **`src/main/resources/templates/crm/newsletter-subscribe.html`:**
    *   Abmeldeformular entfernt; bei bereits abonnierten Nutzern erscheint ein Hinweis auf den MailerLite-Abmelde-Link.

*   **`src/main/resources/templates/home.html`:**
    *   „Abmelden"-Link aus dem Newsletter-Status entfernt; nur noch der Abonnement-Status wird angezeigt.

**Auswirkung:**
Die Abmeldung läuft ausschließlich über MailerLite. Inkonsistenzen durch parallele Abmeldewege sind ausgeschlossen.

---

#### 7. RSS/Atom-Newsfeed mit ROME

**Problemstellung:**
Die Anwendung hatte keinen maschinenlesbaren Feed. Blog-Artikel waren ausschließlich über die Web-Oberfläche erreichbar.

**Ziel:** RSS 2.0- und Atom 1.0-Feeds für veröffentlichte Blog-Artikel bereitstellen. Feed-Einträge werden lokal in der DB persistiert und bei jeder Statusänderung eines Blog-Posts synchronisiert.

**Maßnahmen:**

*   **`pom.xml`:**
    *   Abhängigkeit `com.rometools:rome:2.1.0` hinzugefügt.

*   **`src/main/resources/db/migration/V7__Create_Feed_Items.sql`:**
    *   Neue Tabelle `feed_items` mit Spalten `id`, `title`, `description`, `link`, `pub_date`, `guid` (UNIQUE) und `blog_post_id` (UNIQUE).

*   **`de.example.app.feed.entity.FeedItem`:**
    *   JPA-Entity für die Tabelle `feed_items`. Speichert Titel, Klartext-Beschreibung (max. 500 Zeichen, HTML-Tags entfernt), Link, Veröffentlichungsdatum, GUID und Referenz auf den Blog-Post.

*   **`de.example.app.feed.dto.FeedItemDto`:**
    *   DTO mit statischer Fabrikmethode `fromEntity()`.

*   **`de.example.app.feed.repository.FeedItemRepository`:**
    *   `findByBlogPostId(UUID)` – sucht Eintrag zur Blog-Post-ID.
    *   `findAllByOrderByPubDateDesc()` – alle Einträge absteigend nach Datum.

*   **`de.example.app.feed.service.FeedService`:**
    *   `syncFromBlogPost(BlogPost)` – legt Feed-Eintrag an oder aktualisiert ihn.
    *   `removeByBlogPostId(UUID)` – löscht den Feed-Eintrag.
    *   `buildRssFeed()` / `buildAtomFeed()` – erzeugt ROME-`SyndFeed` und serialisiert ihn als XML-String.
    *   Basis-URL konfigurierbar über `app.base-url` (Standard: `http://localhost:8080`).

*   **`de.example.app.feed.controller.FeedController`:**
    *   `GET /feed/rss` → `application/rss+xml` (RSS 2.0).
    *   `GET /feed/atom` → `application/atom+xml` (Atom 1.0).

*   **`de.example.app.cms.controller.AdminBlogController`:**
    *   `FeedService` per Constructor Injection hinzugefügt.
    *   `publishBlogPost`: ruft `feedService.syncFromBlogPost()` auf.
    *   `updateBlogPost`: synchronisiert oder entfernt Feed-Eintrag je nach `published`-Status.
    *   `unpublishBlogPost`: ruft `feedService.removeByBlogPostId()` auf.
    *   `deleteBlogPost`: ruft `feedService.removeByBlogPostId()` vor dem Löschen auf.

*   **`de.example.app.security.config.SecurityConfig`:**
    *   `/feed/**` zu den öffentlich zugänglichen Pfaden hinzugefügt.

*   **`src/main/resources/application.yml`:**
    *   Neue Property `app.base-url: http://localhost:8080` für die Link-Generierung im Feed.

**Auswirkung:**
Veröffentlichte Blog-Artikel sind unter `/feed/rss` und `/feed/atom` als standardkonformer Feed abrufbar. Die Feed-Datenbank bleibt automatisch konsistent: Veröffentlichen legt einen Eintrag an, Entveröffentlichen und Löschen entfernen ihn. Feed-Einträge enthalten einen HTML-freien Textauszug (max. 500 Zeichen) als Beschreibung.

---

#### 8. RSS-/Atom-Feed-Abonnement in der UI

**Problemstellung:**
Obwohl die Feed-Endpunkte unter `/feed/rss` und `/feed/atom` erreichbar waren, gab es keine Möglichkeit für den Nutzer, den Feed zu entdecken oder zu abonnieren.

**Ziel:** Den Feed-Abonnement-Prozess so implementieren, wie er üblicherweise im Web vorgesehen ist – d. h. durch Browser-Auto-Discovery und sichtbare Links auf der Blog-Seite. Eine Anmeldung ist nicht erforderlich.

**Maßnahmen:**

*   **`src/main/resources/templates/layout/base.html`:**
    *   Zwei `<link rel="alternate">`-Tags im `<head>` hinzugefügt – einen für RSS und einen für Atom. Dies ermöglicht Browsern und Feed-Readern, die Feeds automatisch zu entdecken (Auto-Discovery).

*   **`src/main/resources/templates/cms/blog-list.html`:**
    *   Überschrift in einen Flex-Container (`.blog-list-header`) eingebettet.
    *   Zwei sichtbare Abonnement-Links (`.feed-link`) neben der Überschrift hinzugefügt: ein orangefarbener RSS-Button mit dem SVG-Standard-RSS-Symbol und ein Atom-Link als Ghost-Variante.

*   **`src/main/resources/static/css/style.css`:**
    *   Neuer Abschnitt `Feed / RSS` mit Styles für `.blog-list-header`, `.feed-links`, `.feed-link` und `.feed-link--atom` hinzugefügt.
    *   RSS-Button in standardkonformem Orange (`#e65c00`) mit weißer Schrift; Atom-Variante als transparenter Outline-Button.

**Auswirkung:**
Browser und Feed-Reader können den Feed via Auto-Discovery erkennen. Nutzer sehen auf der Blog-Übersichtsseite direkt die Abonnement-Buttons – keine Anmeldung nötig. Das Design folgt der Web-Konvention (RSS-Orange).

---

#### 9. Feed-Initialisierung beim App-Start

**Problemstellung:**
Nach der Implementierung der Feed-Infrastruktur (Abschnitt 7) war die `feed_items`-Tabelle leer, obwohl bereits veröffentlichte Blog-Posts in der Datenbank existierten. Die Sync-Logik im `AdminBlogController` greift nur bei zukünftigen Admin-Aktionen – bestehende Posts wurden nie synchronisiert. Der Feed lieferte daher ein valides, aber leeres XML-Dokument.

**Maßnahmen:**

*   **`de.example.app.feed.FeedInitializer`** (neu erstellt):
    *   Implementiert `ApplicationRunner` – wird einmalig nach dem vollständigen Start des Spring-Kontexts ausgeführt.
    *   Lädt alle veröffentlichten Blog-Posts via `blogPostService.getPublishedBlogPosts()`.
    *   Ruft für jeden Post `feedService.syncFromBlogPost()` auf (Upsert – keine Duplikate bei Mehrfachstart).
    *   Loggt Anzahl der synchronisierten Posts auf INFO-Level.

**Auswirkung:**
Nach jedem App-Start ist die `feed_items`-Tabelle automatisch mit allen aktuell veröffentlichten Blog-Posts befüllt. Der Feed liefert sofort Einträge, ohne dass ein Admin-Eingriff nötig ist. Der Initializer ist idempotent und kann beliebig oft ausgeführt werden.

---

#### 10. Vollständige Javadoc-Dokumentation

**Problemstellung:**
Das Projekt hatte keinerlei Javadoc-Kommentare. Alle 54 Klassen und 118 öffentlichen Methoden waren undokumentiert, was die Wartbarkeit und Einarbeitung neuer Entwickler erschwert.

**Maßnahmen:**

Alle 53 Java-Dateien wurden mit deutschsprachigem Javadoc versehen:

*   **Klassen und Interfaces:** Beschreibung der fachlichen Verantwortung, Abhängigkeiten und Besonderheiten (z. B. Transaktionsverhalten, Sicherheitsrollen).
*   **Methoden:** Beschreibung des Verhaltens, `@param` für jeden Parameter, `@return` bei Rückgabewerten, `@throws` bei geworfenen Exceptions.
*   **Konstruktoren:** `@param` für alle injizierten Abhängigkeiten (Constructor Injection).
*   **DTOs und Entities:** Klassen-Javadoc mit fachlichem Kontext; Felder mit beschreibenden Inline-Javadoc-Kommentaren.
*   **Repositories:** Interface-Javadoc sowie Javadoc für alle abgeleiteten Query-Methoden.

Javadoc kann über Maven generiert werden:
```bash
./mvnw javadoc:javadoc -DskipTests
```
Ausgabe: `target/site/apidocs/index.html`

**Auswirkung:**
Die gesamte Codebasis ist nun vollständig auf Deutsch dokumentiert. Neue Entwickler können Verantwortlichkeiten, Parameter und Seiteneffekte direkt aus dem Code ablesen, ohne die Implementierung lesen zu müssen.

---

#### 11. Swagger/OpenAPI-Annotationen für alle Controller

**Problemstellung:**
Die Swagger UI unter `/swagger-ui.html` war zwar erreichbar (Springdoc OpenAPI 2.5.0 bereits eingebunden), zeigte aber nur rohe Endpunkt-Adressen ohne Beschreibungen, Tags oder Response-Dokumentation.

**Maßnahmen:**

Alle 11 Controller wurden mit Swagger/OpenAPI-Annotationen aus `io.swagger.v3.oas.annotations.*` versehen:

*   **`@Tag`** auf Klassenebene – gruppiert Endpunkte in der Swagger UI nach fachlichem Bereich.
*   **`@Operation(summary = "...")`** auf jeder Handler-Methode – beschreibt den Zweck des Endpunkts.
*   **`@ApiResponse` / `@ApiResponses`** – dokumentiert HTTP-Statuscodes (200, 302, 404, 500) wo fachlich relevant.
*   **`@Parameter`** – beschreibt Path-Variablen und Request-Parameter wo nicht selbsterklärend.

| Tag (Swagger UI) | Controller |
|---|---|
| Blog | `BlogPostController` |
| Admin – Blog | `AdminBlogController` |
| Shop | `ShopController` |
| Admin – Shop | `AdminShopController` |
| Newsletter | `NewsletterController` |
| Admin – Newsletter | `AdminNewsletterController` |
| Feed | `FeedController` |
| Authentifizierung | `LoginController`, `RegistrationController` |
| Admin – Dashboard | `AdminController` |
| Startseite | `HomeController` |

Kein bestehender Code wurde verändert – ausschließlich Annotationen und Imports ergänzt.

**Auswirkung:**
Die Swagger UI unter `http://localhost:8080/swagger-ui.html` zeigt alle Endpunkte geordnet nach fachlichen Bereichen, mit Beschreibungen, erwarteten Parametern und dokumentierten Response-Codes. Die Annotationen haben keinen Einfluss auf das Laufzeitverhalten der Anwendung.

---
