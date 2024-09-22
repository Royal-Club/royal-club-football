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
    created_date     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    updated_date     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_no) REFERENCES ac_charts (id),
    FOREIGN KEY (nature_no) REFERENCES ac_natures (id)
    );

-- Insert sample data into ac_charts
INSERT INTO ac_charts
(id, name, code, description, parent_no, nature_no, is_active, created_by, created_date, last_modified_by, updated_date)
VALUES
    (1, "Current Assets", "1100-001",
     "Assets that can reasonably be expected to be sold, consumed, or exhausted within a fiscal year or operating cycle.",
     NULL, 2, 1, 11, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP),

    (2, "Rent Expense", "5100-001",
     "Expenses related to rental payments for properties or equipment.",
     NULL, 8, 1, 11, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP),

    (3, "Food Expense", "5100-002",
     "Costs related to providing meals or refreshments.",
     NULL, 8, 1, 11, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP),

    (4, "Cash on Hand (Rakib)", "1100-001-001",
     "Cash physically available with Rakib for daily operations.",
     1, 2, 1, 11, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP),

    (5, "Cash on Hand (Sarower)", "1100-001-002",
     "Cash physically available with Sarower for daily operations.",
     1, 2, 1, 11, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP),

    (6, "Equipment Expense", "5100-003",
     "Expenses related to purchasing and maintaining equipment such as sports kits.",
     NULL, 8, 1, 11, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP),

    (8, "Operating Income", "4100-001",
     "Income generated from the club's core activities, after deducting operating expenses.",
     NULL, 6, 1, 11, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP),

    (9, "Non-Operating Income", "4200-001",
     "Income from activities outside the club's primary operations, such as investment returns or asset sales.",
     NULL, 7, 1, 11, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP),

    (10, "Monthly Membership Fees", "1400-001",
     "Revenue from membership fees collected on a monthly basis from club members.",
     NULL, 4, 1, 11, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP),

    (11, "Donations", "1400-002",
     "Voluntary financial contributions made by members or supporters of the club.",
     NULL, 4, 1, 11, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP),

    (12, "Sponsorship Income", "1400-003",
     "Revenue generated from sponsorship deals or partnerships with businesses and organizations.",
     NULL, 4, 1, 11, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP),

    (13, "Event Fees", "1400-004",
     "Income from fees charged for events or activities organized by the club.",
     NULL, 4, 1, 11, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP),

    (14, "Other Expenses", "5100-004",
     "Expenses related to miscellaneous or all other expenses incurred by the club.",
     NULL, 8, 1, 11, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP);
