package com.compassed.compassed_api.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.compassed.compassed_api.domain.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByOauthProviderAndOauthProviderUserId(String oauthProvider, String oauthProviderUserId);

    @Query(value = """
            SELECT DATE(created_at) AS day, COUNT(*) AS total
            FROM users
            WHERE created_at IS NOT NULL
              AND DATE(created_at) BETWEEN :fromDate AND :toDate
            GROUP BY DATE(created_at)
            """, nativeQuery = true)
    List<DailyCountProjection> countDailyNewUsers(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    interface DailyCountProjection {
        LocalDate getDay();
        Long getTotal();
    }
}
