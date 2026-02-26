package services;

import entities.Entretien;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EntretienService {
    private Connection connection;

    public EntretienService() {
        connection = MyConnection.getInstance();
    }

    // CREATE
    public void addEntretien(Entretien entretien) {
        String sql = "INSERT INTO entretien (date_entretien, heure_entretien, type_entretien, " +
                "statut_entretien, lieu, type_test, id_candidature, id_utilisateur) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setDate(1, entretien.getDateEntretien());
            pst.setTime(2, entretien.getHeureEntretien());
            pst.setString(3, entretien.getTypeEntretien());
            pst.setString(4, entretien.getStatutEntretien());
            pst.setString(5, entretien.getLieu());
            pst.setString(6, entretien.getTypeTest());
            pst.setInt(7, entretien.getIdCandidature());
            pst.setInt(8, entretien.getIdUtilisateur());

            pst.executeUpdate();
            System.out.println("Entretien added successfully!");
        } catch (SQLException e) {
            System.err.println("Error adding entretien: " + e.getMessage());
        }
    }

    // READ ALL
    public List<Entretien> getAllEntretiens() {
        List<Entretien> entretiens = new ArrayList<>();
        String sql = "SELECT * FROM entretien";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Entretien e = new Entretien(
                        rs.getInt("id_entretien"),
                        rs.getDate("date_entretien"),
                        rs.getTime("heure_entretien"),
                        rs.getString("type_entretien"),
                        rs.getString("statut_entretien"),
                        rs.getString("lieu"),
                        rs.getString("type_test"),
                        rs.getInt("id_candidature"),
                        rs.getInt("id_utilisateur")
                );
                entretiens.add(e);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching entretiens: " + e.getMessage());
        }

        return entretiens;
    }

    // READ BY ID
    public Entretien getEntretienById(int id) {
        String sql = "SELECT * FROM entretien WHERE id_entretien = ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return new Entretien(
                        rs.getInt("id_entretien"),
                        rs.getDate("date_entretien"),
                        rs.getTime("heure_entretien"),
                        rs.getString("type_entretien"),
                        rs.getString("statut_entretien"),
                        rs.getString("lieu"),
                        rs.getString("type_test"),
                        rs.getInt("id_candidature"),
                        rs.getInt("id_utilisateur")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching entretien: " + e.getMessage());
        }

        return null;
    }

    // UPDATE
    public void updateEntretien(Entretien entretien) {
        String sql = "UPDATE entretien SET date_entretien = ?, heure_entretien = ?, " +
                "type_entretien = ?, statut_entretien = ?, lieu = ?, type_test = ?, " +
                "id_candidature = ?, id_utilisateur = ? WHERE id_entretien = ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setDate(1, entretien.getDateEntretien());
            pst.setTime(2, entretien.getHeureEntretien());
            pst.setString(3, entretien.getTypeEntretien());
            pst.setString(4, entretien.getStatutEntretien());
            pst.setString(5, entretien.getLieu());
            pst.setString(6, entretien.getTypeTest());
            pst.setInt(7, entretien.getIdCandidature());
            pst.setInt(8, entretien.getIdUtilisateur());
            pst.setInt(9, entretien.getIdEntretien());

            pst.executeUpdate();
            System.out.println("Entretien updated successfully!");
        } catch (SQLException e) {
            System.err.println("Error updating entretien: " + e.getMessage());
        }
    }

    // DELETE
    public void deleteEntretien(int id) {
        String sql = "DELETE FROM entretien WHERE id_entretien = ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Entretien deleted successfully!");
        } catch (SQLException e) {
            System.err.println("Error deleting entretien: " + e.getMessage());
        }
    }
}