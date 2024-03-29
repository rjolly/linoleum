package linoleum.application;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collections;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

public class PreferenceSupport extends Frame implements PreferenceChangeListener {
	private final Preferences prefs = Preferences.userNodeForPackage(getClass());
	private final Properties properties = new Properties();
	private OptionPanel optionPanel;

	public PreferenceSupport() {
		initPref();
	}

	void initPref() {
		try {
			for (final URL resource : Collections.list(ClassLoader.getSystemResources(getClass().getName().replace(".", "/") + ".properties"))) try (final InputStream is = resource.openStream()) {
				properties.load(is);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public void preferenceChange(final PreferenceChangeEvent evt) {
	}

	public void setOptionPanel(final OptionPanel optionPanel) {
		this.optionPanel = optionPanel;
	}

	OptionPanel getOptionPanel() {
		return optionPanel;
	}

	protected void load() {
	}

	protected void save() {
	}

	protected Map<String, String> getMapProperty(final String key) {
		final Map<String, String> map = new TreeMap<>();
		for (final Map.Entry<Object, Object> entry : properties.entrySet()) {
			final String str = entry.getKey().toString();
			if (str.startsWith(key + ".")) {
				map.put(str.substring(str.indexOf(".") + 1), entry.getValue().toString());
			}
		}
		return map;
	}

	protected String getPref(final String key) {
		return prefs.get(getKey(key), properties.getProperty(key, ""));
	}

	protected boolean getBooleanPref(final String key) {
		return prefs.getBoolean(getKey(key), Boolean.valueOf(properties.getProperty(key, Boolean.valueOf(false).toString())));
	}

	protected void putPref(final String key, final String value) {
		if (value == null || value.equals(properties.getProperty(key, ""))) {
			prefs.remove(getKey(key));
		} else {
			prefs.put(getKey(key), value);
		}
	}

	protected void putBooleanPref(final String key, final boolean value) {
		if (value == Boolean.valueOf(properties.getProperty(key, Boolean.valueOf(false).toString())).booleanValue()) {
			prefs.remove(getKey(key));
		} else {
			prefs.putBoolean(getKey(key), value);
		}
	}
}
