package com.compassed.compassed_api.service;

import com.compassed.compassed_api.api.dto.CreateQuestionRequest;
import com.compassed.compassed_api.api.dto.QuestionBankDTO;
import com.compassed.compassed_api.domain.QuestionBank.Level;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface QuestionBankService {
    
    /**
     * Lấy tất cả câu hỏi với phân trang và filter
     */
    Page<QuestionBankDTO> getAllQuestions(Long subjectId, Level level, String className, String skillTag, Boolean isActive, Pageable pageable);
    
    /**
     * Lấy chi tiết 1 câu hỏi
     */
    QuestionBankDTO getQuestionById(Long id);
    
    /**
     * Tạo câu hỏi mới
     */
    QuestionBankDTO createQuestion(CreateQuestionRequest request);
    
    /**
     * Cập nhật câu hỏi
     */
    QuestionBankDTO updateQuestion(Long id, CreateQuestionRequest request);
    
    /**
     * Xóa câu hỏi (soft delete - set isActive = false)
     */
    void deleteQuestion(Long id);
    
    /**
     * Xóa vĩnh viễn
     */
    void hardDeleteQuestion(Long id);
    
    /**
     * Lấy thống kê câu hỏi
     */
    Map<String, Object> getQuestionStats();
    
    /**
     * Lấy danh sách skill types theo subject và level
     */
    List<String> getSkillTypesBySubjectAndLevel(Long subjectId, Level level);
}
