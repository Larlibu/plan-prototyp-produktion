# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring Boot 3.2.5 / Java 21 Monolith mit Thymeleaf-Frontend und MySQL-Datenbank. Kombiniert CMS (Blog), Mini-Shop, CRM/Newsletter und Admin-Dashboard.

## Build & Run

```bash
./mvnw clean package          # Bauen
./mvnw spring-boot:run        # Starten (Umgebungsvariablen erforderlich)
./mvnw test                   # Alle Tests
./mvnw test -Dtest=BlogPostServiceTest  # Einzelner Test
./mvnw verify                 # Tests + JaCoCo Coverage (target/site/jacoco/)
./mvnw spotbugs:check         # Statische Analyse
```

### Erforderliche Umgebungsvariablen

```
DB_URL, DB_USERNAME, DB_PASSWORD
MAILERLITE_API_KEY, MAILERLITE_FROM_EMAIL, MAILERLITE_GROUP_ID
```

Admin-Zugangsdaten: `app.admin.username` / `app.admin.password` / `app.admin.email` (Defaults: `admin` / `password` / `admin@example.com`).

## Architektur

### Paketstruktur (Package-by-Feature)

```
de.example.app
├── cms/        BlogPost – Controller, Service, Repository, Entity, DTOs
├── shop/       Product & Warenkorb – Controller, Service, Repository, Entity, DTOs
├── crm/        Newsletter & MailerLite – Controller, Service, Repository, Entity, Config, DTOs
├── security/   Auth, User, Role, DataInitializer, SecurityConfig
├── controller/ HomeController, AdminController, LoginController
├── config/     OpenApiConfig
└── shared/     ImageUtil, GlobalControllerAdvice
```

Jedes Feature-Paket enthält `entity/`, `dto/`, `repository/`, `service/`, `controller/`. Durchgehend Constructor Injection. Controllers sind dünn; Business Logic liegt in Services.

### Globaler Model-State

`GlobalControllerAdvice` (`shared/web/`) stellt zwei Model-Attribute für **alle** Views bereit:
- `cart` – aktueller `ShoppingCartDto` aus der HTTP-Session
- `isAdminPath` – Boolean, ob der Pfad mit `/admin` beginnt

### Session-basierter Warenkorb

`ShoppingCartService` speichert `ShoppingCartDto` unter Key `"shoppingCart"` in der `HttpSession`. Nicht persistent – geht bei Session-Ende verloren.

### Sicherheit

Rollen: `ROLE_ADMIN`, `ROLE_EDITOR`, `ROLE_USER`. CSRF ist deaktiviert.

- `/admin/**` → `ROLE_ADMIN`
- `/newsletter/admin/**` → `ROLE_ADMIN`  
- Öffentlich: `/`, `/css/**`, `/js/**`, `/images/**`, `/register`, `/blog/**`, `/shop/**`, `/newsletter/**`

`DataInitializer` legt beim Start fehlende Rollen und den Admin-User an und korrigiert das Passwort bei Abweichung. Login-Ereignisse werden nach `logs/security-audit.log` geschrieben.

### Datenbank & Migrationen

MySQL, Flyway-Migrationen in `src/main/resources/db/migration/`. JPA DDL-Auto ist `validate`.

**Bestehende Migrationen (V1–V6) niemals editieren** – immer neue Versionsdatei anlegen.

UUID als Primary Keys, Passwörter BCrypt-gehasht, Bilder als Base64 in `LONGTEXT`-Spalten.

### Templates

Thymeleaf mit `templates/layout/base.html` als Basis-Layout. htmx für dynamische Interaktionen. `sec:authorize` aus `thymeleaf-extras-springsecurity6`.

Admin-Templates unter `templates/admin/`, öffentliche unter `templates/cms/`, `templates/shop/`.

### Bildverarbeitung

`ImageUtil` (`shared/util/`) resized auf max. 1200px (konfigurierbar über `app.upload.max-width`) und kodiert als Base64 für die DB.

### MailerLite-Integration

`MailerLiteService` kommuniziert per `RestClient` mit der MailerLite REST API. Konfiguration über `MailerLiteProperties` gebunden an `app.mailerlite.*`. Wirft `MailerLiteException` bei API-Fehlern.

## API-Dokumentation

Swagger UI: `http://localhost:8080/swagger-ui.html` (Springdoc OpenAPI 2.5.0)
