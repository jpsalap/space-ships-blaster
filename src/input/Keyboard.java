package input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Keyboard implements KeyListener {

    private final boolean[] keys = new boolean[256];
    private final boolean[] prevKeys = new boolean[256];
    private final boolean[] justPressed = new boolean[256];

    public static boolean up, left, right, shoot, enter, escape;
    public static boolean leftPressed, rightPressed, enterPressed, shootPressed, escapePressed;
    public static boolean pausePressed, mutePressed, fpsPressed, titlePressed;

    public Keyboard() {
        up = false;
        left = false;
        right = false;
        shoot = false;
        enter = false;
        escape = false;
    }

    public void update() {
        for (int i = 0; i < keys.length; i++) {
            justPressed[i] = keys[i] && !prevKeys[i];
            prevKeys[i] = keys[i];
        }
        up = keys[KeyEvent.VK_UP];
        left = keys[KeyEvent.VK_LEFT];
        right = keys[KeyEvent.VK_RIGHT];
        shoot = keys[KeyEvent.VK_SPACE];
        enter = keys[KeyEvent.VK_ENTER];
        escape = keys[KeyEvent.VK_ESCAPE];

        leftPressed = justPressed[KeyEvent.VK_LEFT];
        rightPressed = justPressed[KeyEvent.VK_RIGHT];
        enterPressed = justPressed[KeyEvent.VK_ENTER];
        shootPressed = justPressed[KeyEvent.VK_SPACE];
        escapePressed = justPressed[KeyEvent.VK_ESCAPE];
        pausePressed = justPressed[KeyEvent.VK_P];
        mutePressed = justPressed[KeyEvent.VK_M];
        fpsPressed = justPressed[KeyEvent.VK_F3];
        titlePressed = justPressed[KeyEvent.VK_T];
    }

    public void clearActionKeys() {
        int[] toClear = {
            KeyEvent.VK_SPACE,
            KeyEvent.VK_ENTER,
            KeyEvent.VK_ESCAPE,
            KeyEvent.VK_P,
            KeyEvent.VK_T
        };
        for (int code : toClear) {
            keys[code] = false;
            prevKeys[code] = false;
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
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode >= 0 && keyCode < keys.length) {
            keys[keyCode] = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode >= 0 && keyCode < keys.length) {
            keys[keyCode] = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}
