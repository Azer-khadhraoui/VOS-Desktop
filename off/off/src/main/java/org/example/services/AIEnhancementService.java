package org.example.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Service for AI-powered enhancement of job offer descriptions.
 * This service supports multiple AI providers:
 * - Google Gemini API (FREE - generous free tier)
 * - Anthropic Claude API (PAID)
 * 
 * Features:
 * - Fixing spelling and grammar mistakes
 * - Enhancing professionalism and clarity
 * - Adding relevant details and structure
 */
public class AIEnhancementService {

    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String CLAUDE_MODEL = "claude-3-5-sonnet-20241022";
    private static final String GROQ_MODEL = "llama-3.3-70b-versatile";

    private final Gson gson;
    private final String provider;
    private final String apiKey;

    public AIEnhancementService() {
        this.gson = new Gson();
        String[] config = loadConfiguration();
        this.provider = config[0]; // "gemini" or "claude"
        this.apiKey = config[1];   // the API key
    }

    /**
     * Loads the AI provider configuration from environment variable or config file.
     * Priority: 1) Environment variables, 2) config.properties file
     *
     * @return Array of [provider, apiKey]
     */
    private String[] loadConfiguration() {
        String provider = "gemini"; // Default to free option
        String key = null;

        // Try config file first
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                Properties prop = new Properties();
                prop.load(input);
                
                // Get provider preference
                String configProvider = prop.getProperty("ai.provider");
                if (configProvider != null && !configProvider.trim().isEmpty()) {
                    provider = configProvider.trim().toLowerCase();
                }
                
                // Get API key based on provider
                if ("gemini".equals(provider)) {
                    key = prop.getProperty("gemini.api.key");
                    // Also check environment variable
                    String envKey = System.getenv("GEMINI_API_KEY");
                    if (envKey != null && !envKey.isEmpty()) {
                        key = envKey;
                    }
                } else if ("claude".equals(provider)) {
                    key = prop.getProperty("claude.api.key");
                    // Also check environment variable
                    String envKey = System.getenv("CLAUDE_API_KEY");
                    if (envKey != null && !envKey.isEmpty()) {
                        key = envKey;
                    }
                } else if ("groq".equals(provider)) {
                    key = prop.getProperty("groq.api.key");
                    // Also check environment variable
                    String envKey = System.getenv("GROQ_API_KEY");
                    if (envKey != null && !envKey.isEmpty()) {
                        key = envKey;
                    }
                }
                
                // Validate key
                if (key != null && (key.contains("your-") || key.trim().isEmpty())) {
                    key = null;
                }
            }
        } catch (IOException e) {
            // Config file not found, will use defaults
        }

        return new String[]{provider, key};
    }

    /**
     * Enhances a job offer description using AI.
     *
     * @param rawDescription The original job description (may contain errors or be incomplete)
     * @return The enhanced, professional job description
     * @throws IOException If the API call fails
     * @throws IllegalStateException If the API key is not configured
     */
    public String enhanceDescription(String rawDescription) throws IOException {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("API key is not configured. " +
                    "Please add your API key to src/main/resources/config.properties\n\n" +
                    "For FREE option (recommended):\n" +
                    "1. Set ai.provider=gemini\n" +
                    "2. Get free key from: https://aistudio.google.com/app/apikey\n" +
                    "3. Set gemini.api.key=your-key");
        }

        if (rawDescription == null || rawDescription.trim().isEmpty()) {
            throw new IllegalArgumentException("Job description cannot be empty");
        }

        // Route to appropriate provider
        if ("gemini".equals(provider)) {
            return enhanceWithGemini(rawDescription);
        } else if ("claude".equals(provider)) {
            return enhanceWithClaude(rawDescription);
        } else if ("groq".equals(provider)) {
            return enhanceWithGroq(rawDescription);
        } else {
            // Demo mode - works offline, no API needed
            return enhanceWithDemo(rawDescription);
        }
    }

    /**
     * Enhances description using demo mode (NO API CALLS - 100% FREE).
     * This mode works offline and uses intelligent text processing.
     */
    private String enhanceWithDemo(String rawDescription) {
        String text = rawDescription.trim();
        
        // Basic grammar and formatting improvements
        text = text.substring(0, 1).toUpperCase() + text.substring(1);
        
        // Fix common French grammar mistakes
        text = text.replaceAll("\\bcherchon\\b", "cherchons");
        text = text.replaceAll("\\bdevelopeur\\b", "développeur");
        text = text.replaceAll("\\bDevelopeur\\b", "Développeur");
        text = text.replaceAll("\\btravaillé\\b", "travailler");
        text = text.replaceAll("\\bconnaitr\\b", "connaître");
        text = text.replaceAll("\\bsavoir\\b", "maîtriser");
        text = text.replaceAll("\\bbase de donné\\b", "bases de données");
        text = text.replaceAll("\\brechrchons\\b", "recherchons");
        text = text.replaceAll("\\brecherchon\\b", "recherchons");
        text = text.replaceAll("\\bexpérimenté\\b", "expérimenté(e)");
        
        // Add professional enhancements
        StringBuilder enhanced = new StringBuilder();
        
        // Add professional opening if missing
        if (!text.toLowerCase().contains("recherch") && !text.toLowerCase().contains("recrut")) {
            enhanced.append("Nous recherchons activement un(e) professionnel(le) pour ce poste. ");
        }
        
        enhanced.append(text);
        
        // Ensure proper ending
        if (!text.endsWith(".") && !text.endsWith("!")) {
            enhanced.append(".");
        }
        
        // Add professional closing
        if (text.length() < 100) {
            enhanced.append(" Le candidat idéal rejoindra une équipe dynamique dans un environnement stimulant et bénéficiera d'opportunités de développement professionnel.");
        }
        
        return enhanced.toString();
    }

    /**
     * Enhances description using Groq API (FREE with fast inference).
     */
    private String enhanceWithGroq(String rawDescription) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(GROQ_API_URL);

            // Set headers
            request.setHeader("Authorization", "Bearer " + apiKey);
            request.setHeader("Content-Type", "application/json");

            // Build request body (OpenAI-compatible format)
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", GROQ_MODEL);
            requestBody.addProperty("temperature", 0.7);
            requestBody.addProperty("max_tokens", 2000);

            JsonArray messages = new JsonArray();
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", "You are a professional HR assistant. Enhance job descriptions by fixing grammar, spelling, and making them more professional.");
            messages.add(systemMessage);

            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", buildPrompt(rawDescription));
            messages.add(userMessage);

            requestBody.add("messages", messages);

            request.setEntity(new StringEntity(requestBody.toString(), ContentType.APPLICATION_JSON));

            // Execute request
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String responseBody;
                try {
                    responseBody = EntityUtils.toString(response.getEntity());
                } catch (ParseException e) {
                    throw new IOException("Failed to parse API response: " + e.getMessage(), e);
                }

                if (response.getCode() != 200) {
                    throw new IOException("Groq API call failed with status " + response.getCode() + ": " + responseBody);
                }

                // Parse Groq response (OpenAI-compatible format)
                return parseGroqResponse(responseBody);
            }
        }
    }

    /**
     * Enhances description using Google Gemini API (FREE).
     */
    private String enhanceWithGemini(String rawDescription) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String url = GEMINI_API_URL + "?key=" + apiKey;
            HttpPost request = new HttpPost(url);

            // Set headers
            request.setHeader("Content-Type", "application/json");

            // Build request body for Gemini
            JsonObject requestBody = new JsonObject();
            
            JsonArray contents = new JsonArray();
            JsonObject content = new JsonObject();
            
            JsonArray parts = new JsonArray();
            JsonObject part = new JsonObject();
            part.addProperty("text", buildPrompt(rawDescription));
            parts.add(part);
            
            content.add("parts", parts);
            contents.add(content);
            requestBody.add("contents", contents);

            request.setEntity(new StringEntity(requestBody.toString(), ContentType.APPLICATION_JSON));

            // Execute request
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String responseBody;
                try {
                    responseBody = EntityUtils.toString(response.getEntity());
                } catch (ParseException e) {
                    throw new IOException("Failed to parse API response: " + e.getMessage(), e);
                }

                if (response.getCode() != 200) {
                    throw new IOException("Gemini API call failed with status " + response.getCode() + ": " + responseBody);
                }

                // Parse Gemini response
                return parseGeminiResponse(responseBody);
            }
        }
    }

    /**
     * Enhances description using Claude API (PAID).
     */
    private String enhanceWithClaude(String rawDescription) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(CLAUDE_API_URL);

            // Set headers
            request.setHeader("x-api-key", apiKey);
            request.setHeader("anthropic-version", "2023-06-01");
            request.setHeader("Content-Type", "application/json");

            // Build request body
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", CLAUDE_MODEL);
            requestBody.addProperty("max_tokens", 2000);

            JsonArray messages = new JsonArray();
            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", buildPrompt(rawDescription));
            messages.add(userMessage);
            requestBody.add("messages", messages);

            request.setEntity(new StringEntity(requestBody.toString(), ContentType.APPLICATION_JSON));

            // Execute request
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String responseBody;
                try {
                    responseBody = EntityUtils.toString(response.getEntity());
                } catch (ParseException e) {
                    throw new IOException("Failed to parse API response: " + e.getMessage(), e);
                }

                if (response.getCode() != 200) {
                    throw new IOException("Claude API call failed with status " + response.getCode() + ": " + responseBody);
                }

                // Parse response
                return parseClaudeResponse(responseBody);
            }
        }
    }

    /**
     * Builds the prompt for the AI to enhance the job description.
     *
     * @param rawDescription The original job description
     * @return The formatted prompt string
     */
    private String buildPrompt(String rawDescription) {
        return "You are an AI assistant specialized in enhancing job offer descriptions. " +
                "Your role is to take a raw job offer description (which may contain spelling errors, " +
                "grammar issues, or vague wording) and improve it to be more professional, detailed, and complete.\n\n" +
                "Rules:\n" +
                "- Fix all spelling and grammar mistakes\n" +
                "- Enhance the description to be more professional and detailed\n" +
                "- Add relevant details that make the offer more attractive (responsibilities, skills, environment, etc.)\n" +
                "- Keep the original intent and meaning intact\n" +
                "- Return ONLY the improved description text, nothing else (no explanations, no meta-commentary)\n" +
                "- Write in French if the input is in French, or in the same language as the input\n" +
                "- Keep the result concise (under 500 characters if possible)\n\n" +
                "Raw job description to enhance:\n\n" +
                rawDescription;
    }

    /**
     * Parses the Gemini API response to extract the enhanced description.
     *
     * @param responseBody The raw JSON response from the API
     * @return The enhanced job description text
     * @throws IOException If the response format is unexpected
     */
    private String parseGeminiResponse(String responseBody) throws IOException {
        try {
            JsonObject response = gson.fromJson(responseBody, JsonObject.class);
            JsonArray candidates = response.getAsJsonArray("candidates");

            if (candidates != null && candidates.size() > 0) {
                JsonObject candidate = candidates.get(0).getAsJsonObject();
                JsonObject content = candidate.getAsJsonObject("content");
                JsonArray parts = content.getAsJsonArray("parts");
                
                if (parts != null && parts.size() > 0) {
                    JsonObject part = parts.get(0).getAsJsonObject();
                    String text = part.get("text").getAsString();
                    return text.trim();
                }
            }

            throw new IOException("Unexpected Gemini response format: no content found");
        } catch (Exception e) {
            throw new IOException("Failed to parse Gemini API response: " + e.getMessage(), e);
        }
    }

    /**
     * Parses the Claude API response to extract the enhanced description.
     *
     * @param responseBody The raw JSON response from the API
     * @return The enhanced job description text
     * @throws IOException If the response format is unexpected
     */
    private String parseClaudeResponse(String responseBody) throws IOException {
        try {
            JsonObject response = gson.fromJson(responseBody, JsonObject.class);
            JsonArray content = response.getAsJsonArray("content");

            if (content != null && content.size() > 0) {
                JsonObject firstContent = content.get(0).getAsJsonObject();
                String text = firstContent.get("text").getAsString();
                return text.trim();
            }

            throw new IOException("Unexpected Claude response format: no content found");
        } catch (Exception e) {
            throw new IOException("Failed to parse Claude API response: " + e.getMessage(), e);
        }
    }

    /**
     * Parses the Groq API response to extract the enhanced description.
     * Groq uses OpenAI-compatible format.
     *
     * @param responseBody The raw JSON response from the API
     * @return The enhanced job description text
     * @throws IOException If the response format is unexpected
     */
    private String parseGroqResponse(String responseBody) throws IOException {
        try {
            JsonObject response = gson.fromJson(responseBody, JsonObject.class);
            JsonArray choices = response.getAsJsonArray("choices");

            if (choices != null && choices.size() > 0) {
                JsonObject choice = choices.get(0).getAsJsonObject();
                JsonObject message = choice.getAsJsonObject("message");
                String content = message.get("content").getAsString();
                return content.trim();
            }

            throw new IOException("Unexpected Groq response format: no content found");
        } catch (Exception e) {
            throw new IOException("Failed to parse Groq API response: " + e.getMessage(), e);
        }
    }

    /**
     * Validates if the API key is configured.
     *
     * @return true if the API key is set, false otherwise
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }
    
    /**
     * Gets the current AI provider name.
     *
     * @return "gemini" or "claude"
     */
    public String getProvider() {
        return provider;
    }
}
