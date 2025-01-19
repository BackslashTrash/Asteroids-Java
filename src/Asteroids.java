import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;

public class Asteroids {
    private int type;
    private double x;
    private double y;
    private final double angle;
    private final double speed;

    private boolean canPlayerRespawn;
    private int UFOShootCounter = 0;

//    private static final double AsteroidImageX = 236/3;
//    private static final double AsteroidImageY = 211/3;

    private final Image asteroidImage =  new ImageIcon(getClass().getResource("Asteroid.png")).getImage();
    private Image scaledImage = asteroidImage.getScaledInstance(236/3,211/3,Image.SCALE_SMOOTH);
    private final Image ufoImage =  new ImageIcon(getClass().getResource("UFO.png")).getImage();

    private Area hitBox;

    public Asteroids(int asteroidType, double asteroidX, double asteroidY, double asteroidAngle, double asteroidSpeed) {
        type = asteroidType;
        x = asteroidX;
        y = asteroidY;
        angle = asteroidAngle;
        speed = asteroidSpeed;
        Path2D asteroidPath2D = getAsteroidPath2D();
        Path2D ufoPath2D = getUFOPath2D();
        AffineTransform scale = new AffineTransform();
        if (type == 0) {                //BIG ASTEROID
            scaledImage = asteroidImage.getScaledInstance(236/3,211/3,Image.SCALE_SMOOTH);
            hitBox = new Area(asteroidPath2D);
        }
        if (type == 1) {                //MEDIUM ASTEROID
            scaledImage = asteroidImage.getScaledInstance(236/5,211/5,Image.SCALE_SMOOTH);
            scale.scale(0.6,0.6);
            hitBox = new Area(asteroidPath2D);
            hitBox.transform(scale);
        }
        if (type == 2) {                //SMALL ASTEROID
            scaledImage = asteroidImage.getScaledInstance(236/7,211/7,Image.SCALE_SMOOTH);
            scale.scale(0.43,0.4);
            hitBox = new Area(asteroidPath2D);
            hitBox.transform(scale);
        }
        if (type == 3) {        //BIG UFO
            scaledImage = ufoImage.getScaledInstance(200/4,114/4,Image.SCALE_SMOOTH);
            hitBox = new Area(ufoPath2D);
        }
        if (type == 4) {        //SMALL UFO
            scaledImage = ufoImage.getScaledInstance(200/5,114/5 ,Image.SCALE_SMOOTH);
            scale.scale(0.78,0.7);
            hitBox = new Area(ufoPath2D);
            hitBox.transform(scale);
        }
    }

    private Path2D getAsteroidPath2D() {
        Path2D myPath2d = new Path2D.Double();
        myPath2d.moveTo(0,37);
        myPath2d.lineTo(2,30);
        myPath2d.lineTo(11,25);
        myPath2d.lineTo(12,17);
        myPath2d.lineTo(30,6);
        myPath2d.lineTo(40,3);
        myPath2d.lineTo(59,4);
        myPath2d.lineTo(65,10);
        myPath2d.lineTo(65,16);
        myPath2d.lineTo(73,27.9);
        myPath2d.lineTo(70,37);
        myPath2d.lineTo(76,41);
        myPath2d.lineTo(68,60);
        myPath2d.lineTo(60,64);
        myPath2d.lineTo(57,69);
        myPath2d.lineTo(45,69);
        myPath2d.lineTo(40,66);
        myPath2d.lineTo(39,69);
        myPath2d.lineTo(26,69);
        myPath2d.lineTo(4,52);
        myPath2d.lineTo(10,43);
        myPath2d.lineTo(0,37);
        return myPath2d;
    }

    private Path2D getUFOPath2D() {
        Path2D myPath2D = new Path2D.Double();
        myPath2D.moveTo(1,19);
        myPath2D.lineTo(16,13);
        myPath2D.lineTo(17,5);
        myPath2D.lineTo(23,0);
        myPath2D.lineTo(29,0);
        myPath2D.lineTo(35,5);
        myPath2D.lineTo(36,13);
        myPath2D.lineTo(49,19);
        myPath2D.lineTo(36,27);
        myPath2D.lineTo(16,27);
        myPath2D.lineTo(1,19);
        return myPath2D;
    }

    public void drawMe(Graphics2D g) {
        AffineTransform transform = g.getTransform();
        g.translate(x,y);
        g.rotate(Math.toRadians(angle));
        AffineTransform affineTransform = new AffineTransform();
        g.drawImage(scaledImage,affineTransform,null);
//        Shape hitboxShape = getHitBox();
        g.setTransform(transform);
//        g.setColor(Color.red);
//        g.draw(hitboxShape);          //show hitbox
    }

    public void checkAsteroidBounds() {
        if (y < -15) {
            y+= 635;
        }
        if (y > 615) {
            y -= 635;
        }
        if (x < -15) {
            x += 935;
        }
        if (x > 915) {
            x -= 935;
        }
    }

    public void move() {
        double angleRad = Math.toRadians(angle);
        x += Math.cos(angleRad) * speed + 0.02;
        y += Math.sin(angleRad) * speed + 0.02;
    }

    public double getAsteroidY() {
        return y;
    }

    public double getAsteroidX() {
        return x;
    }
    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public Area getHitBox() {
        AffineTransform transform = new AffineTransform();
        transform.translate(x,y);           //move the hitbox to x and y variable
        transform.rotate(Math.toRadians(angle));       //change direction
        return new Area(transform.createTransformedShape(hitBox));
    }

    public boolean isCanPlayerRespawn() {
        return canPlayerRespawn;
    }

    public void setCanPlayerRespawn(boolean canPlayerRespawn) {
        this.canPlayerRespawn = canPlayerRespawn;
    }

    public void setUFOShootCounter(int UFOShootCounter) {
        this.UFOShootCounter = UFOShootCounter;
    }

    public int getUFOShootCounter() {
        return UFOShootCounter;
    }

    public double getAngle() {
        return angle;
    }
}
































































/*
"What is the key to success?"
"CONSISTENCY"
*/