package linoleum.theme;

import java.util.Map;
import java.util.HashMap;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalTheme;
import javax.swing.plaf.metal.OceanTheme;

@SuppressWarnings("serial")
public class MetalThemeMenu extends JMenu {
	private final Map<String, MetalAction> map = new HashMap<>();
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
	public MetalThemeMenu() {
		final ButtonGroup group = new ButtonGroup();
		for (final MetalTheme theme : themes) {
			final MetalAction action = new MetalAction(theme);
			final JRadioButtonMenuItem item = new JRadioButtonMenuItem(action);
			map.put(action.getName(), action);
			group.add(item);
			add(item);
		}
	}

	public void select(final String str) {
		map.get(str).select();
	}

	public String selected() {
		return MetalLookAndFeel.getCurrentTheme().getName();
	}
}
