CREATE TABLE IF NOT EXISTS user_profiles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    learning_goal TEXT NULL,
    target_score INT NULL,
    notify_email BIT(1) NOT NULL DEFAULT b'0',
    notify_in_app BIT(1) NOT NULL DEFAULT b'1',
    updated_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_profiles_user (user_id),
    CONSTRAINT fk_user_profiles_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);
