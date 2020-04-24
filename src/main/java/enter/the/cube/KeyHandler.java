package enter.the.cube;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public final class KeyHandler extends KeyAdapter {
	private final Model model;

	public KeyHandler(View view, Model model) {
		this.model = model;
		Component component = view.getCanvas();
		component.addKeyListener(this);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_A:
			model.goLeft();
			break;
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_D:
			model.goRight();
			break;
		case KeyEvent.VK_UP:
		case KeyEvent.VK_W:
			model.goForward();
			break;
		case KeyEvent.VK_DOWN:
		case KeyEvent.VK_S:
			model.goBack();
			break;
		case KeyEvent.VK_TAB:
			// on tab switch between 2D and 3D. only available for plane maze
			if(model.level == 1) {
				model.skewed = !model.skewed;
				model.viewWalls = true;				
			}

			break;
		case KeyEvent.VK_C:
			// on c switch between colored and invisible walls. only available in 2D.
			if(!model.skewed)
				model.viewWalls = !model.viewWalls;
			break;
		case KeyEvent.VK_SHIFT:
			model.sprint();
			break;
		case KeyEvent.VK_CONTROL:
			model.crouch();
			break;
		case KeyEvent.VK_SPACE:
			model.jump();
			break;
		}
		model.setOriginInSceneCoordinates(model.getOrigin());
	}
}
