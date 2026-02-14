package org.example.services;

import org.example.entities.OffreEmploi;
import org.example.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.sql.ResultSet;

public class OffreEmploiService {

    private Connection connection;

    public OffreEmploiService() {
        connection = MyDataBase.getInstance().getConnection();
    }

    /**
     * Inserts a new job offer (OffreEmploi) into the database.
     * This method creates a new record in the offre_emploi table with all offer details.
     * 
     * @param offre The OffreEmploi object containing the job offer details to be inserted
     */
    public void insertOffre(OffreEmploi offre) {

        String sql = """
            INSERT INTO offre_emploi
            (titre, description, type_contrat, statut_offre, date_publication, id_utilisateur)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, offre.getTitre());
            ps.setString(2, offre.getDescription());
            ps.setString(3, offre.getTypeContrat());
            ps.setString(4, offre.getStatutOffre());
            ps.setDate(5, offre.getDatePublication());
            ps.setInt(6, offre.getIdUtilisateur());

            ps.executeUpdate();
            System.out.println("Offre inserted");
        } catch (SQLException e) {
            System.out.println("Insert error: " + e.getMessage());
        }
    }

    /**
     * Deletes a job offer from the database.
     * This method removes an offer record identified by its ID from the offre_emploi table.
     * 
     * @param idOffre The ID of the job offer to be deleted
     */
    public void deleteOffre(int idOffre) {

        String sql = "DELETE FROM offre_emploi WHERE id_offre=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idOffre);
            ps.executeUpdate();
            System.out.println("Offre deleted");
        } catch (SQLException e) {
            System.out.println("Delete error: " + e.getMessage());
        }
    }
    
    /**
     * Updates an existing job offer in the database.
     * This method modifies all fields of an offer record identified by its ID.
     * 
     * @param idOffre The ID of the job offer to be updated
     * @param offre The OffreEmploi object containing the new offer values
     */
    public void updateOffre(int idOffre, OffreEmploi offre) {

        String sql = """
        UPDATE offre_emploi
        SET titre = ?,
            description = ?,
            type_contrat = ?,
            statut_offre = ?,
            date_publication = ?,
            id_utilisateur = ?
        WHERE id_offre = ?
    """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, offre.getTitre());
            ps.setString(2, offre.getDescription());
            ps.setString(3, offre.getTypeContrat());
            ps.setString(4, offre.getStatutOffre());
            ps.setDate(5, offre.getDatePublication());
            ps.setInt(6, offre.getIdUtilisateur());
            ps.setInt(7, idOffre);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Offre updated");
            } else {
                System.out.println("No offer found with id " + idOffre);
            }

        } catch (SQLException e) {
            System.out.println("Update error: " + e.getMessage());
        }
    }
    
    /**
     * Retrieves all job offers from the database.
     * This method reads and returns all records from the offre_emploi table.
     * 
     * @return A List of all OffreEmploi objects in the database, or an empty list if none exist
     */
    public List<OffreEmploi> getAllOffres() {

        List<OffreEmploi> offres = new ArrayList<>();
        String sql = "SELECT * FROM offre_emploi";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                OffreEmploi offre = new OffreEmploi();
                offre.setIdOffre(rs.getInt("id_offre"));
                offre.setTitre(rs.getString("titre"));
                offre.setDescription(rs.getString("description"));
                offre.setTypeContrat(rs.getString("type_contrat"));
                offre.setStatutOffre(rs.getString("statut_offre"));
                offre.setDatePublication(rs.getDate("date_publication"));
                offre.setIdUtilisateur(rs.getInt("id_utilisateur"));

                offres.add(offre);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return offres;
    }


}
