# Projektdokumentation: Webpräsenz mit integriertem CMS, CRM und Shop

## 1. Projekt-Szenario & Rahmenbedingungen
* **Ziel:** Erstellung einer professionellen Webpräsenz für eine kleine Organisation / einen Freund.
* **Rolle:** 1-Personen-Entwicklerteam (Fullstack).
* **Deadline:** Maximal 2 Wochen (Harte Restriktion wegen anstehender Messe).
* **Budget:** Maximal 2,00 € pro Monat (24,00 € pro Jahr) für Server/Hosting (exklusive Domain).
* **Qualitätsanspruch:** Fehlerfrei, stabil, erweiterbar und verständlich dokumentiert.
* **Technologische Vorgabe:** Strikte Vermeidung von eigenem JavaScript-Code im Frontend.

---

## 2. Gewählte Technologie-Architektur
Anstelle einer getrennten Client-Server-Architektur (z. B. mit React/Angular und einer REST-API) wird ein **moderner, servergetriebener Monolith** eingesetzt.

### Backend & Core
* **Java & Spring Boot:** Liefert das stabile, performante Fundament für die Geschäftslogik und Datenverwaltung.
* **Spring Security:** Übernimmt die Absicherung des Systems (Login, Registrierung, CSRF-Schutz und Admin-Rechte) nativ über sichere Browser-Cookies (kein komplexes JWT-Token-Management nötig).

### Frontend (Ohne JavaScript-Infrastruktur)
* **Thymeleaf:** HTML-Template-Engine für Java. Der Server rendert die Daten direkt in die HTML-Vorlagen. Es gibt keine doppelte Validierungslogik im Frontend.
* **htmx:** Eine minimale HTML-Bibliothek (wird einmalig per Skript-Tag eingebunden). Sie ermöglicht asynchrone Server-Anfragen (AJAX) direkt über HTML-Attribute (z. B. `hx-post`, `hx-target`). Erzeugt ein flüssiges "Single-Page-Application"-Gefühl komplett ohne eigenen JavaScript-Code.
* **Tailwind CSS / Bootstrap:** Für das visuelle Styling der UI-Komponenten ohne CSS-Overhead.

---

## 3. Strategische Vorteile für die 2-Wochen-Deadline
* **Halbierung der Entwicklungszeit:** Es entfällt die komplette Frontend-Pipeline (kein Node.js, kein `npm`, kein Webpack, kein API-Design, keine doppelten Datenmodelle/DTOs).
* **Kosten-Minimum (< 2 €/Monat):** Da die Anwendung als einzelner Monolith kompiliert wird, reicht ein einziger kleiner Server im Betrieb aus.
* **Hosting-Infrastruktur:** 
  * *App-Hosting:* Günstiger Mini-VPS (z. B. via LowEndBox-Anbieter) oder Free-Tier-Anbieter wie Railway/Render.
  * *Datenbank:* Kostenlose Cloud-PostgreSQL (z. B. Supabase).
  * *Inhalte:* Bilder werden ressourcensparend als Byte-Array (`BLOB`) in der DB oder lokal auf dem Server abgelegt.

---

## 4. Umsetzungsfahrplan der Kernfunktionen

### Subtask 1: Der öffentliche Bereich (Besucher & Kunden)
* **Landing Page & News-Feed:** Statische Sektionen kombiniert mit einer dynamischen Thymeleaf-Schleife, die aktuelle Blogartikel aus der Datenbank liest.
* **Shop & Warenkorb:** Der Warenkorb wird in der Java `HTTP-Session` verwaltet. Klickt ein Nutzer auf "In den Warenkorb", sendet `htmx` einen Hintergrund-Request. Der Server aktualisiert die Session und gibt *nur* das HTML-Schnipsel für die neue Artikelanzahl zurück, das htmx im Header austauscht (ohne Seiten-Reload).
* **User-Login:** Standardisierte, sichere Login- und Registrierungsformulare über Spring Security.
* **Newsletter-Anmeldung:** `htmx` validiert die E-Mail-Adresse live beim Tippen über den Server. Bei Erfolg wird die Mail in der DB gespeichert.

### Subtask 2: Das Admin-Dashboard (`/admin`)
* **Zugriffsschutz:** Serverseitige Sperrung des Pfads `/admin/**` über Spring Security (nur zugänglich mit der Rolle `ROLE_ADMIN`).
* **CMS (Blog-Verwaltung):** Admin-Formular mit Datei-Upload (`MultipartFile`) für Bilder. Eine htmx-gestützte Löschen-Funktion entfernt Blog-Einträge per Klick: Der Server löscht den DB-Eintrag, htmx entfernt zeitgleich die Tabellenzeile im Browser.
* **CRM (Newsletter-Versand):** Ein einfaches Textfeld im Dashboard. Beim Absenden zieht Java die Abonnenten-Mails aus der Datenbank und übergibt sie gesammelt an die API eines kostenlosen Versanddienstes (z. B. Brevo Gratis-Tarif).
