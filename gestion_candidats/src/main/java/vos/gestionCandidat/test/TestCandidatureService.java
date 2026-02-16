package vos.gestionCandidat.test;


import vos.gestionCandidat.entities.Candidature;
import vos.gestionCandidat.entities.PreferenceCandidature;
import vos.gestionCandidat.services.CandidatureService;
import vos.gestionCandidat.services.PreferenceCandidatureService;

import java.util.Date;
import java.util.List;

public class TestCandidatureService {

    public static void main(String[] args) {

        // Initialiser les services
        CandidatureService candidatureService = new CandidatureService();
        PreferenceCandidatureService preferenceService = new PreferenceCandidatureService();

        System.out.println("========== TEST DES SERVICES CANDIDATURE ==========\n");

        // ========== TEST 1 : AJOUTER UNE CANDIDATURE ==========
        System.out.println("--- Test 1 : Ajouter une candidature ---");
        Candidature nouvelleCandidature = new Candidature(
                new Date(),                    // date_candidature
                "En attente",                  // statut
                "Je suis démotivé !",       // message_candidat
                "cv_jean_dupont.pdf",          // cv
                "lm_jean_dupont.pdf",          // lettre_motivation
                "Intermédiaire",               // niveau_experience
                3,                             // annees_experience
                "Développement Mobile",           // domaine_experience
                "Développeur Java",            // dernier_poste
                1,                             // id_utilisateur (doit exister dans la table utilisateur)
                1                              // id_offre (doit exister dans la table offre_emploi)
        );

        candidatureService.ajouter(nouvelleCandidature);
        System.out.println();

        // ========== TEST 2 : AFFICHER TOUTES LES CANDIDATURES ==========
        System.out.println("--- Test 2 : Afficher toutes les candidatures ---");
        List<Candidature> candidatures = candidatureService.getAll();
        for (Candidature c : candidatures) {
            System.out.println(c);
        }
        System.out.println("Total candidatures : " + candidatures.size() + "\n");

        // ========== TEST 3 : RÉCUPÉRER UNE CANDIDATURE PAR ID ==========
        System.out.println("--- Test 3 : Récupérer candidature par ID ---");
        Candidature candidature = candidatureService.getById(2);
        if (candidature != null) {
            System.out.println("Candidature trouvée : " + candidature);
        } else {
            System.out.println("Aucune candidature trouvée avec cet ID");
        }
        System.out.println();

        // ========== TEST 4 : MODIFIER UNE CANDIDATURE ==========
        System.out.println("--- Test 4 : Modifier une candidature ---");
        if (candidature != null) {
            candidature.setStatut("Acceptée");
            candidature.setAnneesExperience(5);
            candidatureService.modifier(candidature);
        }
        System.out.println();

        // ========== TEST 5 : AJOUTER UNE PRÉFÉRENCE CANDIDATURE ==========
        System.out.println("--- Test 5 : Ajouter une préférence candidature ---");
        PreferenceCandidature preference = new PreferenceCandidature(
                "Développeur Full Stack",      // type_poste_souhaite
                "Hybride",                     // mode_travail
                "Immédiate",                   // disponibilite
                "Oui",                         // mobilite_geographique
                "Oui",                         // pret_deplacement
                "CDI",                         // type_contrat_souhaite
                45000.0,                       // pretention_salariale
                new Date(),                    // date_disponibilite
                2                              // id_candidature (doit exister)
        );

        preferenceService.ajouter(preference);
        System.out.println();

        // ========== TEST 6 : AFFICHER TOUTES LES PRÉFÉRENCES ==========
        System.out.println("--- Test 6 : Afficher toutes les préférences ---");
        List<PreferenceCandidature> preferences = preferenceService.getAll();
        for (PreferenceCandidature p : preferences) {
            System.out.println(p);
        }
        System.out.println("Total préférences : " + preferences.size() + "\n");

        // ========== TEST 7 : RÉCUPÉRER PRÉFÉRENCE PAR CANDIDATURE ==========
      /*  System.out.println("--- Test 7 : Récupérer préférence par candidature ---");
        PreferenceCandidature pref = preferenceService.getPreferenceByCandidature(1);
        if (pref != null) {
            System.out.println("Préférence trouvée : " + pref);
        } else {
            System.out.println("Aucune préférence trouvée pour cette candidature");
        }
        System.out.println();*/

        // ========== TEST 8 : RÉCUPÉRER CANDIDATURES PAR UTILISATEUR ==========
        /*System.out.println("--- Test 8 : Récupérer candidatures par utilisateur ---");
        List<Candidature> candidaturesUtilisateur = candidatureService.getCandidaturesByUtilisateur(1);
        System.out.println("Candidatures de l'utilisateur 1 : " + candidaturesUtilisateur.size());
        for (Candidature c : candidaturesUtilisateur) {
            System.out.println(c);
        }
        System.out.println();*/

        // ========== TEST 9 : RÉCUPÉRER CANDIDATURES PAR OFFRE ==========
       /* System.out.println("--- Test 9 : Récupérer candidatures par offre ---");
        List<Candidature> candidaturesOffre = candidatureService.getCandidaturesByOffre(1);
        System.out.println("Candidatures pour l'offre 1 : " + candidaturesOffre.size());
        for (Candidature c : candidaturesOffre) {
            System.out.println(c);
        }
        System.out.println();*/

        /*
        // ========== TEST 10 : SUPPRIMER UNE PRÉFÉRENCE ==========
        System.out.println("--- Test 10 : Supprimer une préférence ---");
        preferenceService.supprimer(1);
        System.out.println();

        // ========== TEST 11 : SUPPRIMER UNE CANDIDATURE ==========
        System.out.println("--- Test 11 : Supprimer une candidature ---");
        candidatureService.supprimer(1);
        System.out.println();
        */

        System.out.println("========== FIN DES TESTS ==========");
    }
}