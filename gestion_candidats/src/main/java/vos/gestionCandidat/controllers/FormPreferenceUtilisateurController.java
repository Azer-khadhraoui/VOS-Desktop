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

    @FXML
    private Label formTitle;
    @FXML
    private Label candidatureLabel;

    // Section 1 — Poste
    @FXML
    private ComboBox<String> typePosteSouhaite;
    @FXML
    private ComboBox<String> modeTravail;
    @FXML
    private ComboBox<String> typeContratSouhaite;

    // Section 2 — Disponibilité
    @FXML
    private ComboBox<String> disponibilite;
    @FXML
    private DatePicker dateDisponibilite;

    // Section 3 — Mobilité
    @FXML
    private ComboBox<String> mobiliteGeographique;
    @FXML
    private ComboBox<String> pretDeplacement;

    // Section 4 — Salaire
    @FXML
    private TextField pretentionSalariale;

    // Feedback
    @FXML
    private Label errorLabel;
    @FXML
    private Button btnSauvegarder;

    // Labels d'erreur par champ
    @FXML
    private Label errTypePoste;
    @FXML
    private Label errModeTravail;
    @FXML
    private Label errDisponibilite;
    @FXML
    private Label errDateDispo;
    @FXML
    private Label errSalaire;

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
     * 
     * @param candidature la candidature pour laquelle ajouter/modifier la
     *                    préférence
     * @param preference  null = ajout, objet = édition
     * @param parent      référence pour rafraîchir après sauvegarde
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
                        new java.sql.Date(preference.getDateDisponibilite().getTime()).toLocalDate());
            }

            mobiliteGeographique.setValue(preference.getMobiliteGeographique());
            pretDeplacement.setValue(preference.getPretDeplacement());

            if (preference.getPretentionSalariale() > 0) {
                pretentionSalariale.setText(String.valueOf(preference.getPretentionSalariale()));
            }
        }
        // Listeners pour effacer les erreurs en temps réel
        typePosteSouhaite.valueProperty().addListener((obs, old, val) -> {
            if (val != null) {
                setErreurChamp(errTypePoste, null);
                setBordureErreur(typePosteSouhaite, false);
            }
        });
        modeTravail.valueProperty().addListener((obs, old, val) -> {
            if (val != null) {
                setErreurChamp(errModeTravail, null);
                setBordureErreur(modeTravail, false);
            }
        });
        disponibilite.valueProperty().addListener((obs, old, val) -> {
            if (val != null) {
                setErreurChamp(errDisponibilite, null);
                setBordureErreur(disponibilite, false);
            }
        });
        dateDisponibilite.valueProperty().addListener((obs, old, val) -> {
            if (val != null) {
                setErreurChamp(errDateDispo, null);
                setBordureErreur(dateDisponibilite, false);
            }
        });
        pretentionSalariale.textProperty().addListener((obs, old, val) -> {
            if (estDoubleValide(val)) {
                setErreurChamp(errSalaire, null);
                setBordureErreur(pretentionSalariale, false);
            }
        });
    }

    /* ===================== ACTIONS FXML ===================== */

    @FXML
    private void sauvegarder(ActionEvent event) {
        if (!validerFormulaire())
            return;

        PreferenceCandidature p = construirePreference();

        if (preferenceEnEdition == null) {
            service.ajouter(p);
            afficherSucces("Préférences ajoutées avec succès !");
        } else {
            p.setIdPreference(preferenceEnEdition.getIdPreference());
            service.modifier(p);
            afficherSucces("Préférences modifiées avec succès !");
        }

        if (parentController != null)
            parentController.rafraichir();
        fermerFenetre();
    }

    @FXML
    private void annuler(ActionEvent event) {
        fermerFenetre();
    }

    /* ===================== VALIDATION ===================== */

    private boolean validerFormulaire() {
        boolean valide = true;

        // 1. Réinitialiser
        setErreurChamp(errTypePoste, null);
        setErreurChamp(errModeTravail, null);
        setErreurChamp(errDisponibilite, null);
        setErreurChamp(errDateDispo, null);
        setErreurChamp(errSalaire, null);
        setBordureErreur(typePosteSouhaite, false);
        setBordureErreur(modeTravail, false);
        setBordureErreur(disponibilite, false);
        setBordureErreur(dateDisponibilite, false);
        setBordureErreur(pretentionSalariale, false);

        // 2. Valider chaque champ
        if (typePosteSouhaite.getValue() == null || typePosteSouhaite.getValue().isBlank()) {
            setErreurChamp(errTypePoste, "Le type de poste est obligatoire.");
            setBordureErreur(typePosteSouhaite, true);
            valide = false;
        }

        if (modeTravail.getValue() == null || modeTravail.getValue().isBlank()) {
            setErreurChamp(errModeTravail, "Le mode de travail est obligatoire.");
            setBordureErreur(modeTravail, true);
            valide = false;
        }

        if (disponibilite.getValue() == null || disponibilite.getValue().isBlank()) {
            setErreurChamp(errDisponibilite, "La disponibilité est obligatoire.");
            setBordureErreur(disponibilite, true);
            valide = false;
        }

        if (dateDisponibilite.getValue() == null) {
            setErreurChamp(errDateDispo, "La date de disponibilité est obligatoire.");
            setBordureErreur(dateDisponibilite, true);
            valide = false;
        }

        if (!estDoubleValide(pretentionSalariale.getText())) {
            setErreurChamp(errSalaire, "Veuillez saisir un montant valide (ex: 45000).");
            setBordureErreur(pretentionSalariale, true);
            valide = false;
        }

        // 3. Alerte globale si tout est vide
        if (!valide) {
            boolean toutVide = typePosteSouhaite.getValue() == null
                    && modeTravail.getValue() == null
                    && disponibilite.getValue() == null
                    && dateDisponibilite.getValue() == null
                    && (pretentionSalariale.getText() == null
                            || pretentionSalariale.getText().isBlank());

            if (toutVide) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Formulaire incomplet");
                alert.setHeaderText("⚠️ Aucun champ rempli !");
                alert.setContentText(
                        "Veuillez remplir au moins les champs obligatoires (*) avant d'enregistrer vos préférences.");
                alert.showAndWait();
            }
        }

        return valide;
    }

    private boolean estDoubleValide(String txt) {
        if (txt == null || txt.isBlank())
            return false;
        try {
            Double.parseDouble(txt.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
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

    private void setErreurChamp(Label label, String msg) {
        if (msg == null) {
            label.setText("");
            label.setVisible(false);
            label.setManaged(false);
        } else {
            label.setText("⚠ " + msg);
            label.setVisible(true);
            label.setManaged(true);
        }
    }

    private void setBordureErreur(Control ctrl, boolean erreur) {
        if (erreur) {
            ctrl.setStyle(ctrl.getStyle() + "; -fx-border-color: #ef4444; -fx-border-width: 1.5;");
        } else {
            ctrl.setStyle(ctrl.getStyle()
                    .replace("; -fx-border-color: #ef4444; -fx-border-width: 1.5;", ""));
        }
    }
}