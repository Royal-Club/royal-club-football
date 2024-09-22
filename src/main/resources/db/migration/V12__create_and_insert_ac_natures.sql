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
    created_date     DATETIME                                         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    updated_date     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert the specified data with full four-digit integer codes and slNo as a serial number
INSERT INTO ac_natures (id, name, code, description, type, sl_no, created_date, updated_date, created_by)
VALUES
    (1, 'Liabilities', 1200,
     'Liabilities are obligations the club owes to external parties, such as loans, unpaid invoices, or other financial commitments.',
     'LIABILITY', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 11),
    (2, 'Assets', 1100,
     'Assets are resources owned by the club that are expected to provide future economic benefits, such as cash, equipment, and property.',
     'ASSET', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 11),
    (4, 'Income/ Revenue', 1400,
     'Income or revenue represents the inflow of financial resources resulting from the club\'s primary activities, such as membership fees and donations.',
     'INCOME', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 11),
    (5, 'Equity Accounts', 1300,
     'Equity accounts represent the residual interest in the club\'s assets after deducting liabilities, typically including owner’s capital and retained earnings.',
     'LIABILITY', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 11),
    (6, 'Operating Income', 4100,
     'Operating income is the profit generated from the club\'s core business operations, excluding any non-operating income or expenses.',
     'INCOME', 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 11),
    (7, 'Non-Operating Income', 4200,
     'Non-operating income includes revenue generated from activities outside the club’s primary operations, such as interest, dividends, or asset sales.',
     'INCOME', 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 11),
    (8, 'Direct Expenses', 5100,
     'Direct expenses are costs that can be directly traced to the club\'s main activities, such as player salaries, equipment costs, and match-day expenses.',
     'EXPENSE', 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 11),
    (9, 'Indirect Expenses', 5200,
     'Indirect expenses are overhead costs that cannot be directly attributed to a specific activity but are necessary for the club\'s operations, such as utilities and administrative expenses.',
     'EXPENSE', 9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 11),
    (10, 'Taxes', 5300,
     'Taxes represent amounts owed by the club to government authorities, including income tax, VAT, and other statutory tax liabilities.',
     'EXPENSE', 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 11);
