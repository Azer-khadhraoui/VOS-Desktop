package vos.gestionCandidat.services;

import vos.gestionCandidat.entities.Candidature;
import vos.gestionCandidat.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class CandidatureService implements IService<Candidature> {
    private Connection connection;
    public CandidatureService() {
        connection = MyDataBase.getInstance().getConnection();
    }
    @Override
    public void ajouter(Candidature candidature) {
        String requete = "INSERT INTO candidature (date_candidature, statut, message_candidat, " +
                "cv, lettre_motivation, niveau_experience, annees_experience, " +
                "domaine_experience, dernier_poste, id_utilisateur, id_offre) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement pst = connection.prepareStatement(requete);
            pst.setDate(1, new Date(candidature.getDateCandidature().getTime()));
            pst.setString(2, candidature.getStatut());
            pst.setString(3, candidature.getMessageCandidat());
            pst.setString(4, candidature.getCv());
            pst.setString(5, candidature.getLettreMotivation());
            pst.setString(6, candidature.getNiveauExperience());
            pst.setInt(7, candidature.getAnneesExperience());
            pst.setString(8, candidature.getDomaineExperience());
            pst.setString(9, candidature.getDernierPoste());
            pst.setInt(10, candidature.getIdUtilisateur());
            pst.setInt(11, candidature.getIdOffre());

            pst.executeUpdate();
            System.out.println("Candidature ajoutée avec succès !");

        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    @Override
    public void modifier(Candidature candidature) {
        String requete = "UPDATE candidature SET date_candidature = ?, statut = ?, " +
                "message_candidat = ?, cv = ?, lettre_motivation = ?, " +
                "niveau_experience = ?, annees_experience = ?, domaine_experience = ?, " +
                "dernier_poste = ?, id_utilisateur = ?, id_offre = ? " +
                "WHERE id_candidature = ?";

        try {
            PreparedStatement pst = connection.prepareStatement(requete);
            pst.setDate(1, new Date(candidature.getDateCandidature().getTime()));
            pst.setString(2, candidature.getStatut());
            pst.setString(3, candidature.getMessageCandidat());
            pst.setString(4, candidature.getCv());
            pst.setString(5, candidature.getLettreMotivation());
            pst.setString(6, candidature.getNiveauExperience());
            pst.setInt(7, candidature.getAnneesExperience());
            pst.setString(8, candidature.getDomaineExperience());
            pst.setString(9, candidature.getDernierPoste());
            pst.setInt(10, candidature.getIdUtilisateur());
            pst.setInt(11, candidature.getIdOffre());
            pst.setInt(12, candidature.getIdCandidature());

            pst.executeUpdate();
            System.out.println("Candidature modifiée avec succès !");

        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification : " + e.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {
        String requete = "DELETE FROM candidature WHERE id_candidature = ?";

        try {
            PreparedStatement pst = connection.prepareStatement(requete);
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Candidature supprimée avec succès !");

        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression : " + e.getMessage());
        }
    }

    @Override
    public Candidature getById(int id) {
        String requete = "SELECT * FROM candidature WHERE id_candidature = ?";
        Candidature candidature = null;

        try {
            PreparedStatement pst = connection.prepareStatement(requete);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                candidature = new Candidature();
                candidature.setIdCandidature(rs.getInt("id_candidature"));
                candidature.setDateCandidature(rs.getDate("date_candidature"));
                candidature.setStatut(rs.getString("statut"));
                candidature.setMessageCandidat(rs.getString("message_candidat"));
                candidature.setCv(rs.getString("cv"));
                candidature.setLettreMotivation(rs.getString("lettre_motivation"));
                candidature.setNiveauExperience(rs.getString("niveau_experience"));
                candidature.setAnneesExperience(rs.getInt("annees_experience"));
                candidature.setDomaineExperience(rs.getString("domaine_experience"));
                candidature.setDernierPoste(rs.getString("dernier_poste"));
                candidature.setIdUtilisateur(rs.getInt("id_utilisateur"));
                candidature.setIdOffre(rs.getInt("id_offre"));
            }

        } catch (SQLException e) {
            System.out.println(" Erreur lors de la récupération : " + e.getMessage());
        }

        return candidature;
    }

    @Override
    public List<Candidature> getAll() {
        String requete = "SELECT * FROM candidature";
        List<Candidature> candidatures = new ArrayList<>();

        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(requete);

            while (rs.next()) {
                Candidature candidature = new Candidature();
                candidature.setIdCandidature(rs.getInt("id_candidature"));
                candidature.setDateCandidature(rs.getDate("date_candidature"));
                candidature.setStatut(rs.getString("statut"));
                candidature.setMessageCandidat(rs.getString("message_candidat"));
                candidature.setCv(rs.getString("cv"));
                candidature.setLettreMotivation(rs.getString("lettre_motivation"));
                candidature.setNiveauExperience(rs.getString("niveau_experience"));
                candidature.setAnneesExperience(rs.getInt("annees_experience"));
                candidature.setDomaineExperience(rs.getString("domaine_experience"));
                candidature.setDernierPoste(rs.getString("dernier_poste"));
                candidature.setIdUtilisateur(rs.getInt("id_utilisateur"));
                candidature.setIdOffre(rs.getInt("id_offre"));

                candidatures.add(candidature);
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération de toutes les candidatures : " + e.getMessage());
        }

        return candidatures;
    }


/*
    public List<Candidature> getCandidaturesByUtilisateur(int idUtilisateur) {
        String requete = "SELECT * FROM candidature WHERE id_utilisateur = ?";
        List<Candidature> candidatures = new ArrayList<>();

        try {
            PreparedStatement pst = connection.prepareStatement(requete);
            pst.setInt(1, idUtilisateur);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Candidature candidature = new Candidature();
                candidature.setIdCandidature(rs.getInt("id_candidature"));
                candidature.setDateCandidature(rs.getDate("date_candidature"));
                candidature.setStatut(rs.getString("statut"));
                candidature.setMessageCandidat(rs.getString("message_candidat"));
                candidature.setCv(rs.getString("cv"));
                candidature.setLettreMotivation(rs.getString("lettre_motivation"));
                candidature.setNiveauExperience(rs.getString("niveau_experience"));
                candidature.setAnneesExperience(rs.getInt("annees_experience"));
                candidature.setDomaineExperience(rs.getString("domaine_experience"));
                candidature.setDernierPoste(rs.getString("dernier_poste"));
                candidature.setIdUtilisateur(rs.getInt("id_utilisateur"));
                candidature.setIdOffre(rs.getInt("id_offre"));

                candidatures.add(candidature);
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur : " + e.getMessage());
        }

        return candidatures;
    }
*/

    /**
     * Méthode supplémentaire : Récupérer les candidatures par offre
     * @param idOffre ID de l'offre
     * @return Liste des candidatures pour une offre
     */
/*
    public List<Candidature> getCandidaturesByOffre(int idOffre) {
        String requete = "SELECT * FROM candidature WHERE id_offre = ?";
        List<Candidature> candidatures = new ArrayList<>();

        try {
            PreparedStatement pst = connection.prepareStatement(requete);
            pst.setInt(1, idOffre);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Candidature candidature = new Candidature();
                candidature.setIdCandidature(rs.getInt("id_candidature"));
                candidature.setDateCandidature(rs.getDate("date_candidature"));
                candidature.setStatut(rs.getString("statut"));
                candidature.setMessageCandidat(rs.getString("message_candidat"));
                candidature.setCv(rs.getString("cv"));
                candidature.setLettreMotivation(rs.getString("lettre_motivation"));
                candidature.setNiveauExperience(rs.getString("niveau_experience"));
                candidature.setAnneesExperience(rs.getInt("annees_experience"));
                candidature.setDomaineExperience(rs.getString("domaine_experience"));
                candidature.setDernierPoste(rs.getString("dernier_poste"));
                candidature.setIdUtilisateur(rs.getInt("id_utilisateur"));
                candidature.setIdOffre(rs.getInt("id_offre"));

                candidatures.add(candidature);
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur : " + e.getMessage());
        }

        return candidatures;
    }
*/

}
