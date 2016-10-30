package linoleum.mail;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import javax.activation.*;
import javax.mail.*;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JInternalFrame;
import javax.swing.JSplitPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

public class MultipartViewer extends JPanel implements Viewer {
	final JPanel p = new JPanel(new GridBagLayout());
	final JScrollPane scp = new JScrollPane(p);
	final JSplitPane sp = new JSplitPane();
	Component comp;

	public MultipartViewer() {
		super(new GridLayout(1,1));
		sp.setOneTouchExpandable(true);
		sp.setRightComponent(scp);
		sp.setResizeWeight(1.0);
		add(sp);
	}

	public void setCommandContext(final String verb, final DataHandler dh) throws IOException {
		// get the content, and hope it is a Multipart Object
		Object content = dh.getContent();
		if (content instanceof Multipart) {
			setupDisplay((Multipart)content);
		} else {
			setupErrorDisplay(content);
		}
	}

	private void setupDisplay(final Multipart mp) {
		// we display the first body part in a main frame on the left, and then
		// on the right we display the rest of the parts as attachments

		// get the first part
		try {
			final BodyPart bp = mp.getBodyPart(0);
			comp = getComponent(bp);
			sp.setLeftComponent(comp);
		} catch (final MessagingException me) {
			sp.setLeftComponent(new Label(me.toString()));
		}

		// see if there are more than one parts
		try {
			final int count = mp.getCount();
			final GridBagConstraints gc = new GridBagConstraints();

			// setup how to display them
			gc.gridwidth = GridBagConstraints.REMAINDER;
			gc.gridheight = 1;
			gc.fill = GridBagConstraints.NONE;
			gc.anchor = GridBagConstraints.NORTH;
			gc.weightx = 0.0;
			gc.weighty = 0.0;
			gc.insets = new Insets(4,4,4,4);

			// for each one we create a button with the content type
			for(int i = 1; i < count; i++) { // we skip the first one 
				final BodyPart curr = mp.getBodyPart(i);
				String label = null;
				if (label == null) label = curr.getFileName();
				if (label == null) label = curr.getDescription();
				if (label == null) label = curr.getContentType();

				final JButton button = new JButton(label);
				button.addActionListener(new AttachmentViewer(curr));
				p.add(button, gc);
			}
		} catch(final MessagingException me) {
			me.printStackTrace();
		}
	}

	private Component getComponent(final BodyPart bp) throws MessagingException {
		final DataHandler dh = bp.getDataHandler();
		final CommandInfo ci = dh.getCommand("view");
		if (ci == null) {
			throw new MessagingException("view command failed on: " + bp.getContentType());
		}
		final Object bean = dh.getBean(ci);
		if (bean instanceof Component) {
			return (Component)bean;
		} else {
			throw new MessagingException(bean == null?"bean is null, class " + ci.getCommandClass() + " , command " + ci.getCommandName():"bean is not a awt.Component" + bean.getClass());
		}
	}

	private void setupErrorDisplay(final Object content) {
		final String error = content == null?"Content is null":"Object not of type Multipart, content class = " + content.getClass();
		System.out.println(error);
		final Label lab = new Label(error);
		add(lab);
	}

	private JInternalFrame getFrame() {
		for (Container p = getParent(); p != null; p = p.getParent()) {
			if (p instanceof JInternalFrame) {
				return (JInternalFrame) p;
			}
		}
		return null;
	}

	class AttachmentViewer implements ActionListener {
		final BodyPart bp;

		public AttachmentViewer(final BodyPart part) {
			bp = part;
		}

		public void actionPerformed(final ActionEvent e) {
			(new SwingWorker<Component, Object>() {
				public Component doInBackground() throws MessagingException {
					return getComponent(bp);
				}

				@Override
				protected void done() {
					try {
						display(get());
					} catch (final Exception e) {
						display(new Label(e.toString()));
					}
				}

			}).execute();
		}
	}

	private void display(final Component comp) {
		final ComponentFrame f = new ComponentFrame(comp, "Attachment");
		final JInternalFrame frame = getFrame();
		frame.getDesktopPane().add(f);
		f.pack();
		final Dimension s = f.getSize();
		final Dimension size = frame.getSize();
		final int width = Math.min(s.width, size.width);
		final int height = Math.min(s.height, size.height);
		f.setSize(width, height);
		f.show();
		if (comp instanceof Viewer) {
			((Viewer)comp).scrollToOrigin();
		}
	}

	public void scrollToOrigin() {
		if (comp instanceof Viewer) {
			((Viewer)comp).scrollToOrigin();
		}
	}
}
