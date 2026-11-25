package states;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import GameObjects.Asteroid;
import GameObjects.Constantes;
import GameObjects.Jugador;
import GameObjects.Moving_obj;
import GameObjects.PowerUp;
import GameObjects.ShipStyle;
import GameObjects.Size;
import GameObjects.Ufo;
import graphics.Animacion;
import graphics.Assets;
import graphics.Particle;
import input.Keyboard;
import main.HighScoreStore;
import main.Sonidos;
import math.Vector2D;

public final class Estado_juego {
    private static class FloatingText {
        double x, y;
        String text;
        Color color;
        int life;
        final int maxLife;

        FloatingText(double x, double y, String text, Color color, int maxLife) {
            this.x = x;
            this.y = y;
            this.text = text;
            this.color = color;
            this.life = maxLife;
            this.maxLife = maxLife;
        }
    }

    private Jugador player;
    private ArrayList<Moving_obj> moving_obj = new ArrayList<Moving_obj>();
    private ArrayList<Animacion> explosiones = new ArrayList<Animacion>();
    private ArrayList<Particle> particles = new ArrayList<Particle>();
    private ArrayList<PowerUp> powerUps = new ArrayList<PowerUp>();
    private ArrayList<FloatingText> floatingTexts = new ArrayList<FloatingText>();

    private final HighScoreStore highScoreStore;
    private int puntaje = 0;
    private int highScore = 0;
    private boolean newHighScore = false;
    private int leaderboardRank = 0;
    private int nextExtraLifeScore = 1500;
    private static final int EXTRA_LIFE_STEP = 1500;

    // Score combo multiplier
    private int comboKills = 0;
    private int comboTimer = 0;
    private static final int COMBO_WINDOW = 132; // ~2.2 seconds at 60 FPS

    public static int lives = 3;
    private int asteroids;
    private int wave = 0;
    private int waveBannerTimer = 0;
    private static final int WAVE_BANNER_DURATION = 110;
    private int ufoSpawnCooldown = 0;

    private ShipStyle shipStyle;
    private boolean paused = false;
    private boolean gameOver = false;
    private boolean restartArmed = false;
    private boolean returnToTitle = false;
    private long gameOverTime = 0;

    private int shakeFrames = 0;
    private double shakeMagnitude = 0;

    public Estado_juego() {
        this(ShipStyle.BLUE, 0, null);
    }

    public Estado_juego(ShipStyle shipStyle, int highScore) {
        this(shipStyle, highScore, null);
    }

    public Estado_juego(ShipStyle shipStyle, int highScore, HighScoreStore highScoreStore) {
        lives = 3;
        this.shipStyle = shipStyle != null ? shipStyle : ShipStyle.BLUE;
        this.highScoreStore = highScoreStore;
        int storedBest = (highScoreStore != null) ? highScoreStore.getTopScore() : 0;
        this.highScore = Math.max(Math.max(0, highScore), storedBest);
        BufferedImage shipTex = this.shipStyle.getShipTexture();
        player = new Jugador(
            new Vector2D(Constantes.WIDTH / 2 - Jugador.SHIP_SIZE / 2, Constantes.HEIGHT / 2 - Jugador.SHIP_SIZE / 2),
            new Vector2D(),
            this.shipStyle.getMaxSpeed(),
            shipTex,
            this,
            this.shipStyle
        );
        moving_obj.add(player);
        asteroids = 1;
        startWave();
    }

    public void addScore(int valor) {
        puntaje += valor;
        if (puntaje > highScore) {
            highScore = puntaje;
            newHighScore = true;
        }
        if (!gameOver && lives > 0 && moving_obj.contains(player)) {
            while (puntaje >= nextExtraLifeScore) {
                lives++;
                nextExtraLifeScore += EXTRA_LIFE_STEP;
                Sonidos.extraLife.play();
                floatingTexts.add(new FloatingText(Constantes.WIDTH / 2.0, 125, "+1 EXTRA LIFE!", new Color(110, 255, 140), 90));
            }
        }
    }

    public int getComboMultiplier() {
        if (comboKills >= 10) return 4;
        if (comboKills >= 6) return 3;
        if (comboKills >= 3) return 2;
        return 1;
    }

    public double getWaveSpeedMultiplier() {
        return 1.0 + Math.min(1.5, (wave - 1) * 0.14);
    }

    public void addExhaustParticle(Vector2D pos, Vector2D vel, Color color) {
        if (particles.size() < 260) {
            particles.add(new Particle(pos, vel, color, 3.0 + Math.random() * 2.4, 18 + (int) (Math.random() * 14), true));
        }
    }

    public void onPlayerDash(Vector2D center, Vector2D dir, Color accent) {
        triggerShake(4, 2.5);
        Vector2D back = dir.scale(-1);
        double backAngle = Math.atan2(back.getY(), back.getX());
        for (int i = 0; i < 16; i++) {
            double a = backAngle + (Math.random() - 0.5) * 1.1;
            double sp = 2.0 + Math.random() * 4.2;
            Color c = (i % 3 == 0) ? Color.WHITE : accent;
            particles.add(new Particle(
                new Vector2D(center.getX(), center.getY()),
                new Vector2D(Math.cos(a) * sp, Math.sin(a) * sp),
                c,
                3.2 + Math.random() * 2.4,
                16 + (int) (Math.random() * 12),
                true
            ));
        }
    }

    public void onAsteroidDestroyed(Vector2D center, Size size, int basePoints) {
        comboKills++;
        comboTimer = COMBO_WINDOW;
        int mult = getComboMultiplier();
        int awarded = basePoints * mult;
        addScore(awarded);

        Sonidos.explosion.play();
        int count = (size == Size.BIG) ? 18 : (size == Size.MED ? 13 : 9);
        double shake = (size == Size.BIG) ? 6.0 : (size == Size.MED ? 4.0 : 2.5);
        triggerShake(8, shake);
        Color[] sparkPalette = {
            new Color(255, 195, 75),
            new Color(255, 120, 45),
            new Color(255, 240, 175),
            new Color(190, 170, 150)
        };
        spawnParticles(center, count, sparkPalette, 1.5, 4.5, 22, 38);
        String label = (mult > 1) ? ("+" + awarded + " x" + mult) : ("+" + awarded);
        Color popupColor = (mult >= 3) ? new Color(255, 130, 240) : ((mult == 2) ? new Color(110, 240, 255) : new Color(255, 230, 120));
        floatingTexts.add(new FloatingText(center.getX(), center.getY(), label, popupColor, 44));

        // Occasional power-up drop from BIG or MED asteroids
        if ((size == Size.BIG || size == Size.MED) && powerUps.size() < 2 && Math.random() < 0.14) {
            spawnRandomPowerUp(center);
        }
    }

    public void onUfoDestroyed(Vector2D center) {
        comboKills++;
        comboTimer = COMBO_WINDOW;
        int mult = getComboMultiplier();
        int ufoPoints = 150 * mult;
        addScore(ufoPoints);
        Sonidos.explosion.play();
        triggerShake(12, 8.0);
        Color[] ufoSparks = {
            new Color(245, 90, 255),
            new Color(120, 240, 255),
            Color.WHITE,
            new Color(255, 180, 255)
        };
        spawnParticles(center, 26, ufoSparks, 2.0, 5.5, 26, 44);
        String label = (mult > 1) ? ("+" + ufoPoints + " UFO x" + mult) : ("+" + ufoPoints + " UFO!");
        floatingTexts.add(new FloatingText(center.getX(), center.getY(), label, new Color(245, 130, 255), 65));

        if (powerUps.size() < 2 && Math.random() < 0.55) {
            spawnRandomPowerUp(center);
        }
    }

    public void onShieldAbsorb(Vector2D center) {
        Sonidos.powerUp.play();
        triggerShake(6, 4.0);
        Color[] shieldSparks = {
            new Color(90, 235, 255),
            Color.WHITE,
            new Color(150, 250, 255)
        };
        spawnParticles(center, 16, shieldSparks, 1.8, 4.5, 16, 28);
    }

    private void spawnRandomPowerUp(Vector2D center) {
        PowerUp.Type[] types = PowerUp.Type.values();
        PowerUp.Type type = types[(int) (Math.random() * types.length)];
        powerUps.add(new PowerUp(new Vector2D(center.getX(), center.getY()), type));
    }

    public void onPlayerHit(Vector2D center, Color shipAccent) {
        comboKills = 0;
        comboTimer = 0;
        triggerShake(16, 10.0);
        Color[] sparkPalette = {
            shipAccent,
            Color.WHITE,
            new Color(255, 140, 50),
            new Color(255, 220, 90)
        };
        spawnParticles(center, 28, sparkPalette, 2.0, 6.0, 28, 48);
    }

    private void spawnParticles(Vector2D center, int count, Color[] palette, double minSpeed, double maxSpeed, int minLife, int maxLife) {
        for (int i = 0; i < count; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = minSpeed + Math.random() * (maxSpeed - minSpeed);
            Vector2D vel = new Vector2D(Math.cos(angle) * speed, Math.sin(angle) * speed);
            Color c = palette[(int) (Math.random() * palette.length)];
            double sz = 2.5 + Math.random() * 3.5;
            int life = minLife + (int) (Math.random() * (maxLife - minLife));
            particles.add(new Particle(new Vector2D(center.getX(), center.getY()), vel, c, sz, life));
        }
    }

    private void triggerShake(int frames, double magnitude) {
        if (magnitude >= shakeMagnitude || shakeFrames <= 0) {
            shakeFrames = Math.max(shakeFrames, frames);
            shakeMagnitude = Math.max(shakeMagnitude, magnitude);
        }
    }

    public void divideAsteroid(Asteroid asteroid) {
        Size size = asteroid.getSize();
        BufferedImage[] texturas = size.texturas;
        Size newSize = null;
        switch (size) {
            case BIG:
                newSize = Size.MED;
                break;
            case MED:
                newSize = Size.SMALL;
                break;
            case SMALL:
                newSize = Size.TINY;
                break;
            case TINY:
                break;
            default:
                return;
        }
        double speedScale = getWaveSpeedMultiplier();
        Vector2D parentCenter = asteroid.getCenter();
        for (int i = 0; i < size.cantidad; i++) {
            BufferedImage childTex = texturas[(int) (Math.random() * texturas.length)];
            Vector2D childPos = new Vector2D(
                parentCenter.getX() - childTex.getWidth() / 2.0,
                parentCenter.getY() - childTex.getHeight() / 2.0
            );
            moving_obj.add(new Asteroid(
                childPos,
                new Vector2D(0, 1).setDireccion(Math.random() * Math.PI * 2),
                (Constantes.vaste * Math.random() + 1) * speedScale,
                childTex,
                this,
                newSize
            ));
        }
    }

    private Vector2D findSafeEdgeSpawn(int spriteW, int spriteH) {
        Vector2D playerCenter = (player != null) ? player.getCenter() : new Vector2D(Constantes.WIDTH / 2.0, Constantes.HEIGHT / 2.0);
        double minSafeDist = 240.0;
        Vector2D best = new Vector2D(0, 0);
        double bestDist = -1;

        for (int attempt = 0; attempt < 20; attempt++) {
            int edge = (int) (Math.random() * 4);
            double x, y;
            if (edge == 0) {
                x = Math.random() * Constantes.WIDTH;
                y = -spriteH * 0.5;
            } else if (edge == 1) {
                x = Math.random() * Constantes.WIDTH;
                y = Constantes.HEIGHT - spriteH * 0.5;
            } else if (edge == 2) {
                x = -spriteW * 0.5;
                y = Math.random() * Constantes.HEIGHT;
            } else {
                x = Constantes.WIDTH - spriteW * 0.5;
                y = Math.random() * Constantes.HEIGHT;
            }
            double d = new Vector2D(x + spriteW / 2.0, y + spriteH / 2.0).substract(playerCenter).getMagni();
            if (d >= minSafeDist) {
                return new Vector2D(x, y);
            }
            if (d > bestDist) {
                bestDist = d;
                best = new Vector2D(x, y);
            }
        }
        return best;
    }

    private void startWave() {
        wave++;
        waveBannerTimer = WAVE_BANNER_DURATION;
        if (wave > 1) {
            Sonidos.wave.play();
        }
        if (wave >= 3) {
            ufoSpawnCooldown = 260 + (int) (Math.random() * 180);
        }
        double speedScale = getWaveSpeedMultiplier();
        for (int i = 0; i < asteroids; i++) {
            BufferedImage textura = Assets.met_g[(int) (Math.random() * Assets.met_g.length)];
            Vector2D pos = findSafeEdgeSpawn(textura.getWidth(), textura.getHeight());
            moving_obj.add(new Asteroid(
                pos,
                new Vector2D(0, 1).setDireccion(Math.random() * Math.PI * 2),
                (Constantes.vaste * Math.random() + 1) * speedScale,
                textura,
                this,
                Size.BIG
            ));
        }
        asteroids++;
    }

    private boolean hasActiveUfo() {
        for (Moving_obj obj : moving_obj) {
            if (obj instanceof Ufo) {
                return true;
            }
        }
        return false;
    }

    public void playExplosion(Vector2D posicion) {
        explosiones.add(new Animacion(Assets.expl, 45, new Vector2D(posicion.getX(), posicion.getY())));
    }

    public void update() {
        // Handle Pause toggle during active gameplay
        if (!gameOver) {
            if (Keyboard.pausePressed || Keyboard.escapePressed) {
                paused = !paused;
                if (paused) {
                    Sonidos.propulsor.stop();
                }
                return;
            }
            if (paused) {
                if (Keyboard.enterPressed || Keyboard.titlePressed) {
                    Sonidos.propulsor.stop();
                    returnToTitle = true;
                }
                return;
            }
        }

        if (shakeFrames > 0) {
            shakeFrames--;
            if (shakeFrames == 0) {
                shakeMagnitude = 0;
            }
        }
        if (waveBannerTimer > 0) {
            waveBannerTimer--;
        }
        if (comboTimer > 0) {
            comboTimer--;
            if (comboTimer == 0) {
                comboKills = 0;
            }
        }

        // Spawn enemy UFO periodically on Wave 3+ with eerie entrance cue
        if (!gameOver && wave >= 3 && !hasActiveUfo()) {
            ufoSpawnCooldown--;
            if (ufoSpawnCooldown <= 0) {
                boolean fromLeft = Math.random() < 0.5;
                double y = 90 + Math.random() * (Constantes.HEIGHT - 200);
                moving_obj.add(new Ufo(fromLeft, y, wave, this));
                Sonidos.ufoEnter.play();
                floatingTexts.add(new FloatingText(Constantes.WIDTH / 2.0, 92, "UFO INCOMING!", new Color(245, 115, 255), 75));
                ufoSpawnCooldown = Math.max(480, 960 - (wave - 3) * 70);
            }
        }

        for (Moving_obj obj : new ArrayList<Moving_obj>(moving_obj)) {
            if (moving_obj.contains(obj)) {
                obj.update();
            }
        }
        for (int i = explosiones.size() - 1; i >= 0; i--) {
            Animacion anim = explosiones.get(i);
            anim.update();
            if (!anim.isRunning()) {
                explosiones.remove(i);
            }
        }
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.update();
            if (!p.isAlive()) {
                particles.remove(i);
            }
        }
        for (int i = powerUps.size() - 1; i >= 0; i--) {
            PowerUp pu = powerUps.get(i);
            pu.update();
            if (!pu.isAlive()) {
                powerUps.remove(i);
                continue;
            }
            if (!gameOver && player != null && moving_obj.contains(player)) {
                double dist = pu.getCenter().substract(player.getCenter()).getMagni();
                if (dist < PowerUp.RADIUS + 26) {
                    applyPowerUp(pu);
                    powerUps.remove(i);
                }
            }
        }
        for (int i = floatingTexts.size() - 1; i >= 0; i--) {
            FloatingText ft = floatingTexts.get(i);
            ft.y -= 0.9;
            ft.life--;
            if (ft.life <= 0) {
                floatingTexts.remove(i);
            }
        }

        if (lives <= 0) {
            if (!gameOver) {
                gameOver = true;
                gameOverTime = System.currentTimeMillis();
                restartArmed = false;
                Sonidos.gameOver.play();
                if (highScoreStore != null) {
                    leaderboardRank = highScoreStore.record(puntaje, wave, shipStyle);
                    highScore = Math.max(highScore, highScoreStore.getTopScore());
                }
            } else {
                if (!restartArmed && !Keyboard.enter && !Keyboard.shoot && System.currentTimeMillis() - gameOverTime >= 400) {
                    restartArmed = true;
                }
                if (restartArmed && Keyboard.titlePressed) {
                    returnToTitle = true;
                }
            }
            return;
        }
        for (int i = 0; i < moving_obj.size(); i++) {
            if (moving_obj.get(i) instanceof Asteroid) {
                return;
            }
        }
        startWave();
    }

    private void applyPowerUp(PowerUp pu) {
        Sonidos.powerUp.play();
        switch (pu.getType()) {
            case SHIELD:
                player.activateShield();
                break;
            case RAPID_FIRE:
                player.activateRapidFire();
                break;
            case SPREAD_SHOT:
                player.activateSpreadShot();
                break;
        }
        floatingTexts.add(new FloatingText(
            pu.getCenter().getX(),
            pu.getCenter().getY(),
            pu.getType().label + "!",
            pu.getType().color,
            60
        ));
    }

    public void draw(Graphics g) {
        Graphics2D G2D = (Graphics2D) g;
        Font prevFont = G2D.getFont();
        Color prevColor = G2D.getColor();
        AffineTransform prevTransform = G2D.getTransform();

        G2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        G2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Apply subtle camera shake to gameplay world only
        if (shakeFrames > 0 && !paused) {
            double dx = (Math.random() * 2 - 1) * shakeMagnitude;
            double dy = (Math.random() * 2 - 1) * shakeMagnitude;
            G2D.translate(dx, dy);
        }

        for (int i = 0; i < powerUps.size(); i++) {
            powerUps.get(i).draw(G2D);
        }
        for (int i = 0; i < particles.size(); i++) {
            particles.get(i).draw(G2D);
        }
        for (int i = 0; i < moving_obj.size(); i++) {
            moving_obj.get(i).draw(g);
        }
        for (int i = 0; i < explosiones.size(); i++) {
            explosiones.get(i).draw(G2D);
        }
        for (int i = 0; i < floatingTexts.size(); i++) {
            FloatingText ft = floatingTexts.get(i);
            int alpha = Math.min(255, Math.max(0, (int) (255.0 * ft.life / ft.maxLife)));
            G2D.setFont(Assets.fontSmall);
            G2D.setColor(new Color(ft.color.getRed(), ft.color.getGreen(), ft.color.getBlue(), alpha));
            FontMetrics fm = G2D.getFontMetrics();
            G2D.drawString(ft.text, (int) (ft.x - fm.stringWidth(ft.text) / 2.0), (int) ft.y);
        }

        // Reset camera shake before HUD and overlays
        G2D.setTransform(prevTransform);

        puntajes(G2D);
        vidas(G2D);
        drawPowerUpAndDashBars(G2D);
        drawWaveHud(G2D);

        if (waveBannerTimer > 0 && !gameOver) {
            drawWaveBanner(G2D);
        }
        if (paused && !gameOver) {
            drawPaused(G2D);
        }
        if (gameOver) {
            drawGameOver(G2D);
        }

        G2D.setFont(prevFont);
        G2D.setColor(prevColor);
    }

    private void drawPowerUpAndDashBars(Graphics2D g2d) {
        if (player == null || gameOver) return;
        int x = 25;
        int y = 66;
        // Dash indicator
        Color dashColor = player.isDashReady() ? shipStyle.getAccentColor().brighter() : new Color(110, 125, 155);
        String dashLabel = player.isDashReady() ? "DASH READY (SHIFT)" : "DASH";
        drawStatusBar(g2d, x, y, dashLabel, player.getDashCooldownRatio(), dashColor);
        y += 24;

        if (player.hasShield()) {
            drawStatusBar(g2d, x, y, "SHIELD", player.getShieldRatio(), PowerUp.Type.SHIELD.color);
            y += 24;
        }
        if (player.hasRapidFire()) {
            drawStatusBar(g2d, x, y, "RAPID FIRE", player.getRapidFireRatio(), PowerUp.Type.RAPID_FIRE.color);
            y += 24;
        }
        if (player.hasSpreadShot()) {
            drawStatusBar(g2d, x, y, "TRIPLE SHOT", player.getSpreadShotRatio(), PowerUp.Type.SPREAD_SHOT.color);
        }
    }

    private void drawStatusBar(Graphics2D g2d, int x, int y, String label, float ratio, Color c) {
        int barW = 112;
        int barH = 6;
        g2d.setFont(Assets.fontTiny);
        g2d.setColor(c);
        g2d.drawString(label, x, y);
        g2d.setColor(new Color(20, 28, 48, 190));
        g2d.fillRoundRect(x, y + 4, barW, barH, 4, 4);
        g2d.setColor(c);
        g2d.fillRoundRect(x, y + 4, Math.max(2, (int) (barW * Math.min(1f, Math.max(0f, ratio)))), barH, 4, 4);
    }

    private void drawWaveHud(Graphics2D g2d) {
        g2d.setFont(Assets.fontSmall);
        g2d.setColor(new Color(185, 205, 235, 210));
        drawCenteredString(g2d, "WAVE " + wave, 42);
        if (Sonidos.isMuted()) {
            g2d.setFont(Assets.fontTiny);
            g2d.setColor(new Color(235, 110, 100, 210));
            drawCenteredString(g2d, "MUTED (M)", 62);
        }
    }

    private void drawWaveBanner(Graphics2D g2d) {
        double progress = 1.0 - (double) waveBannerTimer / WAVE_BANNER_DURATION;
        double fade;
        if (progress < 0.2) {
            fade = progress / 0.2;
        } else if (progress > 0.7) {
            fade = (1.0 - progress) / 0.3;
        } else {
            fade = 1.0;
        }
        int alpha = Math.min(255, Math.max(0, (int) (255 * fade)));
        Color accent = shipStyle.getAccentColor();
        g2d.setFont(Assets.fontBig);
        g2d.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), alpha / 2));
        drawCenteredString(g2d, "WAVE " + wave, 192);
        g2d.setColor(new Color(255, 255, 255, alpha));
        drawCenteredString(g2d, "WAVE " + wave, 190);
        if (wave == 3) {
            g2d.setFont(Assets.fontSmall);
            g2d.setColor(new Color(245, 115, 255, alpha));
            drawCenteredString(g2d, "WARNING: ALIEN SAUCERS DETECTED", 224);
        }
    }

    private void drawPaused(Graphics2D g2d) {
        g2d.setColor(new Color(4, 6, 16, 170));
        g2d.fillRect(0, 0, Constantes.WIDTH, Constantes.HEIGHT);

        int centerY = Constantes.HEIGHT / 2;
        g2d.setFont(Assets.fontBig);
        g2d.setColor(shipStyle.getAccentColor().brighter());
        drawCenteredString(g2d, "PAUSED", centerY - 35);

        g2d.setFont(Assets.fontSmall);
        g2d.setColor(Color.WHITE);
        drawCenteredString(g2d, "PRESS P OR ESC TO RESUME", centerY + 25);

        g2d.setColor(new Color(185, 195, 215));
        drawCenteredString(g2d, "PRESS ENTER FOR TITLE SCREEN", centerY + 55);

        g2d.setColor(new Color(145, 160, 185));
        drawCenteredString(g2d, "PRESS M TO " + (Sonidos.isMuted() ? "UNMUTE" : "MUTE") + " SOUND", centerY + 85);
    }

    private void drawGameOver(Graphics2D g2d) {
        g2d.setColor(new Color(4, 6, 16, 185));
        g2d.fillRect(0, 0, Constantes.WIDTH, Constantes.HEIGHT);

        int topY = 170;
        g2d.setFont(Assets.fontBig);
        g2d.setColor(new Color(235, 64, 52));
        drawCenteredString(g2d, "GAME OVER", topY);

        g2d.setFont(Assets.fontMed);
        g2d.setColor(Color.WHITE);
        drawCenteredString(g2d, "SCORE: " + puntaje + "   (WAVE " + wave + ")", topY + 52);

        g2d.setFont(Assets.fontSmall);
        if (newHighScore && puntaje > 0) {
            g2d.setColor(new Color(255, 215, 80));
            drawCenteredString(g2d, "NEW HIGH SCORE!", topY + 84);
        } else if (leaderboardRank > 0) {
            g2d.setColor(new Color(120, 235, 255));
            drawCenteredString(g2d, "RANK #" + leaderboardRank + " ON LEADERBOARD!", topY + 84);
        } else if (highScore > 0) {
            g2d.setColor(new Color(180, 195, 220));
            drawCenteredString(g2d, "BEST: " + highScore, topY + 84);
        }

        // Compact Top 5 Leaderboard panel if available
        int nextY = topY + 130;
        if (highScoreStore != null && !highScoreStore.getEntries().isEmpty()) {
            List<HighScoreStore.Entry> entries = highScoreStore.getEntries();
            int panelW = 420;
            int rowH = 26;
            int panelH = 38 + entries.size() * rowH;
            int px = (Constantes.WIDTH - panelW) / 2;
            int py = topY + 108;

            g2d.setColor(new Color(12, 18, 36, 190));
            g2d.fillRoundRect(px, py, panelW, panelH, 14, 14);
            g2d.setColor(new Color(90, 115, 160, 130));
            g2d.drawRoundRect(px, py, panelW, panelH, 14, 14);

            g2d.setFont(Assets.fontTiny);
            g2d.setColor(new Color(180, 200, 230));
            drawCenteredString(g2d, "TOP PILOTS", py + 22);

            for (int i = 0; i < entries.size(); i++) {
                HighScoreStore.Entry e = entries.get(i);
                int ry = py + 46 + i * rowH;
                boolean isMine = (i + 1 == leaderboardRank && e.score == puntaje);
                g2d.setColor(isMine ? new Color(255, 220, 85) : new Color(215, 225, 240));
                g2d.drawString("#" + (i + 1) + "  " + e.score, px + 24, ry);
                g2d.setColor(isMine ? new Color(255, 220, 85) : new Color(155, 175, 205));
                g2d.drawString("WAVE " + e.wave, px + 185, ry);
                g2d.drawString(e.shipName, px + 290, ry);
            }
            nextY = py + panelH + 46;
        }

        g2d.setFont(Assets.fontSmall);
        g2d.setColor(Color.WHITE);
        drawCenteredString(g2d, "PRESS ENTER OR SPACE TO RESTART", nextY);

        g2d.setColor(new Color(185, 200, 225));
        drawCenteredString(g2d, "PRESS T FOR TITLE SCREEN", nextY + 30);

        g2d.setColor(new Color(145, 155, 175));
        drawCenteredString(g2d, "PRESS ESC TO QUIT", nextY + 60);
    }

    private void drawCenteredString(Graphics2D g2d, String text, int y) {
        FontMetrics fm = g2d.getFontMetrics();
        int x = (Constantes.WIDTH - fm.stringWidth(text)) / 2;
        g2d.drawString(text, x, y);
    }

    public boolean isPaused() { return paused; }
    public boolean isGameOver() { return gameOver; }
    public boolean shouldRestart() { return gameOver && restartArmed && (Keyboard.enter || Keyboard.shoot); }
    public boolean shouldReturnToTitle() { return returnToTitle; }
    public int getHighScore() { return highScore; }
    public int getPuntaje() { return puntaje; }
    public int getWave() { return wave; }
    public ArrayList<PowerUp> getPowerUps() { return powerUps; }

    public Vector2D getPlayerVelocity() {
        if (player != null && moving_obj.contains(player)) {
            return player.getVelocidad();
        }
        return new Vector2D(0, 0);
    }

    private void vidas(Graphics2D g) {
        int x = 25;
        int y = 20;
        g.drawImage(shipStyle.getLifeTexture(), x, y, null);
        g.drawImage(Assets.num[10], x + 40, y + 7, null);
        String liveToString = Integer.toString(Math.max(0, lives));
        int dx = x + 62;
        for (int i = 0; i < liveToString.length(); i++) {
            int numero = Integer.parseInt(liveToString.substring(i, i + 1));
            g.drawImage(Assets.num[numero], dx, y + 7, null);
            dx += 20;
        }
    }

    private void puntajes(Graphics2D g) {
        String scoreToString = Integer.toString(puntaje);
        int digitStep = 20;
        int totalWidth = scoreToString.length() * digitStep;
        int startX = Constantes.WIDTH - 25 - totalWidth;
        int y = 24;
        for (int i = 0; i < scoreToString.length(); i++) {
            g.drawImage(Assets.num[Integer.parseInt(scoreToString.substring(i, i + 1))], startX + i * digitStep, y, null);
        }
        if (highScore > 0) {
            g.setFont(Assets.fontTiny);
            g.setColor(new Color(175, 190, 215, 200));
            String hi = "HI " + highScore;
            FontMetrics fm = g.getFontMetrics();
            g.drawString(hi, Constantes.WIDTH - 25 - fm.stringWidth(hi), y + 36);
        }
        int mult = getComboMultiplier();
        if (comboTimer > 0 && mult > 1 && !gameOver) {
            Color c = (mult >= 3) ? new Color(255, 120, 240) : new Color(100, 235, 255);
            g.setFont(Assets.fontSmall);
            g.setColor(c);
            String comboTxt = "COMBO x" + mult;
            FontMetrics fm = g.getFontMetrics();
            int cx = Constantes.WIDTH - 25 - fm.stringWidth(comboTxt);
            int cy = y + 60;
            g.drawString(comboTxt, cx, cy);

            int barW = fm.stringWidth(comboTxt);
            float ratio = (float) comboTimer / COMBO_WINDOW;
            g.setColor(new Color(20, 28, 48, 190));
            g.fillRoundRect(cx, cy + 5, barW, 5, 3, 3);
            g.setColor(c);
            g.fillRoundRect(cx, cy + 5, Math.max(2, (int) (barW * ratio)), 5, 3, 3);
        }
    }

    public ArrayList<Moving_obj> getMoving_obj() { return moving_obj; }
    public Jugador getJugador() { return player; }

    public void restarvida() {
        if (lives > 0) {
            lives--;
        }
    }
}
