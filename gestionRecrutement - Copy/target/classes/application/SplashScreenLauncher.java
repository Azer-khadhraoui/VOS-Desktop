package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

/**
 * Splash screen launcher with transition to main application
 */
public class SplashScreenLauncher {

    private Stage primaryStage;
    private Runnable onSplashComplete;

    public SplashScreenLauncher(Stage primaryStage, Runnable onSplashComplete) {
        this.primaryStage = primaryStage;
        this.onSplashComplete = onSplashComplete;
    }

    /**
     * Show the splash screen then execute callback when animation completes
     */
    public void showSplashScreen() {
        try {
            FXMLLoader splashLoader = new FXMLLoader(
                getClass().getResource("/views/SplashScreen.fxml")
            );
            Parent splashRoot = splashLoader.load();
            Scene splashScene = new Scene(splashRoot, 1440, 1024);
            
            // Show splash screen without decorations
            primaryStage.setScene(splashScene);
            primaryStage.setResizable(false);
            primaryStage.setWidth(1440);
            primaryStage.setHeight(1024);
            primaryStage.show();

            // After splash animation (3 seconds total), load main app
            Timeline transitionTimer = new Timeline(
                new KeyFrame(Duration.millis(3200), event -> transitionToMainApp())
            );
            transitionTimer.setCycleCount(1);
            transitionTimer.setAutoReverse(false);
            transitionTimer.play();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement du splash screen: " + e.getMessage());
            // Fallback: load main app directly
            transitionToMainApp();
        }
    }

    /**
     * Transition from splash screen to main application
     */
    private void transitionToMainApp() {
        try {
            FXMLLoader mainLoader = new FXMLLoader(
                getClass().getResource("/views/MainView.fxml")
            );
            Parent mainRoot = mainLoader.load();
            Scene mainScene = new Scene(mainRoot);

            // Update stage
            primaryStage.setTitle("Gestion de Recrutement - VOS");
            primaryStage.setScene(mainScene);
            primaryStage.setWidth(1440);
            primaryStage.setHeight(1024);
            primaryStage.setResizable(true);

            // Execute callback if provided
            if (onSplashComplete != null) {
                onSplashComplete.run();
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de l'application: " + e.getMessage());
        }
    }
}
