package linoleum.html;

import java.awt.Rectangle;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;

public class EditorPane extends JEditorPane {

	public void setPage(URL page) throws IOException {
		if (page == null) {
			throw new IOException("invalid url");
		}
		URL loaded = getPage();

		// reset scrollbar
		if (!page.equals(loaded) && page.getRef() == null) {
			scrollRectToVisible(new Rectangle(0, 0, 1, 1));
		}
		boolean reloaded = false;
		Object postData = getPostData();
		if ((loaded == null) || !loaded.sameFile(page) || (postData != null)) {
			// different url or POST method, load the new content

			int p = getAsynchronousLoadPriority(getDocument());
			if (p < 0) {
				// open stream synchronously
				InputStream in = getStream(page);
				final javax.swing.text.EditorKit kit = getEditorKit();
				if (kit != null) {
					javax.swing.text.Document doc = initializeModel(kit, page);

					// At this point, one could either load up the model with no
					// view notifications slowing it down (i.e. best synchronous
					// behavior) or set the model and start to feed it on a separate
					// thread (best asynchronous behavior).
					p = getAsynchronousLoadPriority(doc);
					if (p >= 0) {
						// load asynchronously
						setDocument(doc);
//						synchronized(this) {
//							pageLoader = new PageLoader(doc, in, loaded, page);
//							pageLoader.execute();
//						}
						return;
					}
					read(in, doc);
					setDocument(doc);
					reloaded = true;
				}
			} else {
				// we may need to cancel background loading
//				if (pageLoader != null) {
//					pageLoader.cancel(true);
//				}

				// Do everything in a background thread.
				// Model initialization is deferred to that thread, too.
//				pageLoader = new PageLoader(null, null, loaded, page);
//				pageLoader.execute();
				return;
			}
		}
		final String reference = page.getRef();
		if (reference != null) {
			if (!reloaded) {
				scrollToReference(reference);
			} else {
				// Have to scroll after painted.
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						scrollToReference(reference);
					}
				});
			}
			getDocument().putProperty(Document.StreamDescriptionProperty, page);
		}
		firePropertyChange("page", loaded, page);
	}

	private javax.swing.text.Document initializeModel(javax.swing.text.EditorKit kit, URL page) {
		javax.swing.text.Document doc = kit.createDefaultDocument();
		if (pageProperties != null) {
			// transfer properties discovered in stream to the
			// document property collection.
			for (Enumeration<String> e = pageProperties.keys(); e.hasMoreElements();) {
				String key = e.nextElement();
				doc.putProperty(key, pageProperties.get(key));
			}
			pageProperties.clear();
		}
		if (doc.getProperty(Document.StreamDescriptionProperty) == null) {
			doc.putProperty(Document.StreamDescriptionProperty, page);
		}
		return doc;
	}

	private int getAsynchronousLoadPriority(javax.swing.text.Document doc) {
		return (doc instanceof AbstractDocument
			? ((AbstractDocument) doc).getAsynchronousLoadPriority() : -1);
	}

	protected InputStream getStream(URL page) throws IOException {
		final URLConnection conn = page.openConnection();
		if (conn instanceof HttpURLConnection) {
			HttpURLConnection hconn = (HttpURLConnection) conn;
			hconn.setInstanceFollowRedirects(false);
			Object postData = getPostData();
			if (postData != null) {
				handlePostData(hconn, postData);
			}
			int response = hconn.getResponseCode();
			boolean redirect = (response >= 300 && response <= 399);

			if (redirect) {
				String loc = conn.getHeaderField("Location");
				if (loc.startsWith("http", 0)) {
					page = new URL(loc);
				} else {
					page = new URL(page, loc);
				}
				return getStream(page);
			}
		}

		// Connection properties handler should be forced to run on EDT,
		// as it instantiates the EditorKit.
		if (SwingUtilities.isEventDispatchThread()) {
			handleConnectionProperties(conn);
		} else {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						handleConnectionProperties(conn);
					}
				});
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
		return conn.getInputStream();
	}

	private void handleConnectionProperties(URLConnection conn) {
		if (pageProperties == null) {
			pageProperties = new Hashtable<String, Object>();
		}
		String type = conn.getContentType();
		if (type != null) {
			setContentType(type);
			pageProperties.put("content-type", type);
		}
		pageProperties.put(Document.StreamDescriptionProperty, conn.getURL());
		String enc = conn.getContentEncoding();
		if (enc != null) {
			pageProperties.put("content-encoding", enc);
		}
	}

	private Object getPostData() {
		return getDocument().getProperty(PostDataProperty);
	}

	private void handlePostData(HttpURLConnection conn, Object postData)
		throws IOException {
		conn.setDoOutput(true);
		DataOutputStream os = null;
		try {
			conn.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
			os = new DataOutputStream(conn.getOutputStream());
			os.writeBytes((String) postData);
		} finally {
			if (os != null) {
				os.close();
			}
		}
	}

//	private SwingWorker<URL, Object> pageLoader;
	private Hashtable<String, Object> pageProperties;
	final static String PostDataProperty = "javax.swing.JEditorPane.postdata";
}
