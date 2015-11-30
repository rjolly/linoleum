package linoleum;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

public class Tools {
	public static final Tools instance = new Tools();

	public void compile(final File files[], final File classpath[], final File destDir, final String options[]) throws IOException {
		final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		try (final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {
			fileManager.setLocation(StandardLocation.CLASS_PATH, Arrays.asList(classpath));
			fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(new File[] {destDir}));
			final JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, Arrays.asList(options), null, fileManager.getJavaFileObjectsFromFiles(Arrays.asList(files)));
			task.call();
		}
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

	public void jar(final File manifest, final File dir, final File files[], final File dest) throws IOException {
		final Path path = dir.toPath();
		try (final JarOutputStream jos = new JarOutputStream(new FileOutputStream(dest), manifest.exists()?new Manifest(new FileInputStream(manifest)):new Manifest())) {
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
		Class.forName(name, true, new URLClassLoader(new URL[] {dir.toURI().toURL()})).getMethod("main", new Class[] {args.getClass()}).invoke(null, new Object[] {args});
	}
}
