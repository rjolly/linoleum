package linoleum.mail;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
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
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import linoleum.application.FileChooser;
import linoleum.application.Frame;

public class Compose extends Frame {
	private final Icon sendIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/SendMail16.gif"));
	private final Action attachAction = new AttachAction();
	private final Action sendAction = new SendAction();
	private JTextField toField;
	private JTextField ccField;
	private JTextField bccField;
	private JTextField subField;
	private JTextArea content;
	private String inReplyTo[];
	private String references[];
	private File file;
	private final Session session;
	private final Preferences prefs = Preferences.userNodeForPackage(getClass());
	private final FileChooser chooser = new FileChooser();

	private class AttachAction extends AbstractAction {
		public AttachAction() {
			super("Attach...");
		}

		@Override
		public void actionPerformed(final ActionEvent evt) {
			final int returnVal = chooser.showInternalOpenDialog(Compose.this);
			switch (returnVal) {
			case JFileChooser.APPROVE_OPTION:
				file = chooser.getSelectedFile();
				break;
			default:
				file = null;
			}
			putValue(Action.NAME, file == null?"Attach...":file.getName());
		}
	}

	private class SendAction extends AbstractAction {
		public SendAction() {
			super("Send", sendIcon);
		}

		@Override
		public void actionPerformed(final ActionEvent evt) {
			try {
				send();
				setClosed(true);
			} catch (final PropertyVetoException e) {
				e.printStackTrace();
			} catch (final MessagingException me) {
				me.printStackTrace();
			}
		}
	}

	public Compose() {
		this(Session.getInstance(System.getProperties()));
	}

	public Compose(final Session session) {
		this(session, null);
	}

	public Compose(final Session session, final Frame parent) {
		super(parent);
		setIcon(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/ComposeMail24.gif")));
                setClosable(true);
                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);
		setTitle("Untitled Message " + (index + 1));
		setJMenuBar(this.parent.getJMenuBar());
		this.session = session;

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
	}

	@Override
	public Frame getFrame(final Frame parent) {
		return new Compose(session, parent);
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
	}

	private JPanel buildButtonPanel() {
		final JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		final JButton attach = new JButton(attachAction);
		p.add(attach, BorderLayout.WEST);
		final JButton send = new JButton(sendAction);
		p.add(send, BorderLayout.EAST);
		return p;
	}

	private void send() throws MessagingException {
		final Message msg = new MimeMessage(session);
		final String from = prefs.get(SimpleClient.instance.getKey("from"), "");
		final String to = toField.getText();
		final String cc = ccField.getText();
		final String bcc = bccField.getText();
		final String subject = subField.getText();
		final String text = content.getText();
		final String url = prefs.get(SimpleClient.instance.getKey("url"), "");
		final String record = prefs.get(SimpleClient.instance.getKey("record"), "");

		if (!from.isEmpty()) {
			msg.setFrom(new InternetAddress(from));
		} else {
			msg.setFrom();
		}
		msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
		if (!cc.isEmpty()) {
			msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc, false));
		}
		if (!bcc.isEmpty()) {
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
		msg.setHeader("X-Mailer", SimpleClient.instance.getName());
		msg.setSentDate(new Date());
		Transport.send(msg);
		System.out.println("Mail was sent successfully.");

		if (!url.isEmpty() && !record.isEmpty()) {
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
