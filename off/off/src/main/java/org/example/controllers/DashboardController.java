package org.example.controllers;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.entities.OffreEmploi;
import org.example.services.OffreEmploiService;
import javafx.scene.layout.HBox;
import java.sql.Date;
import java.util.List;
import java.util.Optional;
import javafx.scene.layout.GridPane;
import org.example.entities.CritereOffre;
import org.example.services.CritereOffreService;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.util.Duration;




public class DashboardController {

    // Top search
    @FXML private TextField searchField;
    
    // Sidebar elements
    @FXML private VBox sidebar;
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

    // Table
    @FXML private TableView<OffreEmploi> offreTable;
    @FXML private TableColumn<OffreEmploi, Integer> colIdOffre;
    @FXML private TableColumn<OffreEmploi, String> colTitre;
    @FXML private TableColumn<OffreEmploi, String> colDescription;
    @FXML private TableColumn<OffreEmploi, String> colTypeContrat;
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
    @FXML private TableColumn<CritereOffre, String> colCompetences;
    @FXML private TableColumn<CritereOffre, Integer> colIdOffreCritere;

    @FXML
    private void rafraichirCriteres() {
        loadCriteresForSelectedOffre();
    }



    private final OffreEmploiService service = new OffreEmploiService();
    private final ObservableList<OffreEmploi> data = FXCollections.observableArrayList();
    private CritereOffreService critereService = new CritereOffreService();
    private Integer currentOffreId = null;

    /**
     * Fetches all distinct user IDs from the offre_emploi table in the database.
     * This method retrieves unique user IDs that have created job offers.
     * 
     * @return A List of Integer containing all distinct user IDs, or an empty list if none found
     */
    private List<Integer> getAllUserIds() {
        List<Integer> userIds = new java.util.ArrayList<>();
        String sql = "SELECT DISTINCT id_utilisateur FROM offre_emploi ORDER BY id_utilisateur";
        
        try (java.sql.PreparedStatement ps = org.example.utils.MyDataBase.getInstance().getConnection().prepareStatement(sql);
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
        colStatutOffre.setCellValueFactory(new PropertyValueFactory<>("statutOffre"));
        colDatePublication.setCellValueFactory(new PropertyValueFactory<>("datePublication"));
        colIdUtilisateur.setCellValueFactory(new PropertyValueFactory<>("idUtilisateur"));

        offreTable.setItems(data);

        colIdCritere.setCellValueFactory(new PropertyValueFactory<>("idCritere"));
        colNiveauExperience.setCellValueFactory(new PropertyValueFactory<>("niveauExperience"));
        colNiveauEtude.setCellValueFactory(new PropertyValueFactory<>("niveauEtude"));
        colCompetences.setCellValueFactory(new PropertyValueFactory<>("competencesRequises"));
        colIdOffreCritere.setCellValueFactory(new PropertyValueFactory<>("idOffre"));

        offreTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        currentOffreId = newSelection.getIdOffre();
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

        // Simple search (filters in-memory)
        searchField.textProperty().addListener((obs, oldV, newV) -> applySearch(newV));
        
        // Setup sidebar hover effect
        setupSidebarHoverEffect();
    }
    
    private void setupSidebarHoverEffect() {
        // Set initial state
        sidebar.setPrefWidth(60.0);
        
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
                new KeyValue(sidebar.prefWidthProperty(), 200),
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
                new KeyValue(labelDeconnexion.maxWidthProperty(), 150)
            )
        );
        expandTimeline.play();
    }
    
    private void collapseSidebar() {
        Timeline collapseTimeline = new Timeline(
            new KeyFrame(Duration.millis(250),
                new KeyValue(sidebar.prefWidthProperty(), 60),
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
                new KeyValue(labelDeconnexion.maxWidthProperty(), 0)
            )
        );
        collapseTimeline.play();
    }
    
    private void addNavItemHoverEffect(HBox navItem) {
        navItem.setOnMouseEntered(e -> {
            navItem.setStyle(navItem.getStyle() + "-fx-background-color: rgba(59, 130, 246, 0.3);");
        });
        
        navItem.setOnMouseExited(e -> {
            navItem.setStyle(navItem.getStyle().replace("-fx-background-color: rgba(59, 130, 246, 0.3);", "-fx-background-color: transparent;"));
        });
    }
    private void supprimerCritere(CritereOffre critere) {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer ce critère ?");
        confirm.setContentText("Cette action est irréversible.");

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
                    "Sélectionnez une offre d'abord !");
            return;
        }

        CritereOffre c = showCritereDialog(currentOffreId);

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
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", "Sélectionnez une offre à supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'offre #" + selected.getIdOffre() + " ?");
        confirm.setContentText("Cette action est irréversible.");

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
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", "Sélectionnez une offre.");
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

        ButtonType saveButton = new ButtonType(existing == null ? "Ajouter" : "Mettre à jour", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, cancelButton);

        // Container with dark theme
        VBox container = new VBox(20);
        container.setStyle("-fx-background-color: #1a1a2e; -fx-padding: 30;");
        container.setPrefWidth(450);

        // Title
        Label titleLabel = new Label(existing == null ? "📝 Ajouter une nouvelle offre" : "✏️ Modifier l'offre");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white; -fx-alignment: center;");
        VBox.setMargin(titleLabel, new javafx.geometry.Insets(0, 0, 10, 0));

        // FORM FIELDS with modern dark design
        // Titre
        Label titreLbl = new Label("📋 TITRE");
        titreLbl.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 600;");
        TextField titreField = new TextField();
        titreField.setPromptText("Ex: Développeur Full Stack");
        titreField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                          "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #3d3d5c; " +
                          "-fx-border-radius: 8; -fx-font-size: 14px;");
        // Error label for titre
        Label titreError = new Label(" ");
        titreError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
        
        // Real-time validation for titre
        titreField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                titreError.setText("❌ Le titre est obligatoire.");
                titreError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                titreField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
            } else if (newVal.trim().length() < 5) {
                titreError.setText("❌ Le titre doit contenir au moins 5 caractères (" + newVal.trim().length() + "/5)");
                titreError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                titreField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
            } else if (newVal.trim().length() > 100) {
                titreError.setText("❌ Le titre ne peut pas dépasser 100 caractères (" + newVal.trim().length() + "/100)");
                titreError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                titreField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
            } else {
                titreError.setText("✅ Valide");
                titreError.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                titreField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #10b981; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
            }
        });

        // Description
        Label descLbl = new Label("📝 DESCRIPTION");
        descLbl.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 600;");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Décrivez l'offre d'emploi...");
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                "-fx-background-radius: 8; -fx-border-color: #3d3d5c; -fx-border-radius: 8;");
        // Error label for description
        Label descError = new Label(" ");
        descError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
        
        // Real-time validation for description
        descriptionArea.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                descError.setText("❌ La description est obligatoire.");
                descError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                descriptionArea.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                        "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                        "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
            } else if (newVal.trim().length() < 20) {
                descError.setText("❌ La description doit contenir au moins 20 caractères (" + newVal.trim().length() + "/20)");
                descError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                descriptionArea.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                        "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                        "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
            } else if (newVal.trim().length() > 500) {
                descError.setText("❌ La description ne peut pas dépasser 500 caractères (" + newVal.trim().length() + "/500)");
                descError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                descriptionArea.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                        "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                        "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
            } else {
                descError.setText("✅ Valide (" + newVal.trim().length() + "/500 caractères)");
                descError.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                descriptionArea.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                        "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                        "-fx-background-radius: 8; -fx-border-color: #10b981; -fx-border-width: 2; -fx-border-radius: 8;");
            }
        });

        // Type Contrat
        Label typeLbl = new Label("💼 TYPE DE CONTRAT");
        typeLbl.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 600;");
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("CDI", "CDD", "Stage", "Alternance", "Freelance", "Intérim");
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

        // Statut
        Label statutLbl = new Label("📊 STATUT");
        statutLbl.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 600;");
        ComboBox<String> statutCombo = new ComboBox<>();
        statutCombo.getItems().addAll("ACTIVE", "INACTIVE", "ARCHIVED");
        statutCombo.setValue("ACTIVE");
        statutCombo.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-background-radius: 8; " +
                           "-fx-border-color: #3d3d5c; -fx-border-radius: 8;");
        statutCombo.setPrefWidth(400);
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

        // User ID
        Label userLbl = new Label("👤 ID UTILISATEUR");
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
        
        userIdCombo.setPromptText("Sélectionnez un utilisateur");
        userIdCombo.setEditable(true); // Allow manual entry for new user IDs
        userIdCombo.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-background-radius: 8; " +
                           "-fx-border-color: #3d3d5c; -fx-border-radius: 8;");
        userIdCombo.setPrefWidth(400);
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
                userError.setText("❌ L'ID utilisateur est obligatoire.");
                userError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
            } else if (value == null && editorText != null && !editorText.trim().isEmpty()) {
                try {
                    int id = Integer.parseInt(editorText.trim());
                    if (id <= 0) {
                        userError.setText("❌ L'ID doit être un nombre positif.");
                        userError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                    } else {
                        userError.setText("✅ Valide");
                        userError.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                    }
                } catch (NumberFormatException e) {
                    userError.setText("❌ L'ID doit être un nombre valide.");
                    userError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                }
            } else {
                userError.setText("✅ Valide");
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
        }

        container.getChildren().addAll(
            titleLabel,
            titreLbl, titreField, titreError,
            descLbl, descriptionArea, descError,
            typeLbl, typeCombo,
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
                titreError.setText("❌ Le titre est obligatoire.");
                titreError.setVisible(true);
                titreError.setManaged(true);
                titreField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
                hasError = true;
            } else if (titre.trim().length() < 5) {
                titreError.setText("❌ Le titre doit contenir au moins 5 caractères.");
                titreError.setVisible(true);
                titreError.setManaged(true);
                titreField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
                hasError = true;
            } else if (titre.trim().length() > 100) {
                titreError.setText("❌ Le titre ne peut pas dépasser 100 caractères.");
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
                descError.setText("❌ La description est obligatoire.");
                descError.setVisible(true);
                descError.setManaged(true);
                descriptionArea.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                        "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                        "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
                hasError = true;
            } else if (description.trim().length() < 20) {
                descError.setText("❌ La description doit contenir au moins 20 caractères.");
                descError.setVisible(true);
                descError.setManaged(true);
                descriptionArea.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                        "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                        "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
                hasError = true;
            } else if (description.trim().length() > 500) {
                descError.setText("❌ La description ne peut pas dépasser 500 caractères.");
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
                            userError.setText("❌ L'ID utilisateur doit être un nombre positif.");
                            userError.setVisible(true);
                            userError.setManaged(true);
                            hasError = true;
                        } else {
                            userIdCombo.setValue(userId); // Set the parsed value
                        }
                    } catch (NumberFormatException e) {
                        userError.setText("❌ ID utilisateur invalide. Veuillez entrer un nombre.");
                        userError.setVisible(true);
                        userError.setManaged(true);
                        hasError = true;
                    }
                } else {
                    userError.setText("❌ Veuillez sélectionner ou entrer un ID utilisateur.");
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
                    o.setStatutOffre(statutCombo.getValue());
                    o.setDatePublication(new java.sql.Date(System.currentTimeMillis()));
                    
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
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la création de l'offre");
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
    private CritereOffre showCritereDialog(int offreId) {

        Dialog<CritereOffre> dialog = new Dialog<>();
        dialog.setTitle("Ajouter Critère");

        ButtonType saveButton = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, cancelButton);

        // Container with dark theme
        VBox container = new VBox(20);
        container.setStyle("-fx-background-color: #1a1a2e; -fx-padding: 30;");
        container.setPrefWidth(450);

        // Title
        Label titleLabel = new Label("✨ Ajouter un critère");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white; -fx-alignment: center;");
        VBox.setMargin(titleLabel, new javafx.geometry.Insets(0, 0, 10, 0));

        // Experience Field
        Label expLbl = new Label("⭐ NIVEAU D'EXPÉRIENCE");
        expLbl.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 600;");
        TextField expField = new TextField();
        expField.setPromptText("Ex: 2 ans minimum d'expérience");
        expField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                        "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #3d3d5c; " +
                        "-fx-border-radius: 8; -fx-font-size: 14px;");
        // Error label for experience
        Label expError = new Label(" ");
        expError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
        
        // Real-time validation for experience
        expField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                expError.setText("❌ Le niveau d'expérience est obligatoire.");
                expError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                expField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                "-fx-border-radius: 8; -fx-font-size: 14px;");
            } else if (newVal.trim().length() < 3) {
                expError.setText("❌ Minimum 3 caractères (" + newVal.trim().length() + "/3)");
                expError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                expField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                "-fx-border-radius: 8; -fx-font-size: 14px;");
            } else if (newVal.trim().length() > 100) {
                expError.setText("❌ Maximum 100 caractères  (" + newVal.trim().length() + "/100)");
                expError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                expField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                "-fx-border-radius: 8; -fx-font-size: 14px;");
            } else {
                expError.setText("✅ Valide");
                expError.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                expField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #10b981; -fx-border-width: 2; " +
                                "-fx-border-radius: 8; -fx-font-size: 14px;");
            }
        });

        // Etude Field
        Label etudeLbl = new Label("🎓 NIVEAU D'ÉTUDE");
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
                etudeError.setText("❌ Le niveau d'étude est obligatoire.");
                etudeError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                etudeField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
            } else if (newVal.trim().length() < 3) {
                etudeError.setText("❌ Minimum 3 caractères (" + newVal.trim().length() + "/3)");
                etudeError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                etudeField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
            } else if (newVal.trim().length() > 100) {
                etudeError.setText("❌ Maximum 100 caractères (" + newVal.trim().length() + "/100)");
                etudeError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                etudeField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
            } else {
                etudeError.setText("✅ Valide");
                etudeError.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                etudeField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #10b981; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
            }
        });

        // Competences Field
        Label compLbl = new Label("💡 COMPÉTENCES REQUISES");
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
                compError.setText("❌ Les compétences requises sont obligatoires.");
                compError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                compField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
            } else if (newVal.trim().length() < 5) {
                compError.setText("❌ Minimum 5 caractères (" + newVal.trim().length() + "/5)");
                compError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                compField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
            } else if (newVal.trim().length() > 300) {
                compError.setText("❌ Maximum 300 caractères (" + newVal.trim().length() + "/300)");
                compError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding:  2 0 0 0; -fx-min-height: 16;");
                compField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
            } else {
                compError.setText("✅ Valide (" + newVal.trim().length() + "/300 caractères)");
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
                String newText = text.substring(0, pos) + "\n• " + text.substring(pos);
                compField.setText(newText);
                compField.positionCaret(pos + 3);
            }
        });
        
        // Add bullet point at start if empty
        compField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused && compField.getText().isEmpty()) {
                compField.setText("• ");
                compField.positionCaret(2);
            }
        });

        container.getChildren().addAll(
            titleLabel,
            expLbl, expField, expError,
            etudeLbl, etudeField, etudeError,
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
            
            boolean hasError = false;
            
            // Validate all fields are filled
            String niveauExp = expField.getText();
            String niveauEtude = etudeField.getText();
            String competences = compField.getText();
            
            // Validate Niveau Experience - not empty
            if (niveauExp == null || niveauExp.trim().isEmpty()) {
                expError.setText("❌ Le niveau d'expérience est obligatoire.");
                expError.setVisible(true);
                expError.setManaged(true);
                expField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                "-fx-border-radius: 8; -fx-font-size: 14px;");
                hasError = true;
            } else if (niveauExp.trim().length() < 3) {
                expError.setText("❌ Le niveau d'expérience doit contenir au moins 3 caractères.");
                expError.setVisible(true);
                expError.setManaged(true);
                expField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                "-fx-border-radius: 8; -fx-font-size: 14px;");
                hasError = true;
            } else if (niveauExp.trim().length() > 100) {
                expError.setText("❌ Le niveau d'expérience ne peut pas dépasser 100 caractères.");
                expError.setVisible(true);
                expError.setManaged(true);
                expField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                "-fx-border-radius: 8; -fx-font-size: 14px;");
                hasError = true;
            }
            
            // Validate Niveau Etude - not empty
            if (niveauEtude == null || niveauEtude.trim().isEmpty()) {
                etudeError.setText("❌ Le niveau d'étude est obligatoire.");
                etudeError.setVisible(true);
                etudeError.setManaged(true);
                etudeField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
                hasError = true;
            } else if (niveauEtude.trim().length() < 3) {
                etudeError.setText("❌ Le niveau d'étude doit contenir au moins 3 caractères.");
                etudeError.setVisible(true);
                etudeError.setManaged(true);
                etudeField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
                hasError = true;
            } else if (niveauEtude.trim().length() > 100) {
                etudeError.setText("❌ Le niveau d'étude ne peut pas dépasser 100 caractères.");
                etudeError.setVisible(true);
                etudeError.setManaged(true);
                etudeField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
                hasError = true;
            }
            
            // Validate Competences - not empty
            if (competences == null || competences.trim().isEmpty()) {
                compError.setText("❌ Les compétences requises sont obligatoires.");
                compError.setVisible(true);
                compError.setManaged(true);
                compField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
                hasError = true;
            } else if (competences.trim().length() < 5) {
                compError.setText("❌ Les compétences requises doivent contenir au moins 5 caractères.");
                compError.setVisible(true);
                compError.setManaged(true);
                compField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
                hasError = true;
            } else if (competences.trim().length() > 300) {
                compError.setText("❌ Les compétences requises ne peuvent pas dépasser 300 caractères.");
                compError.setVisible(true);
                compError.setManaged(true);
                compField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
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
                c.setIdOffre(offreId);
                return c;
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }



    private CritereOffre showCritereDialogForUpdate(CritereOffre existing) {

        Dialog<CritereOffre> dialog = new Dialog<>();
        dialog.setTitle("Modifier Critère");

        ButtonType saveButton = new ButtonType("Mettre à jour", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, cancelButton);

        // Container with dark theme
        VBox container = new VBox(20);
        container.setStyle("-fx-background-color: #1a1a2e; -fx-padding: 30;");
        container.setPrefWidth(450);

        // Title
        Label titleLabel = new Label("✏️ Modifier le critère");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white; -fx-alignment: center;");
        VBox.setMargin(titleLabel, new javafx.geometry.Insets(0, 0, 10, 0));

        // Experience Field
        Label expLbl = new Label("⭐ NIVEAU D'EXPÉRIENCE");
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
                expError.setText("❌ Le niveau d'expérience est obligatoire.");
                expError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                expField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                "-fx-border-radius: 8; -fx-font-size: 14px;");
            } else if (newVal.trim().length() < 3) {
                expError.setText("❌ Minimum 3 caractères (" + newVal.trim().length() + "/3)");
                expError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                expField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                "-fx-border-radius: 8; -fx-font-size: 14px;");
            } else if (newVal.trim().length() > 100) {
                expError.setText("❌ Maximum 100 caractères (" + newVal.trim().length() + "/100)");
                expError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                expField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                "-fx-border-radius: 8; -fx-font-size: 14px;");
            } else {
                expError.setText("✅ Valide");
                expError.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                expField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #10b981; -fx-border-width: 2; " +
                                "-fx-border-radius: 8; -fx-font-size: 14px;");
            }
        });

        // Etude Field
        Label etudeLbl = new Label("🎓 NIVEAU D'ÉTUDE");
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
                etudeError.setText("❌ Le niveau d'étude est obligatoire.");
                etudeError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                etudeField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
            } else if (newVal.trim().length() < 3) {
                etudeError.setText("❌ Minimum 3 caractères (" + newVal.trim().length() + "/3)");
                etudeError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                etudeField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
            } else if (newVal.trim().length() > 100) {
                etudeError.setText("❌ Maximum 100 caractères (" + newVal.trim().length() + "/100)");
                etudeError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                etudeField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
            } else {
                etudeError.setText("✅ Valide");
                etudeError.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                etudeField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #10b981; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
            }
        });

        // Competences Field
        Label compLbl = new Label("💡 COMPÉTENCES REQUISES");
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
                compError.setText("❌ Les compétences requises sont obligatoires.");
                compError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                compField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
            } else if (newVal.trim().length() < 5) {
                compError.setText("❌ Minimum 5 caractères (" + newVal.trim().length() + "/5)");
                compError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                compField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
            } else if (newVal.trim().length() > 300) {
                compError.setText("❌ Maximum 300 caractères (" + newVal.trim().length() + "/300)");
                compError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0; -fx-min-height: 16;");
                compField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
            } else {
                compError.setText("✅ Valide (" + newVal.trim().length() + "/300 caractères)");
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
                String newText = text.substring(0, pos) + "\n• " + text.substring(pos);
                compField.setText(newText);
                compField.positionCaret(pos + 3);
            }
        });

        container.getChildren().addAll(
            titleLabel,
            expLbl, expField, expError,
            etudeLbl, etudeField, etudeError,
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
            
            boolean hasError = false;
            
            // Validate all fields are filled
            String niveauExp = expField.getText();
            String niveauEtude = etudeField.getText();
            String competences = compField.getText();
            
            // Validate Niveau Experience - not empty
            if (niveauExp == null || niveauExp.trim().isEmpty()) {
                expError.setText("❌ Le niveau d'expérience est obligatoire.");
                expError.setVisible(true);
                expError.setManaged(true);
                expField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                "-fx-border-radius: 8; -fx-font-size: 14px;");
                hasError = true;
            } else if (niveauExp.trim().length() < 3) {
                expError.setText("❌ Le niveau d'expérience doit contenir au moins 3 caractères.");
                expError.setVisible(true);
                expError.setManaged(true);
                expField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                "-fx-border-radius: 8; -fx-font-size: 14px;");
                hasError = true;
            } else if (niveauExp.trim().length() > 100) {
                expError.setText("❌ Le niveau d'expérience ne peut pas dépasser 100 caractères.");
                expError.setVisible(true);
                expError.setManaged(true);
                expField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                "-fx-border-radius: 8; -fx-font-size: 14px;");
                hasError = true;
            }
            
            // Validate Niveau Etude - not empty
            if (niveauEtude == null || niveauEtude.trim().isEmpty()) {
                etudeError.setText("❌ Le niveau d'étude est obligatoire.");
                etudeError.setVisible(true);
                etudeError.setManaged(true);
                etudeField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
                hasError = true;
            } else if (niveauEtude.trim().length() < 3) {
                etudeError.setText("❌ Le niveau d'étude doit contenir au moins 3 caractères.");
                etudeError.setVisible(true);
                etudeError.setManaged(true);
                etudeField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
                hasError = true;
            } else if (niveauEtude.trim().length() > 100) {
                etudeError.setText("❌ Le niveau d'étude ne peut pas dépasser 100 caractères.");
                etudeError.setVisible(true);
                etudeError.setManaged(true);
                etudeField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                                  "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; " +
                                  "-fx-border-radius: 8; -fx-font-size: 14px;");
                hasError = true;
            }
            
            // Validate Competences - not empty
            if (competences == null || competences.trim().isEmpty()) {
                compError.setText("❌ Les compétences requises sont obligatoires.");
                compError.setVisible(true);
                compError.setManaged(true);
                compField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
                hasError = true;
            } else if (competences.trim().length() < 5) {
                compError.setText("❌ Les compétences requises doivent contenir au moins 5 caractères.");
                compError.setVisible(true);
                compError.setManaged(true);
                compField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                 "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                 "-fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
                hasError = true;
            } else if (competences.trim().length() > 300) {
                compError.setText("❌ Les compétences requises ne peuvent pas dépasser 300 caractères.");
                compError.setVisible(true);
                compError.setManaged(true);
                compField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
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



}
