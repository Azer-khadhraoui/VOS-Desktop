package services.candidat;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import entities.PreferenceCandidature;
import utilis.MyConnection;

public class PreferenceCandidatureService implements IService<PreferenceCandidature>{
    private Connection connection;
    public PreferenceCandidatureService(){
        connection = MyConnection.getInstance().getCnx();
    }

    @Override
    public void ajouter(PreferenceCandidature preference) {
        String requete = "INSERT INTO preference_candidature (type_poste_souhaite, mode_travail, " +
                "disponibilite, mobilite_geographique, pret_deplacement, " +
                "type_contrat_souhaite, pretention_salariale, date_disponibilite, id_utilisateur) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement pst = connection.prepareStatement(requete);
            pst.setString(1, preference.getTypePosteSouhaite());
            pst.setString(2, preference.getModeTravail());
            pst.setString(3, preference.getDisponibilite());
            pst.setString(4, preference.getMobiliteGeographique());
            pst.setString(5, preference.getPretDeplacement());
            pst.setString(6, preference.getTypeContratSouhaite());
            pst.setDouble(7, preference.getPretentionSalariale());
            pst.setDate(8, new Date(preference.getDateDisponibilite().getTime()));
            pst.setInt(9, preference.getidUtilisateur());

            pst.executeUpdate();
            System.out.println("Préférence candidature ajoutée avec succès !");

        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    @Override
    public void modifier(PreferenceCandidature preference) {
        String requete = "UPDATE preference_candidature SET type_poste_souhaite = ?, " +
                "mode_travail = ?, disponibilite = ?, mobilite_geographique = ?, " +
                "pret_deplacement = ?, type_contrat_souhaite = ?, pretention_salariale = ?, " +
                "date_disponibilite = ?, id_utilisateur = ? " +
                "WHERE id_preference = ?";

        try {
            PreparedStatement pst = connection.prepareStatement(requete);
            pst.setString(1, preference.getTypePosteSouhaite());
            pst.setString(2, preference.getModeTravail());
            pst.setString(3, preference.getDisponibilite());
            pst.setString(4, preference.getMobiliteGeographique());
            pst.setString(5, preference.getPretDeplacement());
            pst.setString(6, preference.getTypeContratSouhaite());
            pst.setDouble(7, preference.getPretentionSalariale());
            pst.setDate(8, new Date(preference.getDateDisponibilite().getTime()));
            pst.setInt(9, preference.getidUtilisateur());
            pst.setInt(10, preference.getIdPreference());

            pst.executeUpdate();
            System.out.println("Préférence candidature modifiée avec succès !");

        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification : " + e.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {
        String requete = "DELETE FROM preference_candidature WHERE id_preference = ?";

        try {
            PreparedStatement pst = connection.prepareStatement(requete);
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Préférence candidature supprimée avec succès !");

        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression : " + e.getMessage());
        }
    }

    @Override
    public PreferenceCandidature getById(int id) {
        String requete = "SELECT * FROM preference_candidature WHERE id_preference = ?";
        PreferenceCandidature preference = null;

        try {
            PreparedStatement pst = connection.prepareStatement(requete);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                preference = new PreferenceCandidature();
                preference.setIdPreference(rs.getInt("id_preference"));
                preference.setTypePosteSouhaite(rs.getString("type_poste_souhaite"));
                preference.setModeTravail(rs.getString("mode_travail"));
                preference.setDisponibilite(rs.getString("disponibilite"));
                preference.setMobiliteGeographique(rs.getString("mobilite_geographique"));
                preference.setPretDeplacement(rs.getString("pret_deplacement"));
                preference.setTypeContratSouhaite(rs.getString("type_contrat_souhaite"));
                preference.setPretentionSalariale(rs.getDouble("pretention_salariale"));
                preference.setDateDisponibilite(rs.getDate("date_disponibilite"));
                preference.setIdUtilisateur(rs.getInt("id_utilisateur"));
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération : " + e.getMessage());
        }

        return preference;
    }

    @Override
    public List<PreferenceCandidature> getAll() {
        String requete = "SELECT * FROM preference_candidature";
        List<PreferenceCandidature> preferences = new ArrayList<>();

        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(requete);

            while (rs.next()) {
                PreferenceCandidature preference = new PreferenceCandidature();
                preference.setIdPreference(rs.getInt("id_preference"));
                preference.setTypePosteSouhaite(rs.getString("type_poste_souhaite"));
                preference.setModeTravail(rs.getString("mode_travail"));
                preference.setDisponibilite(rs.getString("disponibilite"));
                preference.setMobiliteGeographique(rs.getString("mobilite_geographique"));
                preference.setPretDeplacement(rs.getString("pret_deplacement"));
                preference.setTypeContratSouhaite(rs.getString("type_contrat_souhaite"));
                preference.setPretentionSalariale(rs.getDouble("pretention_salariale"));
                preference.setDateDisponibilite(rs.getDate("date_disponibilite"));
                preference.setIdUtilisateur((rs.getInt("id_utilisateur")));

                preferences.add(preference);
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération : " + e.getMessage());
        }

        return preferences;
    }

        /**
     * Méthode supplémentaire : Récupérer la préférence par candidature
     * @param idCandidature ID de la candidature
     * @return Préférence associée à l'utilisateur (null si aucune)
     */
    public PreferenceCandidature getByIdUtilisateur  (int idUtilisateur) {
        String requete = "SELECT * FROM preference_candidature WHERE id_utilisateur = ?";
        PreferenceCandidature preference = null;

        try {
            PreparedStatement pst = connection.prepareStatement(requete);
            pst.setInt(1, idUtilisateur);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                preference = new PreferenceCandidature();
                preference.setIdPreference(rs.getInt("id_preference"));
                preference.setTypePosteSouhaite(rs.getString("type_poste_souhaite"));
                preference.setModeTravail(rs.getString("mode_travail"));
                preference.setDisponibilite(rs.getString("disponibilite"));
                preference.setMobiliteGeographique(rs.getString("mobilite_geographique"));
                preference.setPretDeplacement(rs.getString("pret_deplacement"));
                preference.setTypeContratSouhaite(rs.getString("type_contrat_souhaite"));
                preference.setPretentionSalariale(rs.getDouble("pretention_salariale"));
                preference.setDateDisponibilite(rs.getDate("date_disponibilite"));
                preference.setIdUtilisateur(rs.getInt("id_utilisateur"));
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération : " + e.getMessage());
        }

        return preference;
    }

    /**
     * Récupérer toutes les préférences d'une candidature donnée
     */
    //v1 get by id user
    public List<PreferenceCandidature> getByUtilisateur(int idUtilisateur) {
    String requete = "SELECT * FROM preference_candidature WHERE id_utilisateur = ?";
    List<PreferenceCandidature> preferences = new ArrayList<>();

    try {
        PreparedStatement pst = connection.prepareStatement(requete);
        pst.setInt(1, idUtilisateur); // ← était idCandidature, maintenant idUtilisateur
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            PreferenceCandidature preference = new PreferenceCandidature();
            preference.setIdPreference(rs.getInt("id_preference"));
            preference.setTypePosteSouhaite(rs.getString("type_poste_souhaite"));
            preference.setModeTravail(rs.getString("mode_travail"));
            preference.setDisponibilite(rs.getString("disponibilite"));
            preference.setMobiliteGeographique(rs.getString("mobilite_geographique"));
            preference.setPretDeplacement(rs.getString("pret_deplacement"));
            preference.setTypeContratSouhaite(rs.getString("type_contrat_souhaite"));
            preference.setPretentionSalariale(rs.getDouble("pretention_salariale"));
            preference.setDateDisponibilite(rs.getDate("date_disponibilite"));
            preference.setIdUtilisateur(rs.getInt("id_utilisateur"));
            preferences.add(preference);
        }
    } catch (SQLException e) {
        System.out.println("Erreur : " + e.getMessage());
    }
    return preferences;
} 
    //v1 get bi id candidature 
    /*public List<PreferenceCandidature> getByCandidature(int idCandidature) {
        String requete = "SELECT * FROM preference_candidature WHERE id_utilisateur = ?";
        List<PreferenceCandidature> preferences = new ArrayList<>();

        try {
            PreparedStatement pst = connection.prepareStatement(requete);
            pst.setInt(1, idCandidature);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                PreferenceCandidature preference = new PreferenceCandidature();
                preference.setIdPreference(rs.getInt("id_preference"));
                preference.setTypePosteSouhaite(rs.getString("type_poste_souhaite"));
                preference.setModeTravail(rs.getString("mode_travail"));
                preference.setDisponibilite(rs.getString("disponibilite"));
                preference.setMobiliteGeographique(rs.getString("mobilite_geographique"));
                preference.setPretDeplacement(rs.getString("pret_deplacement"));
                preference.setTypeContratSouhaite(rs.getString("type_contrat_souhaite"));
                preference.setPretentionSalariale(rs.getDouble("pretention_salariale"));
                preference.setDateDisponibilite(rs.getDate("date_disponibilite"));
                preference.setIdUtilisateur(rs.getInt("id_utilisateur"));

                preferences.add(preference);
            }

        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }

        return preferences;
    }*/



    /**
     * Méthode supplémentaire : Récupérer la préférence par candidature
     * @param idCandidature ID de la candidature
     * @return Préférence associée à la candidature
     */
/*
    public PreferenceCandidature getPreferenceByCandidature(int idCandidature) {
        String requete = "SELECT * FROM preference_candidature WHERE id_utilisateur = ?";
        PreferenceCandidature preference = null;

        try {
            PreparedStatement pst = connection.prepareStatement(requete);
            pst.setInt(1, idCandidature);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                preference = new PreferenceCandidature();
                preference.setIdPreference(rs.getInt("id_preference"));
                preference.setTypePosteSouhaite(rs.getString("type_poste_souhaite"));
                preference.setModeTravail(rs.getString("mode_travail"));
                preference.setDisponibilite(rs.getString("disponibilite"));
                preference.setMobiliteGeographique(rs.getString("mobilite_geographique"));
                preference.setPretDeplacement(rs.getString("pret_deplacement"));
                preference.setTypeContratSouhaite(rs.getString("type_contrat_souhaite"));
                preference.setPretentionSalariale(rs.getDouble("pretention_salariale"));
                preference.setDateDisponibilite(rs.getDate("date_disponibilite"));
                preference.setIdCandidature(rs.getInt("id_utilisateur"));
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur : " + e.getMessage());
        }

        return preference;
    }
*/
}
