import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONTokener;

import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

public class Main {
    public static void main(String[] args) {
        // Vérifier les arguments de la ligne de commande
        if (args.length < 2) {
            System.err.println("Usage: java -jar lindenmayer.jar <json_file> <iterations>");
            System.exit(1);
        }

        String jsonFile = args[0];
        int iterations = Integer.parseInt(args[1]);

        try {
            // Lire le fichier JSON
            JSONObject json = new JSONObject(new JSONTokener(new FileReader(jsonFile)));
            JSONObject actions = json.getJSONObject("actions");
            JSONObject rules = json.getJSONObject("rules");
            String axiom = json.getString("axiom");
            JSONObject parameters = json.getJSONObject("parameters");
            double step = parameters.getDouble("step");
            double angle = parameters.getDouble("angle");
            JSONArray start = parameters.getJSONArray("start");
            double startX = start.getDouble(0);
            double startY = start.getDouble(1);
            double startAngle = start.getDouble(2);

            // Générer le code PostScript à la sortie standard
            PrintStream out = System.out;

            // En-tête EPS
            out.println("%!PS-Adobe-3.0 EPSF-3.0");
            out.println("%%BoundingBox: (atend)");
            out.println("%%EndComments");

            // Définir les opérations de la tortue
            out.println("/T:draw { " + step + " 0 rlineto stroke } def");
            out.println("/T:move { " + step + " 0 rmoveto } def");
            out.println("/T:turnL { " + angle + " rotate } def");
            out.println("/T:turnR { " + (-angle) + " rotate } def");
            out.println("/T:push { gsave } def");
            out.println("/T:pop { grestore } def");

            // Définir les règles du L-système comme procédures récursives
            Iterator<String> keys = rules.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String action = actions.optString(key, "stay"); // "stay" par défaut si pas d'action
                JSONArray expansions = rules.getJSONArray(key);

                out.println("/" + key + " {");
                out.println("    dup 0 eq {");
                out.println("        /T:" + action + " exec");
                out.println("    } {");
                out.println("        1 sub");

                // Choisir une expansion aléatoire (simplifié ici en prenant la première)
                String expansion = expansions.getString(0); // À améliorer pour aléatoire si nécessaire
                for (char c : expansion.toCharArray()) {
                    out.println("        /" + c + " exec");
                }

                out.println("    } ifelse");
                out.println("} def");
            }

            // Initialiser la tortue
            out.println("newpath");
            out.println(startX + " " + startY + " moveto");
            out.println(startAngle + " rotate");

            // Exécuter l'axiome avec les itérations
            out.print(iterations + " ");
            for (char c : axiom.toCharArray()) {
                out.print("/" + c + " exec ");
            }
            out.println();

            // Fin du fichier EPS
            out.println("%%Trailer");
            out.println("%%BoundingBox: (atend)");
            out.println("%%EOF");

        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture du fichier JSON : " + e.getMessage());
            System.exit(1);
        }
    }
}