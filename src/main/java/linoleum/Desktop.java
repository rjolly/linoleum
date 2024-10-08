package linoleum;

import java.beans.ExceptionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import linoleum.application.ApplicationManager;

public class Desktop extends JFrame {
	private final Preferences prefs = Preferences.userNodeForPackage(getClass());
	private final Icon openIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Open16.gif"));
	private final Icon saveIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Save16.gif"));
	private final Icon contentsIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Help16.gif"));
	private final Icon aboutIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/About16.gif"));
	private static final String ABOUTMSG = "%s %s.%s \n \nJava desktop environment "
		+ "and software distribution. \n \nWritten by \n  "
		+ "%s";
	private final Action openAction = new OpenAction();
	private final Action saveAction = new SaveAction();
	private final Action restartAction = new RestartAction();
	private final Action exitAction = new ExitAction();
	private final Action fullScreenAction = new FullScreenAction();
	private final Action screenshotAction = new ScreenshotAction();
	private final Action contentsAction = new ContentsAction();
	private final Action aboutAction = new AboutAction();
	private final GraphicsDevice devices[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
	private final PropertyChangeListener listener = new PropertyChangeListener() {
		public void propertyChange(final PropertyChangeEvent e) {
			final String name = e.getPropertyName();
			if (name.equals("lookAndFeel")) {
				SwingUtilities.updateComponentTreeUI(getRootPane());
				if (frame != null) {
					frame.update();
				}
			}
		}
	};
	private final File file = new File("desktop.xml");
	private final ApplicationManager apps;
	private final Background frame;
	private final Console console;
	private Rectangle bounds;

	private class OpenAction extends AbstractAction {
		public OpenAction() {
			super("Open", openIcon);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
			putValue(MNEMONIC_KEY, (int) 'o');
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			desktopPane.setRecording(false);
			apps.select();
			contentsAction.setEnabled(true);
		}
	}

	private class SaveAction extends AbstractAction {
		public SaveAction() {
			super("Save", saveIcon);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.ALT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
			putValue(MNEMONIC_KEY, (int) 's');
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					save();
				}
			});
		}
	}

	private class RestartAction extends AbstractAction {
		public RestartAction() {
			super("Restart");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.ALT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
			putValue(MNEMONIC_KEY, (int) 'r');
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			System.exit(1);
		}
	}

	private class ExitAction extends AbstractAction {
		public ExitAction() {
			super("Exit");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.ALT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
			putValue(MNEMONIC_KEY, (int) 'x');
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			System.exit(0);
		}
	}

	private class FullScreenAction extends AbstractAction {
		public FullScreenAction() {
			super("Full screen");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.ALT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
			putValue(MNEMONIC_KEY, (int) 'f');
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			desktopPane.setRecording(false);
			fullScreen();
		}
	}

	private class ScreenshotAction extends AbstractAction {
		public ScreenshotAction() {
			super("Screenshot");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK));
			putValue(MNEMONIC_KEY, (int) 's');
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final BufferedImage bi = new BufferedImage(getRootPane().getWidth(), getRootPane().getHeight(), BufferedImage.TYPE_INT_ARGB);
			getRootPane().print(bi.createGraphics());
			try {
				ImageIO.write(bi, "png", new File("screenshot.png"));
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private class ContentsAction extends AbstractAction {
		public ContentsAction() {
			super("Contents", contentsIcon);
			putValue(MNEMONIC_KEY, (int) 'c');
			setEnabled(false);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final String contents = resolveExpression(frame.getContents(), System.getProperties());
			try {
				apps.open(contents.startsWith("file:")?new File(contents.substring(5)).toURI():new URI(contents));
			} catch (final URISyntaxException ex) {
				ex.printStackTrace();
			}
		}
	}

	private final Pattern EXPRESSION_PATTERN = Pattern.compile("\\$\\{([^}]*)\\}");

	private String resolveExpression(final String expression, final Properties properties) {
		final StringBuilder result = new StringBuilder(expression.length());
		final Matcher matcher = EXPRESSION_PATTERN.matcher(expression);
		int i = 0;
		while (matcher.find()) {
			result.append(expression.substring(i, matcher.start()));
			final String property = matcher.group(1);
			result.append(properties.containsKey(property)?properties.get(property):matcher.group());
			i = matcher.end();
		}
		result.append(expression.substring(i));
		return result.toString();
	}

	private class AboutAction extends AbstractAction {
		public AboutAction() {
			super("About", aboutIcon);
			putValue(MNEMONIC_KEY, (int) 'a');
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final java.lang.Package pkg = getClass().getPackage();
			JOptionPane.showInternalMessageDialog(desktopPane, String.format(ABOUTMSG,
					pkg.getImplementationTitle(),
					pkg.getSpecificationVersion(),
					pkg.getImplementationVersion(),
					pkg.getImplementationVendor()));
		}
	}

	private void save() {
		try (final XMLEncoder e = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(file)))) {
			e.setExceptionListener(new ExceptionListener() {
				public void exceptionThrown(final Exception ex) {
					ex.printStackTrace();
				}
			});
			e.writeObject(desktopPane);
		} catch (final FileNotFoundException ex) {
			ex.printStackTrace();
		}
	}

	private void load() {
		try (final XMLDecoder d = new XMLDecoder(new BufferedInputStream(new FileInputStream(file)))) {
			d.setExceptionListener(new ExceptionListener() {
				public void exceptionThrown(final Exception ex) {
					ex.printStackTrace();
				}
			});
			getContentPane().remove(desktopPane);
			desktopPane.destroy();
			new ApplicationManager();
			getContentPane().add(desktopPane = (DesktopPane) d.readObject(), BorderLayout.CENTER);
		} catch (final FileNotFoundException ex) {
			ex.printStackTrace();
		}
	}

	public Desktop() {
		UIManager.addPropertyChangeListener(listener);
		new Background().updateTheme();
		initComponents();
		if (file.exists()) {
			load();
			desktopPane.setReopen(true);
			ApplicationManager apps = null;
			Background frame = null;
			Console console = null;
			for (final JInternalFrame c : desktopPane.getAllFrames()) {
				if (c instanceof Background) {
					frame = (Background) c;
				} else if (c instanceof Console) {
					console = (Console) c;
				} else if (c instanceof ApplicationManager) {
					apps = (ApplicationManager) c;
					if (apps.getApplications().size() > 0) {
						apps.setVisible(true);
						contentsAction.setEnabled(true);
					}
				} else {
					c.setVisible(true);
				}
			}
			this.apps = apps;
			this.frame = frame;
			this.console = console;
			desktopPane.setReopen(false);
		} else {
			apps = new ApplicationManager();
			frame = new Background();
			console = new Console();
			desktopPane.add(frame);
			desktopPane.add(console);
			desktopPane.add(apps);
		}
		frame.setVisible(true);
		frame.setLayer(0);
		apps.manage(frame);
		apps.manage(console);
		apps.manage(apps);
		loadBounds();
	}

	private void fullScreen() {
		dispose();
		final boolean full = fullScreenMenuItem.isSelected();
		setUndecorated(full);
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

	private String getKey(final String str) {
		return getName() + "." + str;
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                desktopPane = new linoleum.DesktopPane();
                menuBar = new javax.swing.JMenuBar();
                fileMenu = new javax.swing.JMenu();
                openMenuItem = new javax.swing.JMenuItem();
                saveMenuItem = new javax.swing.JMenuItem();
                jSeparator1 = new javax.swing.JPopupMenu.Separator();
                restartMenuItem = new javax.swing.JMenuItem();
                separator = new javax.swing.JPopupMenu.Separator();
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
                getContentPane().add(desktopPane, java.awt.BorderLayout.CENTER);

                fileMenu.setMnemonic('f');
                fileMenu.setText("File");

                openMenuItem.setAction(openAction);
                fileMenu.add(openMenuItem);

                saveMenuItem.setAction(saveAction);
                fileMenu.add(saveMenuItem);
                fileMenu.add(jSeparator1);

                restartMenuItem.setAction(restartAction);
                fileMenu.add(restartMenuItem);
                fileMenu.add(separator);

                exitMenuItem.setAction(exitAction);
                fileMenu.add(exitMenuItem);

                menuBar.add(fileMenu);

                viewMenu.setMnemonic('v');
                viewMenu.setText("View");

                fullScreenMenuItem.setAction(fullScreenAction);
                fullScreenMenuItem.setSelected(isFullScreen());
                viewMenu.add(fullScreenMenuItem);

                screenshotMenuItem.setAction(screenshotAction);
                viewMenu.add(screenshotMenuItem);

                menuBar.add(viewMenu);

                helpMenu.setMnemonic('h');
                helpMenu.setText("Help");

                contentMenuItem.setAction(contentsAction);
                helpMenu.add(contentMenuItem);

                aboutMenuItem.setAction(aboutAction);
                helpMenu.add(aboutMenuItem);

                menuBar.add(helpMenu);

                setJMenuBar(menuBar);

                pack();
        }// </editor-fold>//GEN-END:initComponents

        private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
		if (isShowing() && !fullScreenMenuItem.isSelected() && desktopPane.isRecording()) {
			final Component c = evt.getComponent();
			prefs.putInt(getKey("width"), c.getWidth());
			prefs.putInt(getKey("height"), c.getHeight());
		}
		frame.resize();
        }//GEN-LAST:event_formComponentResized

        private void formComponentMoved(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentMoved
		if (isShowing() && !fullScreenMenuItem.isSelected() && desktopPane.isRecording()) {
			final Component c = evt.getComponent();
			prefs.putInt(getKey("x"), c.getX());
			prefs.putInt(getKey("y"), c.getY());
		}
        }//GEN-LAST:event_formComponentMoved

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
        private javax.swing.JMenuItem contentMenuItem;
        private linoleum.DesktopPane desktopPane;
        private javax.swing.JMenuItem exitMenuItem;
        private javax.swing.JMenu fileMenu;
        private javax.swing.JCheckBoxMenuItem fullScreenMenuItem;
        private javax.swing.JMenu helpMenu;
        private javax.swing.JPopupMenu.Separator jSeparator1;
        private javax.swing.JMenuBar menuBar;
        private javax.swing.JMenuItem openMenuItem;
        private javax.swing.JMenuItem restartMenuItem;
        private javax.swing.JMenuItem saveMenuItem;
        private javax.swing.JMenuItem screenshotMenuItem;
        private javax.swing.JPopupMenu.Separator separator;
        private javax.swing.JMenu viewMenu;
        // End of variables declaration//GEN-END:variables
}
