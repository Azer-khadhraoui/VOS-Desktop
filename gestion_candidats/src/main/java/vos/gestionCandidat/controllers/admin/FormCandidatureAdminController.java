package vos.gestionCandidat.controllers.admin;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import vos.gestionCandidat.entities.Candidature;
import vos.gestionCandidat.services.CandidatureService;

import java.io.File;
import java.net.URL;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

public class FormCandidatureAdminController implements Initializable {

    /* ===================== FXML INJECTIONS ===================== */

    @FXML
    private Label formTitle;

    // Section 1 — Infos générales
    @FXML
    private DatePicker dateCandidature;
    @FXML
    private ComboBox<String> statut;
    @FXML
    private TextField idUtilisateur;
    @FXML
    private TextField idOffre;

    // Section 2 — Expérience
    @FXML
    private ComboBox<String> niveauExperience;
    @FXML
    private Spinner<Integer> anneesExperience;
    @FXML
    private ComboBox<String> domaineExperience;
    @FXML
    private ComboBox<String> dernierPoste;

    // Section 3 — Documents
    @FXML
    private TextField cv;
    @FXML
    private TextField lettreMotivation;

    // Section 4 — Message
    @FXML
    private TextArea messageCandidat;

    // Feedback
    @FXML
    private Label errorLabel;
    @FXML
    private Button btnSauvegarder;

    /* ===================== STATE ===================== */

    private final CandidatureService service = new CandidatureService();
    private Candidature candidatureEnEdition = null; // null = mode ajout
    private ListeCandidaturesAdminController parentController;

    /* ===================== INITIALISE ===================== */

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Le Spinner est déjà configuré via FXML (min=0, max=50, initialValue=0)
        // On s'assure juste qu'il accepte la saisie manuelle
        SpinnerValueFactory<Integer> svf = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 50, 0);
        anneesExperience.setValueFactory(svf);
        anneesExperience.setEditable(true);
    }

    /**
     * Appelé par le contrôleur parent avant affichage.
     *
     * @param candidature null → mode création, sinon → mode édition
     * @param parent      référence pour rafraîchir la liste après sauvegarde
     */
    public void initData(Candidature candidature, ListeCandidaturesAdminController parent) {
        this.parentController = parent;
        this.candidatureEnEdition = candidature;

        if (candidature == null) {
            // Mode ajout
            formTitle.setText("Nouvelle Candidature");
            btnSauvegarder.setText("💾 Enregistrer");
            dateCandidature.setValue(java.time.LocalDate.now());
            statut.setValue("En attente");
        } else {
            // Mode édition — pré-remplissage
            formTitle.setText("Modifier la Candidature #" + candidature.getIdCandidature());
            btnSauvegarder.setText("💾 Mettre à jour");

            if (candidature.getDateCandidature() != null) {
                dateCandidature.setValue(
                        new java.sql.Date(candidature.getDateCandidature().getTime()).toLocalDate());
            }
            statut.setValue(candidature.getStatut());
            idUtilisateur.setText(String.valueOf(candidature.getIdUtilisateur()));
            idOffre.setText(String.valueOf(candidature.getIdOffre()));
            niveauExperience.setValue(candidature.getNiveauExperience());
            anneesExperience.getValueFactory().setValue(candidature.getAnneesExperience());
            domaineExperience.setValue(candidature.getDomaineExperience());
            dernierPoste.setValue(candidature.getDernierPoste());
            cv.setText(candidature.getCv());
            lettreMotivation.setText(candidature.getLettreMotivation());
            messageCandidat.setText(candidature.getMessageCandidat());
        }
    }

    /* ===================== ACTIONS FXML ===================== */

    @FXML
    private void sauvegarder(ActionEvent event) {
        if (!validerFormulaire())
            return;

        Candidature c = construireCandidature();

        if (candidatureEnEdition == null) {
            service.ajouter(c);
            afficherSucces("Candidature ajoutée avec succès !");
        } else {
            c.setIdCandidature(candidatureEnEdition.getIdCandidature());
            service.modifier(c);
            afficherSucces("Candidature modifiée avec succès !");
        }

        if (parentController != null)
            parentController.rafraichir();
        fermerFenetre();
    }

    @FXML
    private void annuler(ActionEvent event) {
        fermerFenetre();
    }

    @FXML
    private void parcourirCv(ActionEvent event) {
        File f = ouvrirSelecteurFichier("Sélectionner le CV");
        if (f != null)
            cv.setText(f.getAbsolutePath());
    }

    @FXML
    private void parcourirLettre(ActionEvent event) {
        File f = ouvrirSelecteurFichier("Sélectionner la lettre de motivation");
        if (f != null)
            lettreMotivation.setText(f.getAbsolutePath());
    }

    /* ===================== VALIDATION ===================== */

    private boolean validerFormulaire() {
        errorLabel.setText("");

        if (dateCandidature.getValue() == null) {
            afficherErreur("La date de candidature est obligatoire.");
            return false;
        }
        if (statut.getValue() == null || statut.getValue().isBlank()) {
            afficherErreur("Le statut est obligatoire.");
            return false;
        }
        if (!estEntierValide(idUtilisateur.getText())) {
            afficherErreur("L'ID Candidat doit être un nombre entier valide.");
            return false;
        }
        if (!estEntierValide(idOffre.getText())) {
            afficherErreur("L'ID Offre doit être un nombre entier valide.");
            return false;
        }
        // Ajouter après la validation du niveauExperience
        if (domaineExperience.getValue() == null || domaineExperience.getValue().isBlank()) {
            afficherErreur("Le domaine d'expérience est obligatoire.");
            return false;
        }
        return true;
    }

    private boolean estEntierValide(String txt) {
        if (txt == null || txt.isBlank())
            return false;
        try {
            Integer.parseInt(txt.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    private Candidature construireCandidature() {
        Candidature c = new Candidature();
        Date date = Date.from(dateCandidature.getValue()
                .atStartOfDay(ZoneId.systemDefault()).toInstant());

        c.setDateCandidature(date);
        c.setStatut(statut.getValue());
        c.setIdUtilisateur(Integer.parseInt(idUtilisateur.getText().trim()));
        c.setIdOffre(Integer.parseInt(idOffre.getText().trim()));
        c.setNiveauExperience(niveauExperience.getValue());
        c.setAnneesExperience(anneesExperience.getValue() != null ? anneesExperience.getValue() : 0);
        c.setDomaineExperience(domaineExperience.getValue());
        c.setDernierPoste(dernierPoste.getValue());
        c.setCv(cv.getText());
        c.setLettreMotivation(lettreMotivation.getText());
        c.setMessageCandidat(messageCandidat.getText());

        return c;
    }


    private File ouvrirSelecteurFichier(String titre) {
        FileChooser fc = new FileChooser();
        fc.setTitle(titre);
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF / Documents", "*.pdf", "*.doc", "*.docx"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*"));
        Stage stage = (Stage) cv.getScene().getWindow();
        return fc.showOpenDialog(stage);
    }

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