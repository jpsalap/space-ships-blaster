package main;

import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Sonidos {
    public static final Sonido Disparo = new Sonido("/Sounds/disparo.wav");
    public static final Sonido propulsor = new Sonido("/Sounds/propulsor.wav");
    public static final Sonido espacio = new Sonido("/Sounds/espacio.wav");

    public static class Sonido {
        private Clip clip;

        public Sonido(String path) {
            try {
                URL url = Sonidos.class.getResource(path);
                if (url != null) {
                    try (AudioInputStream ais = AudioSystem.getAudioInputStream(url)) {
                        clip = AudioSystem.getClip();
                        clip.open(ais);
                    }
                }
            } catch (Exception e) {
                clip = null;
            }
        }

        public void play() {
            if (clip == null) {
                return;
            }
            clip.stop();
            clip.setFramePosition(0);
            clip.start();
        }

        public void loop() {
            if (clip == null || clip.isRunning()) {
                return;
            }
            clip.setFramePosition(0);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }

        public void stop() {
            if (clip == null) {
                return;
            }
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0);
        }

        public boolean isLoaded() {
            return clip != null;
        }
    }
}
