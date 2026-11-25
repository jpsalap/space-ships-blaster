package graphics;

import java.awt.Font;
import java.awt.image.BufferedImage;

public class Assets {
    public static BufferedImage jugador;
    public static BufferedImage playerBlue, playerGreen, playerRed;
    public static BufferedImage life;
    public static BufferedImage lifeBlue, lifeGreen, lifeRed;
    public static BufferedImage ufo;
    public static BufferedImage prop, prop2;
    public static BufferedImage blueLaser, greenLaser, redLaser;
    public static Font fontTitle, fontBig, fontMed, fontSmall, fontTiny;
    public static BufferedImage[] met_g = new BufferedImage[4];
    public static BufferedImage[] met_m = new BufferedImage[2];
    public static BufferedImage[] met_s = new BufferedImage[2];
    public static BufferedImage[] met_t = new BufferedImage[2];
    public static BufferedImage[] expl = new BufferedImage[9];
    public static BufferedImage[] num = new BufferedImage[11];

    public static void inicio() {
        playerBlue = Loader.ImageLoader("/naves/ship_blue.png");
        playerGreen = Loader.ImageLoader("/naves/ship_green.png");
        playerRed = Loader.ImageLoader("/naves/ship_red.png");
        jugador = playerBlue;

        lifeBlue = Loader.ImageLoader("/vidas/life_blue.png");
        lifeGreen = Loader.ImageLoader("/vidas/life_green.png");
        lifeRed = Loader.ImageLoader("/vidas/life_red.png");
        life = lifeBlue;

        ufo = Loader.ImageLoader("/naves/ufo.png");

        prop = Loader.ImageLoader("/efectos/fire01.png");
        prop2 = Loader.ImageLoader("/efectos/fire04.png");
        blueLaser = Loader.ImageLoader("/lasers/laserBlue01.png");
        greenLaser = Loader.ImageLoader("/lasers/laserGreen11.png");
        redLaser = Loader.ImageLoader("/lasers/laserRed01.png");

        for (int i = 0; i < met_g.length; i++) {
            met_g[i] = Loader.ImageLoader("/asteroids/big" + (i + 1) + ".png");
        }
        for (int i = 0; i < met_m.length; i++) {
            met_m[i] = Loader.ImageLoader("/asteroids/med" + (i + 1) + ".png");
        }
        for (int i = 0; i < met_s.length; i++) {
            met_s[i] = Loader.ImageLoader("/asteroids/small" + (i + 1) + ".png");
        }
        for (int i = 0; i < met_t.length; i++) {
            met_t[i] = Loader.ImageLoader("/asteroids/tiny" + (i + 1) + ".png");
        }
        for (int i = 0; i < expl.length; i++) {
            expl[i] = Loader.ImageLoader("/explosion/" + i + ".png");
        }
        for (int i = 0; i < num.length; i++) {
            num[i] = Loader.ImageLoader("/numeros/num" + i + ".png");
        }

        fontTitle = loadFontWithFallback("/fuente/kenvector_future.ttf", 52, Font.BOLD);
        fontBig = loadFontWithFallback("/fuente/kenvector_future.ttf", 44, Font.BOLD);
        fontMed = loadFontWithFallback("/fuente/kenvector_future.ttf", 22, Font.BOLD);
        fontSmall = loadFontWithFallback("/fuente/kenvector_future.ttf", 15, Font.PLAIN);
        fontTiny = loadFontWithFallback("/fuente/kenvector_future.ttf", 11, Font.PLAIN);
    }

    private static Font loadFontWithFallback(String path, int size, int fallbackStyle) {
        Font f = Loader.loadFont(path, size);
        return f != null ? f : new Font("SansSerif", fallbackStyle, size);
    }
}
