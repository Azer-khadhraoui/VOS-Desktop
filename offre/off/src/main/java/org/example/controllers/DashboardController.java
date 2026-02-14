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




public class DashboardController {

    // Top search
    @FXML private TextField searchField;

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

        ButtonType saveButton = new ButtonType("Mettre à jour", ButtonBar.ButtonData.OK_DONE);
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

        // Description
        Label descLbl = new Label("📝 DESCRIPTION");
        descLbl.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 600;");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Décrivez l'offre d'emploi...");
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                                "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                                "-fx-background-radius: 8; -fx-border-color: #3d3d5c; -fx-border-radius: 8;");

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
        TextField userIdField = new TextField();
        userIdField.setPromptText("Ex: 1");
        userIdField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                           "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #3d3d5c; " +
                          "-fx-border-radius: 8; -fx-font-size: 14px;");

        if (existing != null) {
            titreField.setText(existing.getTitre());
            descriptionArea.setText(existing.getDescription());
            typeCombo.setValue(existing.getTypeContrat());
            statutCombo.setValue(existing.getStatutOffre());
            userIdField.setText(String.valueOf(existing.getIdUtilisateur()));
        }

        container.getChildren().addAll(
            titleLabel,
            titreLbl, titreField,
            descLbl, descriptionArea,
            typeLbl, typeCombo,
            statutLbl, statutCombo,
            userLbl, userIdField
        );

        dialog.getDialogPane().setContent(container);
        dialog.getDialogPane().setStyle("-fx-background-color: #1a1a2e;");
        
        // Style buttons
        dialog.getDialogPane().lookupButton(saveButton).setStyle(
            "-fx-background-color: linear-gradient(to right, #8b5cf6, #d946ef); " +
            "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 30; " +
            "-fx-background-radius: 10; -fx-cursor: hand;"
        );
        dialog.getDialogPane().lookupButton(cancelButton).setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #9ca3af; " +
            "-fx-border-color: #3d3d5c; -fx-border-radius: 10; -fx-background-radius: 10; " +
            "-fx-padding: 12 30; -fx-font-weight: bold;"
        );

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                try {
                    int userId = Integer.parseInt(userIdField.getText());

                    OffreEmploi o = new OffreEmploi();
                    o.setTitre(titreField.getText());
                    o.setDescription(descriptionArea.getText());
                    o.setTypeContrat(typeCombo.getValue());
                    o.setStatutOffre(statutCombo.getValue());
                    o.setDatePublication(new java.sql.Date(System.currentTimeMillis()));
                    o.setIdUtilisateur(userId);

                    return o;

                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "ID utilisateur invalide");
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

        // Etude Field
        Label etudeLbl = new Label("🎓 NIVEAU D'ÉTUDE");
        etudeLbl.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 600;");
        TextField etudeField = new TextField();
        etudeField.setPromptText("Ex: Bac +5 en informatique");
        etudeField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                          "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #3d3d5c; " +
                          "-fx-border-radius: 8; -fx-font-size: 14px;");

        // Competences Field
        Label compLbl = new Label("💡 COMPÉTENCES REQUISES");
        compLbl.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 600;");
        TextArea compField = new TextArea();
        compField.setPromptText("Ex: Java, Spring Boot, React...");
        compField.setPrefRowCount(4);
        compField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                         "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                         "-fx-background-radius: 8; -fx-border-color: #3d3d5c; -fx-border-radius: 8;");
        
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
            expLbl, expField,
            etudeLbl, etudeField,
            compLbl, compField
        );

        dialog.getDialogPane().setContent(container);
        dialog.getDialogPane().setStyle("-fx-background-color: #1a202c;");
        
        // Style buttons
        dialog.getDialogPane().lookupButton(saveButton).setStyle(
            "-fx-background-color: linear-gradient(to right, #7c3aed, #ec4899); " +
            "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 30; " +
            "-fx-background-radius: 10; -fx-cursor: hand;"
        );
        dialog.getDialogPane().lookupButton(cancelButton).setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #718096; " +
            "-fx-border-color: #2d3748; -fx-border-radius: 10; -fx-background-radius: 10; " +
            "-fx-padding: 12 30; -fx-font-weight: bold;"
        );

        dialog.setResultConverter(btn -> {
            if (btn == saveButton) {
                CritereOffre c = new CritereOffre();
                c.setNiveauExperience(expField.getText());
                c.setNiveauEtude(etudeField.getText());
                c.setCompetencesRequises(compField.getText());
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

        // Etude Field
        Label etudeLbl = new Label("🎓 NIVEAU D'ÉTUDE");
        etudeLbl.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 600;");
        TextField etudeField = new TextField(existing.getNiveauEtude());
        etudeField.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; " +
                          "-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #3d3d5c; " +
                          "-fx-border-radius: 8; -fx-font-size: 14px;");

        // Competences Field
        Label compLbl = new Label("💡 COMPÉTENCES REQUISES");
        compLbl.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 600;");
        TextArea compField = new TextArea(existing.getCompetencesRequises());
        compField.setPrefRowCount(4);
        compField.setStyle("-fx-control-inner-background: #2d2d48; -fx-text-fill: white; " +
                         "-fx-prompt-text-fill: #6b7280; -fx-background-color: #2d2d48; " +
                         "-fx-background-radius: 8; -fx-border-color: #3d3d5c; -fx-border-radius: 8;");
        
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
            expLbl, expField,
            etudeLbl, etudeField,
            compLbl, compField
        );

        dialog.getDialogPane().setContent(container);
        dialog.getDialogPane().setStyle("-fx-background-color: #1a1a2e;");
        
        // Style buttons
        dialog.getDialogPane().lookupButton(saveButton).setStyle(
            "-fx-background-color: linear-gradient(to right, #8b5cf6, #d946ef); " +
            "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 30; " +
            "-fx-background-radius: 10; -fx-cursor: hand;"
        );
        dialog.getDialogPane().lookupButton(cancelButton).setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #9ca3af; " +
            "-fx-border-color: #3d3d5c; -fx-border-radius: 10; -fx-background-radius: 10; " +
            "-fx-padding: 12 30; -fx-font-weight: bold;"
        );

        dialog.setResultConverter(btn -> {
            if (btn == saveButton) {

                CritereOffre c = new CritereOffre();
                c.setNiveauExperience(expField.getText());
                c.setNiveauEtude(etudeField.getText());
                c.setCompetencesRequises(compField.getText());
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
