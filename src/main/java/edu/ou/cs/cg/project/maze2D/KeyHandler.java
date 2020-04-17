//******************************************************************************
// Copyright (C) 2016 University of Oklahoma Board of Trustees.
//******************************************************************************
// Last modified: Wed Feb 27 17:33:00 2019 by Chris Weaver
//******************************************************************************
// Major Modification History:
//
// 20160225 [weaver]:	Original file.
// 20190227 [weaver]:	Updated to use model and asynchronous event handling.
//
//******************************************************************************
// Notes:
//
//******************************************************************************

package edu.ou.cs.cg.project.maze2D;

//import java.lang.*;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.*;
import java.awt.geom.Point2D;
import edu.ou.cs.cg.utilities.Utilities;

//******************************************************************************

/**
 * The <CODE>KeyHandler</CODE> class.<P>
 *
 * @author  Chris Weaver
 * @version %I%, %G%
 */
public final class KeyHandler extends KeyAdapter
{
	//**********************************************************************
	// Private Members
	//**********************************************************************

	// State (internal) variables
	private final View		view;
	private final Model	model;
	
	private final int stepSize = 5;

	//**********************************************************************
	// Constructors and Finalizer
	//**********************************************************************

	public KeyHandler(View view, Model model)
	{
		this.view = view;
		this.model = model;

		Component	component = view.getCanvas();

		component.addKeyListener(this);
	}

	//**********************************************************************
	// Override Methods (KeyListener)
	//**********************************************************************

	public void		keyPressed(KeyEvent e)
	{
		double			a = (Utilities.isShiftDown(e) ? 0.01 : 0.1);

		switch (e.getKeyCode())
		{
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

			case KeyEvent.VK_SPACE:
				model.skewed = !model.skewed;
				model.viewWalls = true;
				return; // why do we return instead of break here? \:
				
			case KeyEvent.VK_C:
				model.viewWalls = !model.viewWalls;
				return;
		}

		model.setOriginInSceneCoordinates(model.getOrigin());
	}
}

//******************************************************************************
