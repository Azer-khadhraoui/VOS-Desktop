package entities;

public class Utilisateur {
    private int idUtilisateur;
    private String email;
    private String motDePasse;
    private String role;
    private String nom;
    private String prenom;
    private String imageProfil;

    public Utilisateur() {}

    public Utilisateur(int idUtilisateur, String email, String motDePasse,
                       String role, String nom, String prenom, String imageProfil) {
        this.idUtilisateur = idUtilisateur;
        this.email         = email;
        this.motDePasse    = motDePasse;
        this.role          = role;
        this.nom           = nom;
        this.prenom        = prenom;
        this.imageProfil   = imageProfil;
    }

    public int    getIdUtilisateur()               { return idUtilisateur; }
    public void   setIdUtilisateur(int id)         { this.idUtilisateur = id; }

    public String getEmail()                       { return email; }
    public void   setEmail(String email)           { this.email = email; }

    public String getMotDePasse()                  { return motDePasse; }
    public void   setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String getRole()                        { return role; }
    public void   setRole(String role)             { this.role = role; }

    public String getNom()                         { return nom; }
    public void   setNom(String nom)               { this.nom = nom; }

    public String getPrenom()                      { return prenom; }
    public void   setPrenom(String prenom)         { this.prenom = prenom; }

    public String getImageProfil()                 { return imageProfil; }
    public void   setImageProfil(String img)       { this.imageProfil = img; }

    @Override
    public String toString() {
        return prenom + " " + nom + " <" + email + ">";
    }
}