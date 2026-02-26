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

public class EntretienFormController {

    @FXML private DatePicker dateField;
    @FXML private TextField heureField;
    @FXML private ComboBox<String> typeField;
    @FXML private ComboBox<String> statutField;
    @FXML private TextField lieuField;
    @FXML private TextField typeTestField;
    @FXML private TextField idCandidatureField;
    @FXML private TextField idUtilisateurField;

    private EntretienService entretienService = new EntretienService();
    private Entretien currentEntretien;
    private boolean saved = false;

    @FXML
    public void initialize() {
        // Populate ComboBoxes
        typeField.setItems(FXCollections.observableArrayList("RH", "TECHNIQUE"));
        statutField.setItems(FXCollections.observableArrayList("Planifié", "Confirmé", "Terminé", "Annulé"));

        // FIX - Forcer l'affichage du texte dans les ComboBox
        typeField.setButtonCell(new ListCell<String>() {
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

        statutField.setButtonCell(new ListCell<String>() {
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

    public void setEntretien(Entretien entretien) {
        this.currentEntretien = entretien;

        if (entretien != null) {
            // Mode édition
            dateField.setValue(entretien.getDateEntretien().toLocalDate());
            heureField.setText(entretien.getHeureEntretien().toString());
            typeField.setValue(entretien.getTypeEntretien());
            statutField.setValue(entretien.getStatutEntretien());
            lieuField.setText(entretien.getLieu());
            typeTestField.setText(entretien.getTypeTest());
            idCandidatureField.setText(String.valueOf(entretien.getIdCandidature()));
            idUtilisateurField.setText(String.valueOf(entretien.getIdUtilisateur()));
        } else {
            // Mode ajout - valeurs par défaut
            dateField.setValue(LocalDate.now());
            heureField.setText("10:00:00");
            typeField.setValue("RH");
            statutField.setValue("Planifié");
        }
    }

    @FXML
    private void handleSave() {
        try {
            // Validation
            if (dateField.getValue() == null || heureField.getText().isEmpty() ||
                    typeField.getValue() == null || statutField.getValue() == null ||
                    lieuField.getText().isEmpty() || typeTestField.getText().isEmpty() ||
                    idCandidatureField.getText().isEmpty() || idUtilisateurField.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs");
                return;
            }

            Date date = Date.valueOf(dateField.getValue());
            Time heure = Time.valueOf(heureField.getText());
            String type = typeField.getValue();
            String statut = statutField.getValue();
            String lieu = lieuField.getText();
            String typeTest = typeTestField.getText();
            int idCandidature = Integer.parseInt(idCandidatureField.getText());
            int idUtilisateur = Integer.parseInt(idUtilisateurField.getText());

            if (currentEntretien == null) {
                // Ajout
                Entretien newEntretien = new Entretien(date, heure, type, statut, lieu, typeTest, idCandidature, idUtilisateur);
                entretienService.addEntretien(newEntretien);
            } else {
                // Modification
                currentEntretien.setDateEntretien(date);
                currentEntretien.setHeureEntretien(heure);
                currentEntretien.setTypeEntretien(type);
                currentEntretien.setStatutEntretien(statut);
                currentEntretien.setLieu(lieu);
                currentEntretien.setTypeTest(typeTest);
                currentEntretien.setIdCandidature(idCandidature);
                currentEntretien.setIdUtilisateur(idUtilisateur);
                entretienService.updateEntretien(currentEntretien);
            }

            saved = true;
            closeWindow();

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
        Stage stage = (Stage) dateField.getScene().getWindow();
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