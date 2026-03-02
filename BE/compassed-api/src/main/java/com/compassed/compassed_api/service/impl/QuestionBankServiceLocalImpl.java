package com.compassed.compassed_api.service.impl;

import com.compassed.compassed_api.api.dto.CreateQuestionRequest;
import com.compassed.compassed_api.api.dto.QuestionBankDTO;
import com.compassed.compassed_api.domain.QuestionBank.Level;
import com.compassed.compassed_api.service.QuestionBankService;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Profile("local")
public class QuestionBankServiceLocalImpl implements QuestionBankService {

    @Override
    public Page<QuestionBankDTO> getAllQuestions(Long subjectId, Level level, String skillType, Boolean isActive, Integer gradeLevel, Pageable pageable) {
        // TODO: Implement with LocalDataStore
        return new PageImpl<>(new ArrayList<>(), pageable, 0);
    }

    @Override
    public QuestionBankDTO getQuestionById(Long id) {
        // TODO: Implement with LocalDataStore
        throw new UnsupportedOperationException("getQuestionById not yet implemented for local profile");
    }

    @Override
    public QuestionBankDTO createQuestion(CreateQuestionRequest request) {
        // TODO: Implement with LocalDataStore
        throw new UnsupportedOperationException("createQuestion not yet implemented for local profile");
    }

    @Override
    public QuestionBankDTO updateQuestion(Long id, CreateQuestionRequest request) {
        // TODO: Implement with LocalDataStore
        throw new UnsupportedOperationException("updateQuestion not yet implemented for local profile");
    }

    @Override
    public void deleteQuestion(Long id) {
        // TODO: Implement with LocalDataStore
        throw new UnsupportedOperationException("deleteQuestion not yet implemented for local profile");
    }

    @Override
    public void hardDeleteQuestion(Long id) {
        // TODO: Implement with LocalDataStore
        throw new UnsupportedOperationException("hardDeleteQuestion not yet implemented for local profile");
    }

    @Override
    public Map<String, Object> getQuestionStats() {
        // TODO: Implement with LocalDataStore
        return new HashMap<>();
    }

    @Override
    public List<String> getSkillTypesBySubjectAndLevel(Long subjectId, Level level) {
        // TODO: Implement with LocalDataStore
        return new ArrayList<>();
    }
}
