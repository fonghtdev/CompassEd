-- Phase 2 Database Migration Script
-- CompassED - Add Payment, Roadmap, Module, Test tables

-- 1. Create payments table
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'VND',
    payment_method VARCHAR(50),
    payment_gateway VARCHAR(50),
    transaction_id VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    subject_id BIGINT,
    package_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (subject_id) REFERENCES subjects(id),
    INDEX idx_user_id (user_id),
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_status (status)
);

-- 2. Create roadmap_modules table
CREATE TABLE IF NOT EXISTS roadmap_modules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    roadmap_id BIGINT NOT NULL,
    module_name VARCHAR(255) NOT NULL,
    order_index INT NOT NULL,
    content TEXT,
    video_url VARCHAR(500),
    duration_minutes INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (roadmap_id) REFERENCES roadmaps(id),
    UNIQUE KEY unique_roadmap_order (roadmap_id, order_index)
);

-- 3. Create user_module_progress table
CREATE TABLE IF NOT EXISTS user_module_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    module_id BIGINT NOT NULL,
    status VARCHAR(50) DEFAULT 'NOT_STARTED',
    progress_percent INT DEFAULT 0,
    mini_test_score INT,
    completed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (module_id) REFERENCES roadmap_modules(id),
    UNIQUE KEY unique_user_module (user_id, module_id),
    INDEX idx_user_status (user_id, status)
);

-- 4. Create mini_tests table
CREATE TABLE IF NOT EXISTS mini_tests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    module_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    questions_json TEXT NOT NULL,
    pass_threshold INT DEFAULT 70,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (module_id) REFERENCES roadmap_modules(id),
    INDEX idx_module_id (module_id)
);

-- 5. Create mini_test_attempts table
CREATE TABLE IF NOT EXISTS mini_test_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    mini_test_id BIGINT NOT NULL,
    score INT NOT NULL,
    passed BOOLEAN NOT NULL,
    answers_json TEXT,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (mini_test_id) REFERENCES mini_tests(id),
    INDEX idx_user_test (user_id, mini_test_id)
);

-- 6. Create final_tests table
CREATE TABLE IF NOT EXISTS final_tests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    roadmap_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    questions_json TEXT NOT NULL,
    pass_threshold INT DEFAULT 75,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (roadmap_id) REFERENCES roadmaps(id),
    INDEX idx_roadmap_id (roadmap_id)
);

-- 7. Create final_test_attempts table
CREATE TABLE IF NOT EXISTS final_test_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    final_test_id BIGINT NOT NULL,
    score INT NOT NULL,
    passed BOOLEAN NOT NULL,
    promoted BOOLEAN DEFAULT FALSE,
    answers_json TEXT,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (final_test_id) REFERENCES final_tests(id),
    INDEX idx_user_test (user_id, final_test_id)
);

-- ============================================
-- SEED DATA
-- ============================================

-- Insert sample roadmap modules for Mathematics L1
INSERT INTO roadmap_modules (roadmap_id, module_name, order_index, content, duration_minutes) 
SELECT id, 'Module 1: Số nguyên và phép tính cơ bản', 1, 
'# Module 1: Số nguyên và phép tính cơ bản

## Mục tiêu học tập
- Hiểu và vận dụng được các phép toán cơ bản
- Giải quyết các bài toán thực tế

## Nội dung
1. Số nguyên dương, âm, và số 0
2. Phép cộng, trừ số nguyên
3. Phép nhân, chia số nguyên
4. Thứ tự thực hiện phép tính

## Bài tập
- 20 bài tập trắc nghiệm
- 5 bài tập tự luận',
60
FROM roadmaps WHERE level = 'L1' AND subject_id = (SELECT id FROM subjects WHERE code = 'MATH')
LIMIT 1;

INSERT INTO roadmap_modules (roadmap_id, module_name, order_index, content, duration_minutes)
SELECT id, 'Module 2: Phân số và số thập phân', 2,
'# Module 2: Phân số và số thập phân

## Mục tiêu
- Hiểu khái niệm phân số và số thập phân
- Chuyển đổi giữa các dạng số

## Nội dung
1. Khái niệm phân số
2. Các phép toán với phân số
3. Số thập phân
4. Quy đổi giữa phân số và số thập phân',
75
FROM roadmaps WHERE level = 'L1' AND subject_id = (SELECT id FROM subjects WHERE code = 'MATH')
LIMIT 1;

INSERT INTO roadmap_modules (roadmap_id, module_name, order_index, content, duration_minutes)
SELECT id, 'Module 3: Hình học cơ bản', 3,
'# Module 3: Hình học cơ bản

## Mục tiêu
- Nhận biết các hình học cơ bản
- Tính chu vi, diện tích

## Nội dung
1. Điểm, đường thẳng, tia
2. Góc và các loại góc
3. Tam giác và tứ giác
4. Chu vi và diện tích',
90
FROM roadmaps WHERE level = 'L1' AND subject_id = (SELECT id FROM subjects WHERE code = 'MATH')
LIMIT 1;

-- Insert mini tests for each module
INSERT INTO mini_tests (module_id, title, questions_json, pass_threshold)
SELECT id, 'Mini Test: Số nguyên và phép tính',
'[
  {"id":1,"question":"2 + 3 = ?","options":["3","4","5","6"],"answer":"C"},
  {"id":2,"question":"10 - 7 = ?","options":["2","3","4","5"],"answer":"B"},
  {"id":3,"question":"4 × 5 = ?","options":["15","20","25","30"],"answer":"B"},
  {"id":4,"question":"20 ÷ 4 = ?","options":["4","5","6","7"],"answer":"B"},
  {"id":5,"question":"(-5) + 8 = ?","options":["3","13","-3","-13"],"answer":"A"}
]',
70
FROM roadmap_modules WHERE module_name = 'Module 1: Số nguyên và phép tính cơ bản'
LIMIT 1;

INSERT INTO mini_tests (module_id, title, questions_json, pass_threshold)
SELECT id, 'Mini Test: Phân số và số thập phân',
'[
  {"id":1,"question":"1/2 + 1/4 = ?","options":["1/4","1/2","3/4","1"],"answer":"C"},
  {"id":2,"question":"0.5 + 0.25 = ?","options":["0.25","0.5","0.75","1.0"],"answer":"C"},
  {"id":3,"question":"2/3 × 3/4 = ?","options":["1/2","5/7","6/12","2/3"],"answer":"A"},
  {"id":4,"question":"3.5 - 1.2 = ?","options":"["1.3","2.3","2.7","3.3"],"answer":"B"},
  {"id":5,"question":"1/4 = ? (thập phân)","options":["0.2","0.25","0.3","0.5"],"answer":"B"}
]',
70
FROM roadmap_modules WHERE module_name = 'Module 2: Phân số và số thập phân'
LIMIT 1;

INSERT INTO mini_tests (module_id, title, questions_json, pass_threshold)
SELECT id, 'Mini Test: Hình học cơ bản',
'[
  {"id":1,"question":"Chu vi hình vuông cạnh 4cm là?","options":["8cm","12cm","16cm","20cm"],"answer":"C"},
  {"id":2,"question":"Diện tích hình chữ nhật 5×3 là?","options":["8","15","18","20"],"answer":"B"},
  {"id":3,"question":"Góc vuông có số đo?","options":["45°","60°","90°","180°"],"answer":"C"},
  {"id":4,"question":"Tam giác có bao nhiêu cạnh?","options":["2","3","4","5"],"answer":"B"},
  {"id":5,"question":"Hình vuông có bao nhiêu góc vuông?","options":["2","3","4","5"],"answer":"C"}
]',
70
FROM roadmap_modules WHERE module_name = 'Module 3: Hình học cơ bản'
LIMIT 1;

-- Insert final test for Math L1 roadmap
INSERT INTO final_tests (roadmap_id, title, questions_json, pass_threshold)
SELECT id, 'Final Test: Toán L1 - Tổng hợp',
'[
  {"id":1,"question":"5 + 3 × 2 = ?","options":["10","16","11","13"],"answer":"C"},
  {"id":2,"question":"1/2 + 1/3 = ?","options":["2/5","5/6","3/5","1/6"],"answer":"B"},
  {"id":3,"question":"(-4) + 7 = ?","options":["3","11","-3","-11"],"answer":"A"},
  {"id":4,"question":"Chu vi hình vuông cạnh 5cm?","options":["10cm","15cm","20cm","25cm"],"answer":"C"},
  {"id":5,"question":"0.75 = ? (phân số)","options":["1/2","2/3","3/4","4/5"],"answer":"C"},
  {"id":6,"question":"10 ÷ 2 + 3 = ?","options":["6","7","8","9"],"answer":"C"},
  {"id":7,"question":"Diện tích hình vuông cạnh 4cm?","options":["8","12","16","20"],"answer":"C"},
  {"id":8,"question":"2/3 × 3/2 = ?","options":["1","2","3","4"],"answer":"A"},
  {"id":9,"question":"Tam giác có tổng 3 góc bằng?","options":["90°","180°","270°","360°"],"answer":"B"},
  {"id":10,"question":"5.5 - 2.3 = ?","options":["2.2","3.2","3.8","4.2"],"answer":"B"}
]',
75
FROM roadmaps WHERE level = 'L1' AND subject_id = (SELECT id FROM subjects WHERE code = 'MATH')
LIMIT 1;

-- Success message
SELECT 'Phase 2 migration completed successfully!' as Status;
