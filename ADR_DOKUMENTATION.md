```md id="f8nqtw"
# Architecture Decision Records (ADR)
# Webpräsenz mit CMS, CRM und Shop

---

# Inhaltsverzeichnis

- [ADR-001: Wahl der Gesamtarchitektur](#adr-001-wahl-der-gesamtarchitektur)
- [ADR-002: Einsatz von Spring Boot](#adr-002-einsatz-von-spring-boot)
- [ADR-003: Wahl von Maven](#adr-003-wahl-von-maven)
- [ADR-004: Einsatz von htmx statt SPA-Framework](#adr-004-einsatz-von-htmx-statt-spa-framework)
- [ADR-005: Nutzung des MVC-Patterns](#adr-005-nutzung-des-mvc-patterns)
- [ADR-006: Constructor Injection als Standard](#adr-006-constructor-injection-als-standard)
- [ADR-007: PostgreSQL als Datenbank](#adr-007-postgresql-als-datenbank)
- [ADR-008: Speicherung von Bildern als BLOB](#adr-008-speicherung-von-bildern-als-blob)
- [ADR-009: Sessionbasierter Warenkorb](#adr-009-sessionbasierter-warenkorb)
- [ADR-010: Thymeleaf als Template Engine](#adr-010-thymeleaf-als-template-engine)
- [ADR-011: Spring Security für Authentifizierung](#adr-011-spring-security-für-authentifizierung)
- [ADR-012: Monolith statt Microservices](#adr-012-monolith-statt-microservices)

---

# ADR-001: Wahl der Gesamtarchitektur

## Status

Akzeptiert

---

## Kontext

Das Projekt besitzt:

- Sehr kurze Entwicklungszeit
- Kleines Budget
- Ein-Personen-Team
- Begrenzte Infrastrukturressourcen

---

## Entscheidung

Die Anwendung wird als serverseitiger modularer Monolith umgesetzt.

---

## Begründung

Ein Monolith reduziert:

- Infrastrukturkomplexität
- Deployment-Aufwand
- Kommunikationsaufwand zwischen Services
- Betriebskosten

---

## Konsequenzen

### Vorteile

- Einfaches Deployment
- Geringe Infrastrukturkosten
- Schnelle Entwicklung
- Weniger DevOps-Aufwand

### Nachteile

- Begrenzte horizontale Skalierung
- Höhere Kopplung innerhalb der Anwendung

---

# ADR-002: Einsatz von Spring Boot

## Status

Akzeptiert

---

## Kontext

Das Backend benötigt:

- Sicherheitsmechanismen
- Datenbankintegration
- MVC-Unterstützung
- Produktive Standardlösungen

---

## Entscheidung

Verwendung von Spring Boot als zentrales Backend-Framework.

---

## Begründung

Spring Boot bietet:

- Große Community
- Produktionsreife Komponenten
- Auto-Configuration
- Eingebetteten Webserver
- Gute PostgreSQL-Unterstützung

---

## Konsequenzen

### Vorteile

- Hohe Produktivität
- Standardisierte Architektur
- Gute Testbarkeit

### Nachteile

- Höherer RAM-Verbrauch
- Langsame Startzeiten

---

# ADR-003: Wahl von Maven

## Status

Akzeptiert

---

## Kontext

Das Projekt benötigt:

- Reproduzierbare Builds
- Standardisierte Buildprozesse
- Dependency Management

---

## Entscheidung

Apache Maven wird als Buildsystem verwendet.

---

## Begründung

Maven ist:

- Standard im Java-Umfeld
- Sehr gut in Spring integriert
- Einfach in CI/CD integrierbar

---

## Konsequenzen

### Vorteile

- Standardisierte Builds
- Gute IDE-Unterstützung
- Klare Projektstruktur

### Nachteile

- XML-Konfiguration teilweise umfangreich

---

# ADR-004: Einsatz von htmx statt SPA-Framework

## Status

Akzeptiert

---

## Kontext

Die Anwendung benötigt:

- Dynamische UI
- Geringen Frontend-Aufwand
- Schnelle Entwicklung

---

## Entscheidung

Verwendung von htmx statt React, Angular oder Vue.

---

## Begründung

htmx ermöglicht:

- Dynamische Interaktionen
- Serverseitiges Rendering
- Kein komplexes Frontend-Buildsystem

---

## Konsequenzen

### Vorteile

- Weniger JavaScript
- Schnellere Entwicklung
- Einfachere Wartung

### Nachteile

- Weniger clientseitige Interaktivität
- Stärkere Serverabhängigkeit

---

# ADR-005: Nutzung des MVC-Patterns

## Status

Akzeptiert

---

## Kontext

Die Anwendung soll langfristig wartbar und verständlich bleiben.

---

## Entscheidung

Verwendung des MVC-Patterns.

---

## Begründung

Das MVC-Pattern trennt:

- Präsentation
- Geschäftslogik
- Datenzugriff

---

## Konsequenzen

### Vorteile

- Gute Wartbarkeit
- Gute Testbarkeit
- Klare Verantwortlichkeiten

### Nachteile

- Mehr Klassen
- Mehr Boilerplate

---

# ADR-006: Constructor Injection als Standard

## Status

Akzeptiert

---

## Kontext

Field Injection erschwert:

- Testbarkeit
- Explizite Abhängigkeiten
- Immutable Services

---

## Entscheidung

Ausschließlich Constructor Injection verwenden.

---

## Begründung

Constructor Injection:

- Macht Abhängigkeiten sichtbar
- Verbessert Testbarkeit
- Unterstützt immutable Klassen

---

## Konsequenzen

### Vorteile

- Bessere Wartbarkeit
- Explizite Dependencies
- Leichtere Unit-Tests

### Nachteile

- Mehr Boilerplate-Code

---

# ADR-007: PostgreSQL als Datenbank

## Status

Akzeptiert

---

## Kontext

Die Anwendung benötigt:

- Relationale Datenhaltung
- Kostenfreie Nutzung
- Gute Spring-Integration

---

## Entscheidung

Verwendung von PostgreSQL.

---

## Begründung

PostgreSQL bietet:

- Hohe Stabilität
- Gute Performance
- Kostenlosen Betrieb
- Sehr gute Spring-Unterstützung

---

## Konsequenzen

### Vorteile

- Stabilität
- Open Source
- Gute SQL-Unterstützung

### Nachteile

- Höherer Administrationsaufwand als SQLite

---

# ADR-008: Speicherung von Bildern als BLOB

## Status

Akzeptiert

---

## Kontext

Das Projekt besitzt:

- Kein Budget für Object Storage
- Günstigen VPS
- Begrenzte Infrastruktur

---

## Entscheidung

Bilder werden als BLOB (`BYTEA`) direkt in PostgreSQL gespeichert.

---

## Begründung

Dadurch entsteht:

- Einfacheres Backup
- Keine externe Storage-Infrastruktur
- Geringerer Administrationsaufwand

---

## Konsequenzen

### Vorteile

- Einfaches Deployment
- Einheitliche Datensicherung

### Nachteile

- Größere Datenbank
- Höhere DB-Last

---

# ADR-009: Sessionbasierter Warenkorb

## Status

Akzeptiert

---

## Kontext

Der Shop benötigt einen einfachen Warenkorbmechanismus.

---

## Entscheidung

Der Warenkorb wird serverseitig in der HTTP-Session gespeichert.

---

## Begründung

Dies reduziert:

- Frontend-Komplexität
- Manipulationsmöglichkeiten
- Clientseitigen Zustand

---

## Konsequenzen

### Vorteile

- Einfache Implementierung
- Sichere Speicherung

### Nachteile

- Begrenzte horizontale Skalierung
- RAM-Verbrauch pro Session

---

# ADR-010: Thymeleaf als Template Engine

## Status

Akzeptiert

---

## Kontext

Die Anwendung benötigt:

- Serverseitiges Rendering
- Gute Spring-Integration
- HTML-Templates

---

## Entscheidung

Verwendung von Thymeleaf.

---

## Begründung

Thymeleaf:

- Ist direkt in Spring integriert
- Unterstützt serverseitiges Rendering
- Arbeitet gut mit htmx zusammen

---

## Konsequenzen

### Vorteile

- Gute Wartbarkeit
- Kein separates Frontend notwendig

### Nachteile

- Weniger flexibel als moderne SPAs

---

# ADR-011: Spring Security für Authentifizierung

## Status

Akzeptiert

---

## Kontext

Die Anwendung benötigt:

- Loginmechanismen
- Session-Sicherheit
- CSRF-Schutz

---

## Entscheidung

Verwendung von Spring Security.

---

## Begründung

Spring Security bietet:

- Produktionsreife Sicherheitsmechanismen
- Session Management
- CSRF Schutz
- Rollenbasierte Rechte

---

## Konsequenzen

### Vorteile

- Hohe Sicherheit
- Gute Integration in Spring

### Nachteile

- Komplexe Konfiguration

---

# ADR-012: Monolith statt Microservices

## Status

Akzeptiert

---

## Kontext

Microservices würden zusätzliche Komplexität verursachen:

- Service-Kommunikation
- Deployment-Orchestrierung
- Monitoring
- DevOps-Aufwand

---

## Entscheidung

Verzicht auf Microservices.

---

## Begründung

Für Projektgröße und Teamgröße wäre Microservice-Architektur Overengineering.

---

## Konsequenzen

### Vorteile

- Einfachere Architektur
- Schnellere Entwicklung
- Weniger Infrastruktur

### Nachteile

- Begrenzte unabhängige Skalierung
- Größere Deployments
```
