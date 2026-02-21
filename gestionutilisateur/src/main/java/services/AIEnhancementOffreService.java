package services;

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
 * - Groq API (FREE with fast inference)
 * - Anthropic Claude API (PAID)
 * - Demo mode (NO API - fully offline)
 * 
 * Features:
 * - Fixing spelling and grammar mistakes
 * - Enhancing professionalism and clarity
 * - Adding relevant details and structure
 * - Generating responsibilities and competencies based on job criteria
 */
public class AIEnhancementOffreService {

    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String CLAUDE_MODEL = "claude-3-5-sonnet-20241022";
    private static final String GROQ_MODEL = "llama-3.3-70b-versatile";

    private final Gson gson;
    private final String provider;
    private final String apiKey;

    public AIEnhancementOffreService() {
        this.gson = new Gson();
        String[] config = loadConfiguration();
        this.provider = config[0]; // "gemini", "claude", "groq", or "demo"
        this.apiKey = config[1];   // the API key
    }

    /**
     * Loads the AI provider configuration from environment variable or config file.
     * Priority: 1) Environment variables, 2) config.properties file
     *
     * @return Array of [provider, apiKey]
     */
    private String[] loadConfiguration() {
        String provider = "demo"; // Default to demo (free offline mode)
        String key = null;

        // Try config file first
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                Properties prop = new Properties();
                prop.load(input);
                
                // Get provider preference
                String configProvider = prop.getProperty("ai.provider.offres");
                if (configProvider != null && !configProvider.trim().isEmpty()) {
                    provider = configProvider.trim().toLowerCase();
                }
                
                // Get API key based on provider
                if ("gemini".equals(provider)) {
                    key = prop.getProperty("gemini.api.key");
                    String envKey = System.getenv("GEMINI_API_KEY");
                    if (envKey != null && !envKey.isEmpty()) {
                        key = envKey;
                    }
                } else if ("claude".equals(provider)) {
                    key = prop.getProperty("claude.api.key");
                    String envKey = System.getenv("CLAUDE_API_KEY");
                    if (envKey != null && !envKey.isEmpty()) {
                        key = envKey;
                    }
                } else if ("groq".equals(provider)) {
                    key = prop.getProperty("groq.api.key.offres");
                    String envKey = System.getenv("GROQ_API_KEY_OFFRES");
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
            // Config file not found, will use demo mode
        }

        return new String[]{provider, key};
    }

    /**
     * Enhances a job offer description using AI.
     *
     * @param rawDescription The original job description (may contain errors or be incomplete)
     * @return The enhanced, professional job description
     * @throws IOException If the API call fails
     */
    public String enhanceDescription(String rawDescription) throws IOException {
        if (rawDescription == null || rawDescription.trim().isEmpty()) {
            throw new IllegalArgumentException("Job description cannot be empty");
        }

        // Route to appropriate provider
        if ("gemini".equals(provider) && apiKey != null && !apiKey.isEmpty()) {
            return enhanceWithGemini(rawDescription);
        } else if ("claude".equals(provider) && apiKey != null && !apiKey.isEmpty()) {
            return enhanceWithClaude(rawDescription);
        } else if ("groq".equals(provider) && apiKey != null && !apiKey.isEmpty()) {
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
        
        // Add professional closing if description is short
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

            request.setHeader("Authorization", "Bearer " + apiKey);
            request.setHeader("Content-Type", "application/json");

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

            request.setHeader("Content-Type", "application/json");

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

            request.setHeader("x-api-key", apiKey);
            request.setHeader("anthropic-version", "2023-06-01");
            request.setHeader("Content-Type", "application/json");

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

                return parseClaudeResponse(responseBody);
            }
        }
    }

    /**
     * Builds the prompt for the AI to enhance the job description.
     */
    private String buildPrompt(String rawDescription) {
        return "You are an AI assistant specialized in enhancing job offer descriptions. " +
                "Your role is to take a raw job offer description (which may contain spelling errors, " +
                "grammar issues, or vague wording) and improve it to be more professional, detailed, and complete.\\n\\n" +
                "Rules:\\n" +
                "- Fix all spelling and grammar mistakes\\n" +
                "- Enhance the description to be more professional and detailed\\n" +
                "- Add relevant details that make the offer more attractive\\n" +
                "- Keep the original intent and meaning intact\\n" +
                "- Return ONLY the improved description text, nothing else\\n" +
                "- Write in French if the input is in French\\n" +
                "- Keep the result concise (under 500 characters if possible)\\n\\n" +
                "Raw job description:\\n\\n" +
                rawDescription;
    }

    /**
     * Parses the Gemini API response.
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
                    return part.get("text").getAsString().trim();
                }
            }

            throw new IOException("Unexpected Gemini response format");
        } catch (Exception e) {
            throw new IOException("Failed to parse Gemini response: " + e.getMessage(), e);
        }
    }

    /**
     * Parses the Claude API response.
     */
    private String parseClaudeResponse(String responseBody) throws IOException {
        try {
            JsonObject response = gson.fromJson(responseBody, JsonObject.class);
            JsonArray content = response.getAsJsonArray("content");

            if (content != null && content.size() > 0) {
                JsonObject firstContent = content.get(0).getAsJsonObject();
                return firstContent.get("text").getAsString().trim();
            }

            throw new IOException("Unexpected Claude response format");
        } catch (Exception e) {
            throw new IOException("Failed to parse Claude response: " + e.getMessage(), e);
        }
    }

    /**
     * Parses the Groq API response (OpenAI-compatible format).
     */
    private String parseGroqResponse(String responseBody) throws IOException {
        try {
            JsonObject response = gson.fromJson(responseBody, JsonObject.class);
            JsonArray choices = response.getAsJsonArray("choices");

            if (choices != null && choices.size() > 0) {
                JsonObject choice = choices.get(0).getAsJsonObject();
                JsonObject message = choice.getAsJsonObject("message");
                return message.get("content").getAsString().trim();
            }

            throw new IOException("Unexpected Groq response format");
        } catch (Exception e) {
            throw new IOException("Failed to parse Groq response: " + e.getMessage(), e);
        }
    }

    /**
     * Generates job criteria (responsibilities and competencies) based on job details.
     * 
     * @param jobTitle The job title
     * @param experienceLevel Required experience level
     * @param educationLevel Required education level
     * @return Array of [responsibilities, competencies] or null if failed
     */
    public String[] generateJobCriteria(String jobTitle, String experienceLevel, String educationLevel) throws IOException {
        if (!isConfigured()) {
            throw new IOException("AI API non configurée. Veuillez configurer une clé API dans config.properties (Groq ou Gemini recommandés - gratuits)");
        }

        String prompt = String.format(
            "You are an expert HR professional and job description writer. Based on the following job details, generate:\n" +
            "1. Detailed responsibilities (as bullet points, max 300 characters total)\n" +
            "2. Required competences/skills (as bullet points, max 300 characters total)\n\n" +
            "Job Title: %s\n" +
            "Experience Level Required: %s\n" +
            "Education Level Required: %s\n\n" +
            "IMPORTANT: Each list must be:\n" +
            "- Maximum 300 characters total\n" +
            "- Formatted with bullet points (• or -) on separate lines\n" +
            "- Concise and to the point\n\n" +
            "Return ONLY the following format:\n" +
            "RESPONSIBILITIES:\n" +
            "• [responsibility 1]\n" +
            "• [responsibility 2]\n" +
            "• [responsibility 3]\n\n" +
            "COMPETENCIES:\n" +
            "• [competency 1]\n" +
            "• [competency 2]\n" +
            "• [competency 3]\n\n" +
            "Make the lists realistic, relevant, and professional.",
            jobTitle, experienceLevel, educationLevel
        );

        if ("gemini".equalsIgnoreCase(provider)) {
            return generateCriteriaWithGemini(prompt);
        } else if ("claude".equalsIgnoreCase(provider)) {
            return generateCriteriaWithClaude(prompt);
        } else if ("groq".equalsIgnoreCase(provider)) {
            return generateCriteriaWithGroq(prompt);
        }

        throw new IOException("Provider " + provider + " non supporté pour la génération de critères. Utilisez: gemini, groq, ou claude");
    }

    /**
     * Generates job criteria using Google Gemini API.
     */
    private String[] generateCriteriaWithGemini(String prompt) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        try {
            HttpPost httpPost = new HttpPost(GEMINI_API_URL + "?key=" + apiKey);
            httpPost.setHeader("Content-Type", "application/json");

            JsonObject requestBody = new JsonObject();
            JsonArray contents = new JsonArray();
            JsonObject content = new JsonObject();
            JsonArray parts = new JsonArray();
            JsonObject part = new JsonObject();
            part.addProperty("text", prompt);
            parts.add(part);
            content.add("parts", parts);
            contents.add(content);
            requestBody.add("contents", contents);

            httpPost.setEntity(new StringEntity(requestBody.toString(), ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                if (response.getEntity() == null) {
                    System.err.println("Gemini API returned empty response");
                    return null;
                }
                
                try {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

                    if (jsonResponse.has("candidates") && jsonResponse.getAsJsonArray("candidates").size() > 0) {
                        JsonObject candidate = jsonResponse.getAsJsonArray("candidates").get(0).getAsJsonObject();
                        if (candidate.has("content") && candidate.getAsJsonObject("content").has("parts")) {
                            String text = candidate.getAsJsonObject("content").getAsJsonArray("parts")
                                .get(0).getAsJsonObject().get("text").getAsString();
                            return parseAIResponse(text);
                        }
                    }
                } catch (ParseException e) {
                    System.err.println("Failed to parse Gemini API response: " + e.getMessage());
                    return null;
                }
            }
        } finally {
            httpClient.close();
        }

        return null;
    }

    /**
     * Generates job criteria using Anthropic Claude API.
     */
    private String[] generateCriteriaWithClaude(String prompt) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        try {
            HttpPost httpPost = new HttpPost(CLAUDE_API_URL);
            httpPost.setHeader("x-api-key", apiKey);
            httpPost.setHeader("anthropic-version", "2023-06-01");
            httpPost.setHeader("Content-Type", "application/json");

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", CLAUDE_MODEL);
            requestBody.addProperty("max_tokens", 1024);
            
            JsonArray messages = new JsonArray();
            JsonObject message = new JsonObject();
            message.addProperty("role", "user");
            message.addProperty("content", prompt);
            messages.add(message);
            
            requestBody.add("messages", messages);

            httpPost.setEntity(new StringEntity(requestBody.toString(), ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                if (response.getEntity() == null) {
                    System.err.println("Claude API returned empty response");
                    return null;
                }
                
                try {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

                    if (jsonResponse.has("content") && jsonResponse.getAsJsonArray("content").size() > 0) {
                        String text = jsonResponse.getAsJsonArray("content")
                            .get(0).getAsJsonObject().get("text").getAsString();
                        return parseAIResponse(text);
                    }
                } catch (ParseException e) {
                    System.err.println("Failed to parse Claude API response: " + e.getMessage());
                    return null;
                }
            }
        } finally {
            httpClient.close();
        }

        return null;
    }

    /**
     * Generates job criteria using Groq API.
     */
    private String[] generateCriteriaWithGroq(String prompt) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        try {
            HttpPost httpPost = new HttpPost(GROQ_API_URL);
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            httpPost.setHeader("Content-Type", "application/json");

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", GROQ_MODEL);
            requestBody.addProperty("max_tokens", 1024);
            
            JsonArray messages = new JsonArray();
            JsonObject message = new JsonObject();
            message.addProperty("role", "user");
            message.addProperty("content", prompt);
            messages.add(message);
            
            requestBody.add("messages", messages);

            httpPost.setEntity(new StringEntity(requestBody.toString(), ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                if (response.getEntity() == null) {
                    System.err.println("Groq API returned empty response");
                    return null;
                }
                
                try {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

                    if (jsonResponse.has("choices") && jsonResponse.getAsJsonArray("choices").size() > 0) {
                        String text = jsonResponse.getAsJsonArray("choices")
                            .get(0).getAsJsonObject().getAsJsonObject("message").get("content").getAsString();
                        return parseAIResponse(text);
                    }
                } catch (ParseException e) {
                    System.err.println("Failed to parse Groq API response: " + e.getMessage());
                    return null;
                }
            }
        } finally {
            httpClient.close();
        }

        return null;
    }

    /**
     * Parses the AI response to extract responsibilities and competencies.
     * Formats them with bullet points and ensures they don't exceed 300 characters.
     */
    private String[] parseAIResponse(String response) {
        String[] result = new String[2];
        
        try {
            String[] lines = response.split("\n");
            StringBuilder responsibilities = new StringBuilder();
            StringBuilder competencies = new StringBuilder();
            boolean inResponsibilities = false;
            boolean inCompetencies = false;
            
            for (String line : lines) {
                line = line.trim();
                
                if (line.startsWith("RESPONSIBILITIES:")) {
                    inResponsibilities = true;
                    inCompetencies = false;
                    String content = line.substring("RESPONSIBILITIES:".length()).trim();
                    if (!content.isEmpty() && !content.startsWith("•") && !content.startsWith("-")) {
                        if (responsibilities.length() > 0) responsibilities.append("\n");
                        responsibilities.append("• ").append(content);
                    }
                } else if (line.startsWith("COMPETENCIES:") || line.startsWith("COMPETENCES:")) {
                    inCompetencies = true;
                    inResponsibilities = false;
                    String content = line.substring(line.contains("COMPETENCIES:") ? "COMPETENCIES:".length() : "COMPETENCES:".length()).trim();
                    if (!content.isEmpty() && !content.startsWith("•") && !content.startsWith("-")) {
                        if (competencies.length() > 0) competencies.append("\n");
                        competencies.append("• ").append(content);
                    }
                } else if (inResponsibilities && !line.isEmpty()) {
                    if (line.startsWith("•") || line.startsWith("-")) {
                        if (responsibilities.length() > 0) responsibilities.append("\n");
                        responsibilities.append(line.startsWith("•") ? line : "• " + line.substring(1).trim());
                    } else if (!line.isEmpty()) {
                        if (responsibilities.length() > 0) responsibilities.append("\n");
                        responsibilities.append("• ").append(line);
                    }
                } else if (inCompetencies && !line.isEmpty()) {
                    if (line.startsWith("•") || line.startsWith("-")) {
                        if (competencies.length() > 0) competencies.append("\n");
                        competencies.append(line.startsWith("•") ? line : "• " + line.substring(1).trim());
                    } else if (!line.isEmpty()) {
                        if (competencies.length() > 0) competencies.append("\n");
                        competencies.append("• ").append(line);
                    }
                }
            }
            
            // Trim to 300 characters if needed
            String resp = responsibilities.toString().trim();
            String comp = competencies.toString().trim();
            
            if (resp.length() > 300) {
                resp = resp.substring(0, 300).trim();
                int lastNewline = resp.lastIndexOf("\n");
                if (lastNewline > 0) {
                    resp = resp.substring(0, lastNewline);
                }
            }
            
            if (comp.length() > 300) {
                comp = comp.substring(0, 300).trim();
                int lastNewline = comp.lastIndexOf("\n");
                if (lastNewline > 0) {
                    comp = comp.substring(0, lastNewline);
                }
            }
            
            result[0] = resp.isEmpty() ? "" : resp;
            result[1] = comp.isEmpty() ? "" : comp;
            
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Validates if the API key is configured.
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }
    
    /**
     * Gets the current AI provider name.
     */
    public String getProvider() {
        return provider;
    }
}
