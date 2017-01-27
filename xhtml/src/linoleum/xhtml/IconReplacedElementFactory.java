package linoleum.xhtml;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.util.logging.Level;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.xhtmlrenderer.extend.FSCanvas;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;
import org.xhtmlrenderer.swing.SwingReplacedElement;
import org.xhtmlrenderer.util.XRLog;

public abstract class IconReplacedElementFactory implements ReplacedElementFactory {
	private final String name;

	public IconReplacedElementFactory(final String name) {
		this.name = name;
	}

	public ReplacedElement createReplacedElement(
			final LayoutContext c,
			final BlockBox box,
			final UserAgentCallback uac,
			final int cssWidth,
			final int cssHeight) {
		String content = null;
		JComponent cc = null;
		try {
			Element elem = box.getElement();
			if (elem == null || ! isIconEmbedded(elem)) {
				return null;
			}

			content = getElementContent(elem);
			cc = getJComponent(getElementIcon(elem));
		} catch (final Exception e) {
			XRLog.general(Level.WARNING, "Could not replace Icon element; rendering failed" +
					" in Icon renderer. Skipping and using blank JPanel.", e);
			cc = getDefaultJComponent(content, cssWidth, cssHeight);
		}
		if (cc == null) {
			return null;
		} else {
			SwingReplacedElement result = new SwingReplacedElement(cc);
			if (c.isInteractive()) {
				FSCanvas canvas = c.getCanvas();
				if (canvas instanceof JComponent) {
					((JComponent) canvas).add(cc);
				}
			}
			return result;
		}
	}

	private Icon getElementIcon(final Element elem) throws Exception {
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder builder = factory.newDocumentBuilder();
		final Document doc = builder.newDocument();
		doc.appendChild(doc.importNode(elem, true));
		final Image image = createImage(doc);
		return new ImageIcon(image);
	}

	public abstract Image createImage(final Document doc) throws Exception;

	private String getElementContent(final Element elem) {
		if ( elem.getChildNodes().getLength() > 0 ) {
			return elem.getFirstChild().getNodeValue();
		} else {
			return name;
		}
	}

	public boolean isIconEmbedded(final Element elem) {
		return elem.getNodeName().equals(name);
	}

	private JComponent getDefaultJComponent(final String content, final int width, final int height) {
		return getJComponent(new JLabel(content), width, height);
	}

	private JComponent getJComponent(final Icon content) {
		return getJComponent(new JLabel(content), 0, 0);
	}

	private JComponent getJComponent(final JLabel comp, final int width, final int height) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(comp, BorderLayout.CENTER);
		panel.setOpaque(false);
		if ( width > 0 && height > 0 ) {
			panel.setPreferredSize(new Dimension(width, height));
			panel.setSize(panel.getPreferredSize());
		} else {
			panel.setPreferredSize(comp.getPreferredSize());
			panel.setSize(comp.getPreferredSize());
		}
		return panel;
	}

	public void reset() {
	}

	public void remove(final Element e) {
	}

	public void setFormSubmissionListener(final FormSubmissionListener listener) {
	}
}
