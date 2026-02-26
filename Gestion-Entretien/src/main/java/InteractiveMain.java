import entities.Entretien;
import entities.EvaluationEntretien;
import services.EntretienService;
import services.EvaluationEntretienService;

import java.sql.Date;
import java.sql.Time;
import java.util.List;
import java.util.Scanner;

public class InteractiveMain {

    private static final EntretienService entretienService = new EntretienService();
    private static final EvaluationEntretienService evaluationService = new EvaluationEntretienService();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=====================================");
        System.out.println("   VOS - Gestion des Entretiens   ");
        System.out.println("=====================================");

        while (true) {
            printMenu();
            int choice = readInt("Choix : ");

            switch (choice) {
                case 1 -> ajouterEntretien();
                case 2 -> afficherTousEntretiens();
                case 3 -> afficherEntretienParId();
                case 4 -> modifierEntretien();
                case 5 -> supprimerEntretien();
                case 6 -> ajouterEvaluation();
                case 7 -> afficherToutesEvaluations();
                case 8 -> afficherEvaluationParId();
                case 9 -> modifierEvaluation();
                case 10 -> supprimerEvaluation();
                case 0 -> {
                    System.out.println("Au revoir !");
                    scanner.close();
                    return;
                }
                default -> System.out.println("Choix invalide.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n========== MENU ==========");
        System.out.println("--- Gestion Entretiens ---");
        System.out.println("1. Ajouter un entretien");
        System.out.println("2. Afficher tous les entretiens");
        System.out.println("3. Afficher un entretien par ID");
        System.out.println("4. Modifier un entretien");
        System.out.println("5. Supprimer un entretien");
        System.out.println("\n--- Gestion Evaluations ---");
        System.out.println("6. Ajouter une évaluation");
        System.out.println("7. Afficher toutes les évaluations");
        System.out.println("8. Afficher une évaluation par ID");
        System.out.println("9. Modifier une évaluation");
        System.out.println("10. Supprimer une évaluation");
        System.out.println("\n0. Quitter");
        System.out.println("==========================");
    }

    private static int readInt(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.print("Entrez un nombre valide : ");
            scanner.next();
        }
        int val = scanner.nextInt();
        scanner.nextLine();
        return val;
    }

    private static double readDouble(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextDouble()) {
            System.out.print("Entrez un nombre valide : ");
            scanner.next();
        }
        double val = scanner.nextDouble();
        scanner.nextLine();
        return val;
    }

    private static String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    // ========== ENTRETIEN CRUD ==========

    private static void ajouterEntretien() {
        System.out.println("\n--- Ajouter un Entretien ---");

        String dateStr = readString("Date entretien (YYYY-MM-DD) : ");
        String timeStr = readString("Heure entretien (HH:MM:SS) : ");
        String type = readString("Type entretien (RH/TECHNIQUE) : ");
        String statut = readString("Statut (Planifié/Confirmé/Terminé) : ");
        String lieu = readString("Lieu : ");
        String typeTest = readString("Type de test : ");
        int idCandidature = readInt("ID Candidature : ");
        int idUtilisateur = readInt("ID Utilisateur : ");

        try {
            Entretien entretien = new Entretien(
                    Date.valueOf(dateStr),
                    Time.valueOf(timeStr),
                    type,
                    statut,
                    lieu,
                    typeTest,
                    idCandidature,
                    idUtilisateur
            );

            entretienService.addEntretien(entretien);
            System.out.println("✓ Entretien ajouté avec succès !");
        } catch (Exception e) {
            System.out.println("✗ Erreur : " + e.getMessage());
        }
    }

    private static void afficherTousEntretiens() {
        System.out.println("\n--- Liste des Entretiens ---");
        List<Entretien> entretiens = entretienService.getAllEntretiens();

        if (entretiens.isEmpty()) {
            System.out.println("Aucun entretien trouvé.");
            return;
        }

        System.out.println("ID | Date | Heure | Type | Statut | Lieu");
        System.out.println("--------------------------------------------------");
        for (Entretien e : entretiens) {
            System.out.printf("%d | %s | %s | %s | %s | %s%n",
                    e.getIdEntretien(),
                    e.getDateEntretien(),
                    e.getHeureEntretien(),
                    e.getTypeEntretien(),
                    e.getStatutEntretien(),
                    e.getLieu()
            );
        }
    }

    private static void afficherEntretienParId() {
        System.out.println("\n--- Afficher Entretien ---");
        int id = readInt("ID de l'entretien : ");

        Entretien entretien = entretienService.getEntretienById(id);

        if (entretien == null) {
            System.out.println("✗ Entretien non trouvé.");
        } else {
            System.out.println("\n" + entretien);
        }
    }

    private static void modifierEntretien() {
        System.out.println("\n--- Modifier un Entretien ---");
        int id = readInt("ID de l'entretien à modifier : ");

        Entretien entretien = entretienService.getEntretienById(id);

        if (entretien == null) {
            System.out.println("✗ Entretien non trouvé.");
            return;
        }

        System.out.println("Entretien actuel : " + entretien);
        System.out.println("\nLaissez vide pour conserver la valeur actuelle.");

        String dateStr = readString("Nouvelle date (" + entretien.getDateEntretien() + ") : ");
        if (!dateStr.isEmpty()) {
            entretien.setDateEntretien(Date.valueOf(dateStr));
        }

        String timeStr = readString("Nouvelle heure (" + entretien.getHeureEntretien() + ") : ");
        if (!timeStr.isEmpty()) {
            entretien.setHeureEntretien(Time.valueOf(timeStr));
        }

        String type = readString("Nouveau type (" + entretien.getTypeEntretien() + ") : ");
        if (!type.isEmpty()) {
            entretien.setTypeEntretien(type);
        }

        String statut = readString("Nouveau statut (" + entretien.getStatutEntretien() + ") : ");
        if (!statut.isEmpty()) {
            entretien.setStatutEntretien(statut);
        }

        String lieu = readString("Nouveau lieu (" + entretien.getLieu() + ") : ");
        if (!lieu.isEmpty()) {
            entretien.setLieu(lieu);
        }

        String typeTest = readString("Nouveau type test (" + entretien.getTypeTest() + ") : ");
        if (!typeTest.isEmpty()) {
            entretien.setTypeTest(typeTest);
        }

        entretienService.updateEntretien(entretien);
        System.out.println("✓ Entretien modifié avec succès !");
    }

    private static void supprimerEntretien() {
        System.out.println("\n--- Supprimer un Entretien ---");
        int id = readInt("ID de l'entretien à supprimer : ");

        String confirm = readString("Êtes-vous sûr ? (oui/non) : ");
        if (confirm.equalsIgnoreCase("oui")) {
            entretienService.deleteEntretien(id);
            System.out.println("✓ Entretien supprimé !");
        } else {
            System.out.println("Suppression annulée.");
        }
    }

    // ========== EVALUATION CRUD ==========

    private static void ajouterEvaluation() {
        System.out.println("\n--- Ajouter une Évaluation ---");

        double scoreTest = readDouble("Score du test : ");
        int noteEntretien = readInt("Note entretien (1-5) : ");
        String commentaire = readString("Commentaire : ");
        String decision = readString("Décision (Accepté/Refusé/En attente) : ");
        int idEntretien = readInt("ID de l'entretien : ");

        try {
            EvaluationEntretien evaluation = new EvaluationEntretien(
                    scoreTest,
                    noteEntretien,
                    commentaire,
                    decision,
                    idEntretien
            );

            evaluationService.addEvaluation(evaluation);
            System.out.println("✓ Évaluation ajoutée avec succès !");
        } catch (Exception e) {
            System.out.println("✗ Erreur : " + e.getMessage());
        }
    }

    private static void afficherToutesEvaluations() {
        System.out.println("\n--- Liste des Évaluations ---");
        List<EvaluationEntretien> evaluations = evaluationService.getAllEvaluations();

        if (evaluations.isEmpty()) {
            System.out.println("Aucune évaluation trouvée.");
            return;
        }

        System.out.println("ID | Score | Note | Décision | ID Entretien");
        System.out.println("--------------------------------------------------");
        for (EvaluationEntretien e : evaluations) {
            System.out.printf("%d | %.2f | %d | %s | %d%n",
                    e.getIdEvaluation(),
                    e.getScoreTest(),
                    e.getNoteEntretien(),
                    e.getDecision(),
                    e.getIdEntretien()
            );
        }
    }

    private static void afficherEvaluationParId() {
        System.out.println("\n--- Afficher Évaluation ---");
        int id = readInt("ID de l'évaluation : ");

        EvaluationEntretien evaluation = evaluationService.getEvaluationById(id);

        if (evaluation == null) {
            System.out.println("✗ Évaluation non trouvée.");
        } else {
            System.out.println("\n" + evaluation);
        }
    }

    private static void modifierEvaluation() {
        System.out.println("\n--- Modifier une Évaluation ---");
        int id = readInt("ID de l'évaluation à modifier : ");

        EvaluationEntretien evaluation = evaluationService.getEvaluationById(id);

        if (evaluation == null) {
            System.out.println("✗ Évaluation non trouvée.");
            return;
        }

        System.out.println("Évaluation actuelle : " + evaluation);
        System.out.println("\nLaissez vide pour conserver la valeur actuelle.");

        String scoreStr = readString("Nouveau score (" + evaluation.getScoreTest() + ") : ");
        if (!scoreStr.isEmpty()) {
            evaluation.setScoreTest(Double.parseDouble(scoreStr));
        }

        String noteStr = readString("Nouvelle note (" + evaluation.getNoteEntretien() + ") : ");
        if (!noteStr.isEmpty()) {
            evaluation.setNoteEntretien(Integer.parseInt(noteStr));
        }

        String commentaire = readString("Nouveau commentaire (" + evaluation.getCommentaire() + ") : ");
        if (!commentaire.isEmpty()) {
            evaluation.setCommentaire(commentaire);
        }

        String decision = readString("Nouvelle décision (" + evaluation.getDecision() + ") : ");
        if (!decision.isEmpty()) {
            evaluation.setDecision(decision);
        }

        evaluationService.updateEvaluation(evaluation);
        System.out.println("✓ Évaluation modifiée avec succès !");
    }

    private static void supprimerEvaluation() {
        System.out.println("\n--- Supprimer une Évaluation ---");
        int id = readInt("ID de l'évaluation à supprimer : ");

        String confirm = readString("Êtes-vous sûr ? (oui/non) : ");
        if (confirm.equalsIgnoreCase("oui")) {
            evaluationService.deleteEvaluation(id);
            System.out.println("✓ Évaluation supprimée !");
        } else {
            System.out.println("Suppression annulée.");
        }
    }
}