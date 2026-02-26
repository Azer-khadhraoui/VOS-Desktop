package vos.gestionCandidat.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import vos.gestionCandidat.controllers.utilisateur.ListeCandidaturesUtilisateurController;
import vos.gestionCandidat.controllers.utilisateur.ListePreferencesUtilisateurController;
import vos.gestionCandidat.controllers.utilisateur.MatchingUtilisateurController;

public class MainViewController implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private VBox sidebar;
    @FXML private Label navOffresText, navMatchingsText, navPreferencesText, navCandidaturesText, navForumText, navProfilText;
    @FXML private HBox navOffres, navMatchings, navPreferences, navCandidatures, navForum, navProfil;

    // ID utilisateur actuel (à récupérer de la session)
    private int idUtilisateurCourant = 3;

    // Contrôleurs des pages
    private ListeCandidaturesUtilisateurController listeCandidaturesCtrl;
    private MatchingUtilisateurController matchingCtrl;
    private ListePreferencesUtilisateurController preferencesCtrl;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Charger la première page par défaut
        chargerPage("ListeCandidaturesUtilisateur");
        marquerNavActif(navCandidatures);
    }

    /**
     * Charge une page FXML dans le contentArea
     */
    private void chargerPage(String nomFxml) {
        try {
            String cheminFxml = "/fxml/utilisateur/" + nomFxml + ".fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(cheminFxml));
            Node page = loader.load();

            // Passer le contrôleur principal au contrôleur de la page
            Object pageController = loader.getController();
            if (pageController instanceof ListeCandidaturesUtilisateurController) {
                listeCandidaturesCtrl = (ListeCandidaturesUtilisateurController) pageController;
                listeCandidaturesCtrl.setIdUtilisateurCourant(idUtilisateurCourant);
            } else if (pageController instanceof MatchingUtilisateurController) {
                matchingCtrl = (MatchingUtilisateurController) pageController;
                matchingCtrl.setIdUtilisateurConnecte(idUtilisateurCourant);
            } else if (pageController instanceof ListePreferencesUtilisateurController) {
                preferencesCtrl = (ListePreferencesUtilisateurController) pageController;
                //v1
                //preferencesCtrl.setIdUtilisateurConnecte(idUtilisateurCourant);
            }

            // Vider le contentArea et ajouter la nouvelle page
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
        // Retirer la classe active de tous les boutons
        navOffres.getStyleClass().remove("sidebar-nav-item-active");
        navMatchings.getStyleClass().remove("sidebar-nav-item-active");
        navPreferences.getStyleClass().remove("sidebar-nav-item-active");
        navCandidatures.getStyleClass().remove("sidebar-nav-item-active");
        navForum.getStyleClass().remove("sidebar-nav-item-active");
        navProfil.getStyleClass().remove("sidebar-nav-item-active");

        // Ajouter la classe active au bouton sélectionné
        navButton.getStyleClass().add("sidebar-nav-item-active");
    }

    // ============ ACTIONS NAVIGATION ============

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
    private void navToOffres() {
        // À implémenter selon votre structure
        System.out.println("Navigation vers Offres");
    }

    @FXML
    private void navToForum() {
        // À implémenter
        System.out.println("Navigation vers Forum");
    }

    @FXML
    private void navToProfil() {
        // À implémenter
        System.out.println("Navigation vers Profil");
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
                        new KeyValue(sidebar.prefWidthProperty(), 240)
                )
        );
        timeline.play();
        fadeInLabels();
    }

    private void collapseSidebar() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(300),
                        new KeyValue(sidebar.prefWidthProperty(), 60)
                )
        );
        timeline.play();
        fadeOutLabels();
    }

    private void fadeInLabels() {
        Label[] labels = {navOffresText, navMatchingsText, navPreferencesText, navCandidaturesText, navForumText, navProfilText};
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

    private void fadeOutLabels() {
        Label[] labels = {navOffresText, navMatchingsText, navPreferencesText, navCandidaturesText, navForumText, navProfilText};
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

    /**
     * Getter pour accéder au contrôleur depuis les pages enfants
     */
    public int getIdUtilisateurCourant() {
        return idUtilisateurCourant;
    }

    public void setIdUtilisateurCourant(int id) {
        this.idUtilisateurCourant = id;
    }
}