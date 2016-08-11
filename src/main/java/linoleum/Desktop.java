package linoleum;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.filechooser.FileSystemView;
import javax.swing.ImageIcon;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.util.prefs.Preferences;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import linoleum.application.ApplicationManager;
import linoleum.application.OptionPanel;

public class Desktop extends JFrame {
	public static Desktop instance;
	private static final String ABOUTMSG = "%s %s.%s \n \nJava desktop environment "
		+ "and software distribution. \n \nWritten by \n  "
		+ "%s";
	private final Settings settings;
	private final Packages pkgs;
	private final ApplicationManager apps;
	private final GraphicsDevice devices[];
	private final Preferences prefs = Preferences.userNodeForPackage(getClass());
	private Rectangle bounds;

	private Desktop() {
		initComponents();
		frame.setLayer(0);
		apps = new ApplicationManager(desktopPane);
		pkgs = new Packages(apps);
		settings = new Settings(apps);
		devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
		bounds = getBounds();
		prefs.addPreferenceChangeListener(new PreferenceChangeListener() {
			public void preferenceChange(final PreferenceChangeEvent evt) {
				label.setIcon(getBackgroundImage());
			}
		});
	}

	public OptionPanel getOptionPanel() {
		return settings;
	}

	public Packages getPackages() {
		return pkgs;
	}

	private Icon getBackgroundImage() {
		final String str = prefs.get("background", null);
		return str != null && !str.isEmpty()?new ImageIcon(str):new ImageIcon(getClass().getResource("Wave.png"));
	}

	private void open() {
		apps.select();
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
			final File file = new File(new File(System.getProperty("java.home")), "../docs/api/index.html").getCanonicalFile();
			if (file.exists()) apps.open(file.toURI());
		} catch (final IOException ex) {}
	}

	private void fullScreen() {
		dispose();
                setUndecorated(fullScreenMenuItem.isSelected());
		if (devices.length > 1) {
			if (fullScreenMenuItem.isSelected()) {
				bounds = getBounds();
				setExtendedState(MAXIMIZED_BOTH);
			} else {
				setExtendedState(NORMAL);
				setBounds(bounds);
			}
		} else {
			final GraphicsConfiguration c = getGraphicsConfiguration();
			final GraphicsDevice g = c.getDevice();
			g.setFullScreenWindow(fullScreenMenuItem.isSelected()?this:null);
		}
		setVisible(true);
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
			final File dir = FileSystemView.getFileSystemView().getDefaultDirectory();
			ImageIO.write(bi, "png", new File(dir, "screenshot.png"));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                desktopPane = new linoleum.DesktopPane();
                frame = new javax.swing.JInternalFrame();
                label = new javax.swing.JLabel();
                menuBar = new javax.swing.JMenuBar();
                fileMenu = new javax.swing.JMenu();
                openMenuItem = new javax.swing.JMenuItem();
                saveMenuItem = new javax.swing.JMenuItem();
                saveAsMenuItem = new javax.swing.JMenuItem();
                exitMenuItem = new javax.swing.JMenuItem();
                editMenu = new javax.swing.JMenu();
                cutMenuItem = new javax.swing.JMenuItem();
                copyMenuItem = new javax.swing.JMenuItem();
                pasteMenuItem = new javax.swing.JMenuItem();
                deleteMenuItem = new javax.swing.JMenuItem();
                viewMenu = new javax.swing.JMenu();
                fullScreenMenuItem = new javax.swing.JCheckBoxMenuItem();
                screenshotMenuItem = new javax.swing.JMenuItem();
                helpMenu = new javax.swing.JMenu();
                contentMenuItem = new javax.swing.JMenuItem();
                aboutMenuItem = new javax.swing.JMenuItem();

                setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
                addComponentListener(new java.awt.event.ComponentAdapter() {
                        public void componentResized(java.awt.event.ComponentEvent evt) {
                                formComponentResized(evt);
                        }
                });

                frame.setVisible(true);

                label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                label.setIcon(getBackgroundImage());
                frame.getContentPane().add(label, java.awt.BorderLayout.CENTER);

                desktopPane.add(frame);
                frame.setBounds(90, 40, 24, 36);

                fileMenu.setMnemonic('f');
                fileMenu.setText("File");

                openMenuItem.setMnemonic('o');
                openMenuItem.setText("Open");
                openMenuItem.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                openMenuItemActionPerformed(evt);
                        }
                });
                fileMenu.add(openMenuItem);

                saveMenuItem.setMnemonic('s');
                saveMenuItem.setText("Save");
                fileMenu.add(saveMenuItem);

                saveAsMenuItem.setMnemonic('a');
                saveAsMenuItem.setText("Save As ...");
                saveAsMenuItem.setDisplayedMnemonicIndex(5);
                fileMenu.add(saveAsMenuItem);

                exitMenuItem.setMnemonic('x');
                exitMenuItem.setText("Exit");
                exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                exitMenuItemActionPerformed(evt);
                        }
                });
                fileMenu.add(exitMenuItem);

                menuBar.add(fileMenu);

                editMenu.setMnemonic('e');
                editMenu.setText("Edit");

                cutMenuItem.setMnemonic('t');
                cutMenuItem.setText("Cut");
                editMenu.add(cutMenuItem);

                copyMenuItem.setMnemonic('y');
                copyMenuItem.setText("Copy");
                editMenu.add(copyMenuItem);

                pasteMenuItem.setMnemonic('p');
                pasteMenuItem.setText("Paste");
                editMenu.add(pasteMenuItem);

                deleteMenuItem.setMnemonic('d');
                deleteMenuItem.setText("Delete");
                editMenu.add(deleteMenuItem);

                menuBar.add(editMenu);

                viewMenu.setMnemonic('v');
                viewMenu.setText("View");

                fullScreenMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
                fullScreenMenuItem.setMnemonic('f');
                fullScreenMenuItem.setSelected(true);
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

                contentMenuItem.setMnemonic('c');
                contentMenuItem.setText("Contents");
                contentMenuItem.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                contentMenuItemActionPerformed(evt);
                        }
                });
                helpMenu.add(contentMenuItem);

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
		resize();
        }//GEN-LAST:event_formComponentResized

        private void contentMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contentMenuItemActionPerformed
		content();
        }//GEN-LAST:event_contentMenuItemActionPerformed

	public static void main(String args[]) {
		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				(instance = new Desktop()).fullScreen();
			}
		});
	}

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JMenuItem aboutMenuItem;
        private javax.swing.JMenuItem contentMenuItem;
        private javax.swing.JMenuItem copyMenuItem;
        private javax.swing.JMenuItem cutMenuItem;
        private javax.swing.JMenuItem deleteMenuItem;
        private linoleum.DesktopPane desktopPane;
        private javax.swing.JMenu editMenu;
        private javax.swing.JMenuItem exitMenuItem;
        private javax.swing.JMenu fileMenu;
        private javax.swing.JInternalFrame frame;
        private javax.swing.JCheckBoxMenuItem fullScreenMenuItem;
        private javax.swing.JMenu helpMenu;
        private javax.swing.JLabel label;
        private javax.swing.JMenuBar menuBar;
        private javax.swing.JMenuItem openMenuItem;
        private javax.swing.JMenuItem pasteMenuItem;
        private javax.swing.JMenuItem saveAsMenuItem;
        private javax.swing.JMenuItem saveMenuItem;
        private javax.swing.JMenuItem screenshotMenuItem;
        private javax.swing.JMenu viewMenu;
        // End of variables declaration//GEN-END:variables
}
