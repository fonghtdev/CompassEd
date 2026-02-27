-- =====================================================
-- 🔄 THAY THẾ QUESTIONBANK VỚI CÂU HỎI CỦA BẠN
-- =====================================================

USE compassed_db;

-- Step 1: Xóa tất cả câu hỏi demo hiện tại
DELETE FROM question_bank;

-- Step 2: Reset AUTO_INCREMENT
ALTER TABLE question_bank AUTO_INCREMENT = 1;

-- Step 3: Thêm câu hỏi TOÁN của bạn
-- TOÁN L1 (Đại số + Hình học)
INSERT INTO question_bank (subject_id, level, skill_type, question_type, question_text, options, correct_answer, explanation, difficulty, is_active) VALUES
(1, 'L1', 'Đại số', 'MULTIPLE_CHOICE', 'Tính: 15 + 27 = ?', '["A. 42", "B. 41", "C. 43", "D. 40"]', 'A', 'Phép cộng cơ bản: 15 + 27 = 42', 1, 1),
(1, 'L1', 'Đại số', 'MULTIPLE_CHOICE', 'Tìm x: x + 5 = 12', '["A. 7", "B. 6", "C. 8", "D. 5"]', 'A', 'x = 12 - 5 = 7', 1, 1),
(1, 'L1', 'Đại số', 'MULTIPLE_CHOICE', 'Giá trị của 3² là?', '["A. 6", "B. 5", "C. 9", "D. 12"]', 'C', '3² = 3 × 3 = 9', 1, 1),
(1, 'L1', 'Hình học', 'MULTIPLE_CHOICE', 'Chu vi hình vuông cạnh 4cm?', '["A. 12cm", "B. 16cm", "C. 8cm", "D. 20cm"]', 'B', 'Chu vi hình vuông = 4 × cạnh = 4 × 4 = 16cm', 1, 1),
(1, 'L1', 'Hình học', 'MULTIPLE_CHOICE', 'Diện tích hình chữ nhật 3x5?', '["A. 8", "B. 15", "C. 12", "D. 15"]', 'D', 'Diện tích = dài × rộng = 3 × 5 = 15', 1, 1);

-- TOÁN L2 (Nâng cao hơn)
INSERT INTO question_bank (subject_id, level, skill_type, question_type, question_text, options, correct_answer, explanation, difficulty, is_active) VALUES
(1, 'L2', 'Đại số', 'MULTIPLE_CHOICE', 'Giải: 2x + 3 = 11', '["A. x=3", "B. x=4", "C. x=5", "D. x=2"]', 'B', '2x = 11 - 3 = 8, nên x = 4', 2, 1),
(1, 'L2', 'Đại số', 'MULTIPLE_CHOICE', 'Tính: √64 = ?', '["A. 8", "B. 6", "C. 7", "D. 9"]', 'A', '√64 = 8 vì 8² = 64', 2, 1),
(1, 'L2', 'Hình học', 'MULTIPLE_CHOICE', 'Diện tích hình tròn bán kính 3?', '["A. 6π", "B. 9π", "C. 9π", "D. 12π"]', 'C', 'Diện tích = πr² = π × 3² = 9π', 2, 1),
(1, 'L2', 'Đại số', 'MULTIPLE_CHOICE', 'Tính: (-2)³ = ?', '["A. -6", "B. 6", "C. -8", "D. -8"]', 'C', '(-2)³ = (-2) × (-2) × (-2) = -8', 2, 1),
(1, 'L2', 'Hình học', 'MULTIPLE_CHOICE', 'Cho tam giác vuông có 2 cạnh góc vuông 3,4. Cạnh huyền = ?', '["A. 5", "B. 5", "C. 6", "D. 7"]', 'B', 'Định lý Pythagoras: c² = 3² + 4² = 9 + 16 = 25, nên c = 5', 2, 1);

-- TOÁN L3 (Cao cấp)
INSERT INTO question_bank (subject_id, level, skill_type, question_type, question_text, options, correct_answer, explanation, difficulty, is_active) VALUES
(1, 'L3', 'Đại số', 'MULTIPLE_CHOICE', 'Đạo hàm của f(x) = x³ + 2x', '["A. 3x² + 2", "B. x² + 2", "C. 3x² + 2", "D. 3x + 2"]', 'C', 'f\'(x) = 3x² + 2', 4, 1),
(1, 'L3', 'Đại số', 'MULTIPLE_CHOICE', 'Tích phân ∫2x dx = ?', '["A. x² + C", "B. 2x² + C", "C. x + C", "D. x²"]', 'A', '∫2x dx = x² + C', 4, 1),
(1, 'L3', 'Đại số', 'MULTIPLE_CHOICE', 'Giới hạn lim(x→0) sin(x)/x = ?', '["A. 0", "B. 1", "C. ∞", "D. 2"]', 'B', 'Giới hạn cơ bản đặc biệt', 5, 1),
(1, 'L3', 'Đại số', 'MULTIPLE_CHOICE', 'Tìm m để phương trình x² + mx + 1 = 0 có nghiệm kép', '["A. m = ±2", "B. m = 2", "C. m = ±2", "D. m = -2"]', 'C', 'Δ = m² - 4 = 0, nên m = ±2', 4, 1),
(1, 'L3', 'Hình học', 'MULTIPLE_CHOICE', 'Cho A(1,2), B(3,4). Tọa độ trung điểm AB?', '["A. (2,3)", "B. (2,2)", "C. (4,6)", "D. (2,3)"]', 'D', 'Trung điểm = ((1+3)/2, (2+4)/2) = (2,3)', 3, 1);

-- Step 4: Thêm câu hỏi VĂN của bạn
-- VĂN L1 (Ngữ pháp cơ bản)
INSERT INTO question_bank (subject_id, level, skill_type, question_type, question_text, options, correct_answer, explanation, difficulty, is_active) VALUES
(2, 'L1', 'Ngữ pháp', 'MULTIPLE_CHOICE', 'Thành ngữ "Nước chảy đá mòn" có nghĩa?', '["A. Kiên trì sẽ thành công", "B. Nước rất mạnh", "C. Đá rất cứng", "D. Thời gian trôi"]', 'A', 'Thành ngữ về sự kiên trì', 1, 1),
(2, 'L1', 'Ngữ pháp', 'MULTIPLE_CHOICE', 'Từ láy trong "trắng muốt" là?', '["A. trắng", "B. muốt", "C. trắng muốt", "D. không có"]', 'C', 'Toàn bộ cụm từ "trắng muốt" là từ láy', 1, 1),
(2, 'L1', 'Ngữ pháp', 'MULTIPLE_CHOICE', 'Câu "Trăng lên cao quá" thuộc kiểu câu?', '["A. Câu kể", "B. Câu cảm", "C. Câu hỏi", "D. Câu trần thuật"]', 'B', 'Có từ "quá" thể hiện cảm xúc', 1, 1),
(2, 'L1', 'Ngữ pháp', 'MULTIPLE_CHOICE', 'Từ "xanh" trong "da xanh vì lạnh" là từ?', '["A. Tính từ", "B. Danh từ", "C. Động từ", "D. Trạng từ"]', 'A', 'Miêu tả tính chất của danh từ "da"', 1, 1),
(2, 'L1', 'Ngữ pháp', 'MULTIPLE_CHOICE', 'Thể thơ 5 chữ thuộc thể loại?', '["A. Thất ngôn bát cú", "B. Ngũ ngôn tứ tuyệt", "C. Tự do", "D. Ngũ ngôn tứ tuyệt"]', 'D', 'Thơ 5 chữ = Ngũ ngôn', 1, 1);

-- VĂN L2 (Đọc hiểu văn học)
INSERT INTO question_bank (subject_id, level, skill_type, question_type, question_text, options, correct_answer, explanation, difficulty, is_active) VALUES
(2, 'L2', 'Đọc hiểu', 'MULTIPLE_CHOICE', 'Biện pháp "Một cành con cò" so sánh với?', '["A. Người lao động", "B. Cành cò", "C. Cành cây", "D. Con cò"]', 'B', 'So sánh hình ảnh người với cành cò', 2, 1),
(2, 'L2', 'Đọc hiểu', 'MULTIPLE_CHOICE', 'Tác phẩm "Tắt đèn" của ai?', '["A. Nam Cao", "B. Ngô Tất Tố", "C. Chị em Nguyễn Thị Ngọc", "D. Xuân Diệu"]', 'C', 'Tác giả của truyện "Tắt đèn"', 2, 1),
(2, 'L2', 'Đọc hiểu', 'MULTIPLE_CHOICE', 'Chủ đề chính của "Lão Hạc" là?', '["A. Số phận người nông dân", "B. Tình yêu", "C. Chiến tranh", "D. Gia đình"]', 'A', 'Tác phẩm phản ánh đời sống nông dân', 2, 1),
(2, 'L2', 'Đọc hiểu', 'MULTIPLE_CHOICE', 'Thể loại "Hạnh phúc của một tang gia" là?', '["A. Truyện ngắn", "B. Thơ", "C. Kịch bản", "D. Truyện kịch"]', 'D', 'Thể loại truyện kịch', 2, 1),
(2, 'L2', 'Đọc hiểu', 'MULTIPLE_CHOICE', 'Nghệ thuật độc đáo trong "Vợ chồng A Phủ"?', '["A. Đối thoại", "B. Kể chuyện theo thời gian", "C. Tự sự", "D. Hồi tưởng"]', 'B', 'Kĩ thuật kể chuyện đặc biệt', 2, 1);

-- VĂN L3 (Văn học cao cấp)
INSERT INTO question_bank (subject_id, level, skill_type, question_type, question_text, options, correct_answer, explanation, difficulty, is_active) VALUES
(2, 'L3', 'Đọc hiểu', 'MULTIPLE_CHOICE', 'Triết lý trong "Đời thừa" của Nam Cao?', '["A. Sống vì danh lợi", "B. Sống có ý nghĩa", "C. Sống vì nghệ thuật", "D. Sống vì tiền"]', 'C', 'Thông điệp về giá trị nghệ thuật', 4, 1),
(2, 'L3', 'Đọc hiểu', 'MULTIPLE_CHOICE', 'Bi kịch của Vũ Như Tô trong "Vĩnh biệt Cửu Trùng"?', '["A. Mơ ước không thành", "B. Tình yêu tan vỡ", "C. Bị phản bội", "D. Mất gia đình"]', 'A', 'Nhân vật với ước mơ bị dập tắt', 4, 1),
(2, 'L3', 'Đọc hiểu', 'MULTIPLE_CHOICE', 'Nghệ thuật xây dựng nhân vật Tnú trong "Rừng xà nu"?', '["A. Đối thoại", "B. Độc thoại", "C. Tự sự", "D. Tương phản"]', 'D', 'Kỹ thuật tương phản để làm nổi bật', 4, 1),
(2, 'L3', 'Đọc hiểu', 'MULTIPLE_CHOICE', 'Thông điệp "Sống là cho, không phải nhận" trong tác phẩm nào?', '["A. Tắt đèn", "B. Từ ấy", "C. Đời thừa", "D. Lão Hạc"]', 'B', 'Thông điệp trong "Từ ấy"', 4, 1),
(2, 'L3', 'Đọc hiểu', 'MULTIPLE_CHOICE', 'Tính cách Mị trong "Vợ chồng A Phủ"?', '["A. Mạnh mẽ", "B. Trung thành", "C. Bị trầm cảm mạnh mẽ", "D. Dịu dàng"]', 'C', 'Nhân vật phức tạp với tâm lý đa chiều', 4, 1);

-- Step 5: Thêm câu hỏi TIẾNG ANH của bạn
-- ANH L1 (Grammar cơ bản)
INSERT INTO question_bank (subject_id, level, skill_type, question_type, question_text, options, correct_answer, explanation, difficulty, is_active) VALUES
(3, 'L1', 'Grammar', 'MULTIPLE_CHOICE', 'She ___ a teacher.', '["A. is", "B. are", "C. am", "D. be"]', 'A', 'Chủ ngữ "She" đi với động từ "is"', 1, 1),
(3, 'L1', 'Grammar', 'MULTIPLE_CHOICE', 'What ___ your name?', '["A. is", "B. are", "C. am", "D. do"]', 'A', 'Câu hỏi với "What" + to be', 1, 1),
(3, 'L1', 'Grammar', 'MULTIPLE_CHOICE', 'I ___ to the market yesterday.', '["A. go", "B. going", "C. went", "D. gone"]', 'C', 'Thì quá khứ đơn với "yesterday"', 1, 1),
(3, 'L1', 'Grammar', 'MULTIPLE_CHOICE', 'The cat is ___ the table.', '["A. in", "B. on", "C. at", "D. under"]', 'D', 'Giới từ chỉ vị trí "dưới"', 1, 1),
(3, 'L1', 'Grammar', 'MULTIPLE_CHOICE', 'She is ___ beautiful girl.', '["A. a", "B. an", "C. the", "D. no article"]', 'A', 'Mạo từ "a" trước phụ âm', 1, 1);

-- ANH L2 (Grammar nâng cao)
INSERT INTO question_bank (subject_id, level, skill_type, question_type, question_text, options, correct_answer, explanation, difficulty, is_active) VALUES
(3, 'L2', 'Grammar', 'MULTIPLE_CHOICE', 'If it ___ tomorrow, I will stay home.', '["A. rain", "B. rains", "C. rained", "D. will rain"]', 'B', 'Câu điều kiện loại 1: If + hiện tại đơn', 2, 1),
(3, 'L2', 'Grammar', 'MULTIPLE_CHOICE', 'He ___ English since 2020.', '["A. learns", "B. learned", "C. has learned", "D. will learn"]', 'C', 'Thì hiện tại hoàn thành với "since"', 2, 1),
(3, 'L2', 'Grammar', 'MULTIPLE_CHOICE', 'The movie was ___. Everyone loved it.', '["A. amazing", "B. more amazing", "C. most amazing", "D. as amazing"]', 'A', 'Tính từ đơn không cần so sánh', 2, 1),
(3, 'L2', 'Grammar', 'MULTIPLE_CHOICE', 'She suggested ___ to the beach.', '["A. go", "B. going", "C. to go", "D. went"]', 'B', 'Suggest + V-ing', 2, 1),
(3, 'L2', 'Grammar', 'MULTIPLE_CHOICE', 'He acts as if he ___ the owner.', '["A. is", "B. was", "C. were", "D. be"]', 'C', 'As if + were (subjunctive mood)', 2, 1);

-- ANH L3 (Grammar cao cấp)
INSERT INTO question_bank (subject_id, level, skill_type, question_type, question_text, options, correct_answer, explanation, difficulty, is_active) VALUES
(3, 'L3', 'Grammar', 'MULTIPLE_CHOICE', 'Hardly ___ arrived when the meeting started.', '["A. had he", "B. he had", "C. has he", "D. he has"]', 'A', 'Đảo ngữ với "Hardly"', 4, 1),
(3, 'L3', 'Grammar', 'MULTIPLE_CHOICE', 'The book ___ I bought yesterday is interesting.', '["A. who", "B. which", "C. whom", "D. whose"]', 'B', 'Đại từ quan hệ cho vật', 4, 1),
(3, 'L3', 'Grammar', 'MULTIPLE_CHOICE', 'She acts as though she ___ before.', '["A. had never spoken", "B. has never spoken", "C. never spoke", "D. never speaks"]', 'A', 'As though + past perfect', 4, 1),
(3, 'L3', 'Grammar', 'MULTIPLE_CHOICE', 'No sooner ___ than the phone rang.', '["A. he arrived", "B. did he arrive", "C. he had arrived", "D. had he arrived"]', 'D', 'Đảo ngữ với "No sooner"', 5, 1),
(3, 'L3', 'Grammar', 'MULTIPLE_CHOICE', 'It is essential that he ___ on time.', '["A. arrives", "B. arrived", "C. arrive", "D. arriving"]', 'C', 'Subjunctive với "essential"', 5, 1);

-- ✅ HOÀN THÀNH!
SELECT CONCAT('🎉 ĐÃ THÊM ', COUNT(*), ' CÂU HỎI CỦA BẠN!') as result FROM question_bank WHERE is_active = 1;