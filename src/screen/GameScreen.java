package screen;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import audio.AudioManager;
import engine.Cooldown;
import engine.ControlsConfig;
import engine.Core;
import engine.GameSettings;
import engine.GameState;
import entity.Bullet;
import entity.BulletPool;
import entity.EnemyShip;
import entity.EnemyShipFormation;
import entity.Entity;
import entity.Grenade;
import entity.GrenadeFragment;
import entity.Ship;
import entity.item.AnnihilatorItem;
import entity.item.BuffHud;
import entity.item.ChameleonHull;
import entity.item.ClusterGrenadeItem;
import entity.item.Hyperfire;
import entity.item.ItemManager;
import entity.item.OrbitalAnnihilator;
import entity.item.ShieldItem;
import entity.item.TwinCanon;
import event.EnemyDefeatedEvent;
import event.GameplayEventListener;
import event.GameplayEventPublisher;
import records.Achievement;
import records.RecordsService;
import records.RunSession;
import vfx.ParticleSystem;

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
    private static final int BONUS_SHIP_INTERVAL = 20000;
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

    /** Chance for a destroyed enemy ship to drop a Cluster Grenade item. */
    private static final double GRENADE_DROP_CHANCE = 0.15;
    /** Fixed altitude (Y) where a grenade explodes if it hits nothing. */
    private static final int GRENADE_DETONATION_Y = 200;
    /** Maximum number of Cluster Grenade items dropped in one level. */
    private static final int MAX_GRENADE_DROPS_PER_LEVEL = 2;
    /** Chance that a destroyed enemy drops the Orbital Annihilator. */
    private static final double ANNIHILATOR_DROP_CHANCE = 0.05;

    private ItemManager itemManager;
    private final Random itemDropRandom = new Random();
    private boolean shieldDroppedThisLevel = false;
    private boolean chameleonDroppedThisLevel = false;
    private boolean hyperfireDroppedThisLevel = false;
    private boolean annihilatorDroppedThisLevel = false;

    /** Grenades launched by the player and not exploded yet. */
    private Set<Grenade> grenades;
    /** Fragments of exploded grenades. */
    private Set<GrenadeFragment> fragments;
    /** Prevents launching several grenades while holding G. */
    private boolean grenadeKeyPressed = false;
    /** Number of Cluster Grenade items dropped in this level. */
    private int grenadeDropsThisLevel = 0;
    private int shipsDestroyedAtStart;
    private boolean twinCanonSpawned;
    private TwinCanon twinCanonItem;

    /** Current game difficulty settings. */
    private GameSettings gameSettings;
    /** Settings of every wave in this stage, in play order. */
    private List<GameSettings> waves;
    /** Index of the wave currently being played. */
    private int currentWave;
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
    /** Explosion particles. */
    private ParticleSystem particleSystem = new ParticleSystem();
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

    private RunSession runSession;
    private RecordsService recordsService;
    private Achievement visibleAchievement;
    private long achievementNoticeUntil;
    private final GameplayEventPublisher recordsEvents = new GameplayEventPublisher();
    private final Map<EnemyShip, String> defeatIds = new IdentityHashMap<EnemyShip, String>();

    /** Prevents repeated activation while holding Buff key. */
    private boolean hyperfireKeyPressed = false;
    /** Flag to track if the urgent low-health BGM is currently playing. */
    private boolean isLowHealthBGM = false;

    /**
     * Constructor, establishes the properties of the screen.
     */
    public GameScreen(final GameState gameState,
                      final GameSettings gameSettings, final boolean bonusLife,
                      final int width, final int height, final int fps) {
        this(gameState, Collections.singletonList(gameSettings), bonusLife,
                width, height, fps);
    }

    /**
     * Constructor for a stage with one or more waves.
     */
    public GameScreen(final GameState gameState,
                      final List<GameSettings> waves, final boolean bonusLife,
                      final int width, final int height, final int fps) {
        this(gameState, waves, bonusLife, width, height, fps, null);
    }

    public GameScreen(final GameState gameState, final GameSettings gameSettings,
                      final boolean bonusLife, final int width, final int height, final int fps,
                      final RunSession runSession) {
        this(gameState, Collections.singletonList(gameSettings), bonusLife, width, height, fps, runSession);
    }

    public GameScreen(final GameState gameState, final List<GameSettings> waves,
                      final boolean bonusLife, final int width, final int height, final int fps,
                      final RunSession runSession) {
        super(width, height, fps);
        this.runSession = runSession;
        this.recordsService = Core.getRecordsService();
        this.recordsEvents.subscribe(EnemyDefeatedEvent.class,
                new GameplayEventListener<EnemyDefeatedEvent>() {
                    public void onEvent(final EnemyDefeatedEvent event) {
                        if (recordsService != null) {
                            recordsService.enemyDefeated(event.getRunId(), event.getEventId());
                        }
                    }
                });

        this.waves = copyWaves(waves);
        this.currentWave = 0;
        this.gameSettings = this.waves.get(0);
        this.bonusLife = bonusLife;
        this.level = gameState.getLevel();
        this.score = gameState.getScore();
        this.lives = gameState.getLivesRemaining();
        if (this.bonusLife) {
            this.lives++;
        }
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
        this.grenades = new HashSet<Grenade>();
        this.fragments = new HashSet<GrenadeFragment>();
        this.grenadeKeyPressed = false;
        this.grenadeDropsThisLevel = 0;
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

        if (this.lives == 1) {
            AudioManager.playBGM("audio/bgm/music_game_urgent.wav");
            this.isLowHealthBGM = true;
        } else {
            AudioManager.playBGM("audio/bgm/music_game.wav");
            this.isLowHealthBGM = false;
        }
    }

    /**
     * Starts the action.
     */
    public final int run() {
        try {
            super.run();
        } finally {
            if (this.runSession != null) {
                this.runSession.stopActiveSegment(System.nanoTime());
            }
        }

        // Life bonus is only awarded when the stage was cleared.
        if (this.stageCleared) {
            this.score += LIFE_SCORE * (this.lives - 1);
        }
        this.logger.info("Screen cleared with a score of " + this.score);

        return this.returnCode;
    }

    /**
     * Updates the elements on screen and checks for events.
     */
    protected final void update() {
        super.update();

        if (this.recordsService != null) {
            long now = System.currentTimeMillis();
            if (this.visibleAchievement == null || now >= this.achievementNoticeUntil) {
                this.visibleAchievement = this.recordsService.pollNotification();
                this.achievementNoticeUntil = this.visibleAchievement == null ? 0 : now + 3000;
            }
        }

        if (this.inputDelay.checkFinished() && !this.levelFinished) {
            if (this.runSession != null) {
                this.runSession.startActiveSegment(System.nanoTime());
            }
            if (this.runSession != null && this.recordsService != null) {
                this.recordsService.activeDuration(this.runSession.getRunId(),
                        this.runSession.getActiveDurationMillis(System.nanoTime()));
            }

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
                    clampShipPosition();
                }
                if (moveLeft && !isLeftBorder) {
                    this.ship.moveLeft();
                    clampShipPosition();
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

                // Cluster Grenade launch with the G key.
                boolean grenadeKeyDown = inputManager.isKeyDown(KeyEvent.VK_G);
                if (grenadeKeyDown && !this.grenadeKeyPressed) {
                    this.ship.launchGrenade(this.grenades, GRENADE_DETONATION_Y);
                }
                this.grenadeKeyPressed = grenadeKeyDown;

                // Orbital Annihilator charging mechanic.
                if (inputManager.isKeyDown(KeyEvent.VK_SPACE)) {
                    if (!this.annihilator.isReady()) {
                        this.spaceHeldSince = 0;
                    } else if (this.spaceHeldSince == 0) {
                        this.spaceHeldSince = System.currentTimeMillis();
                    }

                    if (this.spaceHeldSince != 0
                            && this.level == OrbitalAnnihilator.REQUIRED_LEVEL
                            && System.currentTimeMillis() - this.spaceHeldSince >= ANNIHILATOR_HOLD_TIME) {
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
                if (!this.enemyShipSpecial.isDestroyed()) {
                    this.enemyShipSpecial.move(2, 0);
                } else if (this.enemyShipSpecialExplosionCooldown.checkFinished()) {
                    this.enemyShipSpecial = null;
                }
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

            updateBallMovement();

            this.annihilator.update();
            this.enemyShipFormation.update();
            this.enemyShipFormation.shoot(this.bullets);
        }

        this.ship.update();

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
            if (this.twinCanonItem.getPositionY() > this.height) {
                this.twinCanonItem = null;
            }
        }

        if (this.inputDelay.checkFinished() && !this.levelFinished) {
            this.itemManager.update(this.ship);

            if (!this.ship.isDestroyed()) {
                this.itemManager.checkCollisions(this.ship);
            }

            clampShipPosition();
            clampBallPositionY();
        }

        manageCollisions();
        manageGrenades();
        manageBallContact();

        if (this.annihilator.isFiring()) {
            manageAnnihilatorBeam();
        }

        cleanBullets();
        this.particleSystem.update();
        draw();

        if (!this.levelFinished) {
            if (this.lives == 0) {
                this.levelFinished = true;
                this.stageCleared = false;
                this.screenFinishedCooldown.reset();
                this.logger.info("Stage " + this.level + " failed.");
            } else if (this.enemyShipFormation.isEmpty()) {
                if (this.currentWave < this.waves.size() - 1) {
                    startNextWave();
                } else {
                    this.levelFinished = true;
                    this.stageCleared = true;
                    this.screenFinishedCooldown.reset();
                    this.logger.info("Stage " + this.level + " cleared.");
                }
            }
        }

        if (this.levelFinished && this.runSession != null) {
            this.runSession.stopActiveSegment(System.nanoTime());
        }

        if (this.levelFinished && this.screenFinishedCooldown.checkFinished()) {
            this.isRunning = false;
        }
    }

    /**
     * Copies the wave list after checking that it can be played.
     */
    static List<GameSettings> copyWaves(final List<GameSettings> waves) {
        if (waves == null || waves.isEmpty()) {
            throw new IllegalArgumentException("A stage must contain at least one wave.");
        }
        List<GameSettings> copy = new ArrayList<GameSettings>(waves);
        for (int i = 0; i < copy.size(); i++) {
            if (copy.get(i) == null) {
                throw new IllegalArgumentException("Wave " + (i + 1) + " must not be null.");
            }
        }
        return copy;
    }

    /**
     * Replaces the empty formation with the next wave's formation.
     */
    private void startNextWave() {
        this.logger.info("Stage " + this.level + " wave " + (this.currentWave + 1) + " cleared.");
        this.currentWave++;
        this.gameSettings = this.waves.get(this.currentWave);
        this.enemyShipFormation = new EnemyShipFormation(this.gameSettings);
        this.enemyShipFormation.attach(this);
        this.logger.info("Stage " + this.level + " wave " + (this.currentWave + 1)
                + " of " + this.waves.size() + " started.");
    }

    /**
     * Draws the elements associated with the screen.
     */
    private void draw() {
        drawManager.initDrawing(this);

        if (this.annihilator.isCharging() || this.annihilator.isFiring()) {
            drawManager.drawOrbitalAnnihilator(
                    this.ship,
                    OrbitalAnnihilator.BEAM_WIDTH,
                    SEPARATION_LINE_HEIGHT,
                    this.annihilator.isFiring(),
                    this.annihilator.getChargeProgress()
            );
        }

        drawManager.drawEntity(this.ship, this.ship.getPositionX(), this.ship.getPositionY());

        if (this.ship.isShieldActive() && !this.ship.isDestroyed()) {
            drawManager.drawShield(this, this.ship);
        }

        if (this.enemyShipSpecial != null) {
            drawManager.drawEntity(this.enemyShipSpecial,
                    this.enemyShipSpecial.getPositionX(),
                    this.enemyShipSpecial.getPositionY());
        }

        enemyShipFormation.draw();

        for (Bullet bullet : this.bullets) {
            drawManager.drawEntity(bullet, bullet.getPositionX(), bullet.getPositionY());
        }

        for (Grenade grenade : this.grenades) {
            drawManager.drawGrenade(grenade);
        }
        for (GrenadeFragment fragment : this.fragments) {
            drawManager.drawGrenadeFragment(fragment);
        }

        this.itemManager.draw(this.drawManager);

        if (this.twinCanonItem != null) {
            drawManager.drawEntity(this.twinCanonItem,
                    this.twinCanonItem.getPositionX(),
                    this.twinCanonItem.getPositionY());
        }

        // Interface.
        drawManager.drawScore(this, this.score);
        drawManager.drawLives(this, this.lives);
        drawManager.drawGrenadeCount(this, this.ship.getGrenadeCount());
        drawManager.drawBuffs(this, BuffHud.buildLines(
                this.itemManager.getBuffManager().getActiveBuffsRemainingTime(),
                System.currentTimeMillis()));

        if (this.annihilator.isReady()) {
            drawManager.drawAnnihilatorReady(this);
        }
        if (this.level == OrbitalAnnihilator.REQUIRED_LEVEL) {
            drawManager.drawAnnihilatorGauge(this, this.annihilator.getChargeProgress());
        }

        drawManager.drawHorizontalLine(this, SEPARATION_LINE_HEIGHT - 1);

        // Countdown to game start.
        if (!this.inputDelay.checkFinished()) {
            int countdown = (int) ((INPUT_DELAY
                    - (System.currentTimeMillis() - this.gameStartTime)) / 1000);
            playCountdownSound(countdown);
            drawManager.drawCountDown(this, this.level, countdown, this.bonusLife);
            drawManager.drawHorizontalLine(this, this.height / 2 - this.height / 12);
            drawManager.drawHorizontalLine(this, this.height / 2 + this.height / 12);
        }

        drawManager.drawParticles(this.particleSystem);

        if (this.visibleAchievement != null) {
            drawManager.drawAchievementNotice(this, this.visibleAchievement);
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
                    || bullet.getPositionY() > this.height) {
                recyclable.add(bullet);
            }
        }
        this.bullets.removeAll(recyclable);
        BulletPool.recycle(recyclable);
    }

    /**
     * Manages collisions between bullets and ships.
     */
    private void manageCollisions() {
        if (this.levelFinished) {
            return;
        }
        Set<Bullet> recyclable = new HashSet<Bullet>();
        for (Bullet bullet : this.bullets) {
            if (bullet.getSpeed() > 0) {
                if (checkCollision(bullet, this.ship) && !this.levelFinished) {
                    recyclable.add(bullet);
                    if (!this.ship.isChameleonHullActive()) {
                        if (!this.ship.isDestroyed()) {
                            if (this.ship.isShieldActive()) {
                                this.logger.info("Enemy shot absorbed by Energy Shield.");
                            } else {
                                this.ship.destroy();
                                this.ship.triggerDamageFeedback();
                                this.particleSystem.spawnPlayerExplosion(
                                        this.ship.getPositionX() + this.ship.getWidth() / 2,
                                        this.ship.getPositionY() + this.ship.getHeight() / 2);
                                this.lives = Math.max(0, this.lives - 1);
                                this.itemManager.getBuffManager().resetAllBuffs(this.ship);
                                if (this.lives == 1 && !this.isLowHealthBGM) {
                                    AudioManager.playBGM("audio/bgm/music_game_urgent.wav");
                                    this.isLowHealthBGM = true;
                                }
                                this.logger.info("Hit on player ship, " + this.lives + " lives remaining.");
                            }
                        }
                    }
                }
            } else {
                for (EnemyShip enemyShip : this.enemyShipFormation) {
                    if (!enemyShip.isDestroyed() && checkCollision(bullet, enemyShip)) {
                        this.score += enemyShip.getPointValue();
                        this.shipsDestroyed++;
                        recordEnemyDefeat(enemyShip);
                        this.enemyShipFormation.destroy(enemyShip);
                        this.particleSystem.spawnEnemyExplosion(
                                enemyShip.getPositionX() + enemyShip.getWidth() / 2,
                                enemyShip.getPositionY() + enemyShip.getHeight() / 2);

                        tryDropChameleonHull(enemyShip);

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
                                this.logger.info("Shield item dropped from enemy ship at ("
                                        + enemyShip.getPositionX() + ", "
                                        + enemyShip.getPositionY() + ").");
                            }
                        }

                        tryDropGrenade(enemyShip);
                        recyclable.add(bullet);
                    }
                }

                if (this.enemyShipSpecial != null
                        && !this.enemyShipSpecial.isDestroyed()
                        && checkCollision(bullet, this.enemyShipSpecial)) {
                    this.score += this.enemyShipSpecial.getPointValue();
                    this.shipsDestroyed++;
                    recordEnemyDefeat(this.enemyShipSpecial);

                    if (!this.hyperfireDroppedThisLevel) {
                        this.itemManager.spawnItem(new Hyperfire(
                                this.enemyShipSpecial.getPositionX(),
                                this.enemyShipSpecial.getPositionY()
                        ));
                        this.hyperfireDroppedThisLevel = true;
                        this.logger.info("Hyperfire dropped from red UFO.");
                    }

                    this.particleSystem.spawnEnemyExplosion(
                            this.enemyShipSpecial.getPositionX() + this.enemyShipSpecial.getWidth() / 2,
                            this.enemyShipSpecial.getPositionY() + this.enemyShipSpecial.getHeight() / 2);
                    tryDropChameleonHull(this.enemyShipSpecial);

                    this.enemyShipSpecial.destroy();
                    this.enemyShipSpecialExplosionCooldown.reset();
                    recyclable.add(bullet);
                }

                // Shooting the Twin Cannon item collects it.
                if (this.twinCanonItem != null && checkCollision(bullet, this.twinCanonItem)) {
                    this.itemManager.getBuffManager().addBuff(this.twinCanonItem, this.ship);
                    this.twinCanonItem = null;
                    recyclable.add(bullet);
                }
            }
        }
        this.bullets.removeAll(recyclable);
        BulletPool.recycle(recyclable);
    }

    /**
     * Each destroyed enemy ship has a chance to drop a Cluster Grenade item.
     */
    private void tryDropGrenade(final EnemyShip enemyShip) {
        if (this.grenadeDropsThisLevel < MAX_GRENADE_DROPS_PER_LEVEL
                && Math.random() < GRENADE_DROP_CHANCE) {
            this.grenadeDropsThisLevel++;
            this.itemManager.spawnItem(new ClusterGrenadeItem(
                    enemyShip.getPositionX(), enemyShip.getPositionY()));
            this.logger.info("Grenade item dropped from enemy ship at ("
                    + enemyShip.getPositionX() + ", "
                    + enemyShip.getPositionY() + ").");
        }
    }

    /**
     * Moves the grenades and their fragments.
     */
    private void manageGrenades() {
        Set<Grenade> exploded = new HashSet<Grenade>();
        for (Grenade grenade : this.grenades) {
            grenade.update();

            boolean impact = false;
            for (EnemyShip enemyShip : this.enemyShipFormation) {
                if (!enemyShip.isDestroyed() && checkCollision(grenade, enemyShip)) {
                    impact = true;
                    break;
                }
            }
            if (!impact && this.enemyShipSpecial != null
                    && !this.enemyShipSpecial.isDestroyed()
                    && checkCollision(grenade, this.enemyShipSpecial)) {
                impact = true;
            }

            if (impact || grenade.hasReachedAltitude()) {
                exploded.add(grenade);
                this.fragments.addAll(GrenadeFragment.explode(
                        grenade.getCenterX(), grenade.getCenterY()));
            }
        }
        this.grenades.removeAll(exploded);

        Set<GrenadeFragment> expired = new HashSet<GrenadeFragment>();
        for (GrenadeFragment fragment : this.fragments) {
            fragment.update();

            boolean used = false;
            for (EnemyShip enemyShip : this.enemyShipFormation) {
                if (!enemyShip.isDestroyed() && checkCollision(fragment, enemyShip)) {
                    this.score += enemyShip.getPointValue();
                    this.shipsDestroyed++;
                    recordEnemyDefeat(enemyShip);
                    this.enemyShipFormation.destroy(enemyShip);
                    this.particleSystem.spawnEnemyExplosion(
                            enemyShip.getPositionX() + enemyShip.getWidth() / 2,
                            enemyShip.getPositionY() + enemyShip.getHeight() / 2);
                    tryDropGrenade(enemyShip);
                    tryDropChameleonHull(enemyShip);
                    used = true;
                    break;
                }
            }

            if (!used && this.enemyShipSpecial != null
                    && !this.enemyShipSpecial.isDestroyed()
                    && checkCollision(fragment, this.enemyShipSpecial)) {
                this.score += this.enemyShipSpecial.getPointValue();
                this.shipsDestroyed++;
                recordEnemyDefeat(this.enemyShipSpecial);
                if (!this.hyperfireDroppedThisLevel) {
                    this.itemManager.spawnItem(new Hyperfire(
                            this.enemyShipSpecial.getPositionX(),
                            this.enemyShipSpecial.getPositionY()));
                    this.hyperfireDroppedThisLevel = true;
                    this.logger.info("Hyperfire dropped from red UFO.");
                }
                this.particleSystem.spawnEnemyExplosion(
                        this.enemyShipSpecial.getPositionX() + this.enemyShipSpecial.getWidth() / 2,
                        this.enemyShipSpecial.getPositionY() + this.enemyShipSpecial.getHeight() / 2);
                tryDropChameleonHull(this.enemyShipSpecial);
                this.enemyShipSpecial.destroy();
                this.enemyShipSpecialExplosionCooldown.reset();
                used = true;
            }

            if (used || fragment.isExpired()) {
                expired.add(fragment);
            }
        }
        this.fragments.removeAll(expired);
    }

    /**
     * Applies the Orbital Annihilator beam.
     */
    private void manageAnnihilatorBeam() {
        if (this.levelFinished) {
            return;
        }
        for (EnemyShip enemyShip : this.enemyShipFormation) {
            if (!enemyShip.isDestroyed() && OrbitalAnnihilator.hits(this.ship, enemyShip)) {
                this.score += enemyShip.getPointValue();
                this.shipsDestroyed++;
                recordEnemyDefeat(enemyShip);
                this.enemyShipFormation.destroy(enemyShip);
                this.particleSystem.spawnEnemyExplosion(
                        enemyShip.getPositionX() + enemyShip.getWidth() / 2,
                        enemyShip.getPositionY() + enemyShip.getHeight() / 2);
                tryDropChameleonHull(enemyShip);
            }
        }

        if (this.enemyShipSpecial != null
                && !this.enemyShipSpecial.isDestroyed()
                && OrbitalAnnihilator.hits(this.ship, this.enemyShipSpecial)) {
            this.score += this.enemyShipSpecial.getPointValue();
            this.shipsDestroyed++;
            recordEnemyDefeat(this.enemyShipSpecial);
            this.particleSystem.spawnEnemyExplosion(
                    this.enemyShipSpecial.getPositionX() + this.enemyShipSpecial.getWidth() / 2,
                    this.enemyShipSpecial.getPositionY() + this.enemyShipSpecial.getHeight() / 2);
            this.enemyShipSpecial.destroy();
            tryDropChameleonHull(this.enemyShipSpecial);
            this.enemyShipSpecialExplosionCooldown.reset();
        }

        Set<Bullet> neutralized = new HashSet<Bullet>();
        for (Bullet bullet : this.bullets) {
            if (bullet.getSpeed() > 0 && OrbitalAnnihilator.hits(this.ship, bullet)) {
                neutralized.add(bullet);
            }
        }
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
        if (countdown == this.lastCountdown) {
            return;
        }
        this.lastCountdown = countdown;
        if (countdown == 0) {
            AudioManager.playSFX("audio/sfx/countdown_go.wav");
        } else if (countdown <= 3) {
            AudioManager.playSFX("audio/sfx/countdown_beep.wav");
        }
    }

    private void recordEnemyDefeat(final EnemyShip enemy) {
        if (this.runSession == null) {
            return;
        }
        String id = this.defeatIds.get(enemy);
        if (id == null) {
            id = UUID.randomUUID().toString();
            this.defeatIds.put(enemy, id);
        }
        this.recordsEvents.publish(new EnemyDefeatedEvent(this.runSession.getRunId(), id));
    }

    public GameplayEventPublisher getGameplayEvents() {
        return this.recordsEvents;
    }

    public String getActiveRunId() {
        return this.runSession == null ? null : this.runSession.getRunId();
    }

    /** Keep the extra movement inside the existing playfield borders. */
    private void clampShipPosition() {
        int maximumX = this.width - this.ship.getWidth() - 1;
        this.ship.setPositionX(
                Math.max(1, Math.min(maximumX, this.ship.getPositionX()))
        );
    }

    private void tryDropChameleonHull(final EnemyShip enemy) {
        if (!this.chameleonDroppedThisLevel
                && this.level >= ChameleonHull.REQUIRED_LEVEL
                && !this.levelFinished
                && this.lives > 0
                && this.itemDropRandom.nextDouble() < ChameleonHull.DROP_CHANCE) {

            this.itemManager.spawnItem(
                    new ChameleonHull(
                            enemy.getPositionX(),
                            enemy.getPositionY()
                    )
            );
            this.chameleonDroppedThisLevel = true;
        }
    }

    /** Vertical keys are enabled only during the star transformation. */
    private void updateBallMovement() {
        if (!this.ship.isChameleonHullActive() || this.ship.isDestroyed()) {
            return;
        }

        boolean up = inputManager.isKeyDown(KeyEvent.VK_UP)
                || inputManager.isKeyDown(KeyEvent.VK_W);
        boolean down = inputManager.isKeyDown(KeyEvent.VK_DOWN)
                || inputManager.isKeyDown(KeyEvent.VK_S);

        if (up && !down) {
            this.ship.moveUp();
        }
        if (down && !up) {
            this.ship.moveDown();
        }

        clampBallPositionY();
    }

    private void clampBallPositionY() {
        if (this.ship.isChameleonHullActive()) {
            int minimumY = SEPARATION_LINE_HEIGHT + 1;
            int maximumY = this.height - this.ship.getHeight() - 1;

            this.ship.setPositionY(
                    Math.max(
                            minimumY,
                            Math.min(maximumY, this.ship.getPositionY())
                    )
            );
        }
    }

    /** Contact kills use the existing score, destruction and drop systems. */
    private void manageBallContact() {
        if (!this.inputDelay.checkFinished()
                || this.levelFinished
                || this.ship.isDestroyed()
                || !this.ship.isChameleonHullActive()) {
            return;
        }

        for (EnemyShip enemy : this.enemyShipFormation) {
            if (!enemy.isDestroyed() && checkCollision(this.ship, enemy)) {
                this.score += enemy.getPointValue();
                this.shipsDestroyed++;
                recordEnemyDefeat(enemy);

                this.enemyShipFormation.destroy(enemy);
                this.particleSystem.spawnEnemyExplosion(
                        enemy.getPositionX() + enemy.getWidth() / 2,
                        enemy.getPositionY() + enemy.getHeight() / 2);
                tryDropChameleonHull(enemy);
            }
        }

        if (this.enemyShipSpecial != null
                && !this.enemyShipSpecial.isDestroyed()
                && checkCollision(this.ship, this.enemyShipSpecial)) {
            this.score += this.enemyShipSpecial.getPointValue();
            this.shipsDestroyed++;
            recordEnemyDefeat(this.enemyShipSpecial);

            this.enemyShipSpecial.destroy();
            this.enemyShipSpecialExplosionCooldown.reset();

            tryDropChameleonHull(this.enemyShipSpecial);
        }
    }
}