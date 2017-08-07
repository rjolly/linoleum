package linoleum.application;

import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

public class OptionPanel extends JPanel {
	private Frame frame;
	private boolean dirty;

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

	public void setFrame(final Frame frame) {
		this.frame = frame;
	}

	public void setDirty(final boolean dirty) {
		this.dirty = dirty;
	}

	public final void load() {
		for (final Component comp : getComponents()) {
			if (comp instanceof JTextComponent) {
				((JTextComponent) comp).getDocument().removeDocumentListener(listener);
			}
		}
		frame.load();
		for (final Component comp : getComponents()) {
			if (comp instanceof JTextComponent) {
				((JTextComponent) comp).getDocument().addDocumentListener(listener);
			}
		}
		dirty = false;
	}

	public final void save() {
		if (dirty) {
			frame.save();
			dirty = false;
		}
	}
}
