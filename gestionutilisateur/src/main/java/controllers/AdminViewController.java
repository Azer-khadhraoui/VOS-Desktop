// package controllers;

// import java.io.IOException;
// import java.net.URL;
// import java.util.ResourceBundle;

// import entities.Utilisateur;
// import javafx.animation.KeyFrame;
// import javafx.animation.KeyValue;
// import javafx.animation.Timeline;
// import javafx.fxml.FXML;
// import javafx.fxml.FXMLLoader;
// import javafx.fxml.Initializable;
// import javafx.scene.Node;
// import javafx.scene.control.Label;
// import javafx.scene.layout.HBox;
// import javafx.scene.layout.StackPane;
// import javafx.scene.layout.VBox;
// import javafx.util.Duration;
// import utilis.UserSession;

// public class AdminViewController implements Initializable {

//     @FXML private StackPane contentArea;
//     @FXML private VBox sidebar;
    
//     @FXML private Label navStatistiquesText, navOffresText, navServicesText, 
//                         navInformationsText, navCandidaturesText, navParametresText, navProfilText,navUtilisateursText;
    
//     @FXML private HBox btnStatistiques, btnOffres, btnServices, btnInformations,
//                        navCandidatures, navParametres, navProfil, navUtilisateurs, logoutBtn;
    
//     @FXML private Label pageTitle, pageSubtitle;
//     @FXML private Label lblUserName, lblUserRole;
//     @FXML private StackPane userAvatarContainer;
    
//     private AdministrationController administrationCtrl;

//     @Override
//     public void initialize(URL url, ResourceBundle rb) {
//         Utilisateur userConnecte = UserSession.getInstance().getCurrentUser();
        
//         if (userConnecte != null) {
//             System.out.println("✅ [AdminViewController] Utilisateur: " + userConnecte.getNom());
//             lblUserName.setText(userConnecte.getNom() + " " + userConnecte.getPrenom());
//             lblUserRole.setText(userConnecte.getRole());
//             loadUserAvatar(userConnecte.getImage_profil());
//         }
        
//         logoutBtn.setOnMouseClicked(event -> logout());
//         navToStatistiques(); // Charger Administration par défaut
//         marquerNavActif(btnStatistiques);
        
//         btnStatistiques.setOnMouseClicked(event -> navToStatistiques());
//         btnOffres.setOnMouseClicked(event -> navToOffres());
//         btnServices.setOnMouseClicked(event -> navToServices());
//         btnInformations.setOnMouseClicked(event -> navToInformations());
//         navCandidatures.setOnMouseClicked(event -> navToCandidatures());
//         navParametres.setOnMouseClicked(event -> navToParametres());
//         navProfil.setOnMouseClicked(event -> navToProfil());
//     }

//     private void chargerPage(String nomPage, String subtitle) {
//         try {
//             Node page;
            
//             switch(nomPage) {
//                 case "Administration":
//                     FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdministrationView.fxml"));
//                     page = loader.load();
//                     administrationCtrl = loader.getController();
//                     break;
                    
//                 case "Statistiques":
//                     loader = new FXMLLoader(getClass().getResource("/StatistiquesView.fxml"));
//                     page = loader.load();
//                     break;
                    
//                 case "Offres":
//                     loader = new FXMLLoader(getClass().getResource("/AdminOffresView.fxml"));
//                     page = loader.load();
//                     break;
                    
//                 case "Services":
//                     loader = new FXMLLoader(getClass().getResource("/ServicesView.fxml"));
//                     page = loader.load();
//                     break;
                    
//                 case "Informations":
//                     System.out.println("⚠️ Page Informations non implémentée encore");
//                     return;
                    
//                 case "Candidatures":
//                     System.out.println("⚠️ Page Candidatures non implémentée encore");
//                     return;
                    
//                 case "Paramètres":
//                     System.out.println("⚠️ Page Paramètres non implémentée encore");
//                     return;
                    
//                 case "Profil":
//                     System.out.println("⚠️ Page Profil non implémentée encore");
//                     return;
                    
//                 default:
//                     System.out.println("⚠️ Page inconnue: " + nomPage);
//                     return;
//             }

//             contentArea.getChildren().clear();
//             contentArea.getChildren().add(page);
            
//             pageTitle.setText(nomPage);
//             pageSubtitle.setText(subtitle);

//         } catch (IOException e) {
//             System.err.println("❌ Erreur chargement page " + nomPage + ": " + e.getMessage());
//             e.printStackTrace();
//         }
//     }
    
//     private void loadUserAvatar(String imagePath) {
//         if (imagePath == null || imagePath.trim().isEmpty()) return;
        
//         try {
//             java.io.File imgFile = new java.io.File(imagePath);
//             if (imgFile.exists()) {
//                 javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView();
//                 imageView.setFitWidth(50);
//                 imageView.setFitHeight(50);
//                 imageView.setPreserveRatio(false);
                
//                 javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(25, 25, 25);
//                 imageView.setClip(clip);
                
//                 javafx.scene.image.Image img = new javafx.scene.image.Image(
//                     new java.io.FileInputStream(imgFile)
//                 );
//                 imageView.setImage(img);
                
//                 userAvatarContainer.getChildren().clear();
//                 userAvatarContainer.getChildren().add(imageView);
//             }
//         } catch (Exception e) {
//             System.err.println("✗ Erreur avatar: " + e.getMessage());
//         }
//     }

//     private void marquerNavActif(HBox navButton) {
//         btnStatistiques.getStyleClass().remove("sidebar-nav-item-active");
//         btnOffres.getStyleClass().remove("sidebar-nav-item-active");
//         btnServices.getStyleClass().remove("sidebar-nav-item-active");
//         btnInformations.getStyleClass().remove("sidebar-nav-item-active");
//         navCandidatures.getStyleClass().remove("sidebar-nav-item-active");
//         navParametres.getStyleClass().remove("sidebar-nav-item-active");
//         navProfil.getStyleClass().remove("sidebar-nav-item-active");

//         navButton.getStyleClass().add("sidebar-nav-item-active");
//     }

//     @FXML private void navToStatistiques() {
//         chargerPage("Statistiques", "Vue d'ensemble des statistiques");
//         marquerNavActif(btnStatistiques);
//     }

//     @FXML private void navToOffres() {
//         chargerPage("Offres", "Gérez vos offres d'emploi");
//         marquerNavActif(btnOffres);
//     }

//     @FXML private void navToServices() {
//         chargerPage("Services", "Gérez vos services");
//         marquerNavActif(btnServices);
//     }

//     @FXML private void navToInformations() {
//         chargerPage("Informations", "Gérez les informations");
//         marquerNavActif(btnInformations);
//     }

//     @FXML private void navToCandidatures() {
//         chargerPage("Candidatures", "Gérez les candidatures");
//         marquerNavActif(navCandidatures);
//     }

//     @FXML private void navToParametres() {
//         chargerPage("Paramètres", "Paramètres du système");
//         marquerNavActif(navParametres);
//     }

//     @FXML private void navToProfil() {
//         chargerPage("Profil", "Mon profil");
//         marquerNavActif(navProfil);
//     }

//     @FXML private void onSidebarEntered() {
//         expandSidebar();
//     }

//     @FXML private void onSidebarExited() {
//         collapseSidebar();
//     }

//     private void expandSidebar() {
//         Timeline timeline = new Timeline(
//             new KeyFrame(Duration.millis(300), new KeyValue(sidebar.prefWidthProperty(), 240))
//         );
//         timeline.play();
//         fadeInLabels();
//     }

//     private void collapseSidebar() {
//         Timeline timeline = new Timeline(
//             new KeyFrame(Duration.millis(300), new KeyValue(sidebar.prefWidthProperty(), 60))
//         );
//         timeline.play();
//         fadeOutLabels();
//     }

//     private void fadeInLabels() {
//         Label[] labels = {navStatistiquesText, navOffresText, navServicesText, 
//                          navInformationsText, navCandidaturesText, navParametresText, navProfilText,navUtilisateursText};
//         for (Label label : labels) {
//             Timeline fade = new Timeline(
//                 new KeyFrame(Duration.millis(200),
//                     new KeyValue(label.opacityProperty(), 1.0),
//                     new KeyValue(label.maxWidthProperty(), 150))
//             );
//             fade.play();
//         }
//     }

//     private void fadeOutLabels() {
//         Label[] labels = {navStatistiquesText, navOffresText, navServicesText, 
//                          navInformationsText, navCandidaturesText, navParametresText, navProfilText,navUtilisateursText};
//         for (Label label : labels) {
//             Timeline fade = new Timeline(
//                 new KeyFrame(Duration.millis(200),
//                     new KeyValue(label.opacityProperty(), 0.0),
//                     new KeyValue(label.maxWidthProperty(), 0))
//             );
//             fade.play();
//         }
//     }
    
//     @FXML private void logout() {
//         try {
//             UserSession.getInstance().clearSession();
//             FXMLLoader loader = new FXMLLoader(getClass().getResource("/SigninView.fxml"));
//             javafx.scene.Parent root = loader.load();
//             javafx.stage.Stage stage = (javafx.stage.Stage) contentArea.getScene().getWindow();
//             javafx.scene.Scene newScene = new javafx.scene.Scene(root, 1440, 1024);
//             stage.setScene(newScene);
//             stage.setResizable(false);
//             stage.setTitle("Connexion - VOS");
//             stage.centerOnScreen();
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }
// }
package controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import utilis.UserSession;

public class AdminViewController implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private VBox sidebar;
    
    // ✅ TOUS les labels de navigation
    @FXML private Label navStatistiquesText, navUtilisateursText, navOffresText, navServicesText, 
                        navInformationsText, navCandidaturesText, navParametresText, navProfilText;
    
    // ✅ TOUS les boutons de navigation
    @FXML private HBox btnStatistiques, btnUtilisateurs, btnOffres, btnServices, btnInformations,
                       navCandidatures, navParametres, navProfil, logoutBtn;
    
    // Header
    
    // Controllers
    private AdministrationController administrationCtrl;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // ✅ Charger l'utilisateur en session
        

        // ✅ Setup logout button
        logoutBtn.setOnMouseClicked(event -> logout());
        
        // ✅ Charger la première page (Statistiques)
        navToStatistiques();
        
        // ✅ TOUS les event handlers de navigation
        btnStatistiques.setOnMouseClicked(event -> navToStatistiques());
        btnUtilisateurs.setOnMouseClicked(event -> navToUtilisateurs());
        btnOffres.setOnMouseClicked(event -> navToOffres());
        btnServices.setOnMouseClicked(event -> navToServices());
        btnInformations.setOnMouseClicked(event -> navToInformations());
        navCandidatures.setOnMouseClicked(event -> navToCandidatures());
        navParametres.setOnMouseClicked(event -> navToParametres());
        navProfil.setOnMouseClicked(event -> navToProfil());
    }

    /**
     * Charge une page FXML dans le contentArea
     */
    private void chargerPage(String nomPage, String subtitle) {
        try {
            Node page;
            
            switch(nomPage) {
                // ✅ STATISTIQUES
                case "Statistiques":
                    FXMLLoader loaderStats = new FXMLLoader(getClass().getResource("/StatistiquesView.fxml"));
                    page = loaderStats.load();
                    break;
                    
                // ✅ UTILISATEURS (Administration)
                case "Utilisateurs":
                    FXMLLoader loaderAdmin = new FXMLLoader(getClass().getResource("/AdministrationView.fxml"));
                    page = loaderAdmin.load();
                    administrationCtrl = loaderAdmin.getController();
                    break;
                    
                // ✅ OFFRES
                case "Offres":
                    FXMLLoader loaderOffres = new FXMLLoader(getClass().getResource("/AdminOffresView.fxml"));
                    page = loaderOffres.load();
                    break;
                    
                // ✅ SERVICES
                case "Services":
                    FXMLLoader loaderServices = new FXMLLoader(getClass().getResource("/ServicesView.fxml"));
                    page = loaderServices.load();
                    break;
                    
                // ⚠️ À IMPLÉMENTER
                case "Informations":
                    System.out.println("⚠️ Page 'Informations' non implémentée");
                    return;
                    
                case "Candidatures":
                    FXMLLoader loaderCandidatures = new FXMLLoader(getClass().getResource("/fxml/admin/ListeCandidaturesAdmin.fxml"));
                    page = loaderCandidatures.load();
                    break;
                    
                case "Paramètres":
                    System.out.println("⚠️ Page 'Paramètres' non implémentée");
                    return;
                    
                case "Profil":
                    FXMLLoader loaderProfil = new FXMLLoader(getClass().getResource("/ProfilView.fxml"));
                    page = loaderProfil.load();
                    break;
                    
                default:
                    System.out.println("⚠️ Page inconnue: " + nomPage);
                    return;
            }

            // ✅ Vider et charger la nouvelle page
            contentArea.getChildren().clear();
            contentArea.getChildren().add(page);
            
            // ✅ Mettre à jour le titre et sous-titre du header
        } catch (IOException e) {
            System.err.println("❌ Erreur chargement page '" + nomPage + "': " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Charge l'avatar de l'utilisateur
     */

    /**
     * ✅ Marque le bouton actif et enlève l'état actif des autres
     */
    private void marquerNavActif(HBox navButton) {
        // Enlever la classe active de tous les boutons
        btnStatistiques.getStyleClass().remove("sidebar-nav-item-active");
        btnUtilisateurs.getStyleClass().remove("sidebar-nav-item-active");
        btnOffres.getStyleClass().remove("sidebar-nav-item-active");
        btnServices.getStyleClass().remove("sidebar-nav-item-active");
        btnInformations.getStyleClass().remove("sidebar-nav-item-active");
        navCandidatures.getStyleClass().remove("sidebar-nav-item-active");
        navParametres.getStyleClass().remove("sidebar-nav-item-active");
        navProfil.getStyleClass().remove("sidebar-nav-item-active");

        // Ajouter la classe active au bouton cliqué
        navButton.getStyleClass().add("sidebar-nav-item-active");
    }

    // ============ ACTIONS NAVIGATION ============

    /**
     * ✅ Navigation vers Statistiques
     */
    @FXML
    private void navToStatistiques() {
        chargerPage("Statistiques", "Vue d'ensemble des statistiques");
        marquerNavActif(btnStatistiques);
    }

    /**
     * ✅ Navigation vers Utilisateurs (Administration)
     */
    @FXML
    private void navToUtilisateurs() {
        chargerPage("Utilisateurs", "Gérez vos utilisateurs");
        marquerNavActif(btnUtilisateurs);
    }

    /**
     * ✅ Navigation vers Offres
     */
    @FXML
    private void navToOffres() {
        chargerPage("Offres", "Gérez vos offres d'emploi");
        marquerNavActif(btnOffres);
    }

    /**
     * ✅ Navigation vers Services
     */
    @FXML
    private void navToServices() {
        chargerPage("Services", "Gérez vos services");
        marquerNavActif(btnServices);
    }

    /**
     * Navigation vers Informations
     */
    @FXML
    private void navToInformations() {
        chargerPage("Informations", "Gérez les informations");
        marquerNavActif(btnInformations);
    }

    /**
     * Navigation vers Candidatures
     */
    @FXML
    private void navToCandidatures() {
        chargerPage("Candidatures", "Gérez les candidatures");
        marquerNavActif(navCandidatures);
    }

    /**
     * Navigation vers Paramètres
     */
    @FXML
    private void navToParametres() {
        chargerPage("Paramètres", "Paramètres du système");
        marquerNavActif(navParametres);
    }

    /**
     * Navigation vers Profil
     */
    @FXML
    private void navToProfil() {
        chargerPage("Profil", "Mon profil");
        marquerNavActif(navProfil);
    }

    // ============ ANIMATIONS SIDEBAR ============

    /**
     * ✅ Expand sidebar au survol
     */
    @FXML
    private void onSidebarEntered() {
        expandSidebar();
    }

    /**
     * ✅ Collapse sidebar quand la souris sort
     */
    @FXML
    private void onSidebarExited() {
        collapseSidebar();
    }

    /**
     * Animation d'expansion du sidebar
     */
    private void expandSidebar() {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(300), 
                new KeyValue(sidebar.prefWidthProperty(), 240)
            )
        );
        timeline.play();
        fadeInLabels();
    }

    /**
     * Animation de fermeture du sidebar
     */
    private void collapseSidebar() {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(300), 
                new KeyValue(sidebar.prefWidthProperty(), 60)
            )
        );
        timeline.play();
        fadeOutLabels();
    }

    /**
     * ✅ Fade in tous les labels de navigation
     */
    private void fadeInLabels() {
        // ✅ TOUS les labels inclus
        Label[] labels = {
            navStatistiquesText, navUtilisateursText, navOffresText, navServicesText, 
            navInformationsText, navCandidaturesText, navParametresText, navProfilText
        };
        
        for (Label label : labels) {
            Timeline fade = new Timeline(
                new KeyFrame(Duration.millis(200),
                    new KeyValue(label.opacityProperty(), 1.0),
                    new KeyValue(label.maxWidthProperty(), 150)
                )
            );
            fade.play();
        }
    }

    /**
     * ✅ Fade out tous les labels de navigation
     */
    private void fadeOutLabels() {
        // ✅ TOUS les labels inclus
        Label[] labels = {
            navStatistiquesText, navUtilisateursText, navOffresText, navServicesText, 
            navInformationsText, navCandidaturesText, navParametresText, navProfilText
        };
        
        for (Label label : labels) {
            Timeline fade = new Timeline(
                new KeyFrame(Duration.millis(200),
                    new KeyValue(label.opacityProperty(), 0.0),
                    new KeyValue(label.maxWidthProperty(), 0)
                )
            );
            fade.play();
        }
    }

    // ============ DÉCONNEXION ============
    
    /**
     * ✅ Déconnecte l'utilisateur et retour à la page de connexion
     */
    @FXML
    private void logout() {
        try {
            // Effacer la session
            UserSession.getInstance().clearSession();
            
            // Charger la page de connexion
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SigninView.fxml"));
            javafx.scene.Parent root = loader.load();
            
            // Changer la scène
            javafx.stage.Stage stage = (javafx.stage.Stage) contentArea.getScene().getWindow();
            javafx.scene.Scene newScene = new javafx.scene.Scene(root, 1440, 1024);
            stage.setScene(newScene);
            stage.setResizable(false);
            stage.setTitle("Connexion - VOS");
            stage.centerOnScreen();
            
            System.out.println("✓ Déconnexion réussie");
        } catch (Exception e) {
            System.err.println("❌ Erreur déconnexion: " + e.getMessage());
            e.printStackTrace();
        }
    }
}