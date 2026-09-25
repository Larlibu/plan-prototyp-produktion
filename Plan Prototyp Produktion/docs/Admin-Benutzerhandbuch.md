# Benutzerhandbuch für den Admin-Bereich

Dieses Handbuch beschreibt die Funktionen und die Bedienung des Admin-Bereichs der "Plan Prototyp Produktion"-Anwendung.

## 1. Zugang zum Admin-Bereich

Um auf den Admin-Bereich zugreifen zu können, müssen Sie sich mit einem Benutzerkonto anmelden, dem die Rolle `ROLE_ADMIN` zugewiesen ist.

1.  Navigieren Sie zur Login-Seite der Anwendung (standardmäßig `/login`).
2.  Melden Sie sich mit Ihren Admin-Zugangsdaten an.
    *   **Standard-Admin-Benutzer (nach Flyway-Migration):**
        *   Benutzername: `admin`
        *   Passwort: `password` (Es wird dringend empfohlen, dieses Passwort sofort nach dem ersten Login zu ändern!)
3.  Nach erfolgreichem Login können Sie über die Navigation oder direkt über die URL `/admin` zum Admin-Dashboard gelangen.

## 2. Admin-Dashboard Übersicht

Das Admin-Dashboard bietet einen schnellen Überblick über wichtige Kennzahlen der Anwendung:

*   **Gesamtzahl Blogbeiträge**: Anzahl aller vorhandenen Blogbeiträge.
*   **Veröffentlichte Blogbeiträge**: Anzahl der aktuell veröffentlichten Blogbeiträge.
*   **Gesamtzahl Produkte**: Anzahl aller im Shop verfügbaren Produkte.
*   **Aktive Newsletter-Abonnenten**: Anzahl der Benutzer, die aktiv den Newsletter abonniert haben.
*   **Gesamtzahl Newsletter-Abonnenten**: Gesamtzahl aller registrierten Newsletter-Abonnenten (aktiv und inaktiv).
*   **Gesamtzahl Benutzer**: Anzahl aller registrierten Benutzerkonten.

## 3. Navigation im Admin-Bereich

Die Navigation im Admin-Bereich ermöglicht den Zugriff auf verschiedene Verwaltungsbereiche:

*   **Blogbeiträge**: Verwaltung von Blogartikeln.
*   **Produkte**: Verwaltung der Shop-Produkte.
*   **Newsletter-Abonnenten**: Verwaltung der Newsletter-Anmeldungen.
*   **Benutzerverwaltung**: Verwaltung der Benutzerkonten und Rollen.

*(Hinweis: Die genauen Navigationspunkte können je nach Implementierung variieren.)*

## 4. Verwaltung von Blogbeiträgen

In diesem Bereich können Sie Blogbeiträge erstellen, bearbeiten, veröffentlichen und löschen.

*   **Anzeigen**: Eine Liste aller Blogbeiträge mit Titel, Slug, Veröffentlichungsstatus und Erstellungsdatum.
*   **Erstellen**: Formular zum Hinzufügen eines neuen Blogbeitrags. Erforderliche Felder sind Titel, Slug, Inhalt und optional ein Bild. Sie können den Beitrag direkt als "veröffentlicht" markieren oder als Entwurf speichern.
*   **Bearbeiten**: Bearbeiten Sie bestehende Blogbeiträge. Sie können alle Details ändern, einschließlich des Bildes und des Veröffentlichungsstatus.
*   **Löschen**: Entfernen Sie Blogbeiträge dauerhaft.

## 5. Verwaltung von Produkten

Hier verwalten Sie die Produkte, die im Shop angeboten werden.

*   **Anzeigen**: Eine Liste aller Produkte mit Name, Preis, Lagerbestand und Bild.
*   **Erstellen**: Formular zum Hinzufügen eines neuen Produkts. Erforderliche Felder sind Name, Beschreibung, Preis, Lagerbestand und optional ein Bild.
*   **Bearbeiten**: Bearbeiten Sie bestehende Produkte. Sie können Details wie Preis, Lagerbestand und Bild aktualisieren.
*   **Löschen**: Entfernen Sie Produkte dauerhaft aus dem Shop.

## 6. Verwaltung von Newsletter-Abonnenten

Dieser Bereich ermöglicht die Verwaltung der E-Mail-Adressen, die den Newsletter abonniert haben.

*   **Anzeigen**: Eine Liste aller Abonnenten mit E-Mail-Adresse, Aktivitätsstatus und Anmeldedatum.
*   **Abonnieren/Abbestellen**: Sie können den Status eines Abonnenten von aktiv auf inaktiv setzen oder umgekehrt.
*   **Löschen**: Entfernen Sie Abonnenten dauerhaft aus der Liste.

## 7. Benutzerverwaltung

Hier können Sie Benutzerkonten verwalten und deren Rollen zuweisen.

*   **Anzeigen**: Eine Liste aller registrierten Benutzer mit Benutzername, E-Mail, Aktivitätsstatus und zugewiesenen Rollen.
*   **Erstellen**: Formular zum Anlegen neuer Benutzerkonten.
*   **Bearbeiten**: Bearbeiten Sie Benutzerdetails und weisen Sie Rollen zu (z.B. `ROLE_ADMIN`, `ROLE_EDITOR`, `ROLE_USER`).
*   **Löschen**: Entfernen Sie Benutzerkonten dauerhaft.

## 8. Sicherheitshinweise

*   **Standardpasswort ändern**: Ändern Sie IMMER das Standardpasswort des Admin-Benutzers (`admin`/`password`) sofort nach dem ersten Login.
*   **Starke Passwörter**: Verwenden Sie für alle Benutzerkonten starke, einzigartige Passwörter.
*   **Rollenprinzip**: Weisen Sie Benutzern nur die Rollen zu, die sie für ihre Aufgaben unbedingt benötigen (Least Privilege Principle).
*   **Regelmäßige Backups**: Stellen Sie sicher, dass regelmäßige Backups der Datenbank durchgeführt werden.

---
*Dieses Handbuch ist ein Entwurf und kann je nach spezifischer Implementierung und weiteren Funktionen der Anwendung angepasst und erweitert werden.*
