package GameObjects;

import input.Keyboard;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import main.Sonidos;
import math.Vector2D;
import states.Estado_juego;

public class Jugador extends Moving_obj {
    private Vector2D direccion;
    private Vector2D aceleracion;
    private boolean acelerando = false;
    private Crono firerate;
    private boolean spawning, visible;
    private Crono spawningTime, titilar;
    private ShipStyle shipStyle;
    private int flameTick = 0;
    private double thrustVisual = 0.0;

    private int shieldFrames = 0;
    private int rapidFireFrames = 0;
    public static final int POWERUP_DURATION = 480; // 8 seconds at 60 FPS

    public Jugador(Vector2D posicion, Vector2D velocidad, double maxV, BufferedImage textura, Estado_juego gameState) {
        this(posicion, velocidad, maxV, textura, gameState, ShipStyle.BLUE);
    }

    public Jugador(Vector2D posicion, Vector2D velocidad, double maxV, BufferedImage textura, Estado_juego gameState, ShipStyle shipStyle) {
        super(posicion, velocidad, shipStyle != null ? shipStyle.getMaxSpeed() : maxV, textura, gameState);
        this.gameState = gameState;
        this.shipStyle = shipStyle != null ? shipStyle : ShipStyle.BLUE;
        direccion = new Vector2D(0, -1);
        aceleracion = new Vector2D();
        firerate = new Crono();
        spawningTime = new Crono();
        titilar = new Crono();
        visible = true;
    }

    public void activateShield() {
        shieldFrames = POWERUP_DURATION;
    }

    public void activateRapidFire() {
        rapidFireFrames = POWERUP_DURATION;
    }

    public boolean hasShield() {
        return shieldFrames > 0;
    }

    public boolean hasRapidFire() {
        return rapidFireFrames > 0;
    }

    public float getShieldRatio() {
        return (float) shieldFrames / POWERUP_DURATION;
    }

    public float getRapidFireRatio() {
        return (float) rapidFireFrames / POWERUP_DURATION;
    }

    @Override
    public void update() {
        if (shieldFrames > 0) shieldFrames--;
        if (rapidFireFrames > 0) rapidFireFrames--;

        if (!spawningTime.isRunning()) {
            spawning = false;
            visible = true;
        }
        if (spawning) {
            if (!titilar.isRunning()) {
                titilar.run(Constantes.titilar);
                visible = !visible;
            }
        }

        if (Keyboard.right) {
            angulo += shipStyle.getTurnSpeed();
        }
        if (Keyboard.left) {
            angulo -= shipStyle.getTurnSpeed();
        }
        direccion = direccion.setDireccion(angulo - Math.PI / 2);

        if (Keyboard.shoot && !firerate.isRunning() && !spawning) {
            int cooldown = shipStyle.getFireCooldownMs();
            if (rapidFireFrames > 0) {
                cooldown = Math.max(45, cooldown / 2);
            }
            gameState.getMoving_obj().add(0, new Laser(
                getCenter().add(direccion.scale(height * 0.48)),
                direccion,
                Constantes.vlaser,
                angulo,
                shipStyle.getLaserTexture(),
                gameState,
                false
            ));
            firerate.run(cooldown);
            Sonidos.Disparo.play();
        }

        double shipAcc = shipStyle.getAcceleration();
        if (Keyboard.up) {
            aceleracion = direccion.scale(shipAcc);
            acelerando = true;
            flameTick++;
            thrustVisual = Math.min(1.0, thrustVisual + 0.25);
            if (flameTick % 2 == 0) {
                emitExhaustParticles();
            }
        } else {
            double decel = shipAcc * 0.48;
            if (velocidad.getMagni() > decel) {
                aceleracion = (velocidad.scale(-1).normalizar()).scale(decel);
            } else {
                velocidad = new Vector2D();
                aceleracion = new Vector2D();
            }
            acelerando = false;
            thrustVisual = Math.max(0.0, thrustVisual - 0.18);
        }
        Sonidos.propulsor.updateLoopFade(acelerando);

        velocidad = velocidad.add(aceleracion);
        velocidad = velocidad.limitev(maxV);
        posicion = posicion.add(velocidad);

        // Smooth wrap-around using sprite bounds
        if (posicion.getX() > Constantes.WIDTH) {
            posicion.setX(-width);
        } else if (posicion.getX() < -width) {
            posicion.setX(Constantes.WIDTH);
        }
        if (posicion.getY() > Constantes.HEIGHT) {
            posicion.setY(-height);
        } else if (posicion.getY() < -height) {
            posicion.setY(Constantes.HEIGHT);
        }

        firerate.update();
        spawningTime.update();
        titilar.update();
        collidesWith();
    }

    private void emitExhaustParticles() {
        double cx = posicion.getX() + width / 2.0;
        double cy = posicion.getY() + height / 2.0;
        double cos = Math.cos(angulo);
        double sin = Math.sin(angulo);
        double nx = shipStyle.getNozzleOffsetX();
        double ny = height / 2.0 - 3.0;
        double[] sides = { -nx, nx };
        for (double lx : sides) {
            double wx = cx + lx * cos - ny * sin;
            double wy = cy + lx * sin + ny * cos;
            double spread = (Math.random() - 0.5) * 0.45;
            Vector2D back = direccion.scale(-1);
            double a = Math.atan2(back.getY(), back.getX()) + spread;
            double sp = 1.6 + Math.random() * 1.8;
            Vector2D pVel = new Vector2D(Math.cos(a) * sp, Math.sin(a) * sp);
            Color c = Math.random() < 0.45 ? Color.WHITE : shipStyle.getAccentColor();
            gameState.addExhaustParticle(new Vector2D(wx, wy), pVel, c);
        }
    }

    @Override
    public void destroy() {
        Sonidos.propulsor.stop();
        Sonidos.playerHit.play();
        gameState.onPlayerHit(getCenter(), shipStyle.getAccentColor());
        gameState.restarvida();
        if (Estado_juego.lives <= 0) {
            super.destroy();
            return;
        }
        spawning = true;
        spawningTime.run(Constantes.spawing_t);
        restablecer();
    }

    private void restablecer() {
        angulo = 0;
        velocidad = new Vector2D();
        aceleracion = new Vector2D();
        direccion = new Vector2D(0, -1);
        acelerando = false;
        thrustVisual = 0.0;
        posicion = new Vector2D(
            Constantes.WIDTH / 2 - width / 2,
            Constantes.HEIGHT / 2 - height / 2
        );
    }

    @Override
    public void draw(Graphics g) {
        if (!visible) {
            return;
        }
        Graphics2D G2D = (Graphics2D) g;
        AffineTransform prev = G2D.getTransform();

        double cx = posicion.getX() + width / 2.0;
        double cy = posicion.getY() + height / 2.0;
        G2D.translate(cx, cy);
        G2D.rotate(angulo);

        // Dynamic glowing plasma thruster jets behind engine nozzles
        if (thrustVisual > 0.05) {
            drawThrusterPlume(G2D, -shipStyle.getNozzleOffsetX(), height / 2.0 - 5.0);
            drawThrusterPlume(G2D, shipStyle.getNozzleOffsetX(), height / 2.0 - 5.0);
        }

        // Draw ship centered at local origin
        G2D.drawImage(textura, -width / 2, -height / 2, null);

        G2D.setTransform(prev);

        // Energy shield bubble when active
        if (shieldFrames > 0) {
            if (shieldFrames > 120 || (flameTick / 4 + shieldFrames / 5) % 2 == 0) {
                drawShieldBubble(G2D, cx, cy);
            }
        }
    }

    private void drawThrusterPlume(Graphics2D g2d, double nx, double ny) {
        double flicker = 0.82 + 0.36 * Math.sin(flameTick * 0.9 + nx);
        double len = (22.0 + Math.random() * 8.0) * thrustVisual * flicker;
        double wOuter = (5.8 + Math.random() * 1.4) * thrustVisual;
        Color accent = shipStyle.getAccentColor();

        // Soft outer plasma halo
        RadialGradientPaint halo = new RadialGradientPaint(
            new Point2D.Double(nx, ny + len * 0.35), (float) Math.max(8, len * 0.85),
            new float[] { 0f, 1f },
            new Color[] {
                new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 145),
                new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0)
            }
        );
        g2d.setPaint(halo);
        g2d.fillOval((int) (nx - len * 0.65), (int) (ny - 2), (int) (len * 1.3), (int) (len * 1.3));

        // Colored plasma jet cone
        Path2D outer = new Path2D.Double();
        outer.moveTo(nx - wOuter, ny);
        outer.quadTo(nx - wOuter * 0.65, ny + len * 0.55, nx, ny + len);
        outer.quadTo(nx + wOuter * 0.65, ny + len * 0.55, nx + wOuter, ny);
        outer.closePath();
        g2d.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 215));
        g2d.fill(outer);

        // Bright white-hot inner core
        double coreLen = len * 0.58;
        double wCore = wOuter * 0.52;
        Path2D inner = new Path2D.Double();
        inner.moveTo(nx - wCore, ny);
        inner.quadTo(nx - wCore * 0.5, ny + coreLen * 0.55, nx, ny + coreLen);
        inner.quadTo(nx + wCore * 0.5, ny + coreLen * 0.55, nx + wCore, ny);
        inner.closePath();
        g2d.setColor(new Color(255, 255, 255, 240));
        g2d.fill(inner);
    }

    private void drawShieldBubble(Graphics2D g2d, double cx, double cy) {
        Stroke prevStroke = g2d.getStroke();
        float r = Math.max(width, height) * 0.66f;
        RadialGradientPaint paint = new RadialGradientPaint(
            new Point2D.Double(cx, cy), r,
            new float[] { 0.45f, 0.82f, 1.0f },
            new Color[] {
                new Color(75, 220, 255, 18),
                new Color(75, 220, 255, 70),
                new Color(140, 245, 255, 165)
            }
        );
        g2d.setPaint(paint);
        g2d.fillOval((int) (cx - r), (int) (cy - r), (int) (r * 2), (int) (r * 2));
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.setColor(new Color(120, 240, 255, 200));
        g2d.drawOval((int) (cx - r), (int) (cy - r), (int) (r * 2), (int) (r * 2));
        g2d.setStroke(prevStroke);
    }

    public boolean isSpawning() {
        return spawning;
    }

    public Vector2D getVelocidad() {
        return velocidad;
    }

    public ShipStyle getShipStyle() {
        return shipStyle;
    }
}
