package graphics;

import GameObjects.Constantes;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Random;

public class Starfield {
    private static final int ALPHA_STEPS = 16;

    private static class Star {
        double x, y;
        int layer; // 0 = far, 1 = mid, 2 = near
        int sizePx;
        int paletteIdx;
        double twinklePhase;
        double twinkleSpeed;
    }

    private final BufferedImage nebulaBackdrop;
    private final Star[] stars;
    private final Color[][] colorLut;

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
        colorLut = new Color[palette.length][ALPHA_STEPS];
        for (int p = 0; p < palette.length; p++) {
            Color base = palette[p];
            for (int a = 0; a < ALPHA_STEPS; a++) {
                int alpha = 70 + (int) ((255 - 70) * (a / (double) (ALPHA_STEPS - 1)));
                colorLut[p][a] = new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha);
            }
        }
        for (int i = 0; i < count; i++) {
            Star s = new Star();
            s.x = rng.nextDouble() * width;
            s.y = rng.nextDouble() * height;
            double r = rng.nextDouble();
            s.layer = (r < 0.55) ? 0 : (r < 0.85 ? 1 : 2);
            s.sizePx = s.layer == 0 ? 1 : (s.layer == 1 ? 2 : 3);
            s.twinklePhase = rng.nextDouble() * Math.PI * 2;
            s.twinkleSpeed = 0.02 + rng.nextDouble() * 0.05;
            s.paletteIdx = rng.nextInt(palette.length);
            stars[i] = s;
        }
    }

    private BufferedImage createNebulaBackdrop(int width, int height) {
        // Opaque compatible image allows fast GPU blitting with zero per-frame alpha compositing
        BufferedImage img = Loader.createCompatibleImage(width, height, Transparency.OPAQUE);
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
        for (int i = 0; i < stars.length; i++) {
            Star s = stars[i];
            double twinkle = 0.55 + 0.45 * Math.sin(s.twinklePhase);
            int minIdx = s.layer == 0 ? 0 : (s.layer == 1 ? 4 : 8);
            int idx = minIdx + (int) ((ALPHA_STEPS - 1 - minIdx) * twinkle);
            if (idx >= ALPHA_STEPS) idx = ALPHA_STEPS - 1;
            g2d.setColor(colorLut[s.paletteIdx][idx]);
            int sx = (int) s.x;
            int sy = (int) s.y;
            if (s.sizePx <= 2) {
                g2d.fillRect(sx, sy, s.sizePx, s.sizePx);
            } else {
                g2d.fillOval(sx, sy, s.sizePx, s.sizePx);
            }
        }
    }
}
