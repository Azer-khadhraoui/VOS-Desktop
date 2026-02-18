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
        System.out.println("=== AJOUT UTILISATEUR ===");
        System.out.println("Nom: " + u.getNom());
        System.out.println("Prénom: " + u.getPrenom());
        System.out.println("Email: " + u.getEmail());
        System.out.println("Role: CLIENT");
        System.out.println("Image_profil à enregistrer: [" + u.getImage_profil() + "]");

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

            int rowsAffected = pst.executeUpdate();

            System.out.println("✅ Utilisateur ajouté ! (" + rowsAffected + " ligne(s) affectée(s))");

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
            String req = "UPDATE utilisateur SET image_profil=?, email=?, mot_de_passe=?, role=?, nom=?, prenom=? WHERE id_utilisateur=?";

            PreparedStatement pst = cnx.prepareStatement(req);

            pst.setString(1, u.getImage_profil());
            pst.setString(2, u.getEmail());
            pst.setString(3, u.getMot_de_passe());
            pst.setString(4, u.getRole());
            pst.setString(5, u.getNom());
            pst.setString(6, u.getPrenom());
            pst.setInt(7, u.getId_utilisateur());

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
        System.out.println("=== RÉCUPÉRATION UTILISATEUR PAR EMAIL ===");
        System.out.println("Email recherché: " + email);
        
        try {
            String req = "SELECT * FROM utilisateur WHERE email=?";
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                int id = rs.getInt("id_utilisateur");
                String imageProfil = rs.getString("image_profil");
                String emailDB = rs.getString("email");
                String motDePasse = rs.getString("mot_de_passe");
                String role = rs.getString("role");
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                
                System.out.println("✓ Utilisateur trouvé en DB:");
                System.out.println("  - ID: " + id);
                System.out.println("  - Nom: " + nom);
                System.out.println("  - Prénom: " + prenom);
                System.out.println("  - Email: " + emailDB);
                System.out.println("  - Role: " + role);
                System.out.println("  - Image_profil (brut DB): [" + imageProfil + "]");
                System.out.println("  - Image_profil NULL?: " + (imageProfil == null));
                System.out.println("  - Image_profil vide?: " + (imageProfil != null && imageProfil.trim().isEmpty()));
                
                Utilisateur user = new Utilisateur(id, imageProfil, emailDB, motDePasse, role, nom, prenom);
                return user;
            } else {
                System.out.println("✗ Aucun utilisateur trouvé avec cet email");
            }

        } catch (SQLException e) {
            System.err.println("✗ ERREUR SQL lors de la récupération de l'utilisateur:");
            e.printStackTrace();
        }
        return null;
    }
    
    // ============================
    // ✅ METTRE À JOUR LE MOT DE PASSE
    // ============================
    public boolean updatePassword(String email, String newPassword) {
        System.out.println("=== MISE À JOUR MOT DE PASSE ===");
        System.out.println("Email: " + email);
        
        try {
            String req = "UPDATE utilisateur SET mot_de_passe = ? WHERE email = ?";
            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setString(1, newPassword);
            pst.setString(2, email);
            
            int rowsAffected = pst.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Mot de passe mis à jour ! (" + rowsAffected + " ligne(s) affectée(s))");
                return true;
            } else {
                System.out.println("❌ Aucun utilisateur trouvé avec cet email");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur mise à jour mot de passe !");
            e.printStackTrace();
            return false;
        }
    }

}
