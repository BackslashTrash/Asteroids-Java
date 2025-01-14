import javax.sound.sampled.*;
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
    public static final double playerSizeX = (double) 94 / 4;           //scaled player x size
    public static final double playerSizeY = (double) 105 / 4;          //scaled player y size
    private double x = 0;                                                                   //player location
    private double y = 0;                                                                   //player y
    private int lives= 9999;                               //player lives
    private int rounds;                                 //rounds
    private int spawnAmount;                     //controls the amount of asteroids that will spawn
    private int score;                                   //the sore
    private static boolean gameRunning = false;          //false = menu, true = in-game
    private static boolean gameOver;                        //checks if game is over(lives = 0)
    private boolean dead;                                           //checks if player is dead (gets hit by asteroid)
    private static boolean mute =  false;       //
    private long TimeStartRound;
    private boolean canShoot;       //indicate if the player can shoot or not

    private double bx = Player.playerSizeX / 2 + 5;
    private double by = Player.playerSizeY / 2 + 10;

    public static int shooter;

    private BackgroundMusic backgroundMusic = new BackgroundMusic();
    private int shootCounter = 0;
    private int liveCounter = 0;    //to calculate when the player will recieve extra lives
    private double vx = 0;              //change in x
    private double vy = 0;              //change in y
    private final double acceleration = 1.8;
    private final double friction = 0.995;
    private final Font myFont = new Font("Futura", Font.PLAIN,27);

    private ArrayList<Bullets> UFOBullets = new ArrayList<>();
    private ArrayList<Bullets> playerBullets = new ArrayList<>();           //player's bullets
    private ArrayList<Asteroids> asteroidsList = new ArrayList<>();       //asteroids

    private double angle = 270;         //angle the player is facing
    private final Image player = new ImageIcon(getClass().getResource("Player.png")).getImage();        //player image
    private final Image scaledPlayer = player.getScaledInstance(94 / 2, 105 / 2, Image.SCALE_SMOOTH);       //scaled player image

    File laserSound = new File("laser.wav");

    MouseHandler mouse = new MouseHandler();
    KeyHandler keys = new KeyHandler();
    Thread gameThread1;

    public Player() {                       // Init player by setting dimension of the screen, location, adding key listener and mouse listener
        this.setPreferredSize(new Dimension(900, 600));
        this.setBackground(Color.LIGHT_GRAY);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        this.addMouseListener(mouse);
        this.addKeyListener(keys);
        setPlayerLocation(435, 290);
    }

    public void startThread() {
        rounds = 0;                             //Reset rounds
        gameThread1 = new Thread(this);     //Add thread
        gameThread1.start();                         //Start game thread
        backgroundMusic.startSoundThread(); //Start sound thread
    }

    @Override
    public void run() {
        double interval = (double) 1000000000 / 120;            //interval, divide by 120 because 120 frames per second
        double nanoTime = System.nanoTime() + interval;
        while (gameThread1 != null) {
            try {
                update();
            } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
                throw new RuntimeException(e);
            }
            repaint();
            try {
                double remain = nanoTime - System.nanoTime(); //calculate interval between each update
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

    public void update() throws UnsupportedAudioFileException, LineUnavailableException, IOException {
        if (gameRunning) {
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
                            if (!mute) {
                            initFile(laserSound);
                            }
                            shoot();
                            keys.shoot = false;
                            canShoot = false;
                        }
                    }
                    detectPlayerCollision();
                }
                startRound();
                spawnUFO();
                updateLives();
                updateAsteroids();
                checkBulletState();
                updateBullets(playerBullets);
                updateBullets(UFOBullets);
                detectUFOBullets();
                UFOShoot();
                moveShipOnScreen();
                checkPlayerBounds();
                checkHighScore();
                detectGameOver();
                checkAsteroidListForSpawn();
                respawn();
                setAngle(angle);
            }
        }
        startGame();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;
        AffineTransform refresh = g2D.getTransform();
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (gameRunning) {  //draw the game if game is running
            if (!dead && !gameOver) {       //for player movement
                g2D.translate(x, y);              //move player to x and y variables
                AffineTransform myTransform = new AffineTransform();
                myTransform.rotate(Math.toRadians(angle + 90), playerSizeX, playerSizeY);       //rotate player to angle + 90 because it starts sideways and I want it to be upright
                g2D.drawImage(scaledPlayer, myTransform, null);
                g2D.setTransform(refresh);
            }
            if (mouse.menuClicked) {                //if player clicked menu after they lose the game
                gameRunning = false;
                mouse.menuClicked = false;
                showMenu(g2D);
            }
            showDeathScreen(g2D);
            String scoreText = "Score: " + score;
            String liveText = "Lives: " + lives;
            g2D.setFont(myFont);
            g2D.drawString(scoreText, 20, 40);
            g2D.drawString(liveText, 20, 65);
            //g2D.setColor(Color.black);
            //g2D.draw(getPlayerHitbox());
            //g2D.draw(getSpawnArea());

            for (int i = 0; i < playerBullets.size(); i++) {
                Bullets b = playerBullets.get(i);
                g2D.fill(new Ellipse2D.Double(b.getBulletX(), b.getBulletY(), 5, 5));
                b.drawMe(g2D);
            }

            for (int i = 0; i < asteroidsList.size(); i++) {
                Asteroids a = asteroidsList.get(i);
                a.drawMe(g2D);
            }
            for (int i = 0; i < UFOBullets.size(); i++) {
                Bullets b = UFOBullets.get(i);
                g2D.fill(new Ellipse2D.Double(b.getBulletX(), b.getBulletY(), 5, 5));
                b.drawMe(g2D);
            }

            g2D.dispose();
        }
        if (!gameRunning) {     //show menu if not in game
            showMenu(g2D);
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

    private void checkPlayerBounds() {      //checks bounds,
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
        shooter = 0;
        playerBullets.add(new Bullets(x, y, angle));
    }

    private void updateBullets(ArrayList<Bullets> arrayList) {
        for (int i = 0; i < arrayList.size(); i++) {
            Bullets bullets = arrayList.get(i);
            bullets.moveBullet();
            bullets.checkBulletBounds();
            if (bullets.shouldBulletDisappear()) {
                arrayList.remove(bullets);
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

    private void checkBulletCollision(Asteroids asteroids) {
        for (int i = 0; i < playerBullets.size(); i++) {
            Bullets bullets = playerBullets.get(i);
            if (collide(bullets.getBulletHitbox(),asteroids.getHitBox())) {
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
        int spawnY;
        if (ElapsedTime > 20000) {
            TimeStartRound = System.currentTimeMillis();
            Random random = new Random();
            big = random.nextBoolean();
            left = random.nextBoolean();
            spawnY = random.nextInt(500);
            if (left) {
                angle = 180;
            } else {
                angle = 0;
            }
            if (big) {
                type = 3;
                angle = 0;
            } else {
                type = 4;
            }
            asteroidsList.add(new Asteroids(type, 0, spawnY + 20, angle, 1));
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
            int storedHighscore = Integer.parseInt(temp);
            FileWriter fw = new FileWriter("highscore.txt");
            PrintWriter pw = new PrintWriter(fw);
            if (score > storedHighscore) {
                storedHighscore = score;
            }
            pw.print(storedHighscore);
            pw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void detectPlayerCollision() {
        for (int i = 0; i< asteroidsList.size(); i++) {
            Asteroids a = asteroidsList.get(i);
            if (collide(getPlayerHitbox(),a.getHitBox())) {
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

    private void detectGameOver() {
        if (lives > 0) {
            gameOver = false;
        } else {
            gameOver = true;
        }
    }

    private void showDeathScreen(Graphics g) {
        if (gameOver) {
            String playAgain = "New game";
            String menu = "Menu";
            Graphics2D g2 = (Graphics2D) g;
            g2.setFont(myFont);
            g2.drawString(playAgain, 400, 290);
            g2.drawString(menu,430,430);
            g2.draw(getArea(getStartButton()));
            g2.draw(getArea(getExitButton()));

        }
    }

    public static boolean isGameOver() {
        return gameOver;
    }

    public static boolean isGameRunning() {
        return gameRunning;
    }

    public static void setGameRunning(boolean mybool) {
        gameRunning = mybool;
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

    private void respawn() {
        if (dead) {
            stopPlayer();
            if (isSpawnEmpty(asteroidsList)) {
                setPlayerLocation(435, 290);
                setAngle(270);
                dead = false;
            }
        }
    }

    private void showMenu(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;
        String startGameText = "Play game";
        String title = "Asteroids";
        String soundOn = "Sound: On";
        String soundOff = "Sound: Off";
        String exit = "Quit Game";
        g2D.setFont(myFont);
        g2D.drawString(startGameText,400,290);
        g2D.drawString("High score:"+readHighscore(),360,360);
        g2D.drawString(exit,400,430);
        if (!mute) {
            g2D.drawString(soundOn,400,200);
        } else {
            g2D.drawString(soundOff,400,200);
        }
        if (mouse.exitClicked) {
            System.exit(0);
        }
        Font titleFont = new Font("Ariel",Font.PLAIN,45);
        g2D.setFont(titleFont);
        g2D.drawString(title,370,100);
        g2D.draw(getArea(getMuteButton()));
        g2D.draw(getArea(getStartButton()));
        g2D.draw(getArea(getExitButton()));
    }

    private void checkAsteroidListForSpawn() {
        for(int i = 0; i < asteroidsList.size(); i++) {
            Asteroids a = asteroidsList.get(i);
            a.setCanPlayerRespawn(!(collide(a.getHitBox(),getSpawnArea())));
        }
    }

    private boolean isSpawnEmpty(ArrayList<Asteroids> arrayList) {
        for(int i = 0; i < arrayList.size(); i++) {
            Asteroids a = arrayList.get(i);
            if (!a.isCanPlayerRespawn()) {
                return false;
            }
        }
        return true;
    }

    private int readHighscore() {
        int myScore;
        try {
            FileReader fr = new FileReader("highscore.txt");
            BufferedReader br = new BufferedReader(fr);
            String temp = br.readLine();
            myScore = Integer.parseInt(temp);
            br.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return myScore;
    }

    private Path2D getMuteButton() {
        Path2D myPath2D = new Path2D.Double();
        myPath2D.moveTo(390,170);
        myPath2D.lineTo(540,170);
        myPath2D.lineTo(540,210);
        myPath2D.lineTo(390,210);
        return myPath2D;
    }

    private Path2D getStartButton() {
        Path2D myPath2D = new Path2D.Double();
        myPath2D.moveTo(390,260);
        myPath2D.lineTo(540,260);
        myPath2D.lineTo(540,300);
        myPath2D.lineTo(390,300);
        return myPath2D;
    }

    private Path2D getExitButton() {
        Path2D myPath2D = new Path2D.Double();
        myPath2D.moveTo(390,400);
        myPath2D.lineTo(540,400);
        myPath2D.lineTo(540,440);
        myPath2D.lineTo(390,440);
        return myPath2D;
    }

    private Area getArea(Path2D path2D) {
        return new Area(path2D);
    }

    public static void setMute(boolean mute) {
        Player.mute = mute;
    }

    public static boolean isMute() {
        return mute;
    }

    public void initFile(File file) {
        try {
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(inputStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    private void UFOShoot() {
        Random random = new Random();
        for (int i  = 0; i < asteroidsList.size();i++) {
            Asteroids a = asteroidsList.get(i);
            if (a.getType() == 3) {
                if (a.getUFOShootCounter() < 200) {
                    a.setUFOShootCounter(a.getUFOShootCounter() + 1);
                } else {
                    int angle = random.nextInt(360);
                    UFOBullets.add(new Bullets(a.getAsteroidX(), a.getAsteroidY(), angle));
                    a.setUFOShootCounter(0);
                }
            }
        }
    }

    private void detectUFOBullets() {
        for (int i = 0; i < UFOBullets.size(); i++) {
            Bullets b = UFOBullets.get(i);
            if (collide(b.getBulletHitbox(),getPlayerHitbox())) {
                UFOBullets.remove(b);
                lives--;
                dead = true;
            }
        }
    }

    private boolean collide(Area a, Area b) {
        a.intersect(b);
        return !a.isEmpty();
    }
}