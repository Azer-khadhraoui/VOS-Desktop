package org.example.services;

import org.example.entities.CritereOffre;
import org.example.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CritereOffreService {

    private Connection connection;

    public CritereOffreService() {
        connection = MyDataBase.getInstance().getConnection();
    }

    /**
     * Inserts a new CritereOffre into the database.
     * This method creates a new record in the critere_offre table with the provided criteria.
     * 
     * @param c The CritereOffre object containing the criteria details to be inserted
     */
    public void insertCritere(CritereOffre c) {

        String sql = "INSERT INTO critere_offre (niveau_experience, niveau_etude, competences_requises, responsibilities, id_offre) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, c.getNiveauExperience());
            ps.setString(2, c.getNiveauEtude());
            ps.setString(3, c.getCompetencesRequises());
            ps.setString(4, c.getResponsibilities());
            ps.setInt(5, c.getIdOffre());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves all CritereOffre records associated with a specific job offer.
     * This method reads all criteria from the database that match the given offer ID.
     * 
     * @param idOffre The ID of the job offer to retrieve criteria for
     * @return A List of CritereOffre objects matching the offer ID, or an empty list if none found
     */
    public List<CritereOffre> getByOffreId(int idOffre) {

        List<CritereOffre> list = new ArrayList<>();
        String sql = "SELECT * FROM critere_offre WHERE id_offre=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, idOffre);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                CritereOffre c = new CritereOffre();
                c.setIdCritere(rs.getInt("id_critere"));
                c.setNiveauExperience(rs.getString("niveau_experience"));
                c.setNiveauEtude(rs.getString("niveau_etude"));
                c.setCompetencesRequises(rs.getString("competences_requises"));
                c.setResponsibilities(rs.getString("responsibilities"));
                c.setIdOffre(rs.getInt("id_offre"));

                list.add(c);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
    
    /**
     * Deletes a CritereOffre from the database.
     * This method removes a criteria record identified by its ID from the critere_offre table.
     * 
     * @param id The ID of the criteria to be deleted
     */
    public void deleteCritere(int id) {
        String sql = "DELETE FROM critere_offre WHERE id_critere=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates an existing CritereOffre in the database.
     * This method modifies the criteria information for a record identified by its ID.
     * 
     * @param id The ID of the criteria to be updated
     * @param c The CritereOffre object containing the new criteria values
     */
    public void updateCritere(int id, CritereOffre c) {
        String sql = "UPDATE critere_offre SET niveau_experience=?, niveau_etude=?, competences_requises=?, responsibilities=? WHERE id_critere=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, c.getNiveauExperience());
            ps.setString(2, c.getNiveauEtude());
            ps.setString(3, c.getCompetencesRequises());
            ps.setString(4, c.getResponsibilities());
            ps.setInt(5, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
