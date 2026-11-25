package GameObjects;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import math.Vector2D;
import states.Estado_juego;

public class Laser extends Moving_obj {
    private final boolean fromEnemy;

    public Laser(Vector2D posicion, Vector2D velocidad, double maxV, double angulo, BufferedImage textura, Estado_juego gameState) {
        this(posicion, velocidad, maxV, angulo, textura, gameState, false);
    }

    public Laser(Vector2D posicion, Vector2D velocidad, double maxV, double angulo, BufferedImage textura, Estado_juego gameState, boolean fromEnemy) {
        super(posicion, velocidad, maxV, textura, gameState);
        this.angulo = angulo;
        this.velocidad = velocidad.scale(maxV);
        this.fromEnemy = fromEnemy;
    }

    public boolean isFromEnemy() {
        return fromEnemy;
    }

    @Override
    public void update() {
        posicion = posicion.add(velocidad);
        if (posicion.getX() < -20 || posicion.getX() > Constantes.WIDTH + 20 || posicion.getY() < -20 || posicion.getY() > Constantes.HEIGHT + 20) {
            destroy();
            return;
        }
        collidesWith();
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D G2D = (Graphics2D) g;
        if (fromEnemy) {
            // Glowing alien magenta plasma orb
            float cx = (float) posicion.getX();
            float cy = (float) posicion.getY();
            RadialGradientPaint paint = new RadialGradientPaint(
                new Point2D.Float(cx, cy), 11f,
                new float[] { 0f, 0.45f, 1f },
                new Color[] { Color.WHITE, new Color(255, 70, 235, 220), new Color(200, 20, 220, 0) }
            );
            G2D.setPaint(paint);
            G2D.fillOval((int) (cx - 11), (int) (cy - 11), 22, 22);
            return;
        }
        at = AffineTransform.getTranslateInstance(posicion.getX() - width / 2, posicion.getY() - height / 2);
        at.rotate(angulo, width / 2, height / 2);
        G2D.drawImage(textura, at, null);
    }

    @Override
    public Vector2D getCenter() {
        return new Vector2D(posicion.getX(), posicion.getY());
    }
}
