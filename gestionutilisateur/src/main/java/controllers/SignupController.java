package controllers;

import entities.Utilisateur;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.ServiceUtilisateur;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class SignupController {

    @FXML private TextField tfNom;
    @FXML private TextField tfPrenom;
    @FXML private TextField tfEmail;
    @FXML private PasswordField tfPassword;
    @FXML private PasswordField tfConfirmPassword;
    @FXML private Label lblMessage;
    @FXML private Label lblPasswordMatch;
    @FXML private ProgressBar passwordStrengthBar;
    @FXML private Label passwordStrengthLabel;
    @FXML private StackPane profileImageContainer;
    @FXML private ImageView imgProfile;

    private String imageName = "default.png";
    private String imageAbsolutePath = null;  // NOUVEAU : chemin absolu
    
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    ServiceUtilisateur su = new ServiceUtilisateur();

    @FXML
    public void initialize() {
        // Écouter les changements du mot de passe pour la barre de force
        tfPassword.textProperty().addListener((obs, oldVal, newVal) -> updatePasswordStrength(newVal));
        
        // Écouter les changements de confirmation de mot de passe
        tfConfirmPassword.textProperty().addListener((obs, oldVal, newVal) -> checkPasswordMatch());
    }

    // =====================================================
    // PASSWORD STRENGTH INDICATOR
    // =====================================================
    private void updatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            passwordStrengthBar.setProgress(0);
            passwordStrengthLabel.setText("Aucun mot de passe");
            passwordStrengthLabel.setStyle("-fx-text-fill: #999;");
            return;
        }
        
        double strength = 0.0;
        String label = "";
        
        // Calcul de la force
        if (password.length() >= 8) strength += 0.2;
        if (password.length() >= 12) strength += 0.15;
        if (password.matches(".*[a-z].*")) strength += 0.15;
        if (password.matches(".*[A-Z].*")) strength += 0.15;
        if (password.matches(".*[0-9].*")) strength += 0.15;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) strength += 0.2;
        
        // Normaliser entre 0 et 1
        strength = Math.min(1.0, strength);
        
        // Déterminer le niveau
        if (strength < 0.3) {
            label = "🔴 Très faible";
            passwordStrengthLabel.setStyle("-fx-text-fill: #e74c3c;");
        } else if (strength < 0.5) {
            label = "🟠 Faible";
            passwordStrengthLabel.setStyle("-fx-text-fill: #e67e22;");
        } else if (strength < 0.7) {
            label = "🟡 Moyen";
            passwordStrengthLabel.setStyle("-fx-text-fill: #f39c12;");
        } else if (strength < 0.9) {
            label = "🟢 Bon";
            passwordStrengthLabel.setStyle("-fx-text-fill: #27ae60;");
        } else {
            label = "🟢 Excellent";
            passwordStrengthLabel.setStyle("-fx-text-fill: #16a085;");
        }
        
        passwordStrengthBar.setProgress(strength);
        passwordStrengthLabel.setText(label);
        
        // Mise à jour de la couleur de la barre
        String styleClass = "";
        if (strength < 0.3) {
            passwordStrengthBar.setStyle("-fx-accent: #e74c3c;");
        } else if (strength < 0.5) {
            passwordStrengthBar.setStyle("-fx-accent: #e67e22;");
        } else if (strength < 0.7) {
            passwordStrengthBar.setStyle("-fx-accent: #f39c12;");
        } else {
            passwordStrengthBar.setStyle("-fx-accent: #27ae60;");
        }
    }
    
    private void checkPasswordMatch() {
        String password = tfPassword.getText();
        String confirmPassword = tfConfirmPassword.getText();
        
        if (password.isEmpty() || confirmPassword.isEmpty()) {
            lblPasswordMatch.setText("");
            return;
        }
        
        if (password.equals(confirmPassword)) {
            lblPasswordMatch.setText("✅ Les mots de passe correspondent");
            lblPasswordMatch.setStyle("-fx-text-fill: #27ae60;");
        } else {
            lblPasswordMatch.setText("❌ Les mots de passe ne correspondent pas");
            lblPasswordMatch.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    // =====================================================
    // UPLOAD IMAGE
    // =====================================================
    @FXML
    public void uploadImage() {

        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fc.showOpenDialog(null);

        if (file != null) {
            try {
                File folder = new File("images");
                if (!folder.exists()) folder.mkdir();

                // Générer un nom unique pour éviter les conflits
                String originalName = file.getName();
                String extension = "";
                int i = originalName.lastIndexOf('.');
                if (i > 0) {
                    extension = originalName.substring(i);
                    originalName = originalName.substring(0, i);
                }
                
                String cleanName = originalName.replaceAll("[^a-zA-Z0-9-_]", "_");
                String uniqueName = cleanName + "_" + System.currentTimeMillis() + extension;

                Path dest = Path.of("images", uniqueName);
                Files.copy(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

                imageName = uniqueName;
                
                // ENREGISTRER LE CHEMIN ABSOLU COMPLET
                imageAbsolutePath = dest.toFile().getAbsolutePath();
                System.out.println("=== UPLOAD IMAGE INSCRIPTION ===");
                System.out.println("Nom fichier: " + uniqueName);
                System.out.println("Chemin absolu: " + imageAbsolutePath);

                // Afficher l'image sélectionnée
                Image image = new Image(new FileInputStream(dest.toFile()));
                imgProfile.setImage(image);
                profileImageContainer.setVisible(true);
                profileImageContainer.setManaged(true);

                lblMessage.setStyle("-fx-text-fill: green;");
                lblMessage.setText("✅ Photo sélectionnée !");

            } catch (Exception e) {
                lblMessage.setStyle("-fx-text-fill: red;");
                lblMessage.setText("❌ Erreur upload image !");
                e.printStackTrace();
            }
        }
    }

    // =====================================================
    // SIGNUP
    // =====================================================
    @FXML
    public void signup() {
        // Validation des champs vides
        if (tfNom.getText().trim().isEmpty() ||
                tfPrenom.getText().trim().isEmpty() ||
                tfEmail.getText().trim().isEmpty() ||
                tfPassword.getText().trim().isEmpty() ||
                tfConfirmPassword.getText().trim().isEmpty()) {

            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("❌ Remplissez tous les champs !");
            return;
        }

        // Validation nom et prénom (minimum 2 caractères)
        if (tfNom.getText().trim().length() < 2 || tfPrenom.getText().trim().length() < 2) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("❌ Nom et prénom doivent contenir au moins 2 caractères !");
            return;
        }

        // Validation format email
        if (!tfEmail.getText().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("❌ Format d'email invalide !");
            return;
        }

        // Validation longueur mot de passe (minimum 6 caractères)
        if (tfPassword.getText().length() < 6) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("❌ Le mot de passe doit contenir au moins 6 caractères !");
            return;
        }
        
        // Validation correspondance des mots de passe
        if (!tfPassword.getText().equals(tfConfirmPassword.getText())) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("❌ Les mots de passe ne correspondent pas !");
            return;
        }

        if (su.emailExiste(tfEmail.getText())) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("❌ Email déjà utilisé !");
            return;
        }

        // Utiliser le chemin absolu si une image a été uploadée, sinon null
        String cheminImage = imageAbsolutePath;  // Peut être null si pas d'image
        
        // Hacher le mot de passe avant de l'enregistrer
        String hashedPassword = passwordEncoder.encode(tfPassword.getText());
        
        System.out.println("=== CRÉATION UTILISATEUR ===");
        System.out.println("Nom: " + tfNom.getText());
        System.out.println("Prénom: " + tfPrenom.getText());
        System.out.println("Email: " + tfEmail.getText());
        System.out.println("Image à enregistrer: " + cheminImage);
        System.out.println("Mot de passe hashé: " + hashedPassword);

        Utilisateur u = new Utilisateur(
                0,
                cheminImage,  // Utiliser le chemin absolu au lieu de imageName
                tfEmail.getText(),
                hashedPassword,  // Utiliser le mot de passe hashé
                "CLIENT",
                tfNom.getText(),
                tfPrenom.getText()
        );

        su.ajouter(u);

        lblMessage.setStyle("-fx-text-fill: green;");
        lblMessage.setText("✨ Inscription réussie !");
    }

    // =====================================================
    // TRANSITION SIGNUP → SIGNIN
    // =====================================================
    @FXML
    public void goToSignin() {
        animateTransition("/SigninView.fxml", -1440);
    }

    // =====================================================
    // MÉTHODE TRANSITION (EASE IN OUT 600ms)
    // =====================================================
    private void animateTransition(String fxmlPath, double startX) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newRoot = loader.load();

            Scene scene = tfEmail.getScene();
            Parent oldRoot = scene.getRoot();

            // Désactiver les events pendant la transition
            oldRoot.setDisable(true);

            newRoot.setTranslateX(startX);

            StackPane stack = new StackPane(oldRoot, newRoot);
            scene.setRoot(stack);

            TranslateTransition ttNew =
                    new TranslateTransition(Duration.millis(600), newRoot);
            ttNew.setFromX(startX);
            ttNew.setToX(0);
            ttNew.setInterpolator(Interpolator.EASE_BOTH);

            TranslateTransition ttOld =
                    new TranslateTransition(Duration.millis(600), oldRoot);
            ttOld.setFromX(0);
            ttOld.setToX(-startX);
            ttOld.setInterpolator(Interpolator.EASE_BOTH);

            ttNew.play();
            ttOld.play();

            ttNew.setOnFinished(e -> {
                newRoot.setTranslateX(0);
                stack.getChildren().remove(newRoot);
                scene.setRoot(newRoot);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
