package controllers;

import entities.Entretien;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.EntretienService;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;

import javafx.scene.layout.VBox;

public class EntretienFormController {

    // ========== CHAMPS ==========
    @FXML private DatePicker dateField;
    @FXML private TextField heureField;
    @FXML private ComboBox<String> typeField;
    @FXML private ComboBox<String> statutField;
    @FXML private TextField lieuField;
    @FXML private VBox lienReunionBox;
    @FXML private TextField lienReunionField;
    @FXML private Label lienReunionError;
    @FXML private TextField typeTestField;
    @FXML private TextField idCandidatureField;
    @FXML private TextField idUtilisateurField;

    // ========== TITRE DU FORMULAIRE ==========
    @FXML private Label formTitle;

    // ========== LABELS D'ERREUR ==========
    @FXML private Label dateError;
    @FXML private Label heureError;
    @FXML private Label typeError;
    @FXML private Label statutError;
    @FXML private Label lieuError;
    @FXML private Label typeTestError;
    @FXML private Label idCandidatureError;
    @FXML private Label idUtilisateurError;

    // ========== BOUTON ENREGISTRER ==========
    @FXML private Button btnEnregistrer;

    private EntretienService entretienService = new EntretienService();
    private Entretien currentEntretien;
    private boolean saved = false;

    @FXML
    public void initialize() {
        typeField.setItems(FXCollections.observableArrayList("RH", "TECHNIQUE"));
        statutField.setItems(FXCollections.observableArrayList("Planifié", "Confirmé", "Terminé", "Annulé"));

        // ===== BLOQUER LES DATES PASSÉES =====
        dateField.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #3D1F1F; -fx-text-fill: #6B7280;");
                }
            }
        });

        typeField.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
            }
        });

        statutField.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
            }
        });

        // ========== VALIDATION EN TEMPS RÉEL ==========
        dateField.valueProperty().addListener((obs, oldVal, newVal) -> { validateDate(); updateSaveButtonState(); });
        heureField.textProperty().addListener((obs, oldVal, newVal) -> { validateHeure(); updateSaveButtonState(); });
        typeField.valueProperty().addListener((obs, oldVal, newVal) -> { validateType(); updateSaveButtonState(); });
        statutField.valueProperty().addListener((obs, oldVal, newVal) -> { validateStatut(); updateSaveButtonState(); });
        lieuField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateLieu();
            updateSaveButtonState();
            // Afficher/masquer le champ lien réunion
            boolean enLigne = newVal != null && newVal.toLowerCase().contains("en ligne");
            if (lienReunionBox != null) {
                lienReunionBox.setVisible(enLigne);
                lienReunionBox.setManaged(enLigne);
                if (!enLigne && lienReunionField != null) {
                    lienReunionField.clear();
                }
            }
        });
        typeTestField.textProperty().addListener((obs, oldVal, newVal) -> { validateTypeTest(); updateSaveButtonState(); });
        idCandidatureField.textProperty().addListener((obs, oldVal, newVal) -> { validateIdCandidature(); updateSaveButtonState(); });
        idUtilisateurField.textProperty().addListener((obs, oldVal, newVal) -> { validateIdUtilisateur(); updateSaveButtonState(); });
        lienReunionField.textProperty().addListener((obs, oldVal, newVal) -> { validateLienReunion(); updateSaveButtonState(); });
    }

    public void setEntretien(Entretien entretien) {
        this.currentEntretien = entretien;

        // ===== TITRE DYNAMIQUE =====
        if (formTitle != null) {
            if (entretien == null) {
                formTitle.setText("📋 Ajouter un Entretien");
            } else {
                formTitle.setText("✏️ Modifier l'Entretien");
            }
        }

        if (entretien != null) {
            dateField.setValue(entretien.getDateEntretien().toLocalDate());
            heureField.setText(entretien.getHeureEntretien().toString());
            typeField.setValue(entretien.getTypeEntretien());
            statutField.setValue(entretien.getStatutEntretien());
            lieuField.setText(entretien.getLieu());
            typeTestField.setText(entretien.getTypeTest());
            idCandidatureField.setText(String.valueOf(entretien.getIdCandidature()));
            idUtilisateurField.setText(String.valueOf(entretien.getIdUtilisateur()));
            // Lien réunion si entretien en ligne
            if (entretien.getLieu() != null && entretien.getLieu().toLowerCase().contains("en ligne")) {
                if (lienReunionBox != null) { lienReunionBox.setVisible(true); lienReunionBox.setManaged(true); }
                if (lienReunionField != null && entretien.getLienReunion() != null)
                    lienReunionField.setText(entretien.getLienReunion());
            }
        } else {
            dateField.setValue(LocalDate.now());
            heureField.setText("10:00:00");
            typeField.setValue("RH");
            statutField.setValue("Planifié");
        }

        updateSaveButtonState();
    }

    // ========== VALIDATION ==========

    private boolean validateDate() {
        if (dateField.getValue() == null) {
            showError(dateField, dateError, "Ce champ est obligatoire");
            return false;
        }
        if (dateField.getValue().isBefore(LocalDate.now()) && currentEntretien == null) {
            showError(dateField, dateError, "La date ne peut pas être dans le passé");
            return false;
        }
        showSuccess(dateField, dateError, "Date valide");
        return true;
    }

    private boolean validateHeure() {
        String heure = heureField.getText().trim();
        if (heure.isEmpty()) {
            showError(heureField, heureError, "Ce champ est obligatoire");
            return false;
        } else if (!heure.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$")) {
            showError(heureField, heureError, "Format invalide (HH:MM:SS)");
            return false;
        }
        showSuccess(heureField, heureError, "Format valide");
        return true;
    }

    private boolean validateType() {
        if (typeField.getValue() == null || typeField.getValue().trim().isEmpty()) {
            showError(typeField, typeError, "Ce champ est obligatoire");
            return false;
        }
        showSuccess(typeField, typeError, "Sélection valide");
        return true;
    }

    private boolean validateStatut() {
        if (statutField.getValue() == null || statutField.getValue().trim().isEmpty()) {
            showError(statutField, statutError, "Ce champ est obligatoire");
            return false;
        }
        showSuccess(statutField, statutError, "Sélection valide");
        return true;
    }

    private boolean validateLienReunion() {
        // Seulement si le champ est visible (entretien en ligne)
        if (lienReunionBox == null || !lienReunionBox.isVisible()) return true;
        String lien = lienReunionField.getText().trim();
        if (lien.isEmpty()) {
            showError(lienReunionField, lienReunionError, "Lien obligatoire pour un entretien en ligne");
            return false;
        }
        if (!lien.startsWith("http://") && !lien.startsWith("https://")) {
            showError(lienReunionField, lienReunionError, "Lien invalide (doit commencer par https://)");
            return false;
        }
        showSuccess(lienReunionField, lienReunionError, "Lien valide");
        return true;
    }

    private boolean validateLieu() {
        String lieu = lieuField.getText().trim();
        if (lieu.isEmpty()) {
            showError(lieuField, lieuError, "Ce champ est obligatoire");
            return false;
        } else if (lieu.length() < 3) {
            showError(lieuField, lieuError, "Minimum 3 caractères");
            return false;
        }
        showSuccess(lieuField, lieuError, "Format valide");
        return true;
    }

    private boolean validateTypeTest() {
        String typeTest = typeTestField.getText().trim();
        if (typeTest.isEmpty()) {
            showError(typeTestField, typeTestError, "Ce champ est obligatoire");
            return false;
        } else if (typeTest.length() < 2) {
            showError(typeTestField, typeTestError, "Minimum 2 caractères");
            return false;
        }
        showSuccess(typeTestField, typeTestError, "Format valide");
        return true;
    }

    private boolean validateIdCandidature() {
        String idText = idCandidatureField.getText().trim();
        if (idText.isEmpty()) {
            showError(idCandidatureField, idCandidatureError, "Ce champ est obligatoire");
            return false;
        }
        try {
            int id = Integer.parseInt(idText);
            if (id <= 0) {
                showError(idCandidatureField, idCandidatureError, "Valeur invalide");
                return false;
            }
            showSuccess(idCandidatureField, idCandidatureError, "Format valide");
            return true;
        } catch (NumberFormatException e) {
            showError(idCandidatureField, idCandidatureError, "Format invalide (nombre requis)");
            return false;
        }
    }

    private boolean validateIdUtilisateur() {
        String idText = idUtilisateurField.getText().trim();
        if (idText.isEmpty()) {
            showError(idUtilisateurField, idUtilisateurError, "Ce champ est obligatoire");
            return false;
        }
        try {
            int id = Integer.parseInt(idText);
            if (id <= 0) {
                showError(idUtilisateurField, idUtilisateurError, "Valeur invalide");
                return false;
            }
            showSuccess(idUtilisateurField, idUtilisateurError, "Format valide");
            return true;
        } catch (NumberFormatException e) {
            showError(idUtilisateurField, idUtilisateurError, "Format invalide (nombre requis)");
            return false;
        }
    }

    // ========== MÉTHODES UTILITAIRES (UNE SEULE DÉFINITION CHACUNE) ==========

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
        return validateDate() &&
                validateHeure() &&
                validateType() &&
                validateStatut() &&
                validateLieu() &&
                validateLienReunion() &&
                validateTypeTest() &&
                validateIdCandidature() &&
                validateIdUtilisateur();
    }

    // ========== SAUVEGARDE ==========

    @FXML
    private void handleSave() {
        try {
            Date date = Date.valueOf(dateField.getValue());
            Time heure = Time.valueOf(heureField.getText().trim());
            String type = typeField.getValue();
            String statut = statutField.getValue();
            String lieu = lieuField.getText().trim();
            String typeTest = typeTestField.getText().trim();
            int idCandidature = Integer.parseInt(idCandidatureField.getText().trim());
            int idUtilisateur = Integer.parseInt(idUtilisateurField.getText().trim());
            String lienReunion = (lienReunionBox != null && lienReunionBox.isVisible() && lienReunionField != null)
                    ? lienReunionField.getText().trim() : null;

            if (currentEntretien == null) {
                Entretien newEntretien = new Entretien(date, heure, type, statut, lieu, typeTest, idCandidature, idUtilisateur);
                newEntretien.setLienReunion(lienReunion);
                entretienService.addEntretien(newEntretien);
                showSuccessAlert("Entretien ajouté avec succès!");
            } else {
                currentEntretien.setDateEntretien(date);
                currentEntretien.setHeureEntretien(heure);
                currentEntretien.setTypeEntretien(type);
                currentEntretien.setStatutEntretien(statut);
                currentEntretien.setLieu(lieu);
                currentEntretien.setTypeTest(typeTest);
                currentEntretien.setIdCandidature(idCandidature);
                currentEntretien.setIdUtilisateur(idUtilisateur);
                currentEntretien.setLienReunion(lienReunion);
                entretienService.updateEntretien(currentEntretien);
                showSuccessAlert("Entretien modifié avec succès!");
            }

            saved = true;
            closeWindow();

        } catch (Exception e) {
            showErrorAlert("Erreur lors de l'enregistrement:\n\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) dateField.getScene().getWindow();
        stage.close();
    }

    public boolean isSaved() {
        return saved;
    }

    // ========== ALERTES ==========

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}