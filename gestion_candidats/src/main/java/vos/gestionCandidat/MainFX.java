package vos.gestionCandidat;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/TestLauncher.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        primaryStage.setTitle("VOS — Test Gestion des Candidatures");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}