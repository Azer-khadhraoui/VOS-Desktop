package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.ChatBotService;
import services.candidat.CandidatureService;
import services.OffreEmploiService;
import entities.Candidature;
import entities.OffreEmploi;
import utilis.UserSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChatBotController {

    @FXML private VBox messagesContainer;
    @FXML private ScrollPane messagesScroll;
    @FXML private TextField messageInput;
    @FXML private Button sendBtn;
    @FXML private Label statusLabel;

    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private CandidatureService candidatureService;
    private OffreEmploiService offreService;

    @FXML
    public void initialize() {
        // Initialiser les services
        candidatureService = new CandidatureService();
        offreService = new OffreEmploiService();
        
        // Auto-scroll vers le bas quand de nouveaux messages arrivent
        messagesContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            messagesScroll.setVvalue(1.0);
        });

        // Afficher un message de bienvenue
        showWelcomeMessage();
    }

    private void showWelcomeMessage() {
        VBox msgBox = createAssistantMessage(
            "👋 Bienvenue! Je suis votre Assistant RH IA.\n\n" +
            "Je peux vous aider avec:\n" +
            "💼 Questions sur les offres d'emploi\n" +
            "🏖️ Procédure de demande de congés\n" +
            "📄 Comment faire une demande de démission\n" +
            "⭐ Conseils carrière\n\n" +
            "Comment puis-je vous aider aujourd'hui?"
        );
        messagesContainer.getChildren().add(msgBox);
    }

    @FXML
    public void sendMessage() {
        String userMessage = messageInput.getText().trim();
        if (userMessage.isEmpty()) {
            return;
        }

        // Afficher le message de l'utilisateur
        VBox userMsgBox = createUserMessage(userMessage);
        messagesContainer.getChildren().add(userMsgBox);
        messageInput.clear();

        // Désactiver le bouton et afficher un indicateur
        sendBtn.setDisable(true);
        statusLabel.setText("⏳ Assistant en train de répondre...");

        // Récupérer les données de la base de données en arrière-plan
        new Thread(() -> {
            try {
                // Récupérer l'ID de l'utilisateur actuel
                int idUtilisateur = UserSession.getInstance().getCurrentUser().getId_utilisateur();
                
                // Récupérer les candidatures de l'utilisateur
                List<Candidature> userCandidatures = candidatureService.getCandidaturesByUtilisateur(idUtilisateur);
                
                // Récupérer toutes les offres disponibles
                List<OffreEmploi> allOffres = offreService.getAllOffres();
                
                // Envoyer le message avec le contexte
                ChatBotService.sendMessageWithContext(
                    userMessage, 
                    userCandidatures, 
                    allOffres,
                    response -> {
                        javafx.application.Platform.runLater(() -> {
                            VBox assistantMsgBox = createAssistantMessage(response);
                            messagesContainer.getChildren().add(assistantMsgBox);
                            sendBtn.setDisable(false);
                            statusLabel.setText("");
                            messageInput.requestFocus();
                        });
                    }, 
                    error -> {
                        javafx.application.Platform.runLater(() -> {
                            VBox errorMsgBox = createAssistantMessage("❌ " + error);
                            messagesContainer.getChildren().add(errorMsgBox);
                            sendBtn.setDisable(false);
                            statusLabel.setText("");
                            messageInput.requestFocus();
                        });
                    }
                );
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    VBox errorMsgBox = createAssistantMessage("❌ Erreur récupération données: " + e.getMessage());
                    messagesContainer.getChildren().add(errorMsgBox);
                    sendBtn.setDisable(false);
                    statusLabel.setText("");
                    messageInput.requestFocus();
                });
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    public void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            sendMessage();
            event.consume();
        }
    }

    /**
     * Crée une bulle de message utilisateur (à droite, bleue)
     */
    private VBox createUserMessage(String text) {
        Label msgLabel = new Label(text);
        msgLabel.setWrapText(true);
        msgLabel.setStyle(
            "-fx-background-color: #667eea; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 12px 16px; " +
            "-fx-border-radius: 12px; " +
            "-fx-background-radius: 12px; " +
            "-fx-max-width: 600px; " +
            "-fx-font-size: 13px;"
        );

        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_RIGHT);
        messageBox.setPrefWidth(800);
        messageBox.getChildren().add(msgLabel);
        HBox.setHgrow(messageBox, Priority.ALWAYS);

        VBox timeBox = new VBox();
        timeBox.setAlignment(Pos.BOTTOM_RIGHT);
        Label timeLabel = new Label(LocalDateTime.now().format(timeFormatter));
        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #999; -fx-padding: 4 16 0 0;");
        timeBox.getChildren().add(timeLabel);

        VBox container = new VBox(10);
        container.getChildren().addAll(messageBox, timeBox);
        return container;
    }

    /**
     * Crée une bulle de message assistant (à gauche, grise)
     */
    private VBox createAssistantMessage(String text) {
        Label msgLabel = new Label(text);
        msgLabel.setWrapText(true);
        msgLabel.setStyle(
            "-fx-background-color: #e8ecf1; " +
            "-fx-text-fill: #1a1a2e; " +
            "-fx-padding: 12px 16px; " +
            "-fx-border-radius: 12px; " +
            "-fx-background-radius: 12px; " +
            "-fx-max-width: 600px; " +
            "-fx-font-size: 13px;"
        );

        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageBox.setPrefWidth(800);
        messageBox.getChildren().add(msgLabel);
        HBox.setHgrow(messageBox, Priority.ALWAYS);

        VBox timeBox = new VBox();
        timeBox.setAlignment(Pos.BOTTOM_LEFT);
        Label timeLabel = new Label(LocalDateTime.now().format(timeFormatter));
        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #999; -fx-padding: 4 0 0 16;");
        timeBox.getChildren().add(timeLabel);

        VBox container = new VBox(10);
        container.getChildren().addAll(messageBox, timeBox);
        return container;
    }

    @FXML
    public void closeChat() {
        Stage stage = (Stage) messageInput.getScene().getWindow();
        stage.close();
    }
}
