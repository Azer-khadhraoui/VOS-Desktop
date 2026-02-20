package services;

import entities.Recrutement;
import entities.RecrutementGroup;
import utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceRecrutement {

    Connection conn;

    public ServiceRecrutement() {
        conn = MyDB.getInstance().getConn();
    }

    // ✅ GET AVAILABLE ENTRETIEN IDs
    public List<Integer> getAvailableEntretienIds() throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String req = "SELECT DISTINCT id_entretien FROM entretien ORDER BY id_entretien";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                ids.add(rs.getInt("id_entretien"));
            }
        }
        return ids;
    }

    // ✅ GET AVAILABLE UTILISATEUR IDs
    public List<Integer> getAvailableUtilisateurIds() throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String req = "SELECT DISTINCT id_utilisateur FROM utilisateur ORDER BY id_utilisateur";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                ids.add(rs.getInt("id_utilisateur"));
            }
        }
        return ids;
    }

    // ✅ CREATE
    public void ajouter(Recrutement r) throws SQLException {

        String req = "INSERT INTO recrutement(date_decision, decision_finale, id_entretien, id_utilisateur) VALUES (?,?,?,?)";

        PreparedStatement ps = conn.prepareStatement(req);

        ps.setDate(1, r.getDate_decision());
        ps.setString(2, r.getDecision_finale());
        ps.setInt(3, r.getId_entretien());
        ps.setInt(4, r.getId_utilisateur());

        ps.executeUpdate();

        System.out.println("✅ Recrutement ajouté !");
    }

    // ✅ UPDATE
    public void modifier(Recrutement r) throws SQLException {

        String req = "UPDATE recrutement SET date_decision=?, decision_finale=?, id_entretien=?, id_utilisateur=? WHERE id_recrutement=?";

        PreparedStatement ps = conn.prepareStatement(req);

        ps.setDate(1, r.getDate_decision());
        ps.setString(2, r.getDecision_finale());
        ps.setInt(3, r.getId_entretien());
        ps.setInt(4, r.getId_utilisateur());
        ps.setInt(5, r.getId_recrutement());

        ps.executeUpdate();

        System.out.println("✅ Recrutement modifié !");
    }

    // ✅ DELETE
    public void supprimer(int id) throws SQLException {

        String req = "DELETE FROM recrutement WHERE id_recrutement=?";

        PreparedStatement ps = conn.prepareStatement(req);
        ps.setInt(1, id);

        ps.executeUpdate();

        System.out.println("✅ Recrutement supprimé !");
    }

    // ✅ READ ALL
    public List<Recrutement> afficher() throws SQLException {

        List<Recrutement> list = new ArrayList<>();

        String req = "SELECT * FROM recrutement";

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {

            Recrutement r = new Recrutement(
                    rs.getInt("id_recrutement"),
                    rs.getDate("date_decision"),
                    rs.getString("decision_finale"),
                    rs.getInt("id_entretien"),
                    rs.getInt("id_utilisateur")
            );

            list.add(r);
        }

        return list;
    }

    // ✅ GET USER NAME BY ID
    public String getUserNameById(int userId) throws SQLException {
        String req = "SELECT nom FROM utilisateur WHERE id_utilisateur = ?";
        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nom");
                }
            }
        }
        return "Utilisateur #" + userId;
    }

    // ✅ GET ALL RECRUITMENTS GROUPED BY USER
    public List<RecrutementGroup> afficherGroupedByUser() throws SQLException {
        // First, get all recruitments
        List<Recrutement> allRecrutements = afficher();
        
        // Group by user ID
        Map<Integer, List<Recrutement>> groupedMap = new HashMap<>();
        for (Recrutement r : allRecrutements) {
            groupedMap.computeIfAbsent(r.getId_utilisateur(), k -> new ArrayList<>()).add(r);
        }
        
        // Create RecrutementGroup objects
        List<RecrutementGroup> groups = new ArrayList<>();
        for (Map.Entry<Integer, List<Recrutement>> entry : groupedMap.entrySet()) {
            int userId = entry.getKey();
            String userName = getUserNameById(userId);
            RecrutementGroup group = new RecrutementGroup(userId, userName, entry.getValue());
            groups.add(group);
        }
        
        return groups;
    }
}