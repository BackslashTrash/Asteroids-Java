import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MouseHandler implements MouseListener {

    public boolean respawnClicked, exitClicked, menuClicked;

    @Override
    public void mouseClicked(MouseEvent e) {
        if (Player.isGameOver()) {
            if (e.getX() > 390 && e.getX()< 540 && e.getY() > 260 && e.getY() < 300) {
                respawnClicked = true;
            } else {
                respawnClicked = false;
            }

            if (e.getX() > 390 && e.getX() < 540 && e.getY() > 400 && e.getY() <440) {
                menuClicked = true;
            }


        }

        if (!Player.isGameRunning()) {  //menu
            if (e.getX() > 390 && e.getX()< 540 && e.getY() > 260 && e.getY() < 300) {
                Player.setGameRunning(true);
            } else {
                Player.setGameRunning(false);
            }
            if (e.getX() > 390 && e.getX() < 540 && e.getY() > 170 && e.getY() < 210) {
                Player.setMute(!Player.isMute());

            }
            if (e.getX() > 390 && e.getX() < 540 && e.getY() > 400 && e.getY() <440) {
                int input = JOptionPane.showConfirmDialog(null, "Confirm exiting program?","Quit Game",JOptionPane.YES_NO_OPTION,JOptionPane.INFORMATION_MESSAGE);
                if (input == JOptionPane.YES_OPTION) {
                    exitClicked = true;
                }
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
