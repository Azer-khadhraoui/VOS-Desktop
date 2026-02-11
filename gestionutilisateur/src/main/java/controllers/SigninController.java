package controllers;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.ServiceUtilisateur;
import utilis.UserSession;
import entities.Utilisateur;

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
        // Validation des champs
        if (tfEmail.getText().trim().isEmpty() || tfPassword.getText().trim().isEmpty()) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("❌ Veuillez remplir tous les champs !");
            return;
        }

        // Validation format email
        if (!tfEmail.getText().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("❌ Format d'email invalide !");
            return;
        }

        if (su.login(tfEmail.getText(), tfPassword.getText())) {
            lblMessage.setStyle("-fx-text-fill: green;");
            lblMessage.setText("✅ Connexion réussie !");
            
            // Récupérer l'utilisateur et le stocker en session
            Utilisateur user = su.getUserByEmail(tfEmail.getText());
            UserSession.getInstance().setCurrentUser(user);
            
            // Redirection selon le rôle après 500ms
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    javafx.application.Platform.runLater(() -> {
                        try {
                            String fxmlPath;
                            
                            // Vérifier le rôle de l'utilisateur
                            if ("ADMIN_RH".equals(user.getRole()) || "ADMIN_TECHNIQUE".equals(user.getRole())) {
                                // Rediriger vers le dashboard d'administration
                                fxmlPath = "/AdministrationView.fxml";
                            } else {
                                // CLIENT : Rediriger vers l'interface des offres
                                fxmlPath = "/OffresView.fxml";
                            }
                            
                            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                            Parent root = loader.load();
                            
                            Stage stage = (Stage) tfEmail.getScene().getWindow();
                            Scene newScene = new Scene(root, 1440, 1024);
                            
                            // Animation de transition
                            animateSceneTransition(stage, newScene);
                            
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
            
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
                stack.getChildren().remove(newRoot);
                scene.setRoot(newRoot);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =====================================================
    // ANIMATION TRANSITION DE CONNEXION
    // =====================================================
    private void animateSceneTransition(Stage stage, Scene newScene) {
        Parent root = newScene.getRoot();
        
        // État initial : invisible et réduit
        root.setOpacity(0);
        root.setScaleX(0.85);
        root.setScaleY(0.85);
        root.setTranslateY(30);
        
        // Appliquer la nouvelle scène
        stage.setScene(newScene);
        stage.setResizable(false);
        stage.centerOnScreen();
        
        // Animation Fade In
        FadeTransition fade = new FadeTransition(Duration.millis(600), root);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setInterpolator(Interpolator.EASE_OUT);
        
        // Animation Scale (zoom in)
        ScaleTransition scaleX = new ScaleTransition(Duration.millis(600), root);
        scaleX.setFromX(0.85);
        scaleX.setToX(1.0);
        scaleX.setInterpolator(Interpolator.EASE_OUT);
        
        ScaleTransition scaleY = new ScaleTransition(Duration.millis(600), root);
        scaleY.setFromY(0.85);
        scaleY.setToY(1.0);
        scaleY.setInterpolator(Interpolator.EASE_OUT);
        
        // Animation Slide (de bas en haut)
        TranslateTransition slide = new TranslateTransition(Duration.millis(600), root);
        slide.setFromY(30);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);
        
        // Lancer toutes les animations en parallèle
        ParallelTransition parallel = new ParallelTransition(fade, scaleX, scaleY, slide);
        parallel.play();
    }

}
