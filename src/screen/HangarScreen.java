package screen;

import java.awt.event.KeyEvent;

public class HangarScreen extends Screen {

    public HangarScreen(final int width, final int height, final int fps) {
        super(width, height, fps);
        this.returnCode = 1;
    }

    @Override
    protected void update() {
        super.update();

        drawManager.initDrawing(this);
        drawManager.drawCenteredBigString(this, "Hangar", height / 3);
        drawManager.drawCenteredRegularString(this,
                "Available ships and customization will be displayed here",
                height / 2);
        drawManager.drawCenteredRegularString(this,
                "Press Escape to return to Main Menu",
                height * 2 / 3);
        drawManager.completeDrawing(this);

        if (inputManager.isKeyDown(KeyEvent.VK_ESCAPE)
                && inputDelay.checkFinished()) {
            this.returnCode = 1;
            this.isRunning = false;
        }
    }

    @Override
    public int run() {
        super.run();
        return this.returnCode;
    }
}