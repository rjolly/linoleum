package linoleum.theme;

import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;

public class GreenMetalTheme extends DefaultMetalTheme {
	@Override
	public String getName() {
		return "Emerald";
	}
	// greenish colors
	private final ColorUIResource primary1 = new ColorUIResource(51, 102, 51);
	private final ColorUIResource primary2 = new ColorUIResource(102, 153, 102);
	private final ColorUIResource primary3 = new ColorUIResource(153, 204, 153);

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
