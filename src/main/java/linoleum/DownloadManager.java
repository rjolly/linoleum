package linoleum;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.util.Collections;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;
import linoleum.application.FileChooser;
import linoleum.application.Frame;

public class DownloadManager extends Frame {
	private final Icon openIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Open16.gif"));
	private final Icon copyIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Copy16.gif"));
	private final Icon deleteIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Delete16.gif"));
	private final DefaultListModel<FileLoader> model = new DefaultListModel<>();
	private final ListCellRenderer renderer = new Renderer();
	private final FileChooser chooser = new FileChooser();
	private final Action openAction = new OpenAction();
	private final Action closeAction = new CloseAction();
	private final Action cancelAction = new CancelAction();
	private final Action clearCompletedAction = new ClearCompletedAction();
	private final Action copyLinkAddressAction = new CopyLinkAddressAction();
	private final Action deleteAction = new DeleteAction();
	private FileLoader loader;

	private class Renderer extends JPanel implements ListCellRenderer {
		final JLabel label = new JLabel();
		final JProgressBar progress = new JProgressBar();

		Renderer() {
			setLayout(new GridBagLayout());
			final GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = GridBagConstraints.RELATIVE;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
			add(label, c);
			add(progress, c);
			progress.setStringPainted(true);
		}

		@Override
		public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			label.setFont(list.getFont());
			final FileLoader loader = (FileLoader) value;
			label.setText(loader.getFile().getName());
			progress.setValue(loader.getProgress());
			loader.addPropertyChangeListener(new PropertyChangeListener() {
				public  void propertyChange(final PropertyChangeEvent evt) {
					if ("progress".equals(evt.getPropertyName())) {
						list.repaint();
					}
				}
			});
			return this;
		}
	}

	private class FileLoader extends SwingWorker<File, Object> {
		private final URL location;
		private final File file;
	
		FileLoader(final URL location, final File file) {
			this.location = location;
			this.file = file;
		}

		public URL getLocation() {
			return location;
		}

		public File getFile() {
			return file;
		}

		public File doInBackground() throws IOException {
			final URLConnection conn = location.openConnection();
			final long length = conn.getContentLengthLong();
			try (final InputStream is = conn.getInputStream(); final OutputStream os = new FileOutputStream(file)) {
				final byte buffer[] = new byte[4096];
				long l = 0;
				int n = 0;
				while ((n = is.read(buffer)) != -1) {
					os.write(buffer, 0, n);
					l += n;
					if (length > 0) {
						setProgress(Long.valueOf(100 * l / length).intValue());
					}
				}
				setProgress(100);
			}
			return file;
		}

		@Override
		protected void done() {
			try {
				get();
				jList1.repaint();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						prepare();
					}
				});
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public String toString() {
			return location.toString();
		}
	}

	private class OpenAction extends AbstractAction {
		public OpenAction() {
			super("Open", openIcon);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			getApplicationManager().open(loader.getFile().toURI());
		}
	}

	private class CloseAction extends AbstractAction {
		public CloseAction() {
			super("Close");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			doDefaultCloseAction();
		}
	}

	private class CancelAction extends AbstractAction {
		public CancelAction() {
			super("Cancel");
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			loader.cancel(true);
			prepare();
		}
	}

	private class ClearCompletedAction extends AbstractAction {
		public ClearCompletedAction() {
			super("Clear completed");
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			for (final FileLoader loader : Collections.list(model.elements())) {
				if (loader.isDone()) {
					model.removeElement(loader);
				}
			}
			prepare();
		}
	}

	private ActionEvent createActionEvent() {
		return new ActionEvent(jList1, ActionEvent.ACTION_PERFORMED, null);
	}

	private class CopyLinkAddressAction extends AbstractAction {
		public CopyLinkAddressAction() {
			super("Copy link address", copyIcon);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			TransferHandler.getCopyAction().actionPerformed(createActionEvent());
		}
	}

	private class DeleteAction extends AbstractAction {
		public DeleteAction() {
			super("Delete", deleteIcon);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			model.removeElement(loader);
			prepare();
		}
	}

	public DownloadManager() {
		initComponents();
		setScheme("ftp:http:https");
		setIcon(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Import24.gif")));
		prepare();
	}

	@Override
	public void open() {
		final URI uri = getURI();
		if (uri != null) try {
			final URL location = uri.toURL();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					final File file = getFile(new File(location.getPath()).getName());
					if (file != null && (!file.exists() || proceed())) {
						final FileLoader loader = new FileLoader(location, file);
						model.addElement(loader);
						loader.execute();
					}
				}
			});
		} catch (final MalformedURLException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void close() {
		setURI(null);
	}

	private File getFile(final String filename) {
		chooser.setSelectedFile(new File(filename));
		final int returnVal = chooser.showInternalSaveDialog(this);
		switch (returnVal) {
		case JFileChooser.APPROVE_OPTION:
			return chooser.getSelectedFile();
		default:
		}
		return null;
	}

	private boolean proceed() {
		switch (JOptionPane.showInternalConfirmDialog(this, "File exists. Overwrite ?", "Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE)) {
		case JOptionPane.OK_OPTION:
			return true;
		default:
		}
		return false;
	}

	private void prepare() {
		if (jList1.isSelectionEmpty()) {
			loader = null;
			openAction.setEnabled(false);
			cancelAction.setEnabled(false);
			deleteAction.setEnabled(false);
			copyLinkAddressAction.setEnabled(false);
		} else {
			loader = (FileLoader) jList1.getSelectedValue();
			openAction.setEnabled(loader.isDone());
			cancelAction.setEnabled(!loader.isDone());
			deleteAction.setEnabled(loader.isDone());
			copyLinkAddressAction.setEnabled(true);
		}
	}

	@Override
	public boolean reuseFor(final URI that) {
		if (that == null) {
			return super.reuseFor(that);
		} else try {
			final URL location = that.toURL();
			for (final FileLoader loader : Collections.list(model.elements())) {
				if (!loader.isDone() && location.equals(loader.getLocation())) {
					return true;
				}
			}
		} catch (final MalformedURLException ex) {
			ex.printStackTrace();
		}
		return false;
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                jScrollPane1 = new javax.swing.JScrollPane();
                jList1 = new javax.swing.JList();
                jMenuBar1 = new javax.swing.JMenuBar();
                jMenu1 = new javax.swing.JMenu();
                jMenuItem1 = new javax.swing.JMenuItem();
                jMenuItem2 = new javax.swing.JMenuItem();
                jMenu2 = new javax.swing.JMenu();
                jMenuItem3 = new javax.swing.JMenuItem();
                jMenuItem4 = new javax.swing.JMenuItem();
                jMenuItem5 = new javax.swing.JMenuItem();
                jMenuItem6 = new javax.swing.JMenuItem();

                setClosable(true);
                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);
                setTitle("Downloads");
                setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Import16.gif"))); // NOI18N
                setName("Downloads"); // NOI18N

                jList1.setModel(model);
                jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
                jList1.setCellRenderer(renderer);
                jList1.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
                        public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                                jList1ValueChanged(evt);
                        }
                });
                jScrollPane1.setViewportView(jList1);

                jMenu1.setText("File");

                jMenuItem1.setAction(openAction);
                jMenu1.add(jMenuItem1);

                jMenuItem2.setAction(closeAction);
                jMenu1.add(jMenuItem2);

                jMenuBar1.add(jMenu1);

                jMenu2.setText("Edit");

                jMenuItem3.setAction(cancelAction);
                jMenu2.add(jMenuItem3);

                jMenuItem4.setAction(clearCompletedAction);
                jMenu2.add(jMenuItem4);

                jMenuItem5.setAction(copyLinkAddressAction);
                jMenu2.add(jMenuItem5);

                jMenuItem6.setAction(deleteAction);
                jMenu2.add(jMenuItem6);

                jMenuBar1.add(jMenu2);

                setJMenuBar(jMenuBar1);

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                                .addContainerGap())
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
                                .addContainerGap())
                );

                pack();
        }// </editor-fold>//GEN-END:initComponents

        private void jList1ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList1ValueChanged
		prepare();
        }//GEN-LAST:event_jList1ValueChanged


        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JList jList1;
        private javax.swing.JMenu jMenu1;
        private javax.swing.JMenu jMenu2;
        private javax.swing.JMenuBar jMenuBar1;
        private javax.swing.JMenuItem jMenuItem1;
        private javax.swing.JMenuItem jMenuItem2;
        private javax.swing.JMenuItem jMenuItem3;
        private javax.swing.JMenuItem jMenuItem4;
        private javax.swing.JMenuItem jMenuItem5;
        private javax.swing.JMenuItem jMenuItem6;
        private javax.swing.JScrollPane jScrollPane1;
        // End of variables declaration//GEN-END:variables
}
