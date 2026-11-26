package graphics;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.imageio.ImageIO;

public class Loader {
    public static BufferedImage ImageLoader(String path) {
        URL url = Loader.class.getResource(path);
        if (url == null) {
            return null;
        }
        try {
            BufferedImage raw = ImageIO.read(url);
            return raw != null ? toCompatibleImage(raw, Transparency.TRANSLUCENT) : null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static BufferedImage createCompatibleImage(int width, int height, int transparency) {
        if (!GraphicsEnvironment.isHeadless()) {
            try {
                GraphicsConfiguration gc = GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice()
                    .getDefaultConfiguration();
                return gc.createCompatibleImage(width, height, transparency);
            } catch (Exception ignored) {
            }
        }
        int type = (transparency == Transparency.OPAQUE)
            ? BufferedImage.TYPE_INT_RGB
            : BufferedImage.TYPE_INT_ARGB_PRE;
        return new BufferedImage(width, height, type);
    }

    public static BufferedImage toCompatibleImage(BufferedImage src, int transparency) {
        if (src == null) {
            return null;
        }
        BufferedImage dst = createCompatibleImage(src.getWidth(), src.getHeight(), transparency);
        Graphics2D g = dst.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return dst;
    }

    public static Font loadFont(String path, int size) {
        try (InputStream is = Loader.class.getResourceAsStream(path)) {
            if (is == null) {
                return null;
            }
            return Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(Font.PLAIN, size);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
