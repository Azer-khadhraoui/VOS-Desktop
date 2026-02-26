package controllers;

import entities.Entretien;
import entities.EvaluationEntretien;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import services.EntretienService;
import services.EvaluationEntretienService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Label;

import java.sql.Date;
import java.sql.Time;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainController {

    private final EntretienService entretienService = new EntretienService();
    private final EvaluationEntretienService evaluationService = new EvaluationEntretienService();

    // Navigation
    @FXML private StackPane contentArea;
    @FXML private VBox pageEntretiens;
    @FXML private VBox pageDashboard;
    @FXML private VBox pageStats;

    @FXML private Button btnNavEntretiens;
    @FXML private Button btnNavDashboard;
    @FXML private Button btnNavStats;

    // Search
    @FXML private TextField searchField;

    // Entretien Table
    @FXML private TableView<Entretien> tableEntretiens;
    @FXML private TableColumn<Entretien, Integer> colIdEntretien;
    @FXML private TableColumn<Entretien, Date> colDateEntretien;
    @FXML private TableColumn<Entretien, Time> colHeureEntretien;
    @FXML private TableColumn<Entretien, String> colTypeEntretien;
    @FXML private TableColumn<Entretien, String> colStatutEntretien;
    @FXML private TableColumn<Entretien, String> colLieuEntretien;
    @FXML private TableColumn<Entretien, String> colTypeTest;
    @FXML private TableColumn<Entretien, Void> colActions;

    // Evaluation Table
    @FXML private TableView<EvaluationEntretien> tableEvaluations;
    @FXML private TableColumn<EvaluationEntretien, Integer> colIdEvaluation;
    @FXML private TableColumn<EvaluationEntretien, Double> colScoreTest;
    @FXML private TableColumn<EvaluationEntretien, Integer> colNoteEntretien;
    @FXML private TableColumn<EvaluationEntretien, String> colCommentaire;
    @FXML private TableColumn<EvaluationEntretien, String> colDecision;
    @FXML private TableColumn<EvaluationEntretien, Integer> colIdEntretienEval;
    @FXML private TableColumn<EvaluationEntretien, Void> colActionsEval;







    @FXML
    public void initialize() {
        setupEntretienTable();
        setupEvaluationTable();
        rafraichirEntretiens();
        rafraichirEvaluations();
        setupSearch();
        setupSearchFunctionality();
        setupDoubleClickOnEntretien();
    }

    // Double-clic sur un entretien pour afficher son évaluation
    private void setupDoubleClickOnEntretien() {
        tableEntretiens.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tableEntretiens.getSelectionModel().getSelectedItem() != null) {
                Entretien entretien = tableEntretiens.getSelectionModel().getSelectedItem();
                voirEvaluationsEntretien(entretien);
            }
        });
    }

    private void setupSearch() {
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filtrerEntretiens(newValue);
            });
        }
    }

    private void setupSearchFunctionality() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (tableEntretiens.isVisible()) {
                filterEntretiens(newValue);
            } else if (tableEvaluations.isVisible()) {
                filterEvaluations(newValue);
            }
        });
    }

    private void filterEntretiens(String recherche) {
        if (recherche == null || recherche.isEmpty()) {
            tableEntretiens.setItems(FXCollections.observableArrayList(entretienService.getAllEntretiens()));
            return;
        }

        ObservableList<Entretien> entretiensFiltrés = FXCollections.observableArrayList(
                entretienService.getAllEntretiens().stream()
                        .filter(e ->
                                e.getTypeEntretien().toLowerCase().contains(recherche.toLowerCase()) ||
                                        e.getStatutEntretien().toLowerCase().contains(recherche.toLowerCase()) ||
                                        e.getLieu().toLowerCase().contains(recherche.toLowerCase()) ||
                                        e.getTypeTest().toLowerCase().contains(recherche.toLowerCase()) ||
                                        String.valueOf(e.getIdEntretien()).contains(recherche)
                        )
                        .collect(Collectors.toList())
        );

        tableEntretiens.setItems(entretiensFiltrés);
    }

    private void filterEvaluations(String recherche) {
        if (recherche == null || recherche.isEmpty()) {
            tableEvaluations.setItems(FXCollections.observableArrayList(evaluationService.getAllEvaluations()));
            return;
        }

        String r = recherche.toLowerCase();
        ObservableList<EvaluationEntretien> evaluationsFiltrées = FXCollections.observableArrayList(
                evaluationService.getAllEvaluations().stream()
                        .filter(e -> {
                            String commentaire = e.getCommentaire() != null ? e.getCommentaire().toLowerCase() : "";
                            String decision    = e.getDecision()    != null ? e.getDecision().toLowerCase()    : "";
                            return commentaire.contains(r) ||
                                    decision.contains(r) ||
                                    String.valueOf(e.getIdEvaluation()).contains(r) ||
                                    String.valueOf(e.getIdEntretien()).contains(r) ||
                                    String.valueOf(e.getScoreTest()).contains(r) ||
                                    String.valueOf(e.getNoteEntretien()).contains(r);
                        })
                        .collect(Collectors.toList())
        );

        tableEvaluations.setItems(evaluationsFiltrées);
    }

    private void filtrerEntretiens(String recherche) {
        if (recherche == null || recherche.isEmpty()) {
            rafraichirEntretiens();
            return;
        }

        ObservableList<Entretien> entretiensFiltrés = FXCollections.observableArrayList(
                entretienService.getAllEntretiens().stream()
                        .filter(e ->
                                e.getTypeEntretien().toLowerCase().contains(recherche.toLowerCase()) ||
                                        e.getStatutEntretien().toLowerCase().contains(recherche.toLowerCase()) ||
                                        e.getLieu().toLowerCase().contains(recherche.toLowerCase()) ||
                                        e.getTypeTest().toLowerCase().contains(recherche.toLowerCase()) ||
                                        String.valueOf(e.getIdEntretien()).contains(recherche)
                        )
                        .collect(Collectors.toList())
        );

        tableEntretiens.setItems(entretiensFiltrés);
    }

    private void setupEntretienTable() {
        colIdEntretien.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getIdEntretien()).asObject());
        colDateEntretien.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getDateEntretien()));
        colHeureEntretien.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getHeureEntretien()));
        colTypeEntretien.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTypeEntretien()));

        // STATUT avec badge coloré
        colStatutEntretien.setCellFactory(column -> new TableCell<Entretien, String>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(statut);
                    badge.getStyleClass().add("status-badge");

                    switch (statut.toLowerCase()) {
                        case "terminé":
                            badge.getStyleClass().add("status-termine");
                            break;
                        case "confirmé":
                            badge.getStyleClass().add("status-confirme");
                            break;
                        case "planifié":
                            badge.getStyleClass().add("status-planifie");
                            break;
                        case "annulé":
                            badge.getStyleClass().add("status-annule");
                            break;
                        default:
                            badge.getStyleClass().add("status-default");
                    }

                    setText(null);
                    setGraphic(badge);
                }
            }
        });
        colStatutEntretien.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatutEntretien()));

        colLieuEntretien.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getLieu()));
        colTypeTest.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTypeTest()));

        // Actions - 3 ou 4 boutons selon le statut
        colActions.setCellFactory(new Callback<TableColumn<Entretien, Void>, TableCell<Entretien, Void>>() {
            @Override
            public TableCell<Entretien, Void> call(TableColumn<Entretien, Void> param) {
                return new TableCell<Entretien, Void>() {

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Entretien entretien = getTableView().getItems().get(getIndex());

                            // Boutons communs
                            Button btnViewEval = new Button("�");
                            Button btnEdit = new Button("✎");
                            Button btnDelete = new Button("╳");

                            btnViewEval.getStyleClass().addAll("action-btn", "action-btn-view");
                            btnEdit.getStyleClass().addAll("action-btn", "action-btn-edit");
                            btnDelete.getStyleClass().addAll("action-btn", "action-btn-delete");

                            btnViewEval.setOnAction(event -> voirEvaluationsEntretien(entretien));
                            btnEdit.setOnAction(event -> modifierEntretienFromRow(entretien));
                            btnDelete.setOnAction(event -> supprimerEntretienFromRow(entretien));

                            HBox pane;

                            // Si Terminé, ajouter le bouton Évaluer
                            if (entretien.getStatutEntretien().equalsIgnoreCase("Terminé")) {
                                Button btnEvaluer = new Button("⭐");
                                btnEvaluer.getStyleClass().addAll("action-btn", "action-btn-evaluer");
                                btnEvaluer.setOnAction(event -> evaluerEntretienFromRow(entretien));

                                // 4 boutons
                                pane = new HBox(6, btnViewEval, btnEvaluer, btnEdit, btnDelete);
                            } else {
                                // 3 boutons
                                pane = new HBox(8, btnViewEval, btnEdit, btnDelete);
                            }

                            pane.setAlignment(Pos.CENTER);
                            setGraphic(pane);
                        }
                    }
                };
            }
        });
    }

    // Méthode helper pour créer les boutons
    private Button createActionButton(String icon, String text) {
        Button btn = new Button(icon);
        btn.setMinWidth(52);
        btn.setPrefWidth(52);
        btn.setMaxWidth(52);
        btn.setMinHeight(40);
        btn.setPrefHeight(40);
        return btn;
    }

    private void setupEvaluationTable() {
        colIdEvaluation.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getIdEvaluation()).asObject());
        colScoreTest.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getScoreTest()).asObject());
        colNoteEntretien.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getNoteEntretien()).asObject());
        colCommentaire.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCommentaire()));

        // DÉCISION avec badge coloré (comme Statut)
        colDecision.setCellFactory(column -> new TableCell<EvaluationEntretien, String>() {
            @Override
            protected void updateItem(String decision, boolean empty) {
                super.updateItem(decision, empty);
                if (empty || decision == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(decision);
                    badge.getStyleClass().add("status-badge");

                    switch (decision.toLowerCase()) {
                        case "accepté":
                            badge.getStyleClass().add("status-termine");
                            break;
                        case "refusé":
                            badge.getStyleClass().add("status-annule");
                            break;
                        case "en attente":
                            badge.getStyleClass().add("status-planifie");
                            break;
                        default:
                            badge.getStyleClass().add("status-confirme");
                    }

                    setText(null);
                    setGraphic(badge);
                }
            }
        });
        colDecision.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDecision()));

        colIdEntretienEval.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getIdEntretien()).asObject());

        // Actions avec boutons rectangulaires
        colActionsEval.setCellFactory(new Callback<TableColumn<EvaluationEntretien, Void>, TableCell<EvaluationEntretien, Void>>() {
            @Override
            public TableCell<EvaluationEntretien, Void> call(TableColumn<EvaluationEntretien, Void> param) {
                return new TableCell<EvaluationEntretien, Void>() {

                    private final Button btnView   = new Button("👁");
                    private final Button btnEdit   = new Button("✎");
                    private final Button btnAI     = new Button("🤖");
                    private final Button btnDelete = new Button("╳");
                    private final HBox pane = new HBox(8);

                    {
                        btnView.getStyleClass().addAll("action-btn", "action-btn-view");
                        btnEdit.getStyleClass().addAll("action-btn", "action-btn-edit");
                        btnAI.getStyleClass().addAll("action-btn", "action-btn-star");
                        btnDelete.getStyleClass().addAll("action-btn", "action-btn-delete");
                        btnAI.setTooltip(new javafx.scene.control.Tooltip("Analyser avec IA"));

                        pane.setAlignment(Pos.CENTER);
                        pane.getChildren().addAll(btnView, btnEdit, btnAI, btnDelete);

                        btnView.setOnAction(event -> {
                            EvaluationEntretien evaluation = getTableView().getItems().get(getIndex());
                            voirEvaluation(evaluation);
                        });

                        btnEdit.setOnAction(event -> {
                            EvaluationEntretien evaluation = getTableView().getItems().get(getIndex());
                            modifierEvaluationFromRow(evaluation);
                        });

                        btnAI.setOnAction(event -> {
                            EvaluationEntretien evaluation = getTableView().getItems().get(getIndex());
                            analyserEvaluationIA(evaluation);
                        });

                        btnDelete.setOnAction(event -> {
                            EvaluationEntretien evaluation = getTableView().getItems().get(getIndex());
                            supprimerEvaluationFromRow(evaluation);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : pane);
                    }
                };
            }
        });
    }
    // ============================================================
    // ANALYSE IA D'UNE ÉVALUATION
    // ============================================================
    private void analyserEvaluationIA(EvaluationEntretien evaluation) {
        // Fenêtre de chargement
        Stage loadingStage = new Stage();
        loadingStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        loadingStage.setTitle("Analyse IA en cours...");

        javafx.scene.control.ProgressIndicator spinner = new javafx.scene.control.ProgressIndicator();
        spinner.setPrefSize(60, 60);
        spinner.setStyle("-fx-progress-color: #667eea;");

        javafx.scene.control.Label loadingLabel = new javafx.scene.control.Label("🤖 L'IA analyse l'évaluation...");
        loadingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151;");

        javafx.scene.layout.VBox loadingBox = new javafx.scene.layout.VBox(15, spinner, loadingLabel);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setStyle("-fx-padding: 40; -fx-background-color: white; -fx-background-radius: 15;");

        loadingStage.setScene(new Scene(loadingBox, 300, 180));
        loadingStage.show();

        // Appel IA en arrière-plan
        services.AIService.analyserEvaluation(evaluation,
                result -> javafx.application.Platform.runLater(() -> {
                    loadingStage.close();
                    afficherRapportIA(evaluation, result);
                }),
                error -> javafx.application.Platform.runLater(() -> {
                    loadingStage.close();
                    showAlert("Erreur IA", "Erreur", error);
                })
        );
    }

    private void afficherRapportIA(EvaluationEntretien evaluation, String rapport) {
        Stage stage = new Stage();
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        stage.setTitle("🤖 Rapport IA — Évaluation #" + evaluation.getIdEvaluation());

        // ===== HEADER =====
        javafx.scene.control.Label titre = new javafx.scene.control.Label("🤖 Analyse IA de l'Évaluation");
        titre.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        javafx.scene.control.Label sousTitre = new javafx.scene.control.Label(
                "Évaluation #" + evaluation.getIdEvaluation()
                        + "  |  Score: " + evaluation.getScoreTest() + "%"
                        + "  |  Note: " + evaluation.getNoteEntretien() + "/5"
                        + "  |  " + evaluation.getDecision());
        sousTitre.setStyle("-fx-font-size: 12px; -fx-text-fill: #A5B4FC;");

        javafx.scene.layout.VBox header = new javafx.scene.layout.VBox(6, titre, sousTitre);
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: linear-gradient(135deg, #667eea, #764ba2);"
                + "-fx-padding: 25 30; -fx-background-radius: 15 15 0 0;");

        // ===== CONTENU DU RAPPORT =====
        javafx.scene.control.TextArea rapportArea = new javafx.scene.control.TextArea(rapport);
        rapportArea.setEditable(false);
        rapportArea.setWrapText(true);
        rapportArea.setPrefHeight(400);
        rapportArea.setStyle("-fx-control-inner-background: #1E293B; -fx-text-fill: #E2E8F0;"
                + "-fx-font-size: 13px; -fx-font-family: 'Segoe UI';"
                + "-fx-background-color: #1E293B; -fx-border-color: transparent;");

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(rapportArea);
        content.setStyle("-fx-background-color: #1E293B; -fx-padding: 20;");

        // ===== BOUTON FERMER =====
        Button btnFermer = new Button("✕ Fermer");
        btnFermer.setStyle("-fx-background-color: linear-gradient(135deg, #667eea, #764ba2);"
                + "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;"
                + "-fx-padding: 10 30; -fx-background-radius: 20; -fx-cursor: hand;");
        btnFermer.setOnAction(e -> stage.close());

        javafx.scene.layout.HBox footer = new javafx.scene.layout.HBox(btnFermer);
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-background-color: #1E293B; -fx-padding: 0 20 20 20;");

        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(header, content, footer);
        root.setStyle("-fx-background-color: #1E293B; -fx-background-radius: 15;");

        stage.setScene(new Scene(root, 650, 580));
        stage.setResizable(false);
        stage.show();
    }

    private void voirEvaluationsEntretien(Entretien entretien) {
        List<EvaluationEntretien> evaluations = evaluationService.getAllEvaluations()
                .stream()
                .filter(eval -> eval.getIdEntretien() == entretien.getIdEntretien())
                .collect(Collectors.toList());

        if (evaluations.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Aucune Évaluation");
            alert.setHeaderText("Entretien #" + entretien.getIdEntretien());
            alert.setContentText("Aucune évaluation trouvée pour cet entretien.");
            alert.showAndWait();
            return;
        }

        try {
            // Charger le dialogue moderne
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EvaluationsDialog.fxml"));
            Parent root = loader.load();

            // Passer les données au contrôleur
            EvaluationsDialogController controller = loader.getController();
            controller.setData(entretien, evaluations);

            // Créer et afficher le dialogue
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Évaluations de l'Entretien");
            dialogStage.setScene(new Scene(root));
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.setResizable(false);
            dialogStage.show();

        } catch (Exception e) {
            showAlert("Erreur", "Erreur", "Impossible d'ouvrir le dialogue: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void evaluerEntretienFromRow(Entretien entretien) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EvaluationGrilleForm.fxml"));
            Parent root = loader.load();

            EvaluationGrilleFormController controller = loader.getController();
            controller.setEntretienToEvaluate(entretien.getIdEntretien());

            Stage stage = new Stage();
            stage.setTitle("Évaluer l'Entretien #" + entretien.getIdEntretien());
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (controller.isSaved()) {
                rafraichirEvaluations();
                showAlert("Info", "Succès", "Évaluation créée avec succès pour l'entretien #" + entretien.getIdEntretien());
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void voirEvaluationDetails(EvaluationEntretien evaluation) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de l'Évaluation");
        alert.setHeaderText("Évaluation #" + evaluation.getIdEvaluation());

        String details = String.format(
                "📊 Score du Test: %.1f\n\n" +
                        "⭐ Note de l'Entretien: %d/5\n\n" +
                        "✅ Décision: %s\n\n" +
                        "🔗 ID Entretien: %d\n\n" +
                        "💬 Commentaire:\n%s",
                evaluation.getScoreTest(),
                evaluation.getNoteEntretien(),
                evaluation.getDecision(),
                evaluation.getIdEntretien(),
                evaluation.getCommentaire()
        );

        alert.setContentText(details);
        alert.setResizable(true);
        alert.getDialogPane().setPrefWidth(550);
        alert.getDialogPane().setPrefHeight(400);
        alert.showAndWait();
    }

    @FXML
    private void rafraichirEntretiens() {
        ObservableList<Entretien> entretiens = FXCollections.observableArrayList(entretienService.getAllEntretiens());
        tableEntretiens.setItems(entretiens);
    }

    @FXML
    private void rafraichirEvaluations() {
        ObservableList<EvaluationEntretien> evaluations = FXCollections.observableArrayList(evaluationService.getAllEvaluations());
        tableEvaluations.setItems(evaluations);
    }

    @FXML
    private void ajouterEntretien() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EntretienForm.fxml"));
            Parent root = loader.load();

            EntretienFormController controller = loader.getController();
            controller.setEntretien(null);

            Stage stage = new Stage();
            stage.setTitle("Ajouter un Entretien");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (controller.isSaved()) {
                rafraichirEntretiens();
                showAlert("Info", "Succès", "Entretien ajouté avec succès");
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void modifierEntretienFromRow(Entretien entretien) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EntretienForm.fxml"));
            Parent root = loader.load();

            EntretienFormController controller = loader.getController();
            controller.setEntretien(entretien);

            Stage stage = new Stage();
            stage.setTitle("Modifier un Entretien");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (controller.isSaved()) {
                rafraichirEntretiens();
                showAlert("Info", "Succès", "Entretien modifié avec succès");
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void supprimerEntretienFromRow(Entretien entretien) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer l'entretien");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cet entretien ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            entretienService.deleteEntretien(entretien.getIdEntretien());
            rafraichirEntretiens();
            showAlert("Info", "Succès", "Entretien supprimé avec succès");
        }
    }

    @FXML
    private void ajouterEvaluation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EvaluationGrilleForm.fxml"));
            Parent root = loader.load();

            EvaluationGrilleFormController controller = loader.getController();
            controller.setEvaluation(null);

            Stage stage = new Stage();
            stage.setTitle("Ajouter une Évaluation");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (controller.isSaved()) {
                rafraichirEvaluations();
                showAlert("Info", "Succès", "Évaluation ajoutée avec succès");
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void modifierEvaluationFromRow(EvaluationEntretien evaluation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EvaluationGrilleForm.fxml"));
            Parent root = loader.load();

            EvaluationGrilleFormController controller = loader.getController();
            controller.setEvaluation(evaluation);

            Stage stage = new Stage();
            stage.setTitle("Modifier une Évaluation");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (controller.isSaved()) {
                rafraichirEvaluations();
                showAlert("Info", "Succès", "Évaluation modifiée avec succès");
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void supprimerEvaluationFromRow(EvaluationEntretien evaluation) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer l'évaluation");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cette évaluation ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            evaluationService.deleteEvaluation(evaluation.getIdEvaluation());
            rafraichirEvaluations();
            showAlert("Info", "Succès", "Évaluation supprimée avec succès");
        }
    }

    private void showAlert(String type, String title, String message) {
        Alert alert = new Alert(type.equals("Erreur") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ========== MÉTHODES DE NAVIGATION ==========

    @FXML
    private void showPageEntretiens() {
        switchPage(pageEntretiens);
        updateActiveNavButton(btnNavEntretiens);
    }

    @FXML
    private void showPageDashboard() {
        switchPage(pageDashboard);
        updateActiveNavButton(btnNavDashboard);
    }

    @FXML
    private void showPageStats() {
        switchPage(pageStats);
        updateActiveNavButton(btnNavStats);
    }

    private void switchPage(VBox pageToShow) {
        // Cacher toutes les pages
        pageEntretiens.setVisible(false);
        pageEntretiens.setManaged(false);

        pageDashboard.setVisible(false);
        pageDashboard.setManaged(false);

        pageStats.setVisible(false);
        pageStats.setManaged(false);

        // Afficher la page demandée
        pageToShow.setVisible(true);
        pageToShow.setManaged(true);
    }

    private void updateActiveNavButton(Button activeBtn) {
        // Retirer la classe active de tous les boutons
        btnNavEntretiens.getStyleClass().remove("nav-icon-active");
        btnNavDashboard.getStyleClass().remove("nav-icon-active");
        btnNavStats.getStyleClass().remove("nav-icon-active");

        // Ajouter la classe active au bouton cliqué
        if (!activeBtn.getStyleClass().contains("nav-icon-active")) {
            activeBtn.getStyleClass().add("nav-icon-active");
        }
    }

    private void voirEvaluation(EvaluationEntretien evaluation) {
        try {
            // Charger le dialogue moderne
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EvaluationDetailDialog.fxml"));
            Parent root = loader.load();

            // Passer les données au contrôleur
            EvaluationDetailController controller = loader.getController();
            controller.setEvaluation(evaluation);

            // Créer et afficher le dialogue
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Détails de l'Évaluation");
            dialogStage.setScene(new Scene(root));
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.setResizable(false);
            dialogStage.show();

        } catch (Exception e) {
            showAlert("Erreur", "Erreur", "Impossible d'ouvrir le dialogue: " + e.getMessage());
            e.printStackTrace();
        }
    }
}