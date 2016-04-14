package linoleum.notepad;

import java.awt.Font;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

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

	public boolean findNext(final String dst, final boolean wrap) throws BadLocationException {
		final Document doc = getReplaceDocument();
		final String text = doc.getText();
		int n = text.indexOf(dst, getSelectionEnd());
		if (wrap?n < 0:false) {
			n = text.indexOf(dst);
		}
		if (n < 0) {
			return false;
		}
		select(n, n + dst.length());
		return true;
	}

	public boolean findFirst(final String dst) throws BadLocationException {
		final Document doc = getReplaceDocument();
		final String text = doc.getText();
		final int n = text.indexOf(dst);
		if (n < 0) {
			return false;
		}
		select(n, n + dst.length());
		return true;
	}

	public void replaceAll(final String dst, final String str) throws BadLocationException {
		final Document doc = getReplaceDocument();
		doc.hold();
		boolean found = findFirst(dst);
		while (found) {
			super.replaceSelection(str);
			found = findNext(dst, false);
		}
		doc.release();
	}

	public boolean replace(final String dst, final String str) throws BadLocationException {
		final Document doc = getReplaceDocument();
		doc.hold();
		super.replaceSelection(str);
		doc.release();
		return findNext(dst, true);
	}

	public Document getReplaceDocument() {
		return (Document)getDocument();
	}

	protected Document createDefaultModel() {
		return new Document();
	}
}
