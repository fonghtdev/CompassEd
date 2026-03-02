ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS transfer_note VARCHAR(255) NULL,
    ADD COLUMN IF NOT EXISTS submitted_at DATETIME NULL;

CREATE TABLE IF NOT EXISTS payment_subject_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    payment_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payment_subject_items_payment_subject (payment_id, subject_id),
    KEY idx_payment_subject_items_payment (payment_id),
    KEY idx_payment_subject_items_subject (subject_id),
    CONSTRAINT fk_payment_subject_items_payment
        FOREIGN KEY (payment_id) REFERENCES payments (id),
    CONSTRAINT fk_payment_subject_items_subject
        FOREIGN KEY (subject_id) REFERENCES subjects (id)
);
