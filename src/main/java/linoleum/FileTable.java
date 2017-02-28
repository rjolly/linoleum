package linoleum;

import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.UIManager;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class FileTable extends JTable {
	private final long timeFactor;
	private final StringBuilder typedString = new StringBuilder();
	private long lastTime = 1000L;

	public FileTable() {
		putClientProperty("JTable.autoStartsEdit", false);
		final Long l = (Long) UIManager.get("Table.timeFactor");
		timeFactor = (l != null) ? l : 1000L;
		addKeyListener(new KeyAdapter() {
			public void keyTyped(final KeyEvent e) {
				final int rowCount = getModel().getRowCount();
				if (rowCount == 0 || e.isAltDown() || e.isControlDown() || e.isMetaDown()) {
					return;
				}
				final InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
				final KeyStroke key = KeyStroke.getKeyStrokeForEvent(e);
				if (inputMap != null && inputMap.get(key) != null) {
					return;
				}
				int startIndex = getSelectionModel().getLeadSelectionIndex();
				if (startIndex < 0) {
					startIndex = 0;
				}
				if (startIndex >= rowCount) {
					startIndex = rowCount - 1;
				}
				final char c = e.getKeyChar();
				final long time = e.getWhen();
				if (time - lastTime < timeFactor) {
					if (typedString.length() == 1 && typedString.charAt(0) == c) {
						startIndex++;
					} else {
						typedString.append(c);
					}
				} else {
					startIndex++;
					typedString.setLength(0);
					typedString.append(c);
				}
				lastTime = time;
				if (startIndex >= rowCount) {
					startIndex = 0;
				}
				int index = getNextMatch(startIndex, rowCount - 1);
				if (index < 0 && startIndex > 0) { // wrap
					index = getNextMatch(0, startIndex - 1);
				}
				if (index >= 0) {
					getSelectionModel().setSelectionInterval(index, index);
					final Rectangle cellRect = getCellRect(index, convertColumnIndexToView(0), false);
					scrollRectToVisible(cellRect);
				}
			}
		});
	}

	private int getNextMatch(int startIndex, int finishIndex) {
		final TableModel model = getModel();
		final RowSorter<? extends TableModel> rowSorter = getRowSorter();
		final String prefix = typedString.toString();

		// Search element
		for (int index = startIndex; index <= finishIndex; index++) {
			final Path path = (Path) model.getValueAt(rowSorter.convertRowIndexToModel(index), 0);
			final String name = FileManager.getFileName(path);
			if (name.regionMatches(true, 0, prefix, 0, prefix.length())) {
				return index;
			}
		}
		return -1;
	}

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
