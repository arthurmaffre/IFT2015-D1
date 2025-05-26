package lindenmayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.geom.Point2D;

public class TurtleImpTest {

    private FakeTurtle turtle;

    @BeforeEach
    void setUp() {
        turtle = new FakeTurtle(new Point2D.Double(0, 0), 0); // origine, orientée vers +X
    }

    // --------------------------------------------------------------------- //
    // Helpers                                                               //
    // --------------------------------------------------------------------- //
    
    private static void assertPosition(Point2D expected, Point2D actual) {
        assertAll(
            () -> assertEquals(expected.getX(), actual.getX(), 1e-9, "x"),
            () -> assertEquals(expected.getY(), actual.getY(), 1e-9, "y")
        );
    }

    // --------------------------------------------------------------------- //
    // Tests init / getters //
    // --------------------------------------------------------------------- //

    @Test
    @DisplayName("init() replace proprement position et angle")
    void testInit() {
        turtle.init(new Point2D.Double(5, 5), 42);

        assertPosition(new Point2D.Double(5, 5), turtle.getPosition());
        assertEquals(42, turtle.getAngle(), 1e-9);
    }

    @Test
    @DisplayName("getPosition() renvoie une copie défensive")
    void testDefensiveCopy() {
        Point2D p = turtle.getPosition();
        p.setLocation(100, 100); // tentative de mutation
        assertPosition(new Point2D.Double(0, 0), turtle.getPosition()); // l'état interne ne bouge pas
    }

    // --------------------------------------------------------------------- //
    // Déplacements                                                          //
    // --------------------------------------------------------------------- //

    @Nested
    @DisplayName("move() / draw()")
    class MoveAndDraq {
        @Test
        void testMoveDefaultStep() {
            turtle.move(); // step = 1, angle = 0
            assertPosition(new Point2D.Double(1, 0), turtle.getPosition());
        }

        @Test
        void testMoveCustomUnits() {
            turtle.setUnits(2.5, 30); // step=2.5, delta=30 deg
            turtle.move(); // angle toujours 0 deg
            assertPosition(new Point2D.Double(2.5, 0), turtle.getPosition());

            turtle.turnL(); // +30 deg
            turtle.move(); // avance selon nouvelle orientation
            // dx - 2.5 * cos30 = 2.165063..., dy = 2.5 * sin30 = 1.25
            assertPosition(new Point2D.Double(4.665063509, 1.25), turtle.getPosition());
        }

        @Test
        void testDrawBehavesLikeMove() {
            turtle.draw();
            assertPosition(new Point2D.Double(1, 0), turtle.getPosition());
        }
    }

    // --------------------------------------------------------------------- //
    // Rotations //
    // --------------------------------------------------------------------- //

    @Test
    void testRotationsRightLeft() {
        turtle.setUnits(1, 90); // delta = 90 deg

        turtle.turnL(); // +90 -> 90
        assertEquals(90, turtle.getAngle(), 1e-9);

        turtle.turnR(); // -90 -> 0
        assertEquals(0, turtle.getAngle(), 1e-9);

        turtle.turnR();
        assertEquals(-90, turtle.getAngle(), 1e-9);
    }

    // --------------------------------------------------------------------- //
    // Pile push/pop //
    // --------------------------------------------------------------------- //

    @Nested
    @DisplayName("push()/pop()")
    class PushPop {

        @Test
        void testPushPopRoundTrip() {
            turtle.move(); // (1,0) angle 0
            turtle.turnL(); // angle 90
            turtle.push(); // empile état A

            turtle.move(); // avance vers (1,1)
            turtle.turnL(); // angle 180

            turtle.pop(); //revient à l'état A

            assertPosition(new Point2D.Double(1, 0), turtle.getPosition());
            assertEquals(90, turtle.getAngle(), 1e-9);
        }

        @Test
        void testPopOnEmptyStackDoesNotCrash() {
            Point2D before = turtle.getPosition();
            double angleBefore = turtle.getAngle();

            turtle.pop(); // pile vide - doit simplement ne rien changer

            assertPosition(before, turtle.getPosition());
            assertEquals(angleBefore, turtle.getAngle(), 1e-9);
        }
    }


    // --------------------------------------------------------------------- //
    // Paramétrage unités //
    // --------------------------------------------------------------------- //

    @Test
    void testGettersAfterSetUnits() {
        turtle.setUnits(3.3, 17.5);
        assertEquals(3.3, turtle.getUnitStep(), 1e-9);
        assertEquals(17.5, turtle.getUnitAngle(), 1e-9);
    }

}