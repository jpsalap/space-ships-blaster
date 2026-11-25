package main;

//librerias JDK
//java.awt me permite trabajar con graficos
import java.awt.Canvas;
import java.awt.Color;//colocarle color
import java.awt.Dimension;//permite darle dimensiones a cualquier objeto segun coordenadas
import java.awt.Graphics;//me permite graficar y actualizar en la ventana
import java.awt.image.BufferStrategy;//manejo de recursos
import javax.swing.JFrame;//caracteristicas que se le dan a una ventana.
//clases
import GameObjects.Constantes;
import graphics.Assets;
import input.Keyboard;
import states.Estado_juego;


//public private protected modifica el acceso
//public permite que sea accesible desde cualquier clase
//private solo permite que sea accesible en la clase donde se declara
//protected permite que sea accesible en la clase, y dentro del paquete donde se trabaje
public class ventana extends JFrame implements Runnable{//
    
    private static final long serialVersionUID=1L;
    private Canvas canvas;
    private Thread hilo;
    private volatile boolean running=false;
    
    private BufferStrategy bs;
    private Graphics g;
    
    private final int fps=60;
    private double target_time= 1000000000/fps;
    private double delta=0;
    private int fpsrate=fps;
    
    private Estado_juego gamestate;
    private Keyboard keyb;
    public ventana(){
        setTitle("Space Ships Blaster");
        setSize(Constantes.WIDTH,Constantes.HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
                
        canvas=new Canvas();
        keyb=new Keyboard();
        
        canvas.setPreferredSize(new Dimension(Constantes.WIDTH,Constantes.HEIGHT));
        canvas.setMaximumSize(new Dimension(Constantes.WIDTH,Constantes.HEIGHT));
        canvas.setMinimumSize(new Dimension(Constantes.WIDTH,Constantes.HEIGHT));
        canvas.setFocusable(true);
        
        add(canvas);
        canvas.addKeyListener(keyb);
        setVisible(true);
    }    
    public static void main(String[] args) {
        new ventana().start();
        Sonidos.espacio.loop();
    }

    private void update(){
        keyb.update();
        gamestate.update();
    }
    private void draw(){
        bs=canvas.getBufferStrategy();
        if(bs==null){
            canvas.createBufferStrategy(3);
            return;
        }
        g=bs.getDrawGraphics();
        //Comienza a dibujar
        g.setColor(Color.black);
        g.fillRect(0, 0, Constantes.WIDTH, Constantes.HEIGHT);
        gamestate.draw(g);
        g.drawString(""+fpsrate, 10,20);
        
        //Fin dibujo
        g.dispose();
        bs.show();
    }
    private void inicio(){
        Assets.inicio();
        gamestate =new Estado_juego();
    }
    @Override
    public void run(){
        long now=0;
        long lastTime=System.nanoTime();
        int frames=0;
        long time=0;
        
        inicio();
        
        while(running){
            now= System.nanoTime();
            delta+=(now-lastTime)/target_time;
            lastTime=now;
            if(delta>=1){
                update();
                draw();
                delta--;
                frames++;
                if(Estado_juego.lives<=0){
                    running=false;
                }
            }
            if(time>=1000000000){
                fpsrate=frames;
                frames=0;
                time=0;
            }
        }
    }
    private void start(){
        running=true;
        hilo = new Thread(this);
        hilo.start();
    }
    private void stop(){
        running=false;
        if(hilo!=null && Thread.currentThread()!=hilo){
            try {
                hilo.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
