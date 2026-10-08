package de.example.app.security.service;

import de.example.app.security.entity.Role;
import de.example.app.security.entity.User;
import de.example.app.security.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Benutzerdefinierte Implementierung von Spring Securitys {@link UserDetailsService},
 * die Benutzerdetails aus dem dateibasierten {@link UserRepository} lädt.
 */
@Service
public class FileUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public FileUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Sucht den Benutzer anhand des Benutzernamens. In der Implementierung wird die Suche
     * standardmäßig als case-insensitive behandelt, da {@code findByUsername} dies intern tut.
     *
     * @param username Der Benutzername, der den Benutzer identifiziert, dessen Daten benötigt werden.
     * @return Ein vollständig ausgefüllter Benutzerdatensatz (niemals null).
     * @throws UsernameNotFoundException wenn der Benutzer nicht gefunden werden konnte.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Benutzer nicht gefunden: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                mapRolesToAuthorities(user.getRoles())
        );
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }
}