package linoleum.notepad;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Dialog extends JInternalFrame {
	public void setParent(final Component parent) {
		final Container root = JOptionPane.getDesktopPaneForComponent(parent);
		root.add(this, Integer.valueOf(2));
		final Dimension size = getSize();
		final Dimension rootSize = root.getSize();
		final Dimension parentSize = parent.getSize();
		final Point coord = SwingUtilities.convertPoint(parent, 0, 0, root);
		int x = (parentSize.width - size.width) / 2 + coord.x;
		int y = (parentSize.height - size.height) / 2 + coord.y;
		final int ovrx = x + size.width - rootSize.width;
		final int ovry = y + size.height - rootSize.height;
		x = Math.max((ovrx > 0? x - ovrx: x), 0);
		y = Math.max((ovry > 0? y - ovry: y), 0);
		setLocation(x, y);
	}
}
