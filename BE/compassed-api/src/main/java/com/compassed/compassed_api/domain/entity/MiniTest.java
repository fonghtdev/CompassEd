package com.compassed.compassed_api.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "mini_tests")
public class MiniTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String questions;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String level;

    @Column(nullable = false)
    private Integer lessonId;

    // Bổ sung các trường mới
    @Column(name = "module_id")
    private Long moduleId;

    @Column(name = "questions_json", columnDefinition = "TEXT")
    private String questionsJson;

    @Column(name = "pass_threshold")
    private Integer passThreshold;

    // Getter/Setter
    public Long getModuleId() { return moduleId; }
    public void setModuleId(Long moduleId) { this.moduleId = moduleId; }

    public String getQuestionsJson() { return questionsJson; }
    public void setQuestionsJson(String questionsJson) { this.questionsJson = questionsJson; }

    public Integer getPassThreshold() { return passThreshold; }
    public void setPassThreshold(Integer passThreshold) { this.passThreshold = passThreshold; }

    // ...existing code...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getQuestions() { return questions; }
    public void setQuestions(String questions) { this.questions = questions; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public Integer getLessonId() { return lessonId; }
    public void setLessonId(Integer lessonId) { this.lessonId = lessonId; }
}
