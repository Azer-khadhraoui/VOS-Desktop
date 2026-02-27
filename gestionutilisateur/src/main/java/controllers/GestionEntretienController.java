package controllers;

import entities.Entretien;
import entities.EvaluationEntretien;
import entities.Utilisateur;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.chart.PieChart;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import services.EntretienService;
import services.EvaluationEntretienService;
import utilis.UserSession;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Label;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.Time;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GestionEntretienController {

    private final EntretienService entretienService = new EntretienService();
    private final EvaluationEntretienService evaluationService = new EvaluationEntretienService();

    // Navigation et Sidebar
    @FXML private BorderPane rootBorder;
    @FXML private StackPane contentArea;
    @FXML private VBox pageEntretiens;
    @FXML private VBox pageDashboard;
    @FXML private VBox pageStats;
    @FXML private VBox sidebar;
    @FXML private VBox navContainer;
    @FXML private HBox btnStatistiques;
    @FXML private HBox btnUtilisateurs;
    @FXML private HBox btnOffres;
    @FXML private HBox btnServices;
    @FXML private HBox btnEntretiens;
    @FXML private HBox logoutBtn;

    @FXML private Button btnNavEntretiens;
    @FXML private Button btnNavDashboard;
    @FXML private Button btnNavStats;

    // User Profile
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private StackPane userAvatarContainer;
    @FXML private Label lblUserAvatar;

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

    // Stats labels
    @FXML private Label lblStatTotalEntretiens;
    @FXML private Label lblStatTermines;
    @FXML private Label lblStatPlanifies;
    @FXML private Label lblStatAutres;
    @FXML private Label lblStatTerminesRow;
    @FXML private Label lblStatPlanifiesRow;
    @FXML private Label lblStatAutresRow;
    @FXML private Label lblStatTotalEvaluations;
    @FXML private Label lblStatAvgScore;
    @FXML private Label lblStatAvgNote;
    @FXML private PieChart chartStatut;
    @FXML private PieChart chartType;
    @FXML private Button btnRefreshStats;
    @FXML private ProgressBar barTermines;
    @FXML private ProgressBar barPlanifies;
    @FXML private ProgressBar barAutres;
    @FXML private ProgressBar barAvgScore;
    @FXML private ProgressBar barAvgNote;
    @FXML private VBox statsRoot;

    private boolean embeddedMode = false;







    @FXML
    public void initialize() {
        loadCurrentUser();
        setupEntretienTable();
        setupEvaluationTable();
        rafraichirEntretiens();
        rafraichirEvaluations();
        setupSearch();
        setupSearchFunctionality();
        setupDoubleClickOnEntretien();

        if (embeddedMode && sidebar != null) {
            sidebar.setVisible(false);
            sidebar.setManaged(false);
            if (rootBorder != null) {
                rootBorder.setLeft(null);
            }
        }

        if (statsRoot != null) {
            FadeTransition fade = new FadeTransition(Duration.millis(450), statsRoot);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);

            TranslateTransition slide = new TranslateTransition(Duration.millis(450), statsRoot);
            slide.setFromY(12);
            slide.setToY(0);

            new ParallelTransition(fade, slide).play();
        }
    }

    public void setEmbeddedMode(boolean embeddedMode) {
        this.embeddedMode = embeddedMode;
        if (sidebar != null) {
            sidebar.setVisible(!embeddedMode);
            sidebar.setManaged(!embeddedMode);
        }
        if (embeddedMode && rootBorder != null) {
            rootBorder.setLeft(null);
        }
    }

    private void loadCurrentUser() {
        Utilisateur currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            lblUserName.setText(currentUser.getNom() + " " + currentUser.getPrenom());
            lblUserRole.setText(currentUser.getRole());
            loadUserAvatar(currentUser.getImage_profil());
        }
    }

    private void loadUserAvatar(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return;
        }

        try {
            File imgFile = resolveAvatarFile(imagePath);
            if (imgFile != null && imgFile.exists()) {
                ImageView imageView = new ImageView();
                imageView.setFitWidth(50);
                imageView.setFitHeight(50);
                imageView.setPreserveRatio(false);
                
                javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(25, 25, 25);
                imageView.setClip(clip);
                
                Image img = new Image(new FileInputStream(imgFile));
                imageView.setImage(img);
                
                userAvatarContainer.getChildren().clear();
                userAvatarContainer.getChildren().add(imageView);
                
                System.out.println("✓ Avatar chargé dans GestionEntretienView: " + imgFile.getAbsolutePath());
            } else {
                System.err.println("⚠ Fichier avatar introuvable: " + imagePath);
            }
        } catch (Exception e) {
            System.err.println("✗ Erreur chargement avatar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private File resolveAvatarFile(String imagePath) {
        File direct = new File(imagePath);
        if (direct.exists()) {
            return direct;
        }

        String fileName = direct.getName();
        Path[] candidates = new Path[] {
            Paths.get(System.getProperty("user.dir"), "images", fileName),
            Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "images", fileName),
            Paths.get(System.getProperty("user.dir"), "target", "classes", "images", fileName)
        };

        for (Path candidate : candidates) {
            File file = candidate.toFile();
            if (file.exists()) {
                return file;
            }
        }

        return null;
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EntretienFXML/EvaluationsDialog.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EntretienFXML/EvaluationGrilleForm.fxml"));
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
        rafraichirStats();
    }

    @FXML
    private void rafraichirEvaluations() {
        ObservableList<EvaluationEntretien> evaluations = FXCollections.observableArrayList(evaluationService.getAllEvaluations());
        tableEvaluations.setItems(evaluations);
        rafraichirStats();
    }

    @FXML
    private void rafraichirStats() {
        if (lblStatTotalEntretiens == null) {
            return;
        }

        List<Entretien> entretiens = entretienService.getAllEntretiens();
        List<EvaluationEntretien> evaluations = evaluationService.getAllEvaluations();

        long total = entretiens.size();
        long termines = entretiens.stream()
                .filter(e -> {
                    String statut = e.getStatutEntretien() != null ? e.getStatutEntretien().toLowerCase() : "";
                    return statut.contains("termine") || statut.contains("termin e9");
                })
                .count();
        long planifies = entretiens.stream()
                .filter(e -> {
                    String statut = e.getStatutEntretien() != null ? e.getStatutEntretien().toLowerCase() : "";
                    return statut.contains("planifie") || statut.contains("planifi e9") || statut.contains("confirm");
                })
                .count();
        long autres = Math.max(0, total - termines - planifies);

        long annules = entretiens.stream()
            .filter(e -> {
                String statut = e.getStatutEntretien() != null ? e.getStatutEntretien().toLowerCase() : "";
                return statut.contains("annule") || statut.contains("annul e9") || statut.contains("annulation");
            })
            .count();

        double avgScore = evaluations.stream()
                .mapToDouble(EvaluationEntretien::getScoreTest)
                .average()
                .orElse(0.0);
        double avgNote = evaluations.stream()
                .mapToInt(EvaluationEntretien::getNoteEntretien)
                .average()
                .orElse(0.0);

        lblStatTotalEntretiens.setText(String.valueOf(total));
        lblStatTermines.setText(String.valueOf(termines));
        lblStatPlanifies.setText(String.valueOf(planifies));
        if (lblStatAutres != null) {
            lblStatAutres.setText(String.valueOf(autres));
        }
        lblStatTotalEvaluations.setText(String.valueOf(evaluations.size()));
        lblStatAvgScore.setText(String.format("%.1f", avgScore));
        lblStatAvgNote.setText(String.format("%.1f", avgNote));

        double totalSafe = total == 0 ? 1 : total;
        if (barTermines != null) {
            barTermines.setProgress(total == 0 ? 0 : termines / totalSafe);
        }
        if (barPlanifies != null) {
            barPlanifies.setProgress(total == 0 ? 0 : planifies / totalSafe);
        }
        if (barAutres != null) {
            barAutres.setProgress(total == 0 ? 0 : autres / totalSafe);
        }
        if (barAvgScore != null) {
            barAvgScore.setProgress(Math.max(0, Math.min(1, avgScore / 100.0)));
        }
        if (barAvgNote != null) {
            barAvgNote.setProgress(Math.max(0, Math.min(1, avgNote / 5.0)));
        }

        if (chartStatut != null) {
            ObservableList<PieChart.Data> statutData = FXCollections.observableArrayList();
            statutData.add(new PieChart.Data("Termine", termines));
            statutData.add(new PieChart.Data("Planifie", planifies));
            statutData.add(new PieChart.Data("Annule", annules));
            statutData.add(new PieChart.Data("Autre", Math.max(0, autres - annules)));
            chartStatut.setData(statutData);
        }

        if (chartType != null) {
            java.util.Map<String, Long> typeCounts = entretiens.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            e -> {
                                String type = e.getTypeEntretien();
                                return type == null || type.trim().isEmpty() ? "Inconnu" : type.trim();
                            },
                            java.util.stream.Collectors.counting()
                    ));

            ObservableList<PieChart.Data> typeData = FXCollections.observableArrayList();
            for (java.util.Map.Entry<String, Long> entry : typeCounts.entrySet()) {
                typeData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }
            chartType.setData(typeData);
        }
    }

    @FXML
    private void ajouterEntretien() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EntretienFXML/EntretienForm.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EntretienFXML/EntretienForm.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EntretienFXML/EvaluationGrilleForm.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EntretienFXML/EvaluationGrilleForm.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EntretienFXML/EvaluationDetailDialog.fxml"));
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

    // ============ NAVIGATION METHODS ============

    @FXML
    public void goToStatistiques() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StatistiquesView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableEntretiens.getScene().getWindow();
            Scene newScene = new Scene(root, 1440, 1024);
            stage.setScene(newScene);
            stage.setTitle("Statistiques - VOS Admin");
            stage.centerOnScreen();
            System.out.println("✓ Navigation vers Statistiques");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur de navigation", "Impossible d'ouvrir les statistiques: " + e.getMessage());
        }
    }

    @FXML
    public void goToAdministration() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdministrationView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableEntretiens.getScene().getWindow();
            Scene newScene = new Scene(root, 1440, 1024);
            stage.setScene(newScene);
            stage.setTitle("Administration - VOS Admin");
            stage.centerOnScreen();
            System.out.println("✓ Navigation vers Administration");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur de navigation", "Impossible d'ouvrir l'administration: " + e.getMessage());
        }
    }

    @FXML
    public void goToOffres() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminOffresView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableEntretiens.getScene().getWindow();
            Scene newScene = new Scene(root, 1440, 1024);
            stage.setScene(newScene);
            stage.setTitle("Gestion des Offres - VOS Admin");
            stage.centerOnScreen();
            System.out.println("✓ Navigation vers Gestion Offres");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur de navigation", "Impossible d'ouvrir la gestion des offres: " + e.getMessage());
        }
    }

    @FXML
    public void goToServices() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ServicesView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableEntretiens.getScene().getWindow();
            Scene newScene = new Scene(root, 1440, 1024);
            stage.setScene(newScene);
            stage.setTitle("Services - VOS Admin");
            stage.centerOnScreen();
            System.out.println("✓ Navigation vers Services");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur de navigation", "Impossible d'ouvrir les services: " + e.getMessage());
        }
    }

    @FXML
    public void logout() {
        try {
            UserSession.getInstance().clearSession();
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SigninView.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) tableEntretiens.getScene().getWindow();
            Scene newScene = new Scene(root, 1440, 1024);
            stage.setScene(newScene);
            stage.setResizable(false);
            stage.setTitle("Connexion - VOS");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur de déconnexion", "Impossible de se déconnecter: " + e.getMessage());
        }
    }
}