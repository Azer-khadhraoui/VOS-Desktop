package controllers;

import java.io.File;
import java.io.FileInputStream;

import entities.Utilisateur;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import services.ServiceUtilisateur;
import utilis.UserSession;

public class ProfilController {

    @FXML private Label lblNom, lblEmail, lblRole, lblStatCandidatures, lblStatus, lblIdUtilisateur;
    @FXML private TextField tfNom, tfPrenom, tfEmail, tfMotDePasse;
    @FXML private Button btnModifier;
    @FXML private ImageView imgProfil;
    @FXML private VBox formContainer;
    
    private ServiceUtilisateur serviceUtilisateur = new ServiceUtilisateur();
    private Utilisateur utilisateur;

    @FXML
    public void initialize() {
        System.out.println("=== PROFIL CONTROLLER INITIALIZE ===");
        
        // Debug: vérifier les composants FXML
        System.out.println("lblNom: " + (lblNom != null ? "OK" : "NULL"));
        System.out.println("lblEmail: " + (lblEmail != null ? "OK" : "NULL"));
        System.out.println("lblRole: " + (lblRole != null ? "OK" : "NULL"));
        System.out.println("lblIdUtilisateur: " + (lblIdUtilisateur != null ? "OK" : "NULL"));
        System.out.println("imgProfil: " + (imgProfil != null ? "OK" : "NULL"));
        
        utilisateur = UserSession.getInstance().getCurrentUser();
        
        if (utilisateur != null) {
            System.out.println("Utilisateur trouvé:");
            System.out.println("  - ID: " + utilisateur.getId_utilisateur());
            System.out.println("  - Nom: " + utilisateur.getNom());
            System.out.println("  - Prénom: " + utilisateur.getPrenom());
            System.out.println("  - Email: " + utilisateur.getEmail());
            System.out.println("  - Role: " + utilisateur.getRole());
            System.out.println("  - Image: " + utilisateur.getImage_profil());
            afficherProfil();
            setupValidation();
        } else {
            System.err.println("ERREUR: Utilisateur null dans UserSession!");
            showError("Erreur", "Utilisateur non trouvé! Veuillez vous reconnecter.");
        }
    }

    private void setupValidation() {
        // Validation Nom - Lettres uniquement
        tfNom.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !newValue.matches("[a-zA-ZÀ-ÿ\\s-]+")) {
                tfNom.setText(oldValue);
            }
            if (newValue.length() > 50) {
                tfNom.setText(oldValue);
            }
        });

        // Validation Prénom - Lettres uniquement
        tfPrenom.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !newValue.matches("[a-zA-ZÀ-ÿ\\s-]+")) {
                tfPrenom.setText(oldValue);
            }
            if (newValue.length() > 50) {
                tfPrenom.setText(oldValue);
            }
        });

        // Validation Email - Limite de caractères
        tfEmail.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 100) {
                tfEmail.setText(oldValue);
            }
        });

        // Validation Mot de passe - Limite de caractères
        tfMotDePasse.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 50) {
                tfMotDePasse.setText(oldValue);
            }
        });
    }

    private void afficherProfil() {
        System.out.println("=== AFFICHER PROFIL ===");
        
        try {
            // Afficher nom complet
            String nomComplet = (utilisateur.getNom() != null ? utilisateur.getNom() : "") + " " + 
                               (utilisateur.getPrenom() != null ? utilisateur.getPrenom() : "");
            nomComplet = nomComplet.trim();
            if (nomComplet.isEmpty()) nomComplet = "Utilisateur";
            
            lblNom.setText(nomComplet);
            System.out.println("✓ Nom affiché: " + nomComplet);
            
            // Afficher email
            String email = utilisateur.getEmail() != null ? utilisateur.getEmail() : "Non renseigné";
            lblEmail.setText(email);
            System.out.println("✓ Email affiché: " + email);
            
            // Afficher role
            String role = utilisateur.getRole() != null ? utilisateur.getRole().toUpperCase() : "CLIENT";
            lblRole.setText(role);
            System.out.println("✓ Role affiché: " + role);
            
            // Afficher ID utilisateur
            lblIdUtilisateur.setText("#" + utilisateur.getId_utilisateur());
            System.out.println("✓ ID utilisateur affiché: #" + utilisateur.getId_utilisateur());
            
            // Charger l'image si elle existe
            if (utilisateur.getImage_profil() != null && !utilisateur.getImage_profil().trim().isEmpty()) {
                try {
                    File imageFile = new File(utilisateur.getImage_profil());
                    System.out.println("Tentative de chargement de l'image:");
                    System.out.println("  - Chemin: " + utilisateur.getImage_profil());
                    System.out.println("  - Chemin absolu: " + imageFile.getAbsolutePath());
                    System.out.println("  - Existe: " + imageFile.exists());
                    System.out.println("  - Est un fichier: " + imageFile.isFile());
                    
                    if (imageFile.exists() && imageFile.isFile()) {
                        Image image = new Image(new FileInputStream(imageFile));
                        imgProfil.setImage(image);
                        System.out.println("✓ Image chargée avec succès!");
                    } else {
                        System.out.println("⚠ Fichier image introuvable ou invalide");
                        // L'image reste le cercle gris par défaut défini dans le FXML
                    }
                } catch (Exception e) {
                    System.err.println("✗ Erreur chargement image: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("ℹ Aucune image de profil définie (NULL ou vide)");
                System.out.println("  - image_profil value: [" + utilisateur.getImage_profil() + "]");
                // L'image reste le cercle gris par défaut
            }
            
            // Remplir les champs du formulaire
            tfNom.setText(utilisateur.getNom() != null ? utilisateur.getNom() : "");
            tfPrenom.setText(utilisateur.getPrenom() != null ? utilisateur.getPrenom() : "");
            tfEmail.setText(utilisateur.getEmail() != null ? utilisateur.getEmail() : "");
            tfMotDePasse.setText(utilisateur.getMot_de_passe() != null ? utilisateur.getMot_de_passe() : "");
            
            // Statistiques - Afficher avec le texte
            int nbCandidatures = 4; // À remplacer par une vraie requête DB plus tard
            String texteCandidatures = nbCandidatures + (nbCandidatures <= 1 ? " candidature" : " candidatures");
            lblStatCandidatures.setText(texteCandidatures);
            System.out.println("✓ Statistiques affichées: " + texteCandidatures);
            
            System.out.println("===== PROFIL AFFICHÉ AVEC SUCCÈS! =====");
            
        } catch (Exception e) {
            System.err.println("ERREUR dans afficherProfil: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void activerEdition() {
        formContainer.setVisible(true);
        formContainer.setManaged(true);
        
        // Animation d'apparition
        formContainer.setOpacity(0);
        formContainer.setTranslateY(20);
        
        FadeTransition fade = new FadeTransition(Duration.millis(300), formContainer);
        fade.setFromValue(0);
        fade.setToValue(1);
        
        TranslateTransition slide = new TranslateTransition(Duration.millis(300), formContainer);
        slide.setFromY(20);
        slide.setToY(0);
        
        ParallelTransition parallel = new ParallelTransition(fade, slide);
        parallel.play();
    }

    @FXML
    public void uploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une photo de profil");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png"),
            new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );
        
        File selectedFile = fileChooser.showOpenDialog(imgProfil.getScene().getWindow());
        if (selectedFile != null) {
            // Vérifier la taille (5MB max)
            if (selectedFile.length() > 5 * 1024 * 1024) {
                showError("Fichier trop gros", "La photo ne doit pas dépasser 5 MB");
                return;
            }
            
            try {
                // Charger et afficher immédiatement l'image
                Image image = new Image(new FileInputStream(selectedFile));
                imgProfil.setImage(image);
                System.out.println("✓ Photo sélectionnée: " + selectedFile.getAbsolutePath());
                
                // SAUVEGARDER IMMÉDIATEMENT EN BASE DE DONNÉES
                String imagePath = selectedFile.getAbsolutePath();
                
                Utilisateur utilisateurModifie = new Utilisateur(
                    utilisateur.getId_utilisateur(),
                    imagePath,
                    utilisateur.getEmail(),
                    utilisateur.getMot_de_passe(),
                    utilisateur.getRole(),
                    utilisateur.getNom(),
                    utilisateur.getPrenom()
                );
                
                serviceUtilisateur.modifier(utilisateurModifie);
                
                // Mettre à jour la session
                utilisateur = utilisateurModifie;
                UserSession.getInstance().setCurrentUser(utilisateur);
                
                System.out.println("✓ Photo sauvegardée en base de données!");
                
                // Afficher une alerte de succès
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Photo mise à jour");
                successAlert.setHeaderText("✅ Photo de profil enregistrée");
                successAlert.setContentText("Votre nouvelle photo de profil a été enregistrée avec succès!");
                successAlert.showAndWait();
                
            } catch (Exception e) {
                System.err.println("✗ Erreur chargement/sauvegarde image: " + e.getMessage());
                e.printStackTrace();
                showError("Erreur", "Impossible de charger ou sauvegarder l'image: " + e.getMessage());
            }
        } else {
            System.out.println("ℹ Sélection d'image annulée");
        }
    }

    @FXML
    public void annulerEdition() {
        formContainer.setVisible(false);
        formContainer.setManaged(false);
        afficherProfil();
    }

    @FXML
    public void sauvegarderModifications() {
        // Validation Nom
        if (tfNom.getText().trim().isEmpty()) {
            showError("Erreur de saisie", "Le nom est obligatoire!");
            tfNom.requestFocus();
            return;
        }
        
        if (tfNom.getText().trim().length() < 2) {
            showError("Erreur de saisie", "Le nom doit contenir au moins 2 caractères!");
            tfNom.requestFocus();
            return;
        }

        // Validation Prénom
        if (tfPrenom.getText().trim().isEmpty()) {
            showError("Erreur de saisie", "Le prénom est obligatoire!");
            tfPrenom.requestFocus();
            return;
        }
        
        if (tfPrenom.getText().trim().length() < 2) {
            showError("Erreur de saisie", "Le prénom doit contenir au moins 2 caractères!");
            tfPrenom.requestFocus();
            return;
        }

        // Validation Email
        if (tfEmail.getText().trim().isEmpty()) {
            showError("Erreur de saisie", "L'email est obligatoire!");
            tfEmail.requestFocus();
            return;
        }

        if (!tfEmail.getText().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            showError("Erreur de saisie", "Format d'email invalide!\n\nExemple: utilisateur@exemple.com");
            tfEmail.requestFocus();
            return;
        }

        // Validation Mot de passe
        if (tfMotDePasse.getText().trim().isEmpty()) {
            showError("Erreur de saisie", "Le mot de passe est obligatoire!");
            tfMotDePasse.requestFocus();
            return;
        }
        
        if (tfMotDePasse.getText().trim().length() < 6) {
            showError("Erreur de saisie", "Le mot de passe doit contenir au moins 6 caractères!");
            tfMotDePasse.requestFocus();
            return;
        }

        // Créer utilisateur modifié (garder la même image)
        Utilisateur utilisateurModifie = new Utilisateur(
            utilisateur.getId_utilisateur(),
            utilisateur.getImage_profil(), // On garde l'image actuelle
            tfEmail.getText().trim(),
            tfMotDePasse.getText(),
            utilisateur.getRole(),
            tfNom.getText().trim(),
            tfPrenom.getText().trim()
        );

        try {
            // Sauvegarder
            serviceUtilisateur.modifier(utilisateurModifie);
            
            // Mettre à jour la session
            utilisateur = utilisateurModifie;
            UserSession.getInstance().setCurrentUser(utilisateur);
            
            System.out.println("✓ Profil modifié avec succès!");
            
            // Afficher succès
            lblStatus.setText("✅ Profil modifié avec succès!");
            lblStatus.setVisible(true);
            lblStatus.setManaged(true);
            
            // Rafraîchir et fermer le formulaire après 1.5s
            PauseTransition pause = new PauseTransition(Duration.millis(1500));
            pause.setOnFinished(e -> {
                afficherProfil();
                annulerEdition();
                lblStatus.setVisible(false);
                lblStatus.setManaged(false);
            });
            pause.play();
            
        } catch (Exception e) {
            System.err.println("✗ Erreur sauvegarde: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur", "Impossible de sauvegarder les modifications:\n" + e.getMessage());
        }
    }

    @FXML
    public void supprimerCompte() {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Suppression de compte");
        confirmDialog.setHeaderText("⚠️ Attention!");
        confirmDialog.setContentText(
            "Êtes-vous sûr de vouloir supprimer votre compte ?\n\n" +
            "Cette action est irréversible."
        );
        
        ButtonType btnOui = new ButtonType("Oui, supprimer", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnNon = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmDialog.getButtonTypes().setAll(btnOui, btnNon);
        
        if (confirmDialog.showAndWait().orElse(btnNon) == btnOui) {
            // Deuxième confirmation avec email
            TextInputDialog inputDialog = new TextInputDialog();
            inputDialog.setTitle("Confirmation finale");
            inputDialog.setHeaderText("Confirmez la suppression");
            inputDialog.setContentText("Tapez votre email pour confirmer:\n" + utilisateur.getEmail());
            
            inputDialog.showAndWait().ifPresent(email -> {
                if (email.equals(utilisateur.getEmail())) {
                    // Supprimer le compte
                    serviceUtilisateur.supprimer(utilisateur.getId_utilisateur());
                    System.out.println("✓ Compte supprimé: " + utilisateur.getEmail());
                    
                    // Déconnecter l'utilisateur
                    UserSession.getInstance().setCurrentUser(null);
                    System.out.println("✓ Utilisateur déconnecté");
                    
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Compte supprimé");
                    successAlert.setHeaderText("✅ Au revoir!");
                    successAlert.setContentText("Votre compte a été supprimé avec succès.\n\nVous allez être redirigé vers la page de connexion.");
                    successAlert.showAndWait();
                    
                    // Rediriger vers la page de connexion
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/SigninView.fxml"));
                        Parent root = loader.load();
                        Scene scene = lblNom.getScene();
                        scene.setRoot(root);
                        System.out.println("✓ Redirection vers SigninView");
                    } catch (Exception e) {
                        System.err.println("✗ Erreur redirection: " + e.getMessage());
                        e.printStackTrace();
                        showError("Erreur", "Impossible de retourner à la page de connexion");
                    }
                } else {
                    showError("Erreur", "Email incorrect!");
                }
            });
        }
    }

    @FXML
    public void retournerOffres() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/AdminView.fxml"));
            Parent root = loader.load();
            Scene scene = lblNom.getScene();
            scene.setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de retourner aux offres");
        }
    }
    
    /**
     * Logs out the current user and returns to signin page.
     */
    @FXML
    public void logout() {
        try {
            // Show confirmation dialog
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Déconnexion");
            alert.setHeaderText(null);
            alert.setContentText("Êtes-vous sûr de vouloir vous déconnecter ?");
            
            java.util.Optional<ButtonType> result = alert.showAndWait();
            
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Clear user session
                UserSession.getInstance().clearSession();
                
                // Navigate to SigninView
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/SigninView.fxml"));
                Parent root = loader.load();
                
                Scene scene = lblNom.getScene();
                scene.setRoot(root);
                
                System.out.println("✓ Déconnexion réussie");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de déconnexion", "Impossible de se déconnecter : " + e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
