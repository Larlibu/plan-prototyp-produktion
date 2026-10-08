package de.example.app.security.service;

import de.example.app.security.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Lauscht auf Spring-Security-Authentifizierungsereignisse und schreibt Audit-Einträge
 * in den dedizierten Logger {@code SECURITY_AUDIT} (Zieldatei: {@code logs/security-audit.log}).
 * Protokolliert erfolgreiche Anmeldungen, fehlgeschlagene Anmeldeversuche sowie Abmeldungen
 * zusammen mit Benutzername und IP-Adresse.
 */
@Component
public class AuthenticationEventListener {

    private static final Logger audit = LoggerFactory.getLogger("SECURITY_AUDIT");

    private final UserRepository userRepository;

    /**
     * Erstellt einen neuen {@code AuthenticationEventListener}.
     *
     * @param userRepository Repository für den Datenzugriff auf Benutzer, wird zur
     *                       Unterscheidung von falschem Passwort und unbekanntem Benutzer genutzt
     */
    public AuthenticationEventListener(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Wird aufgerufen, wenn eine Authentifizierung erfolgreich abgeschlossen wurde.
     * Schreibt einen {@code LOGIN_SUCCESS}-Eintrag mit Benutzername und IP-Adresse ins Audit-Log.
     *
     * @param event Das Ereignisobjekt mit den Authentifizierungsdetails des angemeldeten Benutzers
     */
    @EventListener
    public void onLoginSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        String ip = extractIp(event.getAuthentication().getDetails());
        audit.info("LOGIN_SUCCESS  | user={} | ip={}", username, ip);
    }

    /**
     * Wird aufgerufen, wenn ein Authentifizierungsversuch fehlgeschlagen ist.
     * Ermittelt anhand des Benutzernamens, ob der Benutzer existiert, und protokolliert
     * entweder {@code FALSCHES_PASSWORT} oder {@code BENUTZER_NICHT_GEFUNDEN} als Ursache
     * im Audit-Log.
     *
     * @param event Das Ereignisobjekt mit dem fehlgeschlagenen Authentifizierungsversuch
     */
    @EventListener
    public void onLoginFailure(AbstractAuthenticationFailureEvent event) {
        String username = event.getAuthentication().getName();
        String ip = extractIp(event.getAuthentication().getDetails());

        boolean userExists = userRepository.findByUsernameIgnoreCase(username).isPresent();
        String reason = userExists ? "FALSCHES_PASSWORT" : "BENUTZER_NICHT_GEFUNDEN";

        audit.warn("LOGIN_FAILURE  | user={} | ip={} | ursache={}", username, ip, reason);
    }

    /**
     * Wird aufgerufen, wenn sich ein Benutzer erfolgreich abgemeldet hat.
     * Schreibt einen {@code LOGOUT}-Eintrag mit Benutzername und IP-Adresse ins Audit-Log.
     *
     * @param event Das Ereignisobjekt mit den Details der abgemeldeten Authentifizierungssitzung
     */
    @EventListener
    public void onLogout(LogoutSuccessEvent event) {
        String username = event.getAuthentication().getName();
        String ip = resolveCurrentIp();
        audit.info("LOGOUT         | user={} | ip={}", username, ip);
    }

    /**
     * Extrahiert die IP-Adresse des Clients aus den Authentifizierungsdetails.
     * Falls die Details vom Typ {@link WebAuthenticationDetails} sind, wird die Remote-Adresse
     * direkt ausgelesen; andernfalls wird die IP aus dem aktuellen HTTP-Request ermittelt.
     *
     * @param details Die Authentifizierungsdetails des Ereignisses
     * @return Die IP-Adresse des Clients oder {@code "unknown"}, falls sie nicht ermittelt werden kann
     */
    private String extractIp(Object details) {
        if (details instanceof WebAuthenticationDetails wad) {
            return wad.getRemoteAddress();
        }
        return resolveCurrentIp();
    }

    /**
     * Ermittelt die IP-Adresse des Clients aus dem aktuellen HTTP-Request-Kontext.
     * Berücksichtigt dabei den Header {@code X-Forwarded-For} für Anfragen hinter einem Proxy.
     *
     * @return Die IP-Adresse des Clients oder {@code "unknown"}, falls kein Request-Kontext
     *         verfügbar ist oder ein Fehler auftritt
     */
    private String resolveCurrentIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                String forwarded = req.getHeader("X-Forwarded-For");
                return (forwarded != null && !forwarded.isBlank())
                        ? forwarded.split(",")[0].trim()
                        : req.getRemoteAddr();
            }
        } catch (Exception ignored) {}
        return "unknown";
    }
}
