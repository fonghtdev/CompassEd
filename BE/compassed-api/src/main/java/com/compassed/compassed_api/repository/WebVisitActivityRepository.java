package com.compassed.compassed_api.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.compassed.compassed_api.domain.entity.WebVisitActivity;

public interface WebVisitActivityRepository extends JpaRepository<WebVisitActivity, Long> {
    boolean existsByVisitorIdAndVisitDate(String visitorId, LocalDate visitDate);

    @Query(value = """
            SELECT visit_date AS day, COUNT(*) AS total
            FROM web_visit_activities
            WHERE visit_date BETWEEN :fromDate AND :toDate
            GROUP BY visit_date
            """, nativeQuery = true)
    List<DailyCountProjection> countDailyVisits(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    interface DailyCountProjection {
        LocalDate getDay();
        Long getTotal();
    }
}
