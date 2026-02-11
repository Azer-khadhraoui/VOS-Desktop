package services;

import entities.Utilisateur;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;

public class ServiceUtilisateur {

    Connection cnx;

    public ServiceUtilisateur() {
        cnx = MyConnection.getInstance().getCnx();
    }

    // ============================
    // ✅ AJOUT DYNAMIQUE
    // ============================
    public void ajouter(Utilisateur u) {

        try {
            String req = "INSERT INTO utilisateur(image_profil,email,mot_de_passe,role,nom,prenom) " +
                    "VALUES(?,?,?,?,?,?)";

            PreparedStatement pst = cnx.prepareStatement(req);

            pst.setString(1, u.getImage_profil());
            pst.setString(2, u.getEmail());
            pst.setString(3, u.getMot_de_passe());
            pst.setString(4, "CLIENT"); // rôle par défaut
            pst.setString(5, u.getNom());
            pst.setString(6, u.getPrenom());

            pst.executeUpdate();

            System.out.println("✅ Utilisateur ajouté !");

        } catch (SQLException e) {
            System.out.println("❌ Erreur ajout !");
            e.printStackTrace();
        }
    }

    // ============================
    // ✅ AFFICHER ALL (READ)
    // ============================
    public ArrayList<Utilisateur> afficherAll() {

        ArrayList<Utilisateur> list = new ArrayList<>();

        try {
            String req = "SELECT * FROM utilisateur";

            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);

            while (rs.next()) {

                Utilisateur u = new Utilisateur(
                        rs.getInt("id_utilisateur"),
                        rs.getString("image_profil"),
                        rs.getString("email"),
                        rs.getString("mot_de_passe"),
                        rs.getString("role"),
                        rs.getString("nom"),
                        rs.getString("prenom")
                );

                list.add(u);
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur READ !");
            e.printStackTrace();
        }

        return list;
    }

    // ============================
    // ✅ SUPPRIMER (DELETE)
    // ============================
    public void supprimer(int id) {

        try {
            String req = "DELETE FROM utilisateur WHERE id_utilisateur = ?";

            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setInt(1, id);

            pst.executeUpdate();

            System.out.println("✅ Utilisateur supprimé !");

        } catch (SQLException e) {
            System.out.println("❌ Erreur suppression !");
            e.printStackTrace();
        }
    }

    // ============================
    // ✅ MODIFIER PRENOM (UPDATE)
    // ============================
    public void modifierPrenom(int id, String prenom) {

        try {
            String req = "UPDATE utilisateur SET prenom = ? WHERE id_utilisateur = ?";

            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setString(1, prenom);
            pst.setInt(2, id);

            pst.executeUpdate();

            System.out.println("✅ Prénom modifié !");

        } catch (SQLException e) {
            System.out.println("❌ Erreur modification !");
            e.printStackTrace();
        }
    }
    public void modifier(Utilisateur u) {

        try {
            String req = "UPDATE utilisateur SET email=?, mot_de_passe=?, role=?, nom=?, prenom=? WHERE id_utilisateur=?";

            PreparedStatement pst = cnx.prepareStatement(req);

            pst.setString(1, u.getEmail());
            pst.setString(2, u.getMot_de_passe());
            pst.setString(3, u.getRole());
            pst.setString(4, u.getNom());
            pst.setString(5, u.getPrenom());
            pst.setInt(6, u.getId_utilisateur());

            pst.executeUpdate();

            System.out.println("✅ Utilisateur modifié !");

        } catch (SQLException e) {
            System.out.println("❌ Erreur modification !");
            e.printStackTrace();
        }
    }
    public boolean emailExiste(String email) {

        try {
            String req = "SELECT * FROM utilisateur WHERE email = ?";

            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setString(1, email);

            ResultSet rs = pst.executeQuery();

            return rs.next(); // true si email trouvé

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean login(String email, String password) {
        try {
            String req = "SELECT * FROM utilisateur WHERE email=? AND mot_de_passe=?";
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Utilisateur getUserByEmail(String email) {
        try {
            String req = "SELECT * FROM utilisateur WHERE email=?";
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return new Utilisateur(
                    rs.getInt("id_utilisateur"),
                    rs.getString("image_profil"),
                    rs.getString("email"),
                    rs.getString("mot_de_passe"),
                    rs.getString("role"),
                    rs.getString("nom"),
                    rs.getString("prenom")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
