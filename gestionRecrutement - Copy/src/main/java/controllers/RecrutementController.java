package controllers;

import entities.Recrutement;
import entities.RecrutementGroup;
import entities.RecrutementTableRow;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.application.Platform;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class RecrutementController implements Initializable {

    // Table et colonnes
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
    private ObservableList<RecrutementTableRow> tableRowsList = FXCollections.observableArrayList();
    private List<RecrutementGroup> groupsList;
    private Map<RecrutementGroup, Integer> groupHeaderIndexMap = new HashMap<>();

    private Recrutement selectedRecrutement = null;
    private Recrutement recrutementToDelete = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupSidebarHover();
        // Initialiser les décisions
        cbDecisionFinale.setItems(FXCollections.observableArrayList(
                "Accepté", "Refusé", "En attente"));

        // Configurer les colonnes
        setupTableColumns();

        // Charger les données groupées
        loadGroupedRecrutements();

        // Recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterRecrutements(newValue));
    }

    private void setupTableColumns() {
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
                Button expandBtn = new Button(row.getParentGroup().isExpanded() ? "▼" : "▶");
                expandBtn.setStyle("-fx-padding: 4 8; -fx-font-size: 12px; -fx-cursor: hand; " +
                        "-fx-background-color: #F3F4F6; -fx-border-color: #E5E7EB; -fx-border-width: 1;");
                expandBtn.setPrefWidth(35);

                expandBtn.setOnAction(event -> {
                    // Defer list mutation to next JavaFX pulse so we don't
                    // modify the ObservableList while the cell is still rendering.
                    Platform.runLater(() -> refreshTableWithGroupState(row.getParentGroup()));
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
                            // Find the original Recrutement object
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
                                showDeleteModal(rec);
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
    }

    private void loadGroupedRecrutements() {
        try {
            groupsList = serviceRecrutement.afficherGroupedByUser();
            refreshTableDisplay();
        } catch (SQLException e) {
            showError("Erreur lors du chargement des recrutements: " + e.getMessage());
        }
    }

    private void refreshTableDisplay() {
        tableRowsList.clear();
        groupHeaderIndexMap.clear();

        int index = 0;
        for (RecrutementGroup group : groupsList) {
            // Add group header
            RecrutementTableRow headerRow = new RecrutementTableRow(group);
            tableRowsList.add(headerRow);
            groupHeaderIndexMap.put(group, index);
            index++;

            // Add detail rows if expanded
            if (group.isExpanded()) {
                for (Recrutement recrutement : group.getRecrutements()) {
                    RecrutementTableRow detailRow = new RecrutementTableRow(group, recrutement);
                    tableRowsList.add(detailRow);
                    index++;
                }
            }
        }

        tableRecrutements.setItems(tableRowsList);
        // Force all cells to call updateItem() again so expand buttons
        // redraw with the correct ▼/▶ state after a list rebuild.
        tableRecrutements.refresh();
    }

    private void refreshTableWithGroupState(RecrutementGroup changedGroup) {
        boolean nowExpanded = changedGroup.isExpanded(); // already toggled? No — toggle first:
        changedGroup.setExpanded(!nowExpanded);
        boolean expanding = changedGroup.isExpanded();

        if (expanding) {
            // Find the index of this group's header row in the list
            int headerIndex = -1;
            for (int i = 0; i < tableRowsList.size(); i++) {
                RecrutementTableRow r = tableRowsList.get(i);
                if (r.isGroupHeader() && r.getParentGroup() == changedGroup) {
                    headerIndex = i;
                    break;
                }
            }
            if (headerIndex >= 0) {
                // Insert detail rows directly after the header — JavaFX sees
                // precise add events and renders them immediately.
                int insertAt = headerIndex + 1;
                for (Recrutement rec : changedGroup.getRecrutements()) {
                    tableRowsList.add(insertAt++, new RecrutementTableRow(changedGroup, rec));
                }
            }
        } else {
            // Remove only the detail rows that belong to this group
            tableRowsList.removeIf(r -> !r.isGroupHeader() && r.getParentGroup() == changedGroup);
        }

        // Refresh so the header cell redraws its button icon (▶ ↔ ▼)
        tableRecrutements.refresh();
    }

    private void setupSidebarHover() {
        if (sidebarVBox == null)
            return;

        sidebarVBox.setOnMouseEntered(event -> expandSidebar());
        sidebarVBox.setOnMouseExited(event -> collapseSidebar());
    }

    private void expandSidebar() {
        sidebarVBox.lookupAll(".nav-label").forEach(node -> {
            node.setVisible(true);
            node.setManaged(true);
        });
        sidebarVBox.lookupAll(".logo-subtitle").forEach(node -> {
            node.setVisible(true);
            node.setManaged(true);
        });
        sidebarVBox.lookupAll(".help-section").forEach(node -> {
            node.setVisible(true);
            node.setManaged(true);
        });
        sidebarVBox.lookupAll(".user-profile-sidebar").forEach(node -> {
            node.setVisible(true);
            node.setManaged(true);
        });
    }

    private void collapseSidebar() {
        sidebarVBox.lookupAll(".nav-label").forEach(node -> {
            node.setVisible(false);
            node.setManaged(false);
        });
        sidebarVBox.lookupAll(".logo-subtitle").forEach(node -> {
            node.setVisible(false);
            node.setManaged(false);
        });
        sidebarVBox.lookupAll(".help-section").forEach(node -> {
            node.setVisible(false);
            node.setManaged(false);
        });
        sidebarVBox.lookupAll(".user-profile-sidebar").forEach(node -> {
            node.setVisible(false);
            node.setManaged(false);
        });
    }

    private void filterRecrutements(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            refreshTableDisplay();
            return;
        }

        tableRowsList.clear();
        groupHeaderIndexMap.clear();

        int index = 0;
        for (RecrutementGroup group : groupsList) {
            boolean groupMatches = group.getUserName().toLowerCase().contains(searchText.toLowerCase()) ||
                    String.valueOf(group.getUserId()).contains(searchText);

            List<Recrutement> matchingRecrutements = new java.util.ArrayList<>();
            for (Recrutement r : group.getRecrutements()) {
                if (r.getDecision_finale().toLowerCase().contains(searchText.toLowerCase()) ||
                        String.valueOf(r.getId_recrutement()).contains(searchText) ||
                        String.valueOf(r.getId_entretien()).contains(searchText)) {
                    matchingRecrutements.add(r);
                }
            }

            if (groupMatches || !matchingRecrutements.isEmpty()) {
                // Add group header
                RecrutementTableRow headerRow = new RecrutementTableRow(group);
                if (!matchingRecrutements.isEmpty() || groupMatches) {
                    headerRow.setExpanded(true);
                }
                tableRowsList.add(headerRow);
                groupHeaderIndexMap.put(group, index);
                index++;

                // Add matching detail rows
                if (!matchingRecrutements.isEmpty()) {
                    for (Recrutement recrutement : matchingRecrutements) {
                        RecrutementTableRow detailRow = new RecrutementTableRow(group, recrutement);
                        tableRowsList.add(detailRow);
                        index++;
                    }
                } else if (groupMatches) {
                    // If only group matches, show all its recruitments
                    for (Recrutement r : group.getRecrutements()) {
                        RecrutementTableRow detailRow = new RecrutementTableRow(group, r);
                        tableRowsList.add(detailRow);
                        index++;
                    }
                }
            }
        }

        tableRecrutements.setItems(tableRowsList);
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
                Recrutement newRecrutement = new Recrutement(dateDecision, decisionFinale, idEntretien, idUtilisateur);
                serviceRecrutement.ajouter(newRecrutement);
                showMessage("Recrutement ajouté avec succès!", false);
            } else {
                selectedRecrutement.setDate_decision(dateDecision);
                selectedRecrutement.setDecision_finale(decisionFinale);
                selectedRecrutement.setId_entretien(idEntretien);
                selectedRecrutement.setId_utilisateur(idUtilisateur);
                serviceRecrutement.modifier(selectedRecrutement);
                showMessage("Recrutement modifié avec succès!", false);
            }

            loadGroupedRecrutements();
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
                loadGroupedRecrutements();
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
