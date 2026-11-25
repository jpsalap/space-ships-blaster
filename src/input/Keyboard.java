package input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Keyboard implements KeyListener{

    private boolean[] keys=new boolean[256];
    public static boolean up,left,right,shoot;
    public Keyboard(){
        up=false;
        left=false;
        right=false;
        shoot=false;
    }
    public void update(){
        up=keys[KeyEvent.VK_UP];
        left=keys[KeyEvent.VK_LEFT];
        right=keys[KeyEvent.VK_RIGHT];
        shoot=keys[KeyEvent.VK_SPACE];
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
