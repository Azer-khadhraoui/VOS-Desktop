package entities;

public class EvaluationEntretien {
    private int idEvaluation;
    private double scoreTest;
    private int noteEntretien;
    private String commentaire;
    private String decision;
    private int idEntretien;

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

    @Override
    public String toString() {
        return "EvaluationEntretien{" +
                "idEvaluation=" + idEvaluation +
                ", scoreTest=" + scoreTest +
                ", noteEntretien=" + noteEntretien +
                ", commentaire='" + commentaire + '\'' +
                ", decision='" + decision + '\'' +
                ", idEntretien=" + idEntretien +
                '}';
    }
}