package GameObjects;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import math.Vector2D;
import states.Estado_juego;

public class Asteroid extends Moving_obj {
    private final Size size;
    private final double spinSpeed;

    public Asteroid(Vector2D posicion, Vector2D velocidad, double maxV, BufferedImage textura, Estado_juego gameState, Size size) {
        super(posicion, velocidad, maxV, textura, gameState);
        this.size = size;
        this.velocidad = velocidad.scale(maxV);
        this.angulo = Math.random() * Math.PI * 2;
        this.spinSpeed = computeSpinSpeed(size);
    }

    private static double computeSpinSpeed(Size size) {
        double min, max;
        if (size == Size.BIG) {
            min = 0.010; max = 0.025;
        } else if (size == Size.MED) {
            min = 0.022; max = 0.046;
        } else if (size == Size.SMALL) {
            min = 0.040; max = 0.072;
        } else {
            min = 0.060; max = 0.105;
        }
        double dir = Math.random() < 0.5 ? -1.0 : 1.0;
        return dir * (min + Math.random() * (max - min));
    }

    @Override
    public void destroy() {
        int points = getPointsForSize();
        gameState.divideAsteroid(this);
        gameState.onAsteroidDestroyed(getCenter(), size, points);
        super.destroy();
    }

    private int getPointsForSize() {
        if (size == null) {
            return Constantes.asteroid_score;
        }
        switch (size) {
            case BIG:
                return Constantes.asteroid_score;
            case MED:
                return Constantes.asteroid_score * 2;
            case SMALL:
                return Constantes.asteroid_score * 3;
            case TINY:
                return Constantes.asteroid_score * 4;
            default:
                return Constantes.asteroid_score;
        }
    }

    @Override
    public void update() {
        posicion = posicion.add(velocidad);
        double cx = posicion.getX() + width / 2.0;
        double cy = posicion.getY() + height / 2.0;
        if (cx >= Constantes.WIDTH) {
            posicion.setX(posicion.getX() - Constantes.WIDTH);
        } else if (cx < 0) {
            posicion.setX(posicion.getX() + Constantes.WIDTH);
        }
        if (cy >= Constantes.HEIGHT) {
            posicion.setY(posicion.getY() - Constantes.HEIGHT);
        } else if (cy < 0) {
            posicion.setY(posicion.getY() + Constantes.HEIGHT);
        }
        angulo += spinSpeed;
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D G2D = (Graphics2D) g;
        int[] dxOffsets = { -Constantes.WIDTH, 0, Constantes.WIDTH };
        int[] dyOffsets = { -Constantes.HEIGHT, 0, Constantes.HEIGHT };
        for (int dx : dxOffsets) {
            double rx = posicion.getX() + dx;
            if (rx + width < 0 || rx > Constantes.WIDTH) continue;
            for (int dy : dyOffsets) {
                double ry = posicion.getY() + dy;
                if (ry + height < 0 || ry > Constantes.HEIGHT) continue;
                at = AffineTransform.getTranslateInstance(rx, ry);
                at.rotate(angulo, width / 2.0, height / 2.0);
                G2D.drawImage(textura, at, null);
            }
        }
    }

    public Size getSize() {
        return size;
    }

    public double getSpinSpeed() {
        return spinSpeed;
    }
}
