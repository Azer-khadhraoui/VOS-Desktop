package services;

import entities.Utilisateur;
import utils.MyConnection;

import java.sql.*;

public class UtilisateurService {

    private Connection connection;

    public UtilisateurService() {
        connection = MyConnection.getInstance();
    }

    public Utilisateur getUtilisateurById(int id) {
        String sql = "SELECT * FROM utilisateur WHERE id_utilisateur = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new Utilisateur(
                        rs.getInt("id_utilisateur"),
                        rs.getString("email"),
                        rs.getString("mot_de_passe"),
                        rs.getString("role"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("image_profil")
                );
            }
        } catch (SQLException e) {
            System.err.println("Erreur getUtilisateurById: " + e.getMessage());
        }
        return null;
    }

    // Récupérer l'utilisateur (candidat) lié à une candidature
    public Utilisateur getCandidatByCandidature(int idCandidature) {
        String sql = "SELECT u.* FROM utilisateur u "
                + "JOIN candidature c ON c.id_utilisateur = u.id_utilisateur "
                + "WHERE c.id_candidature = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, idCandidature);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new Utilisateur(
                        rs.getInt("id_utilisateur"),
                        rs.getString("email"),
                        rs.getString("mot_de_passe"),
                        rs.getString("role"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("image_profil")
                );
            }
        } catch (SQLException e) {
            System.err.println("Erreur getCandidatByCandidature: " + e.getMessage());
        }
        return null;
    }
}