package de.example.app.security.config;

import de.example.app.security.service.RoleBasedAuthSuccessHandler;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Zentrale Sicherheitskonfiguration der Anwendung.
 * Definiert Zugriffsregeln für HTTP-Endpunkte, konfiguriert das Login- und Logout-Verhalten,
 * aktiviert die Authentifizierungsereignis-Veröffentlichung und deaktiviert CSRF-Schutz.
 * <p>
 * Zugriffsmatrix:
 * <ul>
 *   <li>{@code /admin/**} – nur {@code ROLE_ADMIN}</li>
 *   <li>{@code /newsletter/admin/**} – nur {@code ROLE_ADMIN}</li>
 *   <li>Öffentlich: {@code /}, statische Ressourcen, {@code /register}, {@code /blog/**},
 *       {@code /shop/**}, {@code /newsletter/**}, {@code /feed/**}</li>
 *   <li>Alle übrigen Pfade – Authentifizierung erforderlich</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Stellt einen {@link PasswordEncoder} auf Basis von BCrypt bereit.
     * BCrypt wird für das Hashen aller Benutzerpasswörter verwendet.
     *
     * @return eine neue Instanz von {@link BCryptPasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Registriert einen {@link DefaultAuthenticationEventPublisher}, der Spring-Security-
     * Authentifizierungsereignisse (Erfolg, Fehlschlag, Abmeldung) über den
     * Spring-{@link ApplicationEventPublisher} veröffentlicht.
     *
     * @param publisher Der Spring-Anwendungsevent-Publisher
     * @return eine konfigurierte Instanz von {@link DefaultAuthenticationEventPublisher}
     */
    @Bean
    public DefaultAuthenticationEventPublisher authenticationEventPublisher(
            ApplicationEventPublisher publisher) {
        return new DefaultAuthenticationEventPublisher(publisher);
    }

    /**
     * Konfiguriert die {@link SecurityFilterChain} mit allen HTTP-Sicherheitsregeln.
     * Legt Zugriffsberechtigungen, das benutzerdefinierte Login-Formular,
     * das Logout-Verhalten und die Deaktivierung von CSRF fest.
     *
     * @param http           Das {@link HttpSecurity}-Objekt zur Konfiguration der Sicherheitsfilter
     * @param successHandler Der rollenbasierte Weiterleitungs-Handler nach erfolgreichem Login
     * @return die fertig konfigurierte {@link SecurityFilterChain}
     * @throws Exception wenn die Konfiguration fehlschlägt
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                    RoleBasedAuthSuccessHandler successHandler) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/admin").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/css/**", "/js/**", "/images/**", "/").permitAll()
                .requestMatchers("/register").permitAll()
                .requestMatchers("/newsletter/admin/**").hasRole("ADMIN")
                .requestMatchers("/blog/**", "/shop/**", "/newsletter/**", "/feed/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
                .successHandler(successHandler)
                .failureUrl("/login?error=true")
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            )
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
