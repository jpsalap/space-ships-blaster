package GameObjects;

import graphics.Assets;
import java.awt.Color;
import java.awt.image.BufferedImage;

public enum ShipStyle {
    BLUE("INTERCEPTOR", "BALANCED", new Color(46, 195, 250), 7.2, 0.22, 0.105, 110, 4, 4, 4, 6.2),
    GREEN("VIPER", "HIGH SPEED", new Color(110, 228, 72), 8.8, 0.28, 0.128, 135, 5, 3, 5, 5.2),
    RED("STRIKER", "RAPID FIRE", new Color(242, 74, 62), 6.2, 0.18, 0.092, 75, 3, 5, 3, 7.8);

    private final String displayName;
    private final String roleTag;
    private final Color accentColor;
    private final double maxSpeed;
    private final double acceleration;
    private final double turnSpeed;
    private final int fireCooldownMs;
    private final int speedRating;
    private final int fireRating;
    private final int agilityRating;
    private final double nozzleOffsetX;

    ShipStyle(String displayName, String roleTag, Color accentColor,
              double maxSpeed, double acceleration, double turnSpeed, int fireCooldownMs,
              int speedRating, int fireRating, int agilityRating, double nozzleOffsetX) {
        this.displayName = displayName;
        this.roleTag = roleTag;
        this.accentColor = accentColor;
        this.maxSpeed = maxSpeed;
        this.acceleration = acceleration;
        this.turnSpeed = turnSpeed;
        this.fireCooldownMs = fireCooldownMs;
        this.speedRating = speedRating;
        this.fireRating = fireRating;
        this.agilityRating = agilityRating;
        this.nozzleOffsetX = nozzleOffsetX;
    }

    public String getDisplayName() { return displayName; }
    public String getRoleTag() { return roleTag; }
    public Color getAccentColor() { return accentColor; }
    public double getMaxSpeed() { return maxSpeed; }
    public double getAcceleration() { return acceleration; }
    public double getTurnSpeed() { return turnSpeed; }
    public int getFireCooldownMs() { return fireCooldownMs; }
    public int getSpeedRating() { return speedRating; }
    public int getFireRating() { return fireRating; }
    public int getAgilityRating() { return agilityRating; }
    public double getNozzleOffsetX() { return nozzleOffsetX; }

    public BufferedImage getShipTexture() {
        switch (this) {
            case GREEN: return Assets.playerGreen;
            case RED: return Assets.playerRed;
            case BLUE:
            default: return Assets.playerBlue;
        }
    }

    public BufferedImage getGameplayShipTexture() {
        BufferedImage img;
        switch (this) {
            case GREEN: img = Assets.playerGreenGame; break;
            case RED: img = Assets.playerRedGame; break;
            case BLUE:
            default: img = Assets.playerBlueGame; break;
        }
        return img != null ? img : getShipTexture();
    }

    public BufferedImage getLaserTexture() {
        switch (this) {
            case GREEN: return Assets.greenLaser;
            case RED: return Assets.redLaser;
            case BLUE:
            default: return Assets.blueLaser;
        }
    }

    public BufferedImage getLifeTexture() {
        switch (this) {
            case GREEN: return Assets.lifeGreen;
            case RED: return Assets.lifeRed;
            case BLUE:
            default: return Assets.lifeBlue;
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
