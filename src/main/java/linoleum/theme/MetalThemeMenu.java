package linoleum.theme;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalTheme;
import javax.swing.plaf.metal.OceanTheme;

@SuppressWarnings("serial")
public class MetalThemeMenu extends JMenu implements ActionListener {
	final MetalTheme[] themes = {
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
		for (int i = 0; i < themes.length; i++) {
			final JRadioButtonMenuItem item = new JRadioButtonMenuItem(themes[i].getName());
			group.add(item);
			add(item);
			item.setActionCommand(i + "");
			item.addActionListener(this);
			if (i == 0) {
				item.setSelected(true);
			}
		}
	}

	public void actionPerformed(final ActionEvent e) {
		final String numStr = e.getActionCommand();
		final MetalTheme selectedTheme = themes[Integer.parseInt(numStr)];
		MetalLookAndFeel.setCurrentTheme(selectedTheme);
		try {
			UIManager.setLookAndFeel(MetalLookAndFeel.class.getName());
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}
}
