package controllers;

import entities.Utilisateur;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.ServiceUtilisateur;
import utilis.UserSession;

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
        Scene scene = tfEmail.getScene();
        Parent oldRoot = scene.getRoot();
        Stage stage = (Stage) tfEmail.getScene().getWindow();
        
        // Créer l'overlay sombre avec le logo
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.95);");
        overlay.setOpacity(0);
        
        // Logo VOS avec slogan
        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/img/VOSwhiteslogan.png")));
        logo.setFitWidth(300);
        logo.setFitHeight(300);
        logo.setPreserveRatio(true);
        logo.setScaleX(0.3);
        logo.setScaleY(0.3);
        logo.setOpacity(0);
        
        // Effet glow sur le logo
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(102, 126, 234, 0.8));
        shadow.setRadius(60);
        shadow.setSpread(0.4);
        Glow glow = new Glow(0.9);
        glow.setInput(shadow);
        logo.setEffect(glow);
        
        // Texte de bienvenue
        Label welcomeLabel = new Label("Bienvenue !");
        welcomeLabel.setStyle("-fx-font-size: 42px; -fx-font-weight: bold; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(102,126,234,0.6), 20, 0, 0, 0);");
        welcomeLabel.setOpacity(0);
        welcomeLabel.setTranslateY(100);
        
        // Conteneur vertical
        VBox content = new VBox(40);
        content.setAlignment(Pos.CENTER);
        content.getChildren().addAll(logo, welcomeLabel);
        
        overlay.getChildren().add(content);
        
        // Ajouter l'overlay à la scène
        StackPane stack = new StackPane(oldRoot, overlay);
        scene.setRoot(stack);
        
        // Animation 1: Fade in de l'overlay
        FadeTransition overlayFadeIn = new FadeTransition(Duration.millis(300), overlay);
        overlayFadeIn.setFromValue(0);
        overlayFadeIn.setToValue(1);
        
        // Animation 2: Logo apparaît avec zoom + rotation
        FadeTransition logoFadeIn = new FadeTransition(Duration.millis(400), logo);
        logoFadeIn.setFromValue(0);
        logoFadeIn.setToValue(1);
        
        ScaleTransition logoZoomIn = new ScaleTransition(Duration.millis(600), logo);
        logoZoomIn.setFromX(0.3);
        logoZoomIn.setFromY(0.3);
        logoZoomIn.setToX(1.2);
        logoZoomIn.setToY(1.2);
        logoZoomIn.setInterpolator(Interpolator.EASE_OUT);
        
        RotateTransition logoRotate = new RotateTransition(Duration.millis(600), logo);
        logoRotate.setFromAngle(-180);
        logoRotate.setToAngle(0);
        logoRotate.setInterpolator(Interpolator.EASE_OUT);
        
        // Animation 3: Logo pulse
        ScaleTransition logoPulse1 = new ScaleTransition(Duration.millis(200), logo);
        logoPulse1.setFromX(1.2);
        logoPulse1.setFromY(1.2);
        logoPulse1.setToX(1.0);
        logoPulse1.setToY(1.0);
        
        ScaleTransition logoPulse2 = new ScaleTransition(Duration.millis(150), logo);
        logoPulse2.setFromX(1.0);
        logoPulse2.setFromY(1.0);
        logoPulse2.setToX(1.15);
        logoPulse2.setToY(1.15);
        
        ScaleTransition logoPulse3 = new ScaleTransition(Duration.millis(150), logo);
        logoPulse3.setFromX(1.15);
        logoPulse3.setFromY(1.15);
        logoPulse3.setToX(1.0);
        logoPulse3.setToY(1.0);
        
        // Animation 4: Texte de bienvenue
        FadeTransition textFadeIn = new FadeTransition(Duration.millis(400), welcomeLabel);
        textFadeIn.setFromValue(0);
        textFadeIn.setToValue(1);
        
        TranslateTransition textSlide = new TranslateTransition(Duration.millis(400), welcomeLabel);
        textSlide.setFromY(100);
        textSlide.setToY(0);
        textSlide.setInterpolator(Interpolator.EASE_OUT);
        
        // Animation 5: Logo zoom out final
        ScaleTransition logoZoomOut = new ScaleTransition(Duration.millis(500), logo);
        logoZoomOut.setFromX(1.0);
        logoZoomOut.setFromY(1.0);
        logoZoomOut.setToX(3.0);
        logoZoomOut.setToY(3.0);
        logoZoomOut.setInterpolator(Interpolator.EASE_IN);
        
        FadeTransition logoFadeOut = new FadeTransition(Duration.millis(400), logo);
        logoFadeOut.setFromValue(1);
        logoFadeOut.setToValue(0);
        
        FadeTransition overlayFadeOut = new FadeTransition(Duration.millis(400), overlay);
        overlayFadeOut.setFromValue(1);
        overlayFadeOut.setToValue(0);
        
        // Séquencer les animations
        ParallelTransition logoAppear = new ParallelTransition(logoFadeIn, logoZoomIn, logoRotate);
        ParallelTransition textAppear = new ParallelTransition(textFadeIn, textSlide);
        SequentialTransition pulse = new SequentialTransition(logoPulse1, logoPulse2, logoPulse3);
        ParallelTransition finalZoom = new ParallelTransition(logoZoomOut, logoFadeOut);
        
        SequentialTransition fullAnimation = new SequentialTransition(
            overlayFadeIn,
            logoAppear,
            pulse,
            new PauseTransition(Duration.millis(200)),
            textAppear,
            new PauseTransition(Duration.millis(500)),
            finalZoom,
            overlayFadeOut
        );
        
        fullAnimation.setOnFinished(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent root = loader.load();
                Scene newScene = new Scene(root, 1440, 768.0);
                
                // Animation de transition finale
                animateSceneTransition(stage, newScene);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        fullAnimation.play();
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
                        fxmlPath = "/AdministrationView.fxml";
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
            stage.show();
            
        } catch (Exception e) {
            System.err.println("❌ Erreur navigation vers ForgotPasswordView: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
