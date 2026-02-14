package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.net.URL;
import java.util.ResourceBundle;

public class AdministrationController implements Initializable {

    // Users container for card-based layout
    @FXML
    private VBox usersContainer;

    // Sidebar
    @FXML
    private VBox sidebarVBox;

    // Header
    @FXML
    private TextField searchField;
    @FXML
    private Label lblUserName;
    @FXML
    private Label lblUserRole;

    // Modal
    @FXML
    private StackPane modalOverlay;
    @FXML
    private Label modalTitle;
    @FXML
    private TextField tfNom;
    @FXML
    private TextField tfPrenom;
    @FXML
    private TextField tfEmail;
    @FXML
    private PasswordField tfPassword;
    @FXML
    private ComboBox<String> cbRole;
    @FXML
    private StackPane profileImageContainer;
    @FXML
    private Label lblProfileIcon;
    @FXML
    private Button btnSave;
    @FXML
    private Label lblModalMessage;

    // Delete Modal
    @FXML
    private StackPane deleteModalOverlay;
    @FXML
    private Label lblDeleteUserInfo;
    @FXML
    private Button btnConfirmDelete;

    private ServiceLocator serviceLocator;
    private UserData selectedUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupSidebarHover();
        setupTable();
        setupRoleComboBox();
        loadUsers();
    }

    private void setupSidebarHover() {
        if (sidebarVBox == null) return;
        
        sidebarVBox.setOnMouseEntered(event -> expandSidebar());
        sidebarVBox.setOnMouseExited(event -> collapseSidebar());
    }

    private void expandSidebar() {
        // Show all nav labels
        sidebarVBox.lookupAll(".nav-label").forEach(node -> {
            node.setVisible(true);
            node.setManaged(true);
        });

        // Show logo subtitle
        sidebarVBox.lookupAll(".logo-subtitle").forEach(node -> {
            node.setVisible(true);
            node.setManaged(true);
        });

        // Show help section
        sidebarVBox.lookupAll(".help-section").forEach(node -> {
            node.setVisible(true);
            node.setManaged(true);
        });

        // Show user profile
        sidebarVBox.lookupAll(".user-profile-sidebar").forEach(node -> {
            node.setVisible(true);
            node.setManaged(true);
        });
    }

    private void collapseSidebar() {
        // Hide all nav labels
        sidebarVBox.lookupAll(".nav-label").forEach(node -> {
            node.setVisible(false);
            node.setManaged(false);
        });

        // Hide logo subtitle
        sidebarVBox.lookupAll(".logo-subtitle").forEach(node -> {
            node.setVisible(false);
            node.setManaged(false);
        });

        // Hide help section
        sidebarVBox.lookupAll(".help-section").forEach(node -> {
            node.setVisible(false);
            node.setManaged(false);
        });

        // Hide user profile
        sidebarVBox.lookupAll(".user-profile-sidebar").forEach(node -> {
            node.setVisible(false);
            node.setManaged(false);
        });
    }

    private void setupTable() {
        // Card-based layout setup - no table columns needed
        // Cards will be populated by loadUsers()
    }

    private void createUserCard(UserData user) {
        // Main card container
        HBox cardRoot = new HBox(12);
        cardRoot.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 16; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.05), 8, 0, 0, 2);");
        cardRoot.setPrefHeight(60);
        cardRoot.setAlignment(Pos.CENTER_LEFT);
        
        // Avatar
        StackPane avatar = new StackPane();
        avatar.setStyle("-fx-background-color: #EFF6FF; -fx-background-radius: 20; -fx-border-color: #DBEAFE; -fx-border-width: 2; -fx-pref-width: 40; -fx-pref-height: 40; -fx-min-width: 40; -fx-min-height: 40;");
        Label avatarText = new Label(user.getNom().substring(0, 1).toUpperCase());
        avatarText.setStyle("-fx-font-size: 16px; -fx-text-fill: #0284C7; -fx-font-weight: 700;");
        avatar.getChildren().add(avatarText);
        
        // Info container (name + email)
        VBox infoBox = new VBox(4);
        infoBox.setStyle("-fx-spacing: 4;");
        
        Label nameLabel = new Label(user.getNom().toUpperCase() + " " + user.getPrenom());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #111827; -fx-font-weight: 700;");
        
        Label emailLabel = new Label(user.getEmail());
        emailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280; -fx-font-weight: 400;");
        
        infoBox.getChildren().addAll(nameLabel, emailLabel);
        
        // Role badge
        Label roleBadge = new Label(user.getRole().toUpperCase());
        String bgColor = "#DBEAFE";
        String textColor = "#0C4A6E";
        
        if (user.getRole().equalsIgnoreCase("ADMIN_RH")) {
            bgColor = "#DDD6FE";
            textColor = "#4F46E5";
        } else if (user.getRole().equalsIgnoreCase("ADMIN_TECHNI")) {
            bgColor = "#D1D5DB";
            textColor = "#374151";
        } else if (user.getRole().equalsIgnoreCase("CLIENT")) {
            bgColor = "#DBEAFE";
            textColor = "#0C4A6E";
        }
        
        roleBadge.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: %s; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 8; -fx-font-weight: 600;",
            bgColor, textColor
        ));
        
        // Actions buttons
        Button editBtn = new Button("✏️ Éditer");
        editBtn.setStyle("-fx-background-color: #C7D2FE; -fx-text-fill: #4F46E5; -fx-padding: 8 12; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 12px; -fx-border-width: 0; -fx-font-weight: 600;");
        editBtn.setOnAction(e -> handleEdit(user));
        
        Button deleteBtn = new Button("🗑️ Supprimer");
        deleteBtn.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-padding: 8 12; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 12px; -fx-border-width: 0; -fx-font-weight: 600;");
        deleteBtn.setOnAction(e -> handleDelete(user));
        
        // Actions container
        HBox actionsBox = new HBox(8);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);
        actionsBox.getChildren().addAll(editBtn, deleteBtn);
        
        // Assembly
        cardRoot.setHgrow(infoBox, Priority.ALWAYS);
        cardRoot.getChildren().addAll(avatar, infoBox, roleBadge, actionsBox);
        
        usersContainer.getChildren().add(cardRoot);
    }

    private void setupRoleComboBox() {
        ObservableList<String> roles = FXCollections.observableArrayList("admin", "user", "recruiter", "client");
        cbRole.setItems(roles);
    }

    private void loadUsers() {
        // Placeholder - Load users from service
        // Sample users for demonstration
        ObservableList<UserData> users = FXCollections.observableArrayList(
            new UserData(1, "Dupont", "Jean", "jean.dupont@example.com", "password123", "ADMIN_RH"),
            new UserData(2, "Martin", "Marie", "marie.martin@example.com", "password123", "CLIENT"),
            new UserData(3, "Bernard", "Pierre", "pierre.bernard@example.com", "password123", "ADMIN_TECHNI")
        );
        
        usersContainer.getChildren().clear();
        for (UserData user : users) {
            createUserCard(user);
        }
    }

    @FXML
    private void showAddUserModal() {
        resetForm();
        modalTitle.setText("Nouvel Utilisateur");
        selectedUser = null;
        lblModalMessage.setText("");
        modalOverlay.setVisible(true);
        modalOverlay.setManaged(true);
    }

    @FXML
    private void closeModal() {
        modalOverlay.setVisible(false);
        modalOverlay.setManaged(false);
        resetForm();
    }

    @FXML
    private void saveUser() {
        if (!validateForm()) {
            lblModalMessage.setText("Veuillez remplir tous les champs");
            return;
        }

        // Save logic here
        lblModalMessage.setText("Utilisateur enregistré avec succès");
        closeModal();
        loadUsers();
    }

    private void handleEdit(UserData user) {
        selectedUser = user;
        tfNom.setText(user.getNom());
        tfPrenom.setText(user.getPrenom());
        tfEmail.setText(user.getEmail());
        tfPassword.setText(user.getPassword());
        cbRole.setValue(user.getRole());
        modalTitle.setText("Modifier Utilisateur");
        modalOverlay.setVisible(true);
        modalOverlay.setManaged(true);
    }

    private void handleDelete(UserData user) {
        selectedUser = user;
        lblDeleteUserInfo.setText(user.getNom() + " " + user.getPrenom());
        deleteModalOverlay.setVisible(true);
        deleteModalOverlay.setManaged(true);
    }

    @FXML
    private void closeDeleteModal() {
        deleteModalOverlay.setVisible(false);
        deleteModalOverlay.setManaged(false);
    }

    @FXML
    private void confirmDelete() {
        if (selectedUser != null) {
            // Delete logic here
            closeDeleteModal();
            loadUsers();
        }
    }

    @FXML
    private void uploadImage() {
        // File chooser logic here
        lblProfileIcon.setText("📸");
    }

    @FXML
    private void logout() {
        // Logout logic here
    }

    private void resetForm() {
        tfNom.clear();
        tfPrenom.clear();
        tfEmail.clear();
        tfPassword.clear();
        cbRole.setValue(null);
        lblProfileIcon.setText("👤");
    }

    private boolean validateForm() {
        return !tfNom.getText().trim().isEmpty() &&
               !tfPrenom.getText().trim().isEmpty() &&
               !tfEmail.getText().trim().isEmpty() &&
               !tfPassword.getText().trim().isEmpty() &&
               cbRole.getValue() != null;
    }

    // Inner class for user data
    public static class UserData {
        private final javafx.beans.property.SimpleIntegerProperty id;
        private final javafx.beans.property.SimpleStringProperty nom;
        private final javafx.beans.property.SimpleStringProperty prenom;
        private final javafx.beans.property.SimpleStringProperty email;
        private final javafx.beans.property.SimpleStringProperty password;
        private final javafx.beans.property.SimpleStringProperty role;

        public UserData(int id, String nom, String prenom, String email, String password, String role) {
            this.id = new javafx.beans.property.SimpleIntegerProperty(id);
            this.nom = new javafx.beans.property.SimpleStringProperty(nom);
            this.prenom = new javafx.beans.property.SimpleStringProperty(prenom);
            this.email = new javafx.beans.property.SimpleStringProperty(email);
            this.password = new javafx.beans.property.SimpleStringProperty(password);
            this.role = new javafx.beans.property.SimpleStringProperty(role);
        }

        public int getId() { return id.get(); }
        public String getNom() { return nom.get(); }
        public String getPrenom() { return prenom.get(); }
        public String getEmail() { return email.get(); }
        public String getPassword() { return password.get(); }
        public String getRole() { return role.get(); }

        public javafx.beans.property.SimpleIntegerProperty idProperty() { return id; }
        public javafx.beans.property.SimpleStringProperty nomProperty() { return nom; }
        public javafx.beans.property.SimpleStringProperty prenomProperty() { return prenom; }
        public javafx.beans.property.SimpleStringProperty emailProperty() { return email; }
        public javafx.beans.property.SimpleStringProperty passwordProperty() { return password; }
        public javafx.beans.property.SimpleStringProperty roleProperty() { return role; }
    }

    // Service locator placeholder
    private static class ServiceLocator {
        // Placeholder for service location pattern
    }
}
