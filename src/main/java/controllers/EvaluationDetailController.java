package controllers;

import entities.EvaluationEntretien;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class EvaluationDetailController {

    @FXML private Label lblEvalTitle;
    @FXML private Label lblEvalSubtitle;
    @FXML private Label lblDecisionBadge;
    @FXML private Label lblScore;
    @FXML private Label lblNote;
    @FXML private Label lblCommentaire;

    public void setEvaluation(EvaluationEntretien evaluation) {
        // Titre et sous-titre
        lblEvalTitle.setText("Évaluation #" + evaluation.getIdEvaluation());
        lblEvalSubtitle.setText("Entretien #" + evaluation.getIdEntretien());

        // Badge décision
        lblDecisionBadge.setText(evaluation.getDecision());
        lblDecisionBadge.getStyleClass().add("decision-badge");

        switch (evaluation.getDecision().toLowerCase()) {
            case "accepté":
                lblDecisionBadge.getStyleClass().add("decision-accepte");
                break;
            case "refusé":
                lblDecisionBadge.getStyleClass().add("decision-refuse");
                break;
            case "en attente":
                lblDecisionBadge.getStyleClass().add("decision-attente");
                break;
            default:
                lblDecisionBadge.getStyleClass().add("decision-default");
        }

        // Scores
        lblScore.setText(String.format("%.1f", evaluation.getScoreTest()));
        lblNote.setText(evaluation.getNoteEntretien() + "/5");

        // Commentaire
        lblCommentaire.setText(evaluation.getCommentaire());
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) lblEvalTitle.getScene().getWindow();
        stage.close();
    }
}