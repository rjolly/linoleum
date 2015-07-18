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

	public PackageManager(final Desktop desktop, final String dir) {
		this.desktop = desktop;
		this.lib = new File(dir);
		for (final File file: listFiles()) {
			add(file);
		}
	}

	public final File[] listFiles() {
		return lib.listFiles();
	}

	public void install(final String names[]) throws Exception {
		for (final String name : names) {
			install(name);
		}
		desktop.getApplicationManager().refresh();
	}

	private void install(final String name) throws Exception {
		final IvySettings ivySettings = new IvySettings();
		ivySettings.loadDefault();
		final Ivy ivy = Ivy.newInstance(ivySettings);
		final ModuleRevisionId mRID = ModuleRevisionId.parse(name);
		final ResolveOptions resolveOptions = new ResolveOptions();
		resolveOptions.setConfs(new String[]{"default"});
		final ResolveReport resolveReport = ivy.resolve(mRID, resolveOptions, true);
		final ModuleDescriptor md = resolveReport.getModuleDescriptor();
		final RetrieveOptions retrieveOptions = new RetrieveOptions();
		retrieveOptions.setDestArtifactPattern(lib.getPath() + "/[artifact]-[revision].[ext]");
		final RetrieveReport retrieveReport = ivy.retrieve(md.getModuleRevisionId(), retrieveOptions);
		for (final Object obj : retrieveReport.getCopiedFiles()) {
			add((File)obj);
		}
	}

	private static void add(final File file) {
		((ClassLoader)ClassLoader.getSystemClassLoader()).add(file);
	}
}
