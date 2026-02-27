ALTER TABLE users
    ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'USER';

CREATE TABLE IF NOT EXISTS app_roles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(30) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_app_roles_name (name)
);

INSERT IGNORE INTO app_roles (name) VALUES ('USER');
INSERT IGNORE INTO app_roles (name) VALUES ('ADMIN');

CREATE TABLE IF NOT EXISTS user_role_assignments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role_assignments_user (user_id),
    KEY idx_user_role_assignments_role (role_id),
    CONSTRAINT fk_user_role_assignments_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_role_assignments_role
        FOREIGN KEY (role_id) REFERENCES app_roles (id)
);

INSERT INTO user_role_assignments (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN app_roles r ON r.name = COALESCE(NULLIF(TRIM(u.role), ''), 'USER')
LEFT JOIN user_role_assignments ura ON ura.user_id = u.id
WHERE ura.user_id IS NULL;
