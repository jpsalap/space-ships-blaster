package GameObjects;

import graphics.Assets;
import input.Keyboard;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import main.Sonidos;
import math.Vector2D;
import states.Estado_juego;

public class Jugador extends Moving_obj{
    private Vector2D direccion;
    private Vector2D aceleracion;
    private boolean acelerando=false;
    private Crono firerate;
    private boolean spawning,visible;
    private Crono spawningTime, titilar;
    public Jugador(Vector2D posicion,Vector2D velocidad,double maxV,BufferedImage textura,Estado_juego gameState){
        super(posicion,velocidad,maxV,textura,gameState);
        this.gameState=gameState;
        direccion=new Vector2D(0, 1);
        aceleracion=new Vector2D();
        firerate=new Crono();
        spawningTime=new Crono();
        titilar=new Crono();
    }
    @Override
    public void update(){
        if(!spawningTime.isRunning()){
            spawning=false;
            visible=true;
        }
        if(spawning){
            if(!titilar.isRunning()){
                titilar.run(Constantes.titilar);
                visible=!visible;
            }
        }
        if(Keyboard.shoot && !firerate.isRunning() && !spawning){
            gameState.getMoving_obj().add(0,new Laser(getCenter().add(direccion.scale(width)),direccion,Constantes.vlaser,angulo,Assets.blueLaser,gameState));
            firerate.run(Constantes.vdisparo);
            Sonidos.Disparo.play();
        }
        if(Keyboard.right){
            angulo +=Constantes.deltaangulo;
        }    
        if(Keyboard.left){
            angulo -=Constantes.deltaangulo;
        }
        if(Keyboard.up){
            aceleracion=direccion.scale(Constantes.acc);
            acelerando=true;
            Sonidos.propulsor.play();
        }else{
            if(velocidad.getMagni() !=0)
                aceleracion=(velocidad.scale(-1).normalizar()).scale(Constantes.acc/2);
            acelerando=false;
        }
        velocidad=velocidad.add(aceleracion);
        velocidad=velocidad.limitev(maxV);
        direccion=direccion.setDireccion(angulo-Math.PI/2);
        posicion=posicion.add(velocidad);
        if(posicion.getX()>Constantes.WIDTH)
            posicion.setX(0);
        if(posicion.getY()>Constantes.HEIGHT)
            posicion.setY(0);
        if(posicion.getX()<0)
            posicion.setX(Constantes.WIDTH);
        if(posicion.getY()<0)
            posicion.setY(Constantes.HEIGHT);
        firerate.update();
        spawningTime.update();
        titilar.update();
        collidesWith();
    }
    @Override
    public void destroy(){
        spawning=true;
        spawningTime.run(Constantes.spawing_t);
        restablecer();
        gameState.restarvida();
    }
    private void restablecer(){
        angulo=0;
        velocidad=new Vector2D();
        posicion= new Vector2D(Constantes.WIDTH/2 - Assets.jugador.getWidth()/2,Constantes.HEIGHT/2 - Assets.jugador.getHeight()/2);
    }
    @Override
    public void draw(Graphics g){
        if(!visible){
            return;
        }    
        Graphics2D G2D=(Graphics2D)g;
        at1=AffineTransform.getTranslateInstance(posicion.getX()+width/2 + 5,posicion.getY()+height/2 +10);
        at2=AffineTransform.getTranslateInstance(posicion.getX() + 5,posicion.getY()+height/2 + 10);
        at1.rotate(angulo,-5,-10);
        at2.rotate(angulo,width/2 -5,-10);
        if(acelerando){
            G2D.drawImage(Assets.prop,at1,null);
            G2D.drawImage(Assets.prop,at2,null);
        }
        at=AffineTransform.getTranslateInstance(posicion.getX(),posicion.getY());
        at.rotate(angulo,width/2,height/2);
        G2D.drawImage(textura,at,null);
    }
    public boolean isSpawning(){
        return spawning;
    }
}
