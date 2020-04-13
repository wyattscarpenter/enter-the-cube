//******************************************************************************
// Copyright (C) 2019 University of Oklahoma Board of Trustees.
//******************************************************************************
// Last modified: Wed Feb 27 17:27:48 2019 by Chris Weaver
//******************************************************************************
// Major Modification History:
//
// 20190227 [weaver]:	Original file.
//
//******************************************************************************
//
// The model manages all of the user-adjustable variables utilized in the scene.
// (You can store non-user-adjustable scene data here too, if you want.)
//
// For each variable that you want to make interactive:
//
//   1. Add a member of the right type
//   2. Initialize it to a reasonable default value in the constructor.
//   3. Add a method to access a copy of the variable's current value.
//   4. Add a method to modify the variable.
//
// Concurrency management is important because the JOGL and the Java AWT run on
// different threads. The modify methods use the GLAutoDrawable.invoke() method
// so that all changes to variables take place on the JOGL thread. Because this
// happens at the END of GLEventListener.display(), all changes will be visible
// to the View.update() and render() methods in the next animation cycle.
//
//******************************************************************************

package edu.ou.cs.cg.project.maze2D;

//import java.lang.*;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.*;
import com.jogamp.opengl.*;
import edu.ou.cs.cg.utilities.Utilities;

//******************************************************************************

/**
 * The <CODE>Model</CODE> class.
 *
 * @author  Chris Weaver
 * @version %I%, %G%
 */
public final class Model
{
	//**********************************************************************
	// Private Members
	//**********************************************************************

	// State (internal) variables
	private final View					view;

	// Model variables
	private Point2D.Double				origin;			// Current origin coords
	public Point2D.Double				cursor = new Point2D.Double(0, 0);			// Current cursor coords
	
	public Point2D.Double				playerLocation;	// location of player
	private int							playerRadius;		// size of player (radius)
	private List<Double[]> 				walls;			// walls in maze
	public boolean skewed;
	public boolean viewWalls;

	//**********************************************************************
	// Constructors and Finalizer
	//**********************************************************************

	public Model(View view)
	{
		this.view = view;

		// Initialize user-adjustable variables (with reasonable default values)
		origin = new Point2D.Double(0.0, 0.0);
		playerLocation = new Point2D.Double(350, 75);
		walls = new ArrayList<Double[]>();
		playerRadius = 10;
		

	}

	//**********************************************************************
	// Public Methods (Access Variables)
	//**********************************************************************

	public Point2D.Double	getOrigin()
	{
		return new Point2D.Double(origin.x, origin.y);
	}

	public Point2D.Double 	getPlayerLocation()
	{
		return playerLocation;
	}
	
	public int getPlayerRadius()
	{
		return playerRadius;
	}
	
	public boolean freeLocation(double x, double y) 
	{
		for(int i = 0; i < walls.size(); ++i)
		{
			if((x >= walls.get(i)[0] && x <= walls.get(i)[1])
					&& (y >= walls.get(i)[2] && y <= walls.get(i)[3])) {
				return false;
			}
		}
		return true;
	}



	//**********************************************************************
	// Public Methods (Modify Variables)
	//**********************************************************************

	public void	setOriginInSceneCoordinates(Point2D.Double q)
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				origin = new Point2D.Double(q.x, q.y);
			}
		});;
	}

	public void	setOriginInViewCoordinates(Point q)
	{
		view.getCanvas().invoke(false, new ViewPointUpdater(q) {
			public void	update(double[] p) {
				origin = new Point2D.Double(p[0], p[1]);
			}
		});;
	}

	public void	setCursorInViewCoordinates(Point q)
	{
		view.getCanvas().invoke(false, new ViewPointUpdater(q) {
			public void	update(double[] p) {
				cursor.x = p[0];
				cursor.y = p[1];
			}
		});;
	}
	
	public void movePlayer(double x, double y) 
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void update(GL2 gl) {
				playerLocation.x += x;
				playerLocation.y += y;
			}
		});;
	}
	
	public void setPlayer(double x, double y)
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void update(GL2 gl) {
				playerLocation.x = x;
				playerLocation.y = y;
			}
		});;
	
		
	}
	
	public void addWall(double x, double y, double w, double h) 
	{
		// left most x, right most x, bottom y, top y
		walls.add(new Double[]{x, x + w, y, y + h});
	}
	

	//**********************************************************************
	// Inner Classes
	//**********************************************************************

	// Convenience class to simplify the implementation of most updaters.
	private abstract class BasicUpdater implements GLRunnable
	{
		public final boolean	run(GLAutoDrawable drawable)
		{
			GL2	gl = drawable.getGL().getGL2();

			update(gl);

			return true;	// Let animator take care of updating the display
		}

		public abstract void	update(GL2 gl);
	}

	// Convenience class to simplify updates in cases in which the input is a
	// single point in view coordinates (integers/pixels).
	private abstract class ViewPointUpdater extends BasicUpdater
	{
		private final Point	q;

		public ViewPointUpdater(Point q)
		{
			this.q = q;
		}

		public final void	update(GL2 gl)
		{
			int		h = view.getHeight();
			double[]	p = Utilities.mapViewToScene(gl, q.x, h - q.y, 0.0);
			p[0] = p[0] * 2;
			p[1] = p[1] * 2 - 700;
			update(p);
		}

		public abstract void	update(double[] p);
	}
}

//******************************************************************************
