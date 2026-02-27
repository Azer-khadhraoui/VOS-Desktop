package controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import entities.Utilisateur;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import utilis.UserSession;

public class AdminViewController implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private VBox sidebar;
    
    // ✅ TOUS les labels de navigation
    @FXML private Label navStatistiquesText, navUtilisateursText, navOffresText, navServicesText,
                        navEntretiensText, navInformationsText, navCandidaturesText, navParametresText, navProfilText;
    
    // ✅ TOUS les boutons de navigation
    @FXML private HBox btnStatistiques, btnUtilisateurs, btnOffres, btnServices, btnEntretiens, btnInformations,
                       navCandidatures, navParametres, navProfil, logoutBtn;
    
    // Header
    
    // Controllers
    private AdministrationController administrationCtrl;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // ✅ Charger l'utilisateur en session
        

        // ✅ Setup logout button
        logoutBtn.setOnMouseClicked(event -> logout());
        
        // ✅ Charger la première page (Statistiques)
        navToStatistiques();
        
        // ✅ TOUS les event handlers de navigation
        btnStatistiques.setOnMouseClicked(event -> navToStatistiques());
        btnUtilisateurs.setOnMouseClicked(event -> navToUtilisateurs());
        btnOffres.setOnMouseClicked(event -> navToOffres());
        btnServices.setOnMouseClicked(event -> navToServices());
        btnEntretiens.setOnMouseClicked(event -> navToEntretiens());
        btnInformations.setOnMouseClicked(event -> navToInformations());
        navCandidatures.setOnMouseClicked(event -> navToCandidatures());
        navParametres.setOnMouseClicked(event -> navToParametres());
        navProfil.setOnMouseClicked(event -> navToProfil());
    }

    /**
     * Charge une page FXML dans le contentArea
     */
    private void chargerPage(String nomPage, String subtitle) {
        try {
            Node page;
            
            switch(nomPage) {
                // ✅ STATISTIQUES
                case "Statistiques":
                    FXMLLoader loaderStats = new FXMLLoader(getClass().getResource("/StatistiquesView.fxml"));
                    page = loaderStats.load();
                    break;
                    
                // ✅ UTILISATEURS (Administration)
                case "Utilisateurs":
                    FXMLLoader loaderAdmin = new FXMLLoader(getClass().getResource("/AdministrationView.fxml"));
                    page = loaderAdmin.load();
                    administrationCtrl = loaderAdmin.getController();
                    if (administrationCtrl != null) {
                        administrationCtrl.setEmbeddedMode(true);
                    }
                    break;
                    
                // ✅ OFFRES
                case "Offres":
                    FXMLLoader loaderOffres = new FXMLLoader(getClass().getResource("/AdminOffresView.fxml"));
                    page = loaderOffres.load();
                    break;
                    
                // ✅ SERVICES
                case "Services":
                    FXMLLoader loaderServices = new FXMLLoader(getClass().getResource("/ServicesView.fxml"));
                    page = loaderServices.load();
                    break;

                // ✅ ENTRETIENS
                case "Entretiens":
                    FXMLLoader loaderEntretiens = new FXMLLoader(getClass().getResource("/GestionEntretienView.fxml"));
                    page = loaderEntretiens.load();
                    GestionEntretienController entretienCtrl = loaderEntretiens.getController();
                    if (entretienCtrl != null) {
                        entretienCtrl.setEmbeddedMode(true);
                    }
                    break;
                    
                // ⚠️ À IMPLÉMENTER
                case "Informations":
                    System.out.println("⚠️ Page 'Informations' non implémentée");
                    return;
                    
                case "Candidatures":
                    FXMLLoader loaderCandidatures = new FXMLLoader(getClass().getResource("/fxml/admin/ListeCandidaturesAdmin.fxml"));
                    page = loaderCandidatures.load();
                    break;
                    
                case "Paramètres":
                    System.out.println("⚠️ Page 'Paramètres' non implémentée");
                    return;
                    
                case "Profil":
                    FXMLLoader loaderProfil = new FXMLLoader(getClass().getResource("/ProfilView.fxml"));
                    page = loaderProfil.load();
                    break;
                    
                default:
                    System.out.println("⚠️ Page inconnue: " + nomPage);
                    return;
            }

            // ✅ Vider et charger la nouvelle page
            contentArea.getChildren().clear();
            contentArea.getChildren().add(page);
            
            // ✅ Mettre à jour le titre et sous-titre du header
        } catch (IOException e) {
            System.err.println("❌ Erreur chargement page '" + nomPage + "': " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Charge l'avatar de l'utilisateur
     */

    /**
     * ✅ Marque le bouton actif et enlève l'état actif des autres
     */
    private void marquerNavActif(HBox navButton) {
        // Enlever la classe active de tous les boutons
        btnStatistiques.getStyleClass().remove("sidebar-nav-item-active");
        btnUtilisateurs.getStyleClass().remove("sidebar-nav-item-active");
        btnOffres.getStyleClass().remove("sidebar-nav-item-active");
        btnServices.getStyleClass().remove("sidebar-nav-item-active");
        btnEntretiens.getStyleClass().remove("sidebar-nav-item-active");
        btnInformations.getStyleClass().remove("sidebar-nav-item-active");
        navCandidatures.getStyleClass().remove("sidebar-nav-item-active");
        navParametres.getStyleClass().remove("sidebar-nav-item-active");
        navProfil.getStyleClass().remove("sidebar-nav-item-active");

        // Ajouter la classe active au bouton cliqué
        navButton.getStyleClass().add("sidebar-nav-item-active");
    }

    // ============ ACTIONS NAVIGATION ============

    /**
     * ✅ Navigation vers Statistiques
     */
    @FXML
    private void navToStatistiques() {
        chargerPage("Statistiques", "Vue d'ensemble des statistiques");
        marquerNavActif(btnStatistiques);
    }

    /**
     * ✅ Navigation vers Utilisateurs (Administration)
     */
    @FXML
    private void navToUtilisateurs() {
        chargerPage("Utilisateurs", "Gérez vos utilisateurs");
        marquerNavActif(btnUtilisateurs);
    }

    /**
     * ✅ Navigation vers Offres
     */
    @FXML
    private void navToOffres() {
        chargerPage("Offres", "Gérez vos offres d'emploi");
        marquerNavActif(btnOffres);
    }

    /**
     * ✅ Navigation vers Services
     */
    @FXML
    private void navToServices() {
        chargerPage("Services", "Gérez vos services");
        marquerNavActif(btnServices);
    }

    /**
     * ✅ Navigation vers Entretiens
     */
    @FXML
    private void navToEntretiens() {
        chargerPage("Entretiens", "Gérez vos entretiens");
        marquerNavActif(btnEntretiens);
    }

    /**
     * Navigation vers Informations
     */
    @FXML
    private void navToInformations() {
        chargerPage("Informations", "Gérez les informations");
        marquerNavActif(btnInformations);
    }

    /**
     * Navigation vers Candidatures
     */
    @FXML
    private void navToCandidatures() {
        chargerPage("Candidatures", "Gérez les candidatures");
        marquerNavActif(navCandidatures);
    }

    /**
     * Navigation vers Paramètres
     */
    @FXML
    private void navToParametres() {
        chargerPage("Paramètres", "Paramètres du système");
        marquerNavActif(navParametres);
    }

    /**
     * Navigation vers Profil
     */
    @FXML
    private void navToProfil() {
        Utilisateur currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Profil");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez vous reconnecter pour acceder a votre profil.");
            alert.showAndWait();
            return;
        }

        chargerPage("Profil", "Mon profil");
        marquerNavActif(navProfil);
    }

    // ============ ANIMATIONS SIDEBAR ============

    /**
     * ✅ Expand sidebar au survol
     */
    @FXML
    private void onSidebarEntered() {
        expandSidebar();
    }

    /**
     * ✅ Collapse sidebar quand la souris sort
     */
    @FXML
    private void onSidebarExited() {
        collapseSidebar();
    }

    /**
     * Animation d'expansion du sidebar
     */
    private void expandSidebar() {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(300), 
                new KeyValue(sidebar.prefWidthProperty(), 240)
            )
        );
        timeline.play();
        fadeInLabels();
    }

    /**
     * Animation de fermeture du sidebar
     */
    private void collapseSidebar() {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(300), 
                new KeyValue(sidebar.prefWidthProperty(), 60)
            )
        );
        timeline.play();
        fadeOutLabels();
    }

    /**
     * ✅ Fade in tous les labels de navigation
     */
    private void fadeInLabels() {
        // ✅ TOUS les labels inclus
        Label[] labels = {
            navStatistiquesText, navUtilisateursText, navOffresText, navServicesText, 
            navInformationsText, navCandidaturesText, navParametresText, navProfilText
        };
        
        for (Label label : labels) {
            Timeline fade = new Timeline(
                new KeyFrame(Duration.millis(200),
                    new KeyValue(label.opacityProperty(), 1.0),
                    new KeyValue(label.maxWidthProperty(), 150)
                )
            );
            fade.play();
        }
    }

    /**
     * ✅ Fade out tous les labels de navigation
     */
    private void fadeOutLabels() {
        // ✅ TOUS les labels inclus
        Label[] labels = {
            navStatistiquesText, navUtilisateursText, navOffresText, navServicesText, 
            navInformationsText, navCandidaturesText, navParametresText, navProfilText
        };
        
        for (Label label : labels) {
            Timeline fade = new Timeline(
                new KeyFrame(Duration.millis(200),
                    new KeyValue(label.opacityProperty(), 0.0),
                    new KeyValue(label.maxWidthProperty(), 0)
                )
            );
            fade.play();
        }
    }

    // ============ DÉCONNEXION ============
    
    /**
     * ✅ Déconnecte l'utilisateur et retour à la page de connexion
     */
    @FXML
    private void logout() {
        try {
            // Effacer la session
            UserSession.getInstance().clearSession();
            
            // Charger la page de connexion
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SigninView.fxml"));
            javafx.scene.Parent root = loader.load();
            
            // Changer la scène
            javafx.stage.Stage stage = (javafx.stage.Stage) contentArea.getScene().getWindow();
            javafx.scene.Scene newScene = new javafx.scene.Scene(root, 1440, 1024);
            stage.setScene(newScene);
            stage.setResizable(false);
            stage.setTitle("Connexion - VOS");
            stage.centerOnScreen();
            
            System.out.println("✓ Déconnexion réussie");
        } catch (Exception e) {
            System.err.println("❌ Erreur déconnexion: " + e.getMessage());
            e.printStackTrace();
        }
    }
}