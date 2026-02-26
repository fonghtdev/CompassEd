package com.compassed.compassed_api.local;

import java.util.*;

public class LessonBank {
    
    public record LessonData(Long id, String title, String content, int displayOrder, int estimatedMinutes) {}
    
    public static Map<String, List<LessonData>> getLessonsBySubjectAndLevel() {
        Map<String, List<LessonData>> map = new HashMap<>();
        
        map.put("MATH_L1", List.of(
            new LessonData(1L, "Cộng trừ các số tự nhiên", "Học cách cộng và trừ các số tự nhiên. Quy tắc: Cộng từ phải sang trái, nhớ khi tổng ≥ 10.", 1, 15),
            new LessonData(2L, "Phép nhân và phép chia", "Nhân: Đặt tính và nhân theo từng chữ số. Chia: Chia từng bước từ trái sang phải.", 2, 20),
            new LessonData(3L, "Luỹ thừa với số mũ tự nhiên", "Luỹ thừa bậc n của a là a^n = a × a × ... × a (n lần). Ví dụ: 2³ = 8.", 3, 15),
            new LessonData(4L, "Hình học cơ bản", "Hình vuông: P=4a, S=a². Hình chữ nhật: P=2(a+b), S=a×b.", 4, 20),
            new LessonData(5L, "Giải toán có lời văn", "Bước: Đọc kỹ → Tìm hiểu → Giải → Trả lời.", 5, 25)
        ));
        
        map.put("MATH_L2", List.of(
            new LessonData(1L, "Phương trình bậc nhất một ẩn", "Dạng ax + b = 0. Giải: x = -b/a (a≠0).", 1, 20),
            new LessonData(2L, "Căn bậc hai", "Căn bậc hai của a (a≥0) là số x thỏa x²=a. VD: √16 = 4.", 2, 15),
            new LessonData(3L, "Hình tròn", "C=2πr, S=πr². r là bán kính.", 3, 20),
            new LessonData(4L, "Số nguyên và số thực", "Số nguyên Z, Số hữu tỉ Q, Số thực R.", 4, 15),
            new LessonData(5L, "Định lý Pitago", "c² = a² + b² (c là cạnh huyền).", 5, 20)
        ));
        
        map.put("MATH_L3", List.of(
            new LessonData(1L, "Đạo hàm và ứng dụng", "f'(x) = lim[h→0] (f(x+h)-f(x))/h.", 1, 30),
            new LessonData(2L, "Tích phân cơ bản", "∫f(x)dx = F(x) + C.", 2, 30),
            new LessonData(3L, "Giới hạn dãy số", "Lim xₙ = L khi n→∞.", 3, 25),
            new LessonData(4L, "Phương trình bậc hai", "Δ = b² - 4ac. Nghiệm: (-b±√Δ)/2a.", 4, 25),
            new LessonData(5L, "Hệ tọa độ trong mặt phẳng", "Khoảng cách, trung điểm, vectơ.", 5, 25)
        ));
        
        map.put("LITERATURE_L1", List.of(
            new LessonData(1L, "Từ và cấu tạo từ", "Từ đơn, từ ghép, từ láy.", 1, 15),
            new LessonData(2L, "Câu và các loại câu", "Câu kể, hỏi, cảm, trần thuật.", 2, 15),
            new LessonData(3L, "Thành ngữ và tục ngữ", "Thành ngữ và tục ngữ Việt Nam.", 3, 15),
            new LessonData(4L, "Văn miêu tả", "Quan sát, so sánh, nhân hoá.", 4, 20),
            new LessonData(5L, "Thể thơ Ngũ ngôn", "5 chữ/câu, 4 câu/bài.", 5, 15)
        ));
        
        map.put("LITERATURE_L2", List.of(
            new LessonData(1L, "Biện pháp tu từ", "So sánh, nhân hoá, ẩn dụ, điệp ngữ.", 1, 20),
            new LessonData(2L, "Truyện ngắn hiện thực", "Đặc điểm truyện ngắn hiện thực.", 2, 20),
            new LessonData(3L, "Tác phẩm: Tắt đèn", "Chị em Nguyễn Thị Ngọc.", 3, 25),
            new LessonData(4L, "Tác phẩm: Lão Hạc", "Nam Cao - số phận người nông dân.", 4, 25),
            new LessonData(5L, "Văn kịch", "Đối thoại, hồi kết, sân khấu.", 5, 20)
        ));
        
        map.put("LITERATURE_L3", List.of(
            new LessonData(1L, "Văn học hiện sinh", "Sống có ý nghĩa, tự do lựa chọn.", 1, 25),
            new LessonData(2L, "Tác phẩm: Đời thừa", "Nam Cao - bi kịch Chí Phèo.", 2, 30),
            new LessonData(3L, "Văn kịch Việt Nam", "Vĩnh biệt Cửu Trùng.", 3, 30),
            new LessonData(4L, "Truyện ngắn Tết 1975", "Rừng xà nu - Nguyễn Trung Thành.", 4, 25),
            new LessonData(5L, "Phân tích tác phẩm", "Cách phân tích văn học.", 5, 30)
        ));
        
        map.put("ENGLISH_L1", List.of(
            new LessonData(1L, "Present Simple", "S + V(s/es) - thói quen, sự thật.", 1, 15),
            new LessonData(2L, "Past Simple", "S + V2 - hành động quá khứ.", 2, 15),
            new LessonData(3L, "Future Simple", "S + will + V - quyết định, dự đoán.", 3, 15),
            new LessonData(4L, "Prepositions of place", "In, on, at, under, between.", 4, 15),
            new LessonData(5L, "Articles: a, an, the", "Danh từ đếm được, xác định.", 5, 15)
        ));
        
        map.put("ENGLISH_L2", List.of(
            new LessonData(1L, "Present Perfect", "S + have/has + V3 - quá khứ đến hiện tại.", 1, 20),
            new LessonData(2L, "Conditionals Type 1", "If + V(s/es), will + V - thực tế.", 2, 20),
            new LessonData(3L, "Comparatives & Superlatives", "So sánh hơn, so sánh nhất.", 3, 20),
            new LessonData(4L, "Modal verbs", "Can, May, Must, Should.", 4, 20),
            new LessonData(5L, "Reported speech", "Chuyển câu trực tiếp → gián tiếp.", 5, 25)
        ));
        
        map.put("ENGLISH_L3", List.of(
            new LessonData(1L, "Conditionals Type 2 & 3", "If + V2/would + V, If + had + V3/would have + V3.", 1, 25),
            new LessonData(2L, "Passive Voice", "S + be + V3.", 2, 20),
            new LessonData(3L, "Relative Clauses", "Who, Which, That, Whose.", 3, 25),
            new LessonData(4L, "Inversion", "Hardly, Never, No sooner đứng đầu.", 4, 25),
            new LessonData(5L, "Subjunctive Mood", "It is essential that he arrive.", 5, 30)
        ));
        
        return map;
    }
    
    public static List<Map<String, Object>> getLessons(String subject, String level) {
        List<Map<String, Object>> lessons = new ArrayList<>();
        
        if ("MATH".equals(subject)) {
            if ("L1".equals(level)) {
                lessons.add(createLesson(1, "Cộng trừ các số tự nhiên", "Học cách cộng và trừ các số tự nhiên. Quy tắc: Cộng từ phải sang trái, nhớ khi tổng ≥ 10. Trừ từ phải sang trái, mượn khi số bị trừ nhỏ hơn.", "MATH", "L1", 1));
                lessons.add(createLesson(2, "Phép nhân và phép chia", "Nhân: Đặt tính và nhân theo từng chữ số. Chia: Chia từng bước từ trái sang phải. Chú ý bảng cửu chương.", "MATH", "L1", 2));
                lessons.add(createLesson(3, "Luỹ thừa với số mũ tự nhiên", "Luỹ thừa bậc n của a là a^n = a × a × ... × a (n lần). Ví dụ: 2³ = 2×2×2 = 8.", "MATH", "L1", 3));
                lessons.add(createLesson(4, "Hình học cơ bản", "Hình vuông: 4 cạnh bằng nhau, P=4a, S=a². Hình chữ nhật: P=2(a+b), S=a×b. Hình tam giác: S=½a×h.", "MATH", "L1", 4));
                lessons.add(createLesson(5, "Giải toán có lời văn", "Bước 1: Đọc kỹ đề bài. Bước 2: Tìm hiểu mối quan hệ. Bước 3: Giải và kiểm tra. Bước 4: Trả lời.", "MATH", "L1", 5));
            } else if ("L2".equals(level)) {
                lessons.add(createLesson(1, "Phương trình bậc nhất một ẩn", "Dạng ax + b = 0. Giải: x = -b/a (a≠0). Ví dụ: 2x + 3 = 11 → 2x = 8 → x = 4.", "MATH", "L2", 1));
                lessons.add(createLesson(2, "Căn bậc hai", "Căn bậc hai của a (a≥0) là số x thỏa x²=a. Ký hiệu: √a. VD: √16 = 4 vì 4²=16.", "MATH", "L2", 2));
                lessons.add(createLesson(3, "Hình tròn", "Đường tròn: tập hợp các điểm cách đều tâm. C=2πr hoặc C=πd. S=πr². r là bán kính, d là đường kính.", "MATH", "L2", 3));
                lessons.add(createLesson(4, "Số nguyên và số thực", "Số nguyên Z: ..., -2, -1, 0, 1, 2, ... Số hữu tỉ Q: a/b (b≠0). Số thực R: tất cả số trên trục số.", "MATH", "L2", 4));
                lessons.add(createLesson(5, "Định lý Pitago", "Trong tam giác vuông: c² = a² + b² (c là cạnh huyền). VD: a=3, b=4 → c=5.", "MATH", "L2", 5));
            } else if ("L3".equals(level)) {
                lessons.add(createLesson(1, "Đạo hàm và ứng dụng", "Đạo hàm: f'(x) = lim[h→0] (f(x+h)-f(x))/h. Quy tắc: (xⁿ)' = nxⁿ⁻¹, (C)' = 0.", "MATH", "L3", 1));
                lessons.add(createLesson(2, "Tích phân cơ bản", "Tích phân nguyên hàm: ∫f(x)dx = F(x) + C. ∫xⁿdx = xⁿ⁺¹/(n+1) + C (n≠-1).", "MATH", "L3", 2));
                lessons.add(createLesson(3, "Giới hạn dãy số", "Lim xₙ = L nếu xₙ tiến gần L khi n→∞. Quy tắc: lim(C) = C, lim(1/n) = 0, lim(qⁿ) = 0 (|q|<1).", "MATH", "L3", 3));
                lessons.add(createLesson(4, "Phương trình bậc hai", "ax² + bx + c = 0 (a≠0). Δ = b² - 4ac. Nếu Δ>0: 2 nghiệm, Δ=0: nghiệm kép, Δ<0: vô nghiệm.", "MATH", "L3", 4));
                lessons.add(createLesson(5, "Hệ tọa độ trong mặt phẳng", "Điểm A(x,y). Khoảng cách OA = √(x²+y²). Trung điểm M của AB: M((x₁+x₂)/2, (y₁+y₂)/2).", "MATH", "L3", 5));
            }
        } else if ("LITERATURE".equals(subject)) {
            if ("L1".equals(level)) {
                lessons.add(createLesson(1, "Từ và cấu tạo từ", "Từ là đơn vị có nghĩa. Cấu tạo từ: từ đơn (đẹp), từ ghép (nhà + nghệ = nhà nghệ), từ láy (trắng muốt).", "LITERATURE", "L1", 1));
                lessons.add(createLesson(2, "Câu và các loại câu", "Câu kể: kể chuyện. Câu hỏi: hỏi. Câu cảm: bộc lộ cảm xúc (có !). Câu trần thuật: tường thuật.", "LITERATURE", "L1", 2));
                lessons.add(createLesson(3, "Thành ngữ và tục ngữ", "Thành ngữ: cố định, 4-5 tiếng. VD: Nước chảy đá mòn. Tục ngữ: kinh nghiệm, ngắn gọn. VD: Có công mài sắt có ngày nên kim.", "LITERATURE", "L1", 3));
                lessons.add(createLesson(4, "Văn miêu tả", "Miêu tả: tả nhân vật, cảnh vật. Chú ý: quan sát kỹ, dùng từ gợi hình, sử dụng biện pháp so sánh, nhân hoá.", "LITERATURE", "L1", 4));
                lessons.add(createLesson(5, "Thể thơ Ngũ ngôn", "5 chữ/câu, thường 4 câu/bài. VD: Trung thu treo trước gió / Đêm nay ta đứng bên lăng ông...", "LITERATURE", "L1", 5));
            } else if ("L2".equals(level)) {
                lessons.add(createLesson(1, "Biện pháp tu từ", "So sánh: ví A như B. Nhân hoá: gán đặc điểm người cho vật. Ẩn dụ: gọi A là B gợi ý nghĩa khác. Điệp ngữ: lặp từ.", "LITERATURE", "L2", 1));
                lessons.add(createLesson(2, "Truyện ngắn hiện thực", "Đặc điểm: phản ánh đời sống, nhân vật điển hình, ít nhân vật, cốt truyện đơn giản, kết thúc tự nhiên.", "LITERATURE", "L2", 2));
                lessons.add(createLesson(3, "Tác phẩm: Tắt đèn", "Tác giả: Chị em Nguyễn Thị Ngọc. Nội dung: Đời sống khổ cực, tinh thần đoàn kết của dân lao động.", "LITERATURE", "L2", 3));
                lessons.add(createLesson(4, "Tác phẩm: Lão Hạc", "Tác giả: Nam Cao. Nhân vật: Lão Hạc, ông giáo. Chủ đề: số phận người nông dân, giá trị đạo đức.", "LITERATURE", "L2", 4));
                lessons.add(createLesson(5, "Văn kịch", "Kịch: đối thoại độc thoại, có hồi kết, sân khấu, diễn xuất. VD: Hạnh phúc của một tang gia.", "LITERATURE", "L2", 5));
            } else if ("L3".equals(level)) {
                lessons.add(createLesson(1, "Văn học hiện sinh", "Triết lý: sống có ý nghĩa, tự do lựa chọn, chịu trách nhiệm về cuộc đời mình. Đại diện: Nam Cao.", "LITERATURE", "L3", 1));
                lessons.add(createLesson(2, "Tác phẩm: Đời thừa", "Tác giả: Nam Cao. Nội dung: Chí Phèo muốn làm người lương thiện nhưng xã hội không cho. Bi kịch cá nhân.", "LITERATURE", "L3", 2));
                lessons.add(createLesson(3, "Văn kịch Việt Nam", "Vĩnh biệt Cửu Trùng - Vũ Như Tô: mơ ước xây dựng văn hoá nhưng thất bại. Chủ đề: nghệ sĩ và quyền lực.", "LITERATURE", "L3", 3));
                lessons.add(createLesson(4, "Truyện ngắn Tết 1975", "Rừng xà nu - Nguyễn Trung Thành. Nhân vật: Tnú, dân làng. Chủ đề: kháng chiến, chiến thắng, hy sinh.", "LITERATURE", "L3", 4));
                lessons.add(createLesson(5, "Phân tích tác phẩm", "Cách phân tích: 1. Tìm hiểu hoàn cảnh. 2. Phân tích nội dung. 3. Phân tích nghệ thuật. 4. Đánh giá.", "LITERATURE", "L3", 5));
            }
        } else if ("ENGLISH".equals(subject)) {
            if ("L1".equals(level)) {
                lessons.add(createLesson(1, "Present Simple", "S + V(s/es). Dùng cho thói quen, sự thật. VD: She is a teacher. They go to school.", "ENGLISH", "L1", 1));
                lessons.add(createLesson(2, "Past Simple", "S + V2. Dùng cho hành động đã xảy ra. VD: I went to market yesterday.", "ENGLISH", "L1", 2));
                lessons.add(createLesson(3, "Future Simple", "S + will + V. Dùng cho quyết định, dự đoán. VD: I will go tomorrow.", "ENGLISH", "L1", 3));
                lessons.add(createLesson(4, "Prepositions of place", "In: trong (phòng). On: trên (bề mặt). At: tại (điểm). Under: dưới. Between: giữa.", "ENGLISH", "L1", 4));
                lessons.add(createLesson(5, "Articles: a, an, the", "A/an: danh từ đếm được số ít (a book, an apple). The: xác định (the book on the table).", "ENGLISH", "L1", 5));
            } else if ("L2".equals(level)) {
                lessons.add(createLesson(1, "Present Perfect", "S + have/has + V3. Dùng cho hành động bắt đầu trong quá khứ và còn tiếp diễn. VD: I have learned English since 2020.", "ENGLISH", "L2", 1));
                lessons.add(createLesson(2, "Conditionals Type 1", "If + S + V(s/es), S + will + V. Thực tề có thể xảy ra. VD: If it rains, I will stay home.", "ENGLISH", "L2", 2));
                lessons.add(createLesson(3, "Comparatives & Superlatives", "So sánh hơn: adj + er / more adj. So sánh nhất: the + adjest / the most adj.", "ENGLISH", "L2", 3));
                lessons.add(createLesson(4, "Modal verbs", "Can/Could: khả năng. May/Might: cho phép, có thể. Must/Have to: phải. Should: nên.", "ENGLISH", "L2", 4));
                lessons.add(createLesson(5, "Reported speech", "Chuyển đổi câu trực tiếp sang gián tiếp. Lưu ý: thay đổi tenses, đại từ, thời gian.", "ENGLISH", "L2", 5));
            } else if ("L3".equals(level)) {
                lessons.add(createLesson(1, "Conditionals Type 2 & 3", "Type 2: If + V2, would + V (giả định không thực). Type 3: If + had + V3, would have + V3 (quá khứ không thực).", "ENGLISH", "L3", 1));
                lessons.add(createLesson(2, "Passive Voice", "S + be + V3. VD: The book was written by him. Dùng khi không quan tâm ai làm gì.", "ENGLISH", "L3", 2));
                lessons.add(createLesson(3, "Relative Clauses", "Who/That: người. Which/That: vật. Whose: sở hữu. VD: The book which I bought is interesting.", "ENGLISH", "L3", 3));
                lessons.add(createLesson(4, "Inversion", "Đảo ngữ: Never, Hardly, No sooner đứng đầu câu. VD: Hardly had he arrived when the phone rang.", "ENGLISH", "L3", 4));
                lessons.add(createLesson(5, "Subjunctive Mood", "It is essential that he arrive on time. Sau wish, would rather, as if: I wish I were rich.", "ENGLISH", "L3", 5));
            }
        }
        
        return lessons;
    }
    
    private static Map<String, Object> createLesson(int id, String title, String content, String subject, String level, int orderIndex) {
        Map<String, Object> lesson = new HashMap<>();
        lesson.put("id", id);
        lesson.put("title", title);
        lesson.put("content", content);
        lesson.put("subject", subject);
        lesson.put("level", level);
        lesson.put("orderIndex", orderIndex);
        return lesson;
    }
}
