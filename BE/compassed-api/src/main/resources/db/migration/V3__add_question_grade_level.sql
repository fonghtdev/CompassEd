-- Add grade level to question bank
ALTER TABLE question_bank
    ADD COLUMN grade_level INT NOT NULL DEFAULT 10 AFTER subject_id;

CREATE INDEX idx_qb_grade_level ON question_bank (grade_level);
