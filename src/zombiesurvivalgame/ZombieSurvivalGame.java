/*
Savannah Bananas
4/28/2020
This is the class that will contain all of the graphics, and will be added to the main class on startup
*/

package zombiesurvivalgame;

import entities.friendlyEntities.Player;
import entities.monsters.Monster;
import entities.monsters.Zombie;
import misc.*;
import weapons.Pistol;
import weapons.projectiles.Projectile;
import misc.items.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ZombieSurvivalGame extends JPanel implements ActionListener {

    private int time; //counts the number of game ticks since the start of the wave
    public Player player = new Player(500, 500, 100.0, 100.0); //This is the player that the user can control.
    private int waveNumber; //Assuming we use the wave system, this will hold the wave number.
    public Monster[] monsters = new Monster[0]; //List of all monsters on screen
    private Projectile[] bullets = new Projectile[0];
    private boolean debugOn = true;
    public int seconds;
    public Tree[] trees = new Tree[4];
    public int kills;
    public int monsterCount;
    public boolean isGameOver = false;
    //Timer clock; //no longer used, infinite while is used instead.
    private HealthKit kit;
    private Armor armor;
    private int z = 1;

    private JFrame window;
    private StartScreen startScreen;
    private GameOverScreen gameOverScreen;
    private HelpScreen helpScreen;
    private int screen; //the screen that should be displayed, e.g. start screen, game screen, pause screen, etc

    //constants for picking a screen
    public static final int START_SCREEN = 0;
    public static final int GAME_SCREEN = 1;
    public static final int PAUSE_SCREEN = 2;
    public static final int NEXT_WAVE_SCREEN = 3;
    public static final int HOW_TO_PLAY_SCREEN = 4;
    public static final int GAME_OVER_SCREEN = 5;


    //Default constructor
    public ZombieSurvivalGame() {
        startScreen = new StartScreen(this);
        gameOverScreen = new GameOverScreen(this);
        helpScreen = new HelpScreen(this);
        this.screen = START_SCREEN;

        time = 0;
        //Clock inserts a delay between redrawing each frame. For testing, the delay can be modified.
        //The delay is in milliseconds. If you set delay to 20, that's 50 fps.
        //clock = new Timer(20, this);
        //clock.start();

        //sets up the window
        JFrame w = new JFrame("ZombieSurvival - by the SavannahBananas");
        this.window = w;

        w.addMouseListener(startScreen);
        w.addMouseMotionListener(startScreen);

        w.setBounds(200, 0, 1000, 1000);
        w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container c = w.getContentPane();
        c.add(this);
        w.setVisible(true);
        w.setResizable(false);

        long timer = System.currentTimeMillis();
        while(true) {
            if(System.currentTimeMillis() - timer > 20) {
                timer += 20;
                time++;
                repaint();
            }
        }

    }

    // start a game. Once we have a "restart" or "Try again" or something, this will be called to restart the game
    // (without restarting the program)
    public void startNewGame() {
        Zombie.setZombieImage();

        time = 0;
        isGameOver = false;
        player.setX(500.0);
        player.setY(500.0);
        player.setMaxHealth(100.0);
        player.setHealth(100.0);
        player.setGun(new Pistol((int) player.getX(), (int) player.getY(), this));
        monsterCount = 2;

        seconds = 0;
        kills = 0;
        monsterCount = 0;
        waveNumber = 0;
        kit = new HealthKit(250, 250);
        armor = new Armor(750, 750);
        kit.hide();
        armor.hide();
        monsters = new Monster[0];
        bullets = new Projectile[0];
        //trees = new Tree[0];

        //appendTree(new Tree((int)Math.random()*900, (int)Math.random()*900));
    }

    // starts a new wave.  Will be called when all zombies are dead not functional yet)
    public void nextWave() {
        //int placeholderNumberOfZombies = 5;
        monsterCount += 2;
        waveNumber++;
        monsters = new Monster[0];

        //spawns lots of zombies
        for(int i = 0; i < monsterCount; i++) {

            //makes sure that a zombie can't spawn too close to the player
            int zombieX = 0;
            int zombieY = 0;
            boolean isValidLocation = false;
            while(isValidLocation == false) {
                //creates new random spawn location
                zombieX = (int) (Math.random() * 1400) - 200;
                zombieY = (int) (Math.random() * 1400) - 200;
                //uses pythagorean theorem to check distance from spawn location to player
                double changeX = zombieX - player.getX();
                double changeY = zombieY - player.getY();
                double distance = Math.sqrt(changeX*changeX + changeY*changeY);
                //if more than 300 pixels away, it's a valid location
                if(distance >= 300) {
                    isValidLocation = true;
                }
            }

            //increases base damage every other round
            int baseDamageIncrease = waveNumber/5;

            monsters = addMonster(monsters, new Zombie(zombieX, zombieY, 500, 500, baseDamageIncrease, this));
        }

        if(waveNumber%3 == 0) { //spawns a health kit and armor pack every third wave, for balancing purposes
            if (kit.pickedUp == true) {
                kit.unHide();
                kit.setX((int)(Math.random() * 850));
                kit.setY((int)(Math.random() * 850));
            }

            if (armor.pickedUp == true) {
                armor.unHide();
                armor.setX((int)(Math.random() * 850));
                armor.setY((int)(Math.random() * 850));
            }
        }

    }

    // This draws the scene every frame.
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Color background = new Color(60, 179, 113);
        setBackground(background);


        this.drawNextFrame(g);

    }

    //This event gets activated by the Clock, and repaints the screen.
    public void actionPerformed(ActionEvent e) {

        if(isGameOver == false) {
            time++;
        }
        repaint();
    }





    //This is the main "repaint" method that will redraw every single frame
    private void drawNextFrame(Graphics g) {

        if(screen == START_SCREEN) {
            startScreen.draw(g);
        } else if(screen == GAME_SCREEN) {
            //moves onto next wave if all monsters are dead
            if(monsters.length == 0) {
                nextWave();
            }

            //draws health kit and armor
            drawItems(g);

            //draws all monsters
            drawMonsters(g);

            //draws all bullets
            drawBullets(g);

            //draws player
            drawPlayer(g);

            //draws the debug overlay, only if debugOn == true
            drawDebug(g);

            if (time % 50 == 0) {
                seconds++;
            }

            if (player.getHealth() <= 0) {
                changeScreen(GAME_OVER_SCREEN);
            }

            drawHUD(g);
        } else if(screen == GAME_OVER_SCREEN) { //if player is dead
            player.setHealth(0.0);

            gameOverScreen.draw(g);
        } else if(screen == HOW_TO_PLAY_SCREEN) {
            helpScreen.draw(g);
        }
    }




    public Player getPlayer() {
        return player;
    }

    //adds element to array
    public static Monster[] addMonster(Monster[] originalArray, Monster newMonster) {
        Monster[] newMonsterArray = new Monster[originalArray.length + 1];
        for(int i = 0; i < originalArray.length; i++) {
            newMonsterArray[i] = originalArray[i];
        }

        newMonsterArray[originalArray.length] = newMonster;
        return newMonsterArray;
    }

    public static Projectile[] addBullet(Projectile[] original, Projectile newBullet) {
        Projectile[] newArray = new Projectile[original.length + 1];
        for(int i = 0; i < original.length; i++) {
            newArray[i] = original[i];
        }
        newArray[original.length] = newBullet;
        return newArray;
    }

    public static Tree[] addTree(Tree[] original, Tree newTree) {
        Tree[] newArray = new Tree[original.length + 1];
        for(int i = 0; i < original.length; i++) {
            newArray[i] = original[i];
        }
        newArray[original.length] = newTree;
        return newArray;
    }




    //generic methods for the arrays
    public Projectile[] getBullets() {
        return bullets;
    }

    public void setBullets(Projectile[] p) {
        this.bullets = p;
    }

    public void appendBullet(Projectile p) {
        this.bullets = addBullet(bullets, p);
    }



    public Tree[] getTrees() {
        return trees;
    }

    public void setTrees(Tree[] t) {
        this.trees = t;
    }

    public void appendTree(Tree t) {
        this.trees = addTree(trees, t);
    }





    public void drawTrees(Graphics g) {
        for(int i = 0; i < trees.length; i++) {
            trees[i].draw(g);
        }
    }

    public void drawMonsters(Graphics g) {
        //checks if each monster is dead. If so, the monster is removed from monsters array.
        Monster[] newMonsters = new Monster[0];
        for(int i = 0; i < monsters.length; i++) {
            if(monsters[i].getHealth() > 0.0) {
                newMonsters = addMonster(newMonsters, monsters[i]);
            }
            else {
                kills++;
            }
        }
        monsters = newMonsters.clone();

        //draws all the monsters
        for(int i = 0; i < monsters.length; i++) {
            monsters[i].draw(g);
            if(debugOn == true) {
                monsters[i].drawHitbox(g);
            }
        }
        for(int i = 0; i < bullets.length; i++) {
            bullets[i].draw(g);
            if(debugOn == true) {
                bullets[i].drawHitbox(g);
            }
        }
    }

    public void drawBullets(Graphics g) {
        //checks if each bullet is dead. If so, the projectile is removed from the array.
        Projectile[] newProjectiles = new Projectile[0];
        for(int i = 0; i < bullets.length; i++) {
            if(bullets[i].getDespawned() == false) {
                newProjectiles = addBullet(newProjectiles, bullets[i]);
            }
        }
        bullets = newProjectiles.clone();
    }

    public void drawItems(Graphics g) {
        kit.draw(g);

        armor.draw(g);

        if(debugOn == true) {
            armor.drawHitbox(g);
            kit.drawHitbox(g);
        }

        if (player.isTouching(kit) && (kit.pickedUp == false)) {
            player.setHealth(player.getHealth() + 15);
            kit.hide();
        }

        if (player.isTouching(armor) && (armor.pickedUp == false)) {
            player.increaseArmorLevel();
            armor.hide();
        }
    }

    public void drawPlayer(Graphics g) {
        //Calls the draw method of the player
        player.draw(g);

        if(debugOn == true) {
            player.drawHitbox(g);
        }

        //reduces player health if it's touching a zombie
        for (int m = 0; m < monsters.length && time%50 == 0; m++) {
            if (player.isTouching(monsters[m])){
                double damage = monsters[m].getBaseDamage();
                int armorLevel = player.getArmorLevel();

                double healthDecrease = damage - armorLevel; //damage decreases by 1 per armor level

                if(healthDecrease < 0.0) {
                    healthDecrease = 0.0;
                }

                player.setHealth(player.getHealth() - healthDecrease);

            }
        }
    }

    public void drawDebug(Graphics g) {
        //debug overlay, more text can be added as needed
        if(debugOn == true) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Impact", Font.PLAIN, 15));
            g.drawString("Ticks = " + time, 50, 50);
            g.drawString("# of bullets = " + bullets.length, 50, 80);
            g.drawString("Player x = " + player.getX(), 50, 110);
            g.drawString("Player y = " + player.getY(), 50, 140);
            g.drawString("Player Health = " + player.getHealth(), 50, 170);
            g.drawString("Time Seconds = " + seconds, 50, 200);
            g.drawString("Kills = " + kills, 50, 230);
        }
    }

    public void drawHUD(Graphics g) {
        //heads up display at the bottom, displays on top of both game and game over
        g.setColor(Color.black);
        g.fillRect(0, 900, 1000, 100);

        g.setColor(Color.yellow);
        g.setFont(new Font("Impact", Font.PLAIN, 24));
        g.drawString("Time: " + seconds, 30, 940);
        g.drawString("Kills: " + kills, 130, 940);
        g.drawString("Monsters left:  " + monsters.length + "/" + monsterCount, 230,  940);
        g.drawString("Wave Number: " + waveNumber, 450, 940);
        g.drawString("Health: " + player.getHealth(), 630, 940);
        g.drawString("Armor Level: " + player.getArmorLevel(), 775, 940);
    }

    public void changeScreen(int newScreen) {
        //removes mouse and key listeners for current screen
        if(screen == START_SCREEN) {
            window.removeMouseListener(startScreen);
            window.removeMouseMotionListener(startScreen);
        } else if(screen == GAME_SCREEN) {
            window.removeKeyListener(player); //allows player movement
            window.removeMouseListener(player.getGun()); //allows gun shooting
            window.removeMouseMotionListener(player); //allows player rotation
        } else if(screen == GAME_OVER_SCREEN) {
            window.removeMouseMotionListener(gameOverScreen);
            window.removeMouseListener(gameOverScreen);
        } else if(screen == HOW_TO_PLAY_SCREEN) {
            window.removeMouseListener(helpScreen);
            window.removeMouseMotionListener(helpScreen);
        }

        //adds mouse and key listeners for new screen, and sets screen variable to be current screen
        if(newScreen == START_SCREEN) {
            screen = START_SCREEN;
            window.addMouseListener(startScreen);
            window.addMouseMotionListener(startScreen);
        } else if(newScreen == GAME_SCREEN) {
            screen = GAME_SCREEN;
            startNewGame();
            window.addKeyListener(player);
            window.addMouseListener(player.getGun());
            window.addMouseMotionListener(player);
        } else if(newScreen == GAME_OVER_SCREEN) {
            screen = GAME_OVER_SCREEN;
            window.addMouseMotionListener(gameOverScreen);
            window.addMouseListener(gameOverScreen);
        } else if(newScreen == HOW_TO_PLAY_SCREEN) {
            screen = HOW_TO_PLAY_SCREEN;
            window.addMouseListener(helpScreen);
            window.addMouseMotionListener(helpScreen);
        }
    }

    public void gameOver(Graphics g) {
        setBackground(Color.black);
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, 1000, 1000);
        g.setColor(Color.RED);
        g.setFont(new Font("Impact", Font.BOLD, 150));
        g.drawString("GAME OVER", 125, 400);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Impact", Font.BOLD, 40));
        g.drawString("You died on wave " + waveNumber + ", with " + kills + " kills.", 200, 525);
    }
}