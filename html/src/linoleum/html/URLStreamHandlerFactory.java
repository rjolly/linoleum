package linoleum.html;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Arrays;

public class URLStreamHandlerFactory implements java.net.URLStreamHandlerFactory {
	private final String protocols[] = new String[] {"mvn", "imap", "imaps", "geo"};

	public URLStreamHandlerFactory() {
		URL.setURLStreamHandlerFactory(this);
	}

	public URLStreamHandler createURLStreamHandler(final String protocol) {
		return Arrays.asList(protocols).contains(protocol)?new URLStreamHandler() {
			public URLConnection openConnection(final URL u) {
				return null;
			}
		}:null;
	}
}
