package controllers;

import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.ServiceUtilisateur;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class SignupController {

    // ===== Champs du formulaire =====
    @FXML private TextField tfNom;
    @FXML private TextField tfPrenom;
    @FXML private TextField tfEmail;
    @FXML private PasswordField tfPassword;

    @FXML private Label lblMessage;

    // ===== Image sélectionnée =====
    private String imageName = "default.png";

    // ===== Service =====
    ServiceUtilisateur su = new ServiceUtilisateur();

    // =====================================================
    // 📷 Upload Image
    // =====================================================
    @FXML
    public void uploadImage() {

        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Images", "*.png", "*.jpg", "*.jpeg"
                )
        );

        File file = fc.showOpenDialog(null);

        if (file != null) {
            try {
                // Créer dossier local "images"
                File folder = new File("images");
                if (!folder.exists()) folder.mkdir();

                // Copier l’image dans le dossier
                Path dest = Path.of("images", file.getName());
                Files.copy(file.toPath(), dest,
                        StandardCopyOption.REPLACE_EXISTING);

                // Sauvegarder le nom
                imageName = file.getName();

                lblMessage.setStyle("-fx-text-fill: green;");
                lblMessage.setText("✅ Photo sélectionnée avec succès");

            } catch (Exception e) {
                lblMessage.setStyle("-fx-text-fill: red;");
                lblMessage.setText("❌ Erreur upload image");
                e.printStackTrace();
            }
        }
    }

    // =====================================================
    // ✨ Signup (Inscription)
    // =====================================================
    @FXML
    public void signup() {

        // Vérification champs obligatoires
        if (tfNom.getText().isEmpty() ||
                tfPrenom.getText().isEmpty() ||
                tfEmail.getText().isEmpty() ||
                tfPassword.getText().isEmpty()) {

            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("❌ Veuillez remplir tous les champs !");
            return;
        }

        // Vérifier email déjà utilisé
        if (su.emailExiste(tfEmail.getText())) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("❌ Email déjà utilisé !");
            return;
        }

        // Créer utilisateur
        Utilisateur u = new Utilisateur(
                0,
                imageName,
                tfEmail.getText(),
                tfPassword.getText(),
                "CLIENT",
                tfNom.getText(),
                tfPrenom.getText()
        );

        // Ajouter dans la BD
        su.ajouter(u);

        lblMessage.setStyle("-fx-text-fill: green;");
        lblMessage.setText("✨ Inscription réussie !");

        // Optionnel : vider les champs
        tfNom.clear();
        tfPrenom.clear();
        tfEmail.clear();
        tfPassword.clear();
    }

    // =====================================================
    // 🔙 Retour vers Signin
    // =====================================================
    @FXML
    public void goToSignin() {

        try {
            Stage stage = (Stage) tfEmail.getScene().getWindow();

            Scene scene = new Scene(
                    FXMLLoader.load(
                            getClass().getResource("/SigninView.fxml")
                    ),
                    1440, 1024
            );

            scene.getStylesheets().add(
                    getClass().getResource("/styleUser.css").toExternalForm()
            );

            stage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
