CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NULL,
    password_hash VARCHAR(255) NULL,
    oauth_provider VARCHAR(50) NULL,
    oauth_provider_user_id VARCHAR(255) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email),
    UNIQUE KEY uk_users_oauth_provider_identity (oauth_provider, oauth_provider_user_id)
);

CREATE TABLE IF NOT EXISTS auth_sessions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_auth_sessions_token (token),
    KEY idx_auth_sessions_user_id (user_id),
    CONSTRAINT fk_auth_sessions_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS subjects (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_subjects_code (code)
);

CREATE TABLE IF NOT EXISTS roadmaps (
    id BIGINT NOT NULL AUTO_INCREMENT,
    subject_id BIGINT NOT NULL,
    level VARCHAR(10) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NULL,
    display_order INT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_roadmaps_subject_level (subject_id, level),
    CONSTRAINT fk_roadmaps_subject
        FOREIGN KEY (subject_id) REFERENCES subjects (id)
);

CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    active BIT(1) NOT NULL,
    activated_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_subscriptions_user_subject (user_id, subject_id),
    CONSTRAINT fk_subscriptions_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_subscriptions_subject
        FOREIGN KEY (subject_id) REFERENCES subjects (id)
);

CREATE TABLE IF NOT EXISTS user_subject_free_attempts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    used BIT(1) NOT NULL,
    used_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_subject_free_attempts_user_subject (user_id, subject_id),
    CONSTRAINT fk_user_subject_free_attempts_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_subject_free_attempts_subject
        FOREIGN KEY (subject_id) REFERENCES subjects (id)
);

CREATE TABLE IF NOT EXISTS placement_attempts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    paper_json JSON NOT NULL,
    started_at DATETIME NULL,
    submitted_at DATETIME NULL,
    PRIMARY KEY (id),
    KEY idx_placement_attempts_user_subject (user_id, subject_id),
    CONSTRAINT fk_placement_attempts_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_placement_attempts_subject
        FOREIGN KEY (subject_id) REFERENCES subjects (id)
);

CREATE TABLE IF NOT EXISTS placement_results (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    score_percent DOUBLE NULL,
    level VARCHAR(10) NULL,
    skill_analysis_json JSON NULL,
    created_at DATETIME NULL,
    PRIMARY KEY (id),
    KEY idx_placement_results_user_subject_created_at (user_id, subject_id, created_at),
    CONSTRAINT fk_placement_results_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_placement_results_subject
        FOREIGN KEY (subject_id) REFERENCES subjects (id)
);

CREATE TABLE IF NOT EXISTS user_roadmap_assignments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    roadmap_id BIGINT NOT NULL,
    assigned_at DATETIME NULL,
    phase VARCHAR(30) NOT NULL,
    replan_count INT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_roadmap_assignments_user_subject (user_id, subject_id),
    CONSTRAINT fk_user_roadmap_assignments_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_roadmap_assignments_subject
        FOREIGN KEY (subject_id) REFERENCES subjects (id),
    CONSTRAINT fk_user_roadmap_assignments_roadmap
        FOREIGN KEY (roadmap_id) REFERENCES roadmaps (id)
);

CREATE TABLE IF NOT EXISTS user_progress (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    subject VARCHAR(50) NOT NULL,
    level VARCHAR(10) NOT NULL,
    lesson_id BIGINT NOT NULL,
    completed BIT(1) NOT NULL,
    score INT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_progress_user_subject_level_lesson (user_id, subject, level, lesson_id),
    KEY idx_user_progress_user_subject_level (user_id, subject, level)
);
