package vos.gestionCandidat.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import vos.gestionCandidat.entities.Candidature;
import vos.gestionCandidat.services.CandidatureService;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DetailCandidatureUtilisateurController implements Initializable {


    @FXML private VBox        miniCardsList;
    @FXML private ScrollPane  listScroll;

    @FXML private VBox   detailPanel;

    @FXML private Label  badgeStatut;
    @FXML private Label  titreCandidature;
    @FXML private Label  offreTag;
    @FXML private Label  dateLabel;

    @FXML private Label  lblNiveauExp;
    @FXML private Label  lblAnneesExp;
    @FXML private Label  lblDomaine;
    @FXML private Label  lblDernierPoste;

    @FXML private HBox   cvCard;
    @FXML private Label  lblCv;
    @FXML private HBox   lettreCard;
    @FXML private Label  lblLettre;

    @FXML private TextArea lblMessage;

    @FXML private VBox sidebar;
    @FXML private Label navCandidaturesText;
    @FXML private Label navOffresText;
    @FXML private Label navForumText;
    @FXML private Label navProfilText;


    private final CandidatureService service = new CandidatureService();
    private ListeCandidaturesUtilisateurController parentController;
    private Candidature candidatureCourante;
    private int idUtilisateurCourant = 1;

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

        this.parentController     = parent;
        this.candidatureCourante  = candidature;

        if (candidature != null) {
            idUtilisateurCourant = candidature.getIdUtilisateur();
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

    @FXML
    private void goOffres(ActionEvent event) {
        fermerFenetre();
    }

    /* ===================== UTILITAIRES ===================== */

    // ✅ APRÈS — reconstruit le chemin absolu depuis la racine du projet
    private void ouvrirFichier(String cheminRelatif) {
        try {
            // Reconstruire le chemin absolu : racine_projet/src/main/resources/uploads/...
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

    @FXML
    private void onSidebarEntered() {
        expandSidebar();
    }

    @FXML
    private void onSidebarExited() {
        collapseSidebar();
    }

    private void expandSidebar() {
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                        javafx.util.Duration.millis(300),
                        new javafx.animation.KeyValue(sidebar.prefWidthProperty(), 240)
                )
        );
        timeline.play();
        fadeInLabels();
    }

    private void collapseSidebar() {
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                        javafx.util.Duration.millis(300),
                        new javafx.animation.KeyValue(sidebar.prefWidthProperty(), 60)
                )
        );
        timeline.play();
        fadeOutLabels();
    }

    private void fadeInLabels() {
        java.util.List<Label> labels = java.util.Arrays.asList(
                navCandidaturesText, navOffresText, navForumText, navProfilText
        );
        for (Label label : labels) {
            javafx.animation.Timeline fade = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(
                            javafx.util.Duration.millis(200),
                            new javafx.animation.KeyValue(label.opacityProperty(), 1.0),
                            new javafx.animation.KeyValue(label.maxWidthProperty(), 150)
                    )
            );
            fade.play();
        }
    }

    private void fadeOutLabels() {
        java.util.List<Label> labels = java.util.Arrays.asList(
                navCandidaturesText, navOffresText, navForumText, navProfilText
        );
        for (Label label : labels) {
            javafx.animation.Timeline fade = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(
                            javafx.util.Duration.millis(200),
                            new javafx.animation.KeyValue(label.opacityProperty(), 0.0),
                            new javafx.animation.KeyValue(label.maxWidthProperty(), 0)
                    )
            );
            fade.play();
        }
    }
}