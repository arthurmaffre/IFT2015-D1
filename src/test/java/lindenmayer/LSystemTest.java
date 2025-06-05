package lindenmayer;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import java.io.StringReader;
import java.util.Iterator;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/** Tests JUnit 5 ― vérifie les fonctions clés de LSystem */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Suite de tests LSystem")
class LSystemTest {

    private LSystem sys;
    private FakeTurtle turtle;

    /* ------------------------------------------------------------------ */
    @BeforeEach
    void init() {
        sys = new LSystem();
        turtle = new FakeTurtle(new Point2D.Double(0, 0), 90);
    }

    /*
     * ------------------------------------------------------------------ *
     * 1) setAction + axiome *
     * ------------------------------------------------------------------
     */
    @Test
    @Order(1)
    @DisplayName("1  setAction() puis exécution d'un axiome de longueur 1")
    void testSetAction() {
        //System.out.println("Test 1");
        sys.setAction('F', "move");
        sys.setAxiom("F");
        Rectangle2D box = sys.tell(turtle, sys.getAxiom(), 0);

        //System.out.println("Position : " + turtle.getPosition().getY());
        //System.out.println("Afficher hauteur : " + box.getHeight());

        assertEquals(1.0, turtle.getPosition().getY(), 1e-9);
        assertEquals(1.0, box.getHeight(), 1e-9);
    }

    /*
     * ------------------------------------------------------------------ *
     * 2) rewrite() *
     * ------------------------------------------------------------------
     */
    @Nested
    @DisplayName("2️⃣  Groupe de tests : rewrite()")
    class Rewrite {

        @Test
        @DisplayName("2.1  Symbole terminal → rewrite() renvoie null")
        void terminalReturnsNull() {
            sys.setAxiom("X");
            Symbol X = sys.getAxiom().next();
            assertNull(sys.rewrite(X));
        }

        @Test
        @DisplayName("2.2  Symbole non-terminal → rewrite() renvoie un itérateur")
        void nonTerminalReturnsIterator() {
            sys.addRule('A', "BC");
            Symbol A = sys.setAction('A', "stay");
            Iterator<Symbol> it = sys.rewrite(A);

            assertNotNull(it);
            assertTrue(it.hasNext());
        }
    }

    /*
     * ------------------------------------------------------------------ *
     * 3) tell() avec 1 itération *
     * ------------------------------------------------------------------
     */
    @Test
    @Order(2)
    @DisplayName("3️⃣  Une réécriture de 'F'→'FF' fait avancer la tortue de 2 pas")
    void testTellOneIteration() {
        sys.setAction('F', "draw");
        sys.addRule('F', "FF");
        sys.setAxiom("F");

        Rectangle2D box = sys.tell(turtle, sys.getAxiom(), 1);
        Point2D p = turtle.getPosition();

        assertEquals(2.0, p.getY(), 1e-9);
        assertEquals(2.0, box.getHeight(), 1e-9);
    }

    /*
     * ------------------------------------------------------------------ *
     * 4) initFromJson *
     * ------------------------------------------------------------------
     */
    @Test
    @Order(3)
    @DisplayName("4 initFromJson charge un système simple 'F→F+F'")
    void testInitFromJson() {

        String jsonStr = """
                {
                  "actions": { "F":"draw", "+":"turnL", "-":"turnR" },
                  "axiom"  : "F",
                  "rules"  : { "F":["F+F"] },
                  "parameters": { "step":2, "angle":90, "start":[0,0,90] }
                }""";

        sys.initFromJson(new JSONObject(new JSONTokener(new StringReader(jsonStr))), turtle);

        Rectangle2D box = sys.tell(turtle, sys.getAxiom(), 1);

        Point2D pos = turtle.getPosition();
        assertEquals(-2.0, pos.getX(), 1e-9);
        assertEquals(2.0, pos.getY(), 1e-9);

        assertEquals(2.0, box.getWidth(), 1e-9);
        assertEquals(2.0, box.getHeight(), 1e-9);
    }


    /*
     * ------------------------------------------------------------------ *
     * 5) tell() avec 2 itérations (prof.: F -> FF -> FFFF) *
     * ------------------------------------------------------------------
     */
    @Test
    @Order(4)
    @DisplayName("5️⃣  Deux réécritures de 'F'→'FF' font avancer la tortue de 4 pas")
    void testTellTwoIterations() {

        /* règle simple : chaque F devient deux F ; la tortue dessine vers le haut */
        sys.setAction('F', "draw"); // draw avance d'un pas
        sys.addRule('F', "FF");
        sys.setAxiom("F"); // longueur 1 → 2 → 4

        Rectangle2D box = sys.tell(turtle, sys.getAxiom(), 2); // 2 itérations
        Point2D p = turtle.getPosition();

        /*
         * La tortue part de (0,0) orientée à 90°.
         * Après 4 draws (pas = 1), elle est en (0,4).
         */
        assertEquals(4.0, p.getY(), 1e-9, "ordonnée finale = 4");
        assertEquals(0.0, p.getX(), 1e-9, "abscisse finale = 0");

        /* Bounding-box de [0,0] à [0,4] : hauteur 4, largeur 0 */
        assertEquals(4.0, box.getHeight(), 1e-9, "hauteur bbox = 4");
        assertEquals(0.0, box.getWidth(), 1e-9, "largeur bbox = 0");
    }


    /*
     * ------------------------------------------------------------------ *
     * 6) Stack + virages : F[+F]F *
     * ------------------------------------------------------------------
     */
    @Test
    @Order(5)
    @DisplayName("6️⃣  Push/Pop et virage gauche – bounding-box [-1,0]×[0,2]")
    void testStackAndTurns() {

        /* 1. Alphabet complet pour le casse-croûte */
        sys.setAction('F', "draw"); // avance d'un pas
        sys.setAction('+', "turnL"); // +90°
        sys.setAction('-', "turnR"); // -90° (pas utilisé ici)
        sys.setAction('[', "push"); // sauvegarde état
        sys.setAction(']', "pop"); // restaure état

        /*
         * 2. Axiome représentant une petite plante : F[+F]F
         * - avance
         * - branche à gauche (+, F) puis revient (pop)
         * - avance encore
         */
        sys.setAxiom("F[+F]F");

        /* 3. Aucune règle → profondeur 0 */
        Rectangle2D box = sys.tell(turtle, sys.getAxiom(), 0);

        /*
         * 4. Vérifs
         * Chemin réel :
         * (0,0) --F--> (0,1)
         * push
         * turnL (angle 180°)
         * F (-1,1)
         * pop → revient à (0,1,90°)
         * F (0,2)
         * Bounding-box attendu : xmin = -1 xmax = 0 ymin = 0 ymax = 2
         */
        Point2D pos = turtle.getPosition();
        assertEquals(0.0, pos.getX(), 1e-9, "abscisse finale");
        assertEquals(2.0, pos.getY(), 1e-9, "ordonnée finale");

        assertEquals(-1.0, box.getMinX(), 1e-9, "xmin");
        assertEquals(0.0, box.getMaxX(), 1e-9, "xmax");
        assertEquals(0.0, box.getMinY(), 1e-9, "ymin");
        assertEquals(2.0, box.getMaxY(), 1e-9, "ymax");
    }
}