package controllers;

import entities.Entretien;
import entities.EvaluationEntretien;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.EntretienService;
import services.EvaluationEntretienService;

public class EvaluationFormController {

    // ========== CHAMPS ==========
    @FXML private TextField scoreField;
    @FXML private TextField noteField;
    @FXML private TextArea commentaireField;
    @FXML private ComboBox<String> decisionField;
    @FXML private TextField idEntretienField;

    // ========== LABELS D'ERREUR ==========
    @FXML private Label scoreError;
    @FXML private Label noteError;
    @FXML private Label commentaireError;
    @FXML private Label decisionError;
    @FXML private Label idEntretienError;

    // ========== BOUTON ENREGISTRER ==========
    @FXML private Button btnEnregistrer;

    private EvaluationEntretienService evaluationService = new EvaluationEntretienService();
    private EntretienService entretienService = new EntretienService();
    private EvaluationEntretien currentEvaluation;
    private boolean saved = false;

    @FXML
    public void initialize() {
        decisionField.setItems(FXCollections.observableArrayList(
                "Accepté", "Refusé", "En attente", "À revoir"
        ));

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

        // ========== VALIDATION EN TEMPS RÉEL ==========
        scoreField.textProperty().addListener((obs, oldVal, newVal) -> { validateScore(); updateSaveButtonState(); });
        noteField.textProperty().addListener((obs, oldVal, newVal) -> { validateNote(); updateSaveButtonState(); });
        commentaireField.textProperty().addListener((obs, oldVal, newVal) -> { validateCommentaire(); updateSaveButtonState(); });
        decisionField.valueProperty().addListener((obs, oldVal, newVal) -> { validateDecision(); updateSaveButtonState(); });
        idEntretienField.textProperty().addListener((obs, oldVal, newVal) -> { validateIdEntretien(); updateSaveButtonState(); });
    }

    /**
     * Méthode pour pré-remplir le formulaire avec un ID entretien
     * Appelée depuis le bouton "Évaluer" dans le tableau
     */
    public void setEntretienToEvaluate(int idEntretien) {
        // Pré-remplir l'ID entretien
        idEntretienField.setText(String.valueOf(idEntretien));

        // Désactiver le champ pour éviter les modifications
        idEntretienField.setDisable(true);
        idEntretienField.setStyle("-fx-opacity: 0.7;");

        // Valeurs par défaut pour les autres champs
        scoreField.setText("");
        noteField.setText("");
        commentaireField.setText("");
        decisionField.setValue("En attente");

        updateSaveButtonState();
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

        updateSaveButtonState();
    }

    // ========== VALIDATION ==========

    private boolean validateScore() {
        String scoreText = scoreField.getText().trim();
        if (scoreText.isEmpty()) {
            showError(scoreField, scoreError, "Ce champ est obligatoire");
            return false;
        }
        try {
            double score = Double.parseDouble(scoreText);
            if (score < 0 || score > 100) {
                showError(scoreField, scoreError, "Le score doit être entre 0 et 100");
                return false;
            }
            showSuccess(scoreField, scoreError, "Format valide");
            return true;
        } catch (NumberFormatException e) {
            showError(scoreField, scoreError, "Format invalide (nombre requis)");
            return false;
        }
    }

    private boolean validateNote() {
        String noteText = noteField.getText().trim();
        if (noteText.isEmpty()) {
            showError(noteField, noteError, "Ce champ est obligatoire");
            return false;
        }
        try {
            int note = Integer.parseInt(noteText);
            if (note < 1 || note > 5) {
                showError(noteField, noteError, "La note doit être entre 1 et 5");
                return false;
            }
            showSuccess(noteField, noteError, "Format valide");
            return true;
        } catch (NumberFormatException e) {
            showError(noteField, noteError, "Format invalide (nombre requis)");
            return false;
        }
    }

    private boolean validateCommentaire() {
        String commentaire = commentaireField.getText().trim();
        if (commentaire.isEmpty()) {
            showError(commentaireField, commentaireError, "Ce champ est obligatoire");
            return false;
        } else if (commentaire.length() < 10) {
            showError(commentaireField, commentaireError, "Minimum 10 caractères");
            return false;
        } else if (commentaire.length() > 500) {
            showError(commentaireField, commentaireError, "Maximum 500 caractères");
            return false;
        }
        showSuccess(commentaireField, commentaireError, "Format valide");
        return true;
    }

    private boolean validateDecision() {
        if (decisionField.getValue() == null || decisionField.getValue().trim().isEmpty()) {
            showError(decisionField, decisionError, "Ce champ est obligatoire");
            return false;
        }
        showSuccess(decisionField, decisionError, "Sélection valide");
        return true;
    }

    private boolean validateIdEntretien() {
        String idText = idEntretienField.getText().trim();
        if (idText.isEmpty()) {
            showError(idEntretienField, idEntretienError, "Ce champ est obligatoire");
            return false;
        }
        try {
            int id = Integer.parseInt(idText);
            if (id <= 0) {
                showError(idEntretienField, idEntretienError, "Valeur invalide (doit être > 0)");
                return false;
            }
            showSuccess(idEntretienField, idEntretienError, "Format valide");
            return true;
        } catch (NumberFormatException e) {
            showError(idEntretienField, idEntretienError, "Format invalide (nombre requis)");
            return false;
        }
    }

    // ========== MÉTHODES UTILITAIRES ==========

    private void showError(Control field, Label errorLabel, String message) {
        errorLabel.setText("⚠ " + message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        errorLabel.getStyleClass().setAll("validation-badge-error");
    }

    private void showSuccess(Control field, Label errorLabel, String message) {
        errorLabel.setText("✓ " + message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        errorLabel.getStyleClass().setAll("validation-badge-success");
    }

    // ========== ÉTAT DU BOUTON ==========

    private void updateSaveButtonState() {
        if (btnEnregistrer == null) return;

        boolean allValid = isFormValid();
        btnEnregistrer.setDisable(!allValid);

        if (allValid) {
            btnEnregistrer.getStyleClass().remove("btn-save-disabled");
        } else {
            if (!btnEnregistrer.getStyleClass().contains("btn-save-disabled")) {
                btnEnregistrer.getStyleClass().add("btn-save-disabled");
            }
        }
    }

    private boolean isFormValid() {
        return validateScore() &&
                validateNote() &&
                validateCommentaire() &&
                validateDecision() &&
                validateIdEntretien();
    }

    // ========== SAUVEGARDE ==========

    @FXML
    private void handleSave() {
        try {
            // ========== VALIDATION FINALE ==========
            if (!isFormValid()) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation",
                        "❌ Veuillez corriger les erreurs dans le formulaire avant d'enregistrer.");
                return;
            }

            // Récupération des valeurs validées
            double score = Double.parseDouble(scoreField.getText().trim());
            int note = Integer.parseInt(noteField.getText().trim());
            String commentaire = commentaireField.getText().trim();
            String decision = decisionField.getValue();
            int idEntretien = Integer.parseInt(idEntretienField.getText().trim());

            // ========== VALIDATION MÉTIER ==========

            // VALIDATION: Vérifier que l'entretien existe
            Entretien entretien = entretienService.getEntretienById(idEntretien);
            if (entretien == null) {
                showAlert(Alert.AlertType.ERROR, "Entretien introuvable",
                        "❌ L'entretien avec l'ID " + idEntretien + " n'existe pas!\n\n" +
                                "Veuillez vérifier l'ID dans la table des entretiens.");
                idEntretienField.requestFocus();
                return;
            }

            // VALIDATION: Vérifier que l'entretien est terminé
            if (!entretien.getStatutEntretien().equalsIgnoreCase("Terminé")) {
                showAlert(Alert.AlertType.ERROR, "Statut incorrect",
                        "❌ Impossible d'évaluer cet entretien!\n\n" +
                                "L'entretien #" + idEntretien + " n'est pas encore terminé.\n" +
                                "Statut actuel: " + entretien.getStatutEntretien() + "\n\n" +
                                "Veuillez attendre que l'entretien soit marqué comme 'Terminé'.");
                return;
            }

            // ========== SAUVEGARDE ==========
            if (currentEvaluation == null) {
                // Ajout
                EvaluationEntretien newEval = new EvaluationEntretien(score, note, commentaire, decision, idEntretien);
                evaluationService.addEvaluation(newEval);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "✅ Évaluation ajoutée avec succès!");
            } else {
                // Modification
                currentEvaluation.setScoreTest(score);
                currentEvaluation.setNoteEntretien(note);
                currentEvaluation.setCommentaire(commentaire);
                currentEvaluation.setDecision(decision);
                currentEvaluation.setIdEntretien(idEntretien);
                evaluationService.updateEvaluation(currentEvaluation);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "✅ Évaluation modifiée avec succès!");
            }

            saved = true;
            closeWindow();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "❌ Erreur lors de l'enregistrement:\n\n" + e.getMessage());
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