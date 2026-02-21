package vos.gestionCandidat.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import vos.gestionCandidat.utils.MyDataBase;

/**
 * Service simple pour récupérer les infos utilisateur par ID
 */
public class UtilisateurService {
    private Connection connection;

    public UtilisateurService() {
        connection = MyDataBase.getInstance().getConnection();
    }

    /**
     * Récupère les informations d'un utilisateur par son ID
     * @param idUtilisateur L'ID de l'utilisateur
     * @return Un tableau [nom, prenom, email] ou null si non trouvé
     */
    public UtilisateurInfo getUtilisateurInfo(int idUtilisateur) {
        String requete = "SELECT nom, prenom, email FROM utilisateur WHERE id_utilisateur = ?";
        
        try {
            PreparedStatement pst = connection.prepareStatement(requete);
            pst.setInt(1, idUtilisateur);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                String email = rs.getString("email");
                
                return new UtilisateurInfo(nom, prenom, email);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération utilisateur : " + e.getMessage());
        }

        return null;
    }

    /**
     * Classe interne pour stocker les infos utilisateur
     */
    public static class UtilisateurInfo {
        public String nom;
        public String prenom;
        public String email;

        public UtilisateurInfo(String nom, String prenom, String email) {
            this.nom = nom != null ? nom : "Non disponible";
            this.prenom = prenom != null ? prenom : "Non disponible";
            this.email = email != null ? email : "Non disponible";
        }
    }
}