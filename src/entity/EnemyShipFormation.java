package entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import screen.Screen;
import level.WaveDefinition;
import level.EnemyCell;
import engine.Cooldown;
import engine.Core;
import engine.DrawManager;
import engine.DrawManager.SpriteType;
import engine.GameSettings;

/**
 * Groups enemy ships into a formation that moves together.
 * 
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 * 
 */
public class EnemyShipFormation implements Iterable<EnemyShip> {

	/** Distance between ships. */
	private static final int SEPARATION_DISTANCE = 40;
	/** Proportion of C-type ships. */
	private static final double PROPORTION_C = 0.2;
	/** Proportion of B-type ships. */
	private static final double PROPORTION_B = 0.4;
	/** Lateral speed of the formation. */
	private static final int X_SPEED = 8;
	/** Downwards speed of the formation. */
	private static final int Y_SPEED = 4;
	/** Speed of the bullets shot by the members. */
	private static final int BULLET_SPEED = 4;
	/** Proportion of differences between shooting times. */
	private static final double SHOOTING_VARIANCE = .2;
	/** Margin on the sides of the screen. */
	private static final int SIDE_MARGIN = 20;
	/** Margin on the bottom of the screen. */
	private static final int BOTTOM_MARGIN = 80;
	/** Distance to go down each pass. */
	private static final int DESCENT_DISTANCE = 20;
	/** Minimum speed allowed. */
	private static final int MINIMUM_SPEED = 10;

	/** DrawManager instance. */
	private DrawManager drawManager;
	/** Application logger. */
	private Logger logger;
	/** Screen to draw ships on. */
	private Screen screen;

	/** List of enemy ships forming the formation. */
	private List<List<EnemyShip>> enemyShips;
	/** Minimum time between shots. */
	private Cooldown shootingCooldown;
	/** Number of ships in the formation - horizontally. */
	private int nShipsWide;
	/** Number of ships in the formation - vertically. */
	private int nShipsHigh;
	/** Time between shots. */
	private int shootingInterval;
	/** Variance in the time between shots. */
	private int shootingVariance;
	/** Initial ship speed. */
	private int baseSpeed;
	/** Speed of the ships. */
	private int movementSpeed;
	/** Current direction the formation is moving on. */
	private Direction currentDirection;
	/** Direction the formation was moving previously. */
	private Direction previousDirection;
	/** Interval between movements, in frames. */
	private int movementInterval;
	/** Total width of the formation. */
	private int width;
	/** Total height of the formation. */
	private int height;
	/** Position in the x-axis of the upper left corner of the formation. */
	private int positionX;
	/** Position in the y-axis of the upper left corner of the formation. */
	private int positionY;
	/** Width of one ship. */
	private int shipWidth;
	/** Height of one ship. */
	private int shipHeight;
	/** List of ships that are able to shoot. */
	private List<EnemyShip> shooters;
	/** Number of not destroyed ships. */
	private int shipCount;
	/** Actual occupied cells at the start, independent of grid capacity. */
	private int initialShipCount;

	/** Directions the formation can move. */
	private enum Direction {
		/** Movement to the right side of the screen. */
		RIGHT,
		/** Movement to the left side of the screen. */
		LEFT,
		/** Movement to the bottom of the screen. */
		DOWN
	};

	/**
	 * Constructor, sets the initial conditions.
	 * 
	 * @param gameSettings
	 *            Current game settings.
	 * @throws IllegalArgumentException If the initial position is outside the
	 *            left/top bounds or is not aligned to the descent distance.
	 */
	public EnemyShipFormation(final GameSettings gameSettings) {
		this(gameSettings, null);
	}

	/** Uses the full grid while taking runtime timing from adjusted settings. */
	public EnemyShipFormation(final GameSettings gameSettings, final WaveDefinition wave) {
		if (wave != null && (wave.getRows() != gameSettings.getFormationHeight()
				|| wave.getColumns() != gameSettings.getFormationWidth()
				|| wave.getInitialX() != gameSettings.getInitialX()
				|| wave.getInitialY() != gameSettings.getInitialY()))
			throw new IllegalArgumentException("Wave grid and runtime settings must share geometry");
		this.drawManager = Core.getDrawManager();
		this.logger = Core.getLogger();
		this.enemyShips = new ArrayList<List<EnemyShip>>();
		this.currentDirection = Direction.RIGHT;
		this.movementInterval = 0;
		this.nShipsWide = gameSettings.getFormationWidth();
		this.nShipsHigh = gameSettings.getFormationHeight();
		this.shootingInterval = gameSettings.getShootingFrecuency();
		this.shootingVariance = (int) (gameSettings.getShootingFrecuency()
				* SHOOTING_VARIANCE);
		this.baseSpeed = gameSettings.getBaseSpeed();
		this.movementSpeed = this.baseSpeed;
		this.positionX = gameSettings.getInitialX();
		this.positionY = gameSettings.getInitialY();
		if (this.positionX < SIDE_MARGIN || this.positionY < 0
				|| this.positionY % DESCENT_DISTANCE != 0)
			throw new IllegalArgumentException("Initial formation position must "
					+ "have X >= " + SIDE_MARGIN + " and non-negative Y divisible by "
					+ DESCENT_DISTANCE + ": (" + positionX + "," + positionY + ")");
		this.shooters = new ArrayList<EnemyShip>();
		SpriteType spriteType;

		this.logger.info("Initializing " + nShipsWide + "x" + nShipsHigh
				+ " ship formation in (" + positionX + "," + positionY + ")");

		// Each sub-list is a column on the formation.
		for (int i = 0; i < this.nShipsWide; i++)
			this.enemyShips.add(new ArrayList<EnemyShip>());

		for (int columnIndex = 0; columnIndex < this.nShipsWide; columnIndex++) {
			List<EnemyShip> column = this.enemyShips.get(columnIndex);
			for (int row = 0; row < this.nShipsHigh; row++) {
				int health = 1;
				if (wave != null) {
					EnemyCell cell = wave.getCell(row, columnIndex);
					if (cell == null)
						continue;
					spriteType = spriteFor(cell.getEnemyType());
					health = cell.getHealth();
				} else if (row / (float) this.nShipsHigh < PROPORTION_C) {
					spriteType = SpriteType.EnemyShipC1;
				} else if (row / (float) this.nShipsHigh < PROPORTION_B + PROPORTION_C) {
					spriteType = SpriteType.EnemyShipB1;
				} else {
					spriteType = SpriteType.EnemyShipA1;
				}
				column.add(new EnemyShip(positionX + SEPARATION_DISTANCE * columnIndex,
						positionY + SEPARATION_DISTANCE * row, spriteType, health));
				this.shipCount++;
			}
		}
		this.initialShipCount = this.shipCount;
		this.shipWidth = 24;
		this.shipHeight = 16;

		this.width = (this.nShipsWide - 1) * SEPARATION_DISTANCE
				+ this.shipWidth;
		this.height = (this.nShipsHigh - 1) * SEPARATION_DISTANCE
				+ this.shipHeight;

		for (List<EnemyShip> column : this.enemyShips)
			if (!column.isEmpty())
				this.shooters.add(column.get(column.size() - 1));
	}

	/**
	 * Associates the formation to a given screen.
	 * 
	 * @param newScreen
	 *            Screen to attach.
	 * @throws IllegalArgumentException If the formation exceeds the screen's
	 *            right or bottom margin.
	 */
	public final void attach(final Screen newScreen) {
		if ((long) positionX + this.width > newScreen.getWidth() - SIDE_MARGIN
				|| (long) positionY + this.height
						> newScreen.getHeight() - BOTTOM_MARGIN)
			throw new IllegalArgumentException("Formation does not fit within "
					+ "the screen margins: (" + positionX + "," + positionY + "), "
					+ this.width + "x" + this.height + " on "
					+ newScreen.getWidth() + "x" + newScreen.getHeight());
		screen = newScreen;
	}

	/**
	 * Draws every individual component of the formation.
	 */
	public final void draw() {
		for (List<EnemyShip> column : this.enemyShips)
			for (EnemyShip enemyShip : column)
				drawManager.drawEntity(enemyShip, enemyShip.getPositionX(),
						enemyShip.getPositionY());
	}

	/**
	 * Updates the position of the ships.
	 */
	public final void update() {
		if(this.shootingCooldown == null) {
			this.shootingCooldown = Core.getVariableCooldown(shootingInterval,
					shootingVariance);
			this.shootingCooldown.reset();
		}
		
		if (isEmpty())
			return;
		cleanUp();

		int movementX = 0;
		int movementY = 0;
		double remainingProportion = (double) this.shipCount
				/ this.initialShipCount;
		this.movementSpeed = (int) (Math.pow(remainingProportion, 2)
				* this.baseSpeed);
		this.movementSpeed += MINIMUM_SPEED;
		
		movementInterval++;
		if (movementInterval >= this.movementSpeed) {
			movementInterval = 0;

			boolean isAtBottom = positionY
					+ this.height > screen.getHeight() - BOTTOM_MARGIN;
			boolean isAtRightSide = positionX
					+ this.width >= screen.getWidth() - SIDE_MARGIN;
			boolean isAtLeftSide = positionX <= SIDE_MARGIN;
			boolean isAtHorizontalAltitude = positionY % DESCENT_DISTANCE == 0;

			if (currentDirection == Direction.DOWN) {
				if (isAtHorizontalAltitude)
					if (previousDirection == Direction.RIGHT) {
						currentDirection = Direction.LEFT;
						this.logger.info("Formation now moving left 1");
					} else {
						currentDirection = Direction.RIGHT;
						this.logger.info("Formation now moving right 2");
					}
			} else if (currentDirection == Direction.LEFT) {
				if (isAtLeftSide)
					if (!isAtBottom) {
						previousDirection = currentDirection;
						currentDirection = Direction.DOWN;
						this.logger.info("Formation now moving down 3");
					} else {
						currentDirection = Direction.RIGHT;
						this.logger.info("Formation now moving right 4");
					}
			} else {
				if (isAtRightSide)
					if (!isAtBottom) {
						previousDirection = currentDirection;
						currentDirection = Direction.DOWN;
						this.logger.info("Formation now moving down 5");
					} else {
						currentDirection = Direction.LEFT;
						this.logger.info("Formation now moving left 6");
					}
			}

			if (currentDirection == Direction.RIGHT)
				movementX = X_SPEED;
			else if (currentDirection == Direction.LEFT)
				movementX = -X_SPEED;
			else
				movementY = Y_SPEED;

			positionX += movementX;
			positionY += movementY;

			// Cleans explosions.
			List<EnemyShip> destroyed;
			for (List<EnemyShip> column : this.enemyShips) {
				destroyed = new ArrayList<EnemyShip>();
				for (EnemyShip ship : column) {
					if (ship != null && ship.isDestroyed()) {
						destroyed.add(ship);
						this.logger.info("Removed enemy "
								+ column.indexOf(ship) + " from column "
								+ this.enemyShips.indexOf(column));
					}
				}
				column.removeAll(destroyed);
			}

			for (List<EnemyShip> column : this.enemyShips)
				for (EnemyShip enemyShip : column) {
					enemyShip.move(movementX, movementY);
					enemyShip.update();
				}
		}
	}

	/**
	 * Cleans empty columns, adjusts the width and height of the formation.
	 */
	private void cleanUp() {
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		Iterator<List<EnemyShip>> columns = this.enemyShips.iterator();
		while (columns.hasNext()) {
			List<EnemyShip> column = columns.next();
			if (column.isEmpty()) {
				columns.remove();
				continue;
			}
			for (EnemyShip enemy : column) {
				if (enemy.isDestroyed())
					continue;
				minX = Math.min(minX, enemy.getPositionX());
				minY = Math.min(minY, enemy.getPositionY());
				maxX = Math.max(maxX, enemy.getPositionX() + enemy.getWidth());
				maxY = Math.max(maxY, enemy.getPositionY() + enemy.getHeight());
			}
		}
		if (minX != Integer.MAX_VALUE) {
			this.positionX = minX;
			this.positionY = minY;
			this.width = maxX - minX;
			this.height = maxY - minY;
		}
	}

	/**
	 * Shoots a bullet downwards.
	 * 
	 * @param bullets
	 *            Bullets set to add the bullet being shot.
	 */
	public final void shoot(final Set<Bullet> bullets) {
		// Only the lowest surviving enemy in each occupied column can shoot.
		if (this.shooters.isEmpty())
			return;
		if (this.shootingCooldown == null) {
			this.shootingCooldown = Core.getVariableCooldown(shootingInterval, shootingVariance);
			this.shootingCooldown.reset();
		}
		int index = (int) (Math.random() * this.shooters.size());
		EnemyShip shooter = this.shooters.get(index);

		if (this.shootingCooldown.checkFinished()) {
			this.shootingCooldown.reset();
			bullets.add(BulletPool.getBullet(shooter.getPositionX()
					+ shooter.width / 2, shooter.getPositionY(), BULLET_SPEED));
		}
	}

	/**
	 * Destroys a ship.
	 * 
	 * @param destroyedShip
	 *            Ship to be destroyed.
	 */
	public final void destroy(final EnemyShip destroyedShip) {
		if (destroyedShip == null || destroyedShip.isDestroyed() || !contains(destroyedShip))
			return;
		for (List<EnemyShip> column : this.enemyShips)
			for (int i = 0; i < column.size(); i++)
				if (column.get(i).equals(destroyedShip)) {
					column.get(i).destroy();
					this.logger.info("Destroyed ship in ("
							+ this.enemyShips.indexOf(column) + "," + i + ")");
				}

		// Updates the list of ships that can shoot the player.
		if (this.shooters.contains(destroyedShip)) {
			int destroyedShipIndex = this.shooters.indexOf(destroyedShip);
			int destroyedShipColumnIndex = -1;

			for (List<EnemyShip> column : this.enemyShips)
				if (column.contains(destroyedShip)) {
					destroyedShipColumnIndex = this.enemyShips.indexOf(column);
					break;
				}

			EnemyShip nextShooter = getNextShooter(this.enemyShips
					.get(destroyedShipColumnIndex));

			if (nextShooter != null)
				this.shooters.set(destroyedShipIndex, nextShooter);
			else {
				this.shooters.remove(destroyedShipIndex);
				this.logger.info("Shooters list reduced to "
						+ this.shooters.size() + " members.");
			}
		}

		this.shipCount--;
	}

	/** One bullet damages once; kill counting and shooter promotion happen on death only. */
	public final boolean hit(final EnemyShip enemy) {
		if (enemy == null || enemy.isDestroyed() || !contains(enemy))
			return false;
		if (enemy.getHealth() > 1) {
			enemy.hit();
			return false;
		}
		destroy(enemy);
		return true;
	}

	private boolean contains(final EnemyShip enemy) {
		for (List<EnemyShip> column : this.enemyShips)
			if (column.contains(enemy))
				return true;
		return false;
	}

	/** Runtime adapter for the enemy families currently implemented in this checkout. */
	public static SpriteType spriteFor(final String identifier) {
		if ("A".equals(identifier)) return SpriteType.EnemyShipA1;
		if ("B".equals(identifier)) return SpriteType.EnemyShipB1;
		if ("C".equals(identifier)) return SpriteType.EnemyShipC1;
		throw new IllegalArgumentException("Unsupported gameplay enemy identifier: " + identifier);
	}

	/** Validates full grid placement, including empty leading/trailing rows and columns. */
	public static void validateWave(final WaveDefinition wave, final int screenWidth,
			final int screenHeight) {
		if (wave.getInitialX() < SIDE_MARGIN || wave.getInitialY() < 40
				|| wave.getInitialY() % DESCENT_DISTANCE != 0)
			throw new IllegalArgumentException("Formation requires X >= 20 and Y >= 40 aligned to 20 pixels");
		long width = (wave.getColumns() - 1L) * SEPARATION_DISTANCE + 24;
		long height = (wave.getRows() - 1L) * SEPARATION_DISTANCE + 16;
		if (wave.getInitialX() + width > screenWidth - SIDE_MARGIN
				|| wave.getInitialY() + height > screenHeight - BOTTOM_MARGIN)
			throw new IllegalArgumentException("Formation grid exceeds screen margins");
		for (int row = 0; row < wave.getRows(); row++)
			for (int column = 0; column < wave.getColumns(); column++) {
				EnemyCell cell = wave.getCell(row, column);
				if (cell != null) spriteFor(cell.getEnemyType());
			}
	}

	/**
	 * Gets the ship on a given column that will be in charge of shooting.
	 * 
	 * @param column
	 *            Column to search.
	 * @return New shooter ship.
	 */
	public final EnemyShip getNextShooter(final List<EnemyShip> column) {
		Iterator<EnemyShip> iterator = column.iterator();
		EnemyShip nextShooter = null;
		while (iterator.hasNext()) {
			EnemyShip checkShip = iterator.next();
			if (checkShip != null && !checkShip.isDestroyed())
				nextShooter = checkShip;
		}

		return nextShooter;
	}

	/**
	 * Returns an iterator over the ships in the formation.
	 * 
	 * @return Iterator over the enemy ships.
	 */
	@Override
	public final Iterator<EnemyShip> iterator() {
		Set<EnemyShip> enemyShipsList = new HashSet<EnemyShip>();

		for (List<EnemyShip> column : this.enemyShips)
			for (EnemyShip enemyShip : column)
				enemyShipsList.add(enemyShip);

		return enemyShipsList.iterator();
	}

	/**
	 * Checks if there are any ships remaining.
	 * 
	 * @return True when all ships have been destroyed.
	 */
	public final boolean isEmpty() {
		return this.shipCount <= 0;
	}
}
