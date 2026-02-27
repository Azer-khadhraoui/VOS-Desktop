package entities;

public class EvaluationEntretien {
    private int idEvaluation;
    private double scoreTest;
    private int noteEntretien;
    private String commentaire;
    private String decision;
    private int idEntretien;
    
    // Nouveaux critères d'évaluation (sur 5)
    private int competencesTechniques;
    private int competencesComportementales;
    private int communication;
    private int motivation;
    private int experience;

    public EvaluationEntretien() {}

    public EvaluationEntretien(double scoreTest, int noteEntretien,
                               String commentaire, String decision, int idEntretien) {
        this.scoreTest = scoreTest;
        this.noteEntretien = noteEntretien;
        this.commentaire = commentaire;
        this.decision = decision;
        this.idEntretien = idEntretien;
    }

    public EvaluationEntretien(int idEvaluation, double scoreTest, int noteEntretien,
                               String commentaire, String decision, int idEntretien) {
        this.idEvaluation = idEvaluation;
        this.scoreTest = scoreTest;
        this.noteEntretien = noteEntretien;
        this.commentaire = commentaire;
        this.decision = decision;
        this.idEntretien = idEntretien;
    }
    
    // Constructeur complet avec critères
    public EvaluationEntretien(double scoreTest, int noteEntretien, String commentaire, String decision, 
                               int idEntretien, int competencesTechniques, int competencesComportementales,
                               int communication, int motivation, int experience) {
        this.scoreTest = scoreTest;
        this.noteEntretien = noteEntretien;
        this.commentaire = commentaire;
        this.decision = decision;
        this.idEntretien = idEntretien;
        this.competencesTechniques = competencesTechniques;
        this.competencesComportementales = competencesComportementales;
        this.communication = communication;
        this.motivation = motivation;
        this.experience = experience;
    }

    public int getIdEvaluation() { return idEvaluation; }
    public void setIdEvaluation(int idEvaluation) { this.idEvaluation = idEvaluation; }

    public double getScoreTest() { return scoreTest; }
    public void setScoreTest(double scoreTest) { this.scoreTest = scoreTest; }

    public int getNoteEntretien() { return noteEntretien; }
    public void setNoteEntretien(int noteEntretien) { this.noteEntretien = noteEntretien; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public int getIdEntretien() { return idEntretien; }
    public void setIdEntretien(int idEntretien) { this.idEntretien = idEntretien; }

    // Getters/Setters pour les critères d'évaluation
    public int getCompetencesTechniques() { return competencesTechniques; }
    public void setCompetencesTechniques(int competencesTechniques) { this.competencesTechniques = competencesTechniques; }

    public int getCompetencesComportementales() { return competencesComportementales; }
    public void setCompetencesComportementales(int competencesComportementales) { this.competencesComportementales = competencesComportementales; }

    public int getCommunication() { return communication; }
    public void setCommunication(int communication) { this.communication = communication; }

    public int getMotivation() { return motivation; }
    public void setMotivation(int motivation) { this.motivation = motivation; }

    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }

    @Override
    public String toString() {
        return "EvaluationEntretien{" +
                "idEvaluation=" + idEvaluation +
                ", scoreTest=" + scoreTest +
                ", noteEntretien=" + noteEntretien +
                ", commentaire='" + commentaire + '\'' +
                ", decision='" + decision + '\'' +
                ", idEntretien=" + idEntretien +
                ", competencesTechniques=" + competencesTechniques +
                ", competencesComportementales=" + competencesComportementales +
                ", communication=" + communication +
                ", motivation=" + motivation +
                ", experience=" + experience +
                '}';
    }
}