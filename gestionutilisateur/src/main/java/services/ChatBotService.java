package services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.function.Consumer;
import org.json.JSONArray;
import org.json.JSONObject;
import entities.Candidature;
import entities.OffreEmploi;

/**
 * Service ChatBot RH - Assistant intelligent pour les employés
 * Utilise Groq API (gratuit, ultra-rapide)
 */
public class ChatBotService {

    private static final String API_KEY = System.getenv("GROQ_API_KEY");
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    /**
     * Envoie un message au chatbot et récupère une réponse (sans contexte de base de données)
     */
    public static void sendMessage(String userMessage, Consumer<String> onSuccess, Consumer<String> onError) {
        sendMessageWithContext(userMessage, null, null, onSuccess, onError);
    }

    /**
     * Envoie un message au chatbot avec contexte de base de données (candidatures et offres)
     * @param userMessage Message de l'utilisateur
     * @param userCandidatures Liste des candidatures de l'utilisateur
     * @param availableOffres Liste de toutes les offres disponibles
     * @param onSuccess Callback en cas de succès
     * @param onError Callback en cas d'erreur
     */
    public static void sendMessageWithContext(String userMessage, List<Candidature> userCandidatures, 
                                              List<OffreEmploi> availableOffres, 
                                              Consumer<String> onSuccess, Consumer<String> onError) {
        new Thread(() -> {
            try {
                String systemPrompt = buildSystemPrompt(userCandidatures, availableOffres);

                // Construire le payload JSON
                JSONObject payload = new JSONObject();
                payload.put("model", MODEL);
                payload.put("temperature", 0.7);
                payload.put("max_tokens", 256);

                JSONArray messages = new JSONArray();
                messages.put(new JSONObject().put("role", "system").put("content", systemPrompt));
                messages.put(new JSONObject().put("role", "user").put("content", userMessage));
                payload.put("messages", messages);

                // Créer la requête HTTP
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(API_URL))
                        .header("Authorization", "Bearer " + API_KEY)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                        .timeout(java.time.Duration.ofSeconds(30))
                        .build();

                // Envoyer la requête
                HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    String responseText = extractResponseText(response.body());
                    onSuccess.accept(responseText);
                } else {
                    onError.accept("Erreur ChatBot (" + response.statusCode() + ")");
                }

            } catch (Exception e) {
                onError.accept("Erreur de connexion: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Construit le prompt système avec le contexte de base de données
     */
    private static String buildSystemPrompt(List<Candidature> userCandidatures, List<OffreEmploi> availableOffres) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Tu es un Assistant RH expert et bienveillant pour la plateforme VOS.\n");
        prompt.append("Tu aides les employés avec:\n");
        prompt.append("- Demandes de congés et procédures\n");
        prompt.append("- Processus de démission et conseils\n");
        prompt.append("- Questions sur les opportunités d'emploi\n");
        prompt.append("- Informations sur les services RH\n");
        prompt.append("- Conseils carrière et développement professionnel\n\n");
        
        // Ajouter contexte des candidatures si disponibles
        if (userCandidatures != null && !userCandidatures.isEmpty()) {
            prompt.append("📋 Candidatures de l'utilisateur:\n");
            for (Candidature cand : userCandidatures) {
                prompt.append("- Offre ID ").append(cand.getIdOffre())
                      .append(" | Statut: ").append(cand.getStatut())
                      .append(" | Domaine: ").append(cand.getDomaineExperience())
                      .append("\n");
            }
            prompt.append("\n");
        }
        
        // Ajouter contexte des offres disponibles si disponibles
        if (availableOffres != null && !availableOffres.isEmpty()) {
            prompt.append("💼 Offres d'emploi disponibles:\n");
            for (OffreEmploi offre : availableOffres) {
                if (offre.getStatutOffre() != null && offre.getStatutOffre().equalsIgnoreCase("active")) {
                    prompt.append("- ").append(offre.getTitre())
                          .append(" (").append(offre.getTypeContrat()).append(")")
                          .append(" - ").append(offre.getLieu())
                          .append("\n");
                }
            }
            prompt.append("\n");
        }
        
        prompt.append("Réponds de manière courtoise, professionnelle et concise (max 3-4 lignes par message).\n");
        prompt.append("Utilise des emojis pertinents pour rendre la conversation plus agréable.\n");
        prompt.append("Si la question ne concerne pas RH, réponds poliment que tu es spécialisé en RH.\n");
        prompt.append("Réfère-toi aux données fournies ci-dessus pour personnaliser tes réponses.\n");
        
        return prompt.toString();
    }

    /**
     * Extrait le texte de réponse du JSON Groq
     */
    private static String extractResponseText(String json) {
        try {
            JSONObject jsonResponse = new JSONObject(json);
            JSONArray choices = jsonResponse.getJSONArray("choices");
            if (choices.length() > 0) {
                JSONObject message = choices.getJSONObject(0).getJSONObject("message");
                return message.getString("content").trim();
            }
            return "Je n'ai pas pu générer une réponse. Veuillez réessayer.";
        } catch (Exception e) {
            return "Erreur lors du traitement de la réponse.";
        }
    }
}
