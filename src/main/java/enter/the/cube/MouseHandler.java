package enter.the.cube;

import java.awt.*;
import java.awt.event.*;

public final class MouseHandler extends MouseAdapter {
	private final Model model;

	public MouseHandler(View view, Model model) {
		this.model = model;

		Component component = view.getCanvas();

		component.addMouseListener(this);
		component.addMouseMotionListener(this);
		component.addMouseWheelListener(this);
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
		model.mouselook(e.getPoint());
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
	}
}
