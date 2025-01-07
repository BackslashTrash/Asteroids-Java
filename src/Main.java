
import javax.swing.*;
public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Asteroids!");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        Player pan = new Player();
        frame.add(pan);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        pan.startThread();
    }
}