package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import entities.Utilisateur;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import services.ServiceUtilisateur;
import utilis.UserSession;

public class StatistiquesController {

    @FXML private Label lblUserName, lblUserRole;
    @FXML private StackPane userAvatarContainer;
    @FXML private Label lblUserAvatar;
    
    @FXML private Label lblTotalUsers, lblTotalClients, lblTotalAdmins;
    @FXML private Label lblUsersGrowth, lblClientsGrowth;
    
    @FXML private PieChart pieChart;
    @FXML private VBox activityContainer;
    
    // @FXML private VBox sidebar, navContainer;
    // @FXML private HBox btnStatistiques, btnUtilisateurs, btnOffres, logoutBtn;
    
    private ServiceUtilisateur serviceUtilisateur = new ServiceUtilisateur();
    private Timeline autoRefreshTimeline;
    private boolean isSidebarHovered = false;

    @FXML
    public void initialize() {
        // initializeSidebar();
        loadCurrentUser();
        loadStatistics();
        setupAutoRefresh();
        System.out.println("=== STATISTIQUES VIEW CHARGÉE ===");
    }
    
    // private void initializeSidebar() {
    //     // Masquer les labels au démarrage
    //     for (javafx.scene.Node node : navContainer.getChildren()) {
    //         if (node instanceof HBox) {
    //             HBox hbox = (HBox) node;
    //             for (javafx.scene.Node child : hbox.getChildren()) {
    //                 if (child instanceof Label && ((Label) child).getStyleClass().contains("sidebar-nav-label")) {
    //                     child.setOpacity(0.0);
    //                 }
    //             }
    //         }
    //     }
    // }

    private void setupAutoRefresh() {
        // Auto-refresh toutes les 30 secondes
        autoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            System.out.println("🔄 Auto-refresh des statistiques...");
            loadStatistics();
        }));
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    private void loadCurrentUser() {
        Utilisateur currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            lblUserName.setText(currentUser.getNom() + " " + currentUser.getPrenom());
            lblUserRole.setText(currentUser.getRole());
            loadUserAvatar(currentUser.getImage_profil());
        }
    }

    private void loadUserAvatar(String imagePath) {
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            try {
                File file = new File(imagePath);
                if (file.exists() && file.isFile()) {
                    Image image = new Image(new FileInputStream(file));
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(50);
                    imageView.setFitHeight(50);
                    imageView.setPreserveRatio(false);
                    
                    javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(25, 25, 25);
                    imageView.setClip(clip);
                    
                    userAvatarContainer.getChildren().clear();
                    userAvatarContainer.getChildren().add(imageView);
                    
                    System.out.println("✓ Avatar chargé: " + imagePath);
                } else {
                    System.err.println("⚠ Fichier avatar introuvable: " + imagePath);
                }
            } catch (Exception e) {
                System.err.println("✗ Erreur chargement avatar: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void loadStatistics() {
        System.out.println("=== CHARGEMENT STATISTIQUES ===");
        
        // Récupérer tous les utilisateurs
        ArrayList<Utilisateur> users = serviceUtilisateur.afficherAll();
        
        if (users.isEmpty()) {
            lblTotalUsers.setText("0");
            lblTotalClients.setText("0");
            lblTotalAdmins.setText("0");
            lblUsersGrowth.setText("--");
            lblClientsGrowth.setText("--");
            return;
        }
        
        // Calculer les statistiques
        int totalUsers = users.size();
        int totalClients = 0;
        int totalAdmins = 0;
        
        Map<String, Integer> roleCount = new HashMap<>();
        
        // Trouver l'ID max pour calculer les "récents"
        int maxId = 0;
        for (Utilisateur u : users) {
            String role = u.getRole();
            if ("CLIENT".equals(role)) {
                totalClients++;
            } else if ("ADMIN_RH".equals(role) || "ADMIN_TECHNIQUE".equals(role)) {
                totalAdmins++;
            }
            
            roleCount.put(role, roleCount.getOrDefault(role, 0) + 1);
            
            if (u.getId_utilisateur() > maxId) {
                maxId = u.getId_utilisateur();
            }
        }
        
        // Calculer les utilisateurs récents (10 derniers IDs)
        int recentThreshold = Math.max(1, maxId - 10);
        int recentUsers = 0;
        int recentClients = 0;
        
        for (Utilisateur u : users) {
            if (u.getId_utilisateur() > recentThreshold) {
                recentUsers++;
                if ("CLIENT".equals(u.getRole())) {
                    recentClients++;
                }
            }
        }
        
        // Mettre à jour les labels
        lblTotalUsers.setText(String.valueOf(totalUsers));
        lblTotalClients.setText(String.valueOf(totalClients));
        lblTotalAdmins.setText(String.valueOf(totalAdmins));
        
        // Calculer la croissance réelle (derniers 10 vs 10 précédents)
        int previousThreshold = Math.max(1, maxId - 20);
        int previousUsers = 0;
        int previousClients = 0;
        
        for (Utilisateur u : users) {
            int id = u.getId_utilisateur();
            if (id > previousThreshold && id <= recentThreshold) {
                previousUsers++;
                if ("CLIENT".equals(u.getRole())) {
                    previousClients++;
                }
            }
        }
        
        // Calculer les pourcentages de croissance
        if (previousUsers > 0) {
            double usersGrowth = ((recentUsers - previousUsers) * 100.0) / previousUsers;
            lblUsersGrowth.setText(String.format("%+.1f%% récemment", usersGrowth));
        } else {
            lblUsersGrowth.setText(recentUsers > 0 ? "+100%" : "--");
        }
        
        if (previousClients > 0) {
            double clientsGrowth = ((recentClients - previousClients) * 100.0) / previousClients;
            lblClientsGrowth.setText(String.format("%+.1f%% récemment", clientsGrowth));
        } else {
            lblClientsGrowth.setText(recentClients > 0 ? "+100%" : "--");
        }
        
        // Générer les graphiques
        generatePieChart(roleCount);
        generateRecentActivity(users);
        
        System.out.println("✓ Statistiques chargées:");
        System.out.println("  - Total: " + totalUsers);
        System.out.println("  - Clients: " + totalClients);
        System.out.println("  - Admins: " + totalAdmins);
        System.out.println("  - Croissance utilisateurs: " + lblUsersGrowth.getText());
        System.out.println("  - Croissance clients: " + lblClientsGrowth.getText());
    }

    private void generatePieChart(Map<String, Integer> roleCount) {
        pieChart.getData().clear();
        
        for (Map.Entry<String, Integer> entry : roleCount.entrySet()) {
            PieChart.Data slice = new PieChart.Data(entry.getKey(), entry.getValue());
            pieChart.getData().add(slice);
        }
        
        // Style moderne
        pieChart.setStyle("-fx-background-color: transparent;");
    }

    private void generateRecentActivity(ArrayList<Utilisateur> users) {
        activityContainer.getChildren().clear();
        
        if (users.isEmpty()) {
            Label emptyLabel = new Label("Aucune activité récente");
            emptyLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 14px;");
            activityContainer.getChildren().add(emptyLabel);
            return;
        }
        
        // Trier les utilisateurs par ID décroissant (les plus récents en premier)
        ArrayList<Utilisateur> sortedUsers = new ArrayList<>(users);
        sortedUsers.sort((u1, u2) -> Integer.compare(u2.getId_utilisateur(), u1.getId_utilisateur()));
        
        // Prendre les 5 plus récents
        int count = Math.min(5, sortedUsers.size());
        
        for (int i = 0; i < count; i++) {
            Utilisateur u = sortedUsers.get(i);
            activityContainer.getChildren().add(createActivityItem(u, i + 1));
        }
    }

    private HBox createActivityItem(Utilisateur u, int position) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle("-fx-background-color: #F9FAFB; -fx-background-radius: 10; -fx-padding: 15;");
        
        // Icône
        StackPane iconContainer = new StackPane();
        iconContainer.setStyle("-fx-background-color: #6366F1; -fx-background-radius: 50; -fx-min-width: 45; -fx-min-height: 45; -fx-max-width: 45; -fx-max-height: 45;");
        Label icon = new Label("👤");
        icon.setStyle("-fx-font-size: 20px;");
        iconContainer.getChildren().add(icon);
        
        // Info
        VBox info = new VBox(5);
        HBox.setHgrow(info, Priority.ALWAYS);
        
        Label name = new Label(u.getNom() + " " + u.getPrenom());
        name.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #1F2937;");
        
        Label action = new Label("Inscription - " + u.getRole() + " • ID: #" + u.getId_utilisateur());
        action.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
        
        info.getChildren().addAll(name, action);
        
        // Badge rôle
        Label roleBadge = new Label(u.getRole());
        String badgeColor = "CLIENT".equals(u.getRole()) ? "#DBEAFE; -fx-text-fill: #1E40AF" : 
                           "ADMIN_RH".equals(u.getRole()) ? "#FEE2E2; -fx-text-fill: #991B1B" :
                           "#D1FAE5; -fx-text-fill: #065F46";
        roleBadge.setStyle("-fx-background-color: " + badgeColor + "; -fx-font-size: 11px; -fx-font-weight: 600; -fx-padding: 4 10; -fx-background-radius: 12;");
        
        // Position dans la liste
        Label posLabel = new Label("#" + position);
        posLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF; -fx-font-weight: 600;");
        
        item.getChildren().addAll(iconContainer, info, roleBadge, posLabel);
        
        // Animation d'apparition
        FadeTransition fade = new FadeTransition(Duration.millis(300), item);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
        
        return item;
    }

    @FXML
    public void refreshStats() {
        System.out.println("🔄 Actualisation des statistiques...");
        loadStatistics();
        
        // Animation de rotation pour feedback visuel
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Actualisation");
        alert.setHeaderText("✅ Statistiques mises à jour");
        alert.setContentText("Les données ont été actualisées avec succès.");
        alert.showAndWait();
    }

    @FXML
    public void exportData() {
        System.out.println("📥 Export des données...");
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les statistiques");
        fileChooser.setInitialFileName("statistiques_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv")
        );
        
        File file = fileChooser.showSaveDialog(lblTotalUsers.getScene().getWindow());
        
        if (file != null) {
            try {
                FileWriter writer = new FileWriter(file);
                
                // En-tête
                writer.write("Type,Valeur\n");
                writer.write("Total Utilisateurs," + lblTotalUsers.getText() + "\n");
                writer.write("Clients," + lblTotalClients.getText() + "\n");
                writer.write("Admins," + lblTotalAdmins.getText() + "\n");
                
                // Détails des utilisateurs
                writer.write("\nNom,Prénom,Email,Role\n");
                for (Utilisateur u : serviceUtilisateur.afficherAll()) {
                    writer.write(String.format("%s,%s,%s,%s\n", 
                        u.getNom(), u.getPrenom(), u.getEmail(), u.getRole()));
                }
                
                writer.close();
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export réussi");
                alert.setHeaderText("✅ Données exportées");
                alert.setContentText("Le fichier a été sauvegardé avec succès:\n" + file.getAbsolutePath());
                alert.showAndWait();
                
                System.out.println("✓ Export réussi: " + file.getAbsolutePath());
                
            } catch (Exception e) {
                System.err.println("✗ Erreur export: " + e.getMessage());
                e.printStackTrace();
                
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("❌ Erreur d'export");
                alert.setContentText("Impossible d'exporter les données:\n" + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    public void goToAdmin() {
        stopAutoRefresh();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdministrationView.fxml"));
            Parent root = loader.load();
            Scene scene = lblTotalUsers.getScene();
            scene.setRoot(root);
            System.out.println("✓ Navigation vers Administration");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void logout() {
        stopAutoRefresh();
        UserSession.getInstance().setCurrentUser(null);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SigninView.fxml"));
            Parent root = loader.load();
            Scene scene = lblTotalUsers.getScene();
            scene.setRoot(root);
            System.out.println("✓ Déconnexion réussie");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopAutoRefresh() {
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
            System.out.println("⏸ Auto-refresh arrêté");
        }
    }

  
}
