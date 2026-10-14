package entity;

import java.awt.Color;
import java.util.Set;

import engine.Cooldown;
import engine.Core;
import engine.DrawManager.SpriteType;

/**
 * Orbital Guardian: bigger and tougher enemy that appears on the last level.
 * It needs several hits to be destroyed, shoots at the player and, once
 * destroyed, drops the Orbital Annihilator item.
 */
public class OrbitalGuardian extends Entity {

    /** Hits needed to destroy the guardian. */
    public static final int MAX_HEALTH = 4;
    /** Size of one sprite pixel, in screen pixels. */
    public static final int PIXEL_SIZE = 3;
    /** Horizontal speed, in pixels per update. */
    private static final int SPEED = 1;
    /** Time between two volleys, in milliseconds. */
    private static final int SHOOTING_INTERVAL = 1400;
    /** Speed of the bullets (positive is down). */
    private static final int BULLET_SPEED = 4;
    /** Duration of the white flash when hit, in milliseconds. */
    private static final int HIT_FLASH = 100;
    /** Score for destroying the guardian. */
    private static final int POINT_VALUE = 500;
    /** Speed of the health bar draining animation, in hits per second. */
    private static final float DRAIN_SPEED = 4f;

    /** Remaining hits. */
    private int health;
    /** Moving direction, 1 is right and -1 is left. */
    private int direction;
    /** Moment of the last hit received. */
    private long lastHitTime;
    /** Time between volleys. */
    private Cooldown shootingCooldown;
    /** Health shown by the bar, it slides down to the real health. */
    private float displayedHealth;
    /** Last time the displayed health was updated. */
    private long lastDisplayTime;
    /** True while the star is touching the guardian. */
    private boolean contact;

    /**
     * Constructor.
     *
     * @param positionX
     *            Initial position in the X axis.
     * @param positionY
     *            Initial position in the Y axis.
     */
    public OrbitalGuardian(final int positionX, final int positionY) {
        super(positionX, positionY, 12 * PIXEL_SIZE, 8 * PIXEL_SIZE,
                Color.ORANGE);

        this.spriteType = SpriteType.EnemyShipC1;
        this.health = MAX_HEALTH;
        this.direction = 1;
        this.lastHitTime = 0;
        this.displayedHealth = MAX_HEALTH;
        this.lastDisplayTime = System.currentTimeMillis();
        this.contact = false;
        this.shootingCooldown = Core.getCooldown(SHOOTING_INTERVAL);
        this.shootingCooldown.reset();
    }

    /**
     * Moves the guardian from side to side of the screen.
     *
     * @param screenWidth
     *            Width of the screen.
     */
    public final void update(final int screenWidth) {
        this.positionX += this.direction * SPEED;
        if (this.positionX <= 0)
            this.direction = 1;
        else if (this.positionX + this.width >= screenWidth)
            this.direction = -1;
    }

    /**
     * Shoots a volley of two bullets downwards.
     *
     * @param bullets
     *            Set of bullets on screen, to add the new ones.
     */
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

    /**
     * Receives one hit.
     *
     * @return True if this hit destroyed the guardian.
     */
    public final boolean hit() {
        if (this.health <= 0)
            return false;
        this.health--;
        this.lastHitTime = System.currentTimeMillis();
        return this.health == 0;
    }

    /**
     * Checks if the guardian is destroyed.
     *
     * @return True if there are no hits left.
     */
    public final boolean isDestroyed() {
        return this.health <= 0;
    }

    /**
     * Checks if the guardian was hit a moment ago (to draw a flash).
     *
     * @return True during the flash.
     */
    public final boolean isFlashing() {
        return System.currentTimeMillis() - this.lastHitTime < HIT_FLASH;
    }

    /**
     * Getter for the remaining hits.
     *
     * @return Remaining health.
     */
    public final int getHealth() {
        return this.health;
    }

    /**
     * Getter for the score of the guardian.
     *
     * @return Points obtained when it is destroyed.
     */
    public final int getPointValue() {
        return POINT_VALUE;
    }

    /**
     * Health to draw in the bar: it slides down smoothly to the real health,
     * so the loss of life is visible.
     *
     * @return Displayed health, between 0 and MAX_HEALTH.
     */
    public final float getDisplayedHealth() {
        long now = System.currentTimeMillis();
        float seconds = (now - this.lastDisplayTime) / 1000f;
        this.lastDisplayTime = now;
        if (this.displayedHealth > this.health)
            this.displayedHealth = Math.max(this.health,
                    this.displayedHealth - DRAIN_SPEED * seconds);
        return this.displayedHealth;
    }

    /**
     * Checks if the health bar is still sliding down.
     *
     * @return True while the displayed health is above the real health.
     */
    public final boolean isHealthDraining() {
        return this.displayedHealth > this.health + 0.01f;
    }

    /**
     * Checks if the guardian must still be drawn. A destroyed guardian stays
     * visible, blinking, until its health bar has finished draining.
     *
     * @return True if it is alive or still dying.
     */
    public final boolean isVisible() {
        return !isDestroyed() || this.displayedHealth > 0.01f;
    }

    /**
     * Sets if the star is touching the guardian.
     *
     * @param contact
     *            True while there is contact.
     */
    public final void setContact(final boolean contact) {
        this.contact = contact;
    }

    /**
     * Checks if the star is touching the guardian.
     *
     * @return True while there is contact.
     */
    public final boolean isContact() {
        return this.contact;
    }
}

