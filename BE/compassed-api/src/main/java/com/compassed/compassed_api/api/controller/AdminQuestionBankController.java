package com.compassed.compassed_api.api.controller;

import com.compassed.compassed_api.api.dto.CreateQuestionRequest;
import com.compassed.compassed_api.api.dto.QuestionBankDTO;
import com.compassed.compassed_api.domain.QuestionBank.Level;
import com.compassed.compassed_api.service.QuestionBankService;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Profile("mysql")
@RequestMapping("/api/admin/questions")
public class AdminQuestionBankController {

    private final QuestionBankService questionBankService;

    public AdminQuestionBankController(QuestionBankService questionBankService) {
        this.questionBankService = questionBankService;
    }

    /**
     * GET /api/admin/questions
     * Lấy danh sách câu hỏi với filter và phân trang
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllQuestions(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Level level,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String skillTag,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<QuestionBankDTO> result = questionBankService.getAllQuestions(
            subjectId, level, className, skillTag, isActive, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("questions", result.getContent());
        response.put("currentPage", result.getNumber());
        response.put("totalItems", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/admin/questions/{id}
     * Lấy chi tiết 1 câu hỏi
     */
    @GetMapping("/{id}")
    public ResponseEntity<QuestionBankDTO> getQuestionById(@PathVariable Long id) {
        QuestionBankDTO question = questionBankService.getQuestionById(id);
        return ResponseEntity.ok(question);
    }

    /**
     * POST /api/admin/questions
     * Tạo câu hỏi mới
     */
    @PostMapping
    public ResponseEntity<QuestionBankDTO> createQuestion(@RequestBody CreateQuestionRequest request) {
        QuestionBankDTO created = questionBankService.createQuestion(request);
        return ResponseEntity.ok(created);
    }

    /**
     * PUT /api/admin/questions/{id}
     * Cập nhật câu hỏi
     */
    @PutMapping("/{id}")
    public ResponseEntity<QuestionBankDTO> updateQuestion(
            @PathVariable Long id,
            @RequestBody CreateQuestionRequest request) {
        QuestionBankDTO updated = questionBankService.updateQuestion(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/admin/questions/{id}
     * Xóa câu hỏi (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteQuestion(@PathVariable Long id) {
        questionBankService.deleteQuestion(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Question deleted successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/admin/questions/{id}/hard
     * Xóa vĩnh viễn
     */
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Map<String, String>> hardDeleteQuestion(@PathVariable Long id) {
        questionBankService.hardDeleteQuestion(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Question permanently deleted");
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/admin/questions/stats
     * Lấy thống kê câu hỏi
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getQuestionStats() {
        Map<String, Object> stats = questionBankService.getQuestionStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/admin/questions/skill-types
     * Lấy danh sách skill types theo subject và level
     */
    @GetMapping("/skill-types")
    public ResponseEntity<List<String>> getSkillTypes(
            @RequestParam Long subjectId,
            @RequestParam Level level) {
        List<String> skillTypes = questionBankService.getSkillTypesBySubjectAndLevel(subjectId, level);
        return ResponseEntity.ok(skillTypes);
    }
}
