package com.compassed.compassed_api.api.controller;

import com.compassed.compassed_api.domain.entity.Subject;
import com.compassed.compassed_api.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Profile("removed-local")
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Profile("mysql")
public class SubjectController {

    private final SubjectRepository subjectRepository;

    @GetMapping
    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    @GetMapping("/{id}")
    public Subject getSubjectById(@PathVariable Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subject not found with id: " + id));
    }

    @GetMapping("/code/{code}")
    public Subject getSubjectByCode(@PathVariable String code) {
        return subjectRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Subject not found with code: " + code));
    }
}

