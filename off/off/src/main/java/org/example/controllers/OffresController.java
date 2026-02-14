package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import org.example.entities.OffreEmploi;
import org.example.entities.CritereOffre;
import org.example.services.OffreEmploiService;
import org.example.services.CritereOffreService;
import javafx.geometry.Pos;
import javafx.geometry.Orientation;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.control.Separator;
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

    private final OffreEmploiService service = new OffreEmploiService();
    private final CritereOffreService critereService = new CritereOffreService();
    private List<OffreEmploi> allOffres;
    private OffreEmploi selectedOffre;

    /**
     * Méthode d'initialisation du contrôleur JavaFX.
     * Cette méthode est appelée automatiquement après le chargement du fichier FXML.
     * Elle charge toutes les offres d'emploi disponibles.
     */
    @FXML
    public void initialize() {
        loadOffres();
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
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);

        Label location = new Label("📍 " + offre.getStatutOffre());
        location.getStyleClass().add("location-text");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button applyBtn = new Button("Postuler");
        applyBtn.getStyleClass().add("apply-btn");
        applyBtn.setOnAction(e -> {
            e.consume(); // Prevent card click
            // Handle apply action
        });

        footer.getChildren().addAll(location, spacer, applyBtn);

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
        
        // Location
        Label location = new Label("📍 " + offre.getStatutOffre());
        location.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        card.getChildren().addAll(header, title, location);

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
        HBox metaInfo = new HBox(30);
        metaInfo.setAlignment(Pos.CENTER);
        metaInfo.setStyle("-fx-padding: 10 0 0 0;");
        
        // Contract Type with phrase
        VBox contractBox = new VBox(4);
        contractBox.setAlignment(Pos.CENTER);
        Label contractLabel = new Label("Type de contrat");
        contractLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: 600;");
        Label contractValue = new Label(offre.getTypeContrat());
        contractValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #1e293b; -fx-font-weight: 700;");
        contractBox.getChildren().addAll(contractLabel, contractValue);
        
        // Separator
        Separator sep1 = new Separator();
        sep1.setOrientation(Orientation.VERTICAL);
        sep1.setPrefHeight(30);
        sep1.setStyle("-fx-background-color: #e2e8f0;");
        
        // Status with phrase
        VBox statusBox = new VBox(4);
        statusBox.setAlignment(Pos.CENTER);
        Label statusLabel = new Label("Statut de l'offre");
        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: 600;");
        Label statusValue = new Label(offre.getStatutOffre());
        statusValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #1e293b; -fx-font-weight: 700;");
        statusBox.getChildren().addAll(statusLabel, statusValue);
        
        // Separator
        Separator sep2 = new Separator();
        sep2.setOrientation(Orientation.VERTICAL);
        sep2.setPrefHeight(30);
        sep2.setStyle("-fx-background-color: #e2e8f0;");
        
        // Publication Date with phrase
        VBox dateBox = new VBox(4);
        dateBox.setAlignment(Pos.CENTER);
        Label dateLabel = new Label("Publiée le");
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: 600;");
        
        String dateValue = "Non spécifiée";
        if (offre.getDatePublication() != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMMM yyyy");
            dateValue = sdf.format(offre.getDatePublication());
        }
        Label dateText = new Label(dateValue);
        dateText.setStyle("-fx-font-size: 14px; -fx-text-fill: #1e293b; -fx-font-weight: 700;");
        dateBox.getChildren().addAll(dateLabel, dateText);
        
        metaInfo.getChildren().addAll(contractBox, sep1, statusBox, sep2, dateBox);
        
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
}



