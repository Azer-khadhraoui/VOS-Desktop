package controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import entities.Utilisateur;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import utilis.UserSession;

public class AdminViewController implements Initializable {

    // ================================
    // CORE LAYOUT
    // ================================
    @FXML
    private StackPane contentArea;
    @FXML
    private VBox sidebar;

    // ================================
    // SIDEBAR ELEMENTS
    // ================================
    private ImageView sidebarLogo;

    /** Non-clickable "DASHBOARD" header row */
    @FXML
    private HBox navDashboard;

    /** TitledPane groups */
    @FXML
    private TitledPane gestionsPane;
    @FXML
    private TitledPane statsPane;

    /** Section title labels (hidden when collapsed) */
    @FXML
    private Label lblDashboardSection;
    @FXML
    private Label lblGestionsSection;
    @FXML
    private Label lblStatistiquesSection;

    /** TitledPane graphic labels */
    @FXML
    private Label lblGestionsText;
    @FXML
    private Label lblStatsText;

    /** Logout row + its text label */
    @FXML
    private HBox navLogout;
    @FXML
    private Label navLogoutText;

    // ================================
    // GESTIONS submenu items
    // ================================
    @FXML
    private HBox menuOffres;
    @FXML
    private HBox menuEntretiens;
    @FXML
    private HBox menuRecrutement;
    @FXML
    private HBox menuAdministration;
    @FXML
    private HBox menuServices;

    // ================================
    // AUTRES submenu items
    // ================================
    @FXML
    private HBox menuStatistiques;
    @FXML
    private HBox menuStatsCandidats;
    @FXML
    private HBox menuStatsEntretiens;
    @FXML
    private HBox menuStatsOffres;
    @FXML
    private HBox menuStatsRecrutements;
    @FXML
    private HBox menuStatsUtilisateurs;
    @FXML
    private HBox menuCandidatures;
    @FXML
    private HBox menuProfil;

    // ================================
    // STATE
    // ================================
    private boolean sidebarExpanded = false;
    private Timeline sidebarAnimation = null;

    // Reuse between calls to avoid recreating controllers
    private AdministrationController administrationCtrl;

    // ================================
    // INIT
    // ================================
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Hide section/label text initially (collapsed state)
        initializeSidebar();

        // Set Admin Name from session or default
        Utilisateur currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            System.out.println("Admin connected: " + currentUser.getNom() + " " + currentUser.getPrenom());
        }

        // Wire all navigation handlers
        setupNavigation();

        // Load default page
        navToStatistiques();
    }

    // ================================
    // SIDEBAR INIT
    // ================================
    private void initializeSidebar() {
        if (sidebar == null)
            return;

        // Hide section title labels
        Label[] sectionLabels = { lblGestionsSection, lblStatistiquesSection };
        for (Label lbl : sectionLabels) {
            if (lbl != null) {
                lbl.setVisible(false);
                lbl.setManaged(false);
            }
        }

        // Hide nav labels
        Label[] navLabels = { navLogoutText, lblGestionsText, lblStatsText, lblDashboardSection };
        for (Label lbl : navLabels) {
            if (lbl != null) {
                lbl.setVisible(false);
                lbl.setManaged(false);
            }
        }

        // Hide all sidebar-nav-label children (submenu labels inside TitledPanes)
        sidebar.lookupAll(".sidebar-nav-label").forEach(node -> {
            if (node instanceof Label) {
                node.setVisible(false);
                node.setManaged(false);
            }
        });

        // Collapse TitledPanes initially
        if (gestionsPane != null)
            gestionsPane.setExpanded(false);
        if (statsPane != null)
            statsPane.setExpanded(false);

        // Make navDashboard non-clickable (it's a section header, not a nav link)
        if (navDashboard != null) {
            navDashboard.setStyle("-fx-cursor: default; -fx-opacity: 0.8;");
            navDashboard.setOnMouseClicked(null);
        }
    }

    // ================================
    // NAVIGATION WIRING
    // ================================
    private void setupNavigation() {
        // Logout
        if (navLogout != null)
            navLogout.setOnMouseClicked(e -> logout());

        // ── GESTIONS submenu ──
        if (menuOffres != null)
            menuOffres.setOnMouseClicked(e -> navToOffres());
        if (menuEntretiens != null)
            menuEntretiens.setOnMouseClicked(e -> navToEntretiens());
        if (menuRecrutement != null)
            menuRecrutement.setOnMouseClicked(e -> navToRecrutement());
        if (menuAdministration != null)
            menuAdministration.setOnMouseClicked(e -> navToUtilisateurs());
        if (menuServices != null)
            menuServices.setOnMouseClicked(e -> navToServices());

        // ── AUTRES submenu ──
        if (menuStatistiques != null)
            menuStatistiques.setOnMouseClicked(e -> navToStatistiques());
        if (menuStatsCandidats != null)
            menuStatsCandidats.setOnMouseClicked(e -> navToCandidatures());
        if (menuStatsEntretiens != null)
            menuStatsEntretiens.setOnMouseClicked(e -> navToEntretiens());
        if (menuStatsOffres != null)
            menuStatsOffres.setOnMouseClicked(e -> navToOffres());
        if (menuStatsRecrutements != null)
            menuStatsRecrutements.setOnMouseClicked(e -> navToRecrutement());
        if (menuStatsUtilisateurs != null)
            menuStatsUtilisateurs.setOnMouseClicked(e -> navToStatistiques());
        if (menuCandidatures != null)
            menuCandidatures.setOnMouseClicked(e -> navToCandidatures());
        if (menuProfil != null)
            menuProfil.setOnMouseClicked(e -> navToProfil());
    }

    // ================================
    // PAGE LOADER
    // ================================
    private void chargerPage(String nomPage) {
        try {
            Node page;

            switch (nomPage) {
                case "Statistiques":
                    FXMLLoader loaderStats = new FXMLLoader(getClass().getResource("/StatistiquesView.fxml"));
                    page = loaderStats.load();
                    break;

                case "Utilisateurs":
                    FXMLLoader loaderAdmin = new FXMLLoader(getClass().getResource("/AdministrationView.fxml"));
                    page = loaderAdmin.load();
                    administrationCtrl = loaderAdmin.getController();
                    if (administrationCtrl != null)
                        administrationCtrl.setEmbeddedMode(true);
                    break;

                case "Offres":
                    FXMLLoader loaderOffres = new FXMLLoader(getClass().getResource("/AdminOffresView.fxml"));
                    page = loaderOffres.load();
                    break;

                case "Services":
                    FXMLLoader loaderServices = new FXMLLoader(getClass().getResource("/ServicesView.fxml"));
                    page = loaderServices.load();
                    break;

                case "Entretiens":
                    FXMLLoader loaderEntretiens = new FXMLLoader(getClass().getResource("/GestionEntretienView.fxml"));
                    page = loaderEntretiens.load();
                    GestionEntretienController entretienCtrl = loaderEntretiens.getController();
                    if (entretienCtrl != null)
                        entretienCtrl.setEmbeddedMode(true);
                    break;

                case "Recrutement":
                    // Load the gestionRecrutement MainView embedded
                    FXMLLoader loaderRec = new FXMLLoader(
                            getClass().getResource("/fxml/admin/RecrutementAdminView.fxml"));
                    page = loaderRec.load();
                    break;

                case "Candidatures":
                    FXMLLoader loaderCandidatures = new FXMLLoader(
                            getClass().getResource("/fxml/admin/ListeCandidaturesAdmin.fxml"));
                    page = loaderCandidatures.load();
                    break;

                case "Profil":
                    FXMLLoader loaderProfil = new FXMLLoader(getClass().getResource("/ProfilView.fxml"));
                    page = loaderProfil.load();
                    break;

                default:
                    System.out.println("⚠️ Page inconnue: " + nomPage);
                    return;
            }

            contentArea.getChildren().clear();
            contentArea.getChildren().add(page);

        } catch (IOException e) {
            System.err.println("❌ Erreur chargement page '" + nomPage + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ================================
    // NAVIGATION ACTIONS
    // ================================

    @FXML
    private void navToStatistiques() {
        chargerPage("Statistiques");
        marquerNavActif(menuStatistiques);
    }

    @FXML
    private void navToUtilisateurs() {
        chargerPage("Utilisateurs");
        marquerNavActif(menuAdministration);
    }

    @FXML
    private void navToOffres() {
        chargerPage("Offres");
        marquerNavActif(menuOffres);
    }

    @FXML
    private void navToServices() {
        chargerPage("Services");
        marquerNavActif(menuServices);
    }

    @FXML
    private void navToEntretiens() {
        chargerPage("Entretiens");
        marquerNavActif(menuEntretiens);
    }

    @FXML
    private void navToRecrutement() {
        chargerPage("Recrutement");
        marquerNavActif(menuRecrutement);
    }

    @FXML
    private void navToCandidatures() {
        chargerPage("Candidatures");
        marquerNavActif(menuCandidatures);
    }

    @FXML
    private void navToProfil() {
        Utilisateur currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Profil");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez vous reconnecter pour accéder à votre profil.");
            alert.showAndWait();
            return;
        }
        chargerPage("Profil");
        marquerNavActif(menuProfil);
    }

    // ================================
    // ACTIVE STATE
    // ================================

    /** All submenu HBoxes that can be marked active */
    private final HBox[] ALL_NAV_ITEMS = {
            // Populated lazily in marquerNavActif() because @FXML fields are only
            // injected after initialize() — we use a helper method instead.
    };

    private void marquerNavActif(HBox active) {
        HBox[] items = {
                menuOffres, menuEntretiens, menuRecrutement, menuAdministration, menuServices,
                menuStatistiques, menuStatsCandidats, menuStatsEntretiens, menuStatsOffres,
                menuStatsRecrutements, menuStatsUtilisateurs, menuCandidatures, menuProfil
        };
        for (HBox item : items) {
            if (item != null) {
                item.getStyleClass().remove("sidebar-submenu-item-active");
                item.getStyleClass().remove("sidebar-nav-item-active");
            }
        }
        if (active != null && !active.getStyleClass().contains("sidebar-submenu-item-active")) {
            active.getStyleClass().add("sidebar-submenu-item-active");
        }
    }

    // ================================
    // SIDEBAR ANIMATION (hover expand/collapse)
    // ================================

    @FXML
    private void onSidebarEntered() {
        if (!sidebarExpanded) {
            expandSidebar();
            // Change logo to slogan version on hover
            if (sidebarLogo != null) {
                URL imgUrl = getClass().getResource("/img/VOSwhiteslogan.png");
                if (imgUrl == null)
                    imgUrl = getClass().getResource("/images/VOSwhiteslogan.png");
                if (imgUrl != null) {
                    sidebarLogo.setImage(new Image(imgUrl.toExternalForm()));
                }
                ScaleTransition scaleUp = new ScaleTransition(Duration.millis(300), sidebarLogo);
                scaleUp.setFromX(1.0);
                scaleUp.setFromY(1.0);
                scaleUp.setToX(1.4);
                scaleUp.setToY(1.4);
                scaleUp.setInterpolator(Interpolator.EASE_OUT);
                scaleUp.play();
            }
        }
    }

    @FXML
    private void onSidebarExited() {
        collapseSidebar();
        // Restore plain logo
        if (sidebarLogo != null) {
            URL imgUrl = getClass().getResource("/images/VOSwhite.png");
            if (imgUrl != null) {
                sidebarLogo.setImage(new Image(imgUrl.toExternalForm()));
            }
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(300), sidebarLogo);
            scaleDown.setFromX(1.4);
            scaleDown.setFromY(1.4);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            scaleDown.setInterpolator(Interpolator.EASE_IN);
            scaleDown.play();
        }
    }

    private void expandSidebar() {
        sidebarExpanded = true;

        if (sidebarAnimation != null)
            sidebarAnimation.stop();

        // Animate width 60 → 240
        sidebarAnimation = new Timeline(
                new KeyFrame(Duration.millis(300),
                        new KeyValue(sidebar.prefWidthProperty(), 240.0, Interpolator.EASE_OUT),
                        new KeyValue(sidebar.minWidthProperty(), 240.0, Interpolator.EASE_OUT)));
        sidebarAnimation.play();

        // Show section titles
        Label[] sectionLabels = { lblGestionsSection, lblStatistiquesSection };
        for (Label lbl : sectionLabels) {
            if (lbl != null) {
                lbl.setVisible(true);
                lbl.setManaged(true);
                fadeIn(lbl, 50);
            }
        }

        // Show nav labels (pane headers + logout)
        Label[] navLabels = { navLogoutText, lblGestionsText, lblStatsText, lblDashboardSection };
        for (Label lbl : navLabels) {
            if (lbl != null) {
                lbl.setVisible(true);
                lbl.setManaged(true);
                fadeIn(lbl, 50);
            }
        }

        // Show all sidebar-nav-label (submenu labels via CSS class lookup)
        if (sidebar != null) {
            sidebar.lookupAll(".sidebar-nav-label").forEach(node -> {
                if (node instanceof Label) {
                    node.setVisible(true);
                    node.setManaged(true);
                    fadeIn((Label) node, 50);
                }
            });
        }
    }

    private void collapseSidebar() {
        sidebarExpanded = false;

        if (sidebarAnimation != null)
            sidebarAnimation.stop();

        // Collapse TitledPanes
        if (gestionsPane != null)
            gestionsPane.setExpanded(false);
        if (statsPane != null)
            statsPane.setExpanded(false);

        // Hide section titles
        Label[] sectionLabels = { lblGestionsSection, lblStatistiquesSection };
        for (Label lbl : sectionLabels) {
            if (lbl != null)
                fadeOut(lbl);
        }

        // Hide nav labels
        Label[] navLabels = { navLogoutText, lblGestionsText, lblStatsText, lblDashboardSection };
        for (Label lbl : navLabels) {
            if (lbl != null)
                fadeOut(lbl);
        }

        // Hide all sidebar-nav-label labels
        if (sidebar != null) {
            sidebar.lookupAll(".sidebar-nav-label").forEach(node -> {
                if (node instanceof Label)
                    fadeOut((Label) node);
            });
        }

        // Animate width 240 → 60
        sidebarAnimation = new Timeline(
                new KeyFrame(Duration.millis(300),
                        new KeyValue(sidebar.prefWidthProperty(), 60.0, Interpolator.EASE_IN),
                        new KeyValue(sidebar.minWidthProperty(), 60.0, Interpolator.EASE_IN)));
        sidebarAnimation.play();
    }

    // ================================
    // ANIMATION HELPERS
    // ================================

    private void fadeIn(Label label, double delayMs) {
        FadeTransition ft = new FadeTransition(Duration.millis(250), label);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.setDelay(Duration.millis(delayMs));
        ft.play();
    }

    private void fadeOut(Label label) {
        FadeTransition ft = new FadeTransition(Duration.millis(150), label);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> {
            label.setVisible(false);
            label.setManaged(false);
        });
        ft.play();
    }

    // ================================
    // LOGOUT
    // ================================

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

            System.out.println("✓ Déconnexion réussie");
        } catch (Exception e) {
            System.err.println("❌ Erreur déconnexion: " + e.getMessage());
            e.printStackTrace();
        }
    }
}