package linoleum.mail;

import javax.mail.*;
import java.net.InetAddress;
import java.awt.*;
import javax.swing.*;

@SuppressWarnings("deprecation")
public class SimpleAuthenticator extends Authenticator implements Runnable {
	final JInternalFrame frame;
	String username;
	String password;
	JComponent d;
	int result;

	public SimpleAuthenticator(final JInternalFrame frame) {
		this.frame = frame;
	}

	protected PasswordAuthentication getPasswordAuthentication() {
		// given a prompt ?
		String prompt = getRequestingPrompt();
		if (prompt == null) {
			prompt = "Please login...";
		}
		// protocol
		String protocol = getRequestingProtocol();
		if (protocol == null) {
			protocol = "Unknown protocol";
		}
		// get the host
		String host = null;
		InetAddress inet = getRequestingSite();
		if (inet != null) {
			host = inet.getHostName();
		}
		if (host == null) {
			host = "Unknown host";
		}
		// port
		String port = "";
		int portnum = getRequestingPort();
		if (portnum != -1) {
			port = ", port " + portnum + " ";
		}
		// Build the info string
		String info = "Connecting to " + protocol + " mail service on host " + host + port;

		//JPanel d = new JPanel();
		// XXX - for some reason using a JPanel here causes JOptionPane
		// to display incorrectly, so we workaround the problem using
		// an anonymous JComponent.
		d = new JComponent() { };

		GridBagLayout gb = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		d.setLayout(gb);
		c.insets = new Insets(2, 2, 2, 2);

		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 0.0;
		d.add(constrain(new JLabel(info), gb, c));
		d.add(constrain(new JLabel(prompt), gb, c));

		c.gridwidth = 1;
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.0;
		d.add(constrain(new JLabel("Username:"), gb, c));

		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		String user = getDefaultUserName();
		JTextField username = new JTextField(user, 20);
		d.add(constrain(username, gb, c));

		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		c.weightx = 0.0;
		d.add(constrain(new JLabel("Password:"), gb, c));

		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		JPasswordField password = new JPasswordField("", 20);
		d.add(constrain(password, gb, c));
		// XXX - following doesn't work
		if (user != null && user.length() > 0) {
			password.requestFocus();
		} else {
			username.requestFocus();
		}
		if (SwingUtilities.isEventDispatchThread()) {
			run();
		} else try {
			SwingUtilities.invokeAndWait(this);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		if (result == JOptionPane.OK_OPTION) {
			return new PasswordAuthentication(username.getText(), password.getText());
		} else {
			return null;
		}
	}

	public void run() {
		result = JOptionPane.showInternalConfirmDialog(frame, d, "Login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
	}

	private Component constrain(Component cmp, GridBagLayout gb, GridBagConstraints c) {
		gb.setConstraints(cmp, c);
		return (cmp);
	}
}
