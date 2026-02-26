package services;


import entities.EvaluationEntretien;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvaluationEntretienService {
    private Connection connection;

    public EvaluationEntretienService() {
        connection = MyConnection.getInstance();
    }

    // CREATE
    public void addEvaluation(EvaluationEntretien evaluation) {
        String sql = "INSERT INTO evaluation_entretien (score_test, note_entretien, " +
                "commentaire, decision, id_entretien) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setDouble(1, evaluation.getScoreTest());
            pst.setInt(2, evaluation.getNoteEntretien());
            pst.setString(3, evaluation.getCommentaire());
            pst.setString(4, evaluation.getDecision());
            pst.setInt(5, evaluation.getIdEntretien());

            pst.executeUpdate();
            System.out.println("Evaluation added successfully!");
        } catch (SQLException e) {
            System.err.println("Error adding evaluation: " + e.getMessage());
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
                return new EvaluationEntretien(
                        rs.getInt("id_evaluation"),
                        rs.getDouble("score_test"),
                        rs.getInt("note_entretien"),
                        rs.getString("commentaire"),
                        rs.getString("decision"),
                        rs.getInt("id_entretien")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching evaluation: " + e.getMessage());
        }

        return null;
    }

    // UPDATE
    public void updateEvaluation(EvaluationEntretien evaluation) {
        String sql = "UPDATE evaluation_entretien SET score_test = ?, note_entretien = ?, " +
                "commentaire = ?, decision = ?, id_entretien = ? WHERE id_evaluation = ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setDouble(1, evaluation.getScoreTest());
            pst.setInt(2, evaluation.getNoteEntretien());
            pst.setString(3, evaluation.getCommentaire());
            pst.setString(4, evaluation.getDecision());
            pst.setInt(5, evaluation.getIdEntretien());
            pst.setInt(6, evaluation.getIdEvaluation());

            pst.executeUpdate();
            System.out.println("Evaluation updated successfully!");
        } catch (SQLException e) {
            System.err.println("Error updating evaluation: " + e.getMessage());
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