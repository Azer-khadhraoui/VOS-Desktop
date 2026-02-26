package entities;

import java.sql.Date;
import java.sql.Time;

public class Entretien {
    private int idEntretien;
    private Date dateEntretien;
    private Time heureEntretien;
    private String typeEntretien;
    private String statutEntretien;
    private String lieu;
    private String typeTest;
    private int idCandidature;
    private int idUtilisateur;
    private String questionsEntretien; // Questions d'entretien (texte)
    private String lienReunion;        // Lien réunion en ligne (Meet, Teams, Zoom...)

    public Entretien() {}

    public Entretien(Date dateEntretien, Time heureEntretien, String typeEntretien,
                     String statutEntretien, String lieu, String typeTest,
                     int idCandidature, int idUtilisateur) {
        this.dateEntretien = dateEntretien;
        this.heureEntretien = heureEntretien;
        this.typeEntretien = typeEntretien;
        this.statutEntretien = statutEntretien;
        this.lieu = lieu;
        this.typeTest = typeTest;
        this.idCandidature = idCandidature;
        this.idUtilisateur = idUtilisateur;
        this.questionsEntretien = null;
    }

    public Entretien(int idEntretien, Date dateEntretien, Time heureEntretien,
                     String typeEntretien, String statutEntretien, String lieu,
                     String typeTest, int idCandidature, int idUtilisateur) {
        this.idEntretien = idEntretien;
        this.dateEntretien = dateEntretien;
        this.heureEntretien = heureEntretien;
        this.typeEntretien = typeEntretien;
        this.statutEntretien = statutEntretien;
        this.lieu = lieu;
        this.typeTest = typeTest;
        this.idCandidature = idCandidature;
        this.idUtilisateur = idUtilisateur;
    }

    public int getIdEntretien() { return idEntretien; }
    public void setIdEntretien(int idEntretien) { this.idEntretien = idEntretien; }

    public Date getDateEntretien() { return dateEntretien; }
    public void setDateEntretien(Date dateEntretien) { this.dateEntretien = dateEntretien; }

    public Time getHeureEntretien() { return heureEntretien; }
    public void setHeureEntretien(Time heureEntretien) { this.heureEntretien = heureEntretien; }

    public String getTypeEntretien() { return typeEntretien; }
    public void setTypeEntretien(String typeEntretien) { this.typeEntretien = typeEntretien; }

    public String getStatutEntretien() { return statutEntretien; }
    public void setStatutEntretien(String statutEntretien) { this.statutEntretien = statutEntretien; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public String getTypeTest() { return typeTest; }
    public void setTypeTest(String typeTest) { this.typeTest = typeTest; }

    public int getIdCandidature() { return idCandidature; }
    public void setIdCandidature(int idCandidature) { this.idCandidature = idCandidature; }

    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public String getQuestionsEntretien() { return questionsEntretien; }
    public void setQuestionsEntretien(String questionsEntretien) { this.questionsEntretien = questionsEntretien; }

    public String getLienReunion() { return lienReunion; }
    public void setLienReunion(String lienReunion) { this.lienReunion = lienReunion; }

    @Override
    public String toString() {
        return "Entretien{" +
                "idEntretien=" + idEntretien +
                ", dateEntretien=" + dateEntretien +
                ", heureEntretien=" + heureEntretien +
                ", typeEntretien='" + typeEntretien + '\'' +
                ", statutEntretien='" + statutEntretien + '\'' +
                ", lieu='" + lieu + '\'' +
                ", typeTest='" + typeTest + '\'' +
                ", idCandidature=" + idCandidature +
                ", idUtilisateur=" + idUtilisateur +
                '}';
    }
}