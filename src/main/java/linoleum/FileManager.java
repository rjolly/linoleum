package linoleum;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.prefs.Preferences;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import linoleum.application.FileChooser;
import linoleum.application.Frame;
import linoleum.application.OptionPanel;

public class FileManager extends Frame {
	private final Preferences prefs = Preferences.userNodeForPackage(getClass());
	private final FileChooser fileChooser = new FileChooser();
	private final Thread thread = new Thread() {
		@Override
		public void run() {
			try (final WatchService service = FileSystems.getDefault().newWatchService()) {
				WatchKey key = register(service);
				for (;;) {
					try {
						key = service.take();
						for (final WatchEvent<?> event : key.pollEvents()) {
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									chooser.rescanCurrentDirectory();
								}
							});
						}
						key.reset();
					} catch (final InterruptedException e) {
						if (closing) {
							service.close();
							break;
						} else {
							key.cancel();
							key = register(service);
						}
					}
				}
			} catch (final IOException e) {}
		}

		private WatchKey register(final WatchService service) throws IOException {
			final Path path = Paths.get(getURI());
			setTitle(path.toFile().getName());
			return path.register(service, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.OVERFLOW);
		}
	};
	private boolean closing;
	private boolean empty = true;

	public FileManager() {
		this(null);
	}

	public FileManager(final Frame parent) {
		super(parent);
		initComponents();
		setIcon(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Open24.gif")));
		setMimeType("application/octet-stream");
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	}

	@Override
	public OptionPanel getOptionPanel() {
		return optionPanel1;
	}

	@Override
	public void load() {
		jTextField1.setText(prefs.get(getName() + ".home", ""));
	}

	@Override
	public void save() {
		prefs.put(getName() + ".home", jTextField1.getText());
	}

	@Override
	public void open() {
		if (empty) {
			final String str = prefs.get(getName() + ".home", "");
			if (!str.isEmpty()) {
				setURI(new File(str).toURI());
			}
		}
		thread.start();
	}

	@Override
	public void setURI(final URI uri) {
		chooser.setCurrentDirectory(Paths.get(uri).toFile());
		empty = false;
	}

	@Override
	public URI getURI() {
		return chooser.getCurrentDirectory().toURI();
	}

	@Override
	public Frame getFrame(final Frame parent) {
		return new FileManager(parent);
	}

	@Override
	protected void close() {
		closing = true;
		thread.interrupt();
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                optionPanel1 = new linoleum.application.OptionPanel();
                jLabel2 = new javax.swing.JLabel();
                jTextField1 = new javax.swing.JTextField();
                jButton1 = new javax.swing.JButton();
                chooser = new javax.swing.JFileChooser();

                optionPanel1.setFrame(this);

                jLabel2.setText("Home :");

                jButton1.setText("Choose...");
                jButton1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton1ActionPerformed(evt);
                        }
                });

                javax.swing.GroupLayout optionPanel1Layout = new javax.swing.GroupLayout(optionPanel1);
                optionPanel1.setLayout(optionPanel1Layout);
                optionPanel1Layout.setHorizontalGroup(
                        optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(optionPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(optionPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel2)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jTextField1))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, optionPanel1Layout.createSequentialGroup()
                                                .addGap(0, 303, Short.MAX_VALUE)
                                                .addComponent(jButton1)))
                                .addContainerGap())
                );
                optionPanel1Layout.setVerticalGroup(
                        optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(optionPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel2)
                                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );

                setClosable(true);
                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);

                chooser.setFileSelectionMode(javax.swing.JFileChooser.FILES_AND_DIRECTORIES);
                chooser.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                chooserActionPerformed(evt);
                        }
                });
                chooser.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
                        public void propertyChange(java.beans.PropertyChangeEvent evt) {
                                chooserPropertyChange(evt);
                        }
                });

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(chooser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(chooser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                );

                pack();
        }// </editor-fold>//GEN-END:initComponents

        private void chooserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooserActionPerformed
		switch (evt.getActionCommand()) {
		case JFileChooser.APPROVE_SELECTION:
			final File file = chooser.getSelectedFile();
			getApplicationManager().open(file.toURI());
			break;
		case JFileChooser.CANCEL_SELECTION:
			try {
				setClosed(true);
			} catch (final PropertyVetoException e) {
				e.printStackTrace();
			}
			break;
		}
        }//GEN-LAST:event_chooserActionPerformed

        private void chooserPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_chooserPropertyChange
		if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
			thread.interrupt();
		}
        }//GEN-LAST:event_chooserPropertyChange

        private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
		final int returnVal = fileChooser.showInternalOpenDialog(optionPanel1);
		switch (returnVal) {
		case JFileChooser.APPROVE_OPTION:
			jTextField1.setText(fileChooser.getSelectedFile().getPath());
			break;
		default:
		}
        }//GEN-LAST:event_jButton1ActionPerformed

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JFileChooser chooser;
        private javax.swing.JButton jButton1;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JTextField jTextField1;
        private linoleum.application.OptionPanel optionPanel1;
        // End of variables declaration//GEN-END:variables
}
