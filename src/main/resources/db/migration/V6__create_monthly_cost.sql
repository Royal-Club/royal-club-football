CREATE TABLE monthly_cost
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    cost_type_id  BIGINT    NOT NULL,
    amount        DOUBLE    NOT NULL,
    month_of_cost DATE      NOT NULL,
    description   VARCHAR(255),
    created_date  TIMESTAMP NOT NULL,
    updated_date  DATETIME NULL DEFAULT NULL,
    FOREIGN KEY (cost_type_id) REFERENCES cost_types (id)
);
