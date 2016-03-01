package linoleum;

import java.io.File;

public class Package {
	private String name;
	private String version;
	private boolean snapshot;
	private boolean sources;
	private String suffix;

	public Package(final File file) {
		name = file.getName();
		int n = name.lastIndexOf(".");
		if (n > -1) {
			suffix = name.substring(n + 1);
			name = name.substring(0, n);
			if ("jar".equals(suffix)) {
				n = name.lastIndexOf("-");
				if (n > -1) {
					version = name.substring(n + 1);
					name = name.substring(0, n);
					if ("sources".equals(version) || "javadoc".equals(version)) {
						version = null;
						sources = true;
						n = name.lastIndexOf("-");
						if (n > -1) {
							version = name.substring(n + 1);
							name = name.substring(0, n);
							if ("SNAPSHOT".equals(version)) {
								version = null;
								snapshot = true;
								n = name.lastIndexOf("-");
								if (n > -1) {
									version = name.substring(n + 1);
									name = name.substring(0, n);
								}
							}
						}
					}
				}
			}
		}
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public boolean isSnapshot() {
		return snapshot;
	}

	public boolean isSourcesOrJavadoc() {
		return sources;
	}

	public String getSuffix() {
		return suffix;
	}
}
