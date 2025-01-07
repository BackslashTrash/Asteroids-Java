import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MouseHandler implements MouseListener {

    public boolean respawnClicked;

    @Override
    public void mouseClicked(MouseEvent e) {
        if (Player.isGameOver()) {
            if (e.getX() > 340 && e.getX()< 520 && e.getY() > 260 && e.getY() < 370) {
                respawnClicked = true;
            } else {
                respawnClicked = false;
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
