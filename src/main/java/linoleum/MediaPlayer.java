package linoleum;

import java.awt.Component;
import java.net.URI;
import java.nio.file.Paths;
import javax.media.Manager;
import javax.media.Player;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;

public class MediaPlayer extends javax.swing.JInternalFrame {
	private final Player player;
	private final ImageIcon playIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/media/Play16.gif"));
	private final ImageIcon pauseIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/media/Pause16.gif"));
	private boolean state;

	public static class Application implements linoleum.application.Application {
		public String getName() {
			return MediaPlayer.class.getSimpleName();
		}

		public ImageIcon getIcon() {
			return new ImageIcon(getClass().getResource("/toolbarButtonGraphics/media/Movie24.gif"));
		}

		public JInternalFrame open(final URI uri) {
			return new MediaPlayer(uri);
		}
	}

	public MediaPlayer(final URI uri) {
		initComponents();
		try {
			if (uri != null) {
				player = Manager.createRealizedPlayer(uri.toURL());
				final Component component = player.getVisualComponent();
				if (component != null) {
					jPanel1.add(component);
					pack();
				}
				setTitle(Paths.get(uri).toFile().getName());
				start();
			} else {
				jButton1.setEnabled(false);
				player = null;
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private void start() {
		if (state) {
			state = false;
			player.stop();
			jButton1.setIcon(playIcon);
		} else {
			state = true;
			player.start();
			jButton1.setIcon(pauseIcon);
		}
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                jPanel1 = new javax.swing.JPanel();
                jPanel2 = new javax.swing.JPanel();
                jButton1 = new javax.swing.JButton();

                setClosable(true);
                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);
                setTitle("Media Player");

                jPanel1.setLayout(new java.awt.BorderLayout());
                getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

                jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/media/Play16.gif"))); // NOI18N
                jButton1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton1ActionPerformed(evt);
                        }
                });
                jPanel2.add(jButton1);

                getContentPane().add(jPanel2, java.awt.BorderLayout.PAGE_END);

                pack();
        }// </editor-fold>//GEN-END:initComponents

        private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
		start();
        }//GEN-LAST:event_jButton1ActionPerformed

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JButton jButton1;
        private javax.swing.JPanel jPanel1;
        private javax.swing.JPanel jPanel2;
        // End of variables declaration//GEN-END:variables
}
