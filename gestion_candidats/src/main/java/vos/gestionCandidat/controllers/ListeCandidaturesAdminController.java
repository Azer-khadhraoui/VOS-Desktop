package vos.gestionCandidat.controllers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import vos.gestionCandidat.entities.Candidature;
import vos.gestionCandidat.services.CandidatureService;

public class ListeCandidaturesAdminController implements Initializable {


    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterStatut;

    @FXML private Label subtitleLabel;

    @FXML private Label lblTotal;
    @FXML private Label lblEnAttente;
    @FXML private Label lblAcceptee;
    @FXML private Label lblRefusee;
    @FXML private Label lblNbResultats;

    @FXML private TableView<Candidature> candidatureTable;
    @FXML private TableColumn<Candidature, Integer> colId;
    @FXML private TableColumn<Candidature, String>  colDate;
    @FXML private TableColumn<Candidature, String>  colStatut;
    @FXML private TableColumn<Candidature, String>  colNiveauExp;
    @FXML private TableColumn<Candidature, Integer> colAnneesExp;
    @FXML private TableColumn<Candidature, String>  colDomaine;
    @FXML private TableColumn<Candidature, String>  colDernierPoste;
    @FXML private TableColumn<Candidature, Integer> colIdUtilisateur;
    @FXML private TableColumn<Candidature, Integer> colIdOffre;
    @FXML private TableColumn<Candidature, Void>    colActions;

    @FXML private VBox sidebar;
    @FXML private Label navCandidaturesText;
    @FXML private Label navOffresText;
    @FXML private Label navStatsText;
    @FXML private Label navLogoutText;

    @FXML private Button btnVoirPreferences;  // Ajouter cette ligne après les autres @FXML



    private final CandidatureService service = new CandidatureService();
    private ObservableList<Candidature> masterList;
    private FilteredList<Candidature>  filteredList;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerColonnes();
        configurerFiltres();
        chargerDonnees();
    }


    private void configurerColonnes() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idCandidature"));

        colDate.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDateCandidature() != null
                        ? SDF.format(data.getValue().getDateCandidature()) : "—"));

        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(statut);
                badge.setPadding(new javafx.geometry.Insets(4, 12, 4, 12));
                badge.setStyle("-fx-background-radius: 20; -fx-font-size: 12px; -fx-font-weight: 700; "
                        + getBadgeStyle(statut));
                setGraphic(badge);
                setText(null);
                setAlignment(Pos.CENTER_LEFT);
            }
        });

        colNiveauExp.setCellValueFactory(new PropertyValueFactory<>("niveauExperience"));
        colAnneesExp.setCellValueFactory(new PropertyValueFactory<>("anneesExperience"));
        colDomaine.setCellValueFactory(new PropertyValueFactory<>("domaineExperience"));
        colDernierPoste.setCellValueFactory(new PropertyValueFactory<>("dernierPoste"));
        colIdUtilisateur.setCellValueFactory(new PropertyValueFactory<>("idUtilisateur"));
        colIdOffre.setCellValueFactory(new PropertyValueFactory<>("idOffre"));

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnModifier     = new Button("✏️ Modifier");
            private final Button btnPreferences  = new Button("⭐ Préf.");
            private final Button btnSupprimer    = new Button("🗑 Supprimer");
            private final HBox   box             = new HBox(8, btnModifier, btnPreferences, btnSupprimer);

            {
                box.setAlignment(Pos.CENTER_LEFT);

                btnModifier.setStyle("-fx-background-color: #C7D2FE; -fx-text-fill: #4F46E5; "
                        + "-fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 6; -fx-cursor: hand;");
                btnPreferences.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #92400E; "
                        + "-fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 6; -fx-cursor: hand;");
                btnSupprimer.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; "
                        + "-fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 6; -fx-cursor: hand;");

                btnModifier.setOnAction(e -> {
                    Candidature c = getTableView().getItems().get(getIndex());
                    ouvrirFormulaire(c);
                });

                btnPreferences.setOnAction(e -> {
                    Candidature c = getTableView().getItems().get(getIndex());
                    ouvrirPreferences(c);
                });

                btnSupprimer.setOnAction(e -> {
                    Candidature c = getTableView().getItems().get(getIndex());
                    confirmerSuppression(c);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }
    /* ---------------------- Filtres (search + statut) ---------------------- */

    private void configurerFiltres() {
        filterStatut.setItems(FXCollections.observableArrayList(
                "Tous", "En attente", "Acceptée", "Refusée", "En cours"));
        filterStatut.setValue("Tous");

        // Écoute dynamique sur le champ texte
        searchField.textProperty().addListener((obs, o, n) -> appliquerFiltres());

        // Écoute sur le ComboBox
        filterStatut.valueProperty().addListener((obs, o, n) -> appliquerFiltres());
    }

    private void appliquerFiltres() {
        if (filteredList == null) return;

        String texte  = searchField.getText().toLowerCase().trim();
        String statut = filterStatut.getValue();

        filteredList.setPredicate(c -> {
            boolean matchTexte = texte.isEmpty()
                    || safeContains(c.getDomaineExperience(), texte)
                    || safeContains(c.getDernierPoste(), texte)
                    || safeContains(c.getNiveauExperience(), texte)
                    || safeContains(c.getStatut(), texte)
                    || String.valueOf(c.getIdCandidature()).contains(texte)
                    || String.valueOf(c.getIdUtilisateur()).contains(texte)
                    || String.valueOf(c.getIdOffre()).contains(texte);

            boolean matchStatut = statut == null || statut.equals("Tous")
                    || statut.equalsIgnoreCase(c.getStatut());

            return matchTexte && matchStatut;
        });

        mettreAJourCompteurResultats();
    }

    private boolean safeContains(String value, String search) {
        return value != null && value.toLowerCase().contains(search);
    }

    /* ===================== CHARGEMENT DONNÉES ===================== */

    private void chargerDonnees() {
        List<Candidature> liste = service.getAll();
        masterList   = FXCollections.observableArrayList(liste);
        filteredList = new FilteredList<>(masterList, p -> true);
        candidatureTable.setItems(filteredList);

        mettreAJourStats(liste);
        mettreAJourCompteurResultats();
        subtitleLabel.setText(liste.size() + " candidature(s) au total");
    }

    private void mettreAJourStats(List<Candidature> liste) {
        long total     = liste.size();
        long enAttente = liste.stream().filter(c -> "En attente".equalsIgnoreCase(c.getStatut())).count();
        long acceptee  = liste.stream().filter(c -> "Acceptée".equalsIgnoreCase(c.getStatut())).count();
        long refusee   = liste.stream().filter(c -> "Refusée".equalsIgnoreCase(c.getStatut())).count();

        lblTotal.setText(String.valueOf(total));
        lblEnAttente.setText(String.valueOf(enAttente));
        lblAcceptee.setText(String.valueOf(acceptee));
        lblRefusee.setText(String.valueOf(refusee));
    }

    private void mettreAJourCompteurResultats() {
        if (filteredList != null) {
            lblNbResultats.setText(filteredList.size() + " résultat(s)");
        }
    }

    /* ===================== ACTIONS FXML ===================== */

    @FXML
    private void ajouterCandidature(ActionEvent event) {
        ouvrirFormulaire(null);
    }

    @FXML
    private void rafraichirCandidatures(ActionEvent event) {
        searchField.clear();
        filterStatut.setValue("Tous");
        chargerDonnees();
    }

    @FXML
    private void exporterCandidatures(ActionEvent event) {
        if (filteredList == null || filteredList.isEmpty()) {
            afficherAlerte(Alert.AlertType.INFORMATION, "Export",
                    "Aucune donnée à exporter.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Exporter les candidatures");
        fc.setInitialFileName("candidatures.csv");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichier CSV", "*.csv"));

        Stage stage = (Stage) candidatureTable.getScene().getWindow();
        File file = fc.showSaveDialog(stage);
        if (file == null) return;

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("ID;Date;Statut;Niveau Exp.;Annees Exp.;Domaine;Dernier Poste;ID Utilisateur;ID Offre");
            for (Candidature c : filteredList) {
                pw.printf("%d;%s;%s;%s;%d;%s;%s;%d;%d%n",
                        c.getIdCandidature(),
                        c.getDateCandidature() != null ? SDF.format(c.getDateCandidature()) : "",
                        nvl(c.getStatut()),
                        nvl(c.getNiveauExperience()),
                        c.getAnneesExperience(),
                        nvl(c.getDomaineExperience()),
                        nvl(c.getDernierPoste()),
                        c.getIdUtilisateur(),
                        c.getIdOffre());
            }
            afficherAlerte(Alert.AlertType.INFORMATION, "Export réussi",
                    "Fichier exporté : " + file.getAbsolutePath());
        } catch (IOException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur d'export", e.getMessage());
        }
    }

    /* ===================== NAVIGATION ===================== */

    private void ouvrirFormulaire(Candidature candidature) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/admin/FormCandidatureAdmin.fxml"));
            Parent root = loader.load();

            FormCandidatureAdminController ctrl = loader.getController();
            ctrl.initData(candidature, this);   // passe null pour ajout, objet pour modif

            Stage stage = new Stage();
            stage.setTitle(candidature == null ? "Nouvelle Candidature" : "Modifier la Candidature");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.showAndWait();

        } catch (IOException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }

        /**
     * Ouvre la fenêtre de consultation des préférences
     */
    private void ouvrirPreferences(Candidature candidature) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/admin/FormPreferenceAdmin.fxml"));
            Parent root = loader.load();

            FormPreferenceAdminController ctrl = loader.getController();
            ctrl.initData(candidature, this);

            Stage stage = new Stage();
            stage.setTitle("Préférences de la candidature #" + candidature.getIdCandidature());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.showAndWait();

        } catch (IOException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les préférences : " + e.getMessage());
        }
    }
    private void confirmerSuppression(Candidature c) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText("Supprimer la candidature #" + c.getIdCandidature() + " ?");
        alert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            service.supprimer(c.getIdCandidature());
            chargerDonnees();
        }
    }

    /* ===================== UTILITAIRES ===================== */

    /** Appelé par FormCandidatureAdminController après sauvegarde pour rafraîchir */
    public void rafraichir() {
        chargerDonnees();
    }

    private String getBadgeStyle(String statut) {
        return switch (statut.toLowerCase()) {
            case "acceptée", "acceptee" ->
                    "-fx-background-color: #D1FAE5; -fx-text-fill: #059669;";
            case "refusée", "refusee" ->
                    "-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626;";
            case "en cours" ->
                    "-fx-background-color: #DBEAFE; -fx-text-fill: #2563EB;";
            default ->
                    "-fx-background-color: #FEF3C7; -fx-text-fill: #D97706;";  // En attente
        };
    }

    private String nvl(String s) {
        return s != null ? s : "";
    }

    private void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
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
        // Animate sidebar width expansion
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                        javafx.util.Duration.millis(300),
                        new javafx.animation.KeyValue(sidebar.prefWidthProperty(), 240)
                )
        );
        timeline.play();

        // Fade in labels
        fadeInLabels();
    }

    private void collapseSidebar() {
        // Animate sidebar width collapse
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                        javafx.util.Duration.millis(300),
                        new javafx.animation.KeyValue(sidebar.prefWidthProperty(), 60)
                )
        );
        timeline.play();

        // Fade out labels
        fadeOutLabels();
    }

    private void fadeInLabels() {
        java.util.List<Label> labels = java.util.Arrays.asList(
                navCandidaturesText, navOffresText, navStatsText, navLogoutText
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
                navCandidaturesText, navOffresText, navStatsText, navLogoutText
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

    @FXML
private void allerAuxPreferencesAdmin(ActionEvent event) {
    try {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/admin/ListePreferencesAdmin.fxml"));
        Parent root = loader.load();

        ListePreferencesAdminController ctrl = loader.getController();
        ctrl.rafraichir();

        Stage stage = new Stage();
        stage.setTitle("Liste des Préférences");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root, 1200, 700));
        stage.showAndWait();

        rafraichir();  // Rafraîchir la liste des candidatures après retour

    } catch (IOException e) {
        afficherAlerte(Alert.AlertType.ERROR, "Erreur", 
                "Impossible d'ouvrir la liste des préférences : " + e.getMessage());
    }
}
}