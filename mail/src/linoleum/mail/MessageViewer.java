package linoleum.mail;

import java.awt.*;
import java.awt.event.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import java.util.Date;
import java.io.IOException;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

public class MessageViewer extends javax.swing.JPanel implements Viewer {
	private final Icon deleteIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Delete16.gif"));
	private final Action replyAction = new ReplyAction();
	private final Action replyToAllAction = new ReplyToAllAction();
	private final Action forwardAction = new ForwardAction();
	private final Action deleteAction = new DeleteAction();
	private final Action structureAction = new StructureAction();
	private SimpleClient client;
	private Message displayed;
	private Component mainbody;

	private class ReplyAction extends AbstractAction {
		public ReplyAction() {
			super("Reply");
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			reply(false);
		}
	}

	private class ReplyToAllAction extends AbstractAction {
		public ReplyToAllAction() {
			super("Reply to all");
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			reply(true);
		}
	}

	private class ForwardAction extends AbstractAction {
		public ForwardAction() {
			super("Forward");
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			reply(false, true);
		}
	}

	private void reply(final boolean all) {
		reply(all, false);
	}

	private void reply(final boolean all, final boolean fw) {
		try {
			final MimeMessage msg = (MimeMessage) displayed;
			final MimeMessage reply = (MimeMessage) msg.reply(all);
			if (fw) {
				reply.setSubject(reply.getSubject().replaceFirst("Re:", "Fw:"));
				reply.setRecipients(Message.RecipientType.TO, new Address[0]);
				reply.setRecipients(Message.RecipientType.CC, new Address[0]);
			}
			client.compose(params(reply));
		} catch (final MessagingException me) {
			me.printStackTrace();
		}
	}

	private String params(final Message msg) throws MessagingException {
		final Address to[] = msg.getRecipients(Message.RecipientType.TO);
		final Address cc[] = msg.getRecipients(Message.RecipientType.CC);
		final StringBuilder bld = new StringBuilder();
		if (cc != null) {
			append(bld, "cc", mkUnicodeString(cc));
		}
		append(bld, "inReplyTo", mkString(msg.getHeader("In-Reply-To")));
		append(bld, "references", mkString(msg.getHeader("References")));
		append(bld, "subject", msg.getSubject());
		final String str = bld.toString();
		return (to == null?"":mkUnicodeString(to)) + (str.isEmpty()?"":"?") + str;
	}

	private StringBuilder append(final StringBuilder bld, final String key, final String value) {
		if (value != null && !value.isEmpty()) {
			if (bld.length() > 0) {
				bld.append("&");
			}
			bld.append(key).append("=").append(value);
		}
		return bld;
	}

	private <T> String mkUnicodeString(final Address array[]) {
		final StringBuilder bld = new StringBuilder();
		for (int i = 0 ; i < array.length ; i++) {
			if (i > 0) {
				bld.append(",");
			}
			final Address a = array[i];
			bld.append(a instanceof InternetAddress?((InternetAddress) a).toUnicodeString():a.toString());
		}
		return bld.toString();
	}

	private <T> String mkString(final T array[]) {
		final StringBuilder bld = new StringBuilder();
		for (int i = 0 ; i < array.length ; i++) {
			if (i > 0) {
				bld.append(",");
			}
			bld.append(array[i]);
		}
		return bld.toString();
	}

	private class DeleteAction extends AbstractAction {
		public DeleteAction() {
			super("Delete", deleteIcon);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			try {
				client.getFolderViewer().getModel().delete(displayed);
			} catch (final MessagingException me) {
				me.printStackTrace();
			}
		}
	}

	private class StructureAction extends AbstractAction {
		public StructureAction() {
			super("Message Structure");
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			System.out.println("\n" + getValue(Action.NAME));
			dumpPart("", displayed);
		}
	}

	public Action getReplyAction() {
		return replyAction;
	}

	public Action getReplyToAllAction() {
		return replyToAllAction;
	}

	public Action getForwardAction() {
		return forwardAction;
	}

	public Action getDeleteAction() {
		return deleteAction;
	}

	public Action getStructureAction() {
		return structureAction;
	}

	public MessageViewer() {
		initComponents();
		setMessage(null);
	}

	public void setClient(final SimpleClient client) {
		this.client = client;
	}

	public final void setMessage(final Message what) {
		displayed = what;

		if (mainbody != null) {
			jPanel1.remove(mainbody);
		}
		if (what != null) {
			loadHeaders();
			replyAction.setEnabled(true);
			replyToAllAction.setEnabled(true);
			forwardAction.setEnabled(true);
			deleteAction.setEnabled(true);
			structureAction.setEnabled(true);

			// add the main body
			jPanel1.add(mainbody = getBodyComponent());
		} else {
			headers.setText("");
			replyAction.setEnabled(false);
			replyToAllAction.setEnabled(false);
			forwardAction.setEnabled(false);
			deleteAction.setEnabled(false);
			structureAction.setEnabled(false);
		}

		invalidate();
		validate();
		scrollToOrigin();
	}

	public void scrollToOrigin() {
		if (mainbody instanceof Viewer) {
			((Viewer)mainbody).scrollToOrigin();
		}
	}

	private void loadHeaders() {
		// setup what we want in our viewer
		final StringBuffer sb = new StringBuffer();

		try {
			// date
			final Date date = displayed.getSentDate();
			if (date != null) {
				sb.append("Date: ");
				sb.append(date.toString());
				sb.append("\n");
			}

			// from
			Address[] adds = displayed.getFrom();
			if (adds != null) {
				sb.append("From: ");
				sb.append(mkUnicodeString(adds));
				sb.append("\n");
			}

			// to
			adds = displayed.getRecipients(Message.RecipientType.TO);
			if (adds != null) {
				sb.append("To: ");
				sb.append(mkUnicodeString(adds));
				sb.append("\n");
			}

			// cc
			adds = displayed.getRecipients(Message.RecipientType.CC);
			if (adds != null) {
				sb.append("Cc: ");
				sb.append(mkUnicodeString(adds));
				sb.append("\n");
			}

			// subject
			sb.append("Subject: ");
			sb.append(displayed.getSubject());

			headers.setText(sb.toString());
		} catch (final MessagingException me) {
			headers.setText("");
		}
	}

	private Component getBodyComponent() {
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
				System.out.println(o.getClass());
			}
		}
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                jScrollPane1 = new javax.swing.JScrollPane();
                headers = new javax.swing.JTextArea();
                jPanel1 = new javax.swing.JPanel();
                jButton5 = new javax.swing.JButton();

                headers.setEditable(false);
                headers.setRows(5);
                jScrollPane1.setViewportView(headers);

                jPanel1.setLayout(new java.awt.CardLayout());

                jButton5.setAction(getStructureAction());
                jButton5.setText("Structure");

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
                this.setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(jButton5)
                                .addGap(0, 0, Short.MAX_VALUE))
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(jButton5)
                                .addGap(1, 1, 1)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE))
                );
        }// </editor-fold>//GEN-END:initComponents

	private void dumpPart(final String prefix, final Part p) {
		try {
			System.out.println(prefix + "----------------");
			System.out.println(prefix + "Content-Type: " + p.getContentType());
			System.out.println(prefix + "Class: " + p.getClass());

			final Object o = p.getContent();
			System.out.println(prefix + "Content: " + (o == null?"is null":o.getClass()));
			if (o instanceof Multipart) {
				final String newpref = prefix + "\t";
				final Multipart mp = (Multipart)o;
				final int count = mp.getCount();
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

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JTextArea headers;
        private javax.swing.JButton jButton5;
        private javax.swing.JPanel jPanel1;
        private javax.swing.JScrollPane jScrollPane1;
        // End of variables declaration//GEN-END:variables
}
