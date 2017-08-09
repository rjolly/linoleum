package linoleum.application;

import java.util.Map;
import java.util.HashMap;
import java.util.prefs.Preferences;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import linoleum.application.event.ClassPathListener;
import linoleum.application.event.ClassPathChangeEvent;

public class ScriptSupport extends Frame {
	private final Preferences prefs = Preferences.userNodeForPackage(getClass());
	private final Map<String, ScriptEngineFactory> factories = new HashMap<>();
	private final Map<String, ScriptEngineFactory> factoriesByName = new HashMap<>();
	private final DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
	private	ScriptEngineFactory factory;
	private ScriptEngineManager manager;

	public ScriptEngineFactory getFactory() {
		return factory;
	}

	public ComboBoxModel<String> getModel() {
		return model;
	}

	@Override
	public void init() {
		getApplicationManager().addClassPathListener(new ClassPathListener() {
			@Override
			public void classPathChanged(final ClassPathChangeEvent e) {
				refresh();
			}
		});
		prefs.addPreferenceChangeListener(new PreferenceChangeListener() {
			@Override
			public void preferenceChange(final PreferenceChangeEvent evt) {
				if (evt.getKey().equals(getKey("language"))) {
					reload();
				}
			}
		});
		refresh();
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
		final String language = prefs.get(getKey("language"), "");
		if ((factory = getFactory(language)) == null) {
			throw new RuntimeException("cannot load " + language + " factory");
		}
	}

	private ScriptEngineFactory getFactory(final String language) {
		return factories.get(factories.containsKey(language) ? language : "");
	}

	@Override
	public void load() {
		model.setSelectedItem(getFactory(prefs.get(getKey("language"), "")).getEngineName());
	}

	@Override
	public void save() {
		prefs.put(getKey("language"), factoriesByName.get(model.getSelectedItem()).getNames().get(0));
	}
}
