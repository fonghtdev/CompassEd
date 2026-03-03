-- Xóa dữ liệu cũ
DELETE FROM question_bank WHERE subject_id = 1;

-- Thêm 25 câu Level L1 (dễ)
INSERT INTO question_bank (subject_id, level, class_name, skill_tag, skill_type, question_text, option_a, option_b, option_c, option_d, correct_answer, grade_level, is_active, created_at, updated_at, subject_code, question_id) VALUES
(1, 'L1', 'lớp 10', 'Phép tính', 'phep-tinh', '5 × 5 = ?', '15', '20', '25', '30', 'C', 10, 1, NOW(), NOW(), 'M', 'M_L1_10.1'),
(1, 'L1', 'lớp 10', 'Phép tính', 'phep-tinh', '10 / 10 = ?', '0', '1', '10', '100', 'B', 10, 1, NOW(), NOW(), 'M', 'M_L1_10.2'),
(1, 'L1', 'lớp 10', 'Phép tính', 'phep-tinh', '15 + 5 = ?', '10', '15', '20', '25', 'C', 10, 1, NOW(), NOW(), 'M', 'M_L1_10.3'),
(1, 'L1', 'lớp 10', 'Phép tính', 'phep-tinh', '20 - 8 = ?', '8', '10', '12', '14', 'C', 10, 1, NOW(), NOW(), 'M', 'M_L1_10.4'),
(1, 'L1', 'lớp 10', 'Phép tính', 'phep-tinh', '3 × 7 = ?', '18', '21', '24', '27', 'B', 10, 1, NOW(), NOW(), 'M', 'M_L1_10.5'),
(1, 'L1', 'lớp 10', 'Phép tính', 'phep-tinh', '100 / 5 = ?', '15', '20', '25', '30', 'B', 10, 1, NOW(), NOW(), 'M', 'M_L1_10.6'),
(1, 'L1', 'lớp 10', 'Phép tính', 'phep-tinh', '8 + 12 = ?', '18', '20', '22', '24', 'B', 10, 1, NOW(), NOW(), 'M', 'M_L1_10.7'),
(1, 'L1', 'lớp 10', 'Phép tính', 'phep-tinh', '50 - 15 = ?', '30', '35', '40', '45', 'B', 10, 1, NOW(), NOW(), 'M', 'M_L1_10.8'),
(1, 'L1', 'lớp 10', 'Phép tính', 'phep-tinh', '6 × 4 = ?', '20', '22', '24', '26', 'C', 10, 1, NOW(), NOW(), 'M', 'M_L1_10.9'),
(1, 'L1', 'lớp 10', 'Phép tính', 'phep-tinh', '45 / 9 = ?', '4', '5', '6', '7', 'B', 10, 1, NOW(), NOW(), 'M', 'M_L1_10.10'),
(1, 'L1', 'lớp 10', 'Phép tính', 'phep-tinh', '11 + 9 = ?', '18', '19', '20', '21', 'C', 10, 1, NOW(), NOW(), 'M', 'M_L1_10.11'),
(1, 'L1', 'lớp 10', 'Phép tính', 'phep-tinh', '30 - 12 = ?', '16', '18', '20', '22', 'B', 10, 1, NOW(), NOW(), 'M', 'M_L1_10.12'),
(1, 'L1', 'lớp 10', 'Phép tính', 'phep-tinh', '9 × 3 = ?', '24', '27', '30', '33', 'B', 10, 1, NOW(), NOW(), 'M', 'M_L1_10.13'),
(1, 'L1', 'lớp 10', 'Phép tính', 'phep-tinh', '64 / 8 = ?', '6', '7', '8', '9', 'C', 10, 1, NOW(), NOW(), 'M', 'M_L1_10.14'),
(1, 'L1', 'lớp 10', 'Phép tính', 'phep-tinh', '25 + 25 = ?', '45', '50', '55', '60', 'B', 10, 1, NOW(), NOW(), 'M', 'M_L1_10.15'),
(1, 'L1', 'lớp 10', 'Phép tính', 'phep-tinh', '40 - 18 = ?', '20', '22', '24', '26', 'B', 10, 1, NOW(), NOW(), 'M', 'M_L1_10.16'),
(1, 'L1', 'lớp 10', 'Phép tính', 'phep-tinh', '7 × 8 = ?', '54', '56', '58', '60', 'B', 10, 1, NOW(), NOW(), 'M', 'M_L1_10.17'),
(1, 'L1', 'lớp 10', 'Phép tính', 'phep-tinh', '81 / 9 = ?', '7', '8', '9', '10', 'C', 10, 1, NOW(), NOW(), 'M', 'M_L1_10.18'),
(1, 'L1', 'lớp 10', 'Phép tính', 'phep-tinh', '33 + 17 = ?', '48', '50', '52', '54', 'B', 10, 1, NOW(), NOW(), 'M', 'M_L1_10.19'),
(1, 'L1', 'lớp 10', 'Phép tính', 'phep-tinh', '55 - 23 = ?', '30', '32', '34', '36', 'B', 10, 1, NOW(), NOW(), 'M', 'M_L1_10.20'),
(1, 'L1', 'lớp 10', 'Phép tính', 'phep-tinh', '12 × 5 = ?', '50', '55', '60', '65', 'C', 10, 1, NOW(), NOW(), 'M', 'M_L1_10.21'),
(1, 'L1', 'lớp 10', 'Phép tính', 'phep-tinh', '72 / 6 = ?', '10', '11', '12', '13', 'C', 10, 1, NOW(), NOW(), 'M', 'M_L1_10.22'),
(1, 'L1', 'lớp 10', 'Phép tính', 'phep-tinh', '28 + 14 = ?', '40', '42', '44', '46', 'B', 10, 1, NOW(), NOW(), 'M', 'M_L1_10.23'),
(1, 'L1', 'lớp 10', 'Phép tính', 'phep-tinh', '90 - 35 = ?', '50', '55', '60', '65', 'B', 10, 1, NOW(), NOW(), 'M', 'M_L1_10.24'),
(1, 'L1', 'lớp 10', 'Phép tính', 'phep-tinh', '15 × 4 = ?', '55', '60', '65', '70', 'B', 10, 1, NOW(), NOW(), 'M', 'M_L1_10.25');

-- Thêm 20 câu Level L2 (trung bình)
INSERT INTO question_bank (subject_id, level, class_name, skill_tag, skill_type, question_text, option_a, option_b, option_c, option_d, correct_answer, grade_level, is_active, created_at, updated_at, subject_code, question_id) VALUES
(1, 'L2', 'lớp 10', 'Đại số', 'dai-so', 'Giải: 2x + 5 = 15', 'x = 3', 'x = 5', 'x = 7', 'x = 10', 'B', 10, 1, NOW(), NOW(), 'M', 'M_L2_10.1'),
(1, 'L2', 'lớp 10', 'Đại số', 'dai-so', 'Giải: 3x - 6 = 9', 'x = 3', 'x = 5', 'x = 7', 'x = 9', 'B', 10, 1, NOW(), NOW(), 'M', 'M_L2_10.2'),
(1, 'L2', 'lớp 10', 'Đại số', 'dai-so', 'Giải: x/2 + 3 = 7', 'x = 6', 'x = 8', 'x = 10', 'x = 12', 'B', 10, 1, NOW(), NOW(), 'M', 'M_L2_10.3'),
(1, 'L2', 'lớp 10', 'Đại số', 'dai-so', 'Giải: 5x = 25', 'x = 3', 'x = 5', 'x = 7', 'x = 10', 'B', 10, 1, NOW(), NOW(), 'M', 'M_L2_10.4'),
(1, 'L2', 'lớp 10', 'Đại số', 'dai-so', 'Giải: x + 12 = 20', 'x = 6', 'x = 8', 'x = 10', 'x = 12', 'B', 10, 1, NOW(), NOW(), 'M', 'M_L2_10.5'),
(1, 'L2', 'lớp 10', 'Hình học', 'hinh-hoc', 'Diện tích hình vuông cạnh 5cm', '20 cm²', '25 cm²', '30 cm²', '35 cm²', 'B', 10, 1, NOW(), NOW(), 'M', 'M_L2_10.6'),
(1, 'L2', 'lớp 10', 'Hình học', 'hinh-hoc', 'Chu vi HCN dài 6cm rộng 4cm', '16 cm', '18 cm', '20 cm', '24 cm', 'C', 10, 1, NOW(), NOW(), 'M', 'M_L2_10.7'),
(1, 'L2', 'lớp 10', 'Hình học', 'hinh-hoc', 'Diện tích tam giác đáy 8 cao 5', '15 cm²', '20 cm²', '25 cm²', '40 cm²', 'B', 10, 1, NOW(), NOW(), 'M', 'M_L2_10.8'),
(1, 'L2', 'lớp 10', 'Hình học', 'hinh-hoc', 'Thể tích lập phương cạnh 3cm', '9 cm³', '18 cm³', '27 cm³', '36 cm³', 'C', 10, 1, NOW(), NOW(), 'M', 'M_L2_10.9'),
(1, 'L2', 'lớp 10', 'Hình học', 'hinh-hoc', 'Chu vi hình tròn r=7 (π≈22/7)', '22 cm', '44 cm', '66 cm', '88 cm', 'B', 10, 1, NOW(), NOW(), 'M', 'M_L2_10.10'),
(1, 'L2', 'lớp 10', 'Phân số', 'phan-so', '1/2 + 1/4 = ?', '1/6', '2/6', '3/4', '3/6', 'C', 10, 1, NOW(), NOW(), 'M', 'M_L2_10.11'),
(1, 'L2', 'lớp 10', 'Phân số', 'phan-so', '2/3 × 3/4 = ?', '1/2', '5/7', '6/12', '5/12', 'A', 10, 1, NOW(), NOW(), 'M', 'M_L2_10.12'),
(1, 'L2', 'lớp 10', 'Phân số', 'phan-so', '3/5 - 1/5 = ?', '1/5', '2/5', '3/5', '4/5', 'B', 10, 1, NOW(), NOW(), 'M', 'M_L2_10.13'),
(1, 'L2', 'lớp 10', 'Phân số', 'phan-so', '1/3 ÷ 2 = ?', '1/6', '2/3', '1/2', '2/6', 'A', 10, 1, NOW(), NOW(), 'M', 'M_L2_10.14'),
(1, 'L2', 'lớp 10', 'Phân số', 'phan-so', '4/5 + 1/10 = ?', '5/10', '8/10', '9/10', '5/15', 'C', 10, 1, NOW(), NOW(), 'M', 'M_L2_10.15'),
(1, 'L2', 'lớp 10', 'Lũy thừa', 'luy-thua', '2³ = ?', '6', '8', '9', '12', 'B', 10, 1, NOW(), NOW(), 'M', 'M_L2_10.16'),
(1, 'L2', 'lớp 10', 'Lũy thừa', 'luy-thua', '5² = ?', '10', '15', '20', '25', 'D', 10, 1, NOW(), NOW(), 'M', 'M_L2_10.17'),
(1, 'L2', 'lớp 10', 'Lũy thừa', 'luy-thua', '10¹ = ?', '1', '10', '100', '1000', 'B', 10, 1, NOW(), NOW(), 'M', 'M_L2_10.18'),
(1, 'L2', 'lớp 10', 'Lũy thừa', 'luy-thua', '4² = ?', '8', '12', '16', '20', 'C', 10, 1, NOW(), NOW(), 'M', 'M_L2_10.19'),
(1, 'L2', 'lớp 10', 'Lũy thừa', 'luy-thua', '3³ = ?', '9', '18', '27', '36', 'C', 10, 1, NOW(), NOW(), 'M', 'M_L2_10.20');

-- Thêm 15 câu Level L3 (khó)
INSERT INTO question_bank (subject_id, level, class_name, skill_tag, skill_type, question_text, option_a, option_b, option_c, option_d, correct_answer, grade_level, is_active, created_at, updated_at, subject_code, question_id) VALUES
(1, 'L3', 'lớp 10', 'Hàm số', 'ham-so', 'y = x² - 4x + 3, min y?', 'y = -1', 'y = 0', 'y = 1', 'y = 3', 'A', 10, 1, NOW(), NOW(), 'M', 'M_L3_10.1'),
(1, 'L3', 'lớp 10', 'Hàm số', 'ham-so', 'x² - 5x + 6 = 0, nghiệm?', 'x=1,6', 'x=2,3', 'x=-2,-3', 'x=5,1', 'B', 10, 1, NOW(), NOW(), 'M', 'M_L3_10.2'),
(1, 'L3', 'lớp 10', 'Bất PT', 'bat-pt', '2x + 3 > 9, giải?', 'x > 2', 'x > 3', 'x > 4', 'x > 6', 'B', 10, 1, NOW(), NOW(), 'M', 'M_L3_10.3'),
(1, 'L3', 'lớp 10', 'Bất PT', 'bat-pt', '3x - 5 < 10, giải?', 'x < 3', 'x < 5', 'x < 7', 'x < 10', 'B', 10, 1, NOW(), NOW(), 'M', 'M_L3_10.4'),
(1, 'L3', 'lớp 10', 'Lượng giác', 'luong-giac', 'sin(30°) = ?', '1/4', '1/3', '1/2', '√3/2', 'C', 10, 1, NOW(), NOW(), 'M', 'M_L3_10.5'),
(1, 'L3', 'lớp 10', 'Lượng giác', 'luong-giac', 'cos(60°) = ?', '1/4', '1/3', '1/2', '√3/2', 'C', 10, 1, NOW(), NOW(), 'M', 'M_L3_10.6'),
(1, 'L3', 'lớp 10', 'Lượng giác', 'luong-giac', 'tan(45°) = ?', '0', '1/2', '1', '√2', 'C', 10, 1, NOW(), NOW(), 'M', 'M_L3_10.7'),
(1, 'L3', 'lớp 10', 'Vectơ', 'vecto', '|a|=3,|b|=4,90°, |a+b|?', '5', '6', '7', '12', 'A', 10, 1, NOW(), NOW(), 'M', 'M_L3_10.8'),
(1, 'L3', 'lớp 10', 'Logarit', 'logarit', 'log₁₀(100) = ?', '1', '2', '10', '100', 'B', 10, 1, NOW(), NOW(), 'M', 'M_L3_10.9'),
(1, 'L3', 'lớp 10', 'Logarit', 'logarit', 'log₂(8) = ?', '2', '3', '4', '8', 'B', 10, 1, NOW(), NOW(), 'M', 'M_L3_10.10'),
(1, 'L3', 'lớp 10', 'Tập hợp', 'tap-hop', 'A={1,2,3},B={2,3,4}, A∩B?', '{1,2,3,4}', '{2,3}', '{1,4}', '{1,2,3}', 'B', 10, 1, NOW(), NOW(), 'M', 'M_L3_10.11'),
(1, 'L3', 'lớp 10', 'Tập hợp', 'tap-hop', 'A={1,2,3},B={2,3,4}, A∪B?', '{1,2,3,4}', '{2,3}', '{1,4}', '{1,2,3}', 'A', 10, 1, NOW(), NOW(), 'M', 'M_L3_10.12'),
(1, 'L3', 'lớp 10', 'Xác suất', 'xac-suat', 'P(tổng 2 xúc xắc = 7)?', '1/6', '1/8', '1/12', '1/36', 'A', 10, 1, NOW(), NOW(), 'M', 'M_L3_10.13'),
(1, 'L3', 'lớp 10', 'Tổ hợp', 'to-hop', 'C(5,2) = ?', '5', '10', '15', '20', 'B', 10, 1, NOW(), NOW(), 'M', 'M_L3_10.14'),
(1, 'L3', 'lớp 10', 'Tổ hợp', 'to-hop', 'P(4) = ?', '12', '16', '20', '24', 'D', 10, 1, NOW(), NOW(), 'M', 'M_L3_10.15');

-- Kiểm tra kết quả
SELECT level, COUNT(*) as total FROM question_bank WHERE subject_id = 1 AND class_name = 'lớp 10' AND is_active = 1 GROUP BY level;
