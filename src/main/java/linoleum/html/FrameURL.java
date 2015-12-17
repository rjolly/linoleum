package linoleum.html;

import java.net.URL;
import java.util.Map;
import java.util.HashMap;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import javax.swing.text.Element;

public class FrameURL {
	private final URL url;
	private final Map<String, URL> map = new HashMap<>();

	public FrameURL(final FrameURL dest, final HTMLFrameHyperlinkEvent evt) {
		this(evt.getTarget().equals("_top")?evt.getURL():dest.getURL());
		final Element element = evt.getSourceElement();
	 	final String name = evt.getTarget();
		final URL url = evt.getURL();
		if (name.equals("_top")) {
		} else if (name.equals("_self")) {
			map.put(getName(element), url);
		} else if (name.equals("_parent")) {
			map.put(getName(element.getParentElement()), url);
		} else {
			map.put(name, url);
		}
	}

	public FrameURL(final URL url) {
		this.url = url;
	}

	public URL getURL() {
		return url;
	}

	public Map<String, URL> getMap() {
		return map;
	}

	public void open(final JEditorPane panel) {
		final javax.swing.text.Document d = panel.getDocument();
		if (d instanceof Document) {
			final Document doc = (Document)d;
			for (final Map.Entry<String, URL> entry : map.entrySet()) {
				doc.processHTMLFrameHyperlinkEvent(new HTMLFrameHyperlinkEvent(panel, HyperlinkEvent.EventType.ACTIVATED, entry.getValue(), entry.getKey()));
			}
			map.putAll(doc.getFrames());
		}
	}

	public String toString() {
		return "[" + url + ", " + map + "]";
	}

	public boolean equals(final Object obj) {
		if (obj instanceof FrameURL) {
			final FrameURL that = (FrameURL)obj;
			return url.equals(that.url) && map.equals(that.map);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return 31 * url.hashCode() + map.hashCode();
	}

	public static FrameURL create(final FrameURL url, final HyperlinkEvent evt) {
		return evt instanceof HTMLFrameHyperlinkEvent?new FrameURL(url, (HTMLFrameHyperlinkEvent)evt):new FrameURL(evt.getURL());
	}

	private static String getName(final Element element) {
	    return (String)element.getAttributes().getAttribute(HTML.Attribute.NAME);
	}
}
