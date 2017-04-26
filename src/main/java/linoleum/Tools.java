package linoleum;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import linoleum.application.Frame;

public class Tools extends Frame {
	public static Tools instance;

	public Tools() {
		if (instance == null) {
			instance = this;
		}
		setSize(150, 150);
                setClosable(true);
	}

	private File[] concat(final File a[], final File b[]) {
		final File c[] = new File[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}

	public void compile(final File files[], final File destDir, final String options[]) throws IOException {
		final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		try (final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {
			fileManager.setLocation(StandardLocation.CLASS_PATH, Arrays.asList(concat(getApplicationManager().getPackages().installed(), new File[] {destDir})));
			fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(new File[] {destDir}));
			final JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, Arrays.asList(options), null, fileManager.getJavaFileObjectsFromFiles(Arrays.asList(files)));
			task.call();
		}
	}

	public void mklink(final File link, final File target) throws IOException {
		final Path s = target.toPath();
		final Path d = link.toPath();
		Files.createSymbolicLink(d, s.isAbsolute()?s:d.isAbsolute()?s.toAbsolutePath():d.getParent().relativize(s));
	}

	public void copy(final File dir, final File files[], final File dest) throws IOException {
		final Path s = dir.toPath();
		final Path d = dest.toPath();
		if (!Files.isSameFile(d, s)) for (final File file : files) {
			final File f = d.resolve(s.relativize(file.toPath())).toFile();
			f.getParentFile().mkdirs();
			try (final InputStream is = new FileInputStream(file); final OutputStream os = new FileOutputStream(f)) {
				final byte buffer[] = new byte[4096];
				int n;
				while ((n = is.read(buffer)) != -1) {
					os.write(buffer, 0, n);
				}
			}
		}
	}

	public void jar(final File dir, final File files[], final File dest) throws IOException {
		final Manifest manifest = new Manifest();
		final Attributes attr = manifest.getMainAttributes();
		attr.put(Attributes.Name.MANIFEST_VERSION, "1.0");
		jar(dir, files, dest, manifest);
	}

	public void jar(final File dir, final File files[], final File dest, final File manifest) throws IOException {
		jar(dir, files, dest, new Manifest(new FileInputStream(manifest)));
	}

	private void jar(final File dir, final File files[], final File dest, final Manifest manifest) throws IOException {
		final Path path = dir.toPath();
		try (final JarOutputStream jos = dest.getName().endsWith(".zip")?new JarOutputStream(new FileOutputStream(dest)):new JarOutputStream(new FileOutputStream(dest), manifest)) {
			for (final File file : files) {
				final JarEntry entry = new JarEntry(path.relativize(file.toPath()).toString().replace(File.separatorChar, '/'));
				try (final InputStream is = new FileInputStream(file)) {
					final byte buffer[] = new byte[4096];
					int n;
					jos.putNextEntry(entry);
					while ((n = is.read(buffer)) != -1) {
						jos.write(buffer, 0, n);
					}
					jos.closeEntry();
				}
			}
		}
	}

	public void run(final String name, final File dir, final String args[]) throws Exception {
		getApplicationManager().getPackages().add(dir);
		Class.forName(name, true, ClassLoader.getSystemClassLoader()).getMethod("main", args.getClass()).invoke(null, (Object) args);
	}
}
