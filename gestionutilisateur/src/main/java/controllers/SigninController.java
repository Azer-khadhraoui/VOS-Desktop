package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.ServiceUtilisateur;

public class SigninController {

    @FXML private TextField tfEmail;
    @FXML private PasswordField tfPassword;
    @FXML private Label lblMessage;

    ServiceUtilisateur su = new ServiceUtilisateur();

    @FXML
    public void signin() {

        if (su.login(tfEmail.getText(), tfPassword.getText())) {
            lblMessage.setStyle("-fx-text-fill: green;");
            lblMessage.setText("Connexion réussie !");
        } else {
            lblMessage.setText("Email ou mot de passe incorrect");
        }
    }

    @FXML
    public void goToSignup() {
        try {
            Stage stage = (Stage) tfEmail.getScene().getWindow();
            Scene scene = new Scene(
                    FXMLLoader.load(getClass().getResource("/SignupView.fxml")),
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
