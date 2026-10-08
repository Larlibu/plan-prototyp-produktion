-- V4__Fix_Admin_Password.sql
-- Setzt das Admin-Passwort auf einen verifizierten BCrypt-Hash für 'password'.

UPDATE users
SET password = '$2a$10$b3X8uC5C.VqQTaS39O7kQu5EIWENCqvl078tnbddanRax7yP9Rk7S'
WHERE username = 'admin';
