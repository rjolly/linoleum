package linoleum;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URLClassLoader;

public class ClassLoader extends URLClassLoader {

	public ClassLoader(final java.lang.ClassLoader parent) {
		super(((URLClassLoader)parent).getURLs(), parent);
	}

	public void add(final File file) {
		try {
			addURL(file.toURI().toURL());
		} catch (final MalformedURLException ex) {
			throw new RuntimeException(ex);
		}
	}
}
