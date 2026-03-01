package entities;

import java.sql.Date;

public class Recrutement {

    private int id_recrutement;
    private Date date_decision;
    private String decision_finale;
    private int id_entretien;
    private int id_utilisateur;

    public Recrutement() {
    }

    public Recrutement(Date date_decision, String decision_finale, int id_entretien, int id_utilisateur) {
        this.date_decision = date_decision;
        this.decision_finale = decision_finale;
        this.id_entretien = id_entretien;
        this.id_utilisateur = id_utilisateur;
    }

    public Recrutement(int id_recrutement, Date date_decision, String decision_finale, int id_entretien,
            int id_utilisateur) {
        this.id_recrutement = id_recrutement;
        this.date_decision = date_decision;
        this.decision_finale = decision_finale;
        this.id_entretien = id_entretien;
        this.id_utilisateur = id_utilisateur;
    }

    // ✅ Getters & Setters

    public int getId_recrutement() {
        return id_recrutement;
    }

    public void setId_recrutement(int id_recrutement) {
        this.id_recrutement = id_recrutement;
    }

    public Date getDate_decision() {
        return date_decision;
    }

    public void setDate_decision(Date date_decision) {
        this.date_decision = date_decision;
    }

    public String getDecision_finale() {
        return decision_finale;
    }

    public void setDecision_finale(String decision_finale) {
        this.decision_finale = decision_finale;
    }

    public int getId_entretien() {
        return id_entretien;
    }

    public void setId_entretien(int id_entretien) {
        this.id_entretien = id_entretien;
    }

    public int getId_utilisateur() {
        return id_utilisateur;
    }

    public void setId_utilisateur(int id_utilisateur) {
        this.id_utilisateur = id_utilisateur;
    }

    @Override
    public String toString() {
        return "Recrutement{" +
                "id_recrutement=" + id_recrutement +
                ", date_decision=" + date_decision +
                ", decision_finale='" + decision_finale + '\'' +
                ", id_entretien=" + id_entretien +
                ", id_utilisateur=" + id_utilisateur +
                '}';
    }
}
