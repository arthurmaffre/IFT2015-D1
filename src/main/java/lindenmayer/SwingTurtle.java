package lindenmayer;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Une tortue qui dessine dans un Graphics2D (Swing).
 * On doit appeler flush() après avoir fini de dessiner pour revalider la zone.
 */
public class SwingTurtle implements Turtle {
    private final Graphics2D g2;
    private Point2D.Double pos;
    private double angle; // en degrés
    private double step = 1.0; // longueur d’un pas
    private double delta = 25; // angle unitaire
    private final Deque<State> stack = new ArrayDeque<>();

    private static class State {
        final Point2D.Double p;
        final double a;

        State(Point2D.Double p, double a) {
            this.p = (Point2D.Double) p.clone();
            this.a = a;
        }
    }

    /**
     * @param g2     le Graphics2D sur lequel on dessine
     * @param startX position initiale x
     * @param startY position initiale y
     * @param startA angle initial (en degrès, 0 = à droite, 90 = en haut)
     */
    public SwingTurtle(Graphics2D g2, double startX, double startY, double startA) {
        this.g2 = g2;
        this.pos = new Point2D.Double(startX, startY);
        this.angle = startA;
        // On fait un "moveto" implicite en positionnant pos
    }

    private void advance(boolean draw) {
        double rad = Math.toRadians(angle);
        double nx = pos.x + step * Math.cos(rad);
        double ny = pos.y + step * Math.sin(rad);
        if (draw) {
            g2.draw(new Line2D.Double(pos.x, pos.y, nx, ny));
        }
        pos.setLocation(nx, ny);
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
        // rien
    }

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
        stack.push(new State(pos, angle));
    }

    @Override
    public void pop() {
        if (!stack.isEmpty()) {
            State s = stack.pop();
            pos = (Point2D.Double) s.p.clone();
            angle = s.a;
        }
    }

    @Override
    public void init(Point2D p, double a) {
        this.pos = new Point2D.Double(p.getX(), p.getY());
        this.angle = a;
        stack.clear();
    }

    @Override
    public Point2D getPosition() {
        return new Point2D.Double(pos.x, pos.y);
    }

    @Override
    public double getAngle() {
        return angle;
    }

    @Override
    public void setUnits(double s, double d) {
        this.step = s;
        this.delta = d;
    }

    @Override
    public double getUnitStep() {
        return step;
    }

    @Override
    public double getUnitAngle() {
        return delta;
    }
}