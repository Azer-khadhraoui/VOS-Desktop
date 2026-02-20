package controllers;

import entities.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.ServiceUtilisateur;

public class UtilisateurController {

    @FXML private TextField tfEmail, tfNom, tfPrenom;
    @FXML private PasswordField tfPassword;

    @FXML private TableView<Utilisateur> tableUsers;
    @FXML private TableColumn<Utilisateur, Integer> colId;
    @FXML private TableColumn<Utilisateur, String> colEmail, colRole, colNom, colPrenom;

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
}
