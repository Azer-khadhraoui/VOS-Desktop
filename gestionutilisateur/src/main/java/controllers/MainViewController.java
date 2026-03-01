package controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import controllers.utilisateur.ListeCandidaturesUtilisateurController;
import controllers.utilisateur.ListePreferencesUtilisateurController;
import controllers.utilisateur.MatchingUtilisateurController;
import entities.Utilisateur;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import utilis.UserSession;

public class MainViewController implements Initializable {

    @FXML
    private StackPane contentArea;
    @FXML
    private VBox sidebar;
    @FXML
    private Label navOffresText, navMatchingsText, navPreferencesText,
            navCandidaturesText, navForumText, navChatBotText, navProfilText;
    @FXML
    private HBox navOffres, navMatchings, navPreferences, navCandidatures,
            navForum, navChatBot, navProfil, logoutBtn;

    // ✅ CHANGÉ : Pas d'ID hardcodé — récupérer de UserSession
    private int idUtilisateurCourant;

    // Controllers
    private OffresController offresCtrl;
    private ListeCandidaturesUtilisateurController listeCandidaturesCtrl;
    private MatchingUtilisateurController matchingCtrl;
    private ListePreferencesUtilisateurController preferencesCtrl;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // ✅ ÉTAPE 1 : Récupérer l'utilisateur de la session
        Utilisateur userConnecte = UserSession.getInstance().getCurrentUser();

        if (userConnecte != null) {
            idUtilisateurCourant = userConnecte.getId_utilisateur();
            System.out.println("✅ [MainViewController] Utilisateur en session : ID=" + idUtilisateurCourant);
        } else {
            System.out.println("❌ [MainViewController] ERREUR : Aucun utilisateur en session !");
            idUtilisateurCourant = -1;
            return; // Ne pas continuer si pas d'utilisateur
        }

        // Charger la page Offres par défaut
        logoutBtn.setOnMouseClicked(event -> logout());
        chargerPage("Offres");
        marquerNavActif(navOffres);
    }

    /**
     * Charge une page FXML dans le contentArea
     */
    private void chargerPage(String nomFxml) {
        try {
            Node page;
            Object pageController;

            // ✅ OFFRES
            if ("Offres".equals(nomFxml)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/OffresView.fxml"));
                page = loader.load();
                offresCtrl = loader.getController();
                pageController = offresCtrl;
                // L'OffresController récupère directement de UserSession
            } else {
                // ✅ AUTRES PAGES
                String cheminFxml = "/fxml/utilisateur/" + nomFxml + ".fxml";
                FXMLLoader loader = new FXMLLoader(getClass().getResource(cheminFxml));
                page = loader.load();
                pageController = loader.getController();

                // ✅ PASSER L'ID UTILISATEUR À CHAQUE CONTROLLER
                if (pageController instanceof ListeCandidaturesUtilisateurController) {
                    listeCandidaturesCtrl = (ListeCandidaturesUtilisateurController) pageController;
                    listeCandidaturesCtrl.setIdUtilisateurCourant(idUtilisateurCourant);
                    System.out.println("✅ ID passé à ListeCandidatures : " + idUtilisateurCourant);

                } else if (pageController instanceof MatchingUtilisateurController) {
                    matchingCtrl = (MatchingUtilisateurController) pageController;
                    matchingCtrl.setIdUtilisateurConnecte(idUtilisateurCourant);
                    System.out.println("✅ ID passé à Matching : " + idUtilisateurCourant);

                } else if (pageController instanceof ListePreferencesUtilisateurController) {
                    preferencesCtrl = (ListePreferencesUtilisateurController) pageController;
                    // ✅ ListePreferencesUtilisateurController.setIdUtilisateurCourant() si besoin
                }
            }

            // Vider et ajouter la nouvelle page
            contentArea.getChildren().clear();
            contentArea.getChildren().add(page);

        } catch (IOException e) {
            System.err.println("[MainViewController] Erreur chargement page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Marque le bouton de navigation comme actif
     */
    private void marquerNavActif(HBox navButton) {
        navOffres.getStyleClass().remove("sidebar-nav-item-active");
        navMatchings.getStyleClass().remove("sidebar-nav-item-active");
        navPreferences.getStyleClass().remove("sidebar-nav-item-active");
        navCandidatures.getStyleClass().remove("sidebar-nav-item-active");
        navForum.getStyleClass().remove("sidebar-nav-item-active");
        navChatBot.getStyleClass().remove("sidebar-nav-item-active");
        navProfil.getStyleClass().remove("sidebar-nav-item-active");

        // Ajouter la classe active uniquement si navButton n'est pas null
        if (navButton != null) {
            navButton.getStyleClass().add("sidebar-nav-item-active");
        }
    }

    // ============ ACTIONS NAVIGATION ============

    @FXML
    private void navToOffres() {
        chargerPage("Offres");
        marquerNavActif(navOffres);
    }

    @FXML
    private void navToCandidatures() {
        chargerPage("ListeCandidaturesUtilisateur");
        marquerNavActif(navCandidatures);
    }

    @FXML
    private void navToMatching() {
        chargerPage("MatchingUtilisateur");
        marquerNavActif(navMatchings);
    }

    @FXML
    private void navToPreferences() {
        chargerPage("ListePreferencesUtilisateur");
        marquerNavActif(navPreferences);
    }

    @FXML
    private void navToForum() {
        System.out.println("Navigation vers Forum");
    }

    @FXML
    private void navToChatBot() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ChatBotView.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Assistant RH IA - VOS");
            stage.setWidth(900);
            stage.setHeight(700);
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.show();
            marquerNavActif(null);
            System.out.println("✅ ChatBot lancé");
        } catch (IOException e) {
            System.err.println("❌ Erreur chargement ChatBot: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void navToProfil() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProfilView.fxml"));
            Node page = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(page);
            marquerNavActif(navProfil);
            System.out.println("Navigation vers Profil");
        } catch (IOException e) {
            System.err.println("[MainViewController] Erreur chargement profil: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ============ ANIMATIONS SIDEBAR ============

    @FXML
    private void onSidebarEntered() {
        expandSidebar();
    }

    @FXML
    private void onSidebarExited() {
        collapseSidebar();
    }

    private void expandSidebar() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(300),
                        new KeyValue(sidebar.prefWidthProperty(), 180)));
        timeline.play();
        fadeInLabels();
    }

    private void collapseSidebar() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(300),
                        new KeyValue(sidebar.prefWidthProperty(), 50)));
        timeline.play();
        fadeOutLabels();
    }

    private void fadeInLabels() {
        Label[] labels = { navOffresText, navMatchingsText, navPreferencesText,
                navCandidaturesText, navForumText, navProfilText };
        for (Label label : labels) {
            Timeline fade = new Timeline(
                    new KeyFrame(Duration.millis(200),
                            new KeyValue(label.opacityProperty(), 1.0),
                            new KeyValue(label.maxWidthProperty(), 150)));
            fade.play();
        }
    }

    private void fadeOutLabels() {
        Label[] labels = { navOffresText, navMatchingsText, navPreferencesText,
                navCandidaturesText, navForumText, navProfilText };
        for (Label label : labels) {
            Timeline fade = new Timeline(
                    new KeyFrame(Duration.millis(200),
                            new KeyValue(label.opacityProperty(), 0.0),
                            new KeyValue(label.maxWidthProperty(), 0)));
            fade.play();
        }
    }

    // ✅ GETTER (si besoin depuis d'autres classes)
    public int getIdUtilisateurCourant() {
        return idUtilisateurCourant;
    }

    @FXML
    private void logout() {
        try {
            UserSession.getInstance().clearSession();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SigninView.fxml"));
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stage = (javafx.stage.Stage) contentArea.getScene().getWindow();
            javafx.scene.Scene newScene = new javafx.scene.Scene(root, 1440, 1024);
            stage.setScene(newScene);
            stage.setResizable(true);
            stage.setMaximized(false);
            stage.setTitle("Connexion - VOS");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur de déconnexion");
            alert.setContentText("Impossible de se déconnecter: " + e.getMessage());
            alert.showAndWait();
        }
    }
}