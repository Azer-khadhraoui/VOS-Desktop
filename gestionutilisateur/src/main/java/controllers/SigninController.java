package controllers;

import entities.Utilisateur;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.ServiceUtilisateur;
import utilis.UserSession;

public class SigninController {

    @FXML
    private TextField tfEmail;
    @FXML
    private PasswordField tfPassword;
    @FXML
    private Label lblMessage;

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

            // LOG DÉTAILLÉ DE L'UTILISATEUR RÉCUPÉRÉ
            System.out.println("========================================");
            System.out.println("🔐 CONNEXION RÉUSSIE");
            System.out.println("========================================");
            System.out.println("ID: " + user.getId_utilisateur());
            System.out.println("Nom: " + user.getNom());
            System.out.println("Prénom: " + user.getPrenom());
            System.out.println("Email: " + user.getEmail());
            System.out.println("Role: " + user.getRole());
            System.out.println("Image_profil (DB): " + user.getImage_profil());
            System.out.println("========================================");

            UserSession.getInstance().setCurrentUser(user);

            // Vérifier que l'utilisateur est bien en session
            Utilisateur sessionUser = UserSession.getInstance().getCurrentUser();
            System.out.println("✓ Utilisateur mis en session");
            System.out.println("  - Image en session: " + sessionUser.getImage_profil());

            // Déterminer le chemin FXML selon le rôle
            String fxmlPath;
            if ("ADMIN_RH".equals(user.getRole()) || "ADMIN_TECHNIQUE".equals(user.getRole())) {
                fxmlPath = "/fxml/admin/AdminView.fxml";
            } else {
                fxmlPath = "/fxml/utilisateur/MainView.fxml";
            }

            // Lancer l'animation avec le logo
            playLogoAnimation(fxmlPath);

        } else {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("❌ Email ou mot de passe incorrect !");
        }
    }

    // =====================================================
    // ANIMATION SPECTACULAIRE AVEC LOGO
    // =====================================================
    private void playLogoAnimation(String fxmlPath) {
        try {
            Stage stage = (Stage) tfEmail.getScene().getWindow();

            // Charger l'écran de splash personnalisé
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SplashScreen.fxml"));
            Parent splashRoot = loader.load();
            Scene splashScene = new Scene(splashRoot);

            // Appliquer le splash screen
            stage.setScene(splashScene);
            stage.setFullScreen(false);
            stage.setMaximized(false);
            stage.centerOnScreen();

            // Attendre la fin de l'animation (3.2 secondes)
            PauseTransition delay = new PauseTransition(Duration.millis(3200));
            delay.setOnFinished(event -> {
                try {
                    FXMLLoader nextLoader = new FXMLLoader(getClass().getResource(fxmlPath));
                    Parent nextRoot = nextLoader.load();
                    Scene nextScene = new Scene(nextRoot);

                    // Transition finale vers le tableau de bord
                    animateSceneTransition(stage, nextScene);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            delay.play();

        } catch (Exception e) {
            e.printStackTrace();
            // Fallback direct en cas d'erreur
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent root = loader.load();
                Stage stage = (Stage) tfEmail.getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
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

            TranslateTransition ttNew = new TranslateTransition(Duration.millis(600), newRoot);
            ttNew.setFromX(startX);
            ttNew.setToX(0);
            ttNew.setInterpolator(Interpolator.EASE_BOTH);

            TranslateTransition ttOld = new TranslateTransition(Duration.millis(600), oldRoot);
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
        stage.setResizable(true);
        stage.setMaximized(true);
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

    // =====================================================
    // CONNEXION PAR RECONNAISSANCE FACIALE
    // =====================================================
    @FXML
    public void signinWithFace() {
        lblMessage.setStyle("-fx-text-fill: #667EEA;");
        lblMessage.setText("📷 Préparation de la caméra...");

        // Validation de l'email
        if (tfEmail.getText().trim().isEmpty()) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("❌ Veuillez saisir votre email d'abord !");
            return;
        }

        // Vérifier que l'utilisateur existe
        Utilisateur user = su.getUserByEmail(tfEmail.getText());
        if (user == null) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("❌ Aucun compte trouvé avec cet email !");
            return;
        }

        // Vérifier que l'utilisateur a une photo de profil
        if (user.getImage_profil() == null || user.getImage_profil().trim().isEmpty()) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("❌ Aucune photo de profil enregistrée !");
            return;
        }

        // Vérifier que le fichier existe (chemin absolu)
        java.io.File imageFile = new java.io.File(user.getImage_profil());
        if (!imageFile.exists()) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("❌ Photo de profil introuvable : " + user.getImage_profil());
            System.err.println("✗ Fichier introuvable: " + user.getImage_profil());
            return;
        }

        // Ouvrir la fenêtre de webcam
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/WebcamView.fxml"));
            Parent root = loader.load();

            WebcamController webcamController = loader.getController();
            webcamController.setUserImagePath(user.getImage_profil());
            webcamController.setCallback(new WebcamController.WebcamCallback() {
                @Override
                public void onSuccess(String email) {
                    lblMessage.setStyle("-fx-text-fill: green;");
                    lblMessage.setText("✅ Visage reconnu ! Connexion réussie !");

                    // LOG DÉTAILLÉ
                    System.out.println("========================================");
                    System.out.println("🔐 CONNEXION PAR RECONNAISSANCE FACIALE");
                    System.out.println("========================================");
                    System.out.println("ID: " + user.getId_utilisateur());
                    System.out.println("Nom: " + user.getNom());
                    System.out.println("Prénom: " + user.getPrenom());
                    System.out.println("Email: " + user.getEmail());
                    System.out.println("Role: " + user.getRole());
                    System.out.println("========================================");

                    UserSession.getInstance().setCurrentUser(user);

                    // Déterminer le chemin FXML selon le rôle
                    String fxmlPath;
                    if ("ADMIN_RH".equals(user.getRole()) || "ADMIN_TECHNIQUE".equals(user.getRole())) {
                        fxmlPath = "/fxml/admin/AdminView.fxml";
                    } else {
                        fxmlPath = "/OffresView.fxml";
                    }

                    // Lancer l'animation
                    playLogoAnimation(fxmlPath);
                }

                @Override
                public void onFailure(String message) {
                    lblMessage.setStyle("-fx-text-fill: red;");
                    lblMessage.setText("❌ " + message);
                }
            });

            // Créer une nouvelle fenêtre
            Stage webcamStage = new Stage();
            webcamStage.setTitle("Reconnaissance Faciale");
            webcamStage.setScene(new Scene(root));
            webcamStage.setResizable(false);
            webcamStage.setOnCloseRequest(e -> webcamController.cleanup());

            // Démarrer la webcam après affichage
            webcamStage.setOnShown(e -> webcamController.startWebcam());

            webcamStage.show();

        } catch (Exception e) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =====================================================
    // MOT DE PASSE OUBLIÉ
    // =====================================================
    @FXML
    public void goToForgotPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ForgotPasswordView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) tfEmail.getScene().getWindow();
            Scene scene = new Scene(root, 1440, 1024);
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMaximized(false);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            System.err.println("❌ Erreur navigation vers ForgotPasswordView: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
