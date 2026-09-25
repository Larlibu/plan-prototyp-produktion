package de.example.app.crm.service;

/**
 * Unkontrollierte Ausnahme für Fehler bei der Kommunikation mit der MailerLite REST API.
 * Wird geworfen, wenn HTTP-Anfragen an MailerLite mit einem unerwarteten Fehlerstatuscode
 * antworten oder die Verarbeitung der API-Antwort fehlschlägt. Ermöglicht eine einheitliche
 * Fehlerbehandlung auf Controller-Ebene ohne erzwungene Try-Catch-Blöcke in Services.
 */
public class MailerLiteException extends RuntimeException {

    /**
     * Erstellt eine neue {@code MailerLiteException} mit der angegebenen Fehlermeldung.
     *
     * @param message die Fehlermeldung, die den Fehlerkontext beschreibt
     */
    public MailerLiteException(String message) {
        super(message);
    }

    /**
     * Erstellt eine neue {@code MailerLiteException} mit Fehlermeldung und auslösender Ursache.
     *
     * @param message die Fehlermeldung, die den Fehlerkontext beschreibt
     * @param cause   die ursprüngliche Ausnahme, die diesen Fehler ausgelöst hat
     */
    public MailerLiteException(String message, Throwable cause) {
        super(message, cause);
    }
}
