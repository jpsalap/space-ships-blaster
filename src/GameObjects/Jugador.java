package GameObjects;

import input.Keyboard;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import main.Sonidos;
import math.Vector2D;
import states.Estado_juego;

public class Jugador extends Moving_obj {
    public static final int SHIP_SIZE = 44;

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
        this.width = SHIP_SIZE;
        this.height = SHIP_SIZE;
        this.gameState = gameState;
        this.shipStyle = shipStyle != null ? shipStyle : ShipStyle.BLUE;
        direccion = new Vector2D(0, -1);
        aceleracion = new Vector2D();
        firerate = new Crono();
        spawningTime = new Crono();
        titilar = new Crono();
        visible = true;
    }

    public void activateShield() { shieldFrames = POWERUP_DURATION; }
    public void activateRapidFire() { rapidFireFrames = POWERUP_DURATION; }
    public boolean hasShield() { return shieldFrames > 0; }
    public boolean hasRapidFire() { return rapidFireFrames > 0; }
    public float getShieldRatio() { return (float) shieldFrames / POWERUP_DURATION; }
    public float getRapidFireRatio() { return (float) rapidFireFrames / POWERUP_DURATION; }

    @Override
    public void update() {
        flameTick++;
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
                getCenter().add(direccion.scale(height * 0.52)),
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
            thrustVisual = Math.min(1.0, thrustVisual + 0.28);
            emitExhaustParticles();
        } else {
            double decel = shipAcc * 0.48;
            if (velocidad.getMagni() > decel) {
                aceleracion = (velocidad.scale(-1).normalizar()).scale(decel);
            } else {
                velocidad = new Vector2D();
                aceleracion = new Vector2D();
            }
            acelerando = false;
            thrustVisual = Math.max(0.0, thrustVisual - 0.16);
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
        double ny = height / 2.0 - 2.0;
        double[] sides = { -nx, nx };
        Color accent = shipStyle.getAccentColor();
        Vector2D back = direccion.scale(-1);
        double backAngle = Math.atan2(back.getY(), back.getX());

        for (double lx : sides) {
            double wx = cx + lx * cos - ny * sin;
            double wy = cy + lx * sin + ny * cos;
            double spread = (Math.random() - 0.5) * 0.36;
            double a = backAngle + spread;
            double sp = 1.8 + Math.random() * 2.2;
            Vector2D pVel = new Vector2D(Math.cos(a) * sp, Math.sin(a) * sp);
            Color c = (Math.random() < 0.32) ? Color.WHITE : ((Math.random() < 0.65) ? accent : accent.brighter());
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

        // Dynamic glowing plasma thruster jets behind engine nozzles (with subtle idle pilot glow)
        double nozzleX = shipStyle.getNozzleOffsetX();
        double nozzleY = height / 2.0 - 4.0;
        double speedBoost = Math.min(0.45, velocidad.getMagni() / maxV * 0.45);
        double power = Math.max(0.18, thrustVisual + (acelerando ? speedBoost : 0.0));
        drawThrusterPlume(G2D, -nozzleX, nozzleY, power, acelerando);
        drawThrusterPlume(G2D, nozzleX, nozzleY, power, acelerando);

        // Draw crisp HD 96x96 ship scaled cleanly to compact 44x44
        G2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        G2D.drawImage(textura, -width / 2, -height / 2, width, height, null);

        G2D.setTransform(prev);

        // Energy shield bubble when active
        if (shieldFrames > 0) {
            if (shieldFrames > 120 || (flameTick / 4 + shieldFrames / 5) % 2 == 0) {
                drawShieldBubble(G2D, cx, cy);
            }
        }
    }

    private void drawThrusterPlume(Graphics2D g2d, double nx, double ny, double power, boolean active) {
        double flicker = 0.85 + 0.30 * Math.sin(flameTick * 1.15 + nx * 1.7);
        double len = (active ? 26.0 : 12.0) * power * flicker + (active ? Math.random() * 6.0 : 0.0);
        double wOuter = (active ? 5.2 : 3.2) * Math.min(1.15, power);
        Color accent = shipStyle.getAccentColor();

        // Wide glowing plasma bloom around nozzle & jet
        float haloR = (float) Math.max(7.0, len * 0.9);
        RadialGradientPaint halo = new RadialGradientPaint(
            new Point2D.Double(nx, ny + len * 0.32), haloR,
            new float[] { 0f, 0.5f, 1f },
            new Color[] {
                new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), active ? 175 : 85),
                new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), active ? 70 : 25),
                new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0)
            }
        );
        g2d.setPaint(halo);
        g2d.fillOval((int) (nx - haloR), (int) (ny + len * 0.32 - haloR), (int) (haloR * 2), (int) (haloR * 2));

        // Colored outer plasma cone
        Path2D outer = new Path2D.Double();
        outer.moveTo(nx - wOuter, ny);
        outer.quadTo(nx - wOuter * 0.65, ny + len * 0.55, nx, ny + len);
        outer.quadTo(nx + wOuter * 0.65, ny + len * 0.55, nx + wOuter, ny);
        outer.closePath();
        g2d.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), active ? 235 : 150));
        g2d.fill(outer);

        // Bright white-hot plasma core
        double coreLen = len * 0.62;
        double wCore = wOuter * 0.54;
        Path2D inner = new Path2D.Double();
        inner.moveTo(nx - wCore, ny);
        inner.quadTo(nx - wCore * 0.5, ny + coreLen * 0.55, nx, ny + coreLen);
        inner.quadTo(nx + wCore * 0.5, ny + coreLen * 0.55, nx + wCore, ny);
        inner.closePath();
        g2d.setColor(new Color(255, 255, 255, active ? 245 : 170));
        g2d.fill(inner);
    }

    private void drawShieldBubble(Graphics2D g2d, double cx, double cy) {
        Stroke prevStroke = g2d.getStroke();
        float r = SHIP_SIZE * 0.72f;
        RadialGradientPaint paint = new RadialGradientPaint(
            new Point2D.Double(cx, cy), r,
            new float[] { 0.45f, 0.82f, 1.0f },
            new Color[] {
                new Color(75, 220, 255, 16),
                new Color(75, 220, 255, 65),
                new Color(140, 245, 255, 165)
            }
        );
        g2d.setPaint(paint);
        g2d.fillOval((int) (cx - r), (int) (cy - r), (int) (r * 2), (int) (r * 2));
        g2d.setStroke(new BasicStroke(1.8f));
        g2d.setColor(new Color(120, 240, 255, 195));
        g2d.drawOval((int) (cx - r), (int) (cy - r), (int) (r * 2), (int) (r * 2));
        g2d.setStroke(prevStroke);
    }

    public boolean isSpawning() { return spawning; }
    public Vector2D getVelocidad() { return velocidad; }
    public ShipStyle getShipStyle() { return shipStyle; }
}
