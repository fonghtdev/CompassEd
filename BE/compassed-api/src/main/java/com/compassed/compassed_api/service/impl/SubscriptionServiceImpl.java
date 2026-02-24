package com.compassed.compassed_api.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.compassed.compassed_api.api.dto.SubscribeItemResponse;
import com.compassed.compassed_api.api.dto.SubscribeRequest;
import com.compassed.compassed_api.api.dto.SubscribeResponse;
import com.compassed.compassed_api.domain.entity.PlacementResult;
import com.compassed.compassed_api.domain.entity.Roadmap;
import com.compassed.compassed_api.domain.entity.Subject;
import com.compassed.compassed_api.domain.entity.Subscription;
import com.compassed.compassed_api.domain.entity.User;
import com.compassed.compassed_api.domain.entity.UserRoadmapAssignment;
import com.compassed.compassed_api.domain.enums.Level;
import com.compassed.compassed_api.repository.PlacementResultRepository;
import com.compassed.compassed_api.repository.RoadmapRepository;
import com.compassed.compassed_api.repository.SubjectRepository;
import com.compassed.compassed_api.repository.SubscriptionRepository;
import com.compassed.compassed_api.repository.UserRepository;
import com.compassed.compassed_api.repository.UserRoadmapAssignmentRepository;
import com.compassed.compassed_api.service.PricingService;
import com.compassed.compassed_api.service.SubscriptionService;

@Service
@Profile("db")
public class SubscriptionServiceImpl implements SubscriptionService {

    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PlacementResultRepository placementResultRepository;
    private final RoadmapRepository roadmapRepository;
    private final UserRoadmapAssignmentRepository assignmentRepository;
    private final PricingService pricingService;

    public SubscriptionServiceImpl(
            UserRepository userRepository,
            SubjectRepository subjectRepository,
            SubscriptionRepository subscriptionRepository,
            PlacementResultRepository placementResultRepository,
            RoadmapRepository roadmapRepository,
            UserRoadmapAssignmentRepository assignmentRepository,
            PricingService pricingService) {
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.placementResultRepository = placementResultRepository;
        this.roadmapRepository = roadmapRepository;
        this.assignmentRepository = assignmentRepository;
        this.pricingService = pricingService;
    }

    @Override
    public SubscribeResponse subscribeAndUnlockRoadmaps(Long userId, SubscribeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Long> subjectIds = Optional.ofNullable(request.getSubjectIds())
                .orElse(List.of())
                .stream()
                .distinct()
                .collect(Collectors.toList());

        if (subjectIds.isEmpty()) {
            throw new RuntimeException("Must select at least 1 subject");
        }

        // Validate tồn tại subjects
        List<Subject> subjects = subjectRepository.findAllById(subjectIds);
        if (subjects.size() != subjectIds.size()) {
            throw new RuntimeException("Some subjects not found");
        }

        // Tính tiền theo số môn (1/2/3)
        long totalAmount = pricingService.calculateTotalAmountVnd(subjectIds.size());

        // V1: giả lập payment OK luôn (sau này gắn VNPay)
        // tạo/active subscription từng môn
        List<SubscribeItemResponse> items = new ArrayList<>();

        for (Subject subject : subjects) {
            Subscription sub = subscriptionRepository.findByUser_IdAndSubject_Id(userId, subject.getId())
                    .orElseGet(Subscription::new);

            sub.setUser(user);
            sub.setSubject(subject);
            sub.setActive(true);
            sub.setActivatedAt(LocalDateTime.now());
            subscriptionRepository.save(sub);

            // Nếu có placement thì assign roadmap theo level
            Optional<PlacementResult> lastPlacementOpt = placementResultRepository
                    .findTopByUser_IdAndSubject_IdOrderByCreatedAtDesc(userId, subject.getId());

            SubscribeItemResponse item = new SubscribeItemResponse();
            item.setSubjectId(subject.getId());
            item.setSubjectCode(subject.getCode());
            item.setSubjectName(subject.getName());

            if (lastPlacementOpt.isPresent()) {
                Level level = lastPlacementOpt.get().getLevel();
                Roadmap roadmap = roadmapRepository.findBySubject_IdAndLevel(subject.getId(), level)
                        .orElseThrow(() -> new RuntimeException(
                                "Roadmap not found for subject=" + subject.getId() + ", level=" + level));

                UserRoadmapAssignment assignment = assignmentRepository
                        .findByUser_IdAndSubject_Id(userId, subject.getId())
                        .orElseGet(UserRoadmapAssignment::new);

                assignment.setUser(user);
                assignment.setSubject(subject);
                assignment.setRoadmap(roadmap);
                assignment.setAssignedAt(LocalDateTime.now());
                assignmentRepository.save(assignment);

                item.setStatus("ROADMAP_UNLOCKED");
                item.setRoadmapTitle(roadmap.getTitle());
                item.setRoadmapDescription(roadmap.getDescription());
            } else {
                // Chưa có placement => chưa assign roadmap
                item.setStatus("NEED_PLACEMENT");
                item.setRoadmapTitle(null);
                item.setRoadmapDescription(null);
            }

            items.add(item);
        }

        SubscribeResponse resp = new SubscribeResponse();
        resp.setTotalSubjects(subjectIds.size());
        resp.setTotalAmountVnd(totalAmount);
        resp.setItems(items);
        return resp;
    }
}
