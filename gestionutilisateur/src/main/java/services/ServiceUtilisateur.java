package services;

import utils.MyConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import entities.Utilisateur;
import java.sql.PreparedStatement;




public class ServiceUtilisateur {

    Connection cnx;

    public ServiceUtilisateur() {
        cnx = MyConnection.getInstance().getCnx();
    }

    public void ajouterUtilisateurStatique() {

        try {
            Statement st = cnx.createStatement();

            String req =
                    "INSERT INTO utilisateur(image_profil,email,mot_de_passe,role,nom,prenom) VALUES(" +
                            "'profil.png'," +
                            "'azer10mo@gmail.com'," +
                            "'1234'," +
                            "'CLIENT'," +   // ✅ ICI MODIFICATION
                            "'Khadhraoui'," +
                            "'Azer'" +
                            ")";

            st.executeUpdate(req);

            System.out.println("✅ Utilisateur ajouté avec succès !");

        } catch (SQLException e) {
            System.out.println("❌ Erreur ajout utilisateur !");
            e.printStackTrace();
        }
    }
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
    public void supprimer(int id) {

        try {
            String req = "DELETE FROM utilisateur WHERE id_utilisateur=" + id;

            Statement st = cnx.createStatement();
            st.executeUpdate(req);

            System.out.println("✅ Utilisateur supprimé avec succès !");

        } catch (SQLException e) {
            System.out.println("❌ Erreur suppression !");
            e.printStackTrace();
        }
    }
    public void modifierPrenom(int id, String nouveauPrenom) {

        try {
            String req = "UPDATE utilisateur SET prenom = ? WHERE id_utilisateur = ?";

            PreparedStatement pst = cnx.prepareStatement(req);

            pst.setString(1, nouveauPrenom);
            pst.setInt(2, id);

            pst.executeUpdate();

            System.out.println("✅ Prénom modifié avec succès !");

        } catch (SQLException e) {
            System.out.println("❌ Erreur modification !");
            e.printStackTrace();
        }
    }



}
