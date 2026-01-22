package com.compassed.compassed_api.api.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.compassed.compassed_api.api.dto.SubscribeRequest;
import com.compassed.compassed_api.api.dto.SubscribeResponse;
import com.compassed.compassed_api.service.SubscriptionService;

@RestController
@RequestMapping("/api")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    // Payment page tick nhiều môn
    @PostMapping("/subscriptions/checkout")
    public SubscribeResponse checkout(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestBody SubscribeRequest request
    ) {
        return subscriptionService.subscribeAndUnlockRoadmaps(userId, request);
    }
}
