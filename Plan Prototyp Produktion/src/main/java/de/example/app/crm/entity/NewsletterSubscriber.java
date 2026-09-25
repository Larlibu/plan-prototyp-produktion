package de.example.app.crm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA-Entität für einen Newsletter-Abonnenten auf der gesunden Lebensmittel Webplattform.
 * Speichert E-Mail-Adresse, Aktivitätsstatus und Erstellungszeitpunkt des Abonnements.
 * Abmeldungen erfolgen ausschließlich über MailerLite-E-Mail-Links – ein direktes Setzen
 * von {@code active = false} durch die Anwendung ist nur für administrative Zwecke vorgesehen.
 */
@Entity
@Table(name = "newsletter_subscribers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsletterSubscriber {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "active", nullable = false)
    private boolean active;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
