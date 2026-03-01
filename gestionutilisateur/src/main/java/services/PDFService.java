package services;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import entities.Recrutement;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import entities.Utilisateur;

public class PDFService {
    private static PDFService instance;

    private PDFService() {
    }

    public static synchronized PDFService getInstance() {
        if (instance == null) {
            instance = new PDFService();
        }
        return instance;
    }

    public File generateTemporaryContractPDF(Recrutement rec, Map<String, String> ctx,
            boolean includeEmployeeSignature) {
        try {
            File tempFile = File.createTempFile("contrat_" + rec.getId_recrutement() + "_", ".pdf");
            tempFile.deleteOnExit();

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                Document doc = new Document(PageSize.A4, 35, 35, 40, 40);
                PdfWriter.getInstance(doc, fos);
                doc.open();

                // Professional Fonts
                Font fontTitle = new Font(Font.HELVETICA, 18, Font.BOLD, java.awt.Color.BLACK);
                Font fontArticle = new Font(Font.HELVETICA, 11, Font.BOLD, java.awt.Color.BLACK);
                Font fontBody = new Font(Font.TIMES_ROMAN, 11, Font.NORMAL, java.awt.Color.BLACK);
                Font fontSubtitle = new Font(Font.HELVETICA, 9, Font.NORMAL,
                        new java.awt.Color(0x6b, 0x7d, 0x8c));
                Font fontSmallGray = new Font(Font.HELVETICA, 8, Font.NORMAL,
                        new java.awt.Color(0x9c, 0xa3, 0xaf));
                Font fontLabel = new Font(Font.HELVETICA, 10, Font.BOLD,
                        new java.awt.Color(0x1f, 0x29, 0x37));

                // Add professional header with logo
                addProfessionalHeader(doc, fontTitle, fontSubtitle);

                // Add introduction paragraph
                addIntroductionParagraph(doc, fontBody, rec, ctx);

                // Add 5 Articles
                addArticle1(doc, fontArticle, fontBody, ctx);
                addArticle2(doc, fontArticle, fontBody, ctx);
                addArticle3(doc, fontArticle, fontBody, ctx, rec);
                addArticle4(doc, fontArticle, fontBody, ctx);
                addArticle5(doc, fontArticle, fontBody, ctx);

                // Add signature block
                addSignatureBlock(doc, fontLabel, fontBody, fontSmallGray, rec,
                        includeEmployeeSignature);

                doc.close();
            }
            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addProfessionalHeader(Document doc, Font fontTitle, Font fontSubtitle) throws DocumentException {
        try {
            PdfPTable headerTable = new PdfPTable(1);
            headerTable.setWidthPercentage(100);
            PdfPCell headerCell = new PdfPCell();
            headerCell.setBackgroundColor(new java.awt.Color(0x1f, 0x29, 0x37));
            headerCell.setPadding(12);
            headerCell.setBorder(Rectangle.NO_BORDER);

            try {
                // Adjusted logo path to match gestionutilisateur resources
                String logoPath = "src/main/resources/images/VOSwhiteslogan.png";
                Image logo = Image.getInstance(logoPath);
                logo.setAlignment(Element.ALIGN_CENTER);
                logo.scaleToFit(270, 105);
                Paragraph logoPara = new Paragraph();
                logoPara.add(new Chunk(logo, 0, 0));
                logoPara.setAlignment(Element.ALIGN_CENTER);
                headerCell.addElement(logoPara);
            } catch (Exception ex) {
                Paragraph companyName = new Paragraph("VOS – VOTRE OUTIL DE SUCCÈS", fontTitle);
                companyName.setAlignment(Element.ALIGN_CENTER);
                companyName.setFont(new Font(Font.HELVETICA, 16, Font.BOLD, java.awt.Color.WHITE));
                headerCell.addElement(companyName);
            }

            headerTable.addCell(headerCell);
            doc.add(headerTable);

            Paragraph title = new Paragraph("CONTRAT DE TRAVAIL", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(10);
            title.setSpacingAfter(2);
            doc.add(title);

            Paragraph dateGen = new Paragraph("Généré le " +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM yyyy",
                            java.util.Locale.FRANCE)),
                    fontSubtitle);
            dateGen.setAlignment(Element.ALIGN_CENTER);
            dateGen.setSpacingAfter(5);
            doc.add(dateGen);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addIntroductionParagraph(Document doc, Font fontBody, Recrutement rec, Map<String, String> ctx)
            throws DocumentException {
        String candidateName = ctx.getOrDefault("candidate_name", "Candidat");
        String intro = "Entre les soussignés,\n" +
                "La société VOS – Votre Outil de Succès, représentée par ses organes de direction légaux et "
                + "dûment habilitée, d'une part,\n" +
                "Et l'employé(e) " + candidateName + " référencé sous le N° de recrutement #"
                + rec.getId_recrutement()
                + ", d'autre part,\n\n" +
                "Il a été librement et consciemment convenu ce qui suit :";

        Paragraph introPara = new Paragraph(intro, fontBody);
        introPara.setAlignment(Element.ALIGN_JUSTIFIED);
        introPara.setLeading(12f);
        introPara.setSpacingAfter(4);
        doc.add(introPara);
    }

    private void addArticle1(Document doc, Font fontArticle, Font fontBody, Map<String, String> ctx)
            throws DocumentException {
        Paragraph article = new Paragraph();
        article.add(new Chunk("Article 1 – Nature du Contrat", fontArticle));
        article.setSpacingBefore(3);
        article.setSpacingAfter(2);
        doc.add(article);

        String type = ctx.getOrDefault("job_contract", "CDI");
        String content = "Le présent contrat est établi en tant que contrat " + type
                + ". Il définit les obligations réciproques et les conditions d'emploi applicables.";
        Paragraph text = new Paragraph(content, fontBody);
        text.setAlignment(Element.ALIGN_JUSTIFIED);
        text.setLeading(11f);
        text.setSpacingAfter(3);
        doc.add(text);
    }

    private void addArticle2(Document doc, Font fontArticle, Font fontBody, Map<String, String> ctx)
            throws DocumentException {
        Paragraph article = new Paragraph();
        article.add(new Chunk("Article 2 – Durée du Contrat", fontArticle));
        article.setSpacingBefore(3);
        article.setSpacingAfter(2);
        doc.add(article);

        String dateDebut = ctx.getOrDefault("date_debut", LocalDate.now().toString());
        String dateFin = ctx.getOrDefault("date_fin", "non définie");
        String periode = ctx.getOrDefault("periode", "0 mois");

        String content = "La date de début de l'employement est fixée au " + dateDebut
                + ". La date de fin du contrat est prévue au " + dateFin
                + ". La période couverte par le présent contrat est : " + periode + ".";
        Paragraph text = new Paragraph(content, fontBody);
        text.setAlignment(Element.ALIGN_JUSTIFIED);
        text.setLeading(11f);
        text.setSpacingAfter(3);
        doc.add(text);
    }

    private void addArticle3(Document doc, Font fontArticle, Font fontBody, Map<String, String> ctx,
            Recrutement rec) throws DocumentException {
        Paragraph article = new Paragraph();
        article.add(new Chunk("Article 3 – Rémunération", fontArticle));
        article.setSpacingBefore(3);
        article.setSpacingAfter(2);
        doc.add(article);

        String salaire = ctx.getOrDefault("salaire", "300.00 DT");
        String content = "La rémunération brute mensuelle garantie est fixée à " + salaire
                + " (Dinars Tunisiens). Cette rémunération est payable selon les modalités légales en vigueur et comprend les contributions sociales obligatoires.";
        Paragraph text = new Paragraph(content, fontBody);
        text.setAlignment(Element.ALIGN_JUSTIFIED);
        text.setLeading(11f);
        text.setSpacingAfter(3);
        doc.add(text);
    }

    private void addArticle4(Document doc, Font fontArticle, Font fontBody, Map<String, String> ctx)
            throws DocumentException {
        Paragraph article = new Paragraph();
        article.add(new Chunk("Article 4 – Conditions de Travail", fontArticle));
        article.setSpacingBefore(3);
        article.setSpacingAfter(2);
        doc.add(article);

        String volume = ctx.getOrDefault("volume_horaire", "65");
        String status = ctx.getOrDefault("status", "Actif");
        String content = "Le volume horaire convenu est de " + volume
                + " heures par semaine. Le statut de l'employé(e) est défini comme : " + status
                + ". L'employé(e) accepte de respecter le règlement intérieur de la société et les dispositions légales en matière de droit du travail.";
        Paragraph text = new Paragraph(content, fontBody);
        text.setAlignment(Element.ALIGN_JUSTIFIED);
        text.setLeading(11f);
        text.setSpacingAfter(3);
        doc.add(text);
    }

    private void addArticle5(Document doc, Font fontArticle, Font fontBody, Map<String, String> ctx)
            throws DocumentException {
        Paragraph article = new Paragraph();
        article.add(new Chunk("Article 5 – Avantages et Bénéfices", fontArticle));
        article.setSpacingBefore(3);
        article.setSpacingAfter(2);
        doc.add(article);

        String avantages = ctx.getOrDefault("avantages", "Assurance Maladie");
        String content = "Les avantages sociaux accordés comprennent : " + avantages
                + " L'employeur s'engage à respecter les obligations légales en matière de couverture sociale et de congés payés.";
        Paragraph text = new Paragraph(content, fontBody);
        text.setAlignment(Element.ALIGN_JUSTIFIED);
        text.setLeading(11f);
        text.setSpacingAfter(3);
        doc.add(text);
    }

    private void addSignatureBlock(Document doc, Font fontLabel, Font fontBody, Font fontSmallGray, Recrutement rec,
            boolean includeEmployeeSignature) throws DocumentException {
        doc.add(new Paragraph(" "));
        Paragraph sigTitle = new Paragraph("SIGNATURES", fontLabel);
        sigTitle.setAlignment(Element.ALIGN_CENTER);
        doc.add(sigTitle);

        PdfPTable sigTable = new PdfPTable(2);
        sigTable.setWidthPercentage(100);
        sigTable.setSpacingBefore(10);

        // Company Signature
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.addElement(new Paragraph("Pour la société", fontLabel));
        try {
            Image sig = Image.getInstance("src/main/resources/images/signature.png");
            sig.scaleToFit(80, 40);
            leftCell.addElement(new Chunk(sig, 0, 0));
            Image cachet = Image.getInstance("src/main/resources/images/cachet.png");
            cachet.scaleToFit(90, 90);
            leftCell.addElement(new Chunk(cachet, 0, 0));
        } catch (Exception e) {
            leftCell.addElement(new Paragraph("[Signature et Cachet]", fontBody));
        }
        sigTable.addCell(leftCell);

        // Employee Signature
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        rightCell.addElement(new Paragraph("L'employé(e)", fontLabel));

        if (includeEmployeeSignature) {
            try {
                // Get User for signature
                ServiceUtilisateur serviceUser = new ServiceUtilisateur();
                Utilisateur user = serviceUser.getById(rec.getId_utilisateur());

                if (user != null) {
                    SignatureService sigService = SignatureService.getInstance();
                    byte[] sigBytes = sigService.getSignatureImageBytes(user);

                    if (sigBytes != null) {
                        Image clientSig = Image.getInstance(sigBytes);
                        clientSig.scaleToFit(120, 60);
                        Paragraph p = new Paragraph();
                        p.add(new Chunk(clientSig, 0, 0));
                        p.setSpacingBefore(10);
                        rightCell.addElement(p);
                    } else {
                        rightCell.addElement(new Paragraph("____________________", fontBody));
                    }
                } else {
                    rightCell.addElement(new Paragraph("____________________", fontBody));
                }
            } catch (Exception e) {
                System.err.println("Error loading dynamic signature: " + e.getMessage());
                rightCell.addElement(new Paragraph("____________________", fontBody));
            }
        } else {
            // Emplacement vide pour signature
            Paragraph p = new Paragraph("\n\n________________\n(Signature)", fontBody);
            p.setAlignment(Element.ALIGN_CENTER);
            rightCell.addElement(p);
        }

        rightCell.addElement(new Paragraph("N° Recrutement : #" + rec.getId_recrutement(), fontBody));
        sigTable.addCell(rightCell);

        doc.add(sigTable);

        // Electronic signature hash
        String electronicSig = String.format("%08X", (rec.getId_recrutement() + "VOS").hashCode())
                .toUpperCase();
        Paragraph hashPara = new Paragraph("Signature électronique : " + electronicSig, fontSmallGray);
        hashPara.setAlignment(Element.ALIGN_CENTER);
        hashPara.setSpacingBefore(20);
        doc.add(hashPara);
    }
}
