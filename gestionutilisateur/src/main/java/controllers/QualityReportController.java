package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.AITextGeneratorService;

import java.util.List;

public class QualityReportController {

    @FXML
    private Label lblSummary;
    @FXML
    private Label lblScore;
    @FXML
    private VBox errorsContainer;
    @FXML
    private Label lblNoErrors;

    private boolean regenerateRequested = false;

    public void setData(AITextGeneratorService.GrammarCheckResult result) {
        lblSummary.setText(String.format("%d erreur(s) détectée(s) dans votre texte.", result.errorCount));
        lblScore.setText(String.format("%.0f%%", result.qualityScore));

        // Style the score badge based on quality
        lblScore.getStyleClass().removeAll("quality-perfect", "quality-good", "quality-average", "quality-poor");
        if (result.qualityScore >= 95) {
            lblScore.getStyleClass().add("quality-perfect");
        } else if (result.qualityScore >= 80) {
            lblScore.getStyleClass().add("quality-good");
        } else if (result.qualityScore >= 60) {
            lblScore.getStyleClass().add("quality-average");
        } else {
            lblScore.getStyleClass().add("quality-poor");
        }

        displayErrors(result.errors);
    }

    private void displayErrors(List<AITextGeneratorService.ErrorDetail> errors) {
        errorsContainer.getChildren().clear();

        if (errors == null || errors.isEmpty()) {
            lblNoErrors.setVisible(true);
            lblNoErrors.setManaged(true);
            errorsContainer.getChildren().add(lblNoErrors);
            return;
        }

        for (AITextGeneratorService.ErrorDetail error : errors) {
            VBox errorCard = createErrorCard(error);
            errorsContainer.getChildren().add(errorCard);
        }
    }

    private VBox createErrorCard(AITextGeneratorService.ErrorDetail error) {
        VBox card = new VBox(10);
        card.getStyleClass().add("error-card");

        Label msgLabel = new Label("⚠️ " + error.message);
        msgLabel.getStyleClass().add("error-message");
        msgLabel.setWrapText(true);

        card.getChildren().add(msgLabel);

        if (error.suggestion != null && !error.suggestion.isEmpty()) {
            VBox suggestionBox = new VBox(5);
            suggestionBox.getStyleClass().add("suggestion-box");

            Label suggestLabel = new Label("💡 SUGGESTION ");
            suggestLabel.getStyleClass().add("suggestion-label");

            Label suggestText = new Label(error.suggestion);
            suggestText.getStyleClass().add("suggestion-text");
            suggestText.setWrapText(true);

            suggestionBox.getChildren().addAll(suggestLabel, suggestText);
            card.getChildren().add(suggestionBox);
        }

        return card;
    }

    public boolean isRegenerateRequested() {
        return regenerateRequested;
    }

    @FXML
    private void handleKeep() {
        closeDialog();
    }

    @FXML
    private void handleRegenerate() {
        regenerateRequested = true;
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) lblSummary.getScene().getWindow();
        stage.close();
    }
}
