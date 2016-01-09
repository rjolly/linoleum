package linoleum;

import java.awt.Component;
import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import javax.activation.FileTypeMap;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.EndOfMediaEvent;
import javax.media.Manager;
import javax.media.Player;
import javax.media.Time;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JSlider;
import linoleum.application.Application;

public class MediaPlayer extends JInternalFrame implements Application {
	private Player player;
	private static final String audio = "audio/*";
	private static final String video = "video/*";
	private final ImageIcon playIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/media/Play16.gif"));
	private final ImageIcon pauseIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/media/Pause16.gif"));
	private File files[] = new File[] {};
	private boolean state;
	private Time duration;
	private Timer timer;
	private int index;

	public MediaPlayer() {
		initComponents();
	}

	public ImageIcon getIcon() {
		return new ImageIcon(getClass().getResource("/toolbarButtonGraphics/media/Movie24.gif"));
	}

	public String getMimeType() {
		return audio + ":" + video;
	}

	public JInternalFrame open(final URI uri) {
		if (uri != null) init(Paths.get(uri).toFile());
		return this;
	}

	public void init(final File file) {
		stop();
		files = file.getParentFile().listFiles(new FileFilter() {
			public boolean accept(final File file) {
				return canOpen(file);
			}
		});
		Arrays.sort(files);
		index = Arrays.binarySearch(files, file);
		play();
	}

	private static boolean canOpen(final File file) {
		final String str = FileTypeMap.getDefaultFileTypeMap().getContentType(file);
		try {
			final MimeType type = new MimeType(str);
			return type.match(audio) || type.match(video);
		} catch (final MimeTypeParseException ex) {}
		return false;
	}

	private void play() {
		open();
		if (player != null) {
			pause();
		}
	}

	private ControllerListener listener = new ControllerListener() {

		@Override
		public void controllerUpdate(ControllerEvent ce) {
			if (ce instanceof EndOfMediaEvent) {
				stop();
				if (files.length > 0) index = (index + 1) % files.length;
				if (index == 0) {
					open();
				} else {
					play();
				}
			}
		}
	};

	private void open() {
		if (index < files.length) {
			final File file = files[index];
			setTitle(file.getName());
			try {
				player = Manager.createRealizedPlayer(file.toURI().toURL());
				final Component component = player.getVisualComponent();
				if (component != null) {
					jPanel1.removeAll();
					jPanel1.add(component);
					pack();
				}
				player.addControllerListener(listener);
				duration = player.getDuration();
				timer = new Timer();
				timer.schedule(new TimerTask() {
					public void run() {
						if (player != null) {
							final Time time = player.getMediaTime();
							jSlider1.setValue((int)(100 * time.getSeconds() / duration.getSeconds()));
						}
					}
				}, 0, 1000);
			} catch (final Exception ex) {
				player = null;
			}
		}
	}

	private void stop() {
		if (player != null) {
			timer.cancel();
			jSlider1.setValue(0);
			player.stop();
			player.removeControllerListener(listener);
			player.close();
			state = false;
			jButton1.setIcon(playIcon);
			player = null;
		}
	}

	private void pause() {
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

	private void skip(final int value) {
		if (player == null) {
			open();
		}
		if (player != null) {
			final Time time = new Time(duration.getNanoseconds() * value / 100);
			player.setMediaTime(time);
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
                setName("MediaPlayer");
                addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
                        public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
                        }
                        public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
                                formInternalFrameClosing(evt);
                        }
                        public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
                        }
                        public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
                        }
                        public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
                        }
                        public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
                        }
                        public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
                        }
                });

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
		if (player == null) {
			play();
		} else {
			pause();
		}
        }//GEN-LAST:event_jButton1ActionPerformed

        private void formInternalFrameClosing(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameClosing
		stop();
        }//GEN-LAST:event_formInternalFrameClosing

        private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
		stop();
		if (files.length > 0) index = (index + 1) % files.length;
		open();
        }//GEN-LAST:event_jButton3ActionPerformed

        private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
		stop();
		if (files.length > 0) index = (index - 1 + files.length) % files.length;
		open();
        }//GEN-LAST:event_jButton2ActionPerformed

        private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
		stop();
        }//GEN-LAST:event_jButton4ActionPerformed

        private void jSlider1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider1StateChanged
		final JSlider source = (JSlider)evt.getSource();
		if (!source.getValueIsAdjusting()) {
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
