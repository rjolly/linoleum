package linoleum;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import linoleum.application.Frame;

public class ImageViewer extends Frame {
	private Path files[] = new Path[0];
	private int index;

	public ImageViewer() {
		initComponents();
		setMimeType("image/*");
	}

	@Override
	public void setURI(final URI uri) {
		try {
			final Path path = Paths.get(uri).toRealPath();
			Arrays.sort(files = listFiles(path.getParent()).toArray(new Path[0]));
			index = Arrays.binarySearch(files, path);
		} catch (final IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public URI getURI() {
		if (index < files.length) {
			return files[index].toUri();
		}
		return null;
	}

	@Override
	protected void open() {
		if (index < files.length) {
			final Path file = files[index];
			try {
				jScrollPane1.setViewportView(new ImagePanel(file, scaleButton.isSelected()));
				setTitle(file.getFileName().toString());
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	protected void close() {
		jScrollPane1.setViewportView(null);
		setTitle("Image Viewer");
		files = new Path[0];
		index = 0;
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                jScrollPane1 = new javax.swing.JScrollPane();
                jPanel1 = new javax.swing.JPanel();
                backButton = new javax.swing.JButton();
                scaleButton = new javax.swing.JToggleButton();
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

                scaleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Zoom16.gif"))); // NOI18N
                scaleButton.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                scaleButtonActionPerformed(evt);
                        }
                });
                jPanel1.add(scaleButton);

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

        private void scaleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scaleButtonActionPerformed
		open();
        }//GEN-LAST:event_scaleButtonActionPerformed

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JButton backButton;
        private javax.swing.JButton forwardButton;
        private javax.swing.JPanel jPanel1;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JToggleButton scaleButton;
        // End of variables declaration//GEN-END:variables
}
