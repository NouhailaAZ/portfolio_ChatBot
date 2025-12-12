package com.project.portfolio.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.portfolio.model.Prompt;
import com.project.portfolio.service.PromptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/portfolio")
public class ChatController {

    @Autowired
    private PromptService promptService;

    @Value("${google.api.key}")
    private String apiKey;

    private final WebClient webClient = WebClient.create("https://generativelanguage.googleapis.com");

    // 🧠 Cache local
    private final Map<String, String> answerCache = new ConcurrentHashMap<>();
    private final Map<String, String> categoryCache = new ConcurrentHashMap<>();

    // 🔢 Compteur de fréquence des questions
    private final Map<String, Integer> questionFrequency = new ConcurrentHashMap<>();

    @PostMapping("/ask")
    public Mono<Map<String, String>> askAI(@RequestBody Map<String, String> request) {
        String question = request.get("question").trim().toLowerCase();

        // 🗣️ 1️⃣ Réponses "sociales" (bonjour, merci, etc.)
        if (question.matches(".*\\b(bonjour|salut|coucou|hey)\\b.*")) {
            return Mono.just(Map.of("answer", "Bonjour 👋 ! Je suis l’assistant de Nouhaila AZLAG, prête à te parler de son parcours."));
        }
        if (question.matches(".*\\b(merci|thanks|super)\\b.*")) {
            return Mono.just(Map.of("answer", "Avec plaisir 😊 ! N’hésite pas si tu veux en savoir plus sur Nouhaila."));
        }
        if (question.matches(".*(qui es[- ]tu|tu es qui|t'es qui|qui êtes[- ]vous|que fais[- ]tu|tu fais quoi).*")) {
            return Mono.just(Map.of("answer", "Je suis un assistant virtuel créé pour présenter le parcours, les projets et les compétences de Nouhaila AZLAG 💻."));
        }

        // 🧠 2️⃣ Vérifier le cache
        if (answerCache.containsKey(question)) {
            return Mono.just(Map.of("answer", answerCache.get(question)));
        }

        // 🔢 Incrémenter la fréquence d’apparition
        questionFrequency.put(question, questionFrequency.getOrDefault(question, 0) + 1);

        // 🧠 3️⃣ Déterminer la ou les catégories
        List<Prompt> categories = promptService.getAll();

        String systemPrompt = "Tu es un assistant spécialisé sur le parcours de Nouhaila AZLAG. Voici les catégories disponibles :\n";
        for (Prompt cat : categories) {
            systemPrompt += "- " + cat.getCategory() + "\n";
        }
        systemPrompt += """
        Si la question concerne plusieurs thèmes (par ex. parcours + compétences), 
        liste toutes les catégories séparées par une virgule. 
        Réponds uniquement avec les noms des catégories, sans phrase inutile.
        """;

        Map<String, Object> classifyBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", systemPrompt + "\nQuestion : " + question))))
        );

        return webClient.post()
                .uri("/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(classifyBody)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(responseString -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode response = mapper.readTree(responseString);
                        String rawText = response.path("candidates").get(0)
                                .path("content").path("parts").get(0)
                                .path("text").asText().toLowerCase().trim();

                        System.out.println("✅ Catégories détectées : " + rawText);

                        // Extraire toutes les catégories trouvées
                        List<String> detectedCategories = new ArrayList<>();
                        for (Prompt cat : categories) {
                            if (rawText.contains(cat.getCategory().toLowerCase())) {
                                detectedCategories.add(cat.getCategory());
                            }
                        }

                        if (detectedCategories.isEmpty()) {
                            return handleUnknownQuestion(question);
                        }

                        return handleMultipleCategories(question, detectedCategories);

                    } catch (Exception e) {
                        e.printStackTrace();
                        return Mono.just(Map.of("answer", "Je ne peux répondre qu’à propos du parcours de Nouhaila AZLAG."));
                    }
                });
    }

    /**
     * 4️⃣ Si aucune catégorie claire n’est détectée
     */
    private Mono<Map<String, String>> handleUnknownQuestion(String question) {
        if (questionFrequency.getOrDefault(question, 0) >= 3) {
            System.out.println("🧩 Nouvelle question fréquente détectée : " + question);
            // 👉 Ici, tu pourrais l’ajouter automatiquement à la base avec une catégorie "unknown"
            // promptService.save(new Prompt("unknown", "Question fréquente : " + question));
        }
        return Mono.just(Map.of("answer", "Je ne peux répondre qu’à propos du parcours de Nouhaila AZLAG."));
    }

    /**
     * 5️⃣ Combiner plusieurs catégories et reformuler
     */
    private Mono<Map<String, String>> handleMultipleCategories(String question, List<String> categories) {
        StringBuilder combinedContent = new StringBuilder();
        for (String category : categories) {
            String content = promptService.getContentByCategory(category);
            if (content != null) {
                combinedContent.append("- ").append(content).append("\n");
            }
        }

        if (combinedContent.isEmpty()) {
            return Mono.just(Map.of("answer", "Je ne peux répondre qu’à propos du parcours de Nouhaila AZLAG."));
        }

        String reformulatePrompt = String.format("""
            Voici une question d’un utilisateur : "%s"
            Voici les informations associées aux catégories %s :
            %s

            Formule une réponse complète, fluide et naturelle, en parlant de Nouhaila à la 3ᵉ personne.
            """, question, String.join(", ", categories), combinedContent);

        Map<String, Object> reformulateBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", reformulatePrompt))))
        );

        return webClient.post()
                .uri("/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(reformulateBody)
                .retrieve()
                .bodyToMono(String.class)
                .map(responseString -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode response = mapper.readTree(responseString);
                        String answer = response.path("candidates").get(0)
                                .path("content").path("parts").get(0)
                                .path("text").asText();

                        // Sauvegarde dans le cache
                        answerCache.put(question, answer);
                        return Map.of("answer", answer);

                    } catch (Exception e) {
                        e.printStackTrace();
                        return Map.of("answer", combinedContent.toString());
                    }
                });
    }

    @GetMapping("/clear-cache")
    public Map<String, String> clearCache() {
        answerCache.clear();
        categoryCache.clear();
        return Map.of("status", "Cache vidé ✅");
    }
}
