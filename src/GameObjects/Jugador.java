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

public class Jugador extends Moving_obj {
    private Vector2D direccion;
    private Vector2D aceleracion;
    private boolean acelerando = false;
    private Crono firerate;
    private boolean spawning, visible;
    private Crono spawningTime, titilar;
    private ShipStyle shipStyle;
    private int flameTick = 0;

    public Jugador(Vector2D posicion, Vector2D velocidad, double maxV, BufferedImage textura, Estado_juego gameState) {
        this(posicion, velocidad, maxV, textura, gameState, ShipStyle.BLUE);
    }

    public Jugador(Vector2D posicion, Vector2D velocidad, double maxV, BufferedImage textura, Estado_juego gameState, ShipStyle shipStyle) {
        super(posicion, velocidad, maxV, textura, gameState);
        this.gameState = gameState;
        this.shipStyle = shipStyle != null ? shipStyle : ShipStyle.BLUE;
        direccion = new Vector2D(0, -1);
        aceleracion = new Vector2D();
        firerate = new Crono();
        spawningTime = new Crono();
        titilar = new Crono();
        visible = true;
    }

    @Override
    public void update() {
        if (!spawningTime.isRunning()) {
            spawning = false;
            visible = true;
        }
        if (spawning) {
            if (!titilar.isRunning()) {
                titilar.run(Constantes.titilar);
                visible = !visible;
            }
        }
        if (Keyboard.right) {
            angulo += Constantes.deltaangulo;
        }
        if (Keyboard.left) {
            angulo -= Constantes.deltaangulo;
        }
        direccion = direccion.setDireccion(angulo - Math.PI / 2);
        if (Keyboard.shoot && !firerate.isRunning() && !spawning) {
            gameState.getMoving_obj().add(0, new Laser(
                getCenter().add(direccion.scale(width / 2.0)),
                direccion,
                Constantes.vlaser,
                angulo,
                shipStyle.getLaserTexture(),
                gameState
            ));
            firerate.run(Constantes.vdisparo);
            Sonidos.Disparo.play();
        }
        if (Keyboard.up) {
            aceleracion = direccion.scale(Constantes.acc);
            acelerando = true;
            flameTick++;
            Sonidos.propulsor.loop();
        } else {
            double decel = Constantes.acc / 2;
            if (velocidad.getMagni() > decel) {
                aceleracion = (velocidad.scale(-1).normalizar()).scale(decel);
            } else {
                velocidad = new Vector2D();
                aceleracion = new Vector2D();
            }
            acelerando = false;
            Sonidos.propulsor.stop();
        }
        velocidad = velocidad.add(aceleracion);
        velocidad = velocidad.limitev(maxV);
        posicion = posicion.add(velocidad);

        // Smooth wrap-around using sprite bounds
        if (posicion.getX() > Constantes.WIDTH) {
            posicion.setX(-width);
        } else if (posicion.getX() < -width) {
            posicion.setX(Constantes.WIDTH);
        }
        if (posicion.getY() > Constantes.HEIGHT) {
            posicion.setY(-height);
        } else if (posicion.getY() < -height) {
            posicion.setY(Constantes.HEIGHT);
        }

        firerate.update();
        spawningTime.update();
        titilar.update();
        collidesWith();
    }

    @Override
    public void destroy() {
        Sonidos.propulsor.stop();
        Sonidos.playerHit.play();
        gameState.onPlayerHit(getCenter(), shipStyle.getAccentColor());
        gameState.restarvida();
        if (Estado_juego.lives <= 0) {
            super.destroy();
            return;
        }
        spawning = true;
        spawningTime.run(Constantes.spawing_t);
        restablecer();
    }

    private void restablecer() {
        angulo = 0;
        velocidad = new Vector2D();
        aceleracion = new Vector2D();
        direccion = new Vector2D(0, -1);
        acelerando = false;
        posicion = new Vector2D(
            Constantes.WIDTH / 2 - width / 2,
            Constantes.HEIGHT / 2 - height / 2
        );
    }

    @Override
    public void draw(Graphics g) {
        if (!visible) {
            return;
        }
        Graphics2D G2D = (Graphics2D) g;
        at1 = AffineTransform.getTranslateInstance(posicion.getX() + width / 2 + 5, posicion.getY() + height / 2 + 10);
        at2 = AffineTransform.getTranslateInstance(posicion.getX() + 5, posicion.getY() + height / 2 + 10);
        at1.rotate(angulo, -5, -10);
        at2.rotate(angulo, width / 2 - 5, -10);
        if (acelerando) {
            BufferedImage flame = ((flameTick / 4) % 2 == 0 || Assets.prop2 == null) ? Assets.prop : Assets.prop2;
            G2D.drawImage(flame, at1, null);
            G2D.drawImage(flame, at2, null);
        }
        at = AffineTransform.getTranslateInstance(posicion.getX(), posicion.getY());
        at.rotate(angulo, width / 2, height / 2);
        G2D.drawImage(textura, at, null);
    }

    public boolean isSpawning() {
        return spawning;
    }

    public Vector2D getVelocidad() {
        return velocidad;
    }

    public ShipStyle getShipStyle() {
        return shipStyle;
    }
}
