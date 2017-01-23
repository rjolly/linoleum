package linoleum.xhtml;

import com.kitfox.svg.SVGException;
import com.kitfox.svg.app.beans.SVGPanel;
import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.FSCanvas;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;
import org.xhtmlrenderer.swing.SwingReplacedElement;
import org.xhtmlrenderer.util.XRLog;

import javax.swing.*;
import java.util.logging.Level;
import java.awt.*;

public class SVGSalamanderReplacedElementFactory implements ReplacedElementFactory {
	public ReplacedElement createReplacedElement(
			final LayoutContext c,
			final BlockBox box,
			final UserAgentCallback uac,
			final int cssWidth,
			final int cssHeight) {
		final SVGPanel panel = new SVGPanel();
		String content = null;
		JComponent cc = null;
		try {
			Element elem = box.getElement();
			if (elem == null || ! isSVGEmbedded(elem)) {
				return null;
			}

			content = getSVGElementContent(elem);

			String path = elem.getAttribute("data");
			XRLog.general(Level.FINE, "Rendering embedded SVG via object tag from: " + path);
			XRLog.general(Level.FINE, "Content is: " + content);
			panel.setAntiAlias(true);
			panel.setSvgResourcePath(path);

			int width = panel.getSVGWidth();
			int height = panel.getSVGHeight();

			if ( cssWidth > 0 ) width = cssWidth;

			if ( cssHeight > 0 ) height = cssHeight;

			String val = elem.getAttribute("width");
			if ( val != null && val.length() > 0 ) {
				width = Integer.valueOf(val).intValue();
			}
			val = elem.getAttribute("height");
			if ( val != null && val.length() > 0 ) {
				height = Integer.valueOf(val).intValue();
			}
			panel.setScaleToFit(true);
			panel.setPreferredSize(new Dimension(width, height));
			panel.setSize(panel.getPreferredSize());

			cc = panel;
		} catch (final SVGException e) {
			e.printStackTrace();
			XRLog.general(Level.WARNING, "Could not replace SVG element; rendering failed" +
					" in SVG renderer. Skipping and using blank JPanel.", e);
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

	private String getSVGElementContent(final Element elem) {
		if ( elem.getChildNodes().getLength() > 0 ) {
			return elem.getFirstChild().getNodeValue();
		} else {
			return "SVG";
		}
	}

	private boolean isSVGEmbedded(final Element elem) {
		return elem.getNodeName().equals("object") && elem.getAttribute("type").equals("image/svg+xml");
	}

	private JComponent getDefaultJComponent(final String content, final int width, final int height) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		JLabel comp = new JLabel(content);
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
