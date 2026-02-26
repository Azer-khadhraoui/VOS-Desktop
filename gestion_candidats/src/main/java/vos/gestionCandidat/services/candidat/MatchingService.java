package vos.gestionCandidat.services.candidat;

import vos.gestionCandidat.entities.MatchResult;
import vos.gestionCandidat.entities.PreferenceCandidature;
import vos.gestionCandidat.services.OffreEmploiServiceTemp;
import vos.gestionCandidat.services.OffreEmploiServiceTemp.OffreAvecCriteres;

import java.util.*;
import java.util.stream.Collectors;

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

    private final OffreEmploiServiceTemp offreService;
    private final PreferenceCandidatureService preferenceService;

    public MatchingService() {
        this.offreService      = new OffreEmploiServiceTemp();
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
        List<OffreAvecCriteres> offres = offreService.getAllOffresOuvertes();

        // 4. Calculer le score pour chaque offre
        List<MatchResult> resultats = new ArrayList<>();
        for (OffreAvecCriteres offre : offres) {
            MatchResult match = scorerPreferenceVsOffre(preference, offre);
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

        OffreAvecCriteres offre = offreService.getOffreById(idOffre);
        if (offre == null) return null;

        return scorerPreferenceVsOffre(preference, offre);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  LOGIQUE DE SCORING
    // ═══════════════════════════════════════════════════════════════════════════

    private MatchResult scorerPreferenceVsOffre(PreferenceCandidature preference,
                                                 OffreAvecCriteres offre) {
        MatchResult match = new MatchResult(
                offre.idOffre,
                offre.titre,
                offre.typeContrat,
                offre.description
        );

        // ── Critère 1 : Type de poste souhaité (40%) ──────────────────────────
        match.setScoreTypePoste(scorerTypePoste(preference, offre));

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
    private int scorerTypePoste(PreferenceCandidature preference, OffreAvecCriteres offre) {
        if (preference.getTypePosteSouhaite() == null) return 0;

        String titreLower       = offre.titre               != null ? offre.titre.toLowerCase()               : "";
        String descLower        = offre.description         != null ? offre.description.toLowerCase()         : "";
        String competencesLower = offre.competencesRequises != null ? offre.competencesRequises.toLowerCase() : "";

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
    private int scorerContrat(PreferenceCandidature preference, OffreAvecCriteres offre) {
        if (preference.getTypeContratSouhaite() == null || offre.typeContrat == null) {
            return 50; // Pas d'info → score neutre
        }

        String contratSouhaite = preference.getTypeContratSouhaite().toUpperCase().trim();
        String contratOffre    = offre.typeContrat.toUpperCase().trim();

        if (contratSouhaite.equals(contratOffre))                         return 100; // Parfait
        if (contratSouhaite.contains("CDI") && contratOffre.contains("CDD")) return 40;  // CDD moins bien
        if (contratSouhaite.contains("CDD") && contratOffre.contains("CDI")) return 60;  // CDI > CDD = acceptable
        if (contratSouhaite.contains("STAGE") && contratOffre.contains("STAGE")) return 100;

        return 0; // Types incompatibles
    }

    // ─── Critère 3 : Mode de travail (15%) ────────────────────────────────────
    private int scorerModeTravail(PreferenceCandidature preference, OffreAvecCriteres offre) {
        if (preference.getModeTravail() == null) return 50;

        String modeLower = preference.getModeTravail().toLowerCase();
        String descLower = offre.description != null ? offre.description.toLowerCase() : "";

        boolean veutTeletravail  = modeLower.contains("télétravail") || modeLower.contains("teletravail") || modeLower.contains("remote");
        boolean veutPresentiel   = modeLower.contains("présentiel")  || modeLower.contains("presentiel");
        boolean veutHybride      = modeLower.contains("hybride");

        boolean offreTeletravail = descLower.contains("télétravail") || descLower.contains("teletravail") || descLower.contains("remote");
        boolean offrePresentiel  = descLower.contains("présentiel")  || descLower.contains("presentiel");
        boolean offreHybride     = descLower.contains("hybride");

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
    private int scorerMobilite(PreferenceCandidature preference, OffreAvecCriteres offre) {
        if (preference.getMobiliteGeographique() == null) return 50;

        String mobilite  = preference.getMobiliteGeographique().toLowerCase();
        String descLower = offre.description != null ? offre.description.toLowerCase() : "";

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