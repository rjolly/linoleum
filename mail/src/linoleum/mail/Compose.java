package linoleum.mail;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import linoleum.application.Frame;

public class Compose extends Frame {

	private final int openFrameCount;
	private final Collection<Integer> openFrames;
	private static final int offset = 30;

	public Compose() {
		this(new HashSet<Integer>(), 0);
	}

	public Compose(final Collection<Integer> openFrames, final int openFrameCount) {
		super("Untitled Message " + openFrameCount);
		this.openFrameCount = openFrameCount;
		this.openFrames = openFrames;
		setIcon(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/ComposeMail24.gif")));

		JPanel top = new JPanel();
		top.setBorder(new EmptyBorder(10, 10, 10, 10));
		top.setLayout(new BorderLayout());
		top.add(buildAddressPanel(), BorderLayout.NORTH);

		JTextArea content = new JTextArea(15, 30);
		content.setBorder(new EmptyBorder(0, 5, 0, 5));
		content.setLineWrap(true);

		JScrollPane textScroller = new JScrollPane(content);
		top.add(textScroller, BorderLayout.CENTER);

		top.add(buildButtonPanel(), BorderLayout.SOUTH);

		setContentPane(top);
		pack();
		setLocation(offset * openFrameCount, offset * openFrameCount);
	}

	@Override
	public Frame getFrame() {
		int openFrameCount = openFrames.isEmpty()?0:Collections.max(openFrames);
		openFrames.add(++openFrameCount);
		return new Compose(openFrames, openFrameCount);
	}

	@Override
	public void close() {
		openFrames.remove(openFrameCount);
	}

	private JPanel buildButtonPanel() {
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());

		JButton send = new JButton("Send");
		send.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				System.out.println("mail sent");
				dispose();
			}
		});
		p.add(send, BorderLayout.EAST);

		return p;
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
