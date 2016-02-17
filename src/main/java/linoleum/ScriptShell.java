package linoleum;

import java.awt.Container;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.swing.ImageIcon;
import linoleum.application.Frame;

public class ScriptShell extends Frame implements ScriptShellPanel.CommandProcessor {
	private volatile ScriptEngine engine;
	private CountDownLatch engineReady = new CountDownLatch(1);
	private String extension;
	private volatile String prompt;

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
		engine.setBindings(createBindings(), ScriptContext.ENGINE_SCOPE);
	}

	// Name of the System property used to select scripting language
	private static final String LANGUAGE_KEY = "linoleum.console.language";

	private String getScriptLanguage() {
		// check whether explicit System property is set
		String lang = System.getProperty(LANGUAGE_KEY);
		if (lang == null) {
			// default is JavaScript
			lang = "JavaScript";
		}
		return lang;
	}

	// create Bindings that is backed by a synchronized HashMap
	private Bindings createBindings() {
		Map<String, Object> map
			= Collections.synchronizedMap(new HashMap<String, Object>());
		return new SimpleBindings(map);
	}

	// create and initialize script engine
	private void initScriptEngine() {
		// set pre-defined global variables
		setGlobals();
		// load pre-defined initialization file
		loadInitFile(ClassLoader.getSystemResource("com/sun/tools/script/shell/init." + extension));
		loadInitFile(getClass().getResource("init." + extension));
		final File home = PackageManager.instance.home();
		loadUserInitFile(new File(home, "init." + extension));
		if (!home.equals(new File("."))) {
			// load current user's initialization file
			loadUserInitFile(new File("init." + extension));
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
