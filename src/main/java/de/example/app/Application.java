package de.example.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Haupt-Einstiegspunkt für die Spring Boot Anwendung.
 * Diese Klasse initialisiert und startet die gesamte Webplattform.
 */
@SpringBootApplication
public class Application {

    /**
     * Die Hauptmethode, die die Spring Boot Anwendung startet.
     *
     * @param args Kommandozeilenargumente, die an die Anwendung übergeben werden.
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
