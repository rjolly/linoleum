package linoleum;

import java.awt.Container;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
		loadInitFile();
		// load current user's initialization file
		loadUserInitFile();
	}

	// set pre-defined global variables for script
	private void setGlobals() {
		engine.put("engine", engine);
		engine.put("frame", this);
	}

	private void loadInitFile() {
		String oldFilename = (String) engine.get(ScriptEngine.FILENAME);
		engine.put(ScriptEngine.FILENAME, "<built-in init." + extension + ">");
		try {
			InputStream stream = ClassLoader.getSystemResourceAsStream("com/sun/tools/script/shell/init."
				+ extension);
			if (stream != null) {
				engine.eval(new InputStreamReader(new BufferedInputStream(stream)));
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		} finally {
			engine.put(ScriptEngine.FILENAME, oldFilename);
		}
	}

	private void loadUserInitFile() {
		final String oldFilename = (String) engine.get(ScriptEngine.FILENAME);
		final String dir = System.getProperty("linoleum.home", System.getProperty("user.dir"));
		if (dir == null) {
			return;
		}
		final File file = new File(dir, "init." + extension);
		if (!file.exists()) {
			return;
		}
		engine.put(ScriptEngine.FILENAME, file.getName());
		try {
			engine.eval(new FileReader(file));
		} catch (Exception exp) {
			exp.printStackTrace();
		} finally {
			engine.put(ScriptEngine.FILENAME, oldFilename);
		}
	}
}
