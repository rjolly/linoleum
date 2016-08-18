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

	public OptionPanel() {
		initComponents();
	}

	@Override
	public String getName() {
		return frame == null?super.getName():frame.getName();
	}

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

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
                this.setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 400, Short.MAX_VALUE)
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 300, Short.MAX_VALUE)
                );
        }// </editor-fold>//GEN-END:initComponents

        // Variables declaration - do not modify//GEN-BEGIN:variables
        // End of variables declaration//GEN-END:variables
}
