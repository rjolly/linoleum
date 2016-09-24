package linoleum;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class FileTransferable implements Transferable {
	private final DataFlavor flavors[] = new DataFlavor[] {DataFlavor.javaFileListFlavor, DataFlavor.stringFlavor};
	private final List<Path> list;

	public FileTransferable(final List<Path> list) {
		this.list = list;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	@Override
	public boolean isDataFlavorSupported(final DataFlavor flavor) {
		return flavor == flavors[0] || flavor == flavors[1];
	}

	@Override
	public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (flavor == flavors[0]) {
			return list;
		} else if (flavor == flavors[1]) {
			final StringBuilder builder = new StringBuilder();
			for (final Path entry : list) {
				builder.append(entry).append("\n");
			}
			return builder.toString();
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}
}
