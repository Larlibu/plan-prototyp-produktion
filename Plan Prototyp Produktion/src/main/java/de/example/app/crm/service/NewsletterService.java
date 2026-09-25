package de.example.app.crm.service;

import de.example.app.crm.entity.NewsletterSubscriber;
import de.example.app.crm.repository.NewsletterSubscriberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service zur Verwaltung von Newsletter-Abonnements.
 * Enthält die Geschäftslogik für das Abonnieren, Abbestellen und Verwalten von Newsletter-Abonnenten.
 */
@Service
public class NewsletterService {

    private static final Logger log = LoggerFactory.getLogger(NewsletterService.class);

    private final NewsletterSubscriberRepository newsletterSubscriberRepository;
    private final MailerLiteService mailerLiteService;

    public NewsletterService(NewsletterSubscriberRepository newsletterSubscriberRepository,
                             MailerLiteService mailerLiteService) {
        this.newsletterSubscriberRepository = newsletterSubscriberRepository;
        this.mailerLiteService = mailerLiteService;
    }

    /**
     * Abonniert eine neue E-Mail-Adresse für den Newsletter.
     * Wenn die E-Mail bereits abonniert, aber inaktiv ist, wird sie reaktiviert.
     *
     * @param email Die zu abonnierende E-Mail-Adresse.
     * @return Der neu erstellte oder aktualisierte {@link NewsletterSubscriber}.
     * @throws IllegalArgumentException wenn die E-Mail bereits aktiv abonniert ist.
     */
    @Transactional
    public NewsletterSubscriber subscribeNewsletter(String email) {
        Optional<NewsletterSubscriber> existingSubscriber = newsletterSubscriberRepository.findByEmailIgnoreCase(email);

        if (existingSubscriber.isPresent()) {
            NewsletterSubscriber subscriber = existingSubscriber.get();
            if (subscriber.isActive()) {
                throw new IllegalArgumentException("E-Mail ist bereits aktiv abonniert.");
            } else {
                subscriber.setActive(true);
                log.info("Reaktiviere Newsletter-Abonnement für E-Mail: {}", email);
                NewsletterSubscriber saved = newsletterSubscriberRepository.save(subscriber);
                mailerLiteService.addSubscriber(email);
                return saved;
            }
        } else {
            NewsletterSubscriber newSubscriber = new NewsletterSubscriber(null, email, true, LocalDateTime.now());
            log.info("Abonniere neue E-Mail für Newsletter: {}", email);
            NewsletterSubscriber saved = newsletterSubscriberRepository.save(newSubscriber);
            mailerLiteService.addSubscriber(email);
            return saved;
        }
    }

    /**
     * Meldet eine E-Mail-Adresse vom Newsletter ab.
     * Setzt den Status des Abonnenten auf inaktiv.
     *
     * @param email Die abzumeldende E-Mail-Adresse.
     * @return True, wenn der Abonnent gefunden und abgemeldet wurde, false sonst.
     */
    public boolean unsubscribeNewsletter(String email) {
        Optional<NewsletterSubscriber> existingSubscriber = newsletterSubscriberRepository.findByEmailIgnoreCase(email);
        if (existingSubscriber.isPresent()) {
            NewsletterSubscriber subscriber = existingSubscriber.get();
            subscriber.setActive(false);
            newsletterSubscriberRepository.save(subscriber);
            log.info("E-Mail '{}' vom Newsletter abgemeldet.", email);
            return true;
        }
        log.warn("Versuch, nicht existierende E-Mail '{}' abzumelden.", email);
        return false;
    }

    public Optional<NewsletterSubscriber> getSubscriberById(UUID id) {
        return newsletterSubscriberRepository.findById(id);
    }

    public List<NewsletterSubscriber> getAllActiveSubscribers() {
        return newsletterSubscriberRepository.findAll().stream()
                .filter(NewsletterSubscriber::isActive)
                .collect(Collectors.toList());
    }

    public List<NewsletterSubscriber> getAllSubscribers() {
        return newsletterSubscriberRepository.findAll();
    }

    /**
     * Löscht einen Newsletter-Abonnenten anhand seiner ID.
     *
     * @param id Die ID des zu löschenden Abonnenten.
     * @return True, wenn der Abonnent erfolgreich gelöscht wurde, false sonst.
     */
    public boolean deleteSubscriber(UUID id) {
        log.info("Versuche Newsletter-Abonnenten mit ID {} zu löschen.", id);
        if (newsletterSubscriberRepository.existsById(id)) {
            newsletterSubscriberRepository.deleteById(id);
            log.info("Newsletter-Abonnent mit ID {} erfolgreich gelöscht.", id);
            return true;
        }
        log.warn("Newsletter-Abonnent mit ID {} nicht gefunden zum Löschen.", id);
        return false;
    }

    public boolean isUserSubscribed(String email) {
        return newsletterSubscriberRepository.findByEmailIgnoreCase(email)
                .map(NewsletterSubscriber::isActive)
                .orElse(false);
    }
}
