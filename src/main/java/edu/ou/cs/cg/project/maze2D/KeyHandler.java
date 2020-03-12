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
		Point2D.Double	p = model.getOrigin();
		double			a = (Utilities.isShiftDown(e) ? 0.01 : 0.1);
		int r = model.getPlayerRadius();
		Point2D player = model.getPlayerLocation();

		switch (e.getKeyCode())
		{
			case KeyEvent.VK_LEFT:
				if(model.freeLocation(player.getX() - r - stepSize, player.getY() - r) && 
						model.freeLocation(player.getX() - r - stepSize, player.getY() + r)) {
					model.movePlayer(-stepSize, 0);
					playerReachGoal(player.getX() - stepSize, player.getY());
				}
				return;
				
			case KeyEvent.VK_RIGHT:
				if(model.freeLocation(player.getX() + r + stepSize, player.getY() - r) && 
						model.freeLocation(player.getX() + r + stepSize, player.getY() + r)) {		
					model.movePlayer(stepSize, 0);
					playerReachGoal(player.getX() + stepSize, player.getY());
				}
				return;
				
			case KeyEvent.VK_UP:
				if(model.freeLocation(player.getX() - r, player.getY() + r + stepSize) && 
						model.freeLocation(player.getX() + r, player.getY() + r + stepSize)) {					
					model.movePlayer(0, stepSize);
					playerReachGoal(player.getX(), player.getY() + stepSize);
				}
				return;
				
			case KeyEvent.VK_DOWN:
				if(model.freeLocation(player.getX() - r, player.getY() - r - stepSize) && 
						model.freeLocation(player.getX() + r, player.getY() - r - stepSize)) {					
					model.movePlayer(0, -stepSize);
					playerReachGoal(player.getX(), player.getY() - stepSize);
				}
				return;
			case KeyEvent.VK_SPACE:
				model.skewed = !model.skewed;
				model.viewWalls = true;
				return; // why do we return instead of break here? \:
				
			case KeyEvent.VK_C:
				model.viewWalls = !model.viewWalls;
				return;
		}

		model.setOriginInSceneCoordinates(p);
	}
	
	private void 	playerReachGoal(double x, double y) {
		if(x >= 345 && x <= 355 && y >= 370 && y <= 380) {
			model.setPlayer(350, 75);
			model.viewWalls = !model.viewWalls;
		}
	}
}

//******************************************************************************
