package linoleum.mail;

import javax.mail.*;
import java.util.Date;
import javax.swing.table.AbstractTableModel; 

public class FolderModel extends AbstractTableModel {
	private Folder folder;
	private Message[] messages;
	private final String[] columnNames = { "Date", "From", "Subject", "Deleted"};
	private final Class[] columnTypes = { String.class, String.class, String.class, Boolean.class };
	private final boolean[] editable = { false, false, false, true };

	public void setFolder(Folder what) throws MessagingException {
		if (what != null) {
			// opened if needed
			if (!what.isOpen()) {
				what.open(Folder.READ_WRITE);
			}
			// get the messages
			messages = what.getMessages();
			cached = new Object[messages.length][];
		} else {
			messages = null;
			cached = null;
		}
		// close previous folder and switch to new folder
		if (folder != null && folder != what) {
			folder.close(true);
		}
		folder = what;
		fireTableDataChanged();
	}

	private void delete(int which, boolean value) throws MessagingException {
		messages[which].setFlag(Flags.Flag.DELETED, value);
		cached[which] = null;
		fireTableDataChanged();
	}

	public Message getMessage(int which) {
		return messages[which];
	}

	//---------------------
	// Implementation of the TableModel methods
	//---------------------

	public String getColumnName(int column) {
		return columnNames[column];
	}

	public Class getColumnClass(int column) {
		return columnTypes[column];
	}

	public int getColumnCount() {
		return columnNames.length; 
	}

	public int getRowCount() {
		if (messages == null) {
			return 0;
		}
		return messages.length;
	}

	@Override
	public boolean isCellEditable(final int rowIndex, final int columnIndex) {
		return editable[columnIndex];
	}

	@Override
	public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
		switch(columnIndex) {
		case 3:
			try {
				delete(rowIndex, (Boolean)aValue);
			} catch (final MessagingException ex) {
				ex.printStackTrace();
			}
		default:
		}
	}

	public Object getValueAt(int aRow, int aColumn) {
		switch(aColumn) {
		case 0:	// date
		case 1: // From
		case 2: // Subject
		case 3: // Deleted
			Object[] what = getCachedData(aRow);
			if (what != null) {
				return what[aColumn];
			} else {
				return "";
			}
		default:
			return "";
		}
	}

	protected static Object[][] cached;

	protected Object[] getCachedData(int row) {
		if (cached[row] == null) {
			try {
				Message m = messages[row];
				Object[] theData = new Object[4];

				// Date
				Date date = m.getSentDate();
				if (date == null) {
					theData[0] = "Unknown";
				} else {
					theData[0] = date.toString();
				}

				// From
				Address[] adds = m.getFrom();
				if (adds != null && adds.length != 0) {
					theData[1] = adds[0].toString();			
				} else {
					theData[1] = "";
				}

				// Subject
				String subject = m.getSubject();
				if (subject != null) {
					theData[2] = subject;
				} else {
					theData[2] = "(No Subject)";
				}

				// Deleted
				theData[3] = m.isSet(Flags.Flag.DELETED);

				cached[row] = theData;
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}
		return cached[row];
	}
}
