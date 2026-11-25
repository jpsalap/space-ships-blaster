package main;

import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

public class Sonidos {
    public static final Sonido Disparo = new Sonido("/Sounds/disparo.wav", 6, -4.0f);
    public static final Sonido propulsor = new Sonido("/Sounds/propulsor.wav", 1, -9.5f);
    public static final Sonido explosion = new Sonido("/Sounds/explosion.wav", 5, -3.0f);
    public static final Sonido playerHit = new Sonido("/Sounds/player_hit.wav", 2, -2.0f);
    public static final Sonido wave = new Sonido("/Sounds/wave.wav", 2, -4.0f);
    public static final Sonido gameOver = new Sonido("/Sounds/gameover.wav", 1, -3.0f);
    public static final Sonido ufoLaser = new Sonido("/Sounds/ufo_laser.wav", 4, -3.5f);
    public static final Sonido powerUp = new Sonido("/Sounds/powerup.wav", 3, -3.0f);
    public static final Sonido extraLife = new Sonido("/Sounds/extralife.wav", 2, -2.5f);
    public static final Sonido espacio = new Sonido("/Sounds/espacio.wav", 1, -5.5f);

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
        private final float baseGainDb;
        private int nextIdx = 0;
        private float fadeLevel = 0.0f;

        public Sonido(String path, int poolSize, float gainDb) {
            this.baseGainDb = gainDb;
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
            applyGain(clip, baseGainDb);
            clip.setFramePosition(0);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }

        /**
         * Smoothly fades a looping sound in while active and out when inactive,
         * avoiding abrupt start/stop clicks on repeated key taps.
         */
        public void updateLoopFade(boolean active) {
            Clip clip = pool[0];
            if (clip == null) {
                return;
            }
            if (muted) {
                stop();
                return;
            }
            if (active) {
                fadeLevel = Math.min(1.0f, fadeLevel + 0.14f);
            } else {
                fadeLevel = Math.max(0.0f, fadeLevel - 0.08f);
            }
            if (fadeLevel > 0.01f) {
                float db = baseGainDb + (float) (20.0 * Math.log10(fadeLevel));
                applyGain(clip, db);
                if (!clip.isRunning()) {
                    clip.setFramePosition(0);
                    clip.loop(Clip.LOOP_CONTINUOUSLY);
                }
            } else {
                if (clip.isRunning()) {
                    clip.stop();
                    clip.setFramePosition(0);
                }
            }
        }

        public void stop() {
            fadeLevel = 0.0f;
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
