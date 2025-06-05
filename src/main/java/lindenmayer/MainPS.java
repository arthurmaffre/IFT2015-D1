package lindenmayer;

import java.io.FileReader;
import java.io.PrintWriter;
import java.awt.geom.Point2D;
import java.util.Map;
import java.util.LinkedHashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * MainPS.java : calcule l’inférence directement en PostScript (EPS).
 * On remplace tous les '[' par LeftBracket et tous les ']' par RightBracket,
 * afin d’éviter toute ambiguïté et l’erreur /unmatchedmark.
 */
public class MainPS {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("usage: java -jar lindenmayer.jar <fichier.json> <iterations>");
            System.exit(1);
        }
        String jsonFile = args[0];
        int iterations = Integer.parseInt(args[1]);
        JSONObject spec = new JSONObject(new JSONTokener(new FileReader(jsonFile)));

        //
        // 1) Paramètres facultatifs : "parameters" → step, angle, start
        //
        JSONObject params = spec.optJSONObject("parameters");
        double step = 1.0;
        double angle = 25.0;
        Point2D.Double startPos = new Point2D.Double(0, 0);
        double startAngle = 90.0;
        if (params != null) {
            if (params.has("step"))
                step = params.getDouble("step");
            if (params.has("angle"))
                angle = params.getDouble("angle");
            if (params.has("start")) {
                JSONArray arr = params.getJSONArray("start");
                double x = arr.getDouble(0);
                double y = arr.getDouble(1);
                double th = arr.getDouble(2);
                startPos = new Point2D.Double(x, y);
                startAngle = th;
            }
        }

        //
        // 2) Construire la map <Symbole → première expansion> :
        //
        JSONObject jsonRules = spec.getJSONObject("rules");
        Map<Character, String> firstRule = new LinkedHashMap<>();
        for (String key : jsonRules.keySet()) {
            char lhs = key.charAt(0);
            JSONArray expansions = jsonRules.getJSONArray(key);
            if (!expansions.isEmpty()) {
                firstRule.put(lhs, expansions.getString(0));
            }
        }

        //
        // 3) Construire la map <Symbole → action Turtle> :
        //
        JSONObject jsonActions = spec.getJSONObject("actions");
        Map<Character, String> actions = new LinkedHashMap<>();
        for (String code : jsonActions.keySet()) {
            char sym = code.charAt(0);
            String act = jsonActions.getString(code);
            actions.put(sym, act);
        }

        //
        // 4) Axiome de départ (chaîne de caractères) :
        //
        String axiom = spec.getString("axiom");

        //
        // 5) Écrire l’en-tête du fichier EPS :
        //
        PrintWriter out = new PrintWriter(System.out, true);
        out.println("%!PS-Adobe-3.0 EPSF-3.0");
        out.printf("%%%%Title: (%s)%n", jsonFile);
        out.println("%%Creator: (IFT2015 - Projet 1, L-system en PostScript)");
        out.println("%%BoundingBox: (atend)");
        out.println("%%LanguageLevel: 2");
        out.println("%%EndComments");
        out.println();

        //
        // 6) Variables globales en PostScript : step et angle
        //
        out.printf("/step  %f def%n", step);
        out.printf("/angle %f def%n%n", angle);

        //
        // 7) Définition des actions Tortue en PostScript :
        //
        out.println("/T:draw  {  0 step rlineto  } def");
        out.println("/T:move  {  0 step rmoveto } def");
        out.println("/T:turnL {  angle rotate     } def");
        out.println("/T:turnR {  -angle rotate    } def");
        out.println("/T:push  {  gsave            } def");
        out.println("/T:pop   {  grestore         } def");
        out.println();

        //
        // 8) Créer LEFTBRACKET / RIGHTBRACKET à la place de "[" et "]" :
        //
        out.println("/LeftBracket {");
        out.println("  dup 0 eq");
        out.println("    { T:push  }"); // si profondeur==0 → empiler l’état
        out.println("    { pop     }"); // sinon → consommer “d” sans rien faire
        out.println("  ifelse");
        out.println("} def\n");

        out.println("/RightBracket {");
        out.println("  dup 0 eq");
        out.println("    { T:pop   }"); // si profondeur==0 → dépiler (pop)
        out.println("    { pop     }"); // sinon → consommer “d” sans rien faire
        out.println("  ifelse");
        out.println("} def\n");

        //
        // 9) Pour chaque symbole S ayant une règle, on génère en PostScript :
        // /S { dup 0 eq { <action> } { /d exch def /d d 1 sub def <boucle récursive> }
        // ifelse } def
        //
        for (Map.Entry<Character, String> entry : firstRule.entrySet()) {
            char sym = entry.getKey();
            String expansion = entry.getValue();

            out.printf("/%c {%n", sym);
            out.println("  dup 0 eq");
            out.println("    {");
            // si d == 0, on exécute l’action associée
            String act = actions.getOrDefault(sym, null);
            if (act != null) {
                switch (act.toLowerCase()) {
                    case "draw" -> out.println("      T:draw");
                    case "move" -> out.println("      T:move");
                    case "turnl" -> out.println("      T:turnL");
                    case "turnr" -> out.println("      T:turnR");
                    case "push" -> out.println("      T:push");
                    case "pop" -> out.println("      T:pop");
                    case "stay" -> out.println("      pop");
                    default -> out.println("      pop");
                }
            } else {
                out.println("      pop");
            }
            out.println("    }");
            out.println("    {");
            out.println("      /d exch def");
            out.println("      /d d 1 sub def");

            // maintenant, pour chaque caractère c de l’expansion :
            for (char c : expansion.toCharArray()) {
                if (c == '[') {
                    // on appelle la procédure LeftBracket au lieu de “[”
                    out.println("      d LeftBracket");
                } else if (c == ']') {
                    // on appelle la procédure RightBracket au lieu de “]”
                    out.println("      d RightBracket");
                } else if (firstRule.containsKey(c) || actions.containsKey(c)) {
                    // appeler la procédure récursive sur “c”
                    out.printf("      d %c%n", c);
                }
                // sinon (caractère sans règle ni action) → on ignore
            }

            out.println("    } ifelse");
            out.println("} def\n");
        }

        //
        // 10) Pour chaque symbole A ayant une action MAIS PAS de règle :
        //
        for (Map.Entry<Character, String> entry : actions.entrySet()) {
            char sym = entry.getKey();
            if (firstRule.containsKey(sym)) {
                continue; // déjà défini par la boucle ci-dessus
            }
            out.printf("/%c {%n", sym);
            out.println("  dup 0 eq");
            out.println("    {");
            switch (entry.getValue().toLowerCase()) {
                case "draw" -> out.println("      T:draw");
                case "move" -> out.println("      T:move");
                case "turnl" -> out.println("      T:turnL");
                case "turnr" -> out.println("      T:turnR");
                case "push" -> out.println("      T:push");
                case "pop" -> out.println("      T:pop");
                case "stay" -> out.println("      pop");
                default -> out.println("      pop");
            }
            out.println("    }");
            out.println("    {");
            out.println("      pop"); // d>0, on consomme le “dup” sans rien faire
            out.println("    } ifelse");
            out.println("} def\n");
        }

        //
        // 11) Position de départ et orientation initiale de la tortue :
        //
        out.printf("%f %f translate   %% déplacer l’origine à (%.3f,%.3f)%n",
                startPos.x, startPos.y, startPos.x, startPos.y);
        out.printf("%f rotate       %% rotation initiale (%.3f°)%n", startAngle, startAngle);
        out.println("newpath 0 0 moveto\n");

        //
        // 12) Appel de l’axiome, en remplaçant toujours [ → LeftBracket, ] →
        // RightBracket :
        //
        out.printf("%d    %% profondeur initiale%n", iterations);
        for (char c : axiom.toCharArray()) {
            if (c == '[') {
                out.print("LeftBracket ");
            } else if (c == ']') {
                out.print("RightBracket ");
            } else {
                out.printf("%c ", c);
            }
        }
        out.println("\n\nstroke");

        //
        // 13) Fin du fichier EPS
        //
        out.println("%%Trailer");
        out.println("%%EOF");
        out.flush();
    }
}