package linoleum;

import java.io.File;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.retrieve.RetrieveOptions;
import org.apache.ivy.core.retrieve.RetrieveReport;
import org.apache.ivy.core.settings.IvySettings;

public class PackageManager {
	private final File lib;
	private final Desktop desktop;
	private final ClassLoader loader = (ClassLoader)ClassLoader.getSystemClassLoader();

	public PackageManager(final Desktop desktop, final File lib) {
		this.desktop = desktop;
		this.lib = lib;
		for (final File file: lib.listFiles()) {
			loader.add(file);
		}
	}

	public File getLib() {
		return lib;
	}

	public void install(final String name, final String conf) throws Exception {
		final IvySettings ivySettings = new IvySettings();
		ivySettings.loadDefault();
		final Ivy ivy = Ivy.newInstance(ivySettings);
		final ModuleRevisionId mRID = ModuleRevisionId.parse(name);
		final ResolveOptions resolveOptions = new ResolveOptions();
		resolveOptions.setConfs(new String[]{conf});
		final ResolveReport resolveReport = ivy.resolve(mRID, resolveOptions, true);
		final ModuleDescriptor md = resolveReport.getModuleDescriptor();
		final RetrieveOptions retrieveOptions = new RetrieveOptions();
		retrieveOptions.setDestArtifactPattern(lib.getPath() + "/[artifact]-[revision].[ext]");
		final RetrieveReport retrieveReport = ivy.retrieve(md.getModuleRevisionId(), retrieveOptions);
		for (final Object obj : retrieveReport.getCopiedFiles()) {
			loader.add((File)obj);
		}
		desktop.getApplicationManager().refresh();
	}
}
