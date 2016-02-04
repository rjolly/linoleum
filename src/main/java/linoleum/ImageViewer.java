package linoleum;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import javax.activation.MimeType;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import linoleum.application.Frame;

public class ImageViewer extends Frame {
	private static final String type = "image/*";
	private File files[] = new File[0];
	private int index;

	public ImageViewer() {
		initComponents();
		setMimeType(type);
	}

	@Override
	protected void open() {
		final File file = Paths.get(getURI()).toFile();
		files = file.getParentFile().listFiles(new FileFilter() {
			public boolean accept(final File file) {
				return canOpen(file);
			}
		});
		Arrays.sort(files);
		index = Arrays.binarySearch(files, file);
		init();
	}

	private static boolean canOpen(final File file) {
		try {
			final String str = Files.probeContentType(file.toPath());
			return new MimeType(str).match(type);
		} catch (final Exception ex) {}
		return false;
	}

	private void init() {
		if (index < files.length) {
			final File file = files[index];
			JPanel panel = null;
			try {
				panel = new ImagePanel(file);
			} catch (final IOException ex) {}
			jScrollPane1.setViewportView(panel);
			setTitle(file.getName());
		}
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                jScrollPane1 = new javax.swing.JScrollPane();
                jPanel1 = new javax.swing.JPanel();
                backButton = new javax.swing.JButton();
                forwardButton = new javax.swing.JButton();

                setClosable(true);
                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);
                setTitle("Image Viewer");

                backButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/navigation/Back16.gif"))); // NOI18N
                backButton.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                backButtonActionPerformed(evt);
                        }
                });
                jPanel1.add(backButton);

                forwardButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/navigation/Forward16.gif"))); // NOI18N
                forwardButton.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                forwardButtonActionPerformed(evt);
                        }
                });
                jPanel1.add(forwardButton);

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                );

                pack();
        }// </editor-fold>//GEN-END:initComponents

        private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
		if (files.length > 0) index = (index - 1 + files.length) % files.length;
		init();
        }//GEN-LAST:event_backButtonActionPerformed

        private void forwardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_forwardButtonActionPerformed
		if (files.length > 0) index = (index + 1) % files.length;
		init();
        }//GEN-LAST:event_forwardButtonActionPerformed


        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JButton backButton;
        private javax.swing.JButton forwardButton;
        private javax.swing.JPanel jPanel1;
        private javax.swing.JScrollPane jScrollPane1;
        // End of variables declaration//GEN-END:variables
}
