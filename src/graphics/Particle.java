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
    private final boolean glow;

    public Particle(Vector2D posicion, Vector2D velocidad, Color color, double size, int maxLife) {
        this(posicion, velocidad, color, size, maxLife, false);
    }

    public Particle(Vector2D posicion, Vector2D velocidad, Color color, double size, int maxLife, boolean glow) {
        this.posicion = posicion;
        this.velocidad = velocidad;
        this.color = color;
        this.size = size;
        this.life = maxLife;
        this.maxLife = maxLife;
        this.glow = glow;
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
        double curSize = Math.max(1.5, size * (0.35 + 0.65 * ratio));
        double x = posicion.getX();
        double y = posicion.getY();

        if (glow) {
            double halo = curSize * 2.5;
            int haloAlpha = Math.max(0, (int) (alpha * 0.32));
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), haloAlpha));
            g2d.fillOval((int) (x - halo / 2.0), (int) (y - halo / 2.0), (int) Math.ceil(halo), (int) Math.ceil(halo));
        }

        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        g2d.fillOval(
            (int) (x - curSize / 2.0),
            (int) (y - curSize / 2.0),
            (int) Math.ceil(curSize),
            (int) Math.ceil(curSize)
        );
    }
}
