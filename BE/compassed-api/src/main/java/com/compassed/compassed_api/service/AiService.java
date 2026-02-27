package com.compassed.compassed_api.service;

public interface AiService {

    String analyzeSkills(
            String subjectCode,
            String paperJson,
            String answersJson);

    String generatePlacementTest(
            String subjectCode,
            String level,
            int questionCount);
}
