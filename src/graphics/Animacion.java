package graphics;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import math.Vector2D;

public class Animacion {
    private static final double FRAME_MS = 1000.0 / 60.0;

    private final BufferedImage[] frames;
    private final int velocidad;
    private int index;
    private boolean running;
    private final Vector2D center;
    private double tiempo;

    public Animacion(BufferedImage[] frames, int velocidad, Vector2D center) {
        this.frames = frames;
        this.velocidad = velocidad;
        this.center = center;
        index = 0;
        running = true;
        tiempo = 0;
    }

    public void update() {
        if (!running) {
            return;
        }
        tiempo += FRAME_MS;
        while (tiempo >= velocidad && running) {
            tiempo -= velocidad;
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
