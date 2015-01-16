package linoleum;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class ImagePanel extends JPanel implements MouseMotionListener {
	private final BufferedImage image;
	private Rectangle r0;
	private int x0;
	private int y0;

	public ImagePanel(final File file) {
		try {
			image = ImageIO.read(file);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		addMouseMotionListener(this);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(image.getWidth(), image.getHeight());
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(image, 0, 0, null);
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
