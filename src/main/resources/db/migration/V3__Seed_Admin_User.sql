-- V3__Seed_Admin_User.sql
-- Legt den Standard-Admin-Benutzer an.
-- Passwort: 'password' (BCrypt, Faktor 10) – MUSS nach dem ersten Login geändert werden.

INSERT INTO users (id, username, password, email, enabled)
VALUES (
    UUID(),
    'admin',
    '$2a$10$9ly7jP7qqmXYxd/..EQHLewmDUMs9Xc.mTHMPUPaVat0cbWIeEX2m',
    'admin@example.com',
    TRUE
)
ON DUPLICATE KEY UPDATE username = username;

INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ROLE_ADMIN'
WHERE u.username = 'admin';
