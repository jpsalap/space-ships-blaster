package main;

import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

public class Sonidos {
    // Kept comfortably below JavaSound's 32-line software mixer limit (22 total clips)
    public static final Sonido Disparo = new Sonido("/Sounds/disparo.wav", 4, -4.0f);
    public static final Sonido propulsor = new Sonido("/Sounds/propulsor.wav", 1, -9.5f);
    public static final Sonido explosion = new Sonido("/Sounds/explosion.wav", 4, -3.0f);
    public static final Sonido playerHit = new Sonido("/Sounds/player_hit.wav", 1, -2.0f);
    public static final Sonido wave = new Sonido("/Sounds/wave.wav", 1, -4.0f);
    public static final Sonido gameOver = new Sonido("/Sounds/gameover.wav", 1, -3.0f);
    public static final Sonido ufoLaser = new Sonido("/Sounds/ufo_laser.wav", 3, -3.5f);
    public static final Sonido ufoEnter = new Sonido("/Sounds/ufo_enter.wav", 1, -4.5f);
    public static final Sonido dash = new Sonido("/Sounds/dash.wav", 2, -4.5f);
    public static final Sonido powerUp = new Sonido("/Sounds/powerup.wav", 2, -3.0f);
    public static final Sonido extraLife = new Sonido("/Sounds/extralife.wav", 1, -2.5f);
    public static final Sonido espacio = new Sonido("/Sounds/espacio.wav", 1, -5.5f);

    private static final Sonido[] ALL_SOUNDS = {
        Disparo, propulsor, explosion, playerHit, wave, gameOver,
        ufoLaser, ufoEnter, dash, powerUp, extraLife, espacio
    };

    private static volatile boolean muted = false;

    public static boolean isMuted() {
        return muted;
    }

    public static synchronized void toggleMute() {
        muted = !muted;
        if (muted) {
            espacio.stop();
            propulsor.stop();
        } else {
            espacio.loop();
        }
    }

    public static void closeAll() {
        for (Sonido s : ALL_SOUNDS) {
            s.close();
        }
    }

    public static class Sonido {
        private final Clip[] pool;
        private final int loadedCount;
        private final float baseGainDb;
        private int nextIdx = 0;
        private float fadeLevel = 0.0f;
        private float lastAppliedFade = -1.0f;

        public Sonido(String path, int poolSize, float gainDb) {
            this.baseGainDb = gainDb;
            pool = new Clip[Math.max(1, poolSize)];
            int opened = 0;
            URL url = Sonidos.class.getResource(path);
            if (url != null) {
                for (int i = 0; i < pool.length; i++) {
                    Clip c = null;
                    try (AudioInputStream ais = AudioSystem.getAudioInputStream(url)) {
                        c = AudioSystem.getClip();
                        c.open(ais);
                        applyGain(c, gainDb);
                        pool[opened++] = c;
                    } catch (Exception e) {
                        if (c != null) {
                            try {
                                c.close();
                            } catch (Exception ignored) {
                            }
                        }
                        break;
                    }
                }
            }
            this.loadedCount = opened;
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

        public synchronized void play() {
            if (muted || loadedCount == 0) {
                return;
            }
            // Prefer an idle clip to avoid blocking CoreAudio stop/restart stalls on the game thread
            Clip clip = null;
            for (int i = 0; i < loadedCount; i++) {
                int idx = (nextIdx + i) % loadedCount;
                Clip candidate = pool[idx];
                if (candidate != null && !candidate.isRunning()) {
                    clip = candidate;
                    nextIdx = (idx + 1) % loadedCount;
                    break;
                }
            }
            if (clip == null) {
                clip = pool[nextIdx];
                nextIdx = (nextIdx + 1) % loadedCount;
            }
            if (clip == null) {
                return;
            }
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0);
            clip.start();
        }

        public synchronized void loop() {
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
        public synchronized void updateLoopFade(boolean active) {
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
                if (Math.abs(fadeLevel - lastAppliedFade) > 0.005f) {
                    float db = baseGainDb + (float) (20.0 * Math.log10(fadeLevel));
                    applyGain(clip, db);
                    lastAppliedFade = fadeLevel;
                }
                if (!clip.isRunning()) {
                    clip.setFramePosition(0);
                    clip.loop(Clip.LOOP_CONTINUOUSLY);
                }
            } else {
                lastAppliedFade = -1.0f;
                if (clip.isRunning()) {
                    clip.stop();
                    clip.setFramePosition(0);
                }
            }
        }

        public synchronized void stop() {
            fadeLevel = 0.0f;
            lastAppliedFade = -1.0f;
            for (Clip clip : pool) {
                if (clip != null) {
                    if (clip.isRunning()) {
                        clip.stop();
                    }
                    clip.setFramePosition(0);
                }
            }
        }

        public synchronized void close() {
            stop();
            for (int i = 0; i < loadedCount; i++) {
                Clip clip = pool[i];
                if (clip != null) {
                    try {
                        clip.close();
                    } catch (Exception ignored) {
                    }
                    pool[i] = null;
                }
            }
        }

        public boolean isLoaded() {
            return loadedCount > 0 && pool[0] != null;
        }
    }
}
