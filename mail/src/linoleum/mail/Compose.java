package linoleum.mail;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import linoleum.application.Frame;

public class Compose extends Frame {

	private int openFrameCount = 0;
	private static final int offset = 30;

	public Compose() {
		this(0);
	}

	public Compose(int openFrameCount) {
		super("Untitled Message " + openFrameCount);
		setIcon(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/ComposeMail24.gif")));

		JPanel top = new JPanel();
		top.setBorder(new EmptyBorder(10, 10, 10, 10));
		top.setLayout(new BorderLayout());
		top.add(buildAddressPanel(), BorderLayout.NORTH);

		JTextArea content = new JTextArea(15, 30);
		content.setBorder(new EmptyBorder(0, 5, 0, 5));
		content.setLineWrap(true);

		JScrollPane textScroller = new JScrollPane(content,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		top.add(textScroller, BorderLayout.CENTER);

		setContentPane(top);
		pack();
		setLocation(offset * openFrameCount, offset * openFrameCount);
	}

	@Override
	public Frame getFrame() {
		return new Compose(++openFrameCount);
	}

	private JPanel buildAddressPanel() {
		JPanel p = new JPanel();
		p.setLayout(new LabeledPairLayout());

		JLabel toLabel = new JLabel("To: ", JLabel.RIGHT);
		JTextField toField = new JTextField(25);
		p.add(toLabel, "label");
		p.add(toField, "field");

		JLabel subLabel = new JLabel("Subj: ", JLabel.RIGHT);
		JTextField subField = new JTextField(25);
		p.add(subLabel, "label");
		p.add(subField, "field");

		JLabel ccLabel = new JLabel("cc: ", JLabel.RIGHT);
		JTextField ccField = new JTextField(25);
		p.add(ccLabel, "label");
		p.add(ccField, "field");

		return p;
	}

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
}
