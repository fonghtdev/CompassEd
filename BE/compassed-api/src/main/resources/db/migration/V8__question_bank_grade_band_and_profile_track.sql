ALTER TABLE question_bank
    ADD COLUMN IF NOT EXISTS grade_band VARCHAR(20) NULL DEFAULT 'GRADE_11';

UPDATE question_bank
SET grade_band = 'GRADE_11'
WHERE grade_band IS NULL OR TRIM(grade_band) = '';

ALTER TABLE user_profiles
    ADD COLUMN IF NOT EXISTS academic_track VARCHAR(20) NULL DEFAULT 'GRADE_11';

UPDATE user_profiles
SET academic_track = 'GRADE_11'
WHERE academic_track IS NULL OR TRIM(academic_track) = '';
