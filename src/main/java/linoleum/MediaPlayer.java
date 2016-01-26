package linoleum;

import java.awt.Component;
import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import javax.activation.MimeType;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.EndOfMediaEvent;
import javax.media.Manager;
import javax.media.Player;
import javax.media.Time;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import linoleum.application.Frame;

public class MediaPlayer extends Frame {
	private Player player;
	private static final String audio = "audio/*";
	private static final String video = "video/*";
	private final Icon playIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/media/Play16.gif"));
	private final Icon pauseIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/media/Pause16.gif"));
	private File files[] = new File[] {};
	private boolean slide;
	private Timer timer;
	private int index;

	public MediaPlayer() {
		initComponents();
		setIcon(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/media/Movie24.gif")));
		setMimeType(audio + ":" + video);
		setSingle(true);
	}

	protected void open() {
		final File file = Paths.get(getURI()).toFile();
		stop();
		files = file.getParentFile().listFiles(new FileFilter() {
			public boolean accept(final File file) {
				return canOpen(file);
			}
		});
		Arrays.sort(files);
		index = Arrays.binarySearch(files, file);
		prepare();
		play();
	}

	private static boolean canOpen(final File file) {
		try {
			final String str = Files.probeContentType(file.toPath());
			final MimeType type = new MimeType(str);
			return type.match(audio) || type.match(video);
		} catch (final Exception ex) {}
		return false;
	}

	protected void close() {
		stop();
	}

	private ControllerListener listener = new ControllerListener() {

		@Override
		public void controllerUpdate(ControllerEvent ce) {
			if (ce instanceof EndOfMediaEvent) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						stop();
						if (files.length > 0) index = (index + 1) % files.length;
						prepare();
						if (index > 0) {
							play();
						}
					}
				});
			}
		}
	};

	private void prepare() {
		if (index < files.length) {
			final File file = files[index];
			setTitle(file.getName());
		}
	}

	private void init() {
		if (index < files.length) {
			final File file = files[index];
			try {
				player = Manager.createRealizedPlayer(file.toURI().toURL());
				final Component component = player.getVisualComponent();
				if (component != null) {
					jPanel1.add(component);
					pack();
				}
				player.addControllerListener(listener);
				timer = new Timer();
				timer.schedule(new TimerTask() {
					public void run() {
						if (SwingUtilities.isEventDispatchThread()) {
							if (player != null && player.getState() == Player.Started) {
								final Time time = player.getMediaTime();
								final Time duration = player.getDuration();
								slide = true;
								jSlider1.setValue((int)(100 * time.getSeconds() / duration.getSeconds()));
								jSlider1.setToolTipText(format(time) + "/" + format(duration));
								slide = false;
							}
						} else {
							SwingUtilities.invokeLater(this);
						}
					}
				}, 0, 1000);
			} catch (final Exception ex) {}
		}
	}

	private static String format(final Time time) {
		return String.format("%tT", 82800000+(time.getNanoseconds()/1000000));
	}

	private void play() {
		if (player == null) {
			init();
		}
		if (player != null) {
			if (player.getState() == Player.Started) {
				player.stop();
				jButton1.setIcon(playIcon);
			} else {
				player.start();
				jButton1.setIcon(pauseIcon);
			}
		}
	}

	private void skip(final int value) {
		if (player != null) {
			final Time duration = player.getDuration();
			final Time time = new Time(duration.getNanoseconds() * value / 100);
			jSlider1.setToolTipText(format(time) + "/" + format(duration));
			player.setMediaTime(time);
		}
	}

	private void stop() {
		if (player != null) {
			timer.cancel();
			jSlider1.setValue(0);
			jSlider1.setToolTipText(null);
			player.stop();
			player.removeControllerListener(listener);
			player.close();
			player = null;
			jButton1.setIcon(playIcon);
			jPanel1.removeAll();
			pack();
		}
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                jPanel1 = new javax.swing.JPanel();
                jPanel2 = new javax.swing.JPanel();
                jSlider1 = new javax.swing.JSlider();
                jButton2 = new javax.swing.JButton();
                jButton4 = new javax.swing.JButton();
                jButton1 = new javax.swing.JButton();
                jButton3 = new javax.swing.JButton();

                setClosable(true);
                setIconifiable(true);
                setTitle("Media Player");

                jPanel1.setLayout(new java.awt.BorderLayout());
                getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

                jSlider1.setValue(0);
                jSlider1.addChangeListener(new javax.swing.event.ChangeListener() {
                        public void stateChanged(javax.swing.event.ChangeEvent evt) {
                                jSlider1StateChanged(evt);
                        }
                });
                jPanel2.add(jSlider1);

                jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/media/StepBack16.gif"))); // NOI18N
                jButton2.setPreferredSize(new java.awt.Dimension(28, 28));
                jButton2.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton2ActionPerformed(evt);
                        }
                });
                jPanel2.add(jButton2);

                jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/media/Stop16.gif"))); // NOI18N
                jButton4.setPreferredSize(new java.awt.Dimension(28, 28));
                jButton4.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton4ActionPerformed(evt);
                        }
                });
                jPanel2.add(jButton4);

                jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/media/Play16.gif"))); // NOI18N
                jButton1.setPreferredSize(new java.awt.Dimension(28, 28));
                jButton1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton1ActionPerformed(evt);
                        }
                });
                jPanel2.add(jButton1);

                jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/media/StepForward16.gif"))); // NOI18N
                jButton3.setPreferredSize(new java.awt.Dimension(28, 28));
                jButton3.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton3ActionPerformed(evt);
                        }
                });
                jPanel2.add(jButton3);

                getContentPane().add(jPanel2, java.awt.BorderLayout.PAGE_END);

                pack();
        }// </editor-fold>//GEN-END:initComponents

        private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
		play();
        }//GEN-LAST:event_jButton1ActionPerformed

        private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
		stop();
		if (files.length > 0) index = (index + 1) % files.length;
		prepare();
        }//GEN-LAST:event_jButton3ActionPerformed

        private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
		stop();
		if (files.length > 0) index = (index - 1 + files.length) % files.length;
		prepare();
        }//GEN-LAST:event_jButton2ActionPerformed

        private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
		stop();
        }//GEN-LAST:event_jButton4ActionPerformed

        private void jSlider1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider1StateChanged
		final JSlider source = (JSlider)evt.getSource();
		if (!source.getValueIsAdjusting() && !slide) {
			skip(source.getValue());
		}
        }//GEN-LAST:event_jSlider1StateChanged

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JButton jButton1;
        private javax.swing.JButton jButton2;
        private javax.swing.JButton jButton3;
        private javax.swing.JButton jButton4;
        private javax.swing.JPanel jPanel1;
        private javax.swing.JPanel jPanel2;
        private javax.swing.JSlider jSlider1;
        // End of variables declaration//GEN-END:variables
}
