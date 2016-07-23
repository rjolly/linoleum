package linoleum.mail;

import java.awt.*;
import java.io.*;
import java.beans.*;
import javax.activation.*;
import javax.swing.JPanel;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;

public class TextViewer extends JPanel implements Viewer {
	private final JEditorPane text_area;

	public TextViewer() {
		super(new GridLayout(1,1));

		// create the text area
		text_area = new JEditorPane();
		text_area.setEditable(false);

		// create a scroll pane for the JTextArea
		final JScrollPane sp = new JScrollPane();
		sp.getViewport().add(text_area);

		add(sp);
	}

	public void setCommandContext(final String verb, final DataHandler dh) throws IOException {
		final InputStream ins = dh.getInputStream();
		String type = dh.getContentType();
		int n = type.indexOf(";");
		n = type.indexOf(";", n + 1);
		if (n > 0) {
			type = type.substring(0, n);
		}
		text_area.setContentType(type.toLowerCase());
		final EditorKit kit = text_area.getEditorKit();
		final Document doc = kit.createDefaultDocument();
		text_area.read(ins, doc);
	}

	public void scrollToOrigin() {
		text_area.setCaretPosition(0);
	}
}
