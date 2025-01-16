import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {
    public boolean up, pause = false, left, right, shoot = false;

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_UP){        //Up Arrow
            up = true;
        }
        if (code == KeyEvent.VK_P){           //P
            pause = !pause;
        }
        if (code == KeyEvent.VK_LEFT){     //Left arrow
            left = true;
        }
        if (code == KeyEvent.VK_RIGHT){   //Right arrow
            right = true;
        }
        if (code == KeyEvent.VK_SPACE) {    //Space
            shoot = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_UP){
            up = false;
        }
        if (code == KeyEvent.VK_LEFT){
            left = false;
        }
        if (code == KeyEvent.VK_RIGHT){
            right = false;
        }
        if (code == KeyEvent.VK_SPACE) {
            shoot = false;
        }
    }
}
