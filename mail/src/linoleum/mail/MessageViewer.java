package linoleum.mail;

import java.awt.*;
import java.awt.event.*;
import javax.mail.*;
import javax.activation.*;
import java.util.Date;
import java.io.IOException;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class MessageViewer extends JPanel implements Viewer {
	Message displayed;
	Component mainbody;
	final JTextArea headers;

	public MessageViewer() {
		// set our layout
		super(new GridBagLayout());

		// add the toolbar
		addToolbar();

		GridBagConstraints gb = new GridBagConstraints();
		gb.gridwidth = GridBagConstraints.REMAINDER;
		gb.fill = GridBagConstraints.BOTH;
		gb.weightx = 1.0;
		gb.weighty = 0.0;

		// add the headers
		headers = new JTextArea("\n\n\n");
		headers.setEditable(false);
		add(headers, gb);
		setMessage(null);
	}

	public void setMessage(final Message what) {
		displayed = what;

		if (mainbody != null) {
			remove(mainbody);
		}
		if (what != null) {
			loadHeaders();
			mainbody = getBodyComponent();
		} else {
			headers.setText("\n\n\n");
			mainbody = new JPanel();
		}

		// add the main body
		final GridBagConstraints gb = new GridBagConstraints();
		gb.gridwidth = GridBagConstraints.REMAINDER;
		gb.fill = GridBagConstraints.BOTH;
		gb.weightx = 1.0;
		gb.weighty = 1.0;
		add(mainbody, gb);

		invalidate();
		validate();
		scrollToOrigin();
	}

	public void scrollToOrigin() {
		if (mainbody instanceof Viewer) {
			((Viewer)mainbody).scrollToOrigin();
		}
	}

	protected void addToolbar() {
		final GridBagConstraints gb = new GridBagConstraints();
		gb.gridheight = 1;
		gb.gridwidth = 1;
		gb.fill = GridBagConstraints.NONE;
		gb.anchor = GridBagConstraints.WEST;
		gb.weightx = 0.0;
		gb.weighty = 0.0;
		gb.insets = new Insets(4,4,4,4);

		// structure button
		gb.gridwidth = GridBagConstraints.REMAINDER; // only for the last one
		final Button b = new Button("Structure");
		b.addActionListener(new StructureAction());
		add(b, gb);
	}

	protected void loadHeaders() {
		// setup what we want in our viewer
		final StringBuffer sb = new StringBuffer();

		// date
		sb.append("Date: ");
		try {
			final Date duh = displayed.getSentDate();
			if (duh != null) {
				sb.append(duh.toString());
			} else {
				sb.append("Unknown");
			}

			sb.append("\n");

			// from
			sb.append("From: ");
			Address[] adds = displayed.getFrom();
			if (adds != null && adds.length > 0) {
				sb.append(adds[0].toString());
			}
			sb.append("\n");

			// to
			sb.append("To: ");
			adds = displayed.getRecipients(Message.RecipientType.TO);
			if (adds != null && adds.length > 0) {
				sb.append(adds[0].toString());
			}
			sb.append("\n");

			// subject
			sb.append("Subject: ");
			sb.append(displayed.getSubject());

			headers.setText(sb.toString());
		} catch (final MessagingException me) {
			headers.setText("\n\n\n");
		}
	}

	protected Component getBodyComponent() {
		//------------
		// now get a content viewer for the main type...
		//------------
		try {
			final DataHandler dh = displayed.getDataHandler();
			final CommandInfo ci = dh.getCommand("view");
			if (ci == null) {
				throw new MessagingException("view command failed on: " + displayed.getContentType());
			}

			final Object bean = dh.getBean(ci);
			if (bean instanceof Component) {
				return (Component)bean;
			} else {
				throw new MessagingException("bean is not a component: " + bean);
			}
		} catch (final MessagingException me) {
			return new Label(me.toString());
		}
	}

	public void setCommandContext(final String verb, final DataHandler dh) throws IOException {
		final Object o = dh.getContent();
		if (o instanceof Message) {
			setMessage((Message)o);
		} else {
			System.out.println("MessageViewer - content not a Message object, " + o);
			if (o != null) {
				System.out.println(o.getClass().toString());
			}
		}
	}

	class StructureAction implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			System.out.println("\n\nMessage Structure");
			if (displayed != null) {
				dumpPart("", displayed);
			}
		}

		protected void dumpPart(final String prefix, final Part p) {
			try {
				System.out.println(prefix + "----------------");
				System.out.println(prefix + "Content-Type: " + p.getContentType());
				System.out.println(prefix + "Class: " + p.getClass().toString());

				Object o = p.getContent();
				if (o == null) {
					System.out.println(prefix + "Content:  is null");
				} else {
					System.out.println(prefix + "Content: " + o.getClass().toString());
				}

				if (o instanceof Multipart) {
					String newpref = prefix + "\t";
					Multipart mp = (Multipart)o;
					int count = mp.getCount();
					for (int i = 0; i < count; i++) {
						dumpPart(newpref, mp.getBodyPart(i));
					}
				}
			} catch (final MessagingException e) {
				e.printStackTrace();
			} catch (final IOException ioex) {
				System.out.println("Cannot get content" + ioex.getMessage());
			}
		}
	}
}
