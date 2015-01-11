package linoleum.application;

import java.net.URI;
import javax.swing.JInternalFrame;

public interface Application {
	public String getName();
	public JInternalFrame open(URI uri);
}
