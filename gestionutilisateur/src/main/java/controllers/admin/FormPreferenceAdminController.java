package controllers.admin;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

import entities.Candidature;
import entities.PreferenceCandidature;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import services.candidat.PreferenceCandidatureService;

public class FormPreferenceAdminController implements Initializable {


    @FXML private Label candidatureLabel;
    @FXML private Label noPreferenceLabel;
    @FXML private ScrollPane contentPane;

    @FXML private Label typePosteLabel;
    @FXML private Label modeTravailLabel;
    @FXML private Label typeContratLabel;
    @FXML private Label disponibiliteLabel;
    @FXML private Label dateDispoLabel;
    @FXML private Label mobiliteLabel;
    @FXML private Label depLabel;
    @FXML private Label salaireLabel;

    @FXML private Button btnFermer;


    private final PreferenceCandidatureService service = new PreferenceCandidatureService();
    private Candidature candidatureActuelle;
    private PreferenceCandidature preferenceActuelle;
    private ListeCandidaturesAdminController parentController;

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd MMM yyyy");

    /* ===================== INITIALISE ===================== */

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Rien à initialiser de particulier
    }

    /**
     * Appelé par le contrôleur parent
     * @param candidature la candidature dont on affiche les préférences
     * @param parent référence du contrôleur parent
     */
    public void initData(Candidature candidature, ListeCandidaturesAdminController parent) {
        this.candidatureActuelle = candidature;
        this.parentController = parent;

        candidatureLabel.setText("Candidature #" + candidature.getIdCandidature()
                + " — Offre #" + candidature.getIdOffre());

        // Charger la préférence
        preferenceActuelle = service.getByIdUtilisateur(candidature.getIdCandidature());

        if (preferenceActuelle == null) {
            afficherAucunePreference();
        } else {
            afficherPreference();
        }
    }

    /* ===================== AFFICHAGE ===================== */

    private void afficherAucunePreference() {
        contentPane.setVisible(false);
        contentPane.setManaged(false);
        noPreferenceLabel.setVisible(true);
        noPreferenceLabel.setManaged(true);
        noPreferenceLabel.setText("❌ Aucune préférence enregistrée pour cette candidature");
        noPreferenceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8; -fx-padding: 40;");
    }

    private void afficherPreference() {
        contentPane.setVisible(true);
        contentPane.setManaged(true);
        noPreferenceLabel.setVisible(false);
        noPreferenceLabel.setManaged(false);

        typePosteLabel.setText(nvl(preferenceActuelle.getTypePosteSouhaite(), "—"));
        modeTravailLabel.setText(nvl(preferenceActuelle.getModeTravail(), "—"));
        typeContratLabel.setText(nvl(preferenceActuelle.getTypeContratSouhaite(), "—"));
        disponibiliteLabel.setText(nvl(preferenceActuelle.getDisponibilite(), "—"));
        dateDispoLabel.setText(preferenceActuelle.getDateDisponibilite() != null
                ? SDF.format(preferenceActuelle.getDateDisponibilite()) : "—");
        mobiliteLabel.setText(nvl(preferenceActuelle.getMobiliteGeographique(), "—"));
        depLabel.setText(nvl(preferenceActuelle.getPretDeplacement(), "—"));
        salaireLabel.setText(preferenceActuelle.getPretentionSalariale() > 0
                ? preferenceActuelle.getPretentionSalariale() + " €/an" : "—");
    }

    /* ===================== ACTIONS ===================== */

    @FXML
    private void fermer(ActionEvent event) {
        Stage stage = (Stage) btnFermer.getScene().getWindow();
        stage.close();
    }

    /* ===================== UTILITAIRES ===================== */

    private String nvl(String val, String defaut) {
        return (val == null || val.isBlank()) ? defaut : val;
    }
}