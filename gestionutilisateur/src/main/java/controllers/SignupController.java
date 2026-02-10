package controllers;

import entities.Utilisateur;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import services.ServiceUtilisateur;
import javafx.scene.Scene;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class SignupController {

    @FXML private TextField tfNom;
    @FXML private TextField tfPrenom;
    @FXML private TextField tfEmail;
    @FXML private PasswordField tfPassword;
    @FXML private Label lblMessage;

    private String imageName = "default.png";

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

                Path dest = Path.of("images", file.getName());
                Files.copy(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

                imageName = file.getName();

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

        if (tfNom.getText().isEmpty() ||
                tfPrenom.getText().isEmpty() ||
                tfEmail.getText().isEmpty() ||
                tfPassword.getText().isEmpty()) {

            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("❌ Remplissez tous les champs !");
            return;
        }

        if (su.emailExiste(tfEmail.getText())) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("❌ Email déjà utilisé !");
            return;
        }

        Utilisateur u = new Utilisateur(
                0,
                imageName,
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
                scene.setRoot(newRoot);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
