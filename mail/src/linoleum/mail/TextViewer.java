package linoleum.mail;

import java.awt.*;
import java.io.*;
import javax.activation.*;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;

public class TextViewer extends AbstractViewer {
	private final EditorPane text_area;
	private final JPopupMenu menu = new JPopupMenu();

	public TextViewer() {
		super(new GridLayout(1,1));

		// create the text area
		text_area = new EditorPane();
		text_area.setEditable(false);

		// create a scroll pane for the JTextArea
		final JScrollPane sp = new JScrollPane();
		sp.getViewport().add(text_area);

		add(sp);
		text_area.setComponentPopupMenu(menu);
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
		text_area.setDocument(doc);
		text_area.read(ins, doc);
		menu.add(new SaveAsAction(dh, null));
	}

	public void scrollToOrigin() {
		text_area.setCaretPosition(0);
	}
}
