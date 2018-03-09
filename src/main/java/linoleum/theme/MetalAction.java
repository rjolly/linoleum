package linoleum.theme;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;

public class MetalAction extends AbstractAction {
	private final MetalTheme theme;

	public MetalAction(final MetalTheme theme) {
		super(theme.getName());
		this.theme = theme;
	}

	@Override
	public void actionPerformed(final ActionEvent evt) {
		MetalLookAndFeel.setCurrentTheme(theme);
		try {
			UIManager.setLookAndFeel(MetalLookAndFeel.class.getName());
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	public String getName() {
		return (String) getValue(NAME);
	}

	public void select() {
		putValue(SELECTED_KEY, true);
		actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
	}
}
