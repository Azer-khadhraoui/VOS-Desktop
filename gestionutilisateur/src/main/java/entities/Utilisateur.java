package entities;

public class Utilisateur {

    private int id_utilisateur;
    private String image_profil;
    private String email;
    private String mot_de_passe;
    private String role;
    private String nom;
    private String prenom;
    private String signature_url;

    public Utilisateur(int id_utilisateur, String image_profil, String email,
            String mot_de_passe, String role, String nom, String prenom) {
        this(id_utilisateur, image_profil, email, mot_de_passe, role, nom, prenom, null);
    }

    public Utilisateur(int id_utilisateur, String image_profil, String email,
            String mot_de_passe, String role, String nom, String prenom, String signature_url) {
        this.id_utilisateur = id_utilisateur;
        this.image_profil = image_profil;
        this.email = email;
        this.mot_de_passe = mot_de_passe;
        this.role = role;
        this.nom = nom;
        this.prenom = prenom;
        this.signature_url = signature_url;
    }

    // ✅ Getters

    public int getId_utilisateur() {
        return id_utilisateur;
    }

    public String getImage_profil() {
        return image_profil;
    }

    public String getEmail() {
        return email;
    }

    public String getMot_de_passe() {
        return mot_de_passe;
    }

    public String getRole() {
        return role;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getSignature_url() {
        return signature_url;
    }

    public void setSignature_url(String signature_url) {
        this.signature_url = signature_url;
    }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + id_utilisateur +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                '}';
    }
}
