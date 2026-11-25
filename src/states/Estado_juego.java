package states;


import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import GameObjects.Asteroid;
import GameObjects.Constantes;
import GameObjects.Jugador;
import GameObjects.Moving_obj;
import GameObjects.Size;
import graphics.Animacion;
import graphics.Assets;
import math.Vector2D;

public class Estado_juego {
    private Jugador player;
    private ArrayList<Moving_obj> moving_obj =new ArrayList<Moving_obj>();
    private ArrayList<Animacion> explosiones=new ArrayList<Animacion>();
    private int puntaje=0;
    public static int lives=3;
    private int asteroids;
    public Estado_juego(){
        player=new Jugador(new Vector2D(Constantes.WIDTH/2 - Assets.jugador.getWidth()/2,Constantes.HEIGHT/2 - Assets.jugador.getHeight()/2),new Vector2D(),Constantes.j_vel_max,Assets.jugador,this);
        moving_obj.add(player);
        asteroids=1;
        startWave();
    }
    public void addScore(int valor){
        puntaje +=valor;
    }
    public void divideAsteroid(Asteroid asteroid){
        Size size= asteroid.getSize();
        BufferedImage[] texturas=size.texturas;
        Size newSize=null;
        switch(size){
            case BIG:
                newSize=Size.MED;
                break;
            case MED:
                newSize=Size.SMALL;
                break;
            case SMALL:
                newSize=Size.TINY;
                break;
            case TINY:
                break;
            default:  
                return;
        }
        for(int i=0;i<size.cantidad;i++){
            moving_obj.add(new Asteroid(asteroid.getPosicion(),new Vector2D(0, 1).setDireccion(Math.random()*Math.PI*2),Constantes.vaste*Math.random()+1,texturas[(int)(Math.random()*texturas.length)],this,newSize));
        }
        }
    private void startWave(){
        double x,y;
        for(int i=0;i<asteroids;i++){
            x=i%2==0 ? Math.random()*Constantes.WIDTH :0;
            y=i%2==0 ? 0: Math.random()*Constantes.HEIGHT;
            BufferedImage textura=Assets.met_g[(int)(Math.random()*Assets.met_g.length)];
            moving_obj.add(new Asteroid(new Vector2D(x, y),new Vector2D(0, 1).setDireccion(Math.random()*Math.PI*2),Constantes.vaste*Math.random()+1,textura,this,Size.BIG));
        }
        asteroids ++;
    }
    public void playExplosion(Vector2D posicion){
        explosiones.add(new Animacion(Assets.expl,50,posicion.substract(new Vector2D(Assets.expl[0].getWidth()/2,Assets.expl[0].getHeight()/2))));
    }
    public void update(){
        for(int i=0;i<moving_obj.size();i++)
            moving_obj.get(i).update();   
        for(int i=0;i<explosiones.size();i++){
            Animacion anim = explosiones.get(i);
            anim.update();
            if(anim.isRunning()){
                explosiones.remove(i);
            }
        }
        for(int i=0;i<moving_obj.size();i++)
            if(moving_obj.get(i) instanceof Asteroid)
                return;
        startWave();
    }
    public void draw(Graphics g){
        Graphics2D G2D=(Graphics2D)g;
        G2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        for(int i=0;i<moving_obj.size();i++){
            moving_obj.get(i).draw(g);
        }    
        for(int i=0;i<explosiones.size();i++){
            Animacion anim = explosiones.get(i);
            G2D.drawImage(anim.getCurrentFrame(), (int)anim.getPosicion().getX(), (int)anim.getPosicion().getY(), null);
        }
        puntajes(g);
        vidas(g);
    }
    private void vidas(Graphics g){
        Vector2D livePosicion=new Vector2D(25,25);
        g.drawImage(Assets.life,(int)livePosicion.getX(),(int)livePosicion.getY(),null);
        g.drawImage(Assets.num[10],(int)livePosicion.getX()+40,(int)livePosicion.getY()+5,null);
        String liveToString=Integer.toString(lives);
        Vector2D pos=new Vector2D(livePosicion.getX(),livePosicion.getY());
        for(int i=0;i<liveToString.length();i++){
            int numero=Integer.parseInt(liveToString.substring(i,i+1));
            if(numero<=0)
                break;
            g.drawImage(Assets.num[numero],(int)pos.getX()+60, (int)pos.getY()+5, null);
            pos.setX(pos.getX()+20);
        }
    }
    private void puntajes(Graphics g){
        Vector2D pos=new Vector2D(800,25);
        String scoreToString = Integer.toString(puntaje);
        for(int i=0;i<scoreToString.length();i++){
            g.drawImage(Assets.num[Integer.parseInt(scoreToString.substring(i,i+1))], (int)pos.getX(), (int)pos.getY(), null);
            pos.setX(pos.getX()+20);
        }
    }
    public ArrayList<Moving_obj> getMoving_obj(){
        return moving_obj;
    }
    public Jugador getJugador(){
        return player;
    }
    public void restarvida(){
        lives--;
    }
}
