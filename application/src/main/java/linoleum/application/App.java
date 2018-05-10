package linoleum.application;

import java.net.URI;
import javax.swing.Icon;
import javax.swing.JDesktopPane;

public interface App {
	public String getName();
	public String getDescription();
	public Icon getIcon();
	public Icon getFrameIcon();
	public String getMimeType();
	public String getScheme();
	public void open(JDesktopPane desktop);
	public void open(URI uri, JDesktopPane desktop);
}
