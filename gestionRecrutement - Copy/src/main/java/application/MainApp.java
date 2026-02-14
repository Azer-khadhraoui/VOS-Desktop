package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Charger la vue principale
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainView.fxml"));
            Parent root = loader.load();

            // Créer la scène
            Scene scene = new Scene(root);

            // Configurer la fenêtre
            primaryStage.setTitle("Gestion de Recrutement - VOS");
            primaryStage.setScene(scene);
            primaryStage.setWidth(1400);
            primaryStage.setHeight(800);
            primaryStage.setResizable(true);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de l'application: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
