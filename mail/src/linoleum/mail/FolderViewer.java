package linoleum.mail;

import java.awt.*;
import javax.mail.*;
import javax.swing.*;
import javax.swing.event.*;

public class FolderViewer extends JPanel {
	private final FolderModel model = new FolderModel();
	private final JScrollPane scrollpane;
	private final JTable table;
	private MessageViewer mv;

	public FolderViewer() {
		super(new GridLayout(1,1));
		table = new JTable(model);
		// find out what is pressed
		table.getSelectionModel().addListSelectionListener(new FolderPressed());
		table.setShowGrid(false);
		scrollpane = new JScrollPane(table);
		add(scrollpane);
	}

	public void setMv(MessageViewer mv) {
		this.mv = mv;
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
