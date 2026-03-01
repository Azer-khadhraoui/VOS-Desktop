package controllers;

import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utilis.UserSession;

public class DashboardController {
    
    // Navigation items
    @FXML private HBox navStatistiques;
    @FXML private HBox navOpportunites;
    @FXML private HBox navServices;
    @FXML private HBox navAdministration;
    @FXML private HBox navDeconnexion;
    
    // Content
    @FXML private TextField searchField;
    @FXML private TableView<?> offreTable;
    @FXML private TableColumn<?, ?> colIdOffre;
    @FXML private TableColumn<?, ?> colTitre;
    @FXML private TableColumn<?, ?> colDescription;
    
    private Utilisateur currentUser;

    @FXML
    public void initialize() {
        loadCurrentUser();
        setupNavigation();
        System.out.println("✅ DashboardController initialisé");
    }

    private void loadCurrentUser() {
        currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            System.out.println("✅ Utilisateur en session : " + currentUser.getNom() + " " + currentUser.getPrenom());
        }
    }

    private void setupNavigation() {
        if (navStatistiques != null) {
            navStatistiques.setOnMouseClicked(event -> goToStatistiques());
        }
        if (navServices != null) {
            navServices.setOnMouseClicked(event -> goToServices());
        }
        if (navAdministration != null) {
            navAdministration.setOnMouseClicked(event -> goToAdministration());
        }
        if (navDeconnexion != null) {
            navDeconnexion.setOnMouseClicked(event -> logout());
        }
    }

    private void goToStatistiques() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StatistiquesView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) navStatistiques.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
            System.out.println("✓ Navigation vers Statistiques");
        } catch (Exception e) {
            System.err.println("❌ Erreur navigation Statistiques : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void goToAdministration() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdministrationView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) navAdministration.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
            System.out.println("✓ Navigation vers Administration");
        } catch (Exception e) {
            System.err.println("❌ Erreur navigation Administration : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void goToServices() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ServicesView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) navServices.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
            System.out.println("✓ Navigation vers Services");
        } catch (Exception e) {
            System.err.println("❌ Erreur navigation Services : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void ajouterOffre() {
        System.out.println("🔧 Fonction ajouterOffre() appelée - À implémenter");
        // TODO: Implémenter l'ajout d'une nouvelle offre
        showInfoAlert("Fonction en cours de développement", "L'ajout d'offres sera bientôt disponible.");
    }

    @FXML
    public void rafraichirOffres() {
        System.out.println("🔄 Rafraîchissement des offres...");
        // TODO: Implémenter le rafraîchissement de la table des offres
        showInfoAlert("Rafraîchissement", "Les offres ont été actualisées.");
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void logout() {
        try {
            UserSession.getInstance().clearSession();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SigninView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) navDeconnexion.getScene().getWindow();
            Scene scene = new Scene(root, 1440, 1024);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.setTitle("Connexion - VOS");
            stage.centerOnScreen();
            System.out.println("✓ Déconnexion réussie");
        } catch (Exception e) {
            System.err.println("❌ Erreur déconnexion : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
