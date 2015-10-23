package linoleum.application;

import java.net.URI;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;

public interface Application {
	public String getName();
	public ImageIcon getIcon();
	public String getMimeType();
	public JInternalFrame open(URI uri);
}
