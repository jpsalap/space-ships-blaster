package graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import math.Vector2D;

public class Particle {
    private Vector2D posicion;
    private Vector2D velocidad;
    private Color color;
    private double size;
    private int life;
    private final int maxLife;

    public Particle(Vector2D posicion, Vector2D velocidad, Color color, double size, int maxLife) {
        this.posicion = posicion;
        this.velocidad = velocidad;
        this.color = color;
        this.size = size;
        this.life = maxLife;
        this.maxLife = maxLife;
    }

    public void update() {
        posicion = posicion.add(velocidad);
        velocidad = velocidad.scale(0.95);
        life--;
    }

    public boolean isAlive() {
        return life > 0;
    }

    public void draw(Graphics2D g2d) {
        float ratio = Math.max(0f, (float) life / (float) maxLife);
        int alpha = Math.min(255, Math.max(0, (int) (255 * ratio)));
        double curSize = Math.max(1.5, size * ratio);
        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        g2d.fillOval(
            (int) (posicion.getX() - curSize / 2.0),
            (int) (posicion.getY() - curSize / 2.0),
            (int) Math.ceil(curSize),
            (int) Math.ceil(curSize)
        );
    }
}
