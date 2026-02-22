package controllers;

import entities.Contrat;
import entities.Recrutement;
import entities.RecrutementTableRow;
import entities.RecrutementGroup;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.*;
import services.EmailService;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.geometry.Pos;

import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.Interpolator;
import javafx.util.Duration;
import services.ServiceContrat;
import services.ServiceRecrutement;
import services.PDFService;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
// OpenPDF imports — explicit to avoid TextField clash with javafx.scene.control.TextField
import com.lowagie.text.Chunk;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import javafx.stage.FileChooser;

public class MainController implements Initializable {

    // ================================
    // SIDEBAR ELEMENTS
    // ================================
    @FXML
    private VBox sidebar;

    @FXML
    private ImageView sidebarLogo;

    @FXML
    private HBox navDashboard;
    @FXML
    private Label navDashboardText;

    @FXML
    private TitledPane gestionsPane;

    @FXML
    private TitledPane statsPane;

    @FXML
    private HBox menuOffres;
    @FXML
    private HBox menuEntretiens;
    @FXML
    private HBox menuRecrutement;
    @FXML
    private HBox menuAdministration;
    @FXML
    private HBox menuCandidats;
    @FXML
    private HBox menuStatEntretiens;
    @FXML
    private HBox menuStatOffres;
    @FXML
    private HBox menuStatRecrutements;
    @FXML
    private HBox menuUtilisateurs;

    // Section title labels
    @FXML
    private Label lblDashboardSection;
    @FXML
    private Label lblGestionsSection;
    @FXML
    private Label lblStatistiquesSection;

    @FXML
    private HBox navLogout;
    @FXML
    private Label navLogoutText;

    // ================================
    // NAVIGATION BUTTONS (OLD - DEPRECATED)
    // ================================
    @FXML
    private Button btnNavEntretiens;
    @FXML
    private Button btnNavDashboard;
    @FXML
    private Button btnNavStats;

    // Header Elements
    @FXML
    private TextField searchField;
    @FXML
    private TabPane recrutementTabPane;

    // Content Area
    @FXML
    private StackPane contentArea;

    // Pages
    @FXML
    private VBox pageRecrutement;
    @FXML
    private VBox pageDashboard;
    @FXML
    private VBox pageOffres;
    @FXML
    private VBox pageEntretiens;
    @FXML
    private VBox pageAdministration;
    @FXML
    private VBox pageCandidats;
    @FXML
    private VBox pageStatEntretiens;
    @FXML
    private VBox pageStatOffres;
    @FXML
    private ScrollPane pageStatRecrutements;
    @FXML
    private VBox pageUtilisateurs;

    // Statistics - KPI Cards
    @FXML
    private Label valTotalRecrutements;
    @FXML
    private Label valTauxAcceptation;
    @FXML
    private Label valEntretiensMoyens;

    @FXML
    private Label valEnAttenteCount;
    @FXML
    private Label valTotalContrats;
    @FXML
    private Label valSalaireMoyen;
    @FXML
    private Label valVolumeMoyen;
    @FXML
    private Label valContratsActifs;

    // Statistics - Charts
    @FXML
    private PieChart chartDecisions;
    @FXML
    private BarChart<String, Number> chartRecruteur;
    @FXML
    private LineChart<String, Number> chartRecrutementMois;
    @FXML
    private PieChart chartTypeContrat;
    @FXML
    private BarChart<String, Number> chartSalaireType;
    @FXML
    private PieChart chartStatutContrat;

    private boolean statsInitialized = false;

    // Contrats Table
    @FXML
    private TableView<ContratRow> tableContrats;
    @FXML
    private ComboBox<String> cbTypeContratFilter;
    @FXML
    private TableColumn<ContratRow, Integer> colIdContrat;
    @FXML
    private TableColumn<ContratRow, String> colTypeContrat;
    @FXML
    private TableColumn<ContratRow, String> colDateDebut;
    @FXML
    private TableColumn<ContratRow, String> colStatus;
    @FXML
    private TableColumn<ContratRow, String> colVolumeHoraire;
    @FXML
    private TableColumn<ContratRow, String> colAvantages;
    @FXML
    private TableColumn<ContratRow, Double> colSalaire;
    @FXML
    private TableColumn<ContratRow, Integer> colIdRecrutementContrat;
    @FXML
    private TableColumn<ContratRow, Void> colActionsContrat;

    // Recrutements Table
    @FXML
    private TableView<RecrutementTableRow> tableRecrutements;
    @FXML
    private TableColumn<RecrutementTableRow, String> colUserName;
    @FXML
    private TableColumn<RecrutementTableRow, Integer> colRecrutementCount;
    @FXML
    private TableColumn<RecrutementTableRow, String> colDecisionDate;
    @FXML
    private TableColumn<RecrutementTableRow, String> colDecisionFinale;
    @FXML
    private TableColumn<RecrutementTableRow, Integer> colIdEntretien;
    @FXML
    private TableColumn<RecrutementTableRow, Void> colActions;

    private ObservableList<ContratRow> contratData = FXCollections.observableArrayList();
    private ObservableList<ContratRow> allContratData = FXCollections.observableArrayList();
    private ObservableList<RecrutementTableRow> recrutementData = FXCollections.observableArrayList();
    private List<RecrutementGroup> recrutementGroups;
    private Map<RecrutementGroup, Integer> groupHeaderIndexMap = new HashMap<>();

    // Services
    private ServiceContrat serviceContrat = new ServiceContrat();
    private ServiceRecrutement serviceRecrutement = new ServiceRecrutement();

    // Modal states
    private ContratRow selectedContratRow = null;

    // Sidebar state
    private boolean sidebarExpanded = false;
    private Timeline sidebarAnimation = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize tables with data from database
        initializeContratTable();
        initializeRecrutementTable();

        // Initialize sidebar animations
        initializeSidebar();

        // Setup navigation click handlers
        setupNavigation();

        // Setup dynamic search for recrutement by name
        setupRecrutementSearch();

        // Setup tab visibility for search bar
        setupTabVisibility();

        // Initialize stats if needed
        if (pageStatRecrutements.isVisible()) {
            initializeStatistiques();
        }
    }

    private void initializeSidebar() {
        if (sidebar == null)
            return;

        // Hide section titles initially (collapsed state)
        Label[] sectionLabels = { lblDashboardSection, lblGestionsSection, lblStatistiquesSection };
        for (Label label : sectionLabels) {
            if (label != null) {
                label.setVisible(false);
                label.setManaged(false);
            }
        }

        // Hide nav item labels initially
        Label[] labels = { navDashboardText, navLogoutText };
        for (Label label : labels) {
            if (label != null) {
                label.setVisible(false);
                label.setManaged(false);
            }
        }

        // Hide all submenu labels (sidebar-nav-label)
        sidebar.lookupAll(".sidebar-nav-label").forEach(node -> {
            if (node instanceof Label) {
                node.setVisible(false);
                node.setManaged(false);
            }
        });

        // Hide TitledPane titles initially (only show icons when collapsed)
        if (gestionsPane != null)
            gestionsPane.setText("");
        if (statsPane != null)
            statsPane.setText("");
    }

    private void setupNavigation() {
        // Dashboard
        if (navDashboard != null) {
            navDashboard.setOnMouseClicked(e -> showPageDashboard());
        }

        // Logout
        if (navLogout != null) {
            navLogout.setOnMouseClicked(e -> handleLogout());
        }

        // Gestions submenu items
        if (menuRecrutement != null) {
            menuRecrutement.setOnMouseClicked(e -> showPageRecrutement());
        }
        if (menuEntretiens != null) {
            menuEntretiens.setOnMouseClicked(e -> showPageEntretiens());
        }
        if (menuOffres != null) {
            menuOffres.setOnMouseClicked(e -> showPageOffres());
        }
        if (menuAdministration != null) {
            menuAdministration.setOnMouseClicked(e -> showPageAdministration());
        }

        // Statistiques submenu items
        if (menuCandidats != null) {
            menuCandidats.setOnMouseClicked(e -> showPageCandidats());
        }
        if (menuStatEntretiens != null) {
            menuStatEntretiens.setOnMouseClicked(e -> showPageStatEntretiens());
        }
        if (menuStatOffres != null) {
            menuStatOffres.setOnMouseClicked(e -> showPageStatOffres());
        }
        if (menuStatRecrutements != null) {
            menuStatRecrutements.setOnMouseClicked(e -> showPageStatRecrutements());
        }
        if (menuUtilisateurs != null) {
            menuUtilisateurs.setOnMouseClicked(e -> showPageUtilisateurs());
        }
    }

    @FXML
    private void onSidebarEntered() {
        if (!sidebarExpanded) {
            expandSidebar();
            // Change logo to slogan version
            if (sidebarLogo != null) {
                sidebarLogo.setImage(new Image(getClass().getResource("/images/VOSwhiteslogan.png").toExternalForm()));

                // Scale animation: grow the logo
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
        // Change logo back to simple version
        if (sidebarLogo != null) {
            sidebarLogo.setImage(new Image(getClass().getResource("/images/VOSwhite.png").toExternalForm()));

            // Scale animation: shrink the logo
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

        // Stop any running animation
        if (sidebarAnimation != null) {
            sidebarAnimation.stop();
        }

        // Animate width expansion: 60px -> 240px
        sidebarAnimation = new Timeline(
                new KeyFrame(Duration.millis(300),
                        new KeyValue(sidebar.prefWidthProperty(), 240.0, Interpolator.EASE_OUT),
                        new KeyValue(sidebar.minWidthProperty(), 240.0, Interpolator.EASE_OUT)));
        sidebarAnimation.play();

        // Show section titles
        Label[] sectionLabels = { lblDashboardSection, lblGestionsSection, lblStatistiquesSection };
        for (Label label : sectionLabels) {
            if (label != null) {
                label.setVisible(true);
                label.setManaged(true);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(250), label);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.setDelay(Duration.millis(50));
                fadeIn.play();
            }
        }

        // Fade in nav item labels
        Label[] labels = { navDashboardText, navLogoutText };
        for (Label label : labels) {
            if (label != null) {
                label.setVisible(true);
                label.setManaged(true);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(250), label);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.setDelay(Duration.millis(50));
                fadeIn.play();
            }
        }

        // Show all sidebar-nav-label (submenu labels) across all nodes
        if (sidebar != null) {
            sidebar.lookupAll(".sidebar-nav-label").forEach(node -> {
                if (node instanceof Label) {
                    node.setVisible(true);
                    node.setManaged(true);
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(250), (Label) node);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.setDelay(Duration.millis(50));
                    fadeIn.play();
                }
            });
        }

        // Show TitledPane titles
        if (gestionsPane != null)
            gestionsPane.setText("Gestions");
        if (statsPane != null)
            statsPane.setText("Statistiques");
    }

    private void collapseSidebar() {
        sidebarExpanded = false;

        // Stop any running animation
        if (sidebarAnimation != null) {
            sidebarAnimation.stop();
        }

        // Close TitledPane menus
        if (gestionsPane != null) {
            gestionsPane.setExpanded(false);
        }
        if (statsPane != null) {
            statsPane.setExpanded(false);
        }

        // Hide section titles
        Label[] sectionLabels = { lblDashboardSection, lblGestionsSection, lblStatistiquesSection };
        for (Label label : sectionLabels) {
            if (label != null) {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(150), label);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> {
                    label.setVisible(false);
                    label.setManaged(false);
                });
                fadeOut.play();
            }
        }

        // Fade out nav item labels
        Label[] labels = { navDashboardText, navLogoutText };
        for (Label label : labels) {
            if (label != null) {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(150), label);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> {
                    label.setVisible(false);
                    label.setManaged(false);
                });
                fadeOut.play();
            }
        }

        // Hide all sidebar-nav-label (submenu labels) across all nodes
        if (sidebar != null) {
            sidebar.lookupAll(".sidebar-nav-label").forEach(node -> {
                if (node instanceof Label) {
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(150), (Label) node);
                    fadeOut.setFromValue(1.0);
                    fadeOut.setToValue(0.0);
                    fadeOut.setOnFinished(e -> {
                        node.setVisible(false);
                        node.setManaged(false);
                    });
                    fadeOut.play();
                }
            });
        }

        // Hide TitledPane titles (show only icons)
        if (gestionsPane != null)
            gestionsPane.setText("");
        if (statsPane != null)
            statsPane.setText("");

        // Animate width collapse: 240px -> 60px
        sidebarAnimation = new Timeline(
                new KeyFrame(Duration.millis(300),
                        new KeyValue(sidebar.prefWidthProperty(), 60.0, Interpolator.EASE_IN),
                        new KeyValue(sidebar.minWidthProperty(), 60.0, Interpolator.EASE_IN)));
        sidebarAnimation.play();
    }

    private void setNavItemActive(HBox activeItem) {
        // Remove active class from all items
        if (navDashboard != null)
            navDashboard.getStyleClass().remove("sidebar-nav-item-active");

        // Add active class to current item
        if (activeItem != null && !activeItem.getStyleClass().contains("sidebar-nav-item-active")) {
            activeItem.getStyleClass().add("sidebar-nav-item-active");
        }
    }

    private void initializeContratTable() {
        // Add icons and text to column headers
        colIdContrat.setText("# ID");
        colTypeContrat.setText("📋 Type");
        colDateDebut.setText("📅 Période");
        colStatus.setText("📊 Statut");
        colVolumeHoraire.setText("⏱️ Volume");
        colAvantages.setText("🎁 Avantages");
        colSalaire.setText("💰 Salaire");
        colIdRecrutementContrat.setText("👤 Recrutement");
        colActionsContrat.setText("⚙️ Actions");

        colIdContrat.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        colTypeContrat.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        colDateDebut.setCellValueFactory(cellData -> cellData.getValue().periodeProperty());
        colStatus.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        colVolumeHoraire.setCellValueFactory(cellData -> cellData.getValue().volumeHoraireProperty());
        colAvantages.setCellValueFactory(cellData -> cellData.getValue().avantagesProperty());
        colSalaire.setCellValueFactory(cellData -> cellData.getValue().salaireProperty().asObject());
        colIdRecrutementContrat.setCellValueFactory(cellData -> cellData.getValue().idRecrutementProperty().asObject());

        // Style Type column with badge icons
        colTypeContrat.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(value);
                    String icon = "";
                    String bgColor = "#E0E7FF";
                    String textColor = "#4F46E5";

                    switch (value.toLowerCase()) {
                        case "cdi":
                            icon = "💼 ";
                            bgColor = "#DBEAFE";
                            textColor = "#0369A1";
                            break;
                        case "cdd":
                            icon = "📝 ";
                            bgColor = "#FEF3C7";
                            textColor = "#92400E";
                            break;
                        case "stage":
                            icon = "🎓 ";
                            bgColor = "#D1FAE5";
                            textColor = "#065F46";
                            break;
                        case "freelance":
                            icon = "🚀 ";
                            bgColor = "#FCD34D";
                            textColor = "#78350F";
                            break;
                        case "alternance":
                            icon = "🔄 ";
                            bgColor = "#F3E8FF";
                            textColor = "#6B21A8";
                            break;
                    }

                    badge.setText(icon + value);
                    badge.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: " + textColor + "; " +
                            "-fx-padding: 6 12; -fx-background-radius: 20; -fx-font-weight: 600; -fx-font-size: 12px;");
                    setGraphic(badge);
                }
            }
        });

        // Style Période column with dates info
        colDateDebut.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setGraphic(null);
                } else {
                    ContratRow row = getTableView().getItems().get(getIndex());
                    VBox container = new VBox();
                    container.setSpacing(4);
                    container.setStyle("-fx-padding: 5;");

                    // Période (gros et bold)
                    Label periode = new Label(value);
                    periode.setStyle("-fx-font-weight: 600; -fx-font-size: 12px; -fx-text-fill: #1F2937;");

                    // Dates (petit et gris)
                    String dateDebut = row.dateDebutProperty().get();
                    String dateFin = row.dateFinProperty().get();
                    Label dates = new Label(dateDebut + " → " + dateFin);
                    dates.setStyle("-fx-font-size: 10px; -fx-text-fill: #9CA3AF; -fx-font-style: italic;");

                    container.getChildren().addAll(periode, dates);
                    setGraphic(container);
                }
            }
        });

        // Style Status column with colored badges
        colStatus.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(value);
                    String bgColor = "#F3F4F6";
                    String textColor = "#111827";
                    String icon = "";

                    switch (value.toLowerCase()) {
                        case "actif":
                            icon = "✓ ";
                            bgColor = "#D1FAE5";
                            textColor = "#065F46";
                            break;
                        case "en attente":
                            icon = "⏳ ";
                            bgColor = "#FCD34D";
                            textColor = "#92400E";
                            break;
                        case "terminé":
                            icon = "✔ ";
                            bgColor = "#E5E7EB";
                            textColor = "#6B7280";
                            break;
                    }

                    badge.setText(icon + value);
                    badge.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: " + textColor + "; " +
                            "-fx-padding: 6 12; -fx-background-radius: 15; -fx-font-weight: 600; -fx-font-size: 11px;");
                    setGraphic(badge);
                }
            }
        });

        // Style Volume Horaire column
        colVolumeHoraire.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText("⏱️ " + value);
                    setStyle("-fx-text-fill: #4F46E5; -fx-font-weight: 500;");
                }
            }
        });

        // Style Avantages column with colored badges
        colAvantages.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(value);
                    String bgColor = "#F3F4F6";
                    String textColor = "#6B7280";
                    String icon = "🎁 ";

                    switch (value.toLowerCase()) {
                        case "aucun":
                            icon = "✖ ";
                            bgColor = "#E5E7EB";
                            textColor = "#6B7280";
                            break;
                        case "tickets restaurant":
                            icon = "🍽️ ";
                            bgColor = "#FECACA";
                            textColor = "#991B1B";
                            break;
                        case "assurance maladie":
                            icon = "🏥 ";
                            bgColor = "#BFDBFE";
                            textColor = "#1E40AF";
                            break;
                        case "transport":
                            icon = "🚗 ";
                            bgColor = "#BBF7D0";
                            textColor = "#065F46";
                            break;
                    }

                    badge.setText(icon + value);
                    badge.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: " + textColor + "; " +
                            "-fx-padding: 5 10; -fx-background-radius: 12; -fx-font-weight: 500; -fx-font-size: 10px;");
                    setGraphic(badge);
                }
            }
        });

        // Style Salaire column with currency formatting
        colSalaire.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(String.format("💰 %.2f DT", value));
                    setStyle("-fx-text-fill: #10B981; -fx-font-weight: 600;");
                }
            }
        });

        // Set row height for better spacing
        tableContrats.setFixedCellSize(75);
        tableContrats.setStyle("-fx-fixed-cell-size: 75px;");

        // Configure Actions column with styled buttons
        colActionsContrat.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button();
            private final Button btnDelete = new Button();
            private final Button btnPreview = new Button();
            private final Button btnDownload = new Button();
            private final HBox hbox = new HBox(6, btnEdit, btnDelete, btnPreview, btnDownload);

            {
                // ── Edit / Delete icons ──────────────────────────────────
                ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/Edit.png")));
                editIcon.setFitHeight(16);
                editIcon.setFitWidth(16);
                editIcon.setPreserveRatio(true);

                ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/delete.png")));
                deleteIcon.setFitHeight(16);
                deleteIcon.setFitWidth(16);
                deleteIcon.setPreserveRatio(true);

                // ── View/Preview icon ───────────────────────────────────
                ImageView viewIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/view.png")));
                viewIcon.setFitHeight(18);
                viewIcon.setFitWidth(18);
                viewIcon.setPreserveRatio(true);

                // ── PDF Download icon ───────────────────────────────────
                ImageView pdfIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/pdf.png")));
                pdfIcon.setFitHeight(18);
                pdfIcon.setFitWidth(18);
                pdfIcon.setPreserveRatio(true);

                btnEdit.setGraphic(editIcon);
                btnDelete.setGraphic(deleteIcon);
                btnPreview.setGraphic(viewIcon);
                btnDownload.setGraphic(pdfIcon);

                String baseStyle = "-fx-padding: 4 4; -fx-cursor: hand; " +
                        "-fx-background-color: transparent; -fx-border-color: transparent; -fx-border-width: 0;";
                btnEdit.setStyle(baseStyle);
                btnDelete.setStyle(baseStyle);
                btnPreview.setStyle(baseStyle);
                btnDownload.setStyle(baseStyle);

                // ── Tooltips ─────────────────────────────────────────────
                Tooltip.install(btnEdit, new Tooltip("Modifier le contrat"));
                Tooltip.install(btnDelete, new Tooltip("Supprimer le contrat"));
                Tooltip.install(btnPreview, new Tooltip("Aperçu du contrat"));
                Tooltip.install(btnDownload, new Tooltip("Télécharger en PDF"));

                // ── Container ────────────────────────────────────────────
                hbox.setStyle("-fx-background-color: rgba(150,171,241,0.52); " +
                        "-fx-padding: 4 6; -fx-border-radius: 4; -fx-background-radius: 4;");
                hbox.setAlignment(Pos.CENTER);

                // ── Actions ──────────────────────────────────────────────
                btnEdit.setOnAction(e -> {
                    ContratRow c = getTableView().getItems().get(getIndex());
                    editContrat(c);
                });
                btnDelete.setOnAction(e -> {
                    ContratRow c = getTableView().getItems().get(getIndex());
                    deleteContrat(c);
                });
                btnPreview.setOnAction(e -> {
                    ContratRow c = getTableView().getItems().get(getIndex());
                    showContratPreview(c);
                });
                btnDownload.setOnAction(e -> {
                    ContratRow c = getTableView().getItems().get(getIndex());
                    downloadContratPdf(c);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableView().getItems().isEmpty() || getIndex() < 0
                        || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }

                ContratRow currentRow = getTableView().getItems().get(getIndex());
                String status = currentRow.statusProperty().get();

                // Update hbox children based on status
                hbox.getChildren().clear();
                hbox.getChildren().add(btnEdit);
                hbox.getChildren().add(btnDelete);

                // Only show preview and download buttons if status is "Actif"
                if (status.equals("Actif")) {
                    hbox.getChildren().add(btnPreview);
                    hbox.getChildren().add(btnDownload);
                }

                setGraphic(hbox);
            }
        });

        // Add row styling with hover effect
        tableContrats.setRowFactory(param -> new TableRow<>() {
            @Override
            protected void updateItem(ContratRow item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    setStyle("-fx-padding: 8px; -fx-background-radius: 4;");
                    setOnMouseEntered(e -> setStyle(
                            "-fx-padding: 8px; -fx-background-radius: 4; -fx-background-color: #F9FAFB;"));
                    setOnMouseExited(e -> setStyle("-fx-padding: 8px; -fx-background-radius: 4;"));
                }
            }
        });

        // Add sample data
        loadSampleContratData();
        tableContrats.setItems(contratData);

        // Initialize type filter
        setupContratTypeFilter();

        // Handle empty table
        if (contratData.isEmpty()) {
            tableContrats.setPlaceholder(createEmptyPlaceholder("Aucun contrat trouvé", "📋"));
        }
    }

    private void initializeRecrutementTable() {
        // ===== USER NAME COLUMN (Name + Count + Expansion Button) =====
        colUserName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUserName()));
        colUserName.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }

                RecrutementTableRow row = getTableRow().getItem();
                if (!row.isGroupHeader()) {
                    // Detail row - show indentation
                    Label detailLabel = new Label("  └─ Détail");
                    detailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
                    setGraphic(detailLabel);
                    return;
                }

                // Group header row
                HBox hbox = new HBox(12);
                hbox.setAlignment(Pos.CENTER_LEFT);

                // Expand/Collapse button
                Button expandBtn = new Button(row.isExpanded() ? "▼" : "▶");
                expandBtn.setStyle("-fx-padding: 4 8; -fx-font-size: 12px; -fx-cursor: hand; " +
                        "-fx-background-color: #F3F4F6; -fx-border-color: #E5E7EB; -fx-border-width: 1;");
                expandBtn.setPrefWidth(35);

                expandBtn.setOnAction(event -> {
                    // Toggle the GROUP's expanded state (not the row's separate property)
                    RecrutementGroup grp = row.getParentGroup();
                    grp.setExpanded(!grp.isExpanded());
                    expandBtn.setText(grp.isExpanded() ? "▼" : "▶");
                    refreshTableWithGroupState(grp);
                });

                // User icon and name
                Label userIcon = new Label("👤");
                userIcon.setStyle("-fx-font-size: 16px;");
                Label userName = new Label(item + " (#" + row.getUserId() + ")");
                userName.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #111827;");

                hbox.getChildren().addAll(expandBtn, userIcon, userName);
                setGraphic(hbox);

                // Style the row as a header
                getTableRow().setStyle("-fx-background-color: #F9FAFB; -fx-font-weight: 600;");
            }
        });

        // ===== RECRUTEMENT COUNT COLUMN =====
        colRecrutementCount.setCellValueFactory(
                data -> new SimpleIntegerProperty(data.getValue().getRecrutementCount()).asObject());
        colRecrutementCount.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }

                RecrutementTableRow row = getTableRow().getItem();
                if (!row.isGroupHeader() || item == null) {
                    setGraphic(null);
                    return;
                }

                HBox hbox = new HBox(8);
                hbox.setAlignment(Pos.CENTER_LEFT);
                Label icon = new Label("📊");
                icon.setStyle("-fx-font-size: 14px;");
                Label count = new Label(item + " recrutement" + (item > 1 ? "s" : ""));
                count.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151; -fx-padding: 4 8; " +
                        "-fx-background-color: #E5E7EB; -fx-background-radius: 4;");
                hbox.getChildren().addAll(icon, count);
                setGraphic(hbox);
            }
        });

        // ===== DECISION DATE COLUMN =====
        colDecisionDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDecisionDate()));
        colDecisionDate.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null || item == null
                        || item.isEmpty()) {
                    setGraphic(null);
                    return;
                }

                RecrutementTableRow row = getTableRow().getItem();
                if (row.isGroupHeader()) {
                    setGraphic(null);
                    return;
                }

                HBox hbox = new HBox(8);
                hbox.setAlignment(Pos.CENTER_LEFT);
                Label badge = new Label("📅 " + item);
                badge.setStyle("-fx-background-color: #FCD34D; -fx-text-fill: #78350F; " +
                        "-fx-padding: 4 8; -fx-background-radius: 4; -fx-font-size: 11px;");
                hbox.getChildren().add(badge);
                setGraphic(hbox);
            }
        });

        // ===== DECISION FINALE COLUMN =====
        colDecisionFinale.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDecision()));
        colDecisionFinale.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null || item == null
                        || item.isEmpty()) {
                    setGraphic(null);
                    return;
                }

                RecrutementTableRow row = getTableRow().getItem();
                if (row.isGroupHeader()) {
                    setGraphic(null);
                    return;
                }

                Label badge = new Label(item);

                switch (item.toLowerCase()) {
                    case "accepté":
                        badge.setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #059669; " +
                                "-fx-padding: 4 8; -fx-background-radius: 4; -fx-font-weight: 600; -fx-font-size: 11px;");
                        break;
                    case "refusé":
                        badge.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; " +
                                "-fx-padding: 4 8; -fx-background-radius: 4; -fx-font-weight: 600; -fx-font-size: 11px;");
                        break;
                    case "en attente":
                        badge.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #92400E; " +
                                "-fx-padding: 4 8; -fx-background-radius: 4; -fx-font-weight: 600; -fx-font-size: 11px;");
                        break;
                    default:
                        badge.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #6B7280; " +
                                "-fx-padding: 4 8; -fx-background-radius: 4; -fx-font-weight: 600; -fx-font-size: 11px;");
                }

                setGraphic(badge);
            }
        });

        // ===== INTERVIEW ID COLUMN =====
        colIdEntretien
                .setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getInterviewId()).asObject());
        colIdEntretien.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null || item == null || item == 0) {
                    setGraphic(null);
                    return;
                }

                RecrutementTableRow row = getTableRow().getItem();
                if (row.isGroupHeader()) {
                    setGraphic(null);
                    return;
                }

                HBox hbox = new HBox(8);
                hbox.setAlignment(Pos.CENTER_LEFT);
                Label badge = new Label("🔗 " + item);
                badge.setStyle("-fx-background-color: #BFDBFE; -fx-text-fill: #1E40AF; " +
                        "-fx-padding: 4 8; -fx-background-radius: 4; -fx-font-size: 11px;");
                hbox.getChildren().add(badge);
                setGraphic(hbox);
            }
        });

        // ===== ACTIONS COLUMN =====
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("✏️");
            private final Button btnDelete = new Button("🗑️");

            {
                btnEdit.getStyleClass().add("btn-action");
                btnDelete.getStyleClass().add("btn-delete");

                btnEdit.setOnAction(event -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        RecrutementTableRow row = getTableRow().getItem();
                        if (!row.isGroupHeader()) {
                            Recrutement rec = row.getParentGroup().getRecrutements().stream()
                                    .filter(r -> r.getId_recrutement() == row.getRecruitmentId())
                                    .findFirst()
                                    .orElse(null);
                            if (rec != null) {
                                editRecrutement(rec);
                            }
                        }
                    }
                });

                btnDelete.setOnAction(event -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        RecrutementTableRow row = getTableRow().getItem();
                        if (!row.isGroupHeader()) {
                            Recrutement rec = row.getParentGroup().getRecrutements().stream()
                                    .filter(r -> r.getId_recrutement() == row.getRecruitmentId())
                                    .findFirst()
                                    .orElse(null);
                            if (rec != null) {
                                deleteRecrutement(rec);
                            }
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }

                RecrutementTableRow row = getTableRow().getItem();
                if (row.isGroupHeader()) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(8, btnEdit, btnDelete);
                    hbox.setAlignment(Pos.CENTER);
                    setGraphic(hbox);
                }
            }
        });

        // Set row styling
        tableRecrutements.setRowFactory(param -> new TableRow<>() {
            @Override
            protected void updateItem(RecrutementTableRow item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                    return;
                }

                if (item.isGroupHeader()) {
                    setStyle("-fx-background-color: #F9FAFB; -fx-font-weight: 600; -fx-padding: 8;");
                } else {
                    setStyle("-fx-background-color: #FFFFFF; -fx-padding: 4;");
                }
            }
        });

        // Add sample data
        loadSampleRecrutementData();

        // Handle empty table
        if (recrutementData.isEmpty()) {
            tableRecrutements.setPlaceholder(createEmptyPlaceholder("Aucun recrutement trouvé", "📋"));
        }
    }

    private void refreshTableWithGroupState(RecrutementGroup changedGroup) {
        // Scan the live list for the header's CURRENT index (the stored map
        // goes stale after rows are inserted/removed above this group).
        int headerIndex = -1;
        for (int i = 0; i < recrutementData.size(); i++) {
            RecrutementTableRow r = recrutementData.get(i);
            if (r.isGroupHeader() && r.getParentGroup() == changedGroup) {
                headerIndex = i;
                break;
            }
        }
        if (headerIndex < 0)
            return; // group not found — nothing to do

        if (changedGroup.isExpanded()) {
            // Insert detail rows directly after the header
            int insertPosition = headerIndex + 1;
            for (Recrutement recrutement : changedGroup.getRecrutements()) {
                RecrutementTableRow detailRow = new RecrutementTableRow(changedGroup, recrutement);
                recrutementData.add(insertPosition, detailRow);
                insertPosition++;
            }
        } else {
            // Remove all detail rows that belong to this group
            recrutementData.removeIf(r -> !r.isGroupHeader() && r.getParentGroup() == changedGroup);
        }

        tableRecrutements.refresh();
    }

    private VBox createEmptyPlaceholder(String message, String icon) {
        VBox placeholder = new VBox();
        placeholder.setStyle("-fx-alignment: CENTER; -fx-spacing: 15; -fx-padding: 40;");

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 48px;");

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B7280; -fx-font-weight: 500;");

        placeholder.getChildren().addAll(iconLabel, messageLabel);
        return placeholder;
    }

    private void loadSampleContratData() {
        try {
            contratData.clear();
            allContratData.clear();
            for (Contrat c : serviceContrat.afficher()) {
                String dateFinStr = (c.getDate_fin() != null) ? c.getDate_fin().toString() : "";
                String periode = calculatePeriode(c.getDate_debut().toString(), dateFinStr);

                // Calculate auto status based on dates
                String autoStatus = "En attente";
                if (c.getDate_fin() != null) {
                    java.time.LocalDate dateDebut = c.getDate_debut().toLocalDate();
                    java.time.LocalDate dateFin = c.getDate_fin().toLocalDate();
                    autoStatus = calculateAutoStatus(dateDebut, dateFin);
                }

                ContratRow row = new ContratRow(c.getId_contrat(), c.getType_contrat(), c.getDate_debut().toString(),
                        dateFinStr, c.getSalaire(), autoStatus, c.getVolume_horaire(), c.getAvantages(),
                        c.getId_recrutement(), periode);
                contratData.add(row);
                allContratData.add(row);
            }
        } catch (SQLException e) {
            showError("Erreur lors du chargement des contrats: " + e.getMessage());
        }
    }

    /** Setup contract type filter dropdown */
    private void setupContratTypeFilter() {
        if (cbTypeContratFilter == null) {
            return;
        }

        // Get distinct contract types from data
        Set<String> typesSet = new HashSet<>();
        typesSet.add("Tous");
        for (ContratRow row : allContratData) {
            typesSet.add(row.typeProperty().get());
        }

        ObservableList<String> types = FXCollections.observableArrayList(typesSet);
        types.sort(null);
        cbTypeContratFilter.setItems(types);
        cbTypeContratFilter.setValue("Tous");

        // Add listener for filter changes
        cbTypeContratFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                filterContratByType(newVal);
            }
        });
    }

    /** Filter contracts by selected type */
    private void filterContratByType(String selectedType) {
        if (selectedType.equals("Tous")) {
            contratData.setAll(allContratData);
        } else {
            List<ContratRow> filtered = allContratData.stream()
                    .filter(row -> row.typeProperty().get().equals(selectedType))
                    .collect(java.util.stream.Collectors.toList());
            contratData.setAll(filtered);
        }
    }

    private void loadSampleRecrutementData() {
        try {
            recrutementData.clear();
            groupHeaderIndexMap.clear();
            recrutementGroups = serviceRecrutement.afficherGroupedByUser();

            int index = 0;
            for (RecrutementGroup group : recrutementGroups) {
                // Add group header
                RecrutementTableRow headerRow = new RecrutementTableRow(group);
                recrutementData.add(headerRow);
                groupHeaderIndexMap.put(group, index);
                index++;
            }

            tableRecrutements.setItems(recrutementData);
        } catch (SQLException e) {
            showError("Erreur lors du chargement des recrutements: " + e.getMessage());
        }
    }

    /** Dynamic search for recrutement by name */
    private void setupRecrutementSearch() {
        if (searchField == null) {
            return;
        }

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterRecrutementByName(newValue);
        });
    }

    /** Filter recrutement table by user name */
    private void filterRecrutementByName(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            // Show all data
            loadSampleRecrutementData();
            return;
        }

        try {
            ObservableList<RecrutementTableRow> filteredData = FXCollections.observableArrayList();
            String searchLower = searchText.toLowerCase().trim();
            Map<RecrutementGroup, Integer> newHeaderIndexMap = new HashMap<>();

            int index = 0;
            for (RecrutementGroup group : recrutementGroups) {
                String groupNameLower = group.getUserName().toLowerCase();

                // Check if group name matches
                if (groupNameLower.contains(searchLower)) {
                    // Add group header
                    RecrutementTableRow headerRow = new RecrutementTableRow(group);
                    filteredData.add(headerRow);
                    newHeaderIndexMap.put(group, index);
                    index++;

                    // If group is expanded, add all its detail rows
                    if (group.isExpanded()) {
                        for (Recrutement recrutement : group.getRecrutements()) {
                            RecrutementTableRow detailRow = new RecrutementTableRow(group, recrutement);
                            filteredData.add(detailRow);
                            index++;
                        }
                    }
                }
            }

            groupHeaderIndexMap = newHeaderIndexMap;
            tableRecrutements.setItems(filteredData);

        } catch (Exception e) {
            showError("Erreur lors de la recherche: " + e.getMessage());
        }
    }

    /** Setup tab visibility for search bar - only show for Recrutement tab */
    private void setupTabVisibility() {
        if (recrutementTabPane == null || searchField == null) {
            return;
        }

        // Initially hide search field
        searchField.setVisible(false);
        searchField.setManaged(false);

        // Listen for tab selection changes
        recrutementTabPane.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            // Show search only for Recrutement tab (index 0)
            boolean isRecrutementTab = newVal.intValue() == 0;
            searchField.setVisible(isRecrutementTab);
            searchField.setManaged(isRecrutementTab);

            // Clear search when switching to Contrat tab
            if (!isRecrutementTab) {
                searchField.clear();
                loadSampleRecrutementData();
            }
        });
    }

    private String calculatePeriode(String dateDebutStr, String dateFinStr) {
        if (dateDebutStr == null || dateDebutStr.isEmpty() || dateFinStr == null || dateFinStr.isEmpty()) {
            return "";
        }

        try {
            java.time.LocalDate dateDebut = java.time.LocalDate.parse(dateDebutStr);
            java.time.LocalDate dateFin = java.time.LocalDate.parse(dateFinStr);

            long months = java.time.temporal.ChronoUnit.MONTHS.between(dateDebut, dateFin);

            if (months < 12) {
                return months + " mois";
            } else if (months % 12 == 0) {
                long years = months / 12;
                return years + " an" + (years > 1 ? "s" : "");
            } else {
                long years = months / 12;
                long remainingMonths = months % 12;
                return years + " an" + (years > 1 ? "s" : "") + " et " + remainingMonths + " mois";
            }
        } catch (Exception e) {
            return "";
        }
    }

    private String calculateAutoStatus(java.time.LocalDate dateDebut, java.time.LocalDate dateFin) {
        if (dateDebut == null || dateFin == null) {
            return "En attente";
        }

        java.time.LocalDate today = java.time.LocalDate.now();

        // Si date_fin < aujourd'hui, alors statut = "Terminé"
        if (dateFin.isBefore(today)) {
            return "Terminé";
        }

        // Si date_debut <= aujourd'hui et aujourd'hui < date_fin, alors statut =
        // "Actif"
        if (!dateDebut.isAfter(today) && dateFin.isAfter(today)) {
            return "Actif";
        }

        // Sinon statut = "En attente"
        return "En attente";
    }

    private void hideAllPages() {
        if (pageRecrutement != null) {
            pageRecrutement.setVisible(false);
            pageRecrutement.setManaged(false);
        }
        if (pageDashboard != null) {
            pageDashboard.setVisible(false);
            pageDashboard.setManaged(false);
        }
        if (pageOffres != null) {
            pageOffres.setVisible(false);
            pageOffres.setManaged(false);
        }
        if (pageEntretiens != null) {
            pageEntretiens.setVisible(false);
            pageEntretiens.setManaged(false);
        }
        if (pageAdministration != null) {
            pageAdministration.setVisible(false);
            pageAdministration.setManaged(false);
        }
        if (pageCandidats != null) {
            pageCandidats.setVisible(false);
            pageCandidats.setManaged(false);
        }
        if (pageStatEntretiens != null) {
            pageStatEntretiens.setVisible(false);
            pageStatEntretiens.setManaged(false);
        }
        if (pageStatOffres != null) {
            pageStatOffres.setVisible(false);
            pageStatOffres.setManaged(false);
        }
        if (pageStatRecrutements != null) {
            pageStatRecrutements.setVisible(false);
            pageStatRecrutements.setManaged(false);
        }
        if (pageUtilisateurs != null) {
            pageUtilisateurs.setVisible(false);
            pageUtilisateurs.setManaged(false);
        }
    }

    private void showPageDashboard() {
        hideAllPages();
        if (pageDashboard != null) {
            pageDashboard.setVisible(true);
            pageDashboard.setManaged(true);
        }
        setNavItemActive(navDashboard);
    }

    private void showPageRecrutement() {
        hideAllPages();
        if (pageRecrutement != null) {
            pageRecrutement.setVisible(true);
            pageRecrutement.setManaged(true);
        }
    }

    private void showPageOffres() {
        hideAllPages();
        if (pageOffres != null) {
            pageOffres.setVisible(true);
            pageOffres.setManaged(true);
        }
    }

    private void showPageEntretiens() {
        hideAllPages();
        if (pageEntretiens != null) {
            pageEntretiens.setVisible(true);
            pageEntretiens.setManaged(true);
        }
    }

    private void showPageAdministration() {
        hideAllPages();
        if (pageAdministration != null) {
            pageAdministration.setVisible(true);
            pageAdministration.setManaged(true);
        }
    }

    private void showPageCandidats() {
        hideAllPages();
        if (pageCandidats != null) {
            pageCandidats.setVisible(true);
            pageCandidats.setManaged(true);
        }
    }

    private void showPageStatEntretiens() {
        hideAllPages();
        if (pageStatEntretiens != null) {
            pageStatEntretiens.setVisible(true);
            pageStatEntretiens.setManaged(true);
        }
    }

    private void showPageStatOffres() {
        hideAllPages();
        if (pageStatOffres != null) {
            pageStatOffres.setVisible(true);
            pageStatOffres.setManaged(true);
        }
    }

    private void showPageStatRecrutements() {
        hideAllPages();
        if (pageStatRecrutements != null) {
            pageStatRecrutements.setVisible(true);
            pageStatRecrutements.setManaged(true);

            // Proactively initialize stats on first view
            if (!statsInitialized) {
                initializeStatistiques();
            }

            // Apply animations every time it is shown
            applyDashboardAnimations();
        }
    }

    private void applyDashboardAnimations() {
        if (pageStatRecrutements == null)
            return;

        // Collect all animatable parents (cards and chart containers)
        List<Node> kpiCards = new ArrayList<>();
        List<Node> chartContainers = new ArrayList<>();

        // Traverse VBox -> ScrollPane content to find nodes
        if (pageStatRecrutements.getContent() instanceof VBox) {
            VBox mainContainer = (VBox) pageStatRecrutements.getContent();
            for (Node section : mainContainer.getChildren()) {
                if (section instanceof VBox) {
                    VBox sectionVBox = (VBox) section;
                    for (Node child : sectionVBox.getChildren()) {
                        if (child instanceof GridPane) {
                            kpiCards.addAll(((GridPane) child).getChildren());
                        } else if (child instanceof HBox) {
                            chartContainers.addAll(((HBox) child).getChildren());
                        } else if (child instanceof VBox && child.getStyleClass().contains("chart-container")) {
                            chartContainers.add(child);
                        }
                    }
                }
            }
        }

        double delay = 0;

        // Animate KPI Cards: Staggered Fade & Slide Up
        for (Node card : kpiCards) {
            card.setOpacity(0);
            card.setTranslateY(20);

            FadeTransition fade = new FadeTransition(Duration.millis(500), card);
            fade.setToValue(1);

            TranslateTransition slide = new TranslateTransition(Duration.millis(500), card);
            slide.setToY(0);

            SequentialTransition seq = new SequentialTransition(new PauseTransition(Duration.millis(delay)), fade,
                    slide);
            // Optimization: Run them in parallel after initial pause
            ParallelTransition parallel = new ParallelTransition(fade, slide);
            SequentialTransition finalSeq = new SequentialTransition(new PauseTransition(Duration.millis(delay)),
                    parallel);
            finalSeq.play();

            delay += 100;
        }

        // Animate Charts: Staggered Fade & Scale
        for (Node chartContainer : chartContainers) {
            chartContainer.setOpacity(0);
            chartContainer.setScaleX(0.95);
            chartContainer.setScaleY(0.95);

            FadeTransition fade = new FadeTransition(Duration.millis(600), chartContainer);
            fade.setToValue(1);

            ScaleTransition scale = new ScaleTransition(Duration.millis(600), chartContainer);
            scale.setToX(1);
            scale.setToY(1);

            SequentialTransition finalSeq = new SequentialTransition(new PauseTransition(Duration.millis(delay)),
                    new ParallelTransition(fade, scale));
            finalSeq.play();

            delay += 200;
        }
    }

    private void initializeStatistiques() {
        try {
            // --- Recrutement Stats ---
            // KPI Data (Mock)
            valTotalRecrutements.setText("7");
            valTauxAcceptation.setText("43%");
            valEntretiensMoyens.setText("2.4");
            valEnAttenteCount.setText("2");

            // Decision Distribution Chart
            chartDecisions.setData(FXCollections.observableArrayList(
                    new PieChart.Data("Accepté", 3),
                    new PieChart.Data("Refusé", 2),
                    new PieChart.Data("En attente", 2)));

            // Recruitments by User (Mock Data based on screenshot)
            XYChart.Series<String, Number> recSeries = new XYChart.Series<>();
            recSeries.setName("Recrutements");
            recSeries.getData().add(new XYChart.Data<>("Ben Ali", 3));
            recSeries.getData().add(new XYChart.Data<>("Trabelsi", 2));
            recSeries.getData().add(new XYChart.Data<>("Khadraoui", 2));
            chartRecruteur.getData().setAll(recSeries);

            // Recruitments per Month
            XYChart.Series<String, Number> monthSeries = new XYChart.Series<>();
            monthSeries.setName("2024");
            monthSeries.getData().add(new XYChart.Data<>("Jan", 1));
            monthSeries.getData().add(new XYChart.Data<>("Feb", 3));
            monthSeries.getData().add(new XYChart.Data<>("Mar", 2));
            monthSeries.getData().add(new XYChart.Data<>("Apr", 1));
            chartRecrutementMois.getData().setAll(monthSeries);

            // --- Contrat Stats ---
            // KPI Data (Mock)
            valTotalContrats.setText("3");
            valSalaireMoyen.setText("3850 DT");
            valVolumeMoyen.setText("38h");
            valContratsActifs.setText("2");

            // Contract Type Distribution
            chartTypeContrat.setData(FXCollections.observableArrayList(
                    new PieChart.Data("CDI", 2),
                    new PieChart.Data("CDD", 1)));

            // Salaries by Type
            XYChart.Series<String, Number> salSeries = new XYChart.Series<>();
            salSeries.setName("Salaire Moyen");
            salSeries.getData().add(new XYChart.Data<>("CDI", 4200));
            salSeries.getData().add(new XYChart.Data<>("CDD", 3100));
            chartSalaireType.getData().setAll(salSeries);

            // Contract Status
            chartStatutContrat.setData(FXCollections.observableArrayList(
                    new PieChart.Data("Actif", 2),
                    new PieChart.Data("Expiré", 1)));

            statsInitialized = true;
        } catch (Exception e) {
            System.err.println("Error initializing statistics: " + e.getMessage());
        }
    }

    private void showPageUtilisateurs() {
        hideAllPages();
        if (pageUtilisateurs != null) {
            pageUtilisateurs.setVisible(true);
            pageUtilisateurs.setManaged(true);
        }
    }

    @FXML
    private void onMenuOffresClicked() {
        showPageOffres();
    }

    @FXML
    private void onMenuEntretiensClicked() {
        showPageEntretiens();
    }

    @FXML
    private void onMenuRecrutementClicked() {
        showPageRecrutement();
    }

    @FXML
    private void onMenuAdministrationClicked() {
        showPageAdministration();
    }

    @FXML
    private void onMenuCandidatsClicked() {
        showPageCandidats();
    }

    @FXML
    private void onMenuStatEntretiensClicked() {
        showPageStatEntretiens();
    }

    @FXML
    private void onMenuStatOffresClicked() {
        showPageStatOffres();
    }

    @FXML
    private void onMenuStatRecrutementsClicked() {
        showPageStatRecrutements();
    }

    @FXML
    private void onMenuUtilisateursClicked() {
        showPageUtilisateurs();
    }

    @FXML
    private void handleLogout() {
        // Placeholder for logout logic
        System.out.println("Logout clicked");
        // TODO: Implement logout functionality
    }

    @FXML
    private void ajouterContrat() {
        // Create custom modal dialog
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Ajouter un Contrat");

        // Modal Content
        VBox modalContent = new VBox();
        modalContent.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #1a1a2e, #16213e); " +
                "-fx-background-radius: 20; -fx-padding: 30;");
        modalContent.setSpacing(15);

        // Header
        Label title = new Label("Nouveau Contrat");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label("Créer un nouveau contrat");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #9CA3AF;");

        VBox header = new VBox(title, subtitle);
        header.setStyle("-fx-spacing: 5;");

        // Form Fields
        VBox formFields = new VBox();
        formFields.setSpacing(12);

        // Type
        Label lblType = new Label("TYPE DE CONTRAT");
        lblType.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.setItems(FXCollections.observableArrayList("CDI", "CDD", "Stage", "Freelance", "Alternance"));
        typeCombo.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                "-fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; " +
                "-fx-font-size: 13px; -fx-text-fill: white; -fx-control-inner-background: rgba(26,26,46,0.9);");
        typeCombo.setPrefWidth(300);
        typeCombo.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
                setStyle(empty ? "" : "-fx-text-fill: white; -fx-background-color: rgba(26,26,46,0.95);");
            }
        });
        typeCombo.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
                setStyle("-fx-text-fill: white;");
            }
        });

        Label typeError = new Label("");
        typeError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0;");

        typeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                typeError.setText("✓ Format valide");
                typeError.setStyle(
                        "-fx-font-size: 10px; -fx-text-fill: #10B981; -fx-padding: 2 0; -fx-font-weight: bold;");
                typeCombo.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(16,185,129,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            } else {
                typeError.setText("Ce champ est obligatoire");
                typeError.setStyle(
                        "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                typeCombo.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            }
        });

        VBox typeBox = new VBox(lblType, typeCombo, typeError);
        typeBox.setSpacing(3);

        // Date Début
        Label lblDate = new Label("DATE DÉBUT");
        lblDate.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");

        DatePicker dateDebut = new DatePicker();
        dateDebut.setValue(java.time.LocalDate.now());
        dateDebut.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                "-fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; " +
                "-fx-font-size: 13px; -fx-text-fill: white;");
        dateDebut.setPrefWidth(300);
        dateDebut.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(java.time.LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(java.time.LocalDate.now()));
            }
        });

        Label dateError = new Label("");
        dateError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0;");

        dateDebut.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                if (newVal.isBefore(java.time.LocalDate.now())) {
                    dateError.setText("La date doit être aujourd'hui ou après");
                    dateError.setStyle(
                            "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    dateDebut.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                            "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                            "-fx-font-size: 13px; -fx-text-fill: white;");
                } else {
                    dateError.setText("✓ Format valide");
                    dateError.setStyle(
                            "-fx-font-size: 10px; -fx-text-fill: #10B981; -fx-padding: 2 0; -fx-font-weight: bold;");
                    dateDebut.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                            "-fx-border-color: rgba(16,185,129,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                            "-fx-font-size: 13px; -fx-text-fill: white;");
                }
            } else {
                dateError.setText("Ce champ est obligatoire");
                dateError.setStyle(
                        "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                dateDebut.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            }
        });

        VBox dateBox = new VBox(lblDate, dateDebut, dateError);
        dateBox.setSpacing(3);

        // Salaire
        Label lblSalaire = new Label("SALAIRE");
        lblSalaire
                .setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");

        TextField salaire = new TextField();
        salaire.setPromptText("Entrez le salaire");
        salaire.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                "-fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; " +
                "-fx-font-size: 13px; -fx-text-fill: white; -fx-prompt-text-fill: #6B7280;");
        salaire.setPrefWidth(300);

        Label salaireError = new Label("");
        salaireError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0;");

        salaire.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                salaireError.setText("Ce champ est obligatoire");
                salaireError.setStyle(
                        "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                salaire.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white; -fx-prompt-text-fill: #6B7280;");
            } else {
                try {
                    double val = Double.parseDouble(newVal);
                    if (val <= 0) {
                        salaireError.setText("Le salaire doit être positif");
                        salaireError.setStyle(
                                "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                        salaire.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                                "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                                "-fx-font-size: 13px; -fx-text-fill: white; -fx-prompt-text-fill: #6B7280;");
                    } else {
                        salaireError.setText("✓ Format valide");
                        salaireError.setStyle(
                                "-fx-font-size: 10px; -fx-text-fill: #10B981; -fx-padding: 2 0; -fx-font-weight: bold;");
                        salaire.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                                "-fx-border-color: rgba(16,185,129,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                                "-fx-font-size: 13px; -fx-text-fill: white; -fx-prompt-text-fill: #6B7280;");
                    }
                } catch (NumberFormatException ex) {
                    salaireError.setText("Format invalide (nombre positif requis)");
                    salaireError.setStyle(
                            "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    salaire.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                            "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                            "-fx-font-size: 13px; -fx-text-fill: white; -fx-prompt-text-fill: #6B7280;");
                }
            }
        });

        VBox salaireBox = new VBox(lblSalaire, salaire, salaireError);
        salaireBox.setSpacing(3);

        // Recrutement
        Label lblRecrutement = new Label("RECRUTEMENT");
        lblRecrutement
                .setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");

        ComboBox<String> recrutementCombo = new ComboBox<>();
        recrutementCombo.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                "-fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; " +
                "-fx-font-size: 13px; -fx-text-fill: white; -fx-control-inner-background: rgba(26,26,46,0.9);");
        recrutementCombo.setPrefWidth(300);
        recrutementCombo.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
                setStyle(empty ? "" : "-fx-text-fill: white; -fx-background-color: rgba(26,26,46,0.95);");
            }
        });
        recrutementCombo.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
                setStyle("-fx-text-fill: white;");
            }
        });

        Label recrutementError = new Label("");
        recrutementError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0;");

        try {
            List<Recrutement> recrutements = serviceRecrutement.afficher();
            ObservableList<String> items = FXCollections.observableArrayList();
            for (Recrutement r : recrutements) {
                if ("Accepté".equalsIgnoreCase(r.getDecision_finale())) {
                    items.add(
                            r.getId_recrutement() + " - " + r.getDecision_finale() + " (" + r.getDate_decision() + ")");
                }
            }
            recrutementCombo.setItems(items);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des recrutements: " + e.getMessage());
        }

        recrutementCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                recrutementError.setText("✓ Sélection valide");
                recrutementError.setStyle(
                        "-fx-font-size: 10px; -fx-text-fill: #10B981; -fx-padding: 2 0; -fx-font-weight: bold;");
                recrutementCombo.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(16,185,129,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            } else {
                recrutementError.setText("Ce champ est obligatoire");
                recrutementError.setStyle(
                        "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                recrutementCombo.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            }
        });

        VBox recrutementBox = new VBox(lblRecrutement, recrutementCombo, recrutementError);
        recrutementBox.setSpacing(3);

        // Date Fin
        Label lblDateFin = new Label("DATE FIN");
        lblDateFin
                .setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");

        DatePicker dateFin = new DatePicker();
        dateFin.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                "-fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; " +
                "-fx-font-size: 13px; -fx-text-fill: white;");
        dateFin.setPrefWidth(300);

        Label dateFinError = new Label("");
        dateFinError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0;");

        dateFin.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && dateDebut.getValue() != null) {
                if (newVal.isAfter(dateDebut.getValue())) {
                    dateFinError.setText("✓ Format valide");
                    dateFinError.setStyle(
                            "-fx-font-size: 10px; -fx-text-fill: #10B981; -fx-padding: 2 0; -fx-font-weight: bold;");
                    dateFin.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                            "-fx-border-color: rgba(16,185,129,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                            "-fx-font-size: 13px; -fx-text-fill: white;");
                } else {
                    dateFinError.setText("La date fin doit être strictement après la date début");
                    dateFinError.setStyle(
                            "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    dateFin.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                            "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                            "-fx-font-size: 13px; -fx-text-fill: white;");
                }
            }
        });

        VBox dateFinBox = new VBox(lblDateFin, dateFin, dateFinError);
        dateFinBox.setSpacing(3);

        // Status (Auto-calculé)
        Label lblStatus = new Label("STATUT (Automatique)");
        lblStatus
                .setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.setItems(FXCollections.observableArrayList("En attente", "Actif", "Terminé"));
        statusCombo.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                "-fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; " +
                "-fx-font-size: 13px; -fx-text-fill: white; -fx-control-inner-background: rgba(26,26,46,0.9);");
        statusCombo.setPrefWidth(300);
        statusCombo.setDisable(true);
        statusCombo.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
                setStyle(empty ? "" : "-fx-text-fill: white; -fx-background-color: rgba(26,26,46,0.95);");
            }
        });
        statusCombo.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
                setStyle("-fx-text-fill: white;");
            }
        });

        Label statusError = new Label("(Rempli automatiquement selon les dates)");
        statusError.setStyle("-fx-font-size: 10px; -fx-text-fill: #667eea; -fx-padding: 2 0; -fx-font-style: italic;");

        VBox statusBox = new VBox(lblStatus, statusCombo, statusError);
        statusBox.setSpacing(3);

        // Set default status and add listeners to dateDebut and dateFin
        statusCombo.setValue("En attente");
        dateDebut.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && dateFin.getValue() != null) {
                String autoStatus = calculateAutoStatus(newVal, dateFin.getValue());
                statusCombo.setValue(autoStatus);
            }
        });
        dateFin.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && dateDebut.getValue() != null) {
                String autoStatus = calculateAutoStatus(dateDebut.getValue(), newVal);
                statusCombo.setValue(autoStatus);
            }
        });

        // Volume Horaire
        Label lblVolumeHoraire = new Label("VOLUME HORAIRE");
        lblVolumeHoraire
                .setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");

        TextField volumeHoraire = new TextField();
        volumeHoraire.setPromptText("Ex: 35h");
        volumeHoraire.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                "-fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; " +
                "-fx-font-size: 13px; -fx-text-fill: white; -fx-prompt-text-fill: #6B7280;");
        volumeHoraire.setPrefWidth(300);

        Label volumeHoraireError = new Label("");
        volumeHoraireError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0;");

        volumeHoraire.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                volumeHoraireError.setText("✓ Format valide");
                volumeHoraireError.setStyle(
                        "-fx-font-size: 10px; -fx-text-fill: #10B981; -fx-padding: 2 0; -fx-font-weight: bold;");
                volumeHoraire.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(16,185,129,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white; -fx-prompt-text-fill: #6B7280;");
            }
        });

        VBox volumeHoraireBox = new VBox(lblVolumeHoraire, volumeHoraire, volumeHoraireError);
        volumeHoraireBox.setSpacing(3);

        // Avantages
        Label lblAvantages = new Label("AVANTAGES");
        lblAvantages
                .setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");

        ComboBox<String> avantages = new ComboBox<>();
        avantages.setItems(
                FXCollections.observableArrayList("Aucun", "Tickets Restaurant", "Assurance Maladie", "Transport"));
        avantages.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                "-fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; " +
                "-fx-font-size: 13px; -fx-text-fill: white; -fx-control-inner-background: rgba(26,26,46,0.9);");
        avantages.setPrefWidth(300);
        avantages.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
                setStyle(empty ? "" : "-fx-text-fill: white; -fx-background-color: rgba(26,26,46,0.95);");
            }
        });
        avantages.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
                setStyle("-fx-text-fill: white;");
            }
        });

        VBox avantagesBox = new VBox(lblAvantages, avantages);
        avantagesBox.setSpacing(3);

        formFields.getChildren().addAll(typeBox, dateBox, dateFinBox, salaireBox, statusBox, volumeHoraireBox,
                avantagesBox, recrutementBox);

        // Buttons
        HBox buttonBox = new HBox();
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setStyle("-fx-padding: 20 0 0 0;");

        Button btnCancel = new Button("Annuler");
        btnCancel.setStyle("-fx-background-color: transparent; -fx-text-fill: #9CA3AF; -fx-font-size: 13px; " +
                "-fx-font-weight: 600; -fx-padding: 12 30; -fx-background-radius: 10; " +
                "-fx-border-color: rgba(156,163,175,0.3); -fx-border-radius: 10; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> modalStage.close());

        Button btnSave = new Button("✨ Enregistrer");
        btnSave.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 0%, #667eea, #f093fb); " +
                "-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 12 30; " +
                "-fx-background-radius: 10; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.4), 15, 0, 0, 5);");

        btnSave.setOnAction(e -> {
            try {
                // Validation
                if (typeCombo.getValue() == null || typeCombo.getValue().isEmpty()) {
                    typeError.setText("Ce champ est obligatoire");
                    typeError.setStyle(
                            "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    return;
                }

                if (dateDebut.getValue() == null) {
                    dateError.setText("Ce champ est obligatoire");
                    dateError.setStyle(
                            "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    return;
                }

                if (dateDebut.getValue().isBefore(java.time.LocalDate.now())) {
                    dateError.setText("La date doit être aujourd'hui ou après");
                    dateError.setStyle(
                            "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    dateDebut.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                            "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                            "-fx-font-size: 13px; -fx-text-fill: white;");
                    return;
                }

                if (salaire.getText().isEmpty()) {
                    salaireError.setText("Ce champ est obligatoire");
                    salaireError.setStyle(
                            "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    return;
                }

                try {
                    double salaireVal = Double.parseDouble(salaire.getText());
                    if (salaireVal <= 0) {
                        salaireError.setText("Le salaire doit être positif");
                        salaireError.setStyle(
                                "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    salaireError.setText("Format invalide (nombre positif requis)");
                    salaireError.setStyle(
                            "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    return;
                }

                if (dateFin.getValue() == null) {
                    dateFinError.setText("Ce champ est obligatoire");
                    dateFinError.setStyle(
                            "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    return;
                }

                if (!dateFin.getValue().isAfter(dateDebut.getValue())) {
                    dateFinError.setText("La date fin doit être strictement après la date début");
                    dateFinError.setStyle(
                            "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    dateFin.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                            "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                            "-fx-font-size: 13px; -fx-text-fill: white;");
                    return;
                }

                if (volumeHoraire.getText().isEmpty()) {
                    volumeHoraireError.setText("Ce champ est obligatoire");
                    volumeHoraireError.setStyle(
                            "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    return;
                }

                if (recrutementCombo.getValue() == null || recrutementCombo.getValue().isEmpty()) {
                    recrutementError.setText("Ce champ est obligatoire");
                    recrutementError.setStyle(
                            "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    return;
                }

                String selectedRecrutement = recrutementCombo.getValue();
                int idRecrutement = Integer.parseInt(selectedRecrutement.split(" - ")[0]);

                Contrat c = new Contrat(
                        typeCombo.getValue(),
                        Date.valueOf(dateDebut.getValue()),
                        Date.valueOf(dateFin.getValue()),
                        Double.parseDouble(salaire.getText()),
                        statusCombo.getValue(),
                        volumeHoraire.getText(),
                        avantages.getValue(),
                        idRecrutement);

                serviceContrat.ajouter(c);
                loadSampleContratData();
                setupContratTypeFilter();
                cbTypeContratFilter.setValue("Tous");
                tableContrats.setItems(contratData);
                showAlert("Succès", "Contrat ajouté avec succès!");
                modalStage.close();
            } catch (NumberFormatException ex) {
                showAlert("Erreur", "Format de nombre invalide");
            } catch (SQLException ex) {
                showAlert("Erreur", "Erreur SQL: " + ex.getMessage());
            }
        });

        buttonBox.getChildren().addAll(btnCancel, btnSave);

        modalContent.getChildren().addAll(header, formFields, buttonBox);

        ScrollPane scrollPane = new ScrollPane(modalContent);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-width: 0;");
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 400, 550);
        scene.setFill(Color.TRANSPARENT);

        modalStage.setScene(scene);
        modalStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/VOS.jpg")));
        modalStage.showAndWait();
    }

    @FXML
    private void ajouterRecrutement() {
        // Create custom modal dialog
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Ajouter un Recrutement");

        // Modal Content
        VBox modalContent = new VBox();
        modalContent.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #1a1a2e, #16213e); " +
                "-fx-background-radius: 20; -fx-padding: 30;");
        modalContent.setSpacing(15);

        // Header
        Label title = new Label("Nouveau Recrutement");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label("Créer un nouveau recrutement");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #9CA3AF;");

        VBox header = new VBox(title, subtitle);
        header.setStyle("-fx-spacing: 5;");

        // Form Fields
        VBox formFields = new VBox();
        formFields.setSpacing(12);

        // Date Décision
        Label lblDateDecision = new Label("DATE DÉCISION");
        lblDateDecision
                .setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");

        DatePicker dateDecision = new DatePicker();
        dateDecision.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                "-fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; " +
                "-fx-font-size: 13px; -fx-text-fill: white;");
        dateDecision.setPrefWidth(300);

        Label dateError = new Label("");
        dateError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0;");

        dateDecision.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                dateError.setText("✓ Format valide");
                dateError.setStyle(
                        "-fx-font-size: 10px; -fx-text-fill: #10B981; -fx-padding: 2 0; -fx-font-weight: bold;");
                dateDecision.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(16,185,129,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            } else {
                dateError.setText("Ce champ est obligatoire");
                dateError.setStyle(
                        "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                dateDecision.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            }
        });

        VBox dateBox = new VBox(lblDateDecision, dateDecision, dateError);
        dateBox.setSpacing(3);

        // Décision
        Label lblDecision = new Label("DÉCISION");
        lblDecision
                .setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");

        ComboBox<String> decisionCombo = new ComboBox<>();
        decisionCombo.setItems(FXCollections.observableArrayList("Accepté", "Refusé", "En attente"));
        decisionCombo.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                "-fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; " +
                "-fx-font-size: 13px; -fx-text-fill: white; -fx-control-inner-background: rgba(26,26,46,0.9);");
        decisionCombo.setPrefWidth(300);
        decisionCombo.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
                setStyle(empty ? "" : "-fx-text-fill: white; -fx-background-color: rgba(26,26,46,0.95);");
            }
        });
        decisionCombo.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
                setStyle("-fx-text-fill: white;");
            }
        });

        Label decisionError = new Label("");
        decisionError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0;");

        decisionCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                decisionError.setText("✓ Sélection valide");
                decisionError.setStyle(
                        "-fx-font-size: 10px; -fx-text-fill: #10B981; -fx-padding: 2 0; -fx-font-weight: bold;");
                decisionCombo.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(16,185,129,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            } else {
                decisionError.setText("Ce champ est obligatoire");
                decisionError.setStyle(
                        "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                decisionCombo.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            }
        });

        VBox decisionBox = new VBox(lblDecision, decisionCombo, decisionError);
        decisionBox.setSpacing(3);

        // ID Entretien
        Label lblIdEntretien = new Label("ID ENTRETIEN");
        lblIdEntretien
                .setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");

        ComboBox<Integer> idEntretien = new ComboBox<>();
        try {
            ObservableList<Integer> entretienIds = FXCollections
                    .observableArrayList(serviceRecrutement.getAvailableEntretienIds());
            idEntretien.setItems(entretienIds);
        } catch (SQLException ex) {
            System.err.println("Erreur lors du chargement des ID Entretien: " + ex.getMessage());
        }
        idEntretien.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                "-fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; " +
                "-fx-font-size: 13px; -fx-text-fill: white; -fx-control-inner-background: rgba(26,26,46,0.9);");
        idEntretien.setPrefWidth(300);
        idEntretien.setCellFactory(param -> new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.toString());
                setStyle(empty ? "" : "-fx-text-fill: white; -fx-background-color: rgba(26,26,46,0.95);");
            }
        });
        idEntretien.setButtonCell(new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.toString());
                setStyle("-fx-text-fill: white;");
            }
        });

        Label entretienError = new Label("");
        entretienError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0;");

        idEntretien.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                entretienError.setText("✓ Sélection valide");
                entretienError.setStyle(
                        "-fx-font-size: 10px; -fx-text-fill: #10B981; -fx-padding: 2 0; -fx-font-weight: bold;");
                idEntretien.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(16,185,129,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            } else {
                entretienError.setText("Ce champ est obligatoire");
                entretienError.setStyle(
                        "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                idEntretien.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            }
        });

        VBox entretienBox = new VBox(lblIdEntretien, idEntretien, entretienError);
        entretienBox.setSpacing(3);

        // ID Utilisateur
        Label lblIdUtilisateur = new Label("ID UTILISATEUR");
        lblIdUtilisateur
                .setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");

        ComboBox<Integer> idUtilisateur = new ComboBox<>();
        try {
            ObservableList<Integer> utilisateurIds = FXCollections
                    .observableArrayList(serviceRecrutement.getAvailableUtilisateurIds());
            idUtilisateur.setItems(utilisateurIds);
        } catch (SQLException ex) {
            System.err.println("Erreur lors du chargement des ID Utilisateur: " + ex.getMessage());
        }
        idUtilisateur.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                "-fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; " +
                "-fx-font-size: 13px; -fx-text-fill: white; -fx-control-inner-background: rgba(26,26,46,0.9);");
        idUtilisateur.setPrefWidth(300);
        idUtilisateur.setCellFactory(param -> new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.toString());
                setStyle(empty ? "" : "-fx-text-fill: white; -fx-background-color: rgba(26,26,46,0.95);");
            }
        });
        idUtilisateur.setButtonCell(new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.toString());
                setStyle("-fx-text-fill: white;");
            }
        });

        Label utilisateurError = new Label("");
        utilisateurError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0;");

        idUtilisateur.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                utilisateurError.setText("✓ Sélection valide");
                utilisateurError.setStyle(
                        "-fx-font-size: 10px; -fx-text-fill: #10B981; -fx-padding: 2 0; -fx-font-weight: bold;");
                idUtilisateur.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(16,185,129,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            } else {
                utilisateurError.setText("Ce champ est obligatoire");
                utilisateurError.setStyle(
                        "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                idUtilisateur.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            }
        });

        VBox utilisateurBox = new VBox(lblIdUtilisateur, idUtilisateur, utilisateurError);
        utilisateurBox.setSpacing(3);

        formFields.getChildren().addAll(dateBox, decisionBox, entretienBox, utilisateurBox);

        // Buttons
        HBox buttonBox = new HBox();
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setStyle("-fx-padding: 20 0 0 0;");

        Button btnCancel = new Button("Annuler");
        btnCancel.setStyle("-fx-background-color: transparent; -fx-text-fill: #9CA3AF; -fx-font-size: 13px; " +
                "-fx-font-weight: 600; -fx-padding: 12 30; -fx-background-radius: 10; " +
                "-fx-border-color: rgba(156,163,175,0.3); -fx-border-radius: 10; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> modalStage.close());

        Button btnSave = new Button("✨ Enregistrer");
        btnSave.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 0%, #667eea, #f093fb); " +
                "-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 12 30; " +
                "-fx-background-radius: 10; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.4), 15, 0, 0, 5);");

        btnSave.setOnAction(e -> {
            try {
                // Validation
                if (dateDecision.getValue() == null) {
                    dateError.setText("Ce champ est obligatoire");
                    dateError.setStyle(
                            "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    return;
                }

                if (decisionCombo.getValue() == null || decisionCombo.getValue().isEmpty()) {
                    decisionError.setText("Ce champ est obligatoire");
                    decisionError.setStyle(
                            "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    return;
                }

                if (idEntretien.getValue() == null) {
                    entretienError.setText("Ce champ est obligatoire");
                    entretienError.setStyle(
                            "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    return;
                }

                if (idUtilisateur.getValue() == null) {
                    utilisateurError.setText("Ce champ est obligatoire");
                    utilisateurError.setStyle(
                            "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    return;
                }

                Recrutement r = new Recrutement(
                        Date.valueOf(dateDecision.getValue()),
                        decisionCombo.getValue(),
                        idEntretien.getValue(),
                        idUtilisateur.getValue());

                serviceRecrutement.ajouter(r);
                loadSampleRecrutementData();
                tableRecrutements.setItems(recrutementData);
                showAlert("Succès", "Recrutement ajouté avec succès!");
                modalStage.close();
            } catch (SQLException ex) {
                showAlert("Erreur", "Erreur SQL: " + ex.getMessage());
            }
        });

        buttonBox.getChildren().addAll(btnCancel, btnSave);

        modalContent.getChildren().addAll(header, formFields, buttonBox);

        ScrollPane scrollPane = new ScrollPane(modalContent);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-width: 0;");
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 400, 620);
        scene.setFill(Color.TRANSPARENT);

        modalStage.setScene(scene);
        modalStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/VOS.jpg")));
        modalStage.showAndWait();
    }

    private void editContrat(ContratRow contratRow) {
        // Create custom modal dialog
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Modifier Contrat");

        // Modal Content
        VBox modalContent = new VBox();
        modalContent.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #1a1a2e, #16213e); " +
                "-fx-background-radius: 20; -fx-padding: 30;");
        modalContent.setSpacing(15);

        // Header
        Label title = new Label("Modifier Contrat");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label("Modifier les données du contrat");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #9CA3AF;");

        VBox header = new VBox(title, subtitle);
        header.setStyle("-fx-spacing: 5;");

        // Form Fields
        VBox formFields = new VBox();
        formFields.setSpacing(12);

        // Type
        Label lblType = new Label("TYPE DE CONTRAT");
        lblType.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.setItems(FXCollections.observableArrayList("CDI", "CDD", "Stage", "Freelance", "Alternance"));
        typeCombo.setValue(contratRow.typeProperty().get());
        typeCombo.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                "-fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; " +
                "-fx-font-size: 13px; -fx-text-fill: white; -fx-control-inner-background: rgba(26,26,46,0.9);");
        typeCombo.setPrefWidth(300);
        typeCombo.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
                setStyle(empty ? "" : "-fx-text-fill: white; -fx-background-color: rgba(26,26,46,0.95);");
            }
        });
        typeCombo.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
                setStyle("-fx-text-fill: white;");
            }
        });

        Label typeError = new Label("");
        typeError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0;");

        typeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                typeError.setText("✓ Format valide");
                typeError.setStyle(
                        "-fx-font-size: 10px; -fx-text-fill: #10B981; -fx-padding: 2 0; -fx-font-weight: bold;");
                typeCombo.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(16,185,129,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            } else {
                typeError.setText("Ce champ est obligatoire");
                typeError.setStyle(
                        "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                typeCombo.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            }
        });

        VBox typeBox = new VBox(lblType, typeCombo, typeError);
        typeBox.setSpacing(3);

        // Date Début
        Label lblDate = new Label("DATE DÉBUT");
        lblDate.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");

        DatePicker dateDebut = new DatePicker();
        if (contratRow.dateDebutProperty().get() != null && !contratRow.dateDebutProperty().get().isEmpty()) {
            dateDebut.setValue(java.time.LocalDate.parse(contratRow.dateDebutProperty().get()));
        } else {
            dateDebut.setValue(java.time.LocalDate.now());
        }
        dateDebut.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                "-fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; " +
                "-fx-font-size: 13px; -fx-text-fill: white;");
        dateDebut.setPrefWidth(300);
        dateDebut.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(java.time.LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(java.time.LocalDate.now()));
            }
        });

        Label dateError = new Label("");
        dateError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0;");

        dateDebut.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                if (newVal.isBefore(java.time.LocalDate.now())) {
                    dateError.setText("La date doit être aujourd'hui ou après");
                    dateError.setStyle(
                            "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    dateDebut.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                            "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                            "-fx-font-size: 13px; -fx-text-fill: white;");
                } else {
                    dateError.setText("✓ Format valide");
                    dateError.setStyle(
                            "-fx-font-size: 10px; -fx-text-fill: #10B981; -fx-padding: 2 0; -fx-font-weight: bold;");
                    dateDebut.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                            "-fx-border-color: rgba(16,185,129,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                            "-fx-font-size: 13px; -fx-text-fill: white;");
                }
            } else {
                dateError.setText("Ce champ est obligatoire");
                dateError.setStyle(
                        "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                dateDebut.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            }
        });

        VBox dateBox = new VBox(lblDate, dateDebut, dateError);
        dateBox.setSpacing(3);

        // Salaire
        Label lblSalaire = new Label("SALAIRE");
        lblSalaire
                .setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");

        TextField salaire = new TextField();
        salaire.setText(String.valueOf(contratRow.salaireProperty().get()));
        salaire.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                "-fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; " +
                "-fx-font-size: 13px; -fx-text-fill: white;");
        salaire.setPrefWidth(300);

        Label salaireError = new Label("");
        salaireError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0;");

        salaire.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                salaireError.setText("Ce champ est obligatoire");
                salaireError.setStyle(
                        "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                salaire.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            } else {
                try {
                    double val = Double.parseDouble(newVal);
                    if (val <= 0) {
                        salaireError.setText("Le salaire doit être positif");
                        salaireError.setStyle(
                                "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                        salaire.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                                "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                                "-fx-font-size: 13px; -fx-text-fill: white;");
                    } else {
                        salaireError.setText("✓ Format valide");
                        salaireError.setStyle(
                                "-fx-font-size: 10px; -fx-text-fill: #10B981; -fx-padding: 2 0; -fx-font-weight: bold;");
                        salaire.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                                "-fx-border-color: rgba(16,185,129,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                                "-fx-font-size: 13px; -fx-text-fill: white;");
                    }
                } catch (NumberFormatException ex) {
                    salaireError.setText("Format invalide (nombre positif requis)");
                    salaireError.setStyle(
                            "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    salaire.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                            "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                            "-fx-font-size: 13px; -fx-text-fill: white;");
                }
            }
        });

        VBox salaireBox = new VBox(lblSalaire, salaire, salaireError);
        salaireBox.setSpacing(3);

        // Date Fin
        Label lblDateFin = new Label("DATE FIN");
        lblDateFin
                .setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");

        DatePicker dateFin = new DatePicker();
        if (contratRow.dateFinProperty().get() != null && !contratRow.dateFinProperty().get().isEmpty()) {
            dateFin.setValue(java.time.LocalDate.parse(contratRow.dateFinProperty().get()));
        }
        dateFin.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                "-fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; " +
                "-fx-font-size: 13px; -fx-text-fill: white;");
        dateFin.setPrefWidth(300);

        Label dateFinError = new Label("");
        dateFinError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0;");

        dateFin.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && dateDebut.getValue() != null) {
                if (newVal.isAfter(dateDebut.getValue())) {
                    dateFinError.setText("✓ Format valide");
                    dateFinError.setStyle(
                            "-fx-font-size: 10px; -fx-text-fill: #10B981; -fx-padding: 2 0; -fx-font-weight: bold;");
                    dateFin.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                            "-fx-border-color: rgba(16,185,129,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                            "-fx-font-size: 13px; -fx-text-fill: white;");
                } else {
                    dateFinError.setText("La date fin doit être strictement après la date début");
                    dateFinError.setStyle(
                            "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    dateFin.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                            "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                            "-fx-font-size: 13px; -fx-text-fill: white;");
                }
            } else {
                dateFinError.setText("");
                dateFin.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            }
        });

        VBox dateFinBox = new VBox(lblDateFin, dateFin, dateFinError);
        dateFinBox.setSpacing(3);

        // Status (Auto-calculé)
        Label lblStatus = new Label("STATUT (Automatique)");
        lblStatus
                .setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.setItems(FXCollections.observableArrayList("En attente", "Actif", "Terminé"));
        statusCombo.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                "-fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; " +
                "-fx-font-size: 13px; -fx-text-fill: white; -fx-control-inner-background: rgba(26,26,46,0.9);");
        statusCombo.setPrefWidth(300);
        statusCombo.setDisable(true);
        statusCombo.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
                setStyle(empty ? "" : "-fx-text-fill: white; -fx-background-color: rgba(26,26,46,0.95);");
            }
        });
        statusCombo.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
                setStyle("-fx-text-fill: white;");
            }
        });

        Label statusError = new Label("(Rempli automatiquement selon les dates)");
        statusError.setStyle("-fx-font-size: 10px; -fx-text-fill: #667eea; -fx-padding: 2 0; -fx-font-style: italic;");

        VBox statusBox = new VBox(lblStatus, statusCombo, statusError);
        statusBox.setSpacing(3);

        // Set initial auto status and add listeners to dateDebut and dateFin
        if (dateDebut.getValue() != null && dateFin.getValue() != null) {
            String autoStatus = calculateAutoStatus(dateDebut.getValue(), dateFin.getValue());
            statusCombo.setValue(autoStatus);
        } else {
            statusCombo.setValue("En attente");
        }

        dateDebut.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && dateFin.getValue() != null) {
                String autoStatus = calculateAutoStatus(newVal, dateFin.getValue());
                statusCombo.setValue(autoStatus);
            }
        });
        dateFin.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && dateDebut.getValue() != null) {
                String autoStatus = calculateAutoStatus(dateDebut.getValue(), newVal);
                statusCombo.setValue(autoStatus);
            }
        });

        // Volume Horaire
        Label lblVolumeHoraire = new Label("VOLUME HORAIRE");
        lblVolumeHoraire
                .setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");

        TextField volumeHoraire = new TextField();
        volumeHoraire.setText(contratRow.volumeHoraireProperty().get());
        volumeHoraire.setPromptText("Ex: 35h");
        volumeHoraire.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                "-fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; " +
                "-fx-font-size: 13px; -fx-text-fill: white;");
        volumeHoraire.setPrefWidth(300);

        Label volumeHoraireError = new Label("");
        volumeHoraireError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0;");

        volumeHoraire.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                volumeHoraireError.setText("✓ Format valide");
                volumeHoraireError.setStyle(
                        "-fx-font-size: 10px; -fx-text-fill: #10B981; -fx-padding: 2 0; -fx-font-weight: bold;");
                volumeHoraire.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(16,185,129,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            } else {
                volumeHoraireError.setText("");
                volumeHoraire.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            }
        });

        VBox volumeHoraireBox = new VBox(lblVolumeHoraire, volumeHoraire, volumeHoraireError);
        volumeHoraireBox.setSpacing(3);

        // Avantages
        Label lblAvantages = new Label("AVANTAGES");
        lblAvantages
                .setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");

        ComboBox<String> avantages = new ComboBox<>();
        avantages.setItems(
                FXCollections.observableArrayList("Aucun", "Tickets Restaurant", "Assurance Maladie", "Transport"));
        avantages.setValue(contratRow.avantagesProperty().get());
        avantages.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                "-fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; " +
                "-fx-font-size: 13px; -fx-text-fill: white; -fx-control-inner-background: rgba(26,26,46,0.9);");
        avantages.setPrefWidth(300);
        avantages.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
                setStyle(empty ? "" : "-fx-text-fill: white; -fx-background-color: rgba(26,26,46,0.95);");
            }
        });
        avantages.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
                setStyle("-fx-text-fill: white;");
            }
        });

        VBox avantagesBox = new VBox(lblAvantages, avantages);
        avantagesBox.setSpacing(3);

        // Recrutement
        Label lblRecrutement = new Label("RECRUTEMENT");
        lblRecrutement
                .setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");

        ComboBox<String> recrutementCombo = new ComboBox<>();
        recrutementCombo.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                "-fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; " +
                "-fx-font-size: 13px; -fx-text-fill: white; -fx-control-inner-background: rgba(26,26,46,0.9);");
        recrutementCombo.setPrefWidth(300);
        recrutementCombo.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
                setStyle(empty ? "" : "-fx-text-fill: white; -fx-background-color: rgba(26,26,46,0.95);");
            }
        });
        recrutementCombo.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
                setStyle("-fx-text-fill: white;");
            }
        });

        Label recrutementError = new Label("");
        recrutementError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0;");

        int currentRecrutementId = contratRow.idRecrutementProperty().get();
        String currentSelection = "";

        try {
            List<Recrutement> recrutements = serviceRecrutement.afficher();
            ObservableList<String> recrutementItems = FXCollections.observableArrayList();
            for (Recrutement r : recrutements) {
                if ("Accepté".equalsIgnoreCase(r.getDecision_finale())) {
                    String item = r.getId_recrutement() + " - " + r.getDecision_finale() + " (" + r.getDate_decision()
                            + ")";
                    recrutementItems.add(item);
                    if (r.getId_recrutement() == currentRecrutementId) {
                        currentSelection = item;
                    }
                }
            }
            recrutementCombo.setItems(recrutementItems);
            if (!currentSelection.isEmpty()) {
                recrutementCombo.setValue(currentSelection);
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des recrutements: " + e.getMessage());
        }

        recrutementCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                recrutementError.setText("✓ Sélection valide");
                recrutementError.setStyle(
                        "-fx-font-size: 10px; -fx-text-fill: #10B981; -fx-padding: 2 0; -fx-font-weight: bold;");
                recrutementCombo.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(16,185,129,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            } else {
                recrutementError.setText("Ce champ est obligatoire");
                recrutementError.setStyle(
                        "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                recrutementCombo.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            }
        });

        VBox recrutementBox = new VBox(lblRecrutement, recrutementCombo, recrutementError);
        recrutementBox.setSpacing(3);

        formFields.getChildren().addAll(typeBox, dateBox, salaireBox, dateFinBox, statusBox, volumeHoraireBox,
                avantagesBox, recrutementBox);

        // Buttons
        HBox buttonBox = new HBox();
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setStyle("-fx-padding: 20 0 0 0;");

        Button btnCancel = new Button("Annuler");
        btnCancel.setStyle("-fx-background-color: transparent; -fx-text-fill: #9CA3AF; -fx-font-size: 13px; " +
                "-fx-font-weight: 600; -fx-padding: 12 30; -fx-background-radius: 10; " +
                "-fx-border-color: rgba(156,163,175,0.3); -fx-border-radius: 10; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> modalStage.close());

        Button btnSave = new Button("✨ Enregistrer");
        btnSave.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 0%, #667eea, #f093fb); " +
                "-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 12 30; " +
                "-fx-background-radius: 10; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.4), 15, 0, 0, 5);");

        btnSave.setOnAction(e -> {
            try {
                // Validation
                if (typeCombo.getValue() == null || typeCombo.getValue().isEmpty()) {
                    typeError.setText("Ce champ est obligatoire");
                    typeError.setStyle(
                            "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    return;
                }

                if (dateDebut.getValue() == null) {
                    dateError.setText("Ce champ est obligatoire");
                    dateError.setStyle(
                            "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    return;
                }

                if (dateDebut.getValue().isBefore(java.time.LocalDate.now())) {
                    dateError.setText("La date doit être aujourd'hui ou après");
                    dateError.setStyle(
                            "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    dateDebut.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                            "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                            "-fx-font-size: 13px; -fx-text-fill: white;");
                    return;
                }

                if (salaire.getText().isEmpty()) {
                    salaireError.setText("Ce champ est obligatoire");
                    salaireError.setStyle(
                            "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    return;
                }

                try {
                    double salaireVal = Double.parseDouble(salaire.getText());
                    if (salaireVal <= 0) {
                        salaireError.setText("Le salaire doit être positif");
                        salaireError.setStyle(
                                "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    salaireError.setText("Format invalide (nombre positif requis)");
                    salaireError.setStyle(
                            "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    return;
                }

                if (dateFin.getValue() != null && !dateFin.getValue().isAfter(dateDebut.getValue())) {
                    dateFinError.setText("La date fin doit être strictement après la date début");
                    dateFinError.setStyle(
                            "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    dateFin.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                            "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                            "-fx-font-size: 13px; -fx-text-fill: white;");
                    return;
                }

                if (recrutementCombo.getValue() == null || recrutementCombo.getValue().isEmpty()) {
                    recrutementError.setText("Ce champ est obligatoire");
                    recrutementError.setStyle(
                            "-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    return;
                }

                String selectedRecrutement = recrutementCombo.getValue();
                int idRecrutement = Integer.parseInt(selectedRecrutement.split(" - ")[0]);

                Contrat c = new Contrat(
                        contratRow.idProperty().get(),
                        typeCombo.getValue(),
                        Date.valueOf(dateDebut.getValue()),
                        Date.valueOf(dateFin.getValue()),
                        Double.parseDouble(salaire.getText()),
                        statusCombo.getValue(),
                        volumeHoraire.getText(),
                        avantages.getValue(),
                        idRecrutement);

                serviceContrat.modifier(c);
                loadSampleContratData();
                setupContratTypeFilter();
                cbTypeContratFilter.setValue("Tous");
                tableContrats.setItems(contratData);
                showAlert("Succès", "Contrat modifié avec succès!");
                modalStage.close();
            } catch (NumberFormatException ex) {
                showAlert("Erreur", "Format de nombre invalide");
            } catch (SQLException ex) {
                showAlert("Erreur", "Erreur SQL: " + ex.getMessage());
            }
        });

        buttonBox.getChildren().addAll(btnCancel, btnSave);

        modalContent.getChildren().addAll(header, formFields, buttonBox);

        ScrollPane scrollPane = new ScrollPane(modalContent);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-width: 0;");
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 400, 500);
        scene.setFill(Color.TRANSPARENT);

        modalStage.setScene(scene);
        modalStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/VOS.jpg")));
        modalStage.showAndWait();
    }

    private void deleteContrat(ContratRow contratRow) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmer la suppression");
        confirmDialog.setHeaderText("Supprimer le contrat #" + contratRow.idProperty().get());
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer ce contrat?");

        confirmDialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    serviceContrat.supprimer(contratRow.idProperty().get());
                    loadSampleContratData();
                    setupContratTypeFilter();
                    cbTypeContratFilter.setValue("Tous");
                    tableContrats.setItems(contratData);
                    showAlert("Succès", "Contrat supprimé avec succès!");
                } catch (SQLException e) {
                    showAlert("Erreur", "Erreur SQL: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void editRecrutement(Recrutement recrutement) {
        // Create custom modal dialog
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Modifier Recrutement");

        // Modal Content
        VBox modalContent = new VBox();
        modalContent.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #1a1a2e, #16213e); " +
                "-fx-background-radius: 20; -fx-padding: 30;");
        modalContent.setSpacing(15);

        // Header
        Label title = new Label("Modifier Recrutement");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label("Modifier les données du recrutement");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #9CA3AF;");

        VBox header = new VBox(title, subtitle);
        header.setStyle("-fx-spacing: 5;");

        // Form Fields
        VBox formFields = new VBox();
        formFields.setSpacing(12);

        // Date Décision
        Label lblDateDecision = new Label("DATE DÉCISION");
        lblDateDecision
                .setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");

        DatePicker dateDecision = new DatePicker();
        dateDecision.setValue(recrutement.getDate_decision().toLocalDate());
        dateDecision.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                "-fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; " +
                "-fx-font-size: 13px; -fx-text-fill: white;");
        dateDecision.setPrefWidth(300);

        VBox dateBox = new VBox(lblDateDecision, dateDecision);
        dateBox.setSpacing(5);

        // Décision
        Label lblDecision = new Label("DÉCISION");
        lblDecision
                .setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");

        ComboBox<String> decisionCombo = new ComboBox<>();
        decisionCombo.setItems(FXCollections.observableArrayList("Accepté", "Refusé", "En attente"));
        decisionCombo.setValue(recrutement.getDecision_finale());
        decisionCombo.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                "-fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; " +
                "-fx-font-size: 13px; -fx-text-fill: white; -fx-control-inner-background: rgba(26,26,46,0.9);");
        decisionCombo.setPrefWidth(300);
        decisionCombo.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
                setStyle(empty ? "" : "-fx-text-fill: white; -fx-background-color: rgba(26,26,46,0.95);");
            }
        });
        decisionCombo.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
                setStyle("-fx-text-fill: white;");
            }
        });

        VBox decisionBox = new VBox(lblDecision, decisionCombo);
        decisionBox.setSpacing(5);

        // ID Entretien
        Label lblIdEntretien = new Label("ID ENTRETIEN");
        lblIdEntretien
                .setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");

        ComboBox<Integer> idEntretien = new ComboBox<>();
        int currentIdEntretien = recrutement.getId_entretien();
        try {
            ObservableList<Integer> entretienIds = FXCollections
                    .observableArrayList(serviceRecrutement.getAvailableEntretienIds());
            idEntretien.setItems(entretienIds);
            idEntretien.setValue(currentIdEntretien);
        } catch (SQLException ex) {
            System.err.println("Erreur lors du chargement des ID Entretien: " + ex.getMessage());
        }
        idEntretien.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                "-fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; " +
                "-fx-font-size: 13px; -fx-text-fill: white; -fx-control-inner-background: rgba(26,26,46,0.9);");
        idEntretien.setPrefWidth(300);
        idEntretien.setCellFactory(param -> new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.toString());
                setStyle(empty ? "" : "-fx-text-fill: white; -fx-background-color: rgba(26,26,46,0.95);");
            }
        });
        idEntretien.setButtonCell(new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.toString());
                setStyle("-fx-text-fill: white;");
            }
        });

        VBox entretienBox = new VBox(lblIdEntretien, idEntretien);
        entretienBox.setSpacing(5);

        // ID Utilisateur
        Label lblIdUtilisateur = new Label("ID UTILISATEUR");
        lblIdUtilisateur
                .setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");

        ComboBox<Integer> idUtilisateur = new ComboBox<>();
        int currentIdUtilisateur = recrutement.getId_utilisateur();
        try {
            ObservableList<Integer> utilisateurIds = FXCollections
                    .observableArrayList(serviceRecrutement.getAvailableUtilisateurIds());
            idUtilisateur.setItems(utilisateurIds);
            idUtilisateur.setValue(currentIdUtilisateur);
        } catch (SQLException ex) {
            System.err.println("Erreur lors du chargement des ID Utilisateur: " + ex.getMessage());
        }
        idUtilisateur.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                "-fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; " +
                "-fx-font-size: 13px; -fx-text-fill: white; -fx-control-inner-background: rgba(26,26,46,0.9);");
        idUtilisateur.setPrefWidth(300);
        idUtilisateur.setCellFactory(param -> new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.toString());
                setStyle(empty ? "" : "-fx-text-fill: white; -fx-background-color: rgba(26,26,46,0.95);");
            }
        });
        idUtilisateur.setButtonCell(new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.toString());
                setStyle("-fx-text-fill: white;");
            }
        });

        VBox utilisateurBox = new VBox(lblIdUtilisateur, idUtilisateur);
        utilisateurBox.setSpacing(5);

        formFields.getChildren().addAll(dateBox, decisionBox, entretienBox, utilisateurBox);

        // Buttons
        HBox buttonBox = new HBox();
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setStyle("-fx-padding: 20 0 0 0;");

        Button btnCancel = new Button("Annuler");
        btnCancel.setStyle("-fx-background-color: transparent; -fx-text-fill: #9CA3AF; -fx-font-size: 13px; " +
                "-fx-font-weight: 600; -fx-padding: 12 30; -fx-background-radius: 10; " +
                "-fx-border-color: rgba(156,163,175,0.3); -fx-border-radius: 10; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> modalStage.close());

        Button btnSave = new Button("✨ Enregistrer");
        btnSave.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 0%, #667eea, #f093fb); " +
                "-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 12 30; " +
                "-fx-background-radius: 10; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.4), 15, 0, 0, 5);");

        btnSave.setOnAction(e -> {
            try {
                if (dateDecision.getValue() == null || decisionCombo.getValue() == null ||
                        idEntretien.getValue() == null || idUtilisateur.getValue() == null) {
                    showAlert("Erreur", "Veuillez remplir tous les champs");
                    return;
                }

                Recrutement r = new Recrutement(
                        recrutement.getId_recrutement(),
                        Date.valueOf(dateDecision.getValue()),
                        decisionCombo.getValue(),
                        idEntretien.getValue(),
                        idUtilisateur.getValue());

                serviceRecrutement.modifier(r);

                // ✅ Automated Email Flow (Accepté/Refusé)
                automatedStatusEmailFlow(r, decisionCombo.getValue());

                loadSampleRecrutementData();
                tableRecrutements.setItems(recrutementData);
                showAlert("Succès", "Recrutement modifié avec succès!");
                modalStage.close();
            } catch (SQLException ex) {
                showAlert("Erreur", "Erreur SQL: " + ex.getMessage());
            }
        });

        buttonBox.getChildren().addAll(btnCancel, btnSave);

        modalContent.getChildren().addAll(header, formFields, buttonBox);

        ScrollPane scrollPane = new ScrollPane(modalContent);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-width: 0;");
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 400, 500);
        scene.setFill(Color.TRANSPARENT);

        modalStage.setScene(scene);
        modalStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/VOS.jpg")));
        modalStage.showAndWait();
    }

    private void deleteRecrutement(Recrutement recrutement) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmer la suppression");
        confirmDialog.setHeaderText("Supprimer le recrutement #" + recrutement.getId_recrutement());
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer ce recrutement?");

        confirmDialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    serviceRecrutement.supprimer(recrutement.getId_recrutement());
                    loadSampleRecrutementData();
                    tableRecrutements.setItems(recrutementData);
                    showAlert("Succès", "Recrutement supprimé avec succès!");
                } catch (SQLException e) {
                    showAlert("Erreur", "Erreur SQL: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void updateNavButtonStyle(Button activeButton) {
        // Reset all old buttons if they exist
        if (btnNavEntretiens != null)
            btnNavEntretiens.setStyle("");
        if (btnNavDashboard != null)
            btnNavDashboard.setStyle("");
        if (btnNavStats != null)
            btnNavStats.setStyle("");

        // Style active button if not null
        if (activeButton != null) {
            activeButton.setStyle("-fx-background-color: #F3F4F6;");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // =========================================================================
    // PDF CONTRACT GENERATION
    // =========================================================================

    /** Generates a 16-char unique signature token for the contract. */
    private String generateContractSignature(int contractId) {
        try {
            String raw = contractId + "-" + System.currentTimeMillis();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash)
                sb.append(String.format("%02x", b));
            return sb.substring(0, 16).toUpperCase();
        } catch (Exception e) {
            return "SIGN" + contractId;
        }
    }

    /** Opens a styled JavaFX modal showing the formatted contract. */
    private void showContratPreview(ContratRow contrat) {
        // Check if contract status is "Actif"
        if (!contrat.statusProperty().get().equals("Actif")) {
            showAlert("Accès refusé", "Seuls les contrats avec le statut 'Actif' peuvent être visualisés.");
            return;
        }
        Stage modal = new Stage();
        modal.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        modal.setTitle("Aperçu du Contrat #" + contrat.idProperty().get());
        modal.setResizable(true);

        // ── Root ─────────────────────────────────────────────────────────────
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #1a1a2e;");

        // ── Header bar ───────────────────────────────────────────────────────
        VBox headerBar = new VBox(4);
        headerBar.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 0%, #667eea, #f093fb); " +
                "-fx-padding: 20 30;");
        Label companyLbl = new Label("🏢  VOS – Votre Outil de Succès");
        companyLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label genDate = new Label("Généré le : " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        genDate.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.75);");
        Label titleLbl = new Label("CONTRAT DE TRAVAIL");
        titleLbl.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white; -fx-padding: 6 0 0 0;");
        headerBar.getChildren().addAll(companyLbl, genDate, titleLbl);

        // ── Body ─────────────────────────────────────────────────────────────
        VBox body = new VBox(18);
        body.setStyle("-fx-padding: 24 30;");

        body.getChildren().addAll(
                buildPreviewSection("📋  Informations du Contrat", new String[][] {
                        { "N° Contrat", "#" + contrat.idProperty().get() },
                        { "Type", contrat.typeProperty().get() },
                        { "Période", contrat.periodeProperty().get() },
                        { "Date de début", contrat.dateDebutProperty().get() },
                        { "Date de fin", contrat.dateFinProperty().get() },
                        { "Réf. Recrutement", "#" + contrat.idRecrutementProperty().get() }
                }),
                buildPreviewSection("⚙️  Conditions", new String[][] {
                        { "Statut", contrat.statusProperty().get() },
                        { "Volume horaire", contrat.volumeHoraireProperty().get() }
                }),
                buildPreviewSection("💰  Rémunération", new String[][] {
                        { "Salaire brut", String.format("%.2f DT", contrat.salaireProperty().get()) }
                }),
                buildPreviewSection("🎁  Avantages", new String[][] {
                        { "Avantages", contrat.avantagesProperty().get() }
                }));

        // ── Signature ─────────────────────────────────────────────────────────
        String sig = generateContractSignature(contrat.idProperty().get());
        VBox sigBox = new VBox(6);
        sigBox.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-padding: 16; " +
                "-fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 8; -fx-background-radius: 8;");
        Label sigTitle = new Label("✍️  Signature électronique");
        sigTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #9CA3AF;");
        Label sigValue = new Label(sig);
        sigValue.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 18px; -fx-font-style: italic; " +
                "-fx-text-fill: #667eea; -fx-letter-spacing: 4;");
        Label sigNote = new Label("Signature unique générée automatiquement – non modifiable");
        sigNote.setStyle("-fx-font-size: 10px; -fx-text-fill: #6B7280; -fx-padding: 2 0 0 0;");
        sigBox.getChildren().addAll(sigTitle, sigValue, sigNote);
        body.getChildren().add(sigBox);

        // ── Close button ─────────────────────────────────────────────────────
        Button btnClose = new Button("✖  Fermer");
        btnClose.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: white; " +
                "-fx-font-size: 13px; -fx-padding: 10 30; -fx-background-radius: 8; -fx-cursor: hand; " +
                "-fx-border-color: rgba(255,255,255,0.15); -fx-border-radius: 8;");
        btnClose.setOnAction(e -> modal.close());
        HBox btnBar = new HBox(btnClose);
        btnBar.setAlignment(Pos.CENTER_RIGHT);
        btnBar.setStyle("-fx-padding: 0 30 24 30;");

        root.getChildren().addAll(headerBar, body, btnBar);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #1a1a2e;");

        Scene scene = new Scene(scroll, 560, 680);
        scene.setFill(Color.web("#1a1a2e"));
        modal.setScene(scene);
        try {
            modal.getIcons().add(new Image(getClass().getResourceAsStream("/images/VOS.jpg")));
        } catch (Exception ignored) {
        }
        modal.show();
    }

    /** Builds one labelled section card for the preview modal. */
    private VBox buildPreviewSection(String title, String[][] rows) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-padding: 14 16; " +
                "-fx-border-color: rgba(102,126,234,0.25); -fx-border-radius: 8; -fx-background-radius: 8;");
        Label sectionTitle = new Label(title);
        sectionTitle
                .setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #c4b5fd; -fx-padding: 0 0 4 0;");
        card.getChildren().add(sectionTitle);
        for (String[] kv : rows) {
            HBox row = new HBox(10);
            Label key = new Label(kv[0] + " :");
            key.setStyle("-fx-min-width: 130; -fx-font-size: 12px; -fx-text-fill: #9CA3AF;");
            Label val = new Label(kv[1] == null || kv[1].isEmpty() ? "—" : kv[1]);
            val.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #F3F4F6;");
            row.getChildren().addAll(key, val);
            card.getChildren().add(row);
        }
        return card;
    }

    /**
     * Saves a professional corporate contract PDF using OpenPDF with French legal
     * formatting.
     */
    /**
     * Automated flow to send emails when recruitment status changes to "Accepté" or
     * "Refusé".
     */
    private void automatedStatusEmailFlow(Recrutement rec, String status) {
        if (!status.equals("Accepté") && !status.equals("Refusé"))
            return;

        Platform.runLater(() -> {
            try {
                String candidateEmail = serviceRecrutement.getUserEmailById(rec.getId_utilisateur());
                if (candidateEmail == null || candidateEmail.isEmpty()) {
                    System.err.println("Email du candidat introuvable pour ID: " + rec.getId_utilisateur());
                    return;
                }

                Map<String, String> ctx = serviceRecrutement.getAIContext(rec.getId_recrutement());
                String candidateName = ctx.getOrDefault("candidate_name", "Candidat");
                String jobTitle = ctx.getOrDefault("job_title", "N/A");

                String emailType = status.equals("Accepté") ? "Acceptation" : "Refus";
                String body = EmailService.getInstance().getTemplate(emailType, candidateName, jobTitle);

                try {
                    File attachment = null;
                    if (status.equals("Accepté")) {
                        attachment = PDFService.getInstance().generateTemporaryContractPDF(rec, ctx, false);
                    }

                    String subject = status.equals("Accepté") ? "Félicitations - Votre candidature chez VOS"
                            : "Mise à jour de votre candidature chez VOS";
                    EmailService.getInstance().sendEmail(candidateEmail, subject, body, attachment);

                    Platform.runLater(() -> {
                        System.out.println("Email automatique envoyé à " + candidateEmail);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void downloadContratPdf(ContratRow contrat) {
        // Check if contract status is "Actif"
        if (!contrat.statusProperty().get().equals("Actif")) {
            showAlert("Accès refusé", "Seuls les contrats avec le statut 'Actif' peuvent être téléchargés.");
            return;
        }

        // ── File chooser ─────────────────────────────────────────────────────
        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer le contrat PDF");
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String defaultName = String.format("contrat_%d_%s_%s.pdf",
                contrat.idProperty().get(),
                contrat.typeProperty().get().replaceAll("\\s+", "_"),
                today);
        fc.setInitialFileName(defaultName);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fc.showSaveDialog(tableContrats.getScene().getWindow());
        if (file == null)
            return;

        try (FileOutputStream fos = new FileOutputStream(file)) {
            // ── Document setup (A4 with compact margins to fit on one page) ────
            com.lowagie.text.Document doc = new com.lowagie.text.Document(PageSize.A4, 35, 35, 40, 40);
            PdfWriter.getInstance(doc, fos);
            doc.open();

            // ── Professional Fonts ────────────────────────────────────────────
            com.lowagie.text.Font fontTitle = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18,
                    com.lowagie.text.Font.BOLD, java.awt.Color.BLACK);
            com.lowagie.text.Font fontArticle = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 11,
                    com.lowagie.text.Font.BOLD, java.awt.Color.BLACK);
            com.lowagie.text.Font fontBody = new com.lowagie.text.Font(com.lowagie.text.Font.TIMES_ROMAN, 11,
                    com.lowagie.text.Font.NORMAL, java.awt.Color.BLACK);
            com.lowagie.text.Font fontSubtitle = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 9,
                    com.lowagie.text.Font.NORMAL, new java.awt.Color(0x6b, 0x7d, 0x8c));
            com.lowagie.text.Font fontSmallGray = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 8,
                    com.lowagie.text.Font.NORMAL, new java.awt.Color(0x9c, 0xa3, 0xaf));
            com.lowagie.text.Font fontLabel = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10,
                    com.lowagie.text.Font.BOLD, new java.awt.Color(0x1f, 0x29, 0x37));

            // ── Add professional header with logo ─────────────────────────────
            addProfessionalHeader(doc, fontTitle, fontSubtitle);

            // ── Add introduction paragraph (French legal style) ───────────────
            addIntroductionParagraph(doc, fontBody, contrat);

            // ── Add 5 Articles as flowing paragraphs ──────────────────────────
            addArticle1(doc, fontArticle, fontBody, contrat);

            addArticle2(doc, fontArticle, fontBody, contrat);

            addArticle3(doc, fontArticle, fontBody, contrat);

            addArticle4(doc, fontArticle, fontBody, contrat);

            addArticle5(doc, fontArticle, fontBody, contrat);

            // ── Add signature block ───────────────────────────────────────────
            addSignatureBlock(doc, fontLabel, fontBody, fontSmallGray, contrat);

            doc.close();
            showAlert("PDF généré", "Contrat enregistré avec succès :\n" + file.getAbsolutePath());

        } catch (Exception e) {
            showError("Erreur lors de la génération PDF : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Adds professional header with logo and document title. */
    private void addProfessionalHeader(com.lowagie.text.Document doc, com.lowagie.text.Font fontTitle,
            com.lowagie.text.Font fontSubtitle) throws com.lowagie.text.DocumentException {
        try {
            // ── Add dark background container for logo ────────────────────────
            PdfPTable headerTable = new PdfPTable(1);
            headerTable.setWidthPercentage(100);
            PdfPCell headerCell = new PdfPCell();
            headerCell.setBackgroundColor(new java.awt.Color(0x1f, 0x29, 0x37));
            headerCell.setPadding(12);
            headerCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);

            // ── Try to add logo ──────────────────────────────────────────────
            try {
                String logoPath = "src/main/resources/Images/VOSwhiteslogan.png";
                com.lowagie.text.Image logo = com.lowagie.text.Image.getInstance(logoPath);
                logo.setAlignment(Element.ALIGN_CENTER);
                logo.scaleToFit(270, 105);
                Paragraph logoPara = new Paragraph();
                logoPara.add(new Chunk(logo, 0, 0));
                logoPara.setAlignment(Element.ALIGN_CENTER);
                headerCell.addElement(logoPara);
            } catch (Exception ex) {
                // If logo fails, add company name instead
                Paragraph companyName = new Paragraph("VOS – VOTRE OUTIL DE SUCCÈS", fontTitle);
                companyName.setAlignment(Element.ALIGN_CENTER);
                companyName.setFont(new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 16,
                        com.lowagie.text.Font.BOLD, java.awt.Color.WHITE));
                headerCell.addElement(companyName);
            }

            headerTable.addCell(headerCell);
            doc.add(headerTable);

            // ── Document title ──────────────────────────────────────────────
            Paragraph title = new Paragraph("CONTRAT DE TRAVAIL", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(10);
            title.setSpacingAfter(2);
            doc.add(title);

            // ── Generation date ──────────────────────────────────────────────
            Paragraph dateGen = new Paragraph("Généré le " +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM yyyy", java.util.Locale.FRANCE)),
                    fontSubtitle);
            dateGen.setAlignment(Element.ALIGN_CENTER);
            dateGen.setSpacingAfter(5);
            doc.add(dateGen);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Adds formal introduction paragraph with French legal phrasing. */
    private void addIntroductionParagraph(com.lowagie.text.Document doc, com.lowagie.text.Font fontBody,
            ContratRow contrat) throws com.lowagie.text.DocumentException {
        String intro = "Entre les soussignés,\n" +
                "La société VOS – Votre Outil de Succès, représentée par ses organes de direction légaux et "
                + "dûment habilitée, d'une part,\n" +
                "Et l'employé(e) référencé sous le N° de recrutement #" + contrat.idRecrutementProperty().get()
                + ", d'autre part,\n\n" +
                "Il a été librement et consciemment convenu ce qui suit :";

        Paragraph introPara = new Paragraph(intro, fontBody);
        introPara.setAlignment(Element.ALIGN_JUSTIFIED);
        introPara.setLeading(12f);
        introPara.setSpacingAfter(4);
        doc.add(introPara);
    }

    /** Article 1: Nature du contrat */
    private void addArticle1(com.lowagie.text.Document doc, com.lowagie.text.Font fontArticle,
            com.lowagie.text.Font fontBody, ContratRow contrat) throws com.lowagie.text.DocumentException {
        Paragraph article = new Paragraph();
        article.add(new Chunk("Article 1 – Nature du Contrat", fontArticle));
        article.setSpacingBefore(3);
        article.setSpacingAfter(2);
        doc.add(article);

        String content = "Le présent contrat est établi en tant que contrat " + contrat.typeProperty().get()
                + " (référence contrat N° " + contrat.idProperty().get()
                + "). Il définit les obligations réciproques et les conditions d'emploi applicables.";
        Paragraph text = new Paragraph(content, fontBody);
        text.setAlignment(Element.ALIGN_JUSTIFIED);
        text.setLeading(11f);
        text.setSpacingAfter(3);
        doc.add(text);
    }

    /** Article 2: Durée (période, dates) */
    private void addArticle2(com.lowagie.text.Document doc, com.lowagie.text.Font fontArticle,
            com.lowagie.text.Font fontBody, ContratRow contrat) throws com.lowagie.text.DocumentException {
        Paragraph article = new Paragraph();
        article.add(new Chunk("Article 2 – Durée du Contrat", fontArticle));
        article.setSpacingBefore(3);
        article.setSpacingAfter(2);
        doc.add(article);

        String dateFin = (contrat.dateFinProperty().get() == null || contrat.dateFinProperty().get().isEmpty())
                ? "non définie"
                : contrat.dateFinProperty().get();

        String content = "La date de début de l'employement est fixée au " + contrat.dateDebutProperty().get()
                + ". La date de fin du contrat est prévue au " + dateFin
                + ". La période couverte par le présent contrat est : " + contrat.periodeProperty().get() + ".";
        Paragraph text = new Paragraph(content, fontBody);
        text.setAlignment(Element.ALIGN_JUSTIFIED);
        text.setLeading(11f);
        text.setSpacingAfter(3);
        doc.add(text);
    }

    /** Article 3: Rémunération (salaire brut) */
    private void addArticle3(com.lowagie.text.Document doc, com.lowagie.text.Font fontArticle,
            com.lowagie.text.Font fontBody, ContratRow contrat) throws com.lowagie.text.DocumentException {
        Paragraph article = new Paragraph();
        article.add(new Chunk("Article 3 – Rémunération", fontArticle));
        article.setSpacingBefore(3);
        article.setSpacingAfter(2);
        doc.add(article);

        String content = "La rémunération brute mensuelle garantie est fixée à "
                + String.format("%.2f DT", contrat.salaireProperty().get())
                + " (Dinars Tunisiens). Cette rémunération est payable selon les modalités légales en vigueur et comprend les contributions sociales obligatoires.";
        Paragraph text = new Paragraph(content, fontBody);
        text.setAlignment(Element.ALIGN_JUSTIFIED);
        text.setLeading(11f);
        text.setSpacingAfter(3);
        doc.add(text);
    }

    /** Article 4: Conditions (volume horaire, statut) */
    private void addArticle4(com.lowagie.text.Document doc, com.lowagie.text.Font fontArticle,
            com.lowagie.text.Font fontBody, ContratRow contrat) throws com.lowagie.text.DocumentException {
        Paragraph article = new Paragraph();
        article.add(new Chunk("Article 4 – Conditions de Travail", fontArticle));
        article.setSpacingBefore(3);
        article.setSpacingAfter(2);
        doc.add(article);

        String content = "Le volume horaire convenu est de " + contrat.volumeHoraireProperty().get()
                + " heures par semaine. Le statut de l'employé(e) est défini comme : "
                + contrat.statusProperty().get()
                + ". L'employé(e) accepte de respecter le règlement intérieur de la société et les dispositions légales en matière de droit du travail.";
        Paragraph text = new Paragraph(content, fontBody);
        text.setAlignment(Element.ALIGN_JUSTIFIED);
        text.setLeading(11f);
        text.setSpacingAfter(3);
        doc.add(text);
    }

    /** Article 5: Avantages */
    private void addArticle5(com.lowagie.text.Document doc, com.lowagie.text.Font fontArticle,
            com.lowagie.text.Font fontBody, ContratRow contrat) throws com.lowagie.text.DocumentException {
        Paragraph article = new Paragraph();
        article.add(new Chunk("Article 5 – Avantages et Bénéfices", fontArticle));
        article.setSpacingBefore(3);
        article.setSpacingAfter(2);
        doc.add(article);

        String avantages = (contrat.avantagesProperty().get() == null || contrat.avantagesProperty().get().isEmpty())
                ? "Aucun avantage spécifique n'est prévu au-delà des garanties légales."
                : "Les avantages sociaux accordés comprennent : " + contrat.avantagesProperty().get();

        String content = avantages
                + " L'employeur s'engage à respecter les obligations légales en matière de couverture sociale et de congés payés.";
        Paragraph text = new Paragraph(content, fontBody);
        text.setAlignment(Element.ALIGN_JUSTIFIED);
        text.setLeading(11f);
        text.setSpacingAfter(3);
        doc.add(text);
    }

    /** Adds signature block with company and employee sections. */
    private void addSignatureBlock(com.lowagie.text.Document doc, com.lowagie.text.Font fontLabel,
            com.lowagie.text.Font fontBody, com.lowagie.text.Font fontSmallGray, ContratRow contrat)
            throws com.lowagie.text.DocumentException {
        doc.add(new Paragraph(" "));

        // ── Signature section title ──────────────────────────────────────────
        Paragraph sigTitle = new Paragraph("SIGNATURES", fontLabel);
        sigTitle.setAlignment(Element.ALIGN_CENTER);
        sigTitle.setSpacingBefore(5);
        sigTitle.setSpacingAfter(8);
        doc.add(sigTitle);

        // ── Create two-column signature table ─────────────────────────────────
        PdfPTable sigTable = new PdfPTable(2);
        sigTable.setWidthPercentage(100);
        sigTable.setWidths(new float[] { 1f, 1f });
        sigTable.setSpacingBefore(2);

        // ── Left column: For the company ─────────────────────────────────────
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
        leftCell.setPadding(5);

        Paragraph companyLabel = new Paragraph("Pour la société", fontLabel);
        companyLabel.setAlignment(Element.ALIGN_CENTER);
        companyLabel.setSpacingAfter(3);
        leftCell.addElement(companyLabel);

        // Add company signature image
        try {
            String sigPath = "src/main/resources/Images/signature.png";
            com.lowagie.text.Image sig = com.lowagie.text.Image.getInstance(sigPath);
            sig.setAlignment(Element.ALIGN_CENTER);
            sig.scaleToFit(80, 40);
            Paragraph sigPara = new Paragraph();
            sigPara.add(new Chunk(sig, 0, 0));
            sigPara.setAlignment(Element.ALIGN_CENTER);
            sigPara.setSpacingAfter(3);
            leftCell.addElement(sigPara);

            // Add cachet image
            String cachetPath = "src/main/resources/Images/cachet.png";
            com.lowagie.text.Image cachet = com.lowagie.text.Image.getInstance(cachetPath);
            cachet.setAlignment(Element.ALIGN_CENTER);
            cachet.scaleToFit(99, 99);
            Paragraph cachetPara = new Paragraph();
            cachetPara.add(new Chunk(cachet, 0, 0));
            cachetPara.setAlignment(Element.ALIGN_CENTER);
            leftCell.addElement(cachetPara);

        } catch (Exception e) {
            Paragraph placeholder = new Paragraph("[Signature et Cachet]", fontBody);
            placeholder.setAlignment(Element.ALIGN_CENTER);
            placeholder.setSpacingBefore(15);
            placeholder.setSpacingAfter(15);
            leftCell.addElement(placeholder);
        }

        // ── Right column: For the employee ───────────────────────────────────
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
        rightCell.setPadding(5);

        Paragraph employeeLabel = new Paragraph("L'employé(e)", fontLabel);
        employeeLabel.setAlignment(Element.ALIGN_CENTER);
        employeeLabel.setSpacingAfter(3);
        rightCell.addElement(employeeLabel);

        // Employee signature image
        try {
            String clientSigPath = "src/main/resources/Images/client.png";
            com.lowagie.text.Image clientSig = com.lowagie.text.Image.getInstance(clientSigPath);
            clientSig.setAlignment(Element.ALIGN_CENTER);
            clientSig.scaleToFit(100, 50);
            Paragraph clientSigPara = new Paragraph();
            clientSigPara.add(new Chunk(clientSig, 0, 0));
            clientSigPara.setAlignment(Element.ALIGN_CENTER);
            clientSigPara.setSpacingBefore(15);
            clientSigPara.setSpacingAfter(2);
            rightCell.addElement(clientSigPara);
        } catch (Exception e) {
            // Fallback to signature line if image not found
            Paragraph signatureLine = new Paragraph(
                    "_____________________________________________________", fontBody);
            signatureLine.setAlignment(Element.ALIGN_CENTER);
            signatureLine.setSpacingBefore(20);
            signatureLine.setSpacingAfter(2);
            rightCell.addElement(signatureLine);
        }

        // Employee name or reference
        Paragraph employeeName = new Paragraph("N° Recrutement : #" + contrat.idRecrutementProperty().get(),
                fontBody);
        employeeName.setAlignment(Element.ALIGN_CENTER);
        rightCell.addElement(employeeName);

        sigTable.addCell(leftCell);
        sigTable.addCell(rightCell);
        doc.add(sigTable);

        // ── Electronic signature hash ────────────────────────────────────────
        String sig = generateContractSignature(contrat.idProperty().get());
        Paragraph hashPara = new Paragraph("Signature électronique : " + sig, fontSmallGray);
        hashPara.setAlignment(Element.ALIGN_CENTER);
        hashPara.setSpacingBefore(3);
        doc.add(hashPara);
    }

    // Inner classes for table data
    public static class ContratRow {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty type;
        private final SimpleStringProperty dateDebut;
        private final SimpleStringProperty dateFin;
        private final SimpleStringProperty periode;
        private final SimpleDoubleProperty salaire;
        private final SimpleStringProperty status;
        private final SimpleStringProperty volumeHoraire;
        private final SimpleStringProperty avantages;
        private final SimpleIntegerProperty idRecrutement;

        public ContratRow(Integer id, String type, String dateDebut, String dateFin, Double salaire, String status,
                String volumeHoraire, String avantages, Integer idRecrutement, String periode) {
            this.id = new SimpleIntegerProperty(id);
            this.type = new SimpleStringProperty(type);
            this.dateDebut = new SimpleStringProperty(dateDebut);
            this.dateFin = new SimpleStringProperty(dateFin);
            this.periode = new SimpleStringProperty(periode);
            this.salaire = new SimpleDoubleProperty(salaire);
            this.status = new SimpleStringProperty(status);
            this.volumeHoraire = new SimpleStringProperty(volumeHoraire);
            this.avantages = new SimpleStringProperty(avantages);
            this.idRecrutement = new SimpleIntegerProperty(idRecrutement);
        }

        public SimpleIntegerProperty idProperty() {
            return id;
        }

        public SimpleStringProperty typeProperty() {
            return type;
        }

        public SimpleStringProperty dateDebutProperty() {
            return dateDebut;
        }

        public SimpleStringProperty dateFinProperty() {
            return dateFin;
        }

        public SimpleStringProperty periodeProperty() {
            return periode;
        }

        public SimpleDoubleProperty salaireProperty() {
            return salaire;
        }

        public SimpleStringProperty statusProperty() {
            return status;
        }

        public SimpleStringProperty volumeHoraireProperty() {
            return volumeHoraire;
        }

        public SimpleStringProperty avantagesProperty() {
            return avantages;
        }

        public SimpleIntegerProperty idRecrutementProperty() {
            return idRecrutement;
        }
    }

}
