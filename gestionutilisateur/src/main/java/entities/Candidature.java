package entities;
import java.util.Date;
public class Candidature {

    private int idCandidature;
    private Date dateCandidature;
    private String statut;
    private String messageCandidat;
    private String cv;
    private String lettreMotivation;
    private String niveauExperience;
    private int anneesExperience;
    private String domaineExperience;
    private String dernierPoste;
    private int idUtilisateur;
    private int idOffre;

    public Candidature(){}

    public Candidature(Date dateCandidature, String statut, String messageCandidat, String cv, String lettreMotivation, String niveauExperience, int anneesExperience, String domaineExperience, String dernierPoste, int idUtilisateur, int idOffre) {
        this.dateCandidature = dateCandidature;
        this.statut = statut;
        this.messageCandidat = messageCandidat;
        this.cv = cv;
        this.lettreMotivation = lettreMotivation;
        this.niveauExperience = niveauExperience;
        this.anneesExperience = anneesExperience;
        this.domaineExperience = domaineExperience;
        this.dernierPoste = dernierPoste;
        this.idUtilisateur = idUtilisateur;
        this.idOffre = idOffre;
    }

    public int getIdCandidature() {
        return idCandidature;
    }

    public void setIdCandidature(int idCandidature) {
        this.idCandidature = idCandidature;
    }

    public Date getDateCandidature() {
        return dateCandidature;
    }

    public void setDateCandidature(Date dateCandidature) {
        this.dateCandidature = dateCandidature;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getMessageCandidat() {
        return messageCandidat;
    }

    public void setMessageCandidat(String messageCandidat) {
        this.messageCandidat = messageCandidat;
    }

    public String getCv() {
        return cv;
    }

    public void setCv(String cv) {
        this.cv = cv;
    }

    public String getLettreMotivation() {
        return lettreMotivation;
    }

    public void setLettreMotivation(String lettreMotivation) {
        this.lettreMotivation = lettreMotivation;
    }

    public String getNiveauExperience() {
        return niveauExperience;
    }

    public void setNiveauExperience(String niveauExperience) {
        this.niveauExperience = niveauExperience;
    }

    public int getAnneesExperience() {
        return anneesExperience;
    }

    public void setAnneesExperience(int anneesExperience) {
        this.anneesExperience = anneesExperience;
    }

    public String getDomaineExperience() {
        return domaineExperience;
    }

    public void setDomaineExperience(String domaineExperience) {
        this.domaineExperience = domaineExperience;
    }

    public String getDernierPoste() {
        return dernierPoste;
    }

    public void setDernierPoste(String dernierPoste) {
        this.dernierPoste = dernierPoste;
    }

    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public int getIdOffre() {
        return idOffre;
    }

    public void setIdOffre(int idOffre) {
        this.idOffre = idOffre;
    }

    @Override
    public String toString() {
        return "Candidature{" +
                "idCandidature=" + idCandidature +
                ", dateCandidature=" + dateCandidature +
                ", statut='" + statut + '\'' +
                ", messageCandidat='" + messageCandidat + '\'' +
                ", cv='" + cv + '\'' +
                ", lettreMotivation='" + lettreMotivation + '\'' +
                ", niveauExperience='" + niveauExperience + '\'' +
                ", anneesExperience=" + anneesExperience +
                ", domaineExperience='" + domaineExperience + '\'' +
                ", dernierPoste='" + dernierPoste + '\'' +
                ", idUtilisateur=" + idUtilisateur +
                ", idOffre=" + idOffre +
                '}';
    }
}
