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
import javafx.stage.Stage;
import javafx.util.Callback;
import services.EntretienService;
import services.EvaluationEntretienService;

import java.sql.Date;
import java.sql.Time;
import java.util.Optional;

public class MainController {

    private final EntretienService entretienService = new EntretienService();
    private final EvaluationEntretienService evaluationService = new EvaluationEntretienService();

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
        colStatutEntretien.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatutEntretien()));
        colLieuEntretien.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getLieu()));
        colTypeTest.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTypeTest()));

        colActions.setCellFactory(new Callback<TableColumn<Entretien, Void>, TableCell<Entretien, Void>>() {
            @Override
            public TableCell<Entretien, Void> call(TableColumn<Entretien, Void> param) {
                return new TableCell<Entretien, Void>() {
                    private final Button btnEdit = new Button("✎");
                    private final Button btnDelete = new Button("╳");
                    private final HBox pane = new HBox(10, btnEdit, btnDelete);

                    {
                        btnEdit.getStyleClass().add("btn-action-icon-edit");
                        btnDelete.getStyleClass().add("btn-action-icon-delete");
                        pane.setAlignment(Pos.CENTER);

                        btnEdit.setOnAction(event -> {
                            Entretien entretien = getTableView().getItems().get(getIndex());
                            modifierEntretienFromRow(entretien);
                        });

                        btnDelete.setOnAction(event -> {
                            Entretien entretien = getTableView().getItems().get(getIndex());
                            supprimerEntretienFromRow(entretien);
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

    private void setupEvaluationTable() {
        colIdEvaluation.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getIdEvaluation()).asObject());
        colScoreTest.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getScoreTest()));
        colNoteEntretien.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getNoteEntretien()).asObject());
        colCommentaire.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCommentaire()));
        colDecision.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDecision()));
        colIdEntretienEval.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getIdEntretien()).asObject());

        // Actions avec 3 boutons: Voir, Modifier, Supprimer
        colActionsEval.setCellFactory(new Callback<TableColumn<EvaluationEntretien, Void>, TableCell<EvaluationEntretien, Void>>() {
            @Override
            public TableCell<EvaluationEntretien, Void> call(TableColumn<EvaluationEntretien, Void> param) {
                return new TableCell<EvaluationEntretien, Void>() {
                    private final Button btnView = new Button("👁");
                    private final Button btnEdit = new Button("✎");
                    private final Button btnDelete = new Button("╳");
                    private final HBox pane = new HBox(8, btnView, btnEdit, btnDelete);

                    {
                        btnView.getStyleClass().add("btn-action-icon-view");
                        btnEdit.getStyleClass().add("btn-action-icon-edit");
                        btnDelete.getStyleClass().add("btn-action-icon-delete");
                        pane.setAlignment(Pos.CENTER);

                        btnView.setOnAction(event -> {
                            EvaluationEntretien evaluation = getTableView().getItems().get(getIndex());
                            voirEvaluationDetails(evaluation);
                        });

                        btnEdit.setOnAction(event -> {
                            EvaluationEntretien evaluation = getTableView().getItems().get(getIndex());
                            modifierEvaluationFromRow(evaluation);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EvaluationForm.fxml"));
            Parent root = loader.load();

            EvaluationFormController controller = loader.getController();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EvaluationForm.fxml"));
            Parent root = loader.load();

            EvaluationFormController controller = loader.getController();
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
}