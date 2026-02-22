package vos.gestionCandidat.controllers.utilisateur;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import vos.gestionCandidat.entities.Candidature;
import vos.gestionCandidat.entities.PreferenceCandidature;
import vos.gestionCandidat.services.CandidatureService;
import vos.gestionCandidat.services.PdfService;
import vos.gestionCandidat.services.PreferenceCandidatureService;

public class ListeCandidaturesUtilisateurController implements Initializable {

    /* ===================== FXML INJECTIONS ===================== */
    // Hero stats
    @FXML
    private Label heroSubtitle;
    @FXML
    private Label heroTotal;
    @FXML
    private Label heroEnAttente;
    @FXML
    private Label heroAcceptee;

    // Search / filter
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> filterStatut;

    // Dynamic cards container
    @FXML
    private VBox cardsContainer;
    @FXML
    private VBox emptyState;

    // Sidebar
    @FXML
    private VBox sidebar;
    @FXML
    private Label navCandidaturesText;
    @FXML
    private Label navOffresText;
    @FXML
    private Label navForumText;
    @FXML
    private Label navProfilText;
    @FXML private HBox navOffres;
    @FXML private HBox navMatchings;
    @FXML private HBox navPreferences;
    @FXML private HBox navCandidatures;
    @FXML
    private TextField searchFieldTable;

    private static final String USER_Preference
            = "/fxml/utilisateur/ListePreferencesUtilisateur.fxml";
    private static final String USER_Candidat
            = "/fxml/utilisateur/ListeCandidaturesUtilisateur.fxml";
    private static final String USER_MATCHING
            = "/fxml/utilisateur/MatchingUtilisateur.fxml";
    private static final String Offre
            = "/fxml/utilisateur/Offre.fxml";

    /* ===================== STATE ===================== */
    private final CandidatureService service = new CandidatureService();
    private final PdfService pdfService = new PdfService();  // ✅ AJOUT : Service PDF
    private List<Candidature> toutesLesCandidatures;

    // ⚠️  En production, récupérer l'ID depuis la session utilisateur courante
    private int idUtilisateurCourant = 3;

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd MMM yyyy");

    /* ===================== INITIALISE ===================== */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerFiltre();
        configurerNavigationSidebar();
        chargerDonnees();

    }

    /* ---------------------- Config filtre ---------------------- */
    private void configurerFiltre() {
        filterStatut.getItems().setAll("Tous", "En attente", "Acceptée", "Refusée", "En cours");
        filterStatut.setValue("Tous");

        searchField.textProperty().addListener((obs, o, n) -> appliquerFiltres());
        filterStatut.valueProperty().addListener((obs, o, n) -> appliquerFiltres());
    }

    /* ===================== CHARGEMENT DONNÉES ===================== */
    private void chargerDonnees() {
        // Récupère uniquement les candidatures de l'utilisateur courant
        toutesLesCandidatures = service.getAll().stream()
                .filter(c -> c.getIdUtilisateur() == idUtilisateurCourant)
                .collect(Collectors.toList());

        mettreAJourStats(toutesLesCandidatures);
        afficherCartes(toutesLesCandidatures);
    }

    private void mettreAJourStats(List<Candidature> liste) {
        long total = liste.size();
        long enAttente = liste.stream().filter(c -> "En attente".equalsIgnoreCase(c.getStatut())).count();
        long acceptee = liste.stream().filter(c -> "Acceptée".equalsIgnoreCase(c.getStatut())).count();

        heroTotal.setText(String.valueOf(total));
        heroEnAttente.setText(String.valueOf(enAttente));
        heroAcceptee.setText(String.valueOf(acceptee));
        heroSubtitle.setText("Vous avez " + total + " candidature(s) enregistrée(s)");
    }

    /* ===================== CONSTRUCTION DES CARDS ===================== */
    private void afficherCartes(List<Candidature> liste) {
        cardsContainer.getChildren().clear();

        if (liste.isEmpty()) {
            emptyState.setVisible(true);
            emptyState.setManaged(true);
            return;
        }

        emptyState.setVisible(false);
        emptyState.setManaged(false);

        for (Candidature c : liste) {
            cardsContainer.getChildren().add(creerCarte(c));
        }
    }

    /**
     * Crée une card candidature reprenant le style des job-cards de offres.fxml
     */
    private Node creerCarte(Candidature c) {
        // Conteneur principal
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20, 24, 20, 24));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; "
                + "-fx-border-color: rgba(226,232,240,0.8); -fx-border-width: 1.5; -fx-border-radius: 16; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 18, 0, 0, 4); -fx-cursor: hand;");

        // Icône statut
        Label icone = new Label(getIconeStatut(c.getStatut()));
        icone.setStyle("-fx-font-size: 26px; -fx-padding: 10; "
                + "-fx-background-color: " + getBgIconeStatut(c.getStatut()) + "; "
                + "-fx-background-radius: 14; -fx-min-width: 50; -fx-min-height: 50; "
                + "-fx-max-width: 50; -fx-max-height: 50; -fx-alignment: center;");

        // Bloc principal info
        VBox infoBox = new VBox(6);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label titre = new Label(nvl(c.getDernierPoste(), "Candidature #" + c.getIdCandidature()));
        titre.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #0f172a;");

        Label domaine = new Label("📁 " + nvl(c.getDomaineExperience(), "Domaine non précisé")
                + "  ·  " + nvl(c.getNiveauExperience(), "Niveau non précisé")
                + "  ·  " + c.getAnneesExperience() + " an(s) d'exp.");
        domaine.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        Label offre = new Label("🏢 Offre #" + c.getIdOffre()
                + "   📅 " + (c.getDateCandidature() != null ? SDF.format(c.getDateCandidature()) : "—"));
        offre.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8; -fx-font-weight: 500;");

        infoBox.getChildren().addAll(titre, domaine, offre);

        // Badge statut
        Label badge = new Label(nvl(c.getStatut(), "En attente"));
        badge.setStyle("-fx-background-radius: 20; -fx-padding: 5 14; "
                + "-fx-font-size: 12px; -fx-font-weight: 700; "
                + getBadgeStyle(c.getStatut()));

        // Boutons action
        VBox btnBox = new VBox(8);
        btnBox.setAlignment(Pos.CENTER);

        Button btnDetail = new Button("👁 Voir");
        btnDetail.setStyle("-fx-background-color: linear-gradient(to right, #6366f1 0%, #8b5cf6 100%); "
                + "-fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 8 18; "
                + "-fx-font-weight: 700; -fx-font-size: 12px; -fx-cursor: hand;");

        Button btnModifier = new Button("✏️ Modifier");
        btnModifier.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #6366f1; "
                + "-fx-background-radius: 10; -fx-padding: 8 18; "
                + "-fx-font-weight: 600; -fx-font-size: 12px; -fx-cursor: hand;");

        // ✅ NOUVEAU : Bouton PDF
        Button btnPdf = new Button("📄 PDF");
        btnPdf.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #d97706; "
                + "-fx-background-radius: 10; -fx-padding: 8 18; "
                + "-fx-font-weight: 600; -fx-font-size: 12px; -fx-cursor: hand;");

        btnDetail.setOnAction(e -> ouvrirDetail(c));
        btnModifier.setOnAction(e -> ouvrirFormulaire(c));
        btnPdf.setOnAction(e -> genererPDF(c));  // ✅ Action PDF

        btnBox.getChildren().addAll(btnDetail, btnModifier, btnPdf);  // ✅ Ajouter le bouton

        card.getChildren().addAll(icone, infoBox, badge, btnBox);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle()
                .replace("-fx-border-color: rgba(226,232,240,0.8);",
                        "-fx-border-color: rgba(99,102,241,0.4);")
                .replace("-fx-background-color: white;",
                        "-fx-background-color: linear-gradient(to right, #ffffff 0%, #f8faff 100%);")));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle()
                .replace("-fx-border-color: rgba(99,102,241,0.4);",
                        "-fx-border-color: rgba(226,232,240,0.8);")
                .replace("-fx-background-color: linear-gradient(to right, #ffffff 0%, #f8faff 100%);",
                        "-fx-background-color: white;")));

        return card;
    }

    /**
     * ✅ NOUVELLE MÉTHODE : Générer le PDF de la candidature
     */
    private void genererPDF(Candidature candidature) {
        try {
            // ÉTAPE 1 : Ouvrir FileChooser pour choisir l'emplacement
            FileChooser fileChooser = new FileChooser();
            
            // Configurer le FileChooser
            fileChooser.setTitle("Sauvegarder la candidature en PDF");
            fileChooser.setInitialFileName("Candidature_" + candidature.getIdCandidature() + ".pdf");
            
            // Filtrer pour afficher uniquement les fichiers PDF
            FileChooser.ExtensionFilter pdfFilter = 
                new FileChooser.ExtensionFilter("Fichiers PDF (*.pdf)", "*.pdf");
            fileChooser.getExtensionFilters().add(pdfFilter);
            
            // Dossier initial : Documents
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Documents"));
            
            // ÉTAPE 2 : Afficher le dialog et récupérer le fichier
            Stage stage = (Stage) cardsContainer.getScene().getWindow();
            File selectedFile = fileChooser.showSaveDialog(stage);
            
            // Si l'utilisateur a annulé
            if (selectedFile == null) {
                return;
            }
            
            // ÉTAPE 3 : Générer le PDF
            pdfService.generateCandidaturePdf(candidature, selectedFile);
            
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

    /* ===================== FILTRES ===================== */
    private void appliquerFiltres() {
        if (toutesLesCandidatures == null) {
            return;
        }

        String texte = searchField.getText().toLowerCase().trim();
        String statut = filterStatut.getValue();

        List<Candidature> filtrées = toutesLesCandidatures.stream()
                .filter(c -> {
                    boolean matchTexte = texte.isEmpty()
                            || safeContains(c.getDomaineExperience(), texte)
                            || safeContains(c.getDernierPoste(), texte)
                            || safeContains(c.getNiveauExperience(), texte)
                            || safeContains(c.getStatut(), texte);

                    boolean matchStatut = statut == null || "Tous".equals(statut)
                            || statut.equalsIgnoreCase(c.getStatut());

                    return matchTexte && matchStatut;
                })
                .collect(Collectors.toList());

        afficherCartes(filtrées);
    }

    private boolean safeContains(String val, String search) {
        return val != null && val.toLowerCase().contains(search);
    }

    @FXML
    private void nouvelleCandidature(ActionEvent event) {
        ouvrirFormulaire(null);
    }

    @FXML
    private void goOffres(ActionEvent event) {
        // Navigation vers la vue des offres — à adapter à votre router
        naviguerVers("/vos/gestionCandidat/fxml/utilisateur/offres.fxml", event);
    }

    @FXML
    private void goForum(ActionEvent event) {
        naviguerVers("/vos/gestionCandidat/fxml/utilisateur/forum.fxml", event);
    }

    @FXML
    private void goProfile(ActionEvent event) {
        naviguerVers("/vos/gestionCandidat/fxml/utilisateur/profil.fxml", event);
    }

    private void ouvrirFormulaire(Candidature candidature) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fxml/utilisateur/FormCandidatureUtilisateur.fxml"));
            Parent root = loader.load();

            FormCandidatureUtilisateurController ctrl = loader.getController();
            ctrl.initData(candidature, this, idUtilisateurCourant);

            Stage stage = new Stage();
            stage.setTitle(candidature == null ? "Nouvelle Candidature" : "Modifier ma Candidature");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }

    private void ouvrirDetail(Candidature candidature) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fxml/utilisateur/DetailCandidatureUtilisateur.fxml"));
            Parent root = loader.load();

            DetailCandidatureUtilisateurController ctrl = loader.getController();
            ctrl.initData(candidature, this);

            Stage stage = new Stage();
            stage.setTitle("Détail de la candidature #" + candidature.getIdCandidature());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le détail : " + e.getMessage());
        }
    }

    private void naviguerVers(String fxmlPath, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            System.err.println("Navigation impossible vers : " + fxmlPath + " — " + e.getMessage());
        }
    }

    /* ===================== HELPERS VISUELS ===================== */
    private String getIconeStatut(String statut) {
        if (statut == null) {
            return "📋";
        }
        return switch (statut.toLowerCase()) {
            case "acceptée", "acceptee" ->
                "✅";
            case "refusée", "refusee" ->
                "❌";
            case "en cours" ->
                "⏳";
            default ->
                "📋";  // En attente
        };
    }

    private String getBgIconeStatut(String statut) {
        if (statut == null) {
            return "#fef3c7";
        }
        return switch (statut.toLowerCase()) {
            case "acceptée", "acceptee" ->
                "#d1fae5";
            case "refusée", "refusee" ->
                "#fee2e2";
            case "en cours" ->
                "#dbeafe";
            default ->
                "#fef3c7";
        };
    }

    private String getBadgeStyle(String statut) {
        if (statut == null) {
            return "-fx-background-color: #FEF3C7; -fx-text-fill: #D97706;";
        }
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

    private String nvl(String s, String def) {
        return (s != null && !s.isBlank()) ? s : def;
    }

    private void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /* ===================== SETTER SESSION ===================== */
    public void setIdUtilisateurCourant(int id) {
        this.idUtilisateurCourant = id;
        chargerDonnees();
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

    private void ouvrirPreferences(Candidature candidature) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fxml/utilisateur/FormPreference.fxml"));
            Parent root = loader.load();

            FormPreferenceUtilisateurController ctrl = loader.getController();

            PreferenceCandidatureService prefService = new PreferenceCandidatureService();
            PreferenceCandidature preference = prefService.getByIdUtilisateur(candidature.getIdCandidature());

            ctrl.initData(candidature, preference, this);

            Stage stage = new Stage();
            stage.setTitle(preference == null ? "Ajouter mes préférences" : "Modifier mes préférences");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les préférences : " + e.getMessage());
        }
    }

    public void rafraichir() {
        chargerDonnees();
    }

    @FXML
    private void allerAuxPreferencesUtilisateur(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/utilisateur/ListePreferencesUtilisateur.fxml"));
            Parent root = loader.load();

            ListePreferencesUtilisateurController ctrl = loader.getController();
            ctrl.initData(idUtilisateurCourant, this);

            Stage stage = new Stage();
            stage.setTitle("Mes Préférences de Candidature");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root, 1300, 800));
            stage.showAndWait();

            rafraichir();

        } catch (IOException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir les préférences : " + e.getMessage());
        }
    }
    private void configurerNavigationSidebar() {
        navOffres.setOnMouseClicked(e -> navGoOffres());
        navMatchings.setOnMouseClicked(e -> navGoMatching());
        navPreferences.setOnMouseClicked(e -> navGoPreferences());
        navCandidatures.setOnMouseClicked(e -> navGoCandidatures());
    }
    private void navGoOffres() {
        chargerDansNouvelleScene(Offre,
                "Offres d'emploi", 1300, 800,
                loader -> {});
    }

    private void navGoMatching() {
        chargerDansNouvelleScene(USER_MATCHING,
                "Candidat #" + idUtilisateurCourant + " — Offres Compatibles",
                1200, 800,
                loader -> {
                    MatchingUtilisateurController ctrl = loader.getController();
                    ctrl.setIdUtilisateurConnecte(idUtilisateurCourant);
                });
    }

    private void navGoPreferences() {
        chargerDansNouvelleScene(USER_Preference,
                "Candidat #" + idUtilisateurCourant + " — Mes Préférences",
                1300, 800,
                loader -> {
                    ListePreferencesUtilisateurController ctrl = loader.getController();
                    ctrl.initData(idUtilisateurCourant, this);
                });
    }

    private void navGoCandidatures() {
        // Page actuelle — simple rafraîchissement
        chargerDansNouvelleScene(USER_Candidat,
                "Candidat #" + idUtilisateurCourant + " — Mes Candidatures",
                1300, 800,
                loader -> {
                    ListeCandidaturesUtilisateurController ctrl = loader.getController();
                    ctrl.setIdUtilisateurCourant(idUtilisateurCourant);
                });
    }

    /**
     * Moteur de navigation — ouvre un FXML dans une nouvelle Stage.
     */
    private void chargerDansNouvelleScene(String chemin, String titre,
                                          double largeur, double hauteur,
                                          PostLoadCallback callback) {

        URL url = getClass().getResource(chemin);
        if (url == null) {
            afficherAlerte(Alert.AlertType.ERROR, "Navigation impossible",
                    "FXML introuvable : " + chemin);
            System.err.println("[Navigation] Ressource null : " + chemin);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            callback.apply(loader);

            Stage stage = new Stage();
            stage.setTitle(titre);
            stage.setScene(new Scene(root));
            stage.setWidth(largeur);
            stage.setHeight(hauteur);
            stage.centerOnScreen();
            stage.show();

            Stage currentStage = (Stage) sidebar.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur",
                    "Chargement impossible : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FunctionalInterface
    private interface PostLoadCallback {
        void apply(FXMLLoader loader) throws IOException;
    }
}