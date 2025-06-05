/*  src/main/java/lindenmayer/PostScriptTurtle.java  */
package lindenmayer;

import java.awt.geom.Point2D;
import java.io.PrintWriter;
import java.util.ArrayDeque;

public class PostScriptTurtle implements Turtle {

    private final PrintWriter out; // flux EPS
    private Point2D.Double pos; // position courante
    private double angle; // degrés
    private double step = 1.0, delta = 25.0; // unités
    private final ArrayDeque<State> stack = new ArrayDeque<>();

    private record State(Point2D.Double pos, double angle) {
    }

    public PostScriptTurtle(Point2D.Double pos, double initialAngle, PrintWriter out) {
        this.out = out;
        this.pos = pos;
        this.angle = initialAngle;
        out.printf("newpath %.3f %.3f moveto%n", pos.x, pos.y);
    }

    /*-------------------------------- actions --------------------------------*/

    private void advance(boolean draw) {
        double rad = Math.toRadians(angle);
        pos.x += step * Math.cos(rad);
        pos.y += step * Math.sin(rad);
        out.printf("%.3f %.3f %s%n", pos.x, pos.y, draw ? "lineto" : "moveto");
    }

    @Override
    public void draw() {
        advance(true);
    }

    @Override
    public void move() {
        advance(false);
    }

    @Override
    public void stay() {
        /* rien */ }

    @Override
    public void turnL() {
        angle += delta;
    }

    @Override
    public void turnR() {
        angle -= delta;
    }

    @Override
    public void push() {

        stack.push(new State((Point2D.Double) pos.clone(), angle));
        out.println("currentpoint stroke newpath moveto");
    }

    @Override
    public void pop() {
        State s = stack.pop();
        out.println("stroke");
        out.printf("%.3f %.3f newpath moveto%n", s.pos.x, s.pos.y);
        pos = s.pos;
        angle = s.angle;
    }

    /*-------------------------------- état / unités -------------------------*/
    @Override
    public void init(Point2D p, double a) {
        pos = new Point2D.Double(p.getX(), p.getY());
        angle = a;
        stack.clear();
        out.printf("stroke newpath %.3f %.3f moveto%n", pos.x, pos.y);
    }

    @Override
    public Point2D getPosition() {
        return pos;
    }

    @Override
    public double getAngle() {
        return angle;
    }

    @Override
    public void setUnits(double s, double d) {
        step = s;
        delta = d;
    }

    @Override
    public double getUnitStep() {
        return step;
    }

    @Override
    public double getUnitAngle() {
        return angle;
    }
}