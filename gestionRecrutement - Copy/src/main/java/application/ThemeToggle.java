package application;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.animation.TranslateTransition;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Composant ThemeToggle animé pour JavaFX
 * Crée un toggle switch moderne avec animation de glissement et changement d'icône
 */
public class ThemeToggle extends StackPane {

    private static final double WIDTH = 50;
    private static final double HEIGHT = 26;
    private static final double CIRCLE_RADIUS = 11;
    private static final double ANIMATION_DURATION = 300;

    // Couleurs Light Mode
    private static final String LIGHT_BACKGROUND = "#E2E8F0";
    private static final String LIGHT_CIRCLE = "#FFFFFF";
    private static final String LIGHT_ICON = "☀️";
    private static final String LIGHT_ICON_COLOR = "#FFD700";

    // Couleurs Dark Mode
    private static final String DARK_BACKGROUND = "#3B5998";
    private static final String DARK_CIRCLE = "#FFFFFF";
    private static final String DARK_ICON = "🌙";
    private static final String DARK_ICON_COLOR = "#60A5FA";

    private Rectangle background;
    private Circle circle;
    private Label iconLabel;
    private Scene scene;
    
    // BooleanProperty pour réagir aux changements d'état
    private BooleanProperty darkModeProperty;

    public ThemeToggle() {
        darkModeProperty = new SimpleBooleanProperty(false);
        initialize();
    }

    public ThemeToggle(Scene scene) {
        this.scene = scene;
        darkModeProperty = new SimpleBooleanProperty(false);
        initialize();
    }

    private void initialize() {
        // Configuration du conteneur principal
        this.setPrefSize(WIDTH, HEIGHT);
        this.setStyle("-fx-cursor: hand;");

        // Fond arrondi (Rectangle)
        background = new Rectangle(WIDTH, HEIGHT);
        background.setArcWidth(12);
        background.setArcHeight(12);
        background.setFill(Color.web(LIGHT_BACKGROUND));
        background.setStyle("-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.15), 4, 0, 0, 2);");

        // Cercle blanc qui glisse
        circle = new Circle(CIRCLE_RADIUS);
        circle.setFill(Color.web(LIGHT_CIRCLE));
        circle.setTranslateX(-WIDTH / 4);

        // Label pour l'icône (soleil ou lune)
        iconLabel = new Label(LIGHT_ICON);
        iconLabel.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-text-fill: " + LIGHT_ICON_COLOR + "; " +
            "-fx-alignment: center;"
        );

        // Ajouter l'icône au cercle
        StackPane.setAlignment(circle, Pos.CENTER);
        StackPane.setAlignment(background, Pos.CENTER);

        this.getChildren().addAll(background, circle);
        StackPane iconContainer = new StackPane(iconLabel);
        iconContainer.setMouseTransparent(true);
        this.getChildren().add(iconContainer);

        // Événement de clic
        this.setOnMouseClicked(this::handleToggleClick);

        // Hover effect
        this.setOnMouseEntered(e -> {
            background.setStyle(
                "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.25), 8, 0, 0, 4);"
            );
        });

        this.setOnMouseExited(e -> {
            background.setStyle(
                "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.15), 4, 0, 0, 2);"
            );
        });
    }

    private void handleToggleClick(MouseEvent event) {
        boolean newDarkMode = !darkModeProperty.get();
        darkModeProperty.set(newDarkMode);
        animateToggle();
        
        // Changer le thème si la scene est disponible
        if (scene != null) {
            switchTheme();
        }
    }

    private void switchTheme() {
        try {
            String lightThemePath = getClass().getResource("/styles/light-theme.css").toExternalForm();
            String darkThemePath = getClass().getResource("/styles/dark-theme.css").toExternalForm();

            // Utiliser clear() puis add() pour s'assurer que le thème change
            scene.getStylesheets().clear();
            
            if (darkModeProperty.get()) {
                // Passer au mode sombre
                scene.getStylesheets().add(darkThemePath);
            } else {
                // Passer au mode clair
                scene.getStylesheets().add(lightThemePath);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du changement de thème: " + e.getMessage());
        }
    }

    private void animateToggle() {
        // Animation de translation du cercle
        TranslateTransition translateTransition = new TranslateTransition(
            Duration.millis(ANIMATION_DURATION),
            circle
        );
        
        if (darkModeProperty.get()) {
            translateTransition.setToX(WIDTH / 4);
        } else {
            translateTransition.setToX(-WIDTH / 4);
        }
        translateTransition.play();

        // Animation de changement de couleur du fond
        animateBackgroundColor();

        // Animation de changement d'icône
        animateIcon();
    }

    private void animateBackgroundColor() {
        // Fade out l'ancienne couleur
        FadeTransition fadeOut = new FadeTransition(Duration.millis(ANIMATION_DURATION / 2), background);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.5);

        fadeOut.setOnFinished(e -> {
            // Changer la couleur
            if (darkModeProperty.get()) {
                background.setFill(Color.web(DARK_BACKGROUND));
            } else {
                background.setFill(Color.web(LIGHT_BACKGROUND));
            }

            // Fade in la nouvelle couleur
            FadeTransition fadeIn = new FadeTransition(Duration.millis(ANIMATION_DURATION / 2), background);
            fadeIn.setFromValue(0.5);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        fadeOut.play();
    }

    private void animateIcon() {
        // Fade out
        FadeTransition fadeOut = new FadeTransition(Duration.millis(ANIMATION_DURATION / 2), iconLabel);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(e -> {
            // Changer l'icône et la couleur
            if (darkModeProperty.get()) {
                iconLabel.setText(DARK_ICON);
                iconLabel.setStyle(
                    "-fx-font-size: 14px; " +
                    "-fx-text-fill: " + DARK_ICON_COLOR + "; " +
                    "-fx-alignment: center;"
                );
            } else {
                iconLabel.setText(LIGHT_ICON);
                iconLabel.setStyle(
                    "-fx-font-size: 14px; " +
                    "-fx-text-fill: " + LIGHT_ICON_COLOR + "; " +
                    "-fx-alignment: center;"
                );
            }

            // Fade in
            FadeTransition fadeIn = new FadeTransition(Duration.millis(ANIMATION_DURATION / 2), iconLabel);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        fadeOut.play();
    }

    /**
     * Définir la scene pour le changement de thème
     */
    public void setScene(Scene scene) {
        this.scene = scene;
    }

    /**
     * Obtenir la BooleanProperty du mode sombre
     */
    public BooleanProperty darkModeProperty() {
        return darkModeProperty;
    }

    /**
     * Obtenir l'état du mode sombre
     */
    public boolean isDarkMode() {
        return darkModeProperty.get();
    }

    /**
     * Définir le mode sombre programmatiquement
     */
    public void setDarkMode(boolean darkMode) {
        if (this.darkModeProperty.get() != darkMode) {
            this.darkModeProperty.set(darkMode);
            animateToggle();
            if (scene != null) {
                switchTheme();
            }
        }
    }
}
