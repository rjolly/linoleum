package linoleum.mail;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JInternalFrame;
import javax.swing.WindowConstants;

public class ComponentFrame extends JInternalFrame {

	public ComponentFrame(Component what) {
		this(what, "Component Frame");
	}

	public ComponentFrame(Component what, String name) {
		super(name);

		// make sure that we close and dispose ourselves when needed
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		// default size of the frame
		setSize(700,600);

		// we want to display just the component in the entire frame
		if (what != null) {
			getContentPane().add("Center", what);
		}
	}
}
