package lindenmayer;

import java.awt.geom.Point2D;
import java.util.ArrayDeque;

public class FakeTurtle implements Turtle {
    // Attributs privés
    private Point2D.Double position;
    private double angle; //en degré
    private double step = 1.0;
    private double unitAngle = 90.0;

    private final ArrayDeque<State> stack = new ArrayDeque<>();

    // Constructeurs
    public FakeTurtle(Point2D.Double pos, double initialAngle) {
        
        this.position = (Point2D.Double) pos.clone();
        this.angle = initialAngle;
    }

    @Override
    public void init(Point2D pos, double angle) {
        this.position = (Point2D.Double) pos.clone();
        this.angle = angle;
    }

    @Override
    public void draw() {
        moveInternal(); //comme move(), mais on imagine qu'on dessine
    }

    @Override
    public void move() {
        moveInternal(); // déplacement sans dessin
    }

    private void moveInternal() {
        double radians = Math.toRadians(angle);
        double dx = step * Math.cos(radians);
        double dy = step * Math.sin(radians);
        position.x += dx;
        position.y += dy;
    }

    @Override
    public void turnR() {
        angle -= unitAngle;
    }

    @Override
    public void turnL() {
        angle += unitAngle;
    }

    @Override
    public void push() {
        stack.push(new State(position, angle));
    }

    @Override
    public void pop() {
        if (!stack.isEmpty()) {
            State state = stack.pop();
            this.position = (Point2D.Double) state.position.clone();
            this.angle = state.angle;
        } else {
            System.err.println("Stack is empty. Cannot pop.");
        }
    }

    @Override
    public Point2D getPosition() {
        return new Point2D.Double(position.x, position.y);
    }

    @Override
    public double getAngle() {
        return angle;
    }

    @Override
    public void setUnits(double step, double delta) {
        this.step = step;
        this.unitAngle = delta;
    }

    @Override
    public double getUnitStep() {
        return step;
    }

    @Override
    public double getUnitAngle() {
        return unitAngle;
    }

    // Classe imbriquée pour encapsuler l'état
    private static class State {
        Point2D.Double position;
        double angle;

        State(Point2D.Double position, double angle) {
            this.position = (Point2D.Double) position.clone();
            this.angle = angle;
        }
    }

}