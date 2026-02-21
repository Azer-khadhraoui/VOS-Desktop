package services;

import entities.OffreEmploi;
import utilis.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.sql.ResultSet;

public class OffreEmploiService {

    private Connection connection;
    private EmailService emailService;

    public OffreEmploiService() {
        connection = MyConnection.getInstance().getCnx();
        emailService = new EmailService();
    }

    /**
     * Inserts a new job offer (OffreEmploi) into the database.
     * This method creates a new record in the offre_emploi table with all offer details.
     * After insertion, sends a notification email to the user who created the offer.
     * 
     * @param offre The OffreEmploi object containing the job offer details to be inserted
     */
    public void insertOffre(OffreEmploi offre) {

        String sql = """
            INSERT INTO offre_emploi
            (titre, description, type_contrat, statut_offre, date_publication, id_utilisateur, work_preference, lieu)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, offre.getTitre());
            ps.setString(2, offre.getDescription());
            ps.setString(3, offre.getTypeContrat());
            ps.setString(4, offre.getStatutOffre());
            ps.setDate(5, offre.getDatePublication());
            ps.setInt(6, offre.getIdUtilisateur());
            ps.setString(7, offre.getWorkPreference());
            ps.setString(8, offre.getLieu());

            ps.executeUpdate();
            System.out.println("Offre inserted");
            
            // Send email notification after insertion
            sendOffreCreationEmail(offre);
            
        } catch (SQLException e) {
            System.out.println("Insert error: " + e.getMessage());
        }
    }

    /**
     * Sends a notification email when a new offer is created.
     * Runs asynchronously in a background thread to avoid blocking the UI.
     * 
     * @param offre The OffreEmploi object that was just created
     */
    private void sendOffreCreationEmail(OffreEmploi offre) {
        // Send email in a background thread to avoid blocking the UI
        Thread emailThread = new Thread(() -> {
            try {
                // Get user email from database
                String userEmail = getUserEmailById(offre.getIdUtilisateur());
                
                if (userEmail != null && !userEmail.isEmpty()) {
                    // Send email notification with all offer details
                    boolean emailSent = emailService.sendOffreInsertionEmail(
                        userEmail,
                        offre
                    );
                    
                    if (emailSent) {
                        System.out.println("Notification email sent to: " + userEmail);
                    } else {
                        System.out.println("Failed to send notification email");
                    }
                } else {
                    System.out.println("User email not found for user ID: " + offre.getIdUtilisateur());
                }
            } catch (Exception e) {
                System.out.println("Error sending email notification: " + e.getMessage());
                e.printStackTrace();
            }
        });
        
        // Set as daemon thread so it doesn't prevent app shutdown
        emailThread.setDaemon(true);
        emailThread.setName("Email-Sender-Thread");
        emailThread.start();
    }

    /**
     * Retrieves the email address of a user by their ID.
     * 
     * @param userId The ID of the user
     * @return The user's email address, or null if not found
     */
    private String getUserEmailById(int userId) {
        String sql = "SELECT email FROM utilisateur WHERE id_utilisateur = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("email");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving user email: " + e.getMessage());
        }
        
        return null;
    }

    /**
     * Deletes a job offer from the database.
     * This method removes an offer record identified by its ID from the offre_emploi table.
     * 
     * @param idOffre The ID of the job offer to be deleted
     */
    public void deleteOffre(int idOffre) {

        String sql = "DELETE FROM offre_emploi WHERE id_offre=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idOffre);
            ps.executeUpdate();
            System.out.println("Offre deleted");
        } catch (SQLException e) {
            System.out.println("Delete error: " + e.getMessage());
        }
    }
    
    /**
     * Updates an existing job offer in the database.
     * This method modifies all fields of an offer record identified by its ID.
     * 
     * @param idOffre The ID of the job offer to be updated
     * @param offre The OffreEmploi object containing the new offer values
     */
    public void updateOffre(int idOffre, OffreEmploi offre) {

        String sql = """
        UPDATE offre_emploi
        SET titre = ?,
            description = ?,
            type_contrat = ?,
            statut_offre = ?,
            date_publication = ?,
            id_utilisateur = ?,
            work_preference = ?,
            lieu = ?
        WHERE id_offre = ?
    """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, offre.getTitre());
            ps.setString(2, offre.getDescription());
            ps.setString(3, offre.getTypeContrat());
            ps.setString(4, offre.getStatutOffre());
            ps.setDate(5, offre.getDatePublication());
            ps.setInt(6, offre.getIdUtilisateur());
            ps.setString(7, offre.getWorkPreference());
            ps.setString(8, offre.getLieu());
            ps.setInt(9, idOffre);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Offre updated");
            } else {
                System.out.println("No offer found with id " + idOffre);
            }

        } catch (SQLException e) {
            System.out.println("Update error: " + e.getMessage());
        }
    }
    
    /**
     * Retrieves all job offers from the database.
     * This method reads and returns all records from the offre_emploi table.
     * 
     * @return A List of all OffreEmploi objects in the database, or an empty list if none exist
     */
    public List<OffreEmploi> getAllOffres() {

        List<OffreEmploi> offres = new ArrayList<>();
        String sql = "SELECT * FROM offre_emploi";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                OffreEmploi offre = new OffreEmploi();
                offre.setIdOffre(rs.getInt("id_offre"));
                offre.setTitre(rs.getString("titre"));
                offre.setDescription(rs.getString("description"));
                offre.setTypeContrat(rs.getString("type_contrat"));
                offre.setStatutOffre(rs.getString("statut_offre"));
                offre.setDatePublication(rs.getDate("date_publication"));
                offre.setIdUtilisateur(rs.getInt("id_utilisateur"));
                offre.setWorkPreference(rs.getString("work_preference"));
                offre.setLieu(rs.getString("lieu"));

                offres.add(offre);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return offres;
    }


}
