package com.compassed.compassed_api.service.impl;

import com.compassed.compassed_api.api.dto.CreateQuestionRequest;
import com.compassed.compassed_api.api.dto.QuestionBankDTO;
import com.compassed.compassed_api.domain.QuestionBank;
import com.compassed.compassed_api.domain.QuestionBank.Level;
import com.compassed.compassed_api.domain.entity.Subject;
import com.compassed.compassed_api.repository.QuestionBankRepository;
import com.compassed.compassed_api.repository.SubjectRepository;
import com.compassed.compassed_api.service.QuestionBankService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
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
    public Page<QuestionBankDTO> getAllQuestions(Long subjectId, Level level, String className, String skillTag, 
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

        if (className != null && !className.isBlank()) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get("className"), className.trim()));
        }
        
        if (skillTag != null && !skillTag.isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.like(root.get("skillTag"), "%" + skillTag + "%"));
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
        question.setQuestionId(request.getQuestionId());
        question.setSubjectCode(request.getSubjectCode());
        question.setLevel(request.getLevel());
        question.setSkillTag(request.getSkillTag());
        question.setQuestionText(request.getQuestionText());
        question.setOptionA(request.getOptionA());
        question.setOptionB(request.getOptionB());
        question.setOptionC(request.getOptionC());
        question.setOptionD(request.getOptionD());
        question.setCorrectAnswer(request.getCorrectAnswer());
        question.setClassName(request.getClassName());
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

        if (request.getQuestionId() != null) question.setQuestionId(request.getQuestionId());
        if (request.getSubjectCode() != null) question.setSubjectCode(request.getSubjectCode());
        if (request.getLevel() != null) question.setLevel(request.getLevel());
        if (request.getSkillTag() != null) question.setSkillTag(request.getSkillTag());
        if (request.getQuestionText() != null) question.setQuestionText(request.getQuestionText());
        if (request.getOptionA() != null) question.setOptionA(request.getOptionA());
        if (request.getOptionB() != null) question.setOptionB(request.getOptionB());
        if (request.getOptionC() != null) question.setOptionC(request.getOptionC());
        if (request.getOptionD() != null) question.setOptionD(request.getOptionD());
        if (request.getCorrectAnswer() != null) question.setCorrectAnswer(request.getCorrectAnswer());
        if (request.getClassName() != null) question.setClassName(request.getClassName());

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
        dto.setQuestionId(question.getQuestionId());
        dto.setSubjectCode(question.getSubjectCode());
        dto.setLevel(question.getLevel());
        dto.setSkillTag(question.getSkillTag());
        dto.setQuestionText(question.getQuestionText());
        dto.setOptionA(question.getOptionA());
        dto.setOptionB(question.getOptionB());
        dto.setOptionC(question.getOptionC());
        dto.setOptionD(question.getOptionD());
        dto.setCorrectAnswer(question.getCorrectAnswer());
        dto.setClassName(question.getClassName());
        dto.setIsActive(question.getIsActive());
        return dto;
    }
}
