package linoleum;

import java.awt.Container;
import java.awt.Rectangle;
import javax.swing.DefaultDesktopManager;
import javax.swing.JInternalFrame;
import javax.swing.JDesktopPane;
import javax.swing.JLayeredPane;

public class DesktopManager extends DefaultDesktopManager {

	public void iconifyFrame(final JInternalFrame f) {
		final JInternalFrame.JDesktopIcon icon = f.getDesktopIcon();
		final Container c = f.getParent();
		final JDesktopPane d = (JDesktopPane)f.getDesktopPane();
		final boolean findNext = f.isSelected();
		if(!wasIcon(f)) {
			final Rectangle r = getBoundsForIconOf(f);
			icon.setBounds(r.x, r.y, r.width, r.height);
			setWasIcon(f, Boolean.TRUE);
		}

		if (c == null || d == null) {
			return;
		}

		if (c instanceof JLayeredPane) {
			((JLayeredPane)c).putLayer(icon, JLayeredPane.POPUP_LAYER - 1);
		}

		if (!f.isMaximum()) {
			f.setNormalBounds(f.getBounds());
		}
//		d.setComponentOrderCheckingEnabled(false);
		c.remove(f);
		c.add(icon);
//		d.setComponentOrderCheckingEnabled(true);
		c.repaint(f.getX(), f.getY(), f.getWidth(), f.getHeight());
		if (findNext) {
			if (d.selectFrame(true) == null) {
				f.restoreSubcomponentFocus();
			}
		}
	}
}
