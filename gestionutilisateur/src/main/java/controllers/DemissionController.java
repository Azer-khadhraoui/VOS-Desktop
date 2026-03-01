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

    @FXML
    private VBox sidebar;
    @FXML
    private VBox navContainer;
    @FXML
    private Label lblUserName, lblUserRole;
    @FXML
    private StackPane userAvatarContainer;
    @FXML
    private Label lblUserAvatar;
    @FXML
    private Button btnBack;
    
    // Navigation items
    @FXML private HBox navStatistiques;
    @FXML private HBox navOpportunites;
    @FXML private HBox navServices;
    @FXML private HBox navAdministration;
    @FXML private HBox navDeconnexion;

    @FXML
    private Label lblFullName, lblEmail, lblRole;
    @FXML
    private DatePicker dpDemissionDate;
    @FXML
    private ComboBox<String> cbRaison;
    @FXML
    private Spinner<Integer> spPreavis;
    @FXML
    private TextArea taComments;
    @FXML
    private CheckBox cbConfirm;
    @FXML
    private Label lblMessage, lblWarning;
    @FXML
    private Button btnGeneratePDF, btnCancel, btnGenerateAI, btnCheckQuality;

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
                "Autre"));
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
        if (navStatistiques != null) {
            navStatistiques.setOnMouseClicked(event -> goToStatistiques());
        }
        if (navOpportunites != null) {
            navOpportunites.setOnMouseClicked(event -> goToOpportunites());
        }
        if (navServices != null) {
            navServices.setOnMouseClicked(event -> goToServices());
        }
        if (navAdministration != null) {
            navAdministration.setOnMouseClicked(event -> goToAdministration());
        }
        if (navDeconnexion != null) {
            navDeconnexion.setOnMouseClicked(event -> logout());
        }
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
                // Convertir le préavis en format texte
                String preavisTexte = formatPreavis(preavis);
                
                String generatedText = AITextGeneratorService.generateDemissionText(
                        cbRaison.getValue(),
                        preavisTexte);

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
                AITextGeneratorService.GrammarCheckResult result = AITextGeneratorService
                        .checkTextQualityDetailed(currentText);

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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/QualityReportView.fxml"));
            Parent root = loader.load();

            QualityReportController controller = loader.getController();
            controller.setData(result);

            Stage stage = new Stage();
            stage.setTitle("Analyse de Qualité");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));

            stage.showAndWait();

            if (controller.isRegenerateRequested()) {
                generateAIText();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de l'affichage du rapport: " + e.getMessage());
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
        Paragraph submissionDate = new Paragraph(
                "Date de soumission: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
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
        Paragraph declaration = new Paragraph(
                "Par cette présente, je déclare officieusement démissionner de mon poste, " +
                        "effective à partir du "
                        + dpDemissionDate.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                        " ou après un délai de préavis de " + spPreavis.getValue()
                        + " jours à compter de cette demande, " +
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
        Paragraph footer = new Paragraph("Document généré par le système de gestion VOS - "
                + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StatistiquesView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) navStatistiques.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void goToOpportunites() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/views/MainView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) navOpportunites.getScene().getWindow();
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
            Stage stage = (Stage) navServices.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void goToAdministration() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdministrationView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) navAdministration.getScene().getWindow();
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
            Stage stage = (Stage) navDeconnexion.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Convertit un nombre de jours de préavis en format texte lisible
     * @param jours Nombre de jours de préavis
     * @return Format texte (ex: "1 mois", "2 semaines", "30 jours")
     */
    private String formatPreavis(int jours) {
        if (jours == 0) {
            return "sans préavis";
        } else if (jours == 30) {
            return "1 mois";
        } else if (jours == 60) {
            return "2 mois";
        } else if (jours == 90) {
            return "3 mois";
        } else if (jours % 30 == 0) {
            return (jours / 30) + " mois";
        } else if (jours % 7 == 0) {
            int semaines = jours / 7;
            return semaines + (semaines > 1 ? " semaines" : " semaine");
        } else {
            return jours + (jours > 1 ? " jours" : " jour");
        }
    }}