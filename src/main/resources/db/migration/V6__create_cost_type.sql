CREATE TABLE cost_types
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(100) NOT NULL UNIQUE,
    description      VARCHAR(255),
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by       BIGINT       NOT NULL,
    created_date     TIMESTAMP    NOT NULL,
    last_modified_by BIGINT,
    updated_date     TIMESTAMP NULL DEFAULT NULL
);

INSERT INTO cost_types (id, name, description, is_active, created_by, created_date, last_modified_by, updated_date)
VALUES (1, 'FIELD_RENT', 'Expenses related to field rent', true, 1, NOW(), 1, NOW()),
       (2, 'FOOD', 'Expenses related to food and beverages', true, 1, NOW(), 1, NOW()),
       (3, 'VEHICLE', 'Expenses related to vehicle maintenance', true, 1, NOW(), 1, NOW()),
       (4, 'EQUIPMENT', 'Expenses related to kits purchase and maintenance', true, 1, NOW(), 1, NOW()),
       (5, 'OTHER', 'Miscellaneous expenses that do not fall under any other category', true, 1, NOW(), 1, NOW());
