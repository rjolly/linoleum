package linoleum.theme;

import java.awt.Font;
import javax.swing.UIDefaults;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalIconFactory;

public class DemoMetalTheme extends DefaultMetalTheme {
	@Override
	public String getName() {
		return "Presentation";
	}
	private final FontUIResource controlFont = new FontUIResource("Dialog",
			Font.BOLD, 18);
	private final FontUIResource systemFont = new FontUIResource("Dialog",
			Font.PLAIN, 18);
	private final FontUIResource userFont = new FontUIResource("SansSerif",
			Font.PLAIN, 18);
	private final FontUIResource smallFont = new FontUIResource("Dialog",
			Font.PLAIN, 14);

	@Override
	public FontUIResource getControlTextFont() {
		return controlFont;
	}

	@Override
	public FontUIResource getSystemTextFont() {
		return systemFont;
	}

	@Override
	public FontUIResource getUserTextFont() {
		return userFont;
	}

	@Override
	public FontUIResource getMenuTextFont() {
		return controlFont;
	}

	@Override
	public FontUIResource getWindowTitleFont() {
		return controlFont;
	}

	@Override
	public FontUIResource getSubTextFont() {
		return smallFont;
	}

	@Override
	public void addCustomEntriesToTable(UIDefaults table) {
		super.addCustomEntriesToTable(table);

		final int internalFrameIconSize = 22;
		table.put("InternalFrame.closeIcon", MetalIconFactory.
				getInternalFrameCloseIcon(internalFrameIconSize));
		table.put("InternalFrame.maximizeIcon", MetalIconFactory.
				getInternalFrameMaximizeIcon(internalFrameIconSize));
		table.put("InternalFrame.iconifyIcon", MetalIconFactory.
				getInternalFrameMinimizeIcon(internalFrameIconSize));
		table.put("InternalFrame.minimizeIcon", MetalIconFactory.
				getInternalFrameAltMaximizeIcon(internalFrameIconSize));

		table.put("ScrollBar.width", 21);
	}
}
