-- ================================================
-- CompassED Database Migration
-- Phase 1: Complete Database Schema
-- ================================================

-- 1. QUESTION BANK (Core - Quan trọng nhất!)
CREATE TABLE IF NOT EXISTS question_bank (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    subject_id BIGINT NOT NULL,
    grade_level INT NOT NULL DEFAULT 10 COMMENT 'Khối lớp 10/11/12',
    level ENUM('L1', 'L2', 'L3') NOT NULL,
    skill_type VARCHAR(100) NOT NULL COMMENT 'Đại số, Hình học, Ngữ pháp, Reading...',
    question_type ENUM('MULTIPLE_CHOICE', 'ESSAY', 'TRUE_FALSE') DEFAULT 'MULTIPLE_CHOICE',
    question_text TEXT NOT NULL,
    options JSON COMMENT '["A. ...", "B. ...", "C. ...", "D. ..."]',
    correct_answer VARCHAR(10) COMMENT 'A hoặc B,C nếu nhiều đáp án',
    explanation TEXT,
    difficulty INT DEFAULT 1 COMMENT '1-5',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    INDEX idx_qb_subject_level (subject_id, level),
    INDEX idx_qb_skill (skill_type),
    INDEX idx_qb_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. USER SUBJECTS (Tracking học viên)
CREATE TABLE IF NOT EXISTS user_subjects (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    current_level ENUM('L1', 'L2', 'L3') DEFAULT 'L1',
    is_unlocked BOOLEAN DEFAULT FALSE,
    free_placement_remaining INT DEFAULT 1,
    placement_completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_subject (user_id, subject_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    INDEX idx_us_user (user_id),
    INDEX idx_us_subject (subject_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. ROADMAP MODULES (Chi tiết module trong roadmap)
CREATE TABLE IF NOT EXISTS roadmap_modules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    roadmap_id BIGINT NOT NULL,
    module_name VARCHAR(255) NOT NULL,
    description TEXT,
    order_index INT NOT NULL,
    content_type ENUM('VIDEO', 'DOCUMENT', 'QUIZ', 'EXERCISE') DEFAULT 'VIDEO',
    content_url VARCHAR(500),
    duration_minutes INT COMMENT 'Thời lượng học',
    is_required BOOLEAN DEFAULT TRUE,
    pass_score INT DEFAULT 60 COMMENT 'Điểm tối thiểu để qua',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (roadmap_id) REFERENCES roadmaps(id) ON DELETE CASCADE,
    INDEX idx_rm_roadmap (roadmap_id),
    INDEX idx_rm_order (order_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. USER MODULE PROGRESS (Tiến độ học)
CREATE TABLE IF NOT EXISTS user_module_progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    module_id BIGINT NOT NULL,
    status ENUM('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED') DEFAULT 'NOT_STARTED',
    score INT,
    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_module (user_id, module_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (module_id) REFERENCES roadmap_modules(id) ON DELETE CASCADE,
    INDEX idx_ump_user (user_id),
    INDEX idx_ump_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. MINI TEST QUESTIONS (Link mini test với câu hỏi)
CREATE TABLE IF NOT EXISTS mini_test_questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    mini_test_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    order_index INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (mini_test_id) REFERENCES mini_tests(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES question_bank(id) ON DELETE CASCADE,
    INDEX idx_mtq_minitest (mini_test_id),
    INDEX idx_mtq_order (order_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. MINI TEST ATTEMPTS (Lịch sử làm mini test)
CREATE TABLE IF NOT EXISTS mini_test_attempts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    mini_test_id BIGINT NOT NULL,
    score DOUBLE,
    max_score DOUBLE,
    passed BOOLEAN DEFAULT FALSE,
    answers JSON COMMENT '{"1": "A", "2": "B", ...}',
    started_at TIMESTAMP NULL,
    submitted_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (mini_test_id) REFERENCES mini_tests(id) ON DELETE CASCADE,
    INDEX idx_mta_user (user_id),
    INDEX idx_mta_minitest (mini_test_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. SUBSCRIPTIONS (Gói học)
CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    package_id BIGINT,
    payment_id BIGINT,
    start_date DATETIME,
    end_date DATETIME,
    is_active BOOLEAN DEFAULT TRUE,
    placement_unlocked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    UNIQUE KEY uk_subscriptions_user_subject (user_id, subject_id),
    INDEX idx_sub_user (user_id),
    INDEX idx_sub_subject (subject_id),
    INDEX idx_sub_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. PAYMENTS (Lịch sử thanh toán)
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    subscription_id BIGINT,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'VND',
    payment_method VARCHAR(50) COMMENT 'VNPAY, MOMO, BANK_TRANSFER',
    transaction_id VARCHAR(255),
    status ENUM('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED') DEFAULT 'PENDING',
    payment_date TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE SET NULL,
    INDEX idx_payment_user (user_id),
    INDEX idx_payment_status (status),
    INDEX idx_payment_transaction (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. SUBJECT CONFIG (Cấu hình môn học)
CREATE TABLE IF NOT EXISTS subject_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    subject_id BIGINT NOT NULL UNIQUE,
    free_placement_count INT DEFAULT 1,
    placement_price DECIMAL(10,2) DEFAULT 99000,
    subscription_price DECIMAL(10,2) DEFAULT 299000,
    subscription_duration_days INT DEFAULT 365,
    placement_question_count INT DEFAULT 20,
    placement_pass_score INT DEFAULT 60,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. SKILL ANALYSIS (Phân tích kỹ năng chi tiết)
CREATE TABLE IF NOT EXISTS skill_analysis (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    placement_result_id BIGINT,
    skill_name VARCHAR(100) NOT NULL,
    total_questions INT NOT NULL,
    correct_answers INT NOT NULL,
    score_percent DOUBLE,
    level_recommendation ENUM('L1', 'L2', 'L3'),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (placement_result_id) REFERENCES placement_results(id) ON DELETE CASCADE,
    INDEX idx_sa_result (placement_result_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. ADMIN LOGS (Log hoạt động admin)
CREATE TABLE IF NOT EXISTS admin_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT NOT NULL,
    action VARCHAR(100) NOT NULL COMMENT 'CREATE_QUESTION, UPDATE_ROADMAP',
    target_type VARCHAR(50) COMMENT 'QUESTION, USER, SUBSCRIPTION',
    target_id BIGINT,
    description TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_al_admin (admin_id),
    INDEX idx_al_action (action),
    INDEX idx_al_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- SEED DATA
-- ================================================

-- Insert Subject Config
INSERT INTO subject_config (subject_id, free_placement_count, placement_price, subscription_price, placement_question_count)
SELECT id, 1, 99000, 299000, 20
FROM subjects
WHERE NOT EXISTS (SELECT 1 FROM subject_config WHERE subject_id = subjects.id);

-- Insert sample questions for MATH (Toán) - Level L1
INSERT INTO question_bank (subject_id, level, skill_type, question_type, question_text, options, correct_answer, explanation, difficulty) VALUES
(1, 'L1', 'Đại số', 'MULTIPLE_CHOICE', 'Tính: 2 + 3 = ?', '["A. 4", "B. 5", "C. 6", "D. 7"]', 'B', '2 cộng 3 bằng 5', 1),
(1, 'L1', 'Đại số', 'MULTIPLE_CHOICE', 'Tính: 5 - 2 = ?', '["A. 2", "B. 3", "C. 4", "D. 5"]', 'B', '5 trừ 2 bằng 3', 1),
(1, 'L1', 'Đại số', 'MULTIPLE_CHOICE', 'Tính: 3 × 4 = ?', '["A. 7", "B. 10", "C. 12", "D. 15"]', 'C', '3 nhân 4 bằng 12', 1),
(1, 'L1', 'Đại số', 'MULTIPLE_CHOICE', 'Tính: 10 ÷ 2 = ?', '["A. 3", "B. 4", "C. 5", "D. 6"]', 'C', '10 chia 2 bằng 5', 1),
(1, 'L1', 'Hình học', 'MULTIPLE_CHOICE', 'Hình vuông có mấy cạnh?', '["A. 3", "B. 4", "C. 5", "D. 6"]', 'B', 'Hình vuông có 4 cạnh', 1),
(1, 'L1', 'Hình học', 'MULTIPLE_CHOICE', 'Hình tròn có mấy đỉnh?', '["A. 0", "B. 1", "C. 2", "D. 3"]', 'A', 'Hình tròn không có đỉnh', 1),
(1, 'L1', 'Hình học', 'MULTIPLE_CHOICE', 'Tính chu vi hình vuông cạnh 5cm?', '["A. 15cm", "B. 20cm", "C. 25cm", "D. 30cm"]', 'B', 'Chu vi = 4 × cạnh = 4 × 5 = 20cm', 2),
(1, 'L1', 'Hình học', 'MULTIPLE_CHOICE', 'Diện tích hình chữ nhật dài 6cm, rộng 3cm?', '["A. 9cm²", "B. 12cm²", "C. 18cm²", "D. 24cm²"]', 'C', 'Diện tích = dài × rộng = 6 × 3 = 18cm²', 2);

-- Insert sample questions for LITERATURE (Văn) - Level L1
INSERT INTO question_bank (subject_id, level, skill_type, question_type, question_text, options, correct_answer, explanation, difficulty) VALUES
(2, 'L1', 'Ngữ pháp', 'MULTIPLE_CHOICE', 'Từ nào là động từ?', '["A. Đẹp", "B. Chạy", "C. Nhà", "D. Xanh"]', 'B', '"Chạy" là động từ chỉ hành động', 1),
(2, 'L1', 'Ngữ pháp', 'MULTIPLE_CHOICE', 'Từ nào là danh từ?', '["A. Học", "B. Đi", "C. Sách", "D. Nhanh"]', 'C', '"Sách" là danh từ chỉ vật', 1),
(2, 'L1', 'Ngữ pháp', 'MULTIPLE_CHOICE', 'Câu nào viết đúng?', '["A. em học bài", "B. Em học bài", "C. em Học bài", "D. Em Học Bài"]', 'B', 'Viết hoa chữ cái đầu câu', 1),
(2, 'L1', 'Đọc hiểu', 'MULTIPLE_CHOICE', 'Trong câu "Con chó đang chạy", chủ ngữ là gì?', '["A. Con", "B. Chó", "C. Con chó", "D. Đang chạy"]', 'C', '"Con chó" là chủ ngữ của câu', 2);

-- Insert sample questions for ENGLISH (Anh) - Level L1
INSERT INTO question_bank (subject_id, level, skill_type, question_type, question_text, options, correct_answer, explanation, difficulty) VALUES
(3, 'L1', 'Grammar', 'MULTIPLE_CHOICE', 'Choose the correct answer: I ___ a student.', '["A. am", "B. is", "C. are", "D. be"]', 'A', '"I" goes with "am"', 1),
(3, 'L1', 'Grammar', 'MULTIPLE_CHOICE', 'Choose the correct answer: She ___ to school.', '["A. go", "B. goes", "C. going", "D. gone"]', 'B', '"She" is third person singular, use "goes"', 1),
(3, 'L1', 'Vocabulary', 'MULTIPLE_CHOICE', 'What is the opposite of "hot"?', '["A. warm", "B. cold", "C. cool", "D. heat"]', 'B', '"Cold" is the opposite of "hot"', 1),
(3, 'L1', 'Reading', 'MULTIPLE_CHOICE', '"The cat is on the table." Where is the cat?', '["A. Under the table", "B. On the table", "C. Near the table", "D. Behind the table"]', 'B', 'The cat is "on" the table', 1);

-- Add more Level L2 and L3 questions
INSERT INTO question_bank (subject_id, level, skill_type, question_type, question_text, options, correct_answer, explanation, difficulty) VALUES
-- MATH L2
(1, 'L2', 'Đại số', 'MULTIPLE_CHOICE', 'Giải phương trình: 2x + 5 = 11', '["A. x = 2", "B. x = 3", "C. x = 4", "D. x = 5"]', 'B', '2x = 11 - 5 = 6, x = 3', 3),
(1, 'L2', 'Hình học', 'MULTIPLE_CHOICE', 'Diện tích hình tròn bán kính 5cm? (π ≈ 3.14)', '["A. 15.7cm²", "B. 31.4cm²", "C. 78.5cm²", "D. 157cm²"]', 'C', 'S = πr² = 3.14 × 5² = 78.5cm²', 3),
-- MATH L3
(1, 'L3', 'Đại số', 'MULTIPLE_CHOICE', 'Giải hệ phương trình: x + y = 5, x - y = 1', '["A. x=2,y=3", "B. x=3,y=2", "C. x=4,y=1", "D. x=1,y=4"]', 'B', 'Cộng 2 phương trình: 2x = 6, x = 3, y = 2', 4),
(1, 'L3', 'Hình học', 'MULTIPLE_CHOICE', 'Thể tích hình cầu bán kính 3cm? (π ≈ 3.14)', '["A. 28.26cm³", "B. 56.52cm³", "C. 113.04cm³", "D. 226.08cm³"]', 'C', 'V = (4/3)πr³ = (4/3) × 3.14 × 27 ≈ 113.04cm³', 5);

-- ================================================
-- VERIFICATION QUERIES
-- ================================================

-- Check table creation
SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    CREATE_TIME
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'compassed_db'
    AND TABLE_NAME IN (
        'question_bank',
        'user_subjects',
        'roadmap_modules',
        'user_module_progress',
        'mini_test_questions',
        'mini_test_attempts',
        'payments',
        'subject_config',
        'skill_analysis',
        'admin_logs'
    )
ORDER BY TABLE_NAME;

-- Check question bank data
SELECT 
    s.name as subject,
    qb.level,
    qb.skill_type,
    COUNT(*) as question_count
FROM question_bank qb
JOIN subjects s ON qb.subject_id = s.id
GROUP BY s.name, qb.level, qb.skill_type
ORDER BY s.name, qb.level, qb.skill_type;
