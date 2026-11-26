package GameObjects;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import math.Vector2D;
import states.Estado_juego;

public abstract class Moving_obj extends GameObject {

    protected Vector2D velocidad;
    protected AffineTransform at;
    protected double angulo;
    protected double maxV;
    protected int width;
    protected int height;
    protected Estado_juego gameState;
    protected boolean dead = false;

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
        if (dead) {
            return;
        }
        ArrayList<Moving_obj> moving_obj = gameState.getMoving_obj();
        double selfCx = getCenterX();
        double selfCy = getCenterY();
        boolean selfIsUfo = (this instanceof Ufo);
        double radiusB = (this instanceof Jugador) ? Math.min(width, height) * 0.36 : Math.min(width, height) * 0.46;
        if (this instanceof Laser && ((Laser) this).isFromEnemy()) radiusB = 7;

        // Iterate backward or over current size without O(N^3) contains() scans
        for (int i = moving_obj.size() - 1; i >= 0; i--) {
            if (dead) {
                break;
            }
            if (i >= moving_obj.size()) {
                continue;
            }
            Moving_obj m = moving_obj.get(i);
            if (m == this || m.dead) {
                continue;
            }
            double dx = Math.abs(m.getCenterX() - selfCx);
            double dy = Math.abs(m.getCenterY() - selfCy);
            // Account for seamless edge wrapping on ships & asteroids (UFOs do not wrap)
            if (!selfIsUfo && !(m instanceof Ufo)) {
                if (dx > Constantes.WIDTH * 0.5) {
                    dx = Constantes.WIDTH - dx;
                }
                if (dy > Constantes.HEIGHT * 0.5) {
                    dy = Constantes.HEIGHT - dy;
                }
            }
            double radiusA = (m instanceof Jugador) ? Math.min(m.width, m.height) * 0.36 : Math.min(m.width, m.height) * 0.46;
            if (m instanceof Laser && ((Laser) m).isFromEnemy()) radiusA = 7;
            double sumR = radiusA + radiusB;
            if (dx < sumR && dy < sumR && (dx * dx + dy * dy) < (sumR * sumR)) {
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

        // Spawn invulnerability & Warp Dash invulnerability
        if (a instanceof Jugador && (((Jugador) a).isSpawning() || ((Jugador) a).isDashing())) return;
        if (b instanceof Jugador && (((Jugador) b).isSpawning() || ((Jugador) b).isDashing())) return;

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

        Vector2D impactCenter = (a instanceof Laser) ? b.getCenter() : ((b instanceof Laser) ? a.getCenter() : getCenter());
        gameState.playExplosion(impactCenter);
        a.destroy();
        b.destroy();
    }

    protected void destroy() {
        dead = true;
        gameState.getMoving_obj().remove(this);
    }

    public boolean isDead() {
        return dead;
    }

    public double getCenterX() {
        return posicion.getX() + width * 0.5;
    }

    public double getCenterY() {
        return posicion.getY() + height * 0.5;
    }

    public Vector2D getCenter() {
        return new Vector2D(posicion.getX() + width / 2.0, posicion.getY() + height / 2.0);
    }
}
