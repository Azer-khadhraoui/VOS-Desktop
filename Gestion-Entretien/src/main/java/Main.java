

import entities.Entretien;
import entities.EvaluationEntretien;
import services.EntretienService;
import services.EvaluationEntretienService;

import java.sql.Date;
import java.sql.Time;

public class Main {
    public static void main(String[] args) {
        EntretienService entretienService = new EntretienService();
        EvaluationEntretienService evaluationService = new EvaluationEntretienService();

        System.out.println("=== CRUD Test for Entretien & Evaluation ===\n");

        // ========== ENTRETIEN CRUD ==========

        // CREATE - Add new Entretien
        System.out.println("=== CREATE Entretien ===");
        Entretien e1 = new Entretien(
                Date.valueOf("2026-02-15"),
                Time.valueOf("10:00:00"),
                "RH",
                "Planifié",
                "Bureau RH",
                "Test RH",
                1,  // id_candidature (use existing ID from your DB)
                1   // id_utilisateur (use existing ID from your DB)
        );
        entretienService.addEntretien(e1);

        // READ ALL - Get all Entretiens
        System.out.println("\n=== READ ALL Entretiens ===");
        entretienService.getAllEntretiens().forEach(System.out::println);

        // READ BY ID - Get specific Entretien
        System.out.println("\n=== READ Entretien by ID ===");
        Entretien found = entretienService.getEntretienById(5);  // Change ID as needed
        System.out.println(found);

        // UPDATE - Modify Entretien
        System.out.println("\n=== UPDATE Entretien ===");
        if (found != null) {
            found.setStatutEntretien("Terminé");
            found.setLieu("Salle B - Updated");
            entretienService.updateEntretien(found);
            System.out.println("Updated: " + entretienService.getEntretienById(5));
        }

        // DELETE - Remove Entretien (commented out)
        System.out.println("\n=== DELETE Entretien (commented) ===");
        // entretienService.deleteEntretien(5);
        System.out.println("Delete skipped");


        // ========== EVALUATION CRUD ==========

        // CREATE - Add new Evaluation
        System.out.println("\n\n=== CREATE Evaluation ===");
        EvaluationEntretien eval1 = new EvaluationEntretien(
                85.5,           // score_test
                4,              // note_entretien
                "Bon candidat", // commentaire
                "Accepté",      // decision
                5               // id_entretien (use existing ID from your DB)
        );
        evaluationService.addEvaluation(eval1);

        // READ ALL - Get all Evaluations
        System.out.println("\n=== READ ALL Evaluations ===");
        evaluationService.getAllEvaluations().forEach(System.out::println);

        // READ BY ID - Get specific Evaluation
        System.out.println("\n=== READ Evaluation by ID ===");
        EvaluationEntretien foundEval = evaluationService.getEvaluationById(5);  // Change ID as needed
        System.out.println(foundEval);

        // UPDATE - Modify Evaluation
        System.out.println("\n=== UPDATE Evaluation ===");
        if (foundEval != null) {
            foundEval.setNoteEntretien(5);
            foundEval.setCommentaire("Excellent candidat - Updated");
            evaluationService.updateEvaluation(foundEval);
            System.out.println("Updated: " + evaluationService.getEvaluationById(5));
        }

        // DELETE - Remove Evaluation (commented out)
        System.out.println("\n=== DELETE Evaluation (commented) ===");
        // evaluationService.deleteEvaluation(5);
        System.out.println("Delete skipped");

        System.out.println("\n=== All CRUD Tests Complete! ===");
    }
}