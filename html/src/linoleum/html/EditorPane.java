package linoleum.html;

import java.awt.Rectangle;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FilterInputStream;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.CookieManager;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.AttributeSet;
import javax.swing.text.ChangedCharSetException;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

public class EditorPane extends JEditorPane {
	private final Basic basic = new Basic();
	private final Map<String, String> map = new HashMap<>();
	private final CookieManager manager = new CookieManager();
	private final Logger logger = Logger.getLogger(getClass().getName());

	public void setPage(final URL page) throws IOException {
		setPage(new FrameURL(page), null, false);
	}

	public void setPage(final FrameURL dest, final PageLoader loader, final boolean force) throws IOException {
		final URL page = dest.getURL();
		final String reference = page.getRef();
		final URL loaded = getPage();
		if (!page.equals(loaded) && reference == null) {
			scrollRectToVisible(new Rectangle(0, 0, 1, 1));
		}
		final Object postData = getPostData();
		if ((loaded == null) || !loaded.sameFile(page) || force || (postData != null)) {
			final InputStream in = getStream(page, loader);
			final EditorKit kit = getEditorKit();
			if (kit != null) {
				final Document doc = initializeModel(kit, page);
				read(in, doc);
				setDocument(doc);
			}
		} else {
			getDocument().putProperty(Document.StreamDescriptionProperty, page);
		}
		if (reference != null) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					scrollToReference(reference);
				}
			});
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				dest.open(EditorPane.this);
			}
		});
		firePropertyChange("page", loaded, page);
	}

	private Document initializeModel(final EditorKit kit, final URL page) {
		final Document doc = kit.createDefaultDocument();
		if (pageProperties != null) {
			for (final Enumeration<String> e = pageProperties.keys(); e.hasMoreElements();) {
				final String key = e.nextElement();
				doc.putProperty(key, pageProperties.get(key));
			}
			pageProperties.clear();
		}
		if (doc.getProperty(Document.StreamDescriptionProperty) == null) {
			doc.putProperty(Document.StreamDescriptionProperty, page);
		}
		return doc;
	}

	private void read(InputStream in, final Document doc) throws IOException {
		if (! Boolean.TRUE.equals(doc.getProperty("IgnoreCharsetDirective"))) {
			final int READ_LIMIT = 1024 * 10;
			in = new BufferedInputStream(in, READ_LIMIT);
			in.mark(READ_LIMIT);
		}
		try {
			final String charset = (String) getClientProperty("charset");
			final Reader r = (charset != null)?new InputStreamReader(in, charset):new InputStreamReader(in);
			getEditorKit().read(r, doc, 0);
			final URL url = (URL)doc.getProperty(Document.StreamDescriptionProperty);
			if (url != null) {
				map.remove(url.getHost());
			}
		} catch (final BadLocationException e) {
			throw new IOException(e.getMessage());
		} catch (final ChangedCharSetException ccse) {
			final String spec = ccse.getCharSetSpec();
			if (ccse.keyEqualsCharSet()) {
				putClientProperty("charset", spec);
			} else {
				setCharsetFromContentTypeParameters(spec);
			}
			try {
				in.reset();
			} catch (final IOException exception) {
				//mark was invalidated
				in.close();
				final URL url = (URL)doc.getProperty(Document.StreamDescriptionProperty);
				if (url != null) {
					in = getStream(url, null);
				} else {
					//there is nothing we can do to recover stream
					throw ccse;
				}
			}
			try {
				doc.remove(0, doc.getLength());
			} catch (final BadLocationException e) {}
			doc.putProperty("IgnoreCharsetDirective", Boolean.valueOf(true));
			read(in, doc);
		}
	}

	private InputStream getStream(final URL page, final PageLoader loader) throws IOException {
		return getStream(page, loader, "");
	}

	protected InputStream getStream(final URL page, final PageLoader loader, final String auth) throws IOException {
		final URLConnection conn = page.openConnection();
		final String host = conn.getURL().getHost();
		if (auth.startsWith("Basic")) {
			if (!map.containsKey(host)) {
				map.put(host, basic.auth(this));
			}
			conn.addRequestProperty("Authorization", map.get(host));
		}
		if (conn instanceof HttpURLConnection) {
			final HttpURLConnection hconn = (HttpURLConnection) conn;
			hconn.setInstanceFollowRedirects(false);
			useCookies(conn);
			final Object postData = getPostData();
			if (postData != null) {
				handlePostData(hconn, postData);
			}
			final int response = hconn.getResponseCode();
			final boolean redirect = (response >= 300 && response <= 399);
			handleCookies(conn);
			if (redirect) {
				clearPostData();
				final String loc = conn.getHeaderField("Location");
				return getStream(loc.startsWith("http", 0)?new URL(loc):new URL(page, loc), loader);
			}
			if (auth.isEmpty() && response == HttpURLConnection.HTTP_UNAUTHORIZED) {
				return getStream(page, loader, conn.getHeaderField("WWW-authenticate"));
			}
		}
		handleConnectionProperties(conn);
		try {
			return new PageStream(conn.getInputStream(), loader, conn.getContentLength());
		} catch (final IOException ex) {
			map.remove(host);
			throw ex;
		}
	}

	private static class PageStream extends FilterInputStream {
		private final PageLoader loader;
		private int n;

		public PageStream(final InputStream in, final PageLoader loader, final int length) {
			super(in);
			if (loader != null) loader.setLength(length);
			this.loader = loader;
		}

		public int read(final byte b[], final int off, final int len) throws IOException {
			final int n = super.read(b, off, len);
			if (n > -1) {
				this.n += n;
			}
			if (loader != null) loader.setNumber(this.n);
			return n;
		}
	}

	private void handleConnectionProperties(final URLConnection conn) throws IOException {
		if (pageProperties == null) {
			pageProperties = new Hashtable<String, Object>();
		}
		final String type = conn.getContentType();
		if (type != null) {
			setContentType(type);
			pageProperties.put("content-type", type);
		}
		pageProperties.put(Document.StreamDescriptionProperty, conn.getURL());
		final String enc = conn.getContentEncoding();
		if (enc != null) {
			pageProperties.put("content-encoding", enc);
		}
	}

	private void handleCookies(final URLConnection conn) throws IOException {
		final Map<String, List<String>> map = new HashMap<>();
		final Map<String, List<String>> fields = conn.getHeaderFields();
		logger.config(fields.toString());
		for (final Map.Entry<String, List<String>> entry : fields.entrySet()) {
			final String key = entry.getKey();
			if ("Set-Cookie".equals(key)) {
				final List<String> list = new ArrayList<>();
				for (final String value : entry.getValue()) {
					list.add(value.replace(" -0000", " GMT"));
				}
				map.put(key, list);
			} else {
				map.put(key, entry.getValue());
			}
		}
		try {
			manager.put(conn.getURL().toURI(), map);
		} catch (final URISyntaxException ex) {
			ex.printStackTrace();
		}
	}

	private void useCookies(final URLConnection conn) throws IOException {
		try {
			for (final Map.Entry<String, List<String>> entry : manager.get(conn.getURL().toURI(), conn.getRequestProperties()).entrySet()) {
				for (final String value : entry.getValue()) {
					conn.addRequestProperty(entry.getKey(), value);
				}
			}
		} catch (final URISyntaxException ex) {
			ex.printStackTrace();
		}
		logger.config(conn.getRequestProperties().toString());
	}

	private void clearPostData() {
		getDocument().putProperty(PostDataProperty, null);
	}

	private Object getPostData() {
		return getDocument().getProperty(PostDataProperty);
	}

	private void handlePostData(final HttpURLConnection conn, final Object postData) throws IOException {
		final String str = (String)postData;
		logger.config(URLDecoder.decode(str, "UTF-8"));
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		try (final DataOutputStream os = new DataOutputStream(conn.getOutputStream())) {
			os.writeBytes(str);
		}
	}

	public void scrollToReference(final String reference) {
		final Document d = getDocument();
		if (d instanceof HTMLDocument) {
			final HTMLDocument doc = (HTMLDocument) d;
			final HTMLDocument.Iterator iter = doc.getIterator(HTML.Tag.A);
			for (; iter.isValid(); iter.next()) {
				final AttributeSet a = iter.getAttributes();
				final String nm = (String) a.getAttribute(HTML.Attribute.NAME);
				final String id = (String) a.getAttribute(HTML.Attribute.ID);
				if (nm != null?nm.equals(reference):id != null && id.equals(reference)) {
					// found a matching reference in the document.
					try {
						final Rectangle r = modelToView(iter.getStartOffset());
						if (r != null) {
							// the view is visible, scroll it to the 
							// center of the current visible area.
							final Rectangle vis = getVisibleRect();
							//r.y -= (vis.height / 2);
							r.height = vis.height;
							scrollRectToVisible(r);
						}
					} catch (final BadLocationException ble) {
						UIManager.getLookAndFeel().provideErrorFeedback(this);
					}
				}
			}
		}
	}

	private void setCharsetFromContentTypeParameters(String paramlist) {
		String charset = null;
		try {
			// paramlist is handed to us with a leading ';', strip it.
			final int semi = paramlist.indexOf(';');
			if (semi > -1 && semi < paramlist.length()-1) {
				paramlist = paramlist.substring(semi + 1);
			}

			if (paramlist.length() > 0) {
				// parse the paramlist into attr-value pairs & get the
				// charset pair's value
				final HeaderParser parser = new HeaderParser(paramlist);
				charset = parser.findValue("charset");
				if (charset != null) {
					putClientProperty("charset", charset);
				}
			}
		} catch (final IndexOutOfBoundsException e) {
			// malformed parameter list, use charset we have
		} catch (final NullPointerException e) {
			// malformed parameter list, use charset we have
		} catch (final RuntimeException e) {
			// malformed parameter list, use charset we have; but complain
			System.err.println("JEditorPane.getCharsetFromContentTypeParameters failed on: " + paramlist);
			e.printStackTrace();
		}
	}

	private Hashtable<String, Object> pageProperties;
	final static String PostDataProperty = "javax.swing.JEditorPane.postdata";

static class HeaderParser {

    /* table of key/val pairs - maxes out at 10!!!!*/
    String raw;
    String[][] tab;
    
    public HeaderParser(String raw) {
	this.raw = raw;
	tab = new String[10][2];
	parse();
    }

    private void parse() {
	
	if (raw != null) {
	    raw = raw.trim();
	    char[] ca = raw.toCharArray();
	    int beg = 0, end = 0, i = 0;
	    boolean inKey = true;
	    boolean inQuote = false;
	    int len = ca.length;
	    while (end < len) {
		char c = ca[end];
		if (c == '=') { // end of a key
		    tab[i][0] = new String(ca, beg, end-beg).toLowerCase();
		    inKey = false;
		    end++;
		    beg = end;
		} else if (c == '\"') {
		    if (inQuote) {
			tab[i++][1]= new String(ca, beg, end-beg);
			inQuote=false;
			do {
			    end++;
			} while (end < len && (ca[end] == ' ' || ca[end] == ','));
			inKey=true;
			beg=end;
		    } else {
			inQuote=true;
			end++;
			beg=end;
		    }
		} else if (c == ' ' || c == ',') { // end key/val, of whatever we're in
		    if (inQuote) {
			end++;
			continue;
		    } else if (inKey) {
			tab[i++][0] = (new String(ca, beg, end-beg)).toLowerCase();
		    } else {
			tab[i++][1] = (new String(ca, beg, end-beg));
		    }
		    while (end < len && (ca[end] == ' ' || ca[end] == ',')) {
			end++;
		    }
		    inKey = true;
		    beg = end;
		} else {
		    end++;
		}
	    } 
	    // get last key/val, if any
	    if (--end > beg) {
		if (!inKey) {
		    if (ca[end] == '\"') {
			tab[i++][1] = (new String(ca, beg, end-beg));
		    } else {
			tab[i++][1] = (new String(ca, beg, end-beg+1));
		    }
		} else {
		    tab[i][0] = (new String(ca, beg, end-beg+1)).toLowerCase();
		}
	    } else if (end == beg) {
		if (!inKey) {
		    if (ca[end] == '\"') {
			tab[i++][1] = String.valueOf(ca[end-1]);
		    } else {
			tab[i++][1] = String.valueOf(ca[end]);
		    }
		} else {
		    tab[i][0] = String.valueOf(ca[end]).toLowerCase();
		}
	    } 
	}
	
    }

    public String findKey(int i) {
	if (i < 0 || i > 10)
	    return null;
	return tab[i][0];
    }

    public String findValue(int i) {
	if (i < 0 || i > 10)
	    return null;
	return tab[i][1];
    }

    public String findValue(String key) {
	return findValue(key, null);
    }

    public String findValue(String k, String Default) {
	if (k == null)
	    return Default;
	k = k.toLowerCase();
	for (int i = 0; i < 10; ++i) {
	    if (tab[i][0] == null) {
		return Default;
	    } else if (k.equals(tab[i][0])) {
		return tab[i][1];
	    }
	}
	return Default;
    }

    public int findInt(String k, int Default) {
	try {
	    return Integer.parseInt(findValue(k, String.valueOf(Default)));
	} catch (Throwable t) {
	    return Default;
	}
    }
}
}
