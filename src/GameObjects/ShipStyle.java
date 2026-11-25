package GameObjects;

import graphics.Assets;
import java.awt.Color;
import java.awt.image.BufferedImage;

public enum ShipStyle {
    BLUE("CYBER BLUE", new Color(46, 184, 240)),
    GREEN("VENOM GREEN", new Color(110, 215, 65)),
    RED("CRIMSON RED", new Color(235, 72, 62));

    private final String displayName;
    private final Color accentColor;

    ShipStyle(String displayName, Color accentColor) {
        this.displayName = displayName;
        this.accentColor = accentColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Color getAccentColor() {
        return accentColor;
    }

    public BufferedImage getShipTexture() {
        switch (this) {
            case GREEN:
                return Assets.playerGreen;
            case RED:
                return Assets.playerRed;
            case BLUE:
            default:
                return Assets.playerBlue;
        }
    }

    public BufferedImage getLaserTexture() {
        switch (this) {
            case GREEN:
                return Assets.greenLaser;
            case RED:
                return Assets.redLaser;
            case BLUE:
            default:
                return Assets.blueLaser;
        }
    }

    public BufferedImage getLifeTexture() {
        switch (this) {
            case GREEN:
                return Assets.lifeGreen;
            case RED:
                return Assets.lifeRed;
            case BLUE:
            default:
                return Assets.lifeBlue;
        }
    }

    public ShipStyle next() {
        ShipStyle[] vals = values();
        return vals[(ordinal() + 1) % vals.length];
    }

    public ShipStyle prev() {
        ShipStyle[] vals = values();
        return vals[(ordinal() - 1 + vals.length) % vals.length];
    }
}
