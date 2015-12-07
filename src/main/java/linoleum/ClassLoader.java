package linoleum;

import java.net.URL;
import java.net.URLClassLoader;

public class ClassLoader extends URLClassLoader {

	public ClassLoader(final java.lang.ClassLoader parent) {
		super(new URL[] {}, parent);
	}

	public void addURL(final URL url) {
		super.addURL(url);
	}
}
