package linoleum.mail;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.*;
import javax.swing.event.*;

public class FolderViewer extends JPanel {
	private final FolderModel model = new FolderModel();
	private final JScrollPane scrollpane;
	private final JTable table;
	private MessageViewer mv;
	private int which;

	public FolderViewer() {
		super(new GridLayout(1,1));
		final JPopupMenu popup = new JPopupMenu();
		final JMenuItem reply = new JMenuItem("Reply");
		reply.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent evt) {
				try {
					reply(false);
				} catch (final MessagingException me) {
					me.printStackTrace();
				}
			}
		});
		popup.add(reply);
		final JMenuItem replyToAll = new JMenuItem("Reply to all");
		replyToAll.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent evt) {
				try {
					reply(true);
				} catch (final MessagingException me) {
					me.printStackTrace();
				}
			}
		});
		popup.add(replyToAll);
		final JMenuItem item = new JMenuItem("Delete");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent evt) {
				try {
					model.delete(which);
				} catch (final MessagingException me) {
					me.printStackTrace();
				}
			}
		});
		popup.add(item);
		table = new JTable(model);
		table.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(final MouseEvent evt) {
				which = table.rowAtPoint(evt.getPoint());
				table.setComponentPopupMenu(which < 0?null:popup);
			}
		});
		// find out what is pressed
		table.getSelectionModel().addListSelectionListener(new FolderPressed());
		table.setShowGrid(false);
		scrollpane = new JScrollPane(table);
		add(scrollpane);
	}

	@SuppressWarnings("unchecked")
	public void reply(boolean all) throws MessagingException {
		final MimeMessage msg = (MimeMessage)model.getMessage(which);
		final MimeMessage reply = (MimeMessage)msg.reply(all);
		for (final Object line : Collections.list(reply.getAllHeaderLines())) {
			System.out.println(line);
		}
	}

	public void setMv(MessageViewer mv) {
		this.mv = mv;
	}

	public void setFolder(Folder what) throws MessagingException {
		table.getSelectionModel().clearSelection();
		if (mv != null) {
			mv.setMessage(null);
		}
		model.setFolder(what);
		scrollpane.invalidate();
		scrollpane.validate();
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
