package linoleum.html;

import java.awt.*;
import java.awt.image.ImageObserver;
import java.net.*;
import java.util.Dictionary;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.Document;
import javax.swing.text.html.*;
import javax.swing.text.html.StyleSheet;
import javax.swing.event.*;

public class ImageView extends View {
	private static boolean sIsInc = false;
	private static int sIncRate = 100;
	private static final String PENDING_IMAGE = "html.pendingImage";
	private static final String MISSING_IMAGE = "html.missingImage";
	private static final String IMAGE_CACHE_PROPERTY = "imageCache";
	private static final int DEFAULT_WIDTH = 38;
	private static final int DEFAULT_HEIGHT= 38;
	private static final int DEFAULT_BORDER = 2;

	// Bitmask values
	private static final int LOADING_FLAG = 1;
	private static final int LINK_FLAG = 2;
	private static final int WIDTH_FLAG = 4;
	private static final int HEIGHT_FLAG = 8;
	private static final int RELOAD_FLAG = 16;
	private static final int RELOAD_IMAGE_FLAG = 32;
	private static final int SYNC_LOAD_FLAG = 64;

	private AttributeSet attr;
	private Image image;
	private Image disabledImage;
	private int width;
	private int height;
	private int state;
	private Container container;
	private Rectangle fBounds;
	private Color borderColor;
	private short borderSize;
	private short leftInset;
	private short rightInset;
	private short topInset;
	private short bottomInset;
	private ImageObserver imageObserver;
	private View altView;
	private float vAlign;

	public ImageView(Element elem) {
		super(elem);
		fBounds = new Rectangle();
		imageObserver = new ImageHandler();
		state = RELOAD_FLAG | RELOAD_IMAGE_FLAG;
	}

	public String getAltText() {
		return (String)getElement().getAttributes().getAttribute
			(HTML.Attribute.ALT);
	}

	public URL getImageURL() {
		String src = (String)getElement().getAttributes().
							 getAttribute(HTML.Attribute.SRC);
		if (src == null) {
			return null;
		}

		URL reference = ((HTMLDocument)getDocument()).getBase();
		try {
			URL u = new URL(reference,src);
			return u;
		} catch (MalformedURLException e) {
			return null;
		}
	}

	public Icon getNoImageIcon() {
		return (Icon) UIManager.getLookAndFeelDefaults().get(MISSING_IMAGE);
	}

	public Icon getLoadingImageIcon() {
		return (Icon) UIManager.getLookAndFeelDefaults().get(PENDING_IMAGE);
	}

	public Image getImage() {
		sync();
		return image;
	}

	private Image getImage(boolean enabled) {
		Image img = getImage();
		if (! enabled) {
			if (disabledImage == null) {
				disabledImage = GrayFilter.createDisabledImage(img);
			}
			img = disabledImage;
		}
		return img;
	}

	public void setLoadsSynchronously(boolean newValue) {
		synchronized(this) {
			if (newValue) {
				state |= SYNC_LOAD_FLAG;
			}
			else {
				state = (state | SYNC_LOAD_FLAG) ^ SYNC_LOAD_FLAG;
			}
		}
	}

	public boolean getLoadsSynchronously() {
		return ((state & SYNC_LOAD_FLAG) != 0);
	}

	protected StyleSheet getStyleSheet() {
		HTMLDocument doc = (HTMLDocument) getDocument();
		return doc.getStyleSheet();
	}

	public AttributeSet getAttributes() {
		sync();
		return attr;
	}

	public String getToolTipText(float x, float y, Shape allocation) {
		return getAltText();
	}

	protected void setPropertiesFromAttributes() {
		StyleSheet sheet = getStyleSheet();
		this.attr = sheet.getViewAttributes(this);

		borderSize = (short)getIntAttr(HTML.Attribute.BORDER, isLink() ? DEFAULT_BORDER : 0);

		leftInset = rightInset = (short)(getIntAttr(HTML.Attribute.HSPACE, 0) + borderSize);
		topInset = bottomInset = (short)(getIntAttr(HTML.Attribute.VSPACE, 0) + borderSize);

		borderColor = ((StyledDocument)getDocument()).getForeground(getAttributes());

		AttributeSet attr = getElement().getAttributes();

		Object alignment = attr.getAttribute(HTML.Attribute.ALIGN);

		vAlign = 1.0f;
		if (alignment != null) {
			alignment = alignment.toString();
			if ("top".equals(alignment)) {
				vAlign = 0f;
			}
			else if ("middle".equals(alignment)) {
				vAlign = .5f;
			}
		}

		AttributeSet anchorAttr = (AttributeSet)attr.getAttribute(HTML.Tag.A);
		if (anchorAttr != null && anchorAttr.isDefined
			(HTML.Attribute.HREF)) {
			synchronized(this) {
				state |= LINK_FLAG;
			}
		}
		else {
			synchronized(this) {
				state = (state | LINK_FLAG) ^ LINK_FLAG;
			}
		}
	}

	public void setParent(View parent) {
		View oldParent = getParent();
		super.setParent(parent);
		container = (parent != null) ? getContainer() : null;
		if (oldParent != parent) {
			synchronized(this) {
				state |= RELOAD_FLAG;
			}
		}
	}

	public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
		super.changedUpdate(e,a,f);

		synchronized(this) {
			state |= RELOAD_FLAG | RELOAD_IMAGE_FLAG;
		}

		// Assume the worst.
		preferenceChanged(null, true, true);
	}

	public void paint(Graphics g, Shape a) {
		sync();

		Rectangle rect = (a instanceof Rectangle) ? (Rectangle)a :
						 a.getBounds();
		Rectangle clip = g.getClipBounds();

		fBounds.setBounds(rect);
		paintHighlights(g, a);
		paintBorder(g, rect);
		if (clip != null) {
			g.clipRect(rect.x + leftInset, rect.y + topInset,
					   rect.width - leftInset - rightInset,
					   rect.height - topInset - bottomInset);
		}

		Container host = getContainer();
		Image img = getImage(host == null || host.isEnabled());
		if (img != null) {
			if (! hasPixels(img)) {
				// No pixels yet, use the default
				Icon icon = getLoadingImageIcon();
				if (icon != null) {
					icon.paintIcon(host, g,
							rect.x + leftInset, rect.y + topInset);
				}
			}
			else {
				// Draw the image
				g.drawImage(img, rect.x + leftInset, rect.y + topInset,
							width, height, imageObserver);
			}
		}
		else {
			Icon icon = getNoImageIcon();
			if (icon != null) {
				icon.paintIcon(host, g,
						rect.x + leftInset, rect.y + topInset);
			}
			View view = getAltView();
			// Paint the view representing the alt text, if its non-null
			if (view != null && ((state & WIDTH_FLAG) == 0 ||
								 width > DEFAULT_WIDTH)) {
				// Assume layout along the y direction
				Rectangle altRect = new Rectangle
					(rect.x + leftInset + DEFAULT_WIDTH, rect.y + topInset,
					 rect.width - leftInset - rightInset - DEFAULT_WIDTH,
					 rect.height - topInset - bottomInset);

				view.paint(g, altRect);
			}
		}
		if (clip != null) {
			// Reset clip.
			g.setClip(clip.x, clip.y, clip.width, clip.height);
		}
	}

	private void paintHighlights(Graphics g, Shape shape) {
		if (container instanceof JTextComponent) {
			JTextComponent tc = (JTextComponent)container;
			Highlighter h = tc.getHighlighter();
			if (h instanceof LayeredHighlighter) {
				((LayeredHighlighter)h).paintLayeredHighlights
					(g, getStartOffset(), getEndOffset(), shape, tc, this);
			}
		}
	}

	private void paintBorder(Graphics g, Rectangle rect) {
		Color color = borderColor;

		if ((borderSize > 0 || image == null) && color != null) {
			int xOffset = leftInset - borderSize;
			int yOffset = topInset - borderSize;
			g.setColor(color);
			int n = (image == null) ? 1 : borderSize;
			for (int counter = 0; counter < n; counter++) {
				g.drawRect(rect.x + xOffset + counter,
						   rect.y + yOffset + counter,
						   rect.width - counter - counter - xOffset -xOffset-1,
						   rect.height - counter - counter -yOffset-yOffset-1);
			}
		}
	}

	public float getPreferredSpan(int axis) {
		sync();

		// If the attributes specified a width/height, always use it!
		if (axis == View.X_AXIS && (state & WIDTH_FLAG) == WIDTH_FLAG) {
			getPreferredSpanFromAltView(axis);
			return width + leftInset + rightInset;
		}
		if (axis == View.Y_AXIS && (state & HEIGHT_FLAG) == HEIGHT_FLAG) {
			getPreferredSpanFromAltView(axis);
			return height + topInset + bottomInset;
		}

		Image image = getImage();

		if (image != null) {
			switch (axis) {
			case View.X_AXIS:
				return width + leftInset + rightInset;
			case View.Y_AXIS:
				return height + topInset + bottomInset;
			default:
				throw new IllegalArgumentException("Invalid axis: " + axis);
			}
		}
		else {
			View view = getAltView();
			float retValue = 0f;

			if (view != null) {
				retValue = view.getPreferredSpan(axis);
			}
			switch (axis) {
			case View.X_AXIS:
				return retValue + (float)(width + leftInset + rightInset);
			case View.Y_AXIS:
				return retValue + (float)(height + topInset + bottomInset);
			default:
				throw new IllegalArgumentException("Invalid axis: " + axis);
			}
		}
	}

	public float getAlignment(int axis) {
		switch (axis) {
		case View.Y_AXIS:
			return vAlign;
		default:
			return super.getAlignment(axis);
		}
	}

	public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
		int p0 = getStartOffset();
		int p1 = getEndOffset();
		if ((pos >= p0) && (pos <= p1)) {
			Rectangle r = a.getBounds();
			if (pos == p1) {
				r.x += r.width;
			}
			r.width = 0;
			return r;
		}
		return null;
	}

	public int viewToModel(float x, float y, Shape a, Position.Bias[] bias) {
		Rectangle alloc = (Rectangle) a;
		if (x < alloc.x + alloc.width) {
			bias[0] = Position.Bias.Forward;
			return getStartOffset();
		}
		bias[0] = Position.Bias.Backward;
		return getEndOffset();
	}

	public void setSize(float width, float height) {
		sync();

		if (getImage() == null) {
			View view = getAltView();

			if (view != null) {
				view.setSize(Math.max(0f, width - (float)(DEFAULT_WIDTH + leftInset + rightInset)),
							 Math.max(0f, height - (float)(topInset + bottomInset)));
			}
		}
	}

	private boolean isLink() {
		return ((state & LINK_FLAG) == LINK_FLAG);
	}

	private boolean hasPixels(Image image) {
		return image != null &&
			(image.getHeight(imageObserver) > 0) &&
			(image.getWidth(imageObserver) > 0);
	}

	private float getPreferredSpanFromAltView(int axis) {
		if (getImage() == null) {
			View view = getAltView();

			if (view != null) {
				return view.getPreferredSpan(axis);
			}
		}
		return 0f;
	}

	private void repaint(long delay) {
		if (container != null && fBounds != null) {
			container.repaint(delay, fBounds.x, fBounds.y, fBounds.width,
							   fBounds.height);
		}
	}

	private int getIntAttr(HTML.Attribute name, int deflt) {
		AttributeSet attr = getElement().getAttributes();
		if (attr.isDefined(name)) {			 // does not check parents!
			int i;
			String val = (String)attr.getAttribute(name);
			if (val == null) {
				i = deflt;
			}
			else {
				try{
					i = Math.max(0, Integer.parseInt(val));
				}catch( NumberFormatException x ) {
					i = deflt;
				}
			}
			return i;
		} else
			return deflt;
	}

	private void sync() {
		int s = state;
		if ((s & RELOAD_IMAGE_FLAG) != 0) {
			refreshImage();
		}
		s = state;
		if ((s & RELOAD_FLAG) != 0) {
			synchronized(this) {
				state = (state | RELOAD_FLAG) ^ RELOAD_FLAG;
			}
			setPropertiesFromAttributes();
		}
	}

	private void refreshImage() {
		synchronized(this) {
			// clear out width/height/realoadimage flag and set loading flag
			state = (state | LOADING_FLAG | RELOAD_IMAGE_FLAG | WIDTH_FLAG |
					 HEIGHT_FLAG) ^ (WIDTH_FLAG | HEIGHT_FLAG |
									 RELOAD_IMAGE_FLAG);
			image = null;
			width = height = 0;
		}

		try {
			// Load the image
			loadImage();

			// And update the size params
			updateImageSize();
		}
		finally {
			synchronized(this) {
				// Clear out state in case someone threw an exception.
				state = (state | LOADING_FLAG) ^ LOADING_FLAG;
			}
		}
	}

	private void loadImage() {
		URL src = getImageURL();
		Image newImage = null;
		if (src != null) {
			Dictionary cache = (Dictionary)getDocument().
									getProperty(IMAGE_CACHE_PROPERTY);
			if (cache != null) {
				newImage = (Image)cache.get(src);
			}
			else {
				newImage = Toolkit.getDefaultToolkit().createImage(src);
				if (newImage != null && getLoadsSynchronously()) {
					// Force the image to be loaded by using an ImageIcon.
					ImageIcon ii = new ImageIcon();
					ii.setImage(newImage);
				}
			}
		}
		image = newImage;
	}

	private void updateImageSize() {
		int newWidth = 0;
		int newHeight = 0;
		int newState = 0;
		Image newImage = getImage();

		if (newImage != null) {
			Element elem = getElement();
			AttributeSet attr = elem.getAttributes();

			// Get the width/height and set the state ivar before calling
			// anything that might cause the image to be loaded, and thus the
			// ImageHandler to be called.
			newWidth = getIntAttr(HTML.Attribute.WIDTH, -1);
			if (newWidth > 0) {
				newState |= WIDTH_FLAG;
			}
			newHeight = getIntAttr(HTML.Attribute.HEIGHT, -1);
			if (newHeight > 0) {
				newState |= HEIGHT_FLAG;
			}

			if (newWidth <= 0) {
				newWidth = newImage.getWidth(imageObserver);
				if (newWidth <= 0) {
					newWidth = DEFAULT_WIDTH;
				}
			}

			if (newHeight <= 0) {
				newHeight = newImage.getHeight(imageObserver);
				if (newHeight <= 0) {
					newHeight = DEFAULT_HEIGHT;
				}
			}

			// Make sure the image starts loading:
			if ((newState & (WIDTH_FLAG | HEIGHT_FLAG)) != 0) {
				Toolkit.getDefaultToolkit().prepareImage(newImage, newWidth, newHeight, imageObserver);
			} else {
				Toolkit.getDefaultToolkit().prepareImage(newImage, -1, -1, imageObserver);
			}

			boolean createText = false;
			synchronized(this) {
				if (image != null) {
					if ((newState & WIDTH_FLAG) == WIDTH_FLAG || width == 0) {
						width = newWidth;
					}
					if ((newState & HEIGHT_FLAG) == HEIGHT_FLAG || height == 0) {
						height = newHeight;
					}
				} else {
					createText = true;
					if ((newState & WIDTH_FLAG) == WIDTH_FLAG) {
						width = newWidth;
					}
					if ((newState & HEIGHT_FLAG) == HEIGHT_FLAG) {
						height = newHeight;
					}
				}
				state = state | newState;
				state = (state | LOADING_FLAG) ^ LOADING_FLAG;
			}
			if (createText) {
				// Only reset if this thread determined image is null
				updateAltTextView();
			}
		}
		else {
			width = height = DEFAULT_HEIGHT;
			updateAltTextView();
		}
	}

	private void updateAltTextView() {
		String text = getAltText();

		if (text != null) {
			ImageLabelView newView;

			newView = new ImageLabelView(getElement(), text);
			synchronized(this) {
				altView = newView;
			}
		}
	}

	private View getAltView() {
		View view;

		synchronized(this) {
			view = altView;
		}
		if (view != null && view.getParent() == null) {
			view.setParent(getParent());
		}
		return view;
	}

	private void safePreferenceChanged() {
		if (SwingUtilities.isEventDispatchThread()) {
			Document doc = getDocument();
			if (doc instanceof AbstractDocument) {
				((AbstractDocument)doc).readLock();
			}
			preferenceChanged(null, true, true);
			if (doc instanceof AbstractDocument) {
				((AbstractDocument)doc).readUnlock();
			}
		}
		else {
			SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						safePreferenceChanged();
					}
				});
		}
	}

	private class ImageHandler implements ImageObserver {
		public boolean imageUpdate(Image img, int flags, int x, int y,
								   int newWidth, int newHeight ) {
			if (img != image && img != disabledImage ||
				image == null || getParent() == null) {

				return false;
			}

			if ((flags & (ABORT|ERROR)) != 0) {
				repaint(0);
				synchronized(ImageView.this) {
					if (image == img) {
						// Be sure image hasn't changed since we don't
						// initialy synchronize
						image = null;
						if ((state & WIDTH_FLAG) != WIDTH_FLAG) {
							width = DEFAULT_WIDTH;
						}
						if ((state & HEIGHT_FLAG) != HEIGHT_FLAG) {
							height = DEFAULT_HEIGHT;
						}
					} else {
						disabledImage = null;
					}
					if ((state & LOADING_FLAG) == LOADING_FLAG) {
						// No need to resize or repaint, still in the process
						// of loading.
						return false;
					}
				}
				updateAltTextView();
				safePreferenceChanged();
				return false;
			}

			if (image == img) {
				short changed = 0;
				if ((flags & ImageObserver.HEIGHT) != 0 && !getElement().
					  getAttributes().isDefined(HTML.Attribute.HEIGHT)) {
					changed |= 1;
				}
				if ((flags & ImageObserver.WIDTH) != 0 && !getElement().
					  getAttributes().isDefined(HTML.Attribute.WIDTH)) {
					changed |= 2;
				}

				synchronized(ImageView.this) {
					if ((changed & 1) == 1 && (state & WIDTH_FLAG) == 0) {
						width = newWidth;
					}
					if ((changed & 2) == 2 && (state & HEIGHT_FLAG) == 0) {
						height = newHeight;
					}
					if ((state & LOADING_FLAG) == LOADING_FLAG) {
						// No need to resize or repaint, still in the process of
						// loading.
						return true;
					}
				}
				if (changed != 0) {
					// May need to resize myself, asynchronously:
					safePreferenceChanged();
					return true;
				}
			}

			if ((flags & (FRAMEBITS|ALLBITS)) != 0) {
				repaint(0);
			}
			else if ((flags & SOMEBITS) != 0 && sIsInc) {
				repaint(sIncRate);
			}
			return ((flags & ALLBITS) == 0);
		}
	}

	private class ImageLabelView extends InlineView {
		private Segment segment;
		private Color fg;

		ImageLabelView(Element e, String text) {
			super(e);
			reset(text);
		}

		public void reset(String text) {
			segment = new Segment(text.toCharArray(), 0, text.length());
		}

		public void paint(Graphics g, Shape a) {
			GlyphPainter painter = getGlyphPainter();

			if (painter != null) {
				g.setColor(getForeground());
				painter.paint(this, g, a, getStartOffset(), getEndOffset());
			}
		}

		public Segment getText(int p0, int p1) {
			if (p0 < 0 || p1 > segment.array.length) {
				throw new RuntimeException("ImageLabelView: Stale view");
			}
			segment.offset = p0;
			segment.count = p1 - p0;
			return segment;
		}

		public int getStartOffset() {
			return 0;
		}

		public int getEndOffset() {
			return segment.array.length;
		}

		public View breakView(int axis, int p0, float pos, float len) {
			return this;
		}

		public Color getForeground() {
			View parent;
			if (fg == null && (parent = getParent()) != null) {
				Document doc = getDocument();
				AttributeSet attr = parent.getAttributes();

				if (attr != null && (doc instanceof StyledDocument)) {
					fg = ((StyledDocument)doc).getForeground(attr);
				}
			}
			return fg;
		}
	}
}
