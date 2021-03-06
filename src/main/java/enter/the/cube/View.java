package enter.the.cube;

import java.awt.Font;
import java.awt.geom.Point2D;
import java.nio.FloatBuffer;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextRenderer;

public final class View implements GLEventListener {
	private static final int DEFAULT_FRAMES_PER_SECOND = 60;

	private final GLJPanel canvas;
	private int w; // Canvas width
	private int h; // Canvas height

	private TextRenderer renderer;

	private final FPSAnimator animator;
	private final Model model;

	public final MouseHandler			mouseHandler;

	private double wallHeight = 50;

	private boolean wallsIn;

	private int counter;

	public View(GLJPanel canvas) {
		this.canvas = canvas;
		canvas.setFocusTraversalKeysEnabled(false); //let tab be a game control
		canvas.addGLEventListener(this);

		// Initialize model (scene data and parameter manager)
		model = new Model(this);

		// Initialize controller (interaction handlers)
		new KeyHandler(this, model);
		mouseHandler = new MouseHandler(this, model);

		// Initialize animation
		animator = new FPSAnimator(canvas, DEFAULT_FRAMES_PER_SECOND);
		animator.start();
	}

	public GLJPanel getCanvas() {
		return canvas;
	}

	public int getWidth() {
		return w;
	}

	public int getHeight() {
		return h;
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		w = drawable.getSurfaceWidth();
		h = drawable.getSurfaceHeight();
		renderer = new TextRenderer(new Font("Monospaced", Font.PLAIN, 12), true, true);
		initPipeline(drawable);
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		renderer = null;
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		// updatePipeline(drawable);
		update(drawable);
		render(drawable);
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
		this.w = w;
		this.h = h;
	}

	// **********************************************************************
	// Private Methods (Rendering)
	// **********************************************************************

	private void update(GLAutoDrawable drawable) {
	}

	private void render(GLAutoDrawable drawable)
	{
		GL2	gl = drawable.getGL().getGL2();
		if(model.viewWalls){gl.glClear(GL2.GL_COLOR_BUFFER_BIT|GL2.GL_DEPTH_BUFFER_BIT);}

		setProjection(gl);
		// create plain maze.
		// draw fake wall that spin out on touch.
		drawSpecialWall(gl, 557.5, 385, 20, 52.5);
		// draw regular walls
		drawWalls(gl);
		wallsIn=true;
		// draw a star where the goal is
		drawGoal(gl, model.floatingCubeLocation.x + 5,model.floatingCubeLocation.y + 5);

		// only draw floor in 3d. only draw player location in 2d.
		if(model.skewed) {
			drawFloors(gl);
		} else {
			drawPlayer(gl);
		}

		// draw a cube and a floating pane in the sky. scenery.
		drawCube(gl,100,100,100,100);
		drawFloatingPlane(gl,model.floatingPlaneLocation.x,model.floatingPlaneLocation.y,model.floatingPlaneLocation.z);
		// draw a spinning floating cube at the goal location.
		drawFloatingCube(gl,model.floatingCubeLocation.x,model.floatingCubeLocation.y,model.floatingCubeLocation.z, 10);
		// draw the cube maze. This the cube also appears in the sky in the plane maze.
		drawCubeCube(gl);

		gl.glFlush(); // Finish and display
	}

	private void initPipeline(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // Black background
	}

	// Position and orient the default camera to view in 2-D, in pixel coords.
	private void setProjection(GL2 gl) {
		GLU glu = new GLU();

		gl.glMatrixMode(GL2.GL_PROJECTION); // Prepare for matrix xform
		gl.glLoadIdentity(); // Set to identity matrix
		if (model.skewed) {

			// enable lighting
			gl.glEnable( GLLightingFunc.GL_LIGHTING );
			gl.glEnable( GL.GL_DEPTH_TEST );
			//gl.glShadeModel(gl.GL_FLAT);


			// get the object colors from glColor.
			gl.glColorMaterial(GL.GL_FRONT, GLLightingFunc.GL_AMBIENT_AND_DIFFUSE);
			gl.glEnable(GLLightingFunc.GL_COLOR_MATERIAL);


			float[] lightColor = model.getFlashlightColor();
			if(lightColor == null) {
				gl.glDisable(GLLightingFunc.GL_LIGHT0);
			} else {
				gl.glEnable( GLLightingFunc.GL_LIGHT0 );
				// set the color for the flashlight
				float[] ambient = {lightColor[0] * .6f, lightColor[1] * .6f, lightColor[2] * .6f, 1f};
				float[] diffuse = {lightColor[0] * .6f, lightColor[1] * .6f, lightColor[2] * .6f, 1f};
				float[] specular = {lightColor[0], lightColor[1], lightColor[2], 1f};

				gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_AMBIENT, ambient, 0);
				gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_DIFFUSE, diffuse, 0);
				gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_SPECULAR, specular, 0);

				// set the position of the flashlight to be at the player's eye
				gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_POSITION, FloatBuffer.wrap(new float[]{
						(float) model.playerLocation.x,
						(float) model.playerLocation.y,
						(float) model.playerLocation.z,
						1f}));
				// set the direction of the flashlight to be the direction the player is facing
				gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_SPOT_DIRECTION, FloatBuffer.wrap(new float[]{
						(float) model.lookPoint.x,
						(float) model.lookPoint.y,
						(float) model.lookPoint.z}));
				// create the spotlight effect to make light function as a flashlight.
				gl.glLightf(GLLightingFunc.GL_LIGHT0, GL2.GL_SPOT_EXPONENT, 20.0f);
				gl.glLightf(GLLightingFunc.GL_LIGHT0, GL2.GL_SPOT_CUTOFF, 45.0f);
				// make it so the light dims the further away it gets.
				gl.glLightf(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_CONSTANT_ATTENUATION, .01f);
				gl.glLightf(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_LINEAR_ATTENUATION, .01f);
			}



			if(model.level == 2) {
				gl.glEnable( GLLightingFunc.GL_LIGHT1 );

				// set the color for the flashlight
				float[] ambient = {.6f, .6f, .6f, 1f};
				float[] diffuse = {.6f, .6f, .6f, 1f};
				float[] specular = {1f, 1f, 1f, 1f};

				gl.glLightfv(GLLightingFunc.GL_LIGHT1, GLLightingFunc.GL_AMBIENT, ambient, 0);
				gl.glLightfv(GLLightingFunc.GL_LIGHT1, GLLightingFunc.GL_DIFFUSE, diffuse, 0);
				gl.glLightfv(GLLightingFunc.GL_LIGHT1, GLLightingFunc.GL_SPECULAR, specular, 0);

				// set the position of the light to be at the center of the cube.
				gl.glLightfv(GLLightingFunc.GL_LIGHT1, GLLightingFunc.GL_POSITION, FloatBuffer.wrap(new float[]{1500f,1500f,1500f,1f}));

				// set attenuation of the light so doesn't fade away to quickly.
				gl.glLightf(GLLightingFunc.GL_LIGHT1, GLLightingFunc.GL_LINEAR_ATTENUATION, .01f);

			}



			//set up the camera and position to accommodate 3D
			glu.gluPerspective(60, 1, 1, 10000);
			//this in part deals with some funkiness regarding how we set up coordinates
			//when we switch to actual 3D coordinates we'll have to figure this out again
			//remember that our mouse coordinates are both 0-700 in Cartesian I right now.
			//our x and y are with respect to the maze plane and our z is up out of the plane
			//so we have to do some trig to find a nice point to look at i guess
			glu.gluLookAt(
					//look from player location
					model.playerLocation.x, model.playerLocation.y, model.playerLocation.z,
					//look at an imaginary point that's in a good place
					model.playerLocation.x + model.lookPoint.x, model.playerLocation.y + model.lookPoint.y, model.playerLocation.z + model.lookPoint.z,
					//what is up but "not down"?
					model.up.x, model.up.y, model.up.z);
			//System.out.println(model.up);
		} else {
			gl.glDisable( GL2.GL_LIGHTING );
			//2D translate and scale
			glu.gluOrtho2D(0.0f, Application.DEFAULT_SIZE.getWidth(), 0.0f, Application.DEFAULT_SIZE.getHeight());
			// reset any skewed positioning to regular positioning
			gl.glMatrixMode(GL2.GL_MODELVIEW);
			gl.glLoadIdentity();
		}
	}

	// **********************************************************************
	// Private Methods (Scene)
	//**********************************************************************
	private void drawFloors(GL2 gl) {
		gl.glColor3f(.05f, .05f, .05f);

		drawFloor(gl, 50, 75, 600, 600);
		drawFloor(gl, 310, 35, 80, 40);
	}

	private void drawFloor(GL2 gl, double x, double y, double w, double l) {

		// draw floor using many equally sized squares.
		// done to get visual appearance of flashlight while doing vertex shading.
		gl.glBegin(GL2.GL_QUADS);
		for(double i = x; i + 2 <= x + w; i += 2) {
			for(double j = y; j + 2 <= y + l; j += 2) {
				gl.glVertex2d(i, j);
				gl.glVertex2d(i + 2, j);
				gl.glVertex2d(i + 2, j + 2);
				gl.glVertex2d(i, j + 2);
			}
		}
		gl.glEnd();
	}


	private void drawWalls(GL2 gl) {
		// draw regular walls
		if(model.viewWalls) {
			gl.glColor3f(.2f, .2f, .2f);
		} else {
			gl.glColor3f(0, 0, 0);
		}

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

	private void drawSpecialWall(GL2 gl, double x, double y, double w, double l) {

		// draw special walls. appears as a normal wall unless second light is used.
		// this makes the light glow light blue.
		// when the wall is touched make the wall spin and expand.
		if(!model.viewWalls) {
			gl.glColor3f(0, 0, 0);
		} else {
			// if flashlight color is blue make the special wall glow light blue.
			if(model.getFlashlightCount() == 1) {
				// set the emission color of the walls to light blue to make it seem as though the walls are glowing.
				gl.glMaterialfv(GL.GL_FRONT, GLLightingFunc.GL_EMISSION, FloatBuffer.wrap(new float[] {.5f,.75f,1f,1f}));
			}
			// set the color to the wall so that when combined with emission it will be white.
			// this is so that the wall appears white when a flashlight is shined on it.
			gl.glColor3f(.2f, .2f, .2f);

		}
		if(model.masterWallSpin != 0 && model.masterWallSpin <= 100) {
			gl.glPushMatrix();
			gl.glTranslated(x + w/2, y + l/2, 0);
			gl.glRotated(model.masterWallSpin++, 0, 0, 1);
			gl.glScaled(model.masterWallSpin,model.masterWallSpin,model.masterWallSpin);
			gl.glTranslated(-(x + w/2), -(y + l/2), 0);
			drawWall(gl, x, y, w, l);

			gl.glPopMatrix();
		} else if (model.masterWallSpin <= 100) {
			drawWall(gl, x, y, w, l);
		}
		gl.glMaterialfv(GL.GL_FRONT, GLLightingFunc.GL_EMISSION, FloatBuffer.wrap(new float[] {0f,0f,0f,1f}));
	}

	private void drawWall(GL2 gl, double x, double y, double w, double l) {
		gl.glBegin(GL2.GL_QUADS);

		// create rectangular prism with the defined dimensions.
		// make each face of the prism using many rectanges. this is so
		//	vertex shading for the flashlight in the game will work effectively.
		double wSplit = 1;
		double lSplit = 1;
		double hSplit = 1;

		double i,j;
		// bottom
		for(i = x; i + wSplit <= x + w ; i += wSplit) {
			for(j = y; j + lSplit <= y + l; j += lSplit) {
				gl.glVertex3d(i, j, 0);
				gl.glVertex3d(i + wSplit, j, 0);
				gl.glVertex3d(i + wSplit, j + lSplit, 0);
				gl.glVertex3d(i, j + lSplit, 0);
			}

		}
		// top
		for(i = x; i + wSplit <= x + w ; i += wSplit) {
			for(j = y; j + lSplit <= y + l; j += lSplit) {
				gl.glVertex3d(i, j, wallHeight);
				gl.glVertex3d(i + wSplit, j, wallHeight);
				gl.glVertex3d(i + wSplit, j + lSplit, wallHeight);
				gl.glVertex3d(i, j + lSplit, wallHeight);
			}
		}

		// left
		for(i = y; i + lSplit <= y + l ; i += lSplit) {
			for(j = 0; j + hSplit <= wallHeight; j += hSplit) {
				gl.glVertex3d(x, i, j);
				gl.glVertex3d(x, i + lSplit, j);
				gl.glVertex3d(x, i + lSplit, j + hSplit);
				gl.glVertex3d(x, i, j + hSplit);
			}
		}
		// right
		for(i = y; i + lSplit <= y + l ; i += lSplit) {
			for(j = 0; j + hSplit <= wallHeight; j += hSplit) {
				gl.glVertex3d(x + w, i, j);
				gl.glVertex3d(x + w, i + lSplit, j);
				gl.glVertex3d(x + w, i + lSplit, j + hSplit);
				gl.glVertex3d(x + w, i, j + hSplit);
			}
		}

		// front
		for(i = x; i + wSplit <= x + w ; i += wSplit) {
			for(j = 0; j + hSplit <= wallHeight; j += hSplit) {
				gl.glVertex3d(i, y, j);
				gl.glVertex3d(i + wSplit, y, j);
				gl.glVertex3d(i + wSplit, y, j + hSplit);
				gl.glVertex3d(i, y, j + hSplit);
			}
		}
		// back
		for(i = x; i + wSplit <= x + w ; i += wSplit) {
			for(j = 0; j + hSplit <= wallHeight; j += hSplit) {
				gl.glVertex3d(i, y + l, j);
				gl.glVertex3d(i + wSplit, y + l, j);
				gl.glVertex3d(i + wSplit, y + l, j + hSplit);
				gl.glVertex3d(i, y + l, j + hSplit);
			}
		}

		gl.glEnd();

		if(!wallsIn){model.addWall(x, y, w, l);}
	}



	private void drawCube(GL2 gl, double x, double y, double z, double l) { //draw l-long cube (x,y,z) to (x+l,y+l,z+l)
		double w = l;
		gl.glColor3f(1, 1, 1);
		gl.glBegin(GL2.GL_QUADS);
		// bottom
		gl.glVertex3d(x, y, z);
		gl.glVertex3d(x + w, y, z);
		gl.glVertex3d(x + w, y + l, z);
		gl.glVertex3d(x, y + l, z);

		// top
		gl.glVertex3d(x, y, z+l);
		gl.glVertex3d(x + w, y, z+l);
		gl.glVertex3d(x + w, y + l, z+l);
		gl.glVertex3d(x, y + l, z+l);

		// left
		gl.glVertex3d(x, y, z+l);
		gl.glVertex3d(x, y + l, z+l);
		gl.glVertex3d(x, y + l, z);
		gl.glVertex3d(x, y, z);
		// right
		gl.glVertex3d(x + w, y, z+l);
		gl.glVertex3d(x + w, y + l, z+l);
		gl.glVertex3d(x + w, y + l, z);
		gl.glVertex3d(x + w, y, z);

		// front
		gl.glVertex3d(x, y, z+l);
		gl.glVertex3d(x + w, y, z+l);
		gl.glVertex3d(x + w, y, z);
		gl.glVertex3d(x, y, z);
		// back
		gl.glVertex3d(x, y + l, z+l);
		gl.glVertex3d(x + w, y + l, z+l);
		gl.glVertex3d(x + w, y + l, z);
		gl.glVertex3d(x, y + l, z);
		gl.glEnd();
	}

	private void drawFloatingPlane(GL2 gl, double x, double y, double z) {
		double l = 10;
		double w = 10;
		gl.glPushMatrix();
		gl.glTranslated(x+ w/2, y + l/2, z);
		gl.glRotated(counter++, 0, 0, 1);
		gl.glTranslated(-x - w/2, -y - l/2, -z);


		gl.glColor3f(.1f, .1f, .1f);
		gl.glBegin(GL2.GL_QUADS);
		// bottom
		gl.glVertex3d(x, y, z);
		gl.glVertex3d(x + w, y, z);
		gl.glVertex3d(x + w, y + l, z);
		gl.glVertex3d(x, y + l, z);
		gl.glEnd();

		gl.glPopMatrix();
	}

	private void drawFloatingCube(GL2 gl, double x, double y, double z, double l) {
		double w = l;
		gl.glPushMatrix();
		gl.glTranslated(x+ w/2, y + l/2, z);
		gl.glRotated(counter++, 0, 0, 1);
		gl.glTranslated(-x - w/2, -y - l/2, -z);

		drawCube(gl, x,y,z,l);

		gl.glPopMatrix();

	}



	private void drawPlayer(GL2 gl) {
		Point2D.Double p = new Point2D.Double(model.playerLocation.x, model.playerLocation.y);

		int r = model.getPlayerRadius();

		gl.glColor3f(1, 0, 0);

		gl.glBegin(GL2.GL_POLYGON);
		gl.glVertex2d(p.x - r, p.y - r);
		gl.glVertex2d(p.x - r, p.y + r);
		gl.glVertex2d(p.x + r, p.y + r);
		gl.glVertex2d(p.x + r, p.y - r);
		gl.glEnd();

	}

	// draw goal of maze with center at provided x, y. 5 pointed star.
	private void drawGoal(GL2 gl, double cx, double cy) {

		double theta = 0.5 * Math.PI;
		double delta = Math.PI / 5;

		gl.glColor3f(1, 1, 0);
		gl.glBegin(GL.GL_TRIANGLE_FAN);
		gl.glVertex2d(cx, cy); // center point

		// draw vertices of star.
		double r1 = 5;
		double r2 = 2;

		for (int i = 0; i < 5; i++) {
			gl.glVertex2d(cx + r1 * Math.cos(theta), cy + r1 * Math.sin(theta));
			theta += delta;

			gl.glVertex2d(cx + r2 * Math.cos(theta), cy + r2 * Math.sin(theta));
			theta += delta;
		}

		gl.glVertex2d(cx + 5 * Math.cos(theta), cy + 5 * Math.sin(theta));
		gl.glEnd();
	}

	private void drawCubeCube(GL2 gl) {
		//I also want to handle gravity here
		double x = model.cubeCubeLocation.x;
		double y = model.cubeCubeLocation.y;
		double z = model.cubeCubeLocation.z;
		for (int[][] i : model.cubeCube) {
			for (int[] j : i) {
				for (int k : j) {
					if(k==1) {
						drawCube(gl,x,y,z,100);
						///* test code, remove this line:*/ model.level = 2; model.skewed=true;
						if(model.level==2) {
							/*gl.glColor3f(1, 0, 0);
							gl.glBegin(GL2.GL_LINES);
							gl.glVertex3d(x+50, y+50, z+50);
							gl.glVertex3d(model.playerLocation.x, model.playerLocation.y, model.playerLocation.z);
							gl.glEnd();*/ //doesn't work?
							Point3D pull = new Point3D(x+50,y+50,z+50); //middle of cube
							pull.subtract(model.playerLocation);
							pull.divide(pull.magnitude()*pull.magnitude());
							//pull.multiply(model.g);
							model.gravityVector.add(pull);
						} else {
							model.gravityVector.set(0,0,-1);
						}
					}
					x+=100;
				}
				y+=100;
				x=model.cubeCubeLocation.x;
			}
			z+=100;
			y=model.cubeCubeLocation.y;
		}
		model.movePlayer(model.gravityVector.x, model.gravityVector.y, model.gravityVector.z);
		model.up.set(model.gravityVector.unit().multiply(-1));
		//model.lookPoint.multiply(model.gravityVector.unit()); //hmm
	}

}
