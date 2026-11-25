package graphics;

import java.awt.image.BufferedImage;
import math.Vector2D;

public class Animacion {
    private BufferedImage[] frames;
    private int velocidad;
    private int index;
    private boolean running;
    private Vector2D posicion;
    private long tiempo, lastTim;
    public Animacion(BufferedImage[] frames,int velocidad,Vector2D posicion){
        this.frames=frames;
        this.velocidad=velocidad;
        this.posicion=posicion;
        index=0;
        running=true;
        tiempo=0;
        lastTim=System.currentTimeMillis();
    }
    public void update(){
        if(!running){
            return;
        }
        tiempo +=System.currentTimeMillis()-lastTim;
        lastTim=System.currentTimeMillis();
        if(tiempo>velocidad){
            tiempo=0;
            index++;
            if(index>=frames.length){
                index=frames.length-1;
                running=false;
            }
        }
    }
    public boolean isRunning(){
        return running;
    }
    public Vector2D getPosicion(){
        return posicion;
    }
    public BufferedImage getCurrentFrame(){
        return frames[index];
    }
}
