package controllers;

import entities.Utilisateur;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.EmailService;
import services.ServiceUtilisateur;

/**
 * Contrôleur pour la réinitialisation de mot de passe
 */
public class ForgotPasswordController {
    
    // ========== PAGE 1 : EMAIL + CODE ==========
    @FXML private TextField tfEmail;
    @FXML private TextField tfCode;
    @FXML private VBox codeSection;
    @FXML private Label lblMessage;
    @FXML private Button btnSendCode;
    
    // ========== PAGE 2 : NOUVEAU MOT DE PASSE ==========
    @FXML private PasswordField tfNewPassword;
    @FXML private PasswordField tfConfirmPassword;
    @FXML private Label lblResetMessage;
    
    private ServiceUtilisateur serviceUtilisateur = new ServiceUtilisateur();
    private String verifiedEmail = null; // Email vérifié après validation du code
    
    /**
     * Envoie le code de vérification par email
     */
    @FXML
    public void sendVerificationCode() {
        String email = tfEmail.getText().trim();
        
        // Validation email
        if (email.isEmpty()) {
            showMessage(lblMessage, "❌ Veuillez entrer votre adresse email", "error");
            shakeAnimation(tfEmail);
            return;
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            showMessage(lblMessage, "❌ Format d'email invalide", "error");
            shakeAnimation(tfEmail);
            return;
        }
        
        // Vérifier si l'utilisateur existe
        Utilisateur user = serviceUtilisateur.getUserByEmail(email);
        if (user == null) {
            showMessage(lblMessage, "❌ Aucun compte associé à cet email", "error");
            shakeAnimation(tfEmail);
            return;
        }
        
        // Désactiver le bouton pendant l'envoi
        btnSendCode.setDisable(true);
        btnSendCode.setText("⏳ Envoi en cours...");
        
        // Envoyer le code (dans un thread séparé pour ne pas bloquer l'UI)
        new Thread(() -> {
            boolean sent = EmailService.sendVerificationCode(email);
            
            javafx.application.Platform.runLater(() -> {
                btnSendCode.setDisable(false);
                btnSendCode.setText("📨 Renvoyer le code");
                
                if (sent) {
                    showMessage(lblMessage, "✅ Code envoyé ! Vérifiez votre boîte mail", "success");
                    
                    // Afficher la section du code
                    codeSection.setVisible(true);
                    codeSection.setManaged(true);
                    
                    // Animation d'apparition
                    FadeTransition fade = new FadeTransition(Duration.millis(300), codeSection);
                    fade.setFromValue(0);
                    fade.setToValue(1);
                    fade.play();
                } else {
                    showMessage(lblMessage, "❌ Erreur lors de l'envoi de l'email", "error");
                }
            });
        }).start();
    }
    
    /**
     * Vérifie le code entré par l'utilisateur
     */
    @FXML
    public void verifyCode() {
        String email = tfEmail.getText().trim();
        String code = tfCode.getText().trim();
        
        // Validation
        if (code.isEmpty()) {
            showMessage(lblMessage, "❌ Veuillez entrer le code reçu", "error");
            shakeAnimation(tfCode);
            return;
        }
        
        if (code.length() != 6) {
            showMessage(lblMessage, "❌ Le code doit contenir 6 chiffres", "error");
            shakeAnimation(tfCode);
            return;
        }
        
        // Vérifier le code
        boolean isValid = EmailService.verifyCode(email, code);
        
        if (isValid) {
            verifiedEmail = email;
            showMessage(lblMessage, "✅ Code vérifié ! Redirection...", "success");
            
            // Attendre 1 seconde puis naviguer vers la page de réinitialisation
            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            pause.setOnFinished(e -> navigateToResetPassword());
            pause.play();
        } else {
            showMessage(lblMessage, "❌ Code invalide ou expiré", "error");
            shakeAnimation(tfCode);
        }
    }
    
    /**
     * Navigue vers la page de réinitialisation du mot de passe
     */
    private void navigateToResetPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ResetPasswordView.fxml"));
            Parent root = loader.load();
            
            // Passer l'email vérifié au contrôleur de la page 2
            ForgotPasswordController controller = loader.getController();
            controller.setVerifiedEmail(verifiedEmail);
            
            Stage stage = (Stage) tfEmail.getScene().getWindow();
            Scene scene = new Scene(root, 1440, 1024);
            stage.setScene(scene);
            stage.show();
            
        } catch (Exception e) {
            System.err.println("❌ Erreur navigation vers ResetPasswordView: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Définit l'email vérifié (appelé depuis la page 1)
     */
    public void setVerifiedEmail(String email) {
        this.verifiedEmail = email;
    }
    
    /**
     * Réinitialise le mot de passe
     */
    @FXML
    public void resetPassword() {
        String newPassword = tfNewPassword.getText();
        String confirmPassword = tfConfirmPassword.getText();
        
        // Validation
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showMessage(lblResetMessage, "❌ Veuillez remplir tous les champs", "error");
            shakeAnimation(tfNewPassword);
            return;
        }
        
        if (newPassword.length() < 6) {
            showMessage(lblResetMessage, "❌ Le mot de passe doit contenir au moins 6 caractères", "error");
            shakeAnimation(tfNewPassword);
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            showMessage(lblResetMessage, "❌ Les mots de passe ne correspondent pas", "error");
            shakeAnimation(tfConfirmPassword);
            return;
        }
        
        // Mettre à jour le mot de passe dans la base de données
        boolean updated = serviceUtilisateur.updatePassword(verifiedEmail, newPassword);
        
        if (updated) {
            showMessage(lblResetMessage, "✅ Mot de passe réinitialisé avec succès !", "success");
            
            // Supprimer le code de vérification
            EmailService.clearCode(verifiedEmail);
            
            // Rediriger vers la page de connexion après 2 secondes
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(e -> goToSignin());
            pause.play();
        } else {
            showMessage(lblResetMessage, "❌ Erreur lors de la réinitialisation", "error");
        }
    }
    
    /**
     * Retourne à la page de connexion
     */
    @FXML
    public void goToSignin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SigninView.fxml"));
            Parent root = loader.load();
            
            // Récupérer le Stage depuis le bon champ selon la page active
            Stage stage = null;
            if (tfEmail != null && tfEmail.getScene() != null) {
                stage = (Stage) tfEmail.getScene().getWindow();
            } else if (tfNewPassword != null && tfNewPassword.getScene() != null) {
                stage = (Stage) tfNewPassword.getScene().getWindow();
            } else if (lblMessage != null && lblMessage.getScene() != null) {
                stage = (Stage) lblMessage.getScene().getWindow();
            } else if (lblResetMessage != null && lblResetMessage.getScene() != null) {
                stage = (Stage) lblResetMessage.getScene().getWindow();
            }
            
            if (stage != null) {
                Scene scene = new Scene(root, 1440, 1024);
                stage.setScene(scene);
                stage.show();
            } else {
                System.err.println("❌ Impossible de récupérer le Stage");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur navigation vers SigninView: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Affiche un message avec style
     */
    private void showMessage(Label label, String message, String type) {
        if (label == null) return;
        
        label.setText(message);
        
        if ("error".equals(type)) {
            label.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
        } else if ("success".equals(type)) {
            label.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
        }
        
        // Animation de fade-in
        FadeTransition fade = new FadeTransition(Duration.millis(300), label);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }
    
    /**
     * Animation de shake pour les erreurs
     */
    private void shakeAnimation(javafx.scene.Node node) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), node);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }
}
