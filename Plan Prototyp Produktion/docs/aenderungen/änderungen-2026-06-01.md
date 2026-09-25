# Fehlerbehebung – 01.06.2026

## Problem

Das Projekt ließ sich nicht starten. Beim Kompilieren trat folgender Fehler auf:

```
java: java.lang.ExceptionInInitializerError
com.sun.tools.javac.code.TypeTag :: UNKNOWN
```

## Ursachenanalyse

### Schritt 1: Java-Version prüfen

```
java -version
→ java version "26"
```

Das System verwendet Java 26, die `pom.xml` ist jedoch für `<java.version>21</java.version>` konfiguriert.

### Schritt 2: Lombok-Inkompatibilität

Spring Boot 3.2.5 verwendet standardmäßig **Lombok 1.18.32**, das Java 26 nicht unterstützt. Der `TypeTag :: UNKNOWN`-Fehler ist ein bekanntes Symptom dieser Inkompatibilität — Lomboks Annotation Processor greift auf interne JDK-APIs zu, die sich in Java 26 geändert haben.

Installierte JDKs auf dem System:
- `C:\Program Files\Java\jdk-26` (aktiv)
- `C:\Users\libud\.jdks\openjdk-22.0.1` (in IntelliJ registriert)
- Java 21 war **nicht** installiert

### Schritt 3: SSL-Fehler beim Maven-Download

Nach dem ersten Fix-Versuch (Lombok-Version auf 1.18.46 erhöhen) trat ein sekundärer Fehler auf:

```
PKIX path building failed: unable to find valid certification path to requested target
```

Maven 3.9.6 konnte mit Java 26 keine Verbindung zu Maven Central aufbauen. Ursache: SSL-Zertifikatsvalidierungsproblem zwischen Java 26 und der Maven-Wagon-Bibliothek.

**Behelf:** Download mit `-Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true`.

### Schritt 4: Lombok Annotation Processor nicht aktiv

Nach erfolgreichem Download folgten Kompilierfehler der Art `Symbol nicht gefunden: Methode getTitle()` auf `@Data`-annotierten Klassen (z. B. `BlogPost`, `Product`). Ursache: Ab neueren Java-Versionen muss Lombok explizit als `annotationProcessorPath` im `maven-compiler-plugin` konfiguriert werden — die automatische Erkennung greift nicht mehr zuverlässig.

## Durchgeführte Änderungen

**Datei:** `pom.xml`

### 1. Lombok-Version auf 1.18.46 überschrieben

```xml
<properties>
    ...
    <lombok.version>1.18.46</lombok.version>
</properties>
```

Lombok 1.18.46 ist die erste Version mit offiziellem Java 26-Support (GitHub Issue [#4019](https://github.com/projectlombok/lombok/issues/4019)).

### 2. Lombok als expliziten Annotation Processor eingebunden

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok.version}</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

## Ergebnis (nach Lombok-Fix)

```
INFO  o.s.b.w.e.tomcat.TomcatWebServer - Tomcat started on port 8080 (http)
INFO  de.example.app.Application - Started Application in 3.166 seconds
```

Die Anwendung startet erfolgreich, jedoch zeigten die Seiten beim Aufruf im Browser weitere Fehler (siehe unten).

---

## Fehler 2: Thymeleaf-Template – Fragment nicht auflösbar

### Fehlermeldung (application.log)

```
ERROR org.thymeleaf.TemplateEngine - Exception processing template "home"
org.thymeleaf.exceptions.TemplateInputException: Error resolving fragment:
"~{'layout/base' :: layout ('_arg0'=~{ :: title},'_arg1'=~{ :: main})}":
template or fragment could not be resolved (template: "home" - line 8, col 10)
```

Betroffen waren alle 14 Templates (u. a. `home.html`, `login.html`, alle Shop-, Blog-, CRM- und Admin-Seiten).

### Ursache

Alle Templates verwenden das Thymeleaf-Fragment-Parameterization-Pattern:

```html
<div th:replace="~{layout/base :: layout(~{::title}, ~{::main})}">
```

Die Layout-Datei `src/main/resources/templates/layout/base.html` definierte jedoch kein Fragment namens `layout` und nahm keine Parameter entgegen. Stattdessen verwendete sie `${content}` als Modell-Attribut — ein anderes, inkompatibles Pattern.

### Durchgeführte Änderung

**Datei:** `src/main/resources/templates/layout/base.html`

```html
<!-- Vorher -->
<html lang="de" xmlns:th="http://www.thymeleaf.org" ...>
<head>
    <title th:text="${pageTitle ?: 'Plan Prototyp Produktion'}"></title>
    ...
</head>
<body>
    ...
    <th:block th:insert="${content}"></th:block>
    ...
</body>
</html>

<!-- Nachher -->
<html lang="de" xmlns:th="http://www.thymeleaf.org" ...
      th:fragment="layout(title, content)">
<head>
    <title th:replace="${title}">Plan Prototyp Produktion</title>
    ...
</head>
<body>
    ...
    <div th:replace="${content}"></div>
    ...
</body>
</html>
```

**Erklärung der Änderungen:**
- `th:fragment="layout(title, content)"` auf dem `<html>`-Tag definiert das Fragment mit zwei Parametern
- `th:replace="${title}"` ersetzt das `<title>`-Tag durch den Seitentitel der aufrufenden Seite (`~{::title}`)
- `th:replace="${content}"` ersetzt den Platzhalter durch den `<main>`-Block der aufrufenden Seite (`~{::main}`)

### Ergebnis

```
INFO  o.s.b.w.e.tomcat.TomcatWebServer - Tomcat started on port 8080 (http)
INFO  de.example.app.Application - Started Application in 3.299 seconds
```

Keine `TemplateInputException` mehr. Alle Seiten werden korrekt gerendert.

---

---

## Feature: Migration auf Spring Data JPA + MySQL (Aiven)

### Ausgangslage

Die Anwendung nutzte bisher eine selbst entwickelte dateibasierte Persistenz (`FilePersistenceService`), die Entitäten als JSON-Dateien im `data/`-Verzeichnis speicherte. Für den Produktionsbetrieb wurde eine echte Datenbank angebunden.

**Bereitgestellte Datenbankverbindung (Aiven Cloud):**
```
mysql://avnadmin:***@mysql-219e10ea-libuda-fc0c.l.aivencloud.com:23628/defaultdb?ssl-mode=REQUIRED
```
MySQL 8.4 auf Aiven.

### Durchgeführte Änderungen

#### `pom.xml` — 3 neue Dependencies
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
```

#### `application.yml` — Datasource, JPA, Flyway konfiguriert
```yaml
spring:
  datasource:
    url: jdbc:mysql://<host>:23628/defaultdb?useSSL=true&requireSSL=true&verifyServerCertificate=false
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    database-platform: org.hibernate.dialect.MySQLDialect
    properties:
      hibernate:
        type:
          preferred_uuid_jdbc_type: VARCHAR   # UUID → VARCHAR(36) statt BINARY(16)
  flyway:
    enabled: true
    locations: classpath:db/migration
```

#### `V1__Initial_Schema.sql` — PostgreSQL → MySQL
| Vorher (PostgreSQL) | Nachher (MySQL) |
|---|---|
| `UUID PRIMARY KEY DEFAULT gen_random_uuid()` | `VARCHAR(36) NOT NULL PRIMARY KEY` |
| `TIMESTAMP WITHOUT TIME ZONE` | `DATETIME` |
| `NUMERIC(10,2)` | `DECIMAL(10,2)` |
| `ON CONFLICT DO NOTHING` | `ON DUPLICATE KEY UPDATE` |

#### `V2__Seed_Default_Roles.sql`
`gen_random_uuid()` durch `UUID()` ersetzt.

#### Alle 5 Entitäten — JPA-Annotationen ergänzt
`@Entity`, `@Table`, `@Id`, `@GeneratedValue(strategy = GenerationType.UUID)`, `@Column` aus `jakarta.persistence` in allen Entitäten (`User`, `Role`, `BlogPost`, `Product`, `NewsletterSubscriber`). `User.roles` erhielt `@ManyToMany` mit `@JoinTable`.

#### Alle 5 Repositories — Spring Data JPA Interfaces
Manuelle Klassen mit `FilePersistenceService`-Abhängigkeit durch `JpaRepository<Entity, UUID>`-Interfaces ersetzt. Alle Finder-Methoden auf `IgnoreCase`-Varianten umgestellt.

#### Services — 3 Anpassungen
1. **Passwort-Encoding:** War zuvor im `UserRepository` — jetzt in `DataInitializer` und `RegistrationService` mit explizitem `passwordEncoder.encode(password)`.
2. **`deleteById`-Rückgabewert:** `JpaRepository.deleteById()` gibt `void` zurück. Gelöst mit `existsById()` vor dem Löschen.
3. **`getPublishedBlogPosts()`:** Nutzt jetzt `findByPublishedTrue()` statt Stream-Filter.

#### `.gitignore` — neu erstellt (Repo-Root)
Schützt `target/`, `data/`, `logs/`, IDE-Dateien und Credentials-Dateien vor versehentlichem Commit.

### Ergebnis

```
HikariPool-1 - Start completed.
Flyway Community Edition 9.22.3 by Redgate
Database: jdbc:mysql://...23628/defaultdb (MySQL 8.4)
Successfully applied 2 migrations to schema `defaultdb`
Started Application in 13.434 seconds
```

Flyway hat V1 und V2 erfolgreich auf der Aiven-Datenbank ausgeführt. Alle 6 Tabellen wurden angelegt.

---

## Offene Punkte

- Das SSL-Problem mit Maven ist ein Workaround, kein dauerhafter Fix. Langfristig sollte entweder das Maven-Zertifikat korrekt konfiguriert oder auf JDK 21 (LTS) gewechselt werden, das zur `pom.xml`-Konfiguration passt.
- IntelliJ benötigt nach der `pom.xml`-Änderung einmalig **Maven > Reload Project**, damit die IDE die neuen Einstellungen übernimmt.
- **Sicherheit:** Die Datenbankzugangsdaten stehen aktuell in `application.yml`. Das Passwort sollte bei Aiven rotiert und künftig über Umgebungsvariablen (`DB_PASSWORD`) übergeben werden statt hartcodiert zu sein.

---

## Sicherheitshinweis: Zugangsdaten in `application.yml`

**Datum:** 01.06.2026

### Problem

`src/main/resources/application.yml` ist eine getrackte Datei im Repository und enthält aktuell die Datenbankzugangsdaten im Klartext:

```yaml
spring:
  datasource:
    username: ${DB_USERNAME:avnadmin}
    password: ${DB_PASSWORD:***REDACTED***}
```

Der Default-Wert hinter `:` ist das tatsächliche Passwort — jeder mit Zugriff auf das Repository kann es lesen.

### Risiko

- Passwort liegt im Git-Verlauf (alle bisherigen Commits)
- Bei einem Push auf GitHub wäre das Passwort öffentlich sichtbar

### Empfohlene Maßnahmen

1. **Passwort bei Aiven rotieren** — das aktuelle Passwort ist kompromittiert, sobald der Code das Repo verlässt.
2. **`application.yml` aus dem Git-Verlauf entfernen** (optional, aufwändig — `git filter-repo`), oder zumindest sicherstellen, dass das Repo nicht öffentlich gepusht wird.
3. **Langfristig:** Zugangsdaten ausschließlich über Umgebungsvariablen übergeben:

```bash
# Startbefehl mit Umgebungsvariablen
DB_URL=jdbc:mysql://... DB_USERNAME=avnadmin DB_PASSWORD=<neues-passwort> ./mvnw spring-boot:run
```

In `application.yml` dann **keine Default-Werte** mehr:

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

Startet die Anwendung ohne gesetzte Variablen, schlägt sie sofort mit einer klaren Fehlermeldung fehl — das ist gewollt.

---

## Feature: Admin-Benutzer via Flyway (V3)

**Datum:** 01.06.2026

### Ausgangslage

Der Admin-Benutzer wurde bisher nur durch `DataInitializer` (`@PostConstruct`) beim Start angelegt — programmatisch, nicht reproduzierbar über die Datenbankhistorie nachvollziehbar.

### Lösung

Neue Flyway-Migration `V3__Seed_Admin_User.sql` legt den Admin-Benutzer deklarativ in der Datenbank an.

**Datei:** `src/main/resources/db/migration/V3__Seed_Admin_User.sql`

```sql
INSERT INTO users (id, username, password, email, enabled)
VALUES (
    UUID(),
    'admin',
    '$2a$10$9ly7jP7qqmXYxd/..EQHLewmDUMs9Xc.mTHMPUPaVat0cbWIeEX2m',
    'admin@example.com',
    TRUE
)
ON DUPLICATE KEY UPDATE username = username;

INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ROLE_ADMIN'
WHERE u.username = 'admin';
```

### Details

| Feld | Wert |
|---|---|
| Benutzername | `admin` |
| Passwort | `password` (BCrypt, Faktor 10) |
| E-Mail | `admin@example.com` |
| Rolle | `ROLE_ADMIN` |

**BCrypt-Hash generiert mit:** Spring Security `BCryptPasswordEncoder` (JDK 26 + `spring-security-crypto-6.2.4.jar`)

**Idempotenz:** `ON DUPLICATE KEY UPDATE username = username` und `INSERT IGNORE` stellen sicher, dass die Migration auch auf Datenbanken sicher läuft, auf denen der Admin-Benutzer bereits durch `DataInitializer` angelegt wurde.

### Sicherheitshinweis

Das Standard-Passwort `password` ist nur für die Ersteinrichtung gedacht und **muss nach dem ersten Login geändert werden**.

---

## Feature: `/admin` öffentlich erreichbar mit rollenbasiertem Inhalt

**Datum:** 01.06.2026

### Ausgangslage

`/admin` war über `hasRole('ADMIN')` in der `SecurityFilterChain` gesperrt. Nicht eingeloggte Nutzer wurden automatisch auf `/login` umgeleitet und sahen die Seite nie.

### Lösung

**Drei Dateien geändert:**

#### `SecurityConfig.java`
```java
.requestMatchers("/admin").permitAll()          // Seite öffentlich
.requestMatchers("/admin/**").hasRole("ADMIN")  // Unterseiten weiterhin geschützt
```

#### `AdminController.java`
`@PreAuthorize` entfernt. Statistik-Daten werden nur geladen, wenn der eingeloggte Nutzer die Rolle `ROLE_ADMIN` trägt:
```java
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
boolean isAdmin = auth != null && auth.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

if (isAdmin) {
    model.addAttribute("totalBlogPosts", blogPostRepository.count());
    // ...
}
```

#### `admin/dashboard.html`
Inhalt über `sec:authorize` aufgeteilt:
```html
<div sec:authorize="hasRole('ADMIN')">
    <!-- Statistiken und Verwaltungs-Links -->
</div>
<div sec:authorize="!hasRole('ADMIN')">
    <p>Bitte melde dich als Administrator an, um diesen Bereich zu nutzen.</p>
    <a href="/login" class="button primary">Anmelden</a>
</div>
```

### Verhalten nach der Änderung

| Zustand | Anzeige unter `/admin` |
|---|---|
| Nicht eingeloggt | "Adminbereich" + Login-Button |
| Eingeloggt als Admin | "Adminbereich" + Statistiken + Verwaltungs-Links |

---

## Blog- und Shop-Admin: Fehlende und fehlerhafte Formulare (01.06.2026)

### Problem 1: Template `cms/admin/blog-create-edit.html` fehlte

Der Controller `BlogPostController` verwies auf das Template `cms/admin/blog-create-edit`,
das nie angelegt worden war. Aufruf von `/blog/admin/create` führte zu:

```
TemplateInputException: Error resolving template [cms/admin/blog-create-edit]
```

**Lösung:** Template neu erstellt unter
`src/main/resources/templates/cms/admin/blog-create-edit.html`.

---

### Problem 2: `th:object` mit ternärem Ausdruck nicht unterstützt

Sowohl das Blog- als auch das Shop-Formular verwendeten:

```html
th:object="${blogPostCreateDto != null ? blogPostCreateDto : blogPostUpdateDto}"
```

Thymeleaf wertet `th:object` nicht als SpEL-Ausdruck aus, sondern interpretiert
den gesamten String als Attributnamen. Das führte zu:

```
IllegalStateException: Neither BindingResult nor plain target object for bean name
'blogPostCreateDto != null ? blogPostCreateDto : blogPostUpdateDto' available
```

**Lösung:** Controller gibt beide DTOs (Create und Edit) unter demselben
Modell-Attributnamen aus; ein `isEdit`-Flag steuert die Unterschiede im Template.

```java
// GET /blog/admin/create
model.addAttribute("blogPost", new BlogPostCreateDto());
model.addAttribute("isEdit", false);

// GET /blog/admin/edit/{id}
model.addAttribute("blogPost", blogPostUpdateDto);
model.addAttribute("isEdit", true);
```

```html
<!-- Template -->
<form th:action="${isEdit} ? @{/blog/admin/edit/{id}(id=${blogPost.id})} : @{/blog/admin/create}"
      th:object="${blogPost}" ...>
```

POST-Handler erhalten `@ModelAttribute("blogPost")` damit Spring MVC das Binding
korrekt durchführt und Validierungsfehler zurück ins Formular gelangen.

Gleiches Muster wurde für `ShopController` und `product-create-edit.html` angewendet.

---

### Problem 3: `MaxUploadSizeExceededException` beim Datei-Upload

Spring Boot begrenzt Datei-Uploads standardmäßig auf 1 MB. Größere Bilder
führten zu:

```
MaxUploadSizeExceededException: Maximum upload size exceeded
```

**Lösung:** Limits in `application.yml` erhöht:

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 12MB
```

---

### Problem 4: `Data too long for column 'image'` beim Speichern

Die `image`-Spalte in `products` und `blog_posts` war als `TEXT` definiert
(MySQL-Limit: 65 535 Byte / ~64 KB). Base64-kodierte Bilder überschreiten
dieses Limit schnell:

```
SQL Error: 1406 – Data truncation: Data too long for column 'image' at row 1
```

**Lösung:** Flyway-Migration `V5__Fix_Image_Column_Size.sql` ändert beide
Spalten auf `MEDIUMTEXT` (Limit: 16 MB):

```sql
ALTER TABLE products   MODIFY COLUMN image MEDIUMTEXT;
ALTER TABLE blog_posts MODIFY COLUMN image MEDIUMTEXT;
```

Zusätzlich wurden die `@Column`-Annotationen in `Product.java` und
`BlogPost.java` auf `columnDefinition = "MEDIUMTEXT"` aktualisiert.

---

## Login-Redirect nach Rolle und Abmelde-Meldung nach Login (01.06.2026)

### Problem 1: Reguläre User landen nach Login auf `/admin`

Nach dem Login wurden alle Benutzer auf `/admin` weitergeleitet
(`defaultSuccessUrl("/admin", false)`). Reguläre User (ohne `ROLE_ADMIN`)
sahen dort das eingebettete Login-Formular und dachten, die Anmeldung sei
fehlgeschlagen.

**Lösung:** `RoleBasedAuthSuccessHandler` implementiert
(`SimpleUrlAuthenticationSuccessHandler`), der nach Rolle weiterleitet:

```java
String targetUrl = isAdmin ? "/admin" : "/";
getRedirectStrategy().sendRedirect(request, response, targetUrl);
```

Außerdem wurde `failureUrl` von `/admin?error=true` auf `/login?error=true`
korrigiert.

### Problem 2: Nach Login erscheint „Sie haben sich abgemeldet"

`SavedRequestAwareAuthenticationSuccessHandler` fand `/login?logout=true`
(die Seite nach dem Logout) als gespeicherten Request in der Session und
leitete dorthin weiter. Durch den Wechsel auf `SimpleUrlAuthenticationSuccessHandler`
wird die gespeicherte Request-URL ignoriert — der Redirect erfolgt immer
direkt zur rollenbasierten Ziel-URL.

### Problem 3: `/newsletter/admin/**` war öffentlich erreichbar

Die Regel `.requestMatchers("/newsletter/**").permitAll()` deckte auch
`/newsletter/admin/**` ab. Unauthentifizierte Anfragen wurden zwar durch
`@PreAuthorize` auf Controller-Ebene abgefangen (403), aber nicht zur
Login-Seite weitergeleitet.

**Lösung:** Spezifischere Regel vor der allgemeinen ergänzt:

```java
.requestMatchers("/newsletter/admin/**").hasRole("ADMIN")
.requestMatchers("/newsletter/**").permitAll()
```

---

## Feature: Newsletter-Versand via MailerLite (01.06.2026)

### Übersicht

Admins können unter `/newsletter/admin/compose` einen Newsletter verfassen
und an alle aktiven Abonnenten aus der MySQL-Datenbank versenden.

### Flow

1. Aktive Abonnenten werden aus der DB geladen (`NewsletterService.getAllActiveSubscribers()`)
2. E-Mails werden per Bulk-Import in eine MailerLite-Gruppe synchronisiert
3. Eine Kampagne wird erstellt (`POST /api/campaigns`)
4. Die Kampagne wird sofort versendet (`POST /api/campaigns/{id}/send-immediately`)

### Neue Komponenten

| Datei | Beschreibung |
|---|---|
| `MailerLiteProperties.java` | `@ConfigurationProperties` für API-Key, Absender, Gruppen-ID |
| `MailerLiteService.java` | Kapselt alle MailerLite-API-Calls (sync, create, send) |
| `NewsletterSendDto.java` | Formular-DTO (Betreff, HTML-Inhalt) |
| `newsletter-compose.html` | Compose-Formular mit Abonnenten-Anzahl |

### Konfiguration (Umgebungsvariablen)

```yaml
app:
  mailerlite:
    api-key: ${MAILERLITE_API_KEY:}
    from-name: "Plan Prototyp Produktion"
    from-email: ${MAILERLITE_FROM_EMAIL:}
    group-id: ${MAILERLITE_GROUP_ID:}
```

API-Key und sensible Werte kommen ausschließlich als Umgebungsvariablen
(nie im Code oder in Git). In IntelliJ unter Run/Debug Configurations →
Environment variables eintragen.
| Eingeloggt ohne Admin-Rolle | "Adminbereich" + Login-Hinweis |
