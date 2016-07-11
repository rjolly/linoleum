package linoleum.mail;

import java.awt.BorderLayout;
import javax.swing.ImageIcon;
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
}
