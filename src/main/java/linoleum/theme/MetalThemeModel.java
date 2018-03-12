package linoleum.theme;

import java.util.Map;
import java.util.HashMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalTheme;
import javax.swing.plaf.metal.OceanTheme;

@SuppressWarnings("serial")
public class MetalThemeModel extends DefaultComboBoxModel<String> {
	private final Map<String, MetalTheme> map = new HashMap<>();
	private final MetalTheme[] themes = {
		new OceanTheme(),
		new DefaultMetalTheme(),
		new GreenMetalTheme(),
		new AquaMetalTheme(),
		new KhakiMetalTheme(),
		new DemoMetalTheme(),
		new ContrastMetalTheme(),
		new BigContrastMetalTheme(),
		new PropertiesMetalTheme("MyTheme.theme")
	};

	@SuppressWarnings("LeakingThisInConstructor")
	public MetalThemeModel() {
		for (final MetalTheme theme : themes) {
			final String name = theme.getName();
			map.put(name, theme);
			addElement(name);
		}
	}

	public void select(final String str) {
		MetalLookAndFeel.setCurrentTheme(map.get(str));
		try {
			UIManager.setLookAndFeel(MetalLookAndFeel.class.getName());
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public String getSelectedItem() {
		return (String) super.getSelectedItem();
	}
}
