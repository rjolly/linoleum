package linoleum.application;

import java.net.URI;
import javax.swing.Icon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

@SuppressWarnings("deprecation")
public class ApplicationWrapper extends Frame {
	private final Application app;

	public ApplicationWrapper(final Application app) {
		this.app = app;
	}

	@Override
	public JInternalFrame getFrame() {
		return app.open(getURI());
	}

	@Override
	public String getName() {
		return app.getName();
	}

	@Override
	public Icon getIcon() {
		return app.getIcon();
	}

	@Override
	public String getMimeType() {
		return app.getMimeType();
	}

	@Override
	public void open(final URI uri, final JDesktopPane desktop) {
		setURI(uri);
		super.open(uri, desktop);
	}
}
