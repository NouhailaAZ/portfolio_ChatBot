package com.project.portfolio.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
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

        String systemPrompt = """
            Tu es un assistant spécialisé sur le parcours de Nouhaila AZLAG.
            Elle est ingénieure en génie logiciel avec compétences en React, TailwindCSS, TypeScript, Java, Spring Boot, MySQL.
            Ses projets incluent : portfolio React/Tailwind, système bancaire distribué avec Oracle PL/SQL, gestion de centre de formation avec Spring Boot et microservices.
            Elle cherche un emploi en développement full stack.
            Répond uniquement si la question concerne son parcours ou ses compétences.
            Si la question est hors sujet, répond : "Je ne peux répondre qu'à des questions sur le parcours de Nouhaila AZLAG."
        """;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("role", "user", "parts", List.of(Map.of("text", systemPrompt))),
                        Map.of("role", "user", "parts", List.of(Map.of("text", question)))
                )
        );

        return webClient.post()
                .uri("/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .map(responseString -> {
                    try {
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
