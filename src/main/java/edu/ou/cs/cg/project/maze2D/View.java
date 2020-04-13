//******************************************************************************
// Copyright (C) 2016-2019 University of Oklahoma Board of Trustees.
//******************************************************************************
// Last modified: Wed Feb 27 17:34:02 2019 by Chris Weaver
//******************************************************************************
// Major Modification History:
//
// 20160209 [weaver]:	Original file.
// 20190203 [weaver]:	Updated to JOGL 2.3.2 and cleaned up.
// 20190227 [weaver]:	Updated to use model and asynchronous event handling.
//
//******************************************************************************
// Notes:
//
//******************************************************************************

package edu.ou.cs.cg.project.maze2D;

//import java.lang.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.List;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.*;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextRenderer;
import edu.ou.cs.cg.utilities.Utilities;

//******************************************************************************

/**
 * The <CODE>View</CODE> class.<P>
 *
 * @author  Chris Weaver
 * @version %I%, %G%
 */
public final class View
	implements GLEventListener
{
	//**********************************************************************
	// Private Class Members
	//**********************************************************************

	private static final int			DEFAULT_FRAMES_PER_SECOND = 60;
	private static final DecimalFormat	FORMAT = new DecimalFormat("0.000");

	//**********************************************************************
	// Private Members
	//**********************************************************************

	// State (internal) variables
	private final GLJPanel				canvas;
	private int						w;			// Canvas width
	private int						h;			// Canvas height

	private TextRenderer				renderer;

	private final FPSAnimator			animator;
	private int						counter;	// Frame counter

	private final Model				model;

	private final KeyHandler			keyHandler;
	public final MouseHandler			mouseHandler;

	//**********************************************************************
	// Constructors and Finalizer
	//**********************************************************************

	public View(GLJPanel canvas)
	{
		this.canvas = canvas;

		// Initialize rendering
		counter = 0;
		canvas.addGLEventListener(this);

		// Initialize model (scene data and parameter manager)
		model = new Model(this);

		// Initialize controller (interaction handlers)
		keyHandler = new KeyHandler(this, model);
		mouseHandler = new MouseHandler(this, model);

		// Initialize animation
		animator = new FPSAnimator(canvas, DEFAULT_FRAMES_PER_SECOND);
		animator.start();
	}

	//**********************************************************************
	// Getters and Setters
	//**********************************************************************

	public GLJPanel	getCanvas()
	{
		return canvas;
	}

	public int	getWidth()
	{
		return w;
	}

	public int	getHeight()
	{
		return h;
	}

	//**********************************************************************
	// Override Methods (GLEventListener)
	//**********************************************************************

	public void	init(GLAutoDrawable drawable)
	{
		w = drawable.getSurfaceWidth();
		h = drawable.getSurfaceHeight();

		renderer = new TextRenderer(new Font("Monospaced", Font.PLAIN, 12),
									true, true);

		initPipeline(drawable);
	}

	public void	dispose(GLAutoDrawable drawable)
	{
		renderer = null;
	}

	public void	display(GLAutoDrawable drawable)
	{
		//updatePipeline(drawable);

		update(drawable);
		render(drawable);
	}

	public void	reshape(GLAutoDrawable drawable, int x, int y, int w, int h)
	{
		this.w = w;
		this.h = h;
	}

	//**********************************************************************
	// Private Methods (Rendering)
	//**********************************************************************

	private void	update(GLAutoDrawable drawable)
	{
		counter++;									// Advance animation counter
	}

	private void	render(GLAutoDrawable drawable)
	{
		GL2	gl = drawable.getGL().getGL2();
		if(model.viewWalls)
			gl.glClear(GL.GL_COLOR_BUFFER_BIT);		// Clear the buffer
		
		setProjection(gl);
		drawWalls(gl);
		drawGoal(gl, 350, 375);
		drawPlayer(gl);
		//drawAxes(gl); //ruins program

		// Draw the scene
		drawMode(drawable);						// Draw mode text

		gl.glFlush();								// Finish and display
	}

	private void drawAxes(GL2 gl) {
        gl.glBegin(GL2.GL_LINE);
        gl.glColor3f(255, 0, 0);
		gl.glVertex2d(0, 0);
		gl.glVertex2d(1000, 0);
		gl.glColor3f(0, 255, 0);
		gl.glVertex2d(0, 0);
		gl.glVertex2d(0, 1000);
		gl.glEnd();
	}

	//**********************************************************************
	// Private Methods (Pipeline)
	//**********************************************************************

	private void	initPipeline(GLAutoDrawable drawable)
	{
		GL2	gl = drawable.getGL().getGL2();

		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);	// Black background
	}
	
	// Position and orient the default camera to view in 2-D, in pixel coords.
	private void setProjection(GL2 gl) {
		GLU glu = new GLU();

		gl.glMatrixMode(GL2.GL_PROJECTION); // Prepare for matrix xform
		gl.glLoadIdentity(); // Set to identity matrix
		if (model.skewed) {
			//set up the camera and position to accommodate 3D
			glu.gluPerspective(60, 1, 1, 10000);
			//this in part deals with some funkiness regarding how we set up coordinates
			//when we switch to actual 3D coordinates we'll have to figure this out again
			glu.gluLookAt(model.playerLocation.x, model.playerLocation.y-10, 10, //hover right above the red square
					model.playerLocation.x+(-300+model.cursor.x)/10, model.playerLocation.y, 10-model.cursor.y/60, //look at an imaginary point that's in a good place
					0, 0, 1);
				//glu.gluLookAt(0+model.playerLocation.x, -10+model.playerLocation.y, 0, 0, 4, -1, 0, 1, 0);

				//glu.gluLookAt(0, -10, 0, 0+model.cursor.x/100.0, 4+model.cursor.y/100.0, -1, 0, 1, 0);

		} else {
			//2D translate and scale
			glu.gluOrtho2D(0.0f, Application.DEFAULT_SIZE.getWidth(), 0.0f, Application.DEFAULT_SIZE.getHeight());
			//reset any skewed positioning to regular positioning
			gl.glMatrixMode(GL2.GL_MODELVIEW);
			gl.glLoadIdentity();
		}
	}

	//**********************************************************************
	// Private Methods (Scene)
	//**********************************************************************
	
	private void drawWalls(GL2 gl) {
		if(model.viewWalls)
			gl.glColor3f(0, 255, 0);
		else
			gl.glColor3f(0, 0, 0);
		drawWall(gl, 50, 75, 20, 600);	// right wall
		drawWall(gl, 70, 655, 560, 20);	// upper wall
		drawWall(gl, 630, 75, 20, 600); // left wall 
		drawWall(gl, 70, 75, 260, 20);	// lower right wall.
		drawWall(gl, 370, 75, 260, 20);	// lower left wall.
		
		// walls that bound user at start.
		drawWall(gl, 310, 35, 20, 40);
		drawWall(gl, 370, 35, 20, 40);
		drawWall(gl, 330, 35, 40, 20);
		
		
		// draw inner maze walls
		drawWall(gl, 267.5, 95, 20, 72.5);
		drawWall(gl, 287.5, 147.5, 290, 20);
		drawWall(gl, 557.5, 167.5, 20, 72.5);
		drawWall(gl, 215, 220, 342.5, 20);
		drawWall(gl, 485, 240, 20, 270);
		drawWall(gl, 557.5, 292.5, 72.5, 20);
		drawWall(gl, 557.5, 312.5, 20, 72.5);
		drawWall(gl, 557.5, 437.5, 20, 165);
		drawWall(gl, 412.5, 582.5, 165, 20);
		drawWall(gl, 267.5, 510, 290, 20);
		drawWall(gl, 412.5, 292.5, 20, 217.5);
		drawWall(gl, 267.5, 292.5, 145, 20);
		drawWall(gl, 267.5, 312.5, 20, 145);
		drawWall(gl, 287.5, 437.5, 72.5, 20);
		drawWall(gl, 122.5, 582.5, 237.5, 20);
		drawWall(gl, 122.5, 510.0, 20, 72.5);
		drawWall(gl, 195, 147.5, 20, 435);
		drawWall(gl, 70, 437.5, 72.5, 20);
		drawWall(gl, 122.5, 365, 72.5, 20);
		drawWall(gl, 122.5, 147.5, 72.5, 20);
		drawWall(gl, 122.5, 167.5, 20, 145);
	}


	private void drawWall(GL2 gl, double x, double y, double w, double h) {
		gl.glBegin(GL2.GL_POLYGON);
		
		gl.glVertex2d(x, y);
		gl.glVertex2d(x + w, y);
		gl.glVertex2d(x + w, y + h);
		gl.glVertex2d(x, y + h);

		gl.glEnd();
		
		model.addWall(x, y, w, h);
	}
	
	
	private void drawPlayer(GL2 gl) {
		Point2D.Double p = model.getPlayerLocation();
		
		int r = model.getPlayerRadius();
		
		gl.glColor3f(255, 0, 0);
		gl.glBegin(GL2.GL_POLYGON);
		gl.glVertex2d(p.x - r, p.y - r);
		gl.glVertex2d(p.x - r, p.y + r);
		gl.glVertex2d(p.x + r, p.y + r);
		gl.glVertex2d(p.x + r, p.y - r);
		gl.glEnd();
		
	}
	
	// draw goal of maze with center at provided x, y. 5 pointed star. 
	private void drawGoal(GL2 gl, double cx, double cy) {
		
		double	theta = 0.5 * Math.PI;
		double	delta = Math.PI / 5;

		gl.glColor3f(255, 255, 0);
		gl.glBegin(GL.GL_TRIANGLE_FAN);
		gl.glVertex2d(cx, cy); // center point
		
		// draw verticies of star.
		double r1 = 5;
		double r2 = 2;
		
		for (int i=0; i<5; i++)
		{
			gl.glVertex2d(cx + r1 * Math.cos(theta), cy + r1 * Math.sin(theta));
			theta += delta;
			
			gl.glVertex2d(cx + r2 * Math.cos(theta), cy + r2 * Math.sin(theta));
			theta += delta;
		}
		
		gl.glVertex2d(cx + 5 * Math.cos(theta), cy + 5 * Math.sin(theta));
		gl.glEnd();
	}
	
	
	
	private void	drawMode(GLAutoDrawable drawable)
	{
		GL2		gl = drawable.getGL().getGL2();
		double[]	p = Utilities.mapViewToScene(gl, 0.5 * w, 0.5 * h, 0.0);
		double[]	q = Utilities.mapSceneToView(gl, 0.0, 0.0, 0.0);
		String		svc = ("View center in scene: [" + p[0] +", "+ p[1] + "]");
		String		sso = ("Scene origin in view: [" + q[0]+", "+ q[1] + "]");

		renderer.beginRendering(w, h);

		// Draw all text in yellow
		renderer.setColor(1.0f, 1.0f, 0.0f, 1.0f);
		renderer.draw("Pointer at (" + model.cursor.x + "," + model.cursor.y + ")", 2, 2);

		renderer.draw(svc, 2, 16);
		renderer.draw(sso, 2, 30);

		renderer.endRendering();
	}
	
}

//******************************************************************************
