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
}
