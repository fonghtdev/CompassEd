-- Script thêm câu hỏi mẫu cho Placement Test
-- Môn Toán (subject_id = 1), Lớp 10, Level L1-L3

-- Xóa dữ liệu cũ (tùy chọn)
-- DELETE FROM question_bank;

-- Thêm câu hỏi Level 1 (dễ) - 25 câu
INSERT INTO question_bank (subject_id, level, class_name, skill_tag, skill_type, question_text, option_a, option_b, option_c, option_d, correct_answer, grade_level, is_active, created_at, updated_at, subject_code, question_id) VALUES
(1, 'L1', 'lớp 10', 'Phép tính cơ bản', 'phep-tinh', '5 × 5 = ?', '15', '20', '25', '30', 'C', 10, 1, NOW(), NOW(), 'M', 'Math_L1_10.1'),
(1, 'L1', 'lớp 10', 'Phép tính cơ bản', 'phep-tinh', '10 / 10 = ?', '0', '1', '10', '100', 'B', 10, 1, NOW(), NOW(), 'M', 'Math_L1_10.2'),
(1, 'L1', 'lớp 10', 'Phép tính cơ bản', 'phep-tinh', '15 + 5 = ?', '10', '15', '20', '25', 'C', 10, 1, NOW(), NOW(), 'M', 'Math_L1_10.3'),
(1, 'L1', 'lớp 10', 'Phép tính cơ bản', 'phep-tinh', '20 - 8 = ?', '8', '10', '12', '14', 'C', 10, 1, NOW(), NOW(), 'M', 'Math_L1_10.4'),
(1, 'L1', 'lớp 10', 'Phép tính cơ bản', 'phep-tinh', '3 × 7 = ?', '18', '21', '24', '27', 'B', 10, 1, NOW(), NOW(), 'M', 'Math_L1_10.5'),
(1, 'L1', 'lớp 10', 'Phép tính cơ bản', 'phep-tinh', '100 / 5 = ?', '15', '20', '25', '30', 'B', 10, 1, NOW(), NOW(), 'M', 'Math_L1_10.6'),
(1, 'L1', 'lớp 10', 'Phép tính cơ bản', 'phep-tinh', '8 + 12 = ?', '18', '20', '22', '24', 'B', 10, 1, NOW(), NOW(), 'M', 'Math_L1_10.7'),
(1, 'L1', 'lớp 10', 'Phép tính cơ bản', 'phep-tinh', '50 - 15 = ?', '30', '35', '40', '45', 'B', 10, 1, NOW(), NOW(), 'M', 'Math_L1_10.8'),
(1, 'L1', 'lớp 10', 'Phép tính cơ bản', 'phep-tinh', '6 × 4 = ?', '20', '22', '24', '26', 'C', 10, 1, NOW(), NOW(), 'M', 'Math_L1_10.9'),
(1, 'L1', 'lớp 10', 'Phép tính cơ bản', 'phep-tinh', '45 / 9 = ?', '4', '5', '6', '7', 'B', 10, 1, NOW(), NOW(), 'M', 'Math_L1_10.10'),
(1, 'L1', 'lớp 10', 'Phép tính cơ bản', 'phep-tinh', '11 + 9 = ?', '18', '19', '20', '21', 'C', 10, 1, NOW(), NOW(), 'M', 'Math_L1_10.11'),
(1, 'L1', 'lớp 10', 'Phép tính cơ bản', 'phep-tinh', '30 - 12 = ?', '16', '18', '20', '22', 'B', 10, 1, NOW(), NOW(), 'M', 'Math_L1_10.12'),
(1, 'L1', 'lớp 10', 'Phép tính cơ bản', 'phep-tinh', '9 × 3 = ?', '24', '27', '30', '33', 'B', 10, 1, NOW(), NOW(), 'M', 'Math_L1_10.13'),
(1, 'L1', 'lớp 10', 'Phép tính cơ bản', 'phep-tinh', '64 / 8 = ?', '6', '7', '8', '9', 'C', 10, 1, NOW(), NOW(), 'M', 'Math_L1_10.14'),
(1, 'L1', 'lớp 10', 'Phép tính cơ bản', 'phep-tinh', '25 + 25 = ?', '45', '50', '55', '60', 'B', 10, 1, NOW(), NOW(), 'M', 'Math_L1_10.15'),
(1, 'L1', 'lớp 10', 'Phép tính cơ bản', 'phep-tinh', '40 - 18 = ?', '20', '22', '24', '26', 'B', 10, 1, NOW(), NOW(), 'M', 'Math_L1_10.16'),
(1, 'L1', 'lớp 10', 'Phép tính cơ bản', 'phep-tinh', '7 × 8 = ?', '54', '56', '58', '60', 'B', 10, 1, NOW(), NOW(), 'M', 'Math_L1_10.17'),
(1, 'L1', 'lớp 10', 'Phép tính cơ bản', 'phep-tinh', '81 / 9 = ?', '7', '8', '9', '10', 'C', 10, 1, NOW(), NOW(), 'M', 'Math_L1_10.18'),
(1, 'L1', 'lớp 10', 'Phép tính cơ bản', 'phep-tinh', '33 + 17 = ?', '48', '50', '52', '54', 'B', 10, 1, NOW(), NOW(), 'M', 'Math_L1_10.19'),
(1, 'L1', 'lớp 10', 'Phép tính cơ bản', 'phep-tinh', '55 - 23 = ?', '30', '32', '34', '36', 'B', 10, 1, NOW(), NOW(), 'M', 'Math_L1_10.20'),
(1, 'L1', 'lớp 10', 'Phép tính cơ bản', 'phep-tinh', '12 × 5 = ?', '50', '55', '60', '65', 'C', 10, 1, NOW(), NOW(), 'M', 'Math_L1_10.21'),
(1, 'L1', 'lớp 10', 'Phép tính cơ bản', 'phep-tinh', '72 / 6 = ?', '10', '11', '12', '13', 'C', 10, 1, NOW(), NOW(), 'M', 'Math_L1_10.22'),
(1, 'L1', 'lớp 10', 'Phép tính cơ bản', 'phep-tinh', '28 + 14 = ?', '40', '42', '44', '46', 'B', 10, 1, NOW(), NOW(), 'M', 'Math_L1_10.23'),
(1, 'L1', 'lớp 10', 'Phép tính cơ bản', 'phep-tinh', '90 - 35 = ?', '50', '55', '60', '65', 'B', 10, 1, NOW(), NOW(), 'M', 'Math_L1_10.24'),
(1, 'L1', 'lớp 10', 'Phép tính cơ bản', 'phep-tinh', '15 × 4 = ?', '55', '60', '65', '70', 'B', 10, 1, NOW(), NOW(), 'M', 'Math_L1_10.25');

-- Thêm câu hỏi Level 2 (trung bình) - 20 câu
INSERT INTO question_bank (subject_id, level, class_name, skill_tag, skill_type, question_text, option_a, option_b, option_c, option_d, correct_answer, grade_level, is_active, created_at, updated_at, subject_code, question_id) VALUES
(1, 'L2', 'lớp 10', 'Đại số', 'Giải phương trình: 2x + 5 = 15. Tìm x?', 'x = 3', 'x = 5', 'x = 7', 'x = 10', 'B', 1, NOW(), NOW()),
(1, 'L2', 'lớp 10', 'Đại số', 'Giải phương trình: 3x - 6 = 9. Tìm x?', 'x = 3', 'x = 5', 'x = 7', 'x = 9', 'B', 1, NOW(), NOW()),
(1, 'L2', 'lớp 10', 'Đại số', 'Giải phương trình: x/2 + 3 = 7. Tìm x?', 'x = 6', 'x = 8', 'x = 10', 'x = 12', 'B', 1, NOW(), NOW()),
(1, 'L2', 'lớp 10', 'Đại số', 'Giải phương trình: 5x = 25. Tìm x?', 'x = 3', 'x = 5', 'x = 7', 'x = 10', 'B', 1, NOW(), NOW()),
(1, 'L2', 'lớp 10', 'Đại số', 'Giải phương trình: x + 12 = 20. Tìm x?', 'x = 6', 'x = 8', 'x = 10', 'x = 12', 'B', 1, NOW(), NOW()),
(1, 'L2', 'lớp 10', 'Hình học', 'Diện tích hình vuông cạnh 5cm là?', '20 cm²', '25 cm²', '30 cm²', '35 cm²', 'B', 1, NOW(), NOW()),
(1, 'L2', 'lớp 10', 'Hình học', 'Chu vi hình chữ nhật dài 6cm, rộng 4cm là?', '16 cm', '18 cm', '20 cm', '24 cm', 'C', 1, NOW(), NOW()),
(1, 'L2', 'lớp 10', 'Hình học', 'Diện tích tam giác có đáy 8cm, cao 5cm là?', '15 cm²', '20 cm²', '25 cm²', '40 cm²', 'B', 1, NOW(), NOW()),
(1, 'L2', 'lớp 10', 'Hình học', 'Thể tích hình lập phương cạnh 3cm là?', '9 cm³', '18 cm³', '27 cm³', '36 cm³', 'C', 1, NOW(), NOW()),
(1, 'L2', 'lớp 10', 'Hình học', 'Chu vi hình tròn bán kính 7cm (π ≈ 22/7) là?', '22 cm', '44 cm', '66 cm', '88 cm', 'B', 1, NOW(), NOW()),
(1, 'L2', 'lớp 10', 'Phân số', '1/2 + 1/4 = ?', '1/6', '2/6', '3/4', '3/6', 'C', 1, NOW(), NOW()),
(1, 'L2', 'lớp 10', 'Phân số', '2/3 × 3/4 = ?', '1/2', '5/7', '6/12', '5/12', 'A', 1, NOW(), NOW()),
(1, 'L2', 'lớp 10', 'Phân số', '3/5 - 1/5 = ?', '1/5', '2/5', '3/5', '4/5', 'B', 1, NOW(), NOW()),
(1, 'L2', 'lớp 10', 'Phân số', '1/3 ÷ 2 = ?', '1/6', '2/3', '1/2', '2/6', 'A', 1, NOW(), NOW()),
(1, 'L2', 'lớp 10', 'Phân số', '4/5 + 1/10 = ?', '5/10', '8/10', '9/10', '5/15', 'C', 1, NOW(), NOW()),
(1, 'L2', 'lớp 10', 'Lũy thừa', '2³ = ?', '6', '8', '9', '12', 'B', 1, NOW(), NOW()),
(1, 'L2', 'lớp 10', 'Lũy thừa', '5² = ?', '10', '15', '20', '25', 'D', 1, NOW(), NOW()),
(1, 'L2', 'lớp 10', 'Lũy thừa', '10¹ = ?', '1', '10', '100', '1000', 'B', 1, NOW(), NOW()),
(1, 'L2', 'lớp 10', 'Lũy thừa', '4² = ?', '8', '12', '16', '20', 'C', 1, NOW(), NOW()),
(1, 'L2', 'lớp 10', 'Lũy thừa', '3³ = ?', '9', '18', '27', '36', 'C', 1, NOW(), NOW());

-- Thêm câu hỏi Level 3 (khó) - 15 câu
INSERT INTO question_bank (subject_id, level, class_name, skill_tag, question_text, option_a, option_b, option_c, option_d, correct_answer, is_active, created_at, updated_at) VALUES
(1, 'L3', 'lớp 10', 'Hàm số bậc hai', 'Cho hàm số y = x² - 4x + 3. Tìm giá trị nhỏ nhất của y?', 'y = -1', 'y = 0', 'y = 1', 'y = 3', 'A', 1, NOW(), NOW()),
(1, 'L3', 'lớp 10', 'Hàm số bậc hai', 'Phương trình x² - 5x + 6 = 0 có nghiệm là?', 'x = 1, x = 6', 'x = 2, x = 3', 'x = -2, x = -3', 'x = 5, x = 1', 'B', 1, NOW(), NOW()),
(1, 'L3', 'lớp 10', 'Bất phương trình', 'Giải bất phương trình: 2x + 3 > 9', 'x > 2', 'x > 3', 'x > 4', 'x > 6', 'B', 1, NOW(), NOW()),
(1, 'L3', 'lớp 10', 'Bất phương trình', 'Giải bất phương trình: 3x - 5 < 10', 'x < 3', 'x < 5', 'x < 7', 'x < 10', 'B', 1, NOW(), NOW()),
(1, 'L3', 'lớp 10', 'Lượng giác', 'sin(30°) = ?', '1/4', '1/3', '1/2', '√3/2', 'C', 1, NOW(), NOW()),
(1, 'L3', 'lớp 10', 'Lượng giác', 'cos(60°) = ?', '1/4', '1/3', '1/2', '√3/2', 'C', 1, NOW(), NOW()),
(1, 'L3', 'lớp 10', 'Lượng giác', 'tan(45°) = ?', '0', '1/2', '1', '√2', 'C', 1, NOW(), NOW()),
(1, 'L3', 'lớp 10', 'Vectơ', 'Cho |a| = 3, |b| = 4, góc giữa a và b là 90°. Tính |a + b|?', '5', '6', '7', '12', 'A', 1, NOW(), NOW()),
(1, 'L3', 'lớp 10', 'Logarit', 'log₁₀(100) = ?', '1', '2', '10', '100', 'B', 1, NOW(), NOW()),
(1, 'L3', 'lớp 10', 'Logarit', 'log₂(8) = ?', '2', '3', '4', '8', 'B', 1, NOW(), NOW()),
(1, 'L3', 'lớp 10', 'Tập hợp', 'Cho A = {1,2,3}, B = {2,3,4}. A ∩ B = ?', '{1,2,3,4}', '{2,3}', '{1,4}', '{1,2,3}', 'B', 1, NOW(), NOW()),
(1, 'L3', 'lớp 10', 'Tập hợp', 'Cho A = {1,2,3}, B = {2,3,4}. A ∪ B = ?', '{1,2,3,4}', '{2,3}', '{1,4}', '{1,2,3}', 'A', 1, NOW(), NOW()),
(1, 'L3', 'lớp 10', 'Xác suất', 'Xác suất tổng 2 con xúc xắc bằng 7 là?', '1/6', '1/8', '1/12', '1/36', 'A', 1, NOW(), NOW()),
(1, 'L3', 'lớp 10', 'Tổ hợp', 'C(5,2) = ?', '5', '10', '15', '20', 'B', 1, NOW(), NOW()),
(1, 'L3', 'lớp 10', 'Tổ hợp', 'P(4) = ?', '12', '16', '20', '24', 'D', 1, NOW(), NOW());

-- Kiểm tra kết quả
SELECT 
    level,
    COUNT(*) as total_questions
FROM question_bank 
WHERE subject_id = 1 AND class_name = 'lớp 10' AND is_active = 1
GROUP BY level;
