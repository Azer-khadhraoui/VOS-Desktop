package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import java.io.ByteArrayInputStream;
import java.io.File;

public class WebcamController {

    @FXML private ImageView webcamPreview;
    @FXML private Label lblStatus;
    @FXML private Button btnCapture;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private StackPane faceDetectionOverlay;

    private VideoCapture camera;
    private CascadeClassifier faceDetector;
    private Thread webcamThread;
    private boolean isRunning = false;
    private String userImagePath;
    private WebcamCallback callback;
    private Mat lastCapturedFace = null;
    private static boolean opencvLoaded = false;

    public interface WebcamCallback {
        void onSuccess(String email);
        void onFailure(String message);
    }

    public void initialize() {
        loadOpenCV();
    }
    
    private void loadOpenCV() {
        if (!opencvLoaded) {
            try {
                String dllPath = new File("lib/opencv/build/java/x64/opencv_java4120.dll").getAbsolutePath();
                System.load(dllPath);
                opencvLoaded = true;
                System.out.println("✓ OpenCV chargé: " + dllPath);
            } catch (Exception e) {
                System.err.println("✗ Erreur chargement OpenCV: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void setUserImagePath(String imagePath) {
        this.userImagePath = imagePath;
    }

    public void setCallback(WebcamCallback callback) {
        this.callback = callback;
    }

    public void startWebcam() {
        try {
            // Charger le détecteur de visages
            String haarPath = new File("lib/opencv/build/etc/haarcascades/haarcascade_frontalface_default.xml").getAbsolutePath();
            faceDetector = new CascadeClassifier(haarPath);

            if (faceDetector.empty()) {
                showError("✗ Impossible de charger le détecteur de visages");
                return;
            }

            // Ouvrir la caméra
            camera = new VideoCapture(0);

            if (!camera.isOpened()) {
                showError("✗ Impossible d'ouvrir la webcam");
                return;
            }

            isRunning = true;
            startWebcamStream();

        } catch (Exception e) {
            showError("✗ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void startWebcamStream() {
        webcamThread = new Thread(() -> {
            Mat frame = new Mat();

            while (isRunning) {
                try {
                    if (camera.read(frame) && !frame.empty()) {
                        // Détecter les visages
                        MatOfRect faceDetections = new MatOfRect();
                        Mat grayFrame = new Mat();
                        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
                        Imgproc.equalizeHist(grayFrame, grayFrame);

                        faceDetector.detectMultiScale(grayFrame, faceDetections, 1.1, 5, 0,
                                new Size(100, 100), new Size());

                        // Dessiner un rectangle autour des visages détectés
                        boolean faceDetected = false;
                        for (Rect rect : faceDetections.toArray()) {
                            Imgproc.rectangle(frame, rect.tl(), rect.br(), new Scalar(76, 175, 80), 3);
                            faceDetected = true;

                            // Sauvegarder le dernier visage détecté
                            lastCapturedFace = new Mat(grayFrame, rect);
                        }

                        // Mettre à jour l'overlay
                        final boolean detected = faceDetected;
                        Platform.runLater(() -> {
                            faceDetectionOverlay.setVisible(detected);
                            if (detected) {
                                lblStatus.setText("✓ Visage détecté ! Cliquez sur Capturer");
                                lblStatus.setStyle("-fx-font-size: 16px; -fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                            } else {
                                lblStatus.setText("Positionnez votre visage face à la caméra");
                                lblStatus.setStyle("-fx-font-size: 16px; -fx-text-fill: rgba(255,255,255,0.9); -fx-font-weight: 500;");
                            }
                        });

                        // Convertir en image JavaFX
                        Image image = mat2Image(frame);
                        Platform.runLater(() -> webcamPreview.setImage(image));
                    }

                    Thread.sleep(33); // ~30 FPS

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        webcamThread.setDaemon(true);
        webcamThread.start();
    }

    @FXML
    public void captureAndAuthenticate() {
        if (lastCapturedFace == null || lastCapturedFace.empty()) {
            lblStatus.setText("❌ Aucun visage détecté");
            lblStatus.setStyle("-fx-font-size: 16px; -fx-text-fill: #F44336; -fx-font-weight: bold;");
            return;
        }

        btnCapture.setDisable(true);
        progressIndicator.setVisible(true);
        lblStatus.setText("🔍 Authentification en cours...");

        Thread authThread = new Thread(() -> {
            try {
                // Sauvegarder le visage capturé
                String tempDir = System.getProperty("java.io.tmpdir");
                String capturedPath = tempDir + "captured_face_" + System.currentTimeMillis() + ".jpg";
                Imgcodecs.imwrite(capturedPath, lastCapturedFace);

                // Comparer avec l'image de l'utilisateur
                boolean authenticated = compareFaces(capturedPath, userImagePath);

                // Nettoyer
                new File(capturedPath).delete();

                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    btnCapture.setDisable(false);

                    if (authenticated) {
                        lblStatus.setText("✅ Authentification réussie !");
                        lblStatus.setStyle("-fx-font-size: 18px; -fx-text-fill: #4CAF50; -fx-font-weight: bold;");

                        // Attendre 1 seconde puis fermer
                        new Thread(() -> {
                            try {
                                Thread.sleep(1000);
                                Platform.runLater(() -> {
                                    stopWebcam();
                                    if (callback != null) {
                                        callback.onSuccess("");
                                    }
                                    closeWindow();
                                });
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();

                    } else {
                        lblStatus.setText("❌ Visage non reconnu ! Réessayez");
                        lblStatus.setStyle("-fx-font-size: 16px; -fx-text-fill: #F44336; -fx-font-weight: bold;");
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    btnCapture.setDisable(false);
                    showError("❌ Erreur: " + e.getMessage());
                });
                e.printStackTrace();
            }
        });

        authThread.setDaemon(true);
        authThread.start();
    }

    private boolean compareFaces(String capturedPath, String userPath) {
        try {
            Mat img1 = Imgcodecs.imread(capturedPath, Imgcodecs.IMREAD_GRAYSCALE);
            Mat img2 = Imgcodecs.imread(userPath, Imgcodecs.IMREAD_GRAYSCALE);

            if (img1.empty() || img2.empty()) {
                System.err.println("✗ Impossible de charger les images");
                System.err.println("  - Captured: " + capturedPath + " (exists: " + new File(capturedPath).exists() + ")");
                System.err.println("  - User: " + userPath + " (exists: " + new File(userPath).exists() + ")");
                return false;
            }

            // Redimensionner à la même taille
            Size size = new Size(200, 200);
            Imgproc.resize(img1, img1, size);
            Imgproc.resize(img2, img2, size);

            // Calculer la différence
            Mat diff = new Mat();
            Core.absdiff(img1, img2, diff);

            Scalar meanDiff = Core.mean(diff);
            double similarity = 100 - (meanDiff.val[0] / 2.55);

            System.out.println("ℹ Similarité: " + String.format("%.2f%%", similarity));

            return similarity >= 50.0; // Seuil réduit à 50%

        } catch (Exception e) {
            System.err.println("✗ Erreur comparaison: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    public void cancel() {
        stopWebcam();
        if (callback != null) {
            callback.onFailure("Annulé par l'utilisateur");
        }
        closeWindow();
    }

    private void stopWebcam() {
        isRunning = false;

        if (webcamThread != null) {
            try {
                webcamThread.join(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (camera != null && camera.isOpened()) {
            camera.release();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) webcamPreview.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            lblStatus.setText(message);
            lblStatus.setStyle("-fx-font-size: 16px; -fx-text-fill: #F44336; -fx-font-weight: bold;");
        });
    }

    private Image mat2Image(Mat frame) {
        try {
            MatOfByte buffer = new MatOfByte();
            Imgcodecs.imencode(".png", frame, buffer);
            return new Image(new ByteArrayInputStream(buffer.toArray()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void cleanup() {
        stopWebcam();
    }
}
