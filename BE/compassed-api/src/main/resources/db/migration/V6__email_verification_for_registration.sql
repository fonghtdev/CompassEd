ALTER TABLE users
    ADD COLUMN IF NOT EXISTS email_verified BIT(1) NOT NULL DEFAULT b'0';

UPDATE users
SET email_verified = b'1'
WHERE email_verified = b'0';

CREATE TABLE IF NOT EXISTS registration_verifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NULL,
    password_hash VARCHAR(255) NOT NULL,
    verification_code VARCHAR(8) NOT NULL,
    expires_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_registration_verifications_email (email)
);
