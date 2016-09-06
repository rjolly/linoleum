package linoleum;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

public class ImagePanel extends JPanel implements Scrollable, MouseMotionListener {
	private final BufferedImage image;
	private final boolean scaled;
	private Rectangle r0;
	private int x0;
	private int y0;

	public ImagePanel(final Path path, final boolean scaled) throws IOException {
		try (final InputStream is = Files.newInputStream(path)) {
			image = ImageIO.read(is);
		}
		addMouseMotionListener(this);
		this.scaled = scaled;
	}

	@Override
	public Dimension getPreferredSize() {
		if (scaled) {
			final int width = getParent().getWidth();
			final int height = image.getHeight() * width / image.getWidth();
			return new Dimension(width, height);
		} else {
			final int width = Math.max(getParent().getWidth(), image.getWidth());
			final int height = Math.max(getParent().getHeight(), image.getHeight());
			return new Dimension(width, height);
		}
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 20;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return orientation == SwingConstants.HORIZONTAL?visibleRect.width:visibleRect.height;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return scaled;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (scaled) {
			g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
		} else {
			final int x = (getWidth() - image.getWidth()) / 2;
			final int y = (getHeight() - image.getHeight()) / 2;
			g.drawImage(image, x, y, null);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		final int x = e.getXOnScreen();
		final int y = e.getYOnScreen();
		final Rectangle r = new Rectangle(r0);
		r.translate(x0 - x, y0 - y);
		scrollRectToVisible(r);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		x0 = e.getXOnScreen();
		y0 = e.getYOnScreen();
		r0 = getVisibleRect();
	}
}
