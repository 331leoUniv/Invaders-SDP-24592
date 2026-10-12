package screen;

import java.awt.event.KeyEvent;

import engine.Cooldown;
import engine.Core;

public class DifficultyScreen extends Screen { //Implementation of the difficulty selection screen.

	
	private static final int SELECTION_TIME = 200;
	private static final int NUM_CHOICES = 3; //Number of difficulty choices (easy, normal, hard).

	private int selected;
	private Cooldown selectionCooldown;


	public DifficultyScreen(final int width, final int height, final int fps) {
		super(width, height, fps);
		this.selected = 0; // Defaults to "Easy".
		this.returnCode = 2; // Defaults to "Play" 
		this.selectionCooldown = Core.getCooldown(SELECTION_TIME);
		this.selectionCooldown.reset();
	}

	public final int run() {
		super.run();

		return this.returnCode;
	}

	protected final void update() {
		super.update();

		draw();
		if (this.selectionCooldown.checkFinished()
				&& this.inputDelay.checkFinished()) {
			if (inputManager.isKeyDown(KeyEvent.VK_UP)
					|| inputManager.isKeyDown(KeyEvent.VK_W)) {
				this.selected = this.selected == 0 ? NUM_CHOICES - 1
						: this.selected - 1;
				this.selectionCooldown.reset();
			}
			if (inputManager.isKeyDown(KeyEvent.VK_DOWN)
					|| inputManager.isKeyDown(KeyEvent.VK_S)) {
				this.selected = (this.selected + 1) % NUM_CHOICES;
				this.selectionCooldown.reset();
			}
			if (inputManager.isKeyDown(KeyEvent.VK_SPACE)) {
				this.returnCode = 2;
				this.isRunning = false;
			}
			if (inputManager.isKeyDown(KeyEvent.VK_ESCAPE)) {
				this.returnCode = 1;
				this.isRunning = false;
			}
		}
	}

	
	public final int getSelectedDifficulty() {
		return this.selected;
	}

	private void draw() {
		drawManager.initDrawing(this);

		drawManager.drawDifficultyMenu(this, this.selected);

		drawManager.completeDrawing(this);
	}
}