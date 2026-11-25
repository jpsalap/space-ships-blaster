package graphics;

import GameObjects.Constantes;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Random;

public class Starfield {
    private static class Star {
        double x, y;
        int layer; // 0 = far, 1 = mid, 2 = near
        double size;
        double twinklePhase;
        double twinkleSpeed;
        Color baseColor;
    }

    private final BufferedImage nebulaBackdrop;
    private final Star[] stars;

    public Starfield(int width, int height) {
        nebulaBackdrop = createNebulaBackdrop(width, height);
        Random rng = new Random(2018);
        int count = 150;
        stars = new Star[count];
        Color[] palette = {
            new Color(220, 235, 255),
            new Color(255, 255, 255),
            new Color(180, 220, 255),
            new Color(255, 232, 195),
            new Color(210, 200, 255)
        };
        for (int i = 0; i < count; i++) {
            Star s = new Star();
            s.x = rng.nextDouble() * width;
            s.y = rng.nextDouble() * height;
            double r = rng.nextDouble();
            s.layer = (r < 0.55) ? 0 : (r < 0.85 ? 1 : 2);
            s.size = s.layer == 0 ? 1.2 : (s.layer == 1 ? 2.0 : 2.8);
            s.twinklePhase = rng.nextDouble() * Math.PI * 2;
            s.twinkleSpeed = 0.02 + rng.nextDouble() * 0.05;
            s.baseColor = palette[rng.nextInt(palette.length)];
            stars[i] = s;
        }
    }

    private BufferedImage createNebulaBackdrop(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Deep midnight-indigo base
        g.setColor(new Color(7, 10, 24));
        g.fillRect(0, 0, width, height);

        // Layered cosmic nebula clouds
        drawNebulaCloud(g, width * 0.20f, height * 0.26f, 460f,
                new Color(78, 34, 155, 135), new Color(34, 18, 86, 65));
        drawNebulaCloud(g, width * 0.80f, height * 0.72f, 500f,
                new Color(18, 110, 160, 125), new Color(12, 48, 92, 55));
        drawNebulaCloud(g, width * 0.68f, height * 0.20f, 350f,
                new Color(145, 36, 115, 90), new Color(55, 16, 66, 38));
        drawNebulaCloud(g, width * 0.34f, height * 0.82f, 380f,
                new Color(28, 72, 155, 100), new Color(12, 30, 74, 42));

        // Faint distant star dust baked into backdrop
        Random rng = new Random(1337);
        for (int i = 0; i < 240; i++) {
            int sx = rng.nextInt(width);
            int sy = rng.nextInt(height);
            int alpha = 25 + rng.nextInt(55);
            g.setColor(new Color(200, 215, 255, alpha));
            g.fillRect(sx, sy, 1, 1);
        }

        // Subtle dark vignette at edges to frame gameplay
        RadialGradientPaint vignette = new RadialGradientPaint(
            new Point2D.Float(width / 2f, height / 2f),
            Math.max(width, height) * 0.68f,
            new float[] { 0.55f, 1.0f },
            new Color[] { new Color(0, 0, 0, 0), new Color(0, 0, 0, 110) },
            MultipleGradientPaint.CycleMethod.NO_CYCLE
        );
        g.setPaint(vignette);
        g.fillRect(0, 0, width, height);

        g.dispose();
        return img;
    }

    private void drawNebulaCloud(Graphics2D g, float cx, float cy, float radius, Color inner, Color mid) {
        RadialGradientPaint paint = new RadialGradientPaint(
            new Point2D.Float(cx, cy),
            radius,
            new float[] { 0.0f, 0.45f, 1.0f },
            new Color[] { inner, mid, new Color(0, 0, 0, 0) },
            MultipleGradientPaint.CycleMethod.NO_CYCLE
        );
        g.setPaint(paint);
        g.fillOval((int) (cx - radius), (int) (cy - radius), (int) (radius * 2), (int) (radius * 2));
    }

    public void update(double driftX, double driftY) {
        for (Star s : stars) {
            double speedFactor = s.layer == 0 ? 0.08 : (s.layer == 1 ? 0.18 : 0.32);
            s.x += driftX * speedFactor;
            s.y += driftY * speedFactor;
            s.twinklePhase += s.twinkleSpeed;

            if (s.x < 0) s.x += Constantes.WIDTH;
            if (s.x >= Constantes.WIDTH) s.x -= Constantes.WIDTH;
            if (s.y < 0) s.y += Constantes.HEIGHT;
            if (s.y >= Constantes.HEIGHT) s.y -= Constantes.HEIGHT;
        }
    }

    public void draw(Graphics2D g2d) {
        g2d.drawImage(nebulaBackdrop, 0, 0, null);
        for (Star s : stars) {
            double twinkle = 0.55 + 0.45 * Math.sin(s.twinklePhase);
            int minAlpha = s.layer == 0 ? 70 : (s.layer == 1 ? 120 : 170);
            int alpha = Math.min(255, (int) (minAlpha + (255 - minAlpha) * twinkle));
            g2d.setColor(new Color(s.baseColor.getRed(), s.baseColor.getGreen(), s.baseColor.getBlue(), alpha));
            int sz = (int) Math.round(s.size);
            g2d.fillOval((int) s.x, (int) s.y, sz, sz);
        }
    }
}
