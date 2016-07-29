package linoleum.mail;

import javax.mail.*;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class FolderViewer extends javax.swing.JPanel {
	private final FolderModel model = new FolderModel();
	private MessageViewer mv;

	public FolderModel getModel() {
		return model;
	}

	public FolderViewer() {
		initComponents();

		// find out what is pressed
		table.getSelectionModel().addListSelectionListener(new FolderPressed());
		table.setShowGrid(false);
	}

	public void setMv(final MessageViewer mv) {
		this.mv = mv;
	}

	public void setFolder(final Folder what) throws MessagingException {
		table.getSelectionModel().clearSelection();
		if (mv != null) {
			mv.setMessage(null);
		}
		model.setFolder(what);
		scrollpane.invalidate();
		scrollpane.validate();
		final int n = model.getRowCount();
		if (n > 0) {
			table.scrollRectToVisible(table.getCellRect(n - 1, 0, true));
		}
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                scrollpane = new javax.swing.JScrollPane();
                table = new javax.swing.JTable();

                table.setModel(getModel());
                scrollpane.setViewportView(table);

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
                this.setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(scrollpane, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(scrollpane, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                );
        }// </editor-fold>//GEN-END:initComponents

	class FolderPressed implements ListSelectionListener {
		public void valueChanged(final ListSelectionEvent e) {
			if (model != null && !e.getValueIsAdjusting()) {
				final ListSelectionModel lm = (ListSelectionModel) e.getSource();
				final int which = lm.getMaxSelectionIndex();
				if (which != -1) {
					// get the message and display it
					final Message msg = model.getMessage(which);
					mv.setMessage(msg);
				}
			}
		}
	}

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JScrollPane scrollpane;
        private javax.swing.JTable table;
        // End of variables declaration//GEN-END:variables
}
