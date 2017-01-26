package linoleum.xhtml;

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

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
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
		String content = null;
		JComponent cc = null;
		try {
			Element elem = box.getElement();
			if (elem == null || ! isSVGEmbedded(elem)) {
				return null;
			}

			content = getSVGElementContent(elem);
			cc = getJComponent(getSVGElementIcon(elem));
		} catch (final Exception e) {
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

	private Icon getSVGElementIcon(final Element elem) throws Exception {
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder builder = factory.newDocumentBuilder();
		final Document doc = builder.newDocument();
		doc.appendChild(doc.importNode(elem, true));
		final Image image = createImage(doc);
		return new ImageIcon(image);
	}

	private Image createImage(final Document doc) throws Exception {
		final MemoryTranscoder transcoder = new MemoryTranscoder();
		transcoder.transcode(new TranscoderInput(doc), new TranscoderOutput());
		return transcoder.getImage();
	}

	private String getSVGElementContent(final Element elem) {
		if ( elem.getChildNodes().getLength() > 0 ) {
			return elem.getFirstChild().getNodeValue();
		} else {
			return "SVG";
		}
	}

	private boolean isSVGEmbedded(final Element elem) {
		return elem.getNodeName().equals("svg");
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
