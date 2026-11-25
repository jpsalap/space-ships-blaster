package input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Keyboard implements KeyListener{

    private boolean[] keys=new boolean[256];
    public static boolean up,left,right,shoot,enter,escape;
    public Keyboard(){
        up=false;
        left=false;
        right=false;
        shoot=false;
        enter=false;
        escape=false;
    }
    public void update(){
        up=keys[KeyEvent.VK_UP];
        left=keys[KeyEvent.VK_LEFT];
        right=keys[KeyEvent.VK_RIGHT];
        shoot=keys[KeyEvent.VK_SPACE];
        enter=keys[KeyEvent.VK_ENTER];
        escape=keys[KeyEvent.VK_ESCAPE];
    }
    public void clearActionKeys(){
        keys[KeyEvent.VK_SPACE]=false;
        keys[KeyEvent.VK_ENTER]=false;
        shoot=false;
        enter=false;
    }
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if(keyCode >= 0 && keyCode < keys.length){
            keys[keyCode]=true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if(keyCode >= 0 && keyCode < keys.length){
            keys[keyCode]=false;
        }
    }
    @Override
    public void keyTyped(KeyEvent e) { }
}
