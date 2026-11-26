package graphics;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import math.Vector2D;

public class Particle {
    private static final int ALPHA_STEPS = 16;
    private static final HashMap<Integer, Color[]> COLOR_LUT_CACHE = new HashMap<Integer, Color[]>();
    private static final HashMap<Integer, BufferedImage> GLOW_SPRITE_CACHE = new HashMap<Integer, BufferedImage>();
    private static final AlphaComposite[] ALPHA_COMPOSITES = new AlphaComposite[ALPHA_STEPS];

    static {
        for (int i = 0; i < ALPHA_STEPS; i++) {
            float a = Math.max(0.04f, (float) (i + 1) / ALPHA_STEPS);
            ALPHA_COMPOSITES[i] = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a);
        }
    }

    private Vector2D posicion;
    private Vector2D velocidad;
    private final Color[] colorSteps;
    private final BufferedImage glowSprite;
    private final double size;
    private int life;
    private final int maxLife;
    private final boolean glow;

    public Particle(Vector2D posicion, Vector2D velocidad, Color color, double size, int maxLife) {
        this(posicion, velocidad, color, size, maxLife, false);
    }

    public Particle(Vector2D posicion, Vector2D velocidad, Color color, double size, int maxLife, boolean glow) {
        this.posicion = posicion;
        this.velocidad = velocidad;
        this.colorSteps = getColorSteps(color);
        this.glowSprite = glow ? getGlowSprite(color) : null;
        this.size = size;
        this.life = maxLife;
        this.maxLife = maxLife;
        this.glow = glow;
    }

    private static Color[] getColorSteps(Color c) {
        int rgb = c.getRGB() & 0x00FFFFFF;
        Color[] lut = COLOR_LUT_CACHE.get(rgb);
        if (lut == null) {
            lut = new Color[ALPHA_STEPS];
            int r = c.getRed();
            int g = c.getGreen();
            int b = c.getBlue();
            for (int i = 0; i < ALPHA_STEPS; i++) {
                int a = Math.min(255, (int) (255.0 * (i + 1) / ALPHA_STEPS));
                lut[i] = new Color(r, g, b, a);
            }
            if (COLOR_LUT_CACHE.size() < 64) {
                COLOR_LUT_CACHE.put(rgb, lut);
            }
        }
        return lut;
    }

    private static BufferedImage getGlowSprite(Color c) {
        int rgb = c.getRGB() & 0x00FFFFFF;
        BufferedImage sprite = GLOW_SPRITE_CACHE.get(rgb);
        if (sprite == null) {
            sprite = Assets.createRadialGlowSprite(7, c, 105, 42);
            if (GLOW_SPRITE_CACHE.size() < 64) {
                GLOW_SPRITE_CACHE.put(rgb, sprite);
            }
        }
        return sprite;
    }

    public void update() {
        posicion = posicion.add(velocidad);
        velocidad = velocidad.scale(0.95);
        life--;
    }

    public boolean isAlive() {
        return life > 0;
    }

    public void draw(Graphics2D g2d) {
        float ratio = Math.max(0f, (float) life / (float) maxLife);
        int idx = (int) (ratio * (ALPHA_STEPS - 1));
        if (idx < 0) idx = 0;
        else if (idx >= ALPHA_STEPS) idx = ALPHA_STEPS - 1;

        int curSize = Math.max(2, (int) Math.round(size * (0.35 + 0.65 * ratio)));
        int x = (int) posicion.getX();
        int y = (int) posicion.getY();

        if (glow && glowSprite != null) {
            Composite prev = g2d.getComposite();
            g2d.setComposite(ALPHA_COMPOSITES[idx]);
            // Unscaled 1:1 sprite blit avoids per-particle bilinear resampling
            g2d.drawImage(glowSprite, x - 7, y - 7, null);
            g2d.setComposite(prev);
        }

        g2d.setColor(colorSteps[idx]);
        int halfCore = curSize >> 1;
        g2d.fillRect(x - halfCore, y - halfCore, curSize, curSize);
    }
}
