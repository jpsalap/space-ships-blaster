package main;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;
import javax.swing.JFrame;
import GameObjects.Constantes;
import GameObjects.ShipStyle;
import graphics.Assets;
import graphics.Starfield;
import input.Keyboard;
import math.Vector2D;
import states.Estado_juego;
import states.TitleScreen;

public class ventana extends JFrame implements Runnable {

    private static final long serialVersionUID = 1L;

    private enum Screen {
        TITLE,
        PLAYING
    }

    private Canvas canvas;
    private Thread hilo;
    private volatile boolean running = false;

    private BufferStrategy bs;
    private Graphics g;

    private final int fps = 60;
    private double target_time = 1000000000.0 / fps;
    private double delta = 0;
    private int fpsrate = fps;
    private boolean showFps = false;

    private Starfield starfield;
    private TitleScreen titleScreen;
    private Estado_juego gamestate;
    private Screen currentScreen = Screen.TITLE;
    private ShipStyle selectedShip = ShipStyle.BLUE;
    private int highScore = 0;
    private Keyboard keyb;

    public ventana() {
        setTitle("Space Ships Blaster");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        canvas = new Canvas();
        keyb = new Keyboard();

        canvas.setPreferredSize(new Dimension(Constantes.WIDTH, Constantes.HEIGHT));
        canvas.setMaximumSize(new Dimension(Constantes.WIDTH, Constantes.HEIGHT));
        canvas.setMinimumSize(new Dimension(Constantes.WIDTH, Constantes.HEIGHT));
        canvas.setFocusable(true);

        add(canvas);
        pack();
        setLocationRelativeTo(null);
        canvas.addKeyListener(keyb);
        setVisible(true);
        canvas.requestFocus();
    }

    public static void main(String[] args) {
        new ventana().start();
        Sonidos.espacio.loop();
    }

    private void update() {
        keyb.update();
        if (Keyboard.mutePressed) {
            Sonidos.toggleMute();
        }
        if (Keyboard.fpsPressed) {
            showFps = !showFps;
        }

        if (currentScreen == Screen.TITLE) {
            starfield.update(-0.45, 0.25);
            titleScreen.update();
            selectedShip = titleScreen.getSelectedShip();
            if (titleScreen.shouldStart()) {
                keyb.clearActionKeys();
                titleScreen.resetRequests();
                gamestate = new Estado_juego(selectedShip, highScore);
                currentScreen = Screen.PLAYING;
            } else if (titleScreen.shouldQuit()) {
                exitGame();
            }
            return;
        }

        gamestate.update();
        highScore = Math.max(highScore, gamestate.getHighScore());

        if (!gamestate.isPaused()) {
            Vector2D vel = gamestate.getPlayerVelocity();
            starfield.update(-vel.getX() * 0.55 - 0.15, -vel.getY() * 0.55 + 0.08);
        }

        if (gamestate.shouldRestart()) {
            keyb.clearActionKeys();
            gamestate = new Estado_juego(selectedShip, highScore);
        } else if (gamestate.shouldReturnToTitle()) {
            keyb.clearActionKeys();
            Sonidos.propulsor.stop();
            titleScreen.setHighScore(highScore);
            titleScreen.resetRequests();
            currentScreen = Screen.TITLE;
        } else if (gamestate.isGameOver() && Keyboard.escapePressed) {
            exitGame();
        }
    }

    private void exitGame() {
        running = false;
        Sonidos.propulsor.stop();
        Sonidos.espacio.stop();
        dispose();
        System.exit(0);
    }

    private void draw() {
        bs = canvas.getBufferStrategy();
        if (bs == null) {
            canvas.createBufferStrategy(3);
            return;
        }
        g = bs.getDrawGraphics();
        Graphics2D g2d = (Graphics2D) g;

        starfield.draw(g2d);
        if (currentScreen == Screen.TITLE) {
            titleScreen.draw(g2d);
        } else {
            gamestate.draw(g);
        }

        if (showFps) {
            g2d.setFont(Assets.fontTiny);
            g2d.setColor(new Color(180, 255, 180, 200));
            g2d.drawString("FPS: " + fpsrate, 12, Constantes.HEIGHT - 12);
        }

        g.dispose();
        bs.show();
    }

    private void inicio() {
        Assets.inicio();
        starfield = new Starfield(Constantes.WIDTH, Constantes.HEIGHT);
        titleScreen = new TitleScreen(selectedShip, highScore);
        currentScreen = Screen.TITLE;
    }

    @Override
    public void run() {
        long now = 0;
        long lastTime = System.nanoTime();
        int frames = 0;
        long time = 0;

        inicio();

        while (running) {
            now = System.nanoTime();
            long elapsed = now - lastTime;
            delta += elapsed / target_time;
            time += elapsed;
            lastTime = now;
            if (delta >= 1) {
                update();
                draw();
                delta--;
                frames++;
            } else {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            if (time >= 1000000000L) {
                fpsrate = frames;
                frames = 0;
                time = 0;
            }
        }
    }

    private void start() {
        running = true;
        hilo = new Thread(this);
        hilo.start();
    }

    private void stop() {
        running = false;
        if (hilo != null && Thread.currentThread() != hilo) {
            try {
                hilo.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
