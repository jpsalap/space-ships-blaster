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
        drawGlowingCentered(g2d, "SPACE SHIPS", 112, accent, Color.WHITE);
        g2d.setFont(Assets.fontBig);
        drawGlowingCentered(g2d, "BLASTER", 164, accent, accent.brighter());

        if (highScore > 0) {
            g2d.setFont(Assets.fontSmall);
            g2d.setColor(new Color(255, 215, 90));
            drawCentered(g2d, "HIGH SCORE: " + highScore, 200);
        }

        // Ship picker header
        g2d.setFont(Assets.fontSmall);
        g2d.setColor(new Color(195, 210, 235));
        drawCentered(g2d, "SELECT YOUR STARFIGHTER  -  LEFT / RIGHT ARROWS", 242);

        // 3 ship cards with stat bars
        ShipStyle[] styles = ShipStyle.values();
        int cardW = 225;
        int cardH = 248;
        int gap = 30;
        int totalW = styles.length * cardW + (styles.length - 1) * gap;
        int startX = (Constantes.WIDTH - totalW) / 2;
        int cardY = 262;

        for (int i = 0; i < styles.length; i++) {
            ShipStyle style = styles[i];
            boolean isSel = (style == selectedShip);
            int cx = startX + i * (cardW + gap);
            int midX = cx + cardW / 2;
            Color c = style.getAccentColor();

            // Card background
            g2d.setColor(isSel ? new Color(c.getRed(), c.getGreen(), c.getBlue(), 42) : new Color(12, 16, 32, 150));
            g2d.fillRoundRect(cx, cardY, cardW, cardH, 18, 18);

            // Card border
            g2d.setStroke(new BasicStroke(isSel ? 2.5f : 1.2f));
            g2d.setColor(isSel ? c : new Color(90, 105, 135, 120));
            g2d.drawRoundRect(cx, cardY, cardW, cardH, 18, 18);

            // Laser preview cleanly above ship nose
            BufferedImage laserImg = style.getLaserTexture();
            int bob = isSel ? (int) (Math.sin(tick * 0.1) * 3) : 0;
            int laserH = 34;
            g2d.drawImage(laserImg, midX - laserImg.getWidth() / 2, cardY + 14 + bob, laserImg.getWidth(), laserH, null);

            // Ship sprite (HD 96x96 scaled cleanly)
            BufferedImage shipImg = style.getShipTexture();
            int sw = isSel ? 76 : 62;
            int sh = isSel ? 76 : 62;
            g2d.drawImage(shipImg, midX - sw / 2, cardY + 95 - sh / 2 + bob, sw, sh, null);

            // Ship name & role tag
            g2d.setFont(Assets.fontSmall);
            g2d.setColor(isSel ? Color.WHITE : new Color(165, 180, 205));
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(style.getDisplayName(), midX - fm.stringWidth(style.getDisplayName()) / 2, cardY + 150);

            g2d.setFont(Assets.fontTiny);
            g2d.setColor(isSel ? c.brighter() : new Color(120, 135, 165));
            FontMetrics fmTiny = g2d.getFontMetrics();
            g2d.drawString(style.getRoleTag(), midX - fmTiny.stringWidth(style.getRoleTag()) / 2, cardY + 168);

            // Stat bars: SPD, FIRE, TURN
            drawStatRow(g2d, cx + 22, cardY + 188, "SPD", style.getSpeedRating(), c, isSel);
            drawStatRow(g2d, cx + 22, cardY + 207, "FIRE", style.getFireRating(), c, isSel);
            drawStatRow(g2d, cx + 22, cardY + 226, "TURN", style.getAgilityRating(), c, isSel);
        }

        // Pulsing Start Prompt
        int pulseAlpha = 140 + (int) (115 * (0.5 + 0.5 * Math.sin(tick * 0.08)));
        g2d.setFont(Assets.fontMed);
        g2d.setColor(new Color(255, 255, 255, pulseAlpha));
        drawCentered(g2d, "PRESS ENTER OR SPACE TO PLAY", 568);

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

    private void drawStatRow(Graphics2D g2d, int x, int y, String label, int rating, Color accent, boolean selected) {
        g2d.setFont(Assets.fontTiny);
        g2d.setColor(selected ? new Color(210, 222, 240) : new Color(130, 145, 170));
        g2d.drawString(label, x, y);

        int barX = x + 58;
        int segW = 20;
        int segH = 7;
        int segGap = 5;
        for (int s = 0; s < 5; s++) {
            int sx = barX + s * (segW + segGap);
            if (s < rating) {
                g2d.setColor(selected ? accent : new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 130));
            } else {
                g2d.setColor(new Color(45, 55, 78, 160));
            }
            g2d.fillRoundRect(sx, y - 8, segW, segH, 3, 3);
        }
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
