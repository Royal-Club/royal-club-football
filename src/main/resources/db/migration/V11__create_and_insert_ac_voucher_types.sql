-- Create the table
CREATE TABLE ac_voucher_types
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    name              VARCHAR(30)             NOT NULL,
    alias             VARCHAR(2)              NOT NULL,
    ac_transaction_type ENUM ('CO', 'CI', 'JL') NOT NULL,
    description       VARCHAR(255),
    is_default         BOOLEAN                 NOT NULL,
    created_date     DATETIME NOT NULL default CURRENT_TIMESTAMP,
    updated_date     DATETIME NOT NULL default CURRENT_TIMESTAMP,
    UNIQUE (name),
    UNIQUE (alias)
);

-- Insert the specified data
INSERT INTO ac_voucher_types (name, alias, ac_transaction_type, description, is_default, created_date, updated_date)
VALUES ('Debit or Payment Voucher', 'DV', 'CO', NULL, TRUE, now(), now()),
       ('Credit or Receipt Voucher', 'IV', 'JL', NULL, FALSE, now(), now()),
       ('Non-Cash or Transfer Voucher', 'PV', 'JL', NULL, FALSE, now(), now()),
       ('Supporting Voucher', 'CV', 'CI', NULL, FALSE, now(), now());
