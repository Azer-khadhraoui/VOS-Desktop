package vos.gestionCandidat.controllers.admin;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import vos.gestionCandidat.entities.PreferenceCandidature;
import vos.gestionCandidat.services.candidat.PreferenceCandidatureService;

public class ListePreferencesAdminController implements Initializable {

    @FXML private TableView<PreferenceCandidature> tablePreferences;
    @FXML private TableColumn<PreferenceCandidature, Integer> colId;
    @FXML private TableColumn<PreferenceCandidature, Integer> colUtilisateur;
    @FXML private TableColumn<PreferenceCandidature, String>  colTypePoste;
    @FXML private TableColumn<PreferenceCandidature, String>  colModeTravail;
    @FXML private TableColumn<PreferenceCandidature, String>  colDisponibilite;
    @FXML private TableColumn<PreferenceCandidature, String>  colContrat;
    @FXML private TableColumn<PreferenceCandidature, Double>  colSalaire;
    @FXML private TableColumn<PreferenceCandidature, String>  colDateDispo;
    @FXML private TableColumn<PreferenceCandidature, Void>    colActions;

    private final PreferenceCandidatureService service = new PreferenceCandidatureService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("idPreference"));
        colUtilisateur.setCellValueFactory(new PropertyValueFactory<>("idUtilisateur"));
        colTypePoste.setCellValueFactory(new PropertyValueFactory<>("typePosteSouhaite"));
        colModeTravail.setCellValueFactory(new PropertyValueFactory<>("modeTravail"));
        colDisponibilite.setCellValueFactory(new PropertyValueFactory<>("disponibilite"));
        colContrat.setCellValueFactory(new PropertyValueFactory<>("typeContratSouhaite"));
        colSalaire.setCellValueFactory(new PropertyValueFactory<>("pretentionSalariale"));
        colDateDispo.setCellValueFactory(new PropertyValueFactory<>("dateDisponibilite"));

        ajouterColonneActions();
        chargerDonnees();
    }

    private void chargerDonnees() {
        List<PreferenceCandidature> liste = service.getAll();
        tablePreferences.setItems(FXCollections.observableArrayList(liste));
    }

    private void ajouterColonneActions() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnSupprimer = new Button("🗑 Supprimer");

            {
                btnSupprimer.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
                btnSupprimer.setOnAction(e -> {
                    PreferenceCandidature p = getTableView().getItems().get(getIndex());
                    service.supprimer(p.getIdPreference());
                    chargerDonnees();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnSupprimer);
            }
        });
    }

    @FXML
    private void retournerAuxCandidatures(ActionEvent event) {
        ((Stage) tablePreferences.getScene().getWindow()).close();
    }
    public void rafraichir() {
        chargerDonnees();
    }

    
}