package com.compassed.compassed_api.api.controller;

import com.compassed.compassed_api.api.dto.QuestionBankDTO;
import com.compassed.compassed_api.domain.QuestionBank.Level;
import com.compassed.compassed_api.service.QuestionBankService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = "*")
public class UserQuestionBankController {

    private final QuestionBankService questionBankService;

    public UserQuestionBankController(QuestionBankService questionBankService) {
        this.questionBankService = questionBankService;
    }

    /**
     * GET /api/questions
     * Public API for users to fetch active questions
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getQuestions(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) String gradeBand,
            @RequestParam(required = false) Level level,
            @RequestParam(required = false) String skillType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<QuestionBankDTO> result = questionBankService.getAllQuestions(
                subjectId, level, gradeBand, skillType, true, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("questions", result.getContent());
        response.put("currentPage", result.getNumber());
        response.put("totalItems", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());

        return ResponseEntity.ok(response);
    }
}
