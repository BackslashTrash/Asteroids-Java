import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

public class Bullets {
    private double x;
    private double y;
    private final double velocity = 5;
    private double angle;

    private Shape bulletShape = new Ellipse2D.Double(x,y,5,5);
    private int bulletTime = 0;
    Area bulletHitbox;

    public Bullets(double bulletX, double bulletY, double bulletAngle) {
        bulletX += Player.playerSizeX / 2 + 5;
        bulletY += Player.playerSizeY / 2 + 10;
        x = bulletX;
        y = bulletY;
        angle = bulletAngle;
        Path2D myPath2d = getPath2D();
        bulletHitbox = new Area(myPath2d);
    }
    private Path2D getPath2D() {
        Path2D myPath2d = new Path2D.Double();
        myPath2d.moveTo(0,0);
        myPath2d.lineTo(5,0);
        myPath2d.lineTo(5,5);
        myPath2d.lineTo(0,5);
        myPath2d.lineTo(0,0);
        return myPath2d;
    }
    public void moveBullet() {
        double angleRad = Math.toRadians(angle);
        x += Math.cos(angleRad) * velocity;
        y += Math.sin(angleRad) * velocity;
    }
    public double getBulletX() {
        return x;
    }
    public double getBulletY() {
        return y;
    }
    public void checkBulletBounds() {
        if (y < -20) {
            y+=610;
        }
        if (y > 600) {
            y -= 620;
        }
        if (x < -20) {
            x += 910;
        }
        if (x > 900) {
            x -= 920;
        }
    }

    public boolean shouldBulletDisappear() {
        if (bulletTime < 100) {
            bulletTime++;
            return false;
        } else {
            return true;
        }
    }

    public double getAngle() {
        return angle;
    }

    //Draw hit box
    public void drawMe(Graphics2D g) {
        AffineTransform transform = g.getTransform();
        g.translate(x,y);
        AffineTransform affineTransform = new AffineTransform();
        g.setColor(Color.red);
        g.draw(bulletHitbox);
        Shape shape = getBulletHitbox();
        g.setTransform(transform);

//        g.setColor(Color.red);
//        g.draw(shape);
    }
    public Area getBulletHitbox() {
        AffineTransform transform = new AffineTransform();
        transform.translate(x,y);
        return new Area(transform.createTransformedShape(bulletHitbox));
    }
}