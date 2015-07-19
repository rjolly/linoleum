package linoleum;

import java.net.URI;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import linoleum.application.Application;

public class Clock extends JInternalFrame implements Application {

	public Clock() {
		initComponents();
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                clockPanel1 = new linoleum.ClockPanel();

                setClosable(true);
                setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);
                setTitle("Clock");
                setName(getClass().getSimpleName());
                addComponentListener(new java.awt.event.ComponentAdapter() {
                        public void componentShown(java.awt.event.ComponentEvent evt) {
                                formComponentShown(evt);
                        }
                        public void componentHidden(java.awt.event.ComponentEvent evt) {
                                formComponentHidden(evt);
                        }
                });

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(clockPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(clockPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                );

                pack();
        }// </editor-fold>//GEN-END:initComponents

        private void formComponentHidden(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentHidden
		clockPanel1.stop();
        }//GEN-LAST:event_formComponentHidden

        private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
		clockPanel1.start();
        }//GEN-LAST:event_formComponentShown

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private linoleum.ClockPanel clockPanel1;
        // End of variables declaration//GEN-END:variables

	public ImageIcon getIcon() {
		return null;
	}

	public String[] getExtensions() {
		return null;
	}

	public JInternalFrame open(final URI uri) {
		return this;
	}
}
