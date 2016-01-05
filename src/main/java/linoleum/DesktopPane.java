package linoleum;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import javax.swing.KeyStroke;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JDesktopPane;

public class DesktopPane extends JDesktopPane {

	public DesktopPane() {
		final InputMap map = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		map.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, InputEvent.ALT_DOWN_MASK), "selectNextFrame");
		map.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, InputEvent.SHIFT_DOWN_MASK | InputEvent.ALT_DOWN_MASK), "selectPreviousFrame");
	}

	public int getLayer(final Component c) {
		return c instanceof JInternalFrame?1:c instanceof JInternalFrame.JDesktopIcon?2:super.getLayer(c);
	}
}
