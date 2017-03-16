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
import javax.swing.SwingWorker;
import linoleum.application.FileChooser;
import linoleum.application.Frame;

public class DownloadManager extends Frame {
	private final Icon deleteIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Delete16.gif"));
	private final DefaultListModel<FileLoader> model = new DefaultListModel<>();
	private final ListCellRenderer renderer = new Renderer();
	private final FileChooser chooser = new FileChooser();
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
			label.setText(loader.getName());
			loader.addPropertyChangeListener(new PropertyChangeListener() {
				public  void propertyChange(final PropertyChangeEvent evt) {
					if ("progress".equals(evt.getPropertyName())) {
						progress.setValue((Integer) evt.getNewValue());
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

		public String getLocation() {
			return location.toString();
		}

		public String getName() {
			return file.getName();
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
			}
			return file;
		}

		@Override
		protected void done() {
			try {
				get();
			} catch (final Exception e) {
				e.printStackTrace();
			}
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

	private class CopyLinkAddressAction extends AbstractAction {
		public CopyLinkAddressAction() {
			super("Copy link address");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final String str = loader.getLocation();
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
		jMenu1.add(closeAction);
		jMenu2.add(cancelAction);
		jMenu2.add(clearCompletedAction);
		jMenu2.add(copyLinkAddressAction);
		jMenu2.add(deleteAction);
	}

	@Override
	public void open() {
		final URI uri = getURI();
		if (uri != null) try {
			jTextField1.setText(uri.toURL().toString());
		} catch (final MalformedURLException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void close() {
		clear();
	}

	private void clear() {
		setURI(null);
		jTextField1.setText("");
	}

	private void start() {
		try {
			final URL location = new URL(jTextField1.getText());
			final File file = getFile(new File(location.getPath()).getName());
			if (file != null && (!file.exists() || proceed())) {
				final FileLoader loader = new FileLoader(location, file);
				model.addElement(loader);
				loader.execute();
			}
		} catch (final MalformedURLException ex) {
			ex.printStackTrace();
		}
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
			cancelAction.setEnabled(false);
			deleteAction.setEnabled(false);
			copyLinkAddressAction.setEnabled(false);
		} else {
			loader = (FileLoader) jList1.getSelectedValue();
			cancelAction.setEnabled(!loader.isDone());
			deleteAction.setEnabled(loader.isDone());
			copyLinkAddressAction.setEnabled(true);
		}
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                jTextField1 = new javax.swing.JTextField();
                jButton1 = new javax.swing.JButton();
                jButton2 = new javax.swing.JButton();
                jScrollPane1 = new javax.swing.JScrollPane();
                jList1 = new javax.swing.JList();
                jSeparator1 = new javax.swing.JSeparator();
                jLabel1 = new javax.swing.JLabel();
                jMenuBar1 = new javax.swing.JMenuBar();
                jMenu1 = new javax.swing.JMenu();
                jMenu2 = new javax.swing.JMenu();

                setClosable(true);
                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);
                setTitle("Downloads");
                setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Import16.gif"))); // NOI18N
                setName("Downloads"); // NOI18N

                jButton1.setText("Start");
                jButton1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton1ActionPerformed(evt);
                        }
                });

                jButton2.setText("Clear");
                jButton2.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton2ActionPerformed(evt);
                        }
                });

                jList1.setModel(model);
                jList1.setCellRenderer(renderer);
                jList1.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
                        public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                                jList1ValueChanged(evt);
                        }
                });
                jScrollPane1.setViewportView(jList1);

                jLabel1.setText("Location :");

                jMenu1.setText("File");
                jMenuBar1.add(jMenu1);

                jMenu2.setText("Edit");
                jMenuBar1.add(jMenu2);

                setJMenuBar(jMenuBar1);

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                                        .addComponent(jTextField1)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addComponent(jButton2)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jButton1))
                                        .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel1)
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jButton1)
                                        .addComponent(jButton2))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
                                .addContainerGap())
                );

                pack();
        }// </editor-fold>//GEN-END:initComponents

        private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
		clear();
        }//GEN-LAST:event_jButton2ActionPerformed

        private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
		start();
        }//GEN-LAST:event_jButton1ActionPerformed

        private void jList1ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList1ValueChanged
		prepare();
        }//GEN-LAST:event_jList1ValueChanged


        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JButton jButton1;
        private javax.swing.JButton jButton2;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JList jList1;
        private javax.swing.JMenu jMenu1;
        private javax.swing.JMenu jMenu2;
        private javax.swing.JMenuBar jMenuBar1;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JSeparator jSeparator1;
        private javax.swing.JTextField jTextField1;
        // End of variables declaration//GEN-END:variables
}
