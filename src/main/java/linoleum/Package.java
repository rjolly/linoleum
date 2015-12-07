package linoleum;

import java.io.File;

public class Package {
	private final String name;
	private final String version;

	public Package(final File file) {
		final String str = file.getName();
		final String s = str.substring(0, str.lastIndexOf("."));
		final int n = s.lastIndexOf("-");
		if (n < 0) {
			name = s;
			version = "";
		} else {
			name = s.substring(0, n);
			version = s.substring(n + 1);
		}
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}
}
