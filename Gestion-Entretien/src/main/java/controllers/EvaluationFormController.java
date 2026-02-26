package controllers;

import entities.EvaluationEntretien;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.EvaluationEntretienService;

public class EvaluationFormController {

    @FXML private TextField scoreField;
    @FXML private TextField noteField;
    @FXML private TextArea commentaireField;
    @FXML private ComboBox<String> decisionField;
    @FXML private TextField idEntretienField;

    private EvaluationEntretienService evaluationService = new EvaluationEntretienService();
    private EvaluationEntretien currentEvaluation;
    private boolean saved = false;

    @FXML
    public void initialize() {
        // Populate ComboBox
        decisionField.setItems(FXCollections.observableArrayList(
                "Accepté", "Refusé", "En attente", "À revoir"
        ));

        // Forcer l'affichage du texte dans les ComboBox (FIX)
        decisionField.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                }
            }
        });
    }

    public void setEvaluation(EvaluationEntretien evaluation) {
        this.currentEvaluation = evaluation;

        if (evaluation != null) {
            // Mode édition
            scoreField.setText(String.valueOf(evaluation.getScoreTest()));
            noteField.setText(String.valueOf(evaluation.getNoteEntretien()));
            commentaireField.setText(evaluation.getCommentaire());
            decisionField.setValue(evaluation.getDecision());
            idEntretienField.setText(String.valueOf(evaluation.getIdEntretien()));
        } else {
            // Mode ajout - valeurs par défaut
            scoreField.setText("");
            noteField.setText("");
            commentaireField.setText("");
            decisionField.setValue("En attente");
            idEntretienField.setText("");
        }
    }

    @FXML
    private void handleSave() {
        try {
            // Validation
            if (scoreField.getText().isEmpty() || noteField.getText().isEmpty() ||
                    commentaireField.getText().isEmpty() || decisionField.getValue() == null ||
                    idEntretienField.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs");
                return;
            }

            double score = Double.parseDouble(scoreField.getText());
            int note = Integer.parseInt(noteField.getText());
            String commentaire = commentaireField.getText();
            String decision = decisionField.getValue();
            int idEntretien = Integer.parseInt(idEntretienField.getText());

            if (currentEvaluation == null) {
                // Ajout
                EvaluationEntretien newEval = new EvaluationEntretien(score, note, commentaire, decision, idEntretien);
                evaluationService.addEvaluation(newEval);
            } else {
                // Modification
                currentEvaluation.setScoreTest(score);
                currentEvaluation.setNoteEntretien(note);
                currentEvaluation.setCommentaire(commentaire);
                currentEvaluation.setDecision(decision);
                currentEvaluation.setIdEntretien(idEntretien);
                evaluationService.updateEvaluation(currentEvaluation);
            }

            saved = true;
            closeWindow();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Score, Note et ID Entretien doivent être des nombres valides");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'enregistrement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) scoreField.getScene().getWindow();
        stage.close();
    }

    public boolean isSaved() {
        return saved;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}