package graphics;

import java.awt.image.BufferedImage;

public class Assets {
    public static BufferedImage jugador;
    public static BufferedImage prop;
    public static BufferedImage blueLaser,greenLaser,redLaser;
    public static BufferedImage life;
    public static BufferedImage[] met_g=new BufferedImage[4];
    public static BufferedImage[] met_m=new BufferedImage[2];
    public static BufferedImage[] met_s=new BufferedImage[2];
    public static BufferedImage[] met_t=new BufferedImage[2];
    public static BufferedImage[] expl=new BufferedImage[9];
    public static BufferedImage[] num=new BufferedImage[11];
    public static void inicio(){
        jugador=Loader.ImageLoader("/naves/player.png");
        prop=Loader.ImageLoader("/efectos/fire01.png");
        blueLaser=Loader.ImageLoader("/lasers/laserBlue01.png");
        greenLaser=Loader.ImageLoader("/lasers/laserGreen11.png");
        redLaser=Loader.ImageLoader("/lasers/laserRed01.png");
        life=Loader.ImageLoader("/vidas/life.png");
        for(int i=0;i<met_g.length;i++){
            met_g[i]=Loader.ImageLoader("/asteroids/big"+(i+1)+".png");
        }        
        for(int i=0;i<met_m.length;i++){
            met_m[i]=Loader.ImageLoader("/asteroids/med"+(i+1)+".png");
        }           
        for(int i=0;i<met_s.length;i++){
            met_s[i]=Loader.ImageLoader("/asteroids/small"+(i+1)+".png");
        }        
        for(int i=0;i<met_t.length;i++){
            met_t[i]=Loader.ImageLoader("/asteroids/tiny"+(i+1)+".png");
        }
        for(int i=0;i<expl.length;i++){
            expl[i]=Loader.ImageLoader("/explosion/"+i+".png");
        }
        for(int i=0;i<num.length;i++){
            num[i]=Loader.ImageLoader("/numeros/num"+i+".png");
        }
    }    
}
