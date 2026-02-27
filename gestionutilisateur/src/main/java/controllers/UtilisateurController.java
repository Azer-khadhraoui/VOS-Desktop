package controllers;

import entities.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import services.ServiceUtilisateur;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UtilisateurController {

    @FXML private TextField tfEmail, tfNom, tfPrenom;
    @FXML private PasswordField tfPassword;

    @FXML private TableView<Utilisateur> tableUsers;
    @FXML private TableColumn<Utilisateur, Integer> colId;
    @FXML private TableColumn<Utilisateur, String> colImage, colEmail, colRole, colNom, colPrenom;

    ServiceUtilisateur su = new ServiceUtilisateur();
    Utilisateur utilisateurSelectionne; // ⭐ IMPORTANT

    // ============================
    // INITIALIZE
    // ============================
    @FXML
    public void initialize() {

        colId.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(
                        data.getValue().getId_utilisateur()
                ).asObject());

        colImage.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getImage_profil()));

        colImage.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);
                if (empty || imagePath == null || imagePath.trim().isEmpty()) {
                    setGraphic(null);
                    return;
                }

                try {
                    File file = resolveAvatarFile(imagePath);
                    if (file != null && file.exists() && file.isFile()) {
                        Image image = new Image(new FileInputStream(file));
                        imageView.setImage(image);
                        imageView.setFitWidth(24);
                        imageView.setFitHeight(24);
                        imageView.setPreserveRatio(false);

                        javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(12, 12, 12);
                        imageView.setClip(clip);
                        setGraphic(imageView);
                    } else {
                        setGraphic(null);
                    }
                } catch (Exception e) {
                    setGraphic(null);
                }
            }
        });

        colEmail.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getEmail()));

        colRole.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getRole()));

        colNom.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getNom()));

        colPrenom.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getPrenom()));

        // 🔥 CLICK TABLE → REMPLIR FORMULAIRE
        tableUsers.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSel, newSel) -> remplirFormulaire(newSel)
        );

        refreshTable();
    }

    // ============================
    // REMPLIR FORMULAIRE
    // ============================
    private void remplirFormulaire(Utilisateur u) {
        if (u != null) {
            utilisateurSelectionne = u;
            tfEmail.setText(u.getEmail());
            tfPassword.setText(u.getMot_de_passe());
            tfNom.setText(u.getNom());
            tfPrenom.setText(u.getPrenom());
        }
    }

    // ============================
    // REFRESH TABLE
    // ============================
    public void refreshTable() {
        ObservableList<Utilisateur> list =
                FXCollections.observableArrayList(su.afficherAll());
        tableUsers.setItems(list);
    }

    // ============================
    // AJOUT
    // ============================
    @FXML
    public void ajouterUtilisateur() {

        Utilisateur u = new Utilisateur(
                0,
                "profil.png",
                tfEmail.getText(),
                tfPassword.getText(),
                "CLIENT",
                tfNom.getText(),
                tfPrenom.getText()
        );

        su.ajouter(u);
        refreshTable();
        clearForm();
    }

    // ============================
    // MODIFIER (LOGIQUE PRO)
    // ============================
    @FXML
    public void modifierUtilisateur() {

        if (utilisateurSelectionne != null) {

            utilisateurSelectionne = new Utilisateur(
                    utilisateurSelectionne.getId_utilisateur(),
                    utilisateurSelectionne.getImage_profil(),
                    tfEmail.getText(),
                    tfPassword.getText(),
                    utilisateurSelectionne.getRole(),
                    tfNom.getText(),
                    tfPrenom.getText()
            );

            su.modifier(utilisateurSelectionne);
            refreshTable();
            clearForm();
        }
    }

    // ============================
    // SUPPRIMER
    // ============================
    @FXML
    public void supprimerUtilisateur() {

        if (utilisateurSelectionne != null) {
            su.supprimer(utilisateurSelectionne.getId_utilisateur());
            refreshTable();
            clearForm();
        }
    }

    // ============================
    // CLEAR FORM
    // ============================
    private void clearForm() {
        tfEmail.clear();
        tfPassword.clear();
        tfNom.clear();
        tfPrenom.clear();
        utilisateurSelectionne = null;
    }
    
    // ============================
    // SET USER FOR PROFILE VIEW
    // ============================
    /**
     * Configure le formulaire pour afficher le profil de l'utilisateur connecté
     * @param user L'utilisateur dont on veut afficher le profil
     */
    public void setCurrentUserForProfile(Utilisateur user) {
        if (user != null) {
            utilisateurSelectionne = user;
            tfEmail.setText(user.getEmail());
            tfPassword.setText(user.getMot_de_passe());
            tfNom.setText(user.getNom());
            tfPrenom.setText(user.getPrenom());
            
            // Sélectionner l'utilisateur dans la table si elle existe
            if (tableUsers != null && tableUsers.getItems() != null) {
                for (Utilisateur u : tableUsers.getItems()) {
                    if (u.getId_utilisateur() == user.getId_utilisateur()) {
                        tableUsers.getSelectionModel().select(u);
                        break;
                    }
                }
            }
            
            System.out.println("✓ Profil chargé pour: " + user.getPrenom() + " " + user.getNom());
        }
    }

    private File resolveAvatarFile(String imagePath) {
        File direct = new File(imagePath);
        if (direct.exists()) {
            return direct;
        }

        String fileName = direct.getName();
        Path[] candidates = new Path[] {
                Paths.get(System.getProperty("user.dir"), "images", fileName),
                Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "images", fileName),
                Paths.get(System.getProperty("user.dir"), "target", "classes", "images", fileName)
        };

        for (Path candidate : candidates) {
            File file = candidate.toFile();
            if (file.exists()) {
                return file;
            }
        }

        return null;
    }
}
