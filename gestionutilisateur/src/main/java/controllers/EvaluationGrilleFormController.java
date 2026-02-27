package controllers;

import entities.Entretien;
import entities.EvaluationEntretien;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.AIService;
import services.EntretienService;
import services.EvaluationEntretienService;

import java.util.HashMap;
import java.util.Map;

public class EvaluationGrilleFormController {

    @FXML private Label entretienInfoLabel;
    @FXML private VBox questionsExemplesBox;
    @FXML private HBox loadingBox;
    @FXML private Button btnGenererIA;

    // Compétences Techniques
    @FXML private ToggleButton techBtn1, techBtn2, techBtn3, techBtn4, techBtn5;

    // Compétences Comportementales
    @FXML private ToggleButton compBtn1, compBtn2, compBtn3, compBtn4, compBtn5;

    // Communication
    @FXML private ToggleButton commBtn1, commBtn2, commBtn3, commBtn4, commBtn5;

    // Motivation
    @FXML private ToggleButton motivBtn1, motivBtn2, motivBtn3, motivBtn4, motivBtn5;

    // Expérience
    @FXML private ToggleButton expBtn1, expBtn2, expBtn3, expBtn4, expBtn5;

    @FXML private TextField scoreField;
    @FXML private TextField noteField;
    @FXML private TextArea commentaireField;
    @FXML private ComboBox<String> decisionField;
    @FXML private TextField idEntretienField;
    @FXML private ComboBox<String> entretienComboField;
    @FXML private javafx.scene.layout.VBox entretienSelectBox;
    @FXML private Label entretienSelectError;

    // Map pour retrouver l'id à partir du label affiché
    private java.util.Map<String, Integer> entretienMap = new java.util.HashMap<>();

    // ========== LABELS DE VALIDATION ==========
    @FXML private Label scoreError;
    @FXML private Label noteError;
    @FXML private Label commentaireError;
    @FXML private Label commentaireCount;
    @FXML private Label decisionError;

    private EvaluationEntretienService evaluationService = new EvaluationEntretienService();
    private EntretienService entretienService = new EntretienService();
    private EvaluationEntretien currentEvaluation;
    private Entretien currentEntretien;
    private boolean saved = false;

    // ToggleGroups pour les ratings
    private ToggleGroup techGroup = new ToggleGroup();
    private ToggleGroup compGroup = new ToggleGroup();
    private ToggleGroup commGroup = new ToggleGroup();
    private ToggleGroup motivGroup = new ToggleGroup();
    private ToggleGroup expGroup = new ToggleGroup();

    // Questions exemples selon le type d'entretien
    private Map<String, String[]> questionsParType = new HashMap<>();

    @FXML
    public void initialize() {
        // Configuration des décisions
        decisionField.setItems(FXCollections.observableArrayList(
                "Accepté", "Refusé", "En attente", "À revoir"
        ));

        // Initialiser les questions exemples
        initQuestionsExemples();

        // ========== CHARGER LES ENTRETIENS TERMINÉS dans le ComboBox ==========
        chargerEntretiensTermines();
        if (entretienComboField != null) {
            entretienComboField.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && entretienMap.containsKey(newVal)) {
                    int id = entretienMap.get(newVal);
                    idEntretienField.setText(String.valueOf(id));
                    currentEntretien = entretienService.getEntretienById(id);
                    if (currentEntretien != null) {
                        entretienInfoLabel.setText(String.format(
                                "Entretien %s - Date: %s à %s - Lieu: %s",
                                currentEntretien.getTypeEntretien(),
                                currentEntretien.getDateEntretien(),
                                currentEntretien.getHeureEntretien(),
                                currentEntretien.getLieu()
                        ));
                        afficherQuestionsExemples(currentEntretien.getTypeEntretien());
                    }
                    // Effacer l'erreur
                    if (entretienSelectError != null) {
                        entretienSelectError.setVisible(false);
                        entretienSelectError.setManaged(false);
                    }
                }
            });
        }

        try {
            // Configuration des ToggleGroups
            setupToggleGroup(techGroup, techBtn1, techBtn2, techBtn3, techBtn4, techBtn5);
            setupToggleGroup(compGroup, compBtn1, compBtn2, compBtn3, compBtn4, compBtn5);
            setupToggleGroup(commGroup, commBtn1, commBtn2, commBtn3, commBtn4, commBtn5);
            setupToggleGroup(motivGroup, motivBtn1, motivBtn2, motivBtn3, motivBtn4, motivBtn5);
            setupToggleGroup(expGroup, expBtn1, expBtn2, expBtn3, expBtn4, expBtn5);

            // ========== AUTO-CALCUL NOTE GLOBALE ==========
            techGroup.selectedToggleProperty().addListener((obs, o, n) -> calculerNoteGlobale());
            compGroup.selectedToggleProperty().addListener((obs, o, n) -> calculerNoteGlobale());
            commGroup.selectedToggleProperty().addListener((obs, o, n) -> calculerNoteGlobale());
            motivGroup.selectedToggleProperty().addListener((obs, o, n) -> calculerNoteGlobale());
            expGroup.selectedToggleProperty().addListener((obs, o, n) -> calculerNoteGlobale());

        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation des boutons: " + e.getMessage());
            e.printStackTrace();
        }

        // ========== VALIDATION EN TEMPS RÉEL ==========
        scoreField.textProperty().addListener((obs, o, n) -> validateScore());
        noteField.textProperty().addListener((obs, o, n) -> validateNote());
        commentaireField.textProperty().addListener((obs, o, newVal) -> {
            validateCommentaire();
            int len = newVal == null ? 0 : newVal.trim().length();
            if (commentaireCount != null) {
                commentaireCount.setText(len + " / 500 caractères");
                commentaireCount.setStyle(len > 500
                        ? "-fx-text-fill: #F87171; -fx-font-size: 11px; -fx-padding: 0 0 0 4;"
                        : "-fx-text-fill: #6B7280; -fx-font-size: 11px; -fx-padding: 0 0 0 4;");
            }
        });
        decisionField.valueProperty().addListener((obs, o, n) -> validateDecision());
    }

    private void setupToggleGroup(ToggleGroup group, ToggleButton... buttons) {
        for (ToggleButton btn : buttons) {
            if (btn != null) {
                btn.setToggleGroup(group);
                // Style pour le bouton sélectionné
                final ToggleButton finalBtn = btn;
                btn.selectedProperty().addListener((obs, oldVal, newVal) -> {
                    if (Boolean.TRUE.equals(newVal)) {
                        finalBtn.setStyle(finalBtn.getStyle() + "; -fx-background-color: linear-gradient(from 0% 0% to 100% 0%, #667eea, #764ba2);");
                    } else {
                        String currentStyle = finalBtn.getStyle();
                        if (currentStyle != null) {
                            finalBtn.setStyle(currentStyle.replace("; -fx-background-color: linear-gradient(from 0% 0% to 100% 0%, #667eea, #764ba2);", ""));
                        }
                    }
                });
            }
        }
    }

    private void initQuestionsExemples() {
        // Questions pour entretien RH
        questionsParType.put("RH", new String[]{
                "• Parlez-moi de votre parcours professionnel",
                "• Quelles sont vos principales forces et faiblesses ?",
                "• Pourquoi souhaitez-vous rejoindre notre entreprise ?",
                "• Où vous voyez-vous dans 5 ans ?",
                "• Comment gérez-vous le stress et les situations difficiles ?",
                "• Décrivez une situation où vous avez travaillé en équipe"
        });

        // Questions pour entretien TECHNIQUE
        questionsParType.put("TECHNIQUE", new String[]{
                "• Présentez votre dernier projet technique en détail",
                "• Quelles technologies maîtrisez-vous le mieux ?",
                "• Comment abordez-vous le débogage d'un problème complexe ?",
                "• Expliquez le concept de [technologie spécifique au poste]",
                "• Avez-vous de l'expérience avec [framework/outil] ?",
                "• Comment assurez-vous la qualité de votre code ?"
        });
    }

    private void afficherQuestionsExemples(String typeEntretien) {
        questionsExemplesBox.getChildren().clear();

        String[] questions = questionsParType.getOrDefault(typeEntretien, new String[]{
                "• Questions générales d'entretien",
                "• Parcours professionnel",
                "• Compétences techniques",
                "• Motivation pour le poste"
        });

        for (String question : questions) {
            Label questionLabel = new Label(question);
            questionLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 3 0;");
            questionLabel.setWrapText(true);
            questionsExemplesBox.getChildren().add(questionLabel);
        }
    }

    // ============================================================
    // BOUTON IA — Générer questions personnalisées
    // ============================================================
    @FXML
    private void handleGenererQuestions() {
        if (currentEntretien == null) {
            afficherMessageQuestions("⚠ Aucun entretien chargé.", "#F87171");
            return;
        }

        // Récupérer les infos du poste via la candidature
        String typeEntretien     = currentEntretien.getTypeEntretien();
        String poste             = obtenirNomPoste();
        String niveauExperience  = obtenirNiveauExperience();
        String domaineExperience = obtenirDomaineExperience();

        // Afficher le chargement
        setLoading(true);

        // Appel IA en arrière-plan
        AIService.genererQuestionsEntretien(
                typeEntretien,
                poste,
                niveauExperience,
                domaineExperience,
                result -> Platform.runLater(() -> {
                    setLoading(false);
                    afficherQuestionsIA(result);
                    // Sauvegarder les questions dans l'entretien
                    sauvegarderQuestionsEntretien(result);
                }),
                error -> Platform.runLater(() -> {
                    setLoading(false);
                    // Afficher le message d'erreur formaté
                    afficherMessageQuestions(error, "#F87171");
                })
        );
    }

    private void setLoading(boolean loading) {
        if (loadingBox != null) {
            loadingBox.setVisible(loading);
            loadingBox.setManaged(loading);
        }
        if (btnGenererIA != null) {
            btnGenererIA.setDisable(loading);
            btnGenererIA.setText(loading ? "⏳ Génération..." : "🤖 Générer avec IA");
        }
    }

    private void afficherQuestionsIA(String texteQuestions) {
        questionsExemplesBox.getChildren().clear();

        // Vérifier si c'est un message avec questions par défaut (fallback)
        boolean estParDefaut = texteQuestions.startsWith("⚠️");

        // Badge IA ou Warning
        Label badgeIA = new Label(estParDefaut ? "⚠️ Questions par défaut" : "✨ Questions générées par IA");
        if (estParDefaut) {
            badgeIA.setStyle("-fx-background-color: rgba(251,146,60,0.3); "
                    + "-fx-text-fill: #fb923c; -fx-font-size: 11px; -fx-font-weight: bold; "
                    + "-fx-padding: 4 10; -fx-background-radius: 20; -fx-border-radius: 20;");
        } else {
            badgeIA.setStyle("-fx-background-color: rgba(102,126,234,0.3); "
                    + "-fx-text-fill: #a5b4fc; -fx-font-size: 11px; -fx-font-weight: bold; "
                    + "-fx-padding: 4 10; -fx-background-radius: 20; -fx-border-radius: 20;");
        }
        questionsExemplesBox.getChildren().add(badgeIA);

        // Extraire le message d'avertissement et les questions si présent
        String texteAfficher = texteQuestions;
        if (estParDefaut) {
            // Afficher le message d'avertissement
            int indexQuestions = texteQuestions.indexOf("Questions par défaut");
            if (indexQuestions > 0) {
                String messageWarning = texteQuestions.substring(0, indexQuestions).trim();
                Label warningLabel = new Label(messageWarning);
                warningLabel.setStyle("-fx-text-fill: #fb923c; -fx-font-size: 11px; "
                        + "-fx-padding: 5; -fx-font-style: italic;");
                warningLabel.setWrapText(true);
                warningLabel.setMaxWidth(Double.MAX_VALUE);
                questionsExemplesBox.getChildren().add(warningLabel);

                // Extraire seulement les questions
                texteAfficher = texteQuestions.substring(indexQuestions + "Questions par défaut :".length()).trim();
            }
        }

        // Afficher chaque question sur une ligne
        String[] lignes = texteAfficher.split("\n");
        for (String ligne : lignes) {
            String q = ligne.trim();
            if (q.isEmpty()) continue;

            Label label = new Label(q);
            label.setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 13px; "
                    + "-fx-padding: 6 10; -fx-background-color: rgba(255,255,255,0.05); "
                    + "-fx-background-radius: 8;");
            label.setWrapText(true);
            label.setMaxWidth(Double.MAX_VALUE);
            questionsExemplesBox.getChildren().add(label);
        }

        // Bouton "Régénérer"
        Button btnRegenerer = new Button("🔄 Régénérer");
        btnRegenerer.setStyle("-fx-background-color: transparent; -fx-text-fill: #667eea; "
                + "-fx-font-size: 11px; -fx-cursor: hand; -fx-underline: true; -fx-border-width: 0;");
        btnRegenerer.setOnAction(e -> handleGenererQuestions());
        questionsExemplesBox.getChildren().add(btnRegenerer);
    }

    private void afficherMessageQuestions(String msg, String couleur) {
        questionsExemplesBox.getChildren().clear();
        Label label = new Label(msg);
        label.setStyle("-fx-text-fill: " + couleur + "; "
                + "-fx-font-size: 12px; "
                + "-fx-padding: 10; "
                + "-fx-background-color: rgba(248, 113, 113, 0.1); "
                + "-fx-background-radius: 5; "
                + "-fx-border-color: " + couleur + "; "
                + "-fx-border-width: 1; "
                + "-fx-border-radius: 5;");
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        questionsExemplesBox.getChildren().add(label);
    }

    // Sauvegarder les questions générées dans l'entretien
    private void sauvegarderQuestionsEntretien(String questions) {
        if (currentEntretien == null || questions == null || questions.trim().isEmpty()) {
            return;
        }

        try {
            // Nettoyer le texte des questions (enlever les avertissements si présents)
            String questionsNettoyees = questions;
            if (questions.contains("Questions par défaut :")) {
                int index = questions.indexOf("Questions par défaut :");
                questionsNettoyees = questions.substring(index + "Questions par défaut :".length()).trim();
            }

            // Mettre à jour l'entretien
            currentEntretien.setQuestionsEntretien(questionsNettoyees);
            entretienService.updateEntretien(currentEntretien);

            System.out.println("✅ Questions sauvegardées pour l'entretien #" + currentEntretien.getIdEntretien());
        } catch (Exception e) {
            System.err.println("❌ Erreur sauvegarde questions: " + e.getMessage());
        }
    }

    // Récupérer le nom du poste depuis la BDD via la candidature → offre
    private String obtenirNomPoste() {
        try {
            java.sql.Connection conn = utilis.MyConnection.getInstance().getCnx();
            String sql = "SELECT o.titre FROM offre_emploi o "
                    + "JOIN candidature c ON c.id_offre = o.id_offre "
                    + "WHERE c.id_candidature = ?";
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, currentEntretien.getIdCandidature());
            java.sql.ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getString("titre");
        } catch (Exception e) {
            System.err.println("Erreur obtenirNomPoste: " + e.getMessage());
        }
        return "Poste non défini";
    }

    private String obtenirNiveauExperience() {
        try {
            java.sql.Connection conn = utilis.MyConnection.getInstance().getCnx();
            String sql = "SELECT c.niveau_experience, c.annees_experience FROM candidature c "
                    + "WHERE c.id_candidature = ?";
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, currentEntretien.getIdCandidature());
            java.sql.ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                String niveau = rs.getString("niveau_experience");
                int annees    = rs.getInt("annees_experience");
                if (niveau != null) return niveau + (annees > 0 ? " (" + annees + " ans)" : "");
            }
        } catch (Exception e) {
            System.err.println("Erreur obtenirNiveauExperience: " + e.getMessage());
        }
        return "";
    }

    private String obtenirDomaineExperience() {
        try {
            java.sql.Connection conn = utilis.MyConnection.getInstance().getCnx();
            String sql = "SELECT c.domaine_experience FROM candidature c WHERE c.id_candidature = ?";
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, currentEntretien.getIdCandidature());
            java.sql.ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                String domaine = rs.getString("domaine_experience");
                return domaine != null ? domaine : "";
            }
        } catch (Exception e) {
            System.err.println("Erreur obtenirDomaineExperience: " + e.getMessage());
        }
        return "";
    }

    /**
     * Méthode pour pré-remplir le formulaire avec un ID entretien
     * Appelée depuis le bouton "Évaluer" dans le tableau
     */
    // ========== CHARGER LES ENTRETIENS TERMINÉS ==========
    private void chargerEntretiensTermines() {
        try {
            java.sql.Connection conn = utilis.MyConnection.getInstance().getCnx();
            String sql = "SELECT e.id_entretien, e.type_entretien, e.date_entretien, "
                    + "u.nom, u.prenom FROM entretien e "
                    + "JOIN candidature c ON c.id_candidature = e.id_candidature "
                    + "JOIN utilisateur u ON u.id_utilisateur = c.id_utilisateur "
                    + "WHERE LOWER(e.statut_entretien) IN ('termine', 'terminé') "
                    + "ORDER BY e.date_entretien DESC";
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            java.sql.ResultSet rs = pst.executeQuery();

            java.util.List<String> items = new java.util.ArrayList<>();
            entretienMap.clear();
            while (rs.next()) {
                int id = rs.getInt("id_entretien");
                String label = String.format("#%d — %s %s (%s) — %s",
                        id,
                        rs.getString("prenom"),
                        rs.getString("nom"),
                        rs.getString("type_entretien"),
                        rs.getDate("date_entretien"));
                items.add(label);
                entretienMap.put(label, id);
            }
            if (entretienComboField != null) {
                entretienComboField.setItems(FXCollections.observableArrayList(items));
            }
        } catch (Exception e) {
            System.err.println("Erreur chargerEntretiensTermines: " + e.getMessage());
        }
    }

    public void setEntretienToEvaluate(int idEntretien) {
        // Mode "évaluer depuis le bouton ⭐" — cacher le ComboBox
        if (entretienSelectBox != null) {
            entretienSelectBox.setVisible(false);
            entretienSelectBox.setManaged(false);
        }
        idEntretienField.setText(String.valueOf(idEntretien));
        currentEntretien = entretienService.getEntretienById(idEntretien);
        if (currentEntretien != null) {
            entretienInfoLabel.setText(String.format(
                    "Entretien %s - Date: %s à %s - Lieu: %s",
                    currentEntretien.getTypeEntretien(),
                    currentEntretien.getDateEntretien(),
                    currentEntretien.getHeureEntretien(),
                    currentEntretien.getLieu()
            ));
            if (currentEntretien.getQuestionsEntretien() != null && !currentEntretien.getQuestionsEntretien().trim().isEmpty()) {
                afficherQuestionsIA(currentEntretien.getQuestionsEntretien());
            } else {
                afficherQuestionsExemples(currentEntretien.getTypeEntretien());
            }
        }
        decisionField.setValue("En attente");
    }

    public void setEvaluation(EvaluationEntretien evaluation) {
        this.currentEvaluation = evaluation;

        if (evaluation != null) {
            // Mode édition
            idEntretienField.setText(String.valueOf(evaluation.getIdEntretien()));
            scoreField.setText(String.valueOf(evaluation.getScoreTest()));
            noteField.setText(String.valueOf(evaluation.getNoteEntretien()));
            commentaireField.setText(evaluation.getCommentaire());
            decisionField.setValue(evaluation.getDecision());

            // Sélectionner les ratings
            selectRating(techGroup, evaluation.getCompetencesTechniques());
            selectRating(compGroup, evaluation.getCompetencesComportementales());
            selectRating(commGroup, evaluation.getCommunication());
            selectRating(motivGroup, evaluation.getMotivation());
            selectRating(expGroup, evaluation.getExperience());

            // Charger les infos de l'entretien
            currentEntretien = entretienService.getEntretienById(evaluation.getIdEntretien());
            if (currentEntretien != null) {
                entretienInfoLabel.setText(String.format(
                        "Entretien %s - Date: %s",
                        currentEntretien.getTypeEntretien(),
                        currentEntretien.getDateEntretien()
                ));
                afficherQuestionsExemples(currentEntretien.getTypeEntretien());
            }
        }
    }

    private void selectRating(ToggleGroup group, int rating) {
        if (rating >= 1 && rating <= 5) {
            for (Toggle toggle : group.getToggles()) {
                ToggleButton btn = (ToggleButton) toggle;
                if (btn.getUserData().equals(String.valueOf(rating))) {
                    btn.setSelected(true);
                    break;
                }
            }
        }
    }

    // ========== VALIDATION EN TEMPS RÉEL ==========

    private boolean validateScore() {
        String txt = scoreField.getText().trim();
        if (txt.isEmpty()) {
            setFieldError(scoreField, scoreError, "⚠ Le score est obligatoire");
            return false;
        }
        try {
            double val = Double.parseDouble(txt);
            if (val < 0 || val > 100) {
                setFieldError(scoreField, scoreError, "⚠ Le score doit être entre 0 et 100");
                return false;
            }
        } catch (NumberFormatException e) {
            setFieldError(scoreField, scoreError, "⚠ Format invalide (ex: 85.5)");
            return false;
        }
        setFieldSuccess(scoreField, scoreError, "✓ Score valide");
        return true;
    }

    private boolean validateNote() {
        String txt = noteField.getText().trim();
        if (txt.isEmpty()) {
            setFieldError(noteField, noteError, "⚠ La note est obligatoire");
            return false;
        }
        try {
            int val = Integer.parseInt(txt);
            if (val < 1 || val > 5) {
                setFieldError(noteField, noteError, "⚠ La note doit être entre 1 et 5");
                return false;
            }
        } catch (NumberFormatException e) {
            setFieldError(noteField, noteError, "⚠ Format invalide (entier entre 1 et 5)");
            return false;
        }
        setFieldSuccess(noteField, noteError, "✓ Note valide");
        return true;
    }

    private boolean validateCommentaire() {
        String txt = commentaireField.getText().trim();
        if (txt.isEmpty()) {
            setAreaError(commentaireField, commentaireError, "⚠ Le commentaire est obligatoire");
            return false;
        } else if (txt.length() < 10) {
            setAreaError(commentaireField, commentaireError, "⚠ Minimum 10 caractères (" + txt.length() + "/10)");
            return false;
        } else if (txt.length() > 500) {
            setAreaError(commentaireField, commentaireError, "⚠ Maximum 500 caractères dépassé");
            return false;
        }
        setAreaSuccess(commentaireField, commentaireError, "✓ Commentaire valide");
        return true;
    }

    private boolean validateDecision() {
        if (decisionField.getValue() == null || decisionField.getValue().trim().isEmpty()) {
            if (decisionError != null) {
                decisionError.setText("⚠ La décision est obligatoire");
                decisionError.setVisible(true);
                decisionError.setManaged(true);
            }
            return false;
        }
        if (decisionError != null) {
            decisionError.setText("✓ Décision sélectionnée");
            decisionError.setStyle("-fx-text-fill: #34D399; -fx-font-size: 11px; -fx-font-weight: 600; -fx-padding: 2 0 0 4;");
            decisionError.setVisible(true);
            decisionError.setManaged(true);
        }
        return true;
    }

    private void setFieldError(TextField field, Label label, String msg) {
        field.setStyle("-fx-background-color: rgba(248,113,113,0.1); -fx-text-fill: white; -fx-border-color: #F87171; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 12 15; -fx-font-size: 14px;");
        if (label != null) {
            label.setText(msg);
            label.setStyle("-fx-text-fill: #F87171; -fx-font-size: 11px; -fx-font-weight: 600; -fx-padding: 2 0 0 4;");
            label.setVisible(true);
            label.setManaged(true);
        }
    }

    private void setFieldSuccess(TextField field, Label label, String msg) {
        field.setStyle("-fx-background-color: rgba(52,211,153,0.1); -fx-text-fill: white; -fx-border-color: #34D399; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 12 15; -fx-font-size: 14px;");
        if (label != null) {
            label.setText(msg);
            label.setStyle("-fx-text-fill: #34D399; -fx-font-size: 11px; -fx-font-weight: 600; -fx-padding: 2 0 0 4;");
            label.setVisible(true);
            label.setManaged(true);
        }
    }

    private void setAreaError(TextArea area, Label label, String msg) {
        area.setStyle("-fx-background-color: #2D3748; -fx-text-fill: white; -fx-border-color: #F87171; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 12 15; -fx-font-size: 14px; -fx-control-inner-background: #2D3748;");
        if (label != null) {
            label.setText(msg);
            label.setStyle("-fx-text-fill: #F87171; -fx-font-size: 11px; -fx-font-weight: 600; -fx-padding: 2 0 0 4;");
            label.setVisible(true);
            label.setManaged(true);
        }
    }

    private void setAreaSuccess(TextArea area, Label label, String msg) {
        area.setStyle("-fx-background-color: #2D3748; -fx-text-fill: white; -fx-border-color: #34D399; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 12 15; -fx-font-size: 14px; -fx-control-inner-background: #2D3748;");
        if (label != null) {
            label.setText(msg);
            label.setStyle("-fx-text-fill: #34D399; -fx-font-size: 11px; -fx-font-weight: 600; -fx-padding: 2 0 0 4;");
            label.setVisible(true);
            label.setManaged(true);
        }
    }

    // ========== CALCUL AUTOMATIQUE DE LA NOTE GLOBALE ==========
    private void calculerNoteGlobale() {
        int tech  = getSelectedRating(techGroup);
        int comp  = getSelectedRating(compGroup);
        int comm  = getSelectedRating(commGroup);
        int motiv = getSelectedRating(motivGroup);
        int exp   = getSelectedRating(expGroup);

        int nbNotes = 0;
        int total   = 0;
        if (tech  > 0) { total += tech;  nbNotes++; }
        if (comp  > 0) { total += comp;  nbNotes++; }
        if (comm  > 0) { total += comm;  nbNotes++; }
        if (motiv > 0) { total += motiv; nbNotes++; }
        if (exp   > 0) { total += exp;   nbNotes++; }

        if (nbNotes > 0) {
            int moyenne = (int) Math.round((double) total / nbNotes);
            noteField.setText(String.valueOf(moyenne));
            noteField.setStyle("-fx-background-color: rgba(16,185,129,0.15); "
                    + "-fx-text-fill: #34D399; "
                    + "-fx-border-color: rgba(52,211,153,0.4); "
                    + "-fx-border-radius: 10; -fx-background-radius: 10; "
                    + "-fx-padding: 12 15; -fx-font-size: 14px; -fx-font-weight: bold;");
        } else {
            noteField.setText("");
            noteField.setStyle("");
        }
    }

    private int getSelectedRating(ToggleGroup group) {
        Toggle selected = group.getSelectedToggle();
        if (selected != null) {
            return Integer.parseInt((String) ((ToggleButton) selected).getUserData());
        }
        return 0;
    }

    @FXML
    private void handleSave() {
        // ===== VALIDATION INLINE - AUCUN POPUP =====
        boolean valid = true;

        // 1. Critères de notation (toggle buttons)
        int tech  = getSelectedRating(techGroup);
        int comp  = getSelectedRating(compGroup);
        int comm  = getSelectedRating(commGroup);
        int motiv = getSelectedRating(motivGroup);
        int exp   = getSelectedRating(expGroup);

        if (tech == 0 || comp == 0 || comm == 0 || motiv == 0 || exp == 0) {
            // Mettre en rouge les critères non notés
            highlightMissingCriteria(tech, comp, comm, motiv, exp);
            valid = false;
        }

        // 2. Score
        if (!validateScore()) valid = false;

        // 3. Note
        if (!validateNote()) valid = false;

        // 4. Commentaire
        if (!validateCommentaire()) valid = false;

        // 5. Décision
        if (!validateDecision()) valid = false;

        if (!valid) return; // Arrêter ici si erreurs

        // ===== SAUVEGARDE =====
        try {
            // Vérifier que l'entretien est sélectionné (mode ajout via ComboBox)
            if (idEntretienField.getText().trim().isEmpty()) {
                if (entretienSelectError != null) {
                    entretienSelectError.setText("⚠ Veuillez sélectionner un entretien");
                    entretienSelectError.setVisible(true);
                    entretienSelectError.setManaged(true);
                }
                return;
            }

            double score       = Double.parseDouble(scoreField.getText().trim());
            int    note        = Integer.parseInt(noteField.getText().trim());
            String commentaire = commentaireField.getText().trim();
            String decision    = decisionField.getValue();
            int    idEntretien = Integer.parseInt(idEntretienField.getText().trim());

            if (currentEvaluation == null) {
                EvaluationEntretien newEval = new EvaluationEntretien(
                        score, note, commentaire, decision, idEntretien,
                        tech, comp, comm, motiv, exp
                );
                evaluationService.addEvaluation(newEval);
            } else {
                currentEvaluation.setScoreTest(score);
                currentEvaluation.setNoteEntretien(note);
                currentEvaluation.setCommentaire(commentaire);
                currentEvaluation.setDecision(decision);
                currentEvaluation.setIdEntretien(idEntretien);
                currentEvaluation.setCompetencesTechniques(tech);
                currentEvaluation.setCompetencesComportementales(comp);
                currentEvaluation.setCommunication(comm);
                currentEvaluation.setMotivation(motiv);
                currentEvaluation.setExperience(exp);
                evaluationService.updateEvaluation(currentEvaluation);
            }

            saved = true;
            closeWindow();

        } catch (Exception e) {
            // Afficher l'erreur inline dans le champ score (premier champ)
            setFieldError(scoreField, scoreError, "⚠ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Mettre en surbrillance rouge les critères non notés
    private void highlightMissingCriteria(int tech, int comp, int comm, int motiv, int exp) {
        String missing = "⚠ Critères non notés : ";
        java.util.List<String> manquants = new java.util.ArrayList<>();
        if (tech  == 0) manquants.add("Compétences Techniques");
        if (comp  == 0) manquants.add("Compétences Comportementales");
        if (comm  == 0) manquants.add("Communication");
        if (motiv == 0) manquants.add("Motivation");
        if (exp   == 0) manquants.add("Expérience");

        // Réutiliser scoreError pour afficher ce message global
        if (scoreError != null) {
            scoreError.setText(missing + String.join(", ", manquants));
            scoreError.setStyle("-fx-text-fill: #F87171; -fx-font-size: 11px; -fx-font-weight: 600; -fx-padding: 2 0 0 4;");
            scoreError.setVisible(true);
            scoreError.setManaged(true);
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) scoreField.getScene().getWindow();
        stage.close();
    }

    public boolean isSaved() {
        return saved;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}