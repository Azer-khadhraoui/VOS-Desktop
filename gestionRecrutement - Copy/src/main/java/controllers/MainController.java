package controllers;

import entities.Contrat;
import entities.Recrutement;
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

import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.List;

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
    private TableColumn<ContratRow, Double> colSalaire;
    @FXML
    private TableColumn<ContratRow, Integer> colIdRecrutementContrat;
    @FXML
    private TableColumn<ContratRow, Void> colActionsContrat;

    // Recrutements Table
    @FXML
    private TableView<RecrutementRow> tableRecrutements;
    @FXML
    private TableColumn<RecrutementRow, Integer> colIdRecrutement;
    @FXML
    private TableColumn<RecrutementRow, String> colDateDecision;
    @FXML
    private TableColumn<RecrutementRow, String> colDecisionFinale;
    @FXML
    private TableColumn<RecrutementRow, Integer> colIdEntretien;
    @FXML
    private TableColumn<RecrutementRow, Integer> colIdUtilisateur;
    @FXML
    private TableColumn<RecrutementRow, Void> colActionsRecrutement;

    private ObservableList<ContratRow> contratData = FXCollections.observableArrayList();
    private ObservableList<RecrutementRow> recrutementData = FXCollections.observableArrayList();

    // Services
    private ServiceContrat serviceContrat = new ServiceContrat();
    private ServiceRecrutement serviceRecrutement = new ServiceRecrutement();

    // Modal states
    private ContratRow selectedContratRow = null;
    private RecrutementRow selectedRecrutementRow = null;
    
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
        if (sidebar == null) return;
        
        // Hide section titles initially (collapsed state)
        Label[] sectionLabels = {lblDashboardSection, lblGestionsSection, lblStatistiquesSection};
        for (Label label : sectionLabels) {
            if (label != null) {
                label.setVisible(false);
                label.setManaged(false);
            }
        }
        
        // Hide nav item labels initially
        Label[] labels = {navDashboardText, navLogoutText};
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
        if (gestionsPane != null) gestionsPane.setText("");
        if (statsPane != null) statsPane.setText("");
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
                new KeyValue(sidebar.minWidthProperty(), 240.0, Interpolator.EASE_OUT)
            )
        );
        sidebarAnimation.play();
        
        // Show section titles
        Label[] sectionLabels = {lblDashboardSection, lblGestionsSection, lblStatistiquesSection};
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
        Label[] labels = {navDashboardText, navLogoutText};
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
        if (gestionsPane != null) gestionsPane.setText("Gestions");
        if (statsPane != null) statsPane.setText("Statistiques");
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
        Label[] sectionLabels = {lblDashboardSection, lblGestionsSection, lblStatistiquesSection};
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
        Label[] labels = {navDashboardText, navLogoutText};
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
        if (gestionsPane != null) gestionsPane.setText("");
        if (statsPane != null) statsPane.setText("");
        
        // Animate width collapse: 240px -> 60px
        sidebarAnimation = new Timeline(
            new KeyFrame(Duration.millis(300),
                new KeyValue(sidebar.prefWidthProperty(), 60.0, Interpolator.EASE_IN),
                new KeyValue(sidebar.minWidthProperty(), 60.0, Interpolator.EASE_IN)
            )
        );
        sidebarAnimation.play();
    }

    private void setNavItemActive(HBox activeItem) {
        // Remove active class from all items
        if (navDashboard != null) navDashboard.getStyleClass().remove("sidebar-nav-item-active");
        
        // Add active class to current item
        if (activeItem != null && !activeItem.getStyleClass().contains("sidebar-nav-item-active")) {
            activeItem.getStyleClass().add("sidebar-nav-item-active");
        }
    }

    private void initializeContratTable() {
        // Add icons and text to column headers
        colIdContrat.setText("# ID");
        colTypeContrat.setText("📋 Type");
        colDateDebut.setText("📅 Date Début");
        colSalaire.setText("💰 Salaire");
        colIdRecrutementContrat.setText("👤 Recrutement");
        colActionsContrat.setText("⚙️ Actions");
        
        colIdContrat.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        colTypeContrat.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        colDateDebut.setCellValueFactory(cellData -> cellData.getValue().dateDebutProperty());
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
                        case "cdi": icon = "💼 "; bgColor = "#DBEAFE"; textColor = "#0369A1"; break;
                        case "cdd": icon = "📝 "; bgColor = "#FEF3C7"; textColor = "#92400E"; break;
                        case "stage": icon = "🎓 "; bgColor = "#D1FAE5"; textColor = "#065F46"; break;
                        case "freelance": icon = "🚀 "; bgColor = "#FCD34D"; textColor = "#78350F"; break;
                        case "alternance": icon = "🔄 "; bgColor = "#F3E8FF"; textColor = "#6B21A8"; break;
                    }
                    
                    badge.setText(icon + value);
                    badge.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: " + textColor + "; " +
                            "-fx-padding: 6 12; -fx-background-radius: 20; -fx-font-weight: 600; -fx-font-size: 12px;");
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
        
        // Configure Actions column with styled buttons
        colActionsContrat.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button();
            private final Button btnDelete = new Button();
            private final HBox hbox = new HBox(8, btnEdit, btnDelete);

            {
                // Create image views for buttons
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
                
                // Style for both buttons - transparent with icons only
                String buttonStyle = "-fx-padding: 4 4; -fx-cursor: hand; " +
                        "-fx-background-color: transparent; " +
                        "-fx-border-color: transparent; -fx-border-width: 0;";
                
                btnEdit.setStyle(buttonStyle);
                btnDelete.setStyle(buttonStyle);
                
                // Add tooltips
                Tooltip editTooltip = new Tooltip("Modifier le contrat");
                editTooltip.setStyle("-fx-font-size: 11px;");
                Tooltip.install(btnEdit, editTooltip);
                
                Tooltip deleteTooltip = new Tooltip("Supprimer le contrat");
                deleteTooltip.setStyle("-fx-font-size: 11px;");
                Tooltip.install(btnDelete, deleteTooltip);
                
                // Shared background container with transparency
                hbox.setStyle("-fx-background-color: rgba(150, 171, 241, 0.52); " +
                        "-fx-padding: 4 4; -fx-border-radius: 4; -fx-background-radius: 4;");
                hbox.setAlignment(Pos.CENTER);
                hbox.setPrefWidth(70);

                btnEdit.setOnAction(event -> {
                    ContratRow contrat = getTableView().getItems().get(getIndex());
                    editContrat(contrat);
                });

                btnDelete.setOnAction(event -> {
                    ContratRow contrat = getTableView().getItems().get(getIndex());
                    deleteContrat(contrat);
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
                    setOnMouseEntered(e -> setStyle("-fx-padding: 8px; -fx-background-radius: 4; -fx-background-color: #F9FAFB;"));
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
        // Add icons and text to column headers
        colIdRecrutement.setText("# ID");
        colDateDecision.setText("📅 Date Décision");
        colDecisionFinale.setText("✓ Décision");
        colIdEntretien.setText("🎤 Entretien");
        colIdUtilisateur.setText("👤 Utilisateur");
        colActionsRecrutement.setText("⚙️ Actions");
        
        colIdRecrutement.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        colDateDecision.setCellValueFactory(cellData -> cellData.getValue().dateDecisionProperty());
        colDecisionFinale.setCellValueFactory(cellData -> cellData.getValue().decisionProperty());
        colIdEntretien.setCellValueFactory(cellData -> cellData.getValue().idEntretienProperty().asObject());
        colIdUtilisateur.setCellValueFactory(cellData -> cellData.getValue().idUtilisateurProperty().asObject());
        
        // Style Decision column with badge colors and icons
        colDecisionFinale.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    String icon = "";
                    String bgColor = "#F3F4F6";
                    String textColor = "#6B7280";
                    
                    // Color badge according to decision with icons
                    switch (item.toLowerCase()) {
                        case "accepté":
                            icon = "✓ ";
                            bgColor = "#D1FAE5";
                            textColor = "#059669";
                            break;
                        case "refusé":
                            icon = "✕ ";
                            bgColor = "#FEE2E2";
                            textColor = "#DC2626";
                            break;
                        case "en attente":
                            icon = "⏳ ";
                            bgColor = "#FEF3C7";
                            textColor = "#92400E";
                            break;
                    }
                    
                    badge.setText(icon + item);
                    badge.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: " + textColor + "; " +
                            "-fx-padding: 6 12; -fx-background-radius: 20; -fx-font-weight: 600; -fx-font-size: 12px;");
                    setGraphic(badge);
                }
            }
        });
        
        // Style ID columns
        colIdEntretien.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Integer value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText("🎤 #" + value);
                    setStyle("-fx-text-fill: #6366F1;");
                }
            }
        });
        
        colIdUtilisateur.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Integer value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText("👤 #" + value);
                    setStyle("-fx-text-fill: #8B5CF6;");
                }
            }
        });
        
        // Configure Actions column with styled buttons
        colActionsRecrutement.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button();
            private final Button btnDelete = new Button();
            private final HBox hbox = new HBox(8, btnEdit, btnDelete);

            {
                // Create image views for buttons
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
                
                // Style for both buttons - transparent with icons only
                String buttonStyle = "-fx-padding: 4 4; -fx-cursor: hand; " +
                        "-fx-background-color: transparent; " +
                        "-fx-border-color: transparent; -fx-border-width: 0;";
                
                btnEdit.setStyle(buttonStyle);
                btnDelete.setStyle(buttonStyle);
                
                // Add tooltips
                Tooltip editTooltip = new Tooltip("Modifier le recrutement");
                editTooltip.setStyle("-fx-font-size: 11px;");
                Tooltip.install(btnEdit, editTooltip);
                
                Tooltip deleteTooltip = new Tooltip("Supprimer le recrutement");
                deleteTooltip.setStyle("-fx-font-size: 11px;");
                Tooltip.install(btnDelete, deleteTooltip);
                
                // Shared background container with transparency
                hbox.setStyle("-fx-background-color: rgba(150, 171, 241, 0.52); " +
                        "-fx-padding: 4 4; -fx-border-radius: 4; -fx-background-radius: 4;");
                hbox.setAlignment(Pos.CENTER);
                hbox.setPrefWidth(70);

                btnEdit.setOnAction(event -> {
                    RecrutementRow recrutement = getTableView().getItems().get(getIndex());
                    editRecrutement(recrutement);
                });

                btnDelete.setOnAction(event -> {
                    RecrutementRow recrutement = getTableView().getItems().get(getIndex());
                    deleteRecrutement(recrutement);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });
        
        // Add row styling with hover effect
        tableRecrutements.setRowFactory(param -> new TableRow<>() {
            @Override
            protected void updateItem(RecrutementRow item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    setStyle("-fx-padding: 8px; -fx-background-radius: 4;");
                    setOnMouseEntered(e -> setStyle("-fx-padding: 8px; -fx-background-radius: 4; -fx-background-color: #F9FAFB;"));
                    setOnMouseExited(e -> setStyle("-fx-padding: 8px; -fx-background-radius: 4;"));
                }
            }
        });
        
        // Add sample data
        loadSampleRecrutementData();
        tableRecrutements.setItems(recrutementData);
        
        // Handle empty table
        if (recrutementData.isEmpty()) {
            tableRecrutements.setPlaceholder(createEmptyPlaceholder("Aucun recrutement trouvé", "📋"));
        }
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
                contratData.add(new ContratRow(c.getId_contrat(), c.getType_contrat(), c.getDate_debut().toString(), c.getSalaire(), c.getId_recrutement()));
            }
        } catch (SQLException e) {
            showError("Erreur lors du chargement des contrats: " + e.getMessage());
        }
    }

    private void loadSampleRecrutementData() {
        try {
            recrutementData.clear();
            for (Recrutement r : serviceRecrutement.afficher()) {
                recrutementData.add(new RecrutementRow(r.getId_recrutement(), r.getDate_decision().toString(), r.getDecision_finale(), r.getId_entretien(), r.getId_utilisateur()));
            }
        } catch (SQLException e) {
            showError("Erreur lors du chargement des recrutements: " + e.getMessage());
        }
    }

    private void hideAllPages() {
        if (pageRecrutement != null) { pageRecrutement.setVisible(false); pageRecrutement.setManaged(false); }
        if (pageDashboard != null) { pageDashboard.setVisible(false); pageDashboard.setManaged(false); }
        if (pageOffres != null) { pageOffres.setVisible(false); pageOffres.setManaged(false); }
        if (pageEntretiens != null) { pageEntretiens.setVisible(false); pageEntretiens.setManaged(false); }
        if (pageAdministration != null) { pageAdministration.setVisible(false); pageAdministration.setManaged(false); }
        if (pageCandidats != null) { pageCandidats.setVisible(false); pageCandidats.setManaged(false); }
        if (pageStatEntretiens != null) { pageStatEntretiens.setVisible(false); pageStatEntretiens.setManaged(false); }
        if (pageStatOffres != null) { pageStatOffres.setVisible(false); pageStatOffres.setManaged(false); }
        if (pageStatRecrutements != null) { pageStatRecrutements.setVisible(false); pageStatRecrutements.setManaged(false); }
        if (pageUtilisateurs != null) { pageUtilisateurs.setVisible(false); pageUtilisateurs.setManaged(false); }
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
                typeError.setStyle("-fx-font-size: 10px; -fx-text-fill: #10B981; -fx-padding: 2 0; -fx-font-weight: bold;");
                typeCombo.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(16,185,129,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            } else {
                typeError.setText("Ce champ est obligatoire");
                typeError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
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
        dateDebut.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                "-fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; " +
                "-fx-font-size: 13px; -fx-text-fill: white;");
        dateDebut.setPrefWidth(300);
        
        Label dateError = new Label("");
        dateError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0;");
        
        dateDebut.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                dateError.setText("✓ Format valide");
                dateError.setStyle("-fx-font-size: 10px; -fx-text-fill: #10B981; -fx-padding: 2 0; -fx-font-weight: bold;");
                dateDebut.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(16,185,129,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            } else {
                dateError.setText("Ce champ est obligatoire");
                dateError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                dateDebut.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            }
        });
        
        VBox dateBox = new VBox(lblDate, dateDebut, dateError);
        dateBox.setSpacing(3);
        
        // Salaire
        Label lblSalaire = new Label("SALAIRE");
        lblSalaire.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");
        
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
                salaireError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                salaire.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white; -fx-prompt-text-fill: #6B7280;");
            } else {
                try {
                    double val = Double.parseDouble(newVal);
                    if (val <= 0) {
                        salaireError.setText("Le salaire doit être positif");
                        salaireError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                        salaire.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                                "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                                "-fx-font-size: 13px; -fx-text-fill: white; -fx-prompt-text-fill: #6B7280;");
                    } else {
                        salaireError.setText("✓ Format valide");
                        salaireError.setStyle("-fx-font-size: 10px; -fx-text-fill: #10B981; -fx-padding: 2 0; -fx-font-weight: bold;");
                        salaire.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                                "-fx-border-color: rgba(16,185,129,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                                "-fx-font-size: 13px; -fx-text-fill: white; -fx-prompt-text-fill: #6B7280;");
                    }
                } catch (NumberFormatException ex) {
                    salaireError.setText("Format invalide (nombre positif requis)");
                    salaireError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
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
        lblRecrutement.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");
        
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
                    items.add(r.getId_recrutement() + " - " + r.getDecision_finale() + " (" + r.getDate_decision() + ")");
                }
            }
            recrutementCombo.setItems(items);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des recrutements: " + e.getMessage());
        }
        
        recrutementCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                recrutementError.setText("✓ Sélection valide");
                recrutementError.setStyle("-fx-font-size: 10px; -fx-text-fill: #10B981; -fx-padding: 2 0; -fx-font-weight: bold;");
                recrutementCombo.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(16,185,129,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            } else {
                recrutementError.setText("Ce champ est obligatoire");
                recrutementError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                recrutementCombo.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            }
        });
        
        VBox recrutementBox = new VBox(lblRecrutement, recrutementCombo, recrutementError);
        recrutementBox.setSpacing(3);
        
        formFields.getChildren().addAll(typeBox, dateBox, salaireBox, recrutementBox);
        
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
                    typeError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    return;
                }
                
                if (dateDebut.getValue() == null) {
                    dateError.setText("Ce champ est obligatoire");
                    dateError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    return;
                }
                
                if (salaire.getText().isEmpty()) {
                    salaireError.setText("Ce champ est obligatoire");
                    salaireError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    return;
                }
                
                try {
                    double salaireVal = Double.parseDouble(salaire.getText());
                    if (salaireVal <= 0) {
                        salaireError.setText("Le salaire doit être positif");
                        salaireError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    salaireError.setText("Format invalide (nombre positif requis)");
                    salaireError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    return;
                }
                
                if (recrutementCombo.getValue() == null || recrutementCombo.getValue().isEmpty()) {
                    recrutementError.setText("Ce champ est obligatoire");
                    recrutementError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    return;
                }
                
                String selectedRecrutement = recrutementCombo.getValue();
                int idRecrutement = Integer.parseInt(selectedRecrutement.split(" - ")[0]);
                
                Contrat c = new Contrat(
                    typeCombo.getValue(),
                    Date.valueOf(dateDebut.getValue()),
                    Double.parseDouble(salaire.getText()),
                    idRecrutement
                );
                
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
        lblDateDecision.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");
        
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
                dateError.setStyle("-fx-font-size: 10px; -fx-text-fill: #10B981; -fx-padding: 2 0; -fx-font-weight: bold;");
                dateDecision.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(16,185,129,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            } else {
                dateError.setText("Ce champ est obligatoire");
                dateError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                dateDecision.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            }
        });
        
        VBox dateBox = new VBox(lblDateDecision, dateDecision, dateError);
        dateBox.setSpacing(3);
        
        // Décision
        Label lblDecision = new Label("DÉCISION");
        lblDecision.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");
        
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
                decisionError.setStyle("-fx-font-size: 10px; -fx-text-fill: #10B981; -fx-padding: 2 0; -fx-font-weight: bold;");
                decisionCombo.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(16,185,129,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            } else {
                decisionError.setText("Ce champ est obligatoire");
                decisionError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                decisionCombo.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            }
        });
        
        VBox decisionBox = new VBox(lblDecision, decisionCombo, decisionError);
        decisionBox.setSpacing(3);
        
        // ID Entretien
        Label lblIdEntretien = new Label("ID ENTRETIEN");
        lblIdEntretien.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");
        
        ComboBox<Integer> idEntretien = new ComboBox<>();
        try {
            ObservableList<Integer> entretienIds = FXCollections.observableArrayList(serviceRecrutement.getAvailableEntretienIds());
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
                entretienError.setStyle("-fx-font-size: 10px; -fx-text-fill: #10B981; -fx-padding: 2 0; -fx-font-weight: bold;");
                idEntretien.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(16,185,129,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            } else {
                entretienError.setText("Ce champ est obligatoire");
                entretienError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                idEntretien.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            }
        });
        
        VBox entretienBox = new VBox(lblIdEntretien, idEntretien, entretienError);
        entretienBox.setSpacing(3);
        
        // ID Utilisateur
        Label lblIdUtilisateur = new Label("ID UTILISATEUR");
        lblIdUtilisateur.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");
        
        ComboBox<Integer> idUtilisateur = new ComboBox<>();
        try {
            ObservableList<Integer> utilisateurIds = FXCollections.observableArrayList(serviceRecrutement.getAvailableUtilisateurIds());
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
                utilisateurError.setStyle("-fx-font-size: 10px; -fx-text-fill: #10B981; -fx-padding: 2 0; -fx-font-weight: bold;");
                idUtilisateur.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(16,185,129,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            } else {
                utilisateurError.setText("Ce champ est obligatoire");
                utilisateurError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
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
                    dateError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    return;
                }
                
                if (decisionCombo.getValue() == null || decisionCombo.getValue().isEmpty()) {
                    decisionError.setText("Ce champ est obligatoire");
                    decisionError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    return;
                }
                
                if (idEntretien.getValue() == null) {
                    entretienError.setText("Ce champ est obligatoire");
                    entretienError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    return;
                }
                
                if (idUtilisateur.getValue() == null) {
                    utilisateurError.setText("Ce champ est obligatoire");
                    utilisateurError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    return;
                }
                
                Recrutement r = new Recrutement(
                    Date.valueOf(dateDecision.getValue()),
                    decisionCombo.getValue(),
                    idEntretien.getValue(),
                    idUtilisateur.getValue()
                );
                
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
                typeError.setStyle("-fx-font-size: 10px; -fx-text-fill: #10B981; -fx-padding: 2 0; -fx-font-weight: bold;");
                typeCombo.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(16,185,129,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            } else {
                typeError.setText("Ce champ est obligatoire");
                typeError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
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
        dateDebut.setValue(java.time.LocalDate.parse(contratRow.dateDebutProperty().get()));
        dateDebut.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                "-fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; " +
                "-fx-font-size: 13px; -fx-text-fill: white;");
        dateDebut.setPrefWidth(300);
        
        Label dateError = new Label("");
        dateError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0;");
        
        dateDebut.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                dateError.setText("✓ Format valide");
                dateError.setStyle("-fx-font-size: 10px; -fx-text-fill: #10B981; -fx-padding: 2 0; -fx-font-weight: bold;");
                dateDebut.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(16,185,129,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            } else {
                dateError.setText("Ce champ est obligatoire");
                dateError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                dateDebut.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            }
        });
        
        VBox dateBox = new VBox(lblDate, dateDebut, dateError);
        dateBox.setSpacing(3);
        
        // Salaire
        Label lblSalaire = new Label("SALAIRE");
        lblSalaire.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");
        
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
                salaireError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                salaire.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            } else {
                try {
                    double val = Double.parseDouble(newVal);
                    if (val <= 0) {
                        salaireError.setText("Le salaire doit être positif");
                        salaireError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                        salaire.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                                "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                                "-fx-font-size: 13px; -fx-text-fill: white;");
                    } else {
                        salaireError.setText("✓ Format valide");
                        salaireError.setStyle("-fx-font-size: 10px; -fx-text-fill: #10B981; -fx-padding: 2 0; -fx-font-weight: bold;");
                        salaire.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                                "-fx-border-color: rgba(16,185,129,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                                "-fx-font-size: 13px; -fx-text-fill: white;");
                    }
                } catch (NumberFormatException ex) {
                    salaireError.setText("Format invalide (nombre positif requis)");
                    salaireError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    salaire.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                            "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                            "-fx-font-size: 13px; -fx-text-fill: white;");
                }
            }
        });
        
        VBox salaireBox = new VBox(lblSalaire, salaire, salaireError);
        salaireBox.setSpacing(3);
        
        // Recrutement
        Label lblRecrutement = new Label("RECRUTEMENT");
        lblRecrutement.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");
        
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
                    String item = r.getId_recrutement() + " - " + r.getDecision_finale() + " (" + r.getDate_decision() + ")";
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
                recrutementError.setStyle("-fx-font-size: 10px; -fx-text-fill: #10B981; -fx-padding: 2 0; -fx-font-weight: bold;");
                recrutementCombo.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(16,185,129,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            } else {
                recrutementError.setText("Ce champ est obligatoire");
                recrutementError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                recrutementCombo.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                        "-fx-border-color: rgba(239,68,68,0.5); -fx-border-radius: 10; -fx-padding: 12 14; " +
                        "-fx-font-size: 13px; -fx-text-fill: white;");
            }
        });
        
        VBox recrutementBox = new VBox(lblRecrutement, recrutementCombo, recrutementError);
        recrutementBox.setSpacing(3);
        
        formFields.getChildren().addAll(typeBox, dateBox, salaireBox, recrutementBox);
        
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
                    typeError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    return;
                }
                
                if (dateDebut.getValue() == null) {
                    dateError.setText("Ce champ est obligatoire");
                    dateError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    return;
                }
                
                if (salaire.getText().isEmpty()) {
                    salaireError.setText("Ce champ est obligatoire");
                    salaireError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    return;
                }
                
                try {
                    double salaireVal = Double.parseDouble(salaire.getText());
                    if (salaireVal <= 0) {
                        salaireError.setText("Le salaire doit être positif");
                        salaireError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    salaireError.setText("Format invalide (nombre positif requis)");
                    salaireError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    return;
                }
                
                if (recrutementCombo.getValue() == null || recrutementCombo.getValue().isEmpty()) {
                    recrutementError.setText("Ce champ est obligatoire");
                    recrutementError.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-padding: 2 0; -fx-font-weight: bold;");
                    return;
                }
                
                String selectedRecrutement = recrutementCombo.getValue();
                int idRecrutement = Integer.parseInt(selectedRecrutement.split(" - ")[0]);
                
                Contrat c = new Contrat(
                    contratRow.idProperty().get(),
                    typeCombo.getValue(),
                    Date.valueOf(dateDebut.getValue()),
                    Double.parseDouble(salaire.getText()),
                    idRecrutement
                );
                
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

    private void editRecrutement(RecrutementRow recrutementRow) {
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
        lblDateDecision.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");
        
        DatePicker dateDecision = new DatePicker();
        dateDecision.setValue(java.time.LocalDate.parse(recrutementRow.dateDecisionProperty().get()));
        dateDecision.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                "-fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; " +
                "-fx-font-size: 13px; -fx-text-fill: white;");
        dateDecision.setPrefWidth(300);
        
        VBox dateBox = new VBox(lblDateDecision, dateDecision);
        dateBox.setSpacing(5);
        
        // Décision
        Label lblDecision = new Label("DÉCISION");
        lblDecision.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");
        
        ComboBox<String> decisionCombo = new ComboBox<>();
        decisionCombo.setItems(FXCollections.observableArrayList("Accepté", "Refusé", "En attente"));
        decisionCombo.setValue(recrutementRow.decisionProperty().get());
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
        lblIdEntretien.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");
        
        ComboBox<Integer> idEntretien = new ComboBox<>();
        int currentIdEntretien = recrutementRow.idEntretienProperty().get();
        try {
            ObservableList<Integer> entretienIds = FXCollections.observableArrayList(serviceRecrutement.getAvailableEntretienIds());
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
        lblIdUtilisateur.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");
        
        ComboBox<Integer> idUtilisateur = new ComboBox<>();
        int currentIdUtilisateur = recrutementRow.idUtilisateurProperty().get();
        try {
            ObservableList<Integer> utilisateurIds = FXCollections.observableArrayList(serviceRecrutement.getAvailableUtilisateurIds());
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
                    recrutementRow.idProperty().get(),
                    Date.valueOf(dateDecision.getValue()),
                    decisionCombo.getValue(),
                    idEntretien.getValue(),
                    idUtilisateur.getValue()
                );
                
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

    private void deleteRecrutement(RecrutementRow recrutementRow) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmer la suppression");
        confirmDialog.setHeaderText("Supprimer le recrutement #" + recrutementRow.idProperty().get());
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer ce recrutement?");

        confirmDialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    serviceRecrutement.supprimer(recrutementRow.idProperty().get());
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
        if (btnNavEntretiens != null) btnNavEntretiens.setStyle("");
        if (btnNavDashboard != null) btnNavDashboard.setStyle("");
        if (btnNavStats != null) btnNavStats.setStyle("");

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

    // Inner classes for table data
    public static class ContratRow {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty type;
        private final SimpleStringProperty dateDebut;
        private final SimpleDoubleProperty salaire;
        private final SimpleIntegerProperty idRecrutement;

        public ContratRow(Integer id, String type, String dateDebut, Double salaire, Integer idRecrutement) {
            this.id = new SimpleIntegerProperty(id);
            this.type = new SimpleStringProperty(type);
            this.dateDebut = new SimpleStringProperty(dateDebut);
            this.salaire = new SimpleDoubleProperty(salaire);
            this.idRecrutement = new SimpleIntegerProperty(idRecrutement);
        }

        public SimpleIntegerProperty idProperty() { return id; }
        public SimpleStringProperty typeProperty() { return type; }
        public SimpleStringProperty dateDebutProperty() { return dateDebut; }
        public SimpleDoubleProperty salaireProperty() { return salaire; }
        public SimpleIntegerProperty idRecrutementProperty() { return idRecrutement; }
    }

    public static class RecrutementRow {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty dateDecision;
        private final SimpleStringProperty decision;
        private final SimpleIntegerProperty idEntretien;
        private final SimpleIntegerProperty idUtilisateur;

        public RecrutementRow(Integer id, String dateDecision, String decision, Integer idEntretien, Integer idUtilisateur) {
            this.id = new SimpleIntegerProperty(id);
            this.dateDecision = new SimpleStringProperty(dateDecision);
            this.decision = new SimpleStringProperty(decision);
            this.idEntretien = new SimpleIntegerProperty(idEntretien);
            this.idUtilisateur = new SimpleIntegerProperty(idUtilisateur);
        }

        public SimpleIntegerProperty idProperty() { return id; }
        public SimpleStringProperty dateDecisionProperty() { return dateDecision; }
        public SimpleStringProperty decisionProperty() { return decision; }
        public SimpleIntegerProperty idEntretienProperty() { return idEntretien; }
        public SimpleIntegerProperty idUtilisateurProperty() { return idUtilisateur; }
    }
}
