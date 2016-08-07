package linoleum.mail;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.prefs.Preferences;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import linoleum.application.Frame;

public class Compose extends Frame {
	private JTextField toField;
	private JTextField ccField;
	private JTextField bccField;
	private JTextField subField;
	private JTextArea content;
	private String inReplyTo[];
	private String references[];
	private File file;
	private final Session session;
	private final int openFrameCount;
	private final Collection<Integer> openFrames;
	private final Preferences prefs = Preferences.userNodeForPackage(getClass());
	private final FileChooser chooser = new FileChooser();
	private static final int offset = 30;

	public Compose() {
		this(Session.getInstance(System.getProperties()));
	}

	public Compose(final Session session) {
		this(session, new HashSet<Integer>());
	}

	public Compose(final Session session, final Collection<Integer> openFrames) {
		openFrameCount = (openFrames.isEmpty()?0:Collections.max(openFrames)) + 1;
		this.openFrames = openFrames;
		this.session = session;
                setClosable(true);
                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);
		setTitle("Untitled Message " + openFrameCount);
		setIcon(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/ComposeMail24.gif")));

		JPanel top = new JPanel();
		top.setBorder(new EmptyBorder(10, 10, 10, 10));
		top.setLayout(new BorderLayout());
		top.add(buildAddressPanel(), BorderLayout.NORTH);

		content = new JTextArea(15, 30);
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
		return new Compose(session, openFrames);
	}

	@Override
	public void open() {
		final URI uri = getURI();
		if (uri != null) {
			final String str = uri.getSchemeSpecificPart();
			final String s[] = str.split("\\?");
			if (s.length > 0) {
				toField.setText(s[0]);
			}
			if (s.length > 1) {
				for (final String t : s[1].split("&")) {
					final String r[] = t.split("=");
					if (r.length > 1) {
						switch (r[0]) {
						case "cc":
							ccField.setText(r[1]);
							break;
						case "subject":
							subField.setText(r[1]);
							break;
						case "inReplyTo":
							inReplyTo = r[1].split(",");
							break;
						case "references":
							references = r[1].split(",");
							break;
						default:
						}
					}
				}
			}
		}
		openFrames.add(openFrameCount);
	}

	@Override
	public void close() {
		openFrames.remove(openFrameCount);
	}

	private JPanel buildButtonPanel() {
		final JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		final JButton attach = new JButton("Attach...");
		attach.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent evt) {
				final int returnVal = chooser.showInternalOpenDialog(Compose.this);
				switch (returnVal) {
				case JFileChooser.APPROVE_OPTION:
					file = chooser.getSelectedFile();
					break;
				case JFileChooser.CANCEL_OPTION:
					file = null;
					break;
				default:
				}
				attach.setText(file == null?"Attach...":file.getName());
			}
		});
		p.add(attach, BorderLayout.WEST);
		final JButton send = new JButton("Send");
		send.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent evt) {
				try {
					send();
					try {
						setClosed(true);
					} catch (final PropertyVetoException e) {
						e.printStackTrace();
					}
				} catch (final MessagingException me) {
					me.printStackTrace();
				}
			}
		});
		p.add(send, BorderLayout.EAST);
		return p;
	}

	private void send() throws MessagingException {
		final Message msg = new MimeMessage(session);
		final String from = prefs.get(SimpleClient.name + ".from", null);
		final String to = toField.getText();
		final String cc = ccField.getText();
		final String bcc = bccField.getText();
		final String subject = subField.getText();
		final String text = content.getText();
		final String url = prefs.get(SimpleClient.name + ".url", null);
		final String record = prefs.get(SimpleClient.name + ".record", null);

		if (from != null) {
			msg.setFrom(new InternetAddress(from));
		} else {
			msg.setFrom();
		}
		msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
		if (cc != null) {
			msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc, false));
		}
		if (bcc != null) {
			msg.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc, false));
		}
		msg.setSubject(subject);
		if (file != null) {
			try {
				MimeBodyPart mbp1 = new MimeBodyPart();
				mbp1.setText(text);
				MimeBodyPart mbp2 = new MimeBodyPart();
				mbp2.attachFile(file.getPath());
				MimeMultipart mp = new MimeMultipart();
				mp.addBodyPart(mbp1);
				mp.addBodyPart(mbp2);
				msg.setContent(mp);
			} catch (final IOException e) {
				e.printStackTrace();
			}
		} else {
			msg.setText(text);
		}
		if (inReplyTo != null) {
			for (final String str : inReplyTo) {
				msg.addHeader("In-Reply-To", str);
			}
		}
		if (references != null) {
			for (final String str : references) {
				msg.addHeader("References", str);
			}
		}
		msg.setHeader("X-Mailer", SimpleClient.name);
		msg.setSentDate(new Date());
		Transport.send(msg);
		System.out.println("Mail was sent successfully.");

		if (url != null && record != null) {
			final Store store = session.getStore(new URLName(url));
			store.connect();
			final Folder folder = store.getFolder(record);
			if (!folder.exists()) {
				folder.create(Folder.HOLDS_MESSAGES);
			}
			final Message msgs[] = new Message[] {msg};
			folder.appendMessages(msgs);
			System.out.println("Mail was recorded successfully.");
		}
	}

	private JPanel buildAddressPanel() {
		final JPanel p = new JPanel();
		p.setLayout(new LabeledPairLayout());

		final JLabel toLabel = new JLabel("To: ", JLabel.RIGHT);
		toField = new JTextField(25);
		p.add(toLabel, "label");
		p.add(toField, "field");

		final JLabel ccLabel = new JLabel("Cc: ", JLabel.RIGHT);
		ccField = new JTextField(25);
		p.add(ccLabel, "label");
		p.add(ccField, "field");

		final JLabel bccLabel = new JLabel("Bcc: ", JLabel.RIGHT);
		bccField = new JTextField(25);
		p.add(bccLabel, "label");
		p.add(bccField, "field");

		final JLabel subLabel = new JLabel("Subj: ", JLabel.RIGHT);
		subField = new JTextField(25);
		p.add(subLabel, "label");
		p.add(subField, "field");

		return p;
	}
}
