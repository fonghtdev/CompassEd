package com.compassed.compassed_api.repository;

import com.compassed.compassed_api.domain.QuestionBank;
import com.compassed.compassed_api.domain.QuestionBank.Level;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface QuestionBankRepository extends JpaRepository<QuestionBank, Long>, JpaSpecificationExecutor<QuestionBank> {

    /**
     * Tìm câu hỏi theo subject và level
     */
    List<QuestionBank> findBySubjectIdAndLevelAndIsActiveTrue(Long subjectId, Level level);

    /**
     * Tìm câu hỏi theo subject, level và skill type
     */
    List<QuestionBank> findBySubjectIdAndLevelAndSkillTypeAndIsActiveTrue(
            Long subjectId, Level level, String skillType);

    /**
     * Random N câu hỏi theo subject và level
     */
    @Query(value = "SELECT * FROM question_bank " +
            "WHERE subject_id = :subjectId " +
            "AND level = :level " +
            "AND is_active = 1 " +
            "ORDER BY RAND()", nativeQuery = true)
    List<QuestionBank> findRandomQuestions(
            @Param("subjectId") Long subjectId,
            @Param("level") String level,
            Pageable pageable);

    /**
     * Random N câu hỏi theo subject, level và skill type
     */
    @Query(value = "SELECT * FROM question_bank " +
            "WHERE subject_id = :subjectId " +
            "AND level = :level " +
            "AND skill_type = :skillType " +
            "AND is_active = 1 " +
            "ORDER BY RAND()", nativeQuery = true)
    List<QuestionBank> findRandomQuestionsBySkill(
            @Param("subjectId") Long subjectId,
            @Param("level") String level,
            @Param("skillType") String skillType,
            Pageable pageable);

    /**
     * Đếm số câu hỏi theo subject và level
     */
    long countBySubjectIdAndLevelAndIsActiveTrue(Long subjectId, Level level);

    /**
     * Tìm tất cả skill types của một subject và level
     */
    @Query("SELECT DISTINCT q.skillType FROM QuestionBank q " +
            "WHERE q.subject.id = :subjectId AND q.level = :level AND q.isActive = true")
    List<String> findDistinctSkillTypes(@Param("subjectId") Long subjectId, @Param("level") Level level);
    
    /**
     * Đếm số câu hỏi active
     */
    long countByIsActiveTrue();
    
    /**
     * Đếm số câu hỏi theo level và active
     */
    long countByLevelAndIsActiveTrue(Level level);
    
    /**
     * Thống kê câu hỏi theo subject
     */
    @Query(value = "SELECT s.name as subjectName, COUNT(qb.id) as count " +
            "FROM question_bank qb " +
            "JOIN subjects s ON qb.subject_id = s.id " +
            "WHERE qb.is_active = 1 " +
            "GROUP BY s.id, s.name", nativeQuery = true)
    List<Map<String, Object>> countBySubject();
}
