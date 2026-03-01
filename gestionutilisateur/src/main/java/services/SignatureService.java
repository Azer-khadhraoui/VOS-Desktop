package services;

import entities.Utilisateur;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;

public class SignatureService {

    private static SignatureService instance;

    private SignatureService() {
    }

    public static synchronized SignatureService getInstance() {
        if (instance == null) {
            instance = new SignatureService();
        }
        return instance;
    }

    /**
     * Generates a "Scribble" or "Loop" style signature image as a byte array.
     * This version uses smoother splines and varied stroke widths for a more
     * authentic look.
     */
    public byte[] getSignatureImageBytes(Utilisateur user) {
        String name = user.getNom() + " " + user.getPrenom();
        int width = 500;
        int height = 150;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // Ultra-high quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        // Transparent background
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, width, height);
        g2d.setComposite(AlphaComposite.SrcOver);

        // Use a random seed based on the user's name for consistency
        long seed = name.hashCode();
        Random rand = new Random(seed);

        // Professional ink color (slightly varied for realism)
        g2d.setColor(new Color(0, 31, 143, 230));

        // 1. Draw the initial letter (stylized cursive)
        Font initialFont = new Font("Brush Script MT", Font.ITALIC, 80);
        if (initialFont.getFamily().equals("Dialog")) {
            initialFont = new Font("Serif", Font.ITALIC, 80);
        }
        g2d.setFont(initialFont);
        String firstLetter = name.substring(0, 1).toUpperCase();
        g2d.drawString(firstLetter, 40, height / 2 + 30);

        // 2. Create the "Scribble/Loop" effect with SMOOTH curves
        Path2D.Double path = new Path2D.Double();

        // Start roughly after the initial letter
        double curX = 100 + rand.nextInt(20);
        double curY = height / 2.0 + (rand.nextDouble() - 0.5) * 20;
        path.moveTo(curX, curY);

        // Fewer loops but more complex curves for a better look
        int segments = 6 + rand.nextInt(4);
        double totalWidth = width - 150.0;
        double stepX = totalWidth / segments;

        for (int i = 0; i < segments; i++) {
            double nextX = curX + stepX;
            double nextY = height / 2.0 + (rand.nextDouble() - 0.5) * 30;

            // Large loops (paraphe style)
            double cp1x = curX + stepX * 0.2;
            double cp1y = curY - 40 - rand.nextInt(40);
            double cp2x = curX + stepX * 0.8;
            double cp2y = nextY + 40 + rand.nextInt(40);

            path.curveTo(cp1x, cp1y, cp2x, cp2y, nextX, nextY);

            curX = nextX;
            curY = nextY;
        }

        // 3. Add a final elegant underline flourish
        path.moveTo(60, height - 35.0);
        path.curveTo(width / 3.0, height - 15.0, 2 * width / 3.0, height - 50.0, width - 60, height - 25.0);

        // Draw with a varied stroke for "pen pressure" simulation
        g2d.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(path);

        // Subtle secondary stroke for "ink bleeding" effect
        g2d.setStroke(new BasicStroke(0.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(new Color(0, 31, 143, 100));
        g2d.draw(path);

        g2d.dispose();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Fallback URL generator (kept for compatibility)
     */
    public String getSignatureUrl(Utilisateur user) {
        String name = user.getNom() + " " + user.getPrenom();
        return "https://api.dicebear.com/7.x/initials/png?seed=" + name + "&fontFamily=Lucid%20Handwriting&chars=2";
    }
}
