package services;

import entities.Contrat;
import utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceContrat {

    Connection conn;

    public ServiceContrat() {
        conn = MyDB.getInstance().getConn();
    }

    // ✅ CREATE : Ajouter un contrat
    public void ajouter(Contrat c) throws SQLException {

        String req = "INSERT INTO contrat_embauche(type_contrat, date_debut, salaire, id_recrutement) VALUES (?,?,?,?)";

        PreparedStatement ps = conn.prepareStatement(req);

        ps.setString(1, c.getType_contrat());
        ps.setDate(2, c.getDate_debut());
        ps.setDouble(3, c.getSalaire());
        ps.setInt(4, c.getId_recrutement());

        ps.executeUpdate();

        System.out.println("✅ Contrat ajouté avec succès !");
    }

    // ✅ UPDATE : Modifier un contrat
    public void modifier(Contrat c) throws SQLException {

        String req = "UPDATE contrat_embauche SET type_contrat=?, date_debut=?, salaire=?, id_recrutement=? WHERE id_contrat=?";

        PreparedStatement ps = conn.prepareStatement(req);

        ps.setString(1, c.getType_contrat());
        ps.setDate(2, c.getDate_debut());
        ps.setDouble(3, c.getSalaire());
        ps.setInt(4, c.getId_recrutement());
        ps.setInt(5, c.getId_contrat());

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

    // ✅ READ : Afficher tous les contrats
    public List<Contrat> afficher() throws SQLException {

        List<Contrat> list = new ArrayList<>();

        String req = "SELECT * FROM contrat_embauche";

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {

            Contrat c = new Contrat(
                    rs.getInt("id_contrat"),
                    rs.getString("type_contrat"),
                    rs.getDate("date_debut"),
                    rs.getDouble("salaire"),
                    rs.getInt("id_recrutement")
            );

            list.add(c);
        }

        return list;
    }

    // ✅ READ BY ID : Afficher un contrat par ID
    public Contrat getById(int id) throws SQLException {

        String req = "SELECT * FROM contrat_embauche WHERE id_contrat=?";

        PreparedStatement ps = conn.prepareStatement(req);
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return new Contrat(
                    rs.getInt("id_contrat"),
                    rs.getString("type_contrat"),
                    rs.getDate("date_debut"),
                    rs.getDouble("salaire"),
                    rs.getInt("id_recrutement")
            );
        }

        return null;
    }
}