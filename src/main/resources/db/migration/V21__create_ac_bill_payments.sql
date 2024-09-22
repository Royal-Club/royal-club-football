-- Create ac_bill_payments table
CREATE TABLE IF NOT EXISTS ac_bill_payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    amount DECIMAL(19, 2) NOT NULL,
    payment_date DATE NOT NULL,
    description VARCHAR(500),
    cost_type_id BIGINT NOT NULL,
    is_paid BOOLEAN NOT NULL DEFAULT false,
    created_by BIGINT NOT NULL,
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    updated_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cost_type FOREIGN KEY (cost_type_id) REFERENCES cost_types(id)
    );

-- Alter ac_vouchers table to add payment_id column and foreign key constraint
ALTER TABLE ac_vouchers
    ADD COLUMN payment_id BIGINT NULL,
    ADD CONSTRAINT fk_payment_id FOREIGN KEY (payment_id) REFERENCES ac_bill_payments(id);

-- Alter cost_types table to add chart_id column and set up foreign key
ALTER TABLE cost_types
    ADD COLUMN chart_id BIGINT NULL,
    ADD CONSTRAINT fk_chart_id FOREIGN KEY (chart_id) REFERENCES ac_charts(id);

-- Update cost_types table with appropriate chart_id values
UPDATE cost_types
SET chart_id = 2
WHERE id = 1;

UPDATE cost_types
SET chart_id = 3
WHERE id = 2;

UPDATE cost_types
SET chart_id = 14
WHERE id = 3;

UPDATE cost_types
SET chart_id = 6
WHERE id = 4;

UPDATE cost_types
SET chart_id = 14
WHERE id = 5;

-- Modify chart_id column in cost_types to make it NOT NULL
ALTER TABLE cost_types
    MODIFY COLUMN chart_id BIGINT NOT NULL;
