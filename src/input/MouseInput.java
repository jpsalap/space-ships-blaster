package input;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.SwingUtilities;

public class MouseInput implements MouseListener, MouseMotionListener, FocusListener {
    private int curX = 500;
    private int curY = 250;
    private boolean curInside = false;
    private boolean leftHeld = false;
    private boolean rightHeld = false;
    private boolean prevRightHeld = false;

    public static int x = 500;
    public static int y = 250;
    public static boolean inside = false;
    public static boolean leftDown = false;
    public static boolean rightPressed = false;

    public synchronized void update() {
        x = curX;
        y = curY;
        inside = curInside;
        leftDown = leftHeld;
        rightPressed = rightHeld && !prevRightHeld;
        prevRightHeld = rightHeld;
    }

    public synchronized void clearButtons() {
        leftHeld = false;
        rightHeld = false;
        prevRightHeld = false;
        leftDown = false;
        rightPressed = false;
    }

    @Override
    public synchronized void mousePressed(MouseEvent e) {
        curX = e.getX();
        curY = e.getY();
        curInside = true;
        if (SwingUtilities.isLeftMouseButton(e)) {
            leftHeld = true;
        }
        if (SwingUtilities.isRightMouseButton(e)) {
            rightHeld = true;
        }
    }

    @Override
    public synchronized void mouseReleased(MouseEvent e) {
        curX = e.getX();
        curY = e.getY();
        if (SwingUtilities.isLeftMouseButton(e)) {
            leftHeld = false;
        }
        if (SwingUtilities.isRightMouseButton(e)) {
            rightHeld = false;
        }
    }

    @Override
    public synchronized void mouseMoved(MouseEvent e) {
        curX = e.getX();
        curY = e.getY();
        curInside = true;
    }

    @Override
    public synchronized void mouseDragged(MouseEvent e) {
        curX = e.getX();
        curY = e.getY();
        curInside = true;
    }

    @Override
    public synchronized void mouseEntered(MouseEvent e) {
        curX = e.getX();
        curY = e.getY();
        curInside = true;
    }

    @Override
    public synchronized void mouseExited(MouseEvent e) {
        curInside = false;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public synchronized void focusLost(FocusEvent e) {
        clearButtons();
    }
}
