package com.compassed.compassed_api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void mysqlProfileShouldRunEndToEndFlowWithJwt() throws Exception {
        String email = "mysql_it_" + UUID.randomUUID() + "@compassed.local";

        Map<String, Object> registerReq = new HashMap<>();
        registerReq.put("email", email);
        registerReq.put("password", "secret123");
        registerReq.put("fullName", "MySQL IT");
        HttpResponse<String> registerResp = post("/api/auth/register", registerReq, null);
        assertTrue(registerResp.statusCode() >= 200 && registerResp.statusCode() < 300);
        Map<String, Object> registerBody = objectMapper.readValue(registerResp.body(), new TypeReference<Map<String, Object>>() {
        });
        String token = String.valueOf(registerBody.get("token"));
        assertNotNull(token);

        HttpResponse<String> unauthorizedRoadmap = get("/api/subjects/1/roadmap", null);
        assertTrue(unauthorizedRoadmap.statusCode() == HttpStatus.UNAUTHORIZED.value()
                || unauthorizedRoadmap.statusCode() == HttpStatus.FORBIDDEN.value());

        HttpResponse<String> subjectsResp = get("/api/subjects", null);
        assertTrue(subjectsResp.statusCode() >= 200 && subjectsResp.statusCode() < 300);
        List<Map<String, Object>> subjects = objectMapper.readValue(subjectsResp.body(), new TypeReference<List<Map<String, Object>>>() {
        });
        assertFalse(subjects.isEmpty());

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
        HttpResponse<String> checkoutResp = post("/api/subscriptions/checkout", checkoutReq, token);
        assertTrue(checkoutResp.statusCode() >= 200 && checkoutResp.statusCode() < 300);

        HttpResponse<String> roadmapResp = get("/api/subjects/1/roadmap", token);
        assertTrue(roadmapResp.statusCode() >= 200 && roadmapResp.statusCode() < 300);
        Map<String, Object> roadmap = objectMapper.readValue(roadmapResp.body(), new TypeReference<Map<String, Object>>() {
        });
        assertTrue((Boolean) roadmap.get("subscribed"));
        assertNotNull(roadmap.get("phase"));
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
