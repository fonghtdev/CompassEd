package com.compassed.compassed_api.service;

public interface AiService {

    /**
     * Phân tích kỹ năng dựa trên:
     * - môn học
     * - đề (paperJson)
     * - bài làm (answersJson)
     *
     * @return JSON string (skill analysis)
     */
    String analyzeSkills(
            String subjectCode,
            String paperJson,
            String answersJson);
}
