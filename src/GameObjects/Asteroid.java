package GameObjects;

import graphics.Assets;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import math.Vector2D;
import states.Estado_juego;

public class Asteroid extends Moving_obj{
    private Size size;
    public Asteroid(Vector2D posicion,Vector2D velocidad,double maxV,BufferedImage textura,Estado_juego gameState,Size size){
    super(posicion,velocidad,maxV,textura,gameState);
    this.size=size;
    this.velocidad=velocidad.scale(maxV);
    }
    @Override
    public void destroy(){
        gameState.divideAsteroid(this);
        gameState.addScore(Constantes.asteroid_score);
        super.destroy();
    }
    @Override
    public void update(){
        posicion=posicion.add(velocidad);
        if(posicion.getX()>Constantes.WIDTH)
            posicion.setX(0);
        if(posicion.getY()>Constantes.HEIGHT)
            posicion.setY(0);
        if(posicion.getX()<0)
            posicion.setX(Constantes.WIDTH);
        if(posicion.getY()<0)
            posicion.setY(Constantes.HEIGHT);
        angulo +=Constantes.deltaangulo/2;
    }
    @Override
    public void draw(Graphics g){
        Graphics2D G2D=(Graphics2D)g;
        at=AffineTransform.getTranslateInstance(posicion.getX(),posicion.getY());
        at.rotate(angulo,width/2,height/2);
        G2D.drawImage(textura,at,null);
    }
    public Size getSize(){
        return size;
    }
}
