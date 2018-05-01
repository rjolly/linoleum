package linoleum;

import java.awt.Component;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import linoleum.application.Frame;
import linoleum.application.ScriptSupport;

public class ScriptShell extends ScriptSupport implements ScriptShellPanel.CommandProcessor, Runnable {
	private final CountDownLatch engineReady = new CountDownLatch(1);
	private final List<String> extensions = new ArrayList<>();
	private volatile ScriptEngine engine;
	private volatile String prompt;
	private ScriptShellPanel panel;
	private String extension;
	private Path file;
	private Path path;

	public ScriptShell() {
		initComponents();
		setIcon(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/development/Host24.gif")));
		setMimeType("application/*");
		setURI(Paths.get("").toUri());
	}

	@Override
	public Component getFocusOwner() {
		return panel;
	}

	@Override
	public void setURI(final URI uri) {
		final Path path = getPath(uri);
		file = Files.isDirectory(path)?null:path;
		extension = file == null?null:getExtension(file.toString());
		this.path = unfile(path);
	}

	private String getExtension(final String name) {
		return name.substring(name.lastIndexOf(".") + 1);
	}

	private Path unfile(final Path path) {
		return Files.isDirectory(path)?path:getParent(path);
	}

	private Path getParent(final Path path) {
		final Path parent = path.getParent();
		return parent == null?Paths.get(""):parent;
	}

	@Override
	public URI getURI() {
		return path.toUri();
	}

	@Override
	public void open() {
		new Thread(this).start();
	}

	public void run() {
		if (engine == null) {
			createScriptEngine();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setTitle(engine.getFactory().getLanguageName());
					setContentPane(panel = new ScriptShellPanel(ScriptShell.this));
				}
			});
			initScriptEngine();
			engineReady.countDown();
		}
		doOpen();
	}

	@Override
	public String getPrompt() {
		return prompt;
	}

	@Override
	public String executeCommand(String cmd) {
		String res;
		try {
			engineReady.await();
			Object tmp = engine.eval(cmd);
			res = (tmp == null) ? null : tmp.toString();
		} catch (final InterruptedException ie) {
			res = ie.getMessage();
		} catch (final ScriptException se) {
			res = se.getMessage();
		}
		return res;
	}

	@Override
	public Frame getFrame() {
		return new ScriptShell();
	}

	@Override
	public boolean reuseFor(final URI that) {
		return that == null?false:getPath().equals(unfile(getPath(that)));
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                optionPanel1 = new linoleum.application.OptionPanel();
                jLabel2 = new javax.swing.JLabel();
                jComboBox1 = new javax.swing.JComboBox();

                jLabel2.setText("Language :");

                jComboBox1.setModel(getModel());
                jComboBox1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jComboBox1ActionPerformed(evt);
                        }
                });

                javax.swing.GroupLayout optionPanel1Layout = new javax.swing.GroupLayout(optionPanel1);
                optionPanel1.setLayout(optionPanel1Layout);
                optionPanel1Layout.setHorizontalGroup(
                        optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(optionPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBox1, 0, 294, Short.MAX_VALUE)
                                .addContainerGap())
                );
                optionPanel1Layout.setVerticalGroup(
                        optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(optionPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel2)
                                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );

                setClosable(true);
                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);
                setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/development/Host16.gif"))); // NOI18N
                setOptionPanel(optionPanel1);

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 394, Short.MAX_VALUE)
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 277, Short.MAX_VALUE)
                );

                pack();
        }// </editor-fold>//GEN-END:initComponents

        private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
		optionPanel1.setDirty(true);
        }//GEN-LAST:event_jComboBox1ActionPerformed

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JComboBox jComboBox1;
        private javax.swing.JLabel jLabel2;
        private linoleum.application.OptionPanel optionPanel1;
        // End of variables declaration//GEN-END:variables

	// create script engine
	private void createScriptEngine() {
		engine = getEngine();
		extensions.addAll(engine.getFactory().getExtensions());
		if (!extensions.contains(extension)) {
			extension = extensions.get(0);
		}
		prompt = extension + ">";
	}

	// initialize script engine
	private void initScriptEngine() {
		// set pre-defined global variables
		setGlobals();
		// load pre-defined initialization file
		try {
			loadInitFile(ClassLoader.getSystemClassLoader().loadClass("com.sun.tools.script.shell.Main").getResource("init." + extension));
		} catch (final ClassNotFoundException e) {
			e.printStackTrace();
		}
		try {
			final String str = System.getProperty("linoleum.home");
			if (str != null && !Files.isSameFile(Paths.get(str), Paths.get("."))) {
				loadUserInitFile(new File(new File(str), "init." + extension));
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		// load current user's initialization file
		loadUserInitFile(new File("init." + extension));
		engine.put("curDir", path.toFile());
	}

	private Path getPath() {
		final Object obj = engine.get("curDir");
		if (obj instanceof File) {
			return ((File) obj).toPath();
		}
		return path;
	}

	private void doOpen() {
		if (file != null && extensions.contains(extension)) {
			loadUserInitFile(file.toFile());
		}
	}

	// set pre-defined global variables for script
	private void setGlobals() {
		engine.put("engine", engine);
		engine.put("frame", this);
	}

	private void loadInitFile(final URL url) {
		String oldFilename = (String) engine.get(ScriptEngine.FILENAME);
		if (url == null) {
			return;
		}
		engine.put(ScriptEngine.FILENAME, url.getPath());
		try (final InputStreamReader reader = new InputStreamReader(url.openStream())) {
			engine.eval(reader);
		} catch (final IOException ex) {
			ex.printStackTrace();
		} catch (final ScriptException se) {
			se.printStackTrace();
		} finally {
			engine.put(ScriptEngine.FILENAME, oldFilename);
		}
	}

	private void loadUserInitFile(final File file) {
		final String oldFilename = (String) engine.get(ScriptEngine.FILENAME);
		if (!file.exists()) {
			return;
		}
		engine.put(ScriptEngine.FILENAME, file.getName());
		try (final InputStreamReader reader = new FileReader(file)) {
			engine.eval(reader);
		} catch (final IOException ex) {
			ex.printStackTrace();
		} catch (final ScriptException se) {
			se.printStackTrace();
		} finally {
			engine.put(ScriptEngine.FILENAME, oldFilename);
		}
	}
}
