package linoleum.mail;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;

class LabeledPairLayout implements LayoutManager {

	List<Component> labels = new ArrayList<Component>();
	List<Component> fields = new ArrayList<Component>();
	int yGap = 2;
	int xGap = 2;

	public void addLayoutComponent(String s, Component c) {
		if (s.equals("label")) {
			labels.add(c);
		} else {
			fields.add(c);
		}
	}

	public void layoutContainer(Container c) {
		Insets insets = c.getInsets();

		int labelWidth = 0;
		for (Component comp : labels) {
			labelWidth = Math.max(labelWidth, comp.getPreferredSize().width);
		}

		int yPos = insets.top;

		Iterator<Component> fieldIter = fields.listIterator();
		Iterator<Component> labelIter = labels.listIterator();
		while (labelIter.hasNext() && fieldIter.hasNext()) {
			JComponent label = (JComponent) labelIter.next();
			JComponent field = (JComponent) fieldIter.next();
			int height = Math.max(label.getPreferredSize().height, field.
					getPreferredSize().height);
			label.setBounds(insets.left, yPos, labelWidth, height);
			field.setBounds(insets.left + labelWidth + xGap,
					yPos,
					c.getSize().width - (labelWidth + xGap + insets.left
					+ insets.right),
					height);
			yPos += (height + yGap);
		}
	}

	public Dimension minimumLayoutSize(Container c) {
		Insets insets = c.getInsets();

		int labelWidth = 0;
		for (Component comp : labels) {
			labelWidth = Math.max(labelWidth, comp.getPreferredSize().width);
		}

		int yPos = insets.top;

		Iterator<Component> labelIter = labels.listIterator();
		Iterator<Component> fieldIter = fields.listIterator();
		while (labelIter.hasNext() && fieldIter.hasNext()) {
			Component label = labelIter.next();
			Component field = fieldIter.next();
			int height = Math.max(label.getPreferredSize().height, field.
					getPreferredSize().height);
			yPos += (height + yGap);
		}
		return new Dimension(labelWidth * 3, yPos);
	}

	public Dimension preferredLayoutSize(Container c) {
		Dimension d = minimumLayoutSize(c);
		d.width *= 2;
		return d;
	}

	public void removeLayoutComponent(Component c) {
	}
}
