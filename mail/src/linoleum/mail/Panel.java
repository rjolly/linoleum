package linoleum.mail;

import java.awt.LayoutManager;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Panel extends JPanel {
	public Panel() {
	}

	public Panel(final LayoutManager layout) {
		super(layout);
	}

	protected SimpleClient getClient() {
		for (final JInternalFrame c : JOptionPane.getDesktopPaneForComponent(this).getAllFrames()) if (c instanceof SimpleClient) {
			return (SimpleClient) c;
		}
		return null;
	}
}
