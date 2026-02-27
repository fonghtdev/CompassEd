package com.compassed.compassed_api.api.controller;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.compassed.compassed_api.api.dto.PricingPlanResponse;
import com.compassed.compassed_api.service.PricingService;

@RestController
@Profile("mysql")
@RequestMapping("/api")
public class CatalogMysqlController {

    private final PricingService pricingService;

    public CatalogMysqlController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @GetMapping("/pricing/plans")
    public List<PricingPlanResponse> pricingPlans() {
        return List.of(
                new PricingPlanResponse(1, pricingService.calculateTotalAmountVnd(1)),
                new PricingPlanResponse(2, pricingService.calculateTotalAmountVnd(2)),
                new PricingPlanResponse(3, pricingService.calculateTotalAmountVnd(3)));
    }
}
