package linoleum;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

public class FileTable extends JTable {
	public List<Path> getSelectedValuesList() {
		final ListSelectionModel sm = getSelectionModel();

		final int iMin = sm.getMinSelectionIndex();
		final int iMax = sm.getMaxSelectionIndex();

		if ((iMin < 0) || (iMax < 0)) {
			return Collections.emptyList();
		}

		final List<Path> selectedItems = new ArrayList<>();
		for(int i = iMin; i <= iMax; i++) {
			if (sm.isSelectedIndex(i)) {
				selectedItems.add((Path) getValueAt(i, 0));
			}
		}
		return selectedItems;
	}
}
