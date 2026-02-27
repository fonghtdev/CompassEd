package com.compassed.compassed_api.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.compassed.compassed_api.api.dto.SubscribeItemResponse;
import com.compassed.compassed_api.api.dto.SubscribeRequest;
import com.compassed.compassed_api.api.dto.SubscribeResponse;
import com.compassed.compassed_api.domain.enums.Level;
import com.compassed.compassed_api.local.LocalDataStore;
import com.compassed.compassed_api.local.LocalDataStore.PlacementResultMem;
import com.compassed.compassed_api.local.LocalDataStore.SubjectInfo;
import com.compassed.compassed_api.service.PricingService;
import com.compassed.compassed_api.service.SubscriptionService;

@Service
@Profile("local")
public class SubscriptionServiceLocalImpl implements SubscriptionService {

    private final LocalDataStore localDataStore;
    private final PricingService pricingService;

    public SubscriptionServiceLocalImpl(LocalDataStore localDataStore, PricingService pricingService) {
        this.localDataStore = localDataStore;
        this.pricingService = pricingService;
    }

    @Override
    public SubscribeResponse subscribeAndUnlockRoadmaps(Long userId, SubscribeRequest request) {
        if (!localDataStore.userExists(userId)) {
            throw new RuntimeException("User not found");
        }

        List<Long> subjectIds = Optional.ofNullable(request.getSubjectIds())
                .orElse(List.of())
                .stream()
                .distinct()
                .toList();
        if (subjectIds.isEmpty()) {
            throw new RuntimeException("Must select at least 1 subject");
        }

        List<SubjectInfo> subjects = localDataStore.findSubjectsByIds(subjectIds);
        if (subjects.stream().anyMatch(s -> s == null)) {
            throw new RuntimeException("Some subjects not found");
        }

        List<SubscribeItemResponse> items = new ArrayList<>();
        for (SubjectInfo subject : subjects) {
            localDataStore.activateSubscription(userId, subject.id());
            PlacementResultMem latestResult = localDataStore.getLatestResult(userId, subject.id());

            SubscribeItemResponse item = new SubscribeItemResponse();
            item.setSubjectId(subject.id());
            item.setSubjectCode(subject.code());
            item.setSubjectName(subject.name());

            if (latestResult == null) {
                item.setStatus("NEED_PLACEMENT");
            } else {
                localDataStore.initializeRoadmapProgress(userId, subject.id(), latestResult.getLevel());
                item.setStatus("ROADMAP_UNLOCKED");
                item.setRoadmapTitle(subject.name() + " - " + latestResult.getLevel().name());
                item.setRoadmapDescription(roadmapDescriptionByLevel(latestResult.getLevel()));
            }
            items.add(item);
        }

        SubscribeResponse resp = new SubscribeResponse();
        resp.setTotalSubjects(subjectIds.size());
        resp.setTotalAmountVnd(pricingService.calculateTotalAmountVnd(subjectIds.size()));
        resp.setItems(items);
        return resp;
    }

    private String roadmapDescriptionByLevel(Level level) {
        return switch (level) {
            case L1 -> "Strengthen core knowledge";
            case L2 -> "Practice mixed question types";
            case L3 -> "Advanced drills and score optimization";
        };
    }
}
