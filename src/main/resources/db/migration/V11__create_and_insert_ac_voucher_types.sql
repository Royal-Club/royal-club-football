-- Create the ac_voucher_types table
CREATE TABLE IF NOT EXISTS ac_voucher_types
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                VARCHAR(30)             NOT NULL,
    alias               VARCHAR(2)              NOT NULL,
    ac_transaction_type ENUM ('CO', 'CI', 'JL') NOT NULL,
    description         VARCHAR(255),
    is_default          BOOLEAN                 NOT NULL,
    created_by          BIGINT                  NOT NULL,
    created_date        DATETIME                NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by    BIGINT,
    updated_date        DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE (name),
    UNIQUE (alias)
);

-- Insert the specified data
INSERT INTO ac_voucher_types (id, name, alias, ac_transaction_type, description, is_default, created_date, updated_date, created_by)
VALUES
    (1, 'Debit or Payment Voucher', 'PV', 'CO', 'Used to record payments made by the club.', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 11),
    (2, 'Credit or Receipt Voucher', 'RV', 'CI', 'Used to record receipts or money received by the club.', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 11),
    (3, 'Non-Cash or Transfer Voucher', 'TV', 'JL', 'Used for non-cash transactions, such as internal fund transfers.', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 11),
    (4, 'Supporting Voucher', 'SV', 'JL', 'Used to attach supporting documents or record miscellaneous transactions.', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 11);
