package linoleum;

import java.io.File;

public class Package {
	private final String name;
	private final String version;
	private final boolean snapshot;

	public Package(final File file) {
		String str = file.getName();
		str = str.substring(0, str.lastIndexOf("."));
		int n = str.lastIndexOf("-");
		snapshot = n > -1 && "SNAPSHOT".equals(str.substring(n + 1));
		if (snapshot) {
			str = str.substring(0, n);
			n = str.lastIndexOf("-");
		}
		if (n > -1) {
			name = str.substring(0, n);
			version = str.substring(n + 1);
		} else {
			name = str;
			version = "";
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
}
