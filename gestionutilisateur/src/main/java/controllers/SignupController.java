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
    @FXML private Label lblMessage;
    @FXML private StackPane profileImageContainer;
    @FXML private ImageView imgProfile;

    private String imageName = "default.png";
    private String imageAbsolutePath = null;  // NOUVEAU : chemin absolu

    ServiceUtilisateur su = new ServiceUtilisateur();

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
                tfPassword.getText().trim().isEmpty()) {

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

        if (su.emailExiste(tfEmail.getText())) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("❌ Email déjà utilisé !");
            return;
        }

        // Utiliser le chemin absolu si une image a été uploadée, sinon null
        String cheminImage = imageAbsolutePath;  // Peut être null si pas d'image
        
        System.out.println("=== CRÉATION UTILISATEUR ===");
        System.out.println("Nom: " + tfNom.getText());
        System.out.println("Prénom: " + tfPrenom.getText());
        System.out.println("Email: " + tfEmail.getText());
        System.out.println("Image à enregistrer: " + cheminImage);

        Utilisateur u = new Utilisateur(
                0,
                cheminImage,  // Utiliser le chemin absolu au lieu de imageName
                tfEmail.getText(),
                tfPassword.getText(),
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
