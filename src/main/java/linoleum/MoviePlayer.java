package linoleum;

import java.awt.Component;
import java.net.URI;
import javax.media.Manager;
import javax.media.Player;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;

public class MoviePlayer extends javax.swing.JInternalFrame {
	private final Player player;

	public static class Application implements linoleum.application.Application {
		public String getName() {
			return MoviePlayer.class.getSimpleName();
		}

		public ImageIcon getIcon() {
			return new ImageIcon(getClass().getResource("/toolbarButtonGraphics/media/Movie24.gif"));
		}

		public JInternalFrame open(final URI uri) {
			return new MoviePlayer(uri);
		}
	}

	public MoviePlayer(final URI uri) {
		initComponents();
		try {
			if (uri != null) {
				player = Manager.createRealizedPlayer(uri.toURL());
				final Component component = player.getVisualComponent();
				jPanel1.add(component);
				player.start();
			} else {
				player = null;
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                jPanel1 = new javax.swing.JPanel();
                jPanel2 = new javax.swing.JPanel();
                playButton = new javax.swing.JButton();
                stopButton = new javax.swing.JButton();

                setClosable(true);
                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);
                setTitle("Movie Player");

                javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
                jPanel1.setLayout(jPanel1Layout);
                jPanel1Layout.setHorizontalGroup(
                        jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
                );
                jPanel1Layout.setVerticalGroup(
                        jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 236, Short.MAX_VALUE)
                );

                playButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/media/Play16.gif"))); // NOI18N
                playButton.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                playButtonActionPerformed(evt);
                        }
                });
                jPanel2.add(playButton);

                stopButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/media/Stop16.gif"))); // NOI18N
                stopButton.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                stopButtonActionPerformed(evt);
                        }
                });
                jPanel2.add(stopButton);

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                );

                pack();
        }// </editor-fold>//GEN-END:initComponents

        private void playButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playButtonActionPerformed
		if (player != null) player.start();
        }//GEN-LAST:event_playButtonActionPerformed

        private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
		if (player != null) player.stop();
        }//GEN-LAST:event_stopButtonActionPerformed

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JPanel jPanel1;
        private javax.swing.JPanel jPanel2;
        private javax.swing.JButton playButton;
        private javax.swing.JButton stopButton;
        // End of variables declaration//GEN-END:variables
}
