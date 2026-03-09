CREATE TABLE IF NOT EXISTS user_ai_roadmap_states (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    roadmap_guide_json LONGTEXT NULL,
    refresh_count INT NOT NULL DEFAULT 0,
    initialized_at DATETIME NULL,
    updated_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_ai_roadmap_states_user_subject (user_id, subject_id),
    KEY idx_user_ai_roadmap_states_user_subject (user_id, subject_id),
    CONSTRAINT fk_user_ai_roadmap_states_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_ai_roadmap_states_subject
        FOREIGN KEY (subject_id) REFERENCES subjects (id)
);

