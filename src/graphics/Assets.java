package graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class Assets {
    public static BufferedImage playerBlue, playerGreen, playerRed;
    public static BufferedImage lifeBlue, lifeGreen, lifeRed;
    public static BufferedImage ufo;
    public static BufferedImage blueLaser, greenLaser, redLaser;
    public static BufferedImage enemyPlasmaOrb;
    public static BufferedImage shieldBubbleSprite;
    public static Font fontTitle, fontBig, fontMed, fontSmall, fontTiny;
    public static BufferedImage[] met_g = new BufferedImage[4];
    public static BufferedImage[] met_m = new BufferedImage[2];
    public static BufferedImage[] met_s = new BufferedImage[2];
    public static BufferedImage[] met_t = new BufferedImage[2];
    public static BufferedImage[] expl = new BufferedImage[9];
    public static BufferedImage[] num = new BufferedImage[11];

    public static void inicio() {
        playerBlue = Loader.ImageLoader("/naves/ship_blue.png");
        playerGreen = Loader.ImageLoader("/naves/ship_green.png");
        playerRed = Loader.ImageLoader("/naves/ship_red.png");

        lifeBlue = Loader.ImageLoader("/vidas/life_blue.png");
        lifeGreen = Loader.ImageLoader("/vidas/life_green.png");
        lifeRed = Loader.ImageLoader("/vidas/life_red.png");

        ufo = Loader.ImageLoader("/naves/ufo.png");

        blueLaser = Loader.ImageLoader("/lasers/laserBlue01.png");
        greenLaser = Loader.ImageLoader("/lasers/laserGreen11.png");
        redLaser = Loader.ImageLoader("/lasers/laserRed01.png");

        enemyPlasmaOrb = createEnemyPlasmaOrb();
        shieldBubbleSprite = createShieldBubbleSprite();

        for (int i = 0; i < met_g.length; i++) {
            met_g[i] = Loader.ImageLoader("/asteroids/big" + (i + 1) + ".png");
        }
        for (int i = 0; i < met_m.length; i++) {
            met_m[i] = Loader.ImageLoader("/asteroids/med" + (i + 1) + ".png");
        }
        for (int i = 0; i < met_s.length; i++) {
            met_s[i] = Loader.ImageLoader("/asteroids/small" + (i + 1) + ".png");
        }
        for (int i = 0; i < met_t.length; i++) {
            met_t[i] = Loader.ImageLoader("/asteroids/tiny" + (i + 1) + ".png");
        }
        for (int i = 0; i < expl.length; i++) {
            expl[i] = Loader.ImageLoader("/explosion/" + i + ".png");
        }
        for (int i = 0; i < num.length; i++) {
            num[i] = Loader.ImageLoader("/numeros/num" + i + ".png");
        }

        fontTitle = loadFontWithFallback("/fuente/kenvector_future.ttf", 52, Font.BOLD);
        fontBig = loadFontWithFallback("/fuente/kenvector_future.ttf", 44, Font.BOLD);
        fontMed = loadFontWithFallback("/fuente/kenvector_future.ttf", 22, Font.BOLD);
        fontSmall = loadFontWithFallback("/fuente/kenvector_future.ttf", 15, Font.PLAIN);
        fontTiny = loadFontWithFallback("/fuente/kenvector_future.ttf", 11, Font.PLAIN);
    }

    public static BufferedImage createRadialGlowSprite(int radius, Color color, int innerAlpha, int midAlpha) {
        int size = radius * 2;
        BufferedImage img = Loader.createCompatibleImage(size, size, Transparency.TRANSLUCENT);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        RadialGradientPaint paint = new RadialGradientPaint(
            new Point2D.Float(radius, radius), radius,
            new float[] { 0f, 0.5f, 1f },
            new Color[] {
                new Color(color.getRed(), color.getGreen(), color.getBlue(), innerAlpha),
                new Color(color.getRed(), color.getGreen(), color.getBlue(), midAlpha),
                new Color(color.getRed(), color.getGreen(), color.getBlue(), 0)
            }
        );
        g.setPaint(paint);
        g.fillOval(0, 0, size, size);
        g.dispose();
        return img;
    }

    private static BufferedImage createEnemyPlasmaOrb() {
        int r = 11;
        int size = r * 2;
        BufferedImage img = Loader.createCompatibleImage(size, size, Transparency.TRANSLUCENT);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        RadialGradientPaint paint = new RadialGradientPaint(
            new Point2D.Float(r, r), r,
            new float[] { 0f, 0.45f, 1f },
            new Color[] { Color.WHITE, new Color(255, 70, 235, 220), new Color(200, 20, 220, 0) }
        );
        g.setPaint(paint);
        g.fillOval(0, 0, size, size);
        g.dispose();
        return img;
    }

    private static BufferedImage createShieldBubbleSprite() {
        float r = 44 * 0.72f;
        int pad = 4;
        int half = (int) Math.ceil(r) + pad;
        int size = half * 2;
        BufferedImage img = Loader.createCompatibleImage(size, size, Transparency.TRANSLUCENT);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        RadialGradientPaint paint = new RadialGradientPaint(
            new Point2D.Float(half, half), r,
            new float[] { 0.45f, 0.82f, 1.0f },
            new Color[] {
                new Color(75, 220, 255, 16),
                new Color(75, 220, 255, 65),
                new Color(140, 245, 255, 165)
            }
        );
        g.setPaint(paint);
        g.fillOval((int) (half - r), (int) (half - r), (int) (r * 2), (int) (r * 2));
        g.setStroke(new BasicStroke(1.8f));
        g.setColor(new Color(120, 240, 255, 195));
        g.drawOval((int) (half - r), (int) (half - r), (int) (r * 2), (int) (r * 2));
        g.dispose();
        return img;
    }

    private static Font loadFontWithFallback(String path, int size, int fallbackStyle) {
        Font f = Loader.loadFont(path, size);
        return f != null ? f : new Font("SansSerif", fallbackStyle, size);
    }
}
