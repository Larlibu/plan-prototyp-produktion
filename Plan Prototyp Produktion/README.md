# Plan Prototyp Produktion

## Webplattform für CMS, Mini-Shop, CRM und Administration

*Stand: 2026-09-25*

Dies ist eine integrierte Webplattform, die als serverseitiger Spring-Boot-Monolith entwickelt wurde. Sie umfasst ein Content-Management-System (CMS) für Blog und News, einen Mini-Shop mit Warenkorbfunktion, ein Customer Relationship Management (CRM) / Newsletter-System sowie einen Administrationsbereich.

---

## Dokumentation

*   [Anforderungen](docs/Anforderungen.md)
*   [Arc42](docs/Arc42.md)
*   [ADR-Dokumentation](docs/Adr_Dokumentation.md)
*   [Datenbankschema](docs/Datenbankschema.md)
*   [Admin-Benutzerhandbuch](docs/Admin-Benutzerhandbuch.md)
*   [Migration: File zu MySQL/JPA](docs/migration-file-to-mysql-jpa.md)
*   [Recherche](docs/Recherche.md)
*   [Eingabe-Prompt](docs/Eingabe_Prompt.md)
*   [Javadoc](docs/javadoc/index.html)
*   Änderungsprotokolle:
    [05-01](docs/aenderungen/änderungen-2026-05-01.md) ·
    [06-01](docs/aenderungen/änderungen-2026-06-01.md) ·
    [06-02](docs/aenderungen/änderungen-2026-06-02.md) ·
    [06-07](docs/aenderungen/änderungen_2026-06-07.md) ·
    [06-08](docs/aenderungen/änderungen_2026-06-08.md) ·
    [06-09](docs/aenderungen/änderungen_2026-06-09.md) ·
    [06-10](docs/aenderungen/änderungen_2026-06-10.md)

---

## Inhaltsverzeichnis

1.  [Projektübersicht](#projektübersicht)
2.  [Technologien](#technologien)
3.  [Voraussetzungen](#voraussetzungen)
4.  [Einrichtung](#einrichtung)
    *   [Datenbank (PostgreSQL)](#datenbank-postgresql)
    *   [Flyway Migrationen](#flyway-migrationen)
5.  [Anwendung starten](#anwendung-starten)
    *   [Entwicklungsumgebung](#entwicklungsumgebung)
    *   [Produktionsumgebung (Docker)](#produktionsumgebung-docker)
6.  [Zugriff auf die Anwendung](#zugriff-auf-die-anwendung)
7.  [Wichtige Hinweise](#wichtige-hinweise)
8.  [Architektur](#architektur)
9.  [Clean Code Regeln](#clean-code-regeln)
10. [Tests](#tests)
11. [Logging](#logging)
12. [Maven Plugins](#maven-plugins)

---

## 1. Projektübersicht

Das Projekt zielt darauf ab, eine moderne, wartbare und sauber strukturierte Webplattform bereitzustellen, die folgende Kernfunktionen integriert:
*   **CMS**: Blog- und News-Artikelverwaltung mit Bild-Upload.
*   **Mini-Shop**: Produktübersicht, Produktdetails, sessionbasierter Warenkorb.
*   **CRM / Newsletter**: Newsletter-Anmeldung, -Verwaltung und -Versand.
*   **Administrationsbereich**: Geschützte Bereiche für die Verwaltung aller Inhalte und Funktionen.

Die Anwendung folgt einem modularen Monolithen-Ansatz mit MVC-Architektur und Server-Side Rendering (Thymeleaf, htmx).

---

## 2. Technologien

### Backend
*   **Java**: 21
*   **Spring Boot**: 3.x
*   **Frameworks**: Spring MVC, Spring Security, Spring Data JPA
*   **ORM**: Hibernate
*   **Validierung**: Validation API
*   **Templating**: Thymeleaf
*   **Frontend-Interaktivität**: htmx
*   **Datenbank**: PostgreSQL
*   **Build-Tool**: Maven

---

## 3. Voraussetzungen

Stellen Sie sicher, dass die folgenden Tools auf Ihrem System installiert sind:
*   **Java Development Kit (JDK)**: Version 21
*   **Maven**: Version 3.8 oder höher
*   **PostgreSQL**: Datenbankserver
*   **Docker** und **Docker Compose**: Für die Produktionsumgebung und Testcontainers
*   **Git**: Für die Versionskontrolle

---

## 4. Einrichtung

### Datenbank (PostgreSQL)

1.  **Datenbanken erstellen**:
    Erstellen Sie die folgenden Datenbanken in Ihrer PostgreSQL-Instanz:
    *   `plan_prototyp_produktion_dev` (für die Entwicklungsumgebung)
    *   `plan_prototyp_produktion_prod` (für die Produktionsumgebung)
    *   `plan_prototyp_produktion_test` (für Integrationstests mit Testcontainers)

2.  **Benutzer erstellen**:
    Erstellen Sie entsprechende Benutzer und weisen Sie ihnen die notwendigen Rechte für die jeweiligen Datenbanken zu.
    *   `dev_user` mit Passwort `dev_password` für `plan_prototyp_produktion_dev`
    *   `prod_user` mit einem sicheren Passwort (wird über Umgebungsvariable `DB_PASSWORD` gesetzt) für `plan_prototyp_produktion_prod`
    *   (Testcontainers erstellt temporäre Benutzer für Tests)

    Beispiel SQL für Entwicklung:
    ```sql
    CREATE USER dev_user WITH PASSWORD 'dev_password';
    CREATE DATABASE plan_prototyp_produktion_dev OWNER dev_user;
    GRANT ALL PRIVILEGES ON DATABASE plan_prototyp_produktion_dev TO dev_user;
    ```

    Beispiel SQL für Produktion (Passwort sollte sicher generiert und als Umgebungsvariable übergeben werden):
    ```sql
    CREATE USER prod_user WITH PASSWORD 'YOUR_SECURE_PROD_PASSWORD';
    CREATE DATABASE plan_prototyp_produktion_prod OWNER prod_user;
    GRANT ALL PRIVILEGES ON DATABASE plan_prototyp_produktion_prod TO prod_user;
    ```

### Flyway Migrationen

Die Datenbankschemata werden automatisch von Flyway verwaltet.
*   Beim Start der Anwendung (sowohl in Dev als auch in Prod) werden ausstehende Migrationen im Verzeichnis `src/main/resources/db/migration` ausgeführt.
*   Für die Entwicklungsumgebung (`application-dev.yml`) ist `baseline-on-migrate` aktiviert, was nützlich ist, wenn Sie eine bestehende Datenbank ohne Flyway-Historie haben.
*   Für die Produktionsumgebung (`application-prod.yml`) ist `baseline-on-migrate` deaktiviert, da dies nach der initialen Migration nicht mehr benötigt wird und in einer Produktionsumgebung riskant sein kann.

---

## 5. Anwendung starten

### Entwicklungsumgebung

1.  **Projekt klonen**:
    ```bash
    git clone [URL_DES_REPOS]
    cd "Plan Prototyp Produktion"
    ```

2.  **Abhängigkeiten installieren**:
    ```bash
    mvn clean install
    ```

3.  **Anwendung starten**:
    Sie können die Anwendung direkt über Ihre IDE (z.B. IntelliJ IDEA, Eclipse) starten, indem Sie die `Application.java` Klasse ausführen.

    Alternativ über Maven:
    ```bash
    mvn spring-boot:run -Dspring-boot.run.profiles=dev
    ```
    Die Anwendung wird auf `http://localhost:8080` verfügbar sein.

### Produktionsumgebung (Docker)

Für die Produktionsumgebung wird Docker Compose verwendet.

1.  **Docker Compose Datei erstellen**:
    Erstellen Sie eine `docker-compose.yml` im Root-Verzeichnis des Projekts (wird noch generiert).

2.  **Umgebungsvariablen setzen**:
    Erstellen Sie eine `.env` Datei im selben Verzeichnis wie die `docker-compose.yml` und definieren Sie die Produktions-Datenbankpasswörter:
    ```
    DB_PASSWORD=YOUR_SECURE_PROD_PASSWORD
    ```
    **WICHTIG**: Ersetzen Sie `YOUR_SECURE_PROD_PASSWORD` durch ein starkes, sicheres Passwort.

3.  **Anwendung bauen und starten**:
    ```bash
    docker-compose up --build -d
    ```
    Dies wird die Anwendung und die PostgreSQL-Datenbank als Docker-Container starten. Die Anwendung wird auf Port `8080` des Hosts verfügbar sein.

---

## 6. Zugriff auf die Anwendung

Nach dem Start der Anwendung können Sie über Ihren Webbrowser auf folgende URLs zugreifen:

*   **Startseite**: `http://localhost:8080/`
*   **Admin-Login**: `http://localhost:8080/admin/login` (wird noch implementiert)
*   **CMS (Blog/News)**: `http://localhost:8080/blog`
*   **Shop**: `http://localhost:8080/shop`

Standard-Admin-Anmeldedaten werden in der Security-Konfiguration definiert (folgt in einem späteren Schritt).

---

## 7. Wichtige Hinweise

*   **Sicherheit**: Die Anwendung verwendet Spring Security für Authentifizierung, Autorisierung, CSRF-Schutz und Passwort-Hashing. Stellen Sie sicher, dass in der Produktion sichere Passwörter und HTTPS verwendet werden.
*   **HTMX**: Die Anwendung nutzt htmx für dynamische UI-Updates ohne komplexes JavaScript-Framework.
*   **Dateiuploads**: Bilder werden als `BYTEA` in der Datenbank gespeichert und automatisch auf eine maximale Breite von 1200px komprimiert.
*   **Logging**: Die Anwendung verwendet SLF4J für strukturiertes Logging. Logs werden in `logs/application.log` (oder `application-dev.log`/`application-prod.log`) gespeichert.

---

## 8. Architektur

*   **Modularer Monolith**: Das Projekt ist in logische Module (cms, crm, shop, security, shared) unterteilt, die jeweils ihre eigenen Controller, Services, Repositories, Entities und DTOs enthalten.
*   **MVC-Architektur**: Klare Trennung von Model, View und Controller.
*   **Package-by-Feature**: Code ist nach Funktionalität organisiert, nicht nach technischen Schichten.
*   **Server-Side Rendering**: Thymeleaf wird für das Rendern der HTML-Seiten auf dem Server verwendet.

---

## 9. Clean Code Regeln

Die Entwicklung folgt strengen Clean Code Regeln:
*   **Constructor Injection**: Für alle Abhängigkeiten.
*   **Keine Field Injection**.
*   **Kleine Klassen und Methoden**.
*   **Keine Businesslogik in Controllern**.
*   **Single Responsibility Principle**.
*   **Saubere Paketstruktur**.
*   **Verständliche Methodennamen**.
*   **JavaDoc**: Für öffentliche Klassen und Methoden.

---

## 10. Tests

Das Projekt umfasst verschiedene Testarten:
*   **Unit Tests**: Für Services und Businesslogik (JUnit 5, Mockito).
*   **Integration Tests**: Mit Spring Boot Test und Testcontainers für Datenbankinteraktionen.
*   **Security Tests**: Für Login, Rollen und Zugriffsschutz.

---

## 11. Logging

*   **SLF4J**: Als Logging-Fassade.
*   **Logback**: Als Implementierung.
*   **Strukturierte Logs**: Für bessere Analyse und Monitoring.

---

## 12. Maven Plugins

Die `pom.xml` ist mit folgenden wichtigen Plugins konfiguriert:
*   **spring-boot-maven-plugin**: Zum Bauen der ausführbaren JAR-Datei.
*   **maven-surefire-plugin**: Für Unit-Tests.
*   **jacoco-maven-plugin**: Für Code-Coverage-Berichte.
*   **spotbugs-maven-plugin**: Für statische Code-Analyse und Erkennung potenzieller Bugs und Sicherheitsprobleme.
