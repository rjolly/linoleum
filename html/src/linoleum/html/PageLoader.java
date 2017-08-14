package linoleum.html;

import javax.swing.SwingWorker;

public abstract class PageLoader extends SwingWorker<Boolean, Object> {
	private int length;

	public void setLength(final int length) {
		this.length = length;
	}

	public void setNumber(final int n) {
		if (length > 0) {
			setProgress(100 * n / length);
		}
	}
}
