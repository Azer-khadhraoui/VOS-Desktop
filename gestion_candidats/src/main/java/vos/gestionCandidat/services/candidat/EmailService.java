package vos.gestionCandidat.services.candidat;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service d'envoi d'emails automatiques pour les candidatures
 * Utilise JavaMail API avec Gmail SMTP
 */
public class EmailService {
    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());
    private static final String CONFIG_FILE = "/mail.properties";

    private String senderEmail;
    private String senderPassword;
    private Properties emailProperties;

    /**
     * Constructeur - Initialise la configuration SMTP depuis mail.properties
     */
    public EmailService() {
        this.emailProperties = new Properties();
        loadConfiguration();
    }

    /**
     * Charge la configuration depuis le fichier mail.properties
     */
    private void loadConfiguration() {
        try (InputStream input = getClass().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                LOGGER.log(Level.SEVERE, "Fichier mail.properties non trouvé!");
                return;
            }
            emailProperties.load(input);
            this.senderEmail = emailProperties.getProperty("sender.email");
            this.senderPassword = emailProperties.getProperty("sender.password");

            LOGGER.log(Level.INFO, "Configuration SMTP chargée avec succès");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement de la configuration: " + e.getMessage());
        }
    }

    /**
     * Méthode générique d'envoi d'email
     * @param to Adresse email destinataire
     * @param subject Sujet de l'email
     * @param message Corps du message (peut être du HTML)
     */
    public void sendEmail(String to, String subject, String message) {
        // Exécuter l'envoi dans un thread séparé pour ne pas bloquer l'UI
        Thread emailThread = new Thread(() -> {
            try {
                // Créer une session avec authentification
                Session session = createEmailSession();

                // Créer le message
                MimeMessage mimeMessage = new MimeMessage(session);
                mimeMessage.setFrom(new InternetAddress(senderEmail));
                mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
                mimeMessage.setSubject(subject, "UTF-8");
                mimeMessage.setContent(message, "text/html; charset=UTF-8");

                // Envoyer le message
                Transport.send(mimeMessage);
                LOGGER.log(Level.INFO, "Email envoyé avec succès à: " + to);

            } catch (MessagingException e) {
                LOGGER.log(Level.SEVERE, "Erreur lors de l'envoi de l'email à " + to + ": " + e.getMessage());
            }
        });

        // Démarrer le thread en tant que daemon
        emailThread.setDaemon(true);
        emailThread.start();
    }

    /**
     * Envoie un email lors de la création d'une candidature
     * @param candidageName Nom du candidat
     * @param candidateEmail Email du candidat
     * @param positionName Nom du poste
     * @param candidatureDate Date de la candidature
     */
    public void sendCandidatureCreatedEmail(String candidageName, String candidateEmail,
                                            String positionName, String candidatureDate) {
        String subject = "Confirmation de candidature - " + positionName;
        String message = buildCandidatureCreatedMessage(candidageName, positionName, candidatureDate);
        sendEmail(candidateEmail, subject, message);
    }

    /**
     * Envoie un email lors de la modification d'une candidature
     * @param candidageName Nom du candidat
     * @param candidateEmail Email du candidat
     * @param positionName Nom du poste
     * @param newStatus Nouveau statut
     */
    public void sendCandidatureUpdatedEmail(String candidageName, String candidateEmail,
                                            String positionName, String newStatus) {
        String subject = "Mise à jour de votre candidature - " + positionName;
        String message = buildCandidatureUpdatedMessage(candidageName, positionName, newStatus);
        sendEmail(candidateEmail, subject, message);
    }

    /**
     * Envoie un email lors de la suppression d'une candidature
     * @param candidageName Nom du candidat
     * @param candidateEmail Email du candidat
     * @param positionName Nom du poste
     */
    public void sendCandidatureDeletedEmail(String candidageName, String candidateEmail,
                                            String positionName) {
        String subject = "Candidature supprimée - " + positionName;
        String message = buildCandidatureDeletedMessage(candidageName, positionName);
        sendEmail(candidateEmail, subject, message);
    }

    /**
     * Crée le corps HTML de l'email de création de candidature
     */
    private String buildCandidatureCreatedMessage(String candidateName, String positionName, String date) {
        return "<html>" +
                "<body style='font-family: Arial, sans-serif; color: #333;'>" +
                "<h2 style='color: #2c3e50;'>Bienvenue!</h2>" +
                "<p>Bonjour <strong>" + candidateName + "</strong>,</p>" +
                "<p>Votre candidature pour le poste de <strong>" + positionName + "</strong> a été reçue avec succès.</p>" +
                "<table style='border-collapse: collapse; width: 100%; margin: 20px 0;'>" +
                "<tr style='background-color: #ecf0f1;'>" +
                "<td style='padding: 10px; border: 1px solid #bdc3c7;'><strong>Poste:</strong></td>" +
                "<td style='padding: 10px; border: 1px solid #bdc3c7;'>" + positionName + "</td>" +
                "</tr>" +
                "<tr>" +
                "<td style='padding: 10px; border: 1px solid #bdc3c7;'><strong>Date de candidature:</strong></td>" +
                "<td style='padding: 10px; border: 1px solid #bdc3c7;'>" + date + "</td>" +
                "</tr>" +
                "</table>" +
                "<p>Nous vous remercions de votre intérêt. Vous recevrez un retour très bientôt.</p>" +
                "<p>Cordialement,<br/>L'équipe RH</p>" +
                "</body>" +
                "</html>";
    }

    /**
     * Crée le corps HTML de l'email de mise à jour de candidature
     */
    private String buildCandidatureUpdatedMessage(String candidateName, String positionName, String status) {
        String statusColor = getStatusColor(status);
        return "<html>" +
                "<body style='font-family: Arial, sans-serif; color: #333;'>" +
                "<h2 style='color: #2c3e50;'>Mise à jour de votre candidature</h2>" +
                "<p>Bonjour <strong>" + candidateName + "</strong>,</p>" +
                "<p>Le statut de votre candidature pour le poste de <strong>" + positionName + "</strong> a été modifié.</p>" +
                "<table style='border-collapse: collapse; width: 100%; margin: 20px 0;'>" +
                "<tr style='background-color: #ecf0f1;'>" +
                "<td style='padding: 10px; border: 1px solid #bdc3c7;'><strong>Poste:</strong></td>" +
                "<td style='padding: 10px; border: 1px solid #bdc3c7;'>" + positionName + "</td>" +
                "</tr>" +
                "<tr>" +
                "<td style='padding: 10px; border: 1px solid #bdc3c7;'><strong>Nouveau statut:</strong></td>" +
                "<td style='padding: 10px; border: 1px solid #bdc3c7;'>" +
                "<span style='background-color: " + statusColor + "; color: white; padding: 5px 10px; border-radius: 3px;'>" +
                status + "</span>" +
                "</td>" +
                "</tr>" +
                "</table>" +
                "<p>Merci de votre patience!</p>" +
                "<p>Cordialement,<br/>L'équipe RH</p>" +
                "</body>" +
                "</html>";
    }

    /**
     * Crée le corps HTML de l'email de suppression de candidature
     */
    private String buildCandidatureDeletedMessage(String candidateName, String positionName) {
        return "<html>" +
                "<body style='font-family: Arial, sans-serif; color: #333;'>" +
                "<h2 style='color: #e74c3c;'>Candidature supprimée</h2>" +
                "<p>Bonjour <strong>" + candidateName + "</strong>,</p>" +
                "<p>Votre candidature pour le poste de <strong>" + positionName + "</strong> a été supprimée.</p>" +
                "<table style='border-collapse: collapse; width: 100%; margin: 20px 0;'>" +
                "<tr style='background-color: #ecf0f1;'>" +
                "<td style='padding: 10px; border: 1px solid #bdc3c7;'><strong>Poste:</strong></td>" +
                "<td style='padding: 10px; border: 1px solid #bdc3c7;'>" + positionName + "</td>" +
                "</tr>" +
                "</table>" +
                "<p>Si vous avez des questions, n'hésitez pas à nous contacter.</p>" +
                "<p>Cordialement,<br/>L'équipe RH</p>" +
                "</body>" +
                "</html>";
    }

    /**
     * Retourne la couleur correspondant au statut
     */
    private String getStatusColor(String status) {
        return switch (status.toLowerCase()) {
            case "acceptée" -> "#27ae60";  // Vert
            case "rejetée" -> "#e74c3c";   // Rouge
            case "en cours" -> "#f39c12";  // Orange
            case "en attente" -> "#3498db"; // Bleu
            default -> "#95a5a6";          // Gris
        };
    }

    /**
     * Crée une session JavaMail avec authentification SMTP
     */
    private Session createEmailSession() {
        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        };

        return Session.getInstance(emailProperties, authenticator);
    }

    /**
     * Vérifie si le service est correctement configuré
     */
    public boolean isConfigured() {
        return senderEmail != null && !senderEmail.isEmpty() &&
                senderPassword != null && !senderPassword.isEmpty();
    }
}