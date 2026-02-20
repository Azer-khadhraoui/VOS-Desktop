package controllers;

import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import utilis.UserSession;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OffresController {

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> cbDomaine;

    @FXML
    private ComboBox<String> cbType;

    @FXML
    private VBox offresContainer;

    @FXML
    private StackPane userAvatarContainer;

    @FXML
    private Label lblUserAvatar;

    @FXML
    private Label lblUserName;

    @FXML
    private Label lblUserRole;

    private List<Offre> allOffres = new ArrayList<>();
    private List<Offre> filteredOffres = new ArrayList<>();

    @FXML
    public void initialize() {
        loadCurrentUserInfo();
        setupFilters();
        loadOffres();
        displayOffres(allOffres);
        setupSearch();
    }

    private void loadCurrentUserInfo() {
        Utilisateur currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            System.out.println("=== CHARGEMENT INFO UTILISATEUR DANS OFFRES ===");
            System.out.println("Nom: " + currentUser.getNom());
            System.out.println("Prénom: " + currentUser.getPrenom());
            System.out.println("Image_profil: " + currentUser.getImage_profil());
            
            lblUserName.setText(currentUser.getNom() + " " + currentUser.getPrenom());
            lblUserRole.setText(currentUser.getRole());
            loadUserAvatar(currentUser.getImage_profil());
        } else {
            System.err.println("ERREUR: currentUser est null!");
        }
    }

    private void loadUserAvatar(String imagePath) {
        System.out.println("=== CHARGEMENT AVATAR ===");
        System.out.println("imagePath: " + imagePath);
        
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            try {
                File file = new File(imagePath);
                System.out.println("Chemin absolu: " + file.getAbsolutePath());
                System.out.println("Fichier existe: " + file.exists());
                
                if (file.exists() && file.isFile()) {
                    Image image = new Image(new FileInputStream(file));
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(50);
                    imageView.setFitHeight(50);
                    imageView.setPreserveRatio(true);
                    
                    // Créer un cercle pour clipper l'image
                    javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(25, 25, 25);
                    imageView.setClip(clip);
                    
                    userAvatarContainer.getChildren().clear();
                    userAvatarContainer.getChildren().add(imageView);
                    System.out.println("✓ Avatar chargé avec succès!");
                } else {
                    System.out.println("⚠ Fichier introuvable, affichage emoji par défaut");
                    afficherAvatarParDefaut();
                }
            } catch (Exception e) {
                System.err.println("✗ Erreur lors du chargement de l'avatar: " + e.getMessage());
                e.printStackTrace();
                afficherAvatarParDefaut();
            }
        } else {
            System.out.println("ℹ Pas d'image de profil définie, affichage emoji par défaut");
            afficherAvatarParDefaut();
        }
    }
    
    private void afficherAvatarParDefaut() {
        // Afficher l'emoji par défaut
        userAvatarContainer.getChildren().clear();
        lblUserAvatar.setText("👤");
        userAvatarContainer.getChildren().add(lblUserAvatar);
    }

    private void setupFilters() {
        cbDomaine.getItems().addAll(
            "Tous les domaines",
            "Développement Web",
            "Data Science",
            "Design UX/UI",
            "Marketing Digital",
            "DevOps",
            "Cybersécurité"
        );
        cbDomaine.setValue("Tous les domaines");

        cbType.getItems().addAll(
            "Tous les types",
            "CDI",
            "CDD",
            "Stage",
            "Freelance",
            "Alternance"
        );
        cbType.setValue("Tous les types");

        cbDomaine.setOnAction(e -> filterOffres());
        cbType.setOnAction(e -> filterOffres());
    }

    private void loadOffres() {
        // Données simulées - À remplacer par une requête base de données
        allOffres.add(new Offre(
            1,
            "Développeur Full Stack",
            "TechCorp Solutions",
            "Développement Web",
            "CDI",
            "Paris, France",
            "Nous recherchons un développeur expérimenté en React et Node.js pour rejoindre notre équipe dynamique.",
            "45000 - 60000 €/an",
            LocalDate.now().minusDays(5)
        ));
        
        allOffres.add(new Offre(
            2,
            "Data Scientist Junior",
            "AI Innovations",
            "Data Science",
            "CDD",
            "Lyon, France",
            "Rejoignez notre équipe d'intelligence artificielle pour développer des modèles prédictifs innovants.",
            "38000 - 45000 €/an",
            LocalDate.now().minusDays(3)
        ));
        
        allOffres.add(new Offre(
            3,
            "Designer UX/UI",
            "Creative Studio",
            "Design UX/UI",
            "CDI",
            "Marseille, France",
            "Conception d'interfaces utilisateur modernes pour applications web et mobile.",
            "35000 - 48000 €/an",
            LocalDate.now().minusDays(7)
        ));
        
        allOffres.add(new Offre(
            4,
            "Ingénieur DevOps",
            "CloudSys Inc",
            "DevOps",
            "CDI",
            "Toulouse, France",
            "Automatisation, CI/CD, Kubernetes - Participez à la transformation cloud de nos clients.",
            "50000 - 65000 €/an",
            LocalDate.now().minusDays(2)
        ));
        
        allOffres.add(new Offre(
            5,
            "Stagiaire Marketing Digital",
            "Webify Agency",
            "Marketing Digital",
            "Stage",
            "Bordeaux, France",
            "Stage de 6 mois en marketing digital: SEO, social media, content marketing.",
            "600 - 800 €/mois",
            LocalDate.now().minusDays(10)
        ));
        
        allOffres.add(new Offre(
            6,
            "Expert Cybersécurité",
            "SecureNet",
            "Cybersécurité",
            "CDI",
            "Paris, France",
            "Protection des infrastructures et audit de sécurité pour grandes entreprises.",
            "60000 - 80000 €/an",
            LocalDate.now().minusDays(1)
        ));
        
        filteredOffres = new ArrayList<>(allOffres);
    }

    private void displayOffres(List<Offre> offres) {
        offresContainer.getChildren().clear();

        if (offres.isEmpty()) {
            Label noResults = new Label("Aucune offre ne correspond à vos critères 😔");
            noResults.setStyle("-fx-font-size: 16px; -fx-text-fill: #6B7280; -fx-padding: 50px;");
            offresContainer.getChildren().add(noResults);
            return;
        }

        for (Offre offre : offres) {
            VBox card = createOffreCard(offre);
            offresContainer.getChildren().add(card);
        }
    }

    private VBox createOffreCard(Offre offre) {
        VBox card = new VBox(15);
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 16px; " +
            "-fx-padding: 25px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0, 0, 4);"
        );
        card.setMinHeight(200);

        // Header row: company + salary
        HBox headerRow = new HBox(20);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        VBox companyInfo = new VBox(5);
        Label titleLabel = new Label(offre.titre);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        
        Label companyLabel = new Label("🏢 " + offre.entreprise);
        companyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B7280;");
        
        companyInfo.getChildren().addAll(titleLabel, companyLabel);
        HBox.setHgrow(companyInfo, Priority.ALWAYS);

        Label salaryLabel = new Label(offre.salaire);
        salaryLabel.setStyle(
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #10B981; " +
            "-fx-background-color: #D1FAE5; " +
            "-fx-background-radius: 8px; " +
            "-fx-padding: 8px 16px;"
        );

        headerRow.getChildren().addAll(companyInfo, salaryLabel);

        // Tags row
        HBox tagsRow = new HBox(10);
        tagsRow.setAlignment(Pos.CENTER_LEFT);

        Label domaineTag = createTag(offre.domaine, "#DBEAFE", "#1E40AF");
        Label typeTag = createTag(offre.type, "#FEF3C7", "#92400E");
        Label locationTag = createTag("📍 " + offre.localisation, "#F3F4F6", "#374151");
        Label dateTag = createTag("📅 Il y a " + getDaysAgo(offre.datePublication) + " jours", "#FEE2E2", "#991B1B");

        tagsRow.getChildren().addAll(domaineTag, typeTag, locationTag, dateTag);

        // Description
        Label descriptionLabel = new Label(offre.description);
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4B5563; -fx-line-spacing: 2px;");

        // Action buttons
        HBox actionRow = new HBox(15);
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        Button btnDetails = new Button("📄 Détails");
        btnDetails.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #3B82F6; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 600; " +
            "-fx-cursor: hand; " +
            "-fx-padding: 10px 20px; " +
            "-fx-border-color: #3B82F6; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px;"
        );
        btnDetails.setOnMouseEntered(e -> btnDetails.setStyle(
            btnDetails.getStyle() + "-fx-background-color: #EFF6FF;"
        ));
        btnDetails.setOnMouseExited(e -> btnDetails.setStyle(
            btnDetails.getStyle().replace("-fx-background-color: #EFF6FF;", "-fx-background-color: transparent;")
        ));
        btnDetails.setOnAction(e -> showOffreDetails(offre));

        Button btnPostuler = new Button("✉️ Postuler");
        btnPostuler.setStyle(
            "-fx-background-color: #3B82F6; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 600; " +
            "-fx-cursor: hand; " +
            "-fx-padding: 10px 24px; " +
            "-fx-background-radius: 8px;"
        );
        btnPostuler.setOnMouseEntered(e -> btnPostuler.setStyle(
            btnPostuler.getStyle() + "-fx-opacity: 0.9; -fx-scale-x: 1.05; -fx-scale-y: 1.05;"
        ));
        btnPostuler.setOnMouseExited(e -> btnPostuler.setStyle(
            btnPostuler.getStyle().replace("-fx-opacity: 0.9; -fx-scale-x: 1.05; -fx-scale-y: 1.05;", "")
        ));
        btnPostuler.setOnAction(e -> postulerOffre(offre));

        actionRow.getChildren().addAll(btnDetails, btnPostuler);

        card.getChildren().addAll(headerRow, tagsRow, descriptionLabel, actionRow);
        return card;
    }

    private Label createTag(String text, String bgColor, String textColor) {
        Label tag = new Label(text);
        tag.setStyle(
            "-fx-background-color: " + bgColor + "; " +
            "-fx-text-fill: " + textColor + "; " +
            "-fx-font-size: 12px; " +
            "-fx-font-weight: 600; " +
            "-fx-background-radius: 6px; " +
            "-fx-padding: 4px 10px;"
        );
        return tag;
    }

    private long getDaysAgo(LocalDate date) {
        return java.time.temporal.ChronoUnit.DAYS.between(date, LocalDate.now());
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterOffres();
        });
    }

    @FXML
    private void filterOffres() {
        String searchText = searchField.getText().toLowerCase().trim();
        String domaine = cbDomaine.getValue();
        String type = cbType.getValue();

        filteredOffres = allOffres.stream()
            .filter(offre -> {
                boolean matchesSearch = searchText.isEmpty() ||
                    offre.titre.toLowerCase().contains(searchText) ||
                    offre.entreprise.toLowerCase().contains(searchText) ||
                    offre.description.toLowerCase().contains(searchText);

                boolean matchesDomaine = domaine.equals("Tous les domaines") || offre.domaine.equals(domaine);
                boolean matchesType = type.equals("Tous les types") || offre.type.equals(type);

                return matchesSearch && matchesDomaine && matchesType;
            })
            .collect(Collectors.toList());

        displayOffres(filteredOffres);
    }

    @FXML
    private void resetFilters() {
        searchField.clear();
        cbDomaine.setValue("Tous les domaines");
        cbType.setValue("Tous les types");
        filterOffres();
    }

    private void showOffreDetails(Offre offre) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de l'offre");
        alert.setHeaderText(offre.titre + " - " + offre.entreprise);
        alert.setContentText(
            "Domaine: " + offre.domaine + "\n" +
            "Type: " + offre.type + "\n" +
            "Localisation: " + offre.localisation + "\n" +
            "Salaire: " + offre.salaire + "\n\n" +
            "Description:\n" + offre.description + "\n\n" +
            "Publié il y a " + getDaysAgo(offre.datePublication) + " jours"
        );
        alert.showAndWait();
    }

    private void postulerOffre(Offre offre) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Postuler à l'offre");
        alert.setHeaderText("Confirmer votre candidature");
        alert.setContentText("Êtes-vous sûr de vouloir postuler à l'offre \"" + offre.titre + "\" chez " + offre.entreprise + " ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Candidature envoyée");
                success.setHeaderText("✅ Succès !");
                success.setContentText("Votre candidature a été envoyée avec succès. L'entreprise vous contactera prochainement.");
                success.showAndWait();
            }
        });
    }

    @FXML
    private void logout() {
        try {
            UserSession.getInstance().clearSession();
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SigninView.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) offresContainer.getScene().getWindow();
            Scene scene = new Scene(root, 1440, 1024);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.setTitle("Connexion - VOS");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur de déconnexion");
            alert.setContentText("Impossible de se déconnecter: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void goToProfil() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProfilView.fxml"));
            Parent root = loader.load();
            Scene scene = lblUserName.getScene();
            scene.setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Impossible d'ouvrir le profil");
            alert.showAndWait();
        }
    }

    // Inner class pour représenter une offre
    private static class Offre {
        int id;
        String titre;
        String entreprise;
        String domaine;
        String type;
        String localisation;
        String description;
        String salaire;
        LocalDate datePublication;

        public Offre(int id, String titre, String entreprise, String domaine, String type, 
                     String localisation, String description, String salaire, LocalDate datePublication) {
            this.id = id;
            this.titre = titre;
            this.entreprise = entreprise;
            this.domaine = domaine;
            this.type = type;
            this.localisation = localisation;
            this.description = description;
            this.salaire = salaire;
            this.datePublication = datePublication;
        }
    }
}
