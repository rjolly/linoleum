package linoleum.application;

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
		model.removeAllElements();
		manager = new ScriptEngineManager();
		for (final ScriptEngineFactory sef : manager.getEngineFactories()) {
			model.addElement(sef.getEngineName());
		}
		reload();
	}

	private void reload() {
		final String language = prefs.get(getKey("language"), "");
		for (final ScriptEngineFactory sef : manager.getEngineFactories()) {
			if (factory == null || language.equals(sef.getEngineName())) {
				factory = sef;
			}
		}
		if (factory == null) {
			throw new RuntimeException("cannot load " + language + " factory");
		}
	}

	@Override
	public void load() {
		model.setSelectedItem(prefs.get(getKey("language"), ""));
	}

	@Override
	public void save() {
		prefs.put(getKey("language"), (String) model.getSelectedItem());
	}
}
