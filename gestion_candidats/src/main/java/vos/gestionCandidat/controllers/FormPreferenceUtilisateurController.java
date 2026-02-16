package vos.gestionCandidat.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import vos.gestionCandidat.entities.Candidature;
import vos.gestionCandidat.entities.PreferenceCandidature;
import vos.gestionCandidat.services.CandidatureService;
import vos.gestionCandidat.services.PreferenceCandidatureService;

import java.net.URL;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

public class FormPreferenceUtilisateurController implements Initializable {

    /* ===================== FXML INJECTIONS ===================== */

    @FXML private Label formTitle;
    @FXML private Label candidatureLabel;

    // Section 1 — Poste
    @FXML private ComboBox<String> typePosteSouhaite;
    @FXML private ComboBox<String> modeTravail;
    @FXML private ComboBox<String> typeContratSouhaite;

    // Section 2 — Disponibilité
    @FXML private ComboBox<String> disponibilite;
    @FXML private DatePicker dateDisponibilite;

    // Section 3 — Mobilité
    @FXML private ComboBox<String> mobiliteGeographique;
    @FXML private ComboBox<String> pretDeplacement;

    // Section 4 — Salaire
    @FXML private TextField pretentionSalariale;

    // Feedback
    @FXML private Label errorLabel;
    @FXML private Button btnSauvegarder;

    /* ===================== STATE ===================== */

    private final PreferenceCandidatureService service = new PreferenceCandidatureService();
    private Candidature candidatureActuelle;
    private PreferenceCandidature preferenceEnEdition = null;
    private ListeCandidaturesUtilisateurController parentController;

    /* ===================== INITIALISE ===================== */

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Les ComboBox sont initialisées via FXML
    }

    /**
     * Appelé par le contrôleur parent
     * @param candidature la candidature pour laquelle ajouter/modifier la préférence
     * @param preference null = ajout, objet = édition
     * @param parent référence pour rafraîchir après sauvegarde
     */
    public void initData(Candidature candidature, PreferenceCandidature preference,
                         ListeCandidaturesUtilisateurController parent) {
        this.candidatureActuelle = candidature;
        this.parentController = parent;
        this.preferenceEnEdition = preference;

        candidatureLabel.setText("Candidature #" + candidature.getIdCandidature()
                + " - Offre #" + candidature.getIdOffre());

        if (preference == null) {
            // Mode ajout
            formTitle.setText("Ajouter mes préférences");
            btnSauvegarder.setText("🚀 Enregistrer mes préférences");
            dateDisponibilite.setValue(java.time.LocalDate.now());
        } else {
            // Mode édition
            formTitle.setText("Modifier mes préférences");
            btnSauvegarder.setText("💾 Mettre à jour mes préférences");

            typePosteSouhaite.setValue(preference.getTypePosteSouhaite());
            modeTravail.setValue(preference.getModeTravail());
            typeContratSouhaite.setValue(preference.getTypeContratSouhaite());
            disponibilite.setValue(preference.getDisponibilite());

            if (preference.getDateDisponibilite() != null) {
                dateDisponibilite.setValue(
                        new java.sql.Date(preference.getDateDisponibilite().getTime()).toLocalDate()
                );
            }

            mobiliteGeographique.setValue(preference.getMobiliteGeographique());
            pretDeplacement.setValue(preference.getPretDeplacement());

            if (preference.getPretentionSalariale() > 0) {
                pretentionSalariale.setText(String.valueOf(preference.getPretentionSalariale()));
            }
        }
    }

    /* ===================== ACTIONS FXML ===================== */

    @FXML
    private void sauvegarder(ActionEvent event) {
        if (!validerFormulaire()) return;

        PreferenceCandidature p = construirePreference();

        if (preferenceEnEdition == null) {
            service.ajouter(p);
            afficherSucces("Préférences ajoutées avec succès !");
        } else {
            p.setIdPreference(preferenceEnEdition.getIdPreference());
            service.modifier(p);
            afficherSucces("Préférences modifiées avec succès !");
        }

        if (parentController != null) parentController.rafraichir();
        fermerFenetre();
    }

    @FXML
    private void annuler(ActionEvent event) {
        fermerFenetre();
    }

    /* ===================== VALIDATION ===================== */

    private boolean validerFormulaire() {
        errorLabel.setText("");

        if (typePosteSouhaite.getValue() == null || typePosteSouhaite.getValue().isBlank()) {
            afficherErreur("Le type de poste souhaité est obligatoire.");
            return false;
        }
        if (modeTravail.getValue() == null || modeTravail.getValue().isBlank()) {
            afficherErreur("Le mode de travail est obligatoire.");
            return false;
        }
        if (disponibilite.getValue() == null || disponibilite.getValue().isBlank()) {
            afficherErreur("La disponibilité est obligatoire.");
            return false;
        }
        if (dateDisponibilite.getValue() == null) {
            afficherErreur("La date de disponibilité est obligatoire.");
            return false;
        }
        if (!estDoubleValide(pretentionSalariale.getText())) {
            afficherErreur("La prétention salariale doit être un nombre valide.");
            return false;
        }
        return true;
    }

    private boolean estDoubleValide(String txt) {
        if (txt == null || txt.isBlank()) return false;
        try { Double.parseDouble(txt.trim()); return true; }
        catch (NumberFormatException e) { return false; }
    }

    /* ===================== CONSTRUCTION ENTITÉ ===================== */

    private PreferenceCandidature construirePreference() {
        PreferenceCandidature p = new PreferenceCandidature();
        Date date = Date.from(dateDisponibilite.getValue()
                .atStartOfDay(ZoneId.systemDefault()).toInstant());

        p.setTypePosteSouhaite(typePosteSouhaite.getValue());
        p.setModeTravail(modeTravail.getValue());
        p.setDisponibilite(disponibilite.getValue());
        p.setMobiliteGeographique(mobiliteGeographique.getValue());
        p.setPretDeplacement(pretDeplacement.getValue());
        p.setTypeContratSouhaite(typeContratSouhaite.getValue());
        p.setPretentionSalariale(Double.parseDouble(pretentionSalariale.getText().trim()));
        p.setDateDisponibilite(date);
        p.setIdCandidature(candidatureActuelle.getIdCandidature());

        return p;
    }

    /* ===================== UTILITAIRES ===================== */

    private void afficherErreur(String msg) {
        errorLabel.setText("⚠️ " + msg);
        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 13px;");
    }

    private void afficherSucces(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) btnSauvegarder.getScene().getWindow();
        stage.close();
    }
}