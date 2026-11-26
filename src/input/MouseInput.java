package input;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class MouseInput implements MouseListener, MouseMotionListener, FocusListener {
    private int curX = 500;
    private int curY = 250;
    private boolean curInside = false;
    private boolean leftHeld = false;
    private boolean rightHeld = false;
    private boolean prevRightHeld = false;
    private boolean rightClickQueued = false;

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
        rightPressed = rightClickQueued || (rightHeld && !prevRightHeld);
        rightClickQueued = false;
        prevRightHeld = rightHeld;
    }

    public synchronized void syncPointer(int px, int py, boolean isInside) {
        curX = px;
        curY = py;
        curInside = isInside;
        x = px;
        y = py;
        inside = isInside;
    }

    public synchronized void clearButtons() {
        leftHeld = false;
        rightHeld = false;
        prevRightHeld = false;
        rightClickQueued = false;
        leftDown = false;
        rightPressed = false;
    }

    @Override
    public synchronized void mousePressed(MouseEvent e) {
        Component comp = e.getComponent();
        if (comp != null && !comp.isFocusOwner()) {
            comp.requestFocusInWindow();
        }
        curX = e.getX();
        curY = e.getY();
        curInside = true;
        int btn = e.getButton();
        if (btn == MouseEvent.BUTTON1) {
            leftHeld = true;
        } else if (btn == MouseEvent.BUTTON3) {
            rightHeld = true;
            rightClickQueued = true;
        }
    }

    @Override
    public synchronized void mouseReleased(MouseEvent e) {
        curX = e.getX();
        curY = e.getY();
        int btn = e.getButton();
        if (btn == MouseEvent.BUTTON1) {
            leftHeld = false;
        } else if (btn == MouseEvent.BUTTON3) {
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
