package lindenmayer;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;

import org.json.JSONArray;
import org.json.JSONObject;



public class LSystem extends AbstractLSystem{

    /* ------------------------------------------------------------------
     * Structures internes
     * ------------------------------------------------------------------ */

    /** Interne: caractère -> instance unique de Symbol */
    private final Map<Character, Symbol> pool = new HashMap<>();

    /** Interne: symbol -> actions Turtle */
    private final Map<Symbol, Consumer<Turtle>> actions = new HashMap<>();

    /** Interne : règles de réécriture */

    private final Map<Symbol, List<List<Symbol>>> rules = new HashMap<>();

    /**Axiome courant (liste immuable) */
    private List<Symbol> axiom = List.of();

    /*
     * ------------------------------------------------------------------
     * Utilitaires internes
     * ------------------------------------------------------------------
     */

    /** Retourne l'instance unique pour le caractère {@code c}. */
    private Symbol intern(char c) {
        return pool.computeIfAbsent(c, Symbol::new);
    }

    /** Convertit une chaîne en liste de Symbol déjà internés. */
    private List<Symbol> toSymbolList(String s) {
        List<Symbol> out = new ArrayList<>(s.length());
        for (char c : s.toCharArray()) out.add(intern(c));
        return out;
    }

    /*
     * ------------------------------------------------------------------
     * Exécute, sur la tortue, l’action associée au symbole donné.
     * ------------------------------------------------------------------
     */
    @Override
    public void tell(Turtle turtle, Symbol sym) {
        // action enregistrée dans setAction(char, String)
        Consumer<Turtle> action = actions.get(sym);

        if (action != null) { // symbole non décoratif
            
            action.accept(turtle); // on applique l'action à la tortue
        }
        // sinon : symbole sans action -> on l’ignore simplement
    }

    @Override
    public Symbol setAction(char symChar, String actionStr) {
        Symbol sym = intern(symChar);

        if (actions.containsKey(sym))
            throw new IllegalArgumentException("Action déjà définie pour '" + symChar + "'");
            
        Consumer<Turtle> action;
        switch (actionStr.trim().toLowerCase()) {
            case "draw" -> action = Turtle::draw;
            case "move" -> action = Turtle::move;
            case "turnl" -> action = Turtle::turnL;
            case "turnr" -> action = Turtle::turnR;
            case "stay" -> action = Turtle::stay;
            case "push" -> action = Turtle::push;
            case "pop" -> action = Turtle::pop;
            default -> throw new IllegalArgumentException("Action inconnue : " + actionStr);
        }
        actions.put(sym, action);
        return sym;
    }

    /*
     * ------------------------------------------------------------------
     * Axiome
     * ------------------------------------------------------------------
     */

     @Override
     public void setAxiom(String str) {
        Objects.requireNonNull(str, "Axiome nul");
        this.axiom = List.copyOf(toSymbolList(str));
    }

    @Override
     public Iterator<Symbol> getAxiom() {
        return axiom.iterator();
    }

    @Override
    public Iterator<Symbol> rewrite(Symbol sym) {
        // récupère la liste d’expansions pour ce symbole
        List<List<Symbol>> candidates = rules.get(sym);

        // symbole terminal si aucune règle
        if (candidates == null || candidates.isEmpty()) {
            return null; // pas de réécriture
        }

        // choix aléatoire d’une expansion
        int k = rnd.nextInt(candidates.size()); // rnd vient d’AbstractLSystem
        return candidates.get(k).iterator();
    }

    /* ------------------------------------------------------------------
     * Règles
     * ------------------------------------------------------------------ */

    @Override
    public void addRule(char symChar, String expansion) {
        Symbol left = intern(symChar);
        List<Symbol> rhs = toSymbolList(expansion);
        rules.computeIfAbsent(left, k -> new ArrayList<>()).add(rhs);

    }

     /*
      * ------------------------------------------------------------------
      * Dessin après n rounds de réécriture
      * ------------------------------------------------------------------
      */

    @Override
    public void initFromJson(JSONObject obj, Turtle turtle) {

        /* ---------- 0) graine aléatoire (facultatif) ---------- */
        if (obj.has("seed")) {
            long seed = obj.getLong("seed");
            setSeed(seed); // méthode d'AbstractLSystem
            resetRnd(); // pour repartir dans le même état
        }

        /* ---------- 1) paramètres de la tortue (facultatif) ---------- */
        if (obj.has("parameters")) {
            JSONObject pars = obj.getJSONObject("parameters");

            // a) unité de longueur et d'angle
            if (pars.has("step") && pars.has("angle")) {
                double step = pars.getDouble("step");
                double delta = pars.getDouble("angle");
                turtle.setUnits(step, delta);
            }

            // b) état de départ [x, y, theta]
            if (pars.has("start")) {
                JSONArray start = pars.getJSONArray("start");
                double x = start.getDouble(0);
                double y = start.getDouble(1);
                double theta = start.getDouble(2);
                turtle.init(new Point2D.Double(x, y), theta);
            }
        }

        /* ---------- 2) alphabet & actions (obligatoire) ---------- */
        JSONObject actions = obj.getJSONObject("actions");
        for (String code : actions.keySet()) {
            char sym = code.charAt(0); // clé "F" -> 'F'
            String action = actions.getString(code); // ex. "draw"
            setAction(sym, action); // enregistre action
        }

        /* ---------- 3) axiome (obligatoire) ---------- */
        setAxiom(obj.getString("axiom"));

        /* ---------- 4) règles (obligatoire) ---------- */
        JSONObject jsonRules = obj.getJSONObject("rules");
        for (String key : jsonRules.keySet()) {
            char lhs = key.charAt(0);
            JSONArray rhsArray = jsonRules.getJSONArray(key);
            for (int i = 0; i < rhsArray.length(); ++i) {
                addRule(lhs, rhsArray.getString(i));
            }
        }
    }




    /*
     * ------------------------------------------------------------------
     * Exécute la chaîne obtenue après n réécritures et renvoie
     * le bounding-box de toutes les positions visitées par la tortue.
     * ------------------------------------------------------------------
     */
    @Override
    public Rectangle2D tell(Turtle turtle,
            Iterator<Symbol> seq,
            int n) {
        /* cas de base : n == 0 → on parcourt directement seq */
        //System.out.println("JJSJJJNNJNJJNNJNJNJJN " + seq.next());
        if (n == 0) {
            //System.out.println("Passé la boucle");

            Point2D start = turtle.getPosition(); // inclure le départ
            double minX = start.getX(), minY = start.getY();
            double maxX = start.getX(), maxY = start.getY();

            while (seq.hasNext()) {
                Symbol s = seq.next();
                tell(turtle, s); // peut déplacer la tortue
                Point2D p = turtle.getPosition(); // position après l’action
                minX = Math.min(minX, p.getX());
                minY = Math.min(minY, p.getY());
                maxX = Math.max(maxX, p.getX());
                maxY = Math.max(maxY, p.getY());
            }
            /* bounding-box de la phase « exécution » */
            return new Rectangle2D.Double(minX, minY,
                    maxX - minX, maxY - minY);
        }

        /* cas récursif : n > 0 → on réécrit symbole-par-symbole */
        Rectangle2D bbox = null; // bounding-box cumulatif
        while (seq.hasNext()) {
            Symbol s = seq.next();
            Iterator<Symbol> expansion = rewrite(s); // null si terminal
            Rectangle2D subBox;

            if (expansion == null) {
                /* s est terminal : on exécute immédiatement (n-1 ne change rien) */
                subBox = tell(turtle, List.of(s).iterator(), 0);
            } else {
                /* s est non-terminal : on descend d’un niveau de récursion */
                subBox = tell(turtle, expansion, n - 1);
            }

            /* union géométrique avec le bbox courant */
            if (bbox == null)
                bbox = subBox;
            else
                bbox = bbox.createUnion(subBox);
        }

        /* si bbox est resté null (chaîne vide) → bbox 0×0 à la position courante */
        if (bbox == null) {
            Point2D p = turtle.getPosition();
            bbox = new Rectangle2D.Double(p.getX(), p.getY(), 0, 0);
        }
        return bbox;
    }


}
