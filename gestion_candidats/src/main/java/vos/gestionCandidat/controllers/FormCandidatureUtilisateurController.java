package vos.gestionCandidat.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import vos.gestionCandidat.entities.Candidature;
import vos.gestionCandidat.entities.PreferenceCandidature;
import vos.gestionCandidat.services.CandidatureService;
import vos.gestionCandidat.services.PreferenceCandidatureService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import vos.gestionCandidat.entities.PreferenceCandidature;
import vos.gestionCandidat.services.PreferenceCandidatureService;

public class FormCandidatureUtilisateurController implements Initializable {

    /* ===================== FXML INJECTIONS ===================== */

    @FXML
    private Label formTitle;
    @FXML
    private Label offreLabel;

    // Section expérience
    @FXML
    private ComboBox<String> niveauExperience;
    @FXML
    private Spinner<Integer> anneesExperience;
    @FXML
    private ComboBox<String> domaineExperience;
    @FXML
    private ComboBox<String> dernierPoste;

    // Section documents (champ caché + label d'affichage)
    @FXML
    private TextField cv;
    @FXML
    private TextField lettreMotivation;

    // Message
    @FXML
    private TextArea messageCandidat;

    // Feedback
    @FXML
    private Label errorLabel;
    @FXML
    private Button btnSoumettre;

    // Labels d'erreur par champ
    @FXML
    private Label errNiveauExp;
    @FXML
    private Label errDomaineExp;
    @FXML
    private Label errCv;

    /* ===================== STATE ===================== */

    private final CandidatureService service = new CandidatureService();
    private Candidature candidatureEnEdition = null;
    private ListeCandidaturesUtilisateurController parentController;
    private int idUtilisateurCourant = 3;

    /* ===================== INITIALISE ===================== */

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        SpinnerValueFactory<Integer> svf = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 50, 0);
        anneesExperience.setValueFactory(svf);
        anneesExperience.setEditable(true);
    }

    /**
     * Initialisation depuis le contrôleur parent.
     *
     * @param candidature   null = ajout, objet = édition
     * @param parent        référence pour rafraîchir la liste après sauvegarde
     * @param idUtilisateur ID de l'utilisateur connecté
     */
    public void initData(Candidature candidature,
            ListeCandidaturesUtilisateurController parent,
            int idUtilisateur) {

        this.parentController = parent;
        this.idUtilisateurCourant = idUtilisateur;
        this.candidatureEnEdition = candidature;
        // Afficher l'offre par défaut
        if (idOffreTemporaire == 0) {
            idOffreTemporaire = 1; // Défaut
        }
        offreLabel.setText("Offre #" + idOffreTemporaire);

        if (candidature == null) {
            // Mode ajout
            formTitle.setText("Postuler à une offre");
            btnSoumettre.setText("🚀 Soumettre ma candidature");
        } else {
            // Mode édition
            formTitle.setText("Modifier ma candidature");
            btnSoumettre.setText("💾 Enregistrer les modifications");
            offreLabel.setText("Offre #" + candidature.getIdOffre());

            niveauExperience.setValue(candidature.getNiveauExperience());
            anneesExperience.getValueFactory().setValue(candidature.getAnneesExperience());
            domaineExperience.setValue(candidature.getDomaineExperience());
            dernierPoste.setValue(candidature.getDernierPoste());

            // Documents
            cv.setText(candidature.getCv());
            lettreMotivation.setText(candidature.getLettreMotivation());

            messageCandidat.setText(candidature.getMessageCandidat());
        }

        // Listeners pour effacer les erreurs en temps réel
        niveauExperience.valueProperty().addListener((obs, old, val) -> {
            if (val != null) {
                setErreurChamp(errNiveauExp, null);
                setBordureErreur(niveauExperience, false);
            }
        });

        domaineExperience.valueProperty().addListener((obs, old, val) -> {
            if (val != null) {
                setErreurChamp(errDomaineExp, null);
                setBordureErreur(domaineExperience, false);
            }
        });
    }

    /**
     * Surcharge permettant de pré-sélectionner l'offre visée.
     */
    public void initDataAvecOffre(int idOffre, ListeCandidaturesUtilisateurController parent, int idUtilisateur) {
        initData(null, parent, idUtilisateur);
        offreLabel.setText("Offre #" + idOffre);
        // Stocker temporairement l'idOffre pour la construction de l'entité
        this.idOffreTemporaire = idOffre;
    }

    private int idOffreTemporaire = 1;

    /* ===================== ACTIONS FXML ===================== */

    @FXML
    private void retour(ActionEvent event) {
        fermerFenetre();
    }

    @FXML
    private void goOffres(ActionEvent event) {
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
        boolean valide = true;

        // 1. Réinitialiser tous les labels d'erreur et bordures
        setErreurChamp(errNiveauExp, null);
        setErreurChamp(errDomaineExp, null);
        setErreurChamp(errCv, null);
        cacherErreur();
        setBordureErreur(niveauExperience, false);
        setBordureErreur(domaineExperience, false);
        setBordureErreur(cv, false);

        // 2. Valider chaque champ obligatoire
        if (niveauExperience.getValue() == null) {
            setErreurChamp(errNiveauExp, "Le niveau d'expérience est obligatoire.");
            setBordureErreur(niveauExperience, true);
            valide = false;
        }

        if (domaineExperience.getValue() == null || domaineExperience.getValue().isBlank()) {
            setErreurChamp(errDomaineExp, "Le domaine d'expérience est obligatoire.");
            setBordureErreur(domaineExperience, true);
            valide = false;
        }

        if (cv.getText() == null || cv.getText().isBlank()) {
            setErreurChamp(errCv, "Veuillez joindre votre CV.");
            valide = false;
        }

        // 3. Alerte globale si tout est vide
        if (!valide) {
            boolean toutVide = niveauExperience.getValue() == null
                    && domaineExperience.getValue() == null
                    && (cv.getText() == null || cv.getText().isBlank());

            if (toutVide) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Formulaire incomplet");
                alert.setHeaderText("⚠️ Aucun champ rempli !");
                alert.setContentText(
                        "Veuillez remplir au moins les champs obligatoires (*) avant de soumettre votre candidature.");
                alert.showAndWait();
            }
        }

        return valide;
    }
    /* ===================== CONSTRUCTION ENTITÉ ===================== */

    private Candidature construireCandidature() {
        Candidature c = new Candidature();

        c.setDateCandidature(new Date());
        c.setStatut("En attente");
        c.setIdUtilisateur(idUtilisateurCourant);

        // idOffre : depuis l'édition ou la sélection temporaire
        if (candidatureEnEdition != null) {
            c.setIdOffre(candidatureEnEdition.getIdOffre());
        } else {
            c.setIdOffre(idOffreTemporaire);
        }

        c.setNiveauExperience(niveauExperience.getValue());
        c.setAnneesExperience(anneesExperience.getValue() != null ? anneesExperience.getValue() : 0);
        c.setDomaineExperience(domaineExperience.getValue() != null ? domaineExperience.getValue().trim() : "");
        c.setDernierPoste(dernierPoste.getValue() != null ? dernierPoste.getValue().trim() : "");
        c.setCv(cv.getText());
        c.setLettreMotivation(lettreMotivation.getText());
        c.setMessageCandidat(messageCandidat.getText() != null ? messageCandidat.getText().trim() : "");

        return c;
    }

    /* ===================== UTILITAIRES ===================== */

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
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void cacherErreur() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void afficherSucces(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) btnSoumettre.getScene().getWindow();
        stage.close();
    }

    private String extractFileName(String path) {
        if (path == null || path.isBlank())
            return "Aucun fichier";
        File f = new File(path);
        return f.getName();
    }

    @FXML
    private void soumettreForm(ActionEvent event) {
        if (!validerFormulaire())
            return;

        Candidature c = construireCandidature();
        int idCandidatureCreee = -1;

        if (candidatureEnEdition == null) {
            service.ajouter(c);
            // Récupérer l'ID de la candidature créée
            List<Candidature> candidates = service.getAll().stream()
                    .filter(cand -> cand.getIdUtilisateur() == idUtilisateurCourant
                            && cand.getIdOffre() == idOffreTemporaire
                            && cand.getStatut().equals("En attente"))
                    .collect(Collectors.toList());
            if (!candidates.isEmpty()) {
                idCandidatureCreee = candidates.get(candidates.size() - 1).getIdCandidature();
            }
            afficherSucces("Candidature soumise avec succès ! Nous reviendrons vers vous sous peu.");
        } else {
            c.setIdCandidature(candidatureEnEdition.getIdCandidature());
            idCandidatureCreee = candidatureEnEdition.getIdCandidature();
            service.modifier(c);
            afficherSucces("Candidature modifiée avec succès !");
        }

        if (parentController != null)
            parentController.rafraichir();

        // Proposer d'ajouter les préférences
        if (idCandidatureCreee > 0 && candidatureEnEdition == null) {
            proposerAjouterPreferences(idCandidatureCreee, c);
            fermerFenetre();
        } else {
            fermerFenetre();
        }
    }

    /**
     * Propose à l'utilisateur d'ajouter ses préférences après création de la
     * candidature
     */
    private void proposerAjouterPreferences(int idCandidature, Candidature candidature) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ajouter vos préférences");
        alert.setHeaderText("Candidature créée avec succès ! 🎉");
        alert.setContentText("Souhaitez-vous ajouter vos préférences maintenant ?");

        ButtonType btnOui = new ButtonType("Oui, ajouter les préférences");
        ButtonType btnNon = new ButtonType("Non, plus tard", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnOui, btnNon);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == btnOui) {
            // Récréer la candidature avec l'ID pour l'ouvrir en préférence
            Candidature cand = new Candidature();
            cand.setIdCandidature(idCandidature);
            cand.setIdOffre(candidature.getIdOffre());
            cand.setIdUtilisateur(idUtilisateurCourant);

            ouvrirPreferences(cand);
        }
    }

    /**
     * Ouvre le formulaire de préférences
     */
    private void ouvrirPreferences(Candidature candidature) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/utilisateur/FormPreference.fxml"));
            Parent root = loader.load();

            FormPreferenceUtilisateurController ctrl = loader.getController();

            PreferenceCandidatureService prefService = new PreferenceCandidatureService();
            PreferenceCandidature preference = prefService.getByIdCandidature(candidature.getIdCandidature());

            ctrl.initData(candidature, preference, parentController);

            Stage stage = new Stage();
            stage.setTitle(preference == null ? "Ajouter mes préférences" : "Modifier mes préférences");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Erreur : " + e.getMessage());
        }
    }

    /** Affiche ou cache un label d'erreur sous un champ */
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

    /** Applique ou retire la bordure rouge sur un contrôle */
    private void setBordureErreur(Control ctrl, boolean erreur) {
        if (erreur) {
            ctrl.setStyle(ctrl.getStyle() + "; -fx-border-color: #ef4444; -fx-border-width: 1.5;");
        } else {
            ctrl.setStyle(ctrl.getStyle()
                    .replace("; -fx-border-color: #ef4444; -fx-border-width: 1.5;", ""));
        }
    }
}