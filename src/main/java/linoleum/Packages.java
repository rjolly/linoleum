package linoleum;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Collection;
import java.util.Collections;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.net.URL;
import linoleum.application.ApplicationManager;
import linoleum.application.event.ClassPathChangeEvent;

public class Packages {
	private final ApplicationManager apps;
	private final Map<String, File> map = new HashMap<>();
	private final SortedMap<String, File> installed = new TreeMap<>();
	private final FileFilter filter = new FileFilter() {
		public boolean accept(final File file) {
			return file.isFile() && file.getName().endsWith(".jar");
		}
	};
	private boolean changed;

	Packages(final ApplicationManager apps) {
		final String extdirs[] = System.getProperty("java.ext.dirs").split(File.pathSeparator);
		for (final String str : extdirs) try {
			final File dir = new File(str).getCanonicalFile();
			if (dir.isDirectory()) {
				for (final File file : dir.listFiles(filter)) {
					map.put(new Package(file).getName(), file);
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		final String classpath[] = System.getProperty("java.class.path").split(File.pathSeparator);
		for (final String str : classpath) try {
			final File file = new File(str).getCanonicalFile();
			if (file.isFile() && file.getName().endsWith(".jar")) {
				put(new Package(file).getName(), file);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		if (classpath.length > 0) try {
			final File jar = new File(classpath[0]).getCanonicalFile();
			final URL url = new URL("jar:" + jar.toURI().toURL() + "!/META-INF/MANIFEST.MF");
			final Manifest manifest = new Manifest(url.openStream());
			final String cp = (String)manifest.getMainAttributes().get(Attributes.Name.CLASS_PATH);
			if (cp != null) for (final String str : cp.split(" ")) {
				final File file = new File(jar.getParentFile(), str).getCanonicalFile();
				if (file.isFile() && file.getName().endsWith(".jar")) {
					put(new Package(file).getName(), file);
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		try {
			final File home = new File(System.getProperty("java.home")).getParentFile().getCanonicalFile();
			final File file = new File(home, "lib/tools.jar");
			if (file.exists()) {
				add(file);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		try {
			String str = System.getProperty("linoleum.home");
			if (str == null) {
				System.setProperty("linoleum.home", str = map.get("linoleum").getParentFile().getCanonicalPath());
			}
			final File home = new File(str).getCanonicalFile();
			if (!Files.isSameFile(home.toPath(), Paths.get("."))) {
				final File lib = new File(home, "lib");
				if (lib.isDirectory()) {
					for (final File file : lib.listFiles(filter)) {
						add(file);
					}
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		final File lib = new File("lib");
		lib.mkdir();
		if (lib.isDirectory()) {
			for (final File file : lib.listFiles(filter)) {
				add(file);
			}
		}
		this.apps = apps;
	}

	public Collection<File> installed() {
		return Collections.unmodifiableCollection(installed.values());
	}

	public void commit(final Object source) {
		if (changed) {
			apps.fireClassPathChange(new ClassPathChangeEvent(source));
			changed = false;
		}
	}

	public void add(final File file) {
		final Package pkg = new Package(file);
		final String name = pkg.getName();
		if (!pkg.isSourcesOrJavadoc() && !map.containsKey(name)) try {
			((ClassLoader) ClassLoader.getSystemClassLoader()).addURL(file.toURI().toURL());
			put(name, file);
			changed = true;
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private void put(final String name, final File file) {
		map.put(name, file);
		installed.put(name, file);
	}
}
