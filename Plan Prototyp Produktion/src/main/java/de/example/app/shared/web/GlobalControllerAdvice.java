package de.example.app.shared.web;

import de.example.app.shop.dto.ShoppingCartDto;
import de.example.app.shop.service.ShoppingCartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Globaler {@link ControllerAdvice}, der gemeinsame Model-Attribute für alle Controller
 * und Thymeleaf-Views bereitstellt.
 * Fügt automatisch den aktuellen Warenkorb ({@code cart}) sowie ein Flag ({@code isAdminPath})
 * zum Spring-Model hinzu, damit diese Werte in Header, Navigation und Layout ohne
 * explizite Übergabe durch einzelne Controller verfügbar sind.
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    private final ShoppingCartService shoppingCartService;

    /**
     * Erstellt eine neue Instanz von {@link GlobalControllerAdvice}.
     *
     * @param shoppingCartService der Service zum Abrufen des session-basierten Warenkorbs
     */
    public GlobalControllerAdvice(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    /**
     * Stellt den aktuellen Warenkorb als Model-Attribut {@code cart} für alle Views bereit,
     * damit er z. B. in der Navigationsleiste angezeigt werden kann.
     *
     * @param session die aktuelle HTTP-Session, aus der der Warenkorb gelesen wird
     * @return das {@link ShoppingCartDto}-Objekt des aktuellen Warenkorbs
     */
    @ModelAttribute("cart")
    public ShoppingCartDto addCartToModel(HttpSession session) {
        return shoppingCartService.getCart(session);
    }

    /**
     * Prüft, ob der aktuelle Request-Pfad ein Admin-Bereich-Pfad ist, und stellt
     * das Ergebnis als Model-Attribut {@code isAdminPath} bereit.
     * Wird im Layout verwendet, um Admin-spezifische Navigationselemente ein- oder auszublenden.
     *
     * @param request der aktuelle {@link HttpServletRequest}
     * @return {@code true}, wenn der Anfrage-Pfad mit {@code /admin} beginnt, sonst {@code false}
     */
    @ModelAttribute("isAdminPath")
    public boolean addIsAdminPathToModel(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/admin");
    }
}
