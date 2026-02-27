CREATE TABLE IF NOT EXISTS user_login_activities (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    login_date DATE NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_login_activities_user_date (user_id, login_date),
    KEY idx_user_login_activities_user (user_id),
    CONSTRAINT fk_user_login_activities_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);
