package entities;

/**
 * Représente le résultat du matching entre les PRÉFÉRENCES d'un candidat et une offre d'emploi.
 *
 * PONDÉRATION :
 *   - scoreTypePoste    : 40%
 *   - scoreContrat      : 30%
 *   - scoreModeTravail  : 15%
 *   - scoreDisponibilite: 10%
 *   - scoreMobilite     :  5%
 */
public class MatchResult {

    private int    idOffre;
    private String titreOffre;
    private String typeContratOffre;
    private String descriptionOffre;

    // ── Scores partiels (0 à 100 chacun) ──────────────────────────────────────
    private int scoreTypePoste;       // type_poste_souhaite   vs titre/description offre
    private int scoreContrat;         // type_contrat_souhaite vs type_contrat offre
    private int scoreModeTravail;     // mode_travail          vs description offre
    private int scoreDisponibilite;   // disponibilite         (score autonome)
    private int scoreMobilite;        // mobilite_geographique vs description offre

    // ── Score final pondéré (0 à 100) ─────────────────────────────────────────
    private int    scoreTotal;
    private String niveauMatch; // "Excellent" / "Bon" / "Moyen" / "Faible"

    // ─── Constructeurs ────────────────────────────────────────────────────────
    public MatchResult() {}

    public MatchResult(int idOffre, String titreOffre, String typeContratOffre, String descriptionOffre) {
        this.idOffre          = idOffre;
        this.titreOffre       = titreOffre;
        this.typeContratOffre = typeContratOffre;
        this.descriptionOffre = descriptionOffre;
    }

    // ─── Calcul du score final ────────────────────────────────────────────────
    /**
     * Calcule le score total pondéré et détermine le niveau de match.
     * À appeler APRÈS avoir renseigné tous les scores partiels.
     *
     * Pondération :
     *   Poste 40% + Contrat 30% + Mode 15% + Dispo 10% + Mobilité 5% = 100%
     */
    public void calculerScoreTotal() {
        this.scoreTotal = (int) (
                scoreTypePoste      * 0.40 +
                scoreContrat        * 0.30 +
                scoreModeTravail    * 0.15 +
                scoreDisponibilite  * 0.10 +
                scoreMobilite       * 0.05
        );

        if      (scoreTotal >= 75) this.niveauMatch = "Excellent";
        else if (scoreTotal >= 50) this.niveauMatch = "Bon";
        else if (scoreTotal >= 25) this.niveauMatch = "Moyen";
        else                       this.niveauMatch = "Faible";
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────
    public int    getIdOffre()           { return idOffre; }
    public void   setIdOffre(int v)      { this.idOffre = v; }

    public String getTitreOffre()        { return titreOffre; }
    public void   setTitreOffre(String v){ this.titreOffre = v; }

    public String getTypeContratOffre()         { return typeContratOffre; }
    public void   setTypeContratOffre(String v) { this.typeContratOffre = v; }

    public String getDescriptionOffre()         { return descriptionOffre; }
    public void   setDescriptionOffre(String v) { this.descriptionOffre = v; }

    public int  getScoreTypePoste()         { return scoreTypePoste; }
    public void setScoreTypePoste(int v)    { this.scoreTypePoste = v; }

    public int  getScoreContrat()           { return scoreContrat; }
    public void setScoreContrat(int v)      { this.scoreContrat = v; }

    public int  getScoreModeTravail()       { return scoreModeTravail; }
    public void setScoreModeTravail(int v)  { this.scoreModeTravail = v; }

    public int  getScoreDisponibilite()     { return scoreDisponibilite; }
    public void setScoreDisponibilite(int v){ this.scoreDisponibilite = v; }

    public int  getScoreMobilite()          { return scoreMobilite; }
    public void setScoreMobilite(int v)     { this.scoreMobilite = v; }

    public int    getScoreTotal()           { return scoreTotal; }
    public void   setScoreTotal(int v)      { this.scoreTotal = v; }

    public String getNiveauMatch()          { return niveauMatch; }
    public void   setNiveauMatch(String v)  { this.niveauMatch = v; }

    @Override
    public String toString() {
        return "MatchResult{" +
                "titreOffre='"   + titreOffre   + '\'' +
                ", scoreTotal="  + scoreTotal   +
                ", niveauMatch='" + niveauMatch + '\'' +
                '}';
    }
}