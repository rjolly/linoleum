package linoleum;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class FileTransferable implements Transferable {
	private final DataFlavor flavors[] = new DataFlavor[] {DataFlavor.javaFileListFlavor};
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
		return flavor == flavors[0];
	}

	@Override
	public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (!isDataFlavorSupported(flavor)) {
			throw new UnsupportedFlavorException(flavor);
		}
		return list;
	}
}
