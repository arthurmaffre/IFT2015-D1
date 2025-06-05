package lindenmayer;

import org.json.JSONObject;
import org.json.JSONTokener;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.FileReader;

/**
 * Programme principal Swing pour afficher un L‐system à l'écran.
 * Usage : java -cp target/projet‐arbre-1.0-SNAPSHOT.jar lindenmayer.MainSwing
 * <fichier.json> <iterations>
 */
public class MainSwing {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("usage: java -jar lindenmayer.jar <fichier.json> <iterations>");
            System.exit(1);
        }

        String jsonFile = args[0];
        int n = Integer.parseInt(args[1]);

        // 1) Chargement du JSON
        JSONObject spec = new JSONObject(new JSONTokener(new FileReader(jsonFile)));

        // 2) Calcul muet du bounding‐box (avec FakeTurtle)
        LSystem sysBBox = new LSystem();
        FakeTurtle dummy = new FakeTurtle(new Point2D.Double(0, 0), 90);
        sysBBox.initFromJson(spec, dummy);
        Rectangle2D bbox = sysBBox.tell(dummy, sysBBox.getAxiom(), n);

        // 3) Créer et montrer la fenêtre Swing (sur le thread EDT)
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("L‐System Viewer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            DrawingPanel drawingPanel = new DrawingPanel(spec, n, bbox);
            frame.getContentPane().add(drawingPanel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    /**
     * JPanel qui contient un BufferedImage de la taille du bounding‐box, dessine le
     * L‐system dedans, puis l'affiche.
     */
    private static class DrawingPanel extends JPanel {
        private final BufferedImage canvas;

        DrawingPanel(JSONObject spec, int iterations, Rectangle2D bbox) {
            // Convertir bounding‐box en dimensions entières (points ≃ pixels)
            int imgW = (int) Math.ceil(bbox.getWidth());
            int imgH = (int) Math.ceil(bbox.getHeight());
            canvas = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);

            // Créer un Graphics2D à partir du BufferedImage
            Graphics2D g2 = canvas.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 1) Fond blanc
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, imgW, imgH);

            // 2) Couleur de dessin noire
            g2.setColor(Color.BLACK);

            // 3) Traduction + inversion Y
            // - translate(-xmin, +ymax) pour que le coin inférieur‐gauche du bbox devienne
            // (0,0)
            // - puis scale(1, -1) pour que l'axe Y monte vers le haut
            g2.translate(-bbox.getMinX(), bbox.getMaxY());
            g2.scale(1, -1);

            // 4) Créer la tortue Swing attachée à ce Graphics2D
            SwingTurtle turtle = new SwingTurtle(g2, 0, 0, 90);

            // 5) Créer un nouveau LSystem pour dessiner
            LSystem sysDraw = new LSystem();
            sysDraw.initFromJson(spec, turtle);

            // 6) Lancer le tracé (itérations données)
            sysDraw.tell(turtle, sysDraw.getAxiom(), iterations);

            // 7) Libérer le Graphics2D
            g2.dispose();

            // Définir la taille du panel pour que Swing lui donne exactement cette
            // dimension
            setPreferredSize(new Dimension(imgW, imgH));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Affiche simplement le BufferedImage pré‐dessiné
            g.drawImage(canvas, 0, 0, null);
        }
    }
}