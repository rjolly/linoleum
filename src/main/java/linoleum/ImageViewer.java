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
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

public class ImageViewer extends JInternalFrame {
	private static final String type = "image/*";
	private final File files[];
	private int index;

	public static class Application implements linoleum.application.Application {
		public String getName() {
			return ImageViewer.class.getSimpleName();
		}

		public ImageIcon getIcon() {
			return null;
		}

		public String getMimeType() {
			return type;
		}

		public JInternalFrame open(final URI uri) {
			return new ImageViewer(uri == null?null:Paths.get(uri).toFile());
		}
	}

	public ImageViewer(final File file) {
		initComponents();
		if (file != null) {
			files = file.getParentFile().listFiles(new FileFilter() {
				public boolean accept(final File file) {
					return canOpen(file);
				}
			});
			Arrays.sort(files);
			index = Arrays.binarySearch(files, file);
			open();
		} else {
			files = new File[] {};
		}
	}

	private static boolean canOpen(final File file) {
		try {
			final String str = Files.probeContentType(file.toPath());
			return new MimeType(str).match(type);
		} catch (final Exception ex) {}
		return false;
	}

	private void open() {
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
		open();
        }//GEN-LAST:event_backButtonActionPerformed

        private void forwardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_forwardButtonActionPerformed
		if (files.length > 0) index = (index + 1) % files.length;
		open();
        }//GEN-LAST:event_forwardButtonActionPerformed


        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JButton backButton;
        private javax.swing.JButton forwardButton;
        private javax.swing.JPanel jPanel1;
        private javax.swing.JScrollPane jScrollPane1;
        // End of variables declaration//GEN-END:variables
}
