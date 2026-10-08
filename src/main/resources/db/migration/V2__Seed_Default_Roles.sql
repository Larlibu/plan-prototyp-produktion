-- V2__Seed_Default_Roles.sql

INSERT INTO roles (id, name) VALUES (UUID(), 'ROLE_ADMIN') ON DUPLICATE KEY UPDATE name = name;
INSERT INTO roles (id, name) VALUES (UUID(), 'ROLE_EDITOR') ON DUPLICATE KEY UPDATE name = name;
INSERT INTO roles (id, name) VALUES (UUID(), 'ROLE_USER') ON DUPLICATE KEY UPDATE name = name;
