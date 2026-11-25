package GameObjects;

import graphics.Assets;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import math.Vector2D;

public class PowerUp {
    public enum Type {
        SHIELD("SHIELD", "S", new Color(75, 220, 255)),
        RAPID_FIRE("RAPID FIRE", "R", new Color(255, 195, 55));

        public final String label;
        public final String icon;
        public final Color color;

        Type(String label, String icon, Color color) {
            this.label = label;
            this.icon = icon;
            this.color = color;
        }
    }

    private Vector2D posicion;
    private Vector2D velocidad;
    private final Type type;
    private int life = 600; // 10 seconds at 60 FPS
    private int tick = 0;
    public static final int RADIUS = 16;

    public PowerUp(Vector2D posicion, Type type) {
        this.posicion = posicion;
        this.type = type;
        double a = Math.random() * Math.PI * 2;
        double sp = 0.5 + Math.random() * 0.6;
        this.velocidad = new Vector2D(Math.cos(a) * sp, Math.sin(a) * sp);
    }

    public void update() {
        tick++;
        life--;
        posicion = posicion.add(velocidad);
        if (posicion.getX() > Constantes.WIDTH + RADIUS) posicion.setX(-RADIUS);
        else if (posicion.getX() < -RADIUS) posicion.setX(Constantes.WIDTH + RADIUS);
        if (posicion.getY() > Constantes.HEIGHT + RADIUS) posicion.setY(-RADIUS);
        else if (posicion.getY() < -RADIUS) posicion.setY(Constantes.HEIGHT + RADIUS);
    }

    public boolean isAlive() {
        return life > 0;
    }

    public Vector2D getCenter() {
        return posicion;
    }

    public Type getType() {
        return type;
    }

    public void draw(Graphics2D g2d) {
        // Blink during final 2.5 seconds
        if (life < 150 && (tick / 6) % 2 == 0) {
            return;
        }
        Stroke prevStroke = g2d.getStroke();
        double pulse = 1.0 + 0.12 * Math.sin(tick * 0.14);
        float r = (float) (RADIUS * pulse);
        float cx = (float) posicion.getX();
        float cy = (float) posicion.getY();
        Color c = type.color;

        // Outer halo glow
        RadialGradientPaint glow = new RadialGradientPaint(
            new Point2D.Float(cx, cy), r * 1.9f,
            new float[] { 0f, 0.55f, 1f },
            new Color[] {
                new Color(c.getRed(), c.getGreen(), c.getBlue(), 120),
                new Color(c.getRed(), c.getGreen(), c.getBlue(), 45),
                new Color(c.getRed(), c.getGreen(), c.getBlue(), 0)
            }
        );
        g2d.setPaint(glow);
        g2d.fillOval((int) (cx - r * 1.9f), (int) (cy - r * 1.9f), (int) (r * 3.8f), (int) (r * 3.8f));

        // Core orb
        g2d.setColor(new Color(14, 22, 40, 215));
        g2d.fillOval((int) (cx - r), (int) (cy - r), (int) (r * 2), (int) (r * 2));
        g2d.setStroke(new BasicStroke(2.2f));
        g2d.setColor(c);
        g2d.drawOval((int) (cx - r), (int) (cy - r), (int) (r * 2), (int) (r * 2));

        // Crisp icon letter
        g2d.setFont(Assets.fontSmall);
        g2d.setColor(Color.WHITE);
        FontMetrics fm = g2d.getFontMetrics();
        int tx = (int) (cx - fm.stringWidth(type.icon) / 2.0);
        int ty = (int) (cy + fm.getAscent() / 2.0 - 2);
        g2d.drawString(type.icon, tx, ty);

        g2d.setStroke(prevStroke);
    }
}
