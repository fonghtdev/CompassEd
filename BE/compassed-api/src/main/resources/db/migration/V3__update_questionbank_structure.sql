-- Update QuestionBank table structure to new format
-- Migrate data from old structure to new structure

-- Step 1: Add new columns
ALTER TABLE question_bank
ADD COLUMN question_id VARCHAR(50),
ADD COLUMN subject_code VARCHAR(10),
ADD COLUMN skill_tag VARCHAR(200),
ADD COLUMN option_a TEXT,
ADD COLUMN option_b TEXT,
ADD COLUMN option_c TEXT,
ADD COLUMN option_d TEXT,
ADD COLUMN class_name VARCHAR(50);

-- Step 2: Migrate existing data to new columns
-- Convert grade_level to class_name (e.g., 11 -> "lớp 11", 12 -> "lớp 12")
UPDATE question_bank
SET class_name = CONCAT('lớp ', grade_level)
WHERE grade_level IS NOT NULL;

-- Copy skill_type to skill_tag
UPDATE question_bank
SET skill_tag = skill_type
WHERE skill_type IS NOT NULL;

-- Extract options from JSON array to separate columns
-- This assumes options is a JSON array like ["A. text", "B. text", "C. text", "D. text"]
-- Parse JSON and extract each option
UPDATE question_bank
SET 
    option_a = JSON_UNQUOTE(JSON_EXTRACT(options, '$[0]')),
    option_b = JSON_UNQUOTE(JSON_EXTRACT(options, '$[1]')),
    option_c = JSON_UNQUOTE(JSON_EXTRACT(options, '$[2]')),
    option_d = JSON_UNQUOTE(JSON_EXTRACT(options, '$[3]'))
WHERE options IS NOT NULL AND JSON_VALID(options);

-- Generate question_id from subject and level
-- Format: SubjectName_Level_id (e.g., "Literature_L1_1")
UPDATE question_bank qb
INNER JOIN subject s ON qb.subject_id = s.id
SET qb.question_id = CONCAT(
    REPLACE(s.name, ' ', '_'),
    '_',
    qb.level,
    '_',
    qb.id
);

-- Set subject_code based on subject name
-- L = Literature/Ngữ văn, M = Math/Toán, E = English/Tiếng Anh
UPDATE question_bank qb
INNER JOIN subject s ON qb.subject_id = s.id
SET qb.subject_code = CASE
    WHEN s.name LIKE '%Văn%' OR s.name LIKE '%Literature%' THEN 'L'
    WHEN s.name LIKE '%Toán%' OR s.name LIKE '%Math%' THEN 'M'
    WHEN s.name LIKE '%Anh%' OR s.name LIKE '%English%' THEN 'E'
    ELSE 'O'
END;

-- Step 3: Modify correct_answer column (already should be A, B, C, D format)
ALTER TABLE question_bank MODIFY COLUMN correct_answer VARCHAR(10);

-- Step 4: Drop old columns
ALTER TABLE question_bank
DROP COLUMN grade_level,
DROP COLUMN grade_band,
DROP COLUMN skill_type,
DROP COLUMN question_type,
DROP COLUMN options,
DROP COLUMN explanation,
DROP COLUMN difficulty;

-- Step 5: Add constraints
ALTER TABLE question_bank
ADD CONSTRAINT uk_question_id UNIQUE (question_id);

-- Step 6: Make new columns NOT NULL where appropriate
ALTER TABLE question_bank
MODIFY COLUMN subject_code VARCHAR(10) NOT NULL,
MODIFY COLUMN skill_tag VARCHAR(200) NOT NULL;

