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
	public int level = 1;
	public final double g = 9.8; //there's no reason to make this earth gravity, but I did.
	public Point3D gravityVector = new Point3D();
	public Point3D up  = new Point3D(); //just a convenience for gravityVector.unit().multiply(-1))

	public Point3D floatingPlaneLocation = new Point3D(200,200,200);
	public Point3D cubeCubeLocation = new Point3D(1000,1000,1000);

	private Point3D level1Start = new Point3D(350, 75, 0);
	private Point3D level2Start = new Point3D(1200, 1200, 1200);

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

	public boolean freeLocation(double x, double y, double z) {
		if(level==1) {
			if(z<10) {return false;} //floor
			for (int i = 0; i < walls.size(); ++i) {
				if ((x >= walls.get(i)[0] && x <= walls.get(i)[1]) && (y >= walls.get(i)[2] && y <= walls.get(i)[3])) {
					return false;
				}
			}
			return true;
		} else {
			double xcorner = cubeCubeLocation.x;
			double ycorner = cubeCubeLocation.y;
			double zcorner = cubeCubeLocation.z;
			for (int[][] i : cubeCube) {
				for (int[] j : i) {
					for (int k : j) {
						if(k==1 && x >= xcorner && x <= xcorner+100 && y >= ycorner && y <= ycorner+100 && z >= zcorner && z <= zcorner+100) {
							return false;
						}
						xcorner+=100;
					}
					ycorner+=100;
					xcorner=cubeCubeLocation.x;
				}
				zcorner+=100;
				ycorner=cubeCubeLocation.y;
			}
			return true;
		}
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

	public boolean movePlayer(double x, double y, double z) {
		if(freeLocation(playerLocation.x + x, playerLocation.y + y, playerLocation.z+z)) {
			playerLocation.add(x,y,z);
			playerReachGoal(playerLocation.x, playerLocation.y);
			return true;
		} else {
			return false;
		}
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
			movePlayer(-lookPoint.y, lookPoint.x, 0);
		} else {
			movePlayer(-stepSize, 0, 0);
		}
	}

	public void goRight() {
		if (skewed) {
			// remember that the right perp of a vector <x,y> is <y,-x>
			movePlayer(lookPoint.y, -lookPoint.x, 0);
		} else {
			movePlayer(stepSize, 0, 0);
		}
	}

	public void goForward() {
		if (skewed) {
			movePlayer(lookPoint.x, lookPoint.y, 0);
		} else {
			movePlayer(0, stepSize, 0);
		}
	}

	public void goBack() {
		if (skewed) {
			movePlayer(-lookPoint.x, -lookPoint.y, 0);
		} else {
			movePlayer(0, -stepSize, 0);
		}
	}

	public void playerReachGoal(double x, double y) {
		if (level == 1 && x >= 345 && x <= 355 && y >= 370 && y <= 380) {
			playerLocation.set(level2Start);
			skewed = true;
			level = 2;
		} else {
			if (level == 2 && playerLocation.closeEnough(floatingPlaneLocation)) {
				playerLocation.set(level1Start);
				level = 1;
			}
		}
	}

	public void mouselook(Point mousepoint) {
		lookPoint.x = Math.cos((view.getWidth() - mousepoint.x) / (20 * Math.PI)) * stepSize;
		lookPoint.y = Math.sin((view.getWidth() - mousepoint.x) / (20 * Math.PI)) * stepSize;
		lookPoint.z = (5.0 - mousepoint.y / 60.0) * stepSize;
		if(level==2) {
			//we want to translate the lookpoint from gravity-dependent to world-coordinate
			lookPoint.divide(up);
		}
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
		playerLocation.z+=stepSize;
		//I'm not even going to stop you from multi-jumping. You do you.
	}

	public int[][][] cubeCube = {
		//@formatter:off
		{{1,1,1,1,1,1,1,1,1,1},
		 {1,1,1,1,1,1,1,1,1,1},
		 {1,1,1,1,1,1,1,1,1,1},
		 {1,1,1,1,1,1,1,1,1,1},
		 {1,1,1,1,1,1,1,1,1,1},
		 {1,1,1,1,1,1,1,1,1,1},
		 {1,1,1,1,1,1,1,1,1,1},
		 {1,1,1,1,1,1,1,1,1,1},
		 {1,1,1,1,1,1,1,1,1,1},
		 {1,1,1,1,1,1,1,1,1,1}},

		{{1,1,1,1,1,1,1,1,1,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,1,1,1,1,1,1,1,1,1}},

		{{1,1,1,1,1,1,1,1,1,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,1,1,1,1,1,1,1,1,1}},

		{{1,1,1,1,1,1,1,1,1,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,1,1,1,1,1,1,1,1,1}},

		{{1,1,1,1,1,1,1,1,1,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,1,1,1,1,1,1,1,1,1}},

		{{1,1,1,1,1,1,1,1,1,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,1,1,1,1,1,1,1,1,1}},

		{{1,1,1,1,1,1,1,1,1,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,1,1,1,1,1,1,1,1,1}},

		{{1,1,1,1,1,1,1,1,1,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,1,1,1,1,1,1,1,1,1}},

		{{1,1,1,1,1,1,1,1,1,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,1,1,1,1,1,1,1,1,1}},

		{{1,1,1,1,1,1,1,1,1,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,1,1,1,1,1,1,1,1,1}},

		{{1,1,1,1,1,1,1,1,1,1},
		 {1,1,1,1,1,1,1,1,1,1},
		 {1,1,1,1,1,1,1,1,1,1},
		 {1,1,1,1,1,1,1,1,1,1},
		 {1,1,1,1,1,1,1,1,1,1},
		 {1,1,1,1,1,1,1,1,1,1},
		 {1,1,1,1,1,1,1,1,1,1},
		 {1,1,1,1,1,1,1,1,1,1},
		 {1,1,1,1,1,1,1,1,1,1},
		 {1,1,1,1,1,1,1,1,1,1}},
		//@formatter:on
	};
}
