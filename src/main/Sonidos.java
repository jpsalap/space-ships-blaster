package main;

import java.applet.Applet;
import java.applet.AudioClip;
public class Sonidos {
    public static final AudioClip Disparo =Applet.newAudioClip(Sonidos.class.getResource("/Sounds/disparo.wav"));
    public static final AudioClip propulsor =Applet.newAudioClip(Sonidos.class.getResource("/Sounds/propulsor.wav"));
    public static final AudioClip espacio =Applet.newAudioClip(Sonidos.class.getResource("/Sounds/espacio.wav"));
}
