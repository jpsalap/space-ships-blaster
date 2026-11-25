package graphics;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import math.Vector2D;

public class Animacion {
    private BufferedImage[] frames;
    private int velocidad;
    private int index;
    private boolean running;
    private Vector2D center;
    private long tiempo, lastTim;

    public Animacion(BufferedImage[] frames, int velocidad, Vector2D center) {
        this.frames = frames;
        this.velocidad = velocidad;
        this.center = center;
        index = 0;
        running = true;
        tiempo = 0;
        lastTim = System.currentTimeMillis();
    }

    public void update() {
        if (!running) {
            return;
        }
        tiempo += System.currentTimeMillis() - lastTim;
        lastTim = System.currentTimeMillis();
        if (tiempo > velocidad) {
            tiempo = 0;
            index++;
            if (index >= frames.length) {
                index = frames.length - 1;
                running = false;
            }
        }
    }

    public boolean isRunning() {
        return running;
    }

    public Vector2D getPosicion() {
        BufferedImage frame = frames[index];
        return new Vector2D(center.getX() - frame.getWidth() / 2.0, center.getY() - frame.getHeight() / 2.0);
    }

    public BufferedImage getCurrentFrame() {
        return frames[index];
    }

    public void draw(Graphics2D g2d) {
        BufferedImage frame = frames[index];
        int x = (int) (center.getX() - frame.getWidth() / 2.0);
        int y = (int) (center.getY() - frame.getHeight() / 2.0);
        g2d.drawImage(frame, x, y, null);
    }
}
