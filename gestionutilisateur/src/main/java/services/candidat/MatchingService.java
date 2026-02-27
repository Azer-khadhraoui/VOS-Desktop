package services.candidat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import entities.CritereOffre;
import entities.MatchResult;
import entities.OffreEmploi;
import entities.PreferenceCandidature;
import services.CritereOffreService;
import services.OffreEmploiService;

/**
 * Service de Matching / Scoring automatique.
 *
 * Compare UNIQUEMENT les préférences du candidat avec les offres ouvertes.
 * La Candidature n'est plus nécessaire pour le scoring.
 *
 * PONDÉRATION :
 *   - Type de poste souhaité   : 40%
 *   - Type de contrat souhaité : 30%
 *   - Mode de travail          : 15%
 *   - Disponibilité            : 10%
 *   - Mobilité géographique    :  5%
 */
public class MatchingService {

    private final OffreEmploiService offreService;  // ✅ REMPLACEMENT
    private final CritereOffreService critereService; 
    private final PreferenceCandidatureService preferenceService;

    public MatchingService() {
        this.offreService = new OffreEmploiService();  // ✅ CHANGÉ
        this.critereService = new CritereOffreService();
        this.preferenceService = new PreferenceCandidatureService();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  MÉTHODE PRINCIPALE
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Calcule les scores de matching pour un utilisateur donné
     * en se basant UNIQUEMENT sur ses préférences.
     *
     * @param idUtilisateur L'ID de l'utilisateur connecté
     * @return Liste de MatchResult triée par score décroissant,
     *         ou liste vide si l'utilisateur n'a pas de préférences
     */
    public List<MatchResult> calculerMatchsPourUtilisateur(int idUtilisateur) {

        // 1. Récupérer les préférences de l'utilisateur
        PreferenceCandidature preference = preferenceService.getByIdUtilisateur(idUtilisateur);

        // 2. Pas de préférences → impossible de calculer le matching
        if (preference == null) {
            return Collections.emptyList();
        }

        // 3. Récupérer toutes les offres ouvertes avec leurs critères
         List<OffreEmploi> toutesLesOffres = offreService.getAllOffres();
        List<OffreEmploi> offresOuvertes = toutesLesOffres.stream()
                .filter(o -> "Actif".equalsIgnoreCase(o.getStatutOffre()) || 
                           "Ouverte".equalsIgnoreCase(o.getStatutOffre()))
                .collect(Collectors.toList());

        // 4. Calculer le score pour chaque offre
        List<MatchResult> resultats = new ArrayList<>();
        for (OffreEmploi offre : offresOuvertes) {
            // ✅ Récupérer les critères pour cette offre
            List<CritereOffre> criteres = critereService.getByOffreId(offre.getIdOffre());
            
            MatchResult match = scorerPreferenceVsOffre(preference, offre, criteres);
            resultats.add(match);
        }

        // 5. Trier par score décroissant (meilleur match en premier)
        resultats.sort(Comparator.comparingInt(MatchResult::getScoreTotal).reversed());

        return resultats;
    }

    /**
     * Calcule le score entre les préférences d'un utilisateur et UNE offre spécifique.
     * Utilisé par exemple pour afficher le score sur la page de détail d'une offre.
     */
    public MatchResult calculerMatchPourUneOffre(int idUtilisateur, int idOffre) {
        PreferenceCandidature preference = preferenceService.getByIdUtilisateur(idUtilisateur);
        if (preference == null) return null;

        // ✅ Récupérer l'offre depuis OffreEmploiService
        OffreEmploi offre = offreService.getOffreById(idOffre);
        if (offre == null) return null;

        // ✅ Récupérer les critères pour cette offre
        List<CritereOffre> criteres = critereService.getByOffreId(idOffre);

        return scorerPreferenceVsOffre(preference, offre, criteres);
    }
    

    // ═══════════════════════════════════════════════════════════════════════════
    //  LOGIQUE DE SCORING
    // ═══════════════════════════════════════════════════════════════════════════

      private MatchResult scorerPreferenceVsOffre(PreferenceCandidature preference,
                                                 OffreEmploi offre,
                                                 List<CritereOffre> criteres) {
        
        MatchResult match = new MatchResult(
                offre.getIdOffre(),
                offre.getTitre(),
                offre.getTypeContrat(),
                offre.getDescription()
        );

        // ── Critère 1 : Type de poste souhaité (40%) ──────────────────────────
        match.setScoreTypePoste(scorerTypePoste(preference, offre, criteres));

        // ── Critère 2 : Type de contrat souhaité (30%) ────────────────────────
        match.setScoreContrat(scorerContrat(preference, offre));

        // ── Critère 3 : Mode de travail (15%) ─────────────────────────────────
        match.setScoreModeTravail(scorerModeTravail(preference, offre));

        // ── Critère 4 : Disponibilité (10%) ───────────────────────────────────
        match.setScoreDisponibilite(scorerDisponibilite(preference));

        // ── Critère 5 : Mobilité géographique (5%) ────────────────────────────
        match.setScoreMobilite(scorerMobilite(preference, offre));

        // ── Score final pondéré ───────────────────────────────────────────────
        match.calculerScoreTotal();

        return match;
    }


    // ─── Critère 1 : Type de poste souhaité (40%) ─────────────────────────────
     private int scorerTypePoste(PreferenceCandidature preference, 
                                OffreEmploi offre,
                                List<CritereOffre> criteres) {
        if (preference.getTypePosteSouhaite() == null) return 0;

        String titreLower       = offre.getTitre() != null ? offre.getTitre().toLowerCase() : "";
        String descLower        = offre.getDescription() != null ? offre.getDescription().toLowerCase() : "";
        
        // ✅ NOUVEAU : Récupérer les compétences requises du CritereOffre
        String competencesLower = "";
        if (criteres != null && !criteres.isEmpty()) {
            CritereOffre critere = criteres.get(0);  // Prendre le premier critère
            if (critere.getCompetencesRequises() != null) {
                competencesLower = critere.getCompetencesRequises().toLowerCase();
            }
        }

        List<String> motsPoste = extraireMots(preference.getTypePosteSouhaite());

        int score = 0;
        for (String mot : motsPoste) {
            if (titreLower.contains(mot)) {
                score = 100; // Mot trouvé dans le TITRE → score maximum
                break;
            }
            if (descLower.contains(mot) || competencesLower.contains(mot)) {
                score = Math.max(score, 60); // Trouvé dans description ou compétences
            }
        }
        return score;
    }
    // ─── Critère 2 : Type de contrat souhaité (30%) ───────────────────────────
    private int scorerContrat(PreferenceCandidature preference, OffreEmploi offre) {
        if (preference.getTypeContratSouhaite() == null || offre.getTypeContrat() == null) {
            return 50; // Pas d'info → score neutre
        }

        String contratSouhaite = preference.getTypeContratSouhaite().toUpperCase().trim();
        String contratOffre    = offre.getTypeContrat().toUpperCase().trim();

        if (contratSouhaite.equals(contratOffre))                         return 100; // Parfait
        if (contratSouhaite.contains("CDI") && contratOffre.contains("CDD")) return 40;  // CDD moins bien
        if (contratSouhaite.contains("CDD") && contratOffre.contains("CDI")) return 60;  // CDI > CDD = acceptable
        if (contratSouhaite.contains("STAGE") && contratOffre.contains("STAGE")) return 100;

        return 0; // Types incompatibles
    }

    // ─── Critère 3 : Mode de travail (15%) ────────────────────────────────────
    private int scorerModeTravail(PreferenceCandidature preference, OffreEmploi offre) {
        if (preference.getModeTravail() == null) return 50;

        String modeLower = preference.getModeTravail().toLowerCase();
        String descLower = offre.getDescription() != null ? offre.getDescription().toLowerCase() : "";
        
        // ✅ BONUS : Vérifier le workPreference de l'offre
        String workPrefLower = offre.getWorkPreference() != null ? offre.getWorkPreference().toLowerCase() : "";

        boolean veutTeletravail  = modeLower.contains("télétravail") || modeLower.contains("teletravail") || modeLower.contains("remote");
        boolean veutPresentiel   = modeLower.contains("présentiel")  || modeLower.contains("presentiel");
        boolean veutHybride      = modeLower.contains("hybride");

        boolean offreTeletravail = descLower.contains("télétravail") || descLower.contains("teletravail") || 
                                   descLower.contains("remote") || workPrefLower.contains("remote");
        boolean offrePresentiel  = descLower.contains("présentiel")  || descLower.contains("presentiel") || 
                                   workPrefLower.contains("on-site");
        boolean offreHybride     = descLower.contains("hybride") || workPrefLower.contains("hybrid");

        if (veutTeletravail && offreTeletravail) return 100;
        if (veutPresentiel  && offrePresentiel)  return 100;
        if (veutHybride     && offreHybride)     return 100;
        if (veutHybride)                          return 60; // Hybride = flexible
        if (veutTeletravail && offreHybride)      return 50; // Partiel acceptable

        return 20; // Mode non précisé ou incompatible
    }

    // ─── Critère 4 : Disponibilité (10%) ──────────────────────────────────────
    private int scorerDisponibilite(PreferenceCandidature preference) {
        if (preference.getDisponibilite() == null) return 50;

        String dispo = preference.getDisponibilite().toLowerCase();

        if (dispo.contains("immédiat") || dispo.contains("immediat")) return 100;
        if (dispo.contains("1 mois")   || dispo.contains("un mois"))  return 80;
        if (dispo.contains("2 mois")   || dispo.contains("deux"))     return 70;
        if (dispo.contains("3 mois")   || dispo.contains("trois"))    return 50;
        if (dispo.contains("6 mois"))                                  return 20;

        return 50;
    }

    // ─── Critère 5 : Mobilité géographique (5%) ───────────────────────────────
   private int scorerMobilite(PreferenceCandidature preference, OffreEmploi offre) {
        if (preference.getMobiliteGeographique() == null) return 50;

        String mobilite  = preference.getMobiliteGeographique().toLowerCase();
        String descLower = offre.getDescription() != null ? offre.getDescription().toLowerCase() : "";
        
        // ✅ BONUS : Vérifier le lieu de l'offre
        String lieuLower = offre.getLieu() != null ? offre.getLieu().toLowerCase() : "";

        // Mobile → compatible avec tout
        if (mobilite.contains("oui") || mobilite.contains("national") || mobilite.contains("international")) {
            return 100;
        }

        // Pas mobile mais offre en télétravail → OK
        if ((mobilite.contains("non") || mobilite.contains("local")) && descLower.contains("télétravail")) {
            return 80;
        }

        // Pas mobile, offre en présentiel → risque
        if (mobilite.contains("non") || mobilite.contains("local")) {
            return 30;
        }

        return 50;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  UTILITAIRES
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Extrait les mots significatifs (> 3 lettres) d'une chaîne, en minuscule.
     */
     private List<String> extraireMots(String texte) {
        if (texte == null) return Collections.emptyList();
        return Arrays.stream(texte.toLowerCase().split("[\\s,/\\-]+"))
                .filter(m -> m.length() > 3)
                .collect(Collectors.toList());
    }
}