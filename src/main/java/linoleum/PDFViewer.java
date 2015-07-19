package linoleum;

import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;

public class PDFViewer extends javax.swing.JInternalFrame {
	private int index;

	public static class Application implements linoleum.application.Application {
		public String getName() {
			return PDFViewer.class.getSimpleName();
		}

		public ImageIcon getIcon() {
			return null;
		}

		public String[] getExtensions() {
			return new String[] {"pdf"};
		}

		public JInternalFrame open(final URI uri) {
			return new PDFViewer(uri == null?null:Paths.get(uri).toFile());
		}
	}

	public PDFViewer(final File file) {
		initComponents();
	}

	private void open() {
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                jScrollPane1 = new javax.swing.JScrollPane();
                pagePanel1 = new linoleum.PagePanel();
                jPanel1 = new javax.swing.JPanel();
                backButton = new javax.swing.JButton();
                forwardButton = new javax.swing.JButton();

                setClosable(true);
                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);
                setTitle("PDF Viewer");

                javax.swing.GroupLayout pagePanel1Layout = new javax.swing.GroupLayout(pagePanel1);
                pagePanel1.setLayout(pagePanel1Layout);
                pagePanel1Layout.setHorizontalGroup(
                        pagePanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 392, Short.MAX_VALUE)
                );
                pagePanel1Layout.setVerticalGroup(
                        pagePanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 255, Short.MAX_VALUE)
                );

                jScrollPane1.setViewportView(pagePanel1);

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
                index -= 1;
                open();
        }//GEN-LAST:event_backButtonActionPerformed

        private void forwardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_forwardButtonActionPerformed
                index += 1;
                open();
        }//GEN-LAST:event_forwardButtonActionPerformed


        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JButton backButton;
        private javax.swing.JButton forwardButton;
        private javax.swing.JPanel jPanel1;
        private javax.swing.JScrollPane jScrollPane1;
        private linoleum.PagePanel pagePanel1;
        // End of variables declaration//GEN-END:variables
}
