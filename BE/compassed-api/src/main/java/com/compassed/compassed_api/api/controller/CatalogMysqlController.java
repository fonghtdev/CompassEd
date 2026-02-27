package com.compassed.compassed_api.api.controller;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.compassed.compassed_api.api.dto.PricingPlanResponse;
import com.compassed.compassed_api.api.dto.SubjectResponse;
import com.compassed.compassed_api.repository.SubjectRepository;
import com.compassed.compassed_api.service.PricingService;

@RestController
@Profile("mysql")
@RequestMapping("/api")
public class CatalogMysqlController {

    private final SubjectRepository subjectRepository;
    private final PricingService pricingService;

    public CatalogMysqlController(SubjectRepository subjectRepository, PricingService pricingService) {
        this.subjectRepository = subjectRepository;
        this.pricingService = pricingService;
    }

    @GetMapping("/subjects")
    public List<SubjectResponse> subjects() {
        return subjectRepository.findAll().stream()
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .map(s -> new SubjectResponse(s.getId(), s.getCode(), s.getName()))
                .toList();
    }

    @GetMapping("/pricing/plans")
    public List<PricingPlanResponse> pricingPlans() {
        return List.of(
                new PricingPlanResponse(1, pricingService.calculateTotalAmountVnd(1)),
                new PricingPlanResponse(2, pricingService.calculateTotalAmountVnd(2)),
                new PricingPlanResponse(3, pricingService.calculateTotalAmountVnd(3)));
    }
}
