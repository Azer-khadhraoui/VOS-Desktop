package vos.gestionCandidat.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import vos.gestionCandidat.utils.MyDataBase;

/**
 * Service pour récupérer les informations des offres d'emploi
 */
public class OffreEmploiService {
    private Connection connection;

    public OffreEmploiService() {
        connection = MyDataBase.getInstance().getConnection();
    }

    /**
     * Récupère le titre d'une offre par son ID
     * @param idOffre L'ID de l'offre
     * @return Le titre de l'offre ou "Non spécifié"
     */
    public String getTitreOffre(int idOffre) {
        String requete = "SELECT titre FROM offre_emploi WHERE id_offre = ?";
        
        try {
            PreparedStatement pst = connection.prepareStatement(requete);
            pst.setInt(1, idOffre);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getString("titre");
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération offre : " + e.getMessage());
        }

        return "Non spécifié";
    }
}