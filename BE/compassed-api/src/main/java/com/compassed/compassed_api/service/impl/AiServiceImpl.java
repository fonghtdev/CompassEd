package com.compassed.compassed_api.service.impl;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.compassed.compassed_api.ai.OpenAiClient;
import com.compassed.compassed_api.domain.entity.AiGenerationLog;
import com.compassed.compassed_api.repository.AiGenerationLogRepository;
import com.compassed.compassed_api.service.AiService;

@Service
public class AiServiceImpl implements AiService {

  private static final Map<String, String> ROADMAP_PROMPT_FILE_BY_SUBJECT = new LinkedHashMap<>();

  static {
    ROADMAP_PROMPT_FILE_BY_SUBJECT.put("M", ".roadmapM.md");
    ROADMAP_PROMPT_FILE_BY_SUBJECT.put("MATH", ".roadmapM.md");
    ROADMAP_PROMPT_FILE_BY_SUBJECT.put("MATHS", ".roadmapM.md");
    ROADMAP_PROMPT_FILE_BY_SUBJECT.put("TOAN", ".roadmapM.md");

    ROADMAP_PROMPT_FILE_BY_SUBJECT.put("L", ".roadmapL.md");
    ROADMAP_PROMPT_FILE_BY_SUBJECT.put("LIT", ".roadmapL.md");
    ROADMAP_PROMPT_FILE_BY_SUBJECT.put("LITERATURE", ".roadmapL.md");
    ROADMAP_PROMPT_FILE_BY_SUBJECT.put("VAN", ".roadmapL.md");

    ROADMAP_PROMPT_FILE_BY_SUBJECT.put("E", ".roadmapE.md");
    ROADMAP_PROMPT_FILE_BY_SUBJECT.put("ENG", ".roadmapE.md");
    ROADMAP_PROMPT_FILE_BY_SUBJECT.put("ENGLISH", ".roadmapE.md");
  }

  private final OpenAiClient openAiClient;
  private final ObjectProvider<AiGenerationLogRepository> aiGenerationLogRepositoryProvider;

  @Value("${openai.model}")
  private String model;

  public AiServiceImpl(
      OpenAiClient openAiClient,
      ObjectProvider<AiGenerationLogRepository> aiGenerationLogRepositoryProvider) {
    this.openAiClient = openAiClient;
    this.aiGenerationLogRepositoryProvider = aiGenerationLogRepositoryProvider;
  }

  @Override
  public String analyzeSkills(String subjectCode, String paperJson, String answersJson) {

    String prompt = """
        Bạn là hệ thống AI chấm bài học sinh THPT.
        Môn: %s

        Đề thi (JSON):
        %s

        Bài làm của học sinh (JSON):
        %s

        Hãy trả về JSON thuần (KHÔNG markdown) theo format:
        {
          "overall_level": "weak | average | good",
          "skills": [
            { "name": "topic", "score": 0-100, "note": "..." }
          ],
          "weak_topics": ["..."],
          "recommendations": ["..."]
        }
        """.formatted(subjectCode, paperJson, answersJson);

    String output = openAiClient.callChatGpt(model, prompt);
    persistAiLog("ANALYZE_SKILLS", subjectCode, prompt, output);
    return output;
  }

  @Override
  public String generatePlacementTest(String subjectCode, String level, int questionCount) {
    String prompt = """
        Bạn là giáo viên THPT chuyên môn.
        Hãy tạo đề placement test cho học sinh.

        Môn: %s
        Level: %s (L1: Nền tảng, L2: Trung cấp, L3: Nâng cao)
        Số câu hỏi: %d

        Hãy trả về JSON array thuần (KHÔNG markdown) với format:
        [
          {
            "id": 1,
            "q": "Nội dung câu hỏi",
            "options": ["A. ...", "B. ...", "C. ...", "D. ..."],
            "correct": "A",
            "skill": "chủ đề",
            "explanation": "Giải thích đáp án đúng"
          }
        ]

        Yêu cầu:
        - Câu hỏi phù hợp với level
        - Đáp án đúng rõ ràng
        - Giải thích ngắn gọn
        - Phù hợp với chương trình THPT Việt Nam
        """.formatted(subjectCode, level, questionCount);

    String output = openAiClient.callChatGpt(model, prompt);
    persistAiLog("GENERATE_PLACEMENT_TEST", subjectCode, prompt, output);
    return output;
  }

  @Override
  public String generatePersonalizedRoadmapGuide(
      String subjectCode,
      String level,
      String academicTrack,
      double placementScorePercent,
      String availableSkillsJson) {
    String subjectRoadmapPrompt = loadRoadmapPromptBySubject(subjectCode);

    String prompt = """
        Bạn là AI học tập cho CompassED.
        Hãy sinh roadmap hướng dẫn học tập CÁ NHÂN HÓA cho học sinh theo thông tin sau:

        - Môn: %s
        - Level hiện tại: %s
        - Hệ/lớp: %s (GRADE_11/GRADE_12/UNI_PREP)
        - Điểm placement: %.1f
        - Danh sách kỹ năng/chủ đề có trong QuestionBank (JSON): %s

        Prompt chuẩn theo môn (PHẢI tuân thủ và xem là nguồn thiết kế roadmap gốc):
        ---- BEGIN ROADMAP PROMPT FILE ----
        %s
        ---- END ROADMAP PROMPT FILE ----

        Yêu cầu bắt buộc:
        1) Bám đúng prompt chuẩn theo môn ở trên để tạo module và lesson.
        2) Chỉ bám theo level hiện tại (ví dụ L2 thì chỉ nội dung phù hợp L2).
        3) Chỉ bám theo hệ/lớp hiện tại (ví dụ GRADE_11 thì nội dung lớp 11).
        4) Trả về đúng 5 module trong roadmapSteps (moduleNo 1..5).
        5) Mỗi module phải có lessonPlan tối thiểu 10 bài học.
        6) Trả về JSON thuần (không markdown) theo format:
        {
          "objective": "...",
          "roadmapSteps": [
            {
              "week": 1,
              "title": "...",
              "focusSkills": ["..."],
              "studyGuide": "...",
              "targetScore": 0-100,
              "duration": "2 tuần",
              "lessonPlan": [
                { "lessonNo": 1, "title": "...", "summary": "...", "duration": "45 phút" }
              ]
            }
          ],
          "miniTestBlueprint": {
            "questionCount": 10,
            "skillsDistribution": [{"skill":"...","count":2}]
          },
          "finalTestBlueprint": {
            "questionCount": 20,
            "skillsDistribution": [{"skill":"...","count":4}]
          }
        }
        """.formatted(
        subjectCode,
        level,
        academicTrack,
        placementScorePercent,
        availableSkillsJson,
        subjectRoadmapPrompt);

    String output = openAiClient.callChatGpt(model, prompt);
    persistAiLog("GENERATE_PERSONALIZED_ROADMAP", subjectCode, prompt, output);
    return output;
  }

  private String loadRoadmapPromptBySubject(String subjectCode) {
    String code = normalizeSubjectCode(subjectCode);
    String fileName = ROADMAP_PROMPT_FILE_BY_SUBJECT.get(code);
    if (fileName == null || fileName.isBlank()) {
      return "Không có file prompt riêng cho môn này. Hãy tự tạo roadmap chuẩn theo level/lớp và danh sách kỹ năng.";
    }

    String content = readPromptFileFromRepoRoot(fileName);
    if (content == null || content.isBlank()) {
      return "Không đọc được file prompt môn " + code
          + ". Hãy tự tạo roadmap chuẩn theo level/lớp và danh sách kỹ năng.";
    }
    return content;
  }

  private String readPromptFileFromRepoRoot(String fileName) {
    Path[] candidates = new Path[] {
        Path.of(fileName),
        Path.of("..", fileName),
        Path.of("..", "..", fileName),
        Path.of("..", "..", "..", fileName)
    };

    for (Path candidate : candidates) {
      try {
        if (Files.exists(candidate)) {
          return Files.readString(candidate, StandardCharsets.UTF_8).trim();
        }
      } catch (Exception ignored) {
        // fallback to next candidate
      }
    }
    return null;
  }

  private String normalizeSubjectCode(String subjectCode) {
    if (subjectCode == null) {
      return "";
    }
    return subjectCode.trim().toUpperCase(Locale.ROOT);
  }

  // ===== JSON GIẢ =====
  private String mockSkillAnalysis(String subjectCode) {
    return """
        {
          "mode": "MOCK",
          "subject": "%s",
          "overall_level": "average",
          "skills": [
            { "name": "Kiến thức cơ bản", "score": 65, "note": "Nắm được kiến thức nền tảng" },
            { "name": "Kỹ năng làm bài", "score": 60, "note": "Cần luyện thêm dạng bài" }
          ],
          "weak_topics": [
            "Dạng bài vận dụng",
            "Câu hỏi suy luận"
          ],
          "recommendations": [
            "Học lại các phần yếu trong roadmap",
            "Làm thêm bài tập mức độ trung bình"
          ]
        }
        """.formatted(subjectCode);
  }

  private void persistAiLog(String taskType, String subjectCode, String inputPrompt, String outputText) {
    AiGenerationLogRepository repository = aiGenerationLogRepositoryProvider.getIfAvailable();
    if (repository == null) {
      return;
    }
    AiGenerationLog log = new AiGenerationLog();
    log.setTaskType(taskType);
    log.setSubjectCode(subjectCode);
    log.setInputPrompt(inputPrompt);
    log.setOutputText(outputText);
    log.setReviewStatus("PENDING");
    log.setCreatedAt(LocalDateTime.now());
    repository.save(log);
  }
}
