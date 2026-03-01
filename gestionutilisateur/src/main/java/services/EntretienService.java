package services;

import entities.Entretien;
import entities.Utilisateur;
import utilis.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EntretienService {

    private Connection connection;

    public EntretienService() {
        // ✅ CORRIGÉ : getCnx() au lieu de getInstance() directement
        connection = MyConnection.getInstance().getCnx();
    }

    // ============================================================
    // MÉTHODES PRIVÉES : remplace UtilisateurService
    // ============================================================

    private Utilisateur getCandidatByCandidature(int idCandidature) {
        String sql = "SELECT u.* FROM utilisateur u " +
                "JOIN candidature c ON c.id_utilisateur = u.id_utilisateur " +
                "WHERE c.id_candidature = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, idCandidature);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new Utilisateur(
                        rs.getInt("id_utilisateur"),
                        rs.getString("image_profil"),
                        rs.getString("email"),
                        rs.getString("mot_de_passe"),
                        rs.getString("role"),
                        rs.getString("nom"),
                        rs.getString("prenom"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching candidat: " + e.getMessage());
        }
        return null;
    }

    private Utilisateur getUtilisateurById(int idUtilisateur) {
        String sql = "SELECT * FROM utilisateur WHERE id_utilisateur = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, idUtilisateur);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new Utilisateur(
                        rs.getInt("id_utilisateur"),
                        rs.getString("image_profil"),
                        rs.getString("email"),
                        rs.getString("mot_de_passe"),
                        rs.getString("role"),
                        rs.getString("nom"),
                        rs.getString("prenom"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching utilisateur: " + e.getMessage());
        }
        return null;
    }

    // ============================================================
    // CREATE
    // ============================================================
    public void addEntretien(Entretien entretien) {
        String sql = "INSERT INTO entretien (date_entretien, heure_entretien, type_entretien, " +
                "statut_entretien, lieu, type_test, id_candidature, id_utilisateur, questions_entretien, lien_reunion) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pst = null;
        try {
            pst = connection.prepareStatement(sql);
            pst.setDate(1, entretien.getDateEntretien());
            pst.setTime(2, entretien.getHeureEntretien());
            pst.setString(3, entretien.getTypeEntretien());
            pst.setString(4, entretien.getStatutEntretien());
            pst.setString(5, entretien.getLieu());
            pst.setString(6, entretien.getTypeTest());
            pst.setInt(7, entretien.getIdCandidature());
            pst.setInt(8, entretien.getIdUtilisateur());
            pst.setString(9, entretien.getQuestionsEntretien());
            pst.setString(10, entretien.getLienReunion());
            pst.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage().contains("questions_entretien") || e.getMessage().contains("lien_reunion")) {
                try {
                    if (pst != null)
                        pst.close();
                    sql = "INSERT INTO entretien (date_entretien, heure_entretien, type_entretien, " +
                            "statut_entretien, lieu, type_test, id_candidature, id_utilisateur) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    pst = connection.prepareStatement(sql);
                    pst.setDate(1, entretien.getDateEntretien());
                    pst.setTime(2, entretien.getHeureEntretien());
                    pst.setString(3, entretien.getTypeEntretien());
                    pst.setString(4, entretien.getStatutEntretien());
                    pst.setString(5, entretien.getLieu());
                    pst.setString(6, entretien.getTypeTest());
                    pst.setInt(7, entretien.getIdCandidature());
                    pst.setInt(8, entretien.getIdUtilisateur());
                    pst.executeUpdate();
                } catch (SQLException e2) {
                    System.err.println("Error adding entretien: " + e2.getMessage());
                    return;
                }
            } else {
                System.err.println("Error adding entretien: " + e.getMessage());
                return;
            }
        } finally {
            try {
                if (pst != null)
                    pst.close();
            } catch (SQLException e) {
            }
        }

        System.out.println("Entretien added successfully!");

        // ✅ EMAIL
        if ("Planifié".equalsIgnoreCase(entretien.getStatutEntretien())
                || "Confirmé".equalsIgnoreCase(entretien.getStatutEntretien())) {
            Utilisateur candidat = getCandidatByCandidature(entretien.getIdCandidature());
            if (candidat != null) {
                EntretienEmailService.envoyerConvocationEntretien(entretien, candidat, "");
            }
        }
    }

    // ============================================================
    // READ ALL
    // ============================================================
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
                        rs.getInt("id_utilisateur"));
                try {
                    e.setQuestionsEntretien(rs.getString("questions_entretien"));
                } catch (SQLException ex) {
                }
                try {
                    e.setLienReunion(rs.getString("lien_reunion"));
                } catch (SQLException ex) {
                }
                entretiens.add(e);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching entretiens: " + e.getMessage());
        }
        return entretiens;
    }

    public List<Entretien> getEntretiensByCandidate(int userId) {
        List<Entretien> entretiens = new ArrayList<>();
        String sql = "SELECT e.* FROM entretien e " +
                "JOIN candidature c ON e.id_candidature = c.id_candidature " +
                "WHERE c.id_utilisateur = ? " +
                "AND e.id_entretien NOT IN (SELECT id_entretien FROM recrutement WHERE id_entretien IS NOT NULL)";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();

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
                        rs.getInt("id_utilisateur"));
                try {
                    e.setQuestionsEntretien(rs.getString("questions_entretien"));
                } catch (SQLException ex) {
                }
                try {
                    e.setLienReunion(rs.getString("lien_reunion"));
                } catch (SQLException ex) {
                }
                entretiens.add(e);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching filtered entretiens: " + e.getMessage());
        }
        return entretiens;
    }

    public List<Utilisateur> getUsersWithAvailableInterviews() {
        List<Utilisateur> users = new ArrayList<>();
        String sql = "SELECT DISTINCT u.* FROM utilisateur u " +
                "JOIN candidature c ON u.id_utilisateur = c.id_utilisateur " +
                "JOIN entretien e ON c.id_candidature = e.id_candidature " +
                "WHERE e.id_entretien NOT IN (SELECT id_entretien FROM recrutement WHERE id_entretien IS NOT NULL)";

        try (Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                users.add(new Utilisateur(
                        rs.getInt("id_utilisateur"),
                        rs.getString("image_profil"),
                        rs.getString("email"),
                        rs.getString("mot_de_passe"),
                        rs.getString("role"),
                        rs.getString("nom"),
                        rs.getString("prenom")));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching users with interviews: " + e.getMessage());
        }
        return users;
    }

    // ============================================================
    // READ BY ID
    // ============================================================
    public Entretien getEntretienById(int id) {
        String query = "SELECT * FROM entretien WHERE id_entretien = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Entretien e = new Entretien(
                        rs.getInt("id_entretien"),
                        rs.getDate("date_entretien"),
                        rs.getTime("heure_entretien"),
                        rs.getString("type_entretien"),
                        rs.getString("statut_entretien"),
                        rs.getString("lieu"),
                        rs.getString("type_test"),
                        rs.getInt("id_candidature"),
                        rs.getInt("id_utilisateur"));
                try {
                    e.setQuestionsEntretien(rs.getString("questions_entretien"));
                } catch (SQLException ex) {
                }
                try {
                    e.setLienReunion(rs.getString("lien_reunion"));
                } catch (SQLException ex) {
                }
                return e;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching entretien by ID: " + e.getMessage());
        }
        return null;
    }

    // ============================================================
    // UPDATE
    // ============================================================
    public void updateEntretien(Entretien entretien) {
        String sql = "UPDATE entretien SET date_entretien = ?, heure_entretien = ?, " +
                "type_entretien = ?, statut_entretien = ?, lieu = ?, type_test = ?, " +
                "id_candidature = ?, id_utilisateur = ?, questions_entretien = ?, lien_reunion = ? WHERE id_entretien = ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setDate(1, entretien.getDateEntretien());
            pst.setTime(2, entretien.getHeureEntretien());
            pst.setString(3, entretien.getTypeEntretien());
            pst.setString(4, entretien.getStatutEntretien());
            pst.setString(5, entretien.getLieu());
            pst.setString(6, entretien.getTypeTest());
            pst.setInt(7, entretien.getIdCandidature());
            pst.setInt(8, entretien.getIdUtilisateur());
            pst.setString(9, entretien.getQuestionsEntretien());
            pst.setString(10, entretien.getLienReunion());
            pst.setInt(11, entretien.getIdEntretien());
            pst.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage().contains("questions_entretien") || e.getMessage().contains("lien_reunion")) {
                sql = "UPDATE entretien SET date_entretien = ?, heure_entretien = ?, " +
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
                } catch (SQLException e2) {
                    System.err.println("Error updating entretien: " + e2.getMessage());
                    return;
                }
            } else {
                System.err.println("Error updating entretien: " + e.getMessage());
                return;
            }
        }

        System.out.println("Entretien updated successfully!");

        // ✅ EMAIL
        if ("Terminé".equalsIgnoreCase(entretien.getStatutEntretien())) {
            Utilisateur admin = getUtilisateurById(entretien.getIdUtilisateur());
            Utilisateur candidat = getCandidatByCandidature(entretien.getIdCandidature());
            if (admin != null && candidat != null) {
                EntretienEmailService.envoyerRappelEvaluationAdmin(entretien, admin, candidat);
            }
        } else if ("Confirmé".equalsIgnoreCase(entretien.getStatutEntretien())) {
            Utilisateur candidat = getCandidatByCandidature(entretien.getIdCandidature());
            if (candidat != null) {
                EntretienEmailService.envoyerConvocationEntretien(entretien, candidat, "");
            }
        }
    }

    // ============================================================
    // DELETE
    // ============================================================
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