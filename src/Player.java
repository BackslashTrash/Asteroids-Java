import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;


public class Player extends JPanel implements Runnable {
    public static final double playerSizeX = (double) 94 / 4;
    public static final double playerSizeY = (double) 105 / 4;
    private double x = 0;
    private double y = 0;

    private int lives = 3;
    private int rounds;
    private int spawnAmount;
    private int score;

    private static boolean gameOver;
    private boolean dead;
    private boolean mute;

    private long TimeStartRound;

    private boolean canShoot;
    private boolean canRespawn;
    private int shootCounter = 0;
    private int liveCounter = 0;
    private double vx = 0;
    private double vy = 0;
    private final double acceleration = 1.8;
    private final double friction = 0.995;

    private final Font myFont = new Font("Futura", Font.PLAIN,27);

    private ArrayList<Bullets> UFOBullets = new ArrayList<>();
    private ArrayList<Bullets> playerBullets = new ArrayList<>();
    private ArrayList<Asteroids> asteroidsList = new ArrayList<>();

    private double angle = 270;
    private final Image player = new ImageIcon(getClass().getResource("Player.png")).getImage();
    private final Image scaledPlayer = player.getScaledInstance(94 / 2, 105 / 2, Image.SCALE_SMOOTH);

    
    MouseHandler mouse = new MouseHandler();
    KeyHandler keys = new KeyHandler();
    Thread gameThread1;

    public Player() {
        this.setPreferredSize(new Dimension(900, 600));
        this.setBackground(Color.LIGHT_GRAY);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        this.addMouseListener(mouse);
        this.addKeyListener(keys);
        setPlayerLocation(435, 290);
    }

    public void startThread() {
        rounds = 0;
        gameThread1 = new Thread(this);
        gameThread1.start();
    }

    @Override
    public void run() {
        double interval = (double) 1000000000 / 120;
        double nanoTime = System.nanoTime() + interval;
        while (gameThread1 != null) {
            update();
            repaint();
            try {
                double remain = nanoTime - System.nanoTime();
                remain = remain / 1000000;
                if (remain < 0) {
                    remain = 0;
                }
                Thread.sleep((long) remain);
                nanoTime += interval;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void update() {
        if (!keys.pause) {
            if (!dead && !gameOver) {
                if (keys.up) {
                    accelerate();
                } else {
                    slowDown();
                }
                if (keys.right) {
                    angle += 2.5;
                }
                if (keys.left) {
                    angle -= 2.5;
                }
                if (keys.shoot) {
                    if (shootCounter == 0 && canShoot) {
                        shoot();
                        keys.shoot = false;
                        canShoot = false;
                    }
                }
                detectPlayerCollision();
            }
            if (dead) {
                if (canRespawn) {
                    stopPlayer();
                    setPlayerLocation(435, 290);
                    setAngle(270);
                    dead = false;
                }
            }
            startRound();
            spawnUFO();
            updateLives();
            updateAsteroids();
            checkBulletState();
            updateBullets();
            moveShipOnScreen();
            checkPlayerBounds();
            checkHighScore();
            detectGameOver();
            startGame();
            checkRespawn();
            setAngle(angle);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;
        AffineTransform refresh = g2D.getTransform();
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (!dead && !gameOver) {
            g2D.translate(x, y);
            AffineTransform myTransform = new AffineTransform();
            myTransform.rotate(Math.toRadians(angle + 90), playerSizeX, playerSizeY);
            g2D.drawImage(scaledPlayer, myTransform, null);
            g2D.setTransform(refresh);
        }
        showDeathScreen(g2D);
        String scoreText= "Score: "+ score;
        String liveText = "" + lives;
        g2D.setFont(myFont);
        g2D.drawString(scoreText,20,40);
        g2D.drawString(liveText,20,65);
        g2D.setColor(Color.red);
        g2D.draw(getPlayerHitbox());
        g2D.draw(getSpawnArea());

        for (int i = 0; i < playerBullets.size(); i++) {
            Bullets b = playerBullets.get(i);
            g2D.fill(new Ellipse2D.Double(b.getBulletX(), b.getBulletY(), 5, 5)  );
            //b.drawMe(g2D);
        }
        for (int i = 0; i< asteroidsList.size(); i++) {
            Asteroids a = asteroidsList.get(i);
            a.drawMe(g2D);
        }

    }

    private void setAngle(double angle) {
        this.angle = angle % 360;
    }

    private void accelerate() {
        double angleRad = Math.toRadians(angle);
        vx += Math.cos(angleRad) * acceleration / 120;
        vy += Math.sin(angleRad) * acceleration / 120;
    }

    private void setPlayerLocation(int LocationX, int LocationY) {
        x = LocationX;
        y = LocationY;
    }

    private void checkPlayerBounds() {
        if (y < -20) {
            y += 610;
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

    private void moveShipOnScreen() {
        x += vx;
        y += vy;
    }

    private void slowDown() {
        vx *= friction;
        vy *= friction;
    }

    private void shoot() {
        playerBullets.add(new Bullets(x, y, angle));
    }

    private void updateBullets() {
        for (int i = 0; i < playerBullets.size(); i++) {
            Bullets bullets = playerBullets.get(i);
            bullets.moveBullet();
            bullets.checkBulletBounds();
            if (bullets.shouldBulletDisappear()) {
                playerBullets.remove(bullets);
            }
        }
    }

    private void checkBulletState() {
        if (!canShoot) {
            shootCounter++;
        }
        if (shootCounter == 26) {
            shootCounter = 0;
            canShoot = true;
        }
    }

    private void updateAsteroids() {
        for (int i = 0; i < asteroidsList.size(); i++) {
            Asteroids asteroids = asteroidsList.get(i);
            asteroids.move();
            asteroids.checkAsteroidBounds();
            checkBulletCollision(asteroids);
        }
    }

    private void spawnAsteroids() {
        Random myRandom = new Random();
        int x = myRandom.nextInt(900);
        int angle = myRandom.nextInt(360);
        double speed = myRandom.nextDouble();
        asteroidsList.add(new Asteroids(0,x,20,angle, speed));
    }

    private void startRound() {
        if (asteroidsList.isEmpty()) {
            TimeStartRound = System.currentTimeMillis();
            rounds++;
            liveCounter++;
            spawnAmount = 5 + rounds/3;
            for (int i = 1; i <= spawnAmount; i++) {
                spawnAsteroids();
            }
        }
    }

    public void checkBulletCollision(Asteroids asteroids) {
        for (int i = 0; i < playerBullets.size(); i++) {
            Bullets bullets = playerBullets.get(i);
            Area bulletArea = bullets.getBulletHitbox();
            Area asteroidArea = asteroids.getHitBox();
            bulletArea.intersect(asteroidArea);
            if (!bulletArea.isEmpty()) {
                if (asteroids.getType() == 0) {
                    splitAsteroids(1,asteroids.getAsteroidX(),asteroids.getAsteroidY());
                    asteroids.setType(1);
                    score+=20;
                } else if (asteroids.getType() == 1) {
                    splitAsteroids(2,asteroids.getAsteroidX(),asteroids.getAsteroidY());
                    asteroids.setType(2);
                    score+=50;
                } else if (asteroids.getType() == 2) {
                    asteroidsList.remove(asteroids);
                    score+=100;
                } else if (asteroids.getType() == 3) {
                    asteroidsList.remove(asteroids);
                    score+=200;
                } else if (asteroids.getType() == 4) {
                    asteroidsList.remove(asteroids);
                    score+=1000;
                }

                asteroidsList.remove(asteroids);
                playerBullets.remove(bullets);
            }
        }
    }

    private void splitAsteroids(int type, double x, double y) {
        Random myRandom = new Random();
        double speed = myRandom.nextDouble() + 0.4;
        int angle = myRandom.nextInt(360);
        asteroidsList.add(new Asteroids(type,x,y,angle, speed));
        speed = myRandom.nextDouble() + 0.4;
        angle = myRandom.nextInt(360);
        asteroidsList.add(new Asteroids(type,x,y,angle, speed));
    }

    private void spawnUFO() {               //3 = big, 4 = small
        long timeNow = System.currentTimeMillis();
        long ElapsedTime = timeNow - TimeStartRound;
        boolean big;
        boolean left;
        int type;
        double angle;
        int spawnX;
        if (ElapsedTime > 20000) {
            TimeStartRound = System.currentTimeMillis();
            Random random = new Random();
            big = random.nextBoolean();
            left = random.nextBoolean();
            spawnX = random.nextInt(500);
            if (big) {
                type = 3;
            } else {
                type = 4;
            }
            if (left) {
                angle = 180;
            } else {
                angle = 0;
            }
            asteroidsList.add(new Asteroids(type, spawnX+20, 0, angle, 1));
        }
    }

    private Path2D getPlayerPath2D() {
        Path2D myPath2D = new Path2D.Double();
        myPath2D.moveTo(23,0);
        myPath2D.lineTo(0,51.2);
        myPath2D.lineTo(22,41);
        myPath2D.lineTo(46,51.2);
        return myPath2D;
    }

    private Area getPlayerHitbox() {
        Area hitBox = new Area(getPlayerPath2D());
        AffineTransform transform = new AffineTransform();
        transform.translate(x,y);
        transform.rotate(Math.toRadians(angle+90),playerSizeX,playerSizeY);
        return new Area(transform.createTransformedShape(hitBox));
    }

    private void updateLives() {
        if (liveCounter == 3) {
            lives++;
            liveCounter = 0;
        }
    }

    private void checkHighScore(){
        try {
            FileReader fr = new FileReader("highscore.txt");
            BufferedReader br = new BufferedReader(fr);
            String temp = br.readLine();
            br.close();
            int storedHighScore = Integer.parseInt(temp);
            FileWriter fw = new FileWriter("highscore.txt");
            PrintWriter pw = new PrintWriter(fw);
            if (score > storedHighScore) {
                storedHighScore = score;
            }
            pw.print(storedHighScore);
            pw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void detectPlayerCollision() {
        for (int i = 0; i< asteroidsList.size(); i++) {
            Asteroids a = asteroidsList.get(i);
            Area playerHitbox = new Area(getPlayerHitbox());
            Area asteroidHitbox = new Area(a.getHitBox());
            asteroidHitbox.intersect(playerHitbox);
            if (!asteroidHitbox.isEmpty()) {
                lives--;
                dead = true;
            }
        }
    }

    private void stopPlayer() {
        vx = 0;
        vy = 0;
    }

    private Path2D getSpawnPath2D() {
        Path2D spawnPath2D = new Path2D.Double();
        spawnPath2D.moveTo(428,285);
        spawnPath2D.lineTo(487,285);
        spawnPath2D.lineTo(487, 350);
        spawnPath2D.lineTo(428,350);
        return spawnPath2D;
    }

    private Area getSpawnArea() {
        AffineTransform transform = new AffineTransform();
        transform.translate(0,0);
        return new Area(transform.createTransformedShape(getSpawnPath2D()));
    }

    private void checkRespawn() {
        for(int i = 0; i < asteroidsList.size(); i++) {
            Asteroids a = asteroidsList.get(i);
            Area asteroidHitbox = new Area(a.getHitBox());
            Area spawnRegion = new Area(getSpawnArea());
            asteroidHitbox.intersect(spawnRegion);
            if (!asteroidHitbox.isEmpty()) {
                canRespawn = false;
            } else {
                canRespawn =true;
            }
        }
    }

    private void playSound(String filename) {

    }

    private void detectGameOver() {
        if (lives > 0) {
            gameOver = false;
        } else {
            gameOver = true;
        }
    }

    private void showDeathScreen(Graphics g) {
        if (gameOver) {
            String playAgain = "Play Again";
            Graphics2D g2 = (Graphics2D) g;
            g2.setFont(myFont);
            g2.drawString(playAgain, 400, 290);
        }
    }

    public static boolean isGameOver() {
        return gameOver;
    }

    private void startGame() {
        if (mouse.respawnClicked) {
            rounds = 0;
            liveCounter = 0;
            score = 0;
            lives = 3;
            asteroidsList.clear();
            playerBullets.clear();
            dead = false;
            gameOver = false;
            mouse.respawnClicked = false;
            setPlayerLocation(435, 290);
            setAngle(270);

        }
    }
}