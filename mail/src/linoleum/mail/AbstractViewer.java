package linoleum.mail;

import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import javax.activation.DataHandler;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import linoleum.application.ApplicationManager;
import linoleum.application.FileChooser;

public abstract class AbstractViewer extends JPanel implements Viewer {
	final Icon saveAsIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/SaveAs16.gif"));
	final SimpleClient client = SimpleClient.instance;
	final ApplicationManager apps = client.getApplicationManager();
	final FileChooser chooser = client.getFileChooser();

	class SaveAsAction extends AbstractAction {
		final DataHandler dh;
		final String filename;

		public SaveAsAction(final DataHandler dh, final String filename) {
			super("Save as...", saveAsIcon);
			this.dh = dh;
			this.filename = filename;
		}

		@Override
		public void actionPerformed(final ActionEvent evt) {
			final File file;
			chooser.setSelectedFile(new File(filename == null?"":filename));
			final int returnVal = chooser.showInternalSaveDialog(AbstractViewer.this);
			switch (returnVal) {
			case JFileChooser.APPROVE_OPTION:
				file = chooser.getSelectedFile();
				break;
			default:
				file = null;
			}
			if (file != null && (!file.exists() || proceed())) (new SwingWorker<URI, Object>() {
				public URI doInBackground() throws Exception {
					try (final InputStream is = dh.getInputStream(); final OutputStream os = new FileOutputStream(file)) {
						final byte buffer[] = new byte[4096];
						int n;
						while ((n = is.read(buffer)) != -1) {
							os.write(buffer, 0, n);
						}
					}
					return file.toPath().toUri();
				}

				@Override
				protected void done() {
					try {
						apps.open(get());
					} catch (final Exception e) {
						e.printStackTrace();
					}
				}
			}).execute();
		}
	}

	public AbstractViewer(final LayoutManager layout) {
		super(layout);
	}

	private boolean proceed() {
		switch (JOptionPane.showInternalConfirmDialog(this, "File exists. Overwrite ?", "Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE)) {
		case JOptionPane.OK_OPTION:
			return true;
		default:
		}
		return false;
	}
}
