package services;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.Properties;

public class RecrutementEmailService {
    // ✅ SMTP Configuration (Placeholders)
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String SMTP_EMAIL = "xsupernovaezzpzz@gmail.com"; // ⚠️ À MODIFIER
    private static final String SMTP_PASSWORD = "kegw jhvd ygvp sbux"; // ⚠️ À MODIFIER
    private static RecrutementEmailService instance;

    private RecrutementEmailService() {
    }

    public static synchronized RecrutementEmailService getInstance() {
        if (instance == null) {
            instance = new RecrutementEmailService();
        }
        return instance;
    }

    /**
     * Returns a static professional email body based on status.
     */
    public String getTemplate(String type, String candidateName, String jobTitle) {
        if ("Acceptation".equals(type)) {
            return String.format(
                    "Bonjour %s,\n\n" +
                            "Nous avons le plaisir de vous informer que votre candidature pour le poste de %s a été retenue.\n"
                            +
                            "Vous trouverez ci-joint votre contrat de travail au format PDF.\n\n" +
                            "Nous sommes impatients de vous accueillir au sein de l'équipe VOS.\n\n" +
                            "Cordialement,\nL'équipe Recrutement VOS",
                    candidateName, jobTitle);
        } else {
            return String.format(
                    "Bonjour %s,\n\n" +
                            "Nous vous remercions de l'intérêt que vous avez porté à notre société VOS.\n" +
                            "Après examen attentif de votre candidature pour le poste de %s, nous avons le regret de vous informer que nous ne pouvons y donner une suite favorable.\n\n"
                            +
                            "Nous vous souhaitons beaucoup de succès dans vos projets futurs.\n\n" +
                            "Cordialement,\nL'équipe Recrutement VOS",
                    candidateName, jobTitle);
        }
    }

    /**
     * Sends an email with an optional attachment.
     */
    public void sendEmail(String to, String subject, String body, File attachment) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_EMAIL, SMTP_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SMTP_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);

        // Body part
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(body);

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        // Attachment part
        if (attachment != null && attachment.exists()) {
            MimeBodyPart attachmentPart = new MimeBodyPart();
            try {
                attachmentPart.attachFile(attachment);
                multipart.addBodyPart(attachmentPart);
            } catch (Exception e) {
                System.err.println("Erreur d'attachement: " + e.getMessage());
            }
        }

        message.setContent(multipart);
        Transport.send(message);
    }
}
