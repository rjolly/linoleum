package linoleum.theme;

import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;

public class AquaMetalTheme extends DefaultMetalTheme {
	@Override
	public String getName() {
		return "Oxide";
	}
	private final ColorUIResource primary1 = new ColorUIResource(102, 153, 153);
	private final ColorUIResource primary2 = new ColorUIResource(128, 192, 192);
	private final ColorUIResource primary3 = new ColorUIResource(159, 235, 235);

	@Override
	protected ColorUIResource getPrimary1() {
		return primary1;
	}

	@Override
	protected ColorUIResource getPrimary2() {
		return primary2;
	}

	@Override
	protected ColorUIResource getPrimary3() {
		return primary3;
	}
}
