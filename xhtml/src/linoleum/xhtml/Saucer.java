package linoleum.xhtml;

import linoleum.application.Frame;
import linoleum.html.Browser;
import linoleum.html.EditorKit;

public class Saucer extends Browser {
	public Saucer() {
		this(null);
	}

	public Saucer(final Frame parent) {
		super(parent);
		setMimeType("text/html:application/xhtml+xml");
		final EditorKit kit = new EditorKit();
		getEditorPane().setEditorKitForContentType("text/xml", kit);
		getEditorPane().setEditorKitForContentType("application/xml", kit);
	}

	@Override
	public Frame getFrame(final Frame parent) {
		return new Saucer(parent);
	}
}
