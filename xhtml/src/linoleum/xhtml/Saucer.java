package linoleum.xhtml;

import linoleum.application.Frame;
import linoleum.html.Browser;

public class Saucer extends Browser {
	public Saucer() {
		this(null);
	}

	public Saucer(final Frame owner) {
		super(owner);
		setMimeType("text/html:application/xhtml+xml");
		final javax.swing.text.EditorKit kit = new EditorKit();
		getEditorPane().setEditorKitForContentType("text/xml", kit);
		getEditorPane().setEditorKitForContentType("application/xml", kit);
	}

	@Override
	public Frame getFrame(final Frame owner) {
		return new Saucer(owner);
	}
}
