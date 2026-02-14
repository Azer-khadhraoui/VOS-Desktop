package entities;

import java.sql.Date;

public class Contrat {

    private int id_contrat;
    private String type_contrat;
    private Date date_debut;
    private double salaire;
    private int id_recrutement;

    public Contrat() {}

    public Contrat(String type_contrat, Date date_debut, double salaire, int id_recrutement) {
        this.type_contrat = type_contrat;
        this.date_debut = date_debut;
        this.salaire = salaire;
        this.id_recrutement = id_recrutement;
    }

    public Contrat(int id_contrat, String type_contrat, Date date_debut, double salaire, int id_recrutement) {
        this.id_contrat = id_contrat;
        this.type_contrat = type_contrat;
        this.date_debut = date_debut;
        this.salaire = salaire;
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

    public double getSalaire() {
        return salaire;
    }

    public void setSalaire(double salaire) {
        this.salaire = salaire;
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
                ", salaire=" + salaire +
                ", id_recrutement=" + id_recrutement +
                '}';
    }
}