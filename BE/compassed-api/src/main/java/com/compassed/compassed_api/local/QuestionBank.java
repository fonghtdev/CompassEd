package com.compassed.compassed_api.local;

import java.util.*;

public class QuestionBank {
    public static List<Map<String, Object>> getQuestions(String subject, String level) {
        List<Map<String, Object>> questions = new ArrayList<>();
        
        if ("MATH".equals(subject)) {
            if ("L1".equals(level)) {
                questions.add(createQuestion(1, "Tính: 15 + 27 = ?", "A", Arrays.asList("42", "41", "43", "40"), "42"));
                questions.add(createQuestion(2, "Tìm x: x + 5 = 12", "A", Arrays.asList("7", "6", "8", "5"), "7"));
                questions.add(createQuestion(3, "Giá trị của 3² là?", "C", Arrays.asList("6", "5", "9", "12"), "9"));
                questions.add(createQuestion(4, "Chu vi hình vuông cạnh 4cm?", "B", Arrays.asList("12cm", "16cm", "8cm", "20cm"), "16cm"));
                questions.add(createQuestion(5, "Diện tích hình chữ nhật 3x5?", "D", Arrays.asList("8", "15", "12", "15"), "15"));
            } else if ("L2".equals(level)) {
                questions.add(createQuestion(1, "Giải: 2x + 3 = 11", "B", Arrays.asList("x=3", "x=4", "x=5", "x=2"), "x=4"));
                questions.add(createQuestion(2, "Tính: √64 = ?", "A", Arrays.asList("8", "6", "7", "9"), "8"));
                questions.add(createQuestion(3, "Diện tích hình tròn bán kính 3?", "C", Arrays.asList("6π", "9π", "9π", "12π"), "9π"));
                questions.add(createQuestion(4, "Tính: (-2)³ = ?", "D", Arrays.asList("-6", "6", "-8", "-8"), "-8"));
                questions.add(createQuestion(5, "Cho tam giác vuông có 2 cạnh góc vuông 3,4. Cạnh huyền = ?", "B", Arrays.asList("5", "5", "6", "7"), "5"));
            } else if ("L3".equals(level)) {
                questions.add(createQuestion(1, "Đạo hàm của f(x) = x³ + 2x", "C", Arrays.asList("3x² + 2", "x² + 2", "3x² + 2", "3x + 2"), "3x² + 2"));
                questions.add(createQuestion(2, "Tích phân ∫2x dx = ?", "A", Arrays.asList("x² + C", "2x² + C", "x + C", "x²"), "x² + C"));
                questions.add(createQuestion(3, "Giới hạn lim(x→0) sin(x)/x = ?", "B", Arrays.asList("0", "1", "∞", "2"), "1"));
                questions.add(createQuestion(4, "Tìm m để phương trình x² + mx + 1 = 0 có nghiệm kép", "C", Arrays.asList("m = ±2", "m = 2", "m = ±2", "m = -2"), "m = ±2"));
                questions.add(createQuestion(5, "Cho A(1,2), B(3,4). Tọa độ trung điểm AB?", "D", Arrays.asList("(2,3)", "(2,2)", "(4,6)", "(2,3)"), "(2,3)"));
            }
        } else if ("LITERATURE".equals(subject)) {
            if ("L1".equals(level)) {
                questions.add(createQuestion(1, "Thành ngữ 'Nước chảy đá mòn' có nghĩa?", "A", Arrays.asList("Kiên trì sẽ thành công", "Nước rất mạnh", "Đá rất cứng", "Thời gian trôi"), "Kiên trì sẽ thành công"));
                questions.add(createQuestion(2, "Từ láy trong 'trắng muốt' là?", "C", Arrays.asList("trắng", "muốt", "trắng muốt", "không có"), "trắng muốt"));
                questions.add(createQuestion(3, "Câu 'Trăng lên cao quá' thuộc kiểu câu?", "B", Arrays.asList("Câu kể", "Câu cảm", "Câu hỏi", "Câu trần thuật"), "Câu cảm"));
                questions.add(createQuestion(4, "Từ 'xanh' trong 'da xanh vì lạnh' là từ?", "A", Arrays.asList("Tính từ", "Danh từ", "Động từ", "Trạng từ"), "Tính từ"));
                questions.add(createQuestion(5, "Thể thơ 5 chữ thuộc thể loại?", "D", Arrays.asList("Thất ngôn bát cú", "Ngũ ngôn tứ tuyệt", "Tự do", "Ngũ ngôn tứ tuyệt"), "Ngũ ngôn tứ tuyệt"));
            } else if ("L2".equals(level)) {
                questions.add(createQuestion(1, "Biện pháp 'Một cành con cò' so sánh với?", "B", Arrays.asList("Người lao động", "Cành cò", "Cành cây", "Con cò"), "Cành cò"));
                questions.add(createQuestion(2, "Tác phẩm 'Tắt đèn' của ai?", "C", Arrays.asList("Nam Cao", "Ngô Tất Tố", "Chị em Nguyễn Thị Ngọc", "Xuân Diệu"), "Chị em Nguyễn Thị Ngọc"));
                questions.add(createQuestion(3, "Chủ đề chính của 'Lão Hạc' là?", "A", Arrays.asList("Số phận người nông dân", "Tình yêu", "Chiến tranh", "Gia đình"), "Số phận người nông dân"));
                questions.add(createQuestion(4, "Thể loại 'Hạnh phúc của một tang gia' là?", "D", Arrays.asList("Truyện ngắn", "Thơ", "Kịch bản", "Truyện kịch"), "Truyện kịch"));
                questions.add(createQuestion(5, "Nghệ thuật độc đáo trong 'Vợ chồng A Phủ'?", "B", Arrays.asList("Đối thoại", "Kể chuyện theo thời gian", "Tự sự", "Hồi tưởng"), "Kể chuyện theo thời gian"));
            } else if ("L3".equals(level)) {
                questions.add(createQuestion(1, "Triết lý trong 'Đời thừa' của Nam Cao?", "C", Arrays.asList("Sống vì danh lợi", "Sống có ý nghĩa", "Sống vì nghệ thuật", "Sống vì tiền"), "Sống vì nghệ thuật"));
                questions.add(createQuestion(2, "Bi kịch của Vũ Như Tô trong 'Vĩnh biệt Cửu Trùng'?", "A", Arrays.asList("Mơ ước không thành", "Tình yêu tan vỡ", "Bị phản bội", "Mất gia đình"), "Mơ ước không thành"));
                questions.add(createQuestion(3, "Nghệ thuật xây dựng nhân vật Tnú trong 'Rừng xà nu'?", "D", Arrays.asList("Đối thoại", "Độc thoại", "Tự sự", "Tương phản"), "Tương phản"));
                questions.add(createQuestion(4, "Thông điệp 'Sống là cho, không phải nhận' trong tác phẩm nào?", "B", Arrays.asList("Tắt đèn", "Từ ấy", "Đời thừa", "Lão Hạc"), "Từ ấy"));
                questions.add(createQuestion(5, "Tính cách Mị trong 'Vợ chồng A Phủ'?", "C", Arrays.asList("Mạnh mẽ", "Trung thành", "Bị trầm cảm mạnh mẽ", "Dịu dàng"), "Bị trầm cảm mạnh mẽ"));
            }
        } else if ("ENGLISH".equals(subject)) {
            if ("L1".equals(level)) {
                questions.add(createQuestion(1, "She ___ a teacher.", "A", Arrays.asList("is", "are", "am", "be"), "is"));
                questions.add(createQuestion(2, "What ___ your name?", "B", Arrays.asList("is", "are", "am", "do"), "is"));
                questions.add(createQuestion(3, "I ___ to the market yesterday.", "C", Arrays.asList("go", "going", "went", "gone"), "went"));
                questions.add(createQuestion(4, "The cat is ___ the table.", "D", Arrays.asList("in", "on", "at", "under"), "under"));
                questions.add(createQuestion(5, "She is ___ beautiful girl.", "A", Arrays.asList("a", "an", "the", "no article"), "a"));
            } else if ("L2".equals(level)) {
                questions.add(createQuestion(1, "If it ___ tomorrow, I will stay home.", "B", Arrays.asList("rain", "rains", "rained", "will rain"), "rains"));
                questions.add(createQuestion(2, "He ___ English since 2020.", "C", Arrays.asList("learns", "learned", "has learned", "will learn"), "has learned"));
                questions.add(createQuestion(3, "The movie was ___. Everyone loved it.", "A", Arrays.asList("amazing", "more amazing", "most amazing", "as amazing"), "amazing"));
                questions.add(createQuestion(4, "She suggested ___ to the beach.", "D", Arrays.asList("go", "going", "to go", "went"), "going"));
                questions.add(createQuestion(5, "He acts as if he ___ the owner.", "C", Arrays.asList("is", "was", "were", "be"), "were"));
            } else if ("L3".equals(level)) {
                questions.add(createQuestion(1, "Hardly ___ arrived when the meeting started.", "A", Arrays.asList("had he", "he had", "has he", "he has"), "had he"));
                questions.add(createQuestion(2, "The book ___ I bought yesterday is interesting.", "B", Arrays.asList("who", "which", "whom", "whose"), "which"));
                questions.add(createQuestion(3, "She acts as though she ___ before.", "C", Arrays.asList("had never spoken", "has never spoken", "never spoke", "never speaks"), "had never spoken"));
                questions.add(createQuestion(4, "No sooner ___ than the phone rang.", "D", Arrays.asList("he arrived", "did he arrive", "he had arrived", "had he arrived"), "had he arrived"));
                questions.add(createQuestion(5, "It is essential that he ___ on time.", "A", Arrays.asList("arrives", "arrived", "arrive", "arriving"), "arrives"));
            }
        }
        
        return questions;
    }
    
    private static Map<String, Object> createQuestion(int number, String question, String correctAnswer, List<String> options, String correctOptionText) {
        Map<String, Object> q = new HashMap<>();
        q.put("number", number);
        q.put("question", question);
        q.put("correctAnswer", correctAnswer);
        q.put("options", options);
        q.put("correctOptionText", correctOptionText);
        return q;
    }
}
