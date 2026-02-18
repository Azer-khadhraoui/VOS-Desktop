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
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFontFactory;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import javafx.stage.Stage;
import utilis.UserSession;

public class DemandeCongeController {

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
    @FXML private DatePicker dpDateDebut, dpDateFin;
    @FXML private ComboBox<String> cbTypeCongé;
    @FXML private Spinner<Integer> spNombreJours;
    @FXML private TextArea taComments;
    @FXML private CheckBox cbConfirm;
    @FXML private Label lblMessage, lblWarning;
    @FXML private Button btnDownloadPDF, btnCancel;

    private Utilisateur currentUser;

    @FXML
    public void initialize() {
        loadCurrentUser();
        setupNavigation();
        setupButtons();
        setupDefaults();
        setupComboBox();
        setupDateListeners();
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
            
            if (imagePath.contains("/") || imagePath.contains("\\")) {
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
        btnDownloadPDF.setOnAction(event -> generateAndDownloadPDF());
        btnCancel.setOnAction(event -> goBack());
    }

    private void setupDefaults() {
        cbConfirm.setSelected(false);
        lblMessage.setText("");
        dpDateDebut.setValue(LocalDate.now());
        dpDateFin.setValue(LocalDate.now().plusDays(1));
    }

    private void setupComboBox() {
        cbTypeCongé.setItems(javafx.collections.FXCollections.observableArrayList(
            "Congé payé",
            "Congé sans solde",
            "Congé parental",
            "Congé maladie",
            "Congé sabbatique",
            "Autres"
        ));
        cbTypeCongé.setValue("Congé payé");
    }

    private void setupDateListeners() {
        dpDateDebut.valueProperty().addListener((obs, oldVal, newVal) -> updateNombreJours());
        dpDateFin.valueProperty().addListener((obs, oldVal, newVal) -> updateNombreJours());
    }

    private void updateNombreJours() {
        LocalDate debut = dpDateDebut.getValue();
        LocalDate fin = dpDateFin.getValue();
        
        if (debut != null && fin != null) {
            if (fin.isBefore(debut)) {
                spNombreJours.getValueFactory().setValue(0);
            } else {
                long jours = ChronoUnit.DAYS.between(debut, fin) + 1;
                spNombreJours.getValueFactory().setValue((int) jours);
            }
        }
    }

    @FXML
    public void generateAndDownloadPDF() {
        try {
            // Validation
            if (!cbConfirm.isSelected()) {
                showError("Veuillez confirmer votre demande de congé");
                return;
            }
            
            if (dpDateDebut.getValue() == null || dpDateFin.getValue() == null) {
                showError("Veuillez sélectionner les dates");
                return;
            }
            
            if (dpDateDebut.getValue().isBefore(LocalDate.now())) {
                showError("La date de début ne peut pas être dans le passé");
                return;
            }
            
            if (dpDateFin.getValue().isBefore(dpDateDebut.getValue())) {
                showError("La date de fin doit être après la date de début");
                return;
            }
            
            if (cbTypeCongé.getValue() == null || cbTypeCongé.getValue().isEmpty()) {
                showError("Veuillez sélectionner un type de congé");
                return;
            }

            // Choose save location
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Sélectionner le dossier de sauvegarde");
            File selectedDirectory = directoryChooser.showDialog(btnDownloadPDF.getScene().getWindow());

            if (selectedDirectory == null) {
                return; // User cancelled
            }

            // Generate PDF
            String fileName = "Demande_Congé_" + currentUser.getNom() + "_" + System.currentTimeMillis() + ".pdf";
            String filePath = selectedDirectory.getAbsolutePath() + File.separator + fileName;

            createCongePDF(filePath);

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

    private void createCongePDF(String filePath) throws Exception {
        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Title
        Paragraph title = new Paragraph("DEMANDE DE CONGÉ")
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);

        // Date of submission
        Paragraph submissionDate = new Paragraph("Date de soumission: " + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")))
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

        // Leave Details
        Paragraph detailsTitle = new Paragraph("DÉTAILS DE LA DEMANDE DE CONGÉ")
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                .setFontSize(12);
        document.add(detailsTitle);

        Table detailsTable = new Table(2);
        detailsTable.addCell("Date de début:").setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));
        detailsTable.addCell(dpDateDebut.getValue().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        detailsTable.addCell("Date de fin:").setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));
        detailsTable.addCell(dpDateFin.getValue().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        detailsTable.addCell("Type de congé:").setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));
        detailsTable.addCell(cbTypeCongé.getValue());
        detailsTable.addCell("Nombre de jours:").setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));
        Integer nombreJours = spNombreJours.getValue();
        detailsTable.addCell(nombreJours + " jour(s)");
        document.add(detailsTable);

        // Comments (if any)
        if (taComments.getText() != null && !taComments.getText().isEmpty()) {
            document.add(new Paragraph("\n"));
            Paragraph commentsTitle = new Paragraph("RAISONS/COMMENTAIRES")
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
        Paragraph declaration = new Paragraph("Par cette présente, je demande un congé du " + 
                dpDateDebut.getValue().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                " au " + dpDateFin.getValue().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                " (soit " + nombreJours + " jour(s)) en tant que congé " + cbTypeCongé.getValue() + ".")
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
        Paragraph footer = new Paragraph("Document généré par le système de gestion VOS - " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
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
