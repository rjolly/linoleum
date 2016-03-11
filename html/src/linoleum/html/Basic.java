package linoleum.html;

import java.awt.Component;
import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.callback.CallbackHandler;
import org.apache.commons.codec.binary.Base64;

public class Basic {

	public String auth(final Component comp) throws IOException {
		final CallbackHandler handler = new DialogCallbackHandler(comp);
		final NameCallback name = new NameCallback("Name:");
		final PasswordCallback password = new PasswordCallback("Password:", false);
		try {
			handler.handle(new Callback[] {name, password});
			return "Basic " + Base64.encodeBase64String((name.getName() + ":" + String.valueOf(password.getPassword())).getBytes());
		} catch (final UnsupportedCallbackException e) {
			throw new IOException(e.getMessage());
		}
	}
}
