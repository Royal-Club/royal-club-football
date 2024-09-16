-- Create AcVoucher table
CREATE TABLE IF NOT EXISTS ac_vouchers (
                                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           code VARCHAR(20) NOT NULL UNIQUE,
    narration VARCHAR(500),
    voucher_date DATE NOT NULL,
    amount DECIMAL(19, 2) NOT NULL, -- Changed to DECIMAL for better precision
    post_flag BOOLEAN NOT NULL,
    posted_by BIGINT,
    post_date DATE,
    voucher_type_id BIGINT NOT NULL,
    collection_id BIGINT NULL,
    created_by       BIGINT   NOT NULL,
    created_date     DATETIME NOT NULL,
    last_modified_by BIGINT,
    updated_date     DATETIME DEFAULT NULL,
    CONSTRAINT fk_voucher_type FOREIGN KEY (voucher_type_id) REFERENCES ac_voucher_types(id),
    CONSTRAINT fk_collection_id FOREIGN KEY (collection_id) REFERENCES ac_collections(id),
    CONSTRAINT fk_posted_by FOREIGN KEY (posted_by) REFERENCES players(id)
    );

-- Create AcVoucherDetail table
CREATE TABLE IF NOT EXISTS ac_voucher_details (
                                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                  narration VARCHAR(500),
    dr DECIMAL(19, 2) NULL,
    cr DECIMAL(19, 2) NULL,
    ac_chart_id BIGINT not null,
    voucher_id BIGINT NOT NULL,
    reference_no VARCHAR(100),
    created_by       BIGINT   NOT NULL,
    created_date     DATETIME NOT NULL,
    last_modified_by BIGINT,
    updated_date     DATETIME DEFAULT NULL,
    CONSTRAINT fk_ac_chart FOREIGN KEY (ac_chart_id) REFERENCES ac_charts(id),
    CONSTRAINT fk_voucher FOREIGN KEY (voucher_id) REFERENCES ac_vouchers(id)
    );
