package linoleum.xhtml;

import linoleum.application.Frame;
import linoleum.html.Browser;

public class Saucer extends Browser {
	public Saucer() {
		setMimeType("text/html:application/xhtml+xml");
		final javax.swing.text.EditorKit kit = new EditorKit();
		getEditorPane().setEditorKitForContentType("text/xml", kit);
		getEditorPane().setEditorKitForContentType("application/xml", kit);
	}

	@Override
	public Frame getFrame() {
		return new Saucer();
	}
}
