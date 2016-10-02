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
		setMimeType("application/xhtml+xml");
		getEditorPane().setEditorKitForContentType("application/xml", new EditorKit());
	}

	@Override
	public Frame getFrame(final Frame parent) {
		return new Saucer(parent);
	}
}
