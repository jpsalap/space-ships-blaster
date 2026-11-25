package main;

import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

public class Sonidos {
    public static final Sonido Disparo = new Sonido("/Sounds/disparo.wav", 5, -4.0f);
    public static final Sonido propulsor = new Sonido("/Sounds/propulsor.wav", 1, -8.0f);
    public static final Sonido explosion = new Sonido("/Sounds/explosion.wav", 5, -3.0f);
    public static final Sonido playerHit = new Sonido("/Sounds/player_hit.wav", 2, -2.0f);
    public static final Sonido wave = new Sonido("/Sounds/wave.wav", 2, -4.0f);
    public static final Sonido gameOver = new Sonido("/Sounds/gameover.wav", 1, -3.0f);
    public static final Sonido espacio = new Sonido("/Sounds/espacio.wav", 1, -11.0f);

    private static volatile boolean muted = false;

    public static boolean isMuted() {
        return muted;
    }

    public static void toggleMute() {
        muted = !muted;
        if (muted) {
            espacio.stop();
            propulsor.stop();
        } else {
            espacio.loop();
        }
    }

    public static class Sonido {
        private final Clip[] pool;
        private int nextIdx = 0;

        public Sonido(String path, int poolSize, float gainDb) {
            pool = new Clip[Math.max(1, poolSize)];
            try {
                URL url = Sonidos.class.getResource(path);
                if (url != null) {
                    for (int i = 0; i < pool.length; i++) {
                        try (AudioInputStream ais = AudioSystem.getAudioInputStream(url)) {
                            Clip c = AudioSystem.getClip();
                            c.open(ais);
                            applyGain(c, gainDb);
                            pool[i] = c;
                        }
                    }
                }
            } catch (Exception e) {
                for (int i = 0; i < pool.length; i++) {
                    pool[i] = null;
                }
            }
        }

        private static void applyGain(Clip clip, float gainDb) {
            try {
                if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    float clamped = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), gainDb));
                    gain.setValue(clamped);
                }
            } catch (Exception ignored) {
            }
        }

        public void play() {
            if (muted) {
                return;
            }
            Clip clip = pool[nextIdx];
            nextIdx = (nextIdx + 1) % pool.length;
            if (clip == null) {
                return;
            }
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0);
            clip.start();
        }

        public void loop() {
            if (muted) {
                return;
            }
            Clip clip = pool[0];
            if (clip == null || clip.isRunning()) {
                return;
            }
            clip.setFramePosition(0);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }

        public void stop() {
            for (Clip clip : pool) {
                if (clip != null) {
                    if (clip.isRunning()) {
                        clip.stop();
                    }
                    clip.setFramePosition(0);
                }
            }
        }

        public boolean isLoaded() {
            return pool[0] != null;
        }
    }
}
