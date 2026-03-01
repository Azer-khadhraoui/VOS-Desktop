package services;

import entities.Contrat;
import utilis.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceContrat {

    Connection conn;

    public ServiceContrat() {
        conn = MyConnection.getInstance().getCnx();
    }

    // ✅ CREATE : Ajouter un contrat
    public void ajouter(Contrat c) throws SQLException {

        String req = "INSERT INTO contrat_embauche(type_contrat, date_debut, date_fin, salaire, status, volume_horaire, avantages, id_recrutement) VALUES (?,?,?,?,?,?,?,?)";

        PreparedStatement ps = conn.prepareStatement(req);

        ps.setString(1, c.getType_contrat());
        ps.setDate(2, c.getDate_debut());
        ps.setDate(3, c.getDate_fin());
        ps.setDouble(4, c.getSalaire());
        ps.setString(5, c.getStatus());
        ps.setString(6, c.getVolume_horaire());
        ps.setString(7, c.getAvantages());
        ps.setInt(8, c.getId_recrutement());

        ps.executeUpdate();

        System.out.println("✅ Contrat ajouté avec succès !");
    }

    // ✅ UPDATE : Modifier un contrat
    public void modifier(Contrat c) throws SQLException {

        String req = "UPDATE contrat_embauche SET type_contrat=?, date_debut=?, date_fin=?, salaire=?, status=?, volume_horaire=?, avantages=?, id_recrutement=? WHERE id_contrat=?";

        PreparedStatement ps = conn.prepareStatement(req);

        ps.setString(1, c.getType_contrat());
        ps.setDate(2, c.getDate_debut());
        ps.setDate(3, c.getDate_fin());
        ps.setDouble(4, c.getSalaire());
        ps.setString(5, c.getStatus());
        ps.setString(6, c.getVolume_horaire());
        ps.setString(7, c.getAvantages());
        ps.setInt(8, c.getId_recrutement());
        ps.setInt(9, c.getId_contrat());

        ps.executeUpdate();

        System.out.println("✅ Contrat modifié avec succès !");
    }

    // ✅ DELETE : Supprimer un contrat
    public void supprimer(int id) throws SQLException {

        String req = "DELETE FROM contrat_embauche WHERE id_contrat=?";

        PreparedStatement ps = conn.prepareStatement(req);
        ps.setInt(1, id);

        ps.executeUpdate();

        System.out.println("✅ Contrat supprimé avec succès !");
    }

    public List<Contrat> afficher() throws SQLException {
        List<Contrat> list = new ArrayList<>();
        String req = "SELECT c.*, CONCAT(u.nom, ' ', u.prenom) as user_name FROM contrat_embauche c " +
                "JOIN recrutement r ON c.id_recrutement = r.id_recrutement " +
                "JOIN utilisateur u ON r.id_utilisateur = u.id_utilisateur";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            Contrat c = new Contrat(
                    rs.getInt("id_contrat"),
                    rs.getString("type_contrat"),
                    rs.getDate("date_debut"),
                    rs.getDate("date_fin"),
                    rs.getDouble("salaire"),
                    rs.getString("status"),
                    rs.getString("volume_horaire"),
                    rs.getString("avantages"),
                    rs.getInt("id_recrutement"),
                    rs.getString("user_name"));
            list.add(c);
        }
        return list;
    }

    public Contrat getById(int id) throws SQLException {
        String req = "SELECT c.*, CONCAT(u.nom, ' ', u.prenom) as user_name FROM contrat_embauche c " +
                "JOIN recrutement r ON c.id_recrutement = r.id_recrutement " +
                "JOIN utilisateur u ON r.id_utilisateur = u.id_utilisateur " +
                "WHERE c.id_contrat=?";
        PreparedStatement ps = conn.prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new Contrat(
                    rs.getInt("id_contrat"),
                    rs.getString("type_contrat"),
                    rs.getDate("date_debut"),
                    rs.getDate("date_fin"),
                    rs.getDouble("salaire"),
                    rs.getString("status"),
                    rs.getString("volume_horaire"),
                    rs.getString("avantages"),
                    rs.getInt("id_recrutement"),
                    rs.getString("user_name"));
        }
        return null;
    }

    public Contrat getByRecrutementId(int recId) throws SQLException {
        String req = "SELECT c.*, CONCAT(u.nom, ' ', u.prenom) as user_name FROM contrat_embauche c " +
                "JOIN recrutement r ON c.id_recrutement = r.id_recrutement " +
                "JOIN utilisateur u ON r.id_utilisateur = u.id_utilisateur " +
                "WHERE c.id_recrutement=?";
        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setInt(1, recId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Contrat(
                            rs.getInt("id_contrat"),
                            rs.getString("type_contrat"),
                            rs.getDate("date_debut"),
                            rs.getDate("date_fin"),
                            rs.getDouble("salaire"),
                            rs.getString("status"),
                            rs.getString("volume_horaire"),
                            rs.getString("avantages"),
                            rs.getInt("id_recrutement"),
                            rs.getString("user_name"));
                }
            }
        }
        return null;
    }
}
