package linoleum;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.text.Position;

public class FileList extends JList<Path> {
	public FileList() {
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(final MouseEvent evt) {
				if (evt.getClickCount() == 1 && !evt.isControlDown() && !evt.isShiftDown()) {
					final int idx = locationToIndex(evt.getPoint());
					if (idx > -1) {
						setSelectedIndex(idx);
					} else {
						clearSelection();
					}
				}
			}
		});
	}

	@Override
	public int getNextMatch(final String prefix, final int startIndex, final Position.Bias bias) {
		final ListModel<Path> model = getModel();
		final int max = model.getSize();
		if (prefix == null || startIndex < 0 || startIndex >= max) {
			throw new IllegalArgumentException();
		}
		// start search from the next element before/after the selected element
		final boolean backwards = (bias == Position.Bias.Backward);
		for (int i = startIndex; backwards ? i >= 0 : i < max; i += (backwards ?  -1 : 1)) {
			final String filename = model.getElementAt(i).getFileName().toString();
			if (filename.regionMatches(true, 0, prefix, 0, prefix.length())) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public int locationToIndex(final Point location) {
		final int n = super.locationToIndex(location);
		final Rectangle q = getCellBounds(n, n);
		return q != null && q.contains(location)?n:-1;
	}
}
