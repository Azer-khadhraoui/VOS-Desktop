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
    @FXML private Label lblImageName;

    private ServiceUtilisateur serviceUtilisateur = new ServiceUtilisateur();
    private Utilisateur selectedUser = null;
    private boolean isEditMode = false;
    private String currentImageName = "default.png";

    @FXML
    public void initialize() {
        setupTable();
        setupComboBox();
        refreshTable();
        setupSearch();
        loadCurrentUser();
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
                    Utilisateur user = getTableView().getItems().get(getIndex());
                    editUser(user);
                });

                btnDelete.setOnAction(event -> {
                    Utilisateur user = getTableView().getItems().get(getIndex());
                    deleteUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
            }
        });
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                refreshTable();
            } else {
                filterTable(newVal.toLowerCase());
            }
        });
    }

    private void filterTable(String query) {
        ObservableList<Utilisateur> allUsers = FXCollections.observableArrayList(
            serviceUtilisateur.afficherAll()
        );
        
        ObservableList<Utilisateur> filtered = allUsers.filtered(user -> 
            user.getNom().toLowerCase().contains(query) ||
            user.getPrenom().toLowerCase().contains(query) ||
            user.getEmail().toLowerCase().contains(query) ||
            user.getRole().toLowerCase().contains(query)
        );
        
        tableUsers.setItems(filtered);
    }

    public void refreshTable() {
        ObservableList<Utilisateur> users = FXCollections.observableArrayList(
            serviceUtilisateur.afficherAll()
        );
        tableUsers.setItems(users);
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
        modalOverlay.setVisible(true);
        modalOverlay.setManaged(true);
        
        // Animation d'ouverture
        animateModalOpen();
    }
    
    private void resetProfileImage() {
        profileImageContainer.getChildren().clear();
        lblProfileIcon.setText("👤");
        profileImageContainer.getChildren().add(lblProfileIcon);
        lblImageName.setText("");
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
        isEditMode = true;
        selectedUser = user;
        currentImageName = user.getImage_profil();
        modalTitle.setText("✏️ Modifier l'utilisateur");
        btnSave.setText("🔄 Mettre à jour");
        
        tfNom.setText(user.getNom());
        tfPrenom.setText(user.getPrenom());
        tfEmail.setText(user.getEmail());
        tfPassword.setText(user.getMot_de_passe());
        cbRole.setValue(user.getRole());
        
        // Charger l'image de profil
        loadProfileImage(user.getImage_profil());
        
        lblModalMessage.setText("");
        modalOverlay.setVisible(true);
        modalOverlay.setManaged(true);
        
        // Animation d'ouverture identique à l'ajout
        animateModalOpen();
        
        // Puis animation mode édition après l'ouverture
        new Thread(() -> {
            try {
                Thread.sleep(500);
                javafx.application.Platform.runLater(this::animateModalEdit);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private void loadProfileImage(String imageName) {
        try {
            File imgFile = new File("images/" + imageName);
            if (imgFile.exists()) {
                ImageView imageView = new ImageView();
                imageView.setFitWidth(110);
                imageView.setFitHeight(110);
                imageView.setPreserveRatio(false);
                
                javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(55, 55, 55);
                imageView.setClip(clip);
                
                Image img = new Image(new FileInputStream(imgFile));
                imageView.setImage(img);
                
                profileImageContainer.getChildren().clear();
                profileImageContainer.getChildren().add(imageView);
                lblImageName.setText(imageName);
            } else {
                resetProfileImage();
            }
        } catch (Exception e) {
            resetProfileImage();
        }
    }
    
    private void animateModalEdit() {
        // Animation subtile de pulsation pour indiquer le mode édition
        modalContent.getStyleClass().clear();
        modalContent.getStyleClass().add("modal-content");
        modalContent.getStyleClass().add("modal-content-editing");
        
        // Petite pulsation
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
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'utilisateur");
        alert.setContentText("Voulez-vous vraiment supprimer " + user.getNom() + " " + user.getPrenom() + " ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                serviceUtilisateur.supprimer(user.getId_utilisateur());
                refreshTable();
                
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Succès");
                success.setHeaderText(null);
                success.setContentText("✅ Utilisateur supprimé avec succès !");
                success.showAndWait();
            }
        });
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
        // Validation
        if (tfNom.getText().isEmpty() || tfPrenom.getText().isEmpty() || 
            tfEmail.getText().isEmpty() || tfPassword.getText().isEmpty()) {
            
            lblModalMessage.setStyle("-fx-text-fill: #DC2626;");
            lblModalMessage.setText("❌ Veuillez remplir tous les champs !");
            
            // Shake animation
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
        // Changer le style du modal temporairement
        modalContent.getStyleClass().clear();
        modalContent.getStyleClass().add("modal-content");
        modalContent.getStyleClass().add("modal-content-success");
        
        // Animation de rebond
        ScaleTransition scale = new ScaleTransition(Duration.millis(300), modalContent);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.05);
        scale.setToY(1.05);
        scale.setCycleCount(2);
        scale.setAutoReverse(true);
        scale.play();
        
        // Réinitialiser le style après l'animation
        scale.setOnFinished(e -> {
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    javafx.application.Platform.runLater(() -> {
                        modalContent.getStyleClass().clear();
                        modalContent.getStyleClass().add("modal-content");
                    });
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
        });
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
            
            // Réinitialiser les styles
            modalContent.getStyleClass().clear();
            modalContent.getStyleClass().add("modal-content");
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
    public void logout() {
        try {
            UserSession.getInstance().clearSession();
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SigninView.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) tableUsers.getScene().getWindow();
            Scene newScene = new Scene(root);
            stage.setScene(newScene);
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
