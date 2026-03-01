package controllers;

import entities.Contrat;
import entities.Recrutement;
import entities.RecrutementGroup;
import entities.RecrutementTableRow;
import entities.Utilisateur;
import entities.Entretien;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.PDFService;
import services.RecrutementEmailService;
import services.ServiceContrat;
import services.ServiceRecrutement;
import services.GoogleCalendarService;
import services.ServiceUtilisateur;
import services.EntretienService;

import java.io.File;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class RecrutementModuleController implements Initializable {

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

    @FXML
    private TableView<Contrat> tableContrats;
    @FXML
    private TableColumn<Contrat, Integer> colIdContrat;
    @FXML
    private TableColumn<Contrat, String> colUserContrat; // New column for user name
    @FXML
    private TableColumn<Contrat, String> colTypeContrat;
    @FXML
    private TableColumn<Contrat, String> colDateDebut;
    @FXML
    private TableColumn<Contrat, String> colStatus;
    @FXML
    private TableColumn<Contrat, String> colVolumeHoraire;
    @FXML
    private TableColumn<Contrat, String> colAvantages;
    @FXML
    private TableColumn<Contrat, Double> colSalaire;
    @FXML
    private TableColumn<Contrat, Integer> colIdRecrutementContrat;
    @FXML
    private TableColumn<Contrat, Void> colActionsContrat;

    @FXML
    private TextField searchRecrutement;
    @FXML
    private TextField searchContrat;
    @FXML
    private ComboBox<String> cbTypeContratFilter;

    // --- Statistics FXML Fields ---
    @FXML
    private Label valTotalRecrutements;
    @FXML
    private Label valTauxAcceptation;
    @FXML
    private Label valEntretiensMoyens;
    @FXML
    private Label valEnAttenteCount;
    @FXML
    private PieChart chartDecisions;
    @FXML
    private BarChart<String, Number> chartRecruteur;
    @FXML
    private LineChart<String, Number> chartRecrutementMois;

    @FXML
    private Label valTotalContrats;
    @FXML
    private Label valSalaireMoyen;
    @FXML
    private Label valVolumeMoyen;
    @FXML
    private Label valContratsActifs;
    @FXML
    private PieChart chartTypeContrat;
    @FXML
    private BarChart<String, Number> chartSalaireType;
    @FXML
    private PieChart chartStatutContrat;

    private boolean statsInitialized = false;

    private final ServiceRecrutement serviceRecrutement = new ServiceRecrutement();
    private final ServiceContrat serviceContrat = new ServiceContrat();
    private final PDFService pdfService = PDFService.getInstance();
    private final GoogleCalendarService googleCalendarService = new GoogleCalendarService();
    private final ServiceUtilisateur serviceUtilisateur = new ServiceUtilisateur();
    private final EntretienService entretienService = new EntretienService();
    private final RecrutementEmailService emailService = RecrutementEmailService.getInstance();

    private final ObservableList<RecrutementTableRow> recrutementData = FXCollections.observableArrayList();
    private final ObservableList<Contrat> contratData = FXCollections.observableArrayList();
    private List<Contrat> allContratData = new ArrayList<>();
    private List<RecrutementGroup> recrutementGroups = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initializeRecrutementTable();
        initializeContratTable();
        setupSearchFilters();
        initializeStatistiques();
    }

    private void setupSearchFilters() {
        searchRecrutement.textProperty().addListener((obs, oldVal, newVal) -> {
            filterRecrutements(newVal);
        });

        searchContrat.textProperty().addListener((obs, oldVal, newVal) -> {
            filterContrats(newVal);
        });
    }

    private void filterRecrutements(String query) {
        if (query == null || query.isEmpty()) {
            refreshRecrutementTable();
            return;
        }
        String lowerQuery = query.toLowerCase();
        ObservableList<RecrutementTableRow> filtered = FXCollections.observableArrayList();
        for (RecrutementGroup group : recrutementGroups) {
            boolean groupMatches = group.getUserName().toLowerCase().contains(lowerQuery);
            List<Recrutement> matchingRecs = new ArrayList<>();
            for (Recrutement r : group.getRecrutements()) {
                if (r.getDecision_finale().toLowerCase().contains(lowerQuery) ||
                        String.valueOf(r.getId_entretien()).contains(query)) {
                    matchingRecs.add(r);
                }
            }

            if (groupMatches || !matchingRecs.isEmpty()) {
                RecrutementTableRow header = new RecrutementTableRow(group);
                header.setExpanded(true); // Auto expand if filtered
                filtered.add(header);
                for (Recrutement r : matchingRecs) {
                    filtered.add(new RecrutementTableRow(group, r));
                }
            }
        }
        tableRecrutements.setItems(filtered);
    }

    private void filterContrats(String query) {
        if (query == null || query.isEmpty()) {
            contratData.setAll(allContratData);
            return;
        }
        String lowerQuery = query.toLowerCase();
        List<Contrat> filtered = new ArrayList<>();
        for (Contrat c : allContratData) {
            if (c.getType_contrat().toLowerCase().contains(lowerQuery) ||
                    c.getStatus().toLowerCase().contains(lowerQuery) ||
                    (c.getAvantages() != null && c.getAvantages().toLowerCase().contains(lowerQuery))) {
                filtered.add(c);
            }
        }
        contratData.setAll(filtered);
    }

    private void initializeRecrutementTable() {
        // User Name Column
        colUserName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUserName()));
        colUserName.setCellFactory(param -> new TableCell<RecrutementTableRow, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                RecrutementTableRow row = getTableRow().getItem();
                if (!row.isGroupHeader()) {
                    Label detailLabel = new Label("  └─ Détail");
                    detailLabel.getStyleClass().add("name-cell-detail");
                    setGraphic(detailLabel);
                } else {
                    HBox hbox = new HBox(12);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    Button expandBtn = new Button(row.isExpanded() ? "▼" : "▶");
                    expandBtn.getStyleClass().add("btn-expand");
                    expandBtn.setStyle(
                            "-fx-padding: 4 8; -fx-font-size: 12px; -fx-cursor: hand; -fx-background-color: #F3F4F6; -fx-border-color: #E5E7EB; -fx-border-width: 1;");
                    expandBtn.setPrefWidth(35);
                    expandBtn.setOnAction(e -> {
                        RecrutementGroup grp = row.getParentGroup();
                        grp.setExpanded(!grp.isExpanded());
                        refreshRecrutementTable();
                    });
                    Label userIcon = new Label("👤");
                    userIcon.setStyle("-fx-font-size: 16px;");
                    Label nameLabel = new Label(item + " (#" + row.getUserId() + ")");
                    nameLabel.getStyleClass().add("name-cell");
                    hbox.getChildren().addAll(expandBtn, userIcon, nameLabel);
                    setGraphic(hbox);
                }
            }
        });

        // RECRUTEMENT COUNT COLUMN
        colRecrutementCount.setCellValueFactory(
                data -> new SimpleIntegerProperty(data.getValue().getRecrutementCount()).asObject());
        colRecrutementCount.setCellFactory(param -> new TableCell<RecrutementTableRow, Integer>() {
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
                count.getStyleClass().addAll("status-badge", "status-refused"); // repurposed style
                count.setStyle("-fx-background-color: #E5E7EB; -fx-text-fill: #374151;");
                hbox.getChildren().addAll(icon, count);
                setGraphic(hbox);
            }
        });

        // DECISION DATE COLUMN
        colDecisionDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDecisionDate()));
        colDecisionDate.setCellFactory(param -> new TableCell<RecrutementTableRow, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isEmpty()) {
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
                badge.getStyleClass().addAll("status-badge", "status-date");
                hbox.getChildren().add(badge);
                setGraphic(hbox);
            }
        });

        // DECISION FINALE COLUMN
        colDecisionFinale.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDecision()));
        colDecisionFinale.setCellFactory(param -> new TableCell<RecrutementTableRow, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isEmpty()) {
                    setGraphic(null);
                    return;
                }
                RecrutementTableRow row = getTableRow().getItem();
                if (row.isGroupHeader()) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(item);
                badge.getStyleClass().add("status-badge");
                switch (item.toLowerCase()) {
                    case "accepté":
                        badge.getStyleClass().add("status-accepted");
                        break;
                    case "refusé":
                        badge.getStyleClass().add("status-refused");
                        break;
                    case "en attente":
                        badge.getStyleClass().add("status-pending");
                        break;
                    default:
                        badge.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #6B7280;");
                }
                setGraphic(badge);
            }
        });

        // INTERVIEW ID COLUMN
        colIdEntretien
                .setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getInterviewId()).asObject());
        colIdEntretien.setCellFactory(param -> new TableCell<RecrutementTableRow, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == 0) {
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
                badge.setStyle(
                        "-fx-background-color: #BFDBFE; -fx-text-fill: #1E40AF; -fx-padding: 4 8; -fx-background-radius: 4; -fx-font-size: 11px;");
                hbox.getChildren().add(badge);
                setGraphic(hbox);
            }
        });

        // Actions Column
        colActions.setCellFactory(param -> new TableCell<RecrutementTableRow, Void>() {
            private final Button btnEdit = new Button();
            private final Button btnDelete = new Button();
            private final HBox hbox = new HBox(btnEdit, btnDelete);

            {
                // Icons - match original work
                try {
                    ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/Edit.png")));
                    editIcon.setFitHeight(16);
                    editIcon.setFitWidth(16);
                    editIcon.setPreserveRatio(true);
                    btnEdit.setGraphic(editIcon);

                    ImageView deleteIcon = new ImageView(
                            new Image(getClass().getResourceAsStream("/images/delete.png")));
                    deleteIcon.setFitHeight(16);
                    deleteIcon.setFitWidth(16);
                    deleteIcon.setPreserveRatio(true);
                    btnDelete.setGraphic(deleteIcon);
                } catch (Exception e) {
                    btnEdit.setText("✏️");
                    btnDelete.setText("🗑️");
                }

                btnEdit.getStyleClass().add("btn-action-icon-edit");
                btnDelete.getStyleClass().add("btn-action-icon-delete");

                hbox.getStyleClass().add("action-hbox");
                hbox.setAlignment(Pos.CENTER);
                hbox.setSpacing(6);

                btnEdit.setOnAction(e -> editRecrutement(getTableRow().getItem()));
                btnDelete.setOnAction(e -> deleteRecrutement(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null
                        || getTableRow().getItem().isGroupHeader()) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
            }
        });

        // ROW FACTORY for header styling
        tableRecrutements.setRowFactory(param -> new TableRow<RecrutementTableRow>() {
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

        loadRecrutementData();
    }

    private void initializeContratTable() {
        colUserContrat.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUser_name()));
        colTypeContrat.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType_contrat()));
        colDateDebut.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate_debut().toString()));
        colDateDebut.setCellFactory(param -> new TableCell<Contrat, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                Contrat contrat = getTableRow().getItem();
                LocalDate start = contrat.getDate_debut().toLocalDate();
                LocalDate end = (contrat.getDate_fin() != null) ? contrat.getDate_fin().toLocalDate() : null;

                VBox vbox = new VBox(2);
                vbox.setAlignment(Pos.CENTER_LEFT);

                Label lblPeriod = new Label();
                lblPeriod.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1F2937;");

                if (end != null) {
                    Period period = Period.between(start, end);
                    int years = period.getYears();
                    int months = period.getMonths();
                    int days = period.getDays();

                    String periodText = "";
                    if (years > 0)
                        periodText += years + " an" + (years > 1 ? "s" : "") + " ";
                    if (months > 0)
                        periodText += months + " mois ";
                    if (days > 0 && years == 0 && months == 0)
                        periodText += days + " jour" + (days > 1 ? "s" : "");

                    if (periodText.isEmpty())
                        periodText = "0 jours";

                    lblPeriod.setText(periodText.trim());
                } else {
                    lblPeriod.setText("Indéterminé");
                }

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String rangeText = start.format(formatter) + " → " + (end != null ? end.format(formatter) : "...");
                Label lblRange = new Label(rangeText);
                lblRange.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF; -fx-font-style: italic;");

                vbox.getChildren().addAll(lblPeriod, lblRange);
                setGraphic(vbox);
            }
        });
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        colStatus.setCellFactory(param -> new TableCell<Contrat, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("status-badge");
                    String lowerStatus = item.toLowerCase();
                    if (lowerStatus.contains("accepté") || lowerStatus.contains("actif")
                            || lowerStatus.contains("signé")) {
                        badge.getStyleClass().add("status-accepted");
                    } else if (lowerStatus.contains("refusé") || lowerStatus.contains("expiré")
                            || lowerStatus.contains("annulé")) {
                        badge.getStyleClass().add("status-refused");
                    } else {
                        badge.getStyleClass().add("status-pending");
                    }
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        colVolumeHoraire.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getVolume_horaire()));
        colAvantages.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAvantages()));
        colSalaire.setCellValueFactory(
                data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getSalaire()).asObject());

        colActionsContrat.setCellFactory(param -> new TableCell<Contrat, Void>() {
            private final Button btnEdit = new Button();
            private final Button btnDelete = new Button();
            private final Button btnPdf = new Button();
            private final Button btnDownload = new Button();
            private final HBox hbox = new HBox(btnEdit, btnDelete, btnPdf, btnDownload);

            {
                try {
                    ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/Edit.png")));
                    editIcon.setFitHeight(16);
                    editIcon.setFitWidth(16);
                    editIcon.setPreserveRatio(true);
                    btnEdit.setGraphic(editIcon);

                    ImageView deleteIcon = new ImageView(
                            new Image(getClass().getResourceAsStream("/images/delete.png")));
                    deleteIcon.setFitHeight(16);
                    deleteIcon.setFitWidth(16);
                    deleteIcon.setPreserveRatio(true);
                    btnDelete.setGraphic(deleteIcon);

                    ImageView pdfIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/view.png")));
                    pdfIcon.setFitHeight(16);
                    pdfIcon.setFitWidth(16);
                    pdfIcon.setPreserveRatio(true);
                    btnPdf.setGraphic(pdfIcon);

                    // Reusing pdf icon for download for now, or use a specific one if exists
                    ImageView downloadIcon = new ImageView(
                            new Image(getClass().getResourceAsStream("/images/pdf.png")));
                    downloadIcon.setFitHeight(16);
                    downloadIcon.setFitWidth(16);
                    downloadIcon.setPreserveRatio(true);
                    btnDownload.setGraphic(downloadIcon);
                } catch (Exception e) {
                    btnEdit.setText("✏️");
                    btnDelete.setText("🗑️");
                    btnPdf.setText("📄");
                    btnDownload.setText("⬇️");
                }

                btnEdit.getStyleClass().add("btn-action-icon-edit");
                btnDelete.getStyleClass().add("btn-action-icon-delete");
                btnPdf.getStyleClass().add("btn-action-icon-view");
                btnDownload.getStyleClass().add("btn-action-icon-eval");

                hbox.getStyleClass().add("action-hbox-contrat");
                hbox.setAlignment(Pos.CENTER);
                hbox.setSpacing(8);

                btnEdit.setOnAction(e -> editContrat(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> deleteContrat(getTableView().getItems().get(getIndex())));
                btnPdf.setOnAction(e -> generateContractPdf(getTableView().getItems().get(getIndex())));
                btnDownload.setOnAction(e -> downloadContractPdf(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
            }
        });

        loadContratData();
    }

    private void loadRecrutementData() {
        try {
            recrutementGroups = serviceRecrutement.afficherGroupedByUser();
            refreshRecrutementTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void refreshRecrutementTable() {
        recrutementData.clear();
        for (RecrutementGroup group : recrutementGroups) {
            recrutementData.add(new RecrutementTableRow(group));
            if (group.isExpanded()) {
                for (Recrutement r : group.getRecrutements()) {
                    recrutementData.add(new RecrutementTableRow(group, r));
                }
            }
        }
        tableRecrutements.setItems(recrutementData);
    }

    private void loadContratData() {
        try {
            allContratData = serviceContrat.afficher();
            contratData.setAll(allContratData);
            tableContrats.setItems(contratData);
            setupContratFilters();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupContratFilters() {
        if (cbTypeContratFilter == null)
            return;
        Set<String> types = new HashSet<>();
        types.add("Tous");
        for (Contrat c : allContratData)
            types.add(c.getType_contrat());
        cbTypeContratFilter.setItems(FXCollections.observableArrayList(types));
        cbTypeContratFilter.setValue("Tous");
        cbTypeContratFilter.setOnAction(e -> {
            String filter = cbTypeContratFilter.getValue();
            if ("Tous".equals(filter))
                contratData.setAll(allContratData);
            else {
                List<Contrat> filtered = new ArrayList<>();
                for (Contrat c : allContratData)
                    if (filter.equals(c.getType_contrat()))
                        filtered.add(c);
                contratData.setAll(filtered);
            }
        });
    }

    @FXML
    private void handleGoogleSync() {
        try {
            List<Recrutement> list = serviceRecrutement.afficher();
            googleCalendarService.syncRecrutements(list);
            showAlert("Succès", "Synchronisation avec Google Calendar réussie !");
        } catch (Exception e) {
            showAlert("Erreur", "Échec de synchronisation Google: " + e.getMessage());
        }
    }

    @FXML
    private void ajouterRecrutement() {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Ajouter un Recrutement");

        VBox modalContent = new VBox();
        modalContent.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #1a1a2e, #16213e); " +
                "-fx-background-radius: 20; -fx-padding: 30;");
        modalContent.setSpacing(15);

        Label title = new Label("Nouveau Recrutement");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label("Créer un nouveau recrutement");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #9CA3AF;");

        VBox header = new VBox(title, subtitle);
        header.setStyle("-fx-spacing: 5;");

        VBox formFields = new VBox();
        formFields.setSpacing(12);

        Label lblDateDecision = new Label("DATE DÉCISION");
        lblDateDecision
                .setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");

        DatePicker dateDecision = new DatePicker();
        dateDecision.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(java.time.LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(java.time.LocalDate.now()));
            }
        });
        dateDecision.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                "-fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; " +
                "-fx-font-size: 13px; -fx-text-fill: white;");
        dateDecision.setPrefWidth(Double.MAX_VALUE);

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
            }
        });

        // User Selection
        Label lblUser = new Label("UTILISATEUR");
        lblUser.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");
        ComboBox<String> userCombo = new ComboBox<>();
        userCombo.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                "-fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; " +
                "-fx-font-size: 13px; -fx-text-fill: white; -fx-control-inner-background: rgba(26,26,46,0.9);");
        userCombo.setPrefWidth(Double.MAX_VALUE);
        userCombo.setPromptText("Sélectionnez l'utilisateur");

        try {
            List<Utilisateur> users = entretienService.getUsersWithAvailableInterviews();
            for (Utilisateur u : users) {
                userCombo.getItems().add(u.getId_utilisateur() + " - " + u.getNom() + " " + u.getPrenom());
            }
            if (users.isEmpty()) {
                userCombo.setPromptText("Aucun utilisateur avec entretien disponible");
            }
        } catch (Exception ex) {
            System.err.println("Error loading users: " + ex.getMessage());
        }

        // Date Decision (already defined above)

        Label lblDecision = new Label("DÉCISION");
        lblDecision
                .setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");

        ComboBox<String> decisionCombo = new ComboBox<>();
        decisionCombo.setItems(FXCollections.observableArrayList("Accepté", "Refusé", "En attente"));
        decisionCombo.setValue("En attente");
        decisionCombo.setDisable(true); // Default to 'En attente' for new recruitments
        decisionCombo.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                "-fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; " +
                "-fx-font-size: 13px; -fx-text-fill: white; -fx-control-inner-background: rgba(26,26,46,0.9);");
        decisionCombo.setPrefWidth(Double.MAX_VALUE);

        Label lblInterview = new Label("ENTRETIEN");
        lblInterview
                .setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");

        ComboBox<String> interviewCombo = new ComboBox<>();
        interviewCombo.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; " +
                "-fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; " +
                "-fx-font-size: 13px; -fx-text-fill: white; -fx-control-inner-background: rgba(26,26,46,0.9);");
        interviewCombo.setPrefWidth(Double.MAX_VALUE);
        interviewCombo.setPromptText("Sélectionnez d'abord l'utilisateur");
        interviewCombo.setDisable(true);

        userCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                try {
                    int userId = Integer.parseInt(newVal.split(" - ")[0]);
                    List<Entretien> filtered = entretienService.getEntretiensByCandidate(userId);
                    interviewCombo.getItems().clear();
                    for (Entretien e : filtered) {
                        interviewCombo.getItems()
                                .add(e.getIdEntretien() + " - " + e.getDateEntretien() + " (" + e.getTypeEntretien()
                                        + ")");
                    }
                    interviewCombo.setDisable(false);
                    interviewCombo
                            .setPromptText(filtered.isEmpty() ? "Aucun entretien trouvé" : "Sélectionnez l'entretien");
                } catch (Exception ex) {
                    System.err.println("Error filtering interviews: " + ex.getMessage());
                }
            } else {
                interviewCombo.setDisable(true);
                interviewCombo.getItems().clear();
                interviewCombo.setPromptText("Sélectionnez d'abord l'utilisateur");
            }
        });

        formFields.getChildren().addAll(
                new VBox(3, lblUser, userCombo),
                new VBox(3, lblDateDecision, dateDecision, dateError),
                new VBox(3, lblDecision, decisionCombo),
                new VBox(3, lblInterview, interviewCombo));

        HBox buttonBox = new HBox(10);
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
                "-fx-background-radius: 10; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(102,126,234,0.4), 15, 0, 0, 5);");

        btnSave.setOnAction(e -> {
            try {
                if (dateDecision.getValue() == null || userCombo.getValue() == null
                        || interviewCombo.getValue() == null) {
                    showAlert("Validation", "Veuillez remplir tous les champs !");
                    return;
                }

                int userId = Integer.parseInt(userCombo.getValue().split(" - ")[0]);
                int interviewId = Integer.parseInt(interviewCombo.getValue().split(" - ")[0]);

                Recrutement r = new Recrutement();
                r.setDate_decision(Date.valueOf(dateDecision.getValue()));
                r.setDecision_finale(decisionCombo.getValue());
                r.setId_entretien(interviewId);
                r.setId_utilisateur(userId);

                serviceRecrutement.ajouter(r);
                sendNotificationEmail(r); // Send email if decision is final
                loadRecrutementData();
                modalStage.close();
                showAlert("Succès", "Recrutement ajouté avec succès!");
            } catch (Exception ex) {
                showAlert("Erreur", "Vérifiez les champs: " + ex.getMessage());
            }
        });

        buttonBox.getChildren().addAll(btnCancel, btnSave);
        modalContent.getChildren().addAll(header, formFields, buttonBox);

        Scene scene = new Scene(modalContent, 400, 500);
        scene.setFill(Color.TRANSPARENT);
        modalStage.setScene(scene);
        modalStage.showAndWait();
    }

    @FXML
    private void ajouterContrat() {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Ajouter un Contrat");

        VBox modalContent = new VBox();
        modalContent.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #1a1a2e, #16213e); " +
                "-fx-background-radius: 20; -fx-padding: 30;");
        modalContent.setSpacing(15);

        Label title = new Label("Nouveau Contrat");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label("Créer un nouveau contrat");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #9CA3AF;");

        VBox header = new VBox(title, subtitle);
        header.setStyle("-fx-spacing: 5;");

        VBox formFields = new VBox();
        formFields.setSpacing(12);

        // 1. TYPE DE CONTRAT
        Label lblType = new Label("TYPE DE CONTRAT");
        lblType.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");
        ComboBox<String> typeCombo = new ComboBox<>(
                FXCollections.observableArrayList("CDI", "CDD", "Stage", "Freelance", "Alternance"));
        typeCombo.setStyle(
                "-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; -fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; -fx-font-size: 13px; -fx-text-fill: white; -fx-control-inner-background: rgba(26,26,46,0.9);");
        typeCombo.setPrefWidth(Double.MAX_VALUE);

        // 2. DATE DÉBUT
        Label lblDateStart = new Label("DATE DÉBUT");
        lblDateStart
                .setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");
        DatePicker startPicker = new DatePicker(LocalDate.now());
        startPicker.setStyle(
                "-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; -fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; -fx-font-size: 13px; -fx-text-fill: white;");
        startPicker.setPrefWidth(Double.MAX_VALUE);

        // 3. DATE FIN
        Label lblDateEnd = new Label("DATE FIN");
        lblDateEnd
                .setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");
        DatePicker endPicker = new DatePicker();
        endPicker.setStyle(
                "-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; -fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; -fx-font-size: 13px; -fx-text-fill: white;");
        endPicker.setPrefWidth(Double.MAX_VALUE);

        // 4. SALAIRE
        Label lblSalaire = new Label("SALAIRE");
        lblSalaire
                .setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");
        TextField salaryField = new TextField();
        salaryField.setPromptText("Entrez le salaire");
        salaryField.setStyle(
                "-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; -fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; -fx-font-size: 13px; -fx-text-fill: white; -fx-prompt-text-fill: #6B7280;");

        // 5. STATUT (Automatique)
        Label lblStatus = new Label("STATUT (Automatique)");
        lblStatus
                .setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");
        ComboBox<String> statusCombo = new ComboBox<>(
                FXCollections.observableArrayList("Actif", "Terminé", "En attente", "Annulé"));
        statusCombo.setValue("En attente");
        statusCombo.setDisable(true);
        statusCombo.setStyle(
                "-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; -fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; -fx-font-size: 13px; -fx-text-fill: white; -fx-control-inner-background: rgba(26,26,46,0.9); -fx-opacity: 0.8;");
        statusCombo.setPrefWidth(Double.MAX_VALUE);

        Label lblStatusNote = new Label("(Rempli automatiquement selon les dates)");
        lblStatusNote.setStyle("-fx-font-size: 10px; -fx-text-fill: #667eea; -fx-font-style: italic;");

        // Auto Status Logic
        Runnable updateStatus = () -> {
            LocalDate start = startPicker.getValue();
            LocalDate end = endPicker.getValue();
            LocalDate today = LocalDate.now();
            if (start == null)
                return;
            if (start.isAfter(today))
                statusCombo.setValue("En attente");
            else if (end != null && end.isBefore(today))
                statusCombo.setValue("Terminé");
            else
                statusCombo.setValue("Actif");
        };
        startPicker.valueProperty().addListener((obs, ov, nv) -> updateStatus.run());
        endPicker.valueProperty().addListener((obs, ov, nv) -> updateStatus.run());

        // 6. VOLUME HORAIRE
        Label lblVolume = new Label("VOLUME HORAIRE");
        lblVolume
                .setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");
        TextField volumeField = new TextField();
        volumeField.setPromptText("Ex: 35h");
        volumeField.setStyle(
                "-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; -fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; -fx-font-size: 13px; -fx-text-fill: white; -fx-prompt-text-fill: #6B7280;");

        // 7. AVANTAGES
        Label lblAvantages = new Label("AVANTAGES");
        lblAvantages
                .setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");
        ComboBox<String> advantagesCombo = new ComboBox<>(
                FXCollections.observableArrayList("Tickets Restaurant", "Mutuelle", "Télétravail", "Voiture Fonction",
                        "Pass Navigo"));
        advantagesCombo.setStyle(
                "-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; -fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; -fx-font-size: 13px; -fx-text-fill: white; -fx-control-inner-background: rgba(26,26,46,0.9);");
        advantagesCombo.setPrefWidth(Double.MAX_VALUE);

        // 8. RECRUTEMENT
        Label lblRec = new Label("RECRUTEMENT");
        lblRec.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-letter-spacing: 1;");
        ComboBox<String> recCombo = new ComboBox<>();
        recCombo.setStyle(
                "-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; -fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; -fx-font-size: 13px; -fx-text-fill: white; -fx-control-inner-background: rgba(26,26,46,0.9);");
        recCombo.setPrefWidth(Double.MAX_VALUE);

        try {
            List<Recrutement> recs = serviceRecrutement.afficher();
            for (Recrutement r : recs) {
                if ("Accepté".equalsIgnoreCase(r.getDecision_finale())) {
                    Utilisateur u = serviceUtilisateur.getById(r.getId_utilisateur());
                    String userName = (u != null) ? u.getNom() + " " + u.getPrenom() : "N/A";
                    recCombo.getItems().add(r.getId_recrutement() + " - " + r.getDate_decision() + " - " + userName);
                }
            }
        } catch (Exception ex) {
            System.err.println("Error loading recruitments: " + ex.getMessage());
        }

        formFields.getChildren().addAll(
                new VBox(3, lblType, typeCombo),
                new VBox(3, lblDateStart, startPicker),
                new VBox(3, lblDateEnd, endPicker),
                new VBox(3, lblSalaire, salaryField),
                new VBox(3, lblStatus, statusCombo, lblStatusNote),
                new VBox(3, lblVolume, volumeField),
                new VBox(3, lblAvantages, advantagesCombo),
                new VBox(3, lblRec, recCombo));

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setStyle("-fx-padding: 20 0 0 0;");

        Button btnCancel = new Button("Annuler");
        btnCancel.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #9CA3AF; -fx-font-size: 13px; -fx-font-weight: 600; "
                        +
                        "-fx-padding: 12 30; -fx-background-radius: 10; -fx-border-color: rgba(156,163,175,0.3); -fx-border-radius: 10; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> modalStage.close());

        Button btnSave = new Button("✨ Enregistrer");
        btnSave.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 0%, #667eea, #f093fb); -fx-text-fill: white; "
                        +
                        "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 12 30; -fx-background-radius: 10; -fx-cursor: hand; "
                        +
                        "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.4), 15, 0, 0, 5);");

        btnSave.setOnAction(e -> {
            try {
                if (typeCombo.getValue() == null || startPicker.getValue() == null || salaryField.getText().isEmpty()
                        || recCombo.getValue() == null) {
                    showAlert("Validation", "Veuillez remplir les champs obligatoires !");
                    return;
                }

                int recId = Integer.parseInt(recCombo.getValue().split(" - ")[0]);
                Contrat c = new Contrat();
                c.setType_contrat(typeCombo.getValue());
                c.setDate_debut(Date.valueOf(startPicker.getValue()));
                if (endPicker.getValue() != null)
                    c.setDate_fin(Date.valueOf(endPicker.getValue()));
                c.setSalaire(Double.parseDouble(salaryField.getText()));
                c.setStatus(statusCombo.getValue());
                c.setVolume_horaire(volumeField.getText());
                c.setAvantages(advantagesCombo.getValue());
                c.setId_recrutement(recId);

                serviceContrat.ajouter(c);
                loadContratData();
                modalStage.close();
                showAlert("Succès", "Contrat ajouté avec succès!");
            } catch (Exception ex) {
                showAlert("Erreur", "Vérifiez les champs: " + ex.getMessage());
            }
        });

        buttonBox.getChildren().addAll(btnCancel, btnSave);
        modalContent.getChildren().addAll(header, formFields, buttonBox);

        ScrollPane sp = new ScrollPane(modalContent);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-width: 0;");
        sp.setFitToWidth(true);
        sp.setPrefHeight(650);

        Scene scene = new Scene(sp, 480, 700);
        scene.setFill(Color.TRANSPARENT);
        modalStage.setScene(scene);
        modalStage.showAndWait();
    }

    private void editRecrutement(RecrutementTableRow row) {
        if (row == null || row.isGroupHeader())
            return;
        Recrutement r = row.getRecrutement();
        if (r == null)
            return;

        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Modifier Recrutement");

        VBox modalContent = new VBox();
        modalContent.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #1a1a2e, #16213e); -fx-background-radius: 20; -fx-padding: 30;");
        modalContent.setSpacing(15);

        Label title = new Label("Modifier Recrutement");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");
        VBox header = new VBox(title);

        VBox formFields = new VBox();
        formFields.setSpacing(12);

        // Date Picker
        Label lblDate = new Label("DATE DÉCISION");
        lblDate.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea;");
        DatePicker datePicker = new DatePicker(r.getDate_decision().toLocalDate());
        datePicker.setStyle(
                "-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; -fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; -fx-font-size: 13px; -fx-text-fill: white;");
        datePicker.setPrefWidth(Double.MAX_VALUE);

        // Decision ComboBox
        Label lblDecision = new Label("DÉCISION");
        lblDecision.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea;");
        ComboBox<String> decisionCombo = new ComboBox<>(
                FXCollections.observableArrayList("Accepté", "Refusé", "En attente"));
        decisionCombo.setValue(r.getDecision_finale());
        decisionCombo.setStyle(
                "-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; -fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; -fx-font-size: 13px; -fx-text-fill: white; -fx-control-inner-background: rgba(26,26,46,0.9);");
        decisionCombo.setPrefWidth(Double.MAX_VALUE);

        formFields.getChildren().addAll(new VBox(3, lblDate, datePicker), new VBox(3, lblDecision, decisionCombo));

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setStyle("-fx-padding: 20 0 0 0;");

        Button btnCancel = new Button("Annuler");
        btnCancel.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #9CA3AF; -fx-padding: 12 30; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> modalStage.close());

        Button btnSave = new Button("💾 Enregistrer");
        btnSave.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 0%, #667eea, #f093fb); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 30; -fx-background-radius: 10; -fx-cursor: hand;");
        btnSave.setOnAction(e -> {
            try {
                r.setDate_decision(java.sql.Date.valueOf(datePicker.getValue()));
                r.setDecision_finale(decisionCombo.getValue());
                serviceRecrutement.modifier(r);
                sendNotificationEmail(r); // Send email on status update
                loadRecrutementData();
                modalStage.close();
                showAlert("Succès", "Recrutement modifié avec succès!");
            } catch (SQLException ex) {
                showAlert("Erreur", "Modification échouée: " + ex.getMessage());
            }
        });

        buttonBox.getChildren().addAll(btnCancel, btnSave);
        modalContent.getChildren().addAll(header, formFields, buttonBox);
        modalStage.setScene(new Scene(modalContent, 400, 350, Color.TRANSPARENT));
        modalStage.showAndWait();
    }

    private void deleteRecrutement(RecrutementTableRow row) {
        if (row == null || row.isGroupHeader())
            return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce recrutement ?", ButtonType.YES,
                ButtonType.NO);
        alert.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                try {
                    serviceRecrutement.supprimer(row.getRecruitmentId());
                    loadRecrutementData();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void editContrat(Contrat c) {
        if (c == null)
            return;
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Modifier Contrat");

        VBox modalContent = new VBox();
        modalContent.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #1a1a2e, #16213e); -fx-background-radius: 20; -fx-padding: 30;");
        modalContent.setSpacing(15);

        Label title = new Label("Modifier Contrat");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        VBox formFields = new VBox();
        formFields.setSpacing(12);

        // Salary Field
        Label lblSalaire = new Label("SALAIRE");
        lblSalaire.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea;");
        TextField salaryField = new TextField(String.valueOf(c.getSalaire()));
        salaryField.setStyle(
                "-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; -fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; -fx-font-size: 13px; -fx-text-fill: white;");

        // Status
        Label lblStatus = new Label("STATUT");
        lblStatus.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667eea;");
        ComboBox<String> statusCombo = new ComboBox<>(
                FXCollections.observableArrayList("Actif", "Terminé", "En attente", "Annulé"));
        statusCombo.setValue(c.getStatus());
        statusCombo.setStyle(
                "-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; -fx-border-color: rgba(102,126,234,0.3); -fx-border-radius: 10; -fx-padding: 12 14; -fx-font-size: 13px; -fx-text-fill: white; -fx-control-inner-background: rgba(26,26,46,0.9);");
        statusCombo.setPrefWidth(Double.MAX_VALUE);

        formFields.getChildren().addAll(new VBox(3, lblSalaire, salaryField), new VBox(3, lblStatus, statusCombo));

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setStyle("-fx-padding: 20 0 0 0;");

        Button btnCancel = new Button("Annuler");
        btnCancel.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #9CA3AF; -fx-padding: 12 30; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> modalStage.close());

        Button btnSave = new Button("💾 Enregistrer");
        btnSave.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 0%, #667eea, #f093fb); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 30; -fx-background-radius: 10; -fx-cursor: hand;");
        btnSave.setOnAction(e -> {
            try {
                c.setSalaire(Double.parseDouble(salaryField.getText()));
                c.setStatus(statusCombo.getValue());
                serviceContrat.modifier(c);
                loadContratData();
                modalStage.close();
                showAlert("Succès", "Contrat modifié avec succès!");
            } catch (Exception ex) {
                showAlert("Erreur", "Echec: " + ex.getMessage());
            }
        });

        buttonBox.getChildren().addAll(btnCancel, btnSave);
        modalContent.getChildren().addAll(title, formFields, buttonBox);
        modalStage.setScene(new Scene(modalContent, 400, 350, Color.TRANSPARENT));
        modalStage.showAndWait();
    }

    private void deleteContrat(Contrat c) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce contrat ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                try {
                    serviceContrat.supprimer(c.getId_contrat());
                    loadContratData();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void generateContractPdf(Contrat c) {
        try {
            // Get recruitment for context
            List<Recrutement> recs = serviceRecrutement.afficher();
            Recrutement target = null;
            for (Recrutement r : recs)
                if (r.getId_recrutement() == c.getId_recrutement())
                    target = r;

            if (target != null) {
                Map<String, String> ctx = serviceRecrutement.getAIContext(target.getId_recrutement());
                ctx.put("date_debut", c.getDate_debut().toString());
                ctx.put("date_fin", c.getDate_fin() != null ? c.getDate_fin().toString() : "Indéterminée");
                ctx.put("salaire", c.getSalaire() + " DT");
                ctx.put("status", c.getStatus());
                ctx.put("volume_horaire", c.getVolume_horaire());
                ctx.put("avantages", c.getAvantages());

                File pdf = pdfService.generateTemporaryContractPDF(target, ctx, true);
                if (pdf != null && pdf.exists()) {
                    java.awt.Desktop.getDesktop().open(pdf);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendNotificationEmail(Recrutement r) {
        if (r == null || r.getDecision_finale() == null || "En attente".equalsIgnoreCase(r.getDecision_finale())) {
            return;
        }

        new Thread(() -> {
            try {
                String email = serviceRecrutement.getUserEmailById(r.getId_utilisateur());
                if (email == null || email.isEmpty()) {
                    System.err.println("Aucun email trouvé pour l'utilisateur #" + r.getId_utilisateur());
                    return;
                }

                Map<String, String> context = serviceRecrutement.getAIContext(r.getId_recrutement());
                String candidateName = context.getOrDefault("candidate_name", "Candidat");
                String jobTitle = context.getOrDefault("job_title", "le poste");
                String type = "Accepté".equalsIgnoreCase(r.getDecision_finale()) ? "Acceptation" : "Refus";

                File pdfFile = null;
                if ("Acceptation".equals(type)) {
                    Contrat c = serviceContrat.getByRecrutementId(r.getId_recrutement());
                    if (c != null) {
                        context.put("date_debut", c.getDate_debut().toString());
                        context.put("date_fin", c.getDate_fin() != null ? c.getDate_fin().toString() : "Indéterminée");
                        context.put("salaire", c.getSalaire() + " DT");
                        context.put("status", c.getStatus());
                        context.put("volume_horaire", c.getVolume_horaire());
                        context.put("avantages", c.getAvantages());

                        // Generate PDF without employee signature (false)
                        pdfFile = pdfService.generateTemporaryContractPDF(r, context, false);
                    }
                }

                String subject = "Mise à jour de votre candidature - VOS";
                String body = emailService.getTemplate(type, candidateName, jobTitle);

                emailService.sendEmail(email, subject, body, pdfFile);
                System.out.println(
                        "📧 Email de notification envoyé à: " + email + (pdfFile != null ? " (avec contrat PDF)" : ""));
            } catch (Exception e) {
                System.err.println("❌ Échec d'envoi d'email: " + e.getMessage());
            }
        }).start();
    }

    private void downloadContractPdf(Contrat c) {
        try {
            // Get recruitment for context
            List<Recrutement> recs = serviceRecrutement.afficher();
            Recrutement target = null;
            for (Recrutement r : recs)
                if (r.getId_recrutement() == c.getId_recrutement())
                    target = r;

            if (target != null) {
                Map<String, String> ctx = serviceRecrutement.getAIContext(target.getId_recrutement());
                ctx.put("date_debut", c.getDate_debut().toString());
                ctx.put("date_fin", c.getDate_fin() != null ? c.getDate_fin().toString() : "Indéterminée");
                ctx.put("salaire", c.getSalaire() + " DT");
                ctx.put("status", c.getStatus());
                ctx.put("volume_horaire", c.getVolume_horaire());
                ctx.put("avantages", c.getAvantages());

                File pdf = pdfService.generateTemporaryContractPDF(target, ctx, true);
                if (pdf != null && pdf.exists()) {
                    javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
                    fileChooser.setTitle("Enregistrer le contrat PDF");
                    fileChooser.setInitialFileName("Contrat_" + target.getId_recrutement() + ".pdf");
                    fileChooser.getExtensionFilters()
                            .add(new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

                    File dest = fileChooser.showSaveDialog(tableContrats.getScene().getWindow());
                    if (dest != null) {
                        java.nio.file.Files.copy(pdf.toPath(), dest.toPath(),
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        showAlert("Succès", "Contrat enregistré avec succès à : " + dest.getAbsolutePath());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de générer ou d'enregistrer le PDF.");
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
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

            // Recruitments by User (Mock Data)
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
}
