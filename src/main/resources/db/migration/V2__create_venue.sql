CREATE TABLE venues
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    address      VARCHAR(255) NOT NULL,
    is_active    BOOLEAN      NOT NULL DEFAULT true,
    created_date TIMESTAMP    NOT NULL,
    updated_date DATETIME NULL DEFAULT NULL
);
/*Insert meta data for venues*/
INSERT INTO venues (id, name, address, is_active, created_date, updated_date)
VALUES (1, 'GreenVill', 'Madani Avenue, Satarkul, (3 km East of US Embassy, 100 Feet, Panch Khola Bus Stand, Dhaka',
        true, NOW(), NULL),
       (2, 'Chef''s Table Courtside', '100 Feet Road Madani Ave, Dhaka', true, NOW(), NULL);
