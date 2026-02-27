package com.compassed.compassed_api.api.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.compassed.compassed_api.api.dto.SubscribeRequest;
import com.compassed.compassed_api.api.dto.SubscribeResponse;
import com.compassed.compassed_api.security.CurrentUserService;
import com.compassed.compassed_api.service.SubscriptionService;

@RestController
@RequestMapping("/api")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final CurrentUserService currentUserService;

    public SubscriptionController(SubscriptionService subscriptionService, CurrentUserService currentUserService) {
        this.subscriptionService = subscriptionService;
        this.currentUserService = currentUserService;
    }

    // Payment page tick nhiều môn
    @PostMapping("/subscriptions/checkout")
    public SubscribeResponse checkout(
            @RequestBody SubscribeRequest request
    ) {
        Long userId = currentUserService.requireCurrentUserId();
        return subscriptionService.subscribeAndUnlockRoadmaps(userId, request);
    }
}
