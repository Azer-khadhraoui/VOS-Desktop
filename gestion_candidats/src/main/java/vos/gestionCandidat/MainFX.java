/*package vos.gestionCandidat;

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
}*/

package vos.gestionCandidat;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import vos.gestionCandidat.controllers.MainViewController;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charger la MainView qui contient le SPA
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/utilisateur/MainView.fxml"));
        Parent root = loader.load();

        // Passer le contrôleur principal
        MainViewController controller = loader.getController();
        // Si vous avez une session utilisateur, passez l'ID ici
        controller.setIdUtilisateurCourant(3); // À remplacer par la vraie session

        Scene scene = new Scene(root, 1300, 800);
        primaryStage.setTitle("VOS — Gestion Candidatures");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}