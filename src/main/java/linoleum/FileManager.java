package linoleum;

import linoleum.application.ApplicationManager;
import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;

public class FileManager extends javax.swing.JInternalFrame {
	public FileManager(final File file) {
		initComponents();
		chooser.setCurrentDirectory(file);
	}

	public static class Application implements linoleum.application.Application {
		public String getName() {
			return FileManager.class.getSimpleName();
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

                chooser.setFileSelectionMode(javax.swing.JFileChooser.FILES_AND_DIRECTORIES);
                chooser.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                chooserActionPerformed(evt);
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
		final String command = evt.getActionCommand();
		if (JFileChooser.APPROVE_SELECTION.equals(command)) {
			final File file = chooser.getSelectedFile();
			ApplicationManager.instance.open(file.toURI());
		} else if (JFileChooser.CANCEL_SELECTION.equals(command)) {
			dispose();
		}
        }//GEN-LAST:event_chooserActionPerformed

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JFileChooser chooser;
        // End of variables declaration//GEN-END:variables
}
