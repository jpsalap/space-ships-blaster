package GameObjects;

import graphics.Assets;
import java.awt.image.BufferedImage;

public enum Size {
    BIG(2,Assets.met_m),MED(2,Assets.met_s),SMALL(2,Assets.met_t),TINY(0,null);
    public int cantidad;
    public BufferedImage[] texturas;
    private Size(int cantidad,BufferedImage[] texturas){
        this.cantidad=cantidad;
        this.texturas=texturas;
    }
}
