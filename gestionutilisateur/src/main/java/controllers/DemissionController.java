package controllers;

import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import utilis.UserSession;
import services.AITextGeneratorService;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFontFactory;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DemissionController {

    @FXML private VBox sidebar;
    @FXML private VBox navContainer;
    @FXML private Label lblUserName, lblUserRole;
    @FXML private StackPane userAvatarContainer;
    @FXML private Label lblUserAvatar;
    @FXML private Button btnBack;
    @FXML private HBox btnStatistiques;
    @FXML private HBox btnServices;
    @FXML private HBox logoutBtn;
    
    @FXML private Label lblFullName, lblEmail, lblRole;
    @FXML private DatePicker dpDemissionDate;
    @FXML private ComboBox<String> cbRaison;
    @FXML private Spinner<Integer> spPreavis;
    @FXML private TextArea taComments;
    @FXML private CheckBox cbConfirm;
    @FXML private Label lblMessage, lblWarning;
    @FXML private Button btnGeneratePDF, btnCancel, btnGenerateAI, btnCheckQuality;

    private Utilisateur currentUser;

    @FXML
    public void initialize() {
        loadCurrentUser();
        setupNavigation();
        setupButtons();
        setupDefaults();
        setupComboBox();
        
        // Set today's date as default
        dpDemissionDate.setValue(LocalDate.now());
        cbRaison.setValue("Raison personnelle");
        spPreavis.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 90, 30));
    }

    private void setupComboBox() {
        cbRaison.setItems(javafx.collections.FXCollections.observableArrayList(
            "Raison personnelle",
            "Changement d'emploi",
            "Salaire insuffisant",
            "Conditions de travail",
            "Opportunité professionnelle",
            "Études/Formation",
            "Raisons familiales",
            "Autre"
        ));
    }

    private void loadCurrentUser() {
        currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            lblFullName.setText(currentUser.getNom() + " " + currentUser.getPrenom());
            lblEmail.setText(currentUser.getEmail());
            lblRole.setText(currentUser.getRole());
            
            lblUserName.setText(currentUser.getNom() + " " + currentUser.getPrenom());
            lblUserRole.setText(currentUser.getRole());
            
            loadUserAvatar(currentUser.getImage_profil());
        }
    }

    private void loadUserAvatar(String imagePath) {
        try {
            if (imagePath == null || imagePath.isEmpty()) {
                lblUserAvatar.setText("👤");
                return;
            }
            
            // Check if it's an absolute path or relative
            if (imagePath.contains("/") || imagePath.contains("\\")) {
                // Absolute path - use directly
                File file = new File(imagePath);
                if (file.exists()) {
                    Image image = new Image(new FileInputStream(file), 40, 40, true, true);
                    ImageView imageView = new ImageView(image);
                    imageView.setFitHeight(40);
                    imageView.setFitWidth(40);
                    imageView.setStyle("-fx-border-radius: 20; -fx-clip-to-bounds: true;");
                    userAvatarContainer.getChildren().clear();
                    userAvatarContainer.getChildren().add(imageView);
                    return;
                }
            } else {
                // Relative path - prepend images/
                File file = new File("src/main/resources/images/" + imagePath);
                if (file.exists()) {
                    Image image = new Image(new FileInputStream(file), 40, 40, true, true);
                    ImageView imageView = new ImageView(image);
                    imageView.setFitHeight(40);
                    imageView.setFitWidth(40);
                    userAvatarContainer.getChildren().clear();
                    userAvatarContainer.getChildren().add(imageView);
                    return;
                }
            }
            
            lblUserAvatar.setText("👤");
        } catch (Exception e) {
            lblUserAvatar.setText("👤");
        }
    }

    private void setupNavigation() {
        btnBack.setOnAction(event -> goBack());
        btnStatistiques.setOnMouseClicked(event -> goToStatistiques());
        btnServices.setOnMouseClicked(event -> goToServices());
        logoutBtn.setOnMouseClicked(event -> logout());
    }

    private void setupButtons() {
        btnGeneratePDF.setOnAction(event -> generateAndDownloadPDF());
        btnCancel.setOnAction(event -> goBack());
        btnGenerateAI.setOnAction(event -> generateAIText());
        btnCheckQuality.setOnAction(event -> checkQuality());
    }

    private void setupDefaults() {
        cbConfirm.setSelected(false);
        lblMessage.setText("");
    }

    @FXML
    private void generateAIText() {
        // Validation des champs requis
        if (cbRaison.getValue() == null || cbRaison.getValue().isEmpty()) {
            showError("Veuillez d'abord sélectionner une raison de démission");
            return;
        }
        
        Integer preavis = spPreavis.getValue();
        if (preavis == null || preavis < 0) {
            showError("Veuillez spécifier un préavis valide");
            return;
        }

        // Affichage d'un message de chargement
        taComments.setText("🤖 Génération en cours...");
        taComments.setDisable(true);
        btnGenerateAI.setDisable(true);

        // Appel de l'IA dans un thread séparé pour ne pas bloquer l'interface
        new Thread(() -> {
            try {
                String generatedText = AITextGeneratorService.generateDemissionText(
                    cbRaison.getValue(),
                    preavis
                );
                
                // Mise à jour de l'interface sur le thread JavaFX
                javafx.application.Platform.runLater(() -> {
                    taComments.setText(generatedText);
                    taComments.setDisable(false);
                    btnGenerateAI.setDisable(false);
                    showSuccess("✨ Texte généré par IA avec succès !");
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    taComments.setText("");
                    taComments.setDisable(false);
                    btnGenerateAI.setDisable(false);
                    showError("Erreur lors de la génération IA: " + e.getMessage());
                });
            }
        }).start();
    }

    private void checkQuality() {
        String currentText = taComments.getText();
        
        if (currentText == null || currentText.trim().isEmpty()) {
            showError("Générez d'abord un texte avant de vérifier.");
            return;
        }

        // Message de chargement
        taComments.setDisable(true);
        btnCheckQuality.setDisable(true);
        
        // Appel de LanguageTool dans un thread séparé
        new Thread(() -> {
            try {
                AITextGeneratorService.GrammarCheckResult result = 
                    AITextGeneratorService.checkTextQualityDetailed(currentText);
                
                // Mise à jour de l'interface
                javafx.application.Platform.runLater(() -> {
                    taComments.setDisable(false);
                    btnCheckQuality.setDisable(false);
                    
                    // Affichage du dialogue avec rapport détaillé
                    showQualityDialog(result);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    taComments.setDisable(false);
                    btnCheckQuality.setDisable(false);
                    showError("Erreur lors de la vérification: " + e.getMessage());
                });
            }
        }).start();
    }
    
    private void showQualityDialog(AITextGeneratorService.GrammarCheckResult result) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("📋 Vérification Grammaticale");
        alert.setHeaderText(null);
        
        // Création du contenu avec le rapport détaillé
        javafx.scene.control.TextArea content = new javafx.scene.control.TextArea();
        content.setText(result.getDetailedReport());
        content.setWrapText(true);
        content.setEditable(false);
        content.setPrefRowCount(15);
        content.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 11px;");
        
        alert.getDialogPane().setContent(content);
        
        // Boutons
        javafx.scene.control.ButtonType btnKeep = new javafx.scene.control.ButtonType("✅ Garder ce texte", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        javafx.scene.control.ButtonType btnRegenerate = new javafx.scene.control.ButtonType("🔄 Régénérer", javafx.scene.control.ButtonBar.ButtonData.NO);
        
        alert.getButtonTypes().setAll(btnKeep, btnRegenerate);
        
        java.util.Optional<javafx.scene.control.ButtonType> result_dialog = alert.showAndWait();
        
        if (result_dialog.isPresent() && result_dialog.get() == btnRegenerate) {
            // Régénération du texte
            generateAIText();
        }
    }

    @FXML
    public void generateAndDownloadPDF() {
        try {
            // Validation
            if (!cbConfirm.isSelected()) {
                showError("Veuillez confirmer votre demande de démission");
                return;
            }
            
            if (dpDemissionDate.getValue() == null) {
                showError("Veuillez sélectionner une date de démission");
                return;
            }            
            if (dpDemissionDate.getValue().isBefore(LocalDate.now())) {
                showError("La date de démission ne peut pas être dans le passé");
                return;
            }            
            if (cbRaison.getValue() == null || cbRaison.getValue().isEmpty()) {
                showError("Veuillez sélectionner une raison");
                return;
            }

            // Choose save location
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Sélectionner le dossier de sauvegarde");
            File selectedDirectory = directoryChooser.showDialog(btnGeneratePDF.getScene().getWindow());

            if (selectedDirectory == null) {
                return; // User cancelled
            }

            // Generate PDF
            String fileName = "Demission_" + currentUser.getNom() + "_" + System.currentTimeMillis() + ".pdf";
            String filePath = selectedDirectory.getAbsolutePath() + File.separator + fileName;

            createDemissionPDF(filePath);

            showSuccess("PDF généré avec succès: " + fileName);
            
            // Optional: Open file explorer to the location
            try {
                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    Runtime.getRuntime().exec("explorer.exe /select," + filePath);
                }
            } catch (Exception e) {
                // Ignore if can't open explorer
            }

        } catch (Exception e) {
            showError("Erreur lors de la génération du PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createDemissionPDF(String filePath) throws Exception {
        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Title
        Paragraph title = new Paragraph("DEMANDE DE DÉMISSION")
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);

        // Date of submission
        Paragraph submissionDate = new Paragraph("Date de soumission: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT);
        document.add(submissionDate);

        document.add(new Paragraph("\n"));

        // Employee Information
        Paragraph empTitle = new Paragraph("INFORMATIONS DE L'EMPLOYÉ")
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                .setFontSize(12);
        document.add(empTitle);

        Table empTable = new Table(2);
        empTable.addCell("Nom complet:").setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));
        empTable.addCell(currentUser.getNom() + " " + currentUser.getPrenom());
        empTable.addCell("Email:").setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));
        empTable.addCell(currentUser.getEmail());
        empTable.addCell("Rôle:").setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));
        empTable.addCell(currentUser.getRole());
        empTable.addCell("ID Utilisateur:").setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));
        empTable.addCell(String.valueOf(currentUser.getId_utilisateur()));
        document.add(empTable);

        document.add(new Paragraph("\n"));

        // Resignation Details
        Paragraph detailsTitle = new Paragraph("DÉTAILS DE LA DÉMISSION")
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                .setFontSize(12);
        document.add(detailsTitle);

        Table detailsTable = new Table(2);
        detailsTable.addCell("Date de démission:").setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));
        detailsTable.addCell(dpDemissionDate.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        detailsTable.addCell("Raison:").setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));
        detailsTable.addCell(cbRaison.getValue());
        detailsTable.addCell("Délai de préavis:").setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));
        detailsTable.addCell(spPreavis.getValue() + " jours");
        document.add(detailsTable);

        // Comments (if any)
        if (taComments.getText() != null && !taComments.getText().isEmpty()) {
            document.add(new Paragraph("\n"));
            Paragraph commentsTitle = new Paragraph("MESSAGES/COMMENTAIRES")
                    .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                    .setFontSize(12);
            document.add(commentsTitle);
            
            Paragraph comments = new Paragraph(taComments.getText())
                    .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE))
                    .setFontSize(10);
            document.add(comments);
        }

        document.add(new Paragraph("\n\n"));

        // Declaration
        Paragraph declaration = new Paragraph("Par cette présente, je déclare officieusement démissionner de mon poste, " +
                "effective à partir du " + dpDemissionDate.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                " ou après un délai de préavis de " + spPreavis.getValue() + " jours à compter de cette demande, " +
                "selon les dispositions légales en vigueur.")
                .setFontSize(10);
        document.add(declaration);

        document.add(new Paragraph("\n\n"));

        // Signature area
        Paragraph signature = new Paragraph("_________________________\nSignature")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10);
        document.add(signature);

        // Footer
        document.add(new Paragraph("\n"));
        Paragraph footer = new Paragraph("Document généré par le système de gestion VOS - " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE));
        document.add(footer);

        document.close();
    }

    private void showError(String message) {
        lblMessage.setText("❌ " + message);
        lblMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
    }

    private void showSuccess(String message) {
        lblMessage.setText("✅ " + message);
        lblMessage.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12px;");
    }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ServicesView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnBack.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goToStatistiques() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdministrationView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnStatistiques.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goToServices() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ServicesView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnServices.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SigninView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
