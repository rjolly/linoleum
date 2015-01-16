package linoleum;

import linoleum.application.ApplicationManager;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Desktop extends javax.swing.JFrame {
	private final GraphicsDevice devices[];
	private Rectangle bounds;

	private Desktop() {
		initComponents();
		try {
			PackageManager.instance.init();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		desktopPane.add(ApplicationManager.instance);
		console.setContentPane(new Console());
		devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
		bounds = getBounds();
	}

	private void open() {
		ApplicationManager.instance.setVisible(true);
	}

	private void about() {
		final AboutFrame frame = new AboutFrame();
		desktopPane.add(frame);
		frame.setVisible(true);
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

	private void screenshot() {
		final BufferedImage bi = new BufferedImage(getRootPane().getWidth(), getRootPane().getHeight(), BufferedImage.TYPE_INT_ARGB);
		getRootPane().print(bi.createGraphics());
		try {
			ImageIO.write(bi, "png", new File(new File(System.getProperty("user.home")), "screenshot.png"));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                desktopPane = new javax.swing.JDesktopPane();
                console = new javax.swing.JInternalFrame();
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
                consoleMenuItem = new javax.swing.JMenuItem();
                fullScreenMenuItem = new javax.swing.JCheckBoxMenuItem();
                screenshotMenuItem = new javax.swing.JMenuItem();
                helpMenu = new javax.swing.JMenu();
                contentMenuItem = new javax.swing.JMenuItem();
                aboutMenuItem = new javax.swing.JMenuItem();

                setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

                console.setClosable(true);
                console.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
                console.setIconifiable(true);
                console.setMaximizable(true);
                console.setResizable(true);
                console.setTitle("Console");

                javax.swing.GroupLayout consoleLayout = new javax.swing.GroupLayout(console.getContentPane());
                console.getContentPane().setLayout(consoleLayout);
                consoleLayout.setHorizontalGroup(
                        consoleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 388, Short.MAX_VALUE)
                );
                consoleLayout.setVerticalGroup(
                        consoleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 277, Short.MAX_VALUE)
                );

                desktopPane.add(console);
                console.setBounds(0, 0, 404, 310);

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

                consoleMenuItem.setMnemonic('c');
                consoleMenuItem.setText("Console");
                consoleMenuItem.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                consoleMenuItemActionPerformed(evt);
                        }
                });
                viewMenu.add(consoleMenuItem);

                fullScreenMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
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

        private void consoleMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_consoleMenuItemActionPerformed
		console.setVisible(true);
        }//GEN-LAST:event_consoleMenuItemActionPerformed

        private void screenshotMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_screenshotMenuItemActionPerformed
		screenshot();
        }//GEN-LAST:event_screenshotMenuItemActionPerformed

	public static void main(String args[]) {
		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new Desktop().fullScreen();
			}
		});
	}

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JMenuItem aboutMenuItem;
        private javax.swing.JInternalFrame console;
        private javax.swing.JMenuItem consoleMenuItem;
        private javax.swing.JMenuItem contentMenuItem;
        private javax.swing.JMenuItem copyMenuItem;
        private javax.swing.JMenuItem cutMenuItem;
        private javax.swing.JMenuItem deleteMenuItem;
        private javax.swing.JDesktopPane desktopPane;
        private javax.swing.JMenu editMenu;
        private javax.swing.JMenuItem exitMenuItem;
        private javax.swing.JMenu fileMenu;
        private javax.swing.JCheckBoxMenuItem fullScreenMenuItem;
        private javax.swing.JMenu helpMenu;
        private javax.swing.JMenuBar menuBar;
        private javax.swing.JMenuItem openMenuItem;
        private javax.swing.JMenuItem pasteMenuItem;
        private javax.swing.JMenuItem saveAsMenuItem;
        private javax.swing.JMenuItem saveMenuItem;
        private javax.swing.JMenuItem screenshotMenuItem;
        private javax.swing.JMenu viewMenu;
        // End of variables declaration//GEN-END:variables
}
