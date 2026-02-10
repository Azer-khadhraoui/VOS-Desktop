package controllers;
import javafx.scene.Scene;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import services.ServiceUtilisateur;

public class SigninController {

    @FXML private TextField tfEmail;
    @FXML private PasswordField tfPassword;
    @FXML private Label lblMessage;

    ServiceUtilisateur su = new ServiceUtilisateur();

    // =====================================================
    // LOGIN
    // =====================================================
    @FXML
    public void signin() {

        if (su.login(tfEmail.getText(), tfPassword.getText())) {
            lblMessage.setStyle("-fx-text-fill: green;");
            lblMessage.setText("✅ Connexion réussie !");
        } else {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("❌ Email ou mot de passe incorrect !");
        }
    }

    // =====================================================
    // TRANSITION ANIMÉE SIGNIN → SIGNUP
    // =====================================================
    @FXML
    public void goToSignup() {
        animateTransition("/SignupView.fxml", 1440);
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
