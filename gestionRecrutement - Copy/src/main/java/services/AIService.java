package services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * Service to handle AI operations using the Groq API.
 * This service provides intelligent matching analysis for candidates.
 */
public class AIService {

    private static final String API_KEY = "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";
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";

    private final HttpClient client;
    private final Gson gson;

    private static AIService instance;

    private AIService() {
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public static synchronized AIService getInstance() {
        if (instance == null) {
            instance = new AIService();
        }
        return instance;
    }

    /**
     * Analyzes the match between a candidate and an interview/offer.
     * Returns a JSON string containing score and analysis.
     */
    public CompletableFuture<String> analyzeMatch(String candidateData, String jobDescription) {
        String prompt = "Tu es un assistant RH professionnel. Analyse l'adéquation entre ce candidat et ce poste. Réponds EXCLUSIVEMENT en Français.\n\n"
                +
                "INFOS CANDIDAT:\n" + candidateData + "\n\n" +
                "DESCRIPTION DU POSTE:\n" + jobDescription + "\n\n" +
                "Réponds UNIQUEMENT au format JSON avec ces clés exactes :\n" +
                "1. 'score': un nombre de 0 à 100\n" +
                "2. 'analysis': un résumé de l'adéquation en 3 phrases maximum\n" +
                "3. 'recommendation': un conseil en 1 phrase (Embaucher/Entretien/Refuser)";

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", MODEL);

        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);

        gson.toJsonTree(new JsonObject[] { message });
        requestBody.add("messages", gson.toJsonTree(new Object[] { message }));

        // Enforce JSON mode
        JsonObject responseFormat = new JsonObject();
        responseFormat.addProperty("type", "json_object");
        requestBody.add("response_format", responseFormat);

        requestBody.addProperty("temperature", 0.5); // Lower temperature for more consistent JSON

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
                        String content = jsonResponse.getAsJsonArray("choices")
                                .get(0).getAsJsonObject()
                                .getAsJsonObject("message")
                                .get("content").getAsString();
                        return cleanJsonResponse(content);
                    } else {
                        return "{\"error\": \"API Error: " + response.statusCode() + "\"}";
                    }
                })
                .exceptionally(ex -> "{\"error\": \"" + ex.getMessage().replace("\"", "\\\"") + "\"}");
    }

    private String cleanJsonResponse(String content) {
        if (content == null)
            return "{}";
        content = content.trim();
        // Remove markdown code blocks if present
        if (content.startsWith("```")) {
            if (content.startsWith("```json")) {
                content = content.substring(7);
            } else {
                content = content.substring(3);
            }
            int lastBackticks = content.lastIndexOf("```");
            if (lastBackticks != -1) {
                content = content.substring(0, lastBackticks);
            }
        }
        return content.trim();
    }
}
