package controllers;

import entities.Entretien;
import entities.EvaluationEntretien;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.util.List;

public class EvaluationsDialogController {

    @FXML private Label lblEntretienInfo;
    @FXML private Label lblEntretienDetails;
    @FXML private Label lblEvalCount;
    @FXML private VBox evaluationsContainer;

    private Entretien entretien;
    private List<EvaluationEntretien> evaluations;

    public void setData(Entretien entretien, List<EvaluationEntretien> evaluations) {
        this.entretien = entretien;
        this.evaluations = evaluations;

        // Mise à jour du header
        lblEntretienInfo.setText("Entretien #" + entretien.getIdEntretien() + " - " + entretien.getTypeEntretien());
        lblEntretienDetails.setText(String.format("📅 %s à %s • 📍 %s",
                entretien.getDateEntretien(),
                entretien.getHeureEntretien(),
                entretien.getLieu()));
        lblEvalCount.setText(evaluations.size() + (evaluations.size() > 1 ? " Évaluations" : " Évaluation"));

        // Afficher les évaluations
        displayEvaluations();
    }

    private void displayEvaluations() {
        evaluationsContainer.getChildren().clear();

        for (int i = 0; i < evaluations.size(); i++) {
            EvaluationEntretien eval = evaluations.get(i);
            VBox evalCard = createEvaluationCard(eval, i + 1);
            evaluationsContainer.getChildren().add(evalCard);

            // Ajouter séparateur sauf pour le dernier
            if (i < evaluations.size() - 1) {
                Separator separator = new Separator();
                separator.getStyleClass().add("eval-separator");
                VBox.setMargin(separator, new Insets(20, 0, 20, 0));
                evaluationsContainer.getChildren().add(separator);
            }
        }
    }

    private VBox createEvaluationCard(EvaluationEntretien eval, int index) {
        VBox card = new VBox(20);
        card.getStyleClass().add("eval-card");

        // ===== HEADER =====
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label numLabel = new Label("#" + index);
        numLabel.getStyleClass().add("eval-number");

        Label idLabel = new Label("Évaluation #" + eval.getIdEvaluation());
        idLabel.getStyleClass().add("eval-id-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Badge décision
        Label decisionBadge = new Label(eval.getDecision());
        decisionBadge.getStyleClass().add("decision-badge");
        switch (eval.getDecision().toLowerCase()) {
            case "accepté":
                decisionBadge.getStyleClass().add("decision-accepte");
                break;
            case "refusé":
                decisionBadge.getStyleClass().add("decision-refuse");
                break;
            case "en attente":
                decisionBadge.getStyleClass().add("decision-attente");
                break;
            default:
                decisionBadge.getStyleClass().add("decision-default");
        }

        header.getChildren().addAll(numLabel, idLabel, spacer, decisionBadge);

        // ===== SCORES SECTION =====
        HBox scoresSection = new HBox(40);
        scoresSection.setAlignment(Pos.CENTER_LEFT);

        // Score du Test
        VBox scoreBox = new VBox(8);
        scoreBox.setAlignment(Pos.CENTER_LEFT);

        Label scoreLabel = new Label("📊 SCORE DU TEST");
        scoreLabel.getStyleClass().add("field-label");

        Label scoreValue = new Label(String.format("%.1f", eval.getScoreTest()));
        scoreValue.getStyleClass().addAll("metric-value-large", "score-value");

        scoreBox.getChildren().addAll(scoreLabel, scoreValue);

        // Note
        VBox noteBox = new VBox(8);
        noteBox.setAlignment(Pos.CENTER_LEFT);

        Label noteLabel = new Label("⭐ NOTE");
        noteLabel.getStyleClass().add("field-label");

        Label noteValue = new Label(eval.getNoteEntretien() + "/5");
        noteValue.getStyleClass().addAll("metric-value-large", "note-value");

        noteBox.getChildren().addAll(noteLabel, noteValue);

        scoresSection.getChildren().addAll(scoreBox, noteBox);

        // ===== COMMENTAIRE SECTION =====
        VBox commentSection = new VBox(8);

        Label commentLabel = new Label("💬 COMMENTAIRE");
        commentLabel.getStyleClass().add("field-label");

        VBox commentBox = new VBox();
        commentBox.getStyleClass().add("comment-box");

        Label commentText = new Label(eval.getCommentaire());
        commentText.getStyleClass().add("comment-text");
        commentText.setWrapText(true);

        commentBox.getChildren().add(commentText);
        commentSection.getChildren().addAll(commentLabel, commentBox);

        card.getChildren().addAll(header, scoresSection, commentSection);

        return card;
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) evaluationsContainer.getScene().getWindow();
        stage.close();
    }
}