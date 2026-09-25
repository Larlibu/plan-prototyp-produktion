# Codex Prompt — Vollständige Implementierung der Webplattform

Du bist ein Senior Software Architect und Senior Fullstack Engineer.

Erstelle ein vollständiges produktionsreifes Projekt basierend auf den folgenden Anforderungen und Architekturvorgaben.

Die Anwendung soll modern, wartbar, sauber strukturiert und vollständig lauffähig sein.

---

# PROJEKTZIEL

Entwickle eine integrierte Webplattform mit:

1. CMS (Blog & News)
2. Mini-Shop mit Warenkorb
3. CRM / Newsletter-System
4. Administrationsbereich

Die Anwendung soll als serverseitiger Spring-Boot-Monolith umgesetzt werden.

---

# TECHNISCHER STACK

## Backend

- Java 21
- Spring Boot 3.x
- Spring MVC
- Spring Security
- Spring Data JPA
- Hibernate
- Validation API
- Thymeleaf
- htmx
- PostgreSQL
- Maven

---

# ARCHITEKTURVORGABEN

## Architekturstil

- Modularer Monolith
- MVC-Architektur
- Package-by-Feature
- Server-Side Rendering
- Sessionbasierter Warenkorb
- Kein React
- Kein Angular
- Kein Vue
- Kein eigenes JavaScript außer minimal notwendigem htmx Setup

---

# CLEAN CODE REGELN

## Verbindlich

- Constructor Injection
- Keine Field Injection
- Kleine Klassen
- Kleine Methoden
- Keine Businesslogik in Controllern
- Single Responsibility Principle
- Saubere Paketstruktur
- Verständliche Methodennamen
- JavaDoc für öffentliche Klassen und Methoden

---

# PROJEKTSTRUKTUR

Erzeuge folgende Struktur:

```text
src/main/java/de/example/app

├── cms
├── crm
├── shop
├── security
├── shared
└── Application.java
```

Jedes Feature besitzt:

```text
controller
service
repository
entity
dto
```

---

# FUNKTIONALE ANFORDERUNGEN

# 1. CMS

## Funktionen

- Blogartikel erstellen
- Blogartikel bearbeiten
- Blogartikel löschen
- Blogartikel veröffentlichen
- Bild-Upload
- Öffentliche Artikelseite
- Admin Dashboard

## Entity Beispiel

```java
BlogPost
- id
- title
- slug
- content
- image
- published
- createdAt
- updatedAt
```

---

# 2. MINI SHOP

## Funktionen

- Produktübersicht
- Produktdetailseite
- Warenkorb
- Artikel hinzufügen
- Artikel entfernen
- Mengen ändern
- Sessionbasierter Warenkorb

## Entity Beispiel

```java
Product
- id
- name
- description
- price
- image
- stock
```

---

# 3. CRM / NEWSLETTER

## Funktionen

- Newsletter Anmeldung
- E-Mail Validierung
- Newsletter Verwaltung
- Newsletter Versand
- Admin Oberfläche

## Entity Beispiel

```java
NewsletterSubscriber
- id
- email
- active
- createdAt
```

---

# 4. AUTHENTIFIZIERUNG

## Anforderungen

- Login
- Logout
- Sessionbasierte Authentifizierung
- Rollenmodell

## Rollen

```text
ROLE_ADMIN
ROLE_EDITOR
```

---

# SICHERHEIT

Nutze Spring Security.

## Anforderungen

- CSRF Schutz
- HttpOnly Cookies
- Passwort Hashing mit BCrypt
- Geschützte Adminbereiche
- Security Filter Chain
- Rollenbasierte Zugriffe

---

# HTMX ANFORDERUNGEN

Nutze htmx für:

- Warenkorb Updates
- Dynamische Tabellen
- Formulare
- Validierungen
- Modals

## Beispiel

```html
<button
    hx-post="/cart/add"
    hx-target="#cart-counter"
    hx-swap="outerHTML">
</button>
```

---

# THYMELEAF

## Anforderungen

- Layout-Fragments
- Wiederverwendbare Komponenten
- Serverseitiges Rendering
- Saubere HTML-Struktur

---

# DATENBANK

## PostgreSQL

Nutze:

- Spring Data JPA
- Hibernate
- Flyway Migrationen

---

# FLYWAY

Erstelle:

- Initialschema
- Tabellenmigrationen
- Beispiel-Daten

---

# DATEIUPLOADS

## Anforderungen

- Bilder als BYTEA speichern
- Automatische Bildkompression
- Maximalbreite 1200px

---

# TESTS

Erstelle:

## Unit Tests

- Services testen
- Businesslogik testen

## Integrationstests

- Spring Kontext
- Repository Tests

## Security Tests

- Login
- Rollen
- Zugriffsschutz

Nutze:

- JUnit 5
- Mockito
- Spring Boot Test
- Testcontainers

---

# LOGGING

Nutze:

- SLF4J
- Strukturierte Logs

---

# DOKUMENTATION

Erstelle:

- JavaDoc
- README.md
- Swagger/OpenAPI
- Beispiel Requests

---

# MAVEN

Erstelle vollständige:

- pom.xml
- Maven Wrapper
- Plugin Konfigurationen

Nutze:

- spring-boot-maven-plugin
- surefire-plugin
- jacoco-plugin
- spotbugs-plugin

---

# DEVOPS

Erstelle zusätzlich:

## Docker

- Dockerfile
- docker-compose.yml

## Umgebungen

- dev
- test
- prod

## Konfiguration

Nutze:

```yaml
application.yml
application-dev.yml
application-prod.yml
```

---

# UI DESIGN

## Anforderungen

- Modernes minimalistisches UI
- Responsive Layout
- Dunkles modernes Farbschema
- Professionelles Admin Dashboard
- Mobile optimiert

Nutze:

- CSS
- Optional Bootstrap
- Keine komplexen Frontend Frameworks

---

# GENERIERE VOLLSTÄNDIG

## Wichtig

Generiere:

- Vollständige Klassen
- Vollständige Controller
- Vollständige Services
- Vollständige Repositories
- HTML Templates
- DTOs
- Security Config
- Maven Dateien
- Docker Dateien
- Tests
- SQL Migrationen

Keine Platzhalter.
Keine Pseudocode-Fragmente.
Keine TODO-Kommentare.

---

# OUTPUT FORMAT

## Liefere:

1. Vollständige Projektstruktur
2. Vollständigen Sourcecode
3. Alle Konfigurationsdateien
4. Maven Konfiguration
5. Docker Setup
6. SQL Migrationen
7. Beispiel Daten
8. README mit Startanleitung

---

# ZUSÄTZLICHE QUALITÄTSANFORDERUNGEN

## Der generierte Code muss:

- Produktionsreif sein
- Kompilierbar sein
- Ohne Fehler startbar sein
- Clean-Code Standards erfüllen
- Spring Best Practices nutzen
- Sicherheitsstandards einhalten
- Sauber dokumentiert sein

---

# PRIORITÄTEN

1. Wartbarkeit
2. Verständlichkeit
3. Sicherheit
4. Saubere Architektur
5. Erweiterbarkeit
6. Performance
7. Moderne UI