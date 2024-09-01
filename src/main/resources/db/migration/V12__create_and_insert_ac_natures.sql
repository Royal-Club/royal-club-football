-- Create the ac_natures table
CREATE TABLE ac_natures
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(50)                                      NOT NULL,
    code         INT                                              NOT NULL, -- Using INTEGER to match the updated field type, full four-digit codes
    type         ENUM ('ASSET', 'LIABILITY', 'INCOME', 'EXPENSE') NOT NULL, -- Using enum values as type
    sl_no        INT                                              NOT NULL,
    created_date DATETIME                                         NOT NULL default CURRENT_TIMESTAMP,
    updated_date DATETIME                                         not null default CURRENT_TIMESTAMP
);

-- Insert the specified data with full four-digit integer codes and slNo as a serial number
INSERT INTO ac_natures (id, name, code, type, sl_no, created_date, updated_date)
VALUES (1, 'Liabilities', 1200, 'LIABILITY', 1, now(), now());
INSERT INTO ac_natures (id, name, code, type, sl_no, created_date, updated_date)
VALUES (2, 'Assets', 1100, 'ASSET', 2, now(), now());
INSERT INTO ac_natures (id, name, code, type, sl_no, created_date, updated_date)
VALUES (4, 'Income/ Revenue', 1400, 'LIABILITY', 4, now(), now());
INSERT INTO ac_natures (id, name, code, type, sl_no, created_date, updated_date)
VALUES (5, 'Equity Accounts', 1300, 'LIABILITY', 5, now(), now());
INSERT INTO ac_natures (id, name, code, type, sl_no, created_date, updated_date)
VALUES (6, 'Operating Income', 4100, 'INCOME', 6, now(), now());
INSERT INTO ac_natures (id, name, code, type, sl_no, created_date, updated_date)
VALUES (7, 'Non-Operating Income', 4200, 'INCOME', 7, now(), now());
INSERT INTO ac_natures (id, name, code, type, sl_no, created_date, updated_date)
VALUES (8, 'Direct Expenses', 5100, 'EXPENSE', 8, now(), now());
INSERT INTO ac_natures (id, name, code, type, sl_no, created_date, updated_date)
VALUES (9, 'Indirect Expenses', 5200, 'EXPENSE', 9, now(), now());
INSERT INTO ac_natures (id, name, code, type, sl_no, created_date, updated_date)
VALUES (10, 'Taxes', 5300, 'EXPENSE', 10, now(), now());
