//******************************************************************************
// Copyright (C) 2016 University of Oklahoma Board of Trustees.
//******************************************************************************
// Last modified: Wed Feb 27 17:33:43 2019 by Chris Weaver
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

package edu.ou.cs.cg.application.interaction;

//import java.lang.*;
import java.awt.*;
import java.awt.event.*;
import edu.ou.cs.cg.utilities.Utilities;

//******************************************************************************

/**
 * The <CODE>MouseHandler</CODE> class.<P>
 *
 * @author  Chris Weaver
 * @version %I%, %G%
 */
public final class MouseHandler extends MouseAdapter
{
	//**********************************************************************
	// Private Members
	//**********************************************************************

	// State (internal) variables
	private final View		view;
	private final Model	model;

	//**********************************************************************
	// Constructors and Finalizer
	//**********************************************************************

	public MouseHandler(View view, Model model)
	{
		this.view = view;
		this.model = model;

		Component	component = view.getCanvas();

		component.addMouseListener(this);
		component.addMouseMotionListener(this);
		component.addMouseWheelListener(this);
	}

	//**********************************************************************
	// Override Methods (MouseListener)
	//**********************************************************************

	public void		mouseClicked(MouseEvent e)
	{
		if (Utilities.isShiftDown(e))
			model.setOriginInViewCoordinates(e.getPoint());
		else
			model.addPolylinePointInViewCoordinates(e.getPoint());
	}

	public void		mouseEntered(MouseEvent e)
	{
		model.setCursorInViewCoordinates(e.getPoint());
	}

	public void		mouseExited(MouseEvent e)
	{
		model.turnCursorOff();
	}

	public void		mousePressed(MouseEvent e)
	{
	}

	public void		mouseReleased(MouseEvent e)
	{
	}

	//**********************************************************************
	// Override Methods (MouseMotionListener)
	//**********************************************************************

	public void		mouseDragged(MouseEvent e)
	{
		model.addPolylinePointInViewCoordinates(e.getPoint());
		model.setCursorInViewCoordinates(e.getPoint());
	}

	public void		mouseMoved(MouseEvent e)
	{
		model.setCursorInViewCoordinates(e.getPoint());
	}

	//**********************************************************************
	// Override Methods (MouseWheelListener)
	//**********************************************************************

	public void		mouseWheelMoved(MouseWheelEvent e)
	{
	}
}

//******************************************************************************
