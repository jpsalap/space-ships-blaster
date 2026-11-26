package GameObjects;

import graphics.Assets;
import graphics.Loader;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import math.Vector2D;

public class PowerUp {
    public enum Type {
        SHIELD("SHIELD", "S", new Color(75, 220, 255)),
        RAPID_FIRE("RAPID FIRE", "R", new Color(255, 195, 55)),
        SPREAD_SHOT("TRIPLE SHOT", "3", new Color(255, 95, 220));

        public final String label;
        public final String icon;
        public final Color color;
        private BufferedImage cachedSprite;

        Type(String label, String icon, Color color) {
            this.label = label;
            this.icon = icon;
            this.color = color;
        }

        BufferedImage getSprite() {
            if (cachedSprite == null) {
                cachedSprite = renderSprite(this);
            }
            return cachedSprite;
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

    private static BufferedImage renderSprite(Type type) {
        int half = (int) Math.ceil(RADIUS * 2.05) + 2;
        int size = half * 2;
        BufferedImage img = Loader.createCompatibleImage(size, size, Transparency.TRANSLUCENT);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        float r = RADIUS;
        float cx = half;
        float cy = half;
        Color c = type.color;

        RadialGradientPaint glow = new RadialGradientPaint(
            new Point2D.Float(cx, cy), r * 1.9f,
            new float[] { 0f, 0.55f, 1f },
            new Color[] {
                new Color(c.getRed(), c.getGreen(), c.getBlue(), 120),
                new Color(c.getRed(), c.getGreen(), c.getBlue(), 45),
                new Color(c.getRed(), c.getGreen(), c.getBlue(), 0)
            }
        );
        g.setPaint(glow);
        g.fillOval((int) (cx - r * 1.9f), (int) (cy - r * 1.9f), (int) (r * 3.8f), (int) (r * 3.8f));

        g.setColor(new Color(14, 22, 40, 215));
        g.fillOval((int) (cx - r), (int) (cy - r), (int) (r * 2), (int) (r * 2));
        g.setStroke(new BasicStroke(2.2f));
        g.setColor(c);
        g.drawOval((int) (cx - r), (int) (cy - r), (int) (r * 2), (int) (r * 2));

        g.setFont(Assets.fontSmall);
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        int tx = (int) (cx - fm.stringWidth(type.icon) / 2.0);
        int ty = (int) (cy + fm.getAscent() / 2.0 - 2);
        g.drawString(type.icon, tx, ty);
        g.dispose();
        return img;
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
        BufferedImage sprite = type.getSprite();
        double pulse = 1.0 + 0.12 * Math.sin(tick * 0.14);
        int drawW = (int) Math.round(sprite.getWidth() * pulse);
        int drawH = (int) Math.round(sprite.getHeight() * pulse);
        int dx = (int) Math.round(posicion.getX() - drawW / 2.0);
        int dy = (int) Math.round(posicion.getY() - drawH / 2.0);
        g2d.drawImage(sprite, dx, dy, drawW, drawH, null);
    }
}
