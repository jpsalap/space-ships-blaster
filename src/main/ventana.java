package main;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import GameObjects.Constantes;
import GameObjects.ShipStyle;
import graphics.Assets;
import graphics.Starfield;
import input.ControlScheme;
import input.Keyboard;
import input.MouseInput;
import math.Vector2D;
import states.Estado_juego;
import states.TitleScreen;

public final class ventana extends JFrame implements Runnable {

    private static final long serialVersionUID = 1L;
    private static final long MAX_ELAPSED_NS = 100_000_000L; // cap at 100ms to prevent sleep/drag fast-forward bursts

    private enum Screen {
        TITLE,
        PLAYING
    }

    private final Canvas canvas;
    private transient Thread hilo;
    private volatile boolean running = false;

    private transient BufferStrategy bs;
    private transient Graphics g;

    private final int fps = 60;
    private final double target_time = 1000000000.0 / fps;
    private double delta = 0;
    private int fpsrate = fps;
    private boolean showFps = false;

    private transient Starfield starfield;
    private transient HighScoreStore highScoreStore;
    private transient TitleScreen titleScreen;
    private transient Estado_juego gamestate;
    private Screen currentScreen = Screen.TITLE;
    private ShipStyle selectedShip = ShipStyle.BLUE;
    private ControlScheme controlScheme = ControlScheme.CLASSIC;
    private int highScore = 0;
    private final transient Keyboard keyb;
    private final transient MouseInput mouse;
    private final transient Cursor blankCursor;
    private boolean cursorHidden = false;

    public ventana() {
        setTitle("Space Ships Blaster");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        canvas = new Canvas();
        keyb = new Keyboard();
        mouse = new MouseInput();
        blankCursor = createBlankCursor();

        canvas.setPreferredSize(new Dimension(Constantes.WIDTH, Constantes.HEIGHT));
        canvas.setMaximumSize(new Dimension(Constantes.WIDTH, Constantes.HEIGHT));
        canvas.setMinimumSize(new Dimension(Constantes.WIDTH, Constantes.HEIGHT));
        canvas.setFocusable(true);
        canvas.setIgnoreRepaint(true);
        setIgnoreRepaint(true);

        add(canvas);
        pack();
        setLocationRelativeTo(null);
        canvas.addKeyListener(keyb);
        canvas.addFocusListener(keyb);
        canvas.addMouseListener(mouse);
        canvas.addMouseMotionListener(mouse);
        canvas.addFocusListener(mouse);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                running = false;
                Sonidos.closeAll();
            }
        });
        setVisible(true);
        canvas.requestFocus();
    }

    private static Cursor createBlankCursor() {
        if (GraphicsEnvironment.isHeadless()) {
            return Cursor.getDefaultCursor();
        }
        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        return Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank");
    }

    private void updateCursorVisibility(boolean hide) {
        if (hide != cursorHidden) {
            cursorHidden = hide;
            final Cursor targetCursor = hide ? blankCursor : Cursor.getDefaultCursor();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    canvas.setCursor(targetCursor);
                }
            });
        }
    }

    private void syncMouseFromCanvas() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        try {
            if (canvas.isShowing()) {
                Point p = canvas.getMousePosition();
                if (p != null) {
                    mouse.syncPointer(p.x, p.y, true);
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void main(String[] args) {
        new ventana().start();
    }

    private void update() {
        keyb.update();
        mouse.update();
        boolean typingName = (currentScreen == Screen.PLAYING && gamestate != null && gamestate.isEnteringName());
        if (!typingName && Keyboard.mutePressed) {
            Sonidos.toggleMute();
        }
        if (Keyboard.fpsPressed) {
            showFps = !showFps;
        }

        if (currentScreen == Screen.TITLE) {
            updateCursorVisibility(false);
            starfield.update(-0.45, 0.25);
            titleScreen.update();
            selectedShip = titleScreen.getSelectedShip();
            controlScheme = titleScreen.getControlScheme();
            if (titleScreen.shouldStart()) {
                keyb.clearActionKeys();
                mouse.clearButtons();
                syncMouseFromCanvas();
                titleScreen.resetRequests();
                gamestate = new Estado_juego(selectedShip, highScore, highScoreStore, controlScheme);
                currentScreen = Screen.PLAYING;
            } else if (titleScreen.shouldQuit()) {
                exitGame();
            }
            return;
        }

        ControlScheme prevScheme = controlScheme;
        gamestate.update();
        controlScheme = gamestate.getControlScheme();
        if (controlScheme == ControlScheme.MOUSE_AIM && prevScheme != ControlScheme.MOUSE_AIM) {
            syncMouseFromCanvas();
        }
        highScore = Math.max(highScore, gamestate.getHighScore());

        boolean hideCursor = (controlScheme == ControlScheme.MOUSE_AIM
            && MouseInput.inside
            && !gamestate.isPaused()
            && !gamestate.isGameOver());
        updateCursorVisibility(hideCursor);

        if (!gamestate.isPaused()) {
            Vector2D vel = gamestate.getPlayerVelocity();
            starfield.update(-vel.getX() * 0.55 - 0.15, -vel.getY() * 0.55 + 0.08);
        }

        if (gamestate.shouldRestart()) {
            keyb.clearActionKeys();
            mouse.clearButtons();
            syncMouseFromCanvas();
            gamestate = new Estado_juego(selectedShip, highScore, highScoreStore, controlScheme);
        } else if (gamestate.shouldReturnToTitle()) {
            keyb.clearActionKeys();
            mouse.clearButtons();
            Sonidos.propulsor.stop();
            titleScreen.setHighScore(highScore);
            titleScreen.setControlScheme(controlScheme);
            titleScreen.resetRequests();
            currentScreen = Screen.TITLE;
        } else if (gamestate.shouldQuitFromGameOver()) {
            exitGame();
        }
    }

    private void exitGame() {
        running = false;
        Sonidos.closeAll();
        dispose();
        System.exit(0);
    }

    private void draw() {
        if (!canvas.isDisplayable()) {
            return;
        }
        try {
            bs = canvas.getBufferStrategy();
            if (bs == null) {
                canvas.createBufferStrategy(3);
                return;
            }
            do {
                do {
                    g = bs.getDrawGraphics();
                    try {
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
                    } finally {
                        g.dispose();
                    }
                } while (bs.contentsRestored());
                bs.show();
            } while (bs.contentsLost());
        } catch (IllegalStateException ignored) {
        }
    }

    private void inicio() {
        Assets.inicio();
        highScoreStore = new HighScoreStore();
        highScore = highScoreStore.getTopScore();
        controlScheme = ControlScheme.loadSaved();
        starfield = new Starfield(Constantes.WIDTH, Constantes.HEIGHT);
        titleScreen = new TitleScreen(selectedShip, highScore, highScoreStore, controlScheme);
        currentScreen = Screen.TITLE;
        Sonidos.espacio.loop();
    }

    @Override
    public void run() {
        long now;
        long lastTime = System.nanoTime();
        int frames = 0;
        long time = 0;

        inicio();
        lastTime = System.nanoTime();

        while (running) {
            now = System.nanoTime();
            long elapsed = Math.min(Math.max(0L, now - lastTime), MAX_ELAPSED_NS);
            delta = Math.min(delta + elapsed / target_time, 3.0);
            time += elapsed;
            lastTime = now;

            int updates = 0;
            while (delta >= 1.0 && updates < 3) {
                update();
                delta -= 1.0;
                updates++;
            }
            if (updates > 0) {
                draw();
                frames++;
            } else {
                long remainingNs = (long) ((1.0 - delta) * target_time);
                long sleepMs = (remainingNs - 1_500_000L) / 1_000_000L;
                try {
                    if (sleepMs >= 1L) {
                        Thread.sleep(sleepMs);
                    } else {
                        Thread.yield();
                    }
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
}
