package linoleum;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import linoleum.application.ApplicationManager;

public class Desktop extends JFrame {
	public static final Desktop instance = new Desktop();
	private final Preferences prefs = Preferences.userNodeForPackage(getClass());
	private static final String ABOUTMSG = "%s %s.%s \n \nJava desktop environment "
		+ "and software distribution. \n \nWritten by \n  "
		+ "%s";
	private final ApplicationManager apps = new ApplicationManager();
	private final Packages pkgs = new Packages(apps);
	private final Console console = new Console();
	private final GraphicsDevice devices[];
	private Rectangle bounds;
	private boolean full;

	private Desktop() {
		initComponents();
		console.setApplicationManager(apps);
		frame.setApplicationManager(apps);
		frame.setLayer(0);
		desktopPane.add(console);
		desktopPane.add(apps);
		devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
		loadBounds();
	}

	public Packages getPackages() {
		return pkgs;
	}

	private void open() {
		apps.select();
		if (!contentMenuItem.isEnabled()) {
			contentMenuItem.setEnabled(true);
		}
	}

	private void about() {
		final java.lang.Package pkg = getClass().getPackage();
		JOptionPane.showInternalMessageDialog(desktopPane, String.format(ABOUTMSG,
				pkg.getImplementationTitle(),
				pkg.getSpecificationVersion(),
				pkg.getImplementationVersion(),
				pkg.getImplementationVendor()));
	}

	private void content() {
		try {
			final File home = new File(System.getProperty("java.home")).getParentFile().getCanonicalFile();
			final File file = new File(home, "docs/api/index.html");
			if (file.exists()) {
				apps.open(file.toURI());
			}
		} catch (final IOException ex) {
			ex.printStackTrace();
		}
	}

	private void fullScreen() {
		dispose();
		setUndecorated(full = fullScreenMenuItem.isSelected());
		if (devices.length > 1) {
			if (full) {
				bounds = getBounds();
				setExtendedState(MAXIMIZED_BOTH);
			} else {
				setExtendedState(NORMAL);
				setBounds(bounds);
			}
		} else {
			getGraphicsConfiguration().getDevice().setFullScreenWindow(full?this:null);
		}
		setVisible(true);
		prefs.putBoolean(getKey("fullScreen"), full);
	}

	private boolean isFullScreen() {
		return prefs.getBoolean(getKey("fullScreen"), true);
	}

	private void loadBounds() {
		final int x = prefs.getInt(getKey("x"), getX());
		final int y = prefs.getInt(getKey("y"), getY());
		final int width = prefs.getInt(getKey("width"), getWidth());
		final int height = prefs.getInt(getKey("height"), getHeight());
		setBounds(x, y, width, height);
		bounds = getBounds();
	}

	private void resize() {
		final Dimension size = desktopPane.getSize();
		final Insets insets = frame.getInsets();
		final Container panel = frame.getContentPane();
		final int width = frame.getWidth() - panel.getWidth();
		final int height = frame.getHeight() - panel.getHeight();
		frame.setBounds(-insets.left, insets.bottom - height, size.width + width, size.height + height);
	}

	private void screenshot() {
		final BufferedImage bi = new BufferedImage(getRootPane().getWidth(), getRootPane().getHeight(), BufferedImage.TYPE_INT_ARGB);
		getRootPane().print(bi.createGraphics());
		try {
			ImageIO.write(bi, "png", new File("screenshot.png"));
		} catch (final IOException ex) {
			ex.printStackTrace();
		}
	}

	private String getKey(final String str) {
		return getName() + "." + str;
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                desktopPane = new linoleum.DesktopPane();
                frame = new linoleum.Background();
                menuBar = new javax.swing.JMenuBar();
                fileMenu = new javax.swing.JMenu();
                openMenuItem = new javax.swing.JMenuItem();
                exitMenuItem = new javax.swing.JMenuItem();
                viewMenu = new javax.swing.JMenu();
                fullScreenMenuItem = new javax.swing.JCheckBoxMenuItem();
                screenshotMenuItem = new javax.swing.JMenuItem();
                helpMenu = new javax.swing.JMenu();
                contentMenuItem = new javax.swing.JMenuItem();
                aboutMenuItem = new javax.swing.JMenuItem();

                setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
                setName("Desktop"); // NOI18N
                addComponentListener(new java.awt.event.ComponentAdapter() {
                        public void componentResized(java.awt.event.ComponentEvent evt) {
                                formComponentResized(evt);
                        }
                        public void componentMoved(java.awt.event.ComponentEvent evt) {
                                formComponentMoved(evt);
                        }
                });

                frame.setVisible(true);
                desktopPane.add(frame);
                frame.setBounds(0, 0, 891, 531);

                fileMenu.setMnemonic('f');
                fileMenu.setText("File");

                openMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
                openMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Open16.gif"))); // NOI18N
                openMenuItem.setMnemonic('o');
                openMenuItem.setText("Open");
                openMenuItem.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                openMenuItemActionPerformed(evt);
                        }
                });
                fileMenu.add(openMenuItem);

                exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
                exitMenuItem.setMnemonic('x');
                exitMenuItem.setText("Exit");
                exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                exitMenuItemActionPerformed(evt);
                        }
                });
                fileMenu.add(exitMenuItem);

                menuBar.add(fileMenu);

                viewMenu.setMnemonic('v');
                viewMenu.setText("View");

                fullScreenMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
                fullScreenMenuItem.setMnemonic('f');
                fullScreenMenuItem.setSelected(isFullScreen());
                fullScreenMenuItem.setText("Full screen");
                fullScreenMenuItem.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                fullScreenMenuItemActionPerformed(evt);
                        }
                });
                viewMenu.add(fullScreenMenuItem);

                screenshotMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_MASK));
                screenshotMenuItem.setText("Screenshot");
                screenshotMenuItem.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                screenshotMenuItemActionPerformed(evt);
                        }
                });
                viewMenu.add(screenshotMenuItem);

                menuBar.add(viewMenu);

                helpMenu.setMnemonic('h');
                helpMenu.setText("Help");

                contentMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Help16.gif"))); // NOI18N
                contentMenuItem.setMnemonic('c');
                contentMenuItem.setText("Contents");
                contentMenuItem.setEnabled(false);
                contentMenuItem.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                contentMenuItemActionPerformed(evt);
                        }
                });
                helpMenu.add(contentMenuItem);

                aboutMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/About16.gif"))); // NOI18N
                aboutMenuItem.setMnemonic('a');
                aboutMenuItem.setText("About");
                aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                aboutMenuItemActionPerformed(evt);
                        }
                });
                helpMenu.add(aboutMenuItem);

                menuBar.add(helpMenu);

                setJMenuBar(menuBar);

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(desktopPane, javax.swing.GroupLayout.DEFAULT_SIZE, 640, Short.MAX_VALUE)
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(desktopPane, javax.swing.GroupLayout.DEFAULT_SIZE, 451, Short.MAX_VALUE)
                );

                pack();
        }// </editor-fold>//GEN-END:initComponents

	private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
		System.exit(0);
	}//GEN-LAST:event_exitMenuItemActionPerformed

	private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
		open();
	}//GEN-LAST:event_openMenuItemActionPerformed

	private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
		about();
	}//GEN-LAST:event_aboutMenuItemActionPerformed

        private void fullScreenMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fullScreenMenuItemActionPerformed
		fullScreen();
        }//GEN-LAST:event_fullScreenMenuItemActionPerformed

        private void screenshotMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_screenshotMenuItemActionPerformed
		screenshot();
        }//GEN-LAST:event_screenshotMenuItemActionPerformed

        private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
		if (isShowing() && !full) {
			final Component c = evt.getComponent();
			prefs.putInt(getKey("width"), c.getWidth());
			prefs.putInt(getKey("height"), c.getHeight());
		}
		resize();
        }//GEN-LAST:event_formComponentResized

        private void contentMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contentMenuItemActionPerformed
		content();
        }//GEN-LAST:event_contentMenuItemActionPerformed

        private void formComponentMoved(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentMoved
		if (isShowing() && !full) {
			final Component c = evt.getComponent();
			prefs.putInt(getKey("x"), c.getX());
			prefs.putInt(getKey("y"), c.getY());
		}
        }//GEN-LAST:event_formComponentMoved

	public static void main(String args[]) {
		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				instance.fullScreen();
			}
		});
	}

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JMenuItem aboutMenuItem;
        private javax.swing.JMenuItem contentMenuItem;
        private linoleum.DesktopPane desktopPane;
        private javax.swing.JMenuItem exitMenuItem;
        private javax.swing.JMenu fileMenu;
        private linoleum.Background frame;
        private javax.swing.JCheckBoxMenuItem fullScreenMenuItem;
        private javax.swing.JMenu helpMenu;
        private javax.swing.JMenuBar menuBar;
        private javax.swing.JMenuItem openMenuItem;
        private javax.swing.JMenuItem screenshotMenuItem;
        private javax.swing.JMenu viewMenu;
        // End of variables declaration//GEN-END:variables
}
