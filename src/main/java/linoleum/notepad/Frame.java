package linoleum.notepad;

import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
import javax.swing.ImageIcon;

public class Frame extends linoleum.application.Frame {
	private final Notepad notepad = new Notepad(this);

	public Frame() {
		super(Notepad.resources.getString("Title"));
		getContentPane().add("Center", notepad);
		setJMenuBar(notepad.createMenubar());
		setSize(500, 400);
		setIcon(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Edit24.gif")));
		setName(Notepad.class.getSimpleName());
		setMimeType("text/*");
	}

	@Override
	public void setURI(final URI uri) {
		notepad.open(Paths.get(uri).toFile());
	}

	@Override
	public URI getURI() {
		final File file = notepad.getFile();
		return file == null?null:file.toURI();
	}

	@Override
	public Frame getFrame() {
		return new Frame();
	}

	@Override
	public void close() {
		notepad.close();
	}
}
