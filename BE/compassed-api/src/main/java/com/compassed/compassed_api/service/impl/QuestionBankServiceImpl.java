package com.compassed.compassed_api.service.impl;

import com.compassed.compassed_api.api.dto.CreateQuestionRequest;
import com.compassed.compassed_api.api.dto.QuestionBankDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.compassed.compassed_api.domain.QuestionBank;
import com.compassed.compassed_api.domain.QuestionBank.Level;
import com.compassed.compassed_api.domain.QuestionBank.QuestionType;
import com.compassed.compassed_api.domain.entity.Subject;
import com.compassed.compassed_api.repository.QuestionBankRepository;
import com.compassed.compassed_api.repository.SubjectRepository;
import com.compassed.compassed_api.service.QuestionBankService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

@Service
@Transactional
public class QuestionBankServiceImpl implements QuestionBankService {

    private final QuestionBankRepository questionBankRepository;
    private final SubjectRepository subjectRepository;
    private final ObjectMapper objectMapper;

    public QuestionBankServiceImpl(QuestionBankRepository questionBankRepository, 
                                  SubjectRepository subjectRepository,
                                  ObjectMapper objectMapper) {
        this.questionBankRepository = questionBankRepository;
        this.subjectRepository = subjectRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Page<QuestionBankDTO> getAllQuestions(Long subjectId, Integer gradeLevel, Level level, String gradeBand, String skillType,
                                                 Boolean isActive, Pageable pageable) {
        Specification<QuestionBank> spec = (root, query, cb) -> cb.conjunction();
        
        if (subjectId != null) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("subject").get("id"), subjectId));
        }
        
        if (level != null) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("level"), level));
        }

        if (gradeLevel != null) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get("gradeLevel"), gradeLevel));
        }

        if (gradeBand != null && !gradeBand.isBlank()) {
            spec = spec.and((root, query, cb) ->
                cb.equal(cb.upper(root.get("gradeBand")), gradeBand.trim().toUpperCase()));
        }
        
        if (skillType != null && !skillType.isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("skillType"), skillType));
        }
        
        if (isActive != null) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("isActive"), isActive));
        }
        
        return questionBankRepository.findAll(spec, pageable).map(this::convertToDTO);
    }

    @Override
    public QuestionBankDTO getQuestionById(Long id) {
        QuestionBank question = questionBankRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Question not found with id: " + id));
        return convertToDTO(question);
    }

    @Override
    public QuestionBankDTO createQuestion(CreateQuestionRequest request) {
        Subject subject = subjectRepository.findById(request.getSubjectId())
            .orElseThrow(() -> new RuntimeException("Subject not found"));

        QuestionBank question = new QuestionBank();
        question.setSubject(subject);
        question.setLevel(request.getLevel());
        question.setGradeLevel(request.getGradeLevel());
        question.setGradeBand(resolveGradeBand(request.getGradeBand(), request.getGradeLevel()));
        question.setSkillType(request.getSkillType());
        question.setQuestionType(request.getQuestionType());
        question.setQuestionText(request.getQuestionText());
        question.setOptions(request.getOptions());
        question.setCorrectAnswer(request.getCorrectAnswer());
        question.setExplanation(request.getExplanation());
        question.setDifficulty(request.getDifficulty());
        question.setIsActive(true);

        QuestionBank saved = questionBankRepository.save(question);
        return convertToDTO(saved);
    }

    @Override
    public QuestionBankDTO updateQuestion(Long id, CreateQuestionRequest request) {
        QuestionBank question = questionBankRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Question not found"));

        if (request.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));
            question.setSubject(subject);
        }

        if (request.getLevel() != null) question.setLevel(request.getLevel());
        if (request.getGradeLevel() != null) question.setGradeLevel(request.getGradeLevel());
        if (request.getGradeBand() != null || request.getGradeLevel() != null) {
            Integer targetGradeLevel = request.getGradeLevel() != null ? request.getGradeLevel() : question.getGradeLevel();
            question.setGradeBand(resolveGradeBand(request.getGradeBand(), targetGradeLevel));
        }
        if (request.getSkillType() != null) question.setSkillType(request.getSkillType());
        if (request.getQuestionType() != null) question.setQuestionType(request.getQuestionType());
        if (request.getQuestionText() != null) question.setQuestionText(request.getQuestionText());
        if (request.getOptions() != null) question.setOptions(request.getOptions());
        if (request.getCorrectAnswer() != null) question.setCorrectAnswer(request.getCorrectAnswer());
        if (request.getExplanation() != null) question.setExplanation(request.getExplanation());
        if (request.getDifficulty() != null) question.setDifficulty(request.getDifficulty());

        QuestionBank updated = questionBankRepository.save(question);
        return convertToDTO(updated);
    }

    @Override
    public void deleteQuestion(Long id) {
        QuestionBank question = questionBankRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Question not found"));
        question.setIsActive(false);
        questionBankRepository.save(question);
    }

    @Override
    public void hardDeleteQuestion(Long id) {
        questionBankRepository.deleteById(id);
    }

    @Override
    public Map<String, Object> getQuestionStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalQuestions", questionBankRepository.count());
        stats.put("activeQuestions", questionBankRepository.countByIsActiveTrue());
        
        // Count by level
        Map<String, Long> byLevel = new HashMap<>();
        for (Level level : Level.values()) {
            long count = questionBankRepository.countByLevelAndIsActiveTrue(level);
            byLevel.put(level.name(), count);
        }
        stats.put("byLevel", byLevel);
        
        // Count by subject
        List<Map<String, Object>> bySubject = questionBankRepository.countBySubject();
        stats.put("bySubject", bySubject);
        
        return stats;
    }

    @Override
    public List<String> getSkillTypesBySubjectAndLevel(Long subjectId, Level level) {
        return questionBankRepository.findDistinctSkillTypes(subjectId, level);
    }

    @Override
    public Map<String, Object> importQuestionsFromLegacyJson(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            throw new RuntimeException("rawJson is required");
        }

        List<Map<String, Object>> errors = new ArrayList<>();
        int created = 0;
        int total = 0;

        try {
            JsonNode root = objectMapper.readTree(rawJson);
            if (!root.isArray()) {
                throw new RuntimeException("JSON must be an array of question objects");
            }
            total = root.size();

            for (int i = 0; i < root.size(); i++) {
                JsonNode row = root.get(i);
                try {
                    CreateQuestionRequest req = mapLegacyRowToCreateRequest(row);
                    createQuestion(req);
                    created++;
                } catch (Exception ex) {
                    Map<String, Object> err = new HashMap<>();
                    err.put("index", i);
                    err.put("id", text(row, "ID"));
                    err.put("error", ex.getMessage());
                    errors.add(err);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("total", total);
            result.put("created", created);
            result.put("failed", total - created);
            result.put("errors", errors);
            return result;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Invalid JSON payload: " + ex.getMessage());
        }
    }

    private QuestionBankDTO convertToDTO(QuestionBank question) {
        QuestionBankDTO dto = new QuestionBankDTO();
        dto.setId(question.getId());
        dto.setSubjectId(question.getSubject().getId());
        dto.setSubjectName(question.getSubject().getName());
        dto.setGradeLevel(question.getGradeLevel());
        dto.setLevel(question.getLevel());
        dto.setGradeBand(question.getGradeBand());
        dto.setSkillType(question.getSkillType());
        dto.setQuestionType(question.getQuestionType());
        dto.setQuestionText(question.getQuestionText());
        dto.setOptions(question.getOptions());
        dto.setCorrectAnswer(question.getCorrectAnswer());
        dto.setExplanation(question.getExplanation());
        dto.setDifficulty(question.getDifficulty());
        dto.setIsActive(question.getIsActive());
        return dto;
    }

    private String normalizeGradeBand(String gradeBand) {
        String normalized = gradeBand == null ? "" : gradeBand.trim().toUpperCase();
        if (normalized.isBlank()) {
            return "GRADE_11";
        }
        return switch (normalized) {
            case "GRADE_11", "GRADE_12", "UNI_PREP" -> normalized;
            default -> throw new RuntimeException("gradeBand must be GRADE_11, GRADE_12 or UNI_PREP");
        };
    }

    private String resolveGradeBand(String gradeBand, Integer gradeLevel) {
        if (gradeBand != null && !gradeBand.trim().isEmpty()) {
            return normalizeGradeBand(gradeBand);
        }
        if (gradeLevel != null) {
            if (gradeLevel == 12) return "GRADE_12";
            if (gradeLevel == 11 || gradeLevel == 10) return "GRADE_11";
        }
        return "UNI_PREP";
    }

    private CreateQuestionRequest mapLegacyRowToCreateRequest(JsonNode row) throws Exception {
        String subjectCode = text(row, "Subject_Code");
        if (subjectCode.isBlank()) {
            throw new RuntimeException("Subject_Code is required");
        }
        Subject subject = resolveSubjectByLegacyCode(subjectCode);
        boolean mathSubject = isMathSubject(subject);

        Level level = parseLegacyLevel(row.get("Level"));
        String legacyClassRaw = text(row, "Class");
        Integer gradeLevel = parseLegacyGradeLevel(row.get("Class"));
        String gradeBand = normalizeLegacyGradeBand(text(row, "Classify"), gradeLevel);
        if (isUniPrepToken(legacyClassRaw)) {
            gradeBand = "UNI_PREP";
        }

        String questionText = normalizeImportedText(text(row, "Question_Text"), mathSubject);
        if (questionText.isBlank()) {
            throw new RuntimeException("Question_Text is required");
        }

        List<String> options = new ArrayList<>();
        addOption(options, "A", normalizeImportedText(text(row, "Option_A"), mathSubject));
        addOption(options, "B", normalizeImportedText(text(row, "Option_B"), mathSubject));
        addOption(options, "C", normalizeImportedText(text(row, "Option_C"), mathSubject));
        addOption(options, "D", normalizeImportedText(text(row, "Option_D"), mathSubject));
        if (options.isEmpty()) {
            throw new RuntimeException("At least one option is required (Option_A...Option_D)");
        }

        String correct = text(row, "Correct").toUpperCase(Locale.ROOT).replaceAll("\\s+", "");
        if (correct.isBlank()) {
            throw new RuntimeException("Correct is required");
        }

        Integer difficulty = parseLegacyDifficulty(row.get("Difficulty"), level);

        CreateQuestionRequest req = new CreateQuestionRequest();
        req.setSubjectId(subject.getId());
        req.setGradeLevel(gradeLevel);
        req.setLevel(level);
        req.setGradeBand(gradeBand);
        req.setSkillType(text(row, "Skill_Tag"));
        req.setQuestionType(QuestionType.MULTIPLE_CHOICE);
        req.setQuestionText(questionText);
        req.setOptions(objectMapper.writeValueAsString(options));
        req.setCorrectAnswer(correct);
        req.setExplanation(normalizeImportedText(text(row, "Explanation"), mathSubject));
        req.setDifficulty(difficulty);
        return req;
    }

    private boolean isMathSubject(Subject subject) {
        if (subject == null || subject.getCode() == null) {
            return false;
        }
        String code = subject.getCode().trim().toUpperCase(Locale.ROOT);
        return code.equals("MATH") || code.equals("M");
    }

    private String normalizeImportedText(String raw, boolean mathSubject) {
        String text = raw == null ? "" : raw.trim();
        if (!mathSubject || text.isEmpty()) {
            return text;
        }
        return normalizeMathLatex(text);
    }

    private String normalizeMathLatex(String raw) {
        StringBuilder out = new StringBuilder();
        int i = 0;
        while (i < raw.length()) {
            char ch = raw.charAt(i);
            if ((ch == '^' || ch == '_') && i + 1 < raw.length()) {
                char next = raw.charAt(i + 1);
                if (next == '{') {
                    out.append(ch);
                    i += 1;
                    continue;
                }
                if (next == '(') {
                    int end = findClosingParen(raw, i + 1);
                    if (end > i + 1) {
                        out.append(ch).append('{').append(raw, i + 2, end).append('}');
                        i = end + 1;
                        continue;
                    }
                }
                int tokenEnd = readMathTokenEnd(raw, i + 1);
                if (tokenEnd > i + 1) {
                    out.append(ch).append('{').append(raw, i + 1, tokenEnd).append('}');
                    i = tokenEnd;
                    continue;
                }
            }
            out.append(ch);
            i += 1;
        }
        return out.toString();
    }

    private int findClosingParen(String raw, int openIndex) {
        int depth = 0;
        for (int i = openIndex; i < raw.length(); i++) {
            char ch = raw.charAt(i);
            if (ch == '(') {
                depth += 1;
            } else if (ch == ')') {
                depth -= 1;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int readMathTokenEnd(String raw, int start) {
        int i = start;
        while (i < raw.length()) {
            char ch = raw.charAt(i);
            if (Character.isLetterOrDigit(ch) || ch == '\\') {
                i += 1;
                continue;
            }
            break;
        }
        return i;
    }

    private Subject resolveSubjectByLegacyCode(String rawSubjectCode) {
        String normalized = rawSubjectCode == null ? "" : rawSubjectCode.trim().toUpperCase(Locale.ROOT);
        if (normalized.isBlank()) {
            throw new RuntimeException("Subject_Code is required");
        }

        List<String> candidates = new ArrayList<>();
        candidates.add(normalized);
        switch (normalized) {
            case "M", "TOAN", "MATH", "MATHEMATICS" -> candidates.add("MATH");
            case "V", "VAN", "NGU_VAN", "LITERATURE", "LIT" -> candidates.add("LITERATURE");
            case "E", "A", "ANH", "ENGLISH", "ENG" -> candidates.add("ENGLISH");
            default -> {
            }
        }

        for (String code : candidates) {
            Subject subject = subjectRepository.findByCode(code).orElse(null);
            if (subject != null) {
                return subject;
            }
        }
        throw new RuntimeException("Subject not found by code: " + rawSubjectCode);
    }

    private void addOption(List<String> options, String label, String value) {
        if (value != null && !value.trim().isEmpty()) {
            options.add(label + ". " + value.trim());
        }
    }

    private String text(JsonNode row, String fieldName) {
        JsonNode node = row == null ? null : row.get(fieldName);
        if (node == null || node.isNull()) {
            return "";
        }
        return node.asText("");
    }

    private Level parseLegacyLevel(JsonNode levelNode) {
        if (levelNode == null || levelNode.isNull()) {
            throw new RuntimeException("Level is required");
        }
        if (levelNode.isNumber()) {
            int v = levelNode.asInt();
            return switch (v) {
                case 1 -> Level.L1;
                case 2 -> Level.L2;
                case 3 -> Level.L3;
                default -> throw new RuntimeException("Level must be 1, 2, 3 or L1, L2, L3");
            };
        }
        String raw = levelNode.asText("").trim().toUpperCase(Locale.ROOT);
        return switch (raw) {
            case "1", "L1" -> Level.L1;
            case "2", "L2" -> Level.L2;
            case "3", "L3" -> Level.L3;
            default -> throw new RuntimeException("Level must be 1, 2, 3 or L1, L2, L3");
        };
    }

    private Integer parseLegacyGradeLevel(JsonNode classNode) {
        if (classNode == null || classNode.isNull()) {
            return 11;
        }
        if (classNode.isNumber()) {
            return classNode.asInt();
        }
        String raw = classNode.asText("").trim();
        if (raw.isEmpty()) {
            return 11;
        }
        if (isUniPrepToken(raw)) {
            // OTDH/UNI_PREP maps to university-prep band.
            // Keep gradeLevel in a valid range for storage.
            return 12;
        }
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            throw new RuntimeException("Class must be numeric (e.g. 11, 12)");
        }
    }

    private Integer parseLegacyDifficulty(JsonNode difficultyNode, Level level) {
        if (difficultyNode != null && !difficultyNode.isNull()) {
            if (difficultyNode.isInt()) {
                return Math.max(1, Math.min(5, difficultyNode.asInt()));
            }
            String raw = difficultyNode.asText("").trim();
            if (!raw.isEmpty()) {
                try {
                    return Math.max(1, Math.min(5, Integer.parseInt(raw)));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return switch (level) {
            case L1 -> 1;
            case L2 -> 3;
            case L3 -> 5;
        };
    }

    private String normalizeLegacyGradeBand(String classify, Integer gradeLevel) {
        String normalized = classify == null ? "" : classify.trim().toUpperCase(Locale.ROOT);
        if (!normalized.isBlank()) {
            if (normalized.equals("GRADE_11") || normalized.equals("11") || normalized.equals("LỚP 11") || normalized.equals("LOP 11")) {
                return "GRADE_11";
            }
            if (normalized.equals("GRADE_12") || normalized.equals("12") || normalized.equals("LỚP 12") || normalized.equals("LOP 12")) {
                return "GRADE_12";
            }
            if (normalized.contains("UNI") || normalized.contains("ÔN") || normalized.contains("ON") || normalized.contains("ĐẠI HỌC") || normalized.contains("DAI HOC")) {
                return "UNI_PREP";
            }
        }

        if (gradeLevel != null) {
            if (gradeLevel == 12) return "GRADE_12";
            if (gradeLevel == 11 || gradeLevel == 10) return "GRADE_11";
        }
        return "UNI_PREP";
    }

    private boolean isUniPrepToken(String raw) {
        if (raw == null) return false;
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        if (normalized.isBlank()) return false;
        return normalized.equals("OTDH")
            || normalized.equals("UNI_PREP")
            || normalized.contains("ÔN")
            || normalized.contains("ON")
            || normalized.contains("ĐẠI HỌC")
            || normalized.contains("DAI HOC")
            || normalized.contains("UNIVERSITY");
    }
}
