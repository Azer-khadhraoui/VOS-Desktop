package services;

import entities.Entretien;
import entities.EvaluationEntretien;
import entities.Utilisateur;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EntretienEmailService {

    // ========== CONFIGURATION GMAIL ==========
    private static final String FROM_EMAIL    = "merhbeney@gmail.com";
    private static final String FROM_PASSWORD = "vrwo coop pequ pliq"; // Voir instructions ci-dessous
    private static final String FROM_NAME     = "VOS - Gestion RH";

    // ============================================================
    // QUESTIONS À PRÉPARER selon le type d'entretien
    // ============================================================
    private static String genererQuestionsHtml(String typeEntretien) {
        String[] questions;

        if (typeEntretien != null && typeEntretien.equalsIgnoreCase("TECHNIQUE")) {
            questions = new String[]{
                    "Décrivez un projet technique complexe que vous avez réalisé.",
                    "Quelles technologies maîtrisez-vous le mieux et pourquoi ?",
                    "Comment abordez-vous le débogage d'un problème difficile ?",
                    "Parlez-nous de votre expérience avec les méthodes Agile/Scrum.",
                    "Comment garantissez-vous la qualité et la maintenabilité de votre code ?",
                    "Décrivez une situation où vous avez dû apprendre rapidement une nouvelle technologie.",
                    "Comment gérez-vous les délais serrés sur un projet technique ?"
            };
        } else {
            questions = new String[]{
                    "Pouvez-vous vous présenter et parler de votre parcours professionnel ?",
                    "Quelles sont vos principales forces et vos axes d'amélioration ?",
                    "Pourquoi souhaitez-vous rejoindre notre entreprise ?",
                    "Décrivez une situation difficile au travail et comment vous l'avez gérée.",
                    "Où vous voyez-vous dans 5 ans ?",
                    "Comment travaillez-vous en équipe ? Donnez un exemple concret.",
                    "Pourquoi devrions-nous vous choisir pour ce poste ?"
            };
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<ol style='margin:0;padding-left:20px;'>");
        for (String q : questions) {
            sb.append("<li style='color:#374151;font-size:13px;padding:5px 0;line-height:1.5;'>")
                    .append(q)
                    .append("</li>");
        }
        sb.append("</ol>");
        return sb.toString();
    }

    // ========== SESSION GMAIL SMTP ==========
    private static Session createSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            "smtp.gmail.com");
        props.put("mail.smtp.port",            "587");
        props.put("mail.smtp.ssl.trust",       "smtp.gmail.com");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, FROM_PASSWORD);
            }
        });
    }

    // ========== MÉTHODE D'ENVOI CENTRALE ==========
    private static void sendEmail(String toEmail, String toName, String subject, String htmlBody) {
        new Thread(() -> {
            try {
                Session session = createSession();

                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                message.setSubject(subject, "UTF-8");

                MimeBodyPart htmlPart = new MimeBodyPart();
                htmlPart.setContent(htmlBody, "text/html; charset=UTF-8");

                Multipart multipart = new MimeMultipart("alternative");
                multipart.addBodyPart(htmlPart);
                message.setContent(multipart);

                Transport.send(message);
                System.out.println("✅ Email envoyé à: " + toEmail + " | Sujet: " + subject);

            } catch (Exception e) {
                System.err.println("❌ Erreur envoi email à " + toEmail + ": " + e.getMessage());
                e.printStackTrace();
            }
        }).start(); // Envoi en arrière-plan pour ne pas bloquer l'UI
    }

    // =============================================================
    // EMAIL 1 : NOTIFICATION ENTRETIEN PLANIFIÉ → CANDIDAT
    // =============================================================
    public static void envoyerConvocationEntretien(Entretien entretien,
                                                   Utilisateur candidat,
                                                   String nomOffre) {
        String subject = "📅 Convocation à un entretien - " + entretien.getTypeEntretien() + " | VOS";

        // Questions selon le type d'entretien
        String questionsHtml = genererQuestionsHtml(entretien.getTypeEntretien());

        String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body style='"
                + "margin:0;padding:0;background-color:#F5F6F8;font-family:Segoe UI,Arial,sans-serif;'>"

                // Header
                + "<div style='background:linear-gradient(135deg,#1a1a2e,#16213e);padding:40px 0;text-align:center;'>"
                + "<h1 style='color:white;margin:0;font-size:28px;'>📋 VOS - Gestion RH</h1>"
                + "<p style='color:#9CA3AF;margin:8px 0 0;'>Système de Gestion des Entretiens</p>"
                + "</div>"

                // Body
                + "<div style='max-width:600px;margin:30px auto;background:white;border-radius:16px;"
                + "box-shadow:0 4px 24px rgba(0,0,0,0.08);overflow:hidden;'>"

                // Title band
                + "<div style='background:linear-gradient(135deg,#2563EB,#4F46E5);padding:25px 35px;'>"
                + "<h2 style='color:white;margin:0;font-size:22px;'>📅 Convocation à un Entretien</h2>"
                + "<p style='color:rgba(255,255,255,0.8);margin:6px 0 0;font-size:14px;'>Vous avez été sélectionné(e) pour un entretien</p>"
                + "</div>"

                // Content
                + "<div style='padding:35px;'>"
                + "<p style='color:#374151;font-size:16px;margin:0 0 20px;'>Bonjour <strong>" + candidat.getPrenom() + " " + candidat.getNom() + "</strong>,</p>"
                + "<p style='color:#6B7280;font-size:15px;line-height:1.6;margin:0 0 25px;'>"
                + "Nous avons le plaisir de vous informer que votre candidature a été retenue et vous êtes convoqué(e) à un entretien."
                + "</p>"

                // Info box
                + "<div style='background:#EFF6FF;border-left:4px solid #2563EB;border-radius:8px;padding:20px 25px;margin:0 0 25px;'>"
                + "<h3 style='color:#1D4ED8;margin:0 0 15px;font-size:16px;'>📌 Détails de l'Entretien</h3>"
                + "<table style='width:100%;border-collapse:collapse;'>"
                + "<tr><td style='color:#6B7280;font-size:14px;padding:6px 0;width:40%;'>📋 Type :</td>"
                + "<td style='color:#111827;font-weight:600;font-size:14px;padding:6px 0;'>" + entretien.getTypeEntretien() + "</td></tr>"
                + "<tr><td style='color:#6B7280;font-size:14px;padding:6px 0;'>📅 Date :</td>"
                + "<td style='color:#111827;font-weight:600;font-size:14px;padding:6px 0;'>" + entretien.getDateEntretien() + "</td></tr>"
                + "<tr><td style='color:#6B7280;font-size:14px;padding:6px 0;'>🕐 Heure :</td>"
                + "<td style='color:#111827;font-weight:600;font-size:14px;padding:6px 0;'>" + entretien.getHeureEntretien() + "</td></tr>"
                + "<tr><td style='color:#6B7280;font-size:14px;padding:6px 0;'>📍 Lieu :</td>"
                + "<td style='color:#111827;font-weight:600;font-size:14px;padding:6px 0;'>" + entretien.getLieu() + "</td></tr>"
                + "<tr><td style='color:#6B7280;font-size:14px;padding:6px 0;'>📝 Type de test :</td>"
                + "<td style='color:#111827;font-weight:600;font-size:14px;padding:6px 0;'>" + entretien.getTypeTest() + "</td></tr>"
                + "</table></div>"
                // Bouton lien réunion si entretien en ligne
                + (entretien.getLienReunion() != null && !entretien.getLienReunion().trim().isEmpty()
                ? "<div style='background:linear-gradient(135deg,#ECFDF5,#D1FAE5);border:2px solid #10B981;border-radius:12px;padding:22px 25px;margin:0 0 25px;text-align:center;'>"
                + "<p style='color:#065F46;font-size:15px;font-weight:600;margin:0 0 6px;'>🎥 Entretien en ligne</p>"
                + "<p style='color:#6B7280;font-size:13px;margin:0 0 16px;'>Cliquez sur le bouton ci-dessous pour rejoindre la réunion à l'heure prévue</p>"
                + "<a href='" + entretien.getLienReunion() + "' style='display:inline-block;background:linear-gradient(135deg,#10B981,#059669);color:white;font-weight:bold;font-size:15px;padding:14px 35px;border-radius:10px;text-decoration:none;'>🎥 Rejoindre la Réunion</a>"
                + "<p style='color:#9CA3AF;font-size:11px;margin:12px 0 0;'>" + entretien.getLienReunion() + "</p>"
                + "</div>"
                : "")
                // Questions d'entretien si disponibles
                + (entretien.getQuestionsEntretien() != null && !entretien.getQuestionsEntretien().trim().isEmpty()
                ? "<div style='background:#FEF3C7;border-left:4px solid #F59E0B;border-radius:8px;padding:20px 25px;margin:0 0 25px;'>\n"
                + "<h3 style='color:#D97706;margin:0 0 15px;font-size:16px;'>📝 Questions d'Entretien</h3>\n"
                + "<p style='color:#92400E;font-size:13px;margin:0 0 12px;'>Voici les questions qui seront abordées lors de votre entretien :</p>\n"
                + "<div style='color:#78350F;font-size:13px;line-height:1.8;white-space:pre-line;'>"
                + nettoyerQuestionsEmail(entretien.getQuestionsEntretien())
                + "</div></div>\n"
                : "")
                // Advice box
                + "<div style='background:#F0FDF4;border:1px solid #BBF7D0;border-radius:8px;padding:18px 22px;margin:0 0 25px;'>"
                + "<h4 style='color:#15803D;margin:0 0 10px;font-size:14px;'>💡 Conseils pour votre entretien :</h4>"
                + "<ul style='color:#374151;font-size:13px;margin:0;padding-left:20px;line-height:1.8;'>"
                + "<li>Arrivez 10 minutes avant l'heure prévue</li>"
                + "<li>Préparez vos documents (CV, pièce d'identité)</li>"
                + "<li>Renseignez-vous sur notre entreprise</li>"
                + "<li>Préparez des questions pertinentes</li>"
                + "</ul></div>"

                + "<p style='color:#6B7280;font-size:14px;'>Pour toute question, contactez-nous à <a href='mailto:" + FROM_EMAIL + "' style='color:#2563EB;'>" + FROM_EMAIL + "</a></p>"

                // Questions à préparer
                + "<div style='background:#F5F3FF;border:1px solid #DDD6FE;border-radius:12px;padding:22px 25px;margin:20px 0 0;'>"
                + "<h3 style='color:#6D28D9;margin:0 0 15px;font-size:16px;'>📚 Questions à préparer pour votre entretien</h3>"
                + "<p style='color:#6B7280;font-size:13px;margin:0 0 12px;font-style:italic;'>Voici quelques exemples de questions qui pourraient vous être posées :</p>"
                + questionsHtml
                + "<p style='color:#9CA3AF;font-size:12px;margin:12px 0 0;'>💡 Ces questions sont données à titre indicatif pour vous aider à vous préparer.</p>"
                + "</div>"

                + "</div>"

                // Footer
                + "<div style='background:#F9FAFB;padding:20px 35px;text-align:center;border-top:1px solid #E5E7EB;'>"
                + "<p style='color:#9CA3AF;font-size:12px;margin:0;'>Cet email a été envoyé automatiquement par VOS - Système de Gestion RH</p>"
                + "</div>"
                + "</div></body></html>";

        sendEmail(candidat.getEmail(), candidat.getPrenom() + " " + candidat.getNom(), subject, html);
    }

    // =============================================================
    // EMAIL 2 : RAPPEL ÉVALUATION → ADMIN
    // =============================================================
    public static void envoyerRappelEvaluationAdmin(Entretien entretien,
                                                    Utilisateur admin,
                                                    Utilisateur candidat) {
        String subject = "🔔 Action requise : Évaluer l'entretien #" + entretien.getIdEntretien() + " | VOS";

        String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body style='"
                + "margin:0;padding:0;background-color:#F5F6F8;font-family:Segoe UI,Arial,sans-serif;'>"

                // Header
                + "<div style='background:linear-gradient(135deg,#1a1a2e,#16213e);padding:40px 0;text-align:center;'>"
                + "<h1 style='color:white;margin:0;font-size:28px;'>📋 VOS - Gestion RH</h1>"
                + "<p style='color:#9CA3AF;margin:8px 0 0;'>Système de Gestion des Entretiens</p>"
                + "</div>"

                + "<div style='max-width:600px;margin:30px auto;background:white;border-radius:16px;"
                + "box-shadow:0 4px 24px rgba(0,0,0,0.08);overflow:hidden;'>"

                // Title band - orange for urgency
                + "<div style='background:linear-gradient(135deg,#D97706,#F59E0B);padding:25px 35px;'>"
                + "<h2 style='color:white;margin:0;font-size:22px;'>🔔 Action Requise — Évaluation en attente</h2>"
                + "<p style='color:rgba(255,255,255,0.85);margin:6px 0 0;font-size:14px;'>Un entretien terminé nécessite votre évaluation</p>"
                + "</div>"

                + "<div style='padding:35px;'>"
                + "<p style='color:#374151;font-size:16px;margin:0 0 20px;'>Bonjour <strong>" + admin.getPrenom() + " " + admin.getNom() + "</strong>,</p>"
                + "<p style='color:#6B7280;font-size:15px;line-height:1.6;margin:0 0 25px;'>"
                + "L'entretien suivant vient de se terminer. Veuillez procéder à l'évaluation du candidat dès que possible."
                + "</p>"

                // Entretien info
                + "<div style='background:#FFFBEB;border-left:4px solid #F59E0B;border-radius:8px;padding:20px 25px;margin:0 0 20px;'>"
                + "<h3 style='color:#B45309;margin:0 0 15px;font-size:16px;'>📋 Entretien #" + entretien.getIdEntretien() + "</h3>"
                + "<table style='width:100%;border-collapse:collapse;'>"
                + "<tr><td style='color:#6B7280;font-size:14px;padding:5px 0;width:40%;'>👤 Candidat :</td>"
                + "<td style='color:#111827;font-weight:600;font-size:14px;padding:5px 0;'>" + candidat.getPrenom() + " " + candidat.getNom() + "</td></tr>"
                + "<tr><td style='color:#6B7280;font-size:14px;padding:5px 0;'>📋 Type :</td>"
                + "<td style='color:#111827;font-weight:600;font-size:14px;padding:5px 0;'>" + entretien.getTypeEntretien() + "</td></tr>"
                + "<tr><td style='color:#6B7280;font-size:14px;padding:5px 0;'>📅 Date :</td>"
                + "<td style='color:#111827;font-weight:600;font-size:14px;padding:5px 0;'>" + entretien.getDateEntretien() + "</td></tr>"
                + "<tr><td style='color:#6B7280;font-size:14px;padding:5px 0;'>📍 Lieu :</td>"
                + "<td style='color:#111827;font-weight:600;font-size:14px;padding:5px 0;'>" + entretien.getLieu() + "</td></tr>"
                + "</table></div>"

                // Urgency note
                + "<div style='background:#FEF3C7;border:1px solid #FCD34D;border-radius:8px;padding:15px 20px;margin:0 0 25px;'>"
                + "<p style='color:#92400E;font-size:13px;margin:0;'>⚡ <strong>Rappel :</strong> L'évaluation permet de fournir un retour au candidat et de prendre la décision finale de recrutement.</p>"
                + "</div>"

                + "<p style='color:#6B7280;font-size:14px;'>Connectez-vous à l'application VOS pour effectuer l'évaluation.</p>"
                + "</div>"

                // Footer
                + "<div style='background:#F9FAFB;padding:20px 35px;text-align:center;border-top:1px solid #E5E7EB;'>"
                + "<p style='color:#9CA3AF;font-size:12px;margin:0;'>Cet email a été envoyé automatiquement par VOS - Système de Gestion RH</p>"
                + "</div>"
                + "</div></body></html>";

        sendEmail(admin.getEmail(), admin.getPrenom() + " " + admin.getNom(), subject, html);
    }

    // =============================================================
    // EMAIL 3 : RÉSULTATS ÉVALUATION → CANDIDAT
    // =============================================================
    public static void envoyerResultatsEvaluation(Entretien entretien,
                                                  EvaluationEntretien evaluation,
                                                  Utilisateur candidat) {
        boolean accepte = evaluation.getDecision().equalsIgnoreCase("Accepté");
        boolean refuse  = evaluation.getDecision().equalsIgnoreCase("Refusé");

        String subject = accepte
                ? "🎉 Félicitations ! Résultats de votre entretien | VOS"
                : refuse
                ? "📋 Résultats de votre entretien | VOS"
                : "📋 Résultats de votre entretien - " + evaluation.getDecision() + " | VOS";

        // Color scheme based on decision
        String bannerColor = accepte ? "linear-gradient(135deg,#059669,#10B981)"
                : refuse  ? "linear-gradient(135deg,#DC2626,#EF4444)"
                :           "linear-gradient(135deg,#D97706,#F59E0B)";
        String bannerIcon  = accepte ? "🎉" : refuse ? "📋" : "⏳";
        String decisionMsg = accepte ? "Votre candidature a été retenue !"
                : refuse  ? "Votre candidature n'a pas été retenue cette fois."
                :           "Votre dossier est en cours d'examen.";
        String badgeColor  = accepte ? "#D1FAE5" : refuse ? "#FEE2E2" : "#FEF3C7";
        String badgeText   = accepte ? "#059669"  : refuse ? "#DC2626"  : "#D97706";

        // Score bar width (on 100%)
        int scoreWidth = (int) Math.min(evaluation.getScoreTest(), 100);
        String scoreBarColor = scoreWidth >= 70 ? "#10B981" : scoreWidth >= 50 ? "#F59E0B" : "#EF4444";

        // Note stars
        StringBuilder stars = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            stars.append(i <= evaluation.getNoteEntretien() ? "⭐" : "☆");
        }

        String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body style='"
                + "margin:0;padding:0;background-color:#F5F6F8;font-family:Segoe UI,Arial,sans-serif;'>"

                + "<div style='background:linear-gradient(135deg,#1a1a2e,#16213e);padding:40px 0;text-align:center;'>"
                + "<h1 style='color:white;margin:0;font-size:28px;'>📋 VOS - Gestion RH</h1>"
                + "<p style='color:#9CA3AF;margin:8px 0 0;'>Système de Gestion des Entretiens</p>"
                + "</div>"

                + "<div style='max-width:600px;margin:30px auto;background:white;border-radius:16px;"
                + "box-shadow:0 4px 24px rgba(0,0,0,0.08);overflow:hidden;'>"

                // Title band
                + "<div style='background:" + bannerColor + ";padding:25px 35px;'>"
                + "<h2 style='color:white;margin:0;font-size:22px;'>" + bannerIcon + " Résultats de votre Entretien</h2>"
                + "<p style='color:rgba(255,255,255,0.85);margin:6px 0 0;font-size:14px;'>" + decisionMsg + "</p>"
                + "</div>"

                + "<div style='padding:35px;'>"
                + "<p style='color:#374151;font-size:16px;margin:0 0 20px;'>Bonjour <strong>" + candidat.getPrenom() + " " + candidat.getNom() + "</strong>,</p>"
                + "<p style='color:#6B7280;font-size:15px;line-height:1.6;margin:0 0 25px;'>"
                + "Suite à votre entretien <strong>" + entretien.getTypeEntretien() + "</strong> du <strong>" + entretien.getDateEntretien()
                + "</strong>, voici les résultats de votre évaluation."
                + "</p>"

                // Decision badge
                + "<div style='text-align:center;margin:0 0 28px;'>"
                + "<span style='display:inline-block;background:" + badgeColor + ";color:" + badgeText + ";"
                + "font-size:18px;font-weight:700;padding:12px 30px;border-radius:50px;'>"
                + "Décision : " + evaluation.getDecision().toUpperCase()
                + "</span></div>"

                // Scores
                + "<div style='background:#F9FAFB;border-radius:12px;padding:22px 25px;margin:0 0 22px;'>"
                + "<h3 style='color:#111827;margin:0 0 18px;font-size:16px;'>📊 Vos Résultats</h3>"

                // Score bar
                + "<div style='margin:0 0 18px;'>"
                + "<div style='display:flex;justify-content:space-between;margin:0 0 6px;'>"
                + "<span style='color:#6B7280;font-size:13px;'>Score du Test</span>"
                + "<span style='color:#111827;font-weight:700;font-size:15px;'>" + String.format("%.1f", evaluation.getScoreTest()) + " / 100</span>"
                + "</div>"
                + "<div style='background:#E5E7EB;border-radius:8px;height:10px;overflow:hidden;'>"
                + "<div style='background:" + scoreBarColor + ";width:" + scoreWidth + "%;height:100%;border-radius:8px;'></div>"
                + "</div></div>"

                // Note stars
                + "<div style='display:flex;justify-content:space-between;align-items:center;padding:12px 0;border-top:1px solid #E5E7EB;'>"
                + "<span style='color:#6B7280;font-size:13px;'>Note d'Entretien</span>"
                + "<span style='font-size:18px;'>" + stars + " <strong style='color:#111827;font-size:14px;'>(" + evaluation.getNoteEntretien() + "/5)</strong></span>"
                + "</div></div>"

                // Commentaire
                + "<div style='background:#F0F9FF;border-left:4px solid #0EA5E9;border-radius:8px;padding:18px 22px;margin:0 0 25px;'>"
                + "<h4 style='color:#0369A1;margin:0 0 10px;font-size:14px;'>💬 Commentaire de l'évaluateur</h4>"
                + "<p style='color:#374151;font-size:14px;line-height:1.6;margin:0;font-style:italic;'>\"" + evaluation.getCommentaire() + "\"</p>"
                + "</div>"

                // Closing message
                + (accepte ? "<div style='background:#F0FDF4;border:1px solid #BBF7D0;border-radius:8px;padding:18px 22px;margin:0 0 20px;'>"
                + "<p style='color:#15803D;font-size:14px;margin:0;'>🎊 Toutes nos félicitations ! Notre équipe RH vous contactera prochainement pour les prochaines étapes.</p>"
                + "</div>"
                : refuse ? "<div style='background:#FFF1F2;border:1px solid #FECDD3;border-radius:8px;padding:18px 22px;margin:0 0 20px;'>"
                + "<p style='color:#BE123C;font-size:14px;margin:0;'>Nous vous remercions pour votre candidature et votre temps. Nous vous encourageons à postuler pour de futures opportunités.</p>"
                + "</div>" : "")

                + "<p style='color:#6B7280;font-size:14px;'>Pour toute question : <a href='mailto:" + FROM_EMAIL + "' style='color:#2563EB;'>" + FROM_EMAIL + "</a></p>"
                + "</div>"

                + "<div style='background:#F9FAFB;padding:20px 35px;text-align:center;border-top:1px solid #E5E7EB;'>"
                + "<p style='color:#9CA3AF;font-size:12px;margin:0;'>Cet email a été envoyé automatiquement par VOS - Système de Gestion RH</p>"
                + "</div>"
                + "</div></body></html>";

        sendEmail(candidat.getEmail(), candidat.getPrenom() + " " + candidat.getNom(), subject, html);
    }

    /**
     * Nettoie et formate le texte des questions pour l'affichage dans l'email
     */
    private static String nettoyerQuestionsEmail(String questions) {
        if (questions == null || questions.trim().isEmpty()) {
            return "";
        }

        // Enlever les avertissements IA si présents
        String texte = questions;
        if (texte.contains("⚠️")) {
            int indexQuestions = texte.indexOf("\n\n");
            if (indexQuestions > 0) {
                texte = texte.substring(indexQuestions).trim();
            }
        }

        // S'assurer que chaque question commence sur une nouvelle ligne
        texte = texte.replace("\\n", "\n");

        // Échapper les caractères HTML
        texte = texte.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");

        return texte;
    }
}