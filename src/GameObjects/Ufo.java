package GameObjects;

import graphics.Assets;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import main.Sonidos;
import math.Vector2D;
import states.Estado_juego;

public class Ufo extends Moving_obj {
    private final double baseY;
    private double wavePhase;
    private final double waveAmplitude;
    private final double waveFreq;
    private int fireTimer;
    private final int fireInterval;
    private final int waveNumber;

    public Ufo(boolean fromLeft, double y, int waveNumber, Estado_juego gameState) {
        super(
            new Vector2D(fromLeft ? -Assets.ufo.getWidth() : Constantes.WIDTH, y),
            new Vector2D(fromLeft ? 1 : -1, 0),
            2.2 + Math.min(1.8, (waveNumber - 3) * 0.22),
            Assets.ufo,
            gameState
        );
        this.baseY = y;
        this.wavePhase = Math.random() * Math.PI * 2;
        this.waveAmplitude = 32 + Math.random() * 28;
        this.waveFreq = 0.04 + Math.random() * 0.025;
        this.waveNumber = waveNumber;
        this.fireInterval = Math.max(42, 96 - (waveNumber - 3) * 9);
        this.fireTimer = 40 + (int) (Math.random() * 30);
        this.velocidad = this.velocidad.scale(this.maxV);
    }

    @Override
    public void update() {
        wavePhase += waveFreq;
        posicion.setX(posicion.getX() + velocidad.getX());
        double ny = baseY + Math.sin(wavePhase) * waveAmplitude;
        ny = Math.max(55, Math.min(Constantes.HEIGHT - height - 45, ny));
        posicion.setY(ny);

        // Leave screen cleanly once crossed to the opposite edge
        if (velocidad.getX() > 0 && posicion.getX() > Constantes.WIDTH + 20) {
            super.destroy();
            return;
        }
        if (velocidad.getX() < 0 && posicion.getX() < -width - 20) {
            super.destroy();
            return;
        }

        // Fire plasma bolt roughly aimed at the player
        fireTimer--;
        if (fireTimer <= 0 && !gameState.isGameOver()) {
            fireAtPlayer();
            fireTimer = fireInterval;
        }

        collidesWith();
    }

    private void fireAtPlayer() {
        Jugador target = gameState.getJugador();
        if (target == null || !gameState.getMoving_obj().contains(target)) {
            return;
        }
        Vector2D toPlayer = target.getCenter().substract(getCenter());
        double aimAngle = Math.atan2(toPlayer.getY(), toPlayer.getX());
        // Slight inaccuracy that tightens on later waves
        double spread = Math.max(0.04, 0.22 - (waveNumber - 3) * 0.035);
        aimAngle += (Math.random() * 2 - 1) * spread;
        Vector2D dir = new Vector2D(Math.cos(aimAngle), Math.sin(aimAngle));
        double boltSpeed = 6.8 + Math.min(3.2, (waveNumber - 3) * 0.4);
        gameState.getMoving_obj().add(new Laser(
            getCenter().add(dir.scale(width * 0.45)),
            dir,
            boltSpeed,
            aimAngle + Math.PI / 2,
            Assets.redLaser,
            gameState,
            true
        ));
        Sonidos.ufoLaser.play();
    }

    @Override
    public void destroy() {
        gameState.onUfoDestroyed(getCenter());
        super.destroy();
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(textura, (int) posicion.getX(), (int) posicion.getY(), null);
    }

    public BufferedImage getTexture() {
        return textura;
    }
}
