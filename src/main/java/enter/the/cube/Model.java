package enter.the.cube;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLRunnable;

public final class Model {
	// State (internal) variables
	private final View view;

	// Model variables
	private Point2D.Double origin; // Current origin coords
	public Point2D.Double cursor = new Point2D.Double(350, 350); // Current cursor coords

	public Point3D playerLocation = new Point3D(); // location of player
	private int playerRadius; // size of player (radius)
	private List<Double[]> walls; // walls in maze
	public boolean skewed;
	public boolean viewWalls = true;
	private double stepSize = 5; // it's 5
	public Point3D lookPoint = new Point3D(); // relative to player location

	public Model(View view) {
		this.view = view;

		// Initialize user-adjustable variables (with reasonable default values)
		origin = new Point2D.Double(0.0, 0.0);
		playerLocation = new Point3D(350, 75, 10);
		walls = new ArrayList<Double[]>();
		playerRadius = 10;
	}

	public Point2D.Double getOrigin() {
		return new Point2D.Double(origin.x, origin.y);
	}

	public int getPlayerRadius() {
		return playerRadius;
	}

	public boolean freeLocation(double x, double y) {
		for (int i = 0; i < walls.size(); ++i) {
			if ((x >= walls.get(i)[0] && x <= walls.get(i)[1]) && (y >= walls.get(i)[2] && y <= walls.get(i)[3])) {
				return false;
			}
		}
		return true;
	}

	public void setOriginInSceneCoordinates(Point2D.Double q) {
		view.getCanvas().invoke(false, new BasicUpdater() {
			@Override
			public void update(GL2 gl) {
				origin = new Point2D.Double(q.x, q.y);
			}
		});
	}

	public void setOriginInViewCoordinates(Point q) {
		view.getCanvas().invoke(false, new ViewPointUpdater(q) {
			@Override
			public void update(double[] p) {
				origin = new Point2D.Double(p[0], p[1]);
			}
		});
	}

	public void setCursorInViewCoordinates(Point q) {
		view.getCanvas().invoke(false, new ViewPointUpdater(q) {
			@Override
			public void update(double[] p) {
				cursor.x = p[0];
				cursor.y = p[1];
			}
		});
	}

	public void movePlayer(double x, double y) {
		view.getCanvas().invoke(false, new BasicUpdater() {
			@Override
			public void update(GL2 gl) {
				playerLocation.x += x;
				playerLocation.y += y;
			}
		});
	}

	public void setPlayer(double x, double y) {
		view.getCanvas().invoke(false, new BasicUpdater() {
			@Override
			public void update(GL2 gl) {
				playerLocation.x = x;
				playerLocation.y = y;
			}
		});
	}

	public void addWall(double x, double y, double w, double h) {
		// left most x, right most x, bottom y, top y
		walls.add(new Double[] { x, x + w, y, y + h });
	}

	// Convenience class to simplify the implementation of most updaters.
	private abstract class BasicUpdater implements GLRunnable {
		@Override
		public final boolean run(GLAutoDrawable drawable) {
			GL2 gl = drawable.getGL().getGL2();
			update(gl);
			return true; // Let animator take care of updating the display
		}

		public abstract void update(GL2 gl);
	}

	// Convenience class to simplify updates in cases in which the input is a
	// single point in view coordinates (integers/pixels).
	private abstract class ViewPointUpdater extends BasicUpdater {
		private final Point q;

		public ViewPointUpdater(Point q) {
			this.q = q;
		}

		@Override
		public final void update(GL2 gl) {
			int h = view.getHeight();
			double[] p = Utilities.mapViewToScene(gl, q.x, h - q.y, 0.0);
			p[0] = p[0] * 2;
			p[1] = p[1] * 2 - 700;
			update(p);
		}

		public abstract void update(double[] p);
	}

	public void goLeft() {
		if (skewed) {
			// remember that the left perp of a vector <x,y> is <-y,x>
			if (freeLocation(playerLocation.y - lookPoint.y, playerLocation.x + lookPoint.x)) {
				movePlayer(-lookPoint.y, lookPoint.x);
				playerReachGoal(playerLocation.x - stepSize, playerLocation.y);
			}
		} else {
			if (freeLocation(playerLocation.x - playerRadius - stepSize, playerLocation.y - playerRadius)
					&& freeLocation(playerLocation.x - playerRadius - stepSize, playerLocation.y + playerRadius)) {
				movePlayer(-stepSize, 0);
				playerReachGoal(playerLocation.x - stepSize, playerLocation.y);
			}
		}
	}

	public void goRight() {
		if (skewed) {
			// remember that the right perp of a vector <x,y> is <y,-x>
			if (freeLocation(playerLocation.y + lookPoint.y, playerLocation.x - lookPoint.x)) {
				movePlayer(lookPoint.y, -lookPoint.x);
				playerReachGoal(playerLocation.x - stepSize, playerLocation.y);
			}
		} else {
			if (freeLocation(playerLocation.x + playerRadius + stepSize, playerLocation.y - playerRadius)
					&& freeLocation(playerLocation.x + playerRadius + stepSize, playerLocation.y + playerRadius)) {
				movePlayer(stepSize, 0);
				playerReachGoal(playerLocation.x + stepSize, playerLocation.y);
			}
		}
	}

	public void goForward() {
		if (skewed) {
			if (freeLocation(playerLocation.x + lookPoint.x, playerLocation.y + lookPoint.y)) {
				movePlayer(lookPoint.x, lookPoint.y);
				playerReachGoal(playerLocation.x - stepSize, playerLocation.y);
			}
		} else {
			if (freeLocation(playerLocation.x - playerRadius, playerLocation.y + playerRadius + stepSize)
					&& freeLocation(playerLocation.x + playerRadius, playerLocation.y + playerRadius + stepSize)) {
				movePlayer(0, stepSize);
				playerReachGoal(playerLocation.x, playerLocation.y + stepSize);
			}
		}
	}

	public void goBack() {
		if (skewed) {
			if (freeLocation(playerLocation.x + -lookPoint.x, playerLocation.y + -lookPoint.y)) {
				movePlayer(-lookPoint.x, -lookPoint.y);
				playerReachGoal(playerLocation.x - stepSize, playerLocation.y);
			}
		} else {
			if (freeLocation(playerLocation.x - playerRadius, playerLocation.y - playerRadius - stepSize)
					&& freeLocation(playerLocation.x + playerRadius, playerLocation.y - playerRadius - stepSize)) {
				movePlayer(0, -stepSize);
				playerReachGoal(playerLocation.x, playerLocation.y - stepSize);
			}
		}
	}

	public void playerReachGoal(double x, double y) {
		if (x >= 345 && x <= 355 && y >= 370 && y <= 380) {
			setPlayer(350, 75);
			viewWalls = !viewWalls;
		}
	}

	public void mouselook(Point point) {
		lookPoint.x = Math.cos((view.getWidth() - point.x) / (20 * Math.PI)) * stepSize;
		lookPoint.y = Math.sin((view.getWidth() - point.x) / (20 * Math.PI)) * stepSize;
		lookPoint.z = (5.0 - point.y / 60.0) * stepSize;
	}

	public void sprint() {
		stepSize *= 1.1;
		System.out.println(stepSize);
	}

	public void crouch() {
		stepSize /= 1.1;
		System.out.println(stepSize);
	}

	public void jump() {
		playerLocation.z+=5;
		//I'm not even going to stop you from multi-jumping. You do you.
		//Nor have I yet implemented gravity.
	}
}
