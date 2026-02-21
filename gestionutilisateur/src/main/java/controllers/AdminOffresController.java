package controllers;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import entities.OffreEmploi;
import services.OffreEmploiService;
import javafx.scene.layout.HBox;
import java.sql.Date;
import java.util.List;
import java.util.Optional;
import javafx.scene.layout.GridPane;
import entities.CritereOffre;
import services.CritereOffreService;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.util.Duration;
import services.AIEnhancementOffreService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import services.StatisticsService;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import java.util.Map;
import javafx.geometry.Pos;
import javafx.scene.layout.Region;




public class AdminOffresController {

    // Top search
    @FXML private TextField searchField;
    
    // User profile elements
    @FXML private StackPane userAvatarContainer;
    @FXML private Label userAvatarLabel;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    
    // Sidebar elements
    @FXML private VBox sidebar;
    @FXML private Label sidebarSubtitle;
    @FXML private HBox navStatistiques;
    @FXML private HBox navOpportunites;
    @FXML private HBox navApropos;
    @FXML private HBox navAdministration;
    @FXML private HBox navParametres;
    @FXML private HBox navDeconnexion;
    @FXML private Label labelStatistiques;
    @FXML private Label labelOpportunites;
    @FXML private Label labelApropos;
    @FXML private Label labelAdministration;
    @FXML private Label labelParametres;
    @FXML private Label labelDeconnexion;
    @FXML private VBox supportSection;

    // Table
    @FXML private TableView<OffreEmploi> offreTable;
    @FXML private TableColumn<OffreEmploi, Integer> colIdOffre;
    @FXML private TableColumn<OffreEmploi, String> colTitre;
    @FXML private TableColumn<OffreEmploi, String> colDescription;
    @FXML private TableColumn<OffreEmploi, String> colTypeContrat;
    @FXML private TableColumn<OffreEmploi, String> colWorkPreference;
    @FXML private TableColumn<OffreEmploi, String> colLieu;
    @FXML private TableColumn<OffreEmploi, String> colStatutOffre;
    @FXML private TableColumn<OffreEmploi, Date> colDatePublication;
    @FXML private TableColumn<OffreEmploi, Integer> colIdUtilisateur;
    @FXML private TableColumn<OffreEmploi, Void> colActions;

    @FXML
    private TableColumn<CritereOffre, Void> colCritereActions;

    @FXML private TableView<CritereOffre> critereTable;
    @FXML private TableColumn<CritereOffre, Integer> colIdCritere;
    @FXML private TableColumn<CritereOffre, String> colNiveauExperience;
    @FXML private TableColumn<CritereOffre, String> colNiveauEtude;
    @FXML private TableColumn<CritereOffre, String> colResponsibilities;
    @FXML private TableColumn<CritereOffre, String> colCompetences;
    @FXML private TableColumn<CritereOffre, Integer> colIdOffreCritere;

    // Statistics components
    @FXML private VBox statisticsContainer;
    @FXML private HBox statsCardsContainer;
    @FXML private PieChart statusPieChart;
    @FXML private PieChart contractTypePieChart;
    @FXML private PieChart workPrefPieChart;
    @FXML private BarChart<String, Number> locationBarChart;

    @FXML
    private void rafraichirCriteres() {
        loadCriteresForSelectedOffre();
    }



    private final OffreEmploiService service = new OffreEmploiService();
    private final ObservableList<OffreEmploi> data = FXCollections.observableArrayList();
    private CritereOffreService critereService = new CritereOffreService();
    private AIEnhancementOffreService aiService = new AIEnhancementOffreService();
    private StatisticsService statsService = new StatisticsService();
    private Integer currentOffreId = null;
    private OffreEmploi currentOffre = null;

    /**
     * Fetches all distinct user IDs from the offre_emploi table in the database.
     * This method retrieves unique user IDs that have created job offers.
     * 
     * @return A List of Integer containing all distinct user IDs, or an empty list if none found
     */
    private List<Integer> getAllUserIds() {
        List<Integer> userIds = new java.util.ArrayList<>();
        String sql = "SELECT id_utilisateur FROM utilisateur ORDER BY id_utilisateur";
        
        try (java.sql.PreparedStatement ps = utilis.MyConnection.getInstance().getCnx().prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                userIds.add(rs.getInt("id_utilisateur"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return userIds;
    }


    @FXML
    public void initialize() {
        // IMPORTANT: property names must match getters/setters in OffreEmploi
        colIdOffre.setCellValueFactory(new PropertyValueFactory<>("idOffre"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colTypeContrat.setCellValueFactory(new PropertyValueFactory<>("typeContrat"));
        colWorkPreference.setCellValueFactory(new PropertyValueFactory<>("workPreference"));
        colLieu.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        colStatutOffre.setCellValueFactory(new PropertyValueFactory<>("statutOffre"));
        colDatePublication.setCellValueFactory(new PropertyValueFactory<>("datePublication"));
        colIdUtilisateur.setCellValueFactory(new PropertyValueFactory<>("idUtilisateur"));

        offreTable.setItems(data);

        colIdCritere.setCellValueFactory(new PropertyValueFactory<>("idCritere"));
        colNiveauExperience.setCellValueFactory(new PropertyValueFactory<>("niveauExperience"));
        colNiveauEtude.setCellValueFactory(new PropertyValueFactory<>("niveauEtude"));
        colResponsibilities.setCellValueFactory(new PropertyValueFactory<>("responsibilities"));
        colCompetences.setCellValueFactory(new PropertyValueFactory<>("competencesRequises"));
        colIdOffreCritere.setCellValueFactory(new PropertyValueFactory<>("idOffre"));

        offreTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        currentOffreId = newSelection.getIdOffre();
                        currentOffre = newSelection;
                        loadCriteres();
                    }
                }
        );
        // Make description column wrap nicer (optional)
        colActions.setCellFactory(param -> new TableCell<>() {

            private final ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/org/example/images/edit.png")));
            private final ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/org/example/images/delete.png")));
            private final Button btnEdit = new Button();
            private final Button btnDelete = new Button();
            private final HBox pane = new HBox(8, btnEdit, btnDelete);

            {
                // Set icon sizes
                editIcon.setFitWidth(16);
                editIcon.setFitHeight(16);
                deleteIcon.setFitWidth(16);
                deleteIcon.setFitHeight(16);
                
                // Set icons as button graphics
                btnEdit.setGraphic(editIcon);
                btnDelete.setGraphic(deleteIcon);
                
                // Edit button styling
                btnEdit.setStyle("-fx-background-color: #e0e7ff; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 10; -fx-min-width: 36; -fx-min-height: 36; -fx-border-width: 0;");
                
                // Edit button hover effect
                btnEdit.setOnMouseEntered(e -> btnEdit.setStyle("-fx-background-color: #c7d2fe; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 10; -fx-min-width: 36; -fx-min-height: 36; -fx-border-width: 0;"));
                btnEdit.setOnMouseExited(e -> btnEdit.setStyle("-fx-background-color: #e0e7ff; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 10; -fx-min-width: 36; -fx-min-height: 36; -fx-border-width: 0;"));
                
                // Delete button styling
                btnDelete.setStyle("-fx-background-color: #fee2e2; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 10; -fx-min-width: 36; -fx-min-height: 36; -fx-border-width: 0;");
                
                // Delete button hover effect
                btnDelete.setOnMouseEntered(e -> btnDelete.setStyle("-fx-background-color: #fecaca; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 10; -fx-min-width: 36; -fx-min-height: 36; -fx-border-width: 0;"));
                btnDelete.setOnMouseExited(e -> btnDelete.setStyle("-fx-background-color: #fee2e2; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 10; -fx-min-width: 36; -fx-min-height: 36; -fx-border-width: 0;"));

                btnEdit.setOnAction(event -> {
                    OffreEmploi offre = getTableView().getItems().get(getIndex());
                    modifierOffreFromRow(offre);
                });

                btnDelete.setOnAction(event -> {
                    OffreEmploi offre = getTableView().getItems().get(getIndex());
                    supprimerOffreFromRow(offre);
                });
            }


            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
        colCritereActions.setCellFactory(param -> new TableCell<>() {

            private final ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/org/example/images/edit.png")));
            private final ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/org/example/images/delete.png")));
            private final Button editBtn = new Button();
            private final Button deleteBtn = new Button();
            private final HBox pane = new HBox(8, editBtn, deleteBtn);

            {
                // Set icon sizes
                editIcon.setFitWidth(16);
                editIcon.setFitHeight(16);
                deleteIcon.setFitWidth(16);
                deleteIcon.setFitHeight(16);
                
                // Set icons as button graphics
                editBtn.setGraphic(editIcon);
                deleteBtn.setGraphic(deleteIcon);
                
                // Edit button styling
                editBtn.setStyle("-fx-background-color: #e0e7ff; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 10; -fx-min-width: 36; -fx-min-height: 36; -fx-border-width: 0;");
                
                // Edit button hover effect
                editBtn.setOnMouseEntered(e -> editBtn.setStyle("-fx-background-color: #c7d2fe; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 10; -fx-min-width: 36; -fx-min-height: 36; -fx-border-width: 0;"));
                editBtn.setOnMouseExited(e -> editBtn.setStyle("-fx-background-color: #e0e7ff; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 10; -fx-min-width: 36; -fx-min-height: 36; -fx-border-width: 0;"));
                
                // Delete button styling
                deleteBtn.setStyle("-fx-background-color: #fee2e2; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 10; -fx-min-width: 36; -fx-min-height: 36; -fx-border-width: 0;");
                
                // Delete button hover effect
                deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle("-fx-background-color: #fecaca; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 10; -fx-min-width: 36; -fx-min-height: 36; -fx-border-width: 0;"));
                deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle("-fx-background-color: #fee2e2; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 10; -fx-min-width: 36; -fx-min-height: 36; -fx-border-width: 0;"));

                editBtn.setOnAction(e -> {
                    CritereOffre critere = getTableView().getItems().get(getIndex());
                    modifierCritere(critere);
                });

                deleteBtn.setOnAction(e -> {
                    CritereOffre critere = getTableView().getItems().get(getIndex());
                    supprimerCritere(critere);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        offreTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> loadCriteresForSelectedOffre()
        );

        rafraichirOffres();
        
        // Initialize statistics on startup
        try {
            refreshStatistics();
        } catch (Exception e) {
            System.err.println("Error initializing statistics: " + e.getMessage());
        }

        // Simple search (filters in-memory)
        searchField.textProperty().addListener((obs, oldV, newV) -> applySearch(newV));
        
        // Setup sidebar hover effect
        setupSidebarHoverEffect();
        
        // Load user info from session
        loadUserInfo();
        
        // Setup sidebar navigation
        setupSidebarNavigation();
    }
    
    private void setupSidebarHoverEffect() {
        // Set initial state
        sidebar.setPrefWidth(90.0);
        
        // Variable to track if mouse is inside sidebar
        final boolean[] isMouseInside = {false};
        
        // Expand on mouse enter to sidebar
        sidebar.setOnMouseEntered(event -> {
            isMouseInside[0] = true;
            expandSidebar();
        });
        
        // Collapse only when mouse truly leaves
        sidebar.setOnMouseExited(event -> {
            isMouseInside[0] = false;
            // Delay check to see if mouse re-entered
            Timeline delayCheck = new Timeline(new KeyFrame(Duration.millis(100), e -> {
                if (!isMouseInside[0]) {
                    collapseSidebar();
                }
            }));
            delayCheck.play();
        });
        
        // Keep expanded when hovering/clicking on nav items
        navStatistiques.setOnMouseEntered(e -> { isMouseInside[0] = true; });
        navStatistiques.setOnMousePressed(e -> { isMouseInside[0] = true; });
        navOpportunites.setOnMouseEntered(e -> { isMouseInside[0] = true; });
        navOpportunites.setOnMousePressed(e -> { isMouseInside[0] = true; });
        navApropos.setOnMouseEntered(e -> { isMouseInside[0] = true; });
        navApropos.setOnMousePressed(e -> { isMouseInside[0] = true; });
        navAdministration.setOnMouseEntered(e -> { isMouseInside[0] = true; });
        navAdministration.setOnMousePressed(e -> { isMouseInside[0] = true; });
        navParametres.setOnMouseEntered(e -> { isMouseInside[0] = true; });
        navParametres.setOnMousePressed(e -> { isMouseInside[0] = true; });
        navDeconnexion.setOnMouseEntered(e -> { isMouseInside[0] = true; });
        navDeconnexion.setOnMousePressed(e -> { isMouseInside[0] = true; });
        
        // Add hover effects on nav items
        addNavItemHoverEffect(navStatistiques);
        addNavItemHoverEffect(navOpportunites);
        addNavItemHoverEffect(navApropos);
        addNavItemHoverEffect(navAdministration);
        addNavItemHoverEffect(navParametres);
        addNavItemHoverEffect(navDeconnexion);
    }
    
    private void expandSidebar() {
        Timeline expandTimeline = new Timeline(
            new KeyFrame(Duration.millis(250),
                new KeyValue(sidebar.prefWidthProperty(), 240),
                new KeyValue(sidebarSubtitle.opacityProperty(), 1),
                new KeyValue(sidebarSubtitle.maxWidthProperty(), 200),
                new KeyValue(labelStatistiques.opacityProperty(), 1),
                new KeyValue(labelStatistiques.maxWidthProperty(), 150),
                new KeyValue(labelOpportunites.opacityProperty(), 1),
                new KeyValue(labelOpportunites.maxWidthProperty(), 150),
                new KeyValue(labelApropos.opacityProperty(), 1),
                new KeyValue(labelApropos.maxWidthProperty(), 150),
                new KeyValue(labelAdministration.opacityProperty(), 1),
                new KeyValue(labelAdministration.maxWidthProperty(), 150),
                new KeyValue(labelParametres.opacityProperty(), 1),
                new KeyValue(labelParametres.maxWidthProperty(), 150),
                new KeyValue(labelDeconnexion.opacityProperty(), 1),
                new KeyValue(labelDeconnexion.maxWidthProperty(), 150),
                new KeyValue(supportSection.opacityProperty(), 1),
                new KeyValue(supportSection.maxHeightProperty(), 200)
            )
        );
        expandTimeline.play();
    }
    
    private void collapseSidebar() {
        Timeline collapseTimeline = new Timeline(
            new KeyFrame(Duration.millis(250),
                new KeyValue(sidebar.prefWidthProperty(), 90),
                new KeyValue(sidebarSubtitle.opacityProperty(), 0),
                new KeyValue(sidebarSubtitle.maxWidthProperty(), 0),
                new KeyValue(labelStatistiques.opacityProperty(), 0),
                new KeyValue(labelStatistiques.maxWidthProperty(), 0),
                new KeyValue(labelOpportunites.opacityProperty(), 0),
                new KeyValue(labelOpportunites.maxWidthProperty(), 0),
                new KeyValue(labelApropos.opacityProperty(), 0),
                new KeyValue(labelApropos.maxWidthProperty(), 0),
                new KeyValue(labelAdministration.opacityProperty(), 0),
                new KeyValue(labelAdministration.maxWidthProperty(), 0),
                new KeyValue(labelParametres.opacityProperty(), 0),
                new KeyValue(labelParametres.maxWidthProperty(), 0),
                new KeyValue(labelDeconnexion.opacityProperty(), 0),
                new KeyValue(labelDeconnexion.maxWidthProperty(), 0),
                new KeyValue(supportSection.opacityProperty(), 0),
                new KeyValue(supportSection.maxHeightProperty(), 0)
            )
        );
        collapseTimeline.play();
    }
    
    private void addNavItemHoverEffect(HBox navItem) {
        navItem.setOnMouseEntered(e -> {
            navItem.setStyle(navItem.getStyle() + "-fx-background-color: #f3f4f6;");
        });
        
        navItem.setOnMouseExited(e -> {
            navItem.setStyle(navItem.getStyle().replace("-fx-background-color: #f3f4f6;", "-fx-background-color: transparent;"));
        });
    }
    private void supprimerCritere(CritereOffre critere) {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer ce critéé¨re ?");
        confirm.setContentText("Cette action est irréé©versible.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {

                critereService.deleteCritere(critere.getIdCritere());
                loadCriteres();
            }
        });
    }
    private void modifierCritere(CritereOffre critere) {

        CritereOffre updated = showCritereDialogForUpdate(critere);

        if (updated != null) {
            critereService.updateCritere(
                    critere.getIdCritere(),
                    updated
            );

            loadCriteres();
        }
    }

    private void loadCriteres() {

        if (currentOffreId == null) return;

        critereTable.getItems().setAll(
                critereService.getByOffreId(currentOffreId)
        );
    }
    @FXML
    private void ajouterCritere() {

        if (currentOffreId == null) {
            showAlert(Alert.AlertType.WARNING,
                    "Attention",
                    "Séé©lectionnez une offre d'abord !");
            return;
        }

        CritereOffre c = showCritereDialog(currentOffreId, 
                                          currentOffre != null ? currentOffre.getTitre() : "Untitled");

        if (c != null) {
            critereService.insertCritere(c);
            loadCriteres();
        }
    }


    private void applySearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            rafraichirOffres();
            return;
        }
        String q = query.toLowerCase().trim();
        ObservableList<OffreEmploi> filtered = data.filtered(o ->
                (o.getTitre() != null && o.getTitre().toLowerCase().contains(q)) ||
                        (o.getDescription() != null && o.getDescription().toLowerCase().contains(q)) ||
                        (o.getTypeContrat() != null && o.getTypeContrat().toLowerCase().contains(q)) ||
                        (o.getStatutOffre() != null && o.getStatutOffre().toLowerCase().contains(q))
        );
        offreTable.setItems(filtered);
    }

    @FXML
    public void rafraichirOffres() {
        List<OffreEmploi> offres = service.getAllOffres();
        data.setAll(offres);
        offreTable.setItems(data);
    }

    @FXML
    public void supprimerOffre() {
        OffreEmploi selected = offreTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune séé©lection", "Séé©lectionnez une offre éé  supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'offre #" + selected.getIdOffre() + " ?");
        confirm.setContentText("Cette action est irréé©versible.");

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            service.deleteOffre(selected.getIdOffre());
            rafraichirOffres();
        }
    }

    @FXML
    public void modifierOffre() {

        OffreEmploi selected = offreTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune séé©lection", "Séé©lectionnez une offre.");
            return;
        }

        OffreEmploi updated = showOffreDialog(selected);

        if (updated != null) {
            service.updateOffre(selected.getIdOffre(), updated);
            rafraichirOffres();
        }
    }

    @FXML
    public void ajouterOffre() {
        OffreEmploi newOffre = showOffreDialog(null);
        if (newOffre != null) {
            service.insertOffre(newOffre);
        }
    }


    private String ask(String title, String label, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(label);
        Optional<String> res = dialog.showAndWait();
        return res.orElse(null);
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private OffreEmploi showOffreDialog(OffreEmploi existing) {

        Dialog<OffreEmploi> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Ajouter Offre" : "Modifier Offre");
        dialog.setHeaderText(null);

        ButtonType saveButton = new ButtonType(existing == null ? "Ajouter" : "Mettre éé  jour", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, cancelButton);

        // Container with dark theme
        VBox container = new VBox(20);
        container.setStyle("-fx-background-color: #1a1a2e; -fx-padding: 30;");
        container.setPrefWidth(450);

        // Title
        Label titleLabel = new Label(existing == null ? "é°Å¸éé Ajouter une nouvelle offre" : "é¢Åéé¯é¸é Modifier l'offre");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white; -fx-alignment: center;");
        VBox.setMargin(titleLabel, new javafx.geometry.Insets(0, 0, 10, 0));

        // FORM FIELDS with modern dark design
        // Titre
        Label titreLbl = new Label("é°Å¸éé¹ TITRE");
        titreLbl.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 600;");
        TextField titreField = new TextField();
        titreField.setPromptText("Ex: Déé©veloppeur Full Stack");
        titreField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                          "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #3d3d5c; " +
                          "-fx-border-radius: 8; -fx-font-size: 14px;");
        // Error label for titre
        Label titreError = new Label(" ");
        titreError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
        
        // Real-time validation for titre
        titreField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                titreError.setText("é¢éÅ Le titre est obligatoire.");
                titreError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                titreField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
            } else if (newVal.trim().length() < 5) {
                titreError.setText("é¢éÅ Le titre doit contenir au moins 5 caractéé¨res (" + newVal.trim().length() + "/5)");
                titreError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                titreField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
            } else if (newVal.trim().length() > 100) {
                titreError.setText("é¢éÅ Le titre ne peut pas déé©passer 100 caractéé¨res (" + newVal.trim().length() + "/100)");
                titreError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                titreField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
            } else {
                titreError.setText("é¢Åé¦ Valide");
                titreError.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                titreField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #10b981; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
            }
        });

        // Description
        Label descLbl = new Label("é°Å¸éé DESCRIPTION");
        descLbl.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 600;");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Déé©crivez l'offre d'emploi...");
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                "-fx-background-radius: 8; -fx-border-color: #3d3d5c; -fx-border-radius: 8;");
        
        // Small AI Enhancement Button (inside TextArea)
        Button enhanceBtn = new Button("é¢Åé¨");
        enhanceBtn.setStyle(
            "-fx-background-color: linear-gradient(to right, #8b5cf6, #ec4899); " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: 600; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 6 10; " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand; " +
            "-fx-opacity: 0.9; " +
            "-fx-effect: dropshadow(gaussian, rgba(139, 92, 246, 0.4), 6, 0, 0, 2);"
        );
        enhanceBtn.setTooltip(new javafx.scene.control.Tooltip("Améé©liorer avec l'IA"));
        
        // Stack the button on top of TextArea
        StackPane descriptionStack = new StackPane();
        descriptionStack.getChildren().addAll(descriptionArea, enhanceBtn);
        StackPane.setAlignment(enhanceBtn, javafx.geometry.Pos.TOP_RIGHT);
        StackPane.setMargin(enhanceBtn, new javafx.geometry.Insets(8, 8, 0, 0));
        
        // Status indicators below the text area
        HBox enhanceBox = new HBox(10);
        enhanceBox.setStyle("-fx-alignment: center-left; -fx-padding: 5 0 0 0;");
        
        ProgressIndicator enhanceProgress = new ProgressIndicator();
        enhanceProgress.setPrefSize(20, 20);
        enhanceProgress.setVisible(false);
        
        Label enhanceStatus = new Label("");
        enhanceStatus.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
        enhanceStatus.setVisible(false);
        
        enhanceBox.getChildren().addAll(enhanceProgress, enhanceStatus);
        
        // Hover effect for small enhance button
        enhanceBtn.setOnMouseEntered(e -> enhanceBtn.setStyle(
            "-fx-background-color: linear-gradient(to right, #7c3aed, #db2777); " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: 600; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 6 10; " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand; " +
            "-fx-opacity: 1.0; " +
            "-fx-effect: dropshadow(gaussian, rgba(139, 92, 246, 0.6), 8, 0, 0, 3);"
        ));
        
        enhanceBtn.setOnMouseExited(e -> enhanceBtn.setStyle(
            "-fx-background-color: linear-gradient(to right, #8b5cf6, #ec4899); " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: 600; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 6 10; " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand; " +
            "-fx-opacity: 0.9; " +
            "-fx-effect: dropshadow(gaussian, rgba(139, 92, 246, 0.4), 6, 0, 0, 2);"
        ));
        
        // AI Enhancement Handler
        enhanceBtn.setOnAction(evt -> {
            String currentText = descriptionArea.getText();
            
            if (currentText == null || currentText.trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Description vide");
                alert.setHeaderText(null);
                alert.setContentText("Veuillez entrer une description avant de l'améé©liorer.");
                alert.showAndWait();
                return;
            }
            
            if (!aiService.isConfigured()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Configuration manquante");
                alert.setHeaderText(null);
                alert.setContentText("L'API AI n'est pas configuréé©e.\n\n" +
                    "OPTION GRATUITE (Recommandéé©e):\n" +
                    "1. Ouvrez: src/main/resources/config.properties\n" +
                    "2. Déé©finissez: ai.provider=gemini\n" +
                    "3. Obtenez une cléé© GRATUITE sur:\n" +
                    "   https://aistudio.google.com/app/apikey\n" +
                    "4. Déé©finissez: gemini.api.key=votre-cléé©\n\n" +
                    "Redéé©marrez l'application apréé¨s configuration.");
                alert.showAndWait();
                return;
            }
            
            // Disable UI during processing
            enhanceBtn.setDisable(true);
            descriptionArea.setDisable(true);
            enhanceProgress.setVisible(true);
            enhanceStatus.setVisible(true);
            enhanceStatus.setText("Améé©lioration en cours...");
            enhanceStatus.setStyle("-fx-text-fill: #60a5fa; -fx-font-size: 11px;");
            
            // Create background task
            Task<String> enhanceTask = new Task<String>() {
                @Override
                protected String call() throws Exception {
                    return aiService.enhanceDescription(currentText);
                }
                
                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        String enhanced = getValue();
                        descriptionArea.setText(enhanced);
                        enhanceStatus.setText("é¢Åé¦ Description améé©lioréé©e!");
                        enhanceStatus.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px;");
                        
                        // Hide status after 3 seconds
                        Timeline timeline = new Timeline(new KeyFrame(
                            Duration.seconds(3),
                            e -> {
                                enhanceStatus.setVisible(false);
                                enhanceProgress.setVisible(false);
                            }
                        ));
                        timeline.play();
                        
                        enhanceBtn.setDisable(false);
                        descriptionArea.setDisable(false);
                    });
                }
                
                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        Throwable ex = getException();
                        enhanceStatus.setText("é¢éÅ éé°chec: " + ex.getMessage());
                        enhanceStatus.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px;");
                        
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erreur d'améé©lioration");
                        alert.setHeaderText(null);
                        alert.setContentText("Impossible d'améé©liorer la description:\n\n" + ex.getMessage());
                        alert.showAndWait();
                        
                        enhanceProgress.setVisible(false);
                        enhanceBtn.setDisable(false);
                        descriptionArea.setDisable(false);
                    });
                }
            };
            
            // Run task in background
            Thread thread = new Thread(enhanceTask);
            thread.setDaemon(true);
            thread.start();
        });
        
        // Error label for description
        Label descError = new Label(" ");
        descError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
        
        // Real-time validation for description
        descriptionArea.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                descError.setText("é¢éÅ La description est obligatoire.");
                descError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                descriptionArea.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                        "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                        "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
            } else if (newVal.trim().length() < 20) {
                descError.setText("é¢éÅ La description doit contenir au moins 20 caractéé¨res (" + newVal.trim().length() + "/20)");
                descError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                descriptionArea.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                        "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                        "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
            } else if (newVal.trim().length() > 500) {
                descError.setText("é¢éÅ La description ne peut pas déé©passer 500 caractéé¨res (" + newVal.trim().length() + "/500)");
                descError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                descriptionArea.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                        "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                        "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
            } else {
                descError.setText("é¢Åé¦ Valide (" + newVal.trim().length() + "/500 caractéé¨res)");
                descError.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                descriptionArea.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                        "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                        "-fx-background-radius: 8; -fx-border-color: #10b981; -fx-border-width: 2; -fx-border-radius: 8;");
            }
        });

        // Type Contrat
        Label typeLbl = new Label("é°Å¸éé¼ TYPE DE CONTRAT");
        typeLbl.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 600;");
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("CDI", "CDD", "Stage", "Alternance", "Freelance", "Intéé©rim");
        typeCombo.setValue("CDI");
        typeCombo.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-background-radius: 8; " +
                         "-fx-border-color: #3d3d5c; -fx-border-radius: 8;");
        typeCombo.setPrefWidth(400);
        typeCombo.setCellFactory(lv -> {
            javafx.scene.control.ListCell<String> cell = new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item);
                    setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-padding: 8;");
                }
            };
            return cell;
        });
        typeCombo.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
                setStyle("-fx-text-fill: white;");
            }
        });

        // Statut - hide when adding new offer (auto-set to ACTIVE)
        Label statutLbl = new Label("é°Å¸éÅ  STATUT");
        statutLbl.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 600;");
        statutLbl.setVisible(existing != null);
        statutLbl.setManaged(existing != null);
        
        ComboBox<String> statutCombo = new ComboBox<>();
        statutCombo.getItems().addAll("ACTIVE", "INACTIVE", "ARCHIVED");
        statutCombo.setValue("ACTIVE");
        statutCombo.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-background-radius: 8; " +
                           "-fx-border-color: #3d3d5c; -fx-border-radius: 8;");
        statutCombo.setPrefWidth(400);
        statutCombo.setVisible(existing != null);
        statutCombo.setManaged(existing != null);
        statutCombo.setCellFactory(lv -> {
            javafx.scene.control.ListCell<String> cell = new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item);
                    setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-padding: 8;");
                }
            };
            return cell;
        });
        statutCombo.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
                setStyle("-fx-text-fill: white;");
            }
        });

        // Work Preference
        Label workPrefLbl = new Label("é°Å¸éé¢ PRéé°Féé°RENCE DE TRAVAIL");
        workPrefLbl.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 600;");
        ComboBox<String> workPrefCombo = new ComboBox<>();
        workPrefCombo.getItems().addAll("On-site", "Remote", "Hybrid");
        workPrefCombo.setValue("On-site");
        workPrefCombo.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-background-radius: 8; " +
                              "-fx-border-color: #3d3d5c; -fx-border-radius: 8;");
        workPrefCombo.setPrefWidth(400);
        workPrefCombo.setCellFactory(lv -> {
            javafx.scene.control.ListCell<String> cell = new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item);
                    setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-padding: 8;");
                }
            };
            return cell;
        });
        workPrefCombo.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
                setStyle("-fx-text-fill: white;");
            }
        });

        // Lieu (Location)
        Label lieuLbl = new Label("é°Å¸éé LIEU");
        lieuLbl.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 600;");
        TextField lieuField = new TextField();
        lieuField.setPromptText("Ex: Paris, Lyon, Remote...");
        lieuField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                          "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #3d3d5c; " +
                          "-fx-border-radius: 8; -fx-font-size: 14px;");
        lieuField.setPrefWidth(400);

        // User ID
        Label userLbl = new Label("é°Å¸éé¤ ID UTILISATEUR");
        userLbl.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 600;");
        ComboBox<Integer> userIdCombo = new ComboBox<>();
        
        // Load user IDs from database
        List<Integer> userIds = getAllUserIds();
        if (!userIds.isEmpty()) {
            userIdCombo.getItems().addAll(userIds);
            userIdCombo.setValue(userIds.get(0)); // Set first user as default
        } else {
            // If no users found, allow manual entry range
            userIdCombo.getItems().addAll(1, 2, 3, 4, 5);
            userIdCombo.setValue(1);
        }
        
        userIdCombo.setPromptText("Sé©lectionnez un utilisateur");
        userIdCombo.setEditable(true); // Allow manual entry for new user IDs
        userIdCombo.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-background-radius: 8; " +
                           "-fx-border-color: #3d3d5c; -fx-border-radius: 8;");
        userIdCombo.setPrefWidth(400);
        
        // Add StringConverter to handle String <-> Integer conversion
        userIdCombo.setConverter(new javafx.util.StringConverter<Integer>() {
            @Override
            public String toString(Integer object) {
                return object == null ? "" : "Utilisateur #" + object;
            }
            
            @Override
            public Integer fromString(String string) {
                if (string == null || string.trim().isEmpty()) {
                    return null;
                }
                // Remove "Utilisateur #" prefix if present
                String cleanString = string.replace("Utilisateur #", "").trim();
                try {
                    return Integer.parseInt(cleanString);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        });
        
        userIdCombo.setCellFactory(lv -> {
            javafx.scene.control.ListCell<Integer> cell = new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : "Utilisateur #" + item);
                    setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-padding: 8;");
                }
            };
            return cell;
        });
        userIdCombo.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : "Utilisateur #" + item);
                setStyle("-fx-text-fill: white;");
            }
        });
        // Error label for user ID
        Label userError = new Label(" ");
        userError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
        
        // Real-time validation for user ID
        Runnable validateUserId = () -> {
            Integer value = userIdCombo.getValue();
            String editorText = userIdCombo.getEditor().getText();
            
            if (value == null && (editorText == null || editorText.trim().isEmpty())) {
                userError.setText("é¢éÅ L'ID utilisateur est obligatoire.");
                userError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
            } else if (value == null && editorText != null && !editorText.trim().isEmpty()) {
                try {
                    int id = Integer.parseInt(editorText.trim());
                    if (id <= 0) {
                        userError.setText("é¢éÅ L'ID doit ééªtre un nombre positif.");
                        userError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                    } else {
                        userError.setText("é¢Åé¦ Valide");
                        userError.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                    }
                } catch (NumberFormatException e) {
                    userError.setText("é¢éÅ L'ID doit ééªtre un nombre valide.");
                    userError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                }
            } else {
                userError.setText("é¢Åé¦ Valide");
                userError.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
            }
        };
        
        userIdCombo.valueProperty().addListener((obs, oldVal, newVal) -> validateUserId.run());
        userIdCombo.getEditor().textProperty().addListener((obs, oldVal, newVal) -> validateUserId.run());

        if (existing != null) {
            titreField.setText(existing.getTitre());
            descriptionArea.setText(existing.getDescription());
            typeCombo.setValue(existing.getTypeContrat());
            statutCombo.setValue(existing.getStatutOffre());
            userIdCombo.setValue(existing.getIdUtilisateur());
            if (existing.getWorkPreference() != null) {
                workPrefCombo.setValue(existing.getWorkPreference());
            }
            if (existing.getLieu() != null) {
                lieuField.setText(existing.getLieu());
            }
        }

        container.getChildren().addAll(
            titleLabel,
            titreLbl, titreField, titreError,
            descLbl, descriptionStack, enhanceBox, descError,
            typeLbl, typeCombo,
            workPrefLbl, workPrefCombo,
            lieuLbl, lieuField,
            statutLbl, statutCombo,
            userLbl, userIdCombo, userError
        );

        dialog.getDialogPane().setContent(container);
        dialog.getDialogPane().setStyle("-fx-background-color: #1a1a2e; -fx-padding: 0;");
        
        // Style buttons to match modern design
        javafx.scene.Node saveButtonNode = dialog.getDialogPane().lookupButton(saveButton);
        javafx.scene.Node cancelButtonNode = dialog.getDialogPane().lookupButton(cancelButton);
        
        saveButtonNode.setStyle(
            "-fx-background-color: linear-gradient(to right, #a855f7, #ec4899); " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 14 35; " +
            "-fx-background-radius: 12; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(168, 85, 247, 0.4), 12, 0, 0, 4);"
        );
        
        cancelButtonNode.setStyle(
            "-fx-background-color: #2d2d48; " +
            "-fx-text-fill: #94a3b8; " +
            "-fx-font-weight: 600; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 14 35; " +
            "-fx-background-radius: 12; " +
            "-fx-cursor: hand; " +
            "-fx-border-color: transparent;"
        );
        
        // Add hover effects
        saveButtonNode.setOnMouseEntered(e -> saveButtonNode.setStyle(
            "-fx-background-color: linear-gradient(to right, #9333ea, #db2777); " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 14 35; " +
            "-fx-background-radius: 12; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(168, 85, 247, 0.6), 15, 0, 0, 5);"
        ));
        
        saveButtonNode.setOnMouseExited(e -> saveButtonNode.setStyle(
            "-fx-background-color: linear-gradient(to right, #a855f7, #ec4899); " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 14 35; " +
            "-fx-background-radius: 12; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(168, 85, 247, 0.4), 12, 0, 0, 4);"
        ));
        
        cancelButtonNode.setOnMouseEntered(e -> cancelButtonNode.setStyle(
            "-fx-background-color: #3d3d5c; " +
            "-fx-text-fill: #a8b3cf; " +
            "-fx-font-weight: 600; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 14 35; " +
            "-fx-background-radius: 12; " +
            "-fx-cursor: hand; " +
            "-fx-border-color: transparent;"
        ));
        
        cancelButtonNode.setOnMouseExited(e -> cancelButtonNode.setStyle(
            "-fx-background-color: #2d2d48; " +
            "-fx-text-fill: #94a3b8; " +
            "-fx-font-weight: 600; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 14 35; " +
            "-fx-background-radius: 12; " +
            "-fx-cursor: hand; " +
            "-fx-border-color: transparent;"
        ));

        // Add event filter to prevent dialog closing on validation failure
        saveButtonNode.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            // Hide all error labels first
            titreError.setVisible(false);
            titreError.setManaged(false);
            descError.setVisible(false);
            descError.setManaged(false);
            userError.setVisible(false);
            userError.setManaged(false);
            
            // Reset field styles
            titreField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                              "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #3d3d5c; " +
                              "-fx-border-radius: 8; -fx-font-size: 14px;");
            descriptionArea.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                    "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                    "-fx-background-radius: 8; -fx-border-color: #3d3d5c; -fx-border-radius: 8;");
            
            boolean hasError = false;
            
            // Validate Titre
            String titre = titreField.getText();
            if (titre == null || titre.trim().isEmpty()) {
                titreError.setText("é¢éÅ Le titre est obligatoire.");
                titreError.setVisible(true);
                titreError.setManaged(true);
                titreField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
                hasError = true;
            } else if (titre.trim().length() < 5) {
                titreError.setText("é¢éÅ Le titre doit contenir au moins 5 caractéé¨res.");
                titreError.setVisible(true);
                titreError.setManaged(true);
                titreField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
                hasError = true;
            } else if (titre.trim().length() > 100) {
                titreError.setText("é¢éÅ Le titre ne peut pas déé©passer 100 caractéé¨res.");
                titreError.setVisible(true);
                titreError.setManaged(true);
                titreField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
                hasError = true;
            }
            
            // Validate Description
            String description = descriptionArea.getText();
            if (description == null || description.trim().isEmpty()) {
                descError.setText("é¢éÅ La description est obligatoire.");
                descError.setVisible(true);
                descError.setManaged(true);
                descriptionArea.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                        "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                        "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
                hasError = true;
            } else if (description.trim().length() < 20) {
                descError.setText("é¢éÅ La description doit contenir au moins 20 caractéé¨res.");
                descError.setVisible(true);
                descError.setManaged(true);
                descriptionArea.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                        "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                        "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
                hasError = true;
            } else if (description.trim().length() > 500) {
                descError.setText("é¢éÅ La description ne peut pas déé©passer 500 caractéé¨res.");
                descError.setVisible(true);
                descError.setManaged(true);
                descriptionArea.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                        "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                        "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
                hasError = true;
            }
            
            // Validate Type Contrat
            String typeContrat = typeCombo.getValue();
            if (typeContrat == null || typeContrat.trim().isEmpty()) {
                hasError = true;
            }
            
            // Validate User ID
            Integer userId = userIdCombo.getValue();
            
            // Handle manual entry if ComboBox value is null but editor has text
            if (userId == null) {
                String editorText = userIdCombo.getEditor().getText();
                if (editorText != null && !editorText.trim().isEmpty()) {
                    try {
                        userId = Integer.parseInt(editorText.trim());
                        if (userId <= 0) {
                            userError.setText("é¢éÅ L'ID utilisateur doit ééªtre un nombre positif.");
                            userError.setVisible(true);
                            userError.setManaged(true);
                            hasError = true;
                        } else {
                            userIdCombo.setValue(userId); // Set the parsed value
                        }
                    } catch (NumberFormatException e) {
                        userError.setText("é¢éÅ ID utilisateur invalide. Veuillez entrer un nombre.");
                        userError.setVisible(true);
                        userError.setManaged(true);
                        hasError = true;
                    }
                } else {
                    userError.setText("é¢éÅ Veuillez séé©lectionner ou entrer un ID utilisateur.");
                    userError.setVisible(true);
                    userError.setManaged(true);
                    hasError = true;
                }
            }
            
            if (hasError) {
                event.consume();
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                try {
                    OffreEmploi o = new OffreEmploi();
                    o.setTitre(titreField.getText().trim());
                    o.setDescription(descriptionArea.getText().trim());
                    o.setTypeContrat(typeCombo.getValue());
                    
                    // Auto-set status to ACTIVE when adding new offer
                    if (existing == null) {
                        o.setStatutOffre("ACTIVE");
                    } else {
                        o.setStatutOffre(statutCombo.getValue());
                    }
                    
                    o.setDatePublication(new java.sql.Date(System.currentTimeMillis()));
                    o.setWorkPreference(workPrefCombo.getValue());
                    o.setLieu(lieuField.getText().trim());
                    
                    Integer userId = userIdCombo.getValue();
                    if (userId == null) {
                        String editorText = userIdCombo.getEditor().getText();
                        if (editorText != null && !editorText.trim().isEmpty()) {
                            userId = Integer.parseInt(editorText.trim());
                        }
                    }
                    o.setIdUtilisateur(userId);

                    return o;

                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la créé©ation de l'offre");
                    return null;
                }
            }
            return null;
        });

        Optional<OffreEmploi> result = dialog.showAndWait();
        return result.orElse(null);
    }
    private void modifierOffreFromRow(OffreEmploi offre) {
        OffreEmploi updated = showOffreDialog(offre);
        if (updated != null) {
            service.updateOffre(offre.getIdOffre(), updated);
            rafraichirOffres();
        }
    }

    private void supprimerOffreFromRow(OffreEmploi offre) {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'offre ?");
        confirm.setContentText("ID: " + offre.getIdOffre());

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                service.deleteOffre(offre.getIdOffre());
                rafraichirOffres();
            }
        });
    }
    private void loadCriteresForSelectedOffre() {

        OffreEmploi selected = offreTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            critereTable.getItems().clear();
            return;
        }

        critereTable.getItems().setAll(
                critereService.getByOffreId(selected.getIdOffre())
        );
    }
    private CritereOffre showCritereDialog(int offreId, String jobTitle) {

        Dialog<CritereOffre> dialog = new Dialog<>();
        dialog.setTitle("Ajouter Critéé¨re");

        ButtonType saveButton = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, cancelButton);

        // Container with dark theme
        VBox container = new VBox(20);
        container.setStyle("-fx-background-color: #1a1a2e; -fx-padding: 30;");
        container.setPrefWidth(450);

        // Title
        Label titleLabel = new Label("é¢Åé¨ Ajouter un critéé¨re");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white; -fx-alignment: center;");
        VBox.setMargin(titleLabel, new javafx.geometry.Insets(0, 0, 10, 0));

        // Experience Field
        Label expLbl = new Label("é¢é­é NIVEAU D'EXPéé°RIENCE");
        expLbl.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 600;");
        TextField expField = new TextField();
        expField.setPromptText("Ex: 2 ans minimum d'expéé©rience");
        expField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                        "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #3d3d5c; " +
                        "-fx-border-radius: 8; -fx-font-size: 14px;");
        // Error label for experience
        Label expError = new Label(" ");
        expError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
        
        // Real-time validation for experience
        expField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                expError.setText("é¢éÅ Le niveau d'expéé©rience est obligatoire.");
                expError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                expField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                "-fx-border-radius: 8; -fx-font-size: 14px;");
            } else if (newVal.trim().length() < 3) {
                expError.setText("é¢éÅ Minimum 3 caractéé¨res (" + newVal.trim().length() + "/3)");
                expError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                expField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                "-fx-border-radius: 8; -fx-font-size: 14px;");
            } else if (newVal.trim().length() > 100) {
                expError.setText("é¢éÅ Maximum 100 caractéé¨res  (" + newVal.trim().length() + "/100)");
                expError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                expField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                "-fx-border-radius: 8; -fx-font-size: 14px;");
            } else {
                expError.setText("é¢Åé¦ Valide");
                expError.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                expField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #10b981; -fx-border-width: 2; " +
                                "-fx-border-radius: 8; -fx-font-size: 14px;");
            }
        });

        // Etude Field
        Label etudeLbl = new Label("é°Å¸Å½é NIVEAU D'éé°TUDE");
        etudeLbl.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 600;");
        TextField etudeField = new TextField();
        etudeField.setPromptText("Ex: Bac +5 en informatique");
        etudeField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                          "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #3d3d5c; " +
                          "-fx-border-radius: 8; -fx-font-size: 14px;");
        // Error label for etude
        Label etudeError = new Label(" ");
        etudeError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
        
        // Real-time validation for etude
        etudeField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                etudeError.setText("é¢éÅ Le niveau d'éé©tude est obligatoire.");
                etudeError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                etudeField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
            } else if (newVal.trim().length() < 3) {
                etudeError.setText("é¢éÅ Minimum 3 caractéé¨res (" + newVal.trim().length() + "/3)");
                etudeError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                etudeField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
            } else if (newVal.trim().length() > 100) {
                etudeError.setText("é¢éÅ Maximum 100 caractéé¨res (" + newVal.trim().length() + "/100)");
                etudeError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                etudeField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
            } else {
                etudeError.setText("é¢Åé¦ Valide");
                etudeError.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                etudeField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #10b981; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
            }
        });

        // Competences Field
        Label compLbl = new Label("é°Å¸éé¡ COMPéé°TENCES REQUISES");
        compLbl.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 600;");
        TextArea compField = new TextArea();
        compField.setPromptText("Ex: Java, Spring Boot, React...");
        compField.setPrefRowCount(4);
        compField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                         "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                         "-fx-background-radius: 8; -fx-border-color: #3d3d5c; -fx-border-radius: 8;");
        // Error label for competences
        Label compError = new Label(" ");
        compError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
        
        // Real-time validation for competences
        compField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                compError.setText("é¢éÅ Les compéé©tences requises sont obligatoires.");
                compError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                compField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
            } else if (newVal.trim().length() < 5) {
                compError.setText("é¢éÅ Minimum 5 caractéé¨res (" + newVal.trim().length() + "/5)");
                compError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                compField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
            } else if (newVal.trim().length() > 300) {
                compError.setText("é¢éÅ Maximum 300 caractéé¨res (" + newVal.trim().length() + "/300)");
                compError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding:  2 0 0 0; -fx-min-height: 16;");
                compField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
            } else {
                compError.setText("é¢Åé¦ Valide (" + newVal.trim().length() + "/300 caractéé¨res)");
                compError.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                compField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #10b981; -fx-border-width: 2; -fx-border-radius: 8;");
            }
        });
        
        // Add bullet point on Enter
        compField.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
                int pos = compField.getCaretPosition();
                String text = compField.getText();
                String newText = text.substring(0, pos) + "\né¢é¬é¢ " + text.substring(pos);
                compField.setText(newText);
                compField.positionCaret(pos + 3);
            }
        });
        
        // Add bullet point at start if empty
        compField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused && compField.getText().isEmpty()) {
                compField.setText("é¢é¬é¢ ");
                compField.positionCaret(2);
            }
        });

        // Responsibilities Field
        Label respLbl = new Label("é°Å¸éé¹ RESPONSABILITéé°S");
        respLbl.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 600;");
        TextArea respField = new TextArea();
        respField.setPromptText("Déé©crivez les responsabilitéé©s du poste...");
        respField.setPrefRowCount(4);
        respField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                         "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                         "-fx-background-radius: 8; -fx-border-color: #3d3d5c; -fx-border-radius: 8;");
        // Error label for responsibilities
        Label respError = new Label(" ");
        respError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
        
        // Real-time validation for responsibilities
        respField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                respError.setText("é¢éÅ Les responsabilitéé©s sont obligatoires.");
                respError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                respField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
            } else if (newVal.trim().length() < 10) {
                respError.setText("é¢éÅ Minimum 10 caractéé¨res (" + newVal.trim().length() + "/10)");
                respError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                respField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
            } else if (newVal.trim().length() > 500) {
                respError.setText("é¢éÅ Maximum 500 caractéé¨res (" + newVal.trim().length() + "/500)");
                respError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                respField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
            } else {
                respError.setText("é¢Åé¦ Valide (" + newVal.trim().length() + "/500 caractéé¨res)");
                respError.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                respField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #10b981; -fx-border-width: 2; -fx-border-radius: 8;");
            }
        });
        
        // Add bullet point on Enter for responsibilities
        respField.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
                int pos = respField.getCaretPosition();
                String text = respField.getText();
                String newText = text.substring(0, pos) + "\\né¢é¬é¢ " + text.substring(pos);
                respField.setText(newText);
                respField.positionCaret(pos + 3);
            }
        });
        
        // Add bullet point at start if empty for responsibilities
        respField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused && respField.getText().isEmpty()) {
                respField.setText("é¢é¬é¢ ");
                respField.positionCaret(2);
            }
        });

        // AI Generation Button
        Button aiButton = new Button("é°Å¸é¤é Géé©néé©rer avec l'IA");
        aiButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: 600; " +
                         "-fx-font-size: 12px; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        aiButton.setOnMouseEntered(e -> aiButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; " +
                         "-fx-font-weight: 600; -fx-font-size: 12px; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;"));
        aiButton.setOnMouseExited(e -> aiButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                         "-fx-font-weight: 600; -fx-font-size: 12px; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;"));

        aiButton.setOnAction(e -> {
            try {
                AIEnhancementOffreService aiService = new AIEnhancementOffreService();
                if (!aiService.isConfigured()) {
                    showAlert(Alert.AlertType.WARNING, "AI Non Configuréé©", 
                            "Veuillez configurer une cléé© API pour utiliser la géé©néé©ration IA.");
                    return;
                }

                // Show loading indicator
                aiButton.setText("é¢éé³ Géé©néé©ration en cours...");
                aiButton.setDisable(true);

                // Generate on background thread to avoid blocking UI
                new Thread(() -> {
                    try {
                        String[] result = aiService.generateJobCriteria(
                            jobTitle,
                            expField.getText().trim(),
                            etudeField.getText().trim()
                        );

                        // Update UI on JavaFX thread
                        javafx.application.Platform.runLater(() -> {
                            if (result != null && result.length == 2) {
                                respField.setText(result[0]);
                                compField.setText(result[1]);
                                showAlert(Alert.AlertType.INFORMATION, "Succéé¨s", 
                                        "Les responsabilitéé©s et compéé©tences ont éé©téé© géé©néé©réé©es avec succéé¨s !");
                            } else {
                                showAlert(Alert.AlertType.ERROR, "Erreur", 
                                        "Impossible de géé©néé©rer le contenu. Veuillez réé©essayer.");
                            }

                            aiButton.setText("é°Å¸é¤é Géé©néé©rer avec l'IA");
                            aiButton.setDisable(false);
                        });
                    } catch (Exception ex) {
                        javafx.application.Platform.runLater(() -> {
                            showAlert(Alert.AlertType.ERROR, "Erreur", 
                                    "Erreur lors de la géé©néé©ration: " + ex.getMessage());
                            aiButton.setText("é°Å¸é¤é Géé©néé©rer avec l'IA");
                            aiButton.setDisable(false);
                        });
                    }
                }).start();
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                        "Erreur lors de l'initialisation du service IA: " + ex.getMessage());
            }
        });

        HBox aiButtonContainer = new HBox(aiButton);
        aiButtonContainer.setAlignment(Pos.CENTER);
        aiButtonContainer.setStyle("-fx-padding: 10 0;");

        container.getChildren().addAll(
            titleLabel,
            expLbl, expField, expError,
            etudeLbl, etudeField, etudeError,
            aiButtonContainer,
            respLbl, respField, respError,
            compLbl, compField, compError
        );

        dialog.getDialogPane().setContent(container);
        dialog.getDialogPane().setStyle("-fx-background-color: #1a202c; -fx-padding: 0;");
        
        // Style buttons to match modern design
        javafx.scene.Node saveBtn = dialog.getDialogPane().lookupButton(saveButton);
        javafx.scene.Node cancelBtn = dialog.getDialogPane().lookupButton(cancelButton);
        
        saveBtn.setStyle(
            "-fx-background-color: linear-gradient(to right, #a855f7, #ec4899); " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 14 35; " +
            "-fx-background-radius: 12; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(168, 85, 247, 0.4), 12, 0, 0, 4);"
        );
        
        cancelBtn.setStyle(
            "-fx-background-color: #2d2d48; " +
            "-fx-text-fill: #94a3b8; " +
            "-fx-font-weight: 600; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 14 35; " +
            "-fx-background-radius: 12; " +
            "-fx-cursor: hand; " +
            "-fx-border-color: transparent;"
        );
        
        // Add hover effects
        saveBtn.setOnMouseEntered(e -> saveBtn.setStyle(
            "-fx-background-color: linear-gradient(to right, #9333ea, #db2777); " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 14 35; " +
            "-fx-background-radius: 12; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(168, 85, 247, 0.6), 15, 0, 0, 5);"
        ));
        
        saveBtn.setOnMouseExited(e -> saveBtn.setStyle(
            "-fx-background-color: linear-gradient(to right, #a855f7, #ec4899); " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 14 35; " +
            "-fx-background-radius: 12; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(168, 85, 247, 0.4), 12, 0, 0, 4);"
        ));
        
        cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle(
            "-fx-background-color: #3d3d5c; " +
            "-fx-text-fill: #a8b3cf; " +
            "-fx-font-weight: 600; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 14 35; " +
            "-fx-background-radius: 12; " +
            "-fx-cursor: hand; " +
            "-fx-border-color: transparent;"
        ));
        
        cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle(
            "-fx-background-color: #2d2d48; " +
            "-fx-text-fill: #94a3b8; " +
            "-fx-font-weight: 600; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 14 35; " +
            "-fx-background-radius: 12; " +
            "-fx-cursor: hand; " +
            "-fx-border-color: transparent;"
        ));

        // Add event filter to prevent dialog closing on validation failure
        saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            // Hide all error labels first
            expError.setVisible(false);
            expError.setManaged(false);
            etudeError.setVisible(false);
            etudeError.setManaged(false);
            compError.setVisible(false);
            compError.setManaged(false);
            respError.setVisible(false);
            respError.setManaged(false);
            
            // Reset field styles
            expField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                            "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #3d3d5c; " +
                            "-fx-border-radius: 8; -fx-font-size: 14px;");
            etudeField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                              "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #3d3d5c; " +
                              "-fx-border-radius: 8; -fx-font-size: 14px;");
            compField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                             "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                             "-fx-background-radius: 8; -fx-border-color: #3d3d5c; -fx-border-radius: 8;");
            respField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                             "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                             "-fx-background-radius: 8; -fx-border-color: #3d3d5c; -fx-border-radius: 8;");
            
            boolean hasError = false;
            
            // Validate all fields are filled
            String niveauExp = expField.getText();
            String niveauEtude = etudeField.getText();
            String competences = compField.getText();
            String responsibilities = respField.getText();
            
            // Validate Niveau Experience - not empty
            if (niveauExp == null || niveauExp.trim().isEmpty()) {
                expError.setText("é¢éÅ Le niveau d'expéé©rience est obligatoire.");
                expError.setVisible(true);
                expError.setManaged(true);
                expField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                "-fx-border-radius: 8; -fx-font-size: 14px;");
                hasError = true;
            } else if (niveauExp.trim().length() < 3) {
                expError.setText("é¢éÅ Le niveau d'expéé©rience doit contenir au moins 3 caractéé¨res.");
                expError.setVisible(true);
                expError.setManaged(true);
                expField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                "-fx-border-radius: 8; -fx-font-size: 14px;");
                hasError = true;
            } else if (niveauExp.trim().length() > 100) {
                expError.setText("é¢éÅ Le niveau d'expéé©rience ne peut pas déé©passer 100 caractéé¨res.");
                expError.setVisible(true);
                expError.setManaged(true);
                expField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                "-fx-border-radius: 8; -fx-font-size: 14px;");
                hasError = true;
            }
            
            // Validate Niveau Etude - not empty
            if (niveauEtude == null || niveauEtude.trim().isEmpty()) {
                etudeError.setText("é¢éÅ Le niveau d'éé©tude est obligatoire.");
                etudeError.setVisible(true);
                etudeError.setManaged(true);
                etudeField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
                hasError = true;
            } else if (niveauEtude.trim().length() < 3) {
                etudeError.setText("é¢éÅ Le niveau d'éé©tude doit contenir au moins 3 caractéé¨res.");
                etudeError.setVisible(true);
                etudeError.setManaged(true);
                etudeField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
                hasError = true;
            } else if (niveauEtude.trim().length() > 100) {
                etudeError.setText("é¢éÅ Le niveau d'éé©tude ne peut pas déé©passer 100 caractéé¨res.");
                etudeError.setVisible(true);
                etudeError.setManaged(true);
                etudeField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
                hasError = true;
            }
            
            // Validate Competences - not empty
            if (competences == null || competences.trim().isEmpty()) {
                compError.setText("é¢éÅ Les compéé©tences requises sont obligatoires.");
                compError.setVisible(true);
                compError.setManaged(true);
                compField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
                hasError = true;
            } else if (competences.trim().length() < 5) {
                compError.setText("é¢éÅ Les compéé©tences requises doivent contenir au moins 5 caractéé¨res.");
                compError.setVisible(true);
                compError.setManaged(true);
                compField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
                hasError = true;
            } else if (competences.trim().length() > 300) {
                compError.setText("é¢éÅ Les compéé©tences requises ne peuvent pas déé©passer 300 caractéé¨res.");
                compError.setVisible(true);
                compError.setManaged(true);
                compField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
                hasError = true;
            }
            
            // Validate Responsibilities - not empty
            if (responsibilities == null || responsibilities.trim().isEmpty()) {
                respError.setText("é¢éÅ Les responsabilitéé©s sont obligatoires.");
                respError.setVisible(true);
                respError.setManaged(true);
                respField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
                hasError = true;
            } else if (responsibilities.trim().length() < 10) {
                respError.setText("é¢éÅ Les responsabilitéé©s doivent contenir au moins 10 caractéé¨res.");
                respError.setVisible(true);
                respError.setManaged(true);
                respField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
                hasError = true;
            } else if (responsibilities.trim().length() > 500) {
                respError.setText("é¢éÅ Les responsabilitéé©s ne peuvent pas déé©passer 500 caractéé¨res.");
                respError.setVisible(true);
                respError.setManaged(true);
                respField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
                hasError = true;
            }
            
            if (hasError) {
                event.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == saveButton) {
                CritereOffre c = new CritereOffre();
                c.setNiveauExperience(expField.getText().trim());
                c.setNiveauEtude(etudeField.getText().trim());
                c.setCompetencesRequises(compField.getText().trim());
                c.setResponsibilities(respField.getText().trim());
                c.setIdOffre(offreId);
                return c;
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }



    private CritereOffre showCritereDialogForUpdate(CritereOffre existing) {

        Dialog<CritereOffre> dialog = new Dialog<>();
        dialog.setTitle("Modifier Critéé¨re");

        ButtonType saveButton = new ButtonType("Mettre éé  jour", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, cancelButton);

        // Container with dark theme
        VBox container = new VBox(20);
        container.setStyle("-fx-background-color: #1a1a2e; -fx-padding: 30;");
        container.setPrefWidth(450);

        // Title
        Label titleLabel = new Label("é¢Åéé¯é¸é Modifier le critéé¨re");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white; -fx-alignment: center;");
        VBox.setMargin(titleLabel, new javafx.geometry.Insets(0, 0, 10, 0));

        // Experience Field
        Label expLbl = new Label("é¢é­é NIVEAU D'EXPéé°RIENCE");
        expLbl.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 600;");
        TextField expField = new TextField(existing.getNiveauExperience());
        expField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                        "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #3d3d5c; " +
                        "-fx-border-radius: 8; -fx-font-size: 14px;");
        // Error label for experience
        Label expError = new Label(" ");
        expError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
        
        // Real-time validation for experience
        expField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                expError.setText("é¢éÅ Le niveau d'expéé©rience est obligatoire.");
                expError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                expField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                "-fx-border-radius: 8; -fx-font-size: 14px;");
            } else if (newVal.trim().length() < 3) {
                expError.setText("é¢éÅ Minimum 3 caractéé¨res (" + newVal.trim().length() + "/3)");
                expError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                expField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                "-fx-border-radius: 8; -fx-font-size: 14px;");
            } else if (newVal.trim().length() > 100) {
                expError.setText("é¢éÅ Maximum 100 caractéé¨res (" + newVal.trim().length() + "/100)");
                expError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                expField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                "-fx-border-radius: 8; -fx-font-size: 14px;");
            } else {
                expError.setText("é¢Åé¦ Valide");
                expError.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                expField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #10b981; -fx-border-width: 2; " +
                                "-fx-border-radius: 8; -fx-font-size: 14px;");
            }
        });

        // Etude Field
        Label etudeLbl = new Label("é°Å¸Å½é NIVEAU D'éé°TUDE");
        etudeLbl.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 600;");
        TextField etudeField = new TextField(existing.getNiveauEtude());
        etudeField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                          "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #3d3d5c; " +
                          "-fx-border-radius: 8; -fx-font-size: 14px;");
        // Error label for etude
        Label etudeError = new Label(" ");
        etudeError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
        
        // Real-time validation for etude
        etudeField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                etudeError.setText("é¢éÅ Le niveau d'éé©tude est obligatoire.");
                etudeError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                etudeField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
            } else if (newVal.trim().length() < 3) {
                etudeError.setText("é¢éÅ Minimum 3 caractéé¨res (" + newVal.trim().length() + "/3)");
                etudeError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                etudeField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
            } else if (newVal.trim().length() > 100) {
                etudeError.setText("é¢éÅ Maximum 100 caractéé¨res (" + newVal.trim().length() + "/100)");
                etudeError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                etudeField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
            } else {
                etudeError.setText("é¢Åé¦ Valide");
                etudeError.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                etudeField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #10b981; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
            }
        });

        // Competences Field
        Label compLbl = new Label("é°Å¸éé¡ COMPéé°TENCES REQUISES");
        compLbl.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 600;");
        TextArea compField = new TextArea(existing.getCompetencesRequises());
        compField.setPrefRowCount(4);
        compField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                         "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                         "-fx-background-radius: 8; -fx-border-color: #3d3d5c; -fx-border-radius: 8;");
        // Error label for competences
        Label compError = new Label(" ");
        compError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
        
        // Real-time validation for competences
        compField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                compError.setText("é¢éÅ Les compéé©tences requises sont obligatoires.");
                compError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                compField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
            } else if (newVal.trim().length() < 5) {
                compError.setText("é¢éÅ Minimum 5 caractéé¨res (" + newVal.trim().length() + "/5)");
                compError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                compField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
            } else if (newVal.trim().length() > 300) {
                compError.setText("é¢éÅ Maximum 300 caractéé¨res (" + newVal.trim().length() + "/300)");
                compError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                compField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
            } else {
                compError.setText("é¢Åé¦ Valide (" + newVal.trim().length() + "/300 caractéé¨res)");
                compError.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                compField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #10b981; -fx-border-width: 2; -fx-border-radius: 8;");
            }
        });
        
        // Add bullet point on Enter
        compField.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
                int pos = compField.getCaretPosition();
                String text = compField.getText();
                String newText = text.substring(0, pos) + "\né¢é¬é¢ " + text.substring(pos);
                compField.setText(newText);
                compField.positionCaret(pos + 3);
            }
        });

        // Responsibilities Field
        Label respLbl = new Label("é°Å¸éé¹ RESPONSABILITéé°S");
        respLbl.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 600;");
        TextArea respField = new TextArea(existing.getResponsibilities() != null ? existing.getResponsibilities() : "");
        respField.setPrefRowCount(4);
        respField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                         "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                         "-fx-background-radius: 8; -fx-border-color: #3d3d5c; -fx-border-radius: 8;");
        // Error label for responsibilities
        Label respError = new Label(" ");
        respError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
        
        // Real-time validation for responsibilities
        respField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                respError.setText("é¢éÅ Les responsabilitéé©s sont obligatoires.");
                respError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                respField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
            } else if (newVal.trim().length() < 10) {
                respError.setText("é¢éÅ Minimum 10 caractéé¨res (" + newVal.trim().length() + "/10)");
                respError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                respField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
            } else if (newVal.trim().length() > 500) {
                respError.setText("é¢éÅ Maximum 500 caractéé¨res (" + newVal.trim().length() + "/500)");
                respError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                respField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
            } else {
                respError.setText("é¢Åé¦ Valide (" + newVal.trim().length() + "/500 caractéé¨res)");
                respError.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                respField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #10b981; -fx-border-width: 2; -fx-border-radius: 8;");
            }
        });
        
        // Add bullet point on Enter for responsibilities
        respField.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
                int pos = respField.getCaretPosition();
                String text = respField.getText();
                String newText = text.substring(0, pos) + "\né¢é¬é¢ " + text.substring(pos);
                respField.setText(newText);
                respField.positionCaret(pos + 3);
            }
        });

        container.getChildren().addAll(
            titleLabel,
            expLbl, expField, expError,
            etudeLbl, etudeField, etudeError,
            respLbl, respField, respError,
            compLbl, compField, compError
        );

        dialog.getDialogPane().setContent(container);
        dialog.getDialogPane().setStyle("-fx-background-color: #1a1a2e; -fx-padding: 0;");
        
        // Style buttons to match modern design
        javafx.scene.Node saveBtnNode = dialog.getDialogPane().lookupButton(saveButton);
        javafx.scene.Node cancelBtnNode = dialog.getDialogPane().lookupButton(cancelButton);
        
        saveBtnNode.setStyle(
            "-fx-background-color: linear-gradient(to right, #a855f7, #ec4899); " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 14 35; " +
            "-fx-background-radius: 12; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(168, 85, 247, 0.4), 12, 0, 0, 4);"
        );
        
        cancelBtnNode.setStyle(
            "-fx-background-color: #2d2d48; " +
            "-fx-text-fill: #94a3b8; " +
            "-fx-font-weight: 600; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 14 35; " +
            "-fx-background-radius: 12; " +
            "-fx-cursor: hand; " +
            "-fx-border-color: transparent;"
        );
        
        // Add hover effects
        saveBtnNode.setOnMouseEntered(e -> saveBtnNode.setStyle(
            "-fx-background-color: linear-gradient(to right, #9333ea, #db2777); " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 14 35; " +
            "-fx-background-radius: 12; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(168, 85, 247, 0.6), 15, 0, 0, 5);"
        ));
        
        saveBtnNode.setOnMouseExited(e -> saveBtnNode.setStyle(
            "-fx-background-color: linear-gradient(to right, #a855f7, #ec4899); " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 14 35; " +
            "-fx-background-radius: 12; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(168, 85, 247, 0.4), 12, 0, 0, 4);"
        ));
        
        cancelBtnNode.setOnMouseEntered(e -> cancelBtnNode.setStyle(
            "-fx-background-color: #3d3d5c; " +
            "-fx-text-fill: #a8b3cf; " +
            "-fx-font-weight: 600; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 14 35; " +
            "-fx-background-radius: 12; " +
            "-fx-cursor: hand; " +
            "-fx-border-color: transparent;"
        ));
        
        cancelBtnNode.setOnMouseExited(e -> cancelBtnNode.setStyle(
            "-fx-background-color: #2d2d48; " +
            "-fx-text-fill: #94a3b8; " +
            "-fx-font-weight: 600; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 14 35; " +
            "-fx-background-radius: 12; " +
            "-fx-cursor: hand; " +
            "-fx-border-color: transparent;"
        ));

        // Add event filter to prevent dialog closing on validation failure
        saveBtnNode.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            // Hide all error labels first
            expError.setVisible(false);
            expError.setManaged(false);
            etudeError.setVisible(false);
            etudeError.setManaged(false);
            compError.setVisible(false);
            compError.setManaged(false);
            respError.setVisible(false);
            respError.setManaged(false);
            
            // Reset field styles
            expField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                            "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #3d3d5c; " +
                            "-fx-border-radius: 8; -fx-font-size: 14px;");
            etudeField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                              "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #3d3d5c; " +
                              "-fx-border-radius: 8; -fx-font-size: 14px;");
            compField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                             "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                             "-fx-background-radius: 8; -fx-border-color: #3d3d5c; -fx-border-radius: 8;");
            respField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                             "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                             "-fx-background-radius: 8; -fx-border-color: #3d3d5c; -fx-border-radius: 8;");
            
            boolean hasError = false;
            
            // Validate all fields are filled
            String niveauExp = expField.getText();
            String niveauEtude = etudeField.getText();
            String competences = compField.getText();
            String responsibilities = respField.getText();
            
            // Validate Niveau Experience - not empty
            if (niveauExp == null || niveauExp.trim().isEmpty()) {
                expError.setText("é¢éÅ Le niveau d'expéé©rience est obligatoire.");
                expError.setVisible(true);
                expError.setManaged(true);
                expField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                "-fx-border-radius: 8; -fx-font-size: 14px;");
                hasError = true;
            } else if (niveauExp.trim().length() < 3) {
                expError.setText("é¢éÅ Le niveau d'expéé©rience doit contenir au moins 3 caractéé¨res.");
                expError.setVisible(true);
                expError.setManaged(true);
                expField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                "-fx-border-radius: 8; -fx-font-size: 14px;");
                hasError = true;
            } else if (niveauExp.trim().length() > 100) {
                expError.setText("é¢éÅ Le niveau d'expéé©rience ne peut pas déé©passer 100 caractéé¨res.");
                expError.setVisible(true);
                expError.setManaged(true);
                expField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                "-fx-border-radius: 8; -fx-font-size: 14px;");
                hasError = true;
            }
            
            // Validate Niveau Etude - not empty
            if (niveauEtude == null || niveauEtude.trim().isEmpty()) {
                etudeError.setText("é¢éÅ Le niveau d'éé©tude est obligatoire.");
                etudeError.setVisible(true);
                etudeError.setManaged(true);
                etudeField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
                hasError = true;
            } else if (niveauEtude.trim().length() < 3) {
                etudeError.setText("é¢éÅ Le niveau d'éé©tude doit contenir au moins 3 caractéé¨res.");
                etudeError.setVisible(true);
                etudeError.setManaged(true);
                etudeField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
                hasError = true;
            } else if (niveauEtude.trim().length() > 100) {
                etudeError.setText("é¢éÅ Le niveau d'éé©tude ne peut pas déé©passer 100 caractéé¨res.");
                etudeError.setVisible(true);
                etudeError.setManaged(true);
                etudeField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
                hasError = true;
            }
            
            // Validate Competences - not empty
            if (competences == null || competences.trim().isEmpty()) {
                compError.setText("é¢éÅ Les compéé©tences requises sont obligatoires.");
                compError.setVisible(true);
                compError.setManaged(true);
                compField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
                hasError = true;
            } else if (competences.trim().length() < 5) {
                compError.setText("é¢éÅ Les compéé©tences requises doivent contenir au moins 5 caractéé¨res.");
                compError.setVisible(true);
                compError.setManaged(true);
                compField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
                hasError = true;
            } else if (competences.trim().length() > 300) {
                compError.setText("é¢éÅ Les compéé©tences requises ne peuvent pas déé©passer 300 caractéé¨res.");
                compError.setVisible(true);
                compError.setManaged(true);
                compField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
                hasError = true;
            }
            
            // Validate Responsibilities - not empty
            if (responsibilities == null || responsibilities.trim().isEmpty()) {
                respError.setText("é¢éÅ Les responsabilitéé©s sont obligatoires.");
                respError.setVisible(true);
                respError.setManaged(true);
                respField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
                hasError = true;
            } else if (responsibilities.trim().length() < 10) {
                respError.setText("é¢éÅ Les responsabilitéé©s doivent contenir au moins 10 caractéé¨res.");
                respError.setVisible(true);
                respError.setManaged(true);
                respField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
                hasError = true;
            } else if (responsibilities.trim().length() > 500) {
                respError.setText("é¢éÅ Les responsabilitéé©s ne peuvent pas déé©passer 500 caractéé¨res.");
                respError.setVisible(true);
                respError.setManaged(true);
                respField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
                hasError = true;
            }
            
            if (hasError) {
                event.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == saveButton) {
                CritereOffre c = new CritereOffre();
                c.setNiveauExperience(expField.getText().trim());
                c.setNiveauEtude(etudeField.getText().trim());
                c.setCompetencesRequises(compField.getText().trim());
                c.setResponsibilities(respField.getText().trim());
                c.setIdOffre(existing.getIdOffre());

                return c;
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }



    @FXML
    private void rafraichirTout() {

        // Reload offres
        offreTable.getItems().setAll(service.getAllOffres());

        // Reload criteres for selected offre
        OffreEmploi selected = offreTable.getSelectionModel().getSelectedItem();

        if (selected != null) {
            critereTable.getItems().setAll(
                    critereService.getByOffreId(selected.getIdOffre())
            );
        } else {
            critereTable.getItems().clear();
        }
    }
    private void loadOffres() {
        offreTable.getItems().setAll(service.getAllOffres());
    }
    private void loadCriteresForSelectedOffre(int offreId) {

        critereTable.getItems().setAll(
                critereService.getByOffreId(offreId)
        );
    }

    /**
     * Refreshes all statistics data and updates charts and cards.
     * Called when the statistics tab is opened or when refresh button is clicked.
     */
    @FXML
    private void refreshStatistics() {
        try {
            // Update statistics cards
            updateStatisticsCards();
            
            // Update pie charts
            updateStatusPieChart();
            updateContractTypePieChart();
            updateWorkPreferencePieChart();
            updateLocationBarChart();
            
            System.out.println("Statistics refreshed successfully");
        } catch (Exception e) {
            System.err.println("Error refreshing statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Updates the statistics cards at the top of the statistics tab.
     */
    private void updateStatisticsCards() {
        statsCardsContainer.getChildren().clear();
        
        int totalOffers = statsService.getTotalOffers();
        int activeOffers = statsService.getActiveOffers();
        double avgActiveTime = statsService.getAverageActiveTime();
        int offersLast7Days = statsService.getOffersInLastDays(7);
        
        // Card 1: Total Offers
        statsCardsContainer.getChildren().add(createStatCard(
            "é°Å¸éé¹", "Total des Offres", String.valueOf(totalOffers), 
            "#3b82f6", "Toutes les offres"
        ));
        
        // Card 2: Active Offers
        statsCardsContainer.getChildren().add(createStatCard(
            "é¢Åé¦", "Offres Actives", String.valueOf(activeOffers), 
            "#10b981", "Actuellement ouvertes"
        ));
        
        // Card 3: Average Active Time
        statsCardsContainer.getChildren().add(createStatCard(
            "é¢éé±é¯é¸é", "Duréé©e Moyenne", String.format("%.1f jours", avgActiveTime), 
            "#f59e0b", "Temps actif moyen"
        ));
        
        // Card 4: Recent Offers
        statsCardsContainer.getChildren().add(createStatCard(
            "é°Å¸é é¢", "Derniers 7 Jours", String.valueOf(offersLast7Days), 
            "#8b5cf6", "Nouvelles offres"
        ));
    }

    /**
     * Creates a statistics card with icon, title, value, and description.
     */
    private VBox createStatCard(String icon, String title, String value, String color, String description) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.TOP_LEFT);
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-padding: 25; " +
            "-fx-background-radius: 12; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
        );
        card.setPrefWidth(250);
        card.setMaxWidth(Region.USE_PREF_SIZE);
        
        // Icon and title row
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle(
            "-fx-font-size: 32px; " +
            "-fx-padding: 10; " +
            "-fx-background-color: " + color + "22; " +
            "-fx-background-radius: 10;"
        );
        
        VBox textContainer = new VBox(4);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-font-weight: 600;");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle(
            "-fx-font-size: 28px; " +
            "-fx-font-weight: 700; " +
            "-fx-text-fill: #1e293b;"
        );
        
        textContainer.getChildren().addAll(titleLabel, valueLabel);
        
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        
        card.getChildren().addAll(textContainer, descLabel);
        
        return card;
    }

    /**
     * Updates the status distribution pie chart.
     */
    private void updateStatusPieChart() {
        if (statusPieChart == null) return;
        
        statusPieChart.getData().clear();
        Map<String, Integer> statusData = statsService.getOffresByStatus();
        
        for (Map.Entry<String, Integer> entry : statusData.entrySet()) {
            PieChart.Data slice = new PieChart.Data(
                entry.getKey() + " (" + entry.getValue() + ")", 
                entry.getValue()
            );
            statusPieChart.getData().add(slice);
        }
        
        statusPieChart.setLabelsVisible(true);
        statusPieChart.setStartAngle(90);
    }

    /**
     * Updates the contract type distribution pie chart.
     */
    private void updateContractTypePieChart() {
        if (contractTypePieChart == null) return;
        
        contractTypePieChart.getData().clear();
        Map<String, Integer> contractData = statsService.getOffresByContractType();
        
        for (Map.Entry<String, Integer> entry : contractData.entrySet()) {
            PieChart.Data slice = new PieChart.Data(
                entry.getKey() + " (" + entry.getValue() + ")", 
                entry.getValue()
            );
            contractTypePieChart.getData().add(slice);
        }
        
        contractTypePieChart.setLabelsVisible(true);
        contractTypePieChart.setStartAngle(90);
    }

    /**
     * Updates the work preference distribution pie chart.
     */
    private void updateWorkPreferencePieChart() {
        if (workPrefPieChart == null) return;
        
        workPrefPieChart.getData().clear();
        Map<String, Integer> workPrefData = statsService.getOffresByWorkPreference();
        
        if (workPrefData.isEmpty()) {
            // Show message when no data
            PieChart.Data slice = new PieChart.Data("Aucune donnéé©e", 1);
            workPrefPieChart.getData().add(slice);
        } else {
            for (Map.Entry<String, Integer> entry : workPrefData.entrySet()) {
                PieChart.Data slice = new PieChart.Data(
                    entry.getKey() + " (" + entry.getValue() + ")", 
                    entry.getValue()
                );
                workPrefPieChart.getData().add(slice);
            }
        }
        
        workPrefPieChart.setLabelsVisible(true);
        workPrefPieChart.setStartAngle(90);
    }

    /**
     * Updates the top locations bar chart.
     */
    private void updateLocationBarChart() {
        if (locationBarChart == null) return;
        
        locationBarChart.getData().clear();
        Map<String, Integer> locationData = statsService.getOffresByLocation();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nombre d'offres");
        
        // Get top 5 locations
        locationData.entrySet().stream()
            .limit(5)
            .forEach(entry -> {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            });
        
        locationBarChart.getData().add(series);
        locationBarChart.setLegendVisible(false);
    }

    /**
     * Navigate back to the main administration view (user management).
     */
    @FXML
    private void goBackToAdmin() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/AdministrationView.fxml"));
            javafx.scene.Parent root = loader.load();
            
            javafx.stage.Stage stage = (javafx.stage.Stage) offreTable.getScene().getWindow();
            javafx.scene.Scene newScene = new javafx.scene.Scene(root, 1440, 1024);
            stage.setScene(newScene);
            stage.setTitle("Administration - VOS");
            stage.centerOnScreen();
            System.out.println("é Retour vers Administration");
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Impossible de retourner é  l'administration: " + e.getMessage());
            alert.showAndWait();
        }
    }



    
    /**
     * Load user information from UserSession and display in header
     */
    private void loadUserInfo() {
        try {
            entities.Utilisateur currentUser = utilis.UserSession.getInstance().getCurrentUser();
            
            if (currentUser != null) {
                // Set user name
                String fullName = currentUser.getPrenom() + " " + currentUser.getNom();
                userNameLabel.setText(fullName);
                
                // Set user role with proper formatting
                String role = currentUser.getRole();
                if (role != null) {
                    // Format role nicely
                    switch (role.toUpperCase()) {
                        case "ADMIN_TECHNIQUE":
                            userRoleLabel.setText("Administrateur Technique");
                            break;
                        case "ADMIN":
                            userRoleLabel.setText("Administrateur");
                            break;
                        case "CLIENT":
                            userRoleLabel.setText("Client");
                            break;
                        default:
                            userRoleLabel.setText(role);
                    }
                } else {
                    userRoleLabel.setText("Utilisateur");
                }
                
                // Set avatar - try to load profile image or use initials
                String profileImage = currentUser.getImage_profil();
                if (profileImage != null && !profileImage.trim().isEmpty()) {
                    try {
                        java.io.File imageFile = new java.io.File(profileImage);
                        if (imageFile.exists()) {
                            // Load image as avatar
                            ImageView avatar = new ImageView(new Image(imageFile.toURI().toString()));
                            avatar.setFitWidth(40);
                            avatar.setFitHeight(40);
                            avatar.setPreserveRatio(false);
                            
                            // Make it circular
                            javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(20, 20, 20);
                            avatar.setClip(clip);
                            
                            // Replace the StackPane content with the image
                            userAvatarContainer.getChildren().clear();
                            userAvatarContainer.getChildren().add(avatar);
                            
                            System.out.println(" Avatar chargé dans AdminOffresView: " + profileImage);
                        } else {
                            // Image file doesn't exist, use initials
                            setAvatarInitials(currentUser);
                        }
                    } catch (Exception e) {
                        System.err.println("Erreur chargement image avatar: " + e.getMessage());
                        setAvatarInitials(currentUser);
                    }
                } else {
                    // No profile image, use initials
                    setAvatarInitials(currentUser);
                }
                
            } else {
                System.err.println(" Aucun utilisateur en session!");
                userNameLabel.setText("Utilisateur");
                userRoleLabel.setText("Non connecté");
                userAvatarLabel.setText("?");
            }
            
        } catch (Exception e) {
            System.err.println("Erreur chargement info utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Set avatar to user's initials
     */
    private void setAvatarInitials(entities.Utilisateur user) {
        String initials = "";
        if (user.getPrenom() != null && !user.getPrenom().isEmpty()) {
            initials += user.getPrenom().charAt(0);
        }
        if (user.getNom() != null && !user.getNom().isEmpty()) {
            initials += user.getNom().charAt(0);
        }
        if (initials.isEmpty()) {
            initials = "U";
        }
        userAvatarLabel.setText(initials.toUpperCase());
    }
    
    /**
     * Setup sidebar navigation handlers
     */
    private void setupSidebarNavigation() {
        // Statistiques - switch to statistics tab
        navStatistiques.setOnMouseClicked(event -> {
            System.out.println(" Navigation: St atistiques");
            try {
                javafx.scene.control.TabPane tabPane = (javafx.scene.control.TabPane) offreTable.getParent().getParent();
                if (tabPane != null && tabPane.getTabs().size() > 2) {
                    tabPane.getSelectionModel().select(2);
                }
            } catch (Exception e) {
                System.err.println("Erreur navigation statistiques: " + e.getMessage());
            }
        });
        
        //Opportunités - switch to offres tab
        navOpportunites.setOnMouseClicked(event -> {
            System.out.println(" Navigation: Opportunités");
            try {
                javafx.scene.control.TabPane tabPane = (javafx.scene.control.TabPane) offreTable.getParent().getParent();
                if (tabPane != null && tabPane.getTabs().size() > 0) {
                    tabPane.getSelectionModel().select(0);
                }
            } catch (Exception e) {
                System.err.println("Erreur navigation opportunités: " + e.getMessage());
            }
        });
        
        // À propos - show about dialog
        navApropos.setOnMouseClicked(event -> {
            System.out.println("ℹ Navigation: À propos");
            Alert aboutAlert = new Alert(Alert.AlertType.INFORMATION);
            aboutAlert.setTitle("À propos - VOS");
            aboutAlert.setHeaderText("VOS - Volunteering Online System");
            aboutAlert.setContentText(
                "Version: 1.0.0\n" +
                "\n" +
                "Système de gestion des offres d'emploi\n" +
                "avec intégration IA pour amélioration automatique.\n" +
                "\n" +
                " 2026 VOS - Tous droits réservés"
            );
            aboutAlert.showAndWait();
        });
        
        // Paramètres - show settings dialog
        navParametres.setOnMouseClicked(event -> {
            System.out.println(" Navigation: Paramètres");
            Alert settingsAlert = new Alert(Alert.AlertType.INFORMATION);
            settingsAlert.setTitle("Paramètres");
            settingsAlert.setHeaderText("Configuration");
            settingsAlert.setContentText(
                "Les paramètres sont disponibles dans:\n" +
                "src/main/resources/config.properties\n" +
                "\n" +
                "Vous pouvez configurer:\n" +
                "- Fournisseur IA (Gemini, Groq, Claude, Demo)\n" +
                "- Clés API\n" +
                "- Paramètres email\n" +
                "\n" +
                "Redémarrez l'application après modification."
            );
            settingsAlert.showAndWait();
        });
        
        // Déconnexion - logout and return to signin
        navDeconnexion.setOnMouseClicked(event -> {
            System.out.println(" Déconnexion demandée");
            
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Déconnexion");
            confirmAlert.setHeaderText("Confirmer la déconnexion");
            confirmAlert.setContentText("êtes-vous sûr de vouloir vous déconnecter ?");
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    utilis.UserSession.getInstance().clearSession();
                    
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/SigninView.fxml"));
                    javafx.scene.Parent root = loader.load();
                    
                    javafx.stage.Stage stage = (javafx.stage.Stage) offreTable.getScene().getWindow();
                    javafx.scene.Scene newScene = new javafx.scene.Scene(root);
                    stage.setScene(newScene);
                    stage.setTitle("Connexion - VOS");
                    stage.centerOnScreen();
                    
                    System.out.println(" Déconnexion réussie");
                } catch (Exception e) {
                    e.printStackTrace();
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Erreur");
                    errorAlert.setContentText("Erreur lors de la déconnexion: " + e.getMessage());
                    errorAlert.showAndWait();
                }
            }
        });
    }
}

