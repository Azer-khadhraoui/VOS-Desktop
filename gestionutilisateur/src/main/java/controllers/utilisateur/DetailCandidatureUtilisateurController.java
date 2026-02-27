package controllers.utilisateur;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import entities.Candidature;
import entities.Utilisateur;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.candidat.CandidatureService;
import services.candidat.PdfService;  // ✅ AJOUT
import utilis.UserSession;

public class DetailCandidatureUtilisateurController implements Initializable {

    @FXML
    private VBox miniCardsList;
    @FXML
    private ScrollPane listScroll;

    @FXML
    private VBox detailPanel;

    @FXML
    private Label badgeStatut;
    @FXML
    private Label titreCandidature;
    @FXML
    private Label offreTag;
    @FXML
    private Label dateLabel;

    @FXML
    private Label lblNiveauExp;
    @FXML
    private Label lblAnneesExp;
    @FXML
    private Label lblDomaine;
    @FXML
    private Label lblDernierPoste;

    @FXML
    private HBox cvCard;
    @FXML
    private Label lblCv;
    @FXML
    private HBox lettreCard;
    @FXML
    private Label lblLettre;

    @FXML
    private TextArea lblMessage;

   


    private final CandidatureService service = new CandidatureService();
    private final PdfService pdfService = new PdfService();  
    private ListeCandidaturesUtilisateurController parentController;
    private Candidature candidatureCourante;
    private int idUtilisateurCourant;

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd MMM yyyy");


    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    /**
     * Appelé par ListeCandidaturesUtilisateurController après le chargement du FXML.
     * @param candidature  candidature à afficher en détail
     * @param parent       pour rafraîchir la liste parente
     */
    public void initData(Candidature candidature,
                         ListeCandidaturesUtilisateurController parent) {

        this.parentController = parent;
        this.candidatureCourante = candidature;

        Utilisateur userConnecte = UserSession.getInstance().getCurrentUser();
        if (userConnecte != null) {
            idUtilisateurCourant = userConnecte.getId_utilisateur();
        } else if (candidature != null) {
            idUtilisateurCourant = candidature.getIdUtilisateur();
        } else {
            idUtilisateurCourant = 1; // Fallback
        }

        chargerMiniListe();
        afficherDetail(candidature);
    }


    private void chargerMiniListe() {
        miniCardsList.getChildren().clear();

        List<Candidature> liste = service.getAll().stream()
                .filter(c -> c.getIdUtilisateur() == idUtilisateurCourant)
                .collect(Collectors.toList());

        for (Candidature c : liste) {
            miniCardsList.getChildren().add(creerMiniCard(c));
        }
    }

    private Node creerMiniCard(Candidature c) {
        boolean estSelectionne = candidatureCourante != null
                && c.getIdCandidature() == candidatureCourante.getIdCandidature();

        VBox card = new VBox(6);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setCursor(javafx.scene.Cursor.HAND);

        if (estSelectionne) {
            card.setStyle("-fx-background-color: linear-gradient(to bottom, #eef2ff 0%, #e0e7ff 100%); "
                    + "-fx-background-radius: 14; -fx-border-color: #6366f1; "
                    + "-fx-border-width: 2; -fx-border-radius: 14; "
                    + "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.2), 12, 0, 0, 3);");
        } else {
            card.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 14; "
                    + "-fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 14; -fx-cursor: hand;");
        }

        HBox headerRow = new HBox(8);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label titre = new Label(nvl(c.getDernierPoste(), "Candidature #" + c.getIdCandidature()));
        titre.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");
        HBox.setHgrow(titre, Priority.ALWAYS);
        titre.setMaxWidth(Double.MAX_VALUE);

        Label badgeMini = new Label(nvl(c.getStatut(), "—"));
        badgeMini.setStyle("-fx-background-radius: 8; -fx-padding: 3 10; "
                + "-fx-font-size: 10px; -fx-font-weight: 700; "
                + getBadgeStyle(c.getStatut()));

        headerRow.getChildren().addAll(titre, badgeMini);

        Label meta = new Label("📁 " + nvl(c.getDomaineExperience(), "—")
                + "  ·  Offre #" + c.getIdOffre());
        meta.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

        card.getChildren().addAll(headerRow, meta);

        card.setOnMouseClicked(e -> {
            candidatureCourante = c;
            chargerMiniListe();   // re-render pour mettre à jour la sélection
            afficherDetail(c);
        });

        return card;
    }


    private void afficherDetail(Candidature c) {
        if (c == null) return;

        badgeStatut.setText(nvl(c.getStatut(), "En attente"));
        badgeStatut.setStyle(badgeStatut.getStyle().replace(
                "-fx-background-color: #fef3c7; -fx-text-fill: #d97706;", "")
                + getBadgeStyleDetail(c.getStatut()));

        titreCandidature.setText("Candidature #" + c.getIdCandidature()
                + (c.getDernierPoste() != null && !c.getDernierPoste().isBlank()
                ? " – " + c.getDernierPoste() : ""));

        offreTag.setText("Offre #" + c.getIdOffre());
        dateLabel.setText("📅 " + (c.getDateCandidature() != null
                ? SDF.format(c.getDateCandidature()) : "—"));

        lblNiveauExp.setText(nvl(c.getNiveauExperience(), "—"));
        lblAnneesExp.setText(c.getAnneesExperience() + " an(s)");
        lblDomaine.setText(nvl(c.getDomaineExperience(), "—"));
        lblDernierPoste.setText(nvl(c.getDernierPoste(), "—"));

        if (c.getCv() != null && !c.getCv().isBlank()) {
            lblCv.setText(extractFileName(c.getCv()));
            cvCard.setOnMouseClicked(e -> ouvrirFichier(c.getCv()));
        } else {
            lblCv.setText("Non fourni");
            cvCard.setStyle(cvCard.getStyle() + "-fx-opacity: 0.5;");
        }

        if (c.getLettreMotivation() != null && !c.getLettreMotivation().isBlank()) {
            lblLettre.setText(extractFileName(c.getLettreMotivation()));
            lettreCard.setOnMouseClicked(e -> ouvrirFichier(c.getLettreMotivation()));
        } else {
            lblLettre.setText("Non fournie");
            lettreCard.setStyle(lettreCard.getStyle() + "-fx-opacity: 0.5;");
        }

        lblMessage.setText(nvl(c.getMessageCandidat(), "Aucun message saisi."));
    }


    @FXML
    private void backToList(ActionEvent event) {
        fermerFenetre();
    }

    @FXML
    private void modifierCandidature(ActionEvent event) {
        if (candidatureCourante == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fxml/utilisateur/FormCandidatureUtilisateur.fxml"));
            Parent root = loader.load();

            FormCandidatureUtilisateurController ctrl = loader.getController();
            ctrl.initData(candidatureCourante, parentController, idUtilisateurCourant);

            Stage stage = new Stage();
            stage.setTitle("Modifier la candidature #" + candidatureCourante.getIdCandidature());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            Candidature updated = service.getById(candidatureCourante.getIdCandidature());
            if (updated != null) {
                candidatureCourante = updated;
                chargerMiniListe();
                afficherDetail(updated);
            }

            if (parentController != null) parentController.rafraichir();

        } catch (IOException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }

    @FXML
    private void supprimerCandidature(ActionEvent event) {
        if (candidatureCourante == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Supprimer la candidature #" + candidatureCourante.getIdCandidature() + " ?");
        confirm.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            service.supprimer(candidatureCourante.getIdCandidature());
            if (parentController != null) parentController.rafraichir();
            fermerFenetre();
        }
    }

    /**
     * ✅ NOUVELLE MÉTHODE : Générer le PDF de la candidature
     */
    @FXML
    private void genererPDF(ActionEvent event) {
        if (candidatureCourante == null) return;

        try {
            // ÉTAPE 1 : Ouvrir FileChooser pour choisir l'emplacement
            FileChooser fileChooser = new FileChooser();
            
            // Configurer le FileChooser
            fileChooser.setTitle("Sauvegarder la candidature en PDF");
            fileChooser.setInitialFileName("Candidature_" + candidatureCourante.getIdCandidature() + ".pdf");
            
            // Filtrer pour afficher uniquement les fichiers PDF
            FileChooser.ExtensionFilter pdfFilter = 
                new FileChooser.ExtensionFilter("Fichiers PDF (*.pdf)", "*.pdf");
            fileChooser.getExtensionFilters().add(pdfFilter);
            
            // Dossier initial : Documents
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Documents"));
            
            // ÉTAPE 2 : Afficher le dialog et récupérer le fichier
            Stage stage = (Stage) detailPanel.getScene().getWindow();
            File selectedFile = fileChooser.showSaveDialog(stage);
            
            // Si l'utilisateur a annulé
            if (selectedFile == null) {
                return;
            }
            
            // ÉTAPE 3 : Générer le PDF
            pdfService.generateCandidaturePdf(candidatureCourante, selectedFile);
            
            // ÉTAPE 4 : Afficher un message de succès
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("✅ Succès");
            successAlert.setHeaderText("PDF généré avec succès");
            successAlert.setContentText("Le fichier a été sauvegardé à :\n" + selectedFile.getAbsolutePath());
            successAlert.showAndWait();
            
        } catch (Exception e) {
            // Afficher un message d'erreur
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("❌ Erreur");
            errorAlert.setHeaderText("Erreur lors de la génération du PDF");
            errorAlert.setContentText(e.getMessage());
            errorAlert.showAndWait();
            
            e.printStackTrace();
        }
    }

    @FXML
    private void goOffres(ActionEvent event) {
        fermerFenetre();
    }

    /* ===================== UTILITAIRES ===================== */

    private void ouvrirFichier(String cheminRelatif) {
        try {
            String cheminAbsolu = System.getProperty("user.dir")
                    + "/src/main/resources/"
                    + cheminRelatif;

            java.io.File fichier = new java.io.File(cheminAbsolu);

            if (!fichier.exists()) {
                afficherAlerte(Alert.AlertType.ERROR, "Fichier introuvable",
                        "Le fichier n'existe pas :\n" + cheminAbsolu);
                return;
            }

            java.awt.Desktop.getDesktop().open(fichier);

        } catch (Exception e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir le fichier : " + e.getMessage());
        }
    }

    private String getBadgeStyle(String statut) {
        if (statut == null) return "-fx-background-color: #FEF3C7; -fx-text-fill: #D97706;";
        return switch (statut.toLowerCase()) {
            case "acceptée", "acceptee" ->
                    "-fx-background-color: #D1FAE5; -fx-text-fill: #059669;";
            case "refusée", "refusee" ->
                    "-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626;";
            case "en cours" ->
                    "-fx-background-color: #DBEAFE; -fx-text-fill: #2563EB;";
            default ->
                    "-fx-background-color: #FEF3C7; -fx-text-fill: #D97706;";
        };
    }

    private String getBadgeStyleDetail(String statut) {
        if (statut == null) return "-fx-background-color: #fef3c7; -fx-text-fill: #d97706; -fx-background-radius: 20; -fx-padding: 6 18; -fx-font-size: 13px; -fx-font-weight: 700;";
        return switch (statut.toLowerCase()) {
            case "acceptée", "acceptee" ->
                    "-fx-background-color: #d1fae5; -fx-text-fill: #059669; -fx-background-radius: 20; -fx-padding: 6 18; -fx-font-size: 13px; -fx-font-weight: 700;";
            case "refusée", "refusee" ->
                    "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-background-radius: 20; -fx-padding: 6 18; -fx-font-size: 13px; -fx-font-weight: 700;";
            case "en cours" ->
                    "-fx-background-color: #dbeafe; -fx-text-fill: #2563eb; -fx-background-radius: 20; -fx-padding: 6 18; -fx-font-size: 13px; -fx-font-weight: 700;";
            default ->
                    "-fx-background-color: #fef3c7; -fx-text-fill: #d97706; -fx-background-radius: 20; -fx-padding: 6 18; -fx-font-size: 13px; -fx-font-weight: 700;";
        };
    }

    private String nvl(String s, String def) {
        return (s != null && !s.isBlank()) ? s : def;
    }

    private String extractFileName(String path) {
        if (path == null || path.isBlank()) return "—";
        return new java.io.File(path).getName();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) detailPanel.getScene().getWindow();
        stage.close();
    }

    private void afficherAlerte(Alert.AlertType type, String titre, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /* ===================== SIDEBAR ANIMATIONS ===================== */



   
}