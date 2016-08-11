package linoleum.application;

import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

public abstract class OptionPanel extends JPanel {
	protected boolean dirty;

	private final DocumentListener listener = new DocumentListener() {
		@Override
		public void insertUpdate(final DocumentEvent e) {
			dirty = true;
		}
		@Override
		public void removeUpdate(final DocumentEvent e) {
			dirty = true;
		}
		@Override
		public void changedUpdate(final DocumentEvent e) {
			dirty = true;
		}
	};

	public final void load() {
		for (final Component comp : getComponents()) {
			if (comp instanceof JTextComponent) {
				((JTextComponent) comp).getDocument().removeDocumentListener(listener);
			}
		}
		loadImpl();
		for (final Component comp : getComponents()) {
			if (comp instanceof JTextComponent) {
				((JTextComponent) comp).getDocument().addDocumentListener(listener);
			}
		}
		dirty = false;
	}
 
	protected abstract void loadImpl();

	public final void save() {
		if (dirty) {
			saveImpl();
			dirty = false;
		}
	}

	protected abstract void saveImpl();
}
