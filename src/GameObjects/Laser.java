package GameObjects;

import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import math.Vector2D;
import states.Estado_juego;

public class Laser extends Moving_obj{
    public Laser(Vector2D posicion,Vector2D velocidad,double maxV,double angulo,BufferedImage textura,Estado_juego gameState){
        super(posicion,velocidad,maxV,textura,gameState);
        this.angulo=angulo;
        this.velocidad=velocidad.scale(maxV);
    }
    @Override
    public void update(){
        posicion=posicion.add(velocidad);
        if(posicion.getX()<0 || posicion.getX()>Constantes.WIDTH || posicion.getY()<0 || posicion.getY()>Constantes.HEIGHT){
            destroy();
            return;
        }
        collidesWith();
    }
    @Override
    public void draw(Graphics g){
        Graphics2D G2D=(Graphics2D)g;
        at=AffineTransform.getTranslateInstance(posicion.getX()-width/2,posicion.getY()-height/2);
        at.rotate(angulo,width/2,height/2);
        G2D.drawImage(textura,at,null);
    }
    @Override
    public Vector2D getCenter(){
        return new Vector2D(posicion.getX(),posicion.getY());
    }
}
