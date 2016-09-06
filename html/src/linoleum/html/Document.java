package linoleum.html;

import java.net.URL;
import java.util.Map;
import java.util.HashMap;
import java.net.MalformedURLException;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import javax.swing.text.ElementIterator;
import javax.swing.text.StyleConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.JEditorPane;

public class Document extends HTMLDocument {
	public Document() {
		super(new StyleSheet());
	}

	public void setFrames(final Map<String, URL> map, final JEditorPane panel) {
		for (final Map.Entry<String, URL> entry : map.entrySet()) {
			processHTMLFrameHyperlinkEvent(new HTMLFrameHyperlinkEvent(panel, HyperlinkEvent.EventType.ACTIVATED, entry.getValue(), entry.getKey()));
		}
		map.putAll(getFrames());
	}

	public Map<String, URL> getFrames() {
		final Map<String, URL> map = new HashMap<>();
		final ElementIterator it = new ElementIterator(this);
		Element next = null;

		while ((next = it.next()) != null) {
			final AttributeSet attr = next.getAttributes();
			if (matchNameAttribute(attr, HTML.Tag.FRAME)) {
				final String target = (String)attr.getAttribute(HTML.Attribute.NAME);
				try {
					final URL url = new URL(getBase(), (String)attr.getAttribute(HTML.Attribute.SRC));
					map.put(target, url);
				} catch (final MalformedURLException e) {}
			}
		}
		return map;
	}

	private static boolean matchNameAttribute(final AttributeSet attr, final HTML.Tag tag) {
		final Object o = attr.getAttribute(StyleConstants.NameAttribute);
		if (o instanceof HTML.Tag) {
			final HTML.Tag name = (HTML.Tag) o;
			if (name == tag) {
				return true;
			}
		}
		return false;
	}
}
