import services.ServiceUtilisateur;

public class Main {

    public static void main(String[] args) {

        ServiceUtilisateur su = new ServiceUtilisateur();

        // ==============================
        // ✅ TEST AJOUT (INSERT)

        // ==============================

      // su.ajouterUtilisateurStatique();


        // ==============================
        // ✅ TEST AFFICHAGE (READ)

        // ==============================

        // System.out.println("📌 Liste des utilisateurs :");
        // su.afficherAll().forEach(System.out::println);


        // ==============================
        // ✅ TEST SUPPRESSION (DELETE)

        // ==============================

        //su.supprimer(2);

// ==============================
// ✅ TEST MODIFICATION (UPDATE)

// ==============================

 //su.modifierPrenom(6, "Mohamed");

        // ==============================
        // ✅ TEST FINAL : Afficher après suppression ou ajout
        // ==============================

        System.out.println("📌 Liste finale :");
        su.afficherAll().forEach(System.out::println);
    }
}
