package entity;

import java.awt.Color;
import java.util.Set;

import engine.Cooldown;
import engine.Core;
import engine.DrawManager.SpriteType;

/**
 * Orbital Guardian: big enemy of the last level. It needs several hits to be
 * destroyed, shoots at the player and drops the Orbital Annihilator item.
 */
public class OrbitalGuardian extends Entity {

    /** Hits needed to destroy the guardian. */
    public static final int MAX_HEALTH = 4;
    /** Size of one sprite pixel, in screen pixels. */
    public static final int PIXEL_SIZE = 3;
    private static final int SPEED = 1;
    private static final int SHOOTING_INTERVAL = 1400;
    private static final int BULLET_SPEED = 4;
    private static final int HIT_FLASH = 100;
    private static final int POINT_VALUE = 500;

    private int health;
    private int direction;
    private long lastHitTime;
    private Cooldown shootingCooldown;

    public OrbitalGuardian(final int positionX, final int positionY) {
        super(positionX, positionY, 12 * PIXEL_SIZE, 8 * PIXEL_SIZE,
                Color.ORANGE);

        this.spriteType = SpriteType.EnemyShipC1;
        this.health = MAX_HEALTH;
        this.direction = 1;
        this.lastHitTime = 0;
        this.shootingCooldown = Core.getCooldown(SHOOTING_INTERVAL);
        this.shootingCooldown.reset();
    }

    /** Moves the guardian from side to side of the screen. */
    public final void update(final int screenWidth) {
        this.positionX += this.direction * SPEED;
        if (this.positionX <= 0)
            this.direction = 1;
        else if (this.positionX + this.width >= screenWidth)
            this.direction = -1;
    }

    /** Shoots a volley of two bullets downwards. */
    public final void shoot(final Set<Bullet> bullets) {
        if (this.shootingCooldown.checkFinished()) {
            this.shootingCooldown.reset();
            bullets.add(BulletPool.getBullet(this.positionX + this.width / 4,
                    this.positionY + this.height, BULLET_SPEED));
            bullets.add(BulletPool.getBullet(
                    this.positionX + this.width * 3 / 4,
                    this.positionY + this.height, BULLET_SPEED));
        }
    }

    /** Receives one hit. Returns true if this hit destroyed the guardian. */
    public final boolean hit() {
        if (this.health <= 0)
            return false;
        this.health--;
        this.lastHitTime = System.currentTimeMillis();
        return this.health == 0;
    }

    public final boolean isDestroyed() {
        return this.health <= 0;
    }

    /** True during a short white flash after a hit. */
    public final boolean isFlashing() {
        return System.currentTimeMillis() - this.lastHitTime < HIT_FLASH;
    }

    public final int getHealth() {
        return this.health;
    }

    public final int getPointValue() {
        return POINT_VALUE;
    }
}