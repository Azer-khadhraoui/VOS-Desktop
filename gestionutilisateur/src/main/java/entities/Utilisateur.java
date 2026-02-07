package entities;

public class Utilisateur {

    private int id_utilisateur;
    private String image_profil;
    private String email;
    private String mot_de_passe;
    private String role;
    private String nom;
    private String prenom;

    public Utilisateur(int id, String image, String email, String mdp, String role, String nom, String prenom) {
        this.id_utilisateur = id;
        this.image_profil = image;
        this.email = email;
        this.mot_de_passe = mdp;
        this.role = role;
        this.nom = nom;
        this.prenom = prenom;
    }

    // Getters
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
    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + id_utilisateur +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", image='" + image_profil + '\'' +
                '}';
    }

}
