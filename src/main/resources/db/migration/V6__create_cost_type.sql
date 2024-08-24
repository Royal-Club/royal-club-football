CREATE TABLE cost_types
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL UNIQUE,
    description  VARCHAR(255),
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_date TIMESTAMP    NOT NULL,
    updated_date DATETIME NULL DEFAULT NULL
);
INSERT INTO cost_types (id, name, description, is_active, created_date, updated_date)
VALUES (1, 'FIELD_RENT', 'Expenses related to field rent', true, NOW(), NOW()),
       (2, 'FOOD', 'Expenses related to food and beverages', true, NOW(), NOW()),
       (3, 'VEHICLE', 'Expenses related to vehicle maintenance', true, NOW(), NOW()),
       (4, 'EQUIPMENT', 'Expenses related to kits purchase and maintenance', true, NOW(), NOW()),
       (5, 'OTHER', 'Miscellaneous expenses that do not fall under any other category', true, NOW(), NOW());