package edu.ou.cs.cg.project.maze2D;

import java.awt.Dimension;
import java.awt.event.*;
import javax.swing.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.*;

public final class Application
	implements Runnable
{

	public static final String		DEFAULT_NAME = "Maze2D";
	public static final Dimension	DEFAULT_SIZE = new Dimension(700, 700);

	public static void	main(String[] args)
	{
		SwingUtilities.invokeLater(new Application(args));
	}

	public Application(String[] args) {}

	public void	run()
	{
		GLProfile		profile = GLProfile.getDefault();

		System.out.println("Running with OpenGL version " + profile.getName());

		GLCapabilities	capabilities = new GLCapabilities(profile);
		//GLCanvas		canvas = new GLCanvas(capabilities);	// Single-buffer
		GLJPanel		canvas = new GLJPanel(capabilities);	// Double-buffer
		JFrame			frame = new JFrame(DEFAULT_NAME);

		// Specify the starting width and height of the canvas itself
		canvas.setPreferredSize(DEFAULT_SIZE);

		// Populate and show the frame
		frame.setBounds(50, 50, 200, 200);
		frame.getContentPane().add(canvas);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// Exit when the user clicks the frame's close button
		frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});
		//make a view to control our canvas or something
		new View(canvas);
	}
}
