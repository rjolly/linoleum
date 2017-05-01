package linoleum.application;

import java.beans.ConstructorProperties;
import javax.swing.JInternalFrame;

public class InternalFrameWrapper extends Frame {
	private final JInternalFrame frame;

	@ConstructorProperties({"frame"})
	public InternalFrameWrapper(final JInternalFrame frame) {
		final String name = frame.getName();
		setName(name == null?frame.getClass().getSimpleName():name);
		this.frame = frame;
	}

	@Override
	public JInternalFrame getFrame() {
		return frame;
	}
}
