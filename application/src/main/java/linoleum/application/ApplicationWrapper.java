package linoleum.application;

import java.beans.ConstructorProperties;
import java.net.URI;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

@SuppressWarnings("deprecation")
public class ApplicationWrapper extends Frame {
	private final Application app;

	@ConstructorProperties({"application"})
	public ApplicationWrapper(final Application app) {
		setName(app.getName());
		setIcon(app.getIcon());
		setMimeType(app.getMimeType());
		this.app = app;
	}

	public Application getApplication() {
		return app;
	}

	@Override
	public JInternalFrame getFrame() {
		return app.open(getURI());
	}

	@Override
	public void open(final URI uri, final JDesktopPane desktop) {
		setURI(uri);
		super.open(uri, desktop);
	}
}
