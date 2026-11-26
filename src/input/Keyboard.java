package input;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Keyboard implements KeyListener, FocusListener {

    private final boolean[] keys = new boolean[256];
    private final boolean[] prevKeys = new boolean[256];
    private final boolean[] pressedSinceUpdate = new boolean[256];
    private final boolean[] justPressed = new boolean[256];
    private final StringBuilder typedBuffer = new StringBuilder();
    private int pendingBackspaces = 0;

    public static boolean up, down, left, right, shoot, enter, escape;
    public static boolean leftPressed, rightPressed, enterPressed, shootPressed, escapePressed;
    public static boolean pausePressed, mutePressed, fpsPressed, titlePressed;
    public static boolean dashPressed, scoresPressed, controlsPressed;
    public static String frameTypedChars = "";
    public static int frameBackspaces = 0;

    public Keyboard() {
        up = false;
        down = false;
        left = false;
        right = false;
        shoot = false;
        enter = false;
        escape = false;
    }

    public synchronized void update() {
        for (int i = 0; i < keys.length; i++) {
            justPressed[i] = pressedSinceUpdate[i] || (keys[i] && !prevKeys[i]);
            pressedSinceUpdate[i] = false;
            prevKeys[i] = keys[i];
        }
        // Arrows OR WASD (down/S only used in Direct 8-Way and Mouse Aim modes)
        up = keys[KeyEvent.VK_UP] || keys[KeyEvent.VK_W];
        down = keys[KeyEvent.VK_DOWN] || keys[KeyEvent.VK_S];
        left = keys[KeyEvent.VK_LEFT] || keys[KeyEvent.VK_A];
        right = keys[KeyEvent.VK_RIGHT] || keys[KeyEvent.VK_D];
        shoot = keys[KeyEvent.VK_SPACE];
        enter = keys[KeyEvent.VK_ENTER];
        escape = keys[KeyEvent.VK_ESCAPE];

        leftPressed = justPressed[KeyEvent.VK_LEFT] || justPressed[KeyEvent.VK_A];
        rightPressed = justPressed[KeyEvent.VK_RIGHT] || justPressed[KeyEvent.VK_D];
        enterPressed = justPressed[KeyEvent.VK_ENTER];
        shootPressed = justPressed[KeyEvent.VK_SPACE];
        escapePressed = justPressed[KeyEvent.VK_ESCAPE];
        pausePressed = justPressed[KeyEvent.VK_P];
        mutePressed = justPressed[KeyEvent.VK_M];
        fpsPressed = justPressed[KeyEvent.VK_F3];
        titlePressed = justPressed[KeyEvent.VK_T];
        dashPressed = justPressed[KeyEvent.VK_SHIFT];
        scoresPressed = justPressed[KeyEvent.VK_H];
        controlsPressed = justPressed[KeyEvent.VK_C];

        if (typedBuffer.length() > 0) {
            frameTypedChars = typedBuffer.toString();
            typedBuffer.setLength(0);
        } else {
            frameTypedChars = "";
        }
        frameBackspaces = pendingBackspaces;
        pendingBackspaces = 0;
    }

    public synchronized void clearActionKeys() {
        int[] toClear = {
            KeyEvent.VK_SPACE,
            KeyEvent.VK_ENTER,
            KeyEvent.VK_ESCAPE,
            KeyEvent.VK_P,
            KeyEvent.VK_T,
            KeyEvent.VK_SHIFT,
            KeyEvent.VK_H,
            KeyEvent.VK_C
        };
        for (int code : toClear) {
            keys[code] = false;
            prevKeys[code] = false;
            pressedSinceUpdate[code] = false;
            justPressed[code] = false;
        }
        shoot = false;
        enter = false;
        escape = false;
        shootPressed = false;
        enterPressed = false;
        escapePressed = false;
        pausePressed = false;
        titlePressed = false;
        dashPressed = false;
        scoresPressed = false;
        controlsPressed = false;
        typedBuffer.setLength(0);
        pendingBackspaces = 0;
        frameTypedChars = "";
        frameBackspaces = 0;
    }

    public synchronized void clearAllKeys() {
        for (int i = 0; i < keys.length; i++) {
            keys[i] = false;
            prevKeys[i] = false;
            pressedSinceUpdate[i] = false;
            justPressed[i] = false;
        }
        up = false;
        down = false;
        left = false;
        right = false;
        shoot = false;
        enter = false;
        escape = false;
        leftPressed = false;
        rightPressed = false;
        enterPressed = false;
        shootPressed = false;
        escapePressed = false;
        pausePressed = false;
        mutePressed = false;
        fpsPressed = false;
        titlePressed = false;
        dashPressed = false;
        scoresPressed = false;
        controlsPressed = false;
        typedBuffer.setLength(0);
        pendingBackspaces = 0;
        frameTypedChars = "";
        frameBackspaces = 0;
    }

    @Override
    public synchronized void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_BACK_SPACE) {
            pendingBackspaces++;
        }
        if (keyCode >= 0 && keyCode < keys.length) {
            if (!keys[keyCode]) {
                pressedSinceUpdate[keyCode] = true;
            }
            keys[keyCode] = true;
        }
    }

    @Override
    public synchronized void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode >= 0 && keyCode < keys.length) {
            keys[keyCode] = false;
        }
    }

    @Override
    public synchronized void keyTyped(KeyEvent e) {
        char ch = e.getKeyChar();
        if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')
                || (ch >= '0' && ch <= '9') || ch == ' ' || ch == '-' || ch == '_') {
            if (typedBuffer.length() < 32) {
                typedBuffer.append(Character.toUpperCase(ch));
            }
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent e) {
        clearAllKeys();
    }
}
