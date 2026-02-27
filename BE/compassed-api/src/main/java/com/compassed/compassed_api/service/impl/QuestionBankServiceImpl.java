package com.compassed.compassed_api.service.impl;

import com.compassed.compassed_api.api.dto.CreateQuestionRequest;
import com.compassed.compassed_api.api.dto.QuestionBankDTO;
import com.compassed.compassed_api.domain.QuestionBank;
import com.compassed.compassed_api.domain.QuestionBank.Level;
import com.compassed.compassed_api.domain.entity.Subject;
import com.compassed.compassed_api.repository.QuestionBankRepository;
import com.compassed.compassed_api.repository.SubjectRepository;
import com.compassed.compassed_api.service.QuestionBankService;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Profile("mysql")
@Transactional
public class QuestionBankServiceImpl implements QuestionBankService {

    private final QuestionBankRepository questionBankRepository;
    private final SubjectRepository subjectRepository;

    public QuestionBankServiceImpl(QuestionBankRepository questionBankRepository, 
                                  SubjectRepository subjectRepository) {
        this.questionBankRepository = questionBankRepository;
        this.subjectRepository = subjectRepository;
    }

    @Override
    public Page<QuestionBankDTO> getAllQuestions(Long subjectId, Level level, String skillType, 
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

    private QuestionBankDTO convertToDTO(QuestionBank question) {
        QuestionBankDTO dto = new QuestionBankDTO();
        dto.setId(question.getId());
        dto.setSubjectId(question.getSubject().getId());
        dto.setSubjectName(question.getSubject().getName());
        dto.setLevel(question.getLevel());
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
}
