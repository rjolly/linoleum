package linoleum;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.net.URL;
import linoleum.application.event.ClassPathListener;
import linoleum.application.event.ClassPathChangeEvent;

public class Packages {
	private final List<ClassPathListener> listeners = new ArrayList<>();
	private final Map<String, File> installed = new HashMap<>();
	private final Map<String, File> map = new HashMap<>();
	private final File lib = new File("lib");
	private final FileFilter filter = new FileFilter() {
		public boolean accept(final File file) {
			return file.isFile() && file.getName().endsWith(".jar");
		}
	};

	public Packages() {
		final String extdirs[] = System.getProperty("java.ext.dirs").split(File.pathSeparator);
		for (final String str : extdirs) {
			final File dir = new File(str);
			if (dir.isDirectory()) {
				for (final File file : dir.listFiles(filter)) {
					map.put(new Package(file).getName(), file);
				}
			}
		}
		final String classpath[] = System.getProperty("java.class.path").split(File.pathSeparator);
		for (final String str : classpath) {
			final File file = new File(str);
			if (file.isFile() && file.getName().endsWith(".jar")) {
				put(new Package(file).getName(), file);
			}
		}
		if (classpath.length > 0) {
			final File jar = new File(classpath[0]);
			try {
				final URL url = new URL("jar:" + jar.toURI().toURL() + "!/META-INF/MANIFEST.MF");
				final Manifest manifest = new Manifest(url.openStream());
				final String cp = (String)manifest.getMainAttributes().get(Attributes.Name.CLASS_PATH);
				if (cp != null) for (final String str : cp.split(" ")) {
					final File file = new File(jar.getParentFile(), str);
					if (file.isFile() && file.getName().endsWith(".jar")) {
						put(new Package(file).getName(), file);
					}
				}
			} catch (final IOException e) {}
		}
		final File tools = new File(new File(System.getProperty("java.home")), "../lib/tools.jar");
		if (tools.exists()) {
			add(tools);
		}
		final File home = home();
		if (!home.equals(new File(""))) {
			final File lib = new File(home, "lib");
			if (lib.isDirectory()) {
				for (final File file: lib.listFiles(filter)) {
					add(file);
				}
			}
		}
		lib.mkdir();
		if (lib.isDirectory()) {
			for (final File file: lib.listFiles(filter)) {
				add(file);
			}
		}
	}

	public void addClassPathListener(final ClassPathListener listener) {
		listeners.add(listener);
	}

	public void removeClassPathListener(final ClassPathListener listener) {
		listeners.remove(listener);
	}

	public void fireClassPathChange(final ClassPathChangeEvent evt) {
		for (final ClassPathListener listener : listeners) {
			listener.classPathChanged(evt);
		}
	}

	public File[] installed() {
		return installed.values().toArray(new File[0]);
	}

	public File home() {
		return (map.containsKey("linoleum")?map.get("linoleum"):lib).getParentFile();
	}

	public File lib() {
		return lib;
	}

	public void add(final File file) {
		final Package pkg = new Package(file);
		final String name = pkg.getName();
		if (!pkg.isSourcesOrJavadoc() && !map.containsKey(name)) try {
			((ClassLoader)ClassLoader.getSystemClassLoader()).addURL(file.toURI().toURL());
			put(name, file);
		} catch (final Exception e) {}
	}

	private void put(final String name, final File file) {
		map.put(name, file);
		installed.put(name, file);
	}
}
