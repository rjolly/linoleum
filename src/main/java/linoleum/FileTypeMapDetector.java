package linoleum;

import java.nio.file.Path;
import java.nio.file.spi.FileTypeDetector;
import javax.activation.FileTypeMap;

public class FileTypeMapDetector extends FileTypeDetector {
	public String probeContentType(final Path path) {
		return FileTypeMap.getDefaultFileTypeMap().getContentType(path.toString());
	}
}
