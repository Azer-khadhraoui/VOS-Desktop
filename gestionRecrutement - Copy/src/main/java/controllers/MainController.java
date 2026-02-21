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
import javafx.scene.*;
import javafx.stage.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.*;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Interpolator;
import javafx.util.Duration;
import services.ServiceContrat;
import services.ServiceRecrutement;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
// OpenPDF imports — explicit to avoid TextField clash with javafx.scene.control.TextField
import com.lowagie.text.Chunk;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import javafx.geometry.Insets;
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
    private VBox pageStatRecrutements;
    @FXML
    private VBox pageUtilisateurs;

    // Contrats Table
    @FXML
    private TableView<ContratRow> tableContrats;
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
            private final Button btnPreview = new Button("📄");
            private final Button btnDownload = new Button("⬇");
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

                btnEdit.setGraphic(editIcon);
                btnDelete.setGraphic(deleteIcon);

                String baseStyle = "-fx-padding: 4 4; -fx-cursor: hand; " +
                        "-fx-background-color: transparent; -fx-border-color: transparent; -fx-border-width: 0;";
                btnEdit.setStyle(baseStyle);
                btnDelete.setStyle(baseStyle);

                // ── PDF Preview button ───────────────────────────────────
                btnPreview.setStyle("-fx-padding: 3 7; -fx-cursor: hand; -fx-font-size: 13px; " +
                        "-fx-background-color: #DBEAFE; -fx-background-radius: 5; " +
                        "-fx-border-color: #BFDBFE; -fx-border-width: 1; -fx-border-radius: 5;");
                Tooltip.install(btnPreview, new Tooltip("Aperçu du contrat"));

                // ── PDF Download button ──────────────────────────────────
                btnDownload.setStyle("-fx-padding: 3 7; -fx-cursor: hand; -fx-font-size: 13px; " +
                        "-fx-background-color: #EDE9FE; -fx-background-radius: 5; " +
                        "-fx-border-color: #DDD6FE; -fx-border-width: 1; -fx-border-radius: 5;");
                Tooltip.install(btnDownload, new Tooltip("Télécharger en PDF"));

                // ── Tooltips ─────────────────────────────────────────────
                Tooltip.install(btnEdit, new Tooltip("Modifier le contrat"));
                Tooltip.install(btnDelete, new Tooltip("Supprimer le contrat"));

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
                setGraphic(empty ? null : hbox);
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
            private final HBox hbox = new HBox(8, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().add("btn-action");
                btnDelete.getStyleClass().add("btn-delete");
                hbox.setAlignment(Pos.CENTER_LEFT);

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

                contratData.add(new ContratRow(c.getId_contrat(), c.getType_contrat(), c.getDate_debut().toString(),
                        dateFinStr, c.getSalaire(), autoStatus, c.getVolume_horaire(), c.getAvantages(),
                        c.getId_recrutement(), periode));
            }
        } catch (SQLException e) {
            showError("Erreur lors du chargement des contrats: " + e.getMessage());
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

    /** Saves a real PDF file for the contract using OpenPDF. */
    private void downloadContratPdf(ContratRow contrat) {
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
            // ── Document setup ────────────────────────────────────────────────
            com.lowagie.text.Document doc = new com.lowagie.text.Document(PageSize.A4, 50, 50, 60, 60);
            PdfWriter.getInstance(doc, fos);
            doc.open();

            // ── Fonts ─────────────────────────────────────────────────────────
            com.lowagie.text.Font fontTitle = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 22,
                    com.lowagie.text.Font.BOLD, new java.awt.Color(0x66, 0x7e, 0xea));
            com.lowagie.text.Font fontSubtitle = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10,
                    com.lowagie.text.Font.NORMAL, java.awt.Color.GRAY);
            com.lowagie.text.Font fontSection = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 13,
                    com.lowagie.text.Font.BOLD, new java.awt.Color(0x13, 0x11, 0x14));
            com.lowagie.text.Font fontKey = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10,
                    com.lowagie.text.Font.NORMAL, java.awt.Color.GRAY);
            com.lowagie.text.Font fontVal = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 11,
                    com.lowagie.text.Font.BOLD, new java.awt.Color(0x11, 0x18, 0x27));
            com.lowagie.text.Font fontSig = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 16,
                    com.lowagie.text.Font.BOLDITALIC, new java.awt.Color(0x66, 0x7e, 0xea));

            // ── Header ────────────────────────────────────────────────────────
            Paragraph company = new Paragraph("VOS – Votre Outil de Succès", fontSection);
            company.setAlignment(Element.ALIGN_CENTER);
            doc.add(company);

            Paragraph dateGen = new Paragraph("Généré le : " +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), fontSubtitle);
            dateGen.setAlignment(Element.ALIGN_CENTER);
            doc.add(dateGen);

            doc.add(new Paragraph(" "));
            Paragraph titlePara = new Paragraph("CONTRAT DE TRAVAIL", fontTitle);
            titlePara.setAlignment(Element.ALIGN_CENTER);
            doc.add(titlePara);

            // Divider
            com.lowagie.text.pdf.draw.LineSeparator lineSep = new com.lowagie.text.pdf.draw.LineSeparator();
            lineSep.setLineColor(new java.awt.Color(0x66, 0x7e, 0xea));
            doc.add(new Chunk(lineSep));
            doc.add(new Paragraph(" "));

            // ── Helper to write a section ─────────────────────────────────────
            // Sections: (title, key-value pairs)
            addPdfSection(doc, "Informations du Contrat", fontSection, fontKey, fontVal, new String[][] {
                    { "N° Contrat", "#" + contrat.idProperty().get() },
                    { "Type de contrat", contrat.typeProperty().get() },
                    { "Période", contrat.periodeProperty().get() },
                    { "Date de début", contrat.dateDebutProperty().get() },
                    { "Date de fin", contrat.dateFinProperty().get() },
                    { "Réf. Recrutement", "#" + contrat.idRecrutementProperty().get() }
            });
            addPdfSection(doc, "Conditions", fontSection, fontKey, fontVal, new String[][] {
                    { "Statut", contrat.statusProperty().get() },
                    { "Volume horaire", contrat.volumeHoraireProperty().get() }
            });
            addPdfSection(doc, "Rémunération", fontSection, fontKey, fontVal, new String[][] {
                    { "Salaire brut", String.format("%.2f DT", contrat.salaireProperty().get()) }
            });
            addPdfSection(doc, "Avantages", fontSection, fontKey, fontVal, new String[][] {
                    { "Avantages", contrat.avantagesProperty().get() }
            });

            // ── Signature ─────────────────────────────────────────────────────
            doc.add(new Paragraph(" "));
            String sig = generateContractSignature(contrat.idProperty().get());
            Paragraph sigTitle = new Paragraph("Signature électronique", fontSection);
            doc.add(sigTitle);
            Paragraph sigPara = new Paragraph(sig, fontSig);
            sigPara.setSpacingBefore(4);
            doc.add(sigPara);
            Paragraph sigNote = new Paragraph(
                    "Signature unique générée automatiquement – valeur non modifiable.", fontSubtitle);
            doc.add(sigNote);

            doc.close();
            showAlert("PDF généré", "Contrat enregistré avec succès :\n" + file.getAbsolutePath());

        } catch (Exception e) {
            showError("Erreur lors de la génération PDF : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Writes a labelled section with key-value rows into the PDF. */
    private void addPdfSection(com.lowagie.text.Document doc, String title,
            com.lowagie.text.Font fontSection, com.lowagie.text.Font fontKey, com.lowagie.text.Font fontVal,
            String[][] rows) throws com.lowagie.text.DocumentException {
        Paragraph sectionTitle = new Paragraph(title, fontSection);
        sectionTitle.setSpacingBefore(12);
        sectionTitle.setSpacingAfter(4);
        doc.add(sectionTitle);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 1.8f, 3f });
        table.setSpacingAfter(4);

        for (String[] kv : rows) {
            String val = (kv[1] == null || kv[1].isEmpty()) ? "—" : kv[1];

            PdfPCell cellKey = new PdfPCell(new Phrase(kv[0], fontKey));
            cellKey.setBorder(com.lowagie.text.Rectangle.BOTTOM);
            cellKey.setBorderColor(new java.awt.Color(0xe5, 0xe7, 0xeb));
            cellKey.setPadding(5);
            cellKey.setBackgroundColor(new java.awt.Color(0xf9, 0xfa, 0xfb));

            PdfPCell cellVal = new PdfPCell(new Phrase(val, fontVal));
            cellVal.setBorder(com.lowagie.text.Rectangle.BOTTOM);
            cellVal.setBorderColor(new java.awt.Color(0xe5, 0xe7, 0xeb));
            cellVal.setPadding(5);

            table.addCell(cellKey);
            table.addCell(cellVal);
        }
        doc.add(table);
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
