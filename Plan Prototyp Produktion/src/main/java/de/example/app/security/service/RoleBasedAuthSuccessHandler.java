package de.example.app.security.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Rollenbasierter Handler für erfolgreiche Authentifizierungen.
 * Leitet Benutzer nach dem Login abhängig von ihrer Rolle auf unterschiedliche Seiten weiter:
 * Administratoren ({@code ROLE_ADMIN}) werden zum Admin-Dashboard ({@code /admin}) geleitet,
 * alle anderen Benutzer zur Startseite ({@code /}).
 */
@Component
public class RoleBasedAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    /**
     * Wird nach erfolgreicher Authentifizierung aufgerufen und führt die rollenbasierte Weiterleitung durch.
     * Prüft, ob der authentifizierte Benutzer die Rolle {@code ROLE_ADMIN} besitzt, und
     * leitet entsprechend weiter.
     *
     * @param request        Der eingehende HTTP-Request
     * @param response       Die ausgehende HTTP-Response, in die die Weiterleitung geschrieben wird
     * @param authentication Das Authentifizierungsobjekt mit Benutzerinformationen und Rollen
     * @throws IOException wenn die Weiterleitung nicht geschrieben werden kann
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        String targetUrl = isAdmin ? "/admin" : "/";
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
