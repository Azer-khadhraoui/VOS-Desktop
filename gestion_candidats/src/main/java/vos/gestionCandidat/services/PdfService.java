package vos.gestionCandidat.services;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;

import vos.gestionCandidat.entities.Candidature;

/**
 * Service pour générer des fichiers PDF pour les candidatures Utilise la
 * bibliothèque iText 7
 *
 * ⚠️ IMPORTANT : Ce service utilise les informations disponibles dans
 * Candidature et récupère les données utilisateur et offre depuis les services
 * correspondants
 */
public class PdfService {

    /**
     * Couleurs personnalisées pour le PDF
     */
    private static final com.itextpdf.kernel.colors.Color COLOR_HEADER
            = new com.itextpdf.kernel.colors.DeviceRgb(25, 25, 112); // Bleu marine (VOS)
    private static final com.itextpdf.kernel.colors.Color COLOR_SECTION
            = new com.itextpdf.kernel.colors.DeviceRgb(230, 230, 250); // Bleu très clair

    // Services utilisés pour récupérer les infos manquantes
    private UtilisateurService utilisateurService;
    private OffreEmploiService offreService;
    private PreferenceCandidatureService preferenceService;

    /**
     * Constructeur - initialise les services
     */
    public PdfService() {
        this.utilisateurService = new UtilisateurService();
        this.offreService = new OffreEmploiService();
        this.preferenceService = new PreferenceCandidatureService();
    }

    /**
     * Génère un PDF pour une candidature donnée
     *
     * @param candidature L'objet Candidature à convertir en PDF
     * @param outputFile Le fichier de sortie (destination du PDF)
     * @throws Exception Si une erreur survient lors de la génération
     */
    public void generateCandidaturePdf(Candidature candidature, File outputFile) throws Exception {
        try {
            // Créer un writer PDF pointant vers le fichier de sortie
            PdfWriter writer = new PdfWriter(outputFile);

            // Créer le document PDF
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Ajouter le logo
            addLogo(document);

            // Ajouter le titre principal
            addTitle(document);

            // Ajouter les sections
            addInfosCandidat(document, candidature);
            addInfosCandidature(document, candidature);
            addPreferences(document, candidature);

            // Ajouter le footer
            addFooter(document);

            // Fermer le document
            document.close();

            System.out.println("✅ PDF généré avec succès : " + outputFile.getAbsolutePath());

        } catch (Exception e) {
            throw new Exception("Erreur lors de la génération du PDF : " + e.getMessage(), e);
        }
    }

    /**
     * Ajoute le logo VOS en haut du document
     */
    private void addLogo(Document document) {
        try {
            // Chemin vers le logo dans les ressources
            URL logoUrl = getClass().getResource("/images/vos_logo.png");

            if (logoUrl != null) {
                ImageData imageData = ImageDataFactory.create(logoUrl);
                Image logo = new Image(imageData);

                // Redimensionner le logo (largeur : 100pt)
                logo.setWidth(100);
                logo.setAutoScale(true);

                // Centrer le logo
                Paragraph logoParagraph = new Paragraph()
                        .add(logo)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(20);

                document.add(logoParagraph);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Attention : Logo non trouvé - " + e.getMessage());
        }
    }

    /**
     * Ajoute le titre centré du document
     */
    private void addTitle(Document document) {
        Paragraph title = new Paragraph("FICHE OFFICIELLE DE CANDIDATURE")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(COLOR_HEADER)
                .setMarginBottom(20);

        document.add(title);
    }

    /**
     * Ajoute la section "Informations Candidat" Récupère les données depuis la
     * table utilisateur via le service
     */
    private void addInfosCandidat(Document document, Candidature candidature) {
        // En-tête de section
        Paragraph sectionTitle = new Paragraph("INFORMATIONS CANDIDAT")
                .setFontSize(12)
                .setBold()
                .setFontColor(com.itextpdf.kernel.colors.ColorConstants.WHITE)
                .setBackgroundColor(COLOR_HEADER)
                .setPadding(10)
                .setMarginBottom(10);

        document.add(sectionTitle);

        // Récupérer les infos utilisateur via le service
        UtilisateurService.UtilisateurInfo userInfo
                = utilisateurService.getUtilisateurInfo(candidature.getIdUtilisateur());

        // Tableau avec les informations
        Table table = new Table(2).setWidth(500);

        if (userInfo != null) {
            addTableRow(table, "Nom :", userInfo.nom);
            addTableRow(table, "Prénom :", userInfo.prenom);
            addTableRow(table, "Email :", userInfo.email);
        } else {
            addTableRow(table, "Nom :", "Non disponible");
            addTableRow(table, "Prénom :", "Non disponible");
            addTableRow(table, "Email :", "Non disponible");
        }

        // Ajouter le domaine d'expérience depuis Candidature
        addTableRow(table, "Domaine :",
                candidature.getDomaineExperience() != null
                ? candidature.getDomaineExperience()
                : "Non spécifié");

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    /**
     * Ajoute la section "Informations Candidature"
     */
    private void addInfosCandidature(Document document, Candidature candidature) {
        // En-tête de section
        Paragraph sectionTitle = new Paragraph("INFORMATIONS CANDIDATURE")
                .setFontSize(12)
                .setBold()
                .setFontColor(com.itextpdf.kernel.colors.ColorConstants.WHITE)
                .setBackgroundColor(COLOR_HEADER)
                .setPadding(10)
                .setMarginBottom(10);

        document.add(sectionTitle);

        // Récupérer le titre de l'offre
        String titreOffre = offreService.getTitreOffre(candidature.getIdOffre());

        // Tableau avec les informations
        Table table = new Table(2).setWidth(500);

        addTableRow(table, "ID Candidature :", String.valueOf(candidature.getIdCandidature()));
        addTableRow(table, "Date :",
                candidature.getDateCandidature() != null
                ? candidature.getDateCandidature().toString()
                : "Non disponible");
        addTableRow(table, "Statut :",
                candidature.getStatut() != null
                ? candidature.getStatut()
                : "En attente");
        addTableRow(table, "Poste Offert :", titreOffre);
        addTableRow(table, "Dernier Poste :",
                candidature.getDernierPoste() != null
                ? candidature.getDernierPoste()
                : "Non spécifié");
        addTableRow(table, "Niveau d'Expérience :",
                candidature.getNiveauExperience() != null
                ? candidature.getNiveauExperience()
                : "Non spécifié");
        addTableRow(table, "Années d'Expérience :",
                String.valueOf(candidature.getAnneesExperience()));

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    /**
     * Ajoute la section "Préférences" Récupère les préférences de l'utilisateur
     */
    private void addPreferences(Document document, Candidature candidature) {
        // En-tête de section
        Paragraph sectionTitle = new Paragraph("PRÉFÉRENCES DE CANDIDATURE")
                .setFontSize(12)
                .setBold()
                .setFontColor(com.itextpdf.kernel.colors.ColorConstants.WHITE)
                .setBackgroundColor(COLOR_HEADER)
                .setPadding(10)
                .setMarginBottom(10);

        document.add(sectionTitle);

        // Récupérer les préférences de l'utilisateur
        var preferences = preferenceService.getByUtilisateur(candidature.getIdUtilisateur());

        if (preferences != null && !preferences.isEmpty()) {
            Table table = new Table(2).setWidth(500);

            // En-têtes du tableau
            addTableHeader(table, "Critère");
            addTableHeader(table, "Valeur");

            // Remplir les préférences
            for (var pref : preferences) {
                addTableRow(table, "Type de Poste Souhaité :",
                        pref.getTypePosteSouhaite() != null ? pref.getTypePosteSouhaite() : "Non spécifié");
                addTableRow(table, "Mode de Travail :",
                        pref.getModeTravail() != null ? pref.getModeTravail() : "Non spécifié");
                addTableRow(table, "Disponibilité :",
                        pref.getDisponibilite() != null ? pref.getDisponibilite() : "Non spécifiée");
                addTableRow(table, "Mobilité Géographique :",
                        pref.getMobiliteGeographique() != null ? pref.getMobiliteGeographique() : "Non spécifiée");
                addTableRow(table, "Prêt au Déplacement :",
                        pref.getPretDeplacement() != null ? pref.getPretDeplacement() : "Non spécifié");
                addTableRow(table, "Type de Contrat Souhaité :",
                        pref.getTypeContratSouhaite() != null ? pref.getTypeContratSouhaite() : "Non spécifié");
                addTableRow(table, "Prétention Salariale :",
                        pref.getPretentionSalariale() > 0 ? pref.getPretentionSalariale() + " €" : "Non communiquée");
                addTableRow(table, "Date de Disponibilité :",
                        pref.getDateDisponibilite() != null ? pref.getDateDisponibilite().toString() : "Non spécifiée");
            }

            document.add(table);
        } else {
            Paragraph noPref = new Paragraph("Aucune préférence déclarée")
                    .setItalic()
                    .setMarginLeft(10);
            document.add(noPref);
        }

        document.add(new Paragraph("\n"));
    }

    /**
     * Ajoute le footer (date de génération et mention légale)
     */
    private void addFooter(Document document) {
        // Séparateur
        Paragraph separator = new Paragraph("_".repeat(80))
                .setFontSize(8)
                .setFontColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY)
                .setMarginTop(20);

        document.add(separator);

        // Date de génération
        String generationDate = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        Paragraph footer = new Paragraph("Document généré automatiquement le " + generationDate)
                .setFontSize(9)
                .setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10);

        document.add(footer);
    }

    /**
     * Ajoute une ligne dans un tableau
     */
    private void addTableRow(Table table, String label, String value) {
        table.addCell(createCell(label, true));
        table.addCell(createCell(value, false));
    }

    /**
     * Ajoute un en-tête de colonne dans un tableau
     */
    private void addTableHeader(Table table, String header) {
        table.addCell(createHeaderCell(header));
    }

    /**
     * Crée une cellule pour le tableau
     */
    private com.itextpdf.layout.element.Cell createCell(String content, boolean isLabel) {
        com.itextpdf.layout.element.Cell cell
                = new com.itextpdf.layout.element.Cell()
                        .add(new Paragraph(content))
                        .setPadding(8);

        if (isLabel) {
            cell.setBackgroundColor(COLOR_SECTION)
                    .setBold();
        }

        return cell;
    }

    /**
     * Crée une cellule d'en-tête pour le tableau
     */
    private com.itextpdf.layout.element.Cell createHeaderCell(String content) {
        return new com.itextpdf.layout.element.Cell()
                .add(new Paragraph(content).setBold())
                .setBackgroundColor(COLOR_HEADER)
                .setFontColor(com.itextpdf.kernel.colors.ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(8);
    }
}
