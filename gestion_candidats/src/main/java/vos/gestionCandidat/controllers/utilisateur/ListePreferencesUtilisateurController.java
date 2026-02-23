package vos.gestionCandidat.controllers.utilisateur;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import vos.gestionCandidat.entities.Candidature;
import vos.gestionCandidat.entities.PreferenceCandidature;
import vos.gestionCandidat.services.candidat.PreferenceCandidatureService;

public class ListePreferencesUtilisateurController {

    @FXML
    private VBox mainView;
    @FXML
    private VBox preferencesContainer;
    @FXML
    private VBox emptyState;
    @FXML
    private Label heroSubtitle;
    @FXML
    private Label lblNombrePreferences;

   

    private PreferenceCandidatureService service = new PreferenceCandidatureService();
    private int idUtilisateurCourant = 3;
    private ListeCandidaturesUtilisateurController parentController;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd MMM yyyy");

    @FXML
    public void initialize() {
        loadPreferences();
    }

    /**
     * Initialiser avec l'ID de l'utilisateur et le contrôleur parent
     */
    public void initData(int idUtilisateur, ListeCandidaturesUtilisateurController parent) {
        this.idUtilisateurCourant = idUtilisateur;
        this.parentController = parent;
        loadPreferences();
    }

    private void loadPreferences() {
        List<PreferenceCandidature> preferences = service.getByUtilisateur(idUtilisateurCourant);

        if (preferences.isEmpty()) {
            preferencesContainer.setVisible(false);
            preferencesContainer.setManaged(false);
            emptyState.setVisible(true);
            emptyState.setManaged(true);
            heroSubtitle.setText("Vous n'avez pas encore de préférences enregistrées");
            lblNombrePreferences.setText("0");

        } else {
            preferencesContainer.setVisible(true);
            preferencesContainer.setManaged(true);
            emptyState.setVisible(false);
            emptyState.setManaged(false);
            heroSubtitle.setText("Vous avez " + preferences.size() + " préférence(s) enregistrée(s)");
            lblNombrePreferences.setText(String.valueOf(preferences.size()));

            preferencesContainer.getChildren().clear();
            for (PreferenceCandidature pref : preferences) {
                preferencesContainer.getChildren().add(creerCartePrefence(pref));
            }
        }
    }

    /**
     * Crée une card de préférence
     */
    //v1
    /*private Node creerCartePrefence(PreferenceCandidature pref) {
        // Conteneur principal
        VBox card = new VBox(16);
        card.setPadding(new Insets(24, 28, 24, 28));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; "
                + "-fx-border-color: rgba(139, 92, 246, 0.2); -fx-border-width: 2; -fx-border-radius: 16; "
                + "-fx-effect: dropshadow(gaussian, rgba(139, 92, 246, 0.1), 20, 0, 0, 5); -fx-cursor: hand;");

        // Header avec ID et badge
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label idLabel = new Label("ID #" + pref.getIdPreference());
        idLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #8b5cf6; "
                + "-fx-background-color: #ede9fe; -fx-background-radius: 20; -fx-padding: 4 12;");

        Label typePosteLabel = new Label(nvl(pref.getTypePosteSouhaite(), "Non spécifié"));
        typePosteLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #0f172a;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnModifier = new Button("✏️ Modifier");
        btnModifier.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #6366f1; "
                + "-fx-background-radius: 10; -fx-padding: 8 16; -fx-font-weight: 600; "
                + "-fx-font-size: 12px; -fx-cursor: hand;");

        Button btnSupprimer = new Button("🗑️ Supprimer");
        btnSupprimer.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; "
                + "-fx-background-radius: 10; -fx-padding: 8 16; -fx-font-weight: 600; "
                + "-fx-font-size: 12px; -fx-cursor: hand;");

        btnModifier.setOnAction(e -> modifierPreference(pref));
        btnSupprimer.setOnAction(e -> supprimerPreference(pref));

        header.getChildren().addAll(idLabel, typePosteLabel, spacer, btnModifier, btnSupprimer);

        // Grille d'informations
        HBox row1 = creerLigneInfo(
                "💼 Mode Travail", nvl(pref.getModeTravail(), "—"),
                "📅 Disponibilité", nvl(pref.getDisponibilite(), "—")
        );

        HBox row2 = creerLigneInfo(
                "📄 Type Contrat", nvl(pref.getTypeContratSouhaite(), "—"),
                "💰 Prétention Salariale", String.format("%.2f DT", pref.getPretentionSalariale())
        );

        HBox row3 = creerLigneInfo(
                "🚗 Mobilité Géographique", nvl(pref.getMobiliteGeographique(), "—"),
                "✈️ Prêt Déplacement", nvl(pref.getPretDeplacement(), "—")
        );

        card.getChildren().addAll(header, row1, row2, row3);

        // Hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle(card.getStyle()
                    .replace("-fx-border-color: rgba(139, 92, 246, 0.2);",
                            "-fx-border-color: rgba(139, 92, 246, 0.6);")
                    .replace("-fx-background-color: white;",
                            "-fx-background-color: linear-gradient(to right, #ffffff 0%, #f5f3ff 100%);"));
        });

        card.setOnMouseExited(e -> {
            card.setStyle(card.getStyle()
                    .replace("-fx-border-color: rgba(139, 92, 246, 0.6);",
                            "-fx-border-color: rgba(139, 92, 246, 0.2);")
                    .replace("-fx-background-color: linear-gradient(to right, #ffffff 0%, #f5f3ff 100%);",
                            "-fx-background-color: white;"));
        });

        return card;
    }*/
    //v2
    private Node creerCartePrefence(PreferenceCandidature pref) {
        // Conteneur principal
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20, 24, 20, 24));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; "
                + "-fx-border-color: rgba(226,232,240,0.8); -fx-border-width: 1.5; -fx-border-radius: 16; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 18, 0, 0, 4); -fx-cursor: hand;");

        // Icône statut
        Label icone = new Label("⭐");
        icone.setStyle("-fx-font-size: 26px; -fx-padding: 10; "
                + "-fx-background-color: #ede9fe; "
                + "-fx-background-radius: 14; -fx-min-width: 50; -fx-min-height: 50; "
                + "-fx-max-width: 50; -fx-max-height: 50; -fx-alignment: center;");

        // Bloc principal info
        VBox infoBox = new VBox(6);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label titre = new Label( nvl(pref.getTypePosteSouhaite(), "Préférence"));
        titre.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #0f172a;");

        Label details = new Label("💼 " + nvl(pref.getTypeContratSouhaite(), "Contrat non précisé")
                + "  ·  💻 " + nvl(pref.getModeTravail(), "Mode non précisé")
                + "  ·  📍 " + nvl(pref.getMobiliteGeographique(), "Mobilité non précisée"));
        details.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        Label salaire = new Label("💰 " + String.format("%.2f DT/mois", pref.getPretentionSalariale())
                + "  ·  📅 " + nvl(pref.getDisponibilite(), "Disponibilité non précisée"));
        salaire.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8; -fx-font-weight: 500;");

        infoBox.getChildren().addAll(titre, details, salaire);

        // Badge statut
        Label badge = new Label("✅ Active");
        badge.setStyle("-fx-background-radius: 20; -fx-padding: 5 14; "
                + "-fx-font-size: 12px; -fx-font-weight: 700; "
                + "-fx-background-color: #d1fae5; -fx-text-fill: #059669;");

        // Boutons action
        VBox btnBox = new VBox(8);
        btnBox.setAlignment(Pos.CENTER);

        Button btnModifier = new Button("✏️ Modifier");
        btnModifier.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #8b5cf6; "
                + "-fx-background-radius: 10; -fx-padding: 8 18; "
                + "-fx-font-weight: 600; -fx-font-size: 12px; -fx-cursor: hand;");

        Button btnSupprimer = new Button("🗑️ Supprimer");
        btnSupprimer.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; "
                + "-fx-background-radius: 10; -fx-padding: 8 18; "
                + "-fx-font-weight: 600; -fx-font-size: 12px; -fx-cursor: hand;");

        btnModifier.setOnAction(e -> modifierPreference(pref));
        btnSupprimer.setOnAction(e -> supprimerPreference(pref));

        btnBox.getChildren().addAll(btnModifier, btnSupprimer);

        card.getChildren().addAll(icone, infoBox, badge, btnBox);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle()
                .replace("-fx-border-color: rgba(226,232,240,0.8);",
                        "-fx-border-color: rgba(139,92,246,0.4);")
                .replace("-fx-background-color: white;",
                        "-fx-background-color: linear-gradient(to right, #ffffff 0%, #f5f3ff 100%);")));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle()
                .replace("-fx-border-color: rgba(139,92,246,0.4);",
                        "-fx-border-color: rgba(226,232,240,0.8);")
                .replace("-fx-background-color: linear-gradient(to right, #ffffff 0%, #f5f3ff 100%);",
                        "-fx-background-color: white;")));

        return card;
    }

    /**
     * Crée une ligne d'information (2 colonnes)
     */
    private HBox creerLigneInfo(String label1, String value1, String label2, String value2) {
        HBox row = new HBox(24);
        row.setAlignment(Pos.CENTER_LEFT);

        // Colonne 1
        VBox col1 = new VBox(4);
        Label lbl1 = new Label(label1);
        lbl1.setStyle("-fx-font-size: 11px; -fx-text-fill: #8b5cf6; -fx-font-weight: 600;");
        Label val1 = new Label(value1);
        val1.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");
        col1.getChildren().addAll(lbl1, val1);
        HBox.setHgrow(col1, Priority.ALWAYS);

        // Colonne 2
        VBox col2 = new VBox(4);
        Label lbl2 = new Label(label2);
        lbl2.setStyle("-fx-font-size: 11px; -fx-text-fill: #8b5cf6; -fx-font-weight: 600;");
        Label val2 = new Label(value2);
        val2.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");
        col2.getChildren().addAll(lbl2, val2);
        HBox.setHgrow(col2, Priority.ALWAYS);

        row.getChildren().addAll(col1, col2);
        return row;
    }

    @FXML
    private void ajouterPreference(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/utilisateur/FormPreference.fxml"));
            Parent root = loader.load();

            FormPreferenceUtilisateurController ctrl = loader.getController();

            Candidature candTemp = new Candidature();
            candTemp.setIdUtilisateur(idUtilisateurCourant);

            ctrl.initData(candTemp, null, parentController);

            Stage stage = new Stage();
            stage.setTitle("Ajouter une préférence");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadPreferences();

        } catch (IOException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void modifierPreference(PreferenceCandidature pref) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/utilisateur/FormPreference.fxml"));
            Parent root = loader.load();

            FormPreferenceUtilisateurController ctrl = loader.getController();

            Candidature candTemp = new Candidature();
            candTemp.setIdUtilisateur(idUtilisateurCourant);

            ctrl.initData(candTemp, pref, parentController);

            Stage stage = new Stage();
            stage.setTitle("Modifier ma préférence");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadPreferences();

        } catch (IOException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void supprimerPreference(PreferenceCandidature pref) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer");
        confirm.setHeaderText("⚠️ Êtes-vous sûr ?");
        confirm.setContentText("Supprimer cette préférence définitivement ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                service.supprimer(pref.getIdPreference());
                loadPreferences();

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("✅ Supprimée");
                success.setHeaderText(null);
                success.setContentText("Préférence supprimée avec succès !");
                success.showAndWait();
            }
        });
    }

    @FXML
    private void retournerAuxCandidatures(ActionEvent event) {
        ((Stage) mainView.getScene().getWindow()).close();
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

   

    public void rafraichir() {
        loadPreferences();
    }
}
