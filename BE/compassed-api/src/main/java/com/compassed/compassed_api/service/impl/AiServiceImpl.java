package com.compassed.compassed_api.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.compassed.compassed_api.ai.OpenAiClient;
import com.compassed.compassed_api.service.AiService;

@Service
public class AiServiceImpl implements AiService {

  private final OpenAiClient openAiClient;

  @Value("${openai.model}")
  private String model;

  public AiServiceImpl(OpenAiClient openAiClient) {
    this.openAiClient = openAiClient;
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

    return openAiClient.callChatGpt(model, prompt);
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
}
