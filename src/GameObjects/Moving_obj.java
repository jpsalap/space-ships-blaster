package GameObjects;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import math.Vector2D;
import states.Estado_juego;

public abstract class Moving_obj extends GameObject {

    protected Vector2D velocidad;
    protected AffineTransform at, at1, at2;
    protected double angulo;
    protected double maxV;
    protected int width;
    protected int height;
    protected Estado_juego gameState;

    public Moving_obj(Vector2D posicion, Vector2D velocidad, double maxV, BufferedImage textura, Estado_juego gameState) {
        super(posicion, textura);
        this.velocidad = velocidad;
        this.maxV = maxV;
        this.gameState = gameState;
        width = textura.getWidth();
        height = textura.getHeight();
        angulo = 0;
    }

    protected void collidesWith() {
        ArrayList<Moving_obj> moving_obj = gameState.getMoving_obj();
        ArrayList<Moving_obj> snapshot = new ArrayList<Moving_obj>(moving_obj);
        for (Moving_obj m : snapshot) {
            if (!moving_obj.contains(this)) {
                break;
            }
            if (m.equals(this) || !moving_obj.contains(m)) {
                continue;
            }
            double distancia = m.getCenter().substract(getCenter()).getMagni();
            double radiusA = Math.min(m.width, m.height) * 0.48;
            double radiusB = Math.min(width, height) * 0.48;
            if (m instanceof Laser && ((Laser) m).isFromEnemy()) radiusA = 8;
            if (this instanceof Laser && ((Laser) this).isFromEnemy()) radiusB = 8;
            if (distancia < radiusA + radiusB) {
                objectCollision(m, this);
            }
        }
    }

    private void objectCollision(Moving_obj a, Moving_obj b) {
        // Ignore non-interacting pairs
        if (a instanceof Asteroid && b instanceof Asteroid) return;
        if (a instanceof Laser && b instanceof Laser) return;
        if ((a instanceof Ufo && b instanceof Asteroid) || (a instanceof Asteroid && b instanceof Ufo)) return;

        // Player laser never hits player; enemy laser never hits UFO or asteroids
        if (a instanceof Laser) {
            boolean enemy = ((Laser) a).isFromEnemy();
            if (!enemy && b instanceof Jugador) return;
            if (enemy && (b instanceof Ufo || b instanceof Asteroid)) return;
        }
        if (b instanceof Laser) {
            boolean enemy = ((Laser) b).isFromEnemy();
            if (!enemy && a instanceof Jugador) return;
            if (enemy && (a instanceof Ufo || a instanceof Asteroid)) return;
        }

        // Spawn invulnerability
        if (a instanceof Jugador && ((Jugador) a).isSpawning()) return;
        if (b instanceof Jugador && ((Jugador) b).isSpawning()) return;

        // Active shield absorbs the hit and destroys the hazard
        if (a instanceof Jugador && ((Jugador) a).hasShield()) {
            gameState.onShieldAbsorb(b.getCenter());
            b.destroy();
            return;
        }
        if (b instanceof Jugador && ((Jugador) b).hasShield()) {
            gameState.onShieldAbsorb(a.getCenter());
            a.destroy();
            return;
        }

        gameState.playExplosion(getCenter());
        a.destroy();
        b.destroy();
    }

    protected void destroy() {
        gameState.getMoving_obj().remove(this);
    }

    public Vector2D getCenter() {
        return new Vector2D(posicion.getX() + width / 2.0, posicion.getY() + height / 2.0);
    }
}
