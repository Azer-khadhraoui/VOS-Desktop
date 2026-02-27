package services;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import java.io.File;

public class FaceRecognitionService {
    
    private static boolean opencvLoaded = false;
    private CascadeClassifier faceDetector;
    
    public FaceRecognitionService() {
        loadOpenCV();
        initializeFaceDetector();
    }
    
    private void loadOpenCV() {
        if (!opencvLoaded) {
            try {
                // Charger la DLL native OpenCV
                String dllPath = new File("lib/opencv/build/java/x64/opencv_java4120.dll").getAbsolutePath();
                System.load(dllPath);
                opencvLoaded = true;
                System.out.println("✓ OpenCV chargé avec succès: " + dllPath);
            } catch (Exception e) {
                System.err.println("✗ Erreur lors du chargement d'OpenCV: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void initializeFaceDetector() {
        try {
            // Utiliser le fichier Haar Cascade pour la détection de visages
            String haarPath = new File("lib/opencv/build/etc/haarcascades/haarcascade_frontalface_default.xml").getAbsolutePath();
            faceDetector = new CascadeClassifier(haarPath);
            
            if (faceDetector.empty()) {
                System.err.println("✗ Impossible de charger le détecteur de visages");
            } else {
                System.out.println("✓ Détecteur de visages initialisé");
            }
        } catch (Exception e) {
            System.err.println("✗ Erreur lors de l'initialisation du détecteur: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Capture une image depuis la webcam et détecte un visage
     * @return Le chemin du fichier image capturé, ou null si échec
     */
    public String captureFaceFromWebcam() {
        VideoCapture camera = new VideoCapture(0);
        
        if (!camera.isOpened()) {
            System.err.println("✗ Impossible d'ouvrir la webcam");
            return null;
        }
        
        Mat frame = new Mat();
        String capturedImagePath = null;
        
        try {
            // Capturer une frame
            camera.read(frame);
            
            if (!frame.empty()) {
                // Détecter les visages
                MatOfRect faceDetections = new MatOfRect();
                faceDetector.detectMultiScale(frame, faceDetections);
                
                if (faceDetections.toArray().length > 0) {
                    // Prendre le premier visage détecté
                    Rect faceRect = faceDetections.toArray()[0];
                    Mat face = new Mat(frame, faceRect);
                    
                    // Sauvegarder l'image
                    String tempDir = System.getProperty("java.io.tmpdir");
                    capturedImagePath = tempDir + "captured_face_" + System.currentTimeMillis() + ".jpg";
                    Imgcodecs.imwrite(capturedImagePath, face);
                    
                    System.out.println("✓ Visage capturé: " + capturedImagePath);
                } else {
                    System.err.println("✗ Aucun visage détecté");
                }
            }
        } catch (Exception e) {
            System.err.println("✗ Erreur lors de la capture: " + e.getMessage());
            e.printStackTrace();
        } finally {
            camera.release();
        }
        
        return capturedImagePath;
    }
    
    /**
     * Compare deux images de visages
     * @param imagePath1 Chemin de la première image
     * @param imagePath2 Chemin de la deuxième image
     * @return true si les visages correspondent (similarité > 80%), false sinon
     */
    public boolean compareFaces(String imagePath1, String imagePath2) {
        try {
            Mat img1 = Imgcodecs.imread(imagePath1, Imgcodecs.IMREAD_GRAYSCALE);
            Mat img2 = Imgcodecs.imread(imagePath2, Imgcodecs.IMREAD_GRAYSCALE);
            
            if (img1.empty() || img2.empty()) {
                System.err.println("✗ Impossible de charger les images pour comparaison");
                return false;
            }
            
            // Redimensionner à la même taille
            Size size = new Size(200, 200);
            Imgproc.resize(img1, img1, size);
            Imgproc.resize(img2, img2, size);
            
            // Calculer la différence absolue
            Mat diff = new Mat();
            Core.absdiff(img1, img2, diff);
            
            // Calculer la moyenne de la différence
            Scalar meanDiff = Core.mean(diff);
            double similarity = 100 - (meanDiff.val[0] / 2.55); // Convertir en pourcentage
            
            System.out.println("ℹ Similarité: " + String.format("%.2f%%", similarity));
            
            // Seuil de similarité à 80%
            return similarity >= 80.0;
            
        } catch (Exception e) {
            System.err.println("✗ Erreur lors de la comparaison: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Vérifie si un visage capturé correspond à l'image de profil d'un utilisateur
     * @param userImagePath Chemin de l'image de profil de l'utilisateur
     * @return true si authentifié, false sinon
     */
    public boolean authenticateWithFace(String userImagePath) {
        // Capturer le visage depuis la webcam
        String capturedFacePath = captureFaceFromWebcam();
        
        if (capturedFacePath == null) {
            return false;
        }
        
        // Comparer avec l'image de l'utilisateur
        boolean isAuthenticated = compareFaces(capturedFacePath, userImagePath);
        
        // Nettoyer le fichier temporaire
        new File(capturedFacePath).delete();
        
        return isAuthenticated;
    }
}
