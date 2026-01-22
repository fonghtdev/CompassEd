package com.compassed.compassed_api.service;

import com.compassed.compassed_api.api.dto.SubscribeRequest;
import com.compassed.compassed_api.api.dto.SubscribeResponse;

public interface SubscriptionService {
    SubscribeResponse subscribeAndUnlockRoadmaps(Long userId, SubscribeRequest request);

    // Optional: lấy roadmap hiện tại của 1 môn (khi user đã mua)
    // SubscribeResponse getCurrentRoadmap(Long userId, Long subjectId);
}
