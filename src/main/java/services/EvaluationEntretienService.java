package services;


import entities.EvaluationEntretien;
import entities.Entretien;
import entities.Utilisateur;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvaluationEntretienService {
    private Connection connection;
    private EntretienService entretienService       = new EntretienService();
    private UtilisateurService utilisateurService   = new UtilisateurService();

    public EvaluationEntretienService() {
        connection = MyConnection.getInstance();
    }

    // CREATE
    public void addEvaluation(EvaluationEntretien evaluation) {
        String sql = "INSERT INTO evaluation_entretien (score_test, note_entretien, " +
                "commentaire, decision, id_entretien, competences_techniques, " +
                "competences_comportementales, communication, motivation, experience) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setDouble(1, evaluation.getScoreTest());
            pst.setInt(2, evaluation.getNoteEntretien());
            pst.setString(3, evaluation.getCommentaire());
            pst.setString(4, evaluation.getDecision());
            pst.setInt(5, evaluation.getIdEntretien());
            pst.setInt(6, evaluation.getCompetencesTechniques());
            pst.setInt(7, evaluation.getCompetencesComportementales());
            pst.setInt(8, evaluation.getCommunication());
            pst.setInt(9, evaluation.getMotivation());
            pst.setInt(10, evaluation.getExperience());

            pst.executeUpdate();
            System.out.println("Evaluation added successfully!");

            // ===== EMAIL : Envoyer les résultats au candidat =====
            Entretien entretien = entretienService.getEntretienById(evaluation.getIdEntretien());
            if (entretien != null) {
                Utilisateur candidat = utilisateurService.getCandidatByCandidature(entretien.getIdCandidature());
                if (candidat != null) {
                    EmailService.envoyerResultatsEvaluation(entretien, evaluation, candidat);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error adding evaluation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // READ ALL
    public List<EvaluationEntretien> getAllEvaluations() {
        List<EvaluationEntretien> evaluations = new ArrayList<>();
        String sql = "SELECT * FROM evaluation_entretien";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                EvaluationEntretien e = new EvaluationEntretien(
                        rs.getInt("id_evaluation"),
                        rs.getDouble("score_test"),
                        rs.getInt("note_entretien"),
                        rs.getString("commentaire"),
                        rs.getString("decision"),
                        rs.getInt("id_entretien")
                );
                // Charger les critères s'ils existent
                try {
                    e.setCompetencesTechniques(rs.getInt("competences_techniques"));
                    e.setCompetencesComportementales(rs.getInt("competences_comportementales"));
                    e.setCommunication(rs.getInt("communication"));
                    e.setMotivation(rs.getInt("motivation"));
                    e.setExperience(rs.getInt("experience"));
                } catch (SQLException ignored) {
                    // Colonnes non présentes dans l'ancienne version
                }
                evaluations.add(e);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching evaluations: " + e.getMessage());
        }

        return evaluations;
    }

    // READ BY ID
    public EvaluationEntretien getEvaluationById(int id) {
        String sql = "SELECT * FROM evaluation_entretien WHERE id_evaluation = ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                EvaluationEntretien e = new EvaluationEntretien(
                        rs.getInt("id_evaluation"),
                        rs.getDouble("score_test"),
                        rs.getInt("note_entretien"),
                        rs.getString("commentaire"),
                        rs.getString("decision"),
                        rs.getInt("id_entretien")
                );
                // Charger les critères s'ils existent
                try {
                    e.setCompetencesTechniques(rs.getInt("competences_techniques"));
                    e.setCompetencesComportementales(rs.getInt("competences_comportementales"));
                    e.setCommunication(rs.getInt("communication"));
                    e.setMotivation(rs.getInt("motivation"));
                    e.setExperience(rs.getInt("experience"));
                } catch (SQLException ignored) {
                    // Colonnes non présentes dans l'ancienne version
                }
                return e;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching evaluation: " + e.getMessage());
        }

        return null;
    }

    // UPDATE
    public void updateEvaluation(EvaluationEntretien evaluation) {
        String sql = "UPDATE evaluation_entretien SET score_test = ?, note_entretien = ?, " +
                "commentaire = ?, decision = ?, id_entretien = ?, " +
                "competences_techniques = ?, competences_comportementales = ?, " +
                "communication = ?, motivation = ?, experience = ? " +
                "WHERE id_evaluation = ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setDouble(1, evaluation.getScoreTest());
            pst.setInt(2, evaluation.getNoteEntretien());
            pst.setString(3, evaluation.getCommentaire());
            pst.setString(4, evaluation.getDecision());
            pst.setInt(5, evaluation.getIdEntretien());
            pst.setInt(6, evaluation.getCompetencesTechniques());
            pst.setInt(7, evaluation.getCompetencesComportementales());
            pst.setInt(8, evaluation.getCommunication());
            pst.setInt(9, evaluation.getMotivation());
            pst.setInt(10, evaluation.getExperience());
            pst.setInt(11, evaluation.getIdEvaluation());

            pst.executeUpdate();
            System.out.println("Evaluation updated successfully!");
        } catch (SQLException e) {
            System.err.println("Error updating evaluation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // DELETE
    public void deleteEvaluation(int id) {
        String sql = "DELETE FROM evaluation_entretien WHERE id_evaluation = ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Evaluation deleted successfully!");
        } catch (SQLException e) {
            System.err.println("Error deleting evaluation: " + e.getMessage());
        }
    }
}