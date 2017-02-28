package linoleum;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

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

	public void setSelectedValue(final Path path) {
		for (int i = 0 ; i < getRowCount() ; i++) {
			if (path.equals(getValueAt(i, 0))) {
				setRowSelectionInterval(i, i);
				break;
			}
		}
	}

	@SuppressWarnings("unchecked")
	void fixRowSorter() {
		final TableRowSorter<? extends TableModel> sorter = (TableRowSorter<? extends TableModel>) getRowSorter();
		sorter.setComparator(0, new DirectoryFirstComparator((Comparator<Path>) sorter.getComparator(0)));
	}
}

class DirectoryFirstComparator implements Comparator<Path> {
	private final Comparator<Path> comparator;

	DirectoryFirstComparator(final Comparator<Path> comparator) {
		this.comparator = comparator;
	}

	public int compare(final Path p1, final Path p2) {
		final boolean d1 = Files.isDirectory(p1);
		final boolean d2 = Files.isDirectory(p2);	
		if (d1 && !d2) {
			return -1;
		}
		if (!d1 && d2) {
			return 1;
		}
		return comparator.compare(p1, p2);
	}
}
