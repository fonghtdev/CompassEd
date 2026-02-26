ALTER TABLE users
    ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'USER';

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(40) NOT NULL,
    read_flag BIT(1) NOT NULL DEFAULT b'0',
    created_at DATETIME NULL,
    read_at DATETIME NULL,
    PRIMARY KEY (id),
    KEY idx_notifications_user_created (user_id, created_at),
    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);

ALTER TABLE notifications
    ADD COLUMN IF NOT EXISTS read_flag BIT(1) NOT NULL DEFAULT b'0';

ALTER TABLE notifications
    ADD COLUMN IF NOT EXISTS read_at DATETIME NULL;

CREATE TABLE IF NOT EXISTS system_configs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    config_key VARCHAR(120) NOT NULL,
    config_value TEXT NOT NULL,
    updated_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_system_configs_key (config_key)
);

CREATE TABLE IF NOT EXISTS ai_generation_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    task_type VARCHAR(60) NOT NULL,
    subject_code VARCHAR(60) NULL,
    input_prompt LONGTEXT NOT NULL,
    output_text LONGTEXT NOT NULL,
    review_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    review_note TEXT NULL,
    reviewed_by_user_id BIGINT NULL,
    created_at DATETIME NULL,
    reviewed_at DATETIME NULL,
    PRIMARY KEY (id),
    KEY idx_ai_generation_logs_created (created_at),
    KEY idx_ai_generation_logs_review_status (review_status)
);
