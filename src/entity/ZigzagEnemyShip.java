package entity;

import engine.DrawManager.SpriteType;

/**
 * Implements an enemy ship that falls down the screen in a zigzag pattern, used
 * by the infinite mode.
 */
public class ZigzagEnemyShip extends EnemyShip {

	/** Distance, in pixels, the ship travels sideways before turning back. */
	private static final int ZIGZAG_AMPLITUDE = 40;
	/** Sideways speed, in pixels per frame. */
	private static final int LATERAL_SPEED = 2;

	/** Position in the x-axis the zigzag is centered on. */
	private int originX;
	/** Downwards speed, in pixels per frame. */
	private int fallSpeed;
	/** Current sideways direction, 1 is right and -1 is left. */
	private int direction;

	/**
	 * Constructor, establishes the ship's properties.
	 *
	 * @param positionX
	 *            Initial position of the ship in the X axis, also the center of
	 *            the zigzag.
	 * @param positionY
	 *            Initial position of the ship in the Y axis.
	 * @param spriteType
	 *            Sprite type, image corresponding to the ship.
	 * @param fallSpeed
	 *            Downwards speed, in pixels per frame.
	 */
	public ZigzagEnemyShip(final int positionX, final int positionY,
			final SpriteType spriteType, final int fallSpeed) {
		super(positionX, positionY, spriteType);

		this.originX = positionX;
		this.fallSpeed = fallSpeed;
		this.direction = Math.random() < 0.5 ? 1 : -1;
	}

	/**
	 * Advances the ship one frame, falling down and swinging sideways.
	 */
	public final void zigzagMove() {
		if (Math.abs(this.positionX + this.direction * LATERAL_SPEED
				- this.originX) > ZIGZAG_AMPLITUDE)
			this.direction = -this.direction;

		move(this.direction * LATERAL_SPEED, this.fallSpeed);
	}

	/**
	 * Getter for the amplitude of the zigzag.
	 *
	 * @return Sideways distance the ship covers from its center.
	 */
	public static int getZigzagAmplitude() {
		return ZIGZAG_AMPLITUDE;
	}
}
