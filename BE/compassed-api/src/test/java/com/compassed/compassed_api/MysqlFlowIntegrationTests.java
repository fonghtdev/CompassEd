package com.compassed.compassed_api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.compassed.compassed_api.domain.QuestionBank;
import com.compassed.compassed_api.domain.entity.Payment;
import com.compassed.compassed_api.domain.entity.User;
import com.compassed.compassed_api.repository.PaymentRepository;
import com.compassed.compassed_api.repository.QuestionBankRepository;
import com.compassed.compassed_api.repository.SubjectRepository;
import com.compassed.compassed_api.repository.UserRepository;
import com.compassed.compassed_api.service.PaymentService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("mysql")
@Testcontainers(disabledWithoutDocker = true)
class MysqlFlowIntegrationTests {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("compassed_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void mysqlProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("auth.jwt.secret", () -> "integration-test-jwt-secret-should-be-32chars");
        registry.add("openai.api.key", () -> "");
    }

    @LocalServerPort
    int port;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SubjectRepository subjectRepository;

    @Autowired
    QuestionBankRepository questionBankRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    PaymentService paymentService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void mysqlProfileShouldRunEndToEndFlowWithJwt() throws Exception {
        String email = "mysql_it_" + UUID.randomUUID() + "@compassed.local";

        // Registration needs an emailed code, so seed an already-verified user and log in.
        User user = new User();
        user.setEmail(email);
        user.setFullName("MySQL IT");
        user.setPasswordHash(new BCryptPasswordEncoder().encode("secret123"));
        user.setEmailVerified(true);
        userRepository.save(user);

        Map<String, Object> loginReq = new HashMap<>();
        loginReq.put("email", email);
        loginReq.put("password", "secret123");
        HttpResponse<String> loginResp = post("/api/auth/login", loginReq, null);
        assertTrue(loginResp.statusCode() >= 200 && loginResp.statusCode() < 300);
        Map<String, Object> loginBody = objectMapper.readValue(loginResp.body(), new TypeReference<Map<String, Object>>() {
        });
        String token = String.valueOf(loginBody.get("token"));
        assertNotNull(token);

        HttpResponse<String> unauthorizedRoadmap = get("/api/subjects/1/roadmap", null);
        assertTrue(unauthorizedRoadmap.statusCode() == HttpStatus.UNAUTHORIZED.value()
                || unauthorizedRoadmap.statusCode() == HttpStatus.FORBIDDEN.value());

        HttpResponse<String> subjectsResp = get("/api/subjects", null);
        assertTrue(subjectsResp.statusCode() >= 200 && subjectsResp.statusCode() < 300);
        List<Map<String, Object>> subjects = objectMapper.readValue(subjectsResp.body(), new TypeReference<List<Map<String, Object>>>() {
        });
        assertFalse(subjects.isEmpty());

        // A placement paper draws 20 L1, 20 L2 and 10 L3 questions for the user's grade band.
        seedQuestions(QuestionBank.Level.L1, 20);
        seedQuestions(QuestionBank.Level.L2, 20);
        seedQuestions(QuestionBank.Level.L3, 10);

        HttpResponse<String> startPlacementResp = post("/api/subjects/1/placement-tests", null, token);
        assertTrue(startPlacementResp.statusCode() >= 200 && startPlacementResp.statusCode() < 300);
        Map<String, Object> startBody = objectMapper.readValue(startPlacementResp.body(), new TypeReference<Map<String, Object>>() {
        });
        Number attemptId = (Number) startBody.get("attemptId");
        String paperJson = String.valueOf(startBody.get("paperJson"));
        List<Map<String, Object>> paper = objectMapper.readValue(paperJson, new TypeReference<List<Map<String, Object>>>() {
        });

        Map<String, String> answers = new HashMap<>();
        for (Map<String, Object> q : paper) {
            answers.put(String.valueOf(q.get("id")), "A");
        }
        Map<String, Object> submitReq = Map.of("answersJson", objectMapper.writeValueAsString(answers));
        HttpResponse<String> submitResp = post("/api/placement-attempts/" + attemptId.longValue() + "/submit", submitReq, token);
        assertTrue(submitResp.statusCode() >= 200 && submitResp.statusCode() < 300);

        Map<String, Object> checkoutReq = Map.of("subjectIds", List.of(1));
        HttpResponse<String> unpaidCheckoutResp = post("/api/subscriptions/checkout", checkoutReq, token);
        assertEquals(HttpStatus.FORBIDDEN.value(), unpaidCheckoutResp.statusCode());

        payForSubject(user.getId(), 1L);

        HttpResponse<String> checkoutResp = post("/api/subscriptions/checkout", checkoutReq, token);
        assertTrue(checkoutResp.statusCode() >= 200 && checkoutResp.statusCode() < 300);

        HttpResponse<String> roadmapResp = get("/api/subjects/1/roadmap", token);
        assertTrue(roadmapResp.statusCode() >= 200 && roadmapResp.statusCode() < 300);
        Map<String, Object> roadmap = objectMapper.readValue(roadmapResp.body(), new TypeReference<Map<String, Object>>() {
        });
        assertTrue((Boolean) roadmap.get("subscribed"));
        assertNotNull(roadmap.get("phase"));
    }

    private void payForSubject(Long userId, Long subjectId) {
        Payment payment = new Payment();
        payment.setUserId(userId);
        payment.setSubjectId(subjectId);
        payment.setAmount(BigDecimal.valueOf(100000));
        payment.setStatus("PENDING");
        paymentService.approveManualPayment(paymentRepository.save(payment).getId());
    }

    private void seedQuestions(QuestionBank.Level level, int count) {
        for (int i = 0; i < count; i++) {
            QuestionBank q = new QuestionBank();
            q.setSubject(subjectRepository.findById(1L).orElseThrow());
            q.setLevel(level);
            q.setGradeLevel(11);
            q.setGradeBand("GRADE_11");
            q.setSkillType("Algebra");
            q.setQuestionType(QuestionBank.QuestionType.MULTIPLE_CHOICE);
            q.setQuestionText(level + " question " + i);
            q.setOptions("[\"A. 1\", \"B. 2\", \"C. 3\", \"D. 4\"]");
            q.setCorrectAnswer("A");
            questionBankRepository.save(q);
        }
    }

    private HttpResponse<String> get(String path, String token) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url(path)))
                .GET()
                .header("Accept", "application/json");
        if (token != null && !token.isBlank()) {
            builder.header("Authorization", "Bearer " + token);
        }
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, Object body, String token) throws Exception {
        String json = body == null ? "" : objectMapper.writeValueAsString(body);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url(path)))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(body == null
                        ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(json));
        if (token != null && !token.isBlank()) {
            builder.header("Authorization", "Bearer " + token);
        }
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
