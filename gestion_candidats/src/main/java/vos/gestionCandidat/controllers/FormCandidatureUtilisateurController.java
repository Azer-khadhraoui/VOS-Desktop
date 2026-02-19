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
import java.nio.file.*;
import java.nio.file.StandardCopyOption;


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
    private File fichierCvSelectionne = null;
    private File fichierLettreSelectionnee = null;

    /* ===================== INITIALISE ===================== */

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        SpinnerValueFactory<Integer> svf = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 50, 0);
        anneesExperience.setValueFactory(svf);
        anneesExperience.setEditable(true);
        setupRealtimeValidation();
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
        if (f != null) {
            fichierCvSelectionne = f;
            cv.setText(f.getName()); // Affiche juste le nom dans l'UI
        }
    }

    @FXML
    private void parcourirLettre(ActionEvent event) {
        File f = ouvrirSelecteurFichier("Sélectionner la lettre de motivation");
        if (f != null) {
            fichierLettreSelectionnee = f;
            lettreMotivation.setText(f.getName()); // Affiche juste le nom dans l'UI
        }
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
        c.setMessageCandidat(messageCandidat.getText() != null ? messageCandidat.getText().trim() : "");
        if (fichierCvSelectionne != null) {
            String cheminCv = copierFichierVersProjet(fichierCvSelectionne, "cv");
            c.setCv(cheminCv); // Chemin relatif ex: "uploads/cv/1234567_monCV.pdf"
        } else {
            // En mode édition, garder l'ancien chemin si pas de nouveau fichier
            c.setCv(candidatureEnEdition != null ? candidatureEnEdition.getCv() : "");
        }

        if (fichierLettreSelectionnee != null) {
            String cheminLettre = copierFichierVersProjet(fichierLettreSelectionnee, "lettres");
            c.setLettreMotivation(cheminLettre);
        } else {
            c.setLettreMotivation(candidatureEnEdition != null ? candidatureEnEdition.getLettreMotivation() : "");
        }

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



    private void cacherErreur() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
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
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("🎉 Candidature soumise !");
        alert.setHeaderText(null);
        alert.setContentText("Souhaitez-vous ajouter vos préférences maintenant ?");

        ButtonType btnOui = new ButtonType("⭐ Oui, ajouter mes préférences");
        ButtonType btnNon = new ButtonType("Plus tard", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btnOui, btnNon);

        // ── Style sombre cohérent avec le formulaire ──
        DialogPane dp = alert.getDialogPane();
        dp.setStyle(
                "-fx-background-color: #1a1a2e;" +
                        "-fx-border-color: #3d3d5c;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-border-radius: 16;" +
                        "-fx-background-radius: 16;"
        );

        // Titre personnalisé
        Label titre = new Label("🎉  Candidature soumise avec succès !");
        titre.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-font-weight: 800;" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 0 0 8 0;"
        );

        Label sousTitre = new Label("Complétez votre dossier en ajoutant vos préférences de poste,\ndisponibilité et prétention salariale.");
        sousTitre.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-text-fill: #94a3b8;" +
                        "-fx-padding: 0 0 6 0;"
        );

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10, titre, sousTitre);
        content.setStyle("-fx-padding: 10 6 0 6;");
        dp.setContent(content);
        dp.setHeader(null);
        dp.setGraphic(null);

        // Style des boutons
        dp.lookupButton(btnOui).setStyle(
                "-fx-background-color: linear-gradient(to right, #a855f7 0%, #ec4899 100%);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: 700;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 12 24;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;"
        );
        dp.lookupButton(btnNon).setStyle(
                "-fx-background-color: #2d2d48;" +
                        "-fx-text-fill: #94a3b8;" +
                        "-fx-font-weight: 600;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 12 24;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-border-color: #3d3d5c;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-border-radius: 10;"
        );

        // ButtonBar fond sombre
        dp.lookup(".button-bar").setStyle("-fx-background-color: #1a1a2e; -fx-padding: 16 20 20 20;");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == btnOui) {
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



// Dans la méthode initialize() ou initData(), ajoutez ces listeners pour la validation en temps réel:

    private void setupRealtimeValidation() {
        // Validation en temps réel pour le niveau d'expérience
        niveauExperience.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                setErreurChamp(errNiveauExp, null);
                niveauExperience.setStyle(niveauExperience.getStyle().replace("-fx-border-color: #ef4444;", "-fx-border-color: #10b981;"));
            }
        });

        // Validation en temps réel pour le domaine d'expérience
        domaineExperience.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                setErreurChamp(errDomaineExp, null);
                domaineExperience.setStyle(domaineExperience.getStyle().replace("-fx-border-color: #ef4444;", "-fx-border-color: #10b981;"));
            }
        });

        // Validation en temps réel pour le CV
        cv.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                setErreurChamp(errCv, null);
                cv.setStyle(cv.getStyle().replace("-fx-border-color: #ef4444;", "-fx-border-color: #10b981;"));
            }
        });
    }

    // Méthode pour appliquer le style d'erreur moderne
    private void setBordureErreur(Control ctrl, boolean erreur) {
        String baseStyle = ctrl.getStyle();

        // Retirer les anciennes bordures
        baseStyle = baseStyle.replace("-fx-border-color: #ef4444; -fx-border-width: 2;", "");
        baseStyle = baseStyle.replace("-fx-border-color: #10b981; -fx-border-width: 2;", "");

        if (erreur) {
            // Bordure rouge pour erreur
            ctrl.setStyle(baseStyle + " -fx-border-color: #ef4444; -fx-border-width: 2;");
        } else {
            // Bordure verte pour validé
            ctrl.setStyle(baseStyle + " -fx-border-color: #10b981; -fx-border-width: 2;");
        }
    }

    // Méthode améliorée pour afficher les erreurs avec le nouveau style
    private void setErreurChamp(Label label, String msg) {
        if (msg == null) {
            label.setText("");
            label.setVisible(false);
            label.setManaged(false);
            label.getStyleClass().remove("error-label");
            label.getStyleClass().add("valid");
        } else {
            label.setText("❌ " + msg);
            label.setVisible(true);
            label.setManaged(true);
            label.getStyleClass().remove("valid");
            label.getStyleClass().add("error-label");
        }
    }

    // Méthode pour afficher un succès moderne
    private void afficherSucces(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("✅ Succès");
        alert.setHeaderText(null);
        alert.setContentText(msg);

        // Style moderne pour l'alerte
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: #2d2d48; " +
                        "-fx-border-color: #10b981; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12;"
        );

        // Style pour les labels
        dialogPane.lookup(".content").setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        // Style pour les boutons
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #10b981, #059669); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 12 30; " +
                        "-fx-background-radius: 10; " +
                        "-fx-cursor: hand;"
        );

        alert.showAndWait();
    }

    // Méthode pour afficher une erreur moderne
    private void afficherErreur(String msg) {
        errorLabel.setText("⚠️ " + msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);

        // Animation de shake pour attirer l'attention
        animerShake(errorLabel);
    }

    // Animation shake pour les erreurs
    private void animerShake(javafx.scene.Node node) {
        javafx.animation.TranslateTransition shake = new javafx.animation.TranslateTransition(
                javafx.util.Duration.millis(50), node
        );
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }
    private String copierFichierVersProjet(File fichierSource, String sousDossier) {
        try {
            // Chemin absolu vers le dossier uploads dans les resources
            String basePath = System.getProperty("user.dir") + "/src/main/resources/uploads/" + sousDossier + "/";

            // Créer le dossier s'il n'existe pas
            Files.createDirectories(Paths.get(basePath));

            // Nom unique pour éviter les conflits : timestamp + nom original
            String nomFichier = System.currentTimeMillis() + "_" + fichierSource.getName();
            Path destination = Paths.get(basePath + nomFichier);

            // Copier le fichier
            Files.copy(fichierSource.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

            // Retourner le chemin RELATIF (ce qui sera enregistré en BDD)
            return "uploads/" + sousDossier + "/" + nomFichier;

        } catch (IOException e) {
            System.out.println("Erreur copie fichier : " + e.getMessage());
            return null;
        }
    }

}