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
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import linoleum.application.Frame;

public class FileManager extends Frame {
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

	public FileManager() {
		initComponents();
		setIcon(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Open24.gif")));
	}

	@Override
	public void open() {
		thread.start();
	}

	@Override
	public void setURI(final URI uri) {
		chooser.setCurrentDirectory(Paths.get(uri).toFile());
	}

	@Override
	public URI getURI() {
		return chooser.getCurrentDirectory().toURI();
	}

	@Override
	public Frame getFrame() {
		return new FileManager();
	}

	@Override
	protected void close() {
		closing = true;
		thread.interrupt();
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                chooser = new javax.swing.JFileChooser();

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

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JFileChooser chooser;
        // End of variables declaration//GEN-END:variables
}
