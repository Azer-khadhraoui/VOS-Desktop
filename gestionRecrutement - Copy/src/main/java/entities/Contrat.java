package entities;

import java.sql.Date;

public class Contrat {

    private int id_contrat;
    private String type_contrat;
    private Date date_debut;
    private Date date_fin;
    private double salaire;
    private String status;
    private String volume_horaire;
    private String avantages;
    private int id_recrutement;

    public Contrat() {}

    public Contrat(String type_contrat, Date date_debut, Date date_fin, double salaire, String status, String volume_horaire, String avantages, int id_recrutement) {
        this.type_contrat = type_contrat;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        this.salaire = salaire;
        this.status = status;
        this.volume_horaire = volume_horaire;
        this.avantages = avantages;
        this.id_recrutement = id_recrutement;
    }

    public Contrat(int id_contrat, String type_contrat, Date date_debut, Date date_fin, double salaire, String status, String volume_horaire, String avantages, int id_recrutement) {
        this.id_contrat = id_contrat;
        this.type_contrat = type_contrat;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        this.salaire = salaire;
        this.status = status;
        this.volume_horaire = volume_horaire;
        this.avantages = avantages;
        this.id_recrutement = id_recrutement;
    }

    // ✅ Getters & Setters

    public int getId_contrat() {
        return id_contrat;
    }

    public void setId_contrat(int id_contrat) {
        this.id_contrat = id_contrat;
    }

    public String getType_contrat() {
        return type_contrat;
    }

    public void setType_contrat(String type_contrat) {
        this.type_contrat = type_contrat;
    }

    public Date getDate_debut() {
        return date_debut;
    }

    public void setDate_debut(Date date_debut) {
        this.date_debut = date_debut;
    }

    public Date getDate_fin() {
        return date_fin;
    }

    public void setDate_fin(Date date_fin) {
        this.date_fin = date_fin;
    }

    public double getSalaire() {
        return salaire;
    }

    public void setSalaire(double salaire) {
        this.salaire = salaire;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVolume_horaire() {
        return volume_horaire;
    }

    public void setVolume_horaire(String volume_horaire) {
        this.volume_horaire = volume_horaire;
    }

    public String getAvantages() {
        return avantages;
    }

    public void setAvantages(String avantages) {
        this.avantages = avantages;
    }

    public int getId_recrutement() {
        return id_recrutement;
    }

    public void setId_recrutement(int id_recrutement) {
        this.id_recrutement = id_recrutement;
    }

    @Override
    public String toString() {
        return "Contrat{" +
                "id_contrat=" + id_contrat +
                ", type_contrat='" + type_contrat + '\'' +
                ", date_debut=" + date_debut +
                ", date_fin=" + date_fin +
                ", salaire=" + salaire +
                ", status='" + status + '\'' +
                ", volume_horaire='" + volume_horaire + '\'' +
                ", avantages='" + avantages + '\'' +
                ", id_recrutement=" + id_recrutement +
                '}';
    }
}