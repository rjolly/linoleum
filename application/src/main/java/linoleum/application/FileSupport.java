package linoleum.application;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.DirectoryStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

public class FileSupport extends PreferenceSupport {
	public final List<Path> listFiles(final Path path) {
		final List<Path> list = new ArrayList<>();
		try (final DirectoryStream<Path> stream = Files.newDirectoryStream(path, new DirectoryStream.Filter<Path>() {
			public boolean accept(final Path entry) {
				return canOpen(entry);
			}
		})) {
			for (final Path entry : stream) {
				list.add(entry);
			}
		} catch (final IOException ex) {
			ex.printStackTrace();
		}
		return Collections.unmodifiableList(list);
	}

	public final Path getPath(final URI uri) {
		try {
			return uri.isOpaque()?"file".equals(uri.getScheme())?Paths.get(uri.getSchemeSpecificPart()):Paths.get(new URI(uri.getScheme(), uri.getSchemeSpecificPart(), null)):Paths.get(new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, null));
		} catch (final FileSystemNotFoundException ex) {
		} catch (final URISyntaxException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@Override
	MimeType getMimeType(final URI uri) {
		final Path path = getPath(uri);
		return path == null?super.getMimeType(uri):getMimeType(path);
	}

	private MimeType getMimeType(final Path path) {
		try {
			return new MimeType(Files.probeContentType(path));
		} catch (final IOException | MimeTypeParseException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	protected boolean canOpen(final Path entry) {
		final MimeType type = getMimeType(entry);
		return type == null?false:canOpen(type);
	}

	protected Path relativize(final Path path) {
		final Path user = Paths.get(System.getProperty("user.dir"));
		return path.startsWith(user)?user.relativize(path):path;
	}

	protected Path getParent(final Path path) {
		final Path parent = path.getParent();
		return parent == null?Paths.get(""):parent;
	}

	protected Path unfile(final Path path) {
		return Files.isDirectory(path)?path:getParent(path);
	}
}
