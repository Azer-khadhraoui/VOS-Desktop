package org.example.services;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.example.entities.OffreEmploi;

public class EmailService {

    private String emailFrom;
    private String emailPassword;
    private String smtpHost;
    private String smtpPort;

    public EmailService() {
        loadEmailConfig();
    }

    /**
     * Loads email configuration from config.properties file
     */
    private void loadEmailConfig() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("config.properties file not found");
                return;
            }
            props.load(input);
            this.emailFrom = props.getProperty("mail.smtp.from", "obelhaj488@gmail.com");
            this.emailPassword = props.getProperty("mail.smtp.password", "");
            this.smtpHost = props.getProperty("mail.smtp.host", "smtp.gmail.com");
            this.smtpPort = props.getProperty("mail.smtp.port", "587");
        } catch (IOException e) {
            System.out.println("Error loading email config: " + e.getMessage());
        }
    }

    /**
     * Sends a notification email when a new job offer is created
     *
     * @param recipientEmail The recipient's email address
     * @param offre The OffreEmploi object with all offer details
     * @return true if email was sent successfully, false otherwise
     */
    public boolean sendOffreInsertionEmail(String recipientEmail, OffreEmploi offre) {
        try {
            // Set up mail properties
            Properties props = new Properties();
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", smtpPort);
            props.put("mail.smtp.auth", "true");
            
            // Enable modern TLS protocols (required for Java 17+)
            props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
            props.put("mail.smtp.ssl.trust", smtpHost);
            
            // Configure based on port type
            int port = Integer.parseInt(smtpPort);
            if (port == 465) {
                // Port 465 uses pure SSL from the start
                props.put("mail.smtp.socketFactory.port", "465");
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.socketFactory.fallback", "false");
                props.put("mail.smtp.ssl.enable", "true");
                props.put("mail.smtp.ssl.checkserveridentity", "true");
            } else {
                // Port 587 uses STARTTLS (plain connection upgraded to TLS)
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.starttls.required", "true");
                props.put("mail.smtp.ssl.checkserveridentity", "true");
            }
            
            props.put("mail.smtp.connectiontimeout", "10000");
            props.put("mail.smtp.timeout", "10000");
            props.put("mail.smtp.writetimeout", "10000");

            // Create session with authentication
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(emailFrom, emailPassword);
                }
            });

            // Create the message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailFrom));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Nouvelle offre d'emploi créée: " + offre.getTitre());

            // Create HTML content with all offer details
            String htmlContent = buildEmailContent(offre);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            // Send the message
            Transport.send(message);
            System.out.println("Email sent successfully to: " + recipientEmail);
            return true;

        } catch (MessagingException e) {
            System.out.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Builds the HTML content of the email with all offer details
     *
     * @param offre The OffreEmploi object with all offer information
     * @return The HTML email content
     */
    private String buildEmailContent(OffreEmploi offre) {
        return "<html>\n" +
                "<head>\n" +
                "<style>\n" +
                "body { font-family: Arial, sans-serif; color: #333; }\n" +
                ".container { max-width: 650px; margin: 0 auto; padding: 20px; }\n" +
                ".header { background: linear-gradient(135deg, #007bff 0%, #0056b3 100%); color: white; padding: 25px; border-radius: 8px; text-align: center; }\n" +
                ".content { padding: 25px; border: 1px solid #ddd; border-radius: 8px; margin-top: 20px; background-color: #f9f9f9; }\n" +
                ".info-section { margin: 20px 0; }\n" +
                ".info-label { font-weight: bold; color: #007bff; display: inline-block; width: 150px; }\n" +
                ".info-value { color: #333; }\n" +
                ".footer { margin-top: 25px; font-size: 12px; color: #666; text-align: center; border-top: 1px solid #ddd; padding-top: 15px; }\n" +
                ".title { color: #007bff; font-size: 22px; font-weight: bold; margin-bottom: 15px; }\n" +
                ".badge { display: inline-block; padding: 5px 12px; background-color: #007bff; color: white; border-radius: 20px; font-size: 12px; margin-right: 5px; }\n" +
                ".description-text { background-color: white; padding: 15px; border-left: 4px solid #007bff; margin: 15px 0; line-height: 1.6; }\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div class=\"container\">\n" +
                "<div class=\"header\">\n" +
                "<h1>🎉 Nouvelle Offre d'Emploi Créée</h1>\n" +
                "<p>Une nouvelle offre a été publiée avec succès</p>\n" +
                "</div>\n" +
                "<div class=\"content\">\n" +
                "<p class=\"title\">📌 " + offre.getTitre() + "</p>\n" +
                "<div class=\"info-section\">\n" +
                "<span class=\"info-label\">Type de Contrat:</span>\n" +
                "<span class=\"badge\">" + (offre.getTypeContrat() != null ? offre.getTypeContrat() : "Non spécifié") + "</span><br>\n" +
                "</div>\n" +
                "<div class=\"info-section\">\n" +
                "<span class=\"info-label\">Statut:</span>\n" +
                "<span class=\"info-value\">" + (offre.getStatutOffre() != null ? offre.getStatutOffre() : "Non spécifié") + "</span><br>\n" +
                "</div>\n" +
                "<div class=\"info-section\">\n" +
                "<span class=\"info-label\">Lieu:</span>\n" +
                "<span class=\"info-value\">📍 " + (offre.getLieu() != null ? offre.getLieu() : "Non spécifié") + "</span><br>\n" +
                "</div>\n" +
                "<div class=\"info-section\">\n" +
                "<span class=\"info-label\">Mode de Travail:</span>\n" +
                "<span class=\"info-value\">" + getWorkPreferenceIcon(offre.getWorkPreference()) + " " + (offre.getWorkPreference() != null ? offre.getWorkPreference() : "Non spécifié") + "</span><br>\n" +
                "</div>\n" +
                "<div class=\"info-section\">\n" +
                "<span class=\"info-label\">Date de Création:</span>\n" +
                "<span class=\"info-value\">📅 " + (offre.getDatePublication() != null ? offre.getDatePublication().toString() : "Non spécifiée") + "</span><br>\n" +
                "</div>\n" +
                "<div class=\"info-section\" style=\"margin-top: 25px;\">\n" +
                "<p style=\"margin: 0 0 10px 0; font-weight: bold;\">📄 Description Complète:</p>\n" +
                "<div class=\"description-text\">\n" +
                "" + (offre.getDescription() != null ? offre.getDescription().replace("\\n", "<br>") : "Aucune description fournie") + "\n" +
                "</div>\n" +
                "</div>\n" +
                "</div>\n" +
                "<div class=\"footer\">\n" +
                "<p>✅ Cette offre d'emploi a été créée avec succès dans le système.</p>\n" +
                "<p>Cet email a été généré automatiquement. Veuillez ne pas répondre à cet email.</p>\n" +
                "</div>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * Returns an emoji icon based on work preference
     *
     * @param workPreference The work preference (Remote, Hybrid, On-site)
     * @return An appropriate emoji icon
     */
    private String getWorkPreferenceIcon(String workPreference) {
        if (workPreference == null) {
            return "🏢";
        }
        return switch (workPreference.toLowerCase()) {
            case "remote" -> "🏠";
            case "hybrid" -> "🔄";
            default -> "🏢";
        };
    }
}

