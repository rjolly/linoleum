package linoleum.mail;

import java.awt.LayoutManager;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import linoleum.application.ApplicationManager;

public class Panel extends JPanel {
	public Panel() {
	}

	public Panel(final LayoutManager layout) {
		super(layout);
	}

	protected SimpleClient getClient() {
		return ApplicationManager.getInstance(JOptionPane.getDesktopPaneForComponent(this)).get(SimpleClient.class);
	}
}
