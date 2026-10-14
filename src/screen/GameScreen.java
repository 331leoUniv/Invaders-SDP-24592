package screen;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;
import entity.item.ItemManager;
import entity.item.ShieldItem;
import engine.Cooldown;
import engine.Core;
import engine.GameSettings;
import engine.GameState;
import engine.ControlsConfig;
import entity.Bullet;
import entity.BulletPool;
import entity.EnemyShip;
import entity.EnemyShipFormation;
import entity.Entity;
import entity.Ship;
import audio.AudioManager;
import entity.item.Hyperfire;
import entity.item.OrbitalAnnihilator;
import entity.item.AnnihilatorItem;
import entity.item.TwinCanon;

/**
 * Implements the game screen, where the action happens.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 */
public class GameScreen extends Screen {

    /** Milliseconds until the screen accepts user input. */
    private static final int INPUT_DELAY = 6000;
    /** Bonus score for each life remaining at the end of the level. */
    private static final int LIFE_SCORE = 100;
    /** Minimum time between bonus ship's appearances. */
    private static final int BONUS_SHIP_INTERVAL = 15000;

    /** Maximum variance in the time between bonus ship's appearances. */
    private static final int BONUS_SHIP_VARIANCE = 10000;
    /** Time until bonus ship explosion disappears. */
    private static final int BONUS_SHIP_EXPLOSION = 500;
    /** Time from finishing the level to screen change. */
    private static final int SCREEN_CHANGE_INTERVAL = 1500;
    /** Height of the interface separation line. */
    private static final int SEPARATION_LINE_HEIGHT = 40;
    /** Time the space key must be held to trigger the Annihilator. */
    private static final int ANNIHILATOR_HOLD_TIME = 1000;

    private ItemManager itemManager;
    private boolean shieldDroppedThisLevel = false;
    private boolean chameleonDroppedThisLevel = false;
    private boolean hyperfireDroppedThisLevel = false;
    /** Chance that a destroyed enemy drops the Orbital Annihilator. */
    private static final double ANNIHILATOR_DROP_CHANCE = 0.05;
    private boolean annihilatorDroppedThisLevel = false;
    private boolean hyperfireKeyPressed = false;
    private int shipsDestroyedAtStart;
    private boolean twinCanonSpawned;
    private TwinCanon twinCanonItem;

    /** Current game difficulty settings. */
    private GameSettings gameSettings;
    /** Current difficulty level number. */
    private int level;
    /** Formation of enemy ships. */
    private EnemyShipFormation enemyShipFormation;
    /** Player's ship. */
    private Ship ship;
    /** Bonus enemy ship that appears sometimes. */
    private EnemyShip enemyShipSpecial;
    /** Minimum time between bonus ship appearances. */
    private Cooldown enemyShipSpecialCooldown;
    /** Time until bonus ship explosion disappears. */
    private Cooldown enemyShipSpecialExplosionCooldown;
    /** Time from finishing the level to screen change. */
    private Cooldown screenFinishedCooldown;
    /** Set of all bullets fired by on screen ships. */
    private Set<Bullet> bullets;
    /** Current score. */
    private int score;
    /** Player lives left. */
    private int lives;
    /** Total bullets shot by the player. */
    private int bulletsShot;
    /** Total ships destroyed by the player. */
    private int shipsDestroyed;
    /** Moment the game starts. */
    private long gameStartTime;
    /** Checks if the stage was cleared. */
    private boolean stageCleared;
    /** Checks if the level is finished. */
    private boolean levelFinished;
    /** Checks if a bonus life is received. */
    private boolean bonusLife;
    /** Orbital Annihilator (Doomsday Laser), only available on level 7. */
    private OrbitalAnnihilator annihilator;
    /** Moment the space key started being held, 0 if not held. */
    private long spaceHeldSince;
    /** Last countdown number that played a sound. */
    private int lastCountdown = -1;

    /**
     * Constructor, establishes the properties of the screen.
     */
    public GameScreen(final GameState gameState,
                      final GameSettings gameSettings, final boolean bonusLife,
                      final int width, final int height, final int fps) {
        super(width, height, fps);

        this.gameSettings = gameSettings;
        this.bonusLife = bonusLife;
        this.level = gameState.getLevel();
        this.score = gameState.getScore();
        this.lives = gameState.getLivesRemaining();
        if (this.bonusLife)
            this.lives++;
        this.bulletsShot = gameState.getBulletsShot();
        this.shipsDestroyed = gameState.getShipsDestroyed();
    }

    /**
     * Initializes basic screen properties, and adds necessary elements.
     */
    public final void initialize() {
        super.initialize();

        enemyShipFormation = new EnemyShipFormation(this.gameSettings);
        enemyShipFormation.attach(this);
        this.ship = new Ship(this.width / 2, this.height - 30);
        this.itemManager = new ItemManager(this.height);
        this.shieldDroppedThisLevel = false;
        this.chameleonDroppedThisLevel = false;
        this.hyperfireDroppedThisLevel = false;
        this.annihilatorDroppedThisLevel = false;

        this.hyperfireKeyPressed = false;
        this.shipsDestroyedAtStart = this.shipsDestroyed;
        this.twinCanonSpawned = false;
        this.twinCanonItem = null;
        // Appears each 10-30 seconds.
        this.enemyShipSpecialCooldown = Core.getVariableCooldown(
                BONUS_SHIP_INTERVAL, BONUS_SHIP_VARIANCE);
        this.enemyShipSpecialCooldown.reset();
        this.enemyShipSpecialExplosionCooldown = Core
                .getCooldown(BONUS_SHIP_EXPLOSION);
        this.screenFinishedCooldown = Core.getCooldown(SCREEN_CHANGE_INTERVAL);
        this.bullets = new HashSet<Bullet>();
        this.annihilator = new OrbitalAnnihilator();

        // Special input delay / countdown.
        this.gameStartTime = System.currentTimeMillis();
        this.inputDelay = Core.getCooldown(INPUT_DELAY);
        this.inputDelay.reset();
    }

    /**
     * Starts the action.
     */
    public final int run() {
        super.run();

        // Life bonus is only awarded when the stage was cleared.
        if (this.stageCleared)
            this.score += LIFE_SCORE * (this.lives - 1);
        this.logger.info("Screen cleared with a score of " + this.score);

        return this.returnCode;
    }

    /**
     * Updates the elements on screen and checks for events.
     */
    protected final void update() {
        super.update();

        if (this.inputDelay.checkFinished() && !this.levelFinished) {

            if (!this.ship.isDestroyed()) {
                boolean moveRight = inputManager.isKeyDown(
                        ControlsConfig.getKey(ControlsConfig.RIGHT));
                boolean moveLeft = inputManager.isKeyDown(
                        ControlsConfig.getKey(ControlsConfig.LEFT));

                boolean isRightBorder = this.ship.getPositionX()
                        + this.ship.getWidth() + this.ship.getSpeed() > this.width - 1;
                boolean isLeftBorder = this.ship.getPositionX()
                        - this.ship.getSpeed() < 1;

                if (moveRight && !isRightBorder) {
                    this.ship.moveRight();
                }
                if (moveLeft && !isLeftBorder) {
                    this.ship.moveLeft();
                }

                // Normal shooting using configured controls.
                if (inputManager.isKeyDown(
                        ControlsConfig.getKey(ControlsConfig.FIRE))) {
                    if (!this.annihilator.isCharging()) {
                        if (this.ship.shoot(this.bullets)) {
                            this.bulletsShot++;
                        }
                    }
                }

                boolean hyperfireKeyDown = inputManager.isKeyDown(
                        ControlsConfig.getKey(ControlsConfig.BUFF));

                if (hyperfireKeyDown && !this.hyperfireKeyPressed) {
                    this.itemManager.getBuffManager().addBuff(
                            new Hyperfire(0, 0), this.ship);
                }

                this.hyperfireKeyPressed = hyperfireKeyDown;

                // Orbital Annihilator charging mechanic.
                if (inputManager.isKeyDown(KeyEvent.VK_SPACE)) {
                    if (!this.annihilator.isReady()) {
                        this.spaceHeldSince = 0;
                    } else if (this.spaceHeldSince == 0) {
                        this.spaceHeldSince = System.currentTimeMillis();
                    }

                    if (this.spaceHeldSince != 0
                            && this.level == OrbitalAnnihilator.REQUIRED_LEVEL
                            && System.currentTimeMillis() - this.spaceHeldSince
                            >= ANNIHILATOR_HOLD_TIME) {
                        this.annihilator.trigger();
                    }
                } else {
                    this.spaceHeldSince = 0;
                }
            } else {
                // Cancel charging when the ship is destroyed.
                this.annihilator.cancelCharge();
                this.spaceHeldSince = 0;
            }

            if (this.enemyShipSpecial != null) {
                if (!this.enemyShipSpecial.isDestroyed())
                    this.enemyShipSpecial.move(2, 0);
                else if (this.enemyShipSpecialExplosionCooldown.checkFinished())
                    this.enemyShipSpecial = null;
            }
            if (this.enemyShipSpecial == null
                    && this.enemyShipSpecialCooldown.checkFinished()) {
                this.enemyShipSpecial = new EnemyShip();
                this.enemyShipSpecialCooldown.reset();
                this.logger.info("A special ship appears");
            }
            if (this.enemyShipSpecial != null
                    && this.enemyShipSpecial.getPositionX() > this.width) {
                this.enemyShipSpecial = null;
                this.logger.info("The special ship has escaped");
            }

            this.ship.update();
            this.annihilator.update();
            this.enemyShipFormation.update();
            this.enemyShipFormation.shoot(this.bullets);
        }

        // Twin Cannon item: appears once in its level, after some kills.
        if (!this.twinCanonSpawned && TwinCanon.isUnlocked(this.level)
                && this.shipsDestroyed - this.shipsDestroyedAtStart >= TwinCanon.SPAWN_KILLS) {
            this.twinCanonSpawned = true;
            this.twinCanonItem = new TwinCanon(
                    30 + (int) (Math.random() * (this.width - 60)), 0);
        }

        // The Twin Cannon item falls, and is lost if it leaves the screen.
        if (this.twinCanonItem != null) {
            this.twinCanonItem.update();
            if (this.twinCanonItem.getPositionY() > this.height)
                this.twinCanonItem = null;
        }

        this.itemManager.update(this.ship);
        this.itemManager.checkCollisions(this.ship);
        manageCollisions();
        if (this.annihilator.isFiring())
            manageAnnihilatorBeam();
        cleanBullets();
        draw();

        if (!this.levelFinished) {
            if (this.lives == 0) {
                this.levelFinished = true;
                this.stageCleared = false;
                this.screenFinishedCooldown.reset();
                this.logger.info("Stage " + this.level + " failed.");
            } else if (this.enemyShipFormation.isEmpty()) {
                this.levelFinished = true;
                this.stageCleared = true;
                this.screenFinishedCooldown.reset();
                this.logger.info("Stage " + this.level + " cleared.");
            }
        }

        if (this.levelFinished && this.screenFinishedCooldown.checkFinished())
            this.isRunning = false;
    }

    /**
     * Draws the elements associated with the screen.
     */
    private void draw() {
        drawManager.initDrawing(this);
        if (this.annihilator.isCharging() || this.annihilator.isFiring())
            drawManager.drawOrbitalAnnihilator(this.ship,
                    OrbitalAnnihilator.BEAM_WIDTH, SEPARATION_LINE_HEIGHT,
                    this.annihilator.isFiring(),
                    this.annihilator.getChargeProgress());

        drawManager.drawEntity(this.ship, this.ship.getPositionX(),
                this.ship.getPositionY());
        if (this.ship.isShieldActive() && !this.ship.isDestroyed()) {
            drawManager.drawShield(this, this.ship);
        }
        if (this.enemyShipSpecial != null)
            drawManager.drawEntity(this.enemyShipSpecial,
                    this.enemyShipSpecial.getPositionX(),
                    this.enemyShipSpecial.getPositionY());

        enemyShipFormation.draw();

        for (Bullet bullet : this.bullets)
            drawManager.drawEntity(bullet, bullet.getPositionX(),
                    bullet.getPositionY());
        this.itemManager.draw(this.drawManager);

        if (this.twinCanonItem != null)
            drawManager.drawEntity(this.twinCanonItem,
                    this.twinCanonItem.getPositionX(),
                    this.twinCanonItem.getPositionY());

        // Interface.
        drawManager.drawScore(this, this.score);
        drawManager.drawLives(this, this.lives);
        if (this.annihilator.isReady())
            drawManager.drawAnnihilatorReady(this);
        drawManager.drawHorizontalLine(this, SEPARATION_LINE_HEIGHT - 1);

        // Countdown to game start.
        if (!this.inputDelay.checkFinished()) {
            int countdown = (int) ((INPUT_DELAY
                    - (System.currentTimeMillis() - this.gameStartTime)) / 1000);
            playCountdownSound(countdown);
            drawManager.drawCountDown(this, this.level, countdown,
                    this.bonusLife);
            drawManager.drawHorizontalLine(this, this.height / 2 - this.height
                    / 12);
            drawManager.drawHorizontalLine(this, this.height / 2 + this.height
                    / 12);
        }

        drawManager.completeDrawing(this);
    }

    /**
     * Cleans bullets that go off screen.
     */
    private void cleanBullets() {
        Set<Bullet> recyclable = new HashSet<Bullet>();
        for (Bullet bullet : this.bullets) {
            bullet.update();
            if (bullet.getPositionY() < SEPARATION_LINE_HEIGHT
                    || bullet.getPositionY() > this.height)
                recyclable.add(bullet);
        }
        this.bullets.removeAll(recyclable);
        BulletPool.recycle(recyclable);
    }

    /**
     * Manages collisions between bullets and ships.
     */
    private void manageCollisions() {
        Set<Bullet> recyclable = new HashSet<Bullet>();
        for (Bullet bullet : this.bullets)
            if (bullet.getSpeed() > 0) {
                if (checkCollision(bullet, this.ship) && !this.levelFinished) {
                    recyclable.add(bullet);
                    if (!this.ship.isDestroyed()) {
                        if (this.ship.isShieldActive()) {
                            this.logger.info("Enemy shot absorbed by Energy Shield.");
                        } else {
                            this.ship.destroy();
                            this.lives--;
                            this.itemManager.getBuffManager().resetAllBuffs(this.ship);
                            this.logger.info("Hit on player ship, " + this.lives
                                    + " lives remaining.");
                        }
                    }
                }
            } else {
                for (EnemyShip enemyShip : this.enemyShipFormation)
                    if (!enemyShip.isDestroyed()
                            && checkCollision(bullet, enemyShip)) {
                        this.score += enemyShip.getPointValue();
                        this.shipsDestroyed++;
                        this.enemyShipFormation.destroy(enemyShip);
                        recyclable.add(bullet);
                        // Orbital Annihilator: small chance, level 7, once.
                        if (!this.annihilatorDroppedThisLevel
                                && this.level == OrbitalAnnihilator.REQUIRED_LEVEL
                                && Math.random() < ANNIHILATOR_DROP_CHANCE) {
                            this.itemManager.spawnItem(new AnnihilatorItem(
                                    enemyShip.getPositionX(),
                                    enemyShip.getPositionY(),
                                    this.annihilator));
                            this.annihilatorDroppedThisLevel = true;
                            this.logger.info("Orbital Annihilator dropped from enemy ship.");
                        }
                        if (!this.shieldDroppedThisLevel) {
                            boolean shouldDrop = (Math.random() < 0.15) || (this.enemyShipFormation.isEmpty());
                            if (shouldDrop) {
                                this.itemManager.spawnItem(new ShieldItem(
                                        enemyShip.getPositionX(),
                                        enemyShip.getPositionY()
                                ));
                                this.shieldDroppedThisLevel = true;
                                this.logger.info("Shield item dropped from enemy ship.");
                            }
                        }
                    }
                if (this.enemyShipSpecial != null
                        && !this.enemyShipSpecial.isDestroyed()
                        && checkCollision(bullet, this.enemyShipSpecial)) {
                    this.score += this.enemyShipSpecial.getPointValue();
                    this.shipsDestroyed++;

                    if (!this.hyperfireDroppedThisLevel) {
                        this.itemManager.spawnItem(new Hyperfire(
                                this.enemyShipSpecial.getPositionX(),
                                this.enemyShipSpecial.getPositionY()
                        ));
                        this.hyperfireDroppedThisLevel = true;
                        this.logger.info("Hyperfire dropped from red UFO.");
                    }

                    this.enemyShipSpecial.destroy();
                    this.enemyShipSpecialExplosionCooldown.reset();
                    recyclable.add(bullet);
                }

                // Shooting the Twin Cannon item collects it.
                if (this.twinCanonItem != null
                        && checkCollision(bullet, this.twinCanonItem)) {
                    this.itemManager.getBuffManager().addBuff(
                            this.twinCanonItem, this.ship);
                    this.twinCanonItem = null;
                    recyclable.add(bullet);
                }
            }
        this.bullets.removeAll(recyclable);
        BulletPool.recycle(recyclable);
    }

    /**
     * Applies the Orbital Annihilator beam.
     */
    private void manageAnnihilatorBeam() {
        for (EnemyShip enemyShip : this.enemyShipFormation)
            if (!enemyShip.isDestroyed()
                    && OrbitalAnnihilator.hits(this.ship, enemyShip)) {
                this.score += enemyShip.getPointValue();
                this.shipsDestroyed++;
                this.enemyShipFormation.destroy(enemyShip);
            }

        if (this.enemyShipSpecial != null
                && !this.enemyShipSpecial.isDestroyed()
                && OrbitalAnnihilator.hits(this.ship, this.enemyShipSpecial)) {
            this.score += this.enemyShipSpecial.getPointValue();
            this.shipsDestroyed++;
            this.enemyShipSpecial.destroy();
            this.enemyShipSpecialExplosionCooldown.reset();
        }

        Set<Bullet> neutralized = new HashSet<Bullet>();
        for (Bullet bullet : this.bullets)
            if (bullet.getSpeed() > 0
                    && OrbitalAnnihilator.hits(this.ship, bullet))
                neutralized.add(bullet);
        this.bullets.removeAll(neutralized);
        BulletPool.recycle(neutralized);
    }

    /**
     * Checks if two entities are colliding.
     */
    private boolean checkCollision(final Entity a, final Entity b) {
        int centerAX = a.getPositionX() + a.getWidth() / 2;
        int centerAY = a.getPositionY() + a.getHeight() / 2;
        int centerBX = b.getPositionX() + b.getWidth() / 2;
        int centerBY = b.getPositionY() + b.getHeight() / 2;

        int maxDistanceX = a.getWidth() / 2 + b.getWidth() / 2;
        int maxDistanceY = a.getHeight() / 2 + b.getHeight() / 2;

        int distanceX = Math.abs(centerAX - centerBX);
        int distanceY = Math.abs(centerAY - centerBY);

        return distanceX < maxDistanceX && distanceY < maxDistanceY;
    }

    /**
     * Checks if the stage was cleared.
     */
    public final boolean isStageCleared() {
        return this.stageCleared;
    }

    /**
     * Returns a GameState object representing the status of the game.
     */
    public final GameState getGameState() {
        return new GameState(this.level, this.score, this.lives,
                this.bulletsShot, this.shipsDestroyed);
    }

    /**
     * Plays the countdown sound once each time the countdown number changes.
     */
    private void playCountdownSound(final int countdown) {
        if (countdown == this.lastCountdown)
            return;
        this.lastCountdown = countdown;
        if (countdown == 0)
            AudioManager.playSFX("audio/sfx/countdown_go.wav");
        else if (countdown <= 3)
            AudioManager.playSFX("audio/sfx/countdown_beep.wav");
    }
}