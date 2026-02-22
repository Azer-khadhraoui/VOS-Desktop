package vos.gestionCandidat.controllers.utilisateur;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import vos.gestionCandidat.entities.MatchResult;
import vos.gestionCandidat.entities.PreferenceCandidature;
import vos.gestionCandidat.services.MatchingService;
import vos.gestionCandidat.services.PreferenceCandidatureService;

public class MatchingUtilisateurController implements Initializable {

    // ─── FXML ─────────────────────────────────────────────────────────────────
    @FXML
    private VBox vboxResultats;
    @FXML
    private VBox boxEtatVide;
    @FXML
    private HBox boxChargement;
    @FXML
    private Label lblNombreOffres;
    @FXML
    private Label lblProfilPoste;
    @FXML
    private Label lblProfilContrat;
    @FXML
    private Label lblProfilMode;
    @FXML
    private Label lblProfilDispo;
    @FXML
    private Label lblProfilMobilite;
    @FXML
    private Label lblProfilStatus;
    @FXML
    private Label lblScoreMinVal;
    @FXML
    private ComboBox<String> cmbFiltreNiveau;
    @FXML
    private Slider sliderScoreMin;

    @FXML
    private VBox sidebar;
    @FXML
    private Label navMatchingsText;
    @FXML
    private Label navOffresText;
    @FXML
    private Label navForumText;
    @FXML
    private Label navProfilText;
    @FXML private VBox mainView;
    // ─── Services ─────────────────────────────────────────────────────────────
    // NOTE : Plus besoin de CandidatureService — on n'utilise que les préférences
    private final MatchingService matchingService = new MatchingService();
    private final PreferenceCandidatureService preferenceService = new PreferenceCandidatureService();

    // ─── État ─────────────────────────────────────────────────────────────────
    private List<MatchResult> tousLesResultats;
    private int idUtilisateurConnecte = 3; // ← Remplacer par la session utilisateur réelle

    // ─── INITIALISATION ───────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurerFiltreNiveau();
        configurerSliderScore();
        chargerMatchings();
    }

    private void configurerFiltreNiveau() {
        cmbFiltreNiveau.getItems().addAll("Tous", "Excellent", "Bon", "Moyen", "Faible");
        cmbFiltreNiveau.setValue("Tous");
    }

    private void configurerSliderScore() {
        sliderScoreMin.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblScoreMinVal.setText((int) newVal.doubleValue() + "%");
            appliquerFiltres();
        });
    }

    // ─── CHARGEMENT ───────────────────────────────────────────────────────────
    private void chargerMatchings() {
        afficherChargement(true);

        new Thread(() -> {
            try {
                // 1. Vérifier que l'utilisateur a bien des préférences
                PreferenceCandidature preference
                        = preferenceService.getByIdUtilisateur(idUtilisateurConnecte);

                if (preference == null) {
                    Platform.runLater(() -> {
                        afficherChargement(false);
                        afficherEtatVide();
                        lblProfilStatus.setText("⚠️ Vous n'avez pas encore renseigné vos préférences.");
                    });
                    return;
                }

                // 2. Calculer les matchs basés sur les préférences uniquement
                tousLesResultats = matchingService.calculerMatchsPourUtilisateur(idUtilisateurConnecte);

                // 3. Mettre à jour l'UI
                final PreferenceCandidature pFinal = preference;
                Platform.runLater(() -> {
                    afficherChargement(false);
                    mettreAJourResumelProfil(pFinal);
                    afficherResultats(tousLesResultats);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    afficherChargement(false);
                    lblProfilStatus.setText("❌ Erreur : " + e.getMessage());
                });
            }
        }).start();
    }

    // ─── RÉSUMÉ DU PROFIL (préférences uniquement) ────────────────────────────
    //v1
    /*private void mettreAJourResumelProfil(PreferenceCandidature preference) {
        lblProfilPoste.setText("Poste souhaité : "
                + (preference.getTypePosteSouhaite() != null ? preference.getTypePosteSouhaite() : "—"));
        lblProfilContrat.setText("Contrat : "
                + (preference.getTypeContratSouhaite() != null ? preference.getTypeContratSouhaite() : "—"));
        lblProfilMode.setText("Mode : "
                + (preference.getModeTravail() != null ? preference.getModeTravail() : "—"));
        lblProfilDispo.setText("Disponibilité : "
                + (preference.getDisponibilite() != null ? preference.getDisponibilite() : "—"));
        lblProfilMobilite.setText("Mobilité : "
                + (preference.getMobiliteGeographique() != null ? preference.getMobiliteGeographique() : "—"));
        lblProfilStatus.setText("✅ Profil chargé depuis vos préférences");
    }*/
    //v2
    private void mettreAJourResumelProfil(PreferenceCandidature preference) {
    lblProfilPoste.setText("🎯 Poste : " +
            (preference.getTypePosteSouhaite() != null ? preference.getTypePosteSouhaite() : "—"));
    lblProfilContrat.setText("📋 Contrat : " +
            (preference.getTypeContratSouhaite() != null ? preference.getTypeContratSouhaite() : "—"));
    lblProfilMode.setText("💻 Mode : " +
            (preference.getModeTravail() != null ? preference.getModeTravail() : "—"));
    lblProfilDispo.setText("📅 Dispo : " +
            (preference.getDisponibilite() != null ? preference.getDisponibilite() : "—"));
    lblProfilMobilite.setText("📍 Mobilité : " +
            (preference.getMobiliteGeographique() != null ? preference.getMobiliteGeographique() : "—"));
    lblProfilStatus.setText("✅ Profil chargé depuis vos préférences");
}

    // ─── AFFICHAGE DES RÉSULTATS ──────────────────────────────────────────────
    private void afficherResultats(List<MatchResult> resultats) {
        vboxResultats.getChildren().clear();

        if (resultats == null || resultats.isEmpty()) {
            afficherEtatVide();
            return;
        }

        boxEtatVide.setVisible(false);
        boxEtatVide.setManaged(false);
        lblNombreOffres.setText(resultats.size() + " offre(s) compatible(s)");

        for (MatchResult match : resultats) {
            vboxResultats.getChildren().add(creerCarteMatch(match));
        }
    }

    // ─── CARTE VISUELLE D'UN RÉSULTAT ─────────────────────────────────────────
    private VBox creerCarteMatch(MatchResult match) {
        VBox carte = new VBox(10);
        carte.setPadding(new Insets(16));
        carte.setStyle(
                "-fx-background-color: white;"
                + "-fx-background-radius: 10;"
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 6, 0, 0, 2);"
        );

        // ── Ligne 1 : Titre + Badge + Score ──────────────────────────────────
        HBox ligne1 = new HBox(12);
        ligne1.setAlignment(Pos.CENTER_LEFT);

        Label lblTitre = new Label(match.getTitreOffre());
        lblTitre.setFont(Font.font("System", FontWeight.BOLD, 15));
        lblTitre.setStyle("-fx-text-fill: #2C3E50;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badgeNiveau = new Label(match.getNiveauMatch());
        badgeNiveau.setStyle(
                "-fx-background-color: " + couleurNiveau(match.getNiveauMatch()) + ";"
                + "-fx-text-fill: white; -fx-padding: 4 12; -fx-background-radius: 20;"
                + "-fx-font-weight: bold; -fx-font-size: 12px;"
        );

        Label lblScore = new Label(match.getScoreTotal() + "%");
        lblScore.setFont(Font.font("System", FontWeight.BOLD, 24));
        lblScore.setStyle("-fx-text-fill: " + couleurScore(match.getScoreTotal()) + ";");

        ligne1.getChildren().addAll(lblTitre, spacer, badgeNiveau, lblScore);

        // ── Ligne 2 : Contrat + Barre de progression ──────────────────────────
        HBox ligne2 = new HBox(12);
        ligne2.setAlignment(Pos.CENTER_LEFT);

        Label lblContrat = new Label("📋 " + (match.getTypeContratOffre() != null ? match.getTypeContratOffre() : "—"));
        lblContrat.setStyle("-fx-font-size: 12px; -fx-text-fill: #7F8C8D;");

        ProgressBar barre = new ProgressBar(match.getScoreTotal() / 100.0);
        barre.setPrefHeight(8);
        HBox.setHgrow(barre, Priority.ALWAYS);
        barre.setStyle("-fx-accent: " + couleurScore(match.getScoreTotal()) + ";");

        ligne2.getChildren().addAll(lblContrat, barre);

        // ── Ligne 3 : Détail des 5 critères de préférences ────────────────────
        HBox ligne3 = new HBox(6);
        ligne3.setAlignment(Pos.CENTER_LEFT);
        ligne3.setPadding(new Insets(6, 0, 0, 0));

        ligne3.getChildren().addAll(
                creerBadgeCritere("🎯 Poste", match.getScoreTypePoste(), "40%"),
                creerBadgeCritere("📋 Contrat", match.getScoreContrat(), "30%"),
                creerBadgeCritere("💻 Mode", match.getScoreModeTravail(), "15%"),
                creerBadgeCritere("📅 Dispo", match.getScoreDisponibilite(), "10%"),
                creerBadgeCritere("📍 Mobilité", match.getScoreMobilite(), "5%")
        );

        // ── Ligne 4 : Description tronquée ───────────────────────────────────
        carte.getChildren().addAll(ligne1, ligne2, ligne3);

        if (match.getDescriptionOffre() != null && !match.getDescriptionOffre().isEmpty()) {
            String desc = match.getDescriptionOffre().length() > 130
                    ? match.getDescriptionOffre().substring(0, 130) + "..."
                    : match.getDescriptionOffre();
            Label lblDesc = new Label(desc);
            lblDesc.setStyle("-fx-font-size: 12px; -fx-text-fill: #95A5A6; -fx-wrap-text: true;");
            lblDesc.setMaxWidth(Double.MAX_VALUE);
            carte.getChildren().addAll(new Separator(), lblDesc);
        }

        // ── Hover ─────────────────────────────────────────────────────────────
        carte.setOnMouseEntered(e -> carte.setStyle(
                "-fx-background-color: #FDFEFE; -fx-background-radius: 10;"
                + "-fx-border-color: #3498DB; -fx-border-width: 1; -fx-border-radius: 10;"
                + "-fx-effect: dropshadow(three-pass-box, rgba(52,152,219,0.15), 10, 0, 0, 3);"
        ));
        carte.setOnMouseExited(e -> carte.setStyle(
                "-fx-background-color: white; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 6, 0, 0, 2);"
        ));

        return carte;
    }

    private HBox creerBadgeCritere(String nom, int score, String poids) {
        HBox badge = new HBox(3);
        badge.setAlignment(Pos.CENTER);
        badge.setPadding(new Insets(3, 8, 3, 8));
        badge.setStyle(
                "-fx-background-color: " + couleurBadge(score) + ";"
                + "-fx-background-radius: 12;"
        );
        Label lbl = new Label(nom + " : " + score + "% (" + poids + ")");
        lbl.setStyle("-fx-font-size: 10px; -fx-text-fill: white; -fx-font-weight: bold;");
        badge.getChildren().add(lbl);
        return badge;
    }

    // ─── FILTRES ──────────────────────────────────────────────────────────────
    @FXML
    private void onFiltrerNiveau() {
        appliquerFiltres();
    }

    private void appliquerFiltres() {
        if (tousLesResultats == null) {
            return;
        }

        String niveauFiltre = cmbFiltreNiveau.getValue();
        int scoreMin = (int) sliderScoreMin.getValue();

        List<MatchResult> filtres = tousLesResultats.stream()
                .filter(m -> "Tous".equals(niveauFiltre) || m.getNiveauMatch().equals(niveauFiltre))
                .filter(m -> m.getScoreTotal() >= scoreMin)
                .collect(Collectors.toList());

        afficherResultats(filtres);
    }

    @FXML
    private void onActualiser() {
        tousLesResultats = null;
        vboxResultats.getChildren().clear();
        chargerMatchings();
    }

    @FXML
    private void onCompleterProfil() {
        System.out.println("→ Naviguer vers le formulaire de préférences");
    }

    // ─── ÉTATS UI ─────────────────────────────────────────────────────────────
    private void afficherChargement(boolean visible) {
        boxChargement.setVisible(visible);
        boxChargement.setManaged(visible);
    }

    private void afficherEtatVide() {
        boxEtatVide.setVisible(true);
        boxEtatVide.setManaged(true);
        lblNombreOffres.setText("0 offre compatible");
    }

    // ─── SETTER SESSION ───────────────────────────────────────────────────────
    public void setIdUtilisateurConnecte(int idUtilisateur) {
        this.idUtilisateurConnecte = idUtilisateur;
        chargerMatchings();
    }

    // ─── COULEURS ─────────────────────────────────────────────────────────────
    private String couleurScore(int score) {
        if (score >= 75) {
            return "#27AE60";
        }
        if (score >= 50) {
            return "#2980B9";
        }
        if (score >= 25) {
            return "#F39C12";
        }
        return "#E74C3C";
    }

    private String couleurNiveau(String niveau) {
        switch (niveau) {
            case "Excellent":
                return "#27AE60";
            case "Bon":
                return "#2980B9";
            case "Moyen":
                return "#F39C12";
            default:
                return "#E74C3C";
        }
    }

    private String couleurBadge(int score) {
        if (score >= 75) {
            return "#27AE60";
        }
        if (score >= 50) {
            return "#2980B9";
        }
        if (score >= 25) {
            return "#F39C12";
        }
        return "#BDC3C7";
    }
    // ─── SIDEBAR ANIMATIONS ───────────────────────────────────────────

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
                navMatchingsText, navOffresText, navForumText, navProfilText
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
                navMatchingsText, navOffresText, navForumText, navProfilText
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
