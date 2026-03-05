package com.compassed.compassed_api.local;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import com.compassed.compassed_api.domain.entity.User;
import com.compassed.compassed_api.domain.enums.AttemptStatus;
import com.compassed.compassed_api.domain.enums.Level;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class LocalDataStore {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public record SubjectInfo(Long id, String code, String name) {
    }

    public record PlacementHistoryItem(
            Long attemptId,
            Long subjectId,
            String subjectCode,
            String subjectName,
            Double scorePercent,
            String level,
            LocalDateTime submittedAt) {
    }

    public static class PlacementAttemptMem {
        private Long id;
        private Long userId;
        private Long subjectId;
        private String paperJson;
        private String answersJson;
        private AttemptStatus status;
        private LocalDateTime startedAt;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Long getSubjectId() {
            return subjectId;
        }

        public void setSubjectId(Long subjectId) {
            this.subjectId = subjectId;
        }

        public String getPaperJson() {
            return paperJson;
        }

        public void setPaperJson(String paperJson) {
            this.paperJson = paperJson;
        }

        public String getAnswersJson() {
            return answersJson;
        }

        public void setAnswersJson(String answersJson) {
            this.answersJson = answersJson;
        }

        public AttemptStatus getStatus() {
            return status;
        }

        public void setStatus(AttemptStatus status) {
            this.status = status;
        }

        public LocalDateTime getStartedAt() {
            return startedAt;
        }

        public void setStartedAt(LocalDateTime startedAt) {
            this.startedAt = startedAt;
        }
    }

    public static class PlacementResultMem {
        private Level level;
        private Double scorePercent;
        private String skillAnalysisJson;

        public Level getLevel() {
            return level;
        }

        public void setLevel(Level level) {
            this.level = level;
        }

        public Double getScorePercent() {
            return scorePercent;
        }

        public void setScorePercent(Double scorePercent) {
            this.scorePercent = scorePercent;
        }

        public String getSkillAnalysisJson() {
            return skillAnalysisJson;
        }

        public void setSkillAnalysisJson(String skillAnalysisJson) {
            this.skillAnalysisJson = skillAnalysisJson;
        }
    }

    public static class RoadmapProgressMem {
        private Level currentLevel;
        private String phase;
        private int replanCount;
        private final Map<String, Set<Long>> completedLessonsByLevel = new HashMap<>();
        private final Map<String, Map<Long, Integer>> miniScoresByLevel = new HashMap<>();
        private final Map<String, Integer> finalScoresByLevel = new HashMap<>();

        public Level getCurrentLevel() {
            return currentLevel;
        }

        public void setCurrentLevel(Level currentLevel) {
            this.currentLevel = currentLevel;
        }

        public String getPhase() {
            return phase;
        }

        public void setPhase(String phase) {
            this.phase = phase;
        }

        public int getReplanCount() {
            return replanCount;
        }

        public void setReplanCount(int replanCount) {
            this.replanCount = replanCount;
        }

        public Set<Long> getCompletedLessons(String levelKey) {
            return completedLessonsByLevel.computeIfAbsent(levelKey, k -> new HashSet<>());
        }

        public Map<Long, Integer> getMiniScores(String levelKey) {
            return miniScoresByLevel.computeIfAbsent(levelKey, k -> new HashMap<>());
        }

        public Integer getFinalScore(String levelKey) {
            return finalScoresByLevel.get(levelKey);
        }

        public void setFinalScore(String levelKey, Integer score) {
            finalScoresByLevel.put(levelKey, score);
        }

        public void resetLevelProgress(String levelKey) {
            completedLessonsByLevel.remove(levelKey);
            miniScoresByLevel.remove(levelKey);
            finalScoresByLevel.remove(levelKey);
        }
    }

    private final AtomicLong userIdSeq = new AtomicLong(1);
    private final AtomicLong attemptIdSeq = new AtomicLong(1);

    private final ConcurrentMap<Long, User> users = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> userIdByEmail = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, String> passwordHashByUserId = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> userIdByProviderIdentity = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> userIdBySessionToken = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, PlacementAttemptMem> attempts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, PlacementResultMem> latestResultsByUserSubject = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, List<PlacementHistoryItem>> placementHistoryByUser = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, RoadmapProgressMem> roadmapProgressByUserSubject = new ConcurrentHashMap<>();
    private final Set<String> usedFreeAttemptKeys = ConcurrentHashMap.newKeySet();
    private final Set<String> activeSubscriptionKeys = ConcurrentHashMap.newKeySet();

    private final Map<Long, SubjectInfo> subjects = Map.of(
            1L, new SubjectInfo(1L, "MATH", "Math"),
            2L, new SubjectInfo(2L, "LITERATURE", "Literature"),
            3L, new SubjectInfo(3L, "ENGLISH", "English"));

    public User createUser(String email, String fullName) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required");
        }
        String normalizedEmail = email.trim().toLowerCase();
        if (userIdByEmail.containsKey(normalizedEmail)) {
            throw new RuntimeException("Email already exists");
        }
        User user = new User();
        user.setId(userIdSeq.getAndIncrement());
        user.setEmail(normalizedEmail);
        user.setFullName(fullName);
        users.put(user.getId(), user);
        userIdByEmail.put(normalizedEmail, user.getId());
        return user;
    }

    public User createLocalAuthUser(String email, String fullName, String passwordHash) {
        User user = createUser(email, fullName);
        passwordHashByUserId.put(user.getId(), passwordHash);
        return user;
    }

    public User findUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        Long userId = userIdByEmail.get(email.trim().toLowerCase());
        return userId == null ? null : users.get(userId);
    }

    public String getPasswordHash(Long userId) {
        return passwordHashByUserId.get(userId);
    }

    public User upsertOAuthUser(String provider, String providerUserId, String email, String fullName) {
        if (provider == null || providerUserId == null) {
            throw new RuntimeException("Provider identity is required");
        }
        String providerKey = provider.trim().toLowerCase() + ":" + providerUserId.trim();
        Long existingId = userIdByProviderIdentity.get(providerKey);
        if (existingId != null) {
            User existing = users.get(existingId);
            if (existing != null) {
                if (fullName != null && !fullName.isBlank()) {
                    existing.setFullName(fullName);
                }
                return existing;
            }
        }

        User existingByEmail = findUserByEmail(email);
        User user;
        if (existingByEmail != null) {
            user = existingByEmail;
        } else {
            user = createUser(email, fullName);
        }
        userIdByProviderIdentity.put(providerKey, user.getId());
        return user;
    }

    public String createSessionToken(Long userId) {
        String token = "sess_" + java.util.UUID.randomUUID();
        userIdBySessionToken.put(token, userId);
        return token;
    }

    public User findUserBySessionToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        Long userId = userIdBySessionToken.get(token.trim());
        return userId == null ? null : users.get(userId);
    }

    public boolean userExists(Long userId) {
        return users.containsKey(userId);
    }

    public User getOrCreateUser(Long userId, String email, String fullName) {
        if (users.containsKey(userId)) {
            return users.get(userId);
        }
        // Auto-create user
        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        user.setFullName(fullName);
        users.put(userId, user);
        userIdByEmail.put(email.toLowerCase(), userId);
        return user;
    }

    public User getUser(Long userId) {
        return users.get(userId);
    }

    public SubjectInfo getSubject(Long subjectId) {
        return subjects.get(subjectId);
    }

    public List<SubjectInfo> findSubjectsByIds(List<Long> ids) {
        return ids.stream().map(subjects::get).toList();
    }

    public List<SubjectInfo> getAllSubjects() {
        return subjects.values().stream().sorted((a, b) -> Long.compare(a.id(), b.id())).toList();
    }

    public Long nextAttemptId() {
        return attemptIdSeq.getAndIncrement();
    }

    public void saveAttempt(PlacementAttemptMem attempt) {
        attempts.put(attempt.getId(), attempt);
    }

    public PlacementAttemptMem getAttempt(Long attemptId) {
        return attempts.get(attemptId);
    }

    public PlacementAttemptMem findLatestInProgressAttempt(Long userId, Long subjectId) {
        return attempts.values().stream()
                .filter(a -> a != null
                        && userId.equals(a.getUserId())
                        && subjectId.equals(a.getSubjectId())
                        && a.getStatus() == AttemptStatus.IN_PROGRESS)
                .sorted((a, b) -> {
                    LocalDateTime ta = a.getStartedAt();
                    LocalDateTime tb = b.getStartedAt();
                    if (ta == null && tb == null) return 0;
                    if (ta == null) return 1;
                    if (tb == null) return -1;
                    return tb.compareTo(ta);
                })
                .findFirst()
                .orElse(null);
    }

    public boolean markFreeAttemptUsed(Long userId, Long subjectId) {
        return usedFreeAttemptKeys.add(userSubjectKey(userId, subjectId));
    }

    public boolean hasUsedFreeAttempt(Long userId, Long subjectId) {
        return usedFreeAttemptKeys.contains(userSubjectKey(userId, subjectId));
    }

    public boolean hasActiveSubscription(Long userId, Long subjectId) {
        return activeSubscriptionKeys.contains(userSubjectKey(userId, subjectId));
    }

    public void activateSubscription(Long userId, Long subjectId) {
        activeSubscriptionKeys.add(userSubjectKey(userId, subjectId));
    }

    public void saveLatestResult(Long userId, Long subjectId, PlacementResultMem result) {
        latestResultsByUserSubject.put(userSubjectKey(userId, subjectId), result);
    }

    public PlacementResultMem getLatestResult(Long userId, Long subjectId) {
        return latestResultsByUserSubject.get(userSubjectKey(userId, subjectId));
    }

    public void recordPlacementHistory(
            Long userId,
            Long attemptId,
            Long subjectId,
            Double scorePercent,
            String level) {
        SubjectInfo subjectInfo = getSubject(subjectId);
        PlacementHistoryItem item = new PlacementHistoryItem(
                attemptId,
                subjectId,
                subjectInfo == null ? "UNKNOWN" : subjectInfo.code(),
                subjectInfo == null ? "Unknown" : subjectInfo.name(),
                scorePercent,
                level,
                LocalDateTime.now());
        placementHistoryByUser.compute(userId, (k, current) -> {
            List<PlacementHistoryItem> list = current == null ? new ArrayList<>() : new ArrayList<>(current);
            list.add(0, item);
            return list;
        });
    }

    public List<PlacementHistoryItem> getPlacementHistory(Long userId) {
        List<PlacementHistoryItem> list = placementHistoryByUser.get(userId);
        return list == null ? List.of() : List.copyOf(list);
    }

    public RoadmapProgressMem getRoadmapProgress(Long userId, Long subjectId) {
        return roadmapProgressByUserSubject.get(userSubjectKey(userId, subjectId));
    }

    public RoadmapProgressMem initializeRoadmapProgress(Long userId, Long subjectId, Level level) {
        String key = userSubjectKey(userId, subjectId);
        return roadmapProgressByUserSubject.compute(key, (k, current) -> {
            if (current != null) {
                return current;
            }
            RoadmapProgressMem progress = new RoadmapProgressMem();
            progress.setCurrentLevel(level);
            progress.setPhase("LESSONS");
            progress.setReplanCount(0);
            return progress;
        });
    }

    private String userSubjectKey(Long userId, Long subjectId) {
        return userId + ":" + subjectId;
    }
}
