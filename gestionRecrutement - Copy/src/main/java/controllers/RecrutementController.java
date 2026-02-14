package controllers;

import entities.Recrutement;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import services.ServiceRecrutement;

import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class RecrutementController implements Initializable {

    // Table et colonnes
    @FXML
    private TableView<Recrutement> tableRecrutements;
    @FXML
    private TableColumn<Recrutement, Integer> colId;
    @FXML
    private TableColumn<Recrutement, String> colDateDecision;
    @FXML
    private TableColumn<Recrutement, String> colDecisionFinale;
    @FXML
    private TableColumn<Recrutement, Integer> colIdEntretien;
    @FXML
    private TableColumn<Recrutement, Integer> colIdUtilisateur;
    @FXML
    private TableColumn<Recrutement, Void> colActions;

    // Sidebar
    @FXML
    private VBox sidebarVBox;

    // Champ de recherche
    @FXML
    private TextField searchField;

    // Modal overlay
    @FXML
    private StackPane modalOverlay;
    @FXML
    private Label modalTitle;
    @FXML
    private DatePicker dpDateDecision;
    @FXML
    private ComboBox<String> cbDecisionFinale;
    @FXML
    private TextField tfIdEntretien;
    @FXML
    private TextField tfIdUtilisateur;
    @FXML
    private Label lblModalMessage;

    // Delete modal
    @FXML
    private StackPane deleteModalOverlay;
    @FXML
    private Label lblDeleteInfo;

    // Service
    private ServiceRecrutement serviceRecrutement = new ServiceRecrutement();
    private ObservableList<Recrutement> recrutementsList = FXCollections.observableArrayList();
    private Recrutement selectedRecrutement = null;
    private Recrutement recrutementToDelete = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupSidebarHover();
        // Initialiser les décisions
        cbDecisionFinale.setItems(FXCollections.observableArrayList(
                "Accepté", "Refusé", "En attente"));

        // Configurer les colonnes avec custom cell rendering

        // Colonne ID avec icône
        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId_recrutement()).asObject());
        colId.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(10);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    Label icon = new Label("👤");
                    icon.setStyle("-fx-font-size: 18px;");
                    Label text = new Label("#" + item);
                    text.setStyle("-fx-font-size: 14px; -fx-font-weight: 500; -fx-text-fill: #111827;");
                    hbox.getChildren().addAll(icon, text);
                    setGraphic(hbox);
                }
            }
        });

        // Colonne Date avec badge orange
        colDateDecision
                .setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate_decision().toString()));
        colDateDecision.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox();
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    Label badge = new Label("📅 " + item);
                    badge.getStyleClass().addAll("badge", "badge-orange");
                    hbox.getChildren().add(badge);
                    setGraphic(hbox);
                }
            }
        });

        // Colonne Décision avec badge coloré selon le statut
        colDecisionFinale.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDecision_finale()));
        colDecisionFinale.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    
                    // Color badge according to decision
                    switch (item.toLowerCase()) {
                        case "accepté":
                            badge.setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #059669; "
                                    + "-fx-padding: 6 12; -fx-background-radius: 20; -fx-font-weight: 600;");
                            break;
                        case "refusé":
                            badge.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; "
                                    + "-fx-padding: 6 12; -fx-background-radius: 20; -fx-font-weight: 600;");
                            break;
                        case "en attente":
                            badge.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #92400E; "
                                    + "-fx-padding: 6 12; -fx-background-radius: 20; -fx-font-weight: 600;");
                            break;
                        default:
                            badge.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #6B7280; "
                                    + "-fx-padding: 6 12; -fx-background-radius: 20; -fx-font-weight: 600;");
                    }
                    
                    setGraphic(badge);
                }
            }
        });

        // Colonne ID Entretien avec badge bleu
        colIdEntretien
                .setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId_entretien()).asObject());
        colIdEntretien.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox();
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    Label badge = new Label("🔗 " + item);
                    badge.getStyleClass().addAll("badge", "badge-blue");
                    hbox.getChildren().add(badge);
                    setGraphic(hbox);
                }
            }
        });

        // Colonne ID Utilisateur avec badge gris
        colIdUtilisateur
                .setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId_utilisateur()).asObject());
        colIdUtilisateur.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox();
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    Label badge = new Label("👤 " + item);
                    badge.getStyleClass().addAll("badge", "badge-gray");
                    hbox.getChildren().add(badge);
                    setGraphic(hbox);
                }
            }
        });

        // Configurer la colonne Actions avec boutons modernes
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("✏️");
            private final Button btnDelete = new Button("🗑️");
            private final HBox hbox = new HBox(8, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().add("btn-action");
                btnDelete.getStyleClass().add("btn-delete");
                hbox.setAlignment(Pos.CENTER_LEFT);

                btnEdit.setOnAction(event -> {
                    Recrutement recrutement = getTableView().getItems().get(getIndex());
                    editRecrutement(recrutement);
                });

                btnDelete.setOnAction(event -> {
                    Recrutement recrutement = getTableView().getItems().get(getIndex());
                    showDeleteModal(recrutement);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });

        // Charger les données
        loadRecrutements();

        // Recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterRecrutements(newValue));
    }

    private void setupSidebarHover() {
        if (sidebarVBox == null) return;
        
        sidebarVBox.setOnMouseEntered(event -> expandSidebar());
        sidebarVBox.setOnMouseExited(event -> collapseSidebar());
    }

    private void expandSidebar() {
        // Show all nav labels
        sidebarVBox.lookupAll(".nav-label").forEach(node -> {
            node.setVisible(true);
            node.setManaged(true);
        });

        // Show logo subtitle
        sidebarVBox.lookupAll(".logo-subtitle").forEach(node -> {
            node.setVisible(true);
            node.setManaged(true);
        });

        // Show help section
        sidebarVBox.lookupAll(".help-section").forEach(node -> {
            node.setVisible(true);
            node.setManaged(true);
        });

        // Show user profile
        sidebarVBox.lookupAll(".user-profile-sidebar").forEach(node -> {
            node.setVisible(true);
            node.setManaged(true);
        });
    }

    private void collapseSidebar() {
        // Hide all nav labels
        sidebarVBox.lookupAll(".nav-label").forEach(node -> {
            node.setVisible(false);
            node.setManaged(false);
        });

        // Hide logo subtitle
        sidebarVBox.lookupAll(".logo-subtitle").forEach(node -> {
            node.setVisible(false);
            node.setManaged(false);
        });

        // Hide help section
        sidebarVBox.lookupAll(".help-section").forEach(node -> {
            node.setVisible(false);
            node.setManaged(false);
        });

        // Hide user profile
        sidebarVBox.lookupAll(".user-profile-sidebar").forEach(node -> {
            node.setVisible(false);
            node.setManaged(false);
        });
    }

    private void loadRecrutements() {
        try {
            recrutementsList.clear();
            recrutementsList.addAll(serviceRecrutement.afficher());
            tableRecrutements.setItems(recrutementsList);
        } catch (SQLException e) {
            showError("Erreur lors du chargement des recrutements: " + e.getMessage());
        }
    }

    private void filterRecrutements(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            tableRecrutements.setItems(recrutementsList);
            return;
        }

        ObservableList<Recrutement> filteredList = FXCollections.observableArrayList();
        for (Recrutement recrutement : recrutementsList) {
            if (recrutement.getDecision_finale().toLowerCase().contains(searchText.toLowerCase()) ||
                    String.valueOf(recrutement.getId_recrutement()).contains(searchText) ||
                    String.valueOf(recrutement.getId_entretien()).contains(searchText)) {
                filteredList.add(recrutement);
            }
        }
        tableRecrutements.setItems(filteredList);
    }

    @FXML
    private void showAddModal() {
        selectedRecrutement = null;
        modalTitle.setText("Nouveau Recrutement");
        clearForm();
        modalOverlay.setVisible(true);
        modalOverlay.setManaged(true);
    }

    private void editRecrutement(Recrutement recrutement) {
        selectedRecrutement = recrutement;
        modalTitle.setText("Modifier Recrutement");

        dpDateDecision.setValue(recrutement.getDate_decision().toLocalDate());
        cbDecisionFinale.setValue(recrutement.getDecision_finale());
        tfIdEntretien.setText(String.valueOf(recrutement.getId_entretien()));
        tfIdUtilisateur.setText(String.valueOf(recrutement.getId_utilisateur()));

        modalOverlay.setVisible(true);
        modalOverlay.setManaged(true);
    }

    @FXML
    private void saveRecrutement() {
        // Validation
        if (dpDateDecision.getValue() == null || cbDecisionFinale.getValue() == null ||
                tfIdEntretien.getText().isEmpty() || tfIdUtilisateur.getText().isEmpty()) {
            showMessage("Veuillez remplir tous les champs", true);
            return;
        }

        try {
            Date dateDecision = Date.valueOf(dpDateDecision.getValue());
            String decisionFinale = cbDecisionFinale.getValue();
            int idEntretien = Integer.parseInt(tfIdEntretien.getText());
            int idUtilisateur = Integer.parseInt(tfIdUtilisateur.getText());

            if (selectedRecrutement == null) {
                // Ajouter
                Recrutement newRecrutement = new Recrutement(dateDecision, decisionFinale, idEntretien, idUtilisateur);
                serviceRecrutement.ajouter(newRecrutement);
                showMessage("Recrutement ajouté avec succès!", false);
            } else {
                // Modifier
                selectedRecrutement.setDate_decision(dateDecision);
                selectedRecrutement.setDecision_finale(decisionFinale);
                selectedRecrutement.setId_entretien(idEntretien);
                selectedRecrutement.setId_utilisateur(idUtilisateur);
                serviceRecrutement.modifier(selectedRecrutement);
                showMessage("Recrutement modifié avec succès!", false);
            }

            loadRecrutements();
            closeModal();
        } catch (NumberFormatException e) {
            showMessage("Erreur: Vérifiez le format des nombres", true);
        } catch (SQLException e) {
            showMessage("Erreur SQL: " + e.getMessage(), true);
        }
    }

    @FXML
    private void closeModal() {
        modalOverlay.setVisible(false);
        modalOverlay.setManaged(false);
        clearForm();
    }

    private void clearForm() {
        dpDateDecision.setValue(null);
        cbDecisionFinale.setValue(null);
        tfIdEntretien.clear();
        tfIdUtilisateur.clear();
        lblModalMessage.setText("");
    }

    private void showDeleteModal(Recrutement recrutement) {
        recrutementToDelete = recrutement;
        lblDeleteInfo
                .setText("Recrutement #" + recrutement.getId_recrutement() + " - " + recrutement.getDecision_finale());
        deleteModalOverlay.setVisible(true);
        deleteModalOverlay.setManaged(true);
    }

    @FXML
    private void confirmDelete() {
        if (recrutementToDelete != null) {
            try {
                serviceRecrutement.supprimer(recrutementToDelete.getId_recrutement());
                loadRecrutements();
                closeDeleteModal();
            } catch (SQLException e) {
                showError("Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    @FXML
    private void closeDeleteModal() {
        deleteModalOverlay.setVisible(false);
        deleteModalOverlay.setManaged(false);
        recrutementToDelete = null;
    }

    private void showMessage(String message, boolean isError) {
        lblModalMessage.setText(message);
        lblModalMessage.getStyleClass().removeAll("message-success", "message-error");
        lblModalMessage.getStyleClass().add(isError ? "message-error" : "message-success");
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
