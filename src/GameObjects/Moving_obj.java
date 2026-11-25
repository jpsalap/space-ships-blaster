package GameObjects;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import math.Vector2D;
import states.Estado_juego;

public abstract class Moving_obj extends GameObject{
    
    protected Vector2D velocidad;
    protected AffineTransform at,at1,at2;
    protected double angulo;
    protected double maxV;
    protected int width;
    protected int height;
    protected Estado_juego gameState;
    public Moving_obj(Vector2D posicion,Vector2D velocidad,double maxV,BufferedImage textura,Estado_juego gameState){
        super(posicion,textura);
        this.velocidad=velocidad;
        this.maxV=maxV;
        this.gameState=gameState;
        width=textura.getWidth();
        height=textura.getHeight();
        angulo=0;
    }
    protected void collidesWith(){
        ArrayList<Moving_obj> moving_obj=gameState.getMoving_obj();
        ArrayList<Moving_obj> snapshot=new ArrayList<Moving_obj>(moving_obj);
        for(Moving_obj m : snapshot){
            if(!moving_obj.contains(this)){
                break;
            }
            if(m.equals(this) || !moving_obj.contains(m)){
                continue;
            }
            double distancia=m.getCenter().substract(getCenter()).getMagni();
            if(distancia < m.width/2+width/2){
                objectCollision(m, this);
            }
        }
    }
    private void objectCollision(Moving_obj a,Moving_obj b){
        if(a instanceof Jugador && ((Jugador)a).isSpawning()){
            return;
        }
        if(b instanceof Jugador && ((Jugador)b).isSpawning()){
            return;
        }
        if((a instanceof Asteroid && b instanceof Asteroid)
                || (a instanceof Laser && b instanceof Laser)
                || (a instanceof Laser && b instanceof Jugador)
                || (a instanceof Jugador && b instanceof Laser)){
            return;
        }
        gameState.playExplosion(getCenter());
        a.destroy();
        b.destroy();
    }
    protected void destroy(){
        gameState.getMoving_obj().remove(this);
    }
    protected Vector2D getCenter(){
        return new Vector2D(posicion.getX()+width/2,posicion.getY()+height/2);
    }
}
