package linoleum.mail;

import java.awt.*;
import java.io.*;
import java.beans.*;
import javax.activation.*;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

public class TextViewer extends JPanel implements Viewer {
	private JTextArea text_area = null;
	private DataHandler dh = null;
	private String verb = null;

	public TextViewer() {
		super(new GridLayout(1,1));

		// create the text area
		text_area = new JTextArea();
		text_area.setEditable(false);
		text_area.setLineWrap(true);

		// create a scroll pane for the JTextArea
		JScrollPane sp = new JScrollPane();
		sp.setPreferredSize(new Dimension(600, 400));
		sp.getViewport().add(text_area);

		add(sp);
	}

	public void setCommandContext(final String verb, final DataHandler dh) throws IOException {
		this.verb = verb;
		this.dh = dh;
		int bytes_read = 0;
		// check that we can actually read
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final byte data[] = new byte[1024];
		try (final InputStream ins = dh.getInputStream()) {
			while ((bytes_read = ins.read(data)) > 0) {
				baos.write(data, 0, bytes_read);
			} 
		}
		// convert the buffer into a string
		// place in the text area
		text_area.setText(baos.toString());
	}

	public void scrollToOrigin() {
		text_area.setCaretPosition(0);
	}
}
