package linoleum.application;

import java.beans.ConstructorProperties;
import javax.swing.JInternalFrame;

public class InternalFrameWrapper extends Frame {
	private final JInternalFrame frame;

	@ConstructorProperties({"frame"})
	public InternalFrameWrapper(final JInternalFrame frame) {
		this.frame = frame;
	}

	@Override
	public JInternalFrame getFrame() {
		return frame;
	}

	@Override
	public String getName() {
		final String name = frame.getName();
		return name == null?frame.getClass().getSimpleName():name;
	}
}
