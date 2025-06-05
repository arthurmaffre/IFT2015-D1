package lindenmayer;

import java.awt.geom.*;
import java.io.*;
import org.json.*;

import lindenmayer.FakeTurtle;
import lindenmayer.LSystem;
import lindenmayer.PostScriptTurtle;
import lindenmayer.Turtle;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("usage: java -jar lindenmayer.jar <file.json> <n>");
            System.exit(1);
        }
        String jsonFile = args[0];
        int n = Integer.parseInt(args[1]);
        JSONObject spec = new JSONObject(new JSONTokener(new FileReader(jsonFile)));

        // 1) Passe muette pour calculer le BoundingBox
        LSystem sysBBox = new LSystem();
        FakeTurtle dummy = new FakeTurtle(new Point2D.Double(0, 0), 90);
        sysBBox.initFromJson(spec, dummy);
        Rectangle2D bbox = sysBBox.tell(dummy, sysBBox.getAxiom(), n);

        // 2) Passe dessin EPS
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out, "UTF-8"))) {
            // En-tête EPS
            out.println("%!PS-Adobe-3.0 EPSF-3.0");
            out.printf("%%%%Title: (%s)%n", new File(jsonFile).getName());
            out.println("%%Creator: (IFT2015 - Projet 1)");
            //out.printf("%%%%BoundingBox: %d %d %d %d%n",
            //        (int) bbox.getMinX(), (int) bbox.getMinY(),
            //        (int) bbox.getMaxX(), (int) bbox.getMaxY());
            //out.println("%%EndComments");

            // Calcul de translation pour mettre le bbox à l'origine (ou centré)
            double transX = Math.abs(bbox.getMaxX() + bbox.getMinX())/2 + 306;
            double transY = -bbox.getMinY();
            out.printf("%.3f %.3f translate\n", transX, transY);

            // (optionnel) Scaling pour ajuster la taille à, par exemple, 500x500
            double width  = bbox.getMaxX() - bbox.getMinX();
            double height = bbox.getMaxY() - bbox.getMinY();
            double scaleX  = 1.0; // ex: 500.0 / Math.max(width, height)
            double scaleY = 1.0;
            if (width > 0 && height > 0) {
                // Mettre l'arbre à échelle de 500x500
                scaleX = 612.0 / (Math.abs(bbox.getMinX() - bbox.getMaxX()));
                scaleY = 792.0 / Math.abs(bbox.getMinY() - bbox.getMaxY());
                out.printf("%.3f %.3f scale\n", scaleX, scaleY);
            }

            // Système pour dessin
            LSystem sysDraw = new LSystem();
            Turtle turtle = new PostScriptTurtle(new Point2D.Double(0, 0), 90, out);
            sysDraw.initFromJson(spec, turtle);
            sysDraw.tell(turtle, sysDraw.getAxiom(), n);

            out.println("stroke");
            out.println("%%Trailer");
            out.println("%%BoundingBox: 97 0 320 341");
            out.println("%%EOF");
        }
    }
}