package linoleum.application;

import java.util.Map;
import java.util.HashMap;
import java.util.prefs.Preferences;
import java.util.prefs.PreferenceChangeEvent;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import linoleum.application.event.ClassPathListener;
import linoleum.application.event.ClassPathChangeEvent;

public class ScriptSupport extends FileSupport {
	private final Map<String, ScriptEngineFactory> factories = new HashMap<>();
	private final Map<String, ScriptEngineFactory> factoriesByName = new HashMap<>();
	private final DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
	private ScriptEngineFactory factory;
	private ScriptEngineManager manager;

	@Override
	public ScriptSupport getOwner() {
		return (ScriptSupport) super.getOwner();
	}

	public ScriptEngine getEngine() {
		return getOwner().factory.getScriptEngine();
	}

	protected ComboBoxModel<String> getModel() {
		return model;
	}

	@Override
	protected void init() {
		getApplicationManager().addClassPathListener(new ClassPathListener() {
			@Override
			public void classPathChanged(final ClassPathChangeEvent e) {
				refresh();
			}
		});
		Preferences.userNodeForPackage(getClass()).addPreferenceChangeListener(this);
		refresh();
	}

	@Override
	public void preferenceChange(final PreferenceChangeEvent evt) {
		if (evt.getKey().equals(getKey("language"))) {
			reload();
		}
	}

	private void refresh() {
		factories.clear();
		factoriesByName.clear();
		model.removeAllElements();
		manager = new ScriptEngineManager();
		for (final ScriptEngineFactory sef : manager.getEngineFactories()) {
			if (!factories.containsKey("")) {
				factories.put("", sef);
			}
			factories.put(sef.getNames().get(0), sef);
			factoriesByName.put(sef.getEngineName(), sef);
			model.addElement(sef.getEngineName());
		}
		reload();
	}

	private void reload() {
		final String language = getPref("language");
		if ((factory = getFactory(language)) == null) {
			throw new RuntimeException("cannot load " + language + " factory");
		}
	}

	private ScriptEngineFactory getFactory(final String language) {
		return factories.get(factories.containsKey(language) ? language : "");
	}

	@Override
	protected void load() {
		model.setSelectedItem(getFactory(getPref("language")).getEngineName());
	}

	@Override
	protected void save() {
		putPref("language", getSelectedLanguage());
	}

	protected String getSelectedLanguage() {
		final ScriptEngineFactory factory = factoriesByName.get(model.getSelectedItem());
		return factory == null?null:factory.getNames().get(0);
	}
}
