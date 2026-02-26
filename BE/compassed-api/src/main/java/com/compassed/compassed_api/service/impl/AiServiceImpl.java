package com.compassed.compassed_api.service.impl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.compassed.compassed_api.ai.OpenAiClient;
import com.compassed.compassed_api.domain.entity.AiGenerationLog;
import com.compassed.compassed_api.repository.AiGenerationLogRepository;
import com.compassed.compassed_api.service.AiService;

@Service
public class AiServiceImpl implements AiService {

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
