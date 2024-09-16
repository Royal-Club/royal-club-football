drop table if exists ac_charts;
drop table if exists ac_natures;
drop table if exists ac_voucher_types;

-- Create the table
CREATE TABLE IF NOT EXISTS ac_voucher_types
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                VARCHAR(30)             NOT NULL,
    alias               VARCHAR(2)              NOT NULL,
    ac_transaction_type ENUM ('CO', 'CI', 'JL') NOT NULL,
    description         VARCHAR(255),
    is_default          BOOLEAN                 NOT NULL,
    created_by          BIGINT                  NOT NULL,
    created_date        DATETIME                NOT NULL,
    last_modified_by    BIGINT,
    updated_date        DATETIME DEFAULT NULL,
    UNIQUE (name),
    UNIQUE (alias)
);

-- Insert the specified data
INSERT INTO ac_voucher_types (id, name, alias, ac_transaction_type, description, is_default,
                              created_date, updated_date, created_by)
VALUES (1, 'Debit or Payment Voucher', 'PV', 'CO', 'Used to record payments made by the club.', 1,
        '2024-09-14 10:00:53', '2024-09-14 10:00:53', 11),
       (2, 'Credit or Receipt Voucher', 'RV', 'CI', 'Used to record receipts or money received by the club.', 0,
        '2024-09-14 10:00:53', '2024-09-14 10:00:53', 11),
       (3, 'Non-Cash or Transfer Voucher', 'TV', 'JL',
        'Used for non-cash transactions, such as internal fund transfers.', 0, '2024-09-14 10:00:53',
        '2024-09-14 10:00:53', 11),
       (4, 'Supporting Voucher', 'SV', 'JL',
        'Used to attach supporting documents or record miscellaneous transactions.', 0, '2024-09-14 10:00:53',
        '2024-09-14 10:00:53', 11);

-- Create the ac_natures table
CREATE TABLE IF NOT EXISTS ac_natures
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(50)                                      NOT NULL,
    code             INT                                              NOT NULL,
    description      VARCHAR(500)                                     NULL,
    type             ENUM ('ASSET', 'LIABILITY', 'INCOME', 'EXPENSE') NOT NULL,
    sl_no            INT                                              NOT NULL,
    created_by       BIGINT                                           NOT NULL,
    created_date     DATETIME                                         NOT NULL,
    last_modified_by BIGINT,
    updated_date     DATETIME DEFAULT NULL
);

-- Insert the specified data with full four-digit integer codes and slNo as a serial number
INSERT INTO ac_natures (id, name, code, description, type, sl_no, created_date, updated_date, created_by)
VALUES (1, 'Liabilities', 1200,
        'Liabilities are obligations the club owes to external parties, such as loans, unpaid invoices, or other financial commitments.',
        'LIABILITY', 1, '2024-09-14 10:00:53', '2024-09-14 10:00:53', 11),
       (2, 'Assets', 1100,
        'Assets are resources owned by the club that are expected to provide future economic benefits, such as cash, equipment, and property.',
        'ASSET', 2, '2024-09-14 10:00:53', '2024-09-14 10:00:53', 11),
       (4, 'Income/ Revenue', 1400,
        'Income or revenue represents the inflow of financial resources resulting from the club\'s primary activities, such as membership fees and donations.',
        'INCOME', 4, '2024-09-14 10:00:53', '2024-09-14 10:00:53', 11),
       (5, 'Equity Accounts', 1300,
        'Equity accounts represent the residual interest in the club\'s assets after deducting liabilities, typically including owner’s capital and retained earnings.',
        'LIABILITY', 5, '2024-09-14 10:00:53', '2024-09-14 10:00:53', 11),
       (6, 'Operating Income', 4100,
        'Operating income is the profit generated from the club\'s core business operations, excluding any non-operating income or expenses.',
        'INCOME', 6, '2024-09-14 10:00:53', '2024-09-14 10:00:53', 11),
       (7, 'Non-Operating Income', 4200,
        'Non-operating income includes revenue generated from activities outside the club’s primary operations, such as interest, dividends, or asset sales.',
        'INCOME', 7, '2024-09-14 10:00:53', '2024-09-14 10:00:53', 11),
       (8, 'Direct Expenses', 5100,
        'Direct expenses are costs that can be directly traced to the club\'s main activities, such as player salaries, equipment costs, and match-day expenses.',
        'EXPENSE', 8, '2024-09-14 10:00:53', '2024-09-14 10:00:53', 11),
       (9, 'Indirect Expenses', 5200,
        'Indirect expenses are overhead costs that cannot be directly attributed to a specific activity but are necessary for the club\'s operations, such as utilities and administrative expenses.',
        'EXPENSE', 9, '2024-09-14 10:00:53', '2024-09-14 10:00:53', 11),
       (10, 'Taxes', 5300,
        'Taxes represent amounts owed by the club to government authorities, including income tax, VAT, and other statutory tax liabilities.',
        'EXPENSE', 10, '2024-09-14 10:00:53', '2024-09-14 10:00:53', 11);


-- Create the ac_charts table
CREATE TABLE IF NOT EXISTS ac_charts
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(250) NOT NULL UNIQUE,
    code             VARCHAR(50)  NOT NULL UNIQUE,
    description      VARCHAR(500),
    parent_no        BIGINT,
    nature_no        BIGINT,
    is_active        BOOLEAN      NOT NULL DEFAULT false,
    created_by       BIGINT       NOT NULL,
    created_date     DATETIME     NOT NULL,
    last_modified_by BIGINT,
    updated_date     DATETIME              DEFAULT NULL,
    FOREIGN KEY (parent_no) REFERENCES ac_charts (id),
    FOREIGN KEY (nature_no) REFERENCES ac_natures (id)
);

-- Insert sample data into ac_charts
-- Insert sample data into ac_charts with meaningful descriptions
INSERT INTO ac_charts (id, name, code, description, parent_no, nature_no, is_active, created_by, created_date,
                       last_modified_by, updated_date)
VALUES (1, 'Current Assets', '1100-001',
        'Assets that can reasonably be expected to be sold, consumed, or exhausted within a fiscal year or operating cycle.',
        null, 2, 1, 11, '2024-09-15 12:45:38', null, '2024-09-15 12:45:38'),
       (2, 'Rent Expense', '5100-001',
        'Expenses related to rental payments for properties or equipment.',
        null, 8, 1, 11, '2024-09-15 12:45:38', null, '2024-09-15 12:45:38'),
       (3, 'Food Expense', '5100-002',
        'Costs related to providing meals or refreshments.',
        null, 8, 1, 11, '2024-09-15 12:45:38', null, '2024-09-15 12:45:38'),
       (4, 'Cash on Hand (Rakib)', '1100-001-001',
        'Cash physically available with Rakib for daily operations.',
        1, 2, 1, 11, '2024-09-15 12:45:38', null, '2024-09-15 12:45:38'),
       (5, 'Cash on Hand (Sarower)', '1100-001-002',
        'Cash physically available with Sarower for daily operations.',
        1, 2, 1, 11, '2024-09-15 12:45:38', null, '2024-09-15 12:45:38'),
       (6, 'Equipment Expense', '5100-003',
        'Expenses related to purchasing and maintaining equipment such as sports kits.',
        null, 8, 1, 11, '2024-09-15 12:45:38', null, '2024-09-15 12:45:38'),
       (8, 'Operating Income', '4100-001',
        'Income generated from the club\'s core activities, after deducting operating expenses.',
        null, 6, 1, 11, '2024-09-15 12:45:38', null, '2024-09-15 12:45:38'),
       (9, 'Non-Operating Income', '4200-001',
        'Income from activities outside the club\'s primary operations, such as investment returns or asset sales.',
        null, 7, 1, 11, '2024-09-15 12:45:38', null, '2024-09-15 12:45:38'),
       (10, 'Monthly Membership Fees', '1400-001',
        'Revenue from membership fees collected on a monthly basis from club members.',
        null, 4, 1, 11, '2024-09-15 12:45:38', null, '2024-09-15 12:45:38'),
       (11, 'Donations', '1400-002',
        'Voluntary financial contributions made by members or supporters of the club.',
        null, 4, 1, 11, '2024-09-15 12:45:38', null, '2024-09-15 12:45:38'),
       (12, 'Sponsorship Income', '1400-003',
        'Revenue generated from sponsorship deals or partnerships with businesses and organizations.',
        null, 4, 1, 11, '2024-09-15 12:45:38', null, '2024-09-15 12:45:38'),
       (13, 'Event Fees', '1400-004',
        'Income from fees charged for events or activities organized by the club.',
        null, 4, 1, 11, '2024-09-15 12:45:38', null, '2024-09-15 12:45:38');
