package controllers;

import java.io.File;
import java.io.FileInputStream;

import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import utilis.UserSession;

public class ServicesController {

    @FXML private Label lblUserName, lblUserRole;
    @FXML private StackPane userAvatarContainer;
    @FXML private Label lblUserAvatar;
    
    // Navigation items
    @FXML private HBox navStatistiques;
    @FXML private HBox navOpportunites;
    @FXML private HBox navServices;
    @FXML private HBox navAdministration;
    @FXML private HBox navDeconnexion;
    
    @FXML private VBox cardCongé, cardDemission;
    @FXML private Button btnCongé, btnDemission;

    private Utilisateur currentUser;
    private boolean isSidebarHovered = false;

    @FXML
    public void initialize() {
        loadCurrentUser();
        setupCardHoverAnimations();
        setupButtonHandlers();
        setupNavigationHandlers();
    }

    private void loadCurrentUser() {
        currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            lblUserName.setText(currentUser.getNom() + " " + currentUser.getPrenom());
            lblUserRole.setText(currentUser.getRole());
            loadUserAvatar(currentUser.getImage_profil());
        }
    }

    private void loadUserAvatar(String imagePath) {
        try {
            if (imagePath == null || imagePath.isEmpty()) {
                lblUserAvatar.setText("👤");
                return;
            }
            
            if (imagePath.contains("/") || imagePath.contains("\\")) {
                File file = new File(imagePath);
                if (file.exists()) {
                    Image image = new Image(new FileInputStream(file), 40, 40, true, true);
                    ImageView imageView = new ImageView(image);
                    imageView.setFitHeight(40);
                    imageView.setFitWidth(40);
                    imageView.setStyle("-fx-border-radius: 20; -fx-clip-to-bounds: true;");
                    userAvatarContainer.getChildren().clear();
                    userAvatarContainer.getChildren().add(imageView);
                    return;
                }
            }
            
            lblUserAvatar.setText("👤");
        } catch (Exception e) {
            lblUserAvatar.setText("👤");
        }
    }

    private void setupCardHoverAnimations() {
        setupCardHover(cardCongé);
        setupCardHover(cardDemission);
    }

    private void setupCardHover(VBox card) {
        card.setOnMouseEntered(event -> {
            javafx.animation.ScaleTransition scale = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(200), card);
            scale.setFromX(1.0);
            scale.setFromY(1.0);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
        });

        card.setOnMouseExited(event -> {
            javafx.animation.ScaleTransition scale = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(200), card);
            scale.setFromX(1.05);
            scale.setFromY(1.05);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });
    }

    private void setupButtonHandlers() {
        btnCongé.setOnAction(event -> goToDemandeConge());
        btnDemission.setOnAction(event -> goToDemission());
    }
    
    private void setupNavigationHandlers() {
        if (navStatistiques != null) {
            navStatistiques.setOnMouseClicked(event -> goToStatistiques());
        }
        if (navOpportunites != null) {
            navOpportunites.setOnMouseClicked(event -> goToOpportunites());
        }
        if (navAdministration != null) {
            navAdministration.setOnMouseClicked(event -> goToAdministration());
        }
        if (navDeconnexion != null) {
            navDeconnexion.setOnMouseClicked(event -> logout());
        }
    }

    // private void setupSidebarHoverAnimation() {
    //     sidebar.setOnMouseEntered(event -> {
    //         isSidebarHovered = true;
    //         javafx.animation.Timeline expandTimeline = new javafx.animation.Timeline(
    //             new javafx.animation.KeyFrame(
    //                 javafx.util.Duration.millis(300),
    //                 new javafx.animation.KeyValue(sidebar.prefWidthProperty(), 250)
    //             )
    //         );
    //         expandTimeline.play();
    //     });

    //     sidebar.setOnMouseExited(event -> {
    //         isSidebarHovered = false;
    //         javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(100));
    //         pause.setOnFinished(e -> {
    //             if (!isSidebarHovered) {
    //                 javafx.animation.Timeline collapseTimeline = new javafx.animation.Timeline(
    //                     new javafx.animation.KeyFrame(
    //                         javafx.util.Duration.millis(300),
    //                         new javafx.animation.KeyValue(sidebar.prefWidthProperty(), 80)
    //                     )
    //                 );
    //                 collapseTimeline.play();
    //             }
    //         });
    //         pause.play();
    //     });
    // }

    // private void setupNavItemsHoverAnimation() {
    //     for (javafx.scene.Node node : navContainer.getChildren()) {
    //         if (node instanceof HBox) {
    //             HBox navItem = (HBox) node;
    //             navItem.setOnMouseEntered(event -> {
    //                 javafx.animation.TranslateTransition translate = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(150), navItem);
    //                 translate.setFromX(0);
    //                 translate.setToX(10);
    //                 translate.play();
    //             });

    //             navItem.setOnMouseExited(event -> {
    //                 javafx.animation.TranslateTransition translate = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(150), navItem);
    //                 translate.setFromX(10);
    //                 translate.setToX(0);
    //                 translate.play();
    //             });
    //         }
    //     }
    // }

    @FXML
    public void goToDemission() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DemissionView.fxml"));
            Parent root = loader.load();
            Scene scene = btnDemission.getScene();
            scene.setRoot(root);
            System.out.println("✓ Navigation vers Demande de Démission");
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Impossible d'ouvrir la demande de démission: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void goToDemandeConge() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DemandeCongeView.fxml"));
            Parent root = loader.load();
            Scene scene = btnCongé.getScene();
            scene.setRoot(root);
            System.out.println("✓ Navigation vers Demande de Congé");
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Impossible d'ouvrir la demande de congé: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void goToStatistiques() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StatistiquesView.fxml"));
            Parent root = loader.load();
            Scene scene = navStatistiques.getScene();
            scene.setRoot(root);
            System.out.println("✓ Navigation vers Statistiques");
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Impossible d'ouvrir les statistiques: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    @FXML
    public void goToOpportunites() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/views/MainView.fxml"));
            Parent root = loader.load();
            Scene scene = navOpportunites.getScene();
            scene.setRoot(root);
            System.out.println("✓ Navigation vers Opportunités");
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Impossible d'ouvrir les opportunités: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    @FXML
    public void goToAdministration() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdministrationView.fxml"));
            Parent root = loader.load();
            Scene scene = navAdministration.getScene();
            scene.setRoot(root);
            System.out.println("✓ Navigation vers Administration");
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Impossible d'ouvrir l'administration: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void logout() {
        try {
            UserSession.getInstance().clearSession();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SigninView.fxml"));
            Parent root = loader.load();
            javafx.stage.Stage stage = (Stage) navDeconnexion.getScene().getWindow();
            Scene newScene = new Scene(root, 1440, 1024);
            stage.setScene(newScene);
            stage.setResizable(false);
            stage.setTitle("Connexion - VOS");
            stage.centerOnScreen();
            System.out.println("✓ Déconnexion réussie");
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur de déconnexion");
            alert.setContentText("Impossible de se déconnecter: " + e.getMessage());
            alert.showAndWait();
        }
    }
}
