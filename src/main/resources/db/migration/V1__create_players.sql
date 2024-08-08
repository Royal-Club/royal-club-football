CREATE TABLE players
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    email        VARCHAR(255) NOT NULL UNIQUE,
    employee_id  VARCHAR(255) NOT NULL UNIQUE,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_date DATETIME     NOT NULL,
    updated_date DATETIME NULL DEFAULT NULL,
    CONSTRAINT UC_Player UNIQUE (email, employee_id)
);