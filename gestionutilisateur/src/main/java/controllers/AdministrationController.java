package controllers;

import entities.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.animation.*;
import javafx.util.Duration;
import services.ServiceUtilisateur;
import utilis.UserSession;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class AdministrationController {

    @FXML private TextField searchField;
    @FXML private Label lblUserName, lblUserRole;
    @FXML private StackPane userAvatarContainer;
    @FXML private Label lblUserAvatar;
    @FXML private VBox sidebar;
    @FXML private VBox navContainer;
    @FXML private javafx.scene.layout.HBox logoutBtn;
    @FXML private javafx.scene.layout.HBox btnStatistiques;
    @FXML private javafx.scene.layout.HBox btnOffres;
    
    @FXML private ComboBox<String> filterRole;
    @FXML private Label lblTotalCount;
    
    @FXML private TableView<Utilisateur> tableUsers;
    @FXML private TableColumn<Utilisateur, Integer> colId;
    @FXML private TableColumn<Utilisateur, String> colNom;
    @FXML private TableColumn<Utilisateur, String> colPrenom;
    @FXML private TableColumn<Utilisateur, String> colEmail;
    @FXML private TableColumn<Utilisateur, String> colPassword;
    @FXML private TableColumn<Utilisateur, String> colRole;
    @FXML private TableColumn<Utilisateur, Void> colActions;

    @FXML private StackPane modalOverlay;
    @FXML private VBox modalContent;
    @FXML private Label modalTitle;
    @FXML private TextField tfNom, tfPrenom, tfEmail;
    @FXML private PasswordField tfPassword;
    @FXML private ComboBox<String> cbRole;
    @FXML private Button btnSave;
    @FXML private Label lblModalMessage;
    @FXML private StackPane profileImageContainer;
    @FXML private Label lblProfileIcon;
    
    // Modal de suppression
    @FXML private StackPane deleteModalOverlay;
    @FXML private VBox deleteModalContent;
    @FXML private Label lblDeleteIcon;
    @FXML private Label lblDeleteUserInfo;
    @FXML private Button btnConfirmDelete;

    private ServiceUtilisateur serviceUtilisateur = new ServiceUtilisateur();
    private Utilisateur selectedUser = null;
    private boolean isEditMode = false;
    private String currentImageName = "default.png";
    private Utilisateur userToDelete = null;
    private boolean isSidebarHovered = false;

    @FXML
    public void initialize() {
        setupSidebarHoverAnimation();
        setupNavItemsHoverAnimation();
        setupTable();
        setupComboBox();
        setupFilters();
        refreshTable();
        setupSearch();
        loadCurrentUser();
        
        // Bind modal overlays to fill parent (1440x1024)
        modalOverlay.setMinSize(1440, 1024);
        modalOverlay.setMaxSize(1440, 1024);
        modalOverlay.setPrefSize(1440, 1024);
        deleteModalOverlay.setMinSize(1440, 1024);
        deleteModalOverlay.setMaxSize(1440, 1024);
        deleteModalOverlay.setPrefSize(1440, 1024);
        
        // Add navigation handlers
        logoutBtn.setOnMouseClicked(event -> logout());
        btnStatistiques.setOnMouseClicked(event -> goToStatistiques());
    }

    private void loadCurrentUser() {
        Utilisateur currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            lblUserName.setText(currentUser.getNom() + " " + currentUser.getPrenom());
            lblUserRole.setText(currentUser.getRole());
            
            // Charger l'image de profil
            loadUserAvatar(currentUser.getImage_profil());
        }
    }

    private void setupSidebarHoverAnimation() {
        sidebar.setOnMouseEntered(event -> {
            isSidebarHovered = true;
            
            // Expand width
            javafx.animation.Timeline expandTimeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                    javafx.util.Duration.millis(300),
                    new javafx.animation.KeyValue(sidebar.prefWidthProperty(), 250)
                )
            );
            expandTimeline.play();
            
            // Fade in labels
            for (javafx.scene.Node node : navContainer.getChildren()) {
                if (node instanceof javafx.scene.layout.HBox) {
                    javafx.scene.layout.HBox hbox = (javafx.scene.layout.HBox) node;
                    for (javafx.scene.Node child : hbox.getChildren()) {
                        if (child instanceof Label && ((Label) child).getStyleClass().contains("nav-item-label")) {
                            Label label = (Label) child;
                            javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), label);
                            fade.setFromValue(0.0);
                            fade.setToValue(1.0);
                            fade.play();
                        }
                    }
                }
            }
        });

        sidebar.setOnMouseExited(event -> {
            isSidebarHovered = false;
            
            // Delay before collapsing to make sure we're not over another element
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(100));
            pause.setOnFinished(e -> {
                if (!isSidebarHovered) {
                    // Collapse width
                    javafx.animation.Timeline collapseTimeline = new javafx.animation.Timeline(
                        new javafx.animation.KeyFrame(
                            javafx.util.Duration.millis(300),
                            new javafx.animation.KeyValue(sidebar.prefWidthProperty(), 60)
                        )
                    );
                    collapseTimeline.play();
                    
                    // Fade out labels
                    for (javafx.scene.Node node : navContainer.getChildren()) {
                        if (node instanceof javafx.scene.layout.HBox) {
                            javafx.scene.layout.HBox hbox = (javafx.scene.layout.HBox) node;
                            for (javafx.scene.Node child : hbox.getChildren()) {
                                if (child instanceof Label && ((Label) child).getStyleClass().contains("nav-item-label")) {
                                    Label label = (Label) child;
                                    javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), label);
                                    fade.setFromValue(1.0);
                                    fade.setToValue(0.0);
                                    fade.play();
                                }
                            }
                        }
                    }
                }
            });
            pause.play();
        });
    }
    
    private void setupNavItemsHoverAnimation() {
        for (javafx.scene.Node node : navContainer.getChildren()) {
            if (node instanceof javafx.scene.layout.HBox) {
                javafx.scene.layout.HBox hbox = (javafx.scene.layout.HBox) node;
                
                hbox.setOnMouseEntered(event -> {
                    isSidebarHovered = true;
                    for (javafx.scene.Node child : hbox.getChildren()) {
                        if (child instanceof Label && ((Label) child).getStyleClass().contains("nav-item-label")) {
                            Label label = (Label) child;
                            label.setStyle("-fx-text-fill: #FF9900;");
                        }
                    }
                });
                
                hbox.setOnMouseExited(event -> {
                    for (javafx.scene.Node child : hbox.getChildren()) {
                        if (child instanceof Label && ((Label) child).getStyleClass().contains("nav-item-label")) {
                            Label label = (Label) child;
                            label.setStyle("-fx-text-fill: #111827;");
                        }
                    }
                });
            }
        }
    }
    
    private void loadUserAvatar(String imageName) {
        try {
            File imgFile = new File("images/" + imageName);
            if (imgFile.exists()) {
                ImageView imageView = new ImageView();
                imageView.setFitWidth(50);
                imageView.setFitHeight(50);
                imageView.setPreserveRatio(false);
                
                // Clip circulaire
                javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(25, 25, 25);
                imageView.setClip(clip);
                
                Image img = new Image(new FileInputStream(imgFile));
                imageView.setImage(img);
                
                userAvatarContainer.getChildren().clear();
                userAvatarContainer.getChildren().add(imageView);
            }
        } catch (Exception e) {
            // Garder l'icône par défaut
        }
    }

    private void setupComboBox() {
        cbRole.setItems(FXCollections.observableArrayList("CLIENT", "ADMIN_RH", "ADMIN_TECHNIQUE"));
        cbRole.setValue("CLIENT");
        
        // Style the ComboBox button cell to show white text on dark background
        cbRole.setButtonCell(new javafx.scene.control.ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setTextFill(javafx.scene.paint.Color.WHITE);
                }
            }
        });
        
        // Style dropdown items
        cbRole.setCellFactory(lv -> new javafx.scene.control.ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setTextFill(javafx.scene.paint.Color.WHITE);
                    setStyle("-fx-background-color: #1a1a2e;");
                }
            }
        });
    }

    private void setupTable() {
        // Colonne ID
        colId.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleIntegerProperty(data.getValue().getId_utilisateur()).asObject()
        );
        colId.setCellFactory(col -> new TableCell<Utilisateur, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.valueOf(item));
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #6B7280;");
                }
            }
        });

        // Colonne Nom avec photo de profil
        colNom.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getNom())
        );
        colNom.setCellFactory(col -> new TableCell<Utilisateur, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Utilisateur user = getTableView().getItems().get(getIndex());
                    HBox hbox = new HBox(10);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    
                    // Image de profil
                    ImageView imageView = new ImageView();
                    imageView.setFitWidth(35);
                    imageView.setFitHeight(35);
                    imageView.setPreserveRatio(true);
                    imageView.setStyle("-fx-background-radius: 50; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 5, 0, 0, 1);");
                    
                    try {
                        File imgFile = new File("images/" + user.getImage_profil());
                        if (imgFile.exists()) {
                            Image img = new Image(new FileInputStream(imgFile));
                            imageView.setImage(img);
                        } else {
                            // Image par défaut (emoji)
                            Label defaultIcon = new Label("👤");
                            defaultIcon.setStyle("-fx-font-size: 28px; -fx-background-color: #DBEAFE; -fx-background-radius: 50; -fx-padding: 3;");
                            hbox.getChildren().add(defaultIcon);
                        }
                    } catch (Exception e) {
                        // Image par défaut en cas d'erreur
                        Label defaultIcon = new Label("👤");
                        defaultIcon.setStyle("-fx-font-size: 28px; -fx-background-color: #DBEAFE; -fx-background-radius: 50; -fx-padding: 3;");
                        hbox.getChildren().add(defaultIcon);
                    }
                    
                    if (imageView.getImage() != null) {
                        hbox.getChildren().add(imageView);
                    }
                    
                    Label nameLabel = new Label(item);
                    nameLabel.getStyleClass().add("name-cell");
                    
                    hbox.getChildren().add(nameLabel);
                    setGraphic(hbox);
                    setText(null);
                }
            }
        });

        // Colonne Prénom
        colPrenom.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getPrenom())
        );
        colPrenom.setCellFactory(col -> new TableCell<Utilisateur, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-font-size: 14px; -fx-text-fill: #111827;");
                }
            }
        });

        // Colonne Email avec badge orange
        colEmail.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getEmail())
        );
        colEmail.setCellFactory(col -> new TableCell<Utilisateur, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    HBox hbox = new HBox(8);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    
                    Label icon = new Label("📧");
                    icon.setStyle("-fx-font-size: 16px;");
                    
                    Label emailBadge = new Label(item);
                    emailBadge.getStyleClass().add("email-badge");
                    
                    hbox.getChildren().addAll(icon, emailBadge);
                    setGraphic(hbox);
                    setText(null);
                }
            }
        });

        // Colonne Mot de passe (masqué)
        colPassword.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getMot_de_passe())
        );
        colPassword.setCellFactory(col -> new TableCell<Utilisateur, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    HBox hbox = new HBox(8);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    
                    Label icon = new Label("🔒");
                    icon.setStyle("-fx-font-size: 14px;");
                    
                    // Masquer le mot de passe
                    String masked = "•".repeat(Math.min(item.length(), 8));
                    Label passwordLabel = new Label(masked);
                    passwordLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #9CA3AF;");
                    
                    hbox.getChildren().addAll(icon, passwordLabel);
                    setGraphic(hbox);
                    setText(null);
                }
            }
        });

        // Colonne Role avec badge gris
        colRole.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getRole())
        );
        colRole.setCellFactory(col -> new TableCell<Utilisateur, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    HBox hbox = new HBox(8);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    
                    Label icon = new Label("👤");
                    icon.setStyle("-fx-font-size: 16px;");
                    
                    Label roleBadge = new Label(item);
                    roleBadge.getStyleClass().add("role-badge");
                    
                    hbox.getChildren().addAll(icon, roleBadge);
                    setGraphic(hbox);
                    setText(null);
                }
            }
        });

        // Colonne Actions avec boutons Edit et Delete
        colActions.setCellFactory(col -> new TableCell<Utilisateur, Void>() {
            private final Button btnEdit = new Button("✎");
            private final Button btnDelete = new Button("🗑");
            private final HBox hbox = new HBox(10, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().add("btn-action");
                btnDelete.getStyleClass().add("btn-delete");
                btnEdit.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
                btnDelete.setStyle("-fx-font-size: 18px;");
                hbox.setAlignment(Pos.CENTER);

                btnEdit.setOnAction(event -> {
                    int index = getIndex();
                    if (index >= 0 && index < getTableView().getItems().size()) {
                        Utilisateur user = getTableView().getItems().get(index);
                        editUser(user);
                    }
                });

                btnDelete.setOnAction(event -> {
                    int index = getIndex();
                    if (index >= 0 && index < getTableView().getItems().size()) {
                        Utilisateur user = getTableView().getItems().get(index);
                        deleteUser(user);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Utilisateur user = getTableView().getItems().get(getIndex());
                    Utilisateur currentUser = UserSession.getInstance().getCurrentUser();
                    
                    // Vérifier si c'est l'utilisateur connecté
                    if (currentUser != null && user.getId_utilisateur() == currentUser.getId_utilisateur()) {
                        // Désactiver le bouton de suppression pour soi-même
                        btnDelete.setDisable(true);
                        btnDelete.setStyle("-fx-font-size: 18px; -fx-opacity: 0.3; -fx-cursor: not-allowed;");
                        Tooltip tooltip = new Tooltip("⚠️ Vous ne pouvez pas supprimer votre propre compte");
                        Tooltip.install(btnDelete, tooltip);
                    } else {
                        btnDelete.setDisable(false);
                        btnDelete.setStyle("-fx-font-size: 18px;");
                        Tooltip.uninstall(btnDelete, null);
                    }
                    
                    setGraphic(hbox);
                }
            }
        });
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            applyFilters();
        });
    }

    private void setupFilters() {
        // Setup filter role combo box
        filterRole.setItems(FXCollections.observableArrayList("Tous", "CLIENT", "ADMIN_RH", "ADMIN_TECHNIQUE"));
        filterRole.setValue("Tous");
        
        // Apply filters when selection changes
        filterRole.setOnAction(e -> applyFilters());
    }

    private void applyFilters() {
        ObservableList<Utilisateur> allUsers = FXCollections.observableArrayList(
            serviceUtilisateur.afficherAll()
        );
        
        // Filter by search query
        String query = searchField.getText();
        if (query != null && !query.isEmpty()) {
            allUsers = allUsers.filtered(user -> 
                user.getNom().toLowerCase().contains(query.toLowerCase()) ||
                user.getPrenom().toLowerCase().contains(query.toLowerCase()) ||
                user.getEmail().toLowerCase().contains(query.toLowerCase()) ||
                user.getRole().toLowerCase().contains(query.toLowerCase())
            );
        }
        
        // Filter by role
        String role = filterRole.getValue();
        if (role != null && !"Tous".equals(role)) {
            allUsers = allUsers.filtered(user -> user.getRole().equals(role));
        }
        
        tableUsers.setItems(allUsers);
        updateUserCount(allUsers.size());
    }

    @FXML
    public void resetFilters() {
        searchField.clear();
        filterRole.setValue("Tous");
        applyFilters();
        
        System.out.println("🔄 Filtres réinitialisés");
    }

    private void updateUserCount(int count) {
        lblTotalCount.setText(String.format("Total: %d utilisateur%s", count, count > 1 ? "s" : ""));
    }

    private void filterTable(String query) {
        applyFilters();
    }

    public void refreshTable() {
        applyFilters();
    }

    @FXML
    public void showAddUserModal() {
        isEditMode = false;
        selectedUser = null;
        currentImageName = "default.png";
        modalTitle.setText("➕ Ajouter un utilisateur");
        btnSave.setText("💾 Enregistrer");
        clearForm();
        resetProfileImage();
        lblModalMessage.setText("");
        
        // Afficher le modal
        modalOverlay.setVisible(true);
        modalOverlay.setManaged(true);
        modalOverlay.toFront();
        
        // Animation simple d'ouverture
        modalContent.setOpacity(0);
        modalContent.setScaleX(0.9);
        modalContent.setScaleY(0.9);
        
        FadeTransition fade = new FadeTransition(Duration.millis(300), modalContent);
        fade.setFromValue(0);
        fade.setToValue(1.0);
        
        ScaleTransition scale = new ScaleTransition(Duration.millis(300), modalContent);
        scale.setFromX(0.9);
        scale.setFromY(0.9);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.setInterpolator(Interpolator.EASE_OUT);
        
        ParallelTransition parallel = new ParallelTransition(fade, scale);
        parallel.play();
    }
    
    private void resetProfileImage() {
        try {
            if (profileImageContainer != null) {
                profileImageContainer.setStyle(
                    "-fx-background-color: #1a1a2e;" +
                    "-fx-background-radius: 47;" +
                    "-fx-min-width: 90; -fx-min-height: 90; -fx-max-width: 90; -fx-max-height: 90;"
                );
            }
            if (lblProfileIcon != null) {
                lblProfileIcon.setText("\uD83D\uDC64");
                lblProfileIcon.setStyle("-fx-font-size: 40px;");
                lblProfileIcon.setVisible(true);
                lblProfileIcon.setManaged(true);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la réinitialisation de l'image: " + e.getMessage());
        }
    }
    
    private void animateModalOpen() {
        // Position initiale : inverse de la fermeture
        modalContent.setScaleX(0.8);
        modalContent.setScaleY(0.8);
        modalContent.setOpacity(0);
        
        // Animation de scale (zoom in)
        ScaleTransition scale = new ScaleTransition(Duration.millis(400), modalContent);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.setInterpolator(Interpolator.SPLINE(0.25, 0.1, 0.25, 1.0)); // Ease out back
        
        // Animation de fade in
        FadeTransition fade = new FadeTransition(Duration.millis(400), modalContent);
        fade.setFromValue(0);
        fade.setToValue(1.0);
        fade.setInterpolator(Interpolator.EASE_OUT);
        
        // Animation du background overlay
        FadeTransition overlayFade = new FadeTransition(Duration.millis(300), modalOverlay);
        overlayFade.setFromValue(0);
        overlayFade.setToValue(1.0);
        
        // Jouer toutes les animations ensemble
        ParallelTransition parallel = new ParallelTransition(scale, fade);
        
        overlayFade.play();
        parallel.play();
        
        // Petit effet de rebond à la fin
        parallel.setOnFinished(e -> {
            ScaleTransition bounce = new ScaleTransition(Duration.millis(150), modalContent);
            bounce.setFromX(1.0);
            bounce.setFromY(1.0);
            bounce.setToX(1.05);
            bounce.setToY(1.05);
            bounce.setCycleCount(2);
            bounce.setAutoReverse(true);
            bounce.setInterpolator(Interpolator.EASE_BOTH);
            bounce.play();
        });
    }

    private void editUser(Utilisateur user) {
        if (user == null) return;
        
        isEditMode = true;
        selectedUser = user;
        currentImageName = user.getImage_profil() != null ? user.getImage_profil() : "default.png";
        
        System.out.println("DEBUG: Editing user - Image: " + currentImageName + ", Role: " + user.getRole());
        
        // Update modal title and button text
        modalTitle.setText("✏️ Modifier l'utilisateur");
        btnSave.setText("🔄 Mettre à jour");
        
        // Fill in the form fields
        tfNom.setText(user.getNom() != null ? user.getNom() : "");
        tfPrenom.setText(user.getPrenom() != null ? user.getPrenom() : "");
        tfEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        tfPassword.setText(user.getMot_de_passe() != null ? user.getMot_de_passe() : "");
        
        // Set ComboBox role - use only selection model for proper visual update
        String userRole = user.getRole() != null ? user.getRole() : "CLIENT";
        System.out.println("DEBUG: Setting ComboBox to: " + userRole);
        System.out.println("DEBUG: ComboBox items: " + cbRole.getItems());
        cbRole.getSelectionModel().select(userRole);
        System.out.println("DEBUG: ComboBox selected value: " + cbRole.getValue());
        
        // Load profile image
        loadProfileImage(currentImageName);
        
        // Clear message
        lblModalMessage.setText("");
        
        // Reset modal content properties
        modalContent.setScaleX(1.0);
        modalContent.setScaleY(1.0);
        modalContent.setOpacity(1.0);
        
        // Show modal with animation
        modalOverlay.setVisible(true);
        modalOverlay.setManaged(true);
        modalOverlay.toFront();
        
        FadeTransition fade = new FadeTransition(Duration.millis(300), modalOverlay);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        
        ScaleTransition scale = new ScaleTransition(Duration.millis(300), modalContent);
        scale.setFromX(0.85);
        scale.setFromY(0.85);
        scale.setToX(1.0);
        scale.setToY(1.0);
        
        ParallelTransition transition = new ParallelTransition(fade, scale);
        transition.play();
    }
    
    private void loadProfileImage(String imageName) {
        try {
            System.out.println("DEBUG: Loading image: " + imageName);
            if (imageName == null || imageName.isEmpty() || imageName.equals("default.png")) {
                resetProfileImage();
                return;
            }
            
            File imgFile = new File("images/" + imageName);
            System.out.println("DEBUG: Image path: " + imgFile.getAbsolutePath());
            System.out.println("DEBUG: Image exists: " + imgFile.exists());
            
            if (imgFile.exists()) {
                // Use CSS background-image - most reliable visual method
                String imageUrl = imgFile.toURI().toString();
                lblProfileIcon.setVisible(false);
                lblProfileIcon.setManaged(false);
                profileImageContainer.setStyle(
                    "-fx-background-image: url('" + imageUrl + "');" +
                    "-fx-background-size: cover;" +
                    "-fx-background-position: center;" +
                    "-fx-background-radius: 47;" +
                    "-fx-min-width: 90; -fx-min-height: 90; -fx-max-width: 90; -fx-max-height: 90;"
                );
                System.out.println("DEBUG: Image set via CSS background");
            } else {
                resetProfileImage();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
            e.printStackTrace();
            resetProfileImage();
        }
    }
    
    private void animateModalEdit() {
        // Petite pulsation pour indiquer le mode édition
        ScaleTransition pulse = new ScaleTransition(Duration.millis(300), modalContent);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.02);
        pulse.setToY(1.02);
        pulse.setCycleCount(2);
        pulse.setAutoReverse(true);
        pulse.setInterpolator(Interpolator.EASE_BOTH);
        pulse.play();
    }

    private void deleteUser(Utilisateur user) {
        // Vérification de sécurité: empêcher la suppression de soi-même
        Utilisateur currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null && user.getId_utilisateur() == currentUser.getId_utilisateur()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("⚠️ Action interdite");
            alert.setHeaderText("Impossible de supprimer votre propre compte");
            alert.setContentText(
                "🚫 Vous ne pouvez pas supprimer votre propre compte pour des raisons de sécurité.\n\n" +
                "👥 Demandez à un autre administrateur de le faire si nécessaire."
            );
            alert.showAndWait();
            return;
        }
        
        userToDelete = user;
        
        // Afficher les informations de l'utilisateur
        lblDeleteUserInfo.setText(
            "👤 " + user.getNom() + " " + user.getPrenom() + "\n" +
            "📧 " + user.getEmail() + "\n" +
            "🎭 " + user.getRole()
        );
        
        // Afficher le modal avec animation
        deleteModalOverlay.setVisible(true);
        deleteModalOverlay.setManaged(true);
        deleteModalOverlay.toFront();
        
        // Animation d'ouverture
        deleteModalContent.setOpacity(0);
        deleteModalContent.setScaleX(0.7);
        deleteModalContent.setScaleY(0.7);
        
        FadeTransition fade = new FadeTransition(Duration.millis(300), deleteModalContent);
        fade.setFromValue(0);
        fade.setToValue(1);
        
        ScaleTransition scaleX = new ScaleTransition(Duration.millis(300), deleteModalContent);
        scaleX.setFromX(0.7);
        scaleX.setToX(1.0);
        
        ScaleTransition scaleY = new ScaleTransition(Duration.millis(300), deleteModalContent);
        scaleY.setFromY(0.7);
        scaleY.setToY(1.0);
        
        ParallelTransition parallel = new ParallelTransition(fade, scaleX, scaleY);
        parallel.setInterpolator(Interpolator.EASE_OUT);
        parallel.play();
        
        // Animation de l'icône
        RotateTransition rotate = new RotateTransition(Duration.millis(400), lblDeleteIcon);
        rotate.setByAngle(15);
        rotate.setCycleCount(6);
        rotate.setAutoReverse(true);
        rotate.play();
    }
    
    @FXML
    private void closeDeleteModal() {
        // Animation de fermeture
        FadeTransition fade = new FadeTransition(Duration.millis(200), deleteModalContent);
        fade.setToValue(0);
        
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), deleteModalContent);
        scale.setToX(0.7);
        scale.setToY(0.7);
        
        ParallelTransition parallel = new ParallelTransition(fade, scale);
        parallel.setOnFinished(e -> {
            deleteModalOverlay.setVisible(false);
            deleteModalOverlay.setManaged(false);
            userToDelete = null;
        });
        parallel.play();
    }
    
    @FXML
    private void confirmDelete() {
        if (userToDelete != null) {
            String nom = userToDelete.getNom();
            String prenom = userToDelete.getPrenom();
            
            // Supprimer l'utilisateur
            serviceUtilisateur.supprimer(userToDelete.getId_utilisateur());
            
            // Fermer le modal
            closeDeleteModal();
            
            // Rafraîchir le tableau
            refreshTable();
            
            // Animation de succès
            new Thread(() -> {
                try {
                    Thread.sleep(300);
                    javafx.application.Platform.runLater(() -> {
                        Alert success = new Alert(Alert.AlertType.INFORMATION);
                        success.setTitle("✅ Suppression réussie");
                        success.setHeaderText(null);
                        success.setContentText(
                            "🎉 L'utilisateur " + nom + " " + prenom + " a été supprimé !\n\n" +
                            "Les données ont été retirées de la base de données."
                        );
                        success.showAndWait();
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @FXML
    public void uploadImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir une photo de profil");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fc.showOpenDialog(null);

        if (file != null) {
            try {
                File folder = new File("images");
                if (!folder.exists()) folder.mkdir();

                // Générer un nom de fichier unique pour éviter les conflits
                String originalName = file.getName();
                String extension = "";
                int i = originalName.lastIndexOf('.');
                if (i > 0) {
                    extension = originalName.substring(i);
                    originalName = originalName.substring(0, i);
                }
                
                // Nettoyer le nom de fichier (enlever les espaces et caractères spéciaux)
                String cleanName = originalName.replaceAll("[^a-zA-Z0-9-_]", "_");
                String uniqueName = cleanName + "_" + System.currentTimeMillis() + extension;

                Path dest = Path.of("images", uniqueName);
                Files.copy(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

                currentImageName = uniqueName;
                loadProfileImage(currentImageName);
                
                // Animation de confirmation
                ScaleTransition scale = new ScaleTransition(Duration.millis(200), profileImageContainer);
                scale.setFromX(0.9);
                scale.setFromY(0.9);
                scale.setToX(1.1);
                scale.setToY(1.1);
                scale.setCycleCount(2);
                scale.setAutoReverse(true);
                scale.play();

            } catch (Exception e) {
                lblModalMessage.setStyle("-fx-text-fill: #DC2626;");
                lblModalMessage.setText("❌ Erreur lors du chargement de l'image !");
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void saveUser() {
        // Validation des champs vides
        if (tfNom.getText().trim().isEmpty() || tfPrenom.getText().trim().isEmpty() || 
            tfEmail.getText().trim().isEmpty() || tfPassword.getText().trim().isEmpty()) {
            
            lblModalMessage.setStyle("-fx-text-fill: #DC2626;");
            lblModalMessage.setText("❌ Veuillez remplir tous les champs !");
            
            // Shake animation
            shakeAnimation(modalContent);
            return;
        }

        // Validation longueur nom et prénom
        if (tfNom.getText().trim().length() < 2 || tfPrenom.getText().trim().length() < 2) {
            lblModalMessage.setStyle("-fx-text-fill: #DC2626;");
            lblModalMessage.setText("❌ Nom et prénom doivent contenir au moins 2 caractères !");
            shakeAnimation(modalContent);
            return;
        }

        // Validation format email
        if (!tfEmail.getText().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            lblModalMessage.setStyle("-fx-text-fill: #DC2626;");
            lblModalMessage.setText("❌ Format d'email invalide !");
            shakeAnimation(modalContent);
            return;
        }

        // Validation longueur mot de passe
        if (tfPassword.getText().length() < 6) {
            lblModalMessage.setStyle("-fx-text-fill: #DC2626;");
            lblModalMessage.setText("❌ Le mot de passe doit contenir au moins 6 caractères !");
            shakeAnimation(modalContent);
            return;
        }

        if (!isEditMode) {
            // Vérifier si l'email existe déjà
            if (serviceUtilisateur.emailExiste(tfEmail.getText())) {
                lblModalMessage.setStyle("-fx-text-fill: #DC2626;");
                lblModalMessage.setText("❌ Cet email est déjà utilisé !");
                shakeAnimation(modalContent);
                return;
            }

            // Ajouter nouveau utilisateur
            Utilisateur newUser = new Utilisateur(
                0,
                currentImageName,
                tfEmail.getText(),
                tfPassword.getText(),
                cbRole.getValue(),
                tfNom.getText(),
                tfPrenom.getText()
            );

            serviceUtilisateur.ajouter(newUser);
            
            lblModalMessage.setStyle("-fx-text-fill: #10B981;");
            lblModalMessage.setText("✅ Utilisateur ajouté avec succès !");
            
        } else {
            // Modifier utilisateur existant
            Utilisateur updatedUser = new Utilisateur(
                selectedUser.getId_utilisateur(),
                currentImageName,
                tfEmail.getText(),
                tfPassword.getText(),
                cbRole.getValue(),
                tfNom.getText(),
                tfPrenom.getText()
            );

            serviceUtilisateur.modifier(updatedUser);
            
            lblModalMessage.setStyle("-fx-text-fill: #10B981;");
            lblModalMessage.setText("✅ Utilisateur modifié avec succès !");
        }
        
        // Animation de succès
        animateSuccess();

        refreshTable();
        
        // Fermer le modal après 1.5 secondes
        new Thread(() -> {
            try {
                Thread.sleep(1500);
                javafx.application.Platform.runLater(this::closeModal);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private void shakeAnimation(javafx.scene.Node node) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), node);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }
    
    private void animateSuccess() {
        // Animation de rebond
        ScaleTransition scale = new ScaleTransition(Duration.millis(300), modalContent);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.05);
        scale.setToY(1.05);
        scale.setCycleCount(2);
        scale.setAutoReverse(true);
        scale.play();
    }

    @FXML
    public void closeModal() {
        // Animation de fermeture
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), modalContent);
        scale.setToX(0.8);
        scale.setToY(0.8);
        
        FadeTransition fade = new FadeTransition(Duration.millis(200), modalContent);
        fade.setToValue(0);
        
        ParallelTransition parallel = new ParallelTransition(scale, fade);
        parallel.setOnFinished(e -> {
            modalOverlay.setVisible(false);
            modalOverlay.setManaged(false);
            
            // Réinitialiser les propriétés
            modalContent.setScaleX(1.0);
            modalContent.setScaleY(1.0);
            modalContent.setOpacity(1.0);
        });
        parallel.play();
        
        clearForm();
        lblModalMessage.setText("");
    }

    private void clearForm() {
        tfNom.clear();
        tfPrenom.clear();
        tfEmail.clear();
        tfPassword.clear();
        cbRole.setValue("CLIENT");
        currentImageName = "default.png";
        resetProfileImage();
    }

    @FXML
    public void goToStatistiques() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StatistiquesView.fxml"));
            Parent root = loader.load();
            Scene scene = tableUsers.getScene();
            scene.setRoot(root);
            System.out.println("✓ Navigation vers Statistiques");
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Impossible d'ouvrir les statistiques: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void logout() {
        try {
            UserSession.getInstance().clearSession();
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SigninView.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) tableUsers.getScene().getWindow();
            Scene newScene = new Scene(root, 1440, 1024);
            stage.setScene(newScene);
            stage.setResizable(false);
            stage.setTitle("Connexion - VOS");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur de déconnexion");
            alert.setContentText("Impossible de se déconnecter: " + e.getMessage());
            alert.showAndWait();
        }
    }
}
