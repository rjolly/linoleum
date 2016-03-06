package linoleum.pkg;

import java.io.File;
import java.util.Arrays;
import linoleum.application.Frame;
import linoleum.application.event.ClassPathChangeEvent;
import linoleum.PackageInstaller;
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

public class PackageManager extends Frame {
	private final Ivy ivy = Ivy.newInstance();
	public static PackageManager instance;

	public PackageManager() {
		super("Packages");
		setSize(300, 400);
		if (instance == null) {
			instance = this;
		}
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
		final PackageInstaller pkgs = PackageInstaller.instance;
		final ModuleRevisionId mRID = ModuleRevisionId.parse(name);
		final ResolveOptions resolveOptions = new ResolveOptions();
		resolveOptions.setConfs(new String[] { conf });
		final ResolveReport resolveReport = ivy.resolve(mRID, resolveOptions, true);
		final ModuleDescriptor md = resolveReport.getModuleDescriptor();
		final RetrieveOptions retrieveOptions = new RetrieveOptions();
		retrieveOptions.setDestArtifactPattern(pkgs.lib().getPath() + "/[artifact]-[revision](-[classifier]).[ext]");
		final RetrieveReport retrieveReport = ivy.retrieve(md.getModuleRevisionId(), retrieveOptions);
		for (final Object obj : retrieveReport.getCopiedFiles()) {
			final File file = (File)obj;
			if (file.getName().endsWith(".jar")) {
				pkgs.add(file);
			}
		}
		pkgs.fireClassPathChange(new ClassPathChangeEvent(this));
	}
}
