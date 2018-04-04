package linoleum.application;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.net.URL;

public class Packages {
	private final Map<String, File> map = new HashMap<>();
	private final FileFilter filter = new FileFilter() {
		public boolean accept(final File file) {
			return file.isFile() && file.getName().endsWith(".jar");
		}
	};
	private final String extdirs = System.getProperty("java.ext.dirs");
	private final String classpath[] = System.getProperty("java.class.path").split(File.pathSeparator);
	private final File home = new File(System.getProperty("java.home")).getParentFile();
	private final File lib = new File("lib");

	Packages() {
		if (extdirs != null) for (final String str : extdirs.split(File.pathSeparator)) {
			final File dir = new File(str);
			if (dir.isDirectory()) {
				for (final File file : dir.listFiles(filter)) {
					map.put(new Package(file).getName(), file);
				}
			}
		}
		for (final String str : classpath) {
			final File file = normalize(new File(str));
			if (file.isFile() && file.getName().endsWith(".jar")) {
				map.put(new Package(file).getName(), file);
			}
		}
		final File jar = map.get("linoleum");
		if (jar != null) try {
			final URL url = new URL("jar:" + jar.toURI().toURL() + "!/META-INF/MANIFEST.MF");
			final Manifest manifest = new Manifest(url.openStream());
			final String cp = (String) manifest.getMainAttributes().get(Attributes.Name.CLASS_PATH);
			if (cp != null) for (final String str : cp.split(" ")) {
				final File file = new File(jar.getParentFile(), str);
				if (file.isFile() && file.getName().endsWith(".jar")) {
					final String name = new Package(file).getName();
					if (!map.containsKey(name)) {
						put(name, file);
					}
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		add1(new File(home, "lib/tools.jar"));
		add1(new File(home, "lib/jconsole.jar"));
		if (jar != null) try {
			String str = System.getProperty("linoleum.home");
			if (str == null) {
				System.setProperty("linoleum.home", str = jar.getParent());
			}
			final File home = new File(str);
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
		lib.mkdir();
		if (lib.isDirectory()) {
			for (final File file : lib.listFiles(filter)) {
				add(file);
			}
		}
	}

	private File normalize(final File file) {
		final Path path = file.toPath().normalize();
		final Path user = Paths.get(System.getProperty("user.dir"));
		return (path.startsWith(user)?user.relativize(path):path).toFile();
	}

	private void add1(final File file) {
		if (file.exists()) {
			add(file);
		}
	}

	public boolean add(final File file) {
		final Package pkg = new Package(file);
		final String name = pkg.getName();
		if (!pkg.isSourcesOrJavadoc() && !map.containsKey(name) && ClassLoader.getSystemClassLoader() instanceof ClassLoader) try {
			((ClassLoader) ClassLoader.getSystemClassLoader()).addURL(file.toURI().toURL());
			put(name, file);
			return true;
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private void put(final String name, final File file) {
		map.put(name, file);
		System.setProperty("java.class.path", System.getProperty("java.class.path") + File.pathSeparator + file);
	}
}
