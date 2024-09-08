-- Create the ac_charts table
CREATE TABLE IF NOT EXISTS ac_charts
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(250) NOT NULL UNIQUE,
    code         VARCHAR(50)  NOT NULL UNIQUE,
    description  VARCHAR(500),
    parent_no    BIGINT,
    nature_no    BIGINT,
    is_active    BOOLEAN      not null default false,
    created_date DATETIME     NOT NULL default current_timestamp,
    updated_date DATETIME     NOT NULL default current_timestamp,
    FOREIGN KEY (parent_no) REFERENCES ac_charts (id),
    FOREIGN KEY (nature_no) REFERENCES ac_natures (id)
);
--
-- -- Insert sample data into ac_charts
INSERT INTO ac_charts (id, name, code, description, parent_no, nature_no, is_active, created_date, updated_date)
VALUES (1, 'Current Assets', '1100-001',
        'A current asset is any asset which can reasonably be expected to be sold, consumed, or exhausted through the normal operations of a business within the current fiscal year or operating cycle.',
        null, 2, 1, now(), now());
INSERT INTO ac_charts (id, name, code, description, parent_no, nature_no, is_active, created_date, updated_date)
VALUES (2, 'Rent Expense', '5100-001', null, null, 8, 1, now(), now());
INSERT INTO ac_charts (id, name, code, description, parent_no, nature_no, is_active, created_date, updated_date)
VALUES (3, 'Food Expense', '5100-002', null, null, 8, 1, now(), now());
INSERT INTO ac_charts (id, name, code, description, parent_no, nature_no, is_active, created_date, updated_date)
VALUES (4, 'Cash on Hand (Rakib)', '1100-001-001', null, 1, 2, 1, now(), now());
INSERT INTO ac_charts (id, name, code, description, parent_no, nature_no, is_active, created_date, updated_date)
VALUES (5, 'Cash on Hand (Sarower)', '1100-001-002', null, 1, 2, 1, now(), now());
INSERT INTO ac_charts (id, name, code, description, parent_no, nature_no, is_active, created_date, updated_date)
VALUES (6, 'Equipment Expense', '5100-003', 'Expenses related to kits purchase and maintenance', null, 8, 1,
        now(), now());
INSERT INTO ac_charts (id, name, code, description, parent_no, nature_no, is_active, created_date, updated_date)
VALUES (8, 'Operating Income', '1400-001',
        'Operating income is an accounting figure that measures the amount of profit realized from a business''s operations, after deducting operating expenses such as wages, depreciation, and cost of goods sold (COGS).',
        null, 4, 1, now(), now());
INSERT INTO ac_charts (id, name, code, description, parent_no, nature_no, is_active, created_date, updated_date)
VALUES (9, 'Non - Operating Income', '1400-002', 'Non-operating income, in accounting and finance, is gains or losses from sources not related to the typical activities of the business or organization. Non-operating income can include gains or losses from investments, property or asset sales, currency exchange, and other atypical gains or losses.', null, 4, 1, now(), now());

