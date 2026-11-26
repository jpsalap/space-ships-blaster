package states;

import GameObjects.Constantes;
import GameObjects.ShipStyle;
import graphics.Assets;
import input.ControlScheme;
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
import java.util.List;
import java.util.Random;
import main.HighScoreStore;
import main.Sonidos;

public class TitleScreen {
    private static class AmbientRock {
        double x, y, vx, vy, angle, rotSpeed;
        BufferedImage sprite;
    }

    private final AmbientRock[] rocks;
    private final HighScoreStore highScoreStore;
    private ShipStyle selectedShip;
    private ControlScheme controlScheme;
    private int highScore;
    private int tick = 0;
    private boolean showScores = false;
    private boolean startRequested = false;
    private boolean quitRequested = false;

    public TitleScreen(ShipStyle initialShip, int highScore) {
        this(initialShip, highScore, null, ControlScheme.CLASSIC);
    }

    public TitleScreen(ShipStyle initialShip, int highScore, HighScoreStore highScoreStore) {
        this(initialShip, highScore, highScoreStore, ControlScheme.CLASSIC);
    }

    public TitleScreen(ShipStyle initialShip, int highScore, HighScoreStore highScoreStore, ControlScheme controlScheme) {
        this.selectedShip = initialShip != null ? initialShip : ShipStyle.BLUE;
        this.controlScheme = controlScheme != null ? controlScheme : ControlScheme.CLASSIC;
        this.highScoreStore = highScoreStore;
        int stored = (highScoreStore != null) ? highScoreStore.getTopScore() : 0;
        this.highScore = Math.max(highScore, stored);
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
        int stored = (highScoreStore != null) ? highScoreStore.getTopScore() : 0;
        this.highScore = Math.max(Math.max(this.highScore, highScore), stored);
    }

    public void setControlScheme(ControlScheme scheme) {
        if (scheme != null) {
            this.controlScheme = scheme;
        }
    }

    public ControlScheme getControlScheme() {
        return controlScheme;
    }

    public void resetRequests() {
        startRequested = false;
        quitRequested = false;
        showScores = false;
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

        if (Keyboard.scoresPressed) {
            showScores = !showScores;
            return;
        }
        if (showScores) {
            if (Keyboard.escapePressed || Keyboard.enterPressed || Keyboard.shootPressed) {
                showScores = false;
            }
            return;
        }

        if (Keyboard.leftPressed) {
            selectedShip = selectedShip.prev();
            Sonidos.Disparo.play();
        } else if (Keyboard.rightPressed) {
            selectedShip = selectedShip.next();
            Sonidos.Disparo.play();
        }

        if (Keyboard.controlsPressed) {
            controlScheme = controlScheme.next();
            controlScheme.save();
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
        drawCentered(g2d, "SELECT YOUR STARFIGHTER  -  A / D  OR  LEFT / RIGHT", 242);

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

            g2d.setColor(isSel ? new Color(c.getRed(), c.getGreen(), c.getBlue(), 42) : new Color(12, 16, 32, 150));
            g2d.fillRoundRect(cx, cardY, cardW, cardH, 18, 18);

            g2d.setStroke(new BasicStroke(isSel ? 2.5f : 1.2f));
            g2d.setColor(isSel ? c : new Color(90, 105, 135, 120));
            g2d.drawRoundRect(cx, cardY, cardW, cardH, 18, 18);

            BufferedImage laserImg = style.getLaserTexture();
            int bob = isSel ? (int) (Math.sin(tick * 0.1) * 3) : 0;
            int laserH = 34;
            g2d.drawImage(laserImg, midX - laserImg.getWidth() / 2, cardY + 14 + bob, laserImg.getWidth(), laserH, null);

            BufferedImage shipImg = style.getShipTexture();
            int sw = isSel ? 76 : 62;
            int sh = isSel ? 76 : 62;
            g2d.drawImage(shipImg, midX - sw / 2, cardY + 95 - sh / 2 + bob, sw, sh, null);

            g2d.setFont(Assets.fontSmall);
            g2d.setColor(isSel ? Color.WHITE : new Color(165, 180, 205));
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(style.getDisplayName(), midX - fm.stringWidth(style.getDisplayName()) / 2, cardY + 150);

            g2d.setFont(Assets.fontTiny);
            g2d.setColor(isSel ? c.brighter() : new Color(120, 135, 165));
            FontMetrics fmTiny = g2d.getFontMetrics();
            g2d.drawString(style.getRoleTag(), midX - fmTiny.stringWidth(style.getRoleTag()) / 2, cardY + 168);

            drawStatRow(g2d, cx + 22, cardY + 188, "SPD", style.getSpeedRating(), c, isSel);
            drawStatRow(g2d, cx + 22, cardY + 207, "FIRE", style.getFireRating(), c, isSel);
            drawStatRow(g2d, cx + 22, cardY + 226, "TURN", style.getAgilityRating(), c, isSel);
        }

        // Pulsing Start Prompt
        int pulseAlpha = 140 + (int) (115 * (0.5 + 0.5 * Math.sin(tick * 0.08)));
        g2d.setFont(Assets.fontMed);
        g2d.setColor(new Color(255, 255, 255, pulseAlpha));
        drawCentered(g2d, "PRESS ENTER OR SPACE TO PLAY", 554);

        // Control Scheme Selector pills (press C to switch)
        drawControlSelector(g2d, 586, accent);

        // Active control hint & secondary shortcuts
        g2d.setFont(Assets.fontTiny);
        g2d.setColor(new Color(175, 192, 220));
        drawCentered(g2d, controlScheme.getHint(), 638);

        g2d.setColor(new Color(125, 140, 168));
        drawCentered(g2d, "H: TOP SCORES   |   P / ESC: PAUSE   |   M: " + (Sonidos.isMuted() ? "UNMUTE" : "MUTE") + "   |   F3: FPS   |   ESC: QUIT", 666);

        if (showScores) {
            drawLeaderboardOverlay(g2d, accent);
        }

        g2d.setFont(prevFont);
        g2d.setColor(prevColor);
        g2d.setStroke(prevStroke);
    }

    private void drawControlSelector(Graphics2D g2d, int y, Color accent) {
        ControlScheme[] modes = ControlScheme.values();
        g2d.setFont(Assets.fontTiny);
        FontMetrics fm = g2d.getFontMetrics();
        String prefix = "CONTROLS (C): ";
        int prefixW = fm.stringWidth(prefix);
        int pillPadX = 14;
        int pillH = 24;
        int pillGap = 10;

        int totalW = prefixW;
        int[] pillWidths = new int[modes.length];
        for (int i = 0; i < modes.length; i++) {
            pillWidths[i] = fm.stringWidth(modes[i].getLabel()) + pillPadX * 2;
            totalW += pillWidths[i];
            if (i < modes.length - 1) totalW += pillGap;
        }

        int x = (Constantes.WIDTH - totalW) / 2;
        g2d.setColor(new Color(185, 200, 225));
        g2d.drawString(prefix, x, y + 16);
        x += prefixW;

        for (int i = 0; i < modes.length; i++) {
            boolean active = (modes[i] == controlScheme);
            int pw = pillWidths[i];
            if (active) {
                g2d.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 60));
                g2d.fillRoundRect(x, y, pw, pillH, 10, 10);
                g2d.setStroke(new BasicStroke(1.8f));
                g2d.setColor(accent);
                g2d.drawRoundRect(x, y, pw, pillH, 10, 10);
                g2d.setColor(Color.WHITE);
            } else {
                g2d.setColor(new Color(16, 22, 42, 160));
                g2d.fillRoundRect(x, y, pw, pillH, 10, 10);
                g2d.setStroke(new BasicStroke(1.0f));
                g2d.setColor(new Color(80, 95, 125, 130));
                g2d.drawRoundRect(x, y, pw, pillH, 10, 10);
                g2d.setColor(new Color(145, 160, 188));
            }
            String lbl = modes[i].getLabel();
            g2d.drawString(lbl, x + (pw - fm.stringWidth(lbl)) / 2, y + 16);
            x += pw + pillGap;
        }
    }

    private void drawLeaderboardOverlay(Graphics2D g2d, Color accent) {
        g2d.setColor(new Color(4, 6, 16, 205));
        g2d.fillRect(0, 0, Constantes.WIDTH, Constantes.HEIGHT);

        int panelW = 620;
        int panelH = 330;
        int px = (Constantes.WIDTH - panelW) / 2;
        int py = (Constantes.HEIGHT - panelH) / 2;

        g2d.setColor(new Color(12, 18, 36, 238));
        g2d.fillRoundRect(px, py, panelW, panelH, 18, 18);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.setColor(accent);
        g2d.drawRoundRect(px, py, panelW, panelH, 18, 18);

        g2d.setFont(Assets.fontMed);
        g2d.setColor(Color.WHITE);
        drawCentered(g2d, "TOP 5 PILOTS", py + 46);

        List<HighScoreStore.Entry> entries = (highScoreStore != null) ? highScoreStore.getEntries() : null;
        if (entries == null || entries.isEmpty()) {
            g2d.setFont(Assets.fontSmall);
            g2d.setColor(new Color(165, 180, 205));
            drawCentered(g2d, "NO SCORES RECORDED YET", py + 170);
        } else {
            g2d.setFont(Assets.fontTiny);
            g2d.setColor(new Color(135, 155, 185));
            g2d.drawString("PILOT", px + 34, py + 82);
            g2d.drawString("SCORE", px + 260, py + 82);
            g2d.drawString("WAVE", px + 380, py + 82);
            g2d.drawString("SHIP", px + 460, py + 82);

            g2d.setFont(Assets.fontSmall);
            for (int i = 0; i < entries.size(); i++) {
                HighScoreStore.Entry e = entries.get(i);
                int ry = py + 114 + i * 36;
                g2d.setColor(i == 0 ? new Color(255, 215, 80) : Color.WHITE);
                g2d.drawString("#" + (i + 1) + "  " + e.playerName, px + 34, ry);
                g2d.drawString(Integer.toString(e.score), px + 260, ry);
                g2d.setColor(i == 0 ? new Color(255, 215, 80) : new Color(165, 185, 215));
                g2d.drawString("W" + e.wave, px + 380, ry);
                g2d.setFont(Assets.fontTiny);
                g2d.drawString(e.shipName, px + 460, ry);
                g2d.setFont(Assets.fontSmall);
            }
        }

        g2d.setFont(Assets.fontTiny);
        g2d.setColor(new Color(165, 180, 205));
        drawCentered(g2d, "PRESS H OR ESC TO CLOSE", py + panelH - 22);
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

    public boolean isShowingScores() {
        return showScores;
    }
}
