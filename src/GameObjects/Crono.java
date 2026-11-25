package GameObjects;

public class Crono {
    private static final double FRAME_MS = 1000.0 / 60.0;

    private double delta;
    private long time;
    private boolean running;

    public Crono() {
        delta = 0;
        running = false;
    }

    public void run(long time) {
        running = true;
        this.time = time;
        delta = 0;
    }

    public void update() {
        if (running) {
            delta += FRAME_MS;
            if (delta >= time) {
                running = false;
                delta = 0;
            }
        }
    }

    public boolean isRunning() {
        return running;
    }
}
