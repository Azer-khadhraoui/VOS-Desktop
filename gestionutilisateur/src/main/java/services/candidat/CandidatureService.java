package services.candidat;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import entities.Candidature;
import services.EmailService;
import utilis.MyConnection;
public class CandidatureService implements IService<Candidature> {
    private Connection connection;
    private EmailService emailService;

    public CandidatureService() {
        connection = MyConnection.getInstance().getCnx();
        this.emailService = new EmailService();
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
            // Envoyer l'email de création (en arrière-plan)
            if (emailService.isConfigured()) {
                String date = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                emailService.sendCandidatureCreatedEmail(
                        "Name-yassine",
                        "mamiy463@gmail.com\n",
                        "Laravel Developper ",
                        date
                );
            }

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

            if (emailService.isConfigured()) {
                emailService.sendCandidatureUpdatedEmail(
                        "Name-yassine",
                        "mamiy463@gmail.com\n",
                        "Laravel Developper ",
                        candidature.getStatut()
                );
            }

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

            // Envoyer l'email de suppression (en arrière-plan)
            if (emailService.isConfigured()) {
                emailService.sendCandidatureDeletedEmail(
                        "Name-yassine",
                        "mamiy463@gmail.com\n",
                        "Laravel Developper "
                );
            }

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

    /**
     * Méthode supplémentaire : Récupérer les candidatures par offre
     * @param idOffre ID de l'offre
     * @return Liste des candidatures pour une offre
     */
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
}
