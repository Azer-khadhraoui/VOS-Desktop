package controllers;

import entities.Contrat;
import javafx.beans.property.SimpleDoubleProperty;
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
import services.ServiceContrat;

import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ContratController implements Initializable {

    // Table et colonnes
    @FXML
    private TableView<Contrat> tableContrats;
    @FXML
    private TableColumn<Contrat, Integer> colId;
    @FXML
    private TableColumn<Contrat, String> colType;
    @FXML
    private TableColumn<Contrat, String> colDateDebut;
    @FXML
    private TableColumn<Contrat, Double> colSalaire;
    @FXML
    private TableColumn<Contrat, Integer> colIdRecrutement;
    @FXML
    private TableColumn<Contrat, Void> colActions;

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
    private ComboBox<String> cbTypeContrat;
    @FXML
    private DatePicker dpDateDebut;
    @FXML
    private TextField tfSalaire;
    @FXML
    private TextField tfIdRecrutement;
    @FXML
    private Label lblModalMessage;

    // Delete modal
    @FXML
    private StackPane deleteModalOverlay;
    @FXML
    private Label lblDeleteInfo;

    // Service
    private ServiceContrat serviceContrat = new ServiceContrat();
    private ObservableList<Contrat> contratsList = FXCollections.observableArrayList();
    private Contrat selectedContrat = null;
    private Contrat contratToDelete = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupSidebarHover();
        // Initialiser les types de contrat
        cbTypeContrat.setItems(FXCollections.observableArrayList(
                "CDI", "CDD", "Stage", "Freelance", "Alternance"));

        // Configurer les colonnes
        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId_contrat()).asObject());
        colType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType_contrat()));
        colDateDebut.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate_debut().toString()));
        colSalaire.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getSalaire()).asObject());
        colIdRecrutement
                .setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId_recrutement()).asObject());

        // Contract Type column with badge styling
        colType.setCellFactory(param -> new TableCell<Contrat, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Label badge = new Label(item);
                    badge.setStyle("-fx-background-color: #DCF1F4; -fx-text-fill: #0891B2; -fx-font-size: 11px; -fx-padding: 6 14; -fx-background-radius: 6; -fx-font-weight: 700; -fx-text-transform: uppercase;");
                    setGraphic(badge);
                }
            }
        });

        // Salary column with currency formatting
        colSalaire.setCellFactory(param -> new TableCell<Contrat, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f DT", item));
                    setStyle("-fx-text-fill: #059669; -fx-font-size: 14px; -fx-font-weight: 600;");
                }
            }
        });

        // Start Date column with formatting
        colDateDebut.setCellFactory(param -> new TableCell<Contrat, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #111827; -fx-font-size: 14px;");
                }
            }
        });

        // Configurer la colonne Actions
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("✏️");
            private final Button btnDelete = new Button("🗑️");
            private final HBox hbox = new HBox(10, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().add("btn-action");
                btnDelete.getStyleClass().add("btn-delete");
                hbox.setAlignment(Pos.CENTER);

                btnEdit.setOnAction(event -> {
                    Contrat contrat = getTableView().getItems().get(getIndex());
                    editContrat(contrat);
                });

                btnDelete.setOnAction(event -> {
                    Contrat contrat = getTableView().getItems().get(getIndex());
                    showDeleteModal(contrat);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });

        // Charger les données
        loadContrats();

        // Recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterContrats(newValue));
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

    private void loadContrats() {
        try {
            contratsList.clear();
            contratsList.addAll(serviceContrat.afficher());
            tableContrats.setItems(contratsList);
        } catch (SQLException e) {
            showError("Erreur lors du chargement des contrats: " + e.getMessage());
        }
    }

    private void filterContrats(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            tableContrats.setItems(contratsList);
            return;
        }

        ObservableList<Contrat> filteredList = FXCollections.observableArrayList();
        for (Contrat contrat : contratsList) {
            if (contrat.getType_contrat().toLowerCase().contains(searchText.toLowerCase()) ||
                    String.valueOf(contrat.getId_contrat()).contains(searchText) ||
                    String.valueOf(contrat.getSalaire()).contains(searchText)) {
                filteredList.add(contrat);
            }
        }
        tableContrats.setItems(filteredList);
    }

    @FXML
    private void showAddModal() {
        selectedContrat = null;
        modalTitle.setText("Nouveau Contrat");
        clearForm();
        modalOverlay.setVisible(true);
        modalOverlay.setManaged(true);
    }

    private void editContrat(Contrat contrat) {
        selectedContrat = contrat;
        modalTitle.setText("Modifier Contrat");

        cbTypeContrat.setValue(contrat.getType_contrat());
        dpDateDebut.setValue(contrat.getDate_debut().toLocalDate());
        tfSalaire.setText(String.valueOf(contrat.getSalaire()));
        tfIdRecrutement.setText(String.valueOf(contrat.getId_recrutement()));

        modalOverlay.setVisible(true);
        modalOverlay.setManaged(true);
    }

    @FXML
    private void saveContrat() {
        // Validation
        if (cbTypeContrat.getValue() == null || dpDateDebut.getValue() == null ||
                tfSalaire.getText().isEmpty() || tfIdRecrutement.getText().isEmpty()) {
            showMessage("Veuillez remplir tous les champs", true);
            return;
        }

        try {
            String type = cbTypeContrat.getValue();
            Date dateDebut = Date.valueOf(dpDateDebut.getValue());
            double salaire = Double.parseDouble(tfSalaire.getText());
            int idRecrutement = Integer.parseInt(tfIdRecrutement.getText());

            if (selectedContrat == null) {
                // Ajouter
                Contrat newContrat = new Contrat(type, dateDebut, salaire, idRecrutement);
                serviceContrat.ajouter(newContrat);
                showMessage("Contrat ajouté avec succès!", false);
            } else {
                // Modifier
                selectedContrat.setType_contrat(type);
                selectedContrat.setDate_debut(dateDebut);
                selectedContrat.setSalaire(salaire);
                selectedContrat.setId_recrutement(idRecrutement);
                serviceContrat.modifier(selectedContrat);
                showMessage("Contrat modifié avec succès!", false);
            }

            loadContrats();
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
        cbTypeContrat.setValue(null);
        dpDateDebut.setValue(null);
        tfSalaire.clear();
        tfIdRecrutement.clear();
        lblModalMessage.setText("");
    }

    private void showDeleteModal(Contrat contrat) {
        contratToDelete = contrat;
        lblDeleteInfo.setText("Contrat #" + contrat.getId_contrat() + " - " + contrat.getType_contrat());
        deleteModalOverlay.setVisible(true);
        deleteModalOverlay.setManaged(true);
    }

    @FXML
    private void confirmDelete() {
        if (contratToDelete != null) {
            try {
                serviceContrat.supprimer(contratToDelete.getId_contrat());
                loadContrats();
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
        contratToDelete = null;
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
