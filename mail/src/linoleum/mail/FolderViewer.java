package linoleum.mail;

import java.awt.*;
import javax.mail.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class FolderViewer extends JPanel {
	FolderModel model = new FolderModel();
	private final MessageViewer mv;
	JScrollPane scrollpane;
	JTable table;

	public FolderViewer(final MessageViewer mv) {
		super(new GridLayout(1,1));
		this.mv = mv;

		table = new JTable(model);
		table.setShowGrid(false);

		scrollpane = new JScrollPane(table);

		// find out what is pressed
		table.getSelectionModel().addListSelectionListener(new FolderPressed());
		add(scrollpane);
	}

	public void setFolder(Folder what) {
		try {
			table.getSelectionModel().clearSelection();
			if (mv != null) {
				mv.setMessage(null);
			}
			model.setFolder(what);
			scrollpane.invalidate();
			scrollpane.validate();
		} catch (MessagingException me) {
			me.printStackTrace();
		}
	}

	class FolderPressed implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			if (model != null && !e.getValueIsAdjusting()) {
				ListSelectionModel lm = (ListSelectionModel) e.getSource();
				int which = lm.getMaxSelectionIndex();
				if (which != -1) {
					// get the message and display it
					Message msg = model.getMessage(which);
					mv.setMessage(msg);
				}
			}
		}
	}
}
