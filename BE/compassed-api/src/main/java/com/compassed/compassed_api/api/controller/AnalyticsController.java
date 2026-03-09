package com.compassed.compassed_api.api.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.compassed.compassed_api.api.dto.TrackVisitRequest;
import com.compassed.compassed_api.domain.entity.WebVisitActivity;
import com.compassed.compassed_api.repository.WebVisitActivityRepository;

@RestController
@Profile("mysql")
@RequestMapping("/api/analytics")
public class AnalyticsController {
    private final WebVisitActivityRepository webVisitActivityRepository;

    public AnalyticsController(WebVisitActivityRepository webVisitActivityRepository) {
        this.webVisitActivityRepository = webVisitActivityRepository;
    }

    @PostMapping("/visit")
    public Map<String, Object> trackVisit(@RequestBody(required = false) TrackVisitRequest request) {
        String visitorId = request == null || request.getVisitorId() == null ? "" : request.getVisitorId().trim();
        if (visitorId.isBlank()) {
            throw new RuntimeException("visitorId is required");
        }

        LocalDate today = LocalDate.now();
        if (webVisitActivityRepository.existsByVisitorIdAndVisitDate(visitorId, today)) {
            return Map.of("tracked", false, "reason", "already-tracked-today");
        }

        WebVisitActivity row = new WebVisitActivity();
        row.setVisitorId(visitorId);
        row.setVisitDate(today);
        row.setPagePath(request == null ? null : request.getPagePath());
        row.setCreatedAt(LocalDateTime.now());
        webVisitActivityRepository.save(row);

        return Map.of("tracked", true);
    }
}
