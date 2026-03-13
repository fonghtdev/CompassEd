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
        Ban la AI hoc tap cua CompassED. Sinh 1 roadmap ca nhan hoa bang JSON thuan, khong markdown.

        Dau vao:
        - Mon: %s
        - Level: %s
        - He/lop: %s
        - Diem placement: %.1f
        - Skill input (JSON ngan gon): %s

        Khung roadmap goc theo mon, phai bam sat:
        ---- BEGIN ROADMAP PROMPT FILE ----
        %s
        ---- END ROADMAP PROMPT FILE ----

        Rang buoc:
        1) Dung dung level va he/lop hien tai.
        2) Tra ve dung 5 module trong roadmapSteps, moduleNo 1..5.
        3) Moi module co it nhat 10 lesson trong lessonPlan.
        4) FocusSkills va lesson uu tien chon tu skill input.
        5) Noi dung ngan gon, tranh giai thich dai.
        6) Tra ve dung schema sau:
        {
          "objective": "...",
          "roadmapSteps": [
            {
              "week": 1,
              "title": "...",
              "focusSkills": ["..."],
              "studyGuide": "...",
              "targetScore": 0-100,
              "duration": "2 tuan",
              "lessonPlan": [
                { "lessonNo": 1, "title": "...", "summary": "...", "duration": "45 phut" }
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
      return "Khong co file prompt rieng cho mon nay. Hay tu tao roadmap chuan theo level/lop va danh sach ky nang.";
    }

    String content = readPromptFileFromRepoRoot(fileName);
    if (content == null || content.isBlank()) {
      return "Khong doc duoc file prompt mon " + code
          + ". Hay tu tao roadmap chuan theo level/lop va danh sach ky nang.";
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
