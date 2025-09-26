package com.project.portfolio.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ChatController {
    @Value("${google.api.key}")
    private String apiKey;

    private final WebClient webClient = WebClient.create("https://generativelanguage.googleapis.com");

    @PostMapping("/ask")
    public Mono<Map<String, String>> askAI(@RequestBody Map<String, String> request) {
        String question = request.get("question");

        return webClient.post()
                .uri("/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", question))))))
                .retrieve()
                .bodyToMono(String.class)
                .map(responseString -> {
                    try {
                        // Convertir la chaîne JSON en JsonNode
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode response = mapper.readTree(responseString);
                        String answer = response.path("candidates").get(0)
                                .path("content").path("parts").get(0)
                                .path("text").asText();
                        return Map.of("answer", answer);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Map.of("answer", "Erreur lors de la lecture de la réponse");
                    }
                });
    }

}

