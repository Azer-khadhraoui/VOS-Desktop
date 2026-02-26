package vos.gestionCandidat.services;

import vos.gestionCandidat.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ⚠️ SERVICE TEMPORAIRE - À remplacer par le service de ton collègue lors de l'intégration.
 * Ce service gère la récupération des offres et leurs critères pour le matching.
 */
public class OffreEmploiServiceTemp {

    private Connection connection;

    public OffreEmploiServiceTemp() {
        connection = MyDataBase.getInstance().getConnection();
    }

    // ─── Inner class pour représenter une offre avec ses critères ─────────────

    public static class OffreAvecCriteres {
        public int idOffre;
        public String titre;
        public String description;
        public String typeContrat;
        public String statutOffre;
        // Critères liés (table critere_offre)
        public String niveauExperienceRequis;
        public String niveauEtudeRequis;
        public String competencesRequises;

        public OffreAvecCriteres() {}
    }

    /**
     * Récupère toutes les offres OUVERTES avec leurs critères associés.
     * Fait un LEFT JOIN avec critere_offre.
     */
    public List<OffreAvecCriteres> getAllOffresOuvertes() {
        String requete =
                "SELECT o.id_offre, o.titre, o.description, o.type_contrat, o.statut_offre, " +
                        "       c.niveau_experience, c.niveau_etude, c.competences_requises " +
                        "FROM offre_emploi o " +
                        "LEFT JOIN critere_offre c ON o.id_offre = c.id_offre " +
                        "WHERE o.statut_offre = 'Ouverte'";

        List<OffreAvecCriteres> offres = new ArrayList<>();

        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(requete);

            while (rs.next()) {
                OffreAvecCriteres offre = new OffreAvecCriteres();
                offre.idOffre               = rs.getInt("id_offre");
                offre.titre                 = rs.getString("titre");
                offre.description           = rs.getString("description");
                offre.typeContrat           = rs.getString("type_contrat");
                offre.statutOffre           = rs.getString("statut_offre");
                offre.niveauExperienceRequis = rs.getString("niveau_experience");
                offre.niveauEtudeRequis      = rs.getString("niveau_etude");
                offre.competencesRequises    = rs.getString("competences_requises");
                offres.add(offre);
            }

        } catch (SQLException e) {
            System.out.println("Erreur getAllOffresOuvertes : " + e.getMessage());
        }

        return offres;
    }

    /**
     * Récupère une offre avec ses critères par son ID.
     */
    public OffreAvecCriteres getOffreById(int idOffre) {
        String requete =
                "SELECT o.id_offre, o.titre, o.description, o.type_contrat, o.statut_offre, " +
                        "       c.niveau_experience, c.niveau_etude, c.competences_requises " +
                        "FROM offre_emploi o " +
                        "LEFT JOIN critere_offre c ON o.id_offre = c.id_offre " +
                        "WHERE o.id_offre = ?";

        try {
            PreparedStatement pst = connection.prepareStatement(requete);
            pst.setInt(1, idOffre);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                OffreAvecCriteres offre = new OffreAvecCriteres();
                offre.idOffre               = rs.getInt("id_offre");
                offre.titre                 = rs.getString("titre");
                offre.description           = rs.getString("description");
                offre.typeContrat           = rs.getString("type_contrat");
                offre.statutOffre           = rs.getString("statut_offre");
                offre.niveauExperienceRequis = rs.getString("niveau_experience");
                offre.niveauEtudeRequis      = rs.getString("niveau_etude");
                offre.competencesRequises    = rs.getString("competences_requises");
                return offre;
            }

        } catch (SQLException e) {
            System.out.println("Erreur getOffreById : " + e.getMessage());
        }

        return null;
    }
}