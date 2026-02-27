package controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import controllers.admin.FormCandidatureAdminController;
import controllers.utilisateur.DetailCandidatureUtilisateurController;
import controllers.utilisateur.FormCandidatureUtilisateurController;
import controllers.utilisateur.ListeCandidaturesUtilisateurController;
import controllers.utilisateur.MatchingUtilisateurController;
import entities.Candidature;
import services.candidat.CandidatureService;

public class TestLauncherController implements Initializable {

    /* ══════════════════════════════════════════
       FXML
    ══════════════════════════════════════════ */
    @FXML
    private TextField fieldUserId;
    @FXML
    private Label errorLabel;

    /* ══════════════════════════════════════════
       CHEMINS FXML
       Structure attendue dans resources :
       src/main/resources/
         vos/gestionCandidat/fxml/
           candidatures/
             admin/
               ListeCandidaturesAdmin.fxml
               FormCandidatureAdmin.fxml
             utilisateur/
               ListeCandidaturesUtilisateur.fxml
               FormCandidatureUtilisateur.fxml
               DetailCandidatureUtilisateur.fxml
           TestLauncher.fxml
    ══════════════════════════════════════════ */
    private static final String ADMIN_LISTE
            = "/fxml/admin/ListeCandidaturesAdmin.fxml";
    private static final String ADMIN_FORM
            = "/fxml/admin/FormCandidatureAdmin.fxml";
    private static final String USER_LISTE
            = "/fxml/utilisateur/ListeCandidaturesUtilisateur.fxml";
    private static final String USER_FORM
            = "/fxml/utilisateur/FormCandidatureUtilisateur.fxml";
    private static final String USER_DETAIL
            = "/fxml/utilisateur/DetailCandidatureUtilisateur.fxml";

    private static final String USER_MATCHING
            = "/fxml/utilisateur/MatchingUtilisateur.fxml";
    private final CandidatureService service = new CandidatureService();

    /* ══════════════════════════════════════════
       INIT
    ══════════════════════════════════════════ */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // id=3 = Yassine (CLIENT) dans ta BDD — modifie librement
        fieldUserId.setText("3");
        cacherErreur();
    }

    /* ══════════════════════════════════════════
       BOUTONS ADMIN
    ══════════════════════════════════════════ */
    @FXML
    private void ouvrirAdminListe(ActionEvent event) {
        charger(ADMIN_LISTE, "Admin — Liste des Candidatures", 1400, 800,
                loader -> {
                    /* rien, initialize() charge tout seul */ });
    }

    @FXML
    private void ouvrirAdminForm(ActionEvent event) {
        charger(ADMIN_FORM, "Admin — Nouvelle Candidature", 900, 750, loader -> {
            FormCandidatureAdminController ctrl = loader.getController();
            ctrl.initData(null, null);   // null = mode ajout
        });
    }

    /* ══════════════════════════════════════════
       BOUTONS UTILISATEUR
    ══════════════════════════════════════════ */
    @FXML
    private void ouvrirUserListe(ActionEvent event) {
        int uid = parseUserId();
        if (uid < 0) {
            return;
        }

        charger(USER_LISTE, "Candidat #" + uid + " — Mes Candidatures", 1300, 800, loader -> {
            ListeCandidaturesUtilisateurController ctrl = loader.getController();
            ctrl.setIdUtilisateurCourant(uid);
        });
    }

    @FXML
    private void ouvrirUserForm(ActionEvent event) {
        int uid = parseUserId();
        if (uid < 0) {
            return;
        }

        charger(USER_FORM, "Candidat #" + uid + " — Nouvelle Candidature", 1100, 800, loader -> {
            FormCandidatureUtilisateurController ctrl = loader.getController();
            ctrl.initData(null, null, uid);
        });
    }

    @FXML
    private void ouvrirUserDetail(ActionEvent event) {
        int uid = parseUserId();
        if (uid < 0) {
            return;
        }

        Candidature c = service.getAll().stream()
                .filter(x -> x.getIdUtilisateur() == uid)
                .findFirst()
                .orElse(null);

        if (c == null) {
            afficherErreur("Aucune candidature pour l'utilisateur #" + uid
                    + ". Créez-en une d'abord.");
            return;
        }

        final Candidature candidature = c;
        charger(USER_DETAIL, "Candidat #" + uid + " — Détail", 1300, 800, loader -> {
            DetailCandidatureUtilisateurController ctrl = loader.getController();
            ctrl.initData(candidature, null);
        });
    }

    /* ══════════════════════════════════════════
       MOTEUR DE CHARGEMENT
    ══════════════════════════════════════════ */
    private void charger(String chemin, String titre,
            double largeur, double hauteur,
            PostLoad callback) {

        URL url = getClass().getResource(chemin);

        if (url == null) {
            afficherErreur("FXML introuvable : " + chemin
                    + "\n→ Vérifie l'emplacement dans src/main/resources");
            System.err.println("[TestLauncher] Ressource null : " + chemin);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            callback.apply(loader);          // injection post-load

            Stage stage = new Stage();
            stage.setTitle(titre);
            stage.setScene(new Scene(root));
            stage.setWidth(largeur);
            stage.setHeight(hauteur);
            stage.centerOnScreen();
            stage.show();
            cacherErreur();

        } catch (IOException e) {
            afficherErreur("Erreur chargement : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /* ══════════════════════════════════════════
       UTILITAIRES
    ══════════════════════════════════════════ */
    private int parseUserId() {
        try {
            int id = Integer.parseInt(fieldUserId.getText().trim());
            if (id <= 0) {
                afficherErreur("ID utilisateur doit être > 0.");
                return -1;
            }
            cacherErreur();
            return id;
        } catch (NumberFormatException e) {
            afficherErreur("ID invalide — entrez un entier (ex: 3).");
            return -1;
        }
    }

    private void afficherErreur(String msg) {
        errorLabel.setText("⚠️  " + msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void cacherErreur() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    @FunctionalInterface
    private interface PostLoad {

        void apply(FXMLLoader loader) throws IOException;
    }

    @FXML
    private void ouvrirUserMatching(ActionEvent event) {
        int uid = parseUserId();
        if (uid < 0) {
            return;
        }

        charger(TestLauncherController.USER_MATCHING, "Candidat #" + uid + " — Matching Offres", 1200, 800, loader -> {
            MatchingUtilisateurController ctrl = loader.getController();
            ctrl.setIdUtilisateurConnecte(uid);
        });
    }

}
