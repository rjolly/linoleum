package linoleum;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.net.URL;
import linoleum.application.event.ClassPathListener;
import linoleum.application.event.ClassPathChangeEvent;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.publish.PublishOptions;
import org.apache.ivy.core.retrieve.RetrieveOptions;
import org.apache.ivy.core.retrieve.RetrieveReport;
import org.apache.ivy.plugins.parser.m2.PomModuleDescriptorWriter;
import org.apache.ivy.plugins.parser.m2.PomWriterOptions;

public class PackageManager {
	public static final PackageManager instance = new PackageManager();
	private final List<ClassPathListener> listeners = new ArrayList<>();
	private final Map<String, File> installed = new HashMap<>();
	private final Map<String, File> map = new HashMap<>();
	private final File lib = new File("lib");
	private final FileFilter filter = new FileFilter() {
		public boolean accept(final File file) {
			return file.isFile() && file.getName().endsWith(".jar");
		}
	};
	private final Ivy ivy = Ivy.newInstance();

	private PackageManager() {
		init();
		populate();
		final File tools = new File(new File(System.getProperty("java.home")), "../lib/tools.jar");
		if (tools.exists()) {
			add(tools);
		}
		final File dir = new File(home(), "lib");
		if (dir.isDirectory()) {
			for (final File file: dir.listFiles(filter)) {
				add(file);
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

	public File[] installed() {
		return installed.values().toArray(new File[0]);
	}

	public File home() {
		return map.get("linoleum").getParentFile();
	}

	private void init() {
		final File settings = new File("ivysettings.xml");
		try {
			if (settings.exists()) {
				ivy.configure(settings);
			} else {
				ivy.configureDefault();
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	private void populate() {
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
	}

	private void put(final String name, final File file) {
		map.put(name, file);
		installed.put(name, file);
	}

	public void makepom(final File source, final File pom) throws Exception {
		final ResolveReport resolveReport = ivy.resolve(source);
		final ModuleDescriptor md = resolveReport.getModuleDescriptor();
		final PomWriterOptions options = new PomWriterOptions();
		options.setPrintIvyInfo(false);
		PomModuleDescriptorWriter.write(md, pom, options);
	}

	public void publish(final File source, final File dir, final String resolver) throws Exception {
		final ResolveReport resolveReport = ivy.resolve(source);
		final ModuleDescriptor md = resolveReport.getModuleDescriptor();
		final ModuleRevisionId mRID = md.getModuleRevisionId();
		final String pattern[] = new String[] { dir.getPath() + "/[artifact]-[type].[ext]", dir.getPath() + "/[artifact].[ext]" };
		final PublishOptions options = new PublishOptions();
		options.setOverwrite(true);
		if (resolver.startsWith("local")) {
			options.setSrcIvyPattern(source.getParent() + "/[artifact].[ext]");
		}
		ivy.publish(mRID, Arrays.asList(pattern), resolver, options);
	}

	public void install(final String name, final String conf) throws Exception {
		final ModuleRevisionId mRID = ModuleRevisionId.parse(name);
		final ResolveOptions resolveOptions = new ResolveOptions();
		resolveOptions.setConfs(new String[] { conf });
		final ResolveReport resolveReport = ivy.resolve(mRID, resolveOptions, true);
		final ModuleDescriptor md = resolveReport.getModuleDescriptor();
		final RetrieveOptions retrieveOptions = new RetrieveOptions();
		retrieveOptions.setDestArtifactPattern(lib.getPath() + "/[artifact]-[revision].[ext]");
		final RetrieveReport retrieveReport = ivy.retrieve(md.getModuleRevisionId(), retrieveOptions);
		for (final Object obj : retrieveReport.getCopiedFiles()) {
			final File file = (File)obj;
			if (file.getName().endsWith(".jar")) {
				add(file);
			}
		}
		fireClassPathChange(new ClassPathChangeEvent(this));
	}

	private void fireClassPathChange(final ClassPathChangeEvent evt) {
		for (final ClassPathListener listener : listeners) {
			listener.classPathChanged(evt);
		}
	}

	private void add(final File file) {
		final Package pkg = new Package(file);
		final String name = pkg.getName();
		if (!pkg.isSourcesOrJavadoc() && !map.containsKey(name)) try {
			((ClassLoader)ClassLoader.getSystemClassLoader()).addURL(file.toURI().toURL());
			put(name, file);
		} catch (final Exception e) {}
	}
}
