package linoleum.notepad;

import java.awt.Font;
import javax.swing.JTextArea;

public class Editor extends JTextArea {
	public Editor() {
		setDragEnabled(true);
		setLineWrap(true);
		setFont(new Font("monospaced", Font.PLAIN, 12));
	}

	public void replaceSelection(final String str) {
		final Document doc = getReplaceDocument();
		doc.hold();
		super.replaceSelection(str);
		doc.release();
	}

	public Document getReplaceDocument() {
		return (Document)getDocument();
	}

	protected Document createDefaultModel() {
		return new Document();
	}
}
