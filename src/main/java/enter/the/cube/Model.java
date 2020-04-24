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

	public Point3D floatingPlaneLocation = new Point3D(1800,1800,1800);
	public Point3D floatingCubeLocation = new Point3D(345, 370,10);
	public Point3D cubeCubeLocation = new Point3D(1000,1000,1000);

	private Point3D level1Start = new Point3D(350, 75, 0);
	private Point3D level2Start = new Point3D(1200, 1200, 1200);

	public double masterWallSpin = 0;

	public Point3D cylinderLookPoint = new Point3D();
	private Point3D playerPlaneLookPoint  = new Point3D();
	
	private int flashlightColor = 0; 

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
			if(z<10) {gravityVector.set(gravityVector.unit()); return false;} //floor
			if(x >= 557.5 && x <= 557.5+20 && y >= 385 && y <= 385+52.5) { //in special wall
				if (masterWallSpin != 0) {
					return true;
				} else {
					masterWallSpin = 1;
					return false;
				}
			}
			for (int i = 0; i < walls.size(); ++i) {
				if ((x >= walls.get(i)[0] && x <= walls.get(i)[1]) && (y >= walls.get(i)[2] && y <= walls.get(i)[3])) {
					gravityVector.set(gravityVector.unit()); return false;
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
							gravityVector.set(gravityVector.unit()); return false;
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
	
	public float[] getFlashlightColor() {
		if(flashlightColor == 0) {
			return new float[] {1f,1f,1f};
		} else if(flashlightColor == 1) {
			return new float[] {0f,0f,1f};
		} else {
			return null;
		}
	}
	
	public int getFlashlightCount() {
		return flashlightColor;
	}
	
	public void nextFlashlightColor() {
		flashlightColor = (flashlightColor + 1) % 3;
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
			Point3D l = playerPlaneLookPoint.leftPerp2D();
			movePlayer(l.x, l.y, l.z);
		} else {
			movePlayer(-stepSize, 0, 0);
		}
	}

	public void goRight() {
		if (skewed) {
			Point3D r = playerPlaneLookPoint.rightPerp2D();
			movePlayer(r.x, r.y, r.z);
		} else {
			movePlayer(stepSize, 0, 0);
		}
	}

	public void goForward() {
		if (skewed) {
			movePlayer(playerPlaneLookPoint.x, playerPlaneLookPoint.y, playerPlaneLookPoint.z);
		} else {
			movePlayer(0, stepSize, 0);
		}
	}

	public void goBack() {
		if (skewed) {
			movePlayer(-playerPlaneLookPoint.x, -playerPlaneLookPoint.y, playerPlaneLookPoint.z);
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
		cylinderLookPoint.x = Math.cos((view.getWidth() - mousepoint.x) / (20 * Math.PI)) * stepSize;
		cylinderLookPoint.y = Math.sin((view.getWidth() - mousepoint.x) / (20 * Math.PI)) * stepSize;
		cylinderLookPoint.z = (5.0 - mousepoint.y / 60.0) * stepSize;
			//we want to translate the lookpoint from gravity-dependent to world-coordinate
			//first, get two arbitrary orthogonal vectors
			Point3D n = new Point3D(up);
			Point3D u = up.perp();
			Point3D v = up.cross(u);
			lookPoint.set(u.multiply(cylinderLookPoint.x).add(v.multiply(cylinderLookPoint.y)).add(n.multiply(cylinderLookPoint.z)));
			Point3D u2 = up.perp();
			Point3D v2 = up.cross(u2);
			playerPlaneLookPoint.set(u2.multiply(cylinderLookPoint.x).add(v2.multiply(cylinderLookPoint.y)).add(up)); //have to go up a lil bit for slopes?
			//System.out.println(lookPoint);
	}

	public void sprint() {
		stepSize *= 1.1;
		//System.out.println(stepSize);
	}

	public void crouch() {
		stepSize /= 1.1;
		//System.out.println(stepSize);
	}

	public void jump() {
		Point3D jumpUp = new Point3D(up);
		jumpUp.multiply(10);
		movePlayer(jumpUp.x, jumpUp.y, jumpUp.z);
		//I'm not even going to stop you from multi-jumping. You do you.
		//Update: turns out physics prevents you from multijumping, since the gravity mounts unavoidably.
		//System.out.println("up: "+up+" perp: "+up.perp());
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
		 {1,0,0,0,1,0,1,1,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,1,1,0,1,0,1,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,1,0,1,1,0,0,0,1},
		 {1,0,1,0,0,0,0,1,0,1},
		 {1,0,0,0,0,0,0,1,0,1},
		 {1,1,1,1,1,1,1,1,1,1}},

		{{1,1,1,1,1,1,1,1,1,1},
		 {1,0,0,0,0,0,0,0,1,1},
		 {1,0,0,0,0,0,0,0,1,1},
		 {1,0,0,0,0,0,0,0,1,1},
		 {1,0,0,0,0,0,0,0,1,1},
		 {1,0,0,0,0,0,0,0,1,1},
		 {1,0,0,0,0,0,0,0,1,1},
		 {1,0,0,0,0,0,0,0,1,1},
		 {1,0,0,0,0,0,0,0,1,1},
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
		 {1,0,0,0,0,0,0,0,1,1},
		 {1,1,0,0,0,0,0,0,1,1},
		 {1,0,0,0,0,0,0,0,1,1},
		 {1,0,0,0,0,0,0,0,1,1},
		 {1,0,0,0,0,0,0,0,1,1},
		 {1,0,0,0,0,0,0,0,1,1},
		 {1,1,0,0,0,0,0,0,1,1},
		 {1,0,0,0,0,0,0,0,1,1},
		 {1,1,1,1,1,1,1,1,1,1}},

		{{1,1,1,1,1,1,1,1,1,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,1,0,0,0,0,0,0,0,1},
		 {1,1,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,1,0,0,0,0,0,0,0,1},
		 {1,1,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,1,1,1,1,1,1,1,1,1}},

		{{1,1,1,1,1,1,1,1,1,1},
		 {1,0,0,0,0,0,0,0,1,1},
		 {1,0,0,0,0,0,0,0,1,1},
		 {1,1,0,0,0,0,0,0,1,1},
		 {1,1,0,0,0,0,0,0,1,1},
		 {1,1,0,0,0,0,0,0,1,1},
		 {1,0,0,0,0,0,0,0,1,1},
		 {1,0,0,0,0,0,0,0,1,1},
		 {1,0,0,0,0,0,0,0,1,1},
		 {1,1,1,1,1,1,1,1,1,1}},

		{{1,1,1,1,1,1,1,1,1,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,1,0,0,0,0,0,0,0,1},
		 {1,1,0,0,0,0,0,0,0,1},
		 {1,1,1,1,1,1,1,1,1,1}},

		{{1,1,1,1,1,1,1,1,1,1},
		 {1,0,0,0,0,0,0,0,1,1},
		 {1,1,0,0,0,0,0,0,1,1},
		 {1,1,0,0,0,0,0,0,1,1},
		 {1,1,0,0,0,0,0,0,1,1},
		 {1,1,0,0,0,0,0,0,1,1},
		 {1,1,0,0,0,0,0,0,1,1},
		 {1,1,0,0,0,0,0,0,1,1},
		 {1,0,0,0,0,0,0,0,1,1},
		 {1,1,1,1,1,1,1,1,1,1}},

		{{1,1,1,1,1,1,1,1,1,1},
		 {1,0,0,0,0,0,0,0,0,1},
		 {1,0,1,1,1,1,1,1,0,1},
		 {1,0,1,0,0,0,0,0,0,1},
		 {1,0,1,1,1,1,1,1,0,1},
		 {1,0,0,0,0,0,0,1,0,1},
		 {1,0,0,1,1,1,0,1,0,1},
		 {1,1,1,1,0,1,1,1,0,1},
		 {1,0,0,1,0,0,0,0,0,1},
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
