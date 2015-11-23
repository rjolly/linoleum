package linoleum;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;
import linoleum.application.Frame;

public class FileManager extends Frame {
	private boolean closing=false;
	private final Thread thread;

	public FileManager(final File file) {
		initComponents();
		thread = new Thread() {

			@Override
			public void run() {
				try (final WatchService service = FileSystems.getDefault().newWatchService()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							chooser.setCurrentDirectory(file);
						}
					});
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
				return Paths.get(chooser.getCurrentDirectory().toURI()).register(service, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.OVERFLOW);
			}
		};
		thread.start();
	}

	public static class Application implements linoleum.application.Application {
		public String getName() {
			return FileManager.class.getSimpleName();
		}

		public ImageIcon getIcon() {
			return new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Open24.gif"));
		}

		public String getMimeType() {
			return null;
		}

		public JInternalFrame open(final URI uri) {
			return new FileManager(uri == null?null:Paths.get(uri).toFile());
		}
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                chooser = new javax.swing.JFileChooser();

                setClosable(true);
                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);
                setTitle("Files");
                setToolTipText("");
                addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
                        public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
                        }
                        public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
                                formInternalFrameClosing(evt);
                        }
                        public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
                        }
                        public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
                        }
                        public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
                        }
                        public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
                        }
                        public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
                        }
                });

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
			dispose();
			break;
		}
        }//GEN-LAST:event_chooserActionPerformed

        private void formInternalFrameClosing(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameClosing
		closing = true;
		thread.interrupt();
        }//GEN-LAST:event_formInternalFrameClosing

        private void chooserPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_chooserPropertyChange
		if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
			thread.interrupt();
		}
        }//GEN-LAST:event_chooserPropertyChange

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JFileChooser chooser;
        // End of variables declaration//GEN-END:variables
}
