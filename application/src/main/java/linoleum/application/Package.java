package linoleum.application;

import java.io.File;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Package {
	private static final Pattern pattern = Pattern.compile("-\\d");
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
			final Matcher matcher = pattern.matcher(name);
			if (matcher.find()) {
				n = matcher.start();
				version = name.substring(n + 1);
				name = name.substring(0, n);
			}
			if (version != null && (version.endsWith("-sources") || version.endsWith("-javadoc"))) {
				version = version.substring(0, version.length() - 8);
				sources = true;
			}
			if (version != null && version.endsWith("-SNAPSHOT")) {
				version = version.substring(0, version.length() - 9);
				snapshot = true;
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
