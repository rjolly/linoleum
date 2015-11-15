package linoleum;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
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

	public void run(final String name, final File dir, final String args[]) throws Exception {
		Class.forName(name, true, new URLClassLoader(new URL[] {dir.toURI().toURL()})).getMethod("main", new Class[] {args.getClass()}).invoke(null, new Object[] {args});
	}
}
