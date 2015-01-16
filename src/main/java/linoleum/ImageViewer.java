package linoleum;

import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
import javax.swing.JInternalFrame;

public class ImageViewer extends javax.swing.JInternalFrame {

	public static class Application implements linoleum.application.Application {
		public String getName() {
			return ImageViewer.class.getSimpleName();
		}

		public JInternalFrame open(final URI uri) {
			return new ImageViewer(uri == null?null:Paths.get(uri).toFile());
		}
	}

	public ImageViewer(final File file) {
		initComponents();
		if (file != null) {
			setTitle(file.getName());
			jScrollPane1.setViewportView(new ImagePanel(file));
		}
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                jScrollPane1 = new javax.swing.JScrollPane();

                setClosable(true);
                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);
                setTitle("Image Viewer");

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
                );

                pack();
        }// </editor-fold>//GEN-END:initComponents


        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JScrollPane jScrollPane1;
        // End of variables declaration//GEN-END:variables
}
