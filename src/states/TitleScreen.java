package states;

import GameObjects.Constantes;
import GameObjects.ShipStyle;
import graphics.Assets;
import input.Keyboard;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Random;
import main.Sonidos;

public class TitleScreen {
    private static class AmbientRock {
        double x, y, vx, vy, angle, rotSpeed;
        BufferedImage sprite;
    }

    private final AmbientRock[] rocks;
    private ShipStyle selectedShip;
    private int highScore;
    private int tick = 0;
    private boolean startRequested = false;
    private boolean quitRequested = false;

    public TitleScreen(ShipStyle initialShip, int highScore) {
        this.selectedShip = initialShip != null ? initialShip : ShipStyle.BLUE;
        this.highScore = highScore;
        Random rng = new Random();
        rocks = new AmbientRock[7];
        for (int i = 0; i < rocks.length; i++) {
            AmbientRock r = new AmbientRock();
            r.x = rng.nextDouble() * Constantes.WIDTH;
            r.y = rng.nextDouble() * Constantes.HEIGHT;
            double a = rng.nextDouble() * Math.PI * 2;
            double sp = 0.4 + rng.nextDouble() * 1.1;
            r.vx = Math.cos(a) * sp;
            r.vy = Math.sin(a) * sp;
            r.angle = rng.nextDouble() * Math.PI * 2;
            r.rotSpeed = (rng.nextBoolean() ? 1 : -1) * (0.008 + rng.nextDouble() * 0.02);
            r.sprite = (i % 2 == 0)
                ? Assets.met_g[rng.nextInt(Assets.met_g.length)]
                : Assets.met_m[rng.nextInt(Assets.met_m.length)];
            rocks[i] = r;
        }
    }

    public void setHighScore(int highScore) {
        this.highScore = Math.max(this.highScore, highScore);
    }

    public void resetRequests() {
        startRequested = false;
        quitRequested = false;
    }

    public void update() {
        tick++;
        for (AmbientRock r : rocks) {
            r.x += r.vx;
            r.y += r.vy;
            r.angle += r.rotSpeed;
            int w = r.sprite.getWidth();
            int h = r.sprite.getHeight();
            if (r.x > Constantes.WIDTH) r.x = -w;
            else if (r.x < -w) r.x = Constantes.WIDTH;
            if (r.y > Constantes.HEIGHT) r.y = -h;
            else if (r.y < -h) r.y = Constantes.HEIGHT;
        }

        if (Keyboard.leftPressed) {
            selectedShip = selectedShip.prev();
            Sonidos.Disparo.play();
        } else if (Keyboard.rightPressed) {
            selectedShip = selectedShip.next();
            Sonidos.Disparo.play();
        }

        if (Keyboard.enterPressed || Keyboard.shootPressed) {
            startRequested = true;
        } else if (Keyboard.escapePressed) {
            quitRequested = true;
        }
    }

    public void draw(Graphics2D g2d) {
        Font prevFont = g2d.getFont();
        Color prevColor = g2d.getColor();
        Stroke prevStroke = g2d.getStroke();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Ambient spinning asteroids in background
        for (AmbientRock r : rocks) {
            AffineTransform at = AffineTransform.getTranslateInstance(r.x, r.y);
            at.rotate(r.angle, r.sprite.getWidth() / 2.0, r.sprite.getHeight() / 2.0);
            g2d.drawImage(r.sprite, at, null);
        }

        // Subtle dark veil for legibility
        g2d.setColor(new Color(4, 6, 16, 115));
        g2d.fillRect(0, 0, Constantes.WIDTH, Constantes.HEIGHT);

        Color accent = selectedShip.getAccentColor();

        // Title with glow
        g2d.setFont(Assets.fontTitle);
        String title = "SPACE SHIPS";
        String subtitle = "BLASTER";
        drawGlowingCentered(g2d, title, 130, accent, Color.WHITE);
        g2d.setFont(Assets.fontBig);
        drawGlowingCentered(g2d, subtitle, 185, accent, accent.brighter());

        if (highScore > 0) {
            g2d.setFont(Assets.fontSmall);
            g2d.setColor(new Color(255, 215, 90));
            drawCentered(g2d, "HIGH SCORE: " + highScore, 224);
        }

        // Ship picker header
        g2d.setFont(Assets.fontSmall);
        g2d.setColor(new Color(195, 210, 235));
        drawCentered(g2d, "SELECT YOUR SHIP  -  LEFT / RIGHT ARROWS", 272);

        // 3 ship cards
        ShipStyle[] styles = ShipStyle.values();
        int cardW = 195;
        int cardH = 190;
        int gap = 36;
        int totalW = styles.length * cardW + (styles.length - 1) * gap;
        int startX = (Constantes.WIDTH - totalW) / 2;
        int cardY = 290;

        for (int i = 0; i < styles.length; i++) {
            ShipStyle style = styles[i];
            boolean isSel = (style == selectedShip);
            int cx = startX + i * (cardW + gap);
            int midX = cx + cardW / 2;
            Color c = style.getAccentColor();

            // Card background
            g2d.setColor(isSel ? new Color(c.getRed(), c.getGreen(), c.getBlue(), 45) : new Color(12, 16, 32, 145));
            g2d.fillRoundRect(cx, cardY, cardW, cardH, 18, 18);

            // Card border
            g2d.setStroke(new BasicStroke(isSel ? 2.5f : 1.2f));
            g2d.setColor(isSel ? c : new Color(90, 105, 135, 120));
            g2d.drawRoundRect(cx, cardY, cardW, cardH, 18, 18);

            // Laser preview cleanly above ship nose
            BufferedImage laserImg = style.getLaserTexture();
            int bob = isSel ? (int) (Math.sin(tick * 0.1) * 3) : 0;
            int laserH = 38;
            g2d.drawImage(laserImg, midX - laserImg.getWidth() / 2, cardY + 18 + bob, laserImg.getWidth(), laserH, null);

            // Ship sprite
            BufferedImage shipImg = style.getShipTexture();
            double scale = isSel ? 1.45 : 1.15;
            int sw = (int) (shipImg.getWidth() * scale);
            int sh = (int) (shipImg.getHeight() * scale);
            g2d.drawImage(shipImg, midX - sw / 2, cardY + 106 - sh / 2 + bob, sw, sh, null);

            // Ship label
            g2d.setFont(Assets.fontTiny);
            g2d.setColor(isSel ? Color.WHITE : new Color(150, 165, 190));
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(style.getDisplayName(), midX - fm.stringWidth(style.getDisplayName()) / 2, cardY + cardH - 18);
        }

        // Pulsing Start Prompt
        int pulseAlpha = 140 + (int) (115 * (0.5 + 0.5 * Math.sin(tick * 0.08)));
        g2d.setFont(Assets.fontMed);
        g2d.setColor(new Color(255, 255, 255, pulseAlpha));
        drawCentered(g2d, "PRESS ENTER OR SPACE TO PLAY", 545);

        // Controls footer
        g2d.setFont(Assets.fontTiny);
        g2d.setColor(new Color(165, 180, 205));
        drawCentered(g2d, "ARROWS: FLY   |   SPACE: SHOOT   |   P / ESC: PAUSE   |   M: " + (Sonidos.isMuted() ? "UNMUTE" : "MUTE") + "   |   F3: FPS", 625);

        g2d.setColor(new Color(120, 135, 160));
        drawCentered(g2d, "ESC TO QUIT", 655);

        g2d.setFont(prevFont);
        g2d.setColor(prevColor);
        g2d.setStroke(prevStroke);
    }

    private void drawGlowingCentered(Graphics2D g2d, String text, int y, Color glow, Color fill) {
        FontMetrics fm = g2d.getFontMetrics();
        int x = (Constantes.WIDTH - fm.stringWidth(text)) / 2;
        g2d.setColor(new Color(glow.getRed(), glow.getGreen(), glow.getBlue(), 75));
        g2d.drawString(text, x - 2, y + 2);
        g2d.drawString(text, x + 2, y + 2);
        g2d.drawString(text, x, y + 3);
        g2d.setColor(fill);
        g2d.drawString(text, x, y);
    }

    private void drawCentered(Graphics2D g2d, String text, int y) {
        FontMetrics fm = g2d.getFontMetrics();
        int x = (Constantes.WIDTH - fm.stringWidth(text)) / 2;
        g2d.drawString(text, x, y);
    }

    public ShipStyle getSelectedShip() {
        return selectedShip;
    }

    public boolean shouldStart() {
        return startRequested;
    }

    public boolean shouldQuit() {
        return quitRequested;
    }
}
