package com.compassed.compassed_api.ai;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class OpenAiClient {

    private final String apiKey;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAiClient(@Value("${openai.api.key:}") String apiKey) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .build();
    }

    public String callChatGpt(String model, String prompt) {
        // Nếu chưa set key: trả JSON giả để test flow
        if (apiKey.isBlank()) {
            return "{\"skills\":[],\"weak_topics\":[],\"recommendations\":[\"OPENAI_API_KEY chưa được cấu hình\"]}";
        }

        Map<String, Object> body = Map.of(
                "model", model,
                "input", prompt);

        String raw = webClient.post()
                .uri("/responses")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return extractOutputText(raw);
    }

    private String extractOutputText(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        try {
            JsonNode root = objectMapper.readTree(raw);
            JsonNode outputText = root.path("output_text");
            if (outputText.isTextual() && !outputText.asText().isBlank()) {
                return outputText.asText();
            }
            JsonNode output = root.path("output");
            if (output.isArray()) {
                for (JsonNode item : output) {
                    JsonNode content = item.path("content");
                    if (!content.isArray()) continue;
                    for (JsonNode part : content) {
                        String type = part.path("type").asText("");
                        if ("output_text".equals(type)) {
                            String text = part.path("text").asText("");
                            if (!text.isBlank()) return text;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            // fall back to raw response
        }
        return raw;
    }
}
