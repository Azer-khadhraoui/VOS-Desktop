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

    public void insertCritere(CritereOffre c) {

        String sql = "INSERT INTO critere_offre (niveau_experience, niveau_etude, competences_requises, id_offre) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, c.getNiveauExperience());
            ps.setString(2, c.getNiveauEtude());
            ps.setString(3, c.getCompetencesRequises());
            ps.setInt(4, c.getIdOffre());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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
                c.setIdOffre(rs.getInt("id_offre"));

                list.add(c);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
    public void deleteCritere(int id) {
        String sql = "DELETE FROM critere_offre WHERE id_critere=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateCritere(int id, CritereOffre c) {
        String sql = "UPDATE critere_offre SET niveau_experience=?, niveau_etude=?, competences_requises=? WHERE id_critere=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, c.getNiveauExperience());
            ps.setString(2, c.getNiveauEtude());
            ps.setString(3, c.getCompetencesRequises());
            ps.setInt(4, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
