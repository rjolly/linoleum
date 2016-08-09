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
	public static final int DEFAULT_LAYER = 1;
	public static final int DIALOG_LAYER = 2;
	public static final int ICON_LAYER = 3;

	public DesktopPane() {
		final InputMap map = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		map.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, InputEvent.ALT_DOWN_MASK), "selectNextFrame");
		map.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, InputEvent.SHIFT_DOWN_MASK | InputEvent.ALT_DOWN_MASK), "selectPreviousFrame");
	}

	@Override
	public Component add(final Component comp) {
		addImpl(comp, DEFAULT_LAYER, -1);
		return comp;
	}

	public int getLayer(final Component c) {
		final int layer = super.getLayer(c);
		return c instanceof JInternalFrame.JDesktopIcon?ICON_LAYER:layer;
	}
}
