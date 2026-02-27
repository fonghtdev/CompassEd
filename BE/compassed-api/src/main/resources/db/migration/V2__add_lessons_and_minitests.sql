CREATE TABLE IF NOT EXISTS lessons (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    content TEXT NULL,
    subject VARCHAR(50) NOT NULL,
    level VARCHAR(10) NOT NULL,
    order_index INT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_lessons_subject_level_order (subject, level, order_index)
);

CREATE TABLE IF NOT EXISTS mini_tests (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    questions TEXT NULL,
    subject VARCHAR(50) NOT NULL,
    level VARCHAR(10) NOT NULL,
    lesson_id INT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_mini_tests_subject_level_lesson (subject, level, lesson_id)
);
