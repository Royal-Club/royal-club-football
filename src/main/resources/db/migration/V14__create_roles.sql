CREATE TABLE roles
(
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);


INSERT INTO roles (name)
VALUES ('ADMIN'),
       ('PLAYER'),
       ('COACH'),
       ('MANAGER'),
       ('SPECTATOR');
