// package com.compassed.compassed_api;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.junit.jupiter.api.Assertions.assertTrue;

// import java.util.LinkedHashMap;
// import java.util.List;
// import java.util.Map;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;

// import com.compassed.compassed_api.api.dto.PlacementStartResponse;
// import com.compassed.compassed_api.api.dto.PlacementSubmitRequest;
// import com.compassed.compassed_api.api.dto.RoadmapResponse;
// import com.compassed.compassed_api.api.dto.SubscribeRequest;
// import com.compassed.compassed_api.local.LocalDataStore;
// import com.compassed.compassed_api.service.impl.PlacementServiceLocalImpl;
// import com.compassed.compassed_api.service.impl.PricingServiceImpl;
// import com.compassed.compassed_api.service.impl.RoadmapServiceLocalImpl;
// import com.compassed.compassed_api.service.impl.SubscriptionServiceLocalImpl;
// import com.fasterxml.jackson.core.type.TypeReference;
// import com.fasterxml.jackson.databind.ObjectMapper;

// class LocalFlowBusinessRulesTests {

// private final ObjectMapper objectMapper = new ObjectMapper();
// private LocalDataStore localDataStore;
// private PlacementServiceLocalImpl placementService;
// private RoadmapServiceLocalImpl roadmapService;
// private SubscriptionServiceLocalImpl subscriptionService;

// @BeforeEach
// void setUp() {
// localDataStore = new LocalDataStore();
// placementService = new PlacementServiceLocalImpl(localDataStore,
// objectMapper, null);
// roadmapService = new RoadmapServiceLocalImpl(localDataStore);
// subscriptionService = new SubscriptionServiceLocalImpl(localDataStore, new
// PricingServiceImpl());
// }

// @Test
// void pricingShouldMatchBusinessTiers() {
// PricingServiceImpl pricing = new PricingServiceImpl();

// assertEquals(50_000L, pricing.calculateTotalAmountVnd(1));
// assertEquals(90_000L, pricing.calculateTotalAmountVnd(2));
// assertEquals(130_000L, pricing.calculateTotalAmountVnd(3));
// }

// @Test
// void placementShouldAllowOnlyOneFreeAttemptPerSubjectWithoutSubscription() {
// Long userId = localDataStore.createUser("student1@compassed.local", "Student
// One").getId();

// PlacementStartResponse first = placementService.startPlacement(userId, 1L);
// assertNotNull(first.getAttemptId());

// RuntimeException secondAttemptError = assertThrows(
// RuntimeException.class,
// () -> placementService.startPlacement(userId, 1L));
// assertTrue(secondAttemptError.getMessage().contains("PAYMENT_REQUIRED"));
// }

// @Test
// void roadmapShouldBeLockedThenWaitingPlacementThenLessons() throws Exception
// {
// Long userId = localDataStore.createUser("student2@compassed.local", "Student
// Two").getId();

// RoadmapResponse locked = roadmapService.getRoadmap(userId, 1L);
// assertEquals("LOCKED", locked.getPhase());
// assertEquals(false, locked.getSubscribed());

// SubscribeRequest subscribeRequest = new SubscribeRequest();
// subscribeRequest.setSubjectIds(List.of(1L));
// subscriptionService.subscribeAndUnlockRoadmaps(userId, subscribeRequest);

// RoadmapResponse waitingPlacement = roadmapService.getRoadmap(userId, 1L);
// assertEquals("WAITING_PLACEMENT", waitingPlacement.getPhase());
// assertEquals(true, waitingPlacement.getSubscribed());
// assertEquals(false, waitingPlacement.getPlacementReady());

// PlacementStartResponse started = placementService.startPlacement(userId, 1L);
// List<Map<String, Object>> paper = objectMapper.readValue(
// started.getPaperJson(),
// new TypeReference<List<Map<String, Object>>>() {
// });
// Map<String, String> answers = new LinkedHashMap<>();
// for (Map<String, Object> q : paper) {
// answers.put(String.valueOf(q.get("id")), "A");
// }
// PlacementSubmitRequest submitRequest = new PlacementSubmitRequest();
// submitRequest.setAnswersJson(objectMapper.writeValueAsString(answers));
// placementService.submitPlacement(userId, started.getAttemptId(),
// submitRequest);

// RoadmapResponse lessons = roadmapService.getRoadmap(userId, 1L);
// assertEquals("LESSONS", lessons.getPhase());
// assertEquals(true, lessons.getSubscribed());
// assertEquals(true, lessons.getPlacementReady());
// assertNotNull(lessons.getLevel());
// assertEquals(5, lessons.getLessons().size());
// }

// @Test
// void getLessonDetailShouldFailWhenLessonIdIsAmbiguousAcrossSubjects() throws
// Exception {
// Long userId = localDataStore.createUser("student3@compassed.local", "Student
// Three").getId();

// SubscribeRequest subscribeRequest = new SubscribeRequest();
// subscribeRequest.setSubjectIds(List.of(1L, 2L));
// subscriptionService.subscribeAndUnlockRoadmaps(userId, subscribeRequest);

// completePlacement(userId, 1L);
// completePlacement(userId, 2L);

// RuntimeException error = assertThrows(
// RuntimeException.class,
// () -> roadmapService.getLessonDetail(userId, 1L));
// assertTrue(error.getMessage().contains("Ambiguous lessonId"));
// }

// private void completePlacement(Long userId, Long subjectId) throws Exception
// {
// PlacementStartResponse started = placementService.startPlacement(userId,
// subjectId);
// List<Map<String, Object>> paper = objectMapper.readValue(
// started.getPaperJson(),
// new TypeReference<List<Map<String, Object>>>() {
// });
// Map<String, String> answers = new LinkedHashMap<>();
// for (Map<String, Object> q : paper) {
// answers.put(String.valueOf(q.get("id")), "A");
// }

// PlacementSubmitRequest submitRequest = new PlacementSubmitRequest();
// submitRequest.setAnswersJson(objectMapper.writeValueAsString(answers));
// placementService.submitPlacement(userId, started.getAttemptId(),
// submitRequest);
// }
// }
