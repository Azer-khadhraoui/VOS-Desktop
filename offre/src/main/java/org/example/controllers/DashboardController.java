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

        ButtonType saveButton = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        // FORM FIELDS
        TextField titreField = new TextField();
        TextArea descriptionArea = new TextArea();
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("CDI", "CDD", "Stage", "Alternance", "Freelance", "Intérim");
        typeCombo.setValue("CDI");
        ComboBox<String> statutCombo = new ComboBox<>();
        statutCombo.getItems().addAll("ACTIVE", "INACTIVE", "ARCHIVED");
        statutCombo.setValue("ACTIVE");

        TextField userIdField = new TextField();

        descriptionArea.setPrefRowCount(3);

        if (existing != null) {
            titreField.setText(existing.getTitre());
            descriptionArea.setText(existing.getDescription());
            typeCombo.setValue(existing.getTypeContrat());
            statutCombo.setValue(existing.getStatutOffre());
            userIdField.setText(String.valueOf(existing.getIdUtilisateur()));
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Titre:"), 0, 0);
        grid.add(titreField, 1, 0);

        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionArea, 1, 1);

        grid.add(new Label("Type Contrat:"), 0, 2);
        grid.add(typeCombo, 1, 2);

        grid.add(new Label("Statut:"), 0, 3);
        grid.add(statutCombo, 1, 3);


        grid.add(new Label("ID Utilisateur:"), 0, 4);
        grid.add(userIdField, 1, 4);

        dialog.getDialogPane().setContent(grid);

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

        ButtonType saveButton = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        TextField expField = new TextField();
        TextField etudeField = new TextField();
        TextArea compField = new TextArea();
        compField.setPromptText("Appuyez sur Entrée pour créer une nouvelle ligne avec •");
        
        // Add bullet point on Enter
        compField.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
                int pos = compField.getCaretPosition();
                String text = compField.getText();
                String newText = text.substring(0, pos) + "\n• " + text.substring(pos);
                compField.setText(newText);
                compField.positionCaret(pos + 3); // Position after "• "
            }
        });
        
        // Add bullet point at start if empty
        compField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused && compField.getText().isEmpty()) {
                compField.setText("• ");
                compField.positionCaret(2);
            }
        });

        VBox box = new VBox(10,
                new Label("Expérience"), expField,
                new Label("Étude"), etudeField,
                new Label("Compétences"), compField
        );
        box.setStyle("-fx-padding: 20;");

        dialog.getDialogPane().setContent(box);

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

        ButtonType saveButton = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        TextField expField = new TextField(existing.getNiveauExperience());
        TextField etudeField = new TextField(existing.getNiveauEtude());
        TextArea compField = new TextArea(existing.getCompetencesRequises());
        compField.setPromptText("Appuyez sur Entrée pour créer une nouvelle ligne avec •");
        
        // Add bullet point on Enter
        compField.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
                int pos = compField.getCaretPosition();
                String text = compField.getText();
                String newText = text.substring(0, pos) + "\n• " + text.substring(pos);
                compField.setText(newText);
                compField.positionCaret(pos + 3); // Position after "• "
            }
        });

        VBox box = new VBox(10,
                new Label("Expérience"), expField,
                new Label("Étude"), etudeField,
                new Label("Compétences"), compField
        );
        box.setStyle("-fx-padding: 20;");

        dialog.getDialogPane().setContent(box);

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
