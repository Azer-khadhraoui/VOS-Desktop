package services;

import entities.Utilisateur;
import utilis.MyConnection;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.*;
import java.util.ArrayList;

public class ServiceUtilisateur {

    Connection cnx;
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Retourne vrai si la chaîne ressemble à un hash BCrypt ($2a$, $2b$, $2y$...)
     */
    private boolean looksLikeBCrypt(String s) {
        if (s == null)
            return false;
        return s.startsWith("$2a$") || s.startsWith("$2b$") || s.startsWith("$2y$") || s.startsWith("$2x$");
    }

    /**
     * Si la valeur fournie n'est pas déjà un hash BCrypt, la hache et retourne le
     * hash.
     * Utile pour accepter les anciens mots de passe en clair et ne pas
     * double-hasher.
     */
    private String ensureHashed(String maybeHash) {
        if (maybeHash == null)
            return null;
        if (looksLikeBCrypt(maybeHash))
            return maybeHash;
        return passwordEncoder.encode(maybeHash);
    }

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
            String req = "INSERT INTO utilisateur(image_profil,email,mot_de_passe,role,nom,prenom,signature_url) " +
                    "VALUES(?,?,?,?,?,?,?)";

            PreparedStatement pst = cnx.prepareStatement(req);

            pst.setString(1, u.getImage_profil());
            pst.setString(2, u.getEmail());
            pst.setString(3, ensureHashed(u.getMot_de_passe()));
            pst.setString(4, "CLIENT"); // rôle par défaut
            pst.setString(5, u.getNom());
            pst.setString(6, u.getPrenom());
            pst.setString(7, u.getSignature_url());

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
                        rs.getString("prenom"),
                        rs.getString("signature_url"));
                list.add(u);
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur READ !");
            e.printStackTrace();
        }
        return list;
    }

    public Utilisateur getById(int id) {
        String sql = "SELECT * FROM utilisateur WHERE id_utilisateur = ?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new Utilisateur(
                        rs.getInt("id_utilisateur"),
                        rs.getString("image_profil"),
                        rs.getString("email"),
                        rs.getString("mot_de_passe"),
                        rs.getString("role"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("signature_url"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user by ID: " + e.getMessage());
        }
        return null;
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
            String req = "UPDATE utilisateur SET image_profil=?, email=?, mot_de_passe=?, role=?, nom=?, prenom=?, signature_url=? WHERE id_utilisateur=?";

            PreparedStatement pst = cnx.prepareStatement(req);

            pst.setString(1, u.getImage_profil());
            pst.setString(2, u.getEmail());
            pst.setString(3, ensureHashed(u.getMot_de_passe()));
            pst.setString(4, u.getRole());
            pst.setString(5, u.getNom());
            pst.setString(6, u.getPrenom());
            pst.setString(7, u.getSignature_url());
            pst.setInt(8, u.getId_utilisateur());

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
            // Récupérer l'utilisateur par son email
            String req = "SELECT mot_de_passe FROM utilisateur WHERE email=?";
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Récupérer le mot de passe hashé de la BD
                String hashedPassword = rs.getString("mot_de_passe");
                // Si le mot de passe en BD ressemble à BCrypt -> vérification normale
                if (looksLikeBCrypt(hashedPassword)) {
                    return passwordEncoder.matches(password, hashedPassword);
                }

                // Ancien mot de passe en clair stocké en DB : comparer directement
                if (password != null && password.equals(hashedPassword)) {
                    // Migrer : hacher le mot de passe et mettre à jour la DB
                    String newHash = passwordEncoder.encode(password);
                    updatePassword(email, newHash);
                    return true;
                }

                return false;
            }

            return false;

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
                String sigUrl = rs.getString("signature_url");

                System.out.println("✓ Utilisateur trouvé en DB:");
                System.out.println("  - ID: " + id);
                System.out.println("  - Nom: " + nom);
                System.out.println("  - Prénom: " + prenom);
                System.out.println("  - Email: " + emailDB);
                System.out.println("  - Role: " + role);
                System.out.println("  - Image_profil (brut DB): [" + imageProfil + "]");
                System.out.println("  - Signature URL: [" + sigUrl + "]");

                Utilisateur user = new Utilisateur(id, imageProfil, emailDB, motDePasse, role, nom, prenom, sigUrl);
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
            pst.setString(1, ensureHashed(newPassword));
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
