package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Interpolator;
import javafx.util.Duration;
import javafx.geometry.Bounds;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the cinematic splash screen with logo reveal animation
 */
public class SplashScreenController implements Initializable {

    @FXML
    private StackPane root;
    
    @FXML
    private ImageView logoImageView;
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private Circle glowCircle;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        playLogoAnimation();
    }

    private void playLogoAnimation() {
        // ========================================
        // 1. INITIAL STATE - Logo is invisible, scaled down, and blurred
        // ========================================
        logoImageView.setOpacity(0.0);
        logoImageView.setScaleX(0.75);
        logoImageView.setScaleY(0.75);
        GaussianBlur initialBlur = new GaussianBlur(20.0);
        logoImageView.setEffect(initialBlur);
        
        // Welcome text is invisible initially
        welcomeLabel.setOpacity(0.0);
        welcomeLabel.setTranslateY(15.0);
        
        // Glow circle is invisible
        glowCircle.setOpacity(0.0);
        glowCircle.setRadius(40.0);
        glowCircle.setFill(createRadialGradient());

        // ========================================
        // 2. CREATE TIMELINE WITH KEYFRAMES
        // ========================================
        Timeline timeline = new Timeline();
        timeline.setCycleCount(1);
        timeline.setAutoReverse(false);

        // ========================================
        // PHASE 1 (0ms - 200ms): Delay, no animation
        // ========================================
        timeline.getKeyFrames().add(
            new KeyFrame(Duration.millis(0))
        );

        // ========================================
        // PHASE 2 (200ms - 1400ms): Logo reveal animation (1.2s)
        // Opacity: 0 → 1, Scale: 0.75 → 1.0, Blur: 20 → 0
        // ========================================
        timeline.getKeyFrames().add(
            new KeyFrame(
                Duration.millis(1400),
                new KeyValue(logoImageView.opacityProperty(), 1.0, Interpolator.EASE_OUT),
                new KeyValue(logoImageView.scaleXProperty(), 1.0, Interpolator.EASE_OUT),
                new KeyValue(logoImageView.scaleYProperty(), 1.0, Interpolator.EASE_OUT),
                new KeyValue(((GaussianBlur) logoImageView.getEffect()).radiusProperty(), 0.0, Interpolator.EASE_OUT)
            )
        );

        // ========================================
        // PHASE 3 (1400ms - 2400ms): Glow pulse effect (1.0s)
        // Glow expands from small to large and fades in
        // ========================================
        timeline.getKeyFrames().add(
            new KeyFrame(
                Duration.millis(1400),
                new KeyValue(glowCircle.opacityProperty(), 0.6, Interpolator.EASE_OUT),
                new KeyValue(glowCircle.radiusProperty(), 40.0, Interpolator.EASE_OUT)
            )
        );

        timeline.getKeyFrames().add(
            new KeyFrame(
                Duration.millis(2400),
                new KeyValue(glowCircle.opacityProperty(), 0.0, Interpolator.EASE_IN),
                new KeyValue(glowCircle.radiusProperty(), 150.0, Interpolator.EASE_IN)
            )
        );

        // ========================================
        // PHASE 4 (2400ms - 3000ms): Welcome text appears (600ms)
        // Opacity: 0 → 1, Translation Y: 15px → 0px (slides up)
        // ========================================
        timeline.getKeyFrames().add(
            new KeyFrame(
                Duration.millis(2400),
                new KeyValue(welcomeLabel.opacityProperty(), 0.0, Interpolator.EASE_IN),
                new KeyValue(welcomeLabel.translateYProperty(), 15.0, Interpolator.EASE_IN)
            )
        );

        timeline.getKeyFrames().add(
            new KeyFrame(
                Duration.millis(3000),
                new KeyValue(welcomeLabel.opacityProperty(), 1.0, Interpolator.EASE_OUT),
                new KeyValue(welcomeLabel.translateYProperty(), 0.0, Interpolator.EASE_OUT)
            )
        );

        timeline.play();
    }

    /**
     * Create a radial gradient for the glow effect
     * Blue glow that fades to transparent
     */
    private RadialGradient createRadialGradient() {
        Stop[] stops = new Stop[]{
            new Stop(0.0, Color.web("#4B83F2").interpolate(Color.TRANSPARENT, 0.3)),
            new Stop(0.5, Color.web("#4B83F2").interpolate(Color.TRANSPARENT, 0.6)),
            new Stop(1.0, Color.TRANSPARENT)
        };
        return new RadialGradient(0, 0, 0.5, 0.5, 1.0, true, CycleMethod.NO_CYCLE, stops);
    }
}
