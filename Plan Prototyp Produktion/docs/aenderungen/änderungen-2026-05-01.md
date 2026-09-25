# Migration von dateibasierter Persistenz zu MySQL/JPA

Dieses Dokument beschreibt die durchgeführten Schritte zur Migration des Projekts von einer dateibasierten Persistenz (`FilePersistenceService`) zu einer relationalen Datenbank (MySQL) unter Verwendung von Spring Data JPA.

## Ausgangssituation

Das Projekt verwendete ursprünglich einen `FilePersistenceService` zur Speicherung von Entitäten (User, Role, NewsletterSubscriber, Product) als JSON-Dateien im `data`-Ordner. Die `pom.xml` wurde bereits auf MySQL und Flyway umgestellt, jedoch waren die Repository-Implementierungen noch auf die dateibasierte Persistenz ausgerichtet. Dies führte zu einem Startfehler (`Symbol nicht gefunden: Methode findByUsernameIgnoreCase`).

## Durchgeführte Änderungen

Die Migration umfasste folgende Schritte:

1.  **Analyse des Startfehlers**: Der Fehler `Symbol nicht gefunden: Methode findByUsernameIgnoreCase` deutete darauf hin, dass die `UserRepository` nicht korrekt für Spring Data JPA konfiguriert war.

2.  **Umstellung der Repository-Schnittstellen auf Spring Data JPA**:
    *   **`UserRepository`**: Die Klasse `UserRepository` wurde in eine Schnittstelle umgewandelt, die `JpaRepository<User, UUID>` erweitert. Die manuellen Implementierungen für `save`, `findById`, `findAll`, `deleteById` sowie die dateibasierten Suchmethoden wurden entfernt. Stattdessen wurden abgeleitete Abfragemethoden `findByUsernameIgnoreCase(String username)` und `findByEmailIgnoreCase(String email)` deklariert, die von Spring Data JPA automatisch implementiert werden. Zusätzlich wurde `long count()` explizit deklariert, um einen "Symbol nicht gefunden"-Fehler zu beheben.
    *   **`RoleRepository`**: Analog zur `UserRepository` wurde `RoleRepository` in eine Schnittstelle umgewandelt, die `JpaRepository<Role, UUID>` erweitert. Die Methode `findByNameIgnoreCase(String name)` wurde hinzugefügt.
    *   **`NewsletterSubscriberRepository`**: Ebenfalls in eine Schnittstelle umgewandelt, die `JpaRepository<NewsletterSubscriber, UUID>` erweitert. Die Methode `findByEmailIgnoreCase(String email)` wurde hinzugefügt.
    *   **`ProductRepository`**: In eine Schnittstelle umgewandelt, die `JpaRepository<Product, UUID>` erweitert. Hier waren keine zusätzlichen abgeleiteten Abfragemethoden erforderlich, da die Standard-CRUD-Operationen ausreichten.

3.  **Entfernung des `FilePersistenceService`**:
    *   Nachdem alle Repository-Implementierungen auf Spring Data JPA umgestellt wurden, wurde der `FilePersistenceService` im Anwendungscode nicht mehr benötigt.
    *   Die Datei `src/main/java/de/example/app/shared/util/FilePersistenceService.java` wurde geleert (effektiv gelöscht).
    *   Die zugehörige Testdatei `src/test/java/de/example/app/shared/util/FilePersistenceServiceTest.java` wurde ebenfalls geleert (effektiv gelöscht), da sie eine nicht mehr existierende Klasse testete.

4.  **Entfernung des `data`-Ordners**:
    *   Der `data`-Ordner, der die JSON-Dateien der dateibasierten Persistenz enthielt, wurde manuell entfernt, da er nach der Migration keine Funktion mehr hatte.

5.  **Behebung von "Symbol nicht gefunden" Fehlern nach Repository-Umstellung**:
    *   **`DataInitializer.java`**: Die Methode `roleRepository.findByName()` wurde in `roleRepository.findByNameIgnoreCase()` geändert.
    *   **`DataInitializer.java`**: Die Methode `userRepository.findByUsername()` wurde in `userRepository.findByUsernameIgnoreCase()` geändert.
    *   **`FileUserDetailsService.java`**: Die Methode `userRepository.findByUsername()` wurde in `userRepository.findByUsernameIgnoreCase()` geändert.
    *   **`NewsletterSubscriberRepository.java`**: Die Methode `countByActiveTrue()` wurde zur Schnittstelle hinzugefügt.
    *   **`UserRepository.java`**: Die Methode `count()` wurde explizit zur Schnittstelle hinzugefügt.
    *   **`BlogPostRepository.java`**: Die Methode `count()` wurde explizit zur Schnittstelle hinzugefügt.

6.  **Behebung von "Inkompatible Typen: void kann nicht in boolean konvertiert werden" Fehlern**:
    *   **`NewsletterService.java`**: Die Methode `deleteSubscriber(UUID id)` wurde angepasst, um `newsletterSubscriberRepository.existsById(id)` zu verwenden und einen `boolean`-Wert zurückzugeben.
    *   **`ProductService.java`**: Die Methode `deleteProduct(UUID id)` wurde angepasst, um `productRepository.existsById(id)` zu verwenden und einen `boolean`-Wert zurückzugeben.

7.  **Entitäten mit JPA-Annotationen versehen**:
    *   Alle Entitäten (`User`, `Role`, `NewsletterSubscriber`, `Product`, `BlogPost`) wurden mit `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column` und weiteren relevanten JPA-Annotationen versehen, um die Persistenz in einer relationalen Datenbank zu ermöglichen.

8.  **Anpassung und Überprüfung der Flyway-Migrationen**:
    *   **`V1__Initial_Schema.sql`**: Die Spalten `content` und `image` in der Tabelle `blog_posts` sowie die Spalte `image` in der Tabelle `products` wurden von `TEXT` auf `LONGTEXT` geändert, um Konsistenz mit den JPA-Entitäten zu gewährleisten.
    *   **`V5__Fix_Image_Column_Size.sql`**: Der Inhalt dieses Migrationsskripts wurde geleert, da die Änderungen an den `image`-Spalten nun direkt in `V1__Initial_Schema.sql` vorgenommen wurden, wodurch `V5` redundant wurde. Die Datei bleibt bestehen, damit Flyway die Migration als ausgeführt betrachtet.
    *   **`V6__Fix_Product_Image_Column_Type.sql`**: Ein neues Migrationsskript wurde erstellt, um den Spaltentyp der `image`-Spalte in der `products`-Tabelle auf `LONGTEXT` zu ändern. Dies behebt Schema-Validierungsfehler, die auftraten, wenn `V1` bereits angewendet war und nicht erneut ausgeführt werden konnte.
    *   Die übrigen Flyway-Migrationsskripte (`V2`, `V3`, `V4`) wurden auf Kompatibilität mit dem neuen JPA-Schema überprüft und für korrekt befunden.

9.  **Überprüfung der Anwendungskonfiguration**:
    *   Die `application.yml` wurde überprüft und enthält keine Überbleibsel der alten dateibasierten Persistenz.
    *   Die `pom.xml` wurde angepasst, um die Datenbankzugangsdaten direkt im Flyway Maven Plugin zu konfigurieren, wobei `&`-Zeichen als `&amp;` maskiert wurden, um Konnektivitätsprobleme zu beheben.

## Ergebnis

Das Projekt ist nun vollständig auf die Verwendung von MySQL als Datenbank und Spring Data JPA als Persistenzschicht umgestellt. Alle ehemaligen dateibasierten Repositories nutzen nun die Vorteile von JPA für Datenbankoperationen. Der `FilePersistenceService` und seine zugehörigen Artefakte wurden aus dem Projekt entfernt, was zu einer saubereren Codebasis führt.

Alle aufgetretenen "Symbol nicht gefunden"-Fehler und Typinkompatibilitäten, die durch die Umstellung der Repository-Methoden und Schema-Validierungsfehler verursacht wurden, sind behoben. Die Anwendung sollte nun korrekt mit der konfigurierten MySQL-Datenbank interagieren.

**Wichtiger Hinweis zur Datenbankerstellung und Flyway-Validierung:**
*   Flyway erstellt die Datenbank selbst nicht. Die Datenbank (`defaultdb`) muss manuell existieren, bevor Flyway seine Migrationen ausführen kann.
*   Bei "Migration checksum mismatch"-Fehlern, die nach dem Ändern bereits angewendeter Migrationsskripte auftreten, muss entweder die Datenbank bereinigt und neu erstellt werden, oder der Befehl `mvn flyway:repair` ausgeführt werden, um die Prüfsummen in der `flyway_schema_history`-Tabelle der Datenbank zu aktualisieren.
