package entities;
import java.util.Date;

public class PreferenceCandidature {
    private int idPreference;
    private String typePosteSouhaite;
    private String modeTravail;
    private String disponibilite;
    private String mobiliteGeographique;
    private String pretDeplacement;
    private String typeContratSouhaite;
    private double pretentionSalariale;
    private Date dateDisponibilite;
    private int idUtilisateur ;

    public PreferenceCandidature() {
    }

    public PreferenceCandidature(String typePosteSouhaite, String modeTravail, String disponibilite, String mobiliteGeographique, String pretDeplacement, String typeContratSouhaite, double pretentionSalariale, Date dateDisponibilite, int idUtilisateur) {
        this.typePosteSouhaite = typePosteSouhaite;
        this.modeTravail = modeTravail;
        this.disponibilite = disponibilite;
        this.mobiliteGeographique = mobiliteGeographique;
        this.pretDeplacement = pretDeplacement;
        this.typeContratSouhaite = typeContratSouhaite;
        this.pretentionSalariale = pretentionSalariale;
        this.dateDisponibilite = dateDisponibilite;
        this.idUtilisateur = idUtilisateur;
    }

    public int getIdPreference() {
        return idPreference;
    }

    public void setIdPreference(int idPreference) {
        this.idPreference = idPreference;
    }

    public String getTypePosteSouhaite() {
        return typePosteSouhaite;
    }

    public void setTypePosteSouhaite(String typePosteSouhaite) {
        this.typePosteSouhaite = typePosteSouhaite;
    }

    public String getModeTravail() {
        return modeTravail;
    }

    public void setModeTravail(String modeTravail) {
        this.modeTravail = modeTravail;
    }

    public String getDisponibilite() {
        return disponibilite;
    }

    public void setDisponibilite(String disponibilite) {
        this.disponibilite = disponibilite;
    }

    public String getMobiliteGeographique() {
        return mobiliteGeographique;
    }

    public void setMobiliteGeographique(String mobiliteGeographique) {
        this.mobiliteGeographique = mobiliteGeographique;
    }

    public String getPretDeplacement() {
        return pretDeplacement;
    }

    public void setPretDeplacement(String pretDeplacement) {
        this.pretDeplacement = pretDeplacement;
    }

    public String getTypeContratSouhaite() {
        return typeContratSouhaite;
    }

    public void setTypeContratSouhaite(String typeContratSouhaite) {
        this.typeContratSouhaite = typeContratSouhaite;
    }

    public double getPretentionSalariale() {
        return pretentionSalariale;
    }

    public void setPretentionSalariale(double pretentionSalariale) {
        this.pretentionSalariale = pretentionSalariale;
    }

    public Date getDateDisponibilite() {
        return dateDisponibilite;
    }

    public void setDateDisponibilite(Date dateDisponibilite) {
        this.dateDisponibilite = dateDisponibilite;
    }

    public int getidUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    @Override
    public String toString() {
        return "PreferenceCandidature{" +
                "idPreference=" + idPreference +
                ", typePosteSouhaite='" + typePosteSouhaite + '\'' +
                ", modeTravail='" + modeTravail + '\'' +
                ", disponibilite='" + disponibilite + '\'' +
                ", mobiliteGeographique='" + mobiliteGeographique + '\'' +
                ", pretDeplacement='" + pretDeplacement + '\'' +
                ", typeContratSouhaite='" + typeContratSouhaite + '\'' +
                ", pretentionSalariale=" + pretentionSalariale +
                ", dateDisponibilite=" + dateDisponibilite +
                ", idUtilisateur=" + idUtilisateur +
                '}';
    }
}
