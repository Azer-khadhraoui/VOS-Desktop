package services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Consumer;

public class AIService {

    // ============================================================
    // GROQ API — 100% GRATUIT, ultra rapide
    // ============================================================
    private static final String API_KEY = "";
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL   = "llama-3.3-70b-versatile";

    // ============================================================
    // ANALYSER UNE ÉVALUATION — Rapport IA structuré
    // ============================================================
    public static void analyserEvaluation(
            entities.EvaluationEntretien evaluation,
            Consumer<String> onSuccess,
            Consumer<String> onError
    ) {
        new Thread(() -> {
            try {
                String prompt = "Tu es un expert RH senior. Analyse cette evaluation d'entretien et genere un rapport structure.\n\n"
                        + "=== DONNEES DE L'EVALUATION ===\n"
                        + "Score du test : " + evaluation.getScoreTest() + " / 100\n"
                        + "Note globale  : " + evaluation.getNoteEntretien() + " / 5\n"
                        + "Decision      : " + evaluation.getDecision() + "\n"
                        + "Competences Techniques       : " + evaluation.getCompetencesTechniques() + " / 5\n"
                        + "Competences Comportementales : " + evaluation.getCompetencesComportementales() + " / 5\n"
                        + "Communication                : " + evaluation.getCommunication() + " / 5\n"
                        + "Motivation                   : " + evaluation.getMotivation() + " / 5\n"
                        + "Experience                   : " + evaluation.getExperience() + " / 5\n"
                        + "Commentaire de l'evaluateur  : " + evaluation.getCommentaire() + "\n\n"
                        + "=== INSTRUCTIONS ===\n"
                        + "Genere un rapport COURT et DIRECT dans ce format exact :\n\n"
                        + "📊 RÉSUMÉ\n"
                        + "[1 phrase max]\n\n"
                        + "✅ POINTS FORTS  |  📈 À AMÉLIORER\n"
                        + "• [Fort 1]          • [Amélioration 1]\n"
                        + "• [Fort 2]          • [Amélioration 2]\n\n"
                        + "🎯 DÉCISION IA : [RECOMMANDE / DÉCONSEILLE / À REVOIR]\n"
                        + "[1-2 phrases de justification]\n\n"
                        + "Sois tres concis et direct. Maximum 10 lignes au total.";

                String body = "{"
                        + "\"model\": \"" + MODEL + "\","
                        + "\"messages\": [{"
                        + "  \"role\": \"user\","
                        + "  \"content\": " + toJsonString(prompt)
                        + "}],"
                        + "\"temperature\": 0.6,"
                        + "\"max_tokens\": 1024"
                        + "}";

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .header("Content-Type",  "application/json")
                        .header("Authorization", "Bearer " + API_KEY)
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

                HttpResponse<String> response = client.send(request,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    onSuccess.accept(extraireTexteGroq(response.body()));
                } else {
                    onError.accept("Erreur Groq API (" + response.statusCode() + "): " + response.body());
                }
            } catch (Exception e) {
                onError.accept("Erreur de connexion: " + e.getMessage());
            }
        }).start();
    }

    public static void genererQuestionsEntretien(
            String typeEntretien,
            String poste,
            String niveauExperience,
            String domaineExperience,
            Consumer<String> onSuccess,
            Consumer<String> onError
    ) {
        new Thread(() -> {
            try {
                String prompt = construirePrompt(typeEntretien, poste, niveauExperience, domaineExperience);

                String body = "{"
                        + "\"model\": \"" + MODEL + "\","
                        + "\"messages\": [{"
                        + "  \"role\": \"user\","
                        + "  \"content\": " + toJsonString(prompt)
                        + "}],"
                        + "\"temperature\": 0.7,"
                        + "\"max_tokens\": 1024"
                        + "}";

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .header("Content-Type",  "application/json")
                        .header("Authorization", "Bearer " + API_KEY)
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

                HttpResponse<String> response = client.send(request,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    onSuccess.accept(extraireTexteGroq(response.body()));
                } else {
                    onError.accept("Erreur Groq API (" + response.statusCode() + "): "
                            + response.body());
                }

            } catch (Exception e) {
                onError.accept("Erreur de connexion: " + e.getMessage());
            }
        }).start();
    }

    private static String construirePrompt(String type, String poste,
                                           String niveau, String domaine) {
        String contexte = type.equalsIgnoreCase("TECHNIQUE")
                ? "technique et pratique, evaluant les competences techniques"
                : "RH et comportemental, evaluant la personnalite et la motivation";

        return "Tu es un expert RH senior specialise dans les entretiens de recrutement.\n\n"
                + "Genere exactement 10 questions d entretien " + contexte + " pour :\n\n"
                + "Poste : " + poste + "\n"
                + "Type : " + type + "\n"
                + (niveau  != null && !niveau.isEmpty()  ? "Niveau : " + niveau  + "\n" : "")
                + (domaine != null && !domaine.isEmpty() ? "Domaine : " + domaine + "\n" : "")
                + "\nRegles STRICTES :\n"
                + "- Retourne UNIQUEMENT les 10 questions numerotees (1. 2. 3. ...)\n"
                + "- Une question par ligne\n"
                + "- Pas d introduction ni de conclusion\n"
                + "- Questions professionnelles et precises\n"
                + (type.equalsIgnoreCase("TECHNIQUE")
                ? "- Inclure des mises en situation et questions sur les technologies\n"
                : "- Utiliser la methode STAR\n");
    }

    // Groq suit le format OpenAI : choices[0].message.content
    private static String extraireTexteGroq(String json) {
        try {
            String marker = "\"content\":\"";
            int start = json.indexOf(marker);
            if (start == -1) {
                marker = "\"content\": \"";
                start  = json.indexOf(marker);
            }
            if (start == -1) return "Impossible de lire la reponse.";

            start += marker.length();
            StringBuilder sb = new StringBuilder();
            int i = start;
            while (i < json.length()) {
                char c = json.charAt(i);
                if (c == '\\' && i + 1 < json.length()) {
                    char next = json.charAt(i + 1);
                    if (next == '"')  { sb.append('"');  i += 2; continue; }
                    if (next == 'n')  { sb.append('\n'); i += 2; continue; }
                    if (next == 't')  { sb.append('\t'); i += 2; continue; }
                    if (next == '\\') { sb.append('\\'); i += 2; continue; }
                }
                if (c == '"') break;
                sb.append(c);
                i++;
            }
            return sb.toString();
        } catch (Exception e) {
            return "Erreur parsing: " + e.getMessage();
        }
    }

    private static String toJsonString(String text) {
        return "\"" + text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "")
                .replace("\t", "\\t")
                + "\"";
    }
}