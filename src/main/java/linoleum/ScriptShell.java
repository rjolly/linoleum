package linoleum;

import java.awt.Container;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.prefs.Preferences;
import java.util.concurrent.CountDownLatch;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.ImageIcon;
import linoleum.application.Frame;

public class ScriptShell extends Frame implements ScriptShellPanel.CommandProcessor {
	private final Preferences prefs = Preferences.userNodeForPackage(getClass());
	private final CountDownLatch engineReady = new CountDownLatch(1);
	private volatile ScriptEngine engine;
	private volatile String prompt;
	private String extension;

	public ScriptShell(final boolean start) {
		initComponents();
		setIcon(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/development/Host24.gif")));
		createScriptEngine();
		setContentPane(new ScriptShellPanel(this));
		final Thread thread = new Thread() {
			@Override
			public void run() {
				initScriptEngine();
				engineReady.countDown();
			}
		};
		if (start) {
			thread.start();
		}
	}

	public ScriptShell() {
		this(false);
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
		} catch (InterruptedException ie) {
			res = ie.getMessage();
		} catch (ScriptException se) {
			res = se.getMessage();
		}
		return res;
	}

	@Override
	public Frame getFrame() {
		return new ScriptShell(true);
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                setClosable(true);
                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);
                setTitle("Script");

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

        // Variables declaration - do not modify//GEN-BEGIN:variables
        // End of variables declaration//GEN-END:variables

	private void createScriptEngine() {
		ScriptEngineManager manager = new ScriptEngineManager();
		String language = getScriptLanguage();
		engine = manager.getEngineByName(language);
		if (engine == null) {
			throw new RuntimeException("cannot load " + language + " engine");
		}
		extension = engine.getFactory().getExtensions().get(0);
		prompt = extension + ">";
	}

	// Name of the System property used to select scripting language
	private static final String LANGUAGE_KEY = "linoleum.console.language";

	private String getScriptLanguage() {
		// check whether explicit System property is set
		String lang = System.getProperty(LANGUAGE_KEY);
		if (lang == null) {
			// default is JavaScript
			lang = prefs.get(getName() + ".lang", "JavaScript");
		}
		return lang;
	}

	// create and initialize script engine
	private void initScriptEngine() {
		// set pre-defined global variables
		setGlobals();
		// load pre-defined initialization file
		loadInitFile(ClassLoader.getSystemResource("com/sun/tools/script/shell/init." + extension));
		final File home = Desktop.pkgs.home();
		loadUserInitFile(new File(home, "init." + extension));
		try {
			if (!Files.isSameFile(home.toPath(), Paths.get(""))) {
				// load current user's initialization file
				loadUserInitFile(new File("init." + extension));
			}
		} catch (final IOException e) {}
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
		} catch (final Exception ex) {
			ex.printStackTrace();
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
		} catch (final Exception ex) {
			ex.printStackTrace();
		} finally {
			engine.put(ScriptEngine.FILENAME, oldFilename);
		}
	}
}
