package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.GridPane;
import org.example.entities.OffreEmploi;
import org.example.entities.CritereOffre;
import org.example.services.OffreEmploiService;
import org.example.services.CritereOffreService;
import javafx.geometry.Pos;
import javafx.geometry.Orientation;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.control.Separator;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.ParallelTransition;
import javafx.util.Duration;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Line;
import javafx.scene.paint.Color;
import javafx.scene.Group;
import javafx.stage.Screen;
import javafx.stage.Window;

import java.util.List;

public class OffresController {

    @FXML
    private StackPane mainStack;
    
    @FXML
    private VBox gridView;
    
    @FXML
    private HBox detailView;
    
    @FXML
    private FlowPane cardsContainer;
    
    @FXML
    private VBox jobsList;
    
    @FXML
    private VBox jobDetailPanel;
    
    @FXML
    private ScrollPane jobsListScroll;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private Label heroSubtitle;
    
    @FXML
    private Label countLabel;
    
    // Filter components
    @FXML
    private CheckBox filterRemote;
    @FXML
    private CheckBox filterHybrid;
    @FXML
    private CheckBox filterOnSite;
    @FXML
    private CheckBox filterCDI;
    @FXML
    private CheckBox filterFreelance;
    @FXML
    private CheckBox filterStage;
    @FXML
    private CheckBox filterAWS;
    @FXML
    private CheckBox filterNodeJS;
    @FXML
    private CheckBox filterPython;
    @FXML
    private CheckBox filterReact;
    @FXML
    private CheckBox filterJavaScript;
    @FXML
    private Button applyFiltersBtn;

    private final OffreEmploiService service = new OffreEmploiService();
    private final CritereOffreService critereService = new CritereOffreService();
    private List<OffreEmploi> allOffres;
    private OffreEmploi selectedOffre;

    /**
     * Méthode d'initialisation du contrôleur JavaFX.
     * Cette méthode est appelée automatiquement après le chargement du fichier FXML.
     * Elle charge toutes les offres d'emploi disponibles et ajoute des listeners.
     */
    @FXML
    public void initialize() {
        loadOffres();
        
        // Add search field listener to filter in real-time
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterAndUpdateCount();
            });
        }
        
        // Add filter button listener
        if (applyFiltersBtn != null) {
            applyFiltersBtn.setOnAction(e -> applyFilters());
        }
    }

    /**
     * Charge toutes les offres d'emploi depuis la base de données.
     * Cette méthode vide le conteneur de cartes et crée une nouvelle carte pour chaque offre.
     * Les cartes sont affichées dans une grille (FlowPane).
     */
    private void loadOffres() {

        cardsContainer.getChildren().clear();

        allOffres = service.getAllOffres();

        for (OffreEmploi offre : allOffres) {
            VBox card = createCard(offre);
            cardsContainer.getChildren().add(card);
        }
        
        // Update count label
        updateCountLabel(allOffres.size());
    }

    /**
     * Crée une carte visuelle pour une offre d'emploi dans la vue grille.
     * La carte contient l'icône, le type de contrat, le titre, la description,
     * le statut et un bouton "Postuler".
     * 
     * @param offre L'objet OffreEmploi contenant les détails de l'offre à afficher
     * @return Une VBox stylisée représentant la carte de l'offre
     */
    private VBox createCard(OffreEmploi offre) {

        VBox card = new VBox(12);
        card.getStyleClass().add("job-card");
        
        // Add click handler to show detail view
        card.setOnMouseClicked(e -> showDetailView(offre));

        // ===== HEADER =====
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        // Create icon with image
        StackPane icon = new StackPane();
        try {
            ImageView iconImage = new ImageView(new Image(getClass().getResourceAsStream("/org/example/images/job-icon.png")));
            iconImage.setFitWidth(28);
            iconImage.setFitHeight(28);
            iconImage.setPreserveRatio(true);
            icon.getChildren().add(iconImage);
        } catch (Exception e) {
            // Fallback to programmatic briefcase icon
            Group briefcase = createBriefcaseIcon(24);
            icon.getChildren().add(briefcase);
        }
        icon.getStyleClass().add("job-icon");

        Label type = new Label(offre.getTypeContrat());
        type.getStyleClass().add("job-type-badge");

        header.getChildren().addAll(icon, type);

        // ===== TITLE =====
        Label title = new Label(offre.getTitre());
        title.getStyleClass().add("job-title");

        // ===== FOOTER =====
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);

        // Work preference icon
        String workPrefIcon = "🏢";
        if (offre.getWorkPreference() != null) {
            switch (offre.getWorkPreference()) {
                case "Remote":
                    workPrefIcon = "🏠";
                    break;
                case "Hybrid":
                    workPrefIcon = "🔄";
                    break;
                default:
                    workPrefIcon = "🏢";
            }
        }
        Label workPref = new Label(workPrefIcon + " " + (offre.getWorkPreference() != null ? offre.getWorkPreference() : "On-site"));
        workPref.getStyleClass().add("location-text");
        workPref.setMaxWidth(80);
        workPref.setStyle(workPref.getStyle() + "; -fx-text-overrun: ellipsis;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button applyBtn = new Button("Postuler");
        applyBtn.getStyleClass().add("apply-btn");
        applyBtn.setOnAction(e -> {
            e.consume(); // Prevent card click
            // Handle apply action
        });

        footer.getChildren().addAll(workPref, spacer, applyBtn);

        Separator separator = new Separator();
        separator.getStyleClass().add("card-divider");

        card.getChildren().addAll(header, title, separator, footer);

        return card;
    }
    
    /**
     * Crée une carte compacte pour la barre latérale de la vue détaillée.
     * Cette carte affiche une version simplifiée de l'offre avec l'icône,
     * le type de contrat, le titre et le statut. La carte sélectionnée est mise en évidence.
     * 
     * @param offre L'objet OffreEmploi à afficher dans la barre latérale
     * @return Une VBox représentant la carte compacte de la barre latérale
     */
    private VBox createSidebarCard(OffreEmploi offre) {
        VBox card = new VBox(10);
        card.getStyleClass().add("sidebar-job-card");
        card.setMaxWidth(308);
        card.setPrefWidth(308);
        
        if (selectedOffre != null && selectedOffre.getIdOffre() == offre.getIdOffre()) {
            card.getStyleClass().add("sidebar-job-card-selected");
        }
        
        card.setOnMouseClicked(e -> {
            selectedOffre = offre;
            showDetailContent(offre);
            refreshSidebarSelection();
        });

        // Header with icon and badge
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        // Create icon with image
        StackPane icon = new StackPane();
        try {
            ImageView iconImage = new ImageView(new Image(getClass().getResourceAsStream("/org/example/images/job-icon.png")));
            iconImage.setFitWidth(24);
            iconImage.setFitHeight(24);
            iconImage.setPreserveRatio(true);
            icon.getChildren().add(iconImage);
        } catch (Exception e) {
            // Fallback to programmatic briefcase icon
            Group briefcase = createBriefcaseIcon(20);
            icon.getChildren().add(briefcase);
        }
        icon.setStyle("-fx-background-color: linear-gradient(to bottom right, #eef2ff 0%, #ddd6fe 100%); -fx-background-radius: 12; -fx-padding: 8; -fx-min-width: 48; -fx-min-height: 48; -fx-alignment: center;");

        Label type = new Label(offre.getTypeContrat());
        type.getStyleClass().add("sidebar-type-badge");

        header.getChildren().addAll(icon, type);

        // Title
        Label title = new Label(offre.getTitre());
        title.getStyleClass().add("sidebar-job-title");
        title.setWrapText(true);
        title.setMaxWidth(290);

        // Tags removed - only show in detail view
        
        // Footer with work preference
        HBox footerInfo = new HBox(10);
        footerInfo.setAlignment(Pos.CENTER_LEFT);
        
        // Work preference
        String workPrefIcon = "🏢";
        if (offre.getWorkPreference() != null) {
            switch (offre.getWorkPreference()) {
                case "Remote":
                    workPrefIcon = "🏠";
                    break;
                case "Hybrid":
                    workPrefIcon = "🔄";
                    break;
                default:
                    workPrefIcon = "🏢";
            }
        }
        Label workPref = new Label(workPrefIcon + " " + (offre.getWorkPreference() != null ? offre.getWorkPreference() : "On-site"));
        workPref.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8; -fx-text-overrun: ellipsis;");
        workPref.setMaxWidth(100);
        
        footerInfo.getChildren().addAll(workPref);

        card.getChildren().addAll(header, title, footerInfo);

        return card;
    }
    
    /**
     * Affiche la vue détaillée pour une offre d'emploi sélectionnée.
     * Cette méthode remplit la barre latérale avec toutes les offres, affiche
     * les détails de l'offre sélectionnée et anime la transition depuis la vue grille.
     * 
     * @param offre L'OffreEmploi à afficher en détail
     */
    private void showDetailView(OffreEmploi offre) {
        selectedOffre = offre;
        
        // Populate sidebar with all jobs
        jobsList.getChildren().clear();
        for (OffreEmploi o : allOffres) {
            VBox sidebarCard = createSidebarCard(o);
            jobsList.getChildren().add(sidebarCard);
        }
        
        // Show detail content
        showDetailContent(offre);
        
        // Animate transition
        animateToDetailView();
    }
    
    /**
     * Affiche le contenu détaillé d'une offre d'emploi dans le panneau de détails.
     * Cette méthode récupère les critères de l'offre depuis la base de données et
     * affiche toutes les informations : titre, description, type de contrat, statut,
     * niveau d'expérience, niveau d'étude et compétences requises.
     * 
     * @param offre L'OffreEmploi dont afficher les détails
     */
    private void showDetailContent(OffreEmploi offre) {
        jobDetailPanel.getChildren().clear();
        
        // Fetch criteria from database
        List<CritereOffre> criteres = critereService.getByOffreId(offre.getIdOffre());
        
        // Header with title centered
        VBox topHeader = new VBox(16);
        topHeader.setAlignment(Pos.CENTER);
        topHeader.setStyle("-fx-padding: 0 0 20 0;");
        
        Label jobTitle = new Label(offre.getTitre());
        jobTitle.getStyleClass().add("detail-job-title");
        jobTitle.setStyle("-fx-text-alignment: center;");
        
        // Metadata information centered with descriptive phrases
        // Create a modern grid layout for criteria (2 rows x 3 columns)
        javafx.scene.layout.GridPane metaGrid = new javafx.scene.layout.GridPane();
        metaGrid.setHgap(25);
        metaGrid.setVgap(20);
        metaGrid.setAlignment(Pos.CENTER);
        metaGrid.setStyle("-fx-padding: 20 0; -fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-padding: 20;");
        
        // Contract Type
        VBox contractBox = new VBox(6);
        contractBox.setAlignment(Pos.CENTER_LEFT);
        contractBox.setMinWidth(150);
        Label contractLabel = new Label("📋 Type de contrat");
        contractLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: 600;");
        Label contractValue = new Label(offre.getTypeContrat());
        contractValue.setStyle("-fx-font-size: 15px; -fx-text-fill: #1e293b; -fx-font-weight: 700;");
        contractValue.setWrapText(true);
        contractBox.getChildren().addAll(contractLabel, contractValue);
        
        // Work Preference
        VBox workPrefBox = new VBox(6);
        workPrefBox.setAlignment(Pos.CENTER_LEFT);
        workPrefBox.setMinWidth(150);
        String workPrefIcon = "🏢";
        String workPrefValue = offre.getWorkPreference() != null ? offre.getWorkPreference() : "On-site";
        if (offre.getWorkPreference() != null) {
            switch (offre.getWorkPreference()) {
                case "Remote": workPrefIcon = "🏠"; break;
                case "Hybrid": workPrefIcon = "🔄"; break;
            }
        }
        Label workPrefLabel = new Label(workPrefIcon + " Mode de travail");
        workPrefLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: 600;");
        Label workPrefText = new Label(workPrefValue);
        workPrefText.setStyle("-fx-font-size: 15px; -fx-text-fill: #1e293b; -fx-font-weight: 700;");
        workPrefText.setWrapText(true);
        workPrefBox.getChildren().addAll(workPrefLabel, workPrefText);
        
        // Location
        VBox lieuBox = new VBox(6);
        lieuBox.setAlignment(Pos.CENTER_LEFT);
        lieuBox.setMinWidth(150);
        Label lieuLabel = new Label("📍 Lieu");
        lieuLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: 600;");
        String lieuValue = (offre.getLieu() != null && !offre.getLieu().isEmpty()) ? offre.getLieu() : "Non spécifié";
        Label lieuText = new Label(lieuValue);
        lieuText.setStyle("-fx-font-size: 15px; -fx-text-fill: #1e293b; -fx-font-weight: 700;");
        lieuText.setWrapText(true);
        lieuBox.getChildren().addAll(lieuLabel, lieuText);
        
        // Status
        VBox statusBox = new VBox(6);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusBox.setMinWidth(150);
        String statusIcon = offre.getStatutOffre().equalsIgnoreCase("ACTIVE") ? "✅" : "⏸";
        Label statusLabel = new Label(statusIcon + " Statut");
        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: 600;");
        Label statusValue = new Label(offre.getStatutOffre());
        statusValue.setStyle("-fx-font-size: 15px; -fx-text-fill: " + 
            (offre.getStatutOffre().equalsIgnoreCase("ACTIVE") ? "#10b981" : "#ef4444") + "; -fx-font-weight: 700;");
        statusValue.setWrapText(true);
        statusBox.getChildren().addAll(statusLabel, statusValue);
        
        // Publication Date
        VBox dateBox = new VBox(6);
        dateBox.setAlignment(Pos.CENTER_LEFT);
        dateBox.setMinWidth(150);
        Label dateLabel = new Label("📅 Publiée le");
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: 600;");
        String dateValue = "Non spécifiée";
        if (offre.getDatePublication() != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy");
            dateValue = sdf.format(offre.getDatePublication());
        }
        Label dateText = new Label(dateValue);
        dateText.setStyle("-fx-font-size: 15px; -fx-text-fill: #1e293b; -fx-font-weight: 700;");
        dateText.setWrapText(true);
        dateBox.getChildren().addAll(dateLabel, dateText);
        
        // Company (placeholder - can be added later)
        VBox companyBox = new VBox(6);
        companyBox.setAlignment(Pos.CENTER_LEFT);
        companyBox.setMinWidth(150);
        Label companyLabel = new Label("🏢 Entreprise");
        companyLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: 600;");
        Label companyValue = new Label("VOS Platform");
        companyValue.setStyle("-fx-font-size: 15px; -fx-text-fill: #1e293b; -fx-font-weight: 700;");
        companyValue.setWrapText(true);
        companyBox.getChildren().addAll(companyLabel, companyValue);
        
        // Add to grid: Row 0
        metaGrid.add(contractBox, 0, 0);
        metaGrid.add(workPrefBox, 1, 0);
        metaGrid.add(lieuBox, 2, 0);
        
        // Add to grid: Row 1
        metaGrid.add(statusBox, 0, 1);
        metaGrid.add(dateBox, 1, 1);
        metaGrid.add(companyBox, 2, 1);
        
        VBox metaInfo = new VBox(metaGrid);
        metaInfo.setAlignment(Pos.CENTER);
        
        topHeader.getChildren().addAll(jobTitle, metaInfo);
        
        // Apply button (aligned to the right)
        Button applyBtn = new Button("Postuler");
        applyBtn.getStyleClass().add("detail-apply-btn");
        
        HBox buttonContainer = new HBox();
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);
        buttonContainer.setStyle("-fx-padding: 10 0;");
        buttonContainer.getChildren().add(applyBtn);
        
        // Separator
        Separator divider = new Separator();
        divider.setStyle("-fx-padding: 15 0;");
        
        // Section: À propos du poste
        Label aboutTitle = new Label("À propos du poste");
        aboutTitle.getStyleClass().add("detail-section-title");
        
        Label aboutText = new Label(offre.getDescription());
        aboutText.setWrapText(true);
        aboutText.getStyleClass().add("detail-section-text");
        aboutText.setStyle("-fx-padding: 0 0 10 0;");
        
        // Add to panel
        jobDetailPanel.getChildren().addAll(topHeader, buttonContainer, divider, aboutTitle, aboutText);
        
        // Show criteria from database if available
        if (!criteres.isEmpty()) {
            for (CritereOffre critere : criteres) {
                
                // Niveau d'expérience
                if (critere.getNiveauExperience() != null && !critere.getNiveauExperience().trim().isEmpty()) {
                    Label expTitle = new Label("Niveau d'expérience");
                    expTitle.getStyleClass().add("detail-section-title");
                    expTitle.setStyle("-fx-padding: 20 0 10 0;");
                    
                    HBox expBox = new HBox(12);
                    expBox.setAlignment(Pos.CENTER_LEFT);
                    expBox.getStyleClass().add("detail-criteria-item");
                    
                    Label expIcon = new Label("★");
                    expIcon.setStyle("-fx-font-size: 18px; -fx-text-fill: #fbbf24; -fx-font-weight: bold;");
                    
                    Label expText = new Label(critere.getNiveauExperience());
                    expText.setWrapText(true);
                    expText.getStyleClass().add("detail-criteria-text");
                    HBox.setHgrow(expText, Priority.ALWAYS);
                    
                    expBox.getChildren().addAll(expIcon, expText);
                    jobDetailPanel.getChildren().addAll(expTitle, expBox);
                }
                
                // Niveau d'étude
                if (critere.getNiveauEtude() != null && !critere.getNiveauEtude().trim().isEmpty()) {
                    Label eduTitle = new Label("Niveau d'étude");
                    eduTitle.getStyleClass().add("detail-section-title");
                    eduTitle.setStyle("-fx-padding: 20 0 10 0;");
                    
                    HBox eduBox = new HBox(12);
                    eduBox.setAlignment(Pos.CENTER_LEFT);
                    eduBox.getStyleClass().add("detail-criteria-item");
                    
                    Label eduIcon = new Label("🎓");
                    eduIcon.setStyle("-fx-font-size: 18px;");
                    
                    Label eduText = new Label(critere.getNiveauEtude());
                    eduText.setWrapText(true);
                    eduText.getStyleClass().add("detail-criteria-text");
                    HBox.setHgrow(eduText, Priority.ALWAYS);
                    
                    eduBox.getChildren().addAll(eduIcon, eduText);
                    jobDetailPanel.getChildren().addAll(eduTitle, eduBox);
                }
                
                // Compétences requises
                if (critere.getCompetencesRequises() != null && !critere.getCompetencesRequises().trim().isEmpty()) {
                    Label compTitle = new Label("Compétences requises");
                    compTitle.getStyleClass().add("detail-section-title");
                    compTitle.setStyle("-fx-padding: 20 0 10 0;");
                    
                    VBox compContainer = new VBox(10);
                    String[] competences = critere.getCompetencesRequises().split(",");
                    for (String comp : competences) {
                        HBox compBox = new HBox(12);
                        compBox.setAlignment(Pos.CENTER_LEFT);
                        compBox.getStyleClass().add("detail-criteria-item");
                        
                        Label compIcon = new Label("✓");
                        compIcon.setStyle("-fx-font-size: 16px; -fx-text-fill: #6366f1; -fx-font-weight: bold;");
                        
                        Label compText = new Label(comp.trim());
                        compText.setWrapText(true);
                        compText.getStyleClass().add("detail-criteria-text");
                        HBox.setHgrow(compText, Priority.ALWAYS);
                        
                        compBox.getChildren().addAll(compIcon, compText);
                        compContainer.getChildren().add(compBox);
                    }
                    
                    jobDetailPanel.getChildren().addAll(compTitle, compContainer);
                }
            }
        }
    }
    
    /**
     * Actualise l'affichage de la barre latérale pour refléter la sélection actuelle.
     * Cette méthode recrée toutes les cartes de la barre latérale, en mettant en évidence
     * l'offre actuellement sélectionnée.
     */
    private void refreshSidebarSelection() {
        jobsList.getChildren().clear();
        for (OffreEmploi o : allOffres) {
            VBox sidebarCard = createSidebarCard(o);
            jobsList.getChildren().add(sidebarCard);
        }
    }
    
    /**
     * Anime la transition de la vue grille vers la vue détaillée.
     * Cette méthode crée un effet de fondu sortant pour la vue grille,
     * puis un effet de fondu entrant et de glissement depuis la droite
     * pour la vue détaillée.
     */
    private void animateToDetailView() {
        // Fade out grid view
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), gridView);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        
        fadeOut.setOnFinished(e -> {
            gridView.setVisible(false);
            gridView.setManaged(false);
            
            detailView.setVisible(true);
            detailView.setManaged(true);
            detailView.setOpacity(0);
            
            // Slide in from right
            detailView.setTranslateX(100);
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), detailView);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            
            TranslateTransition slide = new TranslateTransition(Duration.millis(300), detailView);
            slide.setFromX(100);
            slide.setToX(0);
            
            ParallelTransition parallel = new ParallelTransition(fadeIn, slide);
            parallel.play();
        });
        
        fadeOut.play();
    }
    
    /**
     * Crée une icône de porte-documents (briefcase) de manière programmatique.
     * Cette icône est utilisée comme solution de secours si l'image n'est pas disponible.
     * L'icône est constituée d'un corps rectangulaire arrondi, d'une poignée et d'un verrou.
     * 
     * @param size La taille de l'icône en pixels
     * @return Un Group JavaFX contenant les éléments graphiques de l'icône
     */
    private Group createBriefcaseIcon(double size) {
        Group group = new Group();
        
        // Main briefcase body
        Rectangle body = new Rectangle(0, size * 0.25, size, size * 0.65);
        body.setArcWidth(size * 0.15);
        body.setArcHeight(size * 0.15);
        body.setFill(Color.web("#6366f1"));
        body.setStroke(Color.web("#ffffff"));
        body.setStrokeWidth(size * 0.08);
        
        // Handle
        Rectangle handle = new Rectangle(size * 0.35, 0, size * 0.3, size * 0.3);
        handle.setArcWidth(size * 0.2);
        handle.setArcHeight(size * 0.2);
        handle.setFill(Color.TRANSPARENT);
        handle.setStroke(Color.web("#6366f1"));
        handle.setStrokeWidth(size * 0.08);
        
        // Lock detail
        Rectangle lock = new Rectangle(size * 0.42, size * 0.55, size * 0.16, size * 0.2);
        lock.setArcWidth(size * 0.08);
        lock.setArcHeight(size * 0.08);
        lock.setFill(Color.web("#ffffff"));
        
        group.getChildren().addAll(body, handle, lock);
        return group;
    }
    
    /**
     * Retourne à la vue grille depuis la vue détaillée.
     * Cette méthode anime la transition avec un effet de fondu sortant pour
     * la vue détaillée, puis un effet de fondu entrant pour la vue grille.
     * Méthode liée au bouton de retour dans l'interface FXML.
     */
    @FXML
    private void backToGrid() {
        // Fade out detail view
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), detailView);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        
        fadeOut.setOnFinished(e -> {
            detailView.setVisible(false);
            detailView.setManaged(false);
            
            gridView.setVisible(true);
            gridView.setManaged(true);
            gridView.setOpacity(0);
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), gridView);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        
        fadeOut.play();
    }
    
    /**
     * Affiche une boîte de dialogue de filtres avancés pour filtrer les offres d'emploi.
     * Filtrage automatique en temps réel.
     */
    @FXML
    public void showFilterDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("🔍 Filtres Intelligents");
        dialog.setHeaderText(null);
        
        // Create scrollable content with modern styling
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f8fafc; -fx-background: #f8fafc;");
        scrollPane.setPadding(new javafx.geometry.Insets(0));
        
        VBox content = new VBox(14);
        content.setStyle("-fx-padding: 18; -fx-background-color: #f8fafc;");
        
        // Header with description
        Label headerDesc = new Label("Sélectionnez vos critères - Les résultats se mettent à jour automatiquement");
        headerDesc.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-style: italic; -fx-padding: 0 0 10 0;");
        headerDesc.setWrapText(true);
        content.getChildren().add(headerDesc);
        
        // Filter by contract type section
        VBox contractBox = createModernFilterSection(
            "📋 Type de contrat",
            "Sélectionnez un ou plusieurs types"
        );
        CheckBox cdiCheck = createStyledCheckBox("CDI");
        CheckBox cddCheck = createStyledCheckBox("CDD");
        CheckBox freelanceCheck = createStyledCheckBox("Freelance");
        CheckBox stageCheck = createStyledCheckBox("Stage");
        CheckBox alternanceCheck = createStyledCheckBox("Alternance");
        contractBox.getChildren().addAll(cdiCheck, cddCheck, freelanceCheck, stageCheck, alternanceCheck);
        
        // Localisation (work preference) section
        VBox locBox = createModernFilterSection(
            "🏢 Mode de travail",
            "Préférence de localisation"
        );
        CheckBox remoteCheck = createStyledCheckBox("Remote");
        CheckBox hybridCheck = createStyledCheckBox("Hybrid");
        CheckBox onSiteCheck = createStyledCheckBox("On-Site");
        locBox.getChildren().addAll(remoteCheck, hybridCheck, onSiteCheck);
        
        // Filter by city section
        VBox cityBox = createModernFilterSection(
            "📍 Ville",
            "Rechercher par ville"
        );
        TextField cityField = new TextField();
        cityField.setPromptText("Ex: Paris, Lyon, Marseille...");
        cityField.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e2e8f0; " +
                          "-fx-border-radius: 8; -fx-border-width: 1.5; -fx-padding: 10; -fx-font-size: 13px;");
        cityField.setOnMouseEntered(e -> cityField.setStyle(cityField.getStyle() + "-fx-border-color: #3b82f6;"));
        cityField.setOnMouseExited(e -> cityField.setStyle(cityField.getStyle().replace("-fx-border-color: #3b82f6;", "-fx-border-color: #e2e8f0;")));
        cityBox.getChildren().add(cityField);
        
        // Filter by experience level section
        VBox expBox = createModernFilterSection(
            "⭐ Niveau d'expérience",
            "Filtrer par expérience requise"
        );
        CheckBox juniorCheck = createStyledCheckBox("Junior (0-2 ans)");
        CheckBox mediorCheck = createStyledCheckBox("Médior (2-5 ans)");
        CheckBox seniorCheck = createStyledCheckBox("Senior (5+ ans)");
        expBox.getChildren().addAll(juniorCheck, mediorCheck, seniorCheck);
        
        // Filter by publication date section
        VBox dateBox = createModernFilterSection(
            "📅 Date de publication",
            "Filtrer par récence"
        );
        javafx.scene.control.RadioButton anyTimeRadio = createStyledRadioButton("Toutes les dates");
        javafx.scene.control.RadioButton last24hRadio = createStyledRadioButton("Dernières 24 heures");
        javafx.scene.control.RadioButton last7dRadio = createStyledRadioButton("7 derniers jours");
        javafx.scene.control.RadioButton last30dRadio = createStyledRadioButton("30 derniers jours");
        javafx.scene.control.ToggleGroup dateGroup = new javafx.scene.control.ToggleGroup();
        anyTimeRadio.setToggleGroup(dateGroup);
        last24hRadio.setToggleGroup(dateGroup);
        last7dRadio.setToggleGroup(dateGroup);
        last30dRadio.setToggleGroup(dateGroup);
        anyTimeRadio.setSelected(true);
        dateBox.getChildren().addAll(anyTimeRadio, last24hRadio, last7dRadio, last30dRadio);
        
        // Add all sections with separators
        content.getChildren().addAll(
            contractBox, createStyledSeparator(),
            locBox, createStyledSeparator(),
            cityBox, createStyledSeparator(),
            expBox, createStyledSeparator(),
            dateBox
        );
        
        scrollPane.setContent(content);
        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().setPrefWidth(380);
        dialog.getDialogPane().setPrefHeight(580);
        dialog.getDialogPane().setStyle("-fx-background-color: #f8fafc;");
        
        // Auto-filter functionality - triggers on any change
        Runnable autoFilter = () -> {
            applyAdvancedFilters(
                cdiCheck, cddCheck, freelanceCheck, stageCheck, alternanceCheck,
                remoteCheck, hybridCheck, onSiteCheck,
                cityField.getText(),
                juniorCheck, mediorCheck, seniorCheck,
                anyTimeRadio, last24hRadio, last7dRadio, last30dRadio
            );
        };
        
        // Attach listeners to all controls for real-time filtering
        cdiCheck.setOnAction(e -> autoFilter.run());
        cddCheck.setOnAction(e -> autoFilter.run());
        freelanceCheck.setOnAction(e -> autoFilter.run());
        stageCheck.setOnAction(e -> autoFilter.run());
        alternanceCheck.setOnAction(e -> autoFilter.run());
        remoteCheck.setOnAction(e -> autoFilter.run());
        hybridCheck.setOnAction(e -> autoFilter.run());
        onSiteCheck.setOnAction(e -> autoFilter.run());
        juniorCheck.setOnAction(e -> autoFilter.run());
        mediorCheck.setOnAction(e -> autoFilter.run());
        seniorCheck.setOnAction(e -> autoFilter.run());
        anyTimeRadio.setOnAction(e -> autoFilter.run());
        last24hRadio.setOnAction(e -> autoFilter.run());
        last7dRadio.setOnAction(e -> autoFilter.run());
        last30dRadio.setOnAction(e -> autoFilter.run());
        cityField.textProperty().addListener((obs, oldVal, newVal) -> autoFilter.run());
        
        // Add buttons (no Apply button - filtering is automatic)
        ButtonType resetButton = new ButtonType("🔄 Réinitialiser", ButtonBar.ButtonData.OTHER);
        ButtonType closeButton = new ButtonType("✓ Fermer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(resetButton, closeButton);
        
        // Style buttons
        Button resetBtn = (Button) dialog.getDialogPane().lookupButton(resetButton);
        Button closeBtn = (Button) dialog.getDialogPane().lookupButton(closeButton);
        
        resetBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; " +
                         "-fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;");
        closeBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; " +
                         "-fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;");
        
        // Handle reset button - clear all filters and refresh
        resetBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            cdiCheck.setSelected(false);
            cddCheck.setSelected(false);
            freelanceCheck.setSelected(false);
            stageCheck.setSelected(false);
            alternanceCheck.setSelected(false);
            remoteCheck.setSelected(false);
            hybridCheck.setSelected(false);
            onSiteCheck.setSelected(false);
            cityField.clear();
            juniorCheck.setSelected(false);
            mediorCheck.setSelected(false);
            seniorCheck.setSelected(false);
            anyTimeRadio.setSelected(true);
            autoFilter.run(); // Auto-refresh after reset
            event.consume();
        });
        
        // Show dialog
        dialog.show();
        
        // Position dialog on the right side of the screen
        javafx.stage.Window window = dialog.getDialogPane().getScene().getWindow();
        javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
        javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();
        
        // Position at right side with some margin
        window.setX(bounds.getMaxX() - dialog.getDialogPane().getWidth() - 20);
        window.setY(bounds.getMinY() + 80); // 80px from top
    }
    
    /**
     * Crée une section de filtre moderne avec titre et sous-titre.
     */
    private VBox createModernFilterSection(String title, String subtitle) {
        VBox section = new VBox(8);
        section.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 14; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0, 0, 1);");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b;");
        
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8; -fx-padding: 0 0 6 0;");
        
        section.getChildren().addAll(titleLabel, subtitleLabel);
        return section;
    }
    
    /**
     * Crée un CheckBox stylisé moderne.
     */
    private CheckBox createStyledCheckBox(String text) {
        CheckBox cb = new CheckBox(text);
        cb.setStyle("-fx-font-size: 12px; -fx-text-fill: #334155; -fx-cursor: hand;");
        return cb;
    }
    
    /**
     * Crée un RadioButton stylisé moderne.
     */
    private javafx.scene.control.RadioButton createStyledRadioButton(String text) {
        javafx.scene.control.RadioButton rb = new javafx.scene.control.RadioButton(text);
        rb.setStyle("-fx-font-size: 12px; -fx-text-fill: #334155; -fx-cursor: hand;");
        return rb;
    }
    
    /**
     * Crée un séparateur stylisé moderne.
     */
    private Separator createStyledSeparator() {
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #e2e8f0; -fx-opacity: 0.5;");
        return sep;
    }
    
    /**
     * Applique les filtres avancés sélectionnés.
     */
    private void applyAdvancedFilters(
            CheckBox cdi, CheckBox cdd, CheckBox freelance, CheckBox stage, CheckBox alternance,
            CheckBox remote, CheckBox hybrid, CheckBox onSite,
            String cityText,
            CheckBox junior, CheckBox medior, CheckBox senior,
            javafx.scene.control.RadioButton anyTime, javafx.scene.control.RadioButton last24h, 
            javafx.scene.control.RadioButton last7d, javafx.scene.control.RadioButton last30d) {
        
        cardsContainer.getChildren().clear();
        
        List<OffreEmploi> filteredOffres = allOffres.stream()
            .filter(offre -> matchesContractFilter(offre, cdi, cdd, freelance, stage, alternance))
            .filter(offre -> matchesLocationFilter(offre, remote, hybrid, onSite))
            .filter(offre -> matchesCityFilter(offre, cityText))
            .filter(offre -> matchesExperienceFilter(offre, junior, medior, senior))
            .filter(offre -> matchesDateFilter(offre, anyTime, last24h, last7d, last30d))
            .collect(java.util.stream.Collectors.toList());
        
        for (OffreEmploi offre : filteredOffres) {
            VBox card = createCard(offre);
            cardsContainer.getChildren().add(card);
        }
        
        if (heroSubtitle != null) {
            heroSubtitle.setText("Découvrez " + filteredOffres.size() + " opportunités de carrière");
        }
    }
    
    private boolean matchesSearchFilter(OffreEmploi offre, String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) return true;
        
        String searchTerm = searchText.trim().toLowerCase();
        String titre = (offre.getTitre() != null ? offre.getTitre() : "").toLowerCase();
        
        return titre.contains(searchTerm);
    }
    
    private boolean matchesContractFilter(OffreEmploi offre, CheckBox cdi, CheckBox cdd, 
                                         CheckBox freelance, CheckBox stage, CheckBox alternance) {
        boolean anySelected = cdi.isSelected() || cdd.isSelected() || freelance.isSelected() || 
                             stage.isSelected() || alternance.isSelected();
        if (!anySelected) return true;
        String contract = offre.getTypeContrat();
        if (contract == null) return false;
        return (cdi.isSelected() && contract.equalsIgnoreCase("CDI")) ||
               (cdd.isSelected() && contract.equalsIgnoreCase("CDD")) ||
               (freelance.isSelected() && contract.equalsIgnoreCase("Freelance")) ||
               (stage.isSelected() && contract.equalsIgnoreCase("Stage")) ||
               (alternance.isSelected() && contract.equalsIgnoreCase("Alternance"));
    }
    
    private boolean matchesLocationFilter(OffreEmploi offre, CheckBox remote, CheckBox hybrid, CheckBox onSite) {
        boolean anySelected = remote.isSelected() || hybrid.isSelected() || onSite.isSelected();
        if (!anySelected) return true;
        String workPref = offre.getWorkPreference();
        if (workPref == null) return false;
        return (remote.isSelected() && workPref.equalsIgnoreCase("Remote")) ||
               (hybrid.isSelected() && workPref.equalsIgnoreCase("Hybrid")) ||
               (onSite.isSelected() && workPref.equalsIgnoreCase("On-Site"));
    }
    
    private boolean matchesCityFilter(OffreEmploi offre, String cityText) {
        if (cityText == null || cityText.trim().isEmpty()) return true;
        String lieu = offre.getLieu();
        if (lieu == null) return false;
        return lieu.toLowerCase().contains(cityText.toLowerCase().trim());
    }
    
    private boolean matchesExperienceFilter(OffreEmploi offre, CheckBox junior, CheckBox medior, CheckBox senior) {
        boolean anySelected = junior.isSelected() || medior.isSelected() || senior.isSelected();
        if (!anySelected) return true;
        List<CritereOffre> criteres = critereService.getByOffreId(offre.getIdOffre());
        if (criteres == null || criteres.isEmpty()) return false;
        CritereOffre critere = criteres.get(0);
        String exp = critere.getNiveauExperience();
        if (exp == null) return false;
        String expLower = exp.toLowerCase();
        return (junior.isSelected() && expLower.contains("junior")) ||
               (medior.isSelected() && (expLower.contains("médior") || expLower.contains("medior") || expLower.contains("intermédiaire"))) ||
               (senior.isSelected() && expLower.contains("senior"));
    }
    
    private boolean matchesSkillsFilter(OffreEmploi offre, String skillsText) {
        if (skillsText == null || skillsText.trim().isEmpty()) return true;
        String[] skills = skillsText.split(",");
        List<CritereOffre> criteres = critereService.getByOffreId(offre.getIdOffre());
        CritereOffre critere = (criteres != null && !criteres.isEmpty()) ? criteres.get(0) : null;
        String searchableText = (offre.getTitre() + " " + offre.getDescription() + " " +
                                (critere != null ? critere.getCompetencesRequises() : "")).toLowerCase();
        for (String skill : skills) {
            if (searchableText.contains(skill.trim().toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    private boolean matchesDateFilter(OffreEmploi offre, javafx.scene.control.RadioButton anyTime,
                                     javafx.scene.control.RadioButton last24h,
                                     javafx.scene.control.RadioButton last7d,
                                     javafx.scene.control.RadioButton last30d) {
        if (anyTime.isSelected()) return true;
        java.sql.Date datePublication = offre.getDatePublication();
        if (datePublication == null) return true;
        java.time.LocalDate pubDate = datePublication.toLocalDate();
        java.time.LocalDate now = java.time.LocalDate.now();
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(pubDate, now);
        if (last24h.isSelected()) {
            return daysBetween <= 1;
        } else if (last7d.isSelected()) {
            return daysBetween <= 7;
        } else if (last30d.isSelected()) {
            return daysBetween <= 30;
        }
        return true;
    }
    
    /**
     * Filters offers based on search text and applies all active filters.
     * Updates the display and count label in real-time.
     */
    private void filterAndUpdateCount() {
        String searchText = searchField.getText();
        cardsContainer.getChildren().clear();
        
        List<OffreEmploi> filteredOffres = allOffres.stream()
            .filter(offre -> matchesSearchFilter(offre, searchText))
            .filter(this::matchesWorkPreferenceFilter)
            .filter(this::matchesContractTypeFilter)
            .filter(this::matchesTechnologyFilter)
            .collect(java.util.stream.Collectors.toList());
        
        for (OffreEmploi offre : filteredOffres) {
            VBox card = createCard(offre);
            cardsContainer.getChildren().add(card);
        }
        
        // Update count label
        updateCountLabel(filteredOffres.size());
    }
    
    /**
     * Updates the count label to display the number of offers currently shown.
     * Shows the count in a professional format: "X offre(s)"
     * 
     * @param count The number of offers to display
     */
    private void updateCountLabel(int count) {
        if (countLabel != null) {
            String countText = count + " offre" + (count != 1 ? "s" : "");
            countLabel.setText(countText);
            countLabel.setStyle("-fx-text-fill: #007bff; -fx-font-weight: bold; -fx-font-size: 13;");
        }
    }
    
    /**
     * Applique les filtres sélectionnés aux offres d'emploi.
     * Cette méthode filtre les offres en fonction des critères sélectionnés dans le panneau de filtres
     * (localisation, type de contrat, technologies) et met à jour l'affichage des cartes.
     */
    @FXML
    private void applyFilters() {
        filterAndUpdateCount();
        
        // Update hero subtitle with count
        if (heroSubtitle != null) {
            int displayCount = cardsContainer.getChildren().size();
            heroSubtitle.setText("Découvrez " + displayCount + " opportunités de carrière");
        }
    }
    
    /**
     * Vérifie si une offre correspond aux filtres de préférence de travail (Remote, Hybrid, On-Site).
     * @param offre L'offre à vérifier
     * @return true si l'offre correspond ou si aucun filtre n'est sélectionné
     */
    private boolean matchesWorkPreferenceFilter(OffreEmploi offre) {
        // Return true if filter checkboxes don't exist
        if (filterRemote == null && filterHybrid == null && filterOnSite == null) {
            return true;
        }
        
        boolean anyFilterSelected = (filterRemote != null && filterRemote.isSelected()) || 
                                   (filterHybrid != null && filterHybrid.isSelected()) || 
                                   (filterOnSite != null && filterOnSite.isSelected());
        if (!anyFilterSelected) return true;
        
        String workPref = offre.getWorkPreference();
        if (workPref == null) return false;
        
        return (filterRemote != null && filterRemote.isSelected() && workPref.equalsIgnoreCase("Remote")) ||
               (filterHybrid != null && filterHybrid.isSelected() && workPref.equalsIgnoreCase("Hybrid")) ||
               (filterOnSite != null && filterOnSite.isSelected() && workPref.equalsIgnoreCase("On-Site"));
    }
    
    /**
     * Vérifie si une offre correspond aux filtres de type de contrat (CDI, Freelance, Stage).
     * @param offre L'offre à vérifier
     * @return true si l'offre correspond ou si aucun filtre n'est sélectionné
     */
    private boolean matchesContractTypeFilter(OffreEmploi offre) {
        // Return true if filter checkboxes don't exist
        if (filterCDI == null && filterFreelance == null && filterStage == null) {
            return true;
        }
        
        boolean anyFilterSelected = (filterCDI != null && filterCDI.isSelected()) || 
                                   (filterFreelance != null && filterFreelance.isSelected()) || 
                                   (filterStage != null && filterStage.isSelected());
        if (!anyFilterSelected) return true;
        
        String contractType = offre.getTypeContrat();
        if (contractType == null) return false;
        
        return (filterCDI != null && filterCDI.isSelected() && contractType.equalsIgnoreCase("CDI")) ||
               (filterFreelance != null && filterFreelance.isSelected() && contractType.equalsIgnoreCase("Freelance")) ||
               (filterStage != null && filterStage.isSelected() && contractType.equalsIgnoreCase("Stage"));
    }
    
    /**
     * Vérifie si une offre correspond aux filtres de technologies.
     * Recherche les technologies dans le titre, la description et les compétences requises.
     * @param offre L'offre à vérifier
     * @return true si l'offre correspond ou si aucun filtre n'est sélectionné
     */
    private boolean matchesTechnologyFilter(OffreEmploi offre) {
        // Return true if filter checkboxes don't exist
        if (filterAWS == null && filterNodeJS == null && filterPython == null && 
            filterReact == null && filterJavaScript == null) {
            return true;
        }
        
        boolean anyFilterSelected = (filterAWS != null && filterAWS.isSelected()) || 
                                   (filterNodeJS != null && filterNodeJS.isSelected()) || 
                                   (filterPython != null && filterPython.isSelected()) || 
                                   (filterReact != null && filterReact.isSelected()) || 
                                   (filterJavaScript != null && filterJavaScript.isSelected());
        if (!anyFilterSelected) return true;
        
        // Get criteria for this offer
        List<CritereOffre> criteres = critereService.getByOffreId(offre.getIdOffre());
        CritereOffre critere = (criteres != null && !criteres.isEmpty()) ? criteres.get(0) : null;
        
        // Combine all searchable text
        String searchText = (offre.getTitre() + " " + offre.getDescription() + " " + 
                           (critere != null ? critere.getCompetencesRequises() : "")).toLowerCase();
        
        return (filterAWS != null && filterAWS.isSelected() && searchText.contains("aws")) ||
               (filterNodeJS != null && filterNodeJS.isSelected() && (searchText.contains("node") || searchText.contains("node.js"))) ||
               (filterPython != null && filterPython.isSelected() && searchText.contains("python")) ||
               (filterReact != null && filterReact.isSelected() && searchText.contains("react")) ||
               (filterJavaScript != null && filterJavaScript.isSelected() && (searchText.contains("javascript") || searchText.contains("js")));
    }
}



