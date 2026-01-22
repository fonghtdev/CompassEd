package com.compassed.compassed_api.ai;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class OpenAiClient {

    private final String apiKey;
    private final WebClient webClient;

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

        return webClient.post()
                .uri("/responses")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
